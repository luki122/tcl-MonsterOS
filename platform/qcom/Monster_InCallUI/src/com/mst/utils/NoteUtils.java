package com.mst.utils;

import com.android.incallui.InCallApp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.PhoneLookup;
import android.text.TextUtils;
import android.util.Log;


public class NoteUtils {
	private static final String LOG_TAG = "NoteUtils";

	public static String getNote(Context con, long rawContactId) {

		Log.v(LOG_TAG, " getNote rawContactId = " + rawContactId);
		String re = "";

		Cursor c = con.getContentResolver().query(
				Data.CONTENT_URI,
				new String[] { Data.DATA1 },
				Data.MIMETYPE + " = '" + Note.CONTENT_ITEM_TYPE + "' AND "
						+ Data.RAW_CONTACT_ID + " = " + rawContactId
//						+ " and is_privacy > -1", null, null);
						, null, null);

		if (c != null) {
			if (c.moveToFirst()) {
				re = c.getString(0);
			}
			c.close();
		}

		Log.v("ttwang", " re = " + re);
		return re;
	}
	
	public static String getNote(String number) {
		if(TextUtils.isEmpty(number)) {
			return null;
		}
		return getNote(InCallApp.getInstance(), getRawContactIdByNumber(number));
	}
	
    private static long getRawContactIdByNumber(String number) { 
		Log.v("getRawContactIdByNumber", " number = " + number);
		
		if(TextUtils.isEmpty(number)) {
			return 0;
		}

		Cursor cursor = InCallApp.getInstance().getContentResolver().query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, number), 
				new String[]{"raw_contact_id"},
				null, null, null);
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				return cursor.getLong(0);
			}
	    	return 0;
		} finally {
			if(cursor != null) {
				cursor.close();
			}
		}    
    }


}