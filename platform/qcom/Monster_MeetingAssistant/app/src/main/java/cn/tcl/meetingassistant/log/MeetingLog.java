/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.log;

import android.content.Context;
import android.util.Log;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-1.
 * The Log Tool to print log when developing
 */
public class MeetingLog {
    private static String TAG = "CN_MEETING";
    public static boolean DEBUG = true;

    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(TAG, tag + ":" + msg);
        }
    }

    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(TAG, tag + ":" + msg);
        }
    }

    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(TAG, tag + ":" + msg);
        }
    }

    public static void appInfo(Context context) {
        if (DEBUG) {
            String appName = VersionUtils.getApplicationName(context);
            String sdkVersion = VersionUtils.getVersionNumber(context);
            int sdkLevel = VersionUtils.getAndroidSDKLevel();
            int versionCode = VersionUtils.getVersionCode(context);
            String versionNumber = VersionUtils.getVersionNumber(context);
            Log.i(TAG, "Info        :");
            Log.i(TAG, "appName     :" + appName);
            Log.i(TAG, "sdkVersion  :" + sdkVersion);
            Log.i(TAG, "sdkLevel    :" + sdkLevel);
            Log.i(TAG, "versionCode :" + versionCode);
            Log.i(TAG, "versionNum  :" + versionNumber);
        }
    }

    public static void w(String tag, String msg) {
        Log.w(TAG, tag + ":" + msg);
    }

    public static void w(String tag, String msg, Exception e) {
        Log.w(TAG, tag + ":" + msg, e);
    }

    public static void e(String tag, String msg) {
        Log.e(TAG, tag + ":" + msg);
    }

    public static void e(String tag, String msg, Exception e) {
        Log.e(TAG, tag + ":" + msg, e);
    }
}
