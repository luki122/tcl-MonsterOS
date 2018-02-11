package com.monster.interception.notification;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.UserHandle;
import android.util.Log;

import android.os.AsyncResult;

public class ManageReject{
    private static final String LOG_TAG = "ManageReject";

    private Context mApp;
    static InterceptionNotifier notificationMgr;
    private static final int PHONE_STATE_CHANGED = 1;

    public ManageReject(Context app) {
        mApp = app;
        notificationMgr = InterceptionNotifier.getInstance(app);
    }

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    //用于通知的点击或者删除事件，因为不少代码是要和本进程进行交互的，不是简单的发送intent
     public static class RejectBroadcastReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // TODO: use "if (VDBG)" here.
                Log.d(LOG_TAG, "Broadcast from Notification: " + action);
                if(action.equals(ACTION_NOTIFY_ADD_BLACK)) {
                    String number = intent.getStringExtra("number");
                    String name = intent.getStringExtra("name");
                    notificationMgr.notifyAddBlackCall(number, name);
                } else if(action.equals(ACTION_NOTIFY_CALL)) {
                    notificationMgr.notifyHangupBlackCall();
                } else if(action.equals(ACTION_NOTIFY_SMS)) {
                    notificationMgr.notifyHangupBlackSms();
                } else if(action.equals(ACTION_ADD_BLACK)) {                    
                    int id = intent.getIntExtra("id", -1);
                    String number = intent.getStringExtra("number");
                    String name = intent.getStringExtra("name");                
                    closeSystemDialogs(context);
                    cancelAddBlackNotification(context, id, number);
                    Intent addBlackIntent = createAddBlackIntentInternal(number, name);
                    context.startActivity(addBlackIntent);                    
                } else if (action.equals(ACTION_GOTO_REJECT)) {                    
                    closeSystemDialogs(context);
                    cancelHangupBlackCallNotification(context);
                    Intent hangupBlackIntent = createGotoRejectIntentInternal();
                    context.startActivity(hangupBlackIntent);
                } else {
                    Log.w(LOG_TAG, "Received hang-up request from notification,"
                            + " but there's no call the system can hang up.");
                }
            }

            private void closeSystemDialogs(Context context) {
                Intent intent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                context.sendBroadcastAsUser(intent, UserHandle.ALL);
            }

            private void cancelAddBlackNotification(Context context, int id, String number) {
                Intent clearIntent = new Intent(context, ClearBlackCallsService.class);
                clearIntent.setAction(ClearBlackCallsService.ACTION_CLEAR_ADD_BLACK);
                clearIntent.putExtra("id", id);
                clearIntent.putExtra("number", number);
                context.startService(clearIntent);
            }

            private void cancelHangupBlackCallNotification(Context context) {
                Intent clearIntent = new Intent(context, ClearBlackCallsService.class);
                clearIntent.setAction(ClearBlackCallsService.ACTION_CLEAR_HANGUP_BLACK_CALLS);
                context.startService(clearIntent);
            }
        }
    
     private static final String ACTION_ADD_BLACK = "com.monster.interception.ACTION_ADD_BLACK";
     private static final String ACTION_GOTO_REJECT = "com.monster.interception.ACTION_GOTO_REJECT";
     private static final String ACTION_NOTIFY_CALL = "com.monster.interception.ACTION_NOTIFY_CALL";
     private static final String ACTION_NOTIFY_SMS = "com.monster.interception.ACTION_NOTIFY_SMS";
     private static final String ACTION_NOTIFY_ADD_BLACK = "com.monster.interception.ACTION_NOTIFY_ADD_BLACK";

    public static PendingIntent createAddBlackIntent(Context context, int id, String number, String name) {
        Intent intent = new Intent(ACTION_ADD_BLACK, null, context, RejectBroadcastReceiver.class);
        intent.putExtra("id", id);
        intent.putExtra("number", number);
        intent.putExtra("name", name);
        return PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);          
     }
    
    private static Intent createAddBlackIntentInternal(String number, String name) {        
          Intent intent = new Intent("com.monster.black.add.manually");
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
          Bundle bundle = new Bundle();
          bundle.putString("add_number", number); 
          bundle.putString("add_name", name);
          bundle.putBoolean("add", true);
          intent.putExtras(bundle);
          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          return intent;
      }

    public static PendingIntent createGotoRejectIntent(Context context) {  
        Intent intent = new Intent(ACTION_GOTO_REJECT, null, context, RejectBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);               
    }

    private static Intent createGotoRejectIntentInternal() {  
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
              | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        intent.putExtra("goToSms", InterceptionNotifier.getBlackNotificationMode() == 2);
        intent.putExtra("goToCall", InterceptionNotifier.getBlackNotificationMode() == 1);
        if(InterceptionNotifier.getBlackNotificationMode() == 3) {
            intent.putExtra("all", true);   
        }
        intent.setClassName("com.monster.interception", "com.monster.interception.activity.InterceptionActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;          
    }
}
