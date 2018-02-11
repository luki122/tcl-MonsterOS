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
import android.database.MergeCursor;
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
import com.android.mms.LogTag;
import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.ui.MessageUtils;

//this class add by lichao for Mms Search
public class SearchTask extends AsyncTask<Void, Void, Cursor> {

    private static final String TAG = "Mms/SearchTask";
    private static final boolean DEBUG = false;

    private String mSearchString;
    private String mSearchString_bg;
    private boolean mIsCancelled = false;
    private Cursor mSearchedCursor;
    private Context mContext;
    //private int mHeaderCount = 0;
    //private int mThreadsAndMessagesCount = 0;


    public SearchTask(Context context) {
        mContext = context;
        mIsCancelled = false;
    }

    //doInBackground方法在子线程中运行:执行任务,取得结果
    protected Cursor doInBackground(Void... none) {
        if (DEBUG) Log.d(TAG, "\n\n SearchTask, doInBackground(), mSearchString: " + mSearchString);

        if (isCancelled()) {
            Log.w(TAG, "doInBackground(), isCancelled, return null");
            return null;
        }
        //mHeaderCount = 0;
        //mThreadsAndMessagesCount = 0;
        mSearchString_bg = mSearchString;
        return doSearchInBackground(mContext, mSearchString_bg);
    }

