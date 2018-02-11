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

}
