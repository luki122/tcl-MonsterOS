package com.android.deskclock.net.tcl;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.android.deskclock.Util.SharePreferencesUtils;
import com.google.gson.Gson;

public class LegalHolidayTask extends AsyncTask<String, Void, Object> {

    private static final String LEGAL_HOLIDAY_URL = "http://10.129.32.146:8080/configcenter-web-api/holidaySchedule";

    private Context mContext;
    private UpdateCallBack mCallBack;

    public LegalHolidayTask(Context context,UpdateCallBack callBack) {
        mContext = context;
        mCallBack = callBack;
    }

    @Override
    protected Object doInBackground(String... params) {
        try {
            String result = HttpUtils.postJsonRequest(LEGAL_HOLIDAY_URL,
                    JsonUtil.buildJsonRequestParams(mContext, null), true);
            // Log.i(LegalHolidayUtils.TAG, "result = " + result);

            if (!TextUtils.isEmpty(result)) {
                Gson gson = new Gson();
                LegalHolidayResponse response = gson.fromJson(result, LegalHolidayResponse.class);
                if (response != null && response.getRetCode() == 0) {
                    return response.getBody();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            return e;
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        if (result != null && result instanceof LegalHolidayResponseBody) {
            // LegalHolidayUtils.initNetworkData(mContext,
            // (LegalHolidayResponseBody) result);
            SharePreferencesUtils.saveHolidayToLocal(mContext, (LegalHolidayResponseBody) result);
            SharePreferencesUtils.setUpdateHolidayLab(mContext);
            if(mCallBack !=null){
                mCallBack.update();
            }
        }
    }

}
