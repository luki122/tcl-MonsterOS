package com.monster.market.http.data;

/**
 * Created by xiaobin on 16-8-8.
 */
public class ReportDownloadInfoRequestData {

    private int downloads;  // 下载次数
    private long appId;     // 应用ID
    private int fromId;     // 下载来源 默认0
    private int downloadType = 1;   // 下载类型（下载类型   1：下载  2：更新）
    private int modulId;    // 模块ID（主页、排行….）
    private String packageName;     //  包名
    private String downloadTime;    // 下载时间

    public int getDownloads() {
        return downloads;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public int getDownloadType() {
        return downloadType;
    }

    public void setDownloadType(int downloadType) {
        this.downloadType = downloadType;
    }

    public int getModulId() {
        return modulId;
    }

    public void setModulId(int modulId) {
        this.modulId = modulId;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDownloadTime() {
        return downloadTime;
    }

    public void setDownloadTime(String downloadTime) {
        this.downloadTime = downloadTime;
    }

    @Override
    public String toString() {
        return "ReportDownloadInfoRequestData{" +
                "downloads=" + downloads +
                ", appId=" + appId +
                ", fromId=" + fromId +
                ", downloadType=" + downloadType +
                ", modulId=" + modulId +
                ", packageName='" + packageName + '\'' +
                ", downloadTime='" + downloadTime + '\'' +
                '}';
    }
}
