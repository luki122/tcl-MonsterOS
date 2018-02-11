/*
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

package com.android.providers.telephony;

import java.util.Locale;

import android.app.AppOpsManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.os.UserHandle;
import android.provider.BaseColumns;
import mst.provider.Telephony;
import mst.provider.Telephony.CanonicalAddressesColumns;
import mst.provider.Telephony.Mms;
import mst.provider.Telephony.MmsSms;
import mst.provider.Telephony.MmsSms.PendingMessages;
import mst.provider.Telephony.Sms;
import mst.provider.Telephony.Sms.Conversations;
import mst.provider.Telephony.Threads;
import mst.provider.Telephony.ThreadsColumns;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.mms.pdu.PduHeaders;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class provides the ability to query the MMS and SMS databases
 * at the same time, mixing messages from both in a single thread
 * (A.K.A. conversation).
 *
 * A virtual column, MmsSms.TYPE_DISCRIMINATOR_COLUMN, may be
 * requested in the projection for a query.  Its value is either "mms"
 * or "sms", depending on whether the message represented by the row
 * is an MMS message or an SMS message, respectively.
 *
 * This class also provides the ability to find out what addresses
 * participated in a particular thread.  It doesn't support updates
 * for either of these.
 *
 * This class provides a way to allocate and retrieve thread IDs.
 * This is done atomically through a query.  There is no insert URI
 * for this.
 *
 * Finally, this class provides a way to delete or update all messages
 * in a thread.
 */
public class MmsSmsProvider extends ContentProvider {
    private static final UriMatcher URI_MATCHER =
            new UriMatcher(UriMatcher.NO_MATCH);
    private static final String LOG_TAG = "Mms/MmsSmsProvider";
    private static final boolean DEBUG = false;

    private static final String NO_DELETES_INSERTS_OR_UPDATES =
            "MmsSmsProvider does not support deletes, inserts, or updates for this URI.";
    private static final int URI_CONVERSATIONS                     = 0;
    private static final int URI_CONVERSATIONS_MESSAGES            = 1;
    private static final int URI_CONVERSATIONS_RECIPIENTS          = 2;
    private static final int URI_MESSAGES_BY_PHONE                 = 3;
    private static final int URI_THREAD_ID                         = 4;
    private static final int URI_CANONICAL_ADDRESS                 = 5;
    private static final int URI_PENDING_MSG                       = 6;
    private static final int URI_COMPLETE_CONVERSATIONS            = 7;
    private static final int URI_UNDELIVERED_MSG                   = 8;
    private static final int URI_CONVERSATIONS_SUBJECT             = 9;
    private static final int URI_NOTIFICATIONS                     = 10;
    private static final int URI_OBSOLETE_THREADS                  = 11;
    private static final int URI_DRAFT                             = 12;
    private static final int URI_CANONICAL_ADDRESSES               = 13;
    private static final int URI_SEARCH                            = 14;
    private static final int URI_SEARCH_SUGGEST                    = 15;
    private static final int URI_FIRST_LOCKED_MESSAGE_ALL          = 16;
    private static final int URI_FIRST_LOCKED_MESSAGE_BY_THREAD_ID = 17;
    private static final int URI_MESSAGE_ID_TO_THREAD              = 18;
	//7.0 modify begin
    //private static final int URI_MAILBOX_MESSAGES                  = 19;//7.0 delete
    //private static final int URI_SEARCH_MESSAGE                    = 20;
    //private static final int URI_MESSAGES_COUNT                    = 21;
    //private static final int URI_UPDATE_THREAD_DATE                = 22;
    //private static final int URI_UPDATE_THREAD                     = 23;//7.0 delete
    private static final int URI_MESSAGES_COUNT                    = 19;
    private static final int URI_UPDATE_THREAD_DATE                = 20;
    private static final int URI_SEARCH_MESSAGE                    = 21;
    private static final int URI_MAILBOX_MESSAGES                  = 22;//lichao add again
    private static final int MST_URI_UPDATE_THREAD                          = 23;//tangyisen add for update thread
	//7.0 modify end
    
    //add by lgy
    private static final int URI_THREAD_ID_QUERY_ONLY              = 100;
    //lichao add in 2016-12-07
    private static final int URI_SEARCH_THREAD                     = 101;

    // Escape character
    private static final char SEARCH_ESCAPE_CHARACTER = '!';

    public static final int SEARCH_MODE_CONTENT = 0;
    public static final int SEARCH_MODE_NAME    = 1;
    public static final int SEARCH_MODE_NUMBER  = 2;//6.0 noly, 7.0 delete
    public static final int SEARCH_MODE_SUBJECT = 3;//6.0 noly, 7.0 delete
    public static final int SEARCH_MODE_BLACK   = 10;//lichao add

    // add for different match mode in classify search
    public static final int MATCH_BY_ADDRESS = 0;//6.0 noly, 7.0 delete
    public static final int MATCH_BY_THREAD_ID = 1;//6.0 noly, 7.0 delete

    private static final long RESULT_FOR_ID_NOT_FOUND= -1L;

    /**
     * the name of the table that is used to store the queue of
     * messages(both MMS and SMS) to be sent/downloaded.
     */
    public static final String TABLE_PENDING_MSG = "pending_msgs";

    /**
     * the name of the table that is used to store the canonical addresses for both SMS and MMS.
     */
    private static final String TABLE_CANONICAL_ADDRESSES = "canonical_addresses";
    private static final String DEFAULT_STRING_ZERO = "0";

    /**
     * the name of the table that is used to store the conversation threads.
     */
    static final String TABLE_THREADS = "threads";

    // These constants are used to construct union queries across the
    // MMS and SMS base tables.

    // These are the columns that appear in both the MMS ("pdu") and
    // SMS ("sms") message tables.
    //tangyisen add reject
    private static final String[] MMS_SMS_COLUMNS =
            { BaseColumns._ID, Mms.DATE, Mms.DATE_SENT, Mms.READ, Mms.THREAD_ID, Mms.LOCKED,
                    Mms.SUBSCRIPTION_ID, Mms.REJECT };

    // These are the columns that appear only in the MMS message
    // table.
    //tangyisen if add column,need add here and,add extra
    private static final String[] MMS_ONLY_COLUMNS = {
        Mms.CONTENT_CLASS, Mms.CONTENT_LOCATION, Mms.CONTENT_TYPE,
        Mms.DELIVERY_REPORT, Mms.EXPIRY, Mms.MESSAGE_CLASS, Mms.MESSAGE_ID,
        Mms.MESSAGE_SIZE, Mms.MESSAGE_TYPE, Mms.MESSAGE_BOX, Mms.PRIORITY,
        Mms.READ_STATUS, Mms.RESPONSE_STATUS, Mms.RESPONSE_TEXT,
        Mms.RETRIEVE_STATUS, Mms.RETRIEVE_TEXT_CHARSET, Mms.REPORT_ALLOWED,
        Mms.READ_REPORT, Mms.STATUS, Mms.SUBJECT, Mms.SUBJECT_CHARSET,
        Mms.TRANSACTION_ID, Mms.MMS_VERSION, Mms.TEXT_ONLY, Mms.EXTRA };

    // These are the columns that appear only in the SMS message
    // table.
    private static final String[] SMS_ONLY_COLUMNS =
            { "address", "body", "person", "reply_path_present",
              "service_center", "status", "subject", "type", "error_code" };

    // These are all the columns that appear in the "threads" table.
    private static final String[] THREADS_COLUMNS = {
        BaseColumns._ID,
        ThreadsColumns.DATE,
        ThreadsColumns.RECIPIENT_IDS,
        ThreadsColumns.MESSAGE_COUNT
    };

    private static final String[] CANONICAL_ADDRESSES_COLUMNS_1 =
            new String[] { CanonicalAddressesColumns.ADDRESS };

    private static final String[] CANONICAL_ADDRESSES_COLUMNS_2 =
            new String[] { CanonicalAddressesColumns._ID,
                    CanonicalAddressesColumns.ADDRESS };

    // These are all the columns that appear in the MMS and SMS
    // message tables.
    private static final String[] UNION_COLUMNS =
            new String[MMS_SMS_COLUMNS.length
                       + MMS_ONLY_COLUMNS.length
                       + SMS_ONLY_COLUMNS.length];

    // These are all the columns that appear in the MMS table.
    private static final Set<String> MMS_COLUMNS = new HashSet<String>();

    // These are all the columns that appear in the SMS table.
    private static final Set<String> SMS_COLUMNS = new HashSet<String>();

    private static final String VND_ANDROID_DIR_MMS_SMS =
            "vnd.android-dir/mms-sms";

    private static final String[] ID_PROJECTION = { BaseColumns._ID };

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private static final String[] SEARCH_STRING = new String[1];
	//6.0
    private static final String SEARCH_QUERY = "SELECT index_text as snippet FROM " +
            "words WHERE index_text like ? ORDER BY snippet LIMIT 50;";
	//7.0
    //private static final String SEARCH_QUERY = "SELECT snippet(words, '', ' ', '', 1, 1) as " +
    //        "snippet FROM words WHERE index_text MATCH ? ORDER BY snippet LIMIT 50;";

    private static final String SMS_CONVERSATION_CONSTRAINT = "(" +
            Sms.TYPE + " != " + Sms.MESSAGE_TYPE_DRAFT + ")";

    private static final String MMS_CONVERSATION_CONSTRAINT = "(" +
            Mms.MESSAGE_BOX + " != " + Mms.MESSAGE_BOX_DRAFTS + " AND (" +
            Mms.MESSAGE_TYPE + " = " + PduHeaders.MESSAGE_TYPE_SEND_REQ + " OR " +
            Mms.MESSAGE_TYPE + " = " + PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF + " OR " +
            Mms.MESSAGE_TYPE + " = " + PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND + "))";

    private static String getTextSearchQuery(String smsTable, String pduTable) {
        // Search on the words table but return the rows from the corresponding sms table
        final String smsQuery = "SELECT "
                + smsTable + "._id AS _id,"
                + "thread_id,"
                + "sub_id,"//lichao add in 2016-11-04
                + "address,"
                + "body,"
                + "date,"
                + "date_sent,"
                + "index_text,"
                + "words._id "
                + "FROM " + smsTable + ",words "
                + "WHERE (index_text LIKE ? "
                + "AND " + smsTable + "._id=words.source_id "
                + "AND words.table_to_use=1)";//words.table_to_use=1 means smsTable

        // Search on the words table but return the rows from the corresponding parts table
		//the SELECT arg counts must same as smsQuery
        final String mmsQuery = "SELECT "
                + pduTable + "._id,"
                + "thread_id,"
                + "sub_id,"//lichao add in 2016-11-04
                + "addr.address AS address,"//lichao modify in 2016-11-08
                + "part.text AS body,"
                + pduTable + ".date * 1000 AS date,"//lichao modify in 2016-11-07
                + pduTable + ".date_sent,"
                + "index_text,"
                + "words._id "
                + "FROM " + pduTable + ",part,addr,words "
                + "WHERE ((part.mid=" + pduTable + "._id) "
                + "AND (addr.msg_id=" + pduTable + "._id) "
                + "AND (addr.type=" + PduHeaders.TO + ") "//int TO=0x97(=151);int FROM = 0x89(=137);
                + "AND (part.ct='text/plain') "
                + "AND (index_text LIKE ?) "//index_text in words table
                + "AND (part._id = words.source_id) "
                + "AND (words.table_to_use=2))";//words.table_to_use=2 means pduTable

        // This code queries the sms and mms tables and returns a unified result set
        // of text matches.  We query the sms table which is pretty simple.  We also
        // query the pdu, part and addr table to get the mms result.  Note we're
        // using a UNION so we have to have the same number of result columns from
        // both queries.
        return smsQuery + " UNION " + mmsQuery + " "
                + "GROUP BY thread_id "
                + "ORDER BY thread_id ASC, date DESC";
    }

