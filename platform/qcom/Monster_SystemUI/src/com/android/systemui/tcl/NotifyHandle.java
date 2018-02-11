package com.android.systemui.tcl;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.wandoujia.nisdk.core.NIFilter.FilterResult;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;


public abstract class NotifyHandle {

    protected long mNotifyNum;
    protected int mCountToShow = 0;   // 用于控制界面是否显示清理提醒
    public List<WdjNotification> mNotifyMap;
    // 用于对notification的排序
    private Stack<WdjNotifyType> mPackageStack;
    protected Context mContext;

    protected NotifyHandle(Context context) {
        mContext = context;
        mNotifyNum = 0;
        //开机初始化时从xml读取数据
        mNotifyMap = XmlHelper.getInstance(context).pullPhraseXml(context);
        initPackageStack();
        //数据初始化后刷新下ui
        updateNotifyCount();
    }

    public Stack<WdjNotifyType> getPackageStack() {
        Stack<WdjNotifyType> wdjNotifyTypes = new Stack<>();
        for (WdjNotifyType type : mPackageStack) {
            wdjNotifyTypes.add(type);
        }
        return wdjNotifyTypes;
    }

    private void initPackageStack() {
        mPackageStack = new Stack<>();
        Iterator<WdjNotification> it = mNotifyMap.iterator();
        while (it.hasNext()) {
            WdjNotification tclNotify = it.next();
            mPackageStack.push(tclNotify.getNotifyType());
            mNotifyNum += tclNotify.getNotifications().size();
        }
    }

    public void resetCountToShow() {
        mCountToShow = 0;
    }

    // 接收了一条新的通知，保存该类通知的数目,并保存该notify
    protected boolean receiveNewNotify(FilterResult result, StatusBarNotification sbn) {

        mNotifyNum++;
        mCountToShow++;

        WdjNotifyType notifyType = new WdjNotifyType(sbn.getPackageName(), sbn.getUserId(), result.categoryKey);
        Queue<TclNotification> queue = getNotifyQueue(notifyType);
        TclNotification tclSbn = new TclNotification(sbn, result);

        if (null == queue) {
            queue = new LinkedList<TclNotification>();
            queue.add(tclSbn);
            WdjNotification notification = new WdjNotification(notifyType, queue);
            mNotifyMap.add(notification);
        } else {
            // 清理失效的提醒
            synchronized (queue) {
                Iterator<TclNotification> it = queue.iterator();
                boolean cotains = false;
                while (it.hasNext()) {
                    TclNotification tclNotify = it.next();
                    if (tclNotify.getSbn().getKey().equals(tclSbn.getSbn().getKey())) {
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
                queue.add(tclSbn);
            }
            // 删除该包名
            mPackageStack.remove(notifyType);
        }
        mPackageStack.push(notifyType);
        //将数据写入xml，重启之后可恢复数据
        savdXmlData();
        return true;
    }

    public Queue<TclNotification> getNotifyQueue(WdjNotifyType notifyType) {
        for (WdjNotification notification : mNotifyMap) {
            if (notification.getNotifyType().equals(notifyType)) {
                return notification.getNotifications();
            }
        }
        return null;
    }

    // 清空提醒
    public void clearNotify() {
        mNotifyMap.clear();
        mPackageStack.clear();
        mNotifyNum = 0;
        mCountToShow = 0;
        XmlHelper.getInstance(mContext).writeXML(mNotifyMap);
    }

    public long getNotifyNum() {
        return mNotifyNum;
    }

    public String getRecentApps(int num) {
        String result = "";
        int loop = num > 0 ? num : 3;
        loop = loop > mPackageStack.size() ? mPackageStack.size() : loop;

        int i = 0;
        for (i = 0; i < loop; i++) {
//            result += Utils.getApplicationLabelAsUser(mContext, mPackageStack.get(i).getPackageName(),mPackageStack.get(i).getUserId()) + " ";
        }
        result = result.trim();
        result.replace(" ", ";");

        return result;
    }

    private boolean isEmptyNotification(StatusBarNotification sbn) {
        if (sbn != null) {
            Notification notification = sbn.getNotification();
            CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence content = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
            Log.e("kebelzc24", "receive notify title = " + title + "---- text = " + content + "---- EXTRA = " + notification.extras.toString());
            if (title != null || content != null) {
                return false;
            }
        }
        return true;
    }

    // 判断是否是本类型的提醒，如果是则处理并返回true，如果不是则返回false
    public abstract boolean handleNotify(FilterResult result, StatusBarNotification sbn);

    // 弹出提示框
    public abstract void popNotify();

    public abstract void updateNotifyCount();

    private void savdXmlData() {
        XmlHelper.getInstance(mContext).writeXML(mNotifyMap);
    }

    protected void updateNotifyMap(String pkg) {
        Iterator<WdjNotification> it = mNotifyMap.iterator();
        while (it.hasNext()) {
            WdjNotification wdjNotification = it.next();
            if (wdjNotification.getNotifyType().getPackageName().equals(pkg)) {
                mNotifyNum -= wdjNotification.getNotifications().size();
                mPackageStack.remove(wdjNotification.getNotifyType());
                it.remove();
            }
        }
        savdXmlData();
        updateNotifyCount();
        mContext.sendBroadcast(new Intent(Utils.ACTION_NOTIFICATION_DATA_CHANGE));
    }

    //统计应用通知总数时判断通知是否在已清理列表，如果不在则总数+1
    public boolean isNotifyInCleaned(StatusBarNotification statusBarNotification) {
        Iterator<WdjNotification> it = mNotifyMap.iterator();
        while (it.hasNext()) {
            WdjNotification wdjNotification = it.next();
            Iterator<TclNotification> tct = wdjNotification.getNotifications().iterator();
            while (tct.hasNext()) {
                TclNotification tclNotify = tct.next();
                if (tclNotify.getSbn().getKey().equals(statusBarNotification.getKey()))
                    return true;

            }
        }
        return false;
    }

}
