/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.setupwizard.utils;

import android.content.Context;
import android.util.Log;

public final class LogUtils {

    public static final boolean DEBUG = true;

    /**
     * The method prints the log, level error
     *
     * @param msg the message to print
     */
    public static void e(String tag, String msg) {
        if (DEBUG) {
            Log.e(tag, msg);
        }
    }

    /**
     * The method prints the log, level error
     *
     * @param msg   the message to print
     * @param t   throw an exception to log
     */
    public static void e(String tag, String msg, Throwable t) {
        if (DEBUG) {
            Log.e(tag, msg, t);
        }
    }

    /**
     * The method prints the log, level warning
     *
     * @param msg the message to print
     */
    public static void w(String tag, String msg) {
        if (DEBUG) {
            Log.w(tag, msg);
        }
    }

    /**
     * The method prints the log, level warning
     *
     * @param msg   the message to print
     * @param t   throw an exception to log
     */
    public static void w(String tag, String msg, Throwable t) {
        if (DEBUG) {
            Log.w(tag, msg, t);
        }
    }

    /**
     * The method prints the log, level debug
     *
     * @param msg the message to print
     */
    public static void i(String tag, String msg) {
        if (DEBUG) {
            Log.i(tag, msg);
        }
    }

    /**
     * The method prints the log, level debug
     *
     * @param msg   the message to print
     * @param t   throw an exception to log
     */
    public static void i(String tag, String msg, Throwable t) {
        if (DEBUG) {
            Log.i(tag, msg, t);
        }
    }

    /**
     * The method prints the log, level debug
     *
     * @param msg the message to print
     */
    public static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(tag, msg);
        }
    }

    /**
     * The method prints the log, level debug
     *
     * @param msg   the message to print
     * @param t   throw an exception to log
     */
    public static void d(String tag, String msg, Throwable t) {
        if (DEBUG) {
            Log.d(tag, msg, t);
        }
    }

    /**
     * The method prints the log, level debug
     *
     * @param msg the message to print
     */
    public static void v(String tag, String msg) {
        if (DEBUG) {
            Log.v(tag, msg);
        }
    }

    /**
     * The method prints the log, level debug
     *
     * @param msg   the message to print
     * @param t   throw an exception to log
     */
    public static void v(String tag, String msg, Throwable t) {
        if (DEBUG) {
            Log.v(tag, msg, t);
        }
    }

    /**
     * The method prints the log, version info
     *
     * @param context
     */
    public static void getAppInfo(Context context) {
        i("LogUtils", "SDK Version=" + VersionUtils.getAndroidSDKLevel() +
                " app name=" + VersionUtils.getApplicationName(context) +
                " app version number=" + VersionUtils.getVersionNumber(context) +
                " app version code=" + VersionUtils.getVersionCode(context));
    }
}
