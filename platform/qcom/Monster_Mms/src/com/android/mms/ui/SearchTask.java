package com.android.mms.ui;

import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;

import mst.provider.Telephony;
import mst.provider.Telephony.CanonicalAddressesColumns;
import mst.provider.Telephony.Mms;
import mst.provider.Telephony.Threads;
import mst.provider.Telephony.ThreadsColumns;

import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.data.SearchListItemData;
import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.ui.MessageUtils;


public class SearchTask extends AsyncTask<Void, Void, List<SearchListItemData>> {

    private static final String TAG = "Mms/SearchTask";
    private static final boolean DEBUG = true;

    private String mSearchString;
    private String mSearchString_bg;
    private boolean mIsCancelled = false;
    List<SearchListItemData> mSearchedDatas = null;
    List<SearchListItemData> mSearchedDatas_bg = null;
    /*
    public static final int DATA_TYPE_UNKNOWN = -1;
    public static final int DATA_TYPE_HEADER = 0;
    public static final int DATA_TYPE_THREAD = 1;
    public static final int DATA_TYPE_MESSAGE = 2;
     */
    long mPreDataType = SearchListItemData.DATA_TYPE_UNKNOWN;
    private SearchListItemData recipientHeaderData;
    private SearchListItemData messageHeaderData;
    private boolean mLastTaskFinished = true;
    private Context mContext;

    private List<String> mHandledRecipientIds = null;

    public SearchTask(Context context) {
        mContext = context;
        mIsCancelled = false;
        mLastTaskFinished = false;

        String recipient_str = mContext.getString(R.string.recipient_category);
        /*
        long rowId, long threadId, String titleValue, String subTitleValue,
                              String dateValue, int subId, int dataType, String searchString
         */
        recipientHeaderData = new SearchListItemData(-1L, -1L, recipient_str, null,
                null, -1, SearchListItemData.DATA_TYPE_HEADER, null);

        String message_str = mContext.getString(R.string.message_category);
        messageHeaderData = new SearchListItemData(-1L, -1L, message_str, null,
                null, -1, SearchListItemData.DATA_TYPE_HEADER, null);
    }

    protected List<SearchListItemData> doInBackground(Void... none) {
        if (DEBUG) Log.d(TAG, "\n\n SearchTask, doInBackground(), mSearchString: "+mSearchString);

        if (isCancelled()) {
            return null;
        }
        mSearchString_bg = mSearchString;
        return doSearchInBackground(mContext, mSearchString_bg);
    }

    protected void onPostExecute(List<SearchListItemData> searchedDatas) {
        if (DEBUG) Log.d(TAG, "onPostExecute begin");
        if (!mSearchString_bg.equals(mSearchString)) {
            mLastTaskFinished = true;
            if (DEBUG) Log.d(TAG, "onPostExecute, mSearchString changed, return");
            return;
        }
        //lichao add for solve bug: Long press the delete key to cause ANR
        if (mIsCancelled || isCancelled()) {
            mLastTaskFinished = true;
            if (DEBUG) Log.d(TAG, "onPostExecute, IsCancelled, return");
            return;
        }
        setSearchedDatas(searchedDatas);
        if (null != mOnItemDataChanged) {
            mOnItemDataChanged.onItemDataChanged();
        }
        mLastTaskFinished = true;
        if (DEBUG) Log.d(TAG, "onPostExecute end");
    }

    public void cancel() {
        super.cancel(true);
        // Use a flag to keep track of whether the {@link AsyncTask} was cancelled or not in
        // order to ensure onPostExecute() is not executed after the cancel request. The flag is
        // necessary because {@link AsyncTask} still calls onPostExecute() if the cancel request
        // came after the worker thread was finished.
        mIsCancelled = true;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        //mTaskStatus = TaskStatus.CANCELED;
        mLastTaskFinished = true;
    }

    public String getSearchString() {
        return mSearchString;
    }

    public void setSearchString(String searchString) {
        this.mSearchString = searchString;
    }

    public interface OnItemDataChanged {
        void onItemDataChanged();
    }

    private OnItemDataChanged mOnItemDataChanged = null;

    public void setOnItemDataChanged(OnItemDataChanged onItemDataChanged) {
        this.mOnItemDataChanged = onItemDataChanged;
    }

    private boolean mItemDataChanged = true;

