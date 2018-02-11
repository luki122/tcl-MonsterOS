package com.monster.market.bean;

public class AdInfo {

	private int adId;
	private String adName;
	private String iconPath;
	private String adUrl;
	private String adType;

	public int getAdId() {
		return adId;
	}

	public void setAdId(int adId) {
		this.adId = adId;
	}

	public String getAdName() {
		return adName;
	}

	public void setAdName(String adName) {
		this.adName = adName;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	public String getAdUrl() {
		return adUrl;
	}

	public void setAdUrl(String adUrl) {
		this.adUrl = adUrl;
	}

	public String getAdType() {
		return adType;
	}

	public void setAdType(String adType) {
		this.adType = adType;
	}

	@Override
	public String toString() {
		return "AdInfo [adId=" + adId + ", adName=" + adName + ", iconPath="
				+ iconPath + ", adUrl=" + adUrl + ", adType=" + adType + "]";
	}

}