    //onPostExecute运行在ui线程,也就是主线程中,把结果交给消息处理器
    protected void onPostExecute(Cursor searchedCursor) {
        if (DEBUG) Log.d(TAG, "onPostExecute begin");
        boolean isCursorChanged = false;
        Log.d(TAG, "onPostExecute, mSearchString_bg = "+mSearchString_bg);
        Log.d(TAG, "onPostExecute, mSearchString = "+mSearchString);
        // searchedCursor is for mSearchString_bg
        // maybe mSearchString changed while is searching for mSearchString_bg
        if (mSearchString_bg.equals(mSearchString)
                && !mIsCancelled
                && !isCancelled()) {
            setSearchedCursor(searchedCursor);
            isCursorChanged = true;
        }
        if (null != mMySearchListener) {
            mMySearchListener.onSearchCompleted(isCursorChanged);
        }
        if (DEBUG) Log.d(TAG, "onPostExecute end");
    }

//    protected void onProcessUpdate() {
//        super.onProcessUpdate();
//    }

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
    }

    public String getSearchString() {
        return mSearchString;
    }

    public void setSearchString(String searchString) {
        this.mSearchString = searchString;
    }

    public interface MySearchListener {
        void onSearchCompleted(boolean isCursorChanged);
    }

    private MySearchListener mMySearchListener = null;

    public void setMySearchListener(MySearchListener mySearchListener) {
        this.mMySearchListener = mySearchListener;
    }

    private void setSearchedCursor(Cursor searchedCursor) {
        mSearchedCursor = searchedCursor;
    }

    public Cursor getSearchedCursor() {
        return mSearchedCursor;
    }

    private Cursor doSearchInBackground(Context context, String queryText) {
        if (DEBUG) Log.d(TAG, "doSearchInBackground begin");
        Cursor cacheCursor = ConversationList.mSearchCursorCache.get(queryText);
        if (cacheCursor != null) {
            if (DEBUG) Log.d(TAG, "doSearchInBackground, get cacheItemDatas, return");
            return cacheCursor;
        }

        Cursor searchedCursor = null;
        Cursor[] searchedCursors = new Cursor[3];
        try {
            //Query Step 1. -----------------------------------------------------------------
            if (DEBUG) Log.d(TAG, "\n\n Query Step 1, doSearchInBackground, queryThreadsByNumber");
            boolean isNumber = MessageUtils.isNumeric(queryText);
            if (isNumber) {
                //searchedCursors[0] = getThreadsCursorByRecipientNumber(context, queryText);
                searchedCursors[0] = queryThreadsCursorByRecipientNumber(context, queryText);
            }

            //Query Step 2. -----------------------------------------------------------------
            if (DEBUG) Log.d(TAG, "\n\n Query Step 2, doSearchInBackground, queryThreadsByName");
            //searchedCursors[1] = getThreadsCursorByRecipientName(context, queryText);
            searchedCursors[1] = queryThreadsCursorByRecipientName(context, queryText);

            //Query Step 3. -----------------------------------------------------------------
            if (DEBUG) Log.d(TAG, "\n\n Query Step 3, doSearchInBackground, >>>queryMessagesByContent");
            searchedCursors[2] = queryMessagesCusorByContentText(context, queryText);
            //parseMessagesCursor(context, messagesCusor, queryText);

            if (DEBUG) Log.d(TAG, "doSearchInBackground, >>>getNoneEmptyMergeCursor");
            searchedCursor = getNoneEmptyMergeCursor(searchedCursors);

            if(DEBUG) Log.d(TAG, "doSearchInBackground, mSearchCursorCache.put(key="+queryText +")");
            ConversationList.mSearchCursorCache.put(queryText, searchedCursor);

        } catch (Exception e) {
            Log.e(TAG, "doSearchInBackground, Exception: " + e);
            return null;
        }

        if(DEBUG) Log.d(TAG, "doSearchInBackground end");
        return searchedCursor;
    }

    private Cursor queryThreadsCursorByRecipientNumber(Context context, String numberKey) {
        if(!MessageUtils.isNumeric(numberKey)) {
            return null;
        }
        String searchMode = String.valueOf(MessageUtils.SEARCH_MODE_NUMBER);
        String matchWhole= String.valueOf(MessageUtils.MATCH_BY_ADDRESS);

        Uri queryUri = MessageUtils.SEARCH_THREAD_URI.buildUpon()
                .appendQueryParameter("key_str", numberKey).build().buildUpon()
                .appendQueryParameter("search_mode", searchMode).build().buildUpon()
                .appendQueryParameter("match_whole", matchWhole).build();
        return context.getContentResolver().query(queryUri, null, null, null, null);
    }

    private Cursor queryThreadsCursorByRecipientName(Context context, String nameKey) {
        //Name may also include the number, so also search to see if there is a matching contact
        String addresses = MessageUtils.getSeparatedAddressByNameKey(context, nameKey);
        if(DEBUG) Log.d(TAG, "queryThreadsCursorByRecipientName, addresses = "+addresses);
        if (TextUtils.isEmpty(addresses)) {
            return null;
        }
        String searchMode = String.valueOf(MessageUtils.SEARCH_MODE_NAME);
        String matchWhole= String.valueOf(MessageUtils.MATCH_BY_THREAD_ID);

        Uri queryUri = MessageUtils.SEARCH_THREAD_URI.buildUpon()
                .appendQueryParameter("addresses", addresses).build().buildUpon()
                .appendQueryParameter("search_mode", searchMode).build().buildUpon()
                .appendQueryParameter("match_whole", matchWhole).build();
        return context.getContentResolver().query(queryUri, null, null, null, null);
    }

    private Cursor queryMessagesCusorByContentText(Context context, String queryText) {
        //if(DEBUG) Log.d(TAG, "queryMessagesCusor, queryText = " + queryText);
        //SEARCH_URI = Uri.parse("content://mms-sms/search")
        Uri contentUri = Telephony.MmsSms.SEARCH_URI.buildUpon()
                .appendQueryParameter("pattern", queryText).build();
        //can't set projection here. It use getTextSearchQuery() to set projection in MmsSmsProvider.java
        return context.getContentResolver().query(contentUri, null, null, null, null);
    }

    private Cursor getNoneEmptyMergeCursor(Cursor[] cursors){
        if(null == cursors){
            return null;
        }
        if(0 == cursors.length){
            return null;
        }
        int count = 0;
        for(Cursor cursor : cursors){
            if(null != cursor && cursor.getCount() > 0){
                ++count;
            }
        }
        if(0 == count){
            return null;
        }
        Cursor[] new_cursors = new Cursor[count];
        int index = 0;
        for(Cursor cursor : cursors){
            if(null != cursor && cursor.getCount() > 0){
                new_cursors[index] = cursor;
                ++index;
            }
        }
        Cursor mergeCursor = new MergeCursor(new_cursors);
        return mergeCursor;
    }

