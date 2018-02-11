/*
 * Copyright (C) 2010-2014, The Linux Foundation. All rights reserved.
 * Not a Contribution.
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.ui;

import java.util.ArrayList;
import java.util.regex.Pattern;

import mst.widget.SliderLayout;
import mst.widget.SliderLayout.SwipeListener;
import mst.widget.SliderView;
//import android.graphics.Color;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;
import mst.provider.Telephony.Mms;
import mst.provider.Telephony.MmsSms;
import mst.provider.Telephony.MmsSms.PendingMessages;
import mst.provider.Telephony.Sms;
import mst.provider.Telephony.Sms.Conversations;
import mst.provider.Telephony.Threads;
import mst.provider.Telephony.TextBasedSmsColumns;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
//import android.widget.ListView;
import android.widget.TextView;
import mst.widget.MstListView;

import com.android.mms.LogTag;
import com.android.mms.R;
import com.android.mms.ui.MessageListAdapter.ColumnsMap;
import com.google.android.mms.MmsException;
import android.database.sqlite.SqliteWrapper;

/**
 * The back-end data adapter of a message list.
 */
public class SimMessageListAdapter_Mst extends CursorAdapter {
    private static final String TAG = "SimMessageListAdapter_Mst";
    private static final boolean LOCAL_LOGV = false;
    private static final boolean DEBUG = false;

    private static final int CACHE_SIZE         = 50;

    protected LayoutInflater mInflater;
    private final MstListView mListView;
    private final MessageItemCache mMessageItemCache;
    private final ColumnsMap mColumnsMap;
    private OnDataSetChangedListener mOnDataSetChangedListener;
    private Pattern mHighlight;
    private Context mContext;
    private boolean mIsGroupConversation;

    private float mTextSize = 0;

    public static final int SLIDER_BTN_POSITION_DELETE = 1;

    public SimMessageListAdapter_Mst(
            Context context, Cursor c, MstListView listView,
            boolean useDefaultColumnsMap, Pattern highlight) {
        super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
        mContext = context;
        mHighlight = highlight;

        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mMessageItemCache = new MessageItemCache(CACHE_SIZE);
        mListView = listView;

        if (useDefaultColumnsMap) {
            mColumnsMap = new ColumnsMap();
        } else {
            mColumnsMap = new ColumnsMap(c);
        }
    }

    public class SimMessageListViewHolder {
        SimMessageListItem headerView;
        SliderView sliderView;
        public SimMessageListViewHolder(View view) {
            headerView = (SimMessageListItem) view.findViewById(R.id.sim_msg_list_item);
            sliderView = (SliderView) view.findViewById(R.id.slider_view1);
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        SimMessageListViewHolder viewHolder = (SimMessageListViewHolder) view.getTag();
        if(null == viewHolder){
            viewHolder = new SimMessageListViewHolder(view);
            view.setTag(viewHolder);
        }

        final Uri uri = getUriStrByCursor(cursor);
        viewHolder.headerView.setCheckBoxEnable(mCheckBoxEnable);
        viewHolder.headerView.setChecked(mSelectedUri.contains(uri));

        String type = cursor.getString(mColumnsMap.mColumnMsgType);
        long msgId = cursor.getLong(mColumnsMap.mColumnMsgId);
        MessageItem msgItem = getCachedMessageItem(type, msgId, cursor);
        if (msgItem != null) {
            int position = cursor.getPosition();
            viewHolder.headerView.bindSimMessage(msgItem, mIsGroupConversation, position);
        }

        final SliderView sliderView = viewHolder.sliderView;
        if(null != sliderView){
            if (DEBUG) Log.d(TAG, "bindView, sliderView.setTag: uri = " + uri);
            sliderView.setTag(R.id.slider_tag, uri.toString());
            sliderView.setOnSliderButtonClickListener(onSliderButtonLickListener);
            sliderView.setSwipeListener(new SwipeListener(){
                public void onClosed(SliderLayout view){
                    currentSliderView=null;
                }
                public void onOpened(SliderLayout view){
                    currentSliderView=sliderView;
                }
                public void onSlide(SliderLayout view, float slideOffset){
                }
            });
            sliderView.setLockDrag(mCheckBoxEnable);
            if(sliderView.isOpened()){
                sliderView.close(false);
            }
        }

        //View mAvatar = view.findViewById(R.id.avatar);
        //mAvatar.setVisibility(View.GONE);
        //lichao modify in 2016-08-09 end
    }

    @Override
    public long getItemId(int position) {
        if (getCursor() != null) {
            getCursor().moveToPosition(position);
            return position;
        }
        return 0;
    }

