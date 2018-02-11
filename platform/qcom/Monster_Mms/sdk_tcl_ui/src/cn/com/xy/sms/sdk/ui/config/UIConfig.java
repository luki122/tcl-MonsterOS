package cn.com.xy.sms.sdk.ui.config;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.com.xy.sms.sdk.log.LogManager;

/**
 * @author yangzhi 2016/5/18
 */
public class UIConfig {

    public static String UIVERSION = "201605101230";// ui版本号

    public static int SUPORT_STATE = 6;// 支持web菜单

    public static String getUIVersion() {
        return UIVERSION;
    }

    /**
     * 获取默认支持的菜单数据
     * 
     * @return
     */
    public static JSONArray getDefaultSuportMenuData() {
        try {
            JSONArray array = new JSONArray();
            JSONObject jobj = new JSONObject();
//            JSONObject param = new JSONObject();
            
            jobj.put("name", "刷新");
            jobj.put("web_menu_type", "WM_RELOAD");
            jobj.put("action_data", "7B7D");
            array.put(jobj);

//            param = new JSONObject();
            jobj = new JSONObject();
            jobj.put("name", "服务说明");
//          param.put("type", "WEB_ABOUT");
            jobj.put("action_data", "7B2274797065223A225745425F41424F5554227D");
            array.put(jobj);

            return array;
        } catch (Throwable t) {
            LogManager.e(
                    "xiaoyuan",
                    "OnlineUpdateCycleConfig getSuportMenuDataById："
                            + t.getMessage(), t);
        }
        return null;
    }

}