//    private Cursor getThreadsCursorByRecipientName(Context context, String nameKey) {
//        List<String> numbersList = MessageUtils.getNumbersListByName(context, nameKey);
//        //numbersList = [15608082624, +8618682301353]
//        if (DEBUG) Log.d(TAG, "getThreadsCursorByRecipientName, numbersList = " + numbersList);
//        if(null!= numbersList && numbersList.size() > 0){
//            int i = 0;
//            Cursor[] cursors = new Cursor[numbersList.size()];
//            for (String number : numbersList) {
//                if(DEBUG) Log.d(TAG, "getThreadsCursorByRecipientName, number = " + number);
//                if (!TextUtils.isEmpty(number) && MessageUtils.isNumeric(number)) {
//                    cursors[i] = getThreadsCursorByRecipientNumber(context, number);
//                    ++i;
//                }
//            }
//            return getNoneEmptyMergeCursor(cursors);
//        }
//        return null;
//    }

//    private Cursor getThreadsCursorByRecipientNumber(Context context, String queryNumber) {
//        if (DEBUG) Log.d(TAG, "getThreadsCursorByRecipientNumber, >>>queryCanonicalAddressesCusor");
//        Cursor cursor_number = queryCanonicalAddressesCusor(context, queryNumber);
//        //parseSingleAddressCursor(context, cursor_number, queryNumber);
//
//        if (DEBUG) Log.d(TAG, "getThreadsCursorByRecipientNumber, >>>getThreadCursorByAddressesCursor");
//        Cursor cursor = getThreadCursorByAddressesCursor(context, cursor_number, queryNumber);
//        return cursor;
//    }

//    private Cursor getThreadCursorByAddressesCursor(Context context, Cursor cursor, String queryText) {
//        if(DEBUG) Log.d(TAG, "getThreadCursorByAddressesCursor, begin -----");
//        if (null == cursor) {
//            if(DEBUG) Log.w(TAG, "getThreadCursorByAddressesCursor, null cursor, return");
//            return null;
//        }
//        Cursor mergeCursor = null;
//        int i = 0;
//        try {
//            int cursorCount = cursor.getCount();
//            if (cursorCount > 0) {
//                Cursor[] cursors = new Cursor[cursorCount];
//                Cursor cursor_temp = null;
//                long recipientId = -1L;
//                while (cursor.moveToNext()) {
//                    recipientId = cursor.getLong(cursor.getColumnIndex("_id"));
//                    //if(DEBUG) Log.d(TAG, "parseCanonicalAddressesCursor(), recipientId = " + recipientId);
//                    if(recipientId > 0){
//                        //handle both single recipient and multi recipients thread
//                        cursor_temp = getThreadCursorByRecipientId(context, recipientId, queryText);
//                        if(null != cursor_temp && cursor_temp.getCount() > 0){
//                            if(DEBUG) Log.d(TAG, "getThreadCursorByAddressesCursor, get cursors["+i+"]");
//                            cursors[i] = cursor_temp;
//                            ++i;
//                        }
//                    }
//                }
//                if(cursors != null && cursors.length > 0){
//                    mergeCursor = getNoneEmptyMergeCursor(cursors);
//                }
//            }
//        } finally {
//            cursor.close();
//        }
//        if(DEBUG) Log.d(TAG, "getThreadCursorByAddressesCursor, end -----");
//        return mergeCursor;
//    }

//    //handle both single recipient and multi recipients thread
//    private Cursor getThreadCursorByRecipientId(Context context, long recipientId, String queryText) {
//        Cursor[] cursors = new Cursor[4];
//        String selection0 = "recipient_ids=" + recipientId;
//        cursors[0] = getThreadCursorBySelection(context, selection0, queryText);
//
//        String selection1 = "recipient_ids like '" + recipientId + " %' ";
//        cursors[1] = getThreadCursorBySelection(context, selection1, queryText);
//
//        String selection2 = "recipient_ids like '% " + recipientId + "' ";
//        cursors[2] = getThreadCursorBySelection(context, selection2, queryText);
//
//        //see TelephonyProvider/MmsSmsProvider.java getThreadIds()
//        String selection3 = "recipient_ids like '% " + recipientId + " %' ";
//        cursors[3] = getThreadCursorBySelection(context, selection3, queryText);
//
//        return getNoneEmptyMergeCursor(cursors);
//    }

