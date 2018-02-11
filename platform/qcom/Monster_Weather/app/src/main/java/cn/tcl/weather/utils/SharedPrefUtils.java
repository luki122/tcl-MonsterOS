/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.weather.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import cn.tcl.weather.WeatherCNApplication;

public class SharedPrefUtils {

    //99 ----> in enum of StatusWeather it represent unknown Weather
    public static final String NO_WEATHER_FLAG = "99";
    public static final String NOTIFICATION_SHAREPREF = "weatherCN_sharepref";
    private static SharedPrefUtils sInstance;
    private static SharedPreferences mSharedPreferences;
    private static Context context;

    public static SharedPrefUtils getInstance() {
        if (sInstance == null) {
            sInstance = new SharedPrefUtils();
        }
        if(context == null){
            context = WeatherCNApplication.getWeatherCnApplication().getBaseContext();
        }
        return sInstance;
    }

    /**
     * save weather
     *
     * @param areaId
     * @param timeMili
     * @param weatherNo
     */
    public void saveWeather(String areaId, String timeMili, String weatherNo) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(NOTIFICATION_SHAREPREF,
                    Activity.MODE_PRIVATE);
        }
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(areaId + "|" + timeMili, weatherNo);
        editor.commit();
    }

    /**
     * get the field of "id"
     *
     * @return
     */
    public String getWeather(String areaId, String timeMili) {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(NOTIFICATION_SHAREPREF,
                    Activity.MODE_PRIVATE);
        }
        return mSharedPreferences.getString(areaId + "|" + timeMili, NO_WEATHER_FLAG);
    }

    /**
     * clear ALL data
     */
    public static void clearAllData() {
        if (mSharedPreferences == null) {
            mSharedPreferences = context.getSharedPreferences(NOTIFICATION_SHAREPREF,
                    Activity.MODE_PRIVATE);
        }
        mSharedPreferences.edit().clear().commit();
    }

}
