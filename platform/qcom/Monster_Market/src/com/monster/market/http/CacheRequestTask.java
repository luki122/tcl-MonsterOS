package com.monster.market.http;

import android.database.sqlite.SQLiteFullException;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.monster.market.MarketApplication;
import com.monster.market.constants.HttpConstant;
import com.monster.market.db.DataCacheDao;
import com.monster.market.http.data.BaseHttpResultData;
import com.monster.market.utils.LogUtil;
import com.monster.market.utils.TimeUtil;

import java.io.StringReader;

/**
 * Created by xiaobin on 16-11-22.
 */
public class CacheRequestTask extends RequestTask {

    private static final String TAG = "CacheRequestTask";

    public CacheRequestTask(CacheRequest request) {
        super(request);
    }

    @Override
    protected Object doInBackground(Object... arg0) {
        LogUtil.i(TAG, "CacheRequestTask doInBackground");

        String result = "";
        CacheRequest tempRequest = ((CacheRequest) request);

        if (tempRequest.type != -1 && tempRequest.pageIndex == 0) {   // 只有第一页会去缓存
            DataCacheDao dao = new DataCacheDao(MarketApplication.getInstance());
            long cacheTime = dao.getCacheTime(tempRequest.type, tempRequest.subId, tempRequest.pageIndex);
            if (TimeUtil.isCacheEffectiveTime(cacheTime)) {
                result = dao.getCacheContent(tempRequest.type, tempRequest.subId, tempRequest.pageIndex);
                LogUtil.i(TAG, "read cache result: " + result);
            }

            if (TextUtils.isEmpty(result)) {
                try {
                    result = HttpRequestUtil.excute(request);
                    dao.deleteCache(tempRequest.type, tempRequest.subId, tempRequest.pageIndex);
                    if (isGoodJson(result)) {
                        dao.saveCache(tempRequest.type, tempRequest.subId, tempRequest.pageIndex,
                                System.currentTimeMillis(), result);
                        LogUtil.i(TAG, "save cache result: " + result);
                    }
                } catch (Exception e) {
                    dao.closeDatabase();
                    return e;
                }
            }
            dao.closeDatabase();
        } else {
            LogUtil.i(TAG, "no read cache");
            try {
                result = HttpRequestUtil.excute(request);
            } catch (Exception e) {
                return e;
            }
        }

        LogUtil.i(TAG, "return cache result: " + result);
        return result;
    }

    @Override
    protected void onPostExecute(Object result) {
        if (request.iHttpCallback != null) {
            if (result instanceof Exception) {// 失败
                if (result instanceof SQLiteFullException) {    // 低内存数据库报错
                    request.iHttpCallback.onErrorResponse(
                            new RequestError(RequestError.ERROR_OTHER, "SQLiteFullException!"));
                } else {
                    request.iHttpCallback.onErrorResponse((RequestError) result);
                }
            } else {// 成功
                request.iHttpCallback.onResponse(result.toString());
            }
        }
    }

    public boolean isGoodJson(String json) {
        if (TextUtils.isEmpty(json)) {
            return false;
        }
        try {
            Gson gson = new Gson();
            StringReader sr = new StringReader(json);
            BaseHttpResultData baseResult = gson.fromJson(sr, BaseHttpResultData.class);
            if (baseResult != null) {
                if (baseResult.getRetCode() == HttpConstant.RESULT_CODE_SUCCESS) {
                    return true;
                }
            }
        } catch (JsonParseException e) {
            return false;
        }
        return false;
    }

}
