/*
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

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;

import com.android.mms.LogTag;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.data.Conversation;

//lichao add begin
import android.graphics.drawable.Drawable;
import mst.provider.Telephony.Threads;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.telephony.SubscriptionManager;

import com.android.mms.MmsApp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mst.widget.SliderLayout;
import mst.widget.SliderLayout.SwipeListener;
import mst.widget.SliderView;
//import android.graphics.Color;
//lichao add end

/**
 * The back-end data adapter for ConversationList.
 */
public class ConversationListAdapter extends CursorAdapter implements AbsListView.RecyclerListener {
    private static final String TAG = "Mms/ConversationListAdapter";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    private OnContentChangedListener mOnContentChangedListener;
    //XiaoYuan SDK add
    public boolean SCROLL_STATE_FLING = false;

    public static final int SLIDER_BTN_POSITION_DELETE = 1;
    //begin tangyisen
    //public static HashMap<Long, Integer> mThreadSubId = new HashMap<Long, Integer>();
    //end tangyisen

    public ConversationListAdapter(Context context, Cursor cursor) {
        super(context, cursor, false /* auto-requery */);
        //mMessageItemCache = new MessageItemCache(CACHE_SIZE);
    }

    public class ConversationListViewHolder {
        ConversationListItem headerView;
        SliderView sliderView;
        public ConversationListViewHolder(View view) {
            headerView = (ConversationListItem) view.findViewById(R.id.conv_list_item);
            sliderView = (SliderView) view.findViewById(R.id.slider_view1);
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if(DEBUG) Log.v(TAG, "\n\n == bindView ==");
        //if (!(view instanceof ConversationListItem)) {
        //    Log.e(TAG, "Unexpected bound view: " + view);
        //    return;
        //}

        ConversationListViewHolder viewHolder = (ConversationListViewHolder) view.getTag();
        if(null == viewHolder){
            viewHolder = new ConversationListViewHolder(view);
            view.setTag(viewHolder);
        }

        viewHolder.headerView.setCheckBoxEnable(mCheckBoxEnable);

        viewHolder.headerView.setScreenWidthDip(mScreenWidthDip);

        //XiaoYuan SDK add
        viewHolder.headerView.setScrolling(SCROLL_STATE_FLING);

        Conversation conv = Conversation.from(context, cursor);
        if(conv != null){
            viewHolder.headerView.bindConversation(context, conv);
        }

        final SliderView sliderView = viewHolder.sliderView;
        if(null != sliderView){
            long threadId = conv.getThreadId();
            if (DEBUG) Log.d(TAG, "bindView, sliderView.setTag: threadId = " + threadId);
            sliderView.setTag(R.id.slider_tag, String.valueOf(threadId));
            sliderView.setOnSliderButtonClickListener(onSliderButtonLickListener);
            sliderView.setSwipeListener(new SwipeListener(){
                public void onClosed(SliderLayout view){
                    currentSliderView=null;
                }
                public void onOpened(SliderLayout view){
                    currentSliderView = sliderView;
                }
                public void onSlide(SliderLayout view, float slideOffset){
                }
            });
            sliderView.setLockDrag(mCheckBoxEnable || mIsSearchMode || !MmsConfig.isSmsEnabled(context));
            if(sliderView.isOpened()){
                sliderView.close(false);
            }

        }
    }

    @Override
    public void onMovedToScrapHeap(View view) {
        if(DEBUG) Log.v(TAG, " == onMovedToScrapHeap ==");
	    //lichao modify begin
        //ConversationListItem headerView = (ConversationListItem)view;
        ConversationListItem headerView = (ConversationListItem) view.findViewById(R.id.conv_list_item);
        if(headerView != null) {
            //only ResetCheckBox one time in the lifecycle of ConversationListAdapter
        	headerView.unbindConversation(!mCheckBoxEnable /*resetCheckBox*/);
        }
		//lichao modify end
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if(DEBUG) Log.v(TAG, "\n\n == newView ==");
        //return LayoutInflater.from(context).inflate(R.layout.conversation_list_item, parent, false);

        SliderView sliderView = (SliderView) LayoutInflater.from(context)
                .inflate(R.layout.conversation_list_item_slideview, null);
        sliderView.addTextButton(SLIDER_BTN_POSITION_DELETE, context.getString(R.string.delete_message));
        return sliderView;
    }

    public interface OnContentChangedListener {
        void onContentChanged(ConversationListAdapter adapter);
    }

    public void setOnContentChangedListener(OnContentChangedListener l) {
        mOnContentChangedListener = l;
    }

    @Override
    protected void onContentChanged() {
        if (mCursor != null && !mCursor.isClosed()) {
            if (mOnContentChangedListener != null) {
                mOnContentChangedListener.onContentChanged(this);
            }
        }
    }

    public void uncheckAll() {
        int count = getCount();
        for (int i = 0; i < count; i++) {
            Cursor cursor = (Cursor)getItem(i);
            Conversation conv = Conversation.from(mContext, cursor);
            conv.setIsChecked(false);
        }
    }
    
	//lichao add begin
	private boolean mCheckBoxEnable = false;
	public void setCheckBoxEnable(boolean flag) {
        mCheckBoxEnable = flag;
    }
    public boolean getCheckBoxEnable() {
        return mCheckBoxEnable;
    }

    private boolean mIsSearchMode = false;
    public void setSearchMode(boolean flag) {
        mIsSearchMode = flag;
    }
    public boolean getSearchMode() {
        return mIsSearchMode;
    }

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

    private int mScreenWidthDip = 0;
    public void setScreenWidthDip(int screenWidthDip) {
        mScreenWidthDip = screenWidthDip;
    }
	//lichao add end
    
}
