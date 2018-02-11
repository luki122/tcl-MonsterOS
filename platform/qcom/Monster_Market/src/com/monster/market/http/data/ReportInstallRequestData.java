package com.monster.market.http.data;

/**
 * Created by xiaobin on 16-8-8.
 */
public class ReportInstallRequestData {

    private long appId;
    private String state;       // 0失败 1成功
    private String installType;
    private String installTime;
    private String packageName;

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getInstallType() {
        return installType;
    }

    public void setInstallType(String installType) {
        this.installType = installType;
    }

    public String getInstallTime() {
        return installTime;
    }

    public void setInstallTime(String installTime) {
        this.installTime = installTime;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
