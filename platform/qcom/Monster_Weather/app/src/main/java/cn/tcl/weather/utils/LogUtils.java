/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.weather.utils;

import android.util.Log;

import cn.tcl.weather.WeatherCNApplication;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-5.
 */
public class LogUtils {

    private static final String TIMER_TAG = "timer";

    private static final boolean DEBUG = true;
    private static int mVersionCode;
    private static String mVersionNumber;
    private static String mVersionInfo;

    public static void i(String tag, String logInfo) {
        if (DEBUG) {
            setVersionInfo();
            Log.i(tag, mVersionInfo + logInfo);
        }
    }

    public static void w(String tag, String logInfo) {
        if (DEBUG) {
            setVersionInfo();
            Log.w(tag, mVersionInfo + logInfo);
        }
    }

    public static void d(String tag, String logInfo) {
        if (DEBUG) {
            setVersionInfo();
            Log.d(tag, mVersionInfo + logInfo);
        }
    }

    public static void e(String tag, String logInfo, Exception e) {
        setVersionInfo();
        Log.e(tag, mVersionInfo + logInfo, e);
    }

    private static void setVersionInfo() {
        if (null == mVersionInfo || mVersionInfo.isEmpty()) {
            mVersionCode = VersionUtils.getVersionCode(WeatherCNApplication.getWeatherCnApplication().getBaseContext());
            mVersionNumber = VersionUtils.getVersionNumber(WeatherCNApplication.getWeatherCnApplication().getBaseContext());
            mVersionInfo = new StringBuilder()
                    .append("VersionCode :")
                    .append(mVersionCode)
                    .append(" | VersionNumber :")
                    .append(mVersionNumber)
                    .append(" | LogInfo :")
                    .toString();
        }
    }

    public static void timerStart() {
        if (CommonUtils.IS_DEBUG) {
            StringBuilder builder = new StringBuilder();
            StackTraceElement element = Thread.currentThread().getStackTrace()[3];

            builder.append("cls Name: ");
            builder.append(element.getClassName());
            builder.append(" method Name: ");
            builder.append(element.getMethodName());
            builder.append(" start ");
            builder.append(System.currentTimeMillis());

            d(TIMER_TAG, builder.toString());
        }
    }


    public static void timerEnd() {
        if (CommonUtils.IS_DEBUG) {
            long timeMills = System.currentTimeMillis();
            StringBuilder builder = new StringBuilder();
            StackTraceElement element = Thread.currentThread().getStackTrace()[3];

            builder.append("cls Name: ");
            builder.append(element.getClassName());
            builder.append(" method: ");
            builder.append(element.getMethodName());
            builder.append(" end ");
            builder.append(timeMills);

            d(TIMER_TAG, builder.toString());
        }
    }
}
