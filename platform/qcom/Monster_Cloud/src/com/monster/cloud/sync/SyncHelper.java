package com.monster.cloud.sync;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

import com.monster.cloud.R;
import com.monster.cloud.activity.MainActivity;
import com.monster.cloud.utils.LoginUtil;
import com.tencent.qqpim.sdk.accesslayer.LoginMgrFactory;
import com.tencent.qqpim.sdk.accesslayer.interfaces.ILoginMgr;
import com.tencent.qqpim.sdk.utils.AccountUtils;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by logic on 16-12-15.
 */
public final class SyncHelper {

    public static List<BaseSyncTask> getAllSyncTasks(final Context context, final TCLSyncManager.UIHandler handler){
        return new ArrayList<BaseSyncTask>(){
            {
                if(isCTTAutoSyncEnable(context)) {
                    add(new SyncContactTask(context, handler));
                }
                if (isSMSAutoSyncEnable(context)) {
                    add(new SyncSMSTask(context, handler));
                }
                if (isRCDAutoSyncEnable(context)) {
                    add(new SyncCallLogTask(context, handler));
                }
                if (isAPPAutoSyncEnable(context)) {
                    add(new SyncSoftTask(context, handler));
                }
            }
        };
    }

    public static BaseSyncTask getSyncTask(int taskType, Context mContext, TCLSyncManager.UIHandler mUiHandler) {
        if (taskType == BaseSyncTask.TASK_TYPE_SYNC_CONTACT) {
            return new SyncContactTask(mContext, mUiHandler);
        }else if (taskType == BaseSyncTask.TASK_TYPE_SYNC_CALLLOG) {
            return new SyncCallLogTask(mContext, mUiHandler);
        }else if (taskType == BaseSyncTask.TASK_TYPE_SYNC_SMS) {
            return new SyncSMSTask(mContext, mUiHandler);
        }else if (taskType == BaseSyncTask.TASK_TYPE_SYNC_SOFT) {
            return new SyncSoftTask(mContext, mUiHandler);
        }
        return null;
    }

    public static List<BaseSyncTask> getBackgroundSyncTasks(final  Context context, final  TCLSyncManager.UIHandler handler){
        return new ArrayList<BaseSyncTask>(){
            {
                if (isSMSAutoSyncEnable(context)) {
                    add(new SyncSMSTask(context, handler));
                }
                if (isRCDAutoSyncEnable(context)) {
                    add(new SyncCallLogTask(context, handler));
                }
                if (isAPPAutoSyncEnable(context)) {
                    add(new SyncSoftTask(context, handler));
                }
            }
        };
    }

    public static boolean qqSdkLogin(Context context) {
        if (null == context )
            return false;
        ILoginMgr loginMgr = LoginMgrFactory.getLoginMgr(context, AccountUtils.ACCOUNT_TYPE_QQ_SDK);
        String token = LoginUtil.getToken(context);
        String openId = LoginUtil.getOpenId(context);
        final int result = loginMgr.qqSDKlogin(openId, token, TCLSyncManager.APPID);
        if (result == 0)
            return true;
        if (result == 5001) {
            //出现这种错误，需将SyncManager结束
            actionNotification(context);
        }
        return false;
    }

    public static void actionNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);
        Notification notification = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentTitle(context.getString(R.string.qq_token_expired))
                .setContentText(context.getString(R.string.qq_relogin))
                .setWhen(System.currentTimeMillis())
                .setOngoing(true)
                .setContentIntent(pIntent)
                .setSmallIcon(R.drawable.cloud_icon)
                .build();
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(5001, notification);
    }

    public static boolean isWifiSyncEnable(Context cxt){
       SharedPreferences sp = cxt.getSharedPreferences("Cloud_Preference", Context.MODE_MULTI_PROCESS);
        return sp.getBoolean(PARAM_IS_SYNC_WHEN_WIFI, false);
    }

    public static boolean isSMSAutoSyncEnable(Context cxt){
        SharedPreferences sp = cxt.getSharedPreferences("Cloud_Preference", Context.MODE_MULTI_PROCESS);
        return sp.getBoolean(PARAM_IS_SMS_AUTO_SYNC, false);
    }
    public static boolean isRCDAutoSyncEnable(Context cxt){
        SharedPreferences sp = cxt.getSharedPreferences("Cloud_Preference", Context.MODE_MULTI_PROCESS);
        return sp.getBoolean(PARAM_IS_RCD_AUTO_SYNC, false);
    }
    public static boolean isAPPAutoSyncEnable(Context cxt){
        SharedPreferences sp = cxt.getSharedPreferences("Cloud_Preference", Context.MODE_MULTI_PROCESS);
        return sp.getBoolean(PARAM_IS_APP_AUTO_SYNC, false);
    }
    public static boolean isCTTAutoSyncEnable(Context cxt){
        SharedPreferences sp = cxt.getSharedPreferences("Cloud_Preference", Context.MODE_MULTI_PROCESS);
        return sp.getBoolean(PARAM_IS_CTT_AUTO_SYNC, false);
    }

    public static boolean isAllSyncEnable(Context cxt){
        return isCTTAutoSyncEnable(cxt) || isSMSAutoSyncEnable(cxt) || isRCDAutoSyncEnable(cxt) ||isAPPAutoSyncEnable(cxt);
    }

    //has synchronized
    public static final String PARAM_IS_CTT_AUTO_SYNC = "is_contact_sync";
    public static final String PARAM_IS_SMS_AUTO_SYNC = "is_sms_sync";
    public static final String PARAM_IS_RCD_AUTO_SYNC = "is_record_sync";
    public static final String PARAM_IS_APP_AUTO_SYNC = "is_app_list_sync";
    //sync only when wifi on
    private static final String PARAM_IS_SYNC_WHEN_WIFI = "is_sync_when_wifi";
}
