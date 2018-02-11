package cn.tcl.weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import cn.tcl.weather.utils.LogUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-10-14.
 * Decide to start which activity
 */
public enum  ActivityFactory {
    TCL_ACTIVITY(WeatherCNApplication.SYSTEM_TYPE_LONDON, MainActivity.class, TclLocateActivity.class, TclWeatherManagerActivity.class, TclWeatherWarningActivity.class),
    OTHER_ACTIVITY(WeatherCNApplication.SYSTEM_TYPE_NULL, MainActivity.class, OtherLocateActivity.class, OtherWeatherManagerActivity.class, OtherWeatherWarningActivity.class);

    public static int MAIN_ACTIVITY = 0;
    public static int LOCATE_ACTIVITY = 1;
    public static int WEAHTER_MANAGER_ACTIVITY = 2;
    public static int WEATHER_WARNING_ACTIVITY = 3;

    private int mSystemType= WeatherCNApplication.getWeatherCnApplication().getCurrentSystemType();
    private final Class<?>[] mActivities;
    private static ActivityFactory mCurrentVhFactory;
    private static String TAG = "ActivityFactory";

    ActivityFactory(int systemType, Class<?>... cls) {
        mSystemType = systemType;
        mActivities = cls;
    }

    /**
     * Jump to the index of activityID
     * @param activityID To activity's ID
     * @param activity Now activity
     * @param bundle Intent bundle
     */
    public static void jumpToActivity(int activityID, Activity activity, Bundle bundle){
        int systemType = WeatherCNApplication.getWeatherCnApplication().getCurrentSystemType();

        // Determine the system activity factory
        if(mCurrentVhFactory == null) {
            for (ActivityFactory activityFactory : ActivityFactory.values()) {
                if(systemType == activityFactory.mSystemType){
                    mCurrentVhFactory = activityFactory;
                    break;
                }
            }
        }

        if(mCurrentVhFactory!=null && activityID < mCurrentVhFactory.mActivities.length){
            try{
                Class toActivity = mCurrentVhFactory.mActivities[activityID];
                Intent intent = new Intent(activity, toActivity);

                // If the intent have extra information
                if(bundle!=null)
                    intent.putExtras(bundle);
                activity.startActivity(intent);
            }catch (Exception e){
                LogUtils.e(TAG, "activity start failed", e);
            }
        }
    }
}
