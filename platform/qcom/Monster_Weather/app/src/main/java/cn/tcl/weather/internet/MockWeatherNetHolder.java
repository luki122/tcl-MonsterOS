/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.internet;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.tcl.weather.bean.City;
import cn.tcl.weather.bean.CityWeatherInfo;
import cn.tcl.weather.bean.CityWeatherWarning;
import cn.tcl.weather.bean.DayWeather;
import cn.tcl.weather.bean.HourWeather;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * </p>
 * <p/>
 * created on 16-7-29.
 */
public class MockWeatherNetHolder implements IWeatherNetHolder {

    private final static int TEST_CITY_COUNTS = 30;

    private List<City> mSearchCities = new ArrayList<>(TEST_CITY_COUNTS);

    private City mZhCityModel = new City();

    private City mEnCityModel = new City();

    private Handler mMainHandler;

    public MockWeatherNetHolder(Handler mainHandler) {
        mMainHandler = mainHandler;
    }

    @Override
    public void init() {
        mZhCityModel.latitude = "120.0";
        mZhCityModel.longitude = "130.0";
        mZhCityModel.state = "ok";

        CityWeatherInfo zhWeatherInfo = new CityWeatherInfo();
        zhWeatherInfo.pressure = "2034";
        zhWeatherInfo.observationtime = "10:30 pm";
        zhWeatherInfo.aqiValue = "49";
        zhWeatherInfo.temperature = "25";
        zhWeatherInfo.weatherNo = "15";
        zhWeatherInfo.weatherWarnTxt = "不会有雨";
        zhWeatherInfo.windDirectionNo = "14";
        zhWeatherInfo.windGrade = "4级";
        zhWeatherInfo.humidity = "20%";

        ArrayList<CityWeatherWarning> zhCityWeatherWarnings = new ArrayList<>(4);
        CityWeatherWarning cityWeatherWarning;
        for (int i = 0; i < 4; i++) {
            cityWeatherWarning = new CityWeatherWarning();
            cityWeatherWarning.warnNo = "12";
            cityWeatherWarning.warnContent = "深圳市气象台7月11日14时发布雷电黄色预警信号：预计未来12小时，我市大部将有雷电活动，可能会造成雷击灾害，并可伴有局地性短时强降水、冰雹、大风等恶劣天气，请做好相关防范工作。";
            cityWeatherWarning.warnTime = "2016-07-11 14:00:00";
            zhCityWeatherWarnings.add(cityWeatherWarning);
        }

        DayWeather dayWeather;
        for (int i = 0; i < 7; i++) {
            dayWeather = new DayWeather();
            dayWeather.dayTemp = "25°";
            dayWeather.nightTemp = "15°";
            dayWeather.date = "0";
            zhWeatherInfo.dayWeathers.add(dayWeather);
        }

        HourWeather hourWeather;
        for (int i = 0; i < 24; i++) {
            hourWeather = new HourWeather();
            hourWeather.time = "2014.5.5";
            hourWeather.temperature = 43;
            zhWeatherInfo.hourWeathers.add(hourWeather);
        }
        mZhCityModel.setCityWeatherInfo(zhWeatherInfo);
        mZhCityModel.setCityWeatherWarnings(zhCityWeatherWarnings);

        mEnCityModel.latitude = "120.0";
        mEnCityModel.longitude = "130.0";
        mEnCityModel.state = "ok";

        CityWeatherInfo enWeatherInfo = new CityWeatherInfo();
        enWeatherInfo.pressure = "2034";
        enWeatherInfo.observationtime = "10:30 pm";
        enWeatherInfo.aqiValue = "49";
        enWeatherInfo.temperature = "25";
        enWeatherInfo.weatherNo = "15";
        enWeatherInfo.weatherWarnTxt = "no rain";

        // Weather Warning information
        ArrayList<CityWeatherWarning> enCityWeatherWarnings = new ArrayList<>(4);
        CityWeatherWarning enCityWeatherWarning;
        for (int i = 0; i < 4; i++) {
            enCityWeatherWarning = new CityWeatherWarning();
            enCityWeatherWarning.warnNo = "12";
            enCityWeatherWarning.warnContent = "Shenzhen Municipal Meteorological Station on July 11, 14 release lightning yellow warning signal: is expected in the next 12 hours and the city Department of a lightning activity may cause of lightning disaster, and accompanied by local short-time strong rainfall, hail, high winds and other inclement weather, please to do preventive work related.";
            enCityWeatherWarning.warnTime = "2016-07-11 14:00:00";
            enCityWeatherWarnings.add(enCityWeatherWarning);
        }

        for (int i = 0; i < 7; i++) {
            dayWeather = new DayWeather();
            dayWeather.dayTemp = "25°";
            dayWeather.nightTemp = "15°";
            dayWeather.date = new Date().getTime() + "";
            enWeatherInfo.dayWeathers.add(dayWeather);
        }

        for (int i = 0; i < 24; i++) {
            hourWeather = new HourWeather();
            hourWeather.time = "2014.5.5";
            hourWeather.temperature = 43;
            enWeatherInfo.hourWeathers.add(hourWeather);
        }
        mEnCityModel.setCityWeatherInfo(enWeatherInfo);
        mEnCityModel.setCityWeatherWarnings(enCityWeatherWarnings);


        //add test cities

        City city = new City();
        city.latitude = "120.0";
        city.longitude = "130.0";
        city.state = "ok";
        for (int i = 0; i < TEST_CITY_COUNTS; i++) {
            city = city.clone();
//            city.setCityName("test_city_" + i);
            mSearchCities.add(city);
        }


    }

    @Override
    public void recycle() {

    }

    @Override
    public void onTrimMemory(int level) {

    }

    @Override
    public void requestCityByCity(City city, String lang, IWeatherNetCallback cb) {

    }

    @Override
    public void requestCityListByName(String name, String lang, final IWeatherNetCallback cb) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                cb.onReceivedData(IWeatherNetCallback.ACTION_REQUEST_CITY_BY_LOCATION_KEY, mSearchCities);
            }
        });
    }

    @Override
    public void requestCityBylocationKey(String locationKey, String lang, final IWeatherNetCallback cb) {
        final City city;
        if (lang.contains("zh")) {
            city = mZhCityModel.clone();
        } else {
            city = mEnCityModel.clone();
        }
//        city.setCityName("city:" + locationKey);
        city.setLanguage(lang);
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                cb.onReceivedData(IWeatherNetCallback.ACTION_REQUEST_CITY_BY_LOCATION_KEY, city);
            }
        });
    }

    @Override
    public void requestCityByGeo(String lon, String lat, String lang, IWeatherNetCallback cb) {

    }
}
