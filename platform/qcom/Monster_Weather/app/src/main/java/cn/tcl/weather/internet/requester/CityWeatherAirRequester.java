package cn.tcl.weather.internet.requester;

import com.android.volley.RequestQueue;
import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import cn.tcl.weather.internet.IWeatherNetHolder;
import cn.tcl.weather.internet.ServerConstant;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created on 16-8-26.
 */
public class CityWeatherAirRequester extends BaseWeatherRequester<CityWeatherAirRequester.BeanAir>{
    /**
     * constructor to int request type and request queue
     *
     * @param requestQueue
     */
    public CityWeatherAirRequester(RequestQueue requestQueue, IWeatherNetHolder.IWeatherNetCallback cb) {
        super(requestQueue, ServerConstant.TYPE_AIR, cb);
    }

    public void request(String areaId) {
        resetRequest();
        addParam(ServerConstant.AREA_ID, areaId);
        request();
    }

    public static class BeanAir extends JSONBean {
        @JSONBeanField(name = "p")
        public BeanP p = new BeanP();
    }
}
