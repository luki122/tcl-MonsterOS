package com.monster.interception.notification;

import java.util.Map;
import java.util.Set;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.HashSet;

import com.monster.interception.InterceptionApplication;
import com.monster.interception.R;


public class InterceptionNotifier {
    private static final String LOG_TAG = "InterceptionNotifier";
    private static final boolean DBG = true;
    private static final boolean VDBG = true;



    /** The singleton PrivateNotificationMgr instance. */
    private static InterceptionNotifier sInstance;

    private Context mContext;
    private NotificationManager mNotificationManager;


    /**
     * Private constructor (this is a singleton).
     * @see init()
     */
    private InterceptionNotifier(Context app) {
        mContext = app;
        mNotificationManager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        sp = app.getSharedPreferences("AddBlackTimeList", Context.MODE_PRIVATE); 
    }

    static InterceptionNotifier getInstance(Context app) {
        synchronized (InterceptionNotifier.class) {
            if (sInstance == null) {
                sInstance = new InterceptionNotifier(app);
            } else {
                Log.wtf(LOG_TAG, "init() called multiple times!  sInstance = " + sInstance);
            }
            return sInstance;
        }
    }
    

    private int mBlackNotificationCount = 0;
    private Set<String> mBlackNumbers=new HashSet<String>();
    private SharedPreferences sp;
    private static final int BLACK_NOTIFICATION_BASE = 17000;
    private static final int BLACK_LIST_NOTIFICATION_BASE = 3;

    
    public void notifyAddBlackCall(String number, String name) {        
         if (DBG) log("notifyAddBlackCall(): number = " + number + " name = " + name);
         updateAddBlackTimeList();
         long lastAddTime = sp.getLong(number, 0);       
         long now = System.currentTimeMillis();
         if(mBlackNumbers.contains(number)  || now - lastAddTime < 24 * 3600 * 1000) {           
             if (DBG) log("notifyAddBlackCall(): return now - lastAddTime < 24 * 3600 * 1000 = " + (now - lastAddTime < 24 * 3600 * 1000));
             if (DBG) log("now = "+ now + " lastAddTime = "+ lastAddTime);
             return;
         }
         final Notification.Builder builder = new Notification.Builder(mContext);        
         builder.setUsesChronometer(false);
         builder.setWhen(0);
         builder.setAutoCancel(true);
         builder.setDeleteIntent(createClearAddBlackIntent(BLACK_NOTIFICATION_BASE + BLACK_LIST_NOTIFICATION_BASE + mBlackNotificationCount, number));
//         PendingIntent inCallPendingIntent =  PendingIntent.getActivity(mContext, 0, PhoneGlobals.createAddBlackIntent(number, name), 0);
//         builder.setContentIntent(inCallPendingIntent);
         builder.setContentIntent(ManageReject.createAddBlackIntent(mContext, BLACK_NOTIFICATION_BASE + BLACK_LIST_NOTIFICATION_BASE + mBlackNotificationCount, number, name));
         Drawable largeIcon = mContext.getResources().getDrawable(R.drawable.ic_launcher);
         builder.setLargeIcon(((BitmapDrawable) largeIcon).getBitmap());
         String expandedText = mContext.getString(R.string.add_black_content, TextUtils.isEmpty(name) ? number : name);
         builder.setContentTitle(mContext.getString(R.string.add_black_title));
         builder.setContentText(expandedText);
         builder.setTicker(expandedText);
         //需要双指才能打开，默认还是显示省略号
         Notification notification = new Notification.BigTextStyle(builder).bigText(expandedText).build();
         notification.icon = R.drawable.ic_launcher;
//         Notification notification = builder.getNotification();
         mNotificationManager.notify(BLACK_NOTIFICATION_BASE + BLACK_LIST_NOTIFICATION_BASE + mBlackNotificationCount, notification);
         mBlackNotificationCount++;
         mBlackNotificationCount = mBlackNotificationCount % 100;
         mBlackNumbers.add(number);
         SharedPreferences.Editor editor = sp.edit();
         editor.putLong(number, now);
         editor.commit();
    }    
    
    private void updateAddBlackTimeList() {
        try {
            Map<String, ?> map = sp.getAll();
            Set<String> keys=map.keySet();   
            SharedPreferences.Editor editor = sp.edit();
            long now = System.currentTimeMillis(); 
            for(String key:keys){   
                 long lastAddTime = sp.getLong(key, 0);                 
                 if(now - lastAddTime >= 24 * 3600 * 1000) {
                     editor.remove(key);
                 }
            }  
            editor.commit();
        } catch(Exception e) {
            e.printStackTrace();
        }       
    }
    
    private PendingIntent createClearAddBlackIntent(int id, String number) {
        Intent intent = new Intent(mContext, ClearBlackCallsService.class);
        intent.setAction(ClearBlackCallsService.ACTION_CLEAR_ADD_BLACK);
        intent.putExtra("id", id);
        intent.putExtra("number", number);
        return PendingIntent.getService(mContext, 0, intent, 0);
    }
    
    public void cancelAddBlackNotification(int id, String number) {
        if (DBG) log("cancelAddBlackNotification() id =" + id + " number = " + number);
        mBlackNumbers.remove(number);
        mNotificationManager.cancel(id);
    }
    
    private int mHangupBlackCallCount = 0;
    private int mHangupBlackSmsCount = 0;
    private static final int HANGUP_BLACK_CALL_NOTIFICATION = BLACK_NOTIFICATION_BASE + BLACK_LIST_NOTIFICATION_BASE + 100;
    
