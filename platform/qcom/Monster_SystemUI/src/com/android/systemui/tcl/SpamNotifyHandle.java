package com.android.systemui.tcl;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;

import com.android.systemui.R;
import com.wandoujia.nisdk.core.NIFilter.FilterResult;
import com.wandoujia.nisdk.core.model.NotificationPriority;

// 当前需求只要求清理非重要提醒
public class SpamNotifyHandle extends NotifyHandle {

    private static SpamNotifyHandle mHandle = null;
    private TclNotificationCleanCallback mCallBack = null;

    private SpamNotifyHandle(Context c) {
        super(c);
    }

    public static SpamNotifyHandle getInstance(Context c) {
        if (null == mHandle) {
            mHandle = new SpamNotifyHandle(c);
        }
        return mHandle;
    }

    public void setCallBack(TclNotificationCleanCallback callback) {
        mCallBack = callback;
    }

    @Override
    public void clearNotify() {
        super.clearNotify();
        updateNotifyCount();
    }

    public void updateNotifyCount() {
        if (null != mCallBack) {
            mCallBack.cleanNotify();
        }
    }

    @Override
    public boolean handleNotify(FilterResult result, StatusBarNotification sbn) {
        // TODO Auto-generated method stub
        if (NotificationPriority.SPAM == result.notificationPriority ||
                (NotificationPriority.NORMAL == result.notificationPriority && NotificationPriority.SPAM == result.categoryPriority)) {
            // 是spam类型的通知，则处理
            boolean handle = receiveNewNotify(result, sbn);

            if (null != mCallBack) {
                mCallBack.cleanNotify();
            }
            mContext.sendBroadcast(new Intent(Utils.ACTION_NOTIFICATION_DATA_CHANGE));
            return handle;
        }
        return false;
    }

    @Override
    public void popNotify() {

        ///// 第一步：获取NotificationManager
        NotificationManager nm = (NotificationManager)
                mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        final int textRes = R.string.notify_clean_num_format;
        ///// 第二步：定义Notification
        Intent intent = new Intent(mContext, NotifyInfoActivity.class);
        intent.putExtra("type", "spam");
        //PendingIntent是待执行的Intent
        PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        Notification notification = new Notification.Builder(mContext)
                .setContentTitle(mContext.getString(textRes, mNotifyNum))
                .setContentText(this.getRecentApps(3))
                .setSmallIcon(R.drawable.ic_sysbar_home)
                .setContentIntent(pi)
                .build();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.extras.putBoolean("spamNotifyClean", true);

        /////第三步：启动通知栏，第一个参数是一个通知的唯一标识
        nm.notify(0, notification);
    }

}
