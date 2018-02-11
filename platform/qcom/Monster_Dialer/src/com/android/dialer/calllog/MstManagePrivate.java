
package com.android.dialer.calllog;

import mst.provider.CallLog.Calls;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import com.monster.privacymanage.entity.AidlAccountData;
import com.mst.privacy.PrivacyUtils;

import android.database.ContentObserver;
import android.database.Cursor;
import android.os.*;
import android.net.Uri;



public class MstManagePrivate {
    private static final String LOG_TAG = "ManagePrivate";
    
    private static final String ACTION_SWITCH = "com.mst.privacymanage.SWITCH_ACCOUNT";
//    private static final String ACTION_DELETE = "com.mst.privacymanage.DELETE_ACCOUNT";

    /** The singleton ManagedRoaming instance. */
    private static MstManagePrivate sInstance;

    private Context mContext;
    
    private PrivateCallLogCountObserver mCallLogObserver;

    public static MstManagePrivate init(Context context) {
        synchronized (MstManagePrivate.class) {
            if (sInstance == null) {
                sInstance = new MstManagePrivate(context);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            return sInstance;
        }
    }

    private MstManagePrivate(Context context) {
        mContext = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SWITCH);
        mContext.registerReceiver(mManagePrivateReceiver, filter);
        mCallLogObserver = new PrivateCallLogCountObserver();
           	
    }    
    
    public static MstManagePrivate getInstance() {
        return sInstance;
    }

    private BroadcastReceiver mManagePrivateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

    		
    		if (intent.getExtras() != null) {
    			String action = intent.getAction();
    			AidlAccountData account = intent.getParcelableExtra("account");
            	Log.i(LOG_TAG, "mManagePrivateReceiver  "
            			+ "onReceive action: " + action 
            			+ "  account id: " + account.getAccountId() 
            			+ "  path: " + account.getHomePath());
            	
    			if (action != null && action.equals("com.mst.privacymanage.SWITCH_ACCOUNT")) {
    				if (account.getAccountId() > 0) {
    					updateCallLogNumber(account.getAccountId());
    					if(!isRegister) {
    						isRegister = true;
    						mContext.getContentResolver().registerContentObserver(Calls.CONTENT_URI, true, mCallLogObserver);
    					}
    				} else {
    					if(isRegister) {
    						isRegister = false;
    						mContext.getContentResolver().unregisterContentObserver(mCallLogObserver);
    					}
    				}
    			}
    		}
    	
        }
    };
    
    private class PrivateCallLogCountObserver extends ContentObserver {
    	
		public PrivateCallLogCountObserver(){
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {

			Log.i(LOG_TAG, "onChange :");
			super.onChange(selfChange);
			updateCallLogNumber(PrivacyUtils.getCurrentAccountId());
		}
		
    }
    
	private void updateCallLogNumber(long privacyid) {
    	Log.i(LOG_TAG, "updateCallLogNumber  ");
		Cursor cursor = mContext.getContentResolver().query(Calls.CONTENT_URI, new String[]{"_id"}, 
				"privacy_id = " + privacyid, null, null);
		if (cursor != null) {
    		Log.i(LOG_TAG, "cursor.getCount()  = " + cursor.getCount());  
			PrivacyUtils.mPrivacyCallLogsNum = cursor.getCount();
		} else {
			PrivacyUtils.mPrivacyCallLogsNum = 0;
		}
		PrivacyUtils.setPrivacyNum(mContext,
				"com.android.dialer.MstPrivateCallLogActivity", 
				PrivacyUtils.mPrivacyCallLogsNum, 
				privacyid);
		if(cursor != null) {
			cursor.close();
			cursor = null;
		}
	}
	
	public void updateCallLog() {
    	if (PrivacyUtils.mCurrentAccountId > 0) {
			updateCallLogNumber(PrivacyUtils.mCurrentAccountId);
			if(!isRegister) {
				isRegister = true;
				mContext.getContentResolver().registerContentObserver(Calls.CONTENT_URI, true, mCallLogObserver);
			}
		}
	}
	
	boolean isRegister = false;
    
}