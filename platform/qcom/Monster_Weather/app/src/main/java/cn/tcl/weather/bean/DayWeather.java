/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.bean;

import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import cn.tcl.weather.utils.DateFormatUtils;
import cn.tcl.weather.utils.LogUtils;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * the weather of a day
 */
public class DayWeather extends JSONBean {
    private final static String TAG = "DayWeather";
    @JSONBeanField(name = "dayTemp")
    public String dayTemp;
    @JSONBeanField(name = "nightTemp")
    public String nightTemp;
    @JSONBeanField(name = "dayWeatherPhenomena")
    public String dayWeatherPhenomena;
    @JSONBeanField(name = "nightWeatherPhenomena")
    public String nightWeatherPhenomena;
    @JSONBeanField(name = "dayWindDirection")
    public String dayWindDirection;
    @JSONBeanField(name = "nightWindDirection")
    public String nightWindDirection;
    @JSONBeanField(name = "dayWindPower")
    public String dayWindPower;
    @JSONBeanField(name = "nightWindPower")
    public String nightWindPower;
    @JSONBeanField(name = "sunriseTime")
    public String sunriseTime;
    @JSONBeanField(name = "sunsetTime")
    public String sunsetTime;
    @JSONBeanField(name = "dateTime")
    public String date;


    public String getLastDate() {
        return getOffsetTime(-1);
    }

    public String getOffsetTime(int day) {
        return DateFormatUtils.caculateOffsetTime(date,"yyyyMMdd",-1,0,0);
    }
}
