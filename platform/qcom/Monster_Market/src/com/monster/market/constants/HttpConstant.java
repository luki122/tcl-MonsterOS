package com.monster.market.constants;

public class HttpConstant {
	
	public static final int RESULT_CODE_SUCCESS = 0;
	public static final int RESULT_CODE_FAIL = 1;

	public static String HTTP_BASE = "http://appstore.tclchn.com";
	public static String getBaseApi() {
		return HTTP_BASE + "/appstore-web-api/";
	}

	// 获取首页banner
	public static String getBannerUrl() {
		return getBaseApi() + "banner";
	}
	// 获取首页列表
	public static String getHomePageUrl() {
		return getBaseApi() + "index";
	}
	// 获取广告列表
	public static String getAdListUrl() {
		return getBaseApi() + "ad";
	}
	// 获取搜索提示列表
	public static String getSearchKeyListUrl() {
		return getBaseApi() + "searchKey";
	}
	// 获取热门搜索
	public static String getSearchTopKeyUrl() {
		return getBaseApi() + "searchPopKey";
	}
	// 获取APP搜索列表
	public static String getSearchAppListUrl() {
		return getBaseApi() + "search";
	}
	// 获取APP详情
	public static String getAppDetailUrl() {
		return getBaseApi() + "details";
	}
	// 获取关联推荐
	public static String getAppDetailsRecommendUrl() {
		return getBaseApi() + "detailsRecommend";
	}
	// 获取排行列表
	public static String getRankingAppListUrl() {
		return getBaseApi() + "ranking";
	}
	// 获取应用分类
	public static String getAppTypeUrl() {
		return getBaseApi() + "appType";
	}
	// 获取应用分类详情
	public static String getAppTypeInfoUrl() {
		return getBaseApi() + "appTypeInfo";
	}
	// 获取应用新品
	public static String getNewAppListUrl() {
		return getBaseApi() + "newApp";
	}
	// 获取应用更新
	public static String getAppUpdateListUrl() {
		return getBaseApi() + "appUpdate";
	}
	// 获取专题列表
	public static String getTopicListUrl() {
		return getBaseApi() + "specialTopic";
	}
	// 获取专题详情
	public static String getTopicDetailUrl() {
		return getBaseApi() + "specialTopicDetail";
	}
	// 获取设计奖列表
	public static String getAwardUrl() {
		return getBaseApi() + "award";
	}
	// 必备接口
	public static String getEssentialUrl() {
		return getBaseApi() + "essential";
	}
	// 上报广告点击量
	public static String getReportAdClickUrl() {
		return getBaseApi() + "report/adClick";
	}
	// 上报下载信息
	public static String getReportDownloadInfoUrl() {
		return getBaseApi() + "report/downloadedInfo";
	}
    // 上报安装信息
	public static String getReportInstallUrl() {
		return getBaseApi() + "report/install";
	}
	// 上报下载信息列表
	public static String getReportDownloadInfoListUrl() {
		return getBaseApi() + "report/DownloadInfoList";
	}


	/*
	1	广告推荐
	2	主页应用推荐
	3	重点推广应用
	4	新品推荐
	5	游戏排行
	6	应用排行
	7	应用分类
	8	搜索
	9	设计奖
	10  游戏必备
	11  应用必备
	12  云服务
	*/
	public static final int REPORT_MODULID_BANNER = 1;
	public static final int REPORT_MODULID_HOMEPAGE = 2;
	public static final int REPORT_MODULID_AD = 3;
	public static final int REPORT_MODULID_NEW = 4;
	public static final int REPORT_MODULID_GAME_RANKING = 5;
	public static final int REPORT_MODULID_APP_RANKING = 6;
	public static final int REPORT_MODULID_CATEGORY = 7;
	public static final int REPORT_MODULID_SEARCH = 8;
	public static final int REPORT_MODULID_AWARD = 9;
	public static final int REPORT_MODULID_GAME_ESSENTIAL = 10;
	public static final int REPORT_MODULID_APP_ESSENTIAL = 11;
	public static final int REPORT_MODULID_CLOUD = 12;

	// 广告类型
	public static final String AD_TYPE_APP = "1";			// 应用
	public static final String AD_TYPE_TOPIC = "2";			// 专题
	public static final String AD_TYPE_GRAB = "3";			// 抢号
	public static final String AD_TYPE_GIFTS = "4";			// 礼包
	public static final String AD_TYPE_OPEN_SERVICE = "5";	// 开服
	public static final String AD_TYPE_LINK = "6";			// 网址
	public static final String AD_TYPE_ACTIVITY = "7";		// 活动

	public static final int CACHE_TYPE_BANNER = 1;
	public static final int CACHE_TYPE_HOMEPAGE = 2;
	public static final int CACHE_TYPE_AD = 3;
	public static final int CACHE_TYPE_NEW = 4;
	public static final int CACHE_TYPE_RANKING = 5;
	public static final int CACHE_TYPE_CATEGORY = 6;
	public static final int CACHE_TYPE_ESSENTIAL = 7;
	public static final int CACHE_TYPE_AWARD = 8;
	public static final int CACHE_TYPE_CATEGORY_DETAIL = 9;


}
