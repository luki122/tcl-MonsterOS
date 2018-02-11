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

import java.util.regex.Pattern;

import android.content.Context;
import android.database.Cursor;
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
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
//import android.widget.ListView;
import android.widget.TextView;
import mst.widget.MstListView;

import com.android.mms.LogTag;
import com.android.mms.R;
import com.google.android.mms.MmsException;

//lichao add begin
import java.util.ArrayList;
import java.util.List;

import android.content.ContentUris;
import android.net.Uri;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.database.sqlite.SqliteWrapper;
//lichao add end
import com.google.android.mms.pdu.PduHeaders;

/**
 * The back-end data adapter of a message list.
 */
public class MessageListAdapter extends CursorAdapter {
    private static final String TAG = "MessageListAdapter";
    private static final boolean LOCAL_LOGV = false;

    static final String[] PROJECTION = new String[] {
        // TODO: should move this symbol into com.android.mms.telephony.Telephony.
        MmsSms.TYPE_DISCRIMINATOR_COLUMN,
        BaseColumns._ID,
        Conversations.THREAD_ID,
        // For SMS
        Sms.ADDRESS,
        Sms.BODY,
        Sms.SUBSCRIPTION_ID,
        Sms.DATE,
        Sms.DATE_SENT,
        Sms.READ,
        Sms.TYPE,
        Sms.STATUS,
        Sms.LOCKED,
        Sms.ERROR_CODE,
        // For MMS
        Mms.SUBJECT,
        Mms.SUBJECT_CHARSET,
        Mms.DATE,
        Mms.DATE_SENT,
        Mms.READ,
        Mms.MESSAGE_TYPE,
        Mms.MESSAGE_BOX,
        Mms.DELIVERY_REPORT,
        Mms.READ_REPORT,
        PendingMessages.ERROR_TYPE,
        Mms.LOCKED,
        Mms.STATUS,
        Mms.TEXT_ONLY
    };

    static final String[] MAILBOX_PROJECTION = new String[] {
        // TODO: should move this symbol into android.provider.Telephony.
        MmsSms.TYPE_DISCRIMINATOR_COLUMN,
        BaseColumns._ID,
        Conversations.THREAD_ID,
        // For SMS
        Sms.ADDRESS,
        Sms.BODY,
        Sms.SUBSCRIPTION_ID,
        Sms.DATE,
        Sms.DATE_SENT,
        Sms.READ,
        Sms.TYPE,
        Sms.STATUS,
        Sms.LOCKED,
        Sms.ERROR_CODE,
        // For MMS
        Mms.SUBJECT,
        Mms.SUBJECT_CHARSET,
        Mms.DATE,
        Mms.DATE_SENT,
        Mms.READ,
        Mms.MESSAGE_TYPE,
        Mms.MESSAGE_BOX,
        Mms.DELIVERY_REPORT,
        Mms.READ_REPORT,
        PendingMessages.ERROR_TYPE,
        Mms.LOCKED,
        Mms.STATUS,
        Mms.TEXT_ONLY,
        Mms.SUBSCRIPTION_ID,   // add for DSDS
        Threads.RECIPIENT_IDS  // add for obtaining address of MMS
    };

    static final String[] FORWARD_PROJECTION = new String[] {
        "'sms' AS " + MmsSms.TYPE_DISCRIMINATOR_COLUMN,
        BaseColumns._ID,
        Conversations.THREAD_ID,
        Sms.ADDRESS,
        Sms.BODY,
        Sms.SUBSCRIPTION_ID,
        Sms.DATE,
        Sms.DATE_SENT,
        Sms.READ,
        Sms.TYPE,
        Sms.STATUS,
        Sms.LOCKED,
        Sms.ERROR_CODE
    };

