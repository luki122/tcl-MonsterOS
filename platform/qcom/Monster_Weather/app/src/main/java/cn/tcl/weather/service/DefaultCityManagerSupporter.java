/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.service;

import cn.tcl.weather.bean.City;
import cn.tcl.weather.internet.IWeatherNetHolder;
import cn.tcl.weather.notification.NotificationHelper;
import cn.tcl.weather.provider.DataParam;
import cn.tcl.weather.provider.FindCityListParam;
import cn.tcl.weather.provider.FindHotCityListParam;
import cn.tcl.weather.provider.IDataService;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-10.
 */
class DefaultCityManagerSupporter implements ICityManagerSupporter {

    private DefaultCityManager mCityManager;
    private IWeatherNetHolder mWeahterHolder;
    private IDataService mDataService;

    DefaultCityManagerSupporter(DefaultCityManager cityManager) {
        mCityManager = cityManager;
        mWeahterHolder = cityManager.getWeatherNetHolder();
        mDataService = cityManager.getDataService();
    }

    @Override
    public void requestHotCities(OnRequestCityListListener l) {
        new InnerRequestHotCities(l).request();
    }

    @Override
    public void requestCityListByName(String name, OnRequestCityListListener l) {
        new InnerRequestCityListItem(name, mCityManager.getCurrentLanguage(), l).request();
    }

    @Override
    public void requestRefreshCity(City city, OnRequestRefreshListener l) {
        new InnerRequestRefreshItem(city, l).request();
    }

    @Override
    public void init() {

    }

    @Override
    public void recycle() {
        mWeahterHolder = null;
    }

    @Override
    public void onTrimMemory(int level) {

    }


    class InnerRequestHotCities implements DataParam.OnRequestDataListener {
        private OnRequestCityListListener mListener;

        InnerRequestHotCities(OnRequestCityListListener l) {
            mListener = l;
        }

        void request() {
            FindHotCityListParam param = new FindHotCityListParam(mCityManager.getCurrentLanguage());
            param.setOnRequestDataListener(this);
            mDataService.requestData(param);
        }

        @Override
        public void onRequestDataCallback(DataParam param) {
            FindHotCityListParam fhlp = (FindHotCityListParam) param;
            mListener.onSucceed(fhlp.getCities());
        }

        @Override
        public void onRequestDataFailed(DataParam param, DataParam.RequestError error) {
            mListener.onFailed(error.type);
        }
    }

    /*class for request city list*/
    class InnerRequestCityListItem implements DataParam.OnRequestDataListener {
        private OnRequestCityListListener mListener;
        private String mName;
        private String mLanguage;

        InnerRequestCityListItem(String name, String lang, OnRequestCityListListener l) {
            mName = name;
            mLanguage = lang;
            mListener = l;
        }

        void request() {
            FindCityListParam param = new FindCityListParam(mName, mLanguage);
            param.setOnRequestDataListener(this);
            mDataService.requestData(param);
        }

        @Override
        public void onRequestDataCallback(DataParam param) {
            FindCityListParam fclp = (FindCityListParam) param;
            mListener.onSucceed(fclp.getCities());
        }

        @Override
        public void onRequestDataFailed(DataParam param, DataParam.RequestError error) {
            mListener.onFailed(error.type);
        }
    }

    class InnerRequestRefreshItem implements IWeatherNetHolder.IWeatherNetCallback {

        private City mCity;
        private OnRequestRefreshListener mListener;

        InnerRequestRefreshItem(City city, OnRequestRefreshListener l) {
            mCity = city;
            mListener = l;
        }

        void request() {
            if (null != mWeahterHolder) {
                mListener.onRefreshing(mCity, OnRequestRefreshListener.REFRESH_STATE_START);
                mListener.onRefreshing(mCity, OnRequestRefreshListener.REFRESH_STATE_REFRESHING);
                mWeahterHolder.requestCityByCity(mCity, mCity.getLanguage(), InnerRequestRefreshItem.this);
            } else {
                mListener.onRefreshing(mCity, OnRequestRefreshListener.REFRESH_STATE_RECYCLED);
            }
        }

        @Override
        public void onReceivedData(String action, Object obj) {
            City city = (City) obj;
            mCity.setCity(city);
            mCityManager.requestCitySucceed(mCity, true);
            mListener.onRefreshing(mCity, OnRequestRefreshListener.REFRESH_STATE_SUCCEED);
            mCityManager.onUpdateCityCallback(mCity, ICityManager.CityObserver.UPDATE_STATE_SUCCEED);

            NotificationHelper.createNotifycation(mCityManager.getContext(), mCity);
        }

        @Override
        public void onFailed(String action, int state) {
            mCityManager.requestCityFailed(mCity);
            mListener.onRefreshing(mCity, OnRequestRefreshListener.REFRESH_STATE_FAILED);
        }
    }
}
