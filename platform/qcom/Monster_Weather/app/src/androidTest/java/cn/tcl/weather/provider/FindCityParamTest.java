/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.provider;

import java.sql.SQLException;

import cn.tcl.weather.bean.City;
import cn.tcl.weather.bean.CityWeatherInfo;

/**
 * Created by thundersoft on 16-7-28.
 */
public class FindCityParamTest extends BaseTestCase {

    private final String locationKey = "lsjdlfjslfldfl";
    private final String language = "en";
    private City mCityModel = new City();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mCityModel.latitude = "120.0";
        mCityModel.longitude = "130.0";
        mCityModel.state = "ok";
        mCityModel.setLanguage(language);

        CityWeatherInfo info = new CityWeatherInfo();
        info.pressure = "2034";
        info.observationtime = "10:30";
        info.aqiValue = "good";

        mCityModel.setCityWeatherInfo(info);

//        mCityModel.setLocationKey(locationKey);
//        mCityModel.setLanguage(language);
    }

    public void testFindCity() throws SQLException {

        final StoreCityParam storeCityParam = new StoreCityParam(mCityModel);
        storeCityParam.setOnRequestDataListener(new DataParam.OnRequestDataListener() {
            @Override
            public void onRequestDataCallback(DataParam param) {
                lock.lockNotify();
            }

            @Override
            public void onRequestDataFailed(DataParam param, DataParam.RequestError error) {
                lock.lockNotify();
            }
        });

        mDefaultService.requestData(storeCityParam);
        lock.lockWait();

        City city = new City();
        city.setLanguage(language);

        final FindCityParam fcParam = new FindCityParam(city);
        fcParam.setOnRequestDataListener(new DataParam.OnRequestDataListener() {
            @Override
            public void onRequestDataCallback(DataParam param) {
                lock.lockNotify();
            }

            @Override
            public void onRequestDataFailed(DataParam param, DataParam.RequestError error) {
                lock.lockNotify();
            }
        });
        mDefaultService.requestData(fcParam);
        lock.lockWait(10000);
        assertNotNull("data can not be null", fcParam.getRequestCity());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
