package com.monster.market.http.data;

/**
 * Created by xiaobin on 16-7-20.
 */
public class SearchAppRequestData extends BasePageInfoData {

    private String key;
    private String ip;
    private String macAddress;
    private int apiLevel;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public int getApiLevel() {
        return apiLevel;
    }

    public void setApiLevel(int apiLevel) {
        this.apiLevel = apiLevel;
    }
}
