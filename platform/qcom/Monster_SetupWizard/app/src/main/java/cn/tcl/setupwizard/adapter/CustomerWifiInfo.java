/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.setupwizard.adapter;

/**
 * wifi info
 *
 */
public class CustomerWifiInfo {


    private String ssid;
    private String bssid;
    private String linkSpreed;
    private String frequency;
    private String ip;
    private String security;
    private int level;
    private boolean isConnected;
    /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
    private int state;  // 1:unconnected, 2:connecting, 3:connected

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }

    public String getLinkSpreed() {
        return linkSpreed;
    }

    public void setLinkSpreed(String linkSpreed) {
        this.linkSpreed = linkSpreed;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-04,BUG-2669930*/
    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append(" ssid="+ssid);
        sb.append(" level="+level);
        return sb.toString();
    }
    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
}
