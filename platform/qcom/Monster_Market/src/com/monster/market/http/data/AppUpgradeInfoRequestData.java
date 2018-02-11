package com.monster.market.http.data;

/**
 * Created by xiaobin on 16-7-20.
 */
public class AppUpgradeInfoRequestData {

    private int versionCode;
    private String packageName;
    private String versionName;
    private String md5;
    private String cerStrMd5;

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getCerStrMd5() {
        return cerStrMd5;
    }

    public void setCerStrMd5(String cerStrMd5) {
        this.cerStrMd5 = cerStrMd5;
    }

    @Override
    public String toString() {
        return "AppUpgradeInfoRequestData{" +
                "appVersion='" + versionCode + '\'' +
                ", packageName='" + packageName + '\'' +
                ", versionName='" + versionName + '\'' +
                ", md5='" + md5 + '\'' +
                ", cerStrMd5='" + cerStrMd5 + '\'' +
                '}';
    }
}
