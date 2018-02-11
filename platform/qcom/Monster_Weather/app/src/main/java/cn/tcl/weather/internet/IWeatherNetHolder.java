/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.internet;

import cn.tcl.weather.utils.IManager;
import cn.tcl.weather.bean.City;

/**
 * Created by thundersoft on 16-7-29.
 * <p/>
 */
public interface IWeatherNetHolder extends IManager {


    void requestCityByCity(City city, String lang, IWeatherNetCallback cb);

    /**
     * request city name list
     *
     * @param name
     * @param lang
     * @param cb
     * @return
     */
    void requestCityListByName(String name, String lang, IWeatherNetCallback cb);

    /**
     * request City infos by locationKey
     *
     * @param locationKey city loaction key
     * @param lang        language
     * @param cb          callback for this request
     */
    void requestCityBylocationKey(String locationKey, String lang, IWeatherNetCallback cb);


    /**
     * request city infos by lon and lat
     *
     * @param lon
     * @param lat
     * @param lang
     * @param cb
     */
    void requestCityByGeo(String lon, String lat, String lang, IWeatherNetCallback cb);


    /**
     * IWeatherCallback is a listener for {@link IWeatherNetHolder}
     */
    interface IWeatherNetCallback {
        String ACTION_REQUEST_CITY_BY_LOCATION_KEY = "requestCityByCityInfo";
        String ACTION_REQUEST_CITY_LIST_BY_NAME = "requestCityListByName";
        String ACTION_REQUEST_CITY_BY_GEO = "requestCityByGeo";
        String ACTION_REQUEST_CITY_LIST_BY_CITY = "requestCityByCity";

        /**
         * When City data received from Net, this will be call back
         *
         * @param action
         * @param obj
         */
        void onReceivedData(String action, Object obj);

        /**
         * When City data received failed from Net, this will be call back
         *
         * @param action
         * @param state
         */
        void onFailed(String action, int state);
    }
}
