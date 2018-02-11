package com.monster.market.http.data;

import com.monster.market.bean.AppListInfo;

import java.util.List;

/**
 * Created by xiaobin on 16-9-13.
 */
public class AppDetailRecommendResultData extends BasePageInfoData {

    private List<AppListInfo> appList;

    public List<AppListInfo> getAppList() {
        return appList;
    }

    public void setAppList(List<AppListInfo> appList) {
        this.appList = appList;
    }
}
