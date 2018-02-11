/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.internet.requester;

import android.text.TextUtils;

import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import java.util.Date;

import cn.tcl.weather.bean.DayWeather;
import cn.tcl.weather.internet.ServerConstant;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-24.
 * $desc
 */
public class BeanF extends JSONBean {

    public final static long DAY_TIME_MILLS = 24 * 60 * 60 * 1000;

    @JSONBeanField(name = "fa")
    public int dayWeatherNo;
    @JSONBeanField(name = "fb")
    public int nightWeatherNo;
    @JSONBeanField(name = "fc")
    public String dayTemp;
    @JSONBeanField(name = "fd")
    public String nightTemp;
    @JSONBeanField(name = "fe")
    public int dayWindDirectNo;
    @JSONBeanField(name = "ff")
    public int nightWindDirectNo;
    @JSONBeanField(name = "fg")
    public int dayWindNo;
    @JSONBeanField(name = "fh")
    public int nightWindNo;
    @JSONBeanField(name = "fi")
    public String sunriseSunsetTime;


    public DayWeather getDayWeather(String lang, Date date, int index) {
        Date cDate = new Date(date.getTime() + (DAY_TIME_MILLS * index));
        //Date cDate = new Date(date.getTime() + (DAY_TIME_MILLS * (index + 1)));
        DayWeather weather = new DayWeather();
        weather.dayWeatherPhenomena = dayWeatherNo + "";
        weather.nightWeatherPhenomena = nightWeatherNo + "";
        weather.dayTemp = dayTemp;
        weather.nightTemp = nightTemp;
        if (!TextUtils.isEmpty(sunriseSunsetTime)) {
            String[] times = sunriseSunsetTime.replaceAll(":", "").split("\\|");
            if (times.length > 1) {
                String time = ServerConstant.formateDate(cDate);
                time = time.substring(0, time.length() - 4);
                weather.sunriseTime = time + times[0].trim();
                weather.sunsetTime = time + times[1].trim();
            }
        }
        weather.date = ServerConstant.formateDateNoMinute(cDate);
        return weather;
    }

    @Override
    public String toString() {
        return "BeanF{" +
                "dayWeatherNo=" + dayWeatherNo +
                ", nightWeatherNo=" + nightWeatherNo +
                ", dayTemp='" + dayTemp + '\'' +
                ", nightTemp='" + nightTemp + '\'' +
                ", dayWindDirectNo=" + dayWindDirectNo +
                ", nightWindDirectNo=" + nightWindDirectNo +
                ", dayWindNo=" + dayWindNo +
                ", nightWindNo=" + nightWindNo +
                ", sunriseSunsetTime='" + sunriseSunsetTime + '\'' +
                '}';
    }
}
