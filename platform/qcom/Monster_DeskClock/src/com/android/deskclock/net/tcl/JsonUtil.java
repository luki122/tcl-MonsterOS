package com.android.deskclock.net.tcl;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.google.gson.Gson;

/**
 * Created by xiaobin on 16-7-19.
 */
public class JsonUtil {

    private static BaseHttpRequestData data;

    public static int getVersionCode(Context context) {
        int versionCode = -1;
        try {
            versionCode = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            versionCode = 0;
        }
        return versionCode;
    }

    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            versionName = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            versionName = "v1.0.1.0202.1";
        }
        return versionName;
    }

    public static String getImei(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    public static BaseHttpRequestData buildBaseHttpRequstData(Context context) {
        if (data == null) {
            data = new BaseHttpRequestData();
            data.setVersionCode(getVersionCode(context));
            data.setVersionName(getVersionName(context));
            data.setDeviceId(getImei(context));
            data.setModel(Build.MODEL);
            data.setSystemVersion(Build.VERSION.RELEASE);
        }
        return data;
    }

    public static String buildJsonRequestParams(Context context, Object body) {
        Gson gson = new Gson();
        BaseHttpRequestData data = buildBaseHttpRequstData(context);
        if (body == null) {
            body = new Object();
        }
        data.setBody(body);
        String paramsStr = gson.toJson(data);
        return paramsStr;
    }
}
