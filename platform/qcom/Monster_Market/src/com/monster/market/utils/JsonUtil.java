package com.monster.market.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemProperties;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.monster.market.http.data.BaseHttpRequstData;

import org.json.JSONObject;

/**
 * Created by xiaobin on 16-7-19.
 */
public class JsonUtil {

    private static BaseHttpRequstData data;

    public static BaseHttpRequstData buildBaseHttpRequstData(Context context) {
        if (data == null) {
            data = new BaseHttpRequstData();

            int versionCode = 0;
            String versionName = "";
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = null;
            try {
                pi = pm.getPackageInfo(context.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (pi != null) {
                versionName = pi.versionName;
                versionCode = pi.versionCode;
            }

            data.setVersion(String.valueOf(versionCode));
            data.setDeviceId(SystemUtil.getImei(context));
            data.setModel(android.os.Build.MODEL);
            String sysVer = SystemProperties.get("ro.tct.sys.ver");
            String sw_version;
            try {
                sw_version = sysVer.substring(1, 4) + sysVer.substring(6, 7)
                        + sysVer.substring(11, 12) +  sysVer.substring(11, 12) + sysVer.substring(6, 8);
            } catch (Exception e) {
                sw_version = sysVer;
            }

            data.setSystemVersion(sw_version);
            data.setVersionName(versionName);
        }
        return data;
    }

    public static String buildJsonRequestParams(Context context, Object body) {
        Gson gson = new Gson();
        BaseHttpRequstData data = buildBaseHttpRequstData(context);
        if (body == null) {
            body = new Object();
        }
        data.setBody(body);
        String paramsStr = gson.toJson(data);
        return paramsStr;
    }

}
