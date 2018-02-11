package cn.tcl.weather.internet;

import android.text.TextUtils;

import cn.tcl.weather.R;
import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.utils.LogUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created on 16-9-1.
 */
public enum StatusWindPower {

    LIGHT_WIND(0, R.string.wind_light),
    WIND_3_GRADE(1, R.string.wind_3_grade),
    WIND_4_GRADE(2, R.string.wind_4_grade),
    WIND_5_GRADE(3, R.string.wind_5_grade),
    WIND_6_GRADE(4, R.string.wind_6_grade),
    WIND_7_GRADE(5, R.string.wind_7_grade),
    WIND_8_GRADE(6, R.string.wind_8_grade),
    WIND_9_GRADE(7, R.string.wind_9_grade),
    WIND_10_GRADE(8, R.string.wind_10_grade),
    WIND_11_GRADE(9, R.string.wind_11_grade);

    int windPowerNo;
    int windPowerStrID;

    StatusWindPower(int windPowerNo, int windPowerStrID) {
        this.windPowerNo = windPowerNo;
        this.windPowerStrID = windPowerStrID;
    }

    public static String getWindPowerStrByNo(String windPowerNo) {
        try {
            int tarWindPowerNo = Integer.parseInt(windPowerNo);
            if (!TextUtils.isEmpty(windPowerNo)) {
                for (StatusWindPower sw : StatusWindPower.values()) {
                    if (sw.windPowerNo == tarWindPowerNo) {
                        return WeatherCNApplication.getWeatherCnApplication().getResources().getString(sw.windPowerStrID);
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e("StatusWindPower", "getWindPowerStrByNo", e);
        }
        //if windPower no is empty , return "--"
        return "--";
    }
}
