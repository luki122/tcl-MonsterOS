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
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.telephony.PhoneNumberUtils;

import mst.provider.Telephony.Threads;
import mst.provider.Telephony.ThreadsColumns;

import cn.com.xy.sms.sdk.util.StringUtils;

import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.util.DraftCache;

import java.util.List;
import java.util.HashSet;

//this class add by lichao for Mms Search
public class AddBlackListAdapter extends CursorAdapter {

    private static final String TAG = "Mms/AddBlackListAdapter";
    private static final boolean DEBUG = false;
    private static final int ID             = 0;
    private static final int DATE           = 1;
    private static final int MESSAGE_COUNT  = 2;
    private static final int RECIPIENT_IDS  = 3;
    private static final int SNIPPET        = 4;
    private static final int SNIPPET_CS     = 5;
    private static final int READ           = 6;
    private static final int ERROR          = 7;
    private static final int HAS_ATTACHMENT = 8;

    public AddBlackListAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (DEBUG) Log.d(TAG, "\n\n ----------------newView() ----------------");
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.add_black_list_item, parent, false);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (DEBUG) Log.d(TAG, "\n\n ----------------bindView() ----------------");

        //int position = cursor.getPosition();
        //if (DEBUG) Log.d(TAG, "bindView(), position = " + position);

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (null == viewHolder) {
            if (DEBUG) Log.d(TAG, "bindView(), new ViewHolder");
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }
        int column_id = cursor.getColumnIndex(Threads._ID);//"_id"
        long threadId = -1L;
        if (column_id >= 0) {
            threadId = cursor.getLong(column_id);
        }
        if (DEBUG) Log.d(TAG, "bindView(), threadId = " + threadId);

        String recipient_ids = cursor.getString(
                cursor.getColumnIndex(ThreadsColumns.RECIPIENT_IDS));//"recipient_ids"
        ContactList recipients = ContactList.getByIds(recipient_ids, false);
        if (DEBUG) Log.d(TAG, "bindView(), recipients: " + recipients.serialize());
        String titleString = SearchUtils.getRecipientsStrByContactList(recipients);

//        int msg_count = cursor.getInt(cursor.getColumnIndex(ThreadsColumns.MESSAGE_COUNT));//"message_count"
//        String bodyString = "";
//        if (msg_count == 0) {
//            bodyString = context.getString(R.string.has_draft);
//        } else if (msg_count > 0) {
//            bodyString = context.getString(R.string.mst_messages_count, msg_count);
//        }
        boolean hasDraft = false;
        if (threadId > 0) {
            hasDraft = DraftCache.getInstance().hasDraft(threadId);
        }
        boolean hasError = false;
        int column_error = cursor.getColumnIndex(Threads.ERROR);//"error"
        if(column_error >= 0){
            hasError = ( cursor.getInt(column_error) != 0);
        }

        // Replace the snippet with a default value if it's empty.
        String snippet = MessageUtils.cleanseMmsSubject(context,
                MessageUtils.extractEncStrFromCursor(cursor, SNIPPET, SNIPPET_CS));
        if (DEBUG) Log.d(TAG, "bindView(), snippet = " + snippet);
        if (!TextUtils.isEmpty(snippet)) {
            if(!hasDraft && !hasError){
                viewHolder.subTitle.setText(snippet);
            }else{
                CharSequence bodyText = MessageUtils.formatSubject(context, snippet, hasDraft, hasError);
                viewHolder.subTitle.setText(bodyText);
            }
        } else {
            snippet = context.getString(R.string.no_subject_view);
            viewHolder.subTitle.setText(snippet);
        }

        long when = cursor.getLong(cursor.getColumnIndex(ThreadsColumns.DATE));//"date"
        String dateString = MessageUtils.formatTimeStampStringForItem(context, when);

        int subId = cursor.getInt(cursor.getColumnIndex(Threads.SUBSCRIPTION_ID));//"sub_id"

        viewHolder.checkBox.setChecked(mSelectedThreadId.contains(threadId));
        viewHolder.title.setText(titleString);
        viewHolder.date.setText(dateString);

        int slotId = -1;
        if (subId >= 0) {
            slotId = SubscriptionManager.getSlotId(subId);
        }
        if (DEBUG) Log.d(TAG, "bindView(), slotId = " + slotId);
        boolean isShowSimIcon = MmsApp.isCreateConversaitonIdBySim
                && MessageUtils.isTwoSimCardEnabled() && slotId >= 0;
        if (isShowSimIcon) {
            viewHolder.simicon.setVisibility(View.VISIBLE);
            Drawable mSimIndicatorIcon = MessageUtils.getMultiSimIcon(context, slotId);
            viewHolder.simicon.setImageDrawable(mSimIndicatorIcon);
        } else {
            viewHolder.simicon.setVisibility(View.GONE);
        }
    }

    public class ViewHolder {
        CheckBox checkBox;
        TextView title;
        TextView subTitle;
        TextView date;
        ImageView simicon;

        public ViewHolder(View view) {
            checkBox = (CheckBox) (view.findViewById(R.id.black_item_check_box));
            title = (TextView) (view.findViewById(R.id.black_item_title));
            subTitle = (TextView) (view.findViewById(R.id.black_item_subtitle));
            date = (TextView) (view.findViewById(R.id.black_item_date));
            simicon = (ImageView) (view.findViewById(R.id.black_sim_indicator_icon));
        }
    }

    @Override
    protected void onContentChanged() {
        if (mCursor != null && !mCursor.isClosed()) {
            if (mOnContentChangedListener != null) {
                mOnContentChangedListener.onContentChanged(this);
            }
        }
    }

    public interface OnContentChangedListener {
        void onContentChanged(AddBlackListAdapter adapter);
    }

    private OnContentChangedListener mOnContentChangedListener;

    public void setOnContentChangedListener(OnContentChangedListener l) {
        mOnContentChangedListener = l;
    }

    private boolean mCheckBoxEnable = false;

    public void setCheckBoxEnable(boolean flag) {
        mCheckBoxEnable = flag;
    }

    public boolean getCheckBoxEnable() {
        return mCheckBoxEnable;
    }

    private HashSet<Long> mSelectedThreadId = new HashSet<Long>();

    public void setCheckList(HashSet<Long> l) {
        mSelectedThreadId = l;
    }

}//end of AddBlackListAdapter
