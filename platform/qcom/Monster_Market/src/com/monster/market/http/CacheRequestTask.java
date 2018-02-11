package com.monster.market.http;

import android.text.TextUtils;

import com.monster.market.MarketApplication;
import com.monster.market.db.DataCacheDao;
import com.monster.market.utils.LogUtil;
import com.monster.market.utils.TimeUtil;

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
        String result = "";
        CacheRequest tempRequest = ((CacheRequest) request);

        if (tempRequest.type != -1 && tempRequest.pageIndex == 0) {   // 只有第一页会去缓存
            DataCacheDao dao = new DataCacheDao(MarketApplication.getInstance());
            long cacheTime = dao.getCacheTime(tempRequest.type, tempRequest.subId, tempRequest.pageIndex);
            String cacheTimeStr = TimeUtil.getStringDateShort(cacheTime);
            if (cacheTimeStr.equals(TimeUtil.getStringDateShort())) {
                result = dao.getCacheContent(tempRequest.type, tempRequest.subId, tempRequest.pageIndex);
            }

            if (TextUtils.isEmpty(result)) {
                try {
                    result = HttpRequestUtil.excute(request);
                    dao.deleteCache(tempRequest.type, tempRequest.subId, tempRequest.pageIndex);
                    dao.saveCache(tempRequest.type, tempRequest.subId, tempRequest.pageIndex,
                            System.currentTimeMillis(), result);
                } catch (Exception e) {
                    dao.closeDatabase();
                    return e;
                }
            }
            dao.closeDatabase();
        } else {
            try {
                result = HttpRequestUtil.excute(request);
            } catch (Exception e) {
                return e;
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(Object result) {
        if (request.iHttpCallback != null) {
            if (result instanceof Exception) {// 失败
                request.iHttpCallback.onErrorResponse((RequestError) result);
            } else {// 成功
                request.iHttpCallback.onResponse(result.toString());
            }
        }
    }

}
