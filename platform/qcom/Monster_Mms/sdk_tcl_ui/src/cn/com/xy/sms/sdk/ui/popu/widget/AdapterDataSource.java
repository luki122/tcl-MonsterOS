package cn.com.xy.sms.sdk.ui.popu.widget;

import org.json.JSONArray;
import org.json.JSONException;

import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;

public abstract class AdapterDataSource {

    private static final String DEFAULT_DISPLAY_KEY = "name";
    protected JSONArray mDataSource = null;

    public JSONArray getDataSrouce() {
        return mDataSource;
    }

    public String getDisplayValue(int index) {
        try {
            if(mDataSource == null || mDataSource.length() <= index){
                return "";
            }
            return mDataSource.getJSONObject(index).optString(DEFAULT_DISPLAY_KEY);
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("AdapterDataSource getDisplayValue error:", e);
        }
        return "";
    }
}
