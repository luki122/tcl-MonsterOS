package com.monster.market.http;

import android.content.Context;
import android.os.Build;

import com.monster.market.MarketApplication;
import com.monster.market.bean.AppDetailInfo;
import com.monster.market.constants.HttpConstant;
import com.monster.market.http.Request.RequestMethod;
import com.monster.market.http.data.AdListResultData;
import com.monster.market.http.data.AppDetailRecommendResultData;
import com.monster.market.http.data.AppDetailRequestData;
import com.monster.market.http.data.AppListResultData;
import com.monster.market.http.data.AppTypeListResultData;
import com.monster.market.http.data.AppTypeRequestData;
import com.monster.market.http.data.AppUpgradeInfoRequestData;
import com.monster.market.http.data.AppUpgradeListRequestData;
import com.monster.market.http.data.AppUpgradeListResultData;
import com.monster.market.http.data.BannerListResultData;
import com.monster.market.http.data.BasePageInfoData;
import com.monster.market.http.data.EssentialRequestData;
import com.monster.market.http.data.RankingRequestData;
import com.monster.market.http.data.ReportAdClickRequestData;
import com.monster.market.http.data.ReportDownloadInfoListRequestData;
import com.monster.market.http.data.ReportDownloadInfoRequestData;
import com.monster.market.http.data.ReportInstallRequestData;
import com.monster.market.http.data.SearchAppListResultData;
import com.monster.market.http.data.SearchAppRequestData;
import com.monster.market.http.data.SearchKeyListRequestData;
import com.monster.market.http.data.SearchKeyListResultData;
import com.monster.market.http.data.TopicDetailRequestData;
import com.monster.market.http.data.TopicDetailResultData;
import com.monster.market.http.data.TopicResultData;
import com.monster.market.utils.JsonUtil;
import com.monster.market.utils.SystemUtil;

import java.util.List;

public class RequestHelper {

    /**
     * 获取首页列表
     *
     * @param context
     * @param pageNum
     * @param pageSize
     * @param response
     * @return
     */
    public static Request getIndexInfo(final Context context, int pageNum, int pageSize, final DataResponse<AppListResultData> response) {
        BaseJsonParseCallback<AppListResultData> callback = new BaseJsonParseCallback<>(
                response, AppListResultData.class);
        CacheRequest request = new CacheRequest(HttpConstant.getHomePageUrl(),
                RequestMethod.POST, callback);
        request.type = HttpConstant.CACHE_TYPE_HOMEPAGE;
        request.pageIndex = pageNum;

        BasePageInfoData infoData = new BasePageInfoData();
        infoData.setPageNum(pageNum);
        infoData.setPageSize(pageSize);

        request.setParams(JsonUtil.buildJsonRequestParams(context, infoData));
        request.execute();
        return request;
    }

    /**
     * 获取Banner
     *
     * @param context
     * @param response
     * @return
     */
    public static Request getBanner(final Context context, final DataResponse<BannerListResultData> response) {
        BaseJsonParseCallback<BannerListResultData> callback = new BaseJsonParseCallback<>(
                response, BannerListResultData.class);
        CacheRequest request = new CacheRequest(HttpConstant.getBannerUrl(),
                RequestMethod.POST, callback);
        request.type = HttpConstant.CACHE_TYPE_BANNER;
        request.pageIndex = 0;

        request.setParams(JsonUtil.buildJsonRequestParams(context, null));
        request.execute();
        return request;
    }

    /**
     * 获取广告列表
     *
     * @param context
     * @param response
     * @return
     */
    public static Request getAdList(final Context context, int pageNum, int pageSize, final DataResponse<AdListResultData> response) {
        BaseJsonParseCallback<AdListResultData> callback = new BaseJsonParseCallback<>(
                response, AdListResultData.class);
        CacheRequest request = new CacheRequest(HttpConstant.getAdListUrl(),
                RequestMethod.POST, callback);
        request.type = HttpConstant.CACHE_TYPE_AD;
        request.pageIndex = pageNum;

        BasePageInfoData infoData = new BasePageInfoData();
        infoData.setPageNum(pageNum);
        infoData.setPageSize(pageSize);

        request.setParams(JsonUtil.buildJsonRequestParams(context, infoData));
        request.execute();
        return request;
    }

