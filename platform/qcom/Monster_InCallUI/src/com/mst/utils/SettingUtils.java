package com.mst.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.util.Log;


public class SettingUtils {
	private static final String LOG_TAG = "SettingUtils";

	public static boolean getSetting(Context context, String name) {
   	    boolean result = false;
        Uri uri = Uri.parse("content://com.mst.phone/phone_setting");  
    	Cursor c = context.getContentResolver().query(uri, null, " name = '" + name + "'", null, null);
    	if(c != null) {
    		if(c.moveToFirst()) {
    			result = c.getInt(c.getColumnIndex("value")) > 0;
    		}
    		c.close();
    	}
    	Log.v(LOG_TAG, "getSetting name = " + name +" result = " + result);
        return result;        
	}


}