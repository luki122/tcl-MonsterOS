/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.service;

import java.util.List;

import cn.tcl.weather.utils.IManager;
import cn.tcl.weather.bean.City;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-10.
 * Supporter for {@link ICityManager}
 */
public interface ICityManagerSupporter extends IManager {


    void  requestHotCities(OnRequestCityListListener l);


    /**
     * request city name list
     *
     * @param name
     * @param l
     * @return
     */
    void requestCityListByName(String name, OnRequestCityListListener l);


    /**
     * request refreash city
     *
     * @param city
     * @param l
     */
    void requestRefreshCity(City city, OnRequestRefreshListener l);


    /**
     * this is a listener for {@link ICityManagerSupporter#requestCityListByName(String, OnRequestCityListListener)}
     */
    interface OnRequestCityListListener {

        int FAILED_STATE_RECYCLED = 1003;

        /**
         * callback when request succeed
         *
         * @param cityList
         */
        void onSucceed(List<City> cityList);

        /**
         * callback when request failed
         *
         * @param state
         */
        void onFailed(int state);
    }


    /**
     * this is a listener for {@link ICityManagerSupporter#requestRefreshCity(City, OnRequestRefreshListener)}
     */
    interface OnRequestRefreshListener {

        int REFRESH_STATE_START = 1001;
        int REFRESH_STATE_REFRESHING = 1002;
        int REFRESH_STATE_SUCCEED = 1003;
        int REFRESH_STATE_FAILED = 1004;
        int REFRESH_STATE_RECYCLED = 1005;

        /**
         * call back when refreshing
         *
         * @param city
         * @param state
         */
        void onRefreshing(City city, int state);
    }

}
