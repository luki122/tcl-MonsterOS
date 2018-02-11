package com.mediatek.telecom.recording;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.Data;
import android.util.Log;


public class RecordUtils {
    private static final String LOG_TAG = "RecordUtils";
    
	public static void handleAutoRecord(Context con, String number, long rawContactId) {
		Log.v(LOG_TAG, " handleAutoRecord rawContactId = " + rawContactId + " number = " + number);
		
		boolean isRecord = false;
		if (!containRecordNumber(number)) {
			try {
				Cursor cc = con.getContentResolver().query(
						Data.CONTENT_URI,
						new String[] { "auto_record" },
						Data.DATA1 + " = '" + number + "' AND "
								+ Data.RAW_CONTACT_ID + " = " + rawContactId, null,
						null);
				if (cc != null) {
					Log.v(LOG_TAG, " isRecord count= " + cc.getCount());
					while (cc.moveToNext()) {
						Log.v(LOG_TAG, " isRecord content= " + cc.getInt(0));
						if (cc.getInt(0) == 1) {
							isRecord = true;
							break;
						}
					}
					cc.close();
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			Log.v(LOG_TAG, " isRecord on = " + isRecord);
			setAutoRecordFlagOn(number, isRecord);
		}
	}
	
    public static final HashMap<String, Boolean> mRecordMap = new HashMap<String, Boolean>();
    private static void setAutoRecordFlagOn(String number, Boolean isRecord) {
    	mRecordMap.put(number, isRecord);
    }
    
    public static boolean containRecordNumber(String number) {
    	return mRecordMap.containsKey(number) && mRecordMap.get(number);
    } 

}