    public interface OnDataSetChangedListener {
        void onDataSetChanged(SimMessageListAdapter_Mst adapter);
        void onContentChanged(SimMessageListAdapter_Mst adapter);
    }

    public void setOnDataSetChangedListener(OnDataSetChangedListener l) {
        mOnDataSetChangedListener = l;
    }

    public void setIsGroupConversation(boolean isGroup) {
        mIsGroupConversation = isGroup;
    }

    public void cancelBackgroundLoading() {
        mMessageItemCache.evictAll();   // causes entryRemoved to be called for each MessageItem
                                        // in the cache which causes us to cancel loading of
                                        // background pdu's and images.
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (LOCAL_LOGV) {
            Log.v(TAG, "SimMessageListAdapter_Mst.notifyDataSetChanged().");
        }

        mMessageItemCache.evictAll();

        if (mOnDataSetChangedListener != null) {
            mOnDataSetChangedListener.onDataSetChanged(this);
        }
    }

    @Override
    protected void onContentChanged() {
        if (getCursor() != null && !getCursor().isClosed()) {
            if (mOnDataSetChangedListener != null) {
                mOnDataSetChangedListener.onContentChanged(this);
            }
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        SliderView sliderView = (SliderView) LayoutInflater.from(context)
                .inflate(R.layout.sim_message_list_item_slideview, null);
        sliderView.addTextButton(SLIDER_BTN_POSITION_DELETE, context.getString(R.string.delete_message));
        return sliderView;
    }

    public MessageItem getCachedMessageItem(String type, long msgId, Cursor c) {
        MessageItem item = mMessageItemCache.get(getKey(type, msgId));
        if (item == null && c != null && isCursorValid(c)) {
            try {
                item = new MessageItem(mContext, type, c, mColumnsMap, mHighlight, false);
                mMessageItemCache.put(getKey(item.mType, item.mMsgId), item);
            } catch (MmsException e) {
                Log.e(TAG, "getCachedMessageItem: ", e);
            }
        }
        return item;
    }

    private boolean isCursorValid(Cursor cursor) {
        // Check whether the cursor is valid or not.
        if (cursor == null || cursor.isClosed() || cursor.isBeforeFirst() || cursor.isAfterLast()) {
            return false;
        }
        return true;
    }

    private static long getKey(String type, long id) {
        if (type.equals("mms")) {
            return -id;
        } else {
            return id;
        }
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    public Cursor getCursorForItem(MessageItem item) {
        Cursor cursor = getCursor();
        if (isCursorValid(cursor)) {
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(mRowIDColumn);
                    String type = cursor.getString(mColumnsMap.mColumnMsgType);
                    if (id == item.mMsgId && (type != null && type.equals(item.mType))) {
                        return cursor;
                    }
                } while (cursor.moveToNext());
            }
        }
        return null;
    }


    private static class MessageItemCache extends LruCache<Long, MessageItem> {
        public MessageItemCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected void entryRemoved(boolean evicted, Long key,
                MessageItem oldValue, MessageItem newValue) {
            oldValue.cancelPduLoading();
        }
    }

    public void setTextSize(float size) {
        mTextSize = size;
    }
    
    private ArrayList<Uri> mSelectedUri = new ArrayList<Uri>();
    

	public void setCheckList(ArrayList<Uri> l) {
		mSelectedUri = l;
    }
    
	private boolean mCheckBoxEnable = false;
	
	public void setCheckBoxEnable(boolean flag) {
        mCheckBoxEnable = flag;
    }
    
    public boolean getCheckBoxEnable() {
        return mCheckBoxEnable;
    }
    
	
    private Uri getUriStrByCursor(Cursor cursor) {
        String messageIndexString = cursor.getString(cursor.getColumnIndexOrThrow("index_on_icc"));
        return mIccUri.buildUpon().appendPath(messageIndexString).build();
    }
    
    private Uri mIccUri;
    public void setIccUri(Uri uri) {
    	mIccUri = uri;
    }

    //lichao add in 2016-09-20 begin
    private SliderView.OnSliderButtonLickListener onSliderButtonLickListener;

    public void setOnSliderButtonLickListener(
            SliderView.OnSliderButtonLickListener onSliderButtonLickListener) {
        this.onSliderButtonLickListener = onSliderButtonLickListener;
    }

    private SliderView currentSliderView;
    public SliderView getCurrentSliderView() {
        return currentSliderView;
    }
    public void setCurrentSliderView(SliderView currentSliderView1) {
        currentSliderView = currentSliderView1;
    }
    //lichao add in 2016-09-20 end
    
}
