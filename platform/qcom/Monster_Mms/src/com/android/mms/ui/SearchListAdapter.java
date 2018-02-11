package com.android.mms.ui;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.telephony.PhoneNumberUtils;
import mst.provider.Telephony.CanonicalAddressesColumns;
import mst.provider.Telephony.Threads;
import mst.provider.Telephony.ThreadsColumns;

import cn.com.xy.sms.sdk.util.StringUtils;

import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.MmsApp;
import com.android.mms.ui.SearchActivity.TextViewSnippet;
import com.android.mms.R;

import java.util.List;

//this class add by lichao for Mms Search
public class SearchListAdapter extends CursorAdapter {

    private static final String TAG = "Mms/SearchListAdapter";
    private static final boolean DEBUG = false;

    private String mSearchString = "";

    private static final int VIEW_TYPE_TOTAL_COUNT = 3;
    public static final int VIEW_TYPE_INVALID = -1;
    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_THREAD = 1;
    public static final int VIEW_TYPE_MESSAGE = 2;

    public static final String HIGH_LIGHT_TEXT = "highlight";
    public static final String HIGH_LIGHT_PLACE = "highlight_place";
    /*
    public static final int HIGH_LIGHT_PLACE_INVALID = -1;
    public static final int HIGH_LIGHT_PLACE_TITLE = 1;
    public static final int HIGH_LIGHT_PLACE_CONTENT = 2;
    */

    public static int mPreDataType = VIEW_TYPE_INVALID;
    private static final int POSITION_INVALID = -1;
    int mFirstThreadPosition = POSITION_INVALID;
    int mFirstMessagePosition = POSITION_INVALID;

