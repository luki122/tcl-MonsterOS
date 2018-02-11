package com.monster.market.bean;

import java.util.List;

public class AppDetailInfo {

	private String appIcon;
	private String bigAppIcon;
	private int appId;
	private String packageName;
	private String appName;
	private int applevel;
	private int downloads;
	private String downloadCountStr;
	private String downloadUrl;
	private long appSize;
	private String versionName;
	private int versionCode;
	private String md5;
	private String updateTime;
	private String author;
	private String changeLog;
	private String appDescription;
	private String appType;
	private List<AppImageInfo> appImageList;

	public String getAppIcon() {
		return appIcon;
	}

	public void setAppIcon(String appIcon) {
		this.appIcon = appIcon;
	}

	public String getBigAppIcon() {
		return bigAppIcon;
	}

	public void setBigAppIcon(String bigAppIcon) {
		this.bigAppIcon = bigAppIcon;
	}

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public int getApplevel() {
		return applevel;
	}

	public void setApplevel(int applevel) {
		this.applevel = applevel;
	}

	public int getDownloads() {
		return downloads;
	}

	public void setDownloads(int downloads) {
		this.downloads = downloads;
	}

	public String getDownloadCountStr() {
		return downloadCountStr;
	}

	public void setDownloadCountStr(String downloadCountStr) {
		this.downloadCountStr = downloadCountStr;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public long getAppSize() {
		return appSize;
	}

	public void setAppSize(long appSize) {
		this.appSize = appSize;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getChangelog() {
		return changeLog;
	}

	public void setChangelog(String changelog) {
		this.changeLog = changelog;
	}

	public String getAppDescription() {
		return appDescription;
	}

	public void setAppDescription(String appDescription) {
		this.appDescription = appDescription;
	}

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	public List<AppImageInfo> getAppImageList() {
		return appImageList;
	}

	public void setAppImageList(List<AppImageInfo> appImageList) {
		this.appImageList = appImageList;
	}

	@Override
	public String toString() {
		return "AppDetailInfo{" +
				"appIcon='" + appIcon + '\'' +
				", bigAppIcon='" + bigAppIcon + '\'' +
				", appId=" + appId +
				", packageName='" + packageName + '\'' +
				", appName='" + appName + '\'' +
				", applevel=" + applevel +
				", downloads=" + downloads +
				", downloadCountStr='" + downloadCountStr + '\'' +
				", downloadUrl='" + downloadUrl + '\'' +
				", appSize=" + appSize +
				", versionName='" + versionName + '\'' +
				", versionCode=" + versionCode +
				", md5='" + md5 + '\'' +
				", updateTime='" + updateTime + '\'' +
				", author='" + author + '\'' +
				", changeLog='" + changeLog + '\'' +
				", appDescription='" + appDescription + '\'' +
				", appType='" + appType + '\'' +
				", appImageList=" + appImageList +
				'}';
	}

	public static class AppImageInfo {
		private String normalPic;
		private String smallPic;

		public String getNormalPic() {
			return normalPic;
		}

		public void setNormalPic(String normalPic) {
			this.normalPic = normalPic;
		}

		public String getSmallPic() {
			return smallPic;
		}

		public void setSmallPic(String smallPic) {
			this.smallPic = smallPic;
		}

		@Override
		public String toString() {
			return "AppImageInfo [normalPic=" + normalPic + ", smallPic="
					+ smallPic + "]";
		}
	}

}
