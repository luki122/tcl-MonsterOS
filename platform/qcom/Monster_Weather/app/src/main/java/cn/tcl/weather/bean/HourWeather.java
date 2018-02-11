/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.bean;

import android.text.TextUtils;

import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.tcl.weather.utils.DateFormatUtils;
import cn.tcl.weather.utils.Debugger;
import cn.tcl.weather.utils.LogUtils;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * the weather of hour
 */
public class HourWeather extends JSONBean implements Cloneable {
    private final static String TAG = "HourWeather";
    @JSONBeanField(name = "icon")
    public int icon;
    @JSONBeanField(name = "time")
    public String time;
    @JSONBeanField(name = "temperature")
    public float temperature;

    @Override
    public HourWeather clone() {
        try {
            return (HourWeather) super.clone();
        } catch (CloneNotSupportedException e) {
            Debugger.DEBUG_D(true, TAG, e.toString());
        }
        return null;
    }


    public String getTimeHHSS() {
        return getTimeHHSS(time);
    }

    public String getOffsetTime(int hour) {
        return DateFormatUtils.caculateOffsetTime(time, 0, hour, 0);
    }


    public static String getTimeHHSS(String yyyymmddhhss) {
        if (!TextUtils.isEmpty(yyyymmddhhss)) {
            final int length = yyyymmddhhss.length();
            return yyyymmddhhss.substring(length - 4, length - 2) + ":" + yyyymmddhhss.substring(length - 2);
        }
        return "";
    }


    public String getTemperature() {
        return ((int) temperature) + "°";
    }
}