    //6.0 noly, 7.0 delete begin
    private static final String SMS_PROJECTION = "'sms' AS transport_type, _id, thread_id,"
            + "address, body, sub_id, date, date_sent, read, type,"
            + "status, locked, NULL AS error_code,"
            + "NULL AS sub, NULL AS sub_cs, date, date_sent, read,"
            + "NULL as m_type,"
            + "NULL AS msg_box,"
            + "NULL AS d_rpt, NULL AS rr, NULL AS err_type,"
            + "locked, NULL AS st, NULL AS text_only,"
            + "sub_id, NULL AS recipient_ids";
    //lichao modify
    private static final String MMS_PROJECTION_FOR_CONTENT_SEARCH =
            "'mms' AS transport_type, pdu._id, thread_id,"
            + "addr.address AS address, part.text as body, sub_id,"
            + "pdu.date * 1000 AS date, date_sent, read, NULL AS type,"
            + "NULL AS status, locked, NULL AS error_code,"
            + "sub, sub_cs, date, date_sent, read,"
            + "m_type,"
            + "pdu.msg_box AS msg_box,"
            + "d_rpt, rr, NULL AS err_type,"
            + "locked, NULL AS st, NULL AS text_only,"
            + "sub_id, NULL AS recipient_ids";
    private static final String MMS_PROJECTION_FOR_NUMBER_SEARCH =
            "'mms' AS transport_type, pdu._id, thread_id,"
            + "addr.address AS address, NULL AS body, sub_id,"
            + "pdu.date * 1000 AS date, date_sent, read, NULL AS type,"
            + "NULL AS status, locked, NULL AS error_code,"
            + "sub, sub_cs, date, date_sent, read,"
            + "m_type,"
            + "pdu.msg_box AS msg_box,"
            + "d_rpt, rr, NULL AS err_type,"
            + "locked, NULL AS st, NULL AS text_only,"
            + "sub_id, NULL AS recipient_ids";
    private static final String MMS_PROJECTION_FOR_SUBJECT_SEARCH =
            "'mms' AS transport_type, pdu._id, thread_id,"
            //+ "addr.address AS address, pdu.sub as body, phone_id,"
            + "addr.address AS address, pdu.sub as body, sub_id,"//lichao modify in 2016-11-07
            + "pdu.date * 1000 AS date, date_sent, read, NULL AS type,"
            + "NULL AS status, locked, NULL AS error_code,"
            + "sub, sub_cs, date, date_sent, read,"
            + "m_type,"
            + "pdu.msg_box AS msg_box,"
            + "d_rpt, rr, NULL AS err_type,"
            + "locked, NULL AS st, NULL AS text_only,"
            + "phone_id, NULL AS recipient_ids";
    //6.0 noly, 7.0 delete end
    private static final String AUTHORITY = "mms-sms";

    static {
        URI_MATCHER.addURI(AUTHORITY, "conversations", URI_CONVERSATIONS);
        URI_MATCHER.addURI(AUTHORITY, "complete-conversations", URI_COMPLETE_CONVERSATIONS);

        // In these patterns, "#" is the thread ID.
        URI_MATCHER.addURI(
                AUTHORITY, "conversations/#", URI_CONVERSATIONS_MESSAGES);
        URI_MATCHER.addURI(
                AUTHORITY, "conversations/#/recipients",
                URI_CONVERSATIONS_RECIPIENTS);

        URI_MATCHER.addURI(
                AUTHORITY, "conversations/#/subject",
                URI_CONVERSATIONS_SUBJECT);

        //7.0 delete
        //"#" is the mailbox name id, such as inbox=1, sent=2, draft = 3 , outbox = 4
        URI_MATCHER.addURI(AUTHORITY, "mailbox/#", URI_MAILBOX_MESSAGES);

        // URI for obtaining all short message count
        URI_MATCHER.addURI(AUTHORITY, "messagescount", URI_MESSAGES_COUNT);

        // URI for deleting obsolete threads.
        URI_MATCHER.addURI(AUTHORITY, "conversations/obsolete", URI_OBSOLETE_THREADS);

        // URI for search messages in mailbox mode with obtained search mode
        // such as content, number and name
        URI_MATCHER.addURI(AUTHORITY, "search-message", URI_SEARCH_MESSAGE);

        // URI for search threads with obtained search mode
        // such as number and name
        // lichao add in 2016-12-07
        URI_MATCHER.addURI(AUTHORITY, "search-thread", URI_SEARCH_THREAD);

        URI_MATCHER.addURI(AUTHORITY, "messages/byphone/*", URI_MESSAGES_BY_PHONE);

        // In this pattern, two query parameter names are expected:
        // "subject" and "recipient."  Multiple "recipient" parameters
        // may be present.
        URI_MATCHER.addURI(AUTHORITY, "threadID", URI_THREAD_ID);
        
        // add by lgy 
        URI_MATCHER.addURI(AUTHORITY, "query-threadID", URI_THREAD_ID_QUERY_ONLY);        

        //7.0 delete URI_UPDATE_THREAD
        //URI_MATCHER.addURI(AUTHORITY, "update-thread/#", URI_UPDATE_THREAD);

        URI_MATCHER.addURI(AUTHORITY, "update-date", URI_UPDATE_THREAD_DATE);

        // Use this pattern to query the canonical address by given ID.
        URI_MATCHER.addURI(AUTHORITY, "canonical-address/#", URI_CANONICAL_ADDRESS);

        // Use this pattern to query all canonical addresses.
        URI_MATCHER.addURI(AUTHORITY, "canonical-addresses", URI_CANONICAL_ADDRESSES);

        URI_MATCHER.addURI(AUTHORITY, "search", URI_SEARCH);
        URI_MATCHER.addURI(AUTHORITY, "searchSuggest", URI_SEARCH_SUGGEST);

        // In this pattern, two query parameters may be supplied:
        // "protocol" and "message." For example:
        //   content://mms-sms/pending?
        //       -> Return all pending messages;
        //   content://mms-sms/pending?protocol=sms
        //       -> Only return pending SMs;
        //   content://mms-sms/pending?protocol=mms&message=1
        //       -> Return the the pending MM which ID equals '1'.
        //
        URI_MATCHER.addURI(AUTHORITY, "pending", URI_PENDING_MSG);

        // Use this pattern to get a list of undelivered messages.
        URI_MATCHER.addURI(AUTHORITY, "undelivered", URI_UNDELIVERED_MSG);
		
        // Use this pattern to see what delivery status reports (for
        // both MMS and SMS) have not been delivered to the user.
        URI_MATCHER.addURI(AUTHORITY, "notifications", URI_NOTIFICATIONS);

        URI_MATCHER.addURI(AUTHORITY, "draft", URI_DRAFT);

        URI_MATCHER.addURI(AUTHORITY, "locked", URI_FIRST_LOCKED_MESSAGE_ALL);

        URI_MATCHER.addURI(AUTHORITY, "locked/#", URI_FIRST_LOCKED_MESSAGE_BY_THREAD_ID);

        URI_MATCHER.addURI(AUTHORITY, "messageIdToThread", URI_MESSAGE_ID_TO_THREAD);
        URI_MATCHER.addURI(AUTHORITY, "mst-update-thread/", MST_URI_UPDATE_THREAD);//tangyisen add
        initializeColumnSets();
    }

    private SQLiteOpenHelper mOpenHelper;

    private boolean mUseStrictPhoneNumberComparation;

    @Override
    public boolean onCreate() {
        setAppOps(AppOpsManager.OP_READ_SMS, AppOpsManager.OP_WRITE_SMS);
        mOpenHelper = MmsSmsDatabaseHelper.getInstanceForCe(getContext());
        mUseStrictPhoneNumberComparation =
            getContext().getResources().getBoolean(
                    com.android.internal.R.bool.config_use_strict_phone_number_comparation);
        TelephonyBackupAgent.DeferredSmsMmsRestoreService.startIfFilesExist(getContext());
        return true;
    }

    //begin tangyisen
    public String rebuildSelection(String selection, boolean accessRestricted) {
        if(TextUtils.isEmpty(selection)) {
            return "reject = 0";
        } else {
            if(accessRestricted) {
                return selection + " AND reject = 0";
            } else if (!selection.contains("reject")) {
                return selection + " AND reject = 0";
            }
        }
        //use concatSelections instead of
        return selection;
    }
    //end tangyisen

