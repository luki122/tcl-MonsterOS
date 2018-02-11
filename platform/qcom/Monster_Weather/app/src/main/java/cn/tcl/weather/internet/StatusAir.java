package cn.tcl.weather.internet;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import cn.tcl.weather.R;
import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.utils.LogUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created on 16-8-31.
 */
public enum StatusAir {

    AIR_STATUS_FINE(R.string.air_fine, R.drawable.tcl_air_fine, 0, 50),
    AIR_STATUS_GOOD(R.string.air_good, R.drawable.tcl_air_good, 51, 100),
    AIR_STATUS_COMMON(R.string.air_common, R.drawable.tcl_air_common, 101, 150),
    AIR_STATUS_SUBSTANDARD(R.string.air_substandard, R.drawable.tcl_air_substandard, 151, 200),
    AIR_STATUS_TURBIDITY(R.string.air_turbidity, R.drawable.tcl_air_turbidity, 201, 300),
    AIR_STATUS_BAD(R.string.air_bad, R.drawable.tcl_air_verybad, 300, 100000);


    int airStrId;
    int airDrawalbleId;
    int maxAQI;
    int minAQI;

    StatusAir(int airStrId, int airDrawalbleId, int minAQI, int maxAQI) {
        this.airStrId = airStrId;
        this.airDrawalbleId = airDrawalbleId;
        this.minAQI = minAQI;
        this.maxAQI = maxAQI;
    }

    public static String getAirStrByAQIValue(String AQIValue) {
        try {
            if (!TextUtils.isEmpty(AQIValue)) {
                int intAQIValue = Integer.parseInt(AQIValue);
                for (StatusAir as : StatusAir.values()) {
                    if (intAQIValue >= as.minAQI && intAQIValue <= as.maxAQI) {
                        return WeatherCNApplication.getWeatherCnApplication().getResources().getString(as.airStrId);
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e("StatusAir", "getAirStrByAQIValue", e);
        }
        //if weather AQIValue is empty or is not a digit, return "--"
        return "";
    }

    public static Drawable getAirDrawableByAQIValue(String AQIValue) {
        try {
            if (!TextUtils.isEmpty(AQIValue)) {
                int intAQIValue = Integer.parseInt(AQIValue);
                for (StatusAir as : StatusAir.values()) {
                    if (intAQIValue >= as.minAQI && intAQIValue <= as.maxAQI) {
                        return WeatherCNApplication.getWeatherCnApplication().getResources().getDrawable(as.airDrawalbleId);
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e("StatusAir", "getAirDrawableByAQIValue", e);
        }
        //if weather AQIValue is empty or is not a digit, return null
        return null;
    }


}
