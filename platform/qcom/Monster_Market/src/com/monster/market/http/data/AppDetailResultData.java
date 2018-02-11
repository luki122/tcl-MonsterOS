package com.monster.market.http.data;

import java.util.List;

import com.monster.market.bean.AppDetailInfo;

public class AppDetailResultData {




	private List<AppDetailInfo> appList;

	public List<AppDetailInfo> getAppList() {
		return appList;
	}

	public void setAppList(List<AppDetailInfo> appList) {
		this.appList = appList;
	}

	@Override
	public String toString() {
		return "AppDetailResultData [appList=" + appList + "]";
	}

}
