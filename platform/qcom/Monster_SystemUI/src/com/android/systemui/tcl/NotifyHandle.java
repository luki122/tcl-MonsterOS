package com.android.systemui.tcl;

import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;

import com.wandoujia.nisdk.core.NIFilter.FilterResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;


public abstract class NotifyHandle {

    protected long mNotifyNum;
    protected int mCountToShow = 0;   // 用于控制界面是否显示清理提醒
    public List<WdjNotifyClearGroup> wdjNotifyClearGroups;
    // 用于对notification的排序
    private List<WdjNotifyType> wdjNotifyTypeList;
    protected Context mContext;

    protected NotifyHandle(Context context) {
        mContext = context;
        mNotifyNum = 0;
        //开机初始化时从xml读取数据
        wdjNotifyClearGroups = XmlHelper.getInstance(context).pullPhraseXml(context);
        initWdjNotifyTypeStack();
        //数据初始化后刷新下ui
        updateNotifyCount();
    }

    public List<WdjNotifyType> getWdjNotifyTypeList() {
        List<WdjNotifyType> list = new ArrayList<>();
        int n = wdjNotifyTypeList.size();
        for (int i = 0; i < n; i++) {
            list.add(wdjNotifyTypeList.get(i));
        }
        return wdjNotifyTypeList;
    }

    private void initWdjNotifyTypeStack() {
        wdjNotifyTypeList = new Stack<>();
        Iterator<WdjNotifyClearGroup> it = wdjNotifyClearGroups.iterator();
        while (it.hasNext()) {
            WdjNotifyClearGroup group = it.next();
            wdjNotifyTypeList.add(group.getNotifyType());
            mNotifyNum += group.getWdjNotifyClearItems().size();
        }
    }

    public void resetCountToShow() {
        mCountToShow = 0;
    }

    // 接收了一条新的通知，保存该类通知的数目,并保存该notify
    protected boolean receiveNewNotify(FilterResult result, StatusBarNotification sbn) {

        mNotifyNum++;
        mCountToShow++;

        WdjNotifyType notifyType = new WdjNotifyType(sbn.getPackageName(), sbn.getUid(), result.categoryKey, System.currentTimeMillis());
        Queue<WdjNotifyClearItem> wdjNotifyClearItems = getWdjNotifyClearItems(notifyType);
        WdjNotifyClearItem wdjNotifyClearItem = new WdjNotifyClearItem(sbn, result);

        if (null == wdjNotifyClearItems) {
            wdjNotifyClearItems = new LinkedList<WdjNotifyClearItem>();
            wdjNotifyClearItems.add(wdjNotifyClearItem);
            WdjNotifyClearGroup notification = new WdjNotifyClearGroup(notifyType, wdjNotifyClearItems);
            wdjNotifyClearGroups.add(notification);
        } else {
            // 清理失效的提醒
            synchronized (wdjNotifyClearItems) {
                Iterator<WdjNotifyClearItem> it = wdjNotifyClearItems.iterator();
                boolean cotains = false;
                while (it.hasNext()) {
                    WdjNotifyClearItem item = it.next();
                    if (item.getSbn().getKey().equals(wdjNotifyClearItem.getSbn().getKey())) {
                        cotains = true;
                        it.remove();
                        mNotifyNum--;
                        mCountToShow--;
                        break;
                    }
                }
                if (!cotains) {
                    //统计app被清理的通知数量
                    Utils.saveNotifyCount(mContext, sbn.getPackageName(), false);
                }
                // 压入新的提醒
                wdjNotifyClearItems.add(wdjNotifyClearItem);
            }
            // 删除该包名
            wdjNotifyTypeList.remove(notifyType);
        }
        wdjNotifyTypeList.add(notifyType);
        updateWdjNotifyClearGroups(notifyType);

        return true;
    }


    public Queue<WdjNotifyClearItem> getWdjNotifyClearItems(WdjNotifyType notifyType) {
        for (WdjNotifyClearGroup notification : wdjNotifyClearGroups) {
            if (notification.getNotifyType().equals(notifyType)) {
                return notification.getWdjNotifyClearItems();
            }
        }
        return null;
    }

    // 清空提醒
    public void clearNotify() {
        wdjNotifyClearGroups.clear();
        wdjNotifyTypeList.clear();
        mNotifyNum = 0;
        mCountToShow = 0;
        XmlHelper.getInstance(mContext).writeXML(wdjNotifyClearGroups);
    }

    public long getNotifyNum() {
        return mNotifyNum;
    }

    // 判断是否是本类型的提醒，如果是则处理并返回true，如果不是则返回false
    public abstract boolean handleNotify(FilterResult result, StatusBarNotification sbn);

    public abstract void updateNotifyCount();

    private void savdXmlData() {
        XmlHelper.getInstance(mContext).writeXML(wdjNotifyClearGroups);
    }

    private void updateWdjNotifyClearGroups(WdjNotifyType notifyType) {
        Iterator<WdjNotifyClearGroup> it = wdjNotifyClearGroups.iterator();
        while (it.hasNext()) {
            WdjNotifyClearGroup wdjNotifyClearGroup = it.next();
            if (wdjNotifyClearGroup.getNotifyType().equals(notifyType)) {
                wdjNotifyClearGroup.setNotifyType(notifyType);
                break;
            }
        }
        savdXmlData();
    }

    protected void updateWdjNotifyClearGroups(String pkg) {
        Iterator<WdjNotifyClearGroup> it = wdjNotifyClearGroups.iterator();
        while (it.hasNext()) {
            WdjNotifyClearGroup wdjNotifyClearGroup = it.next();
            if (wdjNotifyClearGroup.getNotifyType().getPackageName().equals(pkg)) {
                mNotifyNum -= wdjNotifyClearGroup.getWdjNotifyClearItems().size();
                wdjNotifyTypeList.remove(wdjNotifyClearGroup.getNotifyType());
                it.remove();
            }
        }
        savdXmlData();
        updateNotifyCount();
        mContext.sendBroadcast(new Intent(Utils.ACTION_NOTIFICATION_DATA_CHANGE));
    }

    //统计应用通知总数时判断通知是否在已清理列表，如果不在则总数+1
    public boolean isNotifyInCleaned(StatusBarNotification statusBarNotification) {
        Iterator<WdjNotifyClearGroup> it = wdjNotifyClearGroups.iterator();
        while (it.hasNext()) {
            WdjNotifyClearGroup wdjNotifyClearGroup = it.next();
            Iterator<WdjNotifyClearItem> tct = wdjNotifyClearGroup.getWdjNotifyClearItems().iterator();
            while (tct.hasNext()) {
                WdjNotifyClearItem item = tct.next();
                if (item.getSbn().getKey().equals(statusBarNotification.getKey()))
                    return true;

            }
        }
        return false;
    }

}
