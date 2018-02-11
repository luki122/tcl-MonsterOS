package com.monster.market.bean;

public class BannerInfo {

	private int bannerId;
	private String bannerName;
	private String iconPath;
	private String bannerUrl;
	private String bannerType;	// Banner类型(广告类型  1：应用 2：专题 3：抢号 4：礼包  5：开服 6：网址 7：活动)

	public int getBannerId() {
		return bannerId;
	}

	public void setBannerId(int bannerId) {
		this.bannerId = bannerId;
	}

	public String getBannerName() {
		return bannerName;
	}

	public void setBannerName(String bannerName) {
		this.bannerName = bannerName;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	public String getBannerUrl() {
		return bannerUrl;
	}

	public void setBannerUrl(String bannerUrl) {
		this.bannerUrl = bannerUrl;
	}

	public String getBannerType() {
		return bannerType;
	}

	public void setBannerType(String bannerType) {
		this.bannerType = bannerType;
	}

	@Override
	public String toString() {
		return "BannerInfo [bannerId=" + bannerId + ", bannerName="
				+ bannerName + ", iconPath=" + iconPath + ", bannerUrl="
				+ bannerUrl + ", bannerType=" + bannerType + "]";
	}

}
