package com.monster.paymentsecurity.util;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import com.monster.paymentsecurity.constant.Constant;
import com.monster.paymentsecurity.detection.AccessibilityUtils;

import java.util.List;

/**
 * Created by sandysheny on 16-11-29.
 */

public class SettingUtil {
    private static final String TAG = "sandysheny";

    /**
     * 安装监控是否开启
     *
     * @param cxt
     * @return
     */
    public static boolean isInstallDetectionEnable(Context cxt) {
        boolean enable;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cxt);
        enable = sp.getBoolean(Constant.SP_INSTALL_DETECTION, true);
        return enable;
    }

    /**
     * 设置安装监控是否开启
     *
     * @param cxt
     * @return
     */
    public static void setInstallDetectionEnable(Context cxt, boolean enable) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cxt);
        sp.edit().putBoolean(Constant.SP_INSTALL_DETECTION, enable).commit();
        Settings.Secure.putInt(cxt.getContentResolver(), Constant.SP_INSTALL_DETECTION, enable ? 1 : 0);
    }


    /**
     * 云端杀毒是否开启
     *
     * @param cxt
     * @return
     */
    public static boolean isScanCloudEnable(Context cxt) {
        boolean enable;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cxt);
        enable = sp.getBoolean(Constant.SP_SCAN_CLOUD, true);
        return enable;
    }


    /**
     * 自动更新病毒库是否开启
     *
     * @param cxt
     * @return
     */
    public static boolean isAutoUpdateVirusLib(Context cxt) {
        boolean enable;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cxt);
        enable = sp.getBoolean(Constant.SP_UPDATE_VIRUS_LIB, true);
        return enable;
    }


    /**
     * 支付应用检测是否开启
     *
     * @param cxt
     * @return
     */
    public static boolean isPayAppDetectionEnable(Context cxt) {
        boolean enable;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cxt);
        enable = sp.getBoolean(Constant.SP_PAY_APP_MONITOR, true);
        return enable;
    }

    /**
     * 设置支付应用检测是否开启
     *
     * @param cxt
     * @return
     */
    public static void setPayAppDetectionEnable(Context cxt, boolean enable) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cxt);
        sp.edit().putBoolean(Constant.SP_PAY_APP_MONITOR, enable).commit();
        if (enable) {
            AccessibilityUtils.setAccessibilityServiceEnabled(cxt);
        } else {
            ComponentName toggledService = ComponentName.unflattenFromString(Constant.ACCESSIBILITY_NAME);
            AccessibilityUtils.setAccessibilityServiceState(cxt, toggledService, enable);
        }
        enabled(cxt);
    }


    private static boolean enabled(Context cxt) {
        AccessibilityManager am = (AccessibilityManager) cxt.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceInfos = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        List<AccessibilityServiceInfo> installedAccessibilityServiceList = am.getInstalledAccessibilityServiceList();
        for (AccessibilityServiceInfo info : installedAccessibilityServiceList) {
            Log.d(TAG, "all installed AccessibilityService-->" + info.getId());
        }

        for (AccessibilityServiceInfo info : serviceInfos) {
            Log.d(TAG, "all enabled AccessibilityService-->" + info.getId());
        }
        return false;
    }


    /**
     * 保存系统更新版本
     *
     * @param context
     * @param newestVersion
     */
    public static void saveSystemVersion(Context context, String newestVersion) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().
                putString(Constant.SP_FRAMEWORK_VERSION, newestVersion)
                .apply();
    }

    /**
     * 获取系统版本
     *
     * @param con
     * @return
     */
    public static String getSystemVersion(Context con) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(con);
        return sp.getString(Constant.SP_FRAMEWORK_VERSION, "");
    }


    /**
     * 应用是否初次运行
     *
     * @param cxt
     * @return
     */
    public static boolean isAppFirstRun(Context cxt) {
        boolean b;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cxt);
        b = sp.getBoolean(Constant.SP_FIRST_RUN, true);
        return b;
    }

    /**
     * 设置支付应用是否初次运行
     *
     * @param cxt
     * @return
     */
    public static void setAppFirstRun(Context cxt) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(cxt);
        sp.edit().putBoolean(Constant.SP_FIRST_RUN, false).apply();
    }

    /**
     * 设置病毒库更新时间
     *
     * @param context
     * @param updateTime 2016-1-1
     */
    public static void setVirusLibUpdateTime(Context context, String updateTime) {
        if (context == null) return;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(Constant.SP_VIRUS_LIB_UPDATE_TIME, updateTime).apply();
    }

    /**
     * 病毒库更新时间
     *
     * @param context
     * @return
     */
    public static String getVirusLibUpdateTime(Context context) {
        if (context == null) return "";
        return PreferenceManager.getDefaultSharedPreferences(context).getString(Constant.SP_VIRUS_LIB_UPDATE_TIME, "");
    }
}
