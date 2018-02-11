package com.monster.cloud.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.google.gson.Gson;
import com.monster.cloud.http.data.BaseHttpRequstData;

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
            data.setModel(Build.MODEL);
            data.setSystemVersion(Build.VERSION.RELEASE);
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
