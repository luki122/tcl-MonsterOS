package cn.com.xy.sms.sdk.ui.popu.widget;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.xy.sms.sdk.ui.popu.util.SmartSmsSdkUtil;

public class DuoquSourceAdapterDataSource extends AdapterDataSource {

    public static final String INDEX_KEY = "index";
    public static final String DISPLAY_KEY = "name";

    public DuoquSourceAdapterDataSource(DuoquSource source) {
        mDataSource = createDataSource(source);
    }

    private static JSONArray createDataSource(DuoquSource source) {
        if (source.getLength() == 0) {
            return null;
        }
        JSONArray sourceJson = new JSONArray();
        try {
            int length = source.getLength();
            for (int i = 0; i < length; i++) {
                JSONObject data = new JSONObject();
                data.put(INDEX_KEY, String.valueOf(i));
                data.put(DISPLAY_KEY, source.getValue(i));
                sourceJson.put(data);
            }
        } catch (Throwable e) {
            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquSourceAdapterDataSource createDataSource error:", e);
        }
        return sourceJson;
    }
}
