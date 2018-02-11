package com.privacymanage.service;

import java.util.ArrayList;
import java.util.List;

import com.monster.privacymanage.entity.AidlAccountData;
import com.mst.privacy.PrivacyUtils;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import com.android.providers.mst.ContactsContract.RawContacts;
import com.android.providers.mst.ContactsContract.Data;
import android.util.Log;

public class PrivacyAccountChangeReceiver extends BroadcastReceiver{
	
	private static final String TAG = "ContactsProvider_PrivacyAccountChangeReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getExtras() != null) {
			String action = intent.getAction();
			AidlAccountData account = intent.getParcelableExtra("account");
        	PrivacyUtils.mCurrentAccountId = account.getAccountId();
            PrivacyUtils.mCurrentAccountHomePath = account.getHomePath();
            Log.i(TAG, "onReceive action: " + action 
                    + "  account id: " + account.getAccountId() 
                    + "  path: " + account.getHomePath());
        	
			if (action != null && action.equals("com.monster.privacymanage.SWITCH_ACCOUNT")) {
	        	if (PrivacyUtils.mCurrentAccountId > 0) {
	        		PrivacyUtils.mIsPrivacyMode = true;
	        	} else {
	        		PrivacyUtils.mIsPrivacyMode = false;
	        	}
	        	
			} else if (action != null && action.equals("com.aurora.privacymanage.DELETE_ACCOUNT")) {
				boolean delete = intent.getBooleanExtra("delete", false);
				PrivacyUtils.mIsPrivacyMode = false;
				
				doDeleteAccount(context, delete);
				
				PrivacyUtils.mCurrentAccountId = 0;
	            PrivacyUtils.mCurrentAccountHomePath = null;
			}
		}
	}
	
	private void doDeleteAccount(Context context, boolean checked) {
	    Cursor cursor = context.getContentResolver().query(RawContacts.CONTENT_URI,
                new String[] {RawContacts._ID}, 
                "is_privacy=" + PrivacyUtils.mCurrentAccountId + " AND deleted=0",
                null, null);
	    
	    boolean mDeleteContactsFromDB = checked;
	    
        if (cursor != null) {
            try {
                ContentValues values = new ContentValues();
                while (cursor.moveToNext()) {
                    String rawContactId = cursor.getString(0);
                    if (mDeleteContactsFromDB) {
                        context.getContentResolver().delete(
                                RawContacts.CONTENT_URI, 
                                RawContacts._ID + "=?" + " and deleted=0 and is_privacy=" + PrivacyUtils.mCurrentAccountId, 
                                new String[] {String.valueOf(rawContactId)});
                    } else {
                        values.put("is_privacy", 0);
                        context.getContentResolver().update(RawContacts.CONTENT_URI, values, 
                                RawContacts._ID + "=" + rawContactId, null);
                        context.getContentResolver().update(Data.CONTENT_URI, values, 
                                Data.RAW_CONTACT_ID + "=" + rawContactId, null);
                        values.clear();
                    }
                }
            } finally {
                cursor.close();
            }
        }
	}
}
