/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.util;

public class QRCodeInfo {
    private String mSsid;
    private long mStorage;
    public void setSsid(String ssid) {
        mSsid = ssid;
    }
    public void setStorage(long storage) {
        mStorage = storage;
    }
    public String getSsid() {
        return mSsid;
    }
    public long getStorage() {
        return mStorage;
    }
}
