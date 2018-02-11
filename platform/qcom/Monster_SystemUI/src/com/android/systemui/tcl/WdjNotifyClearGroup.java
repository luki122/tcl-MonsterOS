package com.android.systemui.tcl;

import java.io.Serializable;
import java.util.Queue;

/**
 * Created by liuzhicang on 16-10-12.
 * 序列化数据
 */

public class WdjNotifyClearGroup implements Serializable {
    private WdjNotifyType notifyType;
    private Queue<WdjNotifyClearItem> wdjNotifyClearItems;

    public WdjNotifyClearGroup() {
    }

    public WdjNotifyClearGroup(WdjNotifyType notifyType, Queue<WdjNotifyClearItem> wdjNotifyClearItems) {
        this.notifyType = notifyType;
        this.wdjNotifyClearItems = wdjNotifyClearItems;
    }

    public WdjNotifyType getNotifyType() {
        return notifyType;
    }

    public void setNotifyType(WdjNotifyType notifyType) {
        this.notifyType = notifyType;
    }

    public Queue<WdjNotifyClearItem> getWdjNotifyClearItems() {
        return wdjNotifyClearItems;
    }

    public void setWdjNotifyClearItems(Queue<WdjNotifyClearItem> wdjNotifyClearItems) {
        this.wdjNotifyClearItems = wdjNotifyClearItems;
    }
}