    /**
     * 获取搜索提示
     *
     * @param context
     * @param key
     * @param response
     * @return
     */
    public static Request getSearchKey(final Context context, String key, final DataResponse<SearchKeyListResultData> response) {
        BaseJsonParseCallback<SearchKeyListResultData> callback = new BaseJsonParseCallback<>(
                response, SearchKeyListResultData.class);
        Request request = new Request(HttpConstant.getSearchKeyListUrl(),
                RequestMethod.POST, callback);

        SearchKeyListRequestData requestData = new SearchKeyListRequestData();
        requestData.setKey(key);

        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();
        return request;
    }

    /**
     * 获取热门搜索
     *
     * @param context
     * @param response
     * @return
     */
    public static Request getSearchPopKey(final Context context, final DataResponse<SearchKeyListResultData> response) {
        BaseJsonParseCallback<SearchKeyListResultData> callback = new BaseJsonParseCallback<>(
                response, SearchKeyListResultData.class);
        Request request = new Request(HttpConstant.getSearchTopKeyUrl(),
                RequestMethod.POST, callback);
        request.setParams(JsonUtil.buildJsonRequestParams(context, null));
        request.execute();
        return request;
    }

    /**
     * 获取APP搜索列表
     *
     * @param context
     * @param pageNum
     * @param pageSize
     * @param key
     * @param response
     * @return
     */
    public static Request searchAppList(final Context context, int pageNum, int pageSize, String key, final DataResponse<SearchAppListResultData> response) {
        BaseJsonParseCallback<SearchAppListResultData> callback = new BaseJsonParseCallback<>(
                response, SearchAppListResultData.class);
        Request request = new Request(HttpConstant.getSearchAppListUrl(),
                RequestMethod.POST, callback);

        SearchAppRequestData requestData = new SearchAppRequestData();
        requestData.setPageNum(pageNum);
        requestData.setPageSize(pageSize);
        requestData.setKey(key);
        requestData.setIp(SystemUtil.getIp(context));
        requestData.setMacAddress(SystemUtil.getMacAddress(context));
        requestData.setApiLevel(SystemUtil.getApiLevel());

        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();
        return request;
    }

    /**
     * 获取APP详情
     *
     * @param context
     * @param packageName
     * @param response
     * @return
     */
    public static Request getAppDetail(final Context context, String packageName, final DataResponse<AppDetailInfo> response) {
        BaseJsonParseCallback<AppDetailInfo> callback = new BaseJsonParseCallback<>(
                response, AppDetailInfo.class);
        Request request = new Request(HttpConstant.getAppDetailUrl(),
                RequestMethod.POST, callback);

        AppDetailRequestData requestData = new AppDetailRequestData();
//        requestData.setAppId(appId);
        requestData.setPackageName(packageName);

        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();
        return request;
    }

    /**
     * 获取关联推荐
     * @param context
     * @param packageName
     * @param response
     * @return
     */
    public static Request getDetailsRecommend(final Context context, String packageName, final DataResponse<AppDetailRecommendResultData> response) {
        BaseJsonParseCallback<AppDetailRecommendResultData> callback = new BaseJsonParseCallback<>(
                response, AppDetailRecommendResultData.class);
        Request request = new Request(HttpConstant.getAppDetailsRecommendUrl(),
                RequestMethod.POST, callback);

        AppDetailRequestData requestData = new AppDetailRequestData();
        requestData.setPackageName(packageName);

        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();

        return request;
    }

    /**
     * 获取排行列表
     *
     * @param context
     * @param pageNum
     * @param pageSize
     * @param rankType
     * @param response
     * @return
     */
    public static Request getRankingAppList(final Context context, int pageNum, int pageSize, int rankType, final DataResponse<AppListResultData> response) {
        BaseJsonParseCallback<AppListResultData> callback = new BaseJsonParseCallback<>(
                response, AppListResultData.class);
        CacheRequest request = new CacheRequest(HttpConstant.getRankingAppListUrl(),
                RequestMethod.POST, callback);
        request.type = HttpConstant.CACHE_TYPE_RANKING;
        request.subId = rankType;
        request.pageIndex = pageNum;

        RankingRequestData requestData = new RankingRequestData();
        requestData.setPageNum(pageNum);
        requestData.setPageSize(pageSize);
        requestData.setRankType(rankType);

        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();
        return request;
    }

