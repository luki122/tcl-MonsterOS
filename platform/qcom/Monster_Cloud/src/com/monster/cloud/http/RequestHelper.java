package com.monster.cloud.http;

import android.content.Context;

import com.monster.cloud.constants.HttpConstant;
import com.monster.cloud.http.data.CloudAppRecoveryAppInfoRequestData;
import com.monster.cloud.http.data.CloudAppRecoveryRequestData;
import com.monster.cloud.http.data.CloudAppRecoveryResultData;
import com.monster.cloud.utils.JsonUtil;

import java.util.List;

public class RequestHelper {

    /**
     * 获取首页列表
     *
     * @param context
     * @param response
     * @return
     */
    public static Request getCloudAppRecoveryApp(final Context context, List<CloudAppRecoveryAppInfoRequestData> infoList,
                                                 final DataResponse<CloudAppRecoveryResultData> response) {
        BaseJsonParseCallback<CloudAppRecoveryResultData> callback = new BaseJsonParseCallback<>(
                response, CloudAppRecoveryResultData.class);
        Request request = new Request(HttpConstant.getCloudAppRecoveryUrl(),
                Request.RequestMethod.POST, callback);

        CloudAppRecoveryRequestData requestData = new CloudAppRecoveryRequestData();
        requestData.setAppList(infoList);

        request.setParams(JsonUtil.buildJsonRequestParams(context, requestData));
        request.execute();
        return request;
    }

}
