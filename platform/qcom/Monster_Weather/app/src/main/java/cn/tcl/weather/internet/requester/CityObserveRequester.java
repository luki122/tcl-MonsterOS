/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.internet.requester;

import com.android.volley.RequestQueue;
import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import java.util.ArrayList;

import cn.tcl.weather.internet.IWeatherNetHolder;
import cn.tcl.weather.internet.ServerConstant;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-24.
 * $desc
 */
public class CityObserveRequester extends BaseWeatherRequester<ObserveBeanArray> {


    /**
     * constructor to int request type and request queue
     *
     * @param requestQueue
     * @param cb
     */
    public CityObserveRequester(RequestQueue requestQueue, IWeatherNetHolder.IWeatherNetCallback cb) {
        super(requestQueue, ServerConstant.TYPE_OBSERVE, cb);
    }

    public void request(String cityName) {
        resetRequest();
        addParam(ServerConstant.CITY, cityName);
        request();
    }

    @Override
    protected String getRootUrl() {
        return ServerConstant.SERVER_URL_SEARCH;
    }

}
