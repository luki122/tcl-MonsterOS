package cn.tcl.weather.service;

import cn.tcl.weather.utils.IManager;
import cn.tcl.weather.utils.LogUtils;

/**
 * Created on 16-9-23.
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 */
public class DynamicIconSwitchService implements IManager {
    private final static String TAG = DynamicIconSwitchService.class.getName();
    private UpdateService mUpdateService;
    private ICityManager.CityObserver mCityObserver = new ICityManager.CityObserver() {


        @Override
        protected void onCityListChanged() {
            LogUtils.i(TAG, "DynamicIconSwitchService...onCityListChanged()...");
            mUpdateService.sendWeatherStateChangeBroadcast();
        }

    };


    public DynamicIconSwitchService(UpdateService updateService) {
        mUpdateService = updateService;
    }

    public void startDynamicIconSwitchService() {
        mUpdateService.addCityObserver(mCityObserver);
    }

    public void stopDynamicIconSwitchService() {
        mUpdateService.removeCityObserver(mCityObserver);
    }


    @Override
    public void init() {
        LogUtils.i(TAG, "DynamicIconSwitchService...init()...");
        startDynamicIconSwitchService();
    }

    @Override
    public void recycle() {
        stopDynamicIconSwitchService();
    }

    @Override
    public void onTrimMemory(int level) {

    }
}
