package com.monster.market.bean;

public class AppUpgradeInfo {

	private int appId;
	private String appIcon;
	private String appName;
	private long appSize;
	private long appSizeNew;
	private int versionCode;
	private int versionCodeNew;
	private String versionName;
	private String versionNameNew;
	private String downloadUrlDif;
	private String downloadUrl;
	private String packageName;

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public String getAppIcon() {
		return appIcon;
	}

	public void setAppIcon(String appIcon) {
		this.appIcon = appIcon;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public long getAppSize() {
		return appSize;
	}

	public void setAppSize(long appSize) {
		this.appSize = appSize;
	}

	public long getAppSizeNew() {
		return appSizeNew;
	}

	public void setAppSizeNew(long appSizeNew) {
		this.appSizeNew = appSizeNew;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public int getVersionCodeNew() {
		return versionCodeNew;
	}

	public void setVersionCodeNew(int versionCodeNew) {
		this.versionCodeNew = versionCodeNew;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String appVersionName) {
		this.versionName = appVersionName;
	}

	public String getVersionNameNew() {
		return versionNameNew;
	}

	public void setVersionNameNew(String appVersionNameNew) {
		this.versionNameNew = appVersionNameNew;
	}

	public String getDownloadUrlDif() {
		return downloadUrlDif;
	}

	public void setDownloadUrlDif(String downloadUrlDif) {
		this.downloadUrlDif = downloadUrlDif;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public String toString() {
		return "AppUpgradeInfo{" +
				"appId=" + appId +
				", appIcon='" + appIcon + '\'' +
				", appName='" + appName + '\'' +
				", appSize=" + appSize +
				", appSizeNew=" + appSizeNew +
				", versionCode=" + versionCode +
				", versionCodeNew=" + versionCodeNew +
				", appVersionName='" + versionName + '\'' +
				", appVersionNameNew='" + versionNameNew + '\'' +
				", downloadUrlDif='" + downloadUrlDif + '\'' +
				", downloadUrl='" + downloadUrl + '\'' +
				", packageName='" + packageName + '\'' +
				'}';
	}
}
