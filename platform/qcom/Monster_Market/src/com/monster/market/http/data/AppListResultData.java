package com.monster.market.http.data;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.monster.market.bean.AppListInfo;

public class AppListResultData extends BasePageInfoData {

	private List<AppListInfo> appList;

	public List<AppListInfo> getAppList() {
		return appList;
	}

	public void setAppList(List<AppListInfo> appList) {
		this.appList = appList;
	}

	@Override
	public String toString() {
		return "AppListResultData [pageNum=" + pageNum + ", pageSize="
				+ pageSize + ", appList=" + appList + "]";
	}

}
