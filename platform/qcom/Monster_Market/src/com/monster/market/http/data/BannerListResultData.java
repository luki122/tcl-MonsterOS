package com.monster.market.http.data;

import java.util.List;

import com.monster.market.bean.BannerInfo;

public class BannerListResultData {
	
	private List<BannerInfo> bannerList;

	public List<BannerInfo> getBannerList() {
		return bannerList;
	}

	public void setBannerList(List<BannerInfo> bannerList) {
		this.bannerList = bannerList;
	}

	@Override
	public String toString() {
		return "BannerListResultData [bannerList=" + bannerList + "]";
	}

}