    public SearchListAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_TOTAL_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor)getItem(position);
        return getItemViewType(cursor);
    }

    private int getItemViewType(Cursor cursor) {
        int recipientIdsPos  = cursor.getColumnIndex(ThreadsColumns.RECIPIENT_IDS);
        //if(DEBUG) Log.d(TAG, "getItemViewType(), recipientIdsPos = " + recipientIdsPos);
        if(recipientIdsPos > 0){
            return VIEW_TYPE_THREAD;
        }else{
            return VIEW_TYPE_MESSAGE;
        }
    }

    public void changeCursor(Cursor cursor) {
        if(DEBUG) Log.d(TAG, "\n\n ----------------changeCursor() ----------------");
        mPreDataType = VIEW_TYPE_INVALID;
        mFirstThreadPosition = POSITION_INVALID;
        mFirstMessagePosition = POSITION_INVALID;
        Cursor old = swapCursor(cursor);
        //don't close here because mSearchCursorCache will use it after
        /*if (old != null) {
            old.close();
        }*/
    }

    //Recycling use
    public class ViewHolder {
        TextView category;
        ViewGroup searchItemClickGroup;
        TextViewSnippet title;
        TextViewSnippet subTitle;
        TextView date;
        ImageView simicon;
        public ViewHolder(View view) {
            category = (TextView)(view.findViewById(R.id.category_title));
            searchItemClickGroup = (ViewGroup)(view.findViewById(R.id.layout_search_item_click_group));
            title = (TextViewSnippet)(view.findViewById(R.id.search_item_title));
            subTitle = (TextViewSnippet)(view.findViewById(R.id.search_item_subtitle));
            date = (TextView)(view.findViewById(R.id.search_item_date));
            simicon = (ImageView)(view.findViewById(R.id.search_sim_indicator_icon));
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if(DEBUG) Log.d(TAG, "\n\n ----------------bindView() ----------------");

        int position = cursor.getPosition();
        if(DEBUG) Log.d(TAG, "bindView(), position = " + position);

        final int type = getItemViewType(position);
        if(DEBUG) Log.d(TAG, "bindView(), item type: " + type);

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if(null == viewHolder){
            if(DEBUG) Log.d(TAG, "bindView(), new ViewHolder");
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }

        long msgId = -1L;
        long threadId = -1L;
        String titleString = "";
        String bodyString = "";
        String dateString = "";
        String categoryString = "";
        Conversation conv = null;
        String canonicalAddress = "";
        int subId = -1;
        if(type == VIEW_TYPE_THREAD){
            if(type != mPreDataType && mFirstThreadPosition == POSITION_INVALID){
                mFirstThreadPosition = position;
            }
            int column_id = cursor.getColumnIndex(Threads._ID);//"_id"
            if(column_id >= 0){
                threadId = cursor.getLong(column_id);
            }
            if (DEBUG) Log.d(TAG, "bindView(), VIEW_TYPE_THREAD, threadId = " + threadId);

            String recipient_ids = cursor.getString(
                    cursor.getColumnIndex(ThreadsColumns.RECIPIENT_IDS));//"recipient_ids"
            ContactList recipients = ContactList.getByIds(recipient_ids, false);
            if (DEBUG) Log.d(TAG, "bindView(), recipients: " + recipients.serialize());
            titleString = SearchUtils.getRecipientsStrByContactList(recipients);

            int msg_count = cursor.getInt(cursor.getColumnIndex(ThreadsColumns.MESSAGE_COUNT));//"message_count"
            if(msg_count == 0){
                bodyString = context.getString(R.string.has_draft);
            }else if(msg_count > 0){
                bodyString = context.getString(R.string.mst_messages_count, msg_count);
            }

            long when = cursor.getLong(cursor.getColumnIndex(ThreadsColumns.DATE));//"date"
            dateString = MessageUtils.formatTimeStampStringForItem(context, when);

            subId = cursor.getInt(cursor.getColumnIndex(Threads.SUBSCRIPTION_ID));//"sub_id"
        }
        else if(type == VIEW_TYPE_MESSAGE){
            if(type != mPreDataType && mFirstMessagePosition == POSITION_INVALID){
                mFirstMessagePosition = position;
            }
            msgId = cursor.getLong(cursor.getColumnIndex("_id"));
            if(DEBUG) Log.d(TAG, "bindView(), msgId = " + msgId);
            threadId = cursor.getLong(cursor.getColumnIndex("thread_id"));
            String address = cursor.getString(cursor.getColumnIndex("address"));
            titleString = SearchUtils.getRecipientsStrByThreadId(context, threadId);
            if (TextUtils.isEmpty(titleString)) {
                titleString = SearchUtils.getNameAndNumberByAddress(address);
            }
            bodyString = cursor.getString(cursor.getColumnIndex("body"));
            long when = cursor.getLong(cursor.getColumnIndex("date"));
            dateString = MessageUtils.formatTimeStampStringForItem(context, when);
            subId = cursor.getInt(cursor.getColumnIndex("sub_id"));
        }
        //set mPreDataType value after used mPreDataType
        mPreDataType = type;

        if (position == mFirstThreadPosition) {
            //show category title
            viewHolder.category.setVisibility(View.VISIBLE);
            viewHolder.category.setText(context.getString(R.string.recipient_category));
        } else if (position == mFirstMessagePosition) {
            //show category title
            viewHolder.category.setVisibility(View.VISIBLE);
            viewHolder.category.setText(context.getString(R.string.message_category));
        } else {
            //hide category title
            viewHolder.category.setVisibility(View.GONE);
        }

        if(type == VIEW_TYPE_THREAD){
            viewHolder.title.setText(titleString, mSearchString);
            viewHolder.subTitle.setText(bodyString, "");
        } else if(type == VIEW_TYPE_MESSAGE){
            viewHolder.title.setText(titleString, "");
            viewHolder.subTitle.setText(bodyString, mSearchString);
        }

        viewHolder.date.setText(dateString);

        int slotId = -1;
        if (subId >= 0) {
            slotId = SubscriptionManager.getSlotId(subId);
        }
        if(DEBUG) Log.d(TAG, "bindView(), slotId = "+slotId);
        boolean isShowSimIcon = MmsApp.isCreateConversaitonIdBySim
                && MessageUtils.isTwoSimCardEnabled() && slotId >= 0;
        if (isShowSimIcon) {
            viewHolder.simicon.setVisibility(View.VISIBLE);
            Drawable mSimIndicatorIcon = MessageUtils.getMultiSimIcon(context, slotId);
            viewHolder.simicon.setImageDrawable(mSimIndicatorIcon);
        } else {
            viewHolder.simicon.setVisibility(View.GONE);
        }
        //end tangyisen

        final long threadId2 = threadId;
        final long msgId2 = msgId;
        // if the user touches the item then launch the compose message
        // activity with some extra parameters to highlight the search
        // results and scroll to the latest part of the conversation
        // that has a match.
        viewHolder.searchItemClickGroup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final Intent onClickIntent = new Intent(context, ComposeMessageActivity.class);
                onClickIntent.putExtra(ComposeMessageActivity.THREAD_ID, threadId2);
                onClickIntent.putExtra(HIGH_LIGHT_PLACE, type);
                onClickIntent.putExtra(HIGH_LIGHT_TEXT, mSearchString);
                onClickIntent.putExtra(ComposeMessageActivity.SELECT_ID, msgId2);
                context.startActivity(onClickIntent);
            }
        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if(DEBUG) Log.d(TAG, "\n\n ----------------newView() ----------------");
        //int itemType = getItemViewType(cursor);
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.search_item_mst2, parent, false);
        return v;
    }

    public String getSearchString() {
        return mSearchString;
    }

    public void setSearchString(String searchString) {
        if(null == searchString){
            mSearchString = "";
        }
        mSearchString = searchString;
    }

    public interface OnContentChangedListener {
        void onContentChanged(SearchListAdapter adapter);
    }

    private OnContentChangedListener mOnContentChangedListener;
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

}//end of SearchListAdapter
