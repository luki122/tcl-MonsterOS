package com.monster.cloud.http.data;

import java.util.List;

/**
 * Created by xiaobin on 16-10-27.
 */
public class CloudAppRecoveryRequestData {

    private List<CloudAppRecoveryAppInfoRequestData> appList;

    public List<CloudAppRecoveryAppInfoRequestData> getAppList() {
        return appList;
    }

    public void setAppList(List<CloudAppRecoveryAppInfoRequestData> appList) {
        this.appList = appList;
    }

    @Override
    public String toString() {
        return "CloudAppRecoveryRequestData{" +
                "appList=" + appList +
                '}';
    }
}