    private boolean isItemDataChanged() {
        return mItemDataChanged;
    }

    private void setItemDataChanged(boolean itemDataChanged) {
        mItemDataChanged = itemDataChanged;
    }

    private void setSearchedDatas(List<SearchListItemData> searchedDatas) {
        mSearchedDatas = searchedDatas;
    }

    public List<SearchListItemData> getSearchedDatas() {
        return mSearchedDatas;
    }

    public boolean getLastTaskFinished() {
        return mLastTaskFinished;
    }

    private List<SearchListItemData> doSearchInBackground(Context context, String queryText) {
        if (DEBUG) Log.d(TAG, "doSearchInBackground begin");
        if (null == mSearchedDatas_bg) {
            mSearchedDatas_bg = new ArrayList<SearchListItemData>();
        } else {
            mSearchedDatas_bg.clear();
        }
        /*
        java.util.Iterator it = SearchListItemData.mSearchDatasCache.entrySet().iterator();
        while (it.hasNext()){
            java.util.Map.Entry entry = (java.util.Map.Entry) it.next();
            Log.d(TAG, "doSearchInBackground, " + entry.getKey() + "-->" + entry.getValue());
        }
        */
        List<SearchListItemData> cacheItemDatas = SearchListItemData.mSearchDatasCache.get(queryText);
        if (cacheItemDatas != null) {
            if (DEBUG) Log.d(TAG, "doSearchInBackground, get cacheItemDatas, return");
            mSearchedDatas_bg = cacheItemDatas;
            return mSearchedDatas_bg;
        }

        mPreDataType = SearchListItemData.DATA_TYPE_UNKNOWN;
        try {
            boolean isNumber = MessageUtils.isNumeric(queryText);
            if (isNumber) {
                Cursor cursor_number = queryCanonicalAddressesCusor(context, queryText);
                Cursor cursor_number2 = cursor_number;
                try {
                    //查询出了共用号码字符串的收信人（不同号码）的短信，但是没有查询出群发短信。
                    //群发短信：只是收信人ID保存在一个thread里，短信的收信人都只1个人。
                    parseSingleAddressesCursor(context, cursor_number, queryText);
                    //获取群发信息效率太低
                    parseMultiAddressesCursor(context, cursor_number2, queryText);
                } finally {
                    cursor_number.close();
                    cursor_number2.close();
                }
            }

            List<String> numbersList = MessageUtils.getNumbersListByName(context, queryText);
            //numbersList = [15608082624, +8618682301353]
            if (DEBUG) Log.d(TAG, "doSearchInBackground, numbersList = " + numbersList);
            Cursor cursorName = null;
            String formatedNumber = null;
            for (String number : numbersList) {
                //if(DEBUG) Log.d(TAG, "doSearchInBackground, number = " + number);
                if (!TextUtils.isEmpty(number)) {
                    formatedNumber = PhoneNumberUtils.formatNumber(number, number,
                            MmsApp.getApplication().getCurrentCountryIso());
                    //if(DEBUG) Log.d(TAG, "doSearchInBackground, formatedNumber = " + formatedNumber);
                    if (!TextUtils.isEmpty(formatedNumber)) {
                        //查询出了共用姓名字符串的收信人（不同号码）的短信，但是没有查询出群发短信。
                        //群发短信：只是recipient_id保存在一个thread表的recipient_ids里。
                        //短信表里群发短信都是一条一条分开存储的，每一条短信的address都只1个号码。
                        cursorName = queryCanonicalAddressesCusor(context, formatedNumber);
                        Cursor cursorName2 = cursorName;
                        try {
                            //通过上面查出的cursor, 可以获取到canonical-addresses表中所有跟formatedNumber匹配的address(只1个号码)
                            parseSingleAddressesCursor(context, cursorName, queryText);
                            //获取群发信息效率太低
                            parseMultiAddressesCursor(context, cursorName2, queryText);
                        } finally {
                            cursorName.close();
                            cursorName2.close();
                        }
                    }
                }
            }

            //SEARCH_URI = Uri.parse("content://mms-sms/search")
            Uri contentUri = Telephony.MmsSms.SEARCH_URI.buildUpon()
                    .appendQueryParameter("pattern", queryText).build();
            //can't set projection here. It use getTextSearchQuery() to set projection in MmsSmsProvider.java
            Cursor cursor = context.getContentResolver().query(contentUri, null, null, null, null);
            parseMessageCursor(context, cursor, queryText);
        } catch (Exception e) {
            Log.e(TAG, "doSearchInBackground, Exception: " + e);
            return null;
        }
        //if (DEBUG) Log.d(TAG, "doSearchInBackground, mSearchDatasCache.put<"+queryText
        //        +"><<"+mSearchedDatas_bg.toString()+">>");
        SearchListItemData.mSearchDatasCache.put(queryText, mSearchedDatas_bg);
        if (DEBUG) Log.d(TAG, "doSearchInBackground end");
        return mSearchedDatas_bg;
    }

