package com.monster.market.http.data;

import java.util.List;

import com.monster.market.bean.AdInfo;

public class AdListResultData {

	private List<AdInfo> adList;

	public List<AdInfo> getAdList() {
		return adList;
	}

	public void setAdList(List<AdInfo> adList) {
		this.adList = adList;
	}

	@Override
	public String toString() {
		return "AdListResultData [adList=" + adList + "]";
	}

}