    /**
     * 获取应用分类
     *
     * @param context
     * @param pageNum
     * @param pageSize
     * @param appType
     * @param response
     * @return
     */
    public static Request getAppTypeList(final Context context, int pageNum, int pageSize, int appType, final DataResponse<AppTypeListResultData> response) {
        BaseJsonParseCallback<AppTypeListResultData> callback = new BaseJsonParseCallback<>(
                response, AppTypeListResultData.class);
        CacheRequest request = new CacheRequest(HttpConstant.getAppTypeUrl(),
                RequestMethod.POST, callback);
        request.type = HttpConstant.CACHE_TYPE_CATEGORY;
        request.subId = appType;
        request.pageIndex = pageNum;

        AppTypeRequestData requestData = new AppTypeRequestData();
        requestData.setPageNum(pageNum);
        requestData.setPageSize(pageSize);
        requestData.setAppType(String.valueOf(appType));

        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();
        return request;
    }

    /**
     * 获取应用分类详情
     *
     * @param context
     * @param pageNum
     * @param pageSize
     * @param appType
     * @param response
     * @return
     */
    public static Request getAppTypeInfoList(final Context context, int pageNum, int pageSize, int appType, final DataResponse<AppListResultData> response) {
        BaseJsonParseCallback<AppListResultData> callback = new BaseJsonParseCallback<>(
                response, AppListResultData.class);
        CacheRequest request = new CacheRequest(HttpConstant.getAppTypeInfoUrl(),
                RequestMethod.POST, callback);
        request.type = HttpConstant.CACHE_TYPE_CATEGORY_DETAIL;
        request.subId = appType;
        request.pageIndex = pageNum;

        AppTypeRequestData requestData = new AppTypeRequestData();
        requestData.setPageNum(pageNum);
        requestData.setPageSize(pageSize);
        requestData.setAppType(String.valueOf(appType));

        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();
        return request;
    }

    /**
     * 获取新品应用列表
     *
     * @param context
     * @param pageNum
     * @param pageSize
     * @param appType
     * @param response
     * @return
     */
    public static Request getNewAppList(final Context context, int pageNum, int pageSize, String appType, DataResponse<AppListResultData> response) {
        BaseJsonParseCallback<AppListResultData> callback = new BaseJsonParseCallback<>(
                response, AppListResultData.class);
        Request request = new Request(HttpConstant.getNewAppListUrl(),
                RequestMethod.POST, callback);

        AppTypeRequestData requestData = new AppTypeRequestData();
        requestData.setPageNum(pageNum);
        requestData.setPageSize(pageSize);
        requestData.setAppType(appType);

        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();
        return request;
    }

    /**
     * 获取应用更新列表
     *
     * @param context
     * @param infoList
     * @param response
     * @return
     */
    public static Request getAppUpdateList(final Context context, List<AppUpgradeInfoRequestData> infoList, DataResponse<AppUpgradeListResultData> response) {
        BaseJsonParseCallback<AppUpgradeListResultData> callback = new BaseJsonParseCallback<>(
                response, AppUpgradeListResultData.class);
        Request request = new Request(HttpConstant.getAppUpdateListUrl(),
                RequestMethod.POST, callback);

        AppUpgradeListRequestData requestData = new AppUpgradeListRequestData();
        requestData.setUpgradeList(infoList);
        requestData.setResolution(MarketApplication.getResolutionStr());
        requestData.setSdkVersion(Build.VERSION.SDK_INT);

        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();
        return request;
    }

