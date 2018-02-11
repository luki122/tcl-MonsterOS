package com.monster.interception.notification;

import com.monster.interception.InterceptionApplication;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.provider.CallLog.Calls;

//add by liguangyu for black list 
public class ClearBlackCallsService extends IntentService {
    /** This action is used to clear missed calls. */
    public static final String ACTION_CLEAR_HANGUP_BLACK_CALLS =
            "com.android.phone.intent.CLEAR_HANGUP_BLACK_CALLS";
    
    public static final String ACTION_CLEAR_ADD_BLACK =
            "com.android.phone.intent.CLEAR_ADD_TO_BLACK";

    private InterceptionApplication mApp;

    public ClearBlackCallsService() {
        super(ClearBlackCallsService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = InterceptionApplication.getInstance();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (ACTION_CLEAR_HANGUP_BLACK_CALLS.equals(intent.getAction())) {
            mApp.mManageReject.notificationMgr.cancelHangupBlackCallNotification();
        } else if (ACTION_CLEAR_ADD_BLACK.equals(intent.getAction())) {
            int id = intent.getIntExtra("id", -1);
            String number = intent.getStringExtra("number");
            mApp.mManageReject.notificationMgr.cancelAddBlackNotification(id, number);
        }
    }
}
