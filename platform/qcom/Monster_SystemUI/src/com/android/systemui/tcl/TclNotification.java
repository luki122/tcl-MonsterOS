package com.android.systemui.tcl;

import android.service.notification.StatusBarNotification;

import com.wandoujia.nisdk.core.NIFilter.FilterResult;

import java.io.Serializable;

// 用于存储notification和对应的categoryKey
public class TclNotification implements Serializable {
    private WdjStatusBarNotification mSbn;
    private WdjFilterResult mFilterResult;

    public TclNotification(StatusBarNotification sbn, FilterResult filterResult) {
        mSbn = new WdjStatusBarNotification(sbn,sbn.getUserId());
        mFilterResult = new WdjFilterResult(filterResult);
    }

    public void setSbn(StatusBarNotification sbn) {
        mSbn.setSbn(sbn);
    }

    public void setFResult(FilterResult result) {
        mFilterResult = new WdjFilterResult(result);
    }

    public StatusBarNotification getSbn() {
        return mSbn.getSbn();
    }

    public FilterResult getFilterResult() {
        return mFilterResult.getFilterResult();
    }

    public int getUid(){
        return mSbn.getUid();
    }
}
