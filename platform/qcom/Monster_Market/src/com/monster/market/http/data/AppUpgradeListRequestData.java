package com.monster.market.http.data;

import java.util.List;

/**
 * Created by xiaobin on 16-7-20.
 */
public class AppUpgradeListRequestData {

    private String resolution;
    private int sdkVersion;

    private List<AppUpgradeInfoRequestData> upgradeList;

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public int getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(int sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public List<AppUpgradeInfoRequestData> getUpgradeList() {
        return upgradeList;
    }

    public void setUpgradeList(List<AppUpgradeInfoRequestData> upgradeList) {
        this.upgradeList = upgradeList;
    }
}