    public void notifyHangupBlackCall() {      
        if (DBG) log("notifyHangupBlackCall()");
        if(!canNotify()) {
            return;
        }
        mHangupBlackCallCount++;
        //begin tangyisen
        InterceptionApplication.getInstance().setUnreadCountCall(mHangupBlackCallCount);
        //end tangyisen
        final Notification.Builder builder = new Notification.Builder(mContext);         
        Drawable largeIcon = mContext.getResources().getDrawable(R.drawable.ic_launcher);
        builder.setLargeIcon(((BitmapDrawable) largeIcon).getBitmap());
        builder.setUsesChronometer(false);
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);
        updateBlackNotificationMode();
        builder.setDeleteIntent(createClearHangupBlackCallsIntent());
        builder.setContentIntent(ManageReject.createGotoRejectIntent(mContext));
        String expandedText;
        if (DBG) log("notifyHangupBlackCall() mHangupBlackCallCount =" + mHangupBlackCallCount + " mHangupBlackSmsCount =" + mHangupBlackSmsCount);
        if (mHangupBlackSmsCount != 0) {
            expandedText = mContext.getString(R.string.hangup_black_many_content, mHangupBlackCallCount + mHangupBlackSmsCount);
        } else {
            expandedText = mContext.getString(R.string.hangup_black_many_content_phone, mHangupBlackCallCount);
        }
        //builder.setContentTitle(mContext.getString(R.string.add_black_title));
        builder.setContentText(expandedText);
//        builder.setTicker(expandedText);
        //需要双指才能打开，默认还是显示省略号
        Notification notification = new Notification.BigTextStyle(builder).bigText(expandedText).build();
        notification.icon = R.drawable.ic_launcher;
        mNotificationManager.notify(HANGUP_BLACK_CALL_NOTIFICATION, notification);
        int countCall=InterceptionApplication.getInstance().getCountCall()+1;
        InterceptionApplication.getInstance().setCountCall(countCall);
   }
    
    
    public void notifyHangupBlackSms() {
        if (DBG) log("notifyHangupBlackSms()");
        if(!canNotify()) {
            return;
        }
        mHangupBlackSmsCount++;
        //begin tangyisen
        InterceptionApplication.getInstance().setUnreadCountSms(mHangupBlackSmsCount);
        //end tangyisen
        final Notification.Builder builder = new Notification.Builder(mContext);         
        Drawable largeIcon = mContext.getResources().getDrawable(R.drawable.ic_launcher);
        builder.setLargeIcon(((BitmapDrawable) largeIcon).getBitmap());
        builder.setUsesChronometer(false);
        builder.setWhen(System.currentTimeMillis());
        builder.setAutoCancel(true);
        updateBlackNotificationMode(); 
        builder.setDeleteIntent(createClearHangupBlackCallsIntent());
        builder.setContentIntent(ManageReject.createGotoRejectIntent(mContext));
        String expandedText;       
        if (DBG) log("notifyHangupBlackSms() mHangupBlackCallCount =" + mHangupBlackCallCount + " mHangupBlackSmsCount=" + mHangupBlackSmsCount);
        //expandedText = mContext.getString(R.string.hangup_black_many_content, mHangupBlackCallCount + mHangupBlackSmsCount);
        if (mHangupBlackCallCount != 0) {
            expandedText = mContext.getString(R.string.hangup_black_many_content, mHangupBlackCallCount + mHangupBlackSmsCount);
        } else {
            expandedText = mContext.getString(R.string.hangup_black_many_content_sms, mHangupBlackSmsCount);
        }
        //builder.setContentTitle(mContext.getString(R.string.add_black_title));
        builder.setContentText(expandedText);
//        builder.setTicker(expandedText);
        //需要双指才能打开，默认还是显示省略号
        Notification notification = new Notification.BigTextStyle(builder).bigText(expandedText).build();
        notification.icon = R.drawable.ic_launcher;
        mNotificationManager.notify(HANGUP_BLACK_CALL_NOTIFICATION, notification);
        
        int countSms=InterceptionApplication.getInstance().getCountSms()+1;
        InterceptionApplication.getInstance().setCountSms(countSms);
   }    
    
    public void cancelHangupBlackCallNotification() {
        if (DBG) log("cancelHangupBlackCallNotification()");
        mHangupBlackCallCount = 0;
        mHangupBlackSmsCount = 0; 
        mHangupBlackMode = 0;
        mNotificationManager.cancel(HANGUP_BLACK_CALL_NOTIFICATION);
    }
    
    private PendingIntent createClearHangupBlackCallsIntent() {
        Intent intent = new Intent(mContext, ClearBlackCallsService.class);
        intent.setAction(ClearBlackCallsService.ACTION_CLEAR_HANGUP_BLACK_CALLS);
        return PendingIntent.getService(mContext, 0, intent, 0);
    }
    
    //0为初始值，1为只有来电，2为只有短信，3为混合拦截
    private static int mHangupBlackMode = 0;
    private void updateBlackNotificationMode() {
        if(mHangupBlackCallCount > 0 && mHangupBlackSmsCount == 0 ) {
            mHangupBlackMode = 1;
        } else if(mHangupBlackCallCount == 0 && mHangupBlackSmsCount > 0) {
            mHangupBlackMode = 2;
        } else if(mHangupBlackCallCount > 0 && mHangupBlackSmsCount > 0) {
            mHangupBlackMode = 3;
        }
    }
    
    public static int getBlackNotificationMode() {
        return mHangupBlackMode;
    }

    private void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    
    private boolean canNotify() {
        SharedPreferences mPreferences = mContext.getSharedPreferences("settings",
                Context.MODE_PRIVATE);
        //tangyisen the default value should same as setting intercept,because the settings xml will add notification when intercept change
        return mPreferences.getBoolean("notification", false);
    }
    
    
}