//    private Cursor getThreadCursorBySelection(Context context, String selection, String queryText) {
//        Cursor cursor = context.getContentResolver().query(Conversation.sAllThreadsUri,
//                Conversation.ALL_THREADS_PROJECTION, selection, null, null);
//        return cursor;
//        //parseThreadsCursor(context, cursor, queryText);
//    }

//    private static final String[] CANONICAL_ADDRESSES_COLUMNS_2 =
//            new String[]{CanonicalAddressesColumns._ID, CanonicalAddressesColumns.ADDRESS};

//    private Cursor queryCanonicalAddressesCusor(Context context, String number) {
//        if (TextUtils.isEmpty(number)) {
//            Log.w(TAG, "queryCanonicalAddressesCusor, null or empty number, return null");
//            return null;
//        }
//        //if(DEBUG) Log.d(TAG, "queryCanonicalAddressesCusor, number = " + number);
//        //String numberLen = String.valueOf(number.length());
//
//        //String strippedNumber = PhoneNumberUtils.stripSeparators(number);
//        //strippedNumber = 15019467235
//        //if(DEBUG) Log.d(TAG, "queryCanonicalAddressesCusor, strippedNumber = " + strippedNumber);
//
//        //String replacedNumber = number.replace(' ', '%');
//        //if (DEBUG) Log.d(TAG, "queryCanonicalAddressesCusor(), replacedNumber: " + replacedNumber);
//
//        String selection = "address like '%" + number + "%' ";
//        if (DEBUG) Log.d(TAG, "queryCanonicalAddressesCusor(), selection: " + selection);
//
//        Uri canonical_addresses_uri = Uri.parse("content://mms-sms/canonical-addresses").buildUpon().build();
//        Cursor cursor = context.getContentResolver().query(canonical_addresses_uri,
//                CANONICAL_ADDRESSES_COLUMNS_2, selection, null, null);
//        return cursor;
//    }

    /*
    private Cursor queryMessagesCusorByRecipientName(Context context, String queryText){
        String numberByName = MessageUtils.getSeparatedAddressByNameKey(context, queryText);
        if(DEBUG) Log.d(TAG, "queryMessagesCusorByName, numberByName = "+numberByName);
        if(TextUtils.isEmpty(numberByName)) {
            return null;
        }
        Uri nameUri = Uri.parse("content://mms-sms/search-message").buildUpon()
                .appendQueryParameter("key_str", numberByName)
                .appendQueryParameter("search_mode", String.valueOf(MessageUtils.SEARCH_MODE_NUMBER))
                .appendQueryParameter("match_whole", String.valueOf(MessageUtils.MATCH_BY_THREAD_ID))
                .build();
        Cursor cursor = context.getContentResolver().query(nameUri, null, null, null, null);
        return cursor;
    }
    */

    /*
    private Cursor queryMessagesCusorByRecipientNumber(Context context, String queryNumber){
        boolean isNumber = MessageUtils.isNumeric(queryNumber);
        if(!isNumber) {
            return null;
        }
        //relate to MmsSmsProvider.java 653
        Uri numberUri = Uri.parse("content://mms-sms/search-message").buildUpon()
                .appendQueryParameter("key_str", queryNumber)
                .appendQueryParameter("search_mode", String.valueOf(MessageUtils.SEARCH_MODE_NUMBER))
                .appendQueryParameter("match_whole", String.valueOf(MessageUtils.MATCH_BY_ADDRESS))
                .build();
        Cursor cursor = context.getContentResolver().query(numberUri, null, null, null, null);
        return cursor;
    }
    */

}
