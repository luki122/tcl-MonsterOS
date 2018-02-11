package com.monster.market.http.data;

import com.google.gson.annotations.SerializedName;
import com.monster.market.bean.AppListInfo;

import java.util.List;

public class SearchAppListResultData extends BasePageInfoData {

	@SerializedName("keyList")
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
