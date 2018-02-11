package com.monster.market.http.data;

import com.monster.market.bean.AppListInfo;
import com.monster.market.bean.TopicInfo;

import java.util.List;

public class TopicDetailResultData extends BasePageInfoData {

	private TopicInfo specialTopic;
	private List<AppListInfo> appList;

	public TopicInfo getSpecialTopic() {
		return specialTopic;
	}

	public void setSpecialTopic(TopicInfo specialTopic) {
		this.specialTopic = specialTopic;
	}

	public List<AppListInfo> getAppList() {
		return appList;
	}

	public void setAppList(List<AppListInfo> appList) {
		this.appList = appList;
	}

	@Override
	public String toString() {
		return "TopicDetailResultData{" +
				"specialTopic=" + specialTopic +
				", appList=" + appList +
				'}';
	}

}
