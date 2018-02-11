package com.monster.market.http.data;

import java.util.List;

import com.monster.market.bean.AppUpgradeInfo;

public class AppUpgradeListResultData {

	private int updates;
	private List<AppUpgradeInfo> appList;

	public int getUpdates() {
		return updates;
	}

	public void setUpdates(int updates) {
		this.updates = updates;
	}

	public List<AppUpgradeInfo> getAppList() {
		return appList;
	}

	public void setAppList(List<AppUpgradeInfo> appList) {
		this.appList = appList;
	}

	@Override
	public String toString() {
		return "AppUpgradeListResultData [updates=" + updates + ", appList="
				+ appList + "]";
	}

}
