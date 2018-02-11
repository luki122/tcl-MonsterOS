package com.android.systemui.tcl;

import java.io.Serializable;
import java.util.Queue;

/**
 * Created by liuzhicang on 16-10-12.
 * 序列化数据
 */

public class WdjNotification implements Serializable {
    private WdjNotifyType notifyType;
    private Queue<TclNotification> notifications;

    public WdjNotification() {
    }

    public WdjNotification(WdjNotifyType notifyType, Queue<TclNotification> notifications) {
        this.notifyType = notifyType;
        this.notifications = notifications;
    }

    public WdjNotifyType getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(WdjNotifyType notifyType) {
        this.notifyType = notifyType;
    }

    public Queue<TclNotification> getNotifications() {
        return notifications;
    }

    public void setNotifications(Queue<TclNotification> notifications) {
        this.notifications = notifications;
    }
}
