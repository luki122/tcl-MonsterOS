package com.mst.privacy;

import com.android.incallui.InCallApp;
import com.android.incallui.R;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;

public class PrivacyDataUtils {

    private static final String TAG = "telecom_PrivacyDataUtils";

    public static int[] getPrivateData(Uri uri) {
        Log.v("getPrvateData", " uri = " + uri);
        if (uri == null || !PrivacyUtils.mIsPrivacySupport) {
            return null;
        }

        String url = uri.toString();

        if (url.startsWith("content://com.android.contacts/contacts/lookup/")
                || url.startsWith("content://com.android.contacts/contacts/")) {
            long RawContactId = queryForRawContactId(InCallApp.getInstance().getContentResolver(), Long.parseLong(uri
                    .getLastPathSegment()));
            Log.v("getPrvateData", " RawContactId= " + RawContactId);
            int[] result = new int[3];
            result[0] = (int) RawContactId;
            result[1] = 0;
            result[2] = 0;

            Cursor c = InCallApp.getInstance()
                    .getContentResolver()
                    .query(RawContacts.CONTENT_URI,
                            new String[] { "is_privacy",
                                    "call_notification_type" },
                            RawContacts._ID + "=" + RawContactId
                                    + " and is_privacy > -1 ", null,
                            PRIVATE_SQL_ORDER);
            if (c != null && c.moveToFirst()) {
                result[1] = c.getInt(0);
                result[2] = c.getInt(1);
            }
            Log.v("getPrvateData", " cursor data1 = " + result[1]
                    + " cursor data2 = " + result[2]);
            if (c != null) {
                c.close();
            }
            return result;
        } else if (url
                .startsWith("content://com.android.contacts/phone_lookup/")) {
            return getPrivateData(uri.getLastPathSegment());
        } else {
            Cursor cursor = InCallApp.getInstance()
                    .getContentResolver()
                    .query(uri,
                            new String[] { "raw_contact_id", "is_privacy",
                                    "call_notification_type" },
                            " is_privacy > -1 ", null, PRIVATE_SQL_ORDER);
            try {
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int[] result = new int[3];
                    for (int i = 0; i < 3; i++) {
                        result[i] = cursor.getInt(i);
                        Log.v("getPrvateData", " cursor data = " + result[i]);
                    }
                    return result;
                }
                return null;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

    }

    private static long queryForRawContactId(ContentResolver cr, long contactId) {
        Cursor rawContactIdCursor = null;
        long rawContactId = -1;
        
        try {
            rawContactIdCursor = cr.query(RawContacts.CONTENT_URI,
                    new String[] {RawContacts._ID},
                    RawContacts.CONTACT_ID + "=" + contactId + " and is_privacy > -1 ", null, null);
            if (rawContactIdCursor != null && rawContactIdCursor.moveToFirst()) {
                // Just return the first one.
                rawContactId = rawContactIdCursor.getLong(0);
            }
        } finally {
            if (rawContactIdCursor != null) {
                rawContactIdCursor.close();
            }
        }
        return rawContactId;
    }
    
    public static long getDataIdByRawContactId(long rawContactId) {
        Log.v("getDataIdByRawContactId", " rawContactId = " + rawContactId);
        long result = -1;
        if (rawContactId <= 0) {
            return result;
        }
        
        Cursor cursor = InCallApp.getInstance().getContentResolver().query(Data.CONTENT_URI,
                new String[] {Data._ID},
                " raw_contact_id  = " + rawContactId + " AND is_privacy > -1",
                null, null);    
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                result = cursor.getLong(0);
            }
            cursor.close();
        }
        Log.v("getDataIdByRawContactId", " dataid = " + result);
        return result;
    }
    
    
    public static int[] getPrivateData(String number) {
        Log.v("getPrvateData", " number = " + number);

        if (TextUtils.isEmpty(number) ||  !PrivacyUtils.mIsPrivacySupport) {
            return null;
        }

        Cursor cursor = InCallApp.getInstance()
                .getContentResolver()
                .query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                        number),
                        new String[] { "raw_contact_id", "is_privacy",
                                "call_notification_type" },
                        " is_privacy > -1 ", null, PRIVATE_SQL_ORDER);
        try {
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                int[] result = new int[3];
                for (int i = 0; i < 3; i++) {
                    result[i] = cursor.getInt(i);
                    Log.v("getPrvateData", " cursor data = " + result[i]);
                }
                return result;
            }
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static String PRIVATE_SQL_ORDER = "is_privacy DESC LIMIT 0,1";

    private static final String URL_STR = "content://com.monster.privacymanage.provider.ConfigProvider";
    private static final Uri PRIVATE_CONTENT_URI = Uri.parse(URL_STR);
    private static final String MSG_NOTIFY_SWITCH = "msg_notify_switch";
    private static final String MSG_NOTIFY_HINT = "msg_notify_hint";
    private static final String ACCOUNT_ID = "account_id";

    public static String getPrivateRingNotificationText(long accountId) {
        Log.v("getPrivateRingNotificationText", " accountId = " + accountId);
        String defaultText = InCallApp.getInstance()
                .getResources().getString(R.string.private_notification_text);
        String[] columns = { MSG_NOTIFY_HINT };

        Cursor cursor = InCallApp.getInstance()
                .getContentResolver()
                .query(PRIVATE_CONTENT_URI, columns, getQueryWhere(),
                        getQueryValue(accountId), null);

        if (cursor != null) {
            Log.v("getPrivateRingNotificationText", " not null");
            if (cursor.moveToFirst()) {
                defaultText = cursor.getString(0);
                Log.v("getPrivateRingNotificationText", " defaultText ="
                        + defaultText);
            }
            cursor.close();
        }

        return defaultText;
    }

    public static String getPrivateHomePath(long accountId) {
        final Uri PRIVATE_PATH_URI = Uri
                .parse("content://com.monster.privacymanage.provider.AccountProvider");

        String path = "";
        String[] columns = { "homePath" };

        Cursor cursor = InCallApp.getInstance()
                .getContentResolver()
                .query(PRIVATE_PATH_URI, columns, getQueryWhere(),
                        getQueryValue(accountId), null);

        if (cursor != null) {
            Log.v("getPrivateHomePath", " not null");
            if (cursor.moveToFirst()) {
                path = cursor.getString(0);
                Log.v("getPrivateHomePath", " path =" + path);
            }
            cursor.close();
        }

        return path;
    }

    private static String getQueryWhere() {
        return ACCOUNT_ID + " = ?";
    }

    private static String[] getQueryValue(long accountId) {
        String[] whereValue = { "" + accountId };
        return whereValue;
    }

    public static boolean isPrivateSendSms(long accountId) {
        boolean defaultValue = true;
        String[] columns = { MSG_NOTIFY_SWITCH };

        Cursor cursor = InCallApp.getInstance()
                .getContentResolver()
                .query(PRIVATE_CONTENT_URI, columns, getQueryWhere(),
                        getQueryValue(accountId), null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                defaultValue = cursor.getInt(0) > 0;
            }
            cursor.close();
        }
        return defaultValue;
    }

}