package cn.tcl.weather.service;

import android.location.Location;
import android.os.Message;
import android.os.Handler;
import java.util.HashSet;

import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.utils.IBoardcaster;
import cn.tcl.weather.utils.IManager;
import cn.tcl.weather.utils.ThreadHandler;
import cn.tcl.weather.bean.City;
import cn.tcl.weather.internet.BaiduLocationClient;
import cn.tcl.weather.internet.ILocationClient;
import cn.tcl.weather.internet.IWeatherNetHolder;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-30.
 * $desc
 */
class LocateService implements ILocator, IManager, ILocationClient.OnLocationClientListener, IWeatherNetHolder.IWeatherNetCallback {
    private final static int SCAN_SPAN = 30 * 60 * 1000;

    private ILocationClient mLocationClient;
    private ThreadHandler mThreadHandler;
    private IWeatherNetHolder mWeatherNetHodler;
    //    private CityStoreData mCityStoreData;
    private DefaultCityManager mCityManager;

    private HashSet<LocateObserver> mLocateObservers = new HashSet<>(8);
    private Handler mMainHandler;

    LocateService(UpdateService service, Handler mainHandler, DefaultCityManager cityManager) {
        mLocationClient = new BaiduLocationClient(service);
        mThreadHandler = cityManager.getThreadHandler();
        mWeatherNetHodler = cityManager.getWeatherNetHolder();
        mMainHandler = mainHandler;
//        mCityStoreData = cityManager.getCityStoreData();
        mCityManager = cityManager;
    }

    @Override
    public void init() {
        mLocationClient.init();
        WeatherCNApplication.getWeatherCnApplication().regiestOnReceiver(UpdateService.START_LOCATING, mStartLocatingReceiver);
        startLocating();
    }

    @Override
    public void recycle() {
        stopLocating();
        WeatherCNApplication.getWeatherCnApplication().unregiestOnReceiver(UpdateService.START_LOCATING, mStartLocatingReceiver);
        mLocationClient.recycle();
    }

    @Override
    public void onTrimMemory(int level) {
        mLocationClient.onTrimMemory(level);
    }

    @Override
    public void onLocated(Location location) {
        mLocationClient.unregiestOnLocationClientListener(this);
        mLocationClient.stopLocate();
        mWeatherNetHodler.requestCityByGeo(Double.toString(location.getLongitude()), Double.toString(location.getLatitude()), mCityManager.getCurrentLanguage(), this);
    }

    @Override
    public void onLocating(int state) {
        switch (state) {
            case ILocationClient.STATE_START:
                notifyObserverLocatingState(STATE_LOCATING);
                break;
            case ILocationClient.STATE_FAILED_LOCATE_FAILED:
                notifyObserverLocatingState(STATE_FAILED_LOCATE_FAILED);
                break;
            case ILocationClient.STATE_FAILED_NO_PERMISSION:
                notifyObserverLocatingState(STATE_FAILED_NO_PERMISSION);
                break;
            case ILocationClient.STATE_FAILED_NETWORK_USELESS:
                notifyObserverLocatingState(STATE_FAILED_USELESS_NETWORK);
                break;
        }
    }

    private void startLocating() {
        mThreadHandler.remove(mLocatingRunnable);
        mThreadHandler.post(mLocatingRunnable, 100);
    }

    private void stopLocating() {
        mThreadHandler.remove(mLocatingRunnable);
        mLocationClient.stopLocate();
    }


    private Runnable mLocatingRunnable = new Runnable() {
        @Override
        public void run() {
            notifyObserverLocatingState(ILocator.STATE_START);
            mLocationClient.regiestOnLocationClientListener(LocateService.this);
            mLocationClient.startLocate();
            mThreadHandler.post(this, SCAN_SPAN);
        }
    };

    @Override
    public void onReceivedData(String action, Object obj) {
        City city = (City) obj;
        city.setLocateCity(true);
        if (mCityManager.requestCitySucceed(city, true)) {
            mCityManager.onAddCityCallback(city, ICityManager.CityObserver.ADD_STATE_LOCATE_ADDED);
        } else {
            mCityManager.onUpdateCityCallback(city, ICityManager.CityObserver.UPDATE_STATE_SUCCEED);
        }
        notifyObserverLocatingState(STATE_SUCCEED);
    }

    private void notifyObserverLocatingState(final int state) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                for (LocateObserver observer : mLocateObservers) {
                    observer.onLocating(state);
                }
            }
        });
    }

    @Override
    public void onFailed(String action, int state) {
        notifyObserverLocatingState(STATE_REQUEST_FAILED);
    }

    @Override
    public void regiestLocateObserver(LocateObserver observer) {
        mLocateObservers.add(observer);
    }

    @Override
    public void unregiestLocateObserver(LocateObserver observer) {
        mLocateObservers.remove(observer);
    }



    private IBoardcaster.Receiver mStartLocatingReceiver = new IBoardcaster.Receiver() {
        @Override
        public void onReceived(String action, Message msg) {
            startLocating();
        }
    };
}
