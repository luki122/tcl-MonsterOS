package com.mst.utils;

import com.android.incallui.Call;
import com.android.incallui.InCallApp;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.Log;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;
import android.provider.CallLog.Calls;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionManager;

public class DatabaseUtil {
	private static final String TAG = "DatabaseUtil";

	public static long getRawContactId(Uri uri) {
		Log.v("getRawContactId", " uri = " + uri);
		if (uri == null) {
			return 0;
		}

		String url = uri.toString();

		if (url.startsWith("content://com.android.contacts/contacts/lookup/")
				|| url.startsWith("content://com.android.contacts/contacts/")) {
			long RawContactId = queryForRawContactId(InCallApp.getInstance()
					.getContentResolver(), Long.parseLong(uri
					.getLastPathSegment()));
			return RawContactId;
		} else if (url
				.startsWith("content://com.android.contacts/phone_lookup/")) {
			return getRawContactIdByNumber(uri.getLastPathSegment());
		} else {
			Cursor cursor = InCallApp
					.getInstance()
					.getContentResolver()
					.query(uri, new String[] { "raw_contact_id" }, null, null,
							null);
			try {
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					return cursor.getLong(0);
				}
				return 0;
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}
	}
	
	public static long queryForRawContactId(ContentResolver cr, long contactId) {
		Cursor rawContactIdCursor = null;
		long rawContactId = -1;

		try {
			rawContactIdCursor = cr.query(RawContacts.CONTENT_URI,
					new String[] { RawContacts._ID }, RawContacts.CONTACT_ID
							+ "=" + contactId + " and is_privacy > -1", null,
					null);
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


	
	private static long getRawContactIdByNumber(String number) {
		Log.v("getRawContactIdByNumber", " number = " + number);

		if (TextUtils.isEmpty(number)) {
			return 0;
		}

		Cursor cursor = InCallApp
				.getInstance()
				.getContentResolver()
				.query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
						number), new String[] { "raw_contact_id" }, null, null,
						null);
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				return cursor.getLong(0);
			}
			return 0;
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

}