    private void parseMessageCursor(Context context, Cursor cursor, String queryText) {
        if (cursor.getCount() <= 0) {
            return;
        }
        try {
            int currentDataType = SearchListItemData.DATA_TYPE_MESSAGE;
            SearchListItemData messageData;
            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(cursor.getColumnIndex("_id"));
                if (DEBUG) Log.d(TAG, "parseMessageCursor(), rowId: " + rowId);

                long threadId = cursor.getLong(cursor.getColumnIndex("thread_id"));
                if(threadId <= 0){
                    continue;
                }

                String address = cursor.getString(cursor.getColumnIndex("address"));
                if (DEBUG) Log.d(TAG, " parseMessageCursor(), address: " + address);
                if(TextUtils.isEmpty(address)){
                    continue;
                }

                String titleString = getRecipientsStrByThreadId(context, threadId);
                if(TextUtils.isEmpty(titleString)){
                    titleString = getFullyRecipientsStrByAddress(address);
                }
                if (DEBUG) Log.d(TAG, " parseMessageCursor(), titleString: " + titleString);

                String bodyString = cursor.getString(cursor.getColumnIndex("body"));
                if(TextUtils.isEmpty(bodyString)){
                    continue;
                }

                long when = cursor.getLong(cursor.getColumnIndex("date"));
                String dateString = MessageUtils.formatTimeStampString(context, when);

                int subId = cursor.getInt(cursor.getColumnIndex("sub_id"));

                if (mPreDataType != currentDataType) {
                    mPreDataType = currentDataType;
                    mSearchedDatas_bg.add(messageHeaderData);
                }
                messageData = new SearchListItemData(rowId, threadId, titleString, bodyString,
                        dateString, subId, currentDataType, queryText);
                mSearchedDatas_bg.add(messageData);
            }
        } finally {
            cursor.close();
        }
    }

    private void parseMultiAddressesCursor(Context context, Cursor cursor, String queryText) {
        if (cursor.getCount() <= 0) {
            return;
        }
//        try {
            while (cursor.moveToNext()) {
                long recipientId = cursor.getLong(cursor.getColumnIndex("_id"));
                //if (DEBUG) Log.d(TAG, "parseMultiAddressesCursor(), recipientId = " + recipientId);
                //handle both single recipient and multi recipients thread
                getGroupMessageByRecipientId(context, recipientId, queryText);
            }
//        } finally {
//            cursor.close();
//        }
    }

    private void parseSingleAddressesCursor(Context context, Cursor cursor, String queryText) {
        if (cursor.getCount() <= 0) {
            return;
        }
//        try {
            while (cursor.moveToNext()) {
                //handle single recipients thread
                int canonicalAddressPos = cursor.getColumnIndex(CanonicalAddressesColumns.ADDRESS);
                if(canonicalAddressPos < 0){
                    continue;
                }
                String canonicalAddress = cursor.getString(canonicalAddressPos);
                if (DEBUG) Log.d(TAG, "parseSingleAddressesCursor(), canonicalAddress: " + canonicalAddress);
                if(TextUtils.isEmpty(canonicalAddress)){
                    continue;
                }
                //没有指定subid，这里实际应该返回Conversation数组
                Conversation conv = getConversationBySingleAddress(context, canonicalAddress);
                if(null == conv){
                    continue;
                }
                addConversationItem(context, conv, queryText);
            }
//        } finally {
//            cursor.close();
//        }
    }

    private void addConversationItem(Context context, Conversation conv, String queryText) {
        if (conv == null) {
            return;
        }
        long threadId = conv.getThreadId();
        if(threadId <= 0){
            return;
        }
        //if (DEBUG) Log.d(TAG, "addConversationItem(), threadId: " + threadId);
        String titleString = getRecipientsStrByContactList(conv.getRecipients());
        if(TextUtils.isEmpty(titleString)){
            return;
        }
        String bodyString = context.getString(R.string.mst_messages_count, conv.getMessageCount());
        //donot show dateView
        //String dateString = MessageUtils.formatTimeStampString(context, conv.getDate());
        int subId = conv.getSubId();
        int currentDataType = SearchListItemData.DATA_TYPE_THREAD;
        SearchListItemData messageData = new SearchListItemData(-1L/*rowId*/, threadId, titleString, bodyString,
                null/*dateValue*/, subId, currentDataType, queryText);
        if (mPreDataType != currentDataType) {
            mPreDataType = currentDataType;
            mSearchedDatas_bg.add(recipientHeaderData);
        }
        mSearchedDatas_bg.add(messageData);
    }

    private Conversation getConversationBySingleAddress(Context context, String address) {
        if (TextUtils.isEmpty(address)) {
            return null;
        }
        Conversation conv = null;
        //if (DEBUG) Log.d(TAG, "getConversationBySingleAddress(), address: " + address);
        //搜索短信内容得到群发信息的多个收件人,即使是单个收件人也可处理
        //String semiSepNumbers = PhoneNumberUtils.replaceUnicodeDigits(address).replace(',', ';');
        //if (DEBUG) Log.d(TAG, "getConversationBySingleAddress(), semiSepNumbers: " + semiSepNumbers);

        //canBlock改为true之后回去遍历整个联系人数据库，会非常慢，270条联系人大概要1分钟
        ContactList recipients = ContactList.getByNumbers(address, false/*canBlock*/, false /*replaceNumber*/);
        if (DEBUG) Log.d(TAG, "getConversationBySingleAddress(), recipients: " + recipients.serialize());

        if (null == recipients) {
            return null;
        }

        //Conversation.get()调用了getOrCreateThreadId()方法所以这里不要用
        //conv = Conversation.get(context, recipients, true/*allowQuery*/);
        conv = Conversation.query(context, recipients, true/*allowQuery*/);
        return conv;
    }

    //handle both single recipient and multi recipients thread
    public void getGroupMessageByRecipientId(Context context, long recipientId, String queryText) {
        if (null == mHandledRecipientIds) {
            mHandledRecipientIds = new ArrayList<String>();
        } else {
            mHandledRecipientIds.clear();
        }
        // 通过recipientId查找thread, thread表里有针对群发信息用空格分开保存的recipientId
        String selection1 = "recipient_ids like '" + recipientId + " %' ";
        getGroupMessageByselection(context, selection1, queryText);

        String selection2 = "recipient_ids like '% " + recipientId + "' ";
        getGroupMessageByselection(context, selection2, queryText);

    }

    public void getGroupMessageByselection(Context context, String selection, String queryText) {
        //if (DEBUG) Log.d(TAG, "\n\n getGroupMessageByselection, selection = " + selection);
        Cursor cursor = context.getContentResolver().query(Conversation.sAllThreadsUri,
                Conversation.ALL_THREADS_PROJECTION, selection, null, null);
        if (null == cursor || 0 == cursor.getCount()) {
            return;
        }
        if(DEBUG) Log.d(TAG, "getGroupMessageByselection, cursor Count = " + cursor.getCount());
        int currentDataType = SearchListItemData.DATA_TYPE_THREAD;
        SearchListItemData messageData;
        try {
            while (cursor.moveToNext()) {
                String recipient_ids = cursor.getString(
                        cursor.getColumnIndex(ThreadsColumns.RECIPIENT_IDS));//"recipient_ids"
                ContactList recipients = ContactList.getByIds(recipient_ids, false);
                Log.d(TAG, "getGroupMessageByselection(), recipients: " + recipients.serialize());
                if (mHandledRecipientIds.contains(recipient_ids)) {
                    continue;
                }
                mHandledRecipientIds.add(recipient_ids);
                //handled single or multi recipients thread
                String titleString = getRecipientsStrByContactList(recipients);
                if(DEBUG) Log.d(TAG, "getGroupMessageByselection(), titleString: " + titleString);
                if(TextUtils.isEmpty(titleString)){
                    continue;
                }

                long threadId = cursor.getLong(cursor.getColumnIndex(Threads._ID));//"_id"
                if(DEBUG) Log.d(TAG, "getGroupMessageByselection(), threadId: " + threadId);
                if(threadId <= 0){
                    continue;
                }

                int msg_count = cursor.getInt(cursor.getColumnIndex(ThreadsColumns.MESSAGE_COUNT));//"message_count"
                if(DEBUG) Log.d(TAG, "getGroupMessageByselection(), msg_count: " + msg_count);
                if(msg_count <= 0){
                    continue;
                }
                String bodyString = context.getString(R.string.mst_messages_count, msg_count);

                int subId = cursor.getInt(cursor.getColumnIndex(Threads.SUBSCRIPTION_ID));//"sub_id"
                if(DEBUG) Log.d(TAG, "getGroupMessageByselection(), subId: " + subId);

                //don't show dateView
                //String dateValue = MessageUtils.formatTimeStampString(context, conv.getDate());

                if (mPreDataType != currentDataType) {
                    mPreDataType = currentDataType;
                    mSearchedDatas_bg.add(recipientHeaderData);
                }
                messageData = new SearchListItemData(-1L/*rowId*/, threadId, titleString, bodyString,
                        null/*dateValue*/, subId, currentDataType, queryText);
                mSearchedDatas_bg.add(messageData);
            }
        } finally {
            cursor.close();
        }
    }

    private String getRecipientsStrByThreadId(Context context, long threadId) {
        if (threadId <= 0) {
            return "";
        }
        Conversation conv = Conversation.query(context, threadId, true/*allowQuery*/);
        if (null == conv) {
            return "";
        }
        //这样“重新获取收信人”就可以获取到该会话的所有联系人，包括群发信息的情况
        String titleString = getRecipientsStrByContactList(conv.getRecipients());
        if(DEBUG) Log.d(TAG, "getRecipientsStrByThreadId(), titleString: " + titleString);
        return titleString;
    }

    private String getRecipientsStrByContactList(ContactList contactList) {
        if (null == contactList) {
            return "";
        }
        String namesAndNumbers = contactList.formatNamesAndNumbers(",");
        if(DEBUG) Log.d(TAG, "getRecipientsStrByContactList(), namesAndNumbers: " + namesAndNumbers);
        boolean isNumber = MessageUtils.isNumeric(namesAndNumbers);
        if (!isNumber) {
            return namesAndNumbers;
        }
        String normalNumber = PhoneNumberUtils.normalizeNumber(namesAndNumbers);
        //if(DEBUG) Log.d(TAG, "getRecipientsStrByContactList(), normalNumber = " + normalNumber);
        //String phoneNumberNo86 = StringUtils.getPhoneNumberNo86(normalizeNumber);
        //if (DEBUG) Log.d(TAG, "getRecipientsStrByContactList(), phoneNumberNo86 = " + phoneNumberNo86);
        return normalNumber;
    }

    private String getFullyRecipientsStrByAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            return "";
        }
        String recipientsString = address;
        Contact contact = Contact.get(address, false);
        //no need hightligt this number,so not normalizeNumber here
        if (contact != null) {
            String contactName = contact.getName();
            //if(DEBUG) Log.d(TAG, "getFullyRecipientsStrByAddress(), contactName = " + contactName);
            String contactNumber = contact.getNumber();
            //if(DEBUG) Log.d(TAG, "getFullyRecipientsStrByAddress(), contactNumber = " + contactNumber);
            //for one contact and have name
            if (!contactName.equals(contactNumber)) {
                recipientsString = contact.getNameAndNumber();
            }
        }
        return recipientsString;
    }

    private static final String[] CANONICAL_ADDRESSES_COLUMNS_2 =
            new String[]{CanonicalAddressesColumns._ID, CanonicalAddressesColumns.ADDRESS};

    private Cursor queryCanonicalAddressesCusor(Context context, String queryText) {
        //if(DEBUG) Log.d(TAG, "queryCanonicalAddressesCusor, queryText = " + queryText);
        String selection = "address like '%" + queryText + "%' ";
        Uri canonical_addresses_uri = Uri.parse("content://mms-sms/canonical-addresses").buildUpon().build();
        Cursor cursor = context.getContentResolver().query(canonical_addresses_uri,
                CANONICAL_ADDRESSES_COLUMNS_2, selection, null, null);
        return cursor;
    }

}
