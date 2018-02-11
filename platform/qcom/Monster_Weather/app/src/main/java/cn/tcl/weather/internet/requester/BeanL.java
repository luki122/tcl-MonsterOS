/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.internet.requester;

import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import cn.tcl.weather.bean.CityWeatherInfo;
import cn.tcl.weather.internet.StatusWeather;
import cn.tcl.weather.service.ICityManager;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-24.
 * $desc
 */
public class BeanL extends JSONBean {

    @JSONBeanField(name = "l1")
    public String temp;

    @JSONBeanField(name = "l2")
    public String humidity;

    @JSONBeanField(name = "l3")
    public int windGrade;

    @JSONBeanField(name = "l4")
    public int windDirectNo;

    @JSONBeanField(name = "l5")
    public int weatherNo;

    @JSONBeanField(name = "l6")
    public String precipitation;

    @JSONBeanField(name = "l7")
    public String observationtime;

    @JSONBeanField(name = "l9")
    public String visibilityMeter;

    @JSONBeanField(name = "l10")
    public String pressure;

    @JSONBeanField(name = "l11")
    public String windSpeed;

    @JSONBeanField(name = "l12")
    public String feelingTemp;

    public CityWeatherInfo getCityWeatherInfo(String lang) {
        CityWeatherInfo info = new CityWeatherInfo();
        if (ICityManager.LANGUAGE_CN.equals(lang)) {
            info.temperature = temp;
            info.humidity = humidity;
            info.windGrade = windGrade + "";
            info.windDirectionNo = windDirectNo + "";
            info.weatherNo = weatherNo + "";
            info.precipitation = precipitation;
            info.observationtime = System.currentTimeMillis() + "";
            info.visibilityMeter = visibilityMeter;
            info.pressure = pressure;
            info.windspeed = windSpeed;
            info.feelingTemp = feelingTemp;
            info.weatherConditionRemind = StatusWeather.getWeatherStatusTipByNo(weatherNo + "");
        } else {
            info.temperature = temp;
            info.humidity = humidity;
            info.windGrade = windGrade + "";
            info.windDirectionNo = windDirectNo + "";
            info.weatherNo = weatherNo + "";
            info.precipitation = precipitation;
            info.observationtime = observationtime;
            info.visibilityMeter = visibilityMeter;
            info.pressure = pressure;
            info.windspeed = windSpeed;
            info.feelingTemp = feelingTemp;
            info.weatherConditionRemind = StatusWeather.getWeatherStatusTipByNo(weatherNo + "");
        }
        return info;
    }

    @Override
    public String toString() {
        return "BeanL{" +
                "temp='" + temp + '\'' +
                ", humidity='" + humidity + '\'' +
                ", windGrade=" + windGrade +
                ", windDirectNo=" + windDirectNo +
                ", weatherNo=" + weatherNo +
                ", precipitation='" + precipitation + '\'' +
                ", releaseTime='" + observationtime + '\'' +
                ", visibilityMeter='" + visibilityMeter + '\'' +
                ", pressure='" + pressure + '\'' +
                ", windSpeed='" + windSpeed + '\'' +
                ", feelingTemp='" + feelingTemp + '\'' +
                '}';
    }
}