    @Override
    public Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        // First check if restricted views of the "sms" and "pdu" tables should be used based on the
        // caller's identity. Only system, phone or the default sms app can have full access
        // of sms/mms data. For other apps, we present a restricted view which only contains sent
        // or received messages, without wap pushes.
        final boolean accessRestricted = ProviderUtil.isAccessRestricted(
                getContext(), getCallingPackage(), Binder.getCallingUid());
        final String pduTable = MmsProvider.getPduTable(accessRestricted);
        final String smsTable = SmsProvider.getSmsTable(accessRestricted);

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = null;
        final int match = URI_MATCHER.match(uri);
        switch (match) {
            case URI_COMPLETE_CONVERSATIONS:
                //begin tangyisen
                selection = rebuildSelection(selection, accessRestricted);
                //end tangyisen
                cursor = getCompleteConversations(projection, selection, sortOrder, smsTable,
                        pduTable);
                break;
            case URI_CONVERSATIONS:
                String simple = uri.getQueryParameter("simple");
                //begin tangyisen
                String reject = uri.getQueryParameter("reject");
                //end tangyisen
                if ((simple != null) && simple.equals("true")) {
                    //tangyisen need operate
                    String threadType = uri.getQueryParameter("thread_type");
                    if (!TextUtils.isEmpty(threadType)) {
                        selection = concatSelections(
                                selection, Threads.TYPE + "=" + threadType);
                    }
                    //begin tangyisen add thread message_count = 0 and subid != -1 means not draft will not return
                    if ((reject == null) || reject.equals("false")) {
                        selection = concatSelections(
                            selection, "(" + Threads.MESSAGE_COUNT + " != 0 OR " + Threads.SUBSCRIPTION_ID + " = -1)");
                    }
                    //end tangyisen
                    cursor = getSimpleConversations(
                            projection, selection, selectionArgs, sortOrder);
                } else {
                    //begin tangyisen
                    selection = rebuildSelection(selection, accessRestricted);
                    //end tangyisen
                    cursor = getConversations(
                            projection, selection, sortOrder, smsTable, pduTable);
                }
                break;
            case URI_CONVERSATIONS_MESSAGES:
                //begin tangyisen
                selection = rebuildSelection(selection, accessRestricted);
                //end tangyisen
                cursor = getConversationMessages(uri.getPathSegments().get(1), projection,
                        selection, sortOrder, smsTable, pduTable);
                break;
            //7.0 delete
            case URI_MAILBOX_MESSAGES:
                //begin tangyisen
                selection = rebuildSelection(selection, accessRestricted);
                //end tangyisen
                cursor = getMailboxMessages(
                        uri.getPathSegments().get(1), projection, selection,
                        selectionArgs, sortOrder, false, pduTable);
                break;
            case URI_MESSAGES_COUNT:
                //tangyisen need operate
                return getAllMessagesCount();
            case URI_CONVERSATIONS_RECIPIENTS:
                //tangyisen need operate all message reject conversation will not return
                //begin tangyisen add thread message_count = 0 and subid != -1 means not draft will not return
                selection = concatSelections(
                    selection, "(" + Threads.MESSAGE_COUNT + " != 0 OR " + Threads.SUBSCRIPTION_ID + " = -1)");
                //end tangyisen
                cursor = getConversationById(
                        uri.getPathSegments().get(1), projection, selection,
                        selectionArgs, sortOrder);
                break;
            case URI_CONVERSATIONS_SUBJECT:
                //begin tangyisen add thread message_count = 0 and subid != -1 means not draft will not return
                selection = concatSelections(
                    selection, "(" + Threads.MESSAGE_COUNT + " != 0 OR " + Threads.SUBSCRIPTION_ID + " = -1)");
                //end tangyisen
                cursor = getConversationById(
                        uri.getPathSegments().get(1), projection, selection,
                        selectionArgs, sortOrder);
                break;
            case URI_MESSAGES_BY_PHONE:
                //begin tangyisen
                selection = rebuildSelection(selection, accessRestricted);
                //end tangyisen
                cursor = getMessagesByPhoneNumber(
                        uri.getPathSegments().get(2), projection, selection, sortOrder, smsTable,
                        pduTable);
                break;
            case URI_THREAD_ID:
                //tangyisen getThreadId not need reject
                List<String> recipients = uri.getQueryParameters("recipient");
                cursor = getThreadId(recipients,selection); //modify by lgy
                break;
            //add by lgy
            case URI_THREAD_ID_QUERY_ONLY: {
                //tangyisen threads not need reject
                List<String> numbers = uri.getQueryParameters("recipient");
                cursor = queryThreadId(numbers,selection);
                break;
            }
            case URI_CANONICAL_ADDRESS: {
                //tangyisen table TABLE_CANONICAL_ADDRESSES not need reject
                String extraSelection = "_id=" + uri.getPathSegments().get(1);
                String finalSelection = TextUtils.isEmpty(selection)
                        ? extraSelection : extraSelection + " AND " + selection;
                cursor = db.query(TABLE_CANONICAL_ADDRESSES,
                        CANONICAL_ADDRESSES_COLUMNS_1,
                        finalSelection,
                        selectionArgs,
                        null, null,
                        sortOrder);
                break;
            }
            case URI_CANONICAL_ADDRESSES:
                //tangyisen table TABLE_CANONICAL_ADDRESSES not need reject
                cursor = db.query(TABLE_CANONICAL_ADDRESSES,
                        CANONICAL_ADDRESSES_COLUMNS_2,
                        selection,
                        selectionArgs,
                        null, null,
                        sortOrder);
                break;
            case URI_SEARCH_SUGGEST: {
                //6.0
                //tangyisen not need add reject,because search in table words and reject will not insert into words
                String pattern = uri.getQueryParameter("pattern");
                if (TextUtils.isEmpty(pattern)) {
                    return null;
                } else {
                    SEARCH_STRING[0] = "%" + pattern + "%";
                }
                //7.0
                //SEARCH_STRING[0] = uri.getQueryParameter("pattern") + '*' ;

                // find the words which match the pattern using the snippet function.  The
                // snippet function parameters mainly describe how to format the result.
                // See http://www.sqlite.org/fts3.html#section_4_2 for details.
                if (sortOrder != null
                        || selection != null
                        || selectionArgs != null
                        || projection != null) {
                    throw new IllegalArgumentException(
                            "do not specify sortOrder, selection, selectionArgs, or projection" +
                            "with this query");
                }

                cursor = db.rawQuery(SEARCH_QUERY, SEARCH_STRING);
                break;
            }
            case URI_MESSAGE_ID_TO_THREAD: {
                // Given a message ID and an indicator for SMS vs. MMS return
                // the thread id of the corresponding thread.
                try {
                    long id = Long.parseLong(uri.getQueryParameter("row_id"));
                    switch (Integer.parseInt(uri.getQueryParameter("table_to_use"))) {
                        case 1:  // sms
                            //tangyisen add reject
                            cursor = db.query(
                                smsTable,
                                new String[] { "thread_id" },
                                "_id=? AND reject=0",
                                new String[] { String.valueOf(id) },
                                null,
                                null,
                                null);
                            break;
                        case 2:  // mms
                            //tangyisen add reject
                            String mmsQuery = "SELECT thread_id "
                                    + "FROM " + pduTable + ",part "
                                    + "WHERE ((part.mid=" + pduTable + "._id) "
                                    + "AND " + "(part._id=?) AND (" + pduTable + ".reject = 0))";
                            cursor = db.rawQuery(mmsQuery, new String[] { String.valueOf(id) });
                            break;
                    }
                } catch (NumberFormatException ex) {
                    // ignore... return empty cursor
                }
                break;
            }
            case URI_SEARCH: {
                //tangyisen not need add reject,because just search in table words
                if (       sortOrder != null
                        || selection != null
                        || selectionArgs != null
                        || projection != null) {
                    throw new IllegalArgumentException(
                            "do not specify sortOrder, selection, selectionArgs, or projection" +
                            "with this query");
                }
                //6.0
                String pattern = uri.getQueryParameter("pattern");
                if (TextUtils.isEmpty(pattern)) {
                    return null;
                }
                String searchString = "%" + pattern + "%";
                //7.0
                //String searchString = uri.getQueryParameter("pattern") + "*";

                //tangyisen reject add in getTextSearchQuery
                try {
                    cursor = db.rawQuery(getTextSearchQuery(smsTable, pduTable),
                            new String[] { searchString, searchString });
                } catch (Exception ex) {
                    Log.e(LOG_TAG, "got exception: " + ex.toString());
                }
                break;
            }
            case URI_SEARCH_MESSAGE:
            //tangyisen add reject in getSearchMessages
                //6.0
                cursor = getSearchMessages(uri, db);
                //NB7.0
                //cursor = getSearchMessages(uri, db, smsTable, pduTable);
                break;
            // lichao add in 2016-12-07
            case URI_SEARCH_THREAD:
                cursor = getSearchThreads(uri, db);
                break;
            case URI_PENDING_MSG: {
                //tangyisen TABLE_PENDING_MSG not need reject
                String protoName = uri.getQueryParameter("protocol");
                String msgId = uri.getQueryParameter("message");
                int proto = TextUtils.isEmpty(protoName) ? -1
                        : (protoName.equals("sms") ? MmsSms.SMS_PROTO : MmsSms.MMS_PROTO);

                String extraSelection = (proto != -1) ?
                        (PendingMessages.PROTO_TYPE + "=" + proto) : " 0=0 ";
                if (!TextUtils.isEmpty(msgId)) {
                    extraSelection += " AND " + PendingMessages.MSG_ID + "=" + msgId;
                }

                String finalSelection = TextUtils.isEmpty(selection)
                        ? extraSelection : ("(" + extraSelection + ") AND " + selection);
                String finalOrder = TextUtils.isEmpty(sortOrder)
                        ? PendingMessages.DUE_TIME : sortOrder;
                cursor = db.query(TABLE_PENDING_MSG, null,
                        finalSelection, selectionArgs, null, null, finalOrder);
                break;
            }
            case URI_UNDELIVERED_MSG: {
                //begin tangyisen
                selection = rebuildSelection(selection, accessRestricted);
                //end tangyisen
                cursor = getUndeliveredMessages(projection, selection,
                        selectionArgs, sortOrder, smsTable, pduTable);
                break;
            }
            case URI_DRAFT: {
                //begin tangyisen
                selection = rebuildSelection(selection, accessRestricted);
                //end tangyisen
                cursor = getDraftThread(projection, selection, sortOrder, smsTable, pduTable);
                break;
            }
            case URI_FIRST_LOCKED_MESSAGE_BY_THREAD_ID: {
                long threadId;
                try {
                    threadId = Long.parseLong(uri.getLastPathSegment());
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "Thread ID must be a long.");
                    break;
                }
                //begin tangyisen
                /*cursor = getFirstLockedMessage(projection, "thread_id=" + Long.toString(threadId),
                        sortOrder, smsTable, pduTable);*/
                cursor = getFirstLockedMessage(projection, "thread_id=" + Long.toString(threadId) + " AND reject = 0",
                    sortOrder, smsTable, pduTable);
                //end tangyisen
                break;
            }
            case URI_FIRST_LOCKED_MESSAGE_ALL: {
                //begin tangyisen
                selection = rebuildSelection(selection, accessRestricted);
                //end tangyisen
                cursor = getFirstLockedMessage(
                        projection, selection, sortOrder, smsTable, pduTable);
                break;
            }
            default:
                throw new IllegalStateException("Unrecognized URI:" + uri);
        }

        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), MmsSms.CONTENT_URI);
        }
        return cursor;
    }

    /**
     * Return the canonical address ID for this address.
     */
    private long getSingleAddressId(String address) {
        boolean isEmail = Mms.isEmailAddress(address);
        boolean isPhoneNumber = Mms.isPhoneNumber(address);

        // We lowercase all email addresses, but not addresses that aren't numbers, because
        // that would incorrectly turn an address such as "My Vodafone" into "my vodafone"
        // and the thread title would be incorrect when displayed in the UI.
        String refinedAddress = isEmail ? address.toLowerCase() : address;

        String selection = "address=?";
        String[] selectionArgs;
        long retVal = -1L;

        if (!isPhoneNumber) {
            selectionArgs = new String[] { refinedAddress };
        } else {
            selection += " OR PHONE_NUMBERS_EQUAL(address, ?, " +
                        (mUseStrictPhoneNumberComparation ? 1 : 0) + ")";
            selectionArgs = new String[] { refinedAddress, refinedAddress };
        }

        Cursor cursor = null;

        try {
            SQLiteDatabase db = mOpenHelper.getReadableDatabase();
            cursor = db.query(
                    "canonical_addresses", ID_PROJECTION,
                    selection, selectionArgs, null, null, null);

            if (cursor.getCount() == 0) {
                //lichao add in 2016-11-24 begin
                String normalizedNumber = refinedAddress.replace("-", "").replace(" ", "");
                //String normalizedNumber = PhoneNumberUtils.normalizeNumber(refinedAddress);
                //lichao add in 2016-11-24 end
                ContentValues contentValues = new ContentValues(1);
                contentValues.put(CanonicalAddressesColumns.ADDRESS, normalizedNumber);

                db = mOpenHelper.getWritableDatabase();
                retVal = db.insert("canonical_addresses",
                        CanonicalAddressesColumns.ADDRESS, contentValues);

                Log.d(LOG_TAG, "getSingleAddressId: insert new canonical_address for " +
                        /*address*/ "xxxxxx" + ", _id=" + retVal);

                return retVal;
            }

            if (cursor.moveToFirst()) {
                retVal = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return retVal;
    }

    /**
     * Return the canonical address IDs for these addresses.
     */
    private Set<Long> getAddressIds(List<String> addresses) {
        Set<Long> result = new HashSet<Long>(addresses.size());

        for (String address : addresses) {
            if (!address.equals(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR)) {
                long id = getSingleAddressId(address);
                if (id != RESULT_FOR_ID_NOT_FOUND) {
                    result.add(id);
                } else {
                    Log.e(LOG_TAG, "getAddressIds: address ID not found for " + address);
                }
            }
        }
        return result;
    }

    /**
     * Return a sorted array of the given Set of Longs.
     */
    private long[] getSortedSet(Set<Long> numbers) {
        int size = numbers.size();
        long[] result = new long[size];
        int i = 0;

        for (Long number : numbers) {
            result[i++] = number;
        }

        if (size > 1) {
            Arrays.sort(result);
        }

        return result;
    }

    /**
     * Return a String of the numbers in the given array, in order,
     * separated by spaces.
     */
    private String getSpaceSeparatedNumbers(long[] numbers) {
        int size = numbers.length;
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < size; i++) {
            if (i != 0) {
                buffer.append(' ');
            }
            buffer.append(numbers[i]);
        }
        return buffer.toString();
    }

    /**
     * Insert a record for a new thread.
     */
    private void insertThread(String recipientIds, int numberOfRecipients) {
        ContentValues values = new ContentValues(4);

        long date = System.currentTimeMillis();
        values.put(ThreadsColumns.DATE, date - date % 1000);
        values.put(ThreadsColumns.RECIPIENT_IDS, recipientIds);
        if (numberOfRecipients > 1) {
            values.put(Threads.TYPE, Threads.BROADCAST_THREAD);
        }
        values.put(ThreadsColumns.MESSAGE_COUNT, 0);

        long result = mOpenHelper.getWritableDatabase().insert(TABLE_THREADS, null, values);
        Log.d(LOG_TAG, "insertThread: created new thread_id " + result +
                " for recipientIds " + /*recipientIds*/ "xxxxxxx");

        getContext().getContentResolver().notifyChange(MmsSms.CONTENT_URI, null, true,
                UserHandle.USER_ALL);
    }

    //add by ligy begin    
    private void insertThread(String recipientIds, int numberOfRecipients, int subId) {
        ContentValues values = new ContentValues(4);

        long date = System.currentTimeMillis();
        values.put(ThreadsColumns.DATE, date - date % 1000);
        values.put(ThreadsColumns.RECIPIENT_IDS, recipientIds);
        if (numberOfRecipients > 1) {
            values.put(Threads.TYPE, Threads.BROADCAST_THREAD);
        }
        values.put(ThreadsColumns.MESSAGE_COUNT, 0);
        values.put("sub_id", subId);

        long result = mOpenHelper.getWritableDatabase().insert(TABLE_THREADS, null, values);
        Log.d(LOG_TAG, "insertThread: created new thread_id " + result +
                " for recipientIds " + /*recipientIds*/ "xxxxxxx");

        getContext().getContentResolver().notifyChange(MmsSms.CONTENT_URI, null, true,
                UserHandle.USER_ALL);
    }
	//add by ligy end

    private static final String THREAD_QUERY =
            "SELECT _id FROM threads " + "WHERE recipient_ids=?";

    /**
     * Return the thread ID for this list of
     * recipients IDs.  If no thread exists with this ID, create
     * one and return it.  Callers should always use
     * Threads.getThreadId to access this information.
     */
    //modify by lgy
    private static final boolean isCreateConversaitonIdBySim = true;
    //private synchronized Cursor getThreadId(List<String> recipients) {
    private synchronized Cursor getThreadId(List<String> recipients, String selection) {
        Set<Long> addressIds = getAddressIds(recipients);
        String recipientIds = "";

        if (addressIds.size() == 0) {
            Log.e(LOG_TAG, "getThreadId: NO receipients specified -- NOT creating thread",
                    new Exception());
            return null;
        } else if (addressIds.size() == 1) {
            // optimize for size==1, which should be most of the cases
            for (Long addressId : addressIds) {
                recipientIds = Long.toString(addressId);
            }
        } else {
            recipientIds = getSpaceSeparatedNumbers(getSortedSet(addressIds));
        }

        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.d(LOG_TAG, "getThreadId: recipientIds (selectionArgs) =" +
                    /*recipientIds*/ "xxxxxxx");
        }

        String[] selectionArgs = new String[] { recipientIds };

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = null;
        try {
            // Find the thread with the given recipients
		    //modify by lgy beign
            //cursor = db.rawQuery(THREAD_QUERY, selectionArgs);
        	boolean useSelection = isCreateConversaitonIdBySim && !TextUtils.isEmpty(selection);
            cursor = db.rawQuery(THREAD_QUERY + (useSelection ? " and "+ selection : ""), selectionArgs);
		    //modify by lgy end

            if (cursor.getCount() == 0) {
                // No thread with those recipients exists, so create the thread.
                cursor.close();

                Log.d(LOG_TAG, "getThreadId: create new thread_id for recipients " +
                        /*recipients*/ "xxxxxxxx");
				//modify by lgy beign
                //insertThread(recipientIds, recipients.size());
                if(useSelection) {
                    int subId = Integer.valueOf(selection.substring(9));
                	insertThread(recipientIds, recipients.size(), subId);
                } else {
                	insertThread(recipientIds, recipients.size());
                }

                // The thread was just created, now find it and return it.
                //cursor = db.rawQuery(THREAD_QUERY, selectionArgs);
                cursor = db.rawQuery(THREAD_QUERY + (useSelection ? " and "+ selection : ""), selectionArgs);
				//modify by lgy end
            }
            db.setTransactionSuccessful();
        } catch (Throwable ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
        } finally {
            db.endTransaction();
        }

        if (cursor != null && cursor.getCount() > 1) {
            Log.w(LOG_TAG, "getThreadId: why is cursorCount=" + cursor.getCount());
        }
        return cursor;
    }
    
    //add by lgy begin
    //lgy merge from ----Cursor getThreadId(List<String> recipients, String selection) {
    private synchronized Cursor queryThreadId(List<String> recipients, String selection) {
        //Set<Long> addressIds = getAddressIds(recipients);
        Set<Long> addressIds = queryAddressIds(recipients);
        String recipientIds = "";

        if (addressIds.size() == 0) {
            Log.e(LOG_TAG, "getThreadId: NO receipients specified -- NOT creating thread",
                    new Exception());
            return null;
        } else if (addressIds.size() == 1) {
            // optimize for size==1, which should be most of the cases
            for (Long addressId : addressIds) {
                recipientIds = Long.toString(addressId);
            }
        } else {
            recipientIds = getSpaceSeparatedNumbers(getSortedSet(addressIds));
        }

        if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
            Log.d(LOG_TAG, "queryThreadId: recipientIds (selectionArgs) =" +
                    /*recipientIds*/ "xxxxxxx");
        }

        String[] selectionArgs = new String[] { recipientIds };

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = null;
        try {
        	boolean useSelection = isCreateConversaitonIdBySim && !TextUtils.isEmpty(selection);
            // Find the threads with the given recipients
            cursor = db.rawQuery(THREAD_QUERY + (useSelection ? " and "+ selection : ""), selectionArgs);
            db.setTransactionSuccessful();
        } catch (Throwable ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
        } finally {
            db.endTransaction();
        }

        if (cursor != null && cursor.getCount() > 1) {
            Log.w(LOG_TAG, "queryThreadId: why is cursorCount=" + cursor.getCount());
        }
        return cursor;
    }

    //lichao add for only query not inset to database in 2016-11-24
    //merge from getAddressIds()
    private Set<Long> queryAddressIds(List<String> addresses) {
        Set<Long> result = new HashSet<Long>(addresses.size());

        for (String address : addresses) {
            if (!address.equals(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR)) {
                long id = querySingleAddressId(address);
                if (id != RESULT_FOR_ID_NOT_FOUND) {
                    result.add(id);
                } else {
                    Log.e(LOG_TAG, "queryAddressIds: address ID not found for " + address);
                }
            }
        }
        return result;
    }

    //lichao add for only query not inset to database in 2016-11-24
    //merge from getSingleAddressId()
    private long querySingleAddressId(String address) {
        boolean isEmail = Mms.isEmailAddress(address);
        boolean isPhoneNumber = Mms.isPhoneNumber(address);

        // We lowercase all email addresses, but not addresses that aren't numbers, because
        // that would incorrectly turn an address such as "My Vodafone" into "my vodafone"
        // and the thread title would be incorrect when displayed in the UI.
        String refinedAddress = isEmail ? address.toLowerCase() : address;

        String selection = "address=?";
        String[] selectionArgs;
        long retVal = -1L;

        if (!isPhoneNumber) {
            selectionArgs = new String[] { refinedAddress };
        } else {
            selection += " OR PHONE_NUMBERS_EQUAL(address, ?, " +
                    (mUseStrictPhoneNumberComparation ? 1 : 0) + ")";
            selectionArgs = new String[] { refinedAddress, refinedAddress };
        }

        Cursor cursor = null;
        try {
            SQLiteDatabase db = mOpenHelper.getReadableDatabase();
            cursor = db.query(
                    "canonical_addresses", ID_PROJECTION,
                    selection, selectionArgs, null, null, null);

            if (null == cursor || cursor.getCount() == 0) {
                return RESULT_FOR_ID_NOT_FOUND;
            }

            if (cursor.moveToFirst()) {
                retVal = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return retVal;
    }
    //add by lgy end

    private static String concatSelections(String selection1, String selection2) {
        if (TextUtils.isEmpty(selection1)) {
            return selection2;
        } else if (TextUtils.isEmpty(selection2)) {
            return selection1;
        } else {
            return selection1 + " AND " + selection2;
        }
    }

    /**
     * If a null projection is given, return the union of all columns
     * in both the MMS and SMS messages tables.  Otherwise, return the
     * given projection.
     */
    private static String[] handleNullMessageProjection(
            String[] projection) {
        return projection == null ? UNION_COLUMNS : projection;
    }

    /**
     * If a null projection is given, return the set of all columns in
     * the threads table.  Otherwise, return the given projection.
     */
    private static String[] handleNullThreadsProjection(
            String[] projection) {
        return projection == null ? THREADS_COLUMNS : projection;
    }

    /**
     * If a null sort order is given, return "normalized_date ASC".
     * Otherwise, return the given sort order.
     */
    private static String handleNullSortOrder (String sortOrder) {
        return sortOrder == null ? "normalized_date ASC" : sortOrder;
    }

    /**
     * Return existing threads in the database.
     */
    private Cursor getSimpleConversations(String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        return mOpenHelper.getReadableDatabase().query(TABLE_THREADS, projection,
                selection, selectionArgs, null, null, " date DESC");
    }

    /**
     * Return the thread which has draft in both MMS and SMS.
     *
     * Use this query:
     *
     *   SELECT ...
     *     FROM (SELECT _id, thread_id, ...
     *             FROM pdu
     *             WHERE msg_box = 3 AND ...
     *           UNION
     *           SELECT _id, thread_id, ...
     *             FROM sms
     *             WHERE type = 3 AND ...
     *          )
     *   ;
     */
    private Cursor getDraftThread(String[] projection, String selection,
            String sortOrder, String smsTable, String pduTable) {
        String[] innerProjection = new String[] {BaseColumns._ID, Conversations.THREAD_ID};
        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setTables(pduTable);
        smsQueryBuilder.setTables(smsTable);

        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerProjection,
                MMS_COLUMNS, 1, "mms",
                concatSelections(selection, Mms.MESSAGE_BOX + "=" + Mms.MESSAGE_BOX_DRAFTS),
                null, null);
        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerProjection,
                SMS_COLUMNS, 1, "sms",
                concatSelections(selection, Sms.TYPE + "=" + Sms.MESSAGE_TYPE_DRAFT),
                null, null);
        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { mmsSubQuery, smsSubQuery }, null, null);

        SQLiteQueryBuilder outerQueryBuilder = new SQLiteQueryBuilder();

        outerQueryBuilder.setTables("(" + unionQuery + ")");

        String outerQuery = outerQueryBuilder.buildQuery(
                projection, null, null, null, sortOrder, null);

        return mOpenHelper.getReadableDatabase().rawQuery(outerQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Return the most recent message in each conversation in both MMS
     * and SMS.
     *
     * Use this query:
     *
     *   SELECT ...
     *     FROM (SELECT thread_id AS tid, date * 1000 AS normalized_date, ...
     *             FROM pdu
     *             WHERE msg_box != 3 AND ...
     *             GROUP BY thread_id
     *             HAVING date = MAX(date)
     *           UNION
     *           SELECT thread_id AS tid, date AS normalized_date, ...
     *             FROM sms
     *             WHERE ...
     *             GROUP BY thread_id
     *             HAVING date = MAX(date))
     *     GROUP BY tid
     *     HAVING normalized_date = MAX(normalized_date);
     *
     * The msg_box != 3 comparisons ensure that we don't include draft
     * messages.
     */
    private Cursor getConversations(String[] projection, String selection,
            String sortOrder, String smsTable, String pduTable) {
        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setTables(pduTable);
        smsQueryBuilder.setTables(smsTable);

        String[] columns = handleNullMessageProjection(projection);
        String[] innerMmsProjection = makeProjectionWithDateAndThreadId(
                UNION_COLUMNS, 1000);
        String[] innerSmsProjection = makeProjectionWithDateAndThreadId(
                UNION_COLUMNS, 1);
        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerMmsProjection,
                MMS_COLUMNS, 1, "mms",
                concatSelections(selection, MMS_CONVERSATION_CONSTRAINT),
                "thread_id", "date = MAX(date)");
        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerSmsProjection,
                SMS_COLUMNS, 1, "sms",
                concatSelections(selection, SMS_CONVERSATION_CONSTRAINT),
                "thread_id", "date = MAX(date)");
        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { mmsSubQuery, smsSubQuery }, null, null);

        SQLiteQueryBuilder outerQueryBuilder = new SQLiteQueryBuilder();

        outerQueryBuilder.setTables("(" + unionQuery + ")");

        String outerQuery = outerQueryBuilder.buildQuery(
                columns, null, "tid",
                "normalized_date = MAX(normalized_date)", sortOrder, null);

        return mOpenHelper.getReadableDatabase().rawQuery(outerQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Return the first locked message found in the union of MMS
     * and SMS messages.
     *
     * Use this query:
     *
     *  SELECT _id FROM pdu GROUP BY _id HAVING locked=1 UNION SELECT _id FROM sms GROUP
     *      BY _id HAVING locked=1 LIMIT 1
     *
     * We limit by 1 because we're only interested in knowing if
     * there is *any* locked message, not the actual messages themselves.
     */
    private Cursor getFirstLockedMessage(String[] projection, String selection,
            String sortOrder, String smsTable, String pduTable) {
        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setTables(pduTable);
        smsQueryBuilder.setTables(smsTable);

        String[] idColumn = new String[] { BaseColumns._ID };

        // NOTE: buildUnionSubQuery *ignores* selectionArgs
        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, idColumn,
                null, 1, "mms",
                selection,
                BaseColumns._ID, "locked=1");

        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, idColumn,
                null, 1, "sms",
                selection,
                BaseColumns._ID, "locked=1");

        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { mmsSubQuery, smsSubQuery }, null, "1");

        Cursor cursor = mOpenHelper.getReadableDatabase().rawQuery(unionQuery, EMPTY_STRING_ARRAY);

        if (DEBUG) {
            Log.v("MmsSmsProvider", "getFirstLockedMessage query: " + unionQuery);
            Log.v("MmsSmsProvider", "cursor count: " + cursor.getCount());
        }
        return cursor;
    }

    /**
     * Return every message in each conversation in both MMS
     * and SMS.
     */
    private Cursor getCompleteConversations(String[] projection,
            String selection, String sortOrder, String smsTable, String pduTable) {
        String unionQuery = buildConversationQuery(projection, selection, sortOrder, smsTable,
                pduTable);

        return mOpenHelper.getReadableDatabase().rawQuery(unionQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Add normalized date and thread_id to the list of columns for an
     * inner projection.  This is necessary so that the outer query
     * can have access to these columns even if the caller hasn't
     * requested them in the result.
     */
    private String[] makeProjectionWithDateAndThreadId(
            String[] projection, int dateMultiple) {
        int projectionSize = projection.length;
        String[] result = new String[projectionSize + 2];

        result[0] = "thread_id AS tid";
        result[1] = "date * " + dateMultiple + " AS normalized_date";
        for (int i = 0; i < projectionSize; i++) {
            result[i + 2] = projection[i];
        }
        return result;
    }

    /**
     * Return the union of MMS and SMS messages for this thread ID.
     */
    private Cursor getConversationMessages(
            String threadIdString, String[] projection, String selection,
            String sortOrder, String smsTable, String pduTable) {
        try {
            Long.parseLong(threadIdString);
        } catch (NumberFormatException exception) {
            Log.e(LOG_TAG, "Thread ID must be a Long.");
            return null;
        }

        String finalSelection = concatSelections(
                selection, "thread_id = " + threadIdString);
        String unionQuery = buildConversationQuery(projection, finalSelection, sortOrder, smsTable,
                pduTable);

        return mOpenHelper.getReadableDatabase().rawQuery(unionQuery, EMPTY_STRING_ARRAY);
    }

    //7.0 delete begin
    /**
     * Return the union of MMS and SMS messages in one mailbox.
     */
    private Cursor getMailboxMessages(String mailboxId, String[] projection,
            String selection, String[] selectionArgs, String sortOrder,
            boolean read, String pduTable) {
        try {
            Integer.parseInt(mailboxId);
        } catch (NumberFormatException exception) {
            Log.e(LOG_TAG, "mailboxId must be a Long.");
            return null;
        }
        String unionQuery = buildMailboxMsgQuery(mailboxId, projection,
                selection, selectionArgs, sortOrder, read, pduTable);

        if (DEBUG) {
            Log.w(LOG_TAG, "getMailboxMessages : unionQuery =" + unionQuery);
        }

        return mOpenHelper.getReadableDatabase().rawQuery(unionQuery,
                EMPTY_STRING_ARRAY);
    }

    private static String appendSmsSelection(String selection) {
        if (!isMmsSelection(selection)) {
            return appendSelection(selection);
        }
        return "";
    }

    private static String appendMmsSelection(String selection) {
        if (!isSmsSelection(selection)) {
            return appendSelection(selection);
        }
        return "";
    }

    private static String appendSelection(String selection) {
        return TextUtils.isEmpty(selection) ? "" : " AND " + selection;
    }

    private static boolean isSmsSelection(String selection) {
        return !TextUtils.isEmpty(selection) && selection.contains("sms.");
    }

    private static boolean isMmsSelection(String selection) {
        return !TextUtils.isEmpty(selection) && selection.contains("pdu.");
    }

    private static String buildMailboxMsgQuery(String mailboxId,
            String[] projection, String selection, String[] selectionArgs,
            String sortOrder, boolean read, String pduTable) {
        String[] mmsProjection = createMmsMailboxProjection(projection);
        String[] smsProjection = createSmsMailboxProjection(projection);

        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setDistinct(true);
        smsQueryBuilder.setDistinct(true);
        mmsQueryBuilder.setTables("threads, " + joinPduAndPendingMsgTables(pduTable));
        smsQueryBuilder.setTables(SmsProvider.TABLE_SMS + ", threads ");

        String[] smsColumns = handleNullMessageProjection(smsProjection);
        String[] mmsColumns = handleNullMessageProjection(mmsProjection);
        String[] innerMmsProjection = makeMmsProjectionWithNormalizedDate(
                mmsColumns, 1000);
        String[] innerSmsProjection = makeSmsProjectionWithNormalizedDate(
                smsColumns, 1);

        Set<String> columnsPresentInTable = new HashSet<String>(MMS_COLUMNS);
        columnsPresentInTable.add("pdu._id AS _id");
        columnsPresentInTable.add("pdu.date AS date");
        columnsPresentInTable.add("pdu.read AS read");
        columnsPresentInTable.add("pdu.sub_id AS sub_id");
        columnsPresentInTable.add("recipient_ids");

        columnsPresentInTable.add(PendingMessages.ERROR_TYPE);
        String compare = " = ";
        int boxidInt = Integer.parseInt(mailboxId);
        if (boxidInt >= 4) {
            compare = " >= ";
        }

        String appendSmsSelection = appendSmsSelection(selection);
        String appendMmsSelection = appendMmsSelection(selection);
        String mmsSelection = Mms.MESSAGE_BOX + compare + mailboxId
                + " AND thread_id = threads._id AND m_type != "
                + PduHeaders.MESSAGE_TYPE_DELIVERY_IND + appendMmsSelection;
        String smsSelection = "(sms." + Sms.TYPE + compare + mailboxId
                + " AND thread_id = threads._id" + appendSmsSelection
                + ")" + " OR (sms." + Sms.TYPE + compare + mailboxId
                + " AND thread_id ISNULL " + appendSmsSelection
                + ")";

        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerMmsProjection,
                columnsPresentInTable, 0, "mms", mmsSelection, selectionArgs,
                null, null);

        Set<String> columnsPresentInSmsTable = new HashSet<String>(SMS_COLUMNS);
        columnsPresentInSmsTable.add("sms._id AS _id");
        columnsPresentInSmsTable.add("sms.date AS date");
        columnsPresentInSmsTable.add("sms.read AS read");
        columnsPresentInSmsTable.add("sms.type AS type");
        columnsPresentInSmsTable.add("sms.sub_id AS sub_id");
        columnsPresentInSmsTable.add("recipient_ids");

        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerSmsProjection,
                columnsPresentInSmsTable, 0, "sms", smsSelection,
                selectionArgs, null, null);

        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();
        String unionQuery = null;
        if (isMmsSelection(selection)) {
            unionQuery = mmsSubQuery;
        } else {
            unionQuery = unionQueryBuilder.buildUnionQuery(new String[] {
                    mmsSubQuery, smsSubQuery
            }, null, null);
        }
        if (DEBUG) {
            Log.w(LOG_TAG, "buildMailboxMsgQuery : unionQuery = " + unionQuery);
        }

        SQLiteQueryBuilder outerQueryBuilder = new SQLiteQueryBuilder();
        outerQueryBuilder.setTables("(" + unionQuery + ")");

        return outerQueryBuilder.buildQuery(projection, null, null, null, null,
                sortOrder, null);
    }
    //7.0 delete end

    /**
     * Return the SMS messages count on phone
     */
    private Cursor getAllMessagesCount() {
        //tangyisen just count sms,add reject
        String unionQuery = "select sum(a) AS count, 1 AS _id "
                + "from (" + "select count(sms._id) as a, 2 AS b from sms, threads"
                + " where reject = 0 AND thread_id NOTNULL AND thread_id = threads._id)";

        return mOpenHelper.getReadableDatabase().rawQuery(unionQuery, EMPTY_STRING_ARRAY);
    }

    //begin tangyisen
    /*private Cursor getAllMessagesCount(boolean accessRestricted) {
        String unionQuery = null;
        if(accessRestricted)
         unionQuery = "select sum(a) AS count, 1 AS _id "
                + "from (" + "select count(sms._id) as a, 2 AS b from sms, threads"
                + " where thread_id NOTNULL AND thread_id = threads._id)";

        return mOpenHelper.getReadableDatabase().rawQuery(unionQuery, EMPTY_STRING_ARRAY);
    }*/
    //end tangyisen
    /**
     * Return the union of MMS and SMS messages whose recipients
     * included this phone number.
     *
     * Use this query:
     *
     * SELECT ...
     *   FROM pdu, (SELECT msg_id AS address_msg_id
     *              FROM addr
     *              WHERE (address='<phoneNumber>' OR
     *              PHONE_NUMBERS_EQUAL(addr.address, '<phoneNumber>', 1/0)))
     *             AS matching_addresses
     *   WHERE pdu._id = matching_addresses.address_msg_id
     * UNION
     * SELECT ...
     *   FROM sms
     *   WHERE (address='<phoneNumber>' OR PHONE_NUMBERS_EQUAL(sms.address, '<phoneNumber>', 1/0));
     */
    private Cursor getMessagesByPhoneNumber(
            String phoneNumber, String[] projection, String selection,
            String sortOrder, String smsTable, String pduTable) {
        String escapedPhoneNumber = DatabaseUtils.sqlEscapeString(phoneNumber);
        String finalMmsSelection =
                concatSelections(
                        selection,
                        pduTable + "._id = matching_addresses.address_msg_id");
        String finalSmsSelection =
                concatSelections(
                        selection,
                        "(address=" + escapedPhoneNumber + " OR PHONE_NUMBERS_EQUAL(address, " +
                        escapedPhoneNumber +
                        (mUseStrictPhoneNumberComparation ? ", 1))" : ", 0))"));
        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setDistinct(true);
        smsQueryBuilder.setDistinct(true);
        mmsQueryBuilder.setTables(
                pduTable +
                ", (SELECT msg_id AS address_msg_id " +
                "FROM addr WHERE (address=" + escapedPhoneNumber +
                " OR PHONE_NUMBERS_EQUAL(addr.address, " +
                escapedPhoneNumber +
                (mUseStrictPhoneNumberComparation ? ", 1))) " : ", 0))) ") +
                "AS matching_addresses");
        smsQueryBuilder.setTables(smsTable);

        String[] columns = handleNullMessageProjection(projection);
        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, columns, MMS_COLUMNS,
                0, "mms", finalMmsSelection, null, null);
        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, columns, SMS_COLUMNS,
                0, "sms", finalSmsSelection, null, null);
        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { mmsSubQuery, smsSubQuery }, sortOrder, null);

        return mOpenHelper.getReadableDatabase().rawQuery(unionQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Return the conversation of certain thread ID.
     */
    private Cursor getConversationById(
            String threadIdString, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        try {
            Long.parseLong(threadIdString);
        } catch (NumberFormatException exception) {
            Log.e(LOG_TAG, "Thread ID must be a Long.");
            return null;
        }

        String extraSelection = "_id=" + threadIdString;
        String finalSelection = concatSelections(selection, extraSelection);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        String[] columns = handleNullThreadsProjection(projection);

        queryBuilder.setDistinct(true);
        queryBuilder.setTables(TABLE_THREADS);
        return queryBuilder.query(
                mOpenHelper.getReadableDatabase(), columns, finalSelection,
                selectionArgs, sortOrder, null, null);
    }

    private static String joinPduAndPendingMsgTables(String pduTable) {
        return pduTable + " LEFT JOIN " + TABLE_PENDING_MSG
                + " ON " + pduTable + "._id = pending_msgs.msg_id";
    }

    private static String[] createMmsProjection(String[] old, String pduTable) {
        String[] newProjection = new String[old.length];
        for (int i = 0; i < old.length; i++) {
            if (old[i].equals(BaseColumns._ID)) {
                newProjection[i] = pduTable + "._id";
            } else {
                newProjection[i] = old[i];
            }
        }
        return newProjection;
    }

    //7.0 delete begin
    private static String[] createMmsMailboxProjection(String[] old) {
        if (old == null) {
            return null;
        }

        int length = old.length;
        String[] newProjection = new String[length];
        for (int i = 0; i < length; i++) {
            if (old[i].equals(BaseColumns._ID)) {
                newProjection[i] = "pdu._id AS _id";
            } else if (old[i].equals("date")) {
                newProjection[i] = "pdu.date AS date";
            } else if (old[i].equals("read")) {
                newProjection[i] = "pdu.read AS read";
            } else if (old[i].equals("sub_id")) {
                newProjection[i] = "pdu.sub_id AS sub_id";
            } else {
                newProjection[i] = old[i];
            }
        }
        return newProjection;
    }

    private static String[] createSmsMailboxProjection(String[] old) {
        if (old == null) {
            return null;
        }

        int length = old.length;
        String[] newProjection = new String[length];
        for (int i = 0; i < length; i++) {
            if (old[i].equals(BaseColumns._ID)) {
                newProjection[i] = "sms._id AS _id";
            } else if (old[i].equals("date")) {
                newProjection[i] = "sms.date AS date";
            } else if (old[i].equals("read")) {
                newProjection[i] = "sms.read AS read";
            } else if (old[i].equals("type")) {
                newProjection[i] = "sms.type AS type";
            } else if (old[i].equals("sub_id")) {
                newProjection[i] = "sms.sub_id AS sub_id";
            } else {
                newProjection[i] = old[i];
            }
        }
        return newProjection;
    }
    //7.0 delete end

    private Cursor getUndeliveredMessages(
            String[] projection, String selection, String[] selectionArgs,
            String sortOrder, String smsTable, String pduTable) {
        String[] mmsProjection = createMmsProjection(projection, pduTable);

        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setTables(joinPduAndPendingMsgTables(pduTable));
        smsQueryBuilder.setTables(smsTable);

        String finalMmsSelection = concatSelections(
                selection, Mms.MESSAGE_BOX + " = " + Mms.MESSAGE_BOX_OUTBOX);
        String finalSmsSelection = concatSelections(
                selection, "(" + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_OUTBOX
                + " OR " + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_FAILED
                + " OR " + Sms.TYPE + " = " + Sms.MESSAGE_TYPE_QUEUED + ")");

        String[] smsColumns = handleNullMessageProjection(projection);
        String[] mmsColumns = handleNullMessageProjection(mmsProjection);
        String[] innerMmsProjection = makeProjectionWithDateAndThreadId(
                mmsColumns, 1000);
        String[] innerSmsProjection = makeProjectionWithDateAndThreadId(
                smsColumns, 1);

        Set<String> columnsPresentInTable = new HashSet<String>(MMS_COLUMNS);
        columnsPresentInTable.add(pduTable + "._id");
        columnsPresentInTable.add(PendingMessages.ERROR_TYPE);
        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerMmsProjection,
                columnsPresentInTable, 1, "mms", finalMmsSelection,
                null, null);
        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerSmsProjection,
                SMS_COLUMNS, 1, "sms", finalSmsSelection,
                null, null);
        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { smsSubQuery, mmsSubQuery }, null, null);

        SQLiteQueryBuilder outerQueryBuilder = new SQLiteQueryBuilder();

        outerQueryBuilder.setTables("(" + unionQuery + ")");

        String outerQuery = outerQueryBuilder.buildQuery(
                smsColumns, null, null, null, sortOrder, null);

        return mOpenHelper.getReadableDatabase().rawQuery(outerQuery, EMPTY_STRING_ARRAY);
    }

    /**
     * Add normalized date to the list of columns for an inner
     * projection.
     */
    private static String[] makeProjectionWithNormalizedDate(
            String[] projection, int dateMultiple) {
        int projectionSize = projection.length;
        String[] result = new String[projectionSize + 1];

        result[0] = "date * " + dateMultiple + " AS normalized_date";
        System.arraycopy(projection, 0, result, 1, projectionSize);
        return result;
    }

    //7.0 delete mailbox codes begin
    /**
     * Add normalized date to the list of columns for an inner
     * projection.
     */
    private static String[] makeSmsProjectionWithNormalizedDate(
            String[] projection, int dateMultiple) {
        int projectionSize = projection.length;
        String[] result = new String[projectionSize + 1];

        result[0] = "sms.date * " + dateMultiple + " AS normalized_date";
        System.arraycopy(projection, 0, result, 1, projectionSize);
        return result;
    }

    private static String[] makeMmsProjectionWithNormalizedDate(
            String[] projection, int dateMultiple) {
        int projectionSize = projection.length;
        String[] result = new String[projectionSize + 1];

        result[0] = "pdu.date * " + dateMultiple + " AS normalized_date";
        System.arraycopy(projection, 0, result, 1, projectionSize);
        return result;
    }
    //7.0 delete mailbox codes end

    private static String buildConversationQuery(String[] projection,
            String selection, String sortOrder, String smsTable, String pduTable) {
        String[] mmsProjection = createMmsProjection(projection, pduTable);

        SQLiteQueryBuilder mmsQueryBuilder = new SQLiteQueryBuilder();
        SQLiteQueryBuilder smsQueryBuilder = new SQLiteQueryBuilder();

        mmsQueryBuilder.setDistinct(true);
        smsQueryBuilder.setDistinct(true);
        mmsQueryBuilder.setTables(joinPduAndPendingMsgTables(pduTable));
        smsQueryBuilder.setTables(smsTable);

        String[] smsColumns = handleNullMessageProjection(projection);
        String[] mmsColumns = handleNullMessageProjection(mmsProjection);
        String[] innerMmsProjection = makeProjectionWithNormalizedDate(mmsColumns, 1000);
        String[] innerSmsProjection = makeProjectionWithNormalizedDate(smsColumns, 1);

        Set<String> columnsPresentInTable = new HashSet<String>(MMS_COLUMNS);
        columnsPresentInTable.add(pduTable + "._id");
        columnsPresentInTable.add(PendingMessages.ERROR_TYPE);

        String mmsSelection = concatSelections(selection,
                                Mms.MESSAGE_BOX + " != " + Mms.MESSAGE_BOX_DRAFTS);
        String mmsSubQuery = mmsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerMmsProjection,
                columnsPresentInTable, 0, "mms",
                concatSelections(mmsSelection, MMS_CONVERSATION_CONSTRAINT),
                null, null);
        String smsSubQuery = smsQueryBuilder.buildUnionSubQuery(
                MmsSms.TYPE_DISCRIMINATOR_COLUMN, innerSmsProjection, SMS_COLUMNS,
                0, "sms", concatSelections(selection, SMS_CONVERSATION_CONSTRAINT),
                null, null);
        SQLiteQueryBuilder unionQueryBuilder = new SQLiteQueryBuilder();

        unionQueryBuilder.setDistinct(true);

        String unionQuery = unionQueryBuilder.buildUnionQuery(
                new String[] { smsSubQuery, mmsSubQuery },
                handleNullSortOrder(sortOrder), null);

        SQLiteQueryBuilder outerQueryBuilder = new SQLiteQueryBuilder();

        outerQueryBuilder.setTables("(" + unionQuery + ")");

        return outerQueryBuilder.buildQuery(
                smsColumns, null, null, null, sortOrder, null);
    }

    @Override
    public String getType(Uri uri) {
        return VND_ANDROID_DIR_MMS_SMS;
    }

    @Override
    public int delete(Uri uri, String selection,
            String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Context context = getContext();
        int affectedRows = 0;

        switch(URI_MATCHER.match(uri)) {
            case URI_CONVERSATIONS_MESSAGES:
                long threadId;
                try {
                    threadId = Long.parseLong(uri.getLastPathSegment());
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "Thread ID must be a long.");
                    break;
                }
                affectedRows = deleteConversation(uri, selection, selectionArgs);
                MmsSmsDatabaseHelper.updateThread(db, threadId);
                break;
            case URI_CONVERSATIONS:
                //tangyisen add reject
                String finalSelection = null;
                if (!selection.contains("reject")) {
                    finalSelection = concatSelections(selection, "reject = 0");
                } else {
                    finalSelection = selection;
                }
                affectedRows = MmsProvider.deleteMessages(context, db,
                                        finalSelection, selectionArgs, uri)
                        + db.delete("sms", finalSelection, selectionArgs);
                // Intentionally don't pass the selection variable to updateAllThreads.
                // When we pass in "locked=0" there, the thread will get excluded from
                // the selection and not get updated.
                MmsSmsDatabaseHelper.updateAllThreads(db, null, null);
                break;
            case URI_OBSOLETE_THREADS:
                //tangyisen can not add reject,just has sms or mms,not delete the thread
                affectedRows = db.delete(TABLE_THREADS,
                        "_id NOT IN (SELECT DISTINCT thread_id FROM sms where thread_id NOT NULL " +
                        "UNION SELECT DISTINCT thread_id FROM pdu where thread_id NOT NULL)", null);
                break;
            default:
                throw new UnsupportedOperationException(NO_DELETES_INSERTS_OR_UPDATES + uri);
        }

        if (affectedRows > 0) {
            context.getContentResolver().notifyChange(MmsSms.CONTENT_URI, null, true,
                    UserHandle.USER_ALL);
        }
        return affectedRows;
    }

    /**
     * Delete the conversation with the given thread ID.
     */
    private int deleteConversation(Uri uri, String selection, String[] selectionArgs) {
        String threadId = uri.getLastPathSegment();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        //tangyisen add reject
        String finalSelection = null;
        if (!selection.contains("reject")) {
            finalSelection = concatSelections(selection, "thread_id = " + threadId + " AND reject = 0");
        } else {
            finalSelection = concatSelections(selection, "thread_id = " + threadId);
        }
        //String finalSelection = concatSelections(selection, "thread_id = " + threadId);
        return MmsProvider.deleteMessages(getContext(), db, finalSelection,
                                          selectionArgs, uri)
                + db.delete("sms", finalSelection, selectionArgs);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (URI_MATCHER.match(uri) == URI_PENDING_MSG) {
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            long rowId = db.insert(TABLE_PENDING_MSG, null, values);
            return Uri.parse(uri + "/" + rowId);
        }
        throw new UnsupportedOperationException(NO_DELETES_INSERTS_OR_UPDATES + uri);
    }

    @Override
    public int update(Uri uri, ContentValues values,
            String selection, String[] selectionArgs) {
        final int callerUid = Binder.getCallingUid();
        final String callerPkg = getCallingPackage();
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int affectedRows = 0;
        //tangyisen not need add reject,because not access sms or pdu
        switch(URI_MATCHER.match(uri)) {
            case URI_CONVERSATIONS_MESSAGES:
                String threadIdString = uri.getPathSegments().get(1);
                //NB someone add ?
                if (values.containsKey(Threads.NOTIFICATION) ||
                        values.containsKey(Threads.ATTACHMENT_INFO)) {
                    String finalSelection = concatSelections(selection, "_id=" + threadIdString);
                    affectedRows = db.update(TABLE_THREADS, values, finalSelection, null);
                } else {
                    affectedRows = updateConversation(threadIdString, values,
                            selection, selectionArgs, callerUid, callerPkg);
                }
                break;

            case URI_PENDING_MSG:
                affectedRows = db.update(TABLE_PENDING_MSG, values, selection, null);
                break;

            case URI_CANONICAL_ADDRESS: {
                String extraSelection = "_id=" + uri.getPathSegments().get(1);
                String finalSelection = TextUtils.isEmpty(selection)
                        ? extraSelection : extraSelection + " AND " + selection;

                affectedRows = db.update(TABLE_CANONICAL_ADDRESSES, values, finalSelection, null);
                break;
            }

            case URI_CONVERSATIONS: {
                final ContentValues finalValues = new ContentValues(1);
                if (values.containsKey(Threads.ARCHIVED)) {
                    // Only allow update archived
                    finalValues.put(Threads.ARCHIVED, values.getAsBoolean(Threads.ARCHIVED));
                }
                affectedRows = db.update(TABLE_THREADS, finalValues, selection, selectionArgs);
                break;
            }

            //7.0 delete URI_UPDATE_THREAD
			/*
            case URI_UPDATE_THREAD:
                long threadId;
                try {
                    threadId = Long.parseLong(uri.getLastPathSegment());
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "Thread ID must be a long.");
                    break;
                }
                updateConversationUnread(db, threadId);
                MmsSmsDatabaseHelper.updateThread(db, threadId);
                break;
			*/

            case URI_UPDATE_THREAD_DATE:
                MmsSmsDatabaseHelper.updateThreadsDate(db, selection, selectionArgs);
                break;
            //begin tangyisen
            case MST_URI_UPDATE_THREAD:
                /*long threadId  = Long.parseLong(uri.getQueryParameter("threadId"));//Long.parseLong(uri.getLastPathSegment());
                 * try {
                    threadId = Long.parseLong(uri.getLastPathSegment());
                } catch (NumberFormatException e) {
                    Log.e(LOG_TAG, "Thread ID must be a long.");
                    break;
                }
                MmsSmsDatabaseHelper.updateThread(db, threadId);*/
                Long threadId = values.getAsLong("threadId");
                if(threadId != null) {
                    MmsSmsDatabaseHelper.updateThread(db, threadId.longValue());
                }
                break;
            //end tangyisen
            default:
                throw new UnsupportedOperationException(
                        NO_DELETES_INSERTS_OR_UPDATES + uri);
        }

        if (affectedRows > 0) {
            getContext().getContentResolver().notifyChange(
                    MmsSms.CONTENT_URI, null, true, UserHandle.USER_ALL);
        }
        return affectedRows;
    }

    //7.0 delete URI_UPDATE_THREAD
	/*
    private void updateConversationUnread(SQLiteDatabase db, long threadId) {
        db.execSQL(
                "  UPDATE threads SET read = " +
                        "     CASE (SELECT COUNT(*) FROM (SELECT read FROM " +
                        "     sms WHERE read = 0 AND thread_id = " + threadId +
                        "     UNION ALL SELECT read FROM pdu WHERE read = 0 " +
                        "     AND thread_id = " + threadId +"))" +
                        "     WHEN 0 THEN 1 ELSE 0 END;");
    }
	*/

    private int updateConversation(String threadIdString, ContentValues values, String selection,
            String[] selectionArgs, int callerUid, String callerPkg) {
        try {
            Long.parseLong(threadIdString);
        } catch (NumberFormatException exception) {
            Log.e(LOG_TAG, "Thread ID must be a Long.");
            return 0;

        }
        if (ProviderUtil.shouldRemoveCreator(values, callerUid)) {
            // CREATOR should not be changed by non-SYSTEM/PHONE apps
            Log.w(LOG_TAG, callerPkg + " tries to update CREATOR");
            // Sms.CREATOR and Mms.CREATOR are same. But let's do this
            // twice in case the names may differ in the future
            values.remove(Sms.CREATOR);
            values.remove(Mms.CREATOR);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String finalSelection = concatSelections(selection, "thread_id=" + threadIdString);
        return db.update(MmsProvider.TABLE_PDU, values, finalSelection, selectionArgs)
                + db.update("sms", values, finalSelection, selectionArgs);
    }

    /**
     * Construct Sets of Strings containing exactly the columns
     * present in each table.  We will use this when constructing
     * UNION queries across the MMS and SMS tables.
     */
    private static void initializeColumnSets() {
        int commonColumnCount = MMS_SMS_COLUMNS.length;
        int mmsOnlyColumnCount = MMS_ONLY_COLUMNS.length;
        int smsOnlyColumnCount = SMS_ONLY_COLUMNS.length;
        Set<String> unionColumns = new HashSet<String>();

        for (int i = 0; i < commonColumnCount; i++) {
            MMS_COLUMNS.add(MMS_SMS_COLUMNS[i]);
            SMS_COLUMNS.add(MMS_SMS_COLUMNS[i]);
            unionColumns.add(MMS_SMS_COLUMNS[i]);
        }
        for (int i = 0; i < mmsOnlyColumnCount; i++) {
            MMS_COLUMNS.add(MMS_ONLY_COLUMNS[i]);
            unionColumns.add(MMS_ONLY_COLUMNS[i]);
        }
        for (int i = 0; i < smsOnlyColumnCount; i++) {
            SMS_COLUMNS.add(SMS_ONLY_COLUMNS[i]);
            unionColumns.add(SMS_ONLY_COLUMNS[i]);
        }

        int i = 0;
        for (String columnName : unionColumns) {
            UNION_COLUMNS[i++] = columnName;
        }
    }

    @Override
    public void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        // Dump default SMS app
        String defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(getContext());
        if (TextUtils.isEmpty(defaultSmsApp)) {
            defaultSmsApp = "None";
        }
        writer.println("Default SMS app: " + defaultSmsApp);
    }

    //6.0 methord
    private Cursor getSearchMessages(Uri uri, SQLiteDatabase db) {
        int searchMode = Integer.parseInt(uri.getQueryParameter("search_mode"));
        String keyStr = uri.getQueryParameter("key_str");
        int matchWhole = Integer.parseInt(uri.getQueryParameter("match_whole"));
        if (DEBUG) Log.d(LOG_TAG, "getSearchMessages : searchMode =" + searchMode + ",keyStr="
                    + keyStr + ",matchWhole=" + matchWhole);

        String threadIdString = getThreadIdString(keyStr, matchWhole);

        String smsQuery = getSmsQueryString(searchMode, matchWhole, threadIdString);
        String mmsQuery = getMmsQueryString(searchMode, matchWhole, threadIdString);
        String searchString = "%" + addEscapeCharacter(keyStr) + "%";
        String rawQuery = String.format(
                "%s UNION %s  ORDER BY date DESC",
                smsQuery,
                mmsQuery);
        if(DEBUG) Log.d(LOG_TAG, "getSearchMessages : rawQuery =" + rawQuery);//lichao add

        String[] strArray;
        if (searchMode == SEARCH_MODE_CONTENT
                || (searchMode == SEARCH_MODE_NUMBER && matchWhole == MATCH_BY_ADDRESS)) {
            //smsQuery need one and mmsQuery need one searchString for match
            strArray = new String[] {searchString, searchString};
        } else if (searchMode == SEARCH_MODE_SUBJECT) {
            rawQuery = String.format("%s ORDER BY date DESC", mmsQuery);
            //noly mmsQuery need one searchString for match
            strArray = new String[] {searchString};
        } else {
            //matched by threadIdString, no need searchString
            strArray = EMPTY_STRING_ARRAY;
        }
        return db.rawQuery(rawQuery, strArray);
    }
	
	//NB7.0 modify getSearchMessages(Uri uri, SQLiteDatabase db) to this
    private Cursor getSearchMessages(Uri uri, SQLiteDatabase db,
                                     String smsTable, String pduTable) {
        int searchMode = Integer.parseInt(uri.getQueryParameter("search_mode"));
        String keyStr = uri.getQueryParameter("key_str");
        String address = uri.getQueryParameter("contact_addr");
        String searchString = "%" + addEscapeCharacter(keyStr) + "%";
        String threadIdString = DEFAULT_STRING_ZERO;

        if (searchMode == SEARCH_MODE_NAME) {
            threadIdString = getThreadIdByAddress(address);
            if (threadIdString.equals(DEFAULT_STRING_ZERO)) {
                searchMode = SEARCH_MODE_CONTENT;
            }
        }
        if (DEBUG) {
            Log.d(LOG_TAG, "keystr=" + searchString +
                    "|searchMode=" + searchMode + "|address=" + address);
        }
        String rawQuery = getConversationQueryString(searchMode, smsTable,
                pduTable, threadIdString);
        String[] strArray = new String[]{searchString, searchString, searchString,
                searchString, searchString};
        return db.rawQuery(rawQuery, strArray);
    }
	
	// lichao add in 2016-12-07 begin
    private Cursor getSearchThreads(Uri uri, SQLiteDatabase db) {
        int searchMode = Integer.parseInt(uri.getQueryParameter("search_mode"));
        if (DEBUG) Log.d(LOG_TAG, "\n\n getSearchThreads : searchMode=" + searchMode);

        String keyStr = uri.getQueryParameter("key_str");
        if (DEBUG) Log.d(LOG_TAG, "getSearchThreads : keyStr=" + keyStr);

        int matchWhole = Integer.parseInt(uri.getQueryParameter("match_whole"));
        if (DEBUG) Log.d(LOG_TAG, "getSearchThreads : matchWhole=" + matchWhole);

        String separatedAddress = uri.getQueryParameter("addresses");
        if (DEBUG) Log.d(LOG_TAG, "getSearchThreads : separatedAddress=" + separatedAddress);

        //for Blacklist
        String blacklist = uri.getQueryParameter("blacklist");
        if (DEBUG) Log.d(LOG_TAG, "getSearchThreads : blacklist=" + blacklist);

        if (searchMode == SEARCH_MODE_NUMBER && matchWhole == MATCH_BY_ADDRESS
                && !TextUtils.isEmpty(keyStr)) {
            String searchString = "%" + addEscapeCharacter(keyStr) + "%";
            if (DEBUG) {
                Log.d(LOG_TAG, "getSearchThreads, searchString=" + searchString);
            }
            String[] strArray = new String[] {searchString};
            String rawQuery = getThreadQueryStringMatchByAddress();
            if (DEBUG) {
                Log.d(LOG_TAG, "getSearchThreads, 111, rawQuery=" + rawQuery);
            }
            return db.rawQuery(rawQuery, strArray);
        }
        if(searchMode == SEARCH_MODE_NAME && matchWhole == MATCH_BY_THREAD_ID
                && !TextUtils.isEmpty(separatedAddress)){
            String threadIdString = DEFAULT_STRING_ZERO;
            if(searchMode == SEARCH_MODE_NAME){
                threadIdString = getThreadIdString(separatedAddress, matchWhole);
            }
            if (DEBUG) {
                Log.d(LOG_TAG, "getSearchThreads, threadIdString=" + threadIdString);
            }
            if(TextUtils.isEmpty(threadIdString) || threadIdString == DEFAULT_STRING_ZERO){
                return null;
            }
            String[] strArray = EMPTY_STRING_ARRAY;
            String rawQuery = getThreadQueryStringMatchByThreadId(threadIdString);
            if (DEBUG) {
                Log.d(LOG_TAG, "getSearchThreads, 222, rawQuery=" + rawQuery);
            }
            return db.rawQuery(rawQuery, strArray);
        }
        if (searchMode == SEARCH_MODE_BLACK && matchWhole == MATCH_BY_ADDRESS) {
            String[] strArray = EMPTY_STRING_ARRAY;
            String rawQuery = getThreadQueryStringForNoBlack(blacklist);
            if (DEBUG) {
                Log.d(LOG_TAG, "getSearchThreads, 333, rawQuery=" + rawQuery);
            }
            return db.rawQuery(rawQuery, strArray);
        }
        return null;
    }
    // lichao add in 2016-12-07 end

    //6.0 methord
    private String getThreadIdString(String keyStr, int matchWhole) {
        if (matchWhole != MATCH_BY_THREAD_ID) {
            return "";
        }
        long[] addressIdSet = getSortedSet(getAddressIdsByAddressList(keyStr.split(",")));
        String threadIdString = getCommaSeparatedId(addressIdSet);
        if (TextUtils.isEmpty(threadIdString)) {
            threadIdString = DEFAULT_STRING_ZERO;
        }
        return threadIdString;
    }
	
	//7.0 modify getThreadIdString(String keyStr, int matchWhole) to this
    private String getThreadIdByAddress(String keyStr) {
        long[] addressIdSet = getSortedSet(getThreadIdsByAddressList(keyStr.split(",")));
        String threadIdString = getCommaSeparatedId(addressIdSet);
        if (TextUtils.isEmpty(threadIdString)) {
            threadIdString = DEFAULT_STRING_ZERO;
        }
        if (DEBUG) {
            Log.d(LOG_TAG, "getThreadIdByAddress=" + threadIdString);
        }
        return threadIdString;
    }

    private String addEscapeCharacter(String keyStr) {
        if (keyStr == null) {
            return keyStr;
        }
        if (keyStr.contains("%") ||
                keyStr.contains(String.valueOf(SEARCH_ESCAPE_CHARACTER))) {
            StringBuilder searchKeyStrBuilder = new StringBuilder();
            int keyStrLen = keyStr.length();
            for (int i = 0; i < keyStrLen; i++) {
                if (keyStr.charAt(i) == '%' ||
                        keyStr.charAt(i) == SEARCH_ESCAPE_CHARACTER) {
                    searchKeyStrBuilder.append(SEARCH_ESCAPE_CHARACTER);
                    searchKeyStrBuilder.append(keyStr.charAt(i));
                    continue;
                }
                searchKeyStrBuilder.append(keyStr.charAt(i));
            }
            return searchKeyStrBuilder.toString();
        }
        return keyStr;
    }

    //6.0 methord
    private Set<Long> getAddressIdsByAddressList(String[] addresses) {
        int count = addresses.length;
        Set<Long> result = new HashSet<Long>(count);

        for (int i = 0; i < count; i++) {
            String address = addresses[i];
            if (address != null && !address.equals(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR)) {
                long id = getSingleThreadId(address);
                if (id != RESULT_FOR_ID_NOT_FOUND) {
                    result.add(id);
                } else {
                    Log.e(LOG_TAG, "Address ID not found for: " + address);
                }
            }
        }
        return result;
    }
	
	//7.0 modify getAddressIdsByAddressList(String[] addresses) to this
    private Set<Long> getThreadIdsByAddressList(String[] addresses) {
        int count = addresses.length;
        Set<Long> result = new HashSet<Long>(count);

        for (int i = 0; i < count; i++) {
            String address = addresses[i];
            if (address != null && !address.equals(PduHeaders.FROM_INSERT_ADDRESS_TOKEN_STR)) {
                long id = getSingleThreadId(address);
                if (id != RESULT_FOR_ID_NOT_FOUND) {
                    result.add(id);
                } else {
                    Log.e(LOG_TAG, "Address ID not found for: " + address);
                }
            }
        }
        return result;
    }

    private String getCommaSeparatedId(long[] addrIds) {
        int size = addrIds.length;
        StringBuilder buffer = new StringBuilder();

        for (int i = 0; i < size; i++) {
            if (i != 0) {
                buffer.append(',');
            }
            buffer.append(getThreadIdsByRecipientId(String.valueOf(addrIds[i])));
        }
        return buffer.toString();
    }

    private long getSingleThreadId(String address) {
        boolean isEmail = Mms.isEmailAddress(address);
        String refinedAddress = isEmail ? address.toLowerCase() : address;
        String selection = "address=?";
        String[] selectionArgs;

        if (isEmail) {
            selectionArgs = new String[]{refinedAddress};
        } else {
            selection += " OR " + String.format("PHONE_NUMBERS_EQUAL(address, ?, %d)",
                    (mUseStrictPhoneNumberComparation ? 1 : 0));
            selectionArgs = new String[]{refinedAddress, refinedAddress};
        }

        Cursor cursor = null;

        try {
            SQLiteDatabase db = mOpenHelper.getReadableDatabase();
            cursor = db.query(
                    "canonical_addresses", ID_PROJECTION,
                    selection, selectionArgs, null, null, null);

            if (cursor.getCount() == 0) {
                return RESULT_FOR_ID_NOT_FOUND;
            }

            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return RESULT_FOR_ID_NOT_FOUND;
    }

    /* //6.0 methord delete by ligy begin
    private synchronized String getThreadIdByRecipientIds(String recipientIds) {
        String THREAD_QUERY = "SELECT _id FROM threads " +
                "WHERE recipient_ids = ?";
        String resultString = DEFAULT_STRING_ZERO;

        if (DEBUG) {
            Log.v(LOG_TAG, "getThreadId THREAD_QUERY: " + THREAD_QUERY +
                    ", recipientIds=" + recipientIds);
        }
        Cursor cursor = null;
        try {
            SQLiteDatabase db = mOpenHelper.getReadableDatabase();
            cursor = db.rawQuery(THREAD_QUERY, new String[] { recipientIds });

            if (cursor == null || cursor.getCount() == 0) {
                return resultString;
            }

            if (cursor.moveToFirst()) {
                resultString = String.valueOf(cursor.getLong(0));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return resultString;
    }
	*/ //delete by ligy end

    private String getSmsQueryString(int searchMode, int matchWhole, String threadIdString) {
        String smsQuery = "";
        //tangyisen add reject reject now not support search
        if (searchMode == SEARCH_MODE_CONTENT) {
            /*smsQuery = String.format(
                    "SELECT %s FROM sms WHERE (body LIKE ? ESCAPE '" +
                            SEARCH_ESCAPE_CHARACTER + "') ",
                    SMS_PROJECTION);*/
            smsQuery = String.format(
                "SELECT %s FROM sms WHERE (reject = 0 AND body LIKE ? ESCAPE '" +
                        SEARCH_ESCAPE_CHARACTER + "') ",
                SMS_PROJECTION);
        } else if (searchMode == SEARCH_MODE_NUMBER && matchWhole == MATCH_BY_ADDRESS) {
            smsQuery = String.format(
                    //"SELECT %s FROM sms WHERE (address LIKE ?)",//modify by ligy delete space
                    "SELECT %s FROM sms WHERE (reject = 0 AND REPLACE(address, \" \", \"\") LIKE ?)" + "group by thread_id",
                    SMS_PROJECTION);
        } else if (searchMode == SEARCH_MODE_NUMBER && matchWhole == MATCH_BY_THREAD_ID) {
            smsQuery = String.format(
                    //"SELECT %s FROM sms WHERE (thread_id in (%s))",//modify by ligy
                    "SELECT %s FROM sms WHERE (reject = 0 AND thread_id in (%s))"  + "group by thread_id",
                    SMS_PROJECTION,
                    threadIdString);
        }
        return smsQuery;
    }

    private String getMmsQueryString(int searchMode, int matchWhole, String threadIdString) {
        String mmsQuery = "";
        //tangyisen add reject and reject now tmp not support search
        if (searchMode == SEARCH_MODE_CONTENT) {
            mmsQuery = String.format(Locale.US,
                    "SELECT %s FROM pdu,part,addr WHERE ((pdu.reject = 0) AND (part.mid=pdu._id) AND " +
                    "(addr.msg_id=pdu._id) AND " +
                    "(addr.type=%d) AND " +
                    "(part.ct='text/plain') AND " +
                    "(body like ? escape '" + SEARCH_ESCAPE_CHARACTER + "')) GROUP BY pdu._id",
                    MMS_PROJECTION_FOR_CONTENT_SEARCH,
                    PduHeaders.TO);
        } else if (searchMode == SEARCH_MODE_SUBJECT) {
            mmsQuery = String.format(
                    "SELECT %s FROM pdu,addr WHERE ((pdu.reject = 0) AND " +
                    "(addr.msg_id = pdu._id)  AND (addr.type=%d) AND " +
                    "(body like ? escape '" + SEARCH_ESCAPE_CHARACTER + "')) GROUP BY pdu._id",
                    MMS_PROJECTION_FOR_SUBJECT_SEARCH,
                    PduHeaders.TO);
        } else if (searchMode == SEARCH_MODE_NUMBER && matchWhole == MATCH_BY_ADDRESS) {
            mmsQuery = String.format(
                    "SELECT %s FROM pdu,addr WHERE ((pdu.reject = 0) AND " +
                    "(addr.msg_id=pdu._id) AND " +
                    //"(address like ?)) GROUP BY pdu._id",//modify by ligy
                    //"(REPLACE(address, \" \", \"\")  like ?)) GROUP BY pdu._id, thread_id",//lichao modify again
                       "(REPLACE(addr.address, \" \", \"\")  like ?)) GROUP BY pdu._id, thread_id",
                    MMS_PROJECTION_FOR_NUMBER_SEARCH);
        } else if (searchMode == SEARCH_MODE_NUMBER && matchWhole == MATCH_BY_THREAD_ID) {
            mmsQuery = String.format(Locale.US,
                    "SELECT %s FROM pdu,addr WHERE ((pdu.reject = 0) AND " +
                    "(thread_id in (%s)) AND " +
                    "(addr.msg_id = pdu._id) AND " +
                    //"(addr.type=%d))",//modify by ligy
                    "(addr.type=%d))"  + "group by thread_id",
                    MMS_PROJECTION_FOR_NUMBER_SEARCH,
                    threadIdString,
                    PduHeaders.TO);
        }
        return mmsQuery;
    }
	
	//add by 7.0
    private String getConversationQueryString(int searchMode,
                String smsTable, String pduTable, final String threadIds) {
        String nameQuery = "";
        String smsContentQuery = "";
        String pduContentQuery = "";
        String rawQuery = "";

        final String NAME_PROJECTION = "threads._id AS _id,"
                + "threads.date AS date,"
                + "threads.message_count AS message_count,"
                + "threads.recipient_ids AS recipient_ids,"
                + "threads.snippet AS snippet,"
                + "threads.snippet_cs AS snippet_cs,"
                + "threads.read AS read,"
                + "NULL AS error,"
                + "threads.has_attachment AS has_attachment,"
                + "threads.attachment_info AS attachment_info";
        final String SMS_PROJECTION = "threads._id AS _id,"
                + smsTable + ".date AS date,"
                + "threads.message_count AS message_count,"
                + "threads.recipient_ids AS recipient_ids,"
                + smsTable + ".body AS snippet,"
                + "threads.snippet_cs AS snippet_cs,"
                + "threads.read AS read,"
                + "NULL AS error,"
                + "threads.has_attachment AS has_attachment,"
                + "'SMS' AS attachment_info";
        final String PDU_PROJECTION = "threads._id AS _id,"
                + pduTable + ".date * 1000 AS date,"
                + "threads.message_count AS message_count,"
                + "threads.recipient_ids AS recipient_ids,"
                + pduTable + ".sub AS snippet,"
                + pduTable + ".sub_cs AS snippet_cs,"
                + "threads.read AS read,"
                + "NULL AS error,"
                + "threads.has_attachment AS has_attachment,"
                + "part.text AS attachment_info";

        if (searchMode == SEARCH_MODE_NAME) {
            nameQuery = String.format(
                    "SELECT %s FROM threads WHERE threads._id in (%s)",
                    NAME_PROJECTION,
                    threadIds);
            smsContentQuery = String.format("SELECT %s FROM threads, " + smsTable
                    + " WHERE ("
                    + "(threads._id NOT in (%s))"
                    + " AND (" + smsTable + ".thread_id = " + "threads._id )"
                    + " AND ((" + smsTable + ".body LIKE ? ESCAPE '"
                    + SEARCH_ESCAPE_CHARACTER + "')"
                    + " OR (" + smsTable + ".address LIKE ? ESCAPE '"
                    + SEARCH_ESCAPE_CHARACTER + "')))"
                    + " GROUP BY threads._id",
                    SMS_PROJECTION,
                    threadIds);
            pduContentQuery = String.format("SELECT %s FROM threads, addr, part, " + pduTable
                    + " WHERE((threads._id NOT in (%s))"
                    + " AND (addr.msg_id=" + pduTable + "._id)"
                    + " AND (addr.type=%d)"
                    + " AND (part.mid=" + pduTable + "._id)"
                    + " AND (part.ct='text/plain')"
                    + " AND (threads._id =" + pduTable + ".thread_id)"
                    + " AND ((part.text LIKE ? ESCAPE '" + SEARCH_ESCAPE_CHARACTER + "')"
                    + " OR (addr.address LIKE ? ESCAPE '" + SEARCH_ESCAPE_CHARACTER + "')"
                    + " OR (" + pduTable + ".sub LIKE ? ESCAPE '"
                    + SEARCH_ESCAPE_CHARACTER + "')))",
                    PDU_PROJECTION,
                    threadIds,
                    PduHeaders.TO);

            rawQuery = String.format(
                    "%s UNION %s UNION %s GROUP BY threads._id ORDER BY date DESC",
                    nameQuery,
                    smsContentQuery,
                    pduContentQuery);
        } else {
            smsContentQuery = String.format("SELECT %s FROM threads, " + smsTable
                    + " WHERE ("
                    + "(" + smsTable + ".thread_id = " + "threads._id )"
                    + " AND ((" + smsTable + ".body LIKE ? ESCAPE '"
                    + SEARCH_ESCAPE_CHARACTER + "')"
                    + " OR (" + smsTable + ".address LIKE ? ESCAPE '"
                    + SEARCH_ESCAPE_CHARACTER + "')))"
                    + " GROUP BY threads._id",
                    SMS_PROJECTION);
            pduContentQuery = String.format("SELECT %s FROM threads, addr, part, " + pduTable
                    + " WHERE((addr.msg_id=" + pduTable + "._id)"
                    + " AND (addr.type=%d)"
                    + " AND (part.mid=" + pduTable + "._id)"
                    + " AND (part.ct='text/plain')"
                    + " AND (threads._id =" + pduTable + ".thread_id)"
                    + " AND ((part.text LIKE ? ESCAPE '" + SEARCH_ESCAPE_CHARACTER + "')"
                    + " OR (addr.address LIKE ? ESCAPE '" + SEARCH_ESCAPE_CHARACTER + "')"
                    + " OR (" + pduTable + ".sub LIKE ? ESCAPE '"
                    + SEARCH_ESCAPE_CHARACTER + "')))",
                    PDU_PROJECTION,
                    PduHeaders.TO);

            rawQuery = String.format(
                    "%s UNION %s GROUP BY threads._id ORDER BY date DESC",
                    smsContentQuery,
                    pduContentQuery);
        }
        if (DEBUG) {
            Log.d(LOG_TAG, "getConversationQueryString = " + rawQuery);
        }
        return rawQuery;
    }

    //lichao add in 2016-12-07 begin
    private String getThreadQueryStringMatchByAddress() {
        // Keyword is a pure digital, non complete recipient number, so using address LIKE.
        // But threads table does not have the address field,
        // so need to use the address in canonical_addresses table to match the query.
        //test111,fail
//        String address_match =" (recipient_ids in " +
//                "(SELECT distinct canonical_id FROM canonical_addresses WHERE (address LIKE ?))) ";
//        String recipient_match =" (recipient_ids = canonical_id) ";
        //test222,OK
        String address_match =" (address LIKE ? )";
        String recipient_match =" (recipient_ids = canonical_id) ";
        //test333,fail
//        //can't use "(CONTAINS(recipient_ids, canonical_id)", if recipient_ids==21 while canonical_id=2
//        String recipient_match = "(recipient_ids = canonical_id "
//                + " OR recipient_ids like 'canonical_id %' "
//                + " OR recipient_ids like '% canonical_id' "
//                + " OR recipient_ids like '% canonical_id %')";
        //test444,fail
//        String recipient_match = "(recipient_ids = canonical_id "
//                + " OR CONTAINS(recipient_ids, rpad(canonical_id,len(canonical_id)+1,' '))"
//                + " OR CONTAINS(recipient_ids, lpad(canonical_id,len(canonical_id)+1,' '))";
        String threadQuery = String.format("SELECT %s FROM threads, canonical_addresses "
                        + " WHERE ("
                        + address_match
                        + " AND "
                        + recipient_match
                        + " ) group by threads._id ",
                THREADS_CANONICAL_ADDRESSES_PROJECTION);
        return String.format("%s ORDER BY date DESC", threadQuery);
    }
    //As in src/com/android/mms/data/Conversation.java
    /*
    public static final String[] ALL_THREADS_PROJECTION = {
            Threads._ID, Threads.DATE, Threads.MESSAGE_COUNT, Threads.RECIPIENT_IDS,
            Threads.SNIPPET, Threads.SNIPPET_CHARSET, Threads.READ, Threads.ERROR,
            Threads.HAS_ATTACHMENT, Threads.SUBSCRIPTION_ID
    };
    */
    private static final String THREADS_CANONICAL_ADDRESSES_PROJECTION =
            "threads._id AS _id, "//0:Threads._ID
                    + "threads.date AS date, "//1:Threads.DATE
                    + "threads.message_count AS message_count, "//2:Threads.MESSAGE_COUNT
                    + "threads.recipient_ids AS recipient_ids, "//3.Threads.RECIPIENT_IDS
                    + "threads.snippet AS snippet, "//4:Threads.SNIPPET(The first 45 characters of the latest message in the thread.)
                    + "threads.snippet_cs AS snippet_cs, "//5: Threads.SNIPPET_CHARSET(The charset of the snippet.)
                    + "threads.read AS read, "//6:Threads.READ
                    + "threads.error AS error, "//7:Threads.ERROR
                    + "threads.has_attachment AS has_attachment, "//8:Threads.HAS_ATTACHMENT
                    + "threads.sub_id AS sub_id, "//9:Threads.SUBSCRIPTION_ID
                    + "threads.attachment_info AS attachment_info, "
                    + "canonical_addresses._id AS canonical_id, "
                    + "canonical_addresses.address AS address ";

    private String getThreadQueryStringMatchByThreadId(String threadIdString) {
        // The key is thread_id collection.
        // first get the recipient number by query contacts database,
        // then get the thread_id set according to the recipient number.
        // So use thread_id in threadIdString to match
        String threadQuery = String.format(
                "SELECT %s FROM threads WHERE (_id in (%s))" + " group by threads._id",
                ALL_THREADS_PROJECTION,
                threadIdString);
        return String.format("%s ORDER BY date DESC", threadQuery);
    }
    private static final String ALL_THREADS_PROJECTION =
            "threads._id AS _id, "//0:Threads._ID
                    + "threads.date AS date, "//1:Threads.DATE
                    + "threads.message_count AS message_count, "//2:Threads.MESSAGE_COUNT
                    + "threads.recipient_ids AS recipient_ids, "//3.Threads.RECIPIENT_IDS
                    + "threads.snippet AS snippet, "//4:Threads.SNIPPET(The first 45 characters of the latest message in the thread.)
                    + "threads.snippet_cs AS snippet_cs, "//5: Threads.SNIPPET_CHARSET(The charset of the snippet.)
                    + "threads.read AS read, "//6:Threads.READ
                    + "threads.error AS error, "//7:Threads.ERROR
                    + "threads.has_attachment AS has_attachment, "//8:Threads.HAS_ATTACHMENT
                    + "threads.sub_id AS sub_id, "//9:Threads.SUBSCRIPTION_ID
                    + "threads.attachment_info AS attachment_info";

    //lichao add in 2016-12-14 begin
    private String getThreadQueryStringForNoBlack(String blacklist) {
        String threadQuery = String.format("SELECT %s FROM threads, canonical_addresses "
                        + " WHERE ("
                        + " (address not in (%s))"
                        + " AND "
                        + " (recipient_ids = canonical_id) "
                        + " ) group by threads._id ",
                THREADS_CANONICAL_ADDRESSES_PROJECTION,
                blacklist);
        return String.format("%s ORDER BY date DESC", threadQuery);
    }
    //lichao add in 2016-12-14 end

    // A group session holds multiple recipientId separated by spaces,
    // so a recipientId may be included by multiple Thread
	// lichao refactor it in 2016-12-07
    private synchronized String getThreadIdsByRecipientId(String recipientId) {
        //can't use "(CONTAINS(recipient_ids, "+recipientId+")", if recipient_ids==21 while recipientId=2
        String THREAD_QUERY = "SELECT _id FROM threads " +
                "WHERE recipient_ids = ? or recipient_ids like '" + recipientId
                + " %' or recipient_ids like '% " + recipientId
                + "' or recipient_ids like '% " + recipientId + " %'";
        String resultString = DEFAULT_STRING_ZERO;
        StringBuilder buffer = new StringBuilder();

        if (DEBUG) {
            Log.v(LOG_TAG, "getThreadId THREAD_QUERY: " + THREAD_QUERY +
                    ", recipientId=" + recipientId);
        }
        Cursor cursor = null;
        try {
            SQLiteDatabase db = mOpenHelper.getReadableDatabase();
            cursor = db.rawQuery(THREAD_QUERY, new String[]{
                    recipientId
            });

            if (cursor == null || cursor.getCount() == 0) {
                return resultString;
            }
            int i = 0;
            while (cursor.moveToNext()) {
                if (i != 0) {
                    buffer.append(',');
                }
                buffer.append(String.valueOf(cursor.getLong(0)));
                i++;
            }
            resultString = buffer.toString();

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return resultString;
    }
}
