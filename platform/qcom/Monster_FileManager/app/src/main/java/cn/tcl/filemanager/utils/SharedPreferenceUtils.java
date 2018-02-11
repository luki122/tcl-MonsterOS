/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtils {

    private static final String NAME = "filemanager_sp";
    private static final String KEY_SHOW_HIDDEN = "key_show_hidden";
    private static final String FRIST_ENTER_SAFE = "frist_enter_safe";
    private static final String CURRENT_SAFE_NAME = "boxFolderName";
    private static final String CURRENT_SAFE_ROOT = "cardRootpath";
    public static final String PREF_VIEW_BY = "pref_view_by";
    private static final String FINGERPRING_LOCK = "fingerprintlock";
    private static final String TEMP_IMEI_VALUE = "temp_imei_value";


    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static void setShowHidden(Context context, boolean showHidden) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putBoolean(KEY_SHOW_HIDDEN, showHidden).commit();
    }

    public static void setFristEnterSafe(Context context, boolean showHidden) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putBoolean(FRIST_ENTER_SAFE, true).commit();
    }

    public static boolean isShowHidden(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_SHOW_HIDDEN, false);
    }

    public static boolean isFristEnterSafe(Context context) {
        return getSharedPreferences(context).getBoolean(FRIST_ENTER_SAFE, false);
    }

    public static void removeShowHiddenPref(Context context) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putBoolean(FRIST_ENTER_SAFE, true).commit();
    }

    public static void setCurrentSafeName(Context context, String currentSafeName) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(CURRENT_SAFE_NAME, currentSafeName).commit();
    }

    public static String getCurrentSafeName(Context context) {
        return getSharedPreferences(context).getString(CURRENT_SAFE_NAME, null);
    }

    public static void setCurrentSafeRoot(Context context, String currentSafeRoot) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(CURRENT_SAFE_ROOT, currentSafeRoot).commit();
    }

    public static String getCurrentSafeRoot(Context context) {
        return getSharedPreferences(context).getString(CURRENT_SAFE_ROOT, null);
    }
    public static void setCurrentViewMode(Context context, String currentSafeRoot) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putString(PREF_VIEW_BY, currentSafeRoot).commit();
    }

    public static String getCurrentViewMode(Context context) {
        return getSharedPreferences(context).getString(PREF_VIEW_BY, null);
    }

    public static void setFingerPrintLock(Context context, boolean isUnlock) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putBoolean(FINGERPRING_LOCK,isUnlock).commit();
    }

    public static boolean getFingerPrintLock(Context context) {
        return getSharedPreferences(context).getBoolean(FINGERPRING_LOCK,false);
    }

    public static void setTempImeiValue(Context context, long tempImei) {
        SharedPreferences sp = getSharedPreferences(context);
        sp.edit().putLong(TEMP_IMEI_VALUE, tempImei).commit();
    }

    public static long getTempImeiValue(Context context) {
        return getSharedPreferences(context).getLong(TEMP_IMEI_VALUE, 0);
    }
}
