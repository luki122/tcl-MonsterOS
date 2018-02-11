package cn.tcl.weather.internet.requester;

import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import cn.tcl.weather.bean.HourWeather;
import cn.tcl.weather.internet.StatusWeather;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-24.
 * $desc
 */
public class BeanJ extends JSONBean {

    @JSONBeanField(name = "ja")
    public int weatherNo;
    @JSONBeanField(name = "jb")
    public int temp;
    @JSONBeanField(name = "jc")
    public String windDirect;
    @JSONBeanField(name = "jd")
    public String windSpeed;
    @JSONBeanField(name = "je")
    public String humidity;
    @JSONBeanField(name = "jf")
    public String releaseTime;

    public HourWeather getHourWeather(String lang) {
        HourWeather weather = new HourWeather();
        weather.temperature = temp;
        weather.time = releaseTime;
        weather.icon = StatusWeather.getWeather24HourIconByNo(weatherNo + "");
        return weather;
    }

}
