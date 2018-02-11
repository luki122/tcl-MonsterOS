package com.monster.market.bean;

/**
 * Created by xiaobin on 16-9-19.
 */
public class MainAppListInfo {

    public static final int TYPE_LIST = 0;
    public static final int TYPE_AD = 1;

    private int type = TYPE_LIST;
    private AdInfo adInfo;
    private AppListInfo appListInfo;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public AdInfo getAdInfo() {
        return adInfo;
    }

    public void setAdInfo(AdInfo adInfo) {
        this.adInfo = adInfo;
    }

    public AppListInfo getAppListInfo() {
        return appListInfo;
    }

    public void setAppListInfo(AppListInfo appListInfo) {
        this.appListInfo = appListInfo;
    }

    @Override
    public String toString() {
        return "MainAppListInfo{" +
                "type=" + type +
                ", adInfo=" + adInfo +
                ", appListInfo=" + appListInfo +
                '}';
    }
}
