/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.internet.requester;

import com.android.volley.RequestQueue;

import cn.tcl.weather.internet.IWeatherNetHolder;
import cn.tcl.weather.internet.ServerConstant;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-24.
 * $desc
 */
public class CityWeatherObserveRequester extends BaseWeatherRequester<ObserveBeanArray> {


    /**
     * constructor to int request type and request queue
     *
     * @param requestQueue
     * @param cb
     */
    public CityWeatherObserveRequester(RequestQueue requestQueue, IWeatherNetHolder.IWeatherNetCallback cb) {
        super(requestQueue, ServerConstant.TYPE_OBSERVE, cb);
    }

    public void request(String areaId) {
        resetRequest();
        addParam(ServerConstant.AREA_ID, areaId);
        request();
    }

}
