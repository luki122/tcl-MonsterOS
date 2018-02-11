package cn.tcl.weather.internet.requester;

import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import cn.tcl.weather.bean.CityWeatherInfo;
import cn.tcl.weather.service.ICityManager;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created on 16-8-26.
 */
public class BeanP extends JSONBean {
    @JSONBeanField(name = "p1")
    public String pmValue;
    @JSONBeanField(name = "p2")
    public String aqiValue;
    @JSONBeanField(name = "observationTime")
    public String observationTime;

    public CityWeatherInfo getCityAirInfo(String lang) {
        CityWeatherInfo info = new CityWeatherInfo();
        if (ICityManager.LANGUAGE_CN.equals(lang)) {
            info.pmValue = pmValue;
            info.aqiValue = aqiValue;
            info.observationtime = observationTime;
        } else {
            info.pmValue = pmValue;
            info.aqiValue = aqiValue;
            info.observationtime = observationTime;
        }
        return info;
    }

    @Override
    public String toString() {
        return "BeanP{" +
                "pmValue='" + pmValue + '\'' +
                ", aqiValue='" + aqiValue + '\'' +
                ", observationTime='" + observationTime + '\'' +
                '}';
    }
}
