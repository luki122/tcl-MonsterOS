package com.monster.cloud.http.data;

import com.monster.cloud.bean.RecoveryAppInfo;

import java.util.List;

/**
 * Created by xiaobin on 16-10-27.
 */
public class CloudAppRecoveryResultData {

    private List<RecoveryAppInfo> appList;
    private List<RecoveryAppInfo> recList;

    public List<RecoveryAppInfo> getAppList() {
        return appList;
    }

    public void setAppList(List<RecoveryAppInfo> appList) {
        this.appList = appList;
    }

    public List<RecoveryAppInfo> getRecList() {
        return recList;
    }

    public void setRecList(List<RecoveryAppInfo> recList) {
        this.recList = recList;
    }

    @Override
    public String toString() {
        return "CloudAppRecoveryResultData{" +
                "appList=" + appList +
                ", recList=" + recList +
                '}';
    }
}
