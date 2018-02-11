/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.text.DecimalFormat;
import java.util.Locale;

import cn.tcl.weather.R;

public class CommonUtils {

    private final static String TAG = "CommonUtils";

    // if open debug model
    public static final boolean IS_DEBUG = false;

    private static final String LOG_TAG = "CommonUtils";
    public static final String TAG_BING = "DebugByBing";
    //Defect 212555 Outdoor auto location is failed by bing.wang.hz
    public static final String FIRST_LOCATION_TRY = "is_first_location_try";

    public static final int LOCATION_TIMER_DURATION = 60 * 60 * 1000;
    public static final String LOCATION_TIMER_TASK_ACTION = "com.tct.action.LOCATION_TIMER_TASK_ACTION";
    public static final String AUTO_LOCATION_TASK_ACTION = "com.tct.weather.START_AUTO_LOCATION_TASK_ACTION";

    public static boolean isSupportHorizontal(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenHeight = 0;
        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            screenHeight = dm.heightPixels;
        } else if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            screenHeight = dm.widthPixels;
        }

        if (screenHeight <= 480 && getScreenInch(activity) <= 4.5) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isPad(Activity activity) {
        double screenInches = getScreenInch(activity);
        // bigger than 6 inch --->Pad
        if (screenInches >= 6.0) {
            return true;
        }
        return false;
    }

    /**
     * if day temp is bigger than night temp , put the day temp to the front of the tempString
     * if day temp or night temp equals empty string , replace it with "--"
     *
     * @param nightTemp night temp string
     * @param dayTemp   day temp string
     * @return
     */
    public static String getTempString(String nightTemp, String dayTemp) {
        StringBuilder targetString = new StringBuilder();
        if (TextUtils.isEmpty(nightTemp) || TextUtils.isEmpty(dayTemp)) {
            if (TextUtils.isEmpty(nightTemp)) {
                nightTemp = "--";
            } else {
                nightTemp = nightTemp + "°";
            }
            if (TextUtils.isEmpty(dayTemp)) {
                dayTemp = "--";
            } else {
                dayTemp = dayTemp + "°";
            }
            targetString.append(nightTemp).append("/").append(dayTemp);
            return targetString.toString();
        } else {
            float fNightTemp, fDayTemp;
            try {
                fNightTemp = Float.parseFloat(nightTemp);
                fDayTemp = Float.parseFloat(dayTemp);
            } catch (Exception e) {
                fNightTemp = fDayTemp = 0;
                LogUtils.e(TAG, " getTempString", e);
            }

            nightTemp = nightTemp + "°";
            dayTemp = dayTemp + "°";

            int nightTempValue = Math.round(fNightTemp);
            int dayTempValue = Math.round(fDayTemp);
            boolean flag = dayTempValue > nightTempValue;
            if (flag) {
                targetString.append(nightTemp).append("/").append(dayTemp);
            } else {
                targetString.append(dayTemp).append("/").append(nightTemp);
            }
        }
        return targetString.toString();
    }

    public static double getScreenInch(Activity activity) {
//		WindowManager wm = (WindowManager)activity.getSystemService(Context.WINDOW_SERVICE);
//		Display display = wm.getDefaultDisplay();
//		// Screen width
//		float screenWidth = display.getWidth();
//		// Screen height
//		float screenHeight = display.getHeight();
//		DisplayMetrics dm = new DisplayMetrics();
//		display.getMetrics(dm);
//		double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
//		double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
//		//Screen size
//		double screenInches = Math.sqrt(x + y);
//		return screenInches;
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2) + Math.pow(dm.heightPixels, 2));
        int densityDpi = dm.densityDpi;
        double screenInches = diagonalPixels / densityDpi;
        return screenInches;
    }

    public static String getHourOfTime(Context context, boolean is24Hour, String time) {
        if (is24Hour) {
            if (time.equals("12 PM")) {
                time = "12:00";
            } else if (time.equals("12 AM")) {
                time = "0:00";
            } else if (time.contains("PM")) {
                int num = Integer.parseInt(time.substring(0, time.indexOf(" PM")));
                if (num > 0 && num < 12) {
                    time = (num + 12) + ":00";
                }
            } else if (time.contains("AM")) {
                int num = Integer.parseInt(time.substring(0, time.indexOf(" AM")));
                if (num > 0 && num < 12) {
                    time = num + ":00";
                }
            }
        } else {
            if (time.contains("PM")) {
                time = time.replace("PM", context.getResources().getString(R.string.date_pm));
            } else if (time.contains("AM")) {
                time = time.replace("AM", context.getResources().getString(R.string.date_am));
            }
        }
        return time;
    }

    public static String f2c(String fahrenheit) {
        if (!TextUtils.isEmpty(fahrenheit)) {
            try {
                int celsius = (int) ((Float.parseFloat(fahrenheit) - 32) * 5 / 9);
                return celsius + "";
            } catch (Exception e) {
                LogUtils.e(TAG, " f2c", e);
            }
        }
        return "";
    }

    //[GAPP][Android6.0][Weather]Please remove decimals from all
    public static String c2f(String celsius) {
        if (!TextUtils.isEmpty(celsius)) {
            try {
                int fahrenheit = (int) (Float.parseFloat(celsius) * 9 / 5 + 32);
                return fahrenheit + "";
            } catch (Exception e) {
                LogUtils.e(TAG, " f2c", e);
            }
        }
        return "";
    }

    public static String km2mi(String km) {
        try {
            double mi = Float.parseFloat(km) * 0.6214;
            return String.valueOf((int) mi);
        } catch (Exception e) {
            Log.i(LOG_TAG, "km2mi exception : wind = " + km);
            return "0";
        }
    }

    public static String deletaDec(String de) {
        try {
            float tem = Float.parseFloat(de);
            return String.valueOf((int) tem);
        } catch (Exception e) {
            Log.i(LOG_TAG, "deletaDec exception : wind = " + de);
            return "0";
        }
    }

    public static String km2m(String km) {
        try {
            DecimalFormat fnum = (DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH);
            fnum.applyPattern("##0.0");
            return fnum.format(Float.parseFloat(km) * 0.2778);
        } catch (Exception e) {
            Log.e(LOG_TAG, "km2m exception : wind = " + km);
            return "0";
        }
    }

    public static boolean isQcomPlatform() {
        try {
            Class<?> managerClass = Class
                    .forName("qcom.fmradio.FmConfig");
            if (managerClass.getClass() != null) {
                return true;
            }
        } catch (ClassNotFoundException e) {
            Log.i(LOG_TAG, "Can't find the class 'qcom.fmradio.FmConfig',maybe it's not Qcom platform.");
        } catch (LinkageError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Set statebar's color
    public static void setStateBarColor(Activity activity, int id){
        try{
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
                Window window = activity.getWindow();
                WindowManager.LayoutParams winParams = window.getAttributes();
                winParams.flags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
                window.setStatusBarColor(activity.getResources().getColor(id));
            }
        }catch (Exception e){
            LogUtils.e("", "setting state color is wrong", e);
        }
    }
}
