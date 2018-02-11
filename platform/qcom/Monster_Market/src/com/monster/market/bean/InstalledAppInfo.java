package com.monster.market.bean;

public class InstalledAppInfo {

	public static final int FLAG_SYSTEM = 0;
	public static final int FLAG_USER = 1;
	public static final int FLAG_UPDATE = 2;

	private String name; // 软件名称
	private int iconId; // 图标ID
	private int versionCode; // 版本号
	private String version; // 软件版本
	private String packageName; // 包名
	private String apkPath; // 安装后的包路径

	private int appFlag = FLAG_SYSTEM;

	private String md5;
	private String cerStrMd5;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIconId() {
		return iconId;
	}

	public void setIconId(int iconId) {
		this.iconId = iconId;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getApkPath() {
		return apkPath;
	}

	public void setApkPath(String apkPath) {
		this.apkPath = apkPath;
	}

	public int getAppFlag() {
		return appFlag;
	}

	public void setAppFlag(int appFlag) {
		this.appFlag = appFlag;
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
}
