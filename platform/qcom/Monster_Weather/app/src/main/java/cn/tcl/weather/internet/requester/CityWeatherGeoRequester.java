/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.internet.requester;

import com.android.volley.RequestQueue;
import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import cn.tcl.weather.internet.IWeatherNetHolder;
import cn.tcl.weather.internet.ServerConstant;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-24.
 * $desc
 */
public class CityWeatherGeoRequester extends BaseWeatherRequester<CityWeatherGeoRequester.Bean> {


    /**
     * constructor to int request type and request queue
     *
     * @param requestQueue
     * @param cb
     */
    public CityWeatherGeoRequester(RequestQueue requestQueue, IWeatherNetHolder.IWeatherNetCallback cb) {
        super(requestQueue, "", cb);
    }

    public void request(String lon, String lat) {
        resetRequest();
        addParam(ServerConstant.LON, lon);
        addParam(ServerConstant.LAT, lat);
        request();
    }

    @Override
    protected String getRootUrl() {
        return ServerConstant.SERVER_URL_GEO;
    }

    public static class Bean extends JSONBean {

        final static int STATAUS_SUCCEED = 0;
        @JSONBeanField(name = "status")
        public int status;

        @JSONBeanField(name = "msg")
        public String msg;

        @JSONBeanField(name = "geo")
        public GeoBean bean = new GeoBean();
    }


    public static class GeoBean extends JSONBean {
        @JSONBeanField(name = "id")
        public String areaId;
    }
}