    /**
     * 获取专题列表
     * @param context
     * @param response
     * @return
     */
    public static Request getTopicList(final Context context, int pageNum, int pageSize, DataResponse<TopicResultData> response) {
        BaseJsonParseCallback<TopicResultData> callback = new BaseJsonParseCallback<>(
                response, TopicResultData.class);

        BasePageInfoData requestData = new BasePageInfoData();
        requestData.setPageNum(pageNum);
        requestData.setPageSize(pageSize);

        Request request = new Request(HttpConstant.getTopicListUrl(),
                RequestMethod.POST, callback);
        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();
        return request;
    }

    /**
     * 获取专题详情
     * @param context
     * @param pageNum
     * @param pageSize
     * @param id
     * @param response
     * @return
     */
    public static Request getTopicDetail(final Context context, int pageNum, int pageSize, int id, DataResponse<TopicDetailResultData> response) {
        BaseJsonParseCallback<TopicDetailResultData> callback = new BaseJsonParseCallback<>(
                response, TopicDetailResultData.class);
        Request request = new Request(HttpConstant.getTopicDetailUrl(),
                RequestMethod.POST, callback);

        TopicDetailRequestData requestData = new TopicDetailRequestData();
        requestData.setPageNum(pageNum);
        requestData.setPageSize(pageSize);
        requestData.setId(id);

        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();
        return request;
    }

    /**
     * 获取设计奖应用列表
     * @param context
     * @param pageNum
     * @param pageSize
     * @param response
     * @return
     */
    public static Request getAward(final Context context, int pageNum, int pageSize, DataResponse<AppListResultData> response) {
        BaseJsonParseCallback<AppListResultData> callback = new BaseJsonParseCallback<>(
                response, AppListResultData.class);
        CacheRequest request = new CacheRequest(HttpConstant.getAwardUrl(),
                RequestMethod.POST, callback);
        request.type = HttpConstant.CACHE_TYPE_AWARD;
        request.pageIndex = pageNum;

        BasePageInfoData infoData = new BasePageInfoData();
        infoData.setPageNum(pageNum);
        infoData.setPageSize(pageSize);

        request.setParams(JsonUtil.buildJsonRequestParams(context, infoData));
        request.execute();
        return request;
    }

    /**
     * 获取必备列表
     * @param context
     * @param pageNum
     * @param pageSize
     * @param type
     * @param response
     * @return
     */
    public static Request getEssentialList(final Context context, int pageNum, int pageSize, int type, final DataResponse<AppListResultData> response) {
        BaseJsonParseCallback<AppListResultData> callback = new BaseJsonParseCallback<>(
                response, AppListResultData.class);
        CacheRequest request = new CacheRequest(HttpConstant.getEssentialUrl(),
                RequestMethod.POST, callback);
        request.type = HttpConstant.CACHE_TYPE_ESSENTIAL;
        request.subId = type;
        request.pageIndex = pageNum;

        EssentialRequestData requestData = new EssentialRequestData();
        requestData.setPageNum(pageNum);
        requestData.setPageSize(pageSize);
        requestData.setAppType(type);

        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();
        return request;
    }

    public static Request reportAdClick(final Context context, ReportAdClickRequestData requestData) {
        DataResponse<Object> response = new DataResponse<Object>() {
            @Override
            public void onResponse(Object value) {}

            @Override
            public void onErrorResponse(RequestError error) {}
        };
        BaseJsonParseCallback callback = new BaseJsonParseCallback(
                response, DataResponse.class);
        Request request = new Request(HttpConstant.getReportAdClickUrl(),
                RequestMethod.POST, callback);

        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();
        return request;
    }

    public static Request reportDownloadInfo(final Context context, ReportDownloadInfoRequestData requestData) {
        Request request = new Request(HttpConstant.getReportDownloadInfoUrl(),
                RequestMethod.POST, null);

        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();
        return request;
    }

    public static Request reportInstall(final Context context, ReportInstallRequestData requestData) {
        Request request = new Request(HttpConstant.getReportInstallUrl(),
                RequestMethod.POST, null);

        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();
        return request;
    }

    public static Request reportDownloadInfoList(final Context context, ReportDownloadInfoListRequestData requestData) {
        Request request = new Request(HttpConstant.getReportDownloadInfoListUrl(),
                RequestMethod.POST, null);

        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();
        return request;
    }

}
