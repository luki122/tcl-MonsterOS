package cn.tcl.weather.internet.requester;

import com.android.volley.RequestQueue;
import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import java.util.ArrayList;

import cn.tcl.weather.bean.HourWeather;
import cn.tcl.weather.internet.IWeatherNetHolder;
import cn.tcl.weather.internet.ServerConstant;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-24.
 * $desc
 */
public class CityWeatherHourRequester extends BaseWeatherRequester<CityWeatherHourRequester.BeanHour> {

    /**
     * constructor to int request type and request queue
     *
     * @param requestQueue
     */
    public CityWeatherHourRequester(RequestQueue requestQueue, IWeatherNetHolder.IWeatherNetCallback cb) {
        super(requestQueue, ServerConstant.TYPE_HOUR_FC, cb);
    }

    public void request(String areaId) {
        resetRequest();
        addParam(ServerConstant.AREA_ID, areaId);
        request();
    }

    public static class BeanHour extends JSONBean {
        @JSONBeanField(name = "jh")
        public ArrayList<BeanJ> hourWeathers = new ArrayList<>(24);

        public ArrayList<HourWeather> get24HourWeahters(String lang) {
            ArrayList<HourWeather> hourWeathers = new ArrayList<>(24);
            for (BeanJ j : this.hourWeathers) {
                hourWeathers.add(j.getHourWeather(lang));
            }
            return hourWeathers;
        }
    }
}
