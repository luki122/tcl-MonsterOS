package com.monster.market.http.data;

import java.util.List;

import com.monster.market.bean.AppTypeInfo;

public class AppTypeListResultData extends BasePageInfoData {

	private List<AppTypeInfo> appTypeList;

	public List<AppTypeInfo> getAppTypeList() {
		return appTypeList;
	}

	public void setAppTypeList(List<AppTypeInfo> appTypeList) {
		this.appTypeList = appTypeList;
	}

	@Override
	public String toString() {
		return "AppTypeListResultData [appTypeList=" + appTypeList + "]";
	}

}
