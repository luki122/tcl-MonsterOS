/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.service;

import android.os.Handler;
import android.test.ActivityInstrumentationTestCase2;


import cn.tcl.weather.MainActivity;
import cn.tcl.weather.TestUtils;
import cn.tcl.weather.bean.City;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created  on 16-8-1.
 */
public class DefaultCityManagerTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private DefaultCityManager mDefaultUpdateService;
    private Handler mHandler;
    protected TestUtils.Lock lock = new TestUtils.Lock();

    public DefaultCityManagerTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        lock.lockWait();
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHandler = new Handler();
                    lock.lockNotify();
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        mDefaultUpdateService = new DefaultCityManager(getActivity(), mHandler);
        mDefaultUpdateService.init();
    }

    public void test_AddCity() {

        ICityManager.CityObserver co = new ICityManager.CityObserver() {
            @Override
            protected void onCityAdding(City city, int state) {
                if (state == ICityManager.CityObserver.ADD_STATE_ADDED || state == ICityManager.CityObserver.ADD_STATE_FAILED)
                    lock.lockNotify();
            }

            @Override
            protected void onCityUpdate(City city, int type) {
                int i = 0;
            }
        };
        City city = new City();
        mDefaultUpdateService.addCityObserver(co);
        mDefaultUpdateService.addCity(city);
        lock.lockWait(10000);
        assertNotNull("the city data is not null, but is " + city.getCityWeatherInfo().temperature, city.getCityWeatherInfo().temperature);
        mDefaultUpdateService.removeCityObserver(co);
    }


    public void test_CheckLanguage() {
        String lang = mDefaultUpdateService.checkCurrentLanguage();
        assertNotNull("the language is not null, but is " + lang, lang);
    }

    @Override
    protected void tearDown() throws Exception {
        mDefaultUpdateService.recycle();
        super.tearDown();
    }
}
