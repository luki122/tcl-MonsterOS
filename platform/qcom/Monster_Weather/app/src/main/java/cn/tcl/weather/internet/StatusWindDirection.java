package cn.tcl.weather.internet;

import android.text.TextUtils;

import cn.tcl.weather.R;
import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.utils.LogUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created on 16-9-1.
 */
public enum StatusWindDirection {
    wind_direction_no_wind(0, R.string.wind_direction_no_wind),
    wind_direction_northeast(1, R.string.wind_direction_northeast),
    wind_direction_east(2, R.string.wind_direction_east),
    wind_direction_southeast(3, R.string.wind_direction_southeast),
    wind_direction_south(4, R.string.wind_direction_south),
    wind_direction_southwest(5, R.string.wind_direction_southwest),
    wind_direction_west(6, R.string.wind_direction_west),
    wind_direction_northwest(7, R.string.wind_direction_northwest),
    wind_direction_north(8, R.string.wind_direction_north),
    wind_direction_whirl_wind(9, R.string.wind_direction_whirl_wind);

    int windDirectionNo;
    int windDirectionStrID;

    StatusWindDirection(int windDirectionNo, int windDirectionStrID) {
        this.windDirectionNo = windDirectionNo;
        this.windDirectionStrID = windDirectionStrID;
    }

    public static String getWindDirectionStrByNo(String windDirectionNo) {
        try {
            int tarWindDirectionNo = Integer.parseInt(windDirectionNo);
            if (!TextUtils.isEmpty(windDirectionNo)) {
                for (StatusWindDirection swd : StatusWindDirection.values()) {
                    if (swd.windDirectionNo == tarWindDirectionNo) {
                        return WeatherCNApplication.getWeatherCnApplication().getResources().getString(swd.windDirectionStrID);
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.e("StatusWindDirection", "getWindDirectionStrByNo", e);
        }
        //if wind direction no is empty , return "--"
        return "--";
    }
}