    // The indexes of the default columns which must be consistent
    // with above PROJECTION.
    static final int COLUMN_MSG_TYPE            = 0;
    static final int COLUMN_ID                  = 1;
    static final int COLUMN_THREAD_ID           = 2;
    static final int COLUMN_SMS_ADDRESS         = 3;
    static final int COLUMN_SMS_BODY            = 4;
    static final int COLUMN_SUB_ID              = 5;
    static final int COLUMN_SMS_DATE            = 6;
    static final int COLUMN_SMS_DATE_SENT       = 7;
    static final int COLUMN_SMS_READ            = 8;
    static final int COLUMN_SMS_TYPE            = 9;
    static final int COLUMN_SMS_STATUS          = 10;
    static final int COLUMN_SMS_LOCKED          = 11;
    static final int COLUMN_SMS_ERROR_CODE      = 12;
    static final int COLUMN_MMS_SUBJECT         = 13;
    static final int COLUMN_MMS_SUBJECT_CHARSET = 14;
    static final int COLUMN_MMS_DATE            = 15;
    static final int COLUMN_MMS_DATE_SENT       = 16;
    static final int COLUMN_MMS_READ            = 17;
    static final int COLUMN_MMS_MESSAGE_TYPE    = 18;
    static final int COLUMN_MMS_MESSAGE_BOX     = 19;
    static final int COLUMN_MMS_DELIVERY_REPORT = 20;
    static final int COLUMN_MMS_READ_REPORT     = 21;
    static final int COLUMN_MMS_ERROR_TYPE      = 22;
    static final int COLUMN_MMS_LOCKED          = 23;
    static final int COLUMN_MMS_STATUS          = 24;
    static final int COLUMN_MMS_TEXT_ONLY       = 25;
    static final int COLUMN_MMS_SUB_ID          = 26;
    static final int COLUMN_RECIPIENT_IDS       = 27;

    private static final int CACHE_SIZE         = 50;

    public static final int INCOMING_ITEM_TYPE_SMS = 0;
    public static final int OUTGOING_ITEM_TYPE_SMS = 1;
    public static final int INCOMING_ITEM_TYPE_MMS = 2;
    public static final int OUTGOING_ITEM_TYPE_MMS = 3;

    protected LayoutInflater mInflater;
    private final MstListView mListView;
    private final MessageItemCache mMessageItemCache;
    private final ColumnsMap mColumnsMap;
    private OnDataSetChangedListener mOnDataSetChangedListener;
    private Handler mMsgListItemHandler;
    private Pattern mHighlight;
    private Context mContext;
    private boolean mIsGroupConversation;
    private boolean mMultiChoiceMode = false;
    // for multi delete sim messages or forward merged message
    private int mMultiManageMode = MessageUtils.INVALID_MODE;

    private float mTextSize = 0;
    private SparseArray<Boolean> mDateShow = new SparseArray<>();

