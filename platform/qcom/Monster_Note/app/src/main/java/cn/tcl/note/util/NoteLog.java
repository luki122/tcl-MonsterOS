/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.util;

import android.content.Context;
import android.util.Log;


public class NoteLog {
    private static String TAG = "CNNote";
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

    public static void getAppInfo(Context context) {
        i(TAG, "SDK Version=" + VersionUtils.getAndroidSDKLevel() +
                " app name=" + VersionUtils.getApplicationName(context) +
                " app version number=" + VersionUtils.getVersionNumber(context) +
                " app version code=" + VersionUtils.getVersionCode(context));
    }

    public static void printTrace(String tag) {
        d(tag, Log.getStackTraceString(new Throwable()));
    }
}
