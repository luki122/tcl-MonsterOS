/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.operator.wifi;

public class HotspotBean {
    private String BSSID;
    private String SSID;
    private int level;

    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public String getBSSID() {
        return BSSID;
    }
    public void setBSSID(String bSSID) {
        BSSID = bSSID;
    }
    public String getSSID() {
        return SSID;
    }
    public void setSSID(String sSID) {
        SSID = sSID;
    }

}