    public MessageListAdapter(
            Context context, Cursor c, MstListView listView,
            boolean useDefaultColumnsMap, Pattern highlight) {
        super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
        mContext = context;
        mHighlight = highlight;

        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mMessageItemCache = new MessageItemCache(CACHE_SIZE);
        mListView = listView;
        getDateShowList();

        if (useDefaultColumnsMap) {
            mColumnsMap = new ColumnsMap();
        } else {
            mColumnsMap = new ColumnsMap(c);
        }

        listView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                if (view instanceof MessageListItem) {
                    MessageListItem mli = (MessageListItem) view;
                     //Clear references to resources
                     mli.unbindMessageItem();
                }
            }
        });
    }

    public class MessageListViewHolder {
        MessageListItem headerView;
        TextView dateView;
        public MessageListViewHolder(View view) {
            headerView = (MessageListItem) view.findViewById(R.id.msg_list_item);
            dateView = (TextView) view.findViewById(R.id.date_view);
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        /*MessageListViewHolder viewHolder = (MessageListViewHolder) view.getTag();
        if(null == viewHolder){
            viewHolder = new MessageListViewHolder(view);
            view.setTag(viewHolder);
        }
        
        if (viewHolder.headerView != null) {
            //tangyisen
            int boxType = getItemViewType(cursor);
            boolean isIncoming = boxType == INCOMING_ITEM_TYPE_SMS ||boxType == INCOMING_ITEM_TYPE_MMS;
            //the same time , checkBox.setVisibility() in MessageListItem
            viewHolder.headerView.setIsCheckBoxMode(mIsCheckBoxMode);
            viewHolder.headerView.setIsSendItem(!isIncoming);
            viewHolder.headerView.resetItem();
            String type = cursor.getString(mColumnsMap.mColumnMsgType);
            long msgId = cursor.getLong(mColumnsMap.mColumnMsgId);

            MessageItem msgItem = getCachedMessageItem(type, msgId, cursor);
            if (msgItem != null) {
                int position = cursor.getPosition();
                if (mMultiManageMode != MessageUtils.INVALID_MODE) {
                    viewHolder.headerView.setManageSelectMode(mMultiManageMode);
                }
                //lichao move up for editmode on xiaoyuan rich bubble
                viewHolder.headerView.setMsgListItemHandler(mMsgListItemHandler);
                viewHolder.headerView.bindMessageItem(msgItem, mIsGroupConversation, position);
            }

            Uri uri = null;
            if ("sms".equals(type)) {
                uri = ContentUris.withAppendedId(
                        Sms.CONTENT_URI, cursor.getLong(COLUMN_ID));
            } else if ("mms".equals(type)) {
                uri = ContentUris.withAppendedId(
                        Mms.CONTENT_URI, cursor.getLong(COLUMN_ID));
            }
            if (null != uri && mSelectedUri.contains(uri)) {
                viewHolder.headerView.setChecked(true);
            } else {
                viewHolder.headerView.setChecked(false);
            }
        }

        boolean showDate = mDateShow.get(cursor.getPosition(), false);
        viewHolder.dateView.setVisibility(showDate ? View.VISIBLE : View.GONE);*/
        MessageListItem headerView = (MessageListItem) view.findViewById(R.id.msg_list_item);
        if (headerView != null) {
            //tangyisen
            int boxType = getItemViewType(cursor);
            boolean isIncoming = boxType == INCOMING_ITEM_TYPE_SMS ||boxType == INCOMING_ITEM_TYPE_MMS;
            //the same time , checkBox.setVisibility() in MessageListItem
            //headerView.setCheckEnable(mCheckBoxEnable);
            headerView.setIsCheckBoxMode(mIsCheckBoxMode);
            headerView.setIsSendItem(!isIncoming);
            headerView.resetItem();
            String type = cursor.getString(mColumnsMap.mColumnMsgType);
            long msgId = cursor.getLong(mColumnsMap.mColumnMsgId);

            MessageItem msgItem = getCachedMessageItem(type, msgId, cursor);
            if (msgItem != null) {
                int position = cursor.getPosition();
                if (mMultiManageMode != MessageUtils.INVALID_MODE) {
                    headerView.setManageSelectMode(mMultiManageMode);
                }
                headerView.setMsgListItemHandler(mMsgListItemHandler);
                headerView.bindMessageItem(msgItem, mIsGroupConversation, position);
            }

            Uri uri = null;
            if ("sms".equals(type)) {
                uri = ContentUris.withAppendedId(
                        Sms.CONTENT_URI, cursor.getLong(COLUMN_ID));
            } else if ("mms".equals(type)) {
                uri = ContentUris.withAppendedId(
                        Mms.CONTENT_URI, cursor.getLong(COLUMN_ID));
            }
            if (null != uri && mSelectedUri.contains(uri)) {
                headerView.markAsSelected(true);
            } else {
                headerView.markAsSelected(false);
            }
        }
        boolean showDate = mDateShow.get(cursor.getPosition(), false);
        headerView.showDateView(showDate);
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
        void onDataSetChanged(MessageListAdapter adapter);
        void onContentChanged(MessageListAdapter adapter);
    }

    public void setOnDataSetChangedListener(OnDataSetChangedListener l) {
        mOnDataSetChangedListener = l;
    }

    public void setMsgListItemHandler(Handler handler) {
        mMsgListItemHandler = handler;
    }

    public void setIsGroupConversation(boolean isGroup) {
        mIsGroupConversation = isGroup;
    }

    public void cancelBackgroundLoading() {
        mMessageItemCache.evictAll();   // causes entryRemoved to be called for each MessageItem
                                        // in the cache which causes us to cancel loading of
                                        // background pdu's and images.
    }

    public void setMultiChoiceMode(boolean isMultiChoiceMode) {
        mMultiChoiceMode = isMultiChoiceMode;
    }

    public void setMultiManageMode(int manageMode) {
        mMultiManageMode = manageMode;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        getDateShowList();
        if (LOCAL_LOGV) {
            Log.v(TAG, "MessageListAdapter.notifyDataSetChanged().");
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
        int boxType = getItemViewType(cursor);
        View view;
        view = mInflater.inflate((boxType == INCOMING_ITEM_TYPE_SMS || boxType == INCOMING_ITEM_TYPE_MMS)
            ? R.layout.message_list_item_recv_mst : R.layout.message_list_item_send_mst, parent, false);
        if (boxType == INCOMING_ITEM_TYPE_MMS || boxType == OUTGOING_ITEM_TYPE_MMS) {
            // We've got an mms item, pre-inflate the mms portion of the view
            view.findViewById(R.id.mms_layout_view_stub).setVisibility(View.VISIBLE);
        }
        return view;
    }

    public MessageItem getCachedMessageItem(String type, long msgId, Cursor c) {
        MessageItem item = mMessageItemCache.get(getKey(type, msgId));
        if (item == null && c != null && isCursorValid(c)) {
            try {
                item = new MessageItem(mContext, type, c, mColumnsMap, mHighlight);
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

    /* MessageListAdapter says that it contains four types of views. Really, it just contains
     * a single type, a MessageListItem. Depending upon whether the message is an incoming or
     * outgoing message, the avatar and text and other items are laid out either left or right
     * justified. That works fine for everything but the message text. When views are recycled,
     * there's a greater than zero chance that the right-justified text on outgoing messages
     * will remain left-justified. The best solution at this point is to tell the adapter we've
     * got two different types of views. That way we won't recycle views between the two types.
     * @see android.widget.BaseAdapter#getViewTypeCount()
     */
    @Override
    public int getViewTypeCount() {
        return 4;   // Incoming and outgoing messages, both sms and mms
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor)getItem(position);
        return getItemViewType(cursor);
    }

    private int getItemViewType(Cursor cursor) {
        String type = cursor.getString(mColumnsMap.mColumnMsgType);
        int boxId;
        if ("sms".equals(type)) {
            boxId = cursor.getInt(mColumnsMap.mColumnSmsType);
            // Note that messages from the SIM card all have a boxId of zero.
            return (boxId == TextBasedSmsColumns.MESSAGE_TYPE_INBOX ||
                    boxId == TextBasedSmsColumns.MESSAGE_TYPE_ALL) ?
                    INCOMING_ITEM_TYPE_SMS : OUTGOING_ITEM_TYPE_SMS;
        } else {
            boxId = cursor.getInt(mColumnsMap.mColumnMmsMessageBox);
            // Note that messages from the SIM card all have a boxId of zero: Mms.MESSAGE_BOX_ALL
            return (boxId == Mms.MESSAGE_BOX_INBOX || boxId == Mms.MESSAGE_BOX_ALL) ?
                    INCOMING_ITEM_TYPE_MMS : OUTGOING_ITEM_TYPE_MMS;
        }
    }

    public boolean hasSmsInConversation(Cursor cursor) {
        boolean hasSms = false;
        if (isCursorValid(cursor)) {
            if (cursor.moveToFirst()) {
                do {
                    String type = cursor.getString(mColumnsMap.mColumnMsgType);
                    if ("sms".equals(type)) {
                        hasSms = true;
                        break;
                    }
                } while (cursor.moveToNext());
                // Reset the position to 0
                cursor.moveToFirst();
            }
        }
        return hasSms;
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

    public int getPositionForItem(MessageItem item) {
        Cursor cursor = getCursor();
        int position = -1;
        if (isCursorValid(cursor)) {
            if (cursor.moveToFirst()) {
                do {
                    position ++;
                    long id = cursor.getLong(mRowIDColumn);
                    String type = cursor.getString(mColumnsMap.mColumnMsgType);
                    if (id == item.mMsgId && (type != null && type.equals(item.mType))) {
                        return position;
                    }
                } while (cursor.moveToNext());
            }
        }
        return position;
    }

    public static class ColumnsMap {
        public int mColumnMsgType;
        public int mColumnMsgId;
        //tangyisen begin
        public int mColumnSmsThreadId;
        //tangyisen end
        public int mColumnSmsAddress;
        public int mColumnSmsBody;
        public int mColumnSubId;
        public int mColumnSmsDate;
        public int mColumnSmsDateSent;
        public int mColumnSmsRead;
        public int mColumnSmsType;
        public int mColumnSmsStatus;
        public int mColumnSmsLocked;
        public int mColumnSmsErrorCode;
        public int mColumnMmsSubject;
        public int mColumnMmsSubjectCharset;
        public int mColumnMmsDate;
        public int mColumnMmsDateSent;
        public int mColumnMmsRead;
        public int mColumnMmsMessageType;
        public int mColumnMmsMessageBox;
        public int mColumnMmsDeliveryReport;
        public int mColumnMmsReadReport;
        public int mColumnMmsErrorType;
        public int mColumnMmsLocked;
        public int mColumnMmsStatus;
        public int mColumnMmsTextOnly;
        public int mColumnMmsSubId;
        public int mColumnRecipientIds;

        public ColumnsMap() {
            mColumnMsgType            = COLUMN_MSG_TYPE;
            mColumnMsgId              = COLUMN_ID;
            //tangyisen begin
            mColumnSmsThreadId           = COLUMN_THREAD_ID;
            //tangyisen end
            mColumnSmsAddress         = COLUMN_SMS_ADDRESS;
            mColumnSmsBody            = COLUMN_SMS_BODY;
            mColumnSubId              = COLUMN_SUB_ID;
            mColumnSmsDate            = COLUMN_SMS_DATE;
            mColumnSmsDateSent        = COLUMN_SMS_DATE_SENT;
            mColumnSmsType            = COLUMN_SMS_TYPE;
            mColumnSmsStatus          = COLUMN_SMS_STATUS;
            mColumnSmsLocked          = COLUMN_SMS_LOCKED;
            mColumnSmsErrorCode       = COLUMN_SMS_ERROR_CODE;
            mColumnMmsSubject         = COLUMN_MMS_SUBJECT;
            mColumnMmsSubjectCharset  = COLUMN_MMS_SUBJECT_CHARSET;
            mColumnMmsMessageType     = COLUMN_MMS_MESSAGE_TYPE;
            mColumnMmsMessageBox      = COLUMN_MMS_MESSAGE_BOX;
            mColumnMmsDeliveryReport  = COLUMN_MMS_DELIVERY_REPORT;
            mColumnMmsReadReport      = COLUMN_MMS_READ_REPORT;
            mColumnMmsErrorType       = COLUMN_MMS_ERROR_TYPE;
            mColumnMmsLocked          = COLUMN_MMS_LOCKED;
            mColumnMmsStatus          = COLUMN_MMS_STATUS;
            mColumnMmsTextOnly        = COLUMN_MMS_TEXT_ONLY;
            mColumnMmsSubId           = COLUMN_MMS_SUB_ID;
            mColumnRecipientIds       = COLUMN_RECIPIENT_IDS;
        }

        public ColumnsMap(Cursor cursor) {
            // Ignore all 'not found' exceptions since the custom columns
            // may be just a subset of the default columns.
            try {
                mColumnMsgType = cursor.getColumnIndexOrThrow(
                        MmsSms.TYPE_DISCRIMINATOR_COLUMN);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMsgId = cursor.getColumnIndexOrThrow(BaseColumns._ID);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            //tangyisen begin
            try {
                mColumnSmsThreadId = cursor.getColumnIndexOrThrow(Sms.THREAD_ID);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            //tangyisen end

            try {
                mColumnSmsAddress = cursor.getColumnIndexOrThrow(Sms.ADDRESS);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsBody = cursor.getColumnIndexOrThrow(Sms.BODY);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSubId = cursor.getColumnIndexOrThrow(Sms.SUBSCRIPTION_ID);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsDate = cursor.getColumnIndexOrThrow(Sms.DATE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsDateSent = cursor.getColumnIndexOrThrow(Sms.DATE_SENT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsType = cursor.getColumnIndexOrThrow(Sms.TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsStatus = cursor.getColumnIndexOrThrow(Sms.STATUS);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsLocked = cursor.getColumnIndexOrThrow(Sms.LOCKED);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnSmsErrorCode = cursor.getColumnIndexOrThrow(Sms.ERROR_CODE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsSubject = cursor.getColumnIndexOrThrow(Mms.SUBJECT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsSubjectCharset = cursor.getColumnIndexOrThrow(Mms.SUBJECT_CHARSET);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsMessageType = cursor.getColumnIndexOrThrow(Mms.MESSAGE_TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsMessageBox = cursor.getColumnIndexOrThrow(Mms.MESSAGE_BOX);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsDeliveryReport = cursor.getColumnIndexOrThrow(Mms.DELIVERY_REPORT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsReadReport = cursor.getColumnIndexOrThrow(Mms.READ_REPORT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsErrorType = cursor.getColumnIndexOrThrow(PendingMessages.ERROR_TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsLocked = cursor.getColumnIndexOrThrow(Mms.LOCKED);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsStatus = cursor.getColumnIndexOrThrow(Mms.STATUS);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsTextOnly = cursor.getColumnIndexOrThrow(Mms.TEXT_ONLY);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnMmsSubId = cursor.getColumnIndexOrThrow(Mms.SUBSCRIPTION_ID);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }

            try {
                mColumnRecipientIds = cursor.getColumnIndexOrThrow(Threads.RECIPIENT_IDS);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
        }
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
    
	//lichao add begin
	private boolean mIsCheckBoxMode = false;
	public void setIsCheckBoxMode(boolean isCheckBoxMode) {
        mIsCheckBoxMode = isCheckBoxMode;
    }
    public boolean getIsCheckBoxMode() {
        return mIsCheckBoxMode;
    }
    
    private ArrayList<Uri> mSelectedUri = new ArrayList<Uri>();
    

    public void setCheckList(ArrayList<Uri> l) {
        mSelectedUri = l;
    }

    private final static long INTERVAL_TIME = 5 * 60 * 1000;

    private void getDateShowList(/*Cursor cursor*/) {
        mDateShow.clear();
        Cursor cursor = getCursor();
        if(cursor == null) {
            return;
        }
        int oldPosition = cursor.getPosition();
        if(cursor.moveToFirst()) {
            mDateShow.put(0, true);
        } else {
            cursor.moveToPosition(oldPosition);
            return;
        }
        String type = cursor.getString(mColumnsMap.mColumnMsgType);
        long msgId = cursor.getLong(mColumnsMap.mColumnMsgId);
        MessageItem msgItem = getCachedMessageItem(type, msgId, cursor);
        long current = msgItem.mDate;
        while(cursor.moveToNext()) {
            type = cursor.getString(mColumnsMap.mColumnMsgType);
            msgId = cursor.getLong(mColumnsMap.mColumnMsgId);
            msgItem = getCachedMessageItem(type, msgId, cursor);
            if(/*(msgItem.isMms() && msgItem.isDownloaded()) || */msgItem.mDate - current  > INTERVAL_TIME) {
                mDateShow.put(cursor.getPosition(), true);
                current = msgItem.mDate;
            }
        }
        cursor.moveToPosition(oldPosition);
    }

    private final static long FAILED_INTERVAL_TIME = 1 * 1000;
    public List<MessageItem> getSendFailRecipients(MessageItem msgItem) {
        //Cursor cursor = getCursorForItem(msgItem);
        boolean isMms = msgItem.isMms();
        ArrayList<MessageItem> msgList = new ArrayList<MessageItem>();
        Cursor cursor = getCursor();
        int cursorCurrPosition = cursor.getPosition();;
        long date = msgItem.mDate;
        String body = msgItem.mBody;
        long threadId = msgItem.mThreadId;
        if(cursor.moveToFirst()) {
            do{
                String type = cursor.getString(mColumnsMap.mColumnMsgType);
                long msgId = cursor.getLong(mColumnsMap.mColumnMsgId);
                MessageItem msgItemtmp = getCachedMessageItem(type, msgId, cursor);
                //tangyisen add tmp for via mms
                if(msgItemtmp.isMms()) {
                    continue;
                }
                if(msgItemtmp.mBody.equals(body) && msgItemtmp.isFailedMessage()){
                    long time = Math.abs(msgItem.mFailedDate - msgItemtmp.mFailedDate);
                    if(Math.abs(msgItem.mFailedDate - msgItemtmp.mFailedDate) < FAILED_INTERVAL_TIME) {
                        msgList.add(msgItemtmp);
                    }
                }
            }while(cursor.moveToNext());
        }
        cursor.moveToPosition(cursorCurrPosition);
        return msgList;
    }

    public MessageItem getItemFromPosition(int position) {
        Cursor itemCursor = (Cursor)getItem(position);
        String type = itemCursor.getString(mColumnsMap.mColumnMsgType);
        long msgId = itemCursor.getLong(mColumnsMap.mColumnMsgId);
        MessageItem msgItemtmp = getCachedMessageItem(type, msgId, itemCursor);
        return msgItemtmp;
    }
}
