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
public class SearchUtils {

    private static final String TAG = "Mms/SearchUtils";
    private static final boolean DEBUG = LogTag.DEBUG;

    //this can get multi recipients String
    public static String getRecipientsStrByThreadId(Context context, long threadId) {
        if (threadId <= 0) {
            Log.w(TAG, "getRecipientsStrByThreadId, invalid threadId, return");
            return "";
        }
        Conversation conv = Conversation.query(context, threadId, true/*allowQuery*/);
        if (null == conv) {
            Log.w(TAG, "getRecipientsStrByThreadId, null conv, return");
            return "";
        }
        //这样“重新获取收信人”就可以获取到该会话的所有联系人，包括群发信息的情况
        String titleString = getRecipientsStrByContactList(conv.getRecipients());
        if (DEBUG) Log.d(TAG, "getRecipientsStrByThreadId(), titleString: " + titleString);
        return titleString;
    }

    public static String getRecipientsStrByContactList(ContactList contactList) {
        if (null == contactList) {
            Log.w(TAG, "getRecipientsStrByContactList, null contactList, return");
            return "";
        }
        String namesAndNumbers = contactList.formatNamesAndNumbers(",");
        //namesAndNumbers: 哈哈1116 <18866661116>
        if (DEBUG) Log.d(TAG, "\n\n getRecipientsStrByContactList(), namesAndNumbers: " + namesAndNumbers);

        String replaceNumbers = namesAndNumbers.replace(" ", "");
        //replaceNumbers = 哈哈1116<18866661116>
        if(DEBUG) Log.d(TAG, "getRecipientsStrByContactList(), replaceNumbers = " + replaceNumbers);

        //String strippedNumber = PhoneNumberUtils.stripSeparators(namesAndNumbers);
        ////strippedNumber = 111618866661116
        //if(DEBUG) Log.d(TAG, "getRecipientsStrByContactList(), strippedNumber = " + strippedNumber);

        boolean isNumber = MessageUtils.isNumeric(replaceNumbers);
        if (!isNumber) {
            return replaceNumbers;
        }
        String normalNumbers = PhoneNumberUtils.normalizeNumber(replaceNumbers);
        if(DEBUG) Log.d(TAG, "getRecipientsStrByContactList(), normalNumbers = " + normalNumbers);

        //String phoneNumberNo86 = StringUtils.getPhoneNumberNo86(normalNumber);
        //if (DEBUG) Log.d(TAG, "getRecipientsStrByContactList(), phoneNumberNo86 = " + phoneNumberNo86);
        return normalNumbers;
    }

    public static String getNameAndNumberByAddress(String address) {
        if (TextUtils.isEmpty(address)) {
            Log.w(TAG, "getNameAndNumberByAddress, null or empty address, return");
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
}
