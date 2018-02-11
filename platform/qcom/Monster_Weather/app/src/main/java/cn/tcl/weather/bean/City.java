/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.bean;

import android.text.TextUtils;

import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import java.util.ArrayList;
import java.util.List;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * </p>
 * <p/>
 * created on 16-7-29.
 */
public class City extends JSONBean implements Cloneable {

    private final static String SKIP_SIGN = "-";
    public final static int CITY_TYPE_UNKNOW = 0;
    public final static int CITY_TYPE_INTERNAL = 1;
    public final static int CITY_TYPE_EXTERNAL = 2;

    private boolean isLocateCity;


    @JSONBeanField(name = "cityType")
    private int cityType = CITY_TYPE_UNKNOW;

    @JSONBeanField(name = "state")
    public String state;

    @JSONBeanField(name = "latitude")
    public String latitude;

    @JSONBeanField(name = "longitude")
    public String longitude;

    @JSONBeanField(name = "launguage")
    public String launguage;

    @JSONBeanField(name = "countyName")
    private String countyName = "";

    @JSONBeanField(name = "province")
    private String province = "";

    @JSONBeanField(name = "cityName")
    private String cityName = "";

    @JSONBeanField(name = "country")
    private String country = "";

    @JSONBeanField(name = "locationKey")
    private String locationKey = "";

    @JSONBeanField(name = "cityWeatherInfos")
    private CityWeatherInfo mCityWeatherInfo = new CityWeatherInfo();//city weather info

    @JSONBeanField(name = "CityWeatherWarning")
    private ArrayList<CityWeatherWarning> mCityWeatherWarnings = new ArrayList<>(5);    // Weather warning

    @JSONBeanField(name = "refreshTimeMills")
    public long refreshTimeMills;

    @JSONBeanField(name = "isRefreshSucceed")
    public boolean isRefreshSucceed;

    public String getCountry() {
        return country;
    }


    public String getCityName() {
        return cityName;
    }

    public String getProvince() {
        return province;
    }

    public String getCountyName() {
        return countyName;
    }


    public String getFullName(String sign) {
        StringBuilder builder = new StringBuilder();
        builder.append(countyName);
        builder.append(sign);
        if (!cityName.equals(countyName)) {
            builder.append(cityName);
            builder.append(sign);
        }

        if (!province.equals(cityName)) {
            builder.append(province);
            builder.append(sign);
        }
        builder.append(country);
        return builder.toString();
    }

    public boolean hasWeatherWarning() {
        return !mCityWeatherWarnings.isEmpty();
    }


    public ArrayList<CityWeatherWarning> getCityWeatherWarnings() {
        return mCityWeatherWarnings;
    }

    public void setCityWeatherWarnings(ArrayList<CityWeatherWarning> warnings) {
        mCityWeatherWarnings.addAll(warnings);
    }

    public CityWeatherInfo getCityWeatherInfo() {
        return mCityWeatherInfo;
    }

    public void setCityWeatherInfo(CityWeatherInfo info) {
        mCityWeatherInfo.setCityWeatherInfo(info);
    }


    public void combineWeatherInfo(CityWeatherInfo info) {
        mCityWeatherInfo.combineWeatherInfo(info);
    }


    /**
     * is located city
     *
     * @return
     */
    public boolean isLocateCity() {
        return isLocateCity;
    }

    public void setLocateCity(boolean isLocateCity) {
        this.isLocateCity = isLocateCity;
    }


    /**
     * set Id to city
     *
     * @param id
     */
    public void setId(int id) {
    }

    /**
     * get the id from city
     *
     * @return
     */
    public int getId() {
        return 1;
    }

    /**
     * set city data
     *
     * @param city
     */
    public void setCity(City city) {
        state = city.state;
        latitude = city.latitude;
        longitude = city.longitude;
        launguage = city.launguage;
        this.locationKey = city.locationKey;
        this.cityType = city.cityType;
        isRefreshSucceed = city.isRefreshSucceed;
        refreshTimeMills = city.refreshTimeMills;
        setCity(city.country, city.province, city.cityName, city.countyName);
        mCityWeatherInfo.setCityWeatherInfo(city.mCityWeatherInfo);
        mCityWeatherWarnings.clear();
        mCityWeatherWarnings.addAll(city.getCityWeatherWarnings());
    }

    public static String getString(String value, String defaultString) {
        return TextUtils.isEmpty(value) ? defaultString : value;
    }

    public static int getInt(int value, int defaultValue) {
        return value != 0 ? value : defaultValue;
    }

    public void conbineCity(City city) {
        state = getString(city.state, state);
        latitude = getString(city.latitude, latitude);
        longitude = getString(city.longitude, longitude);
        launguage = getString(city.launguage, launguage);
        country = getString(city.country, country);
        province = getString(city.province, province);
        cityName = getString(city.cityName, cityName);
        countyName = getString(city.countyName, countyName);
        locationKey = getString(city.locationKey, locationKey);
        cityType = getInt(city.cityType, cityType);
        combineCityWeatherInfo(city.getCityWeatherInfo());
        combineCityWeatherWarning(city.getCityWeatherWarnings());
    }

    private void combineCityWeatherInfo(CityWeatherInfo cityWeatherInfo) {
        this.mCityWeatherInfo.combineWeatherInfo(cityWeatherInfo);
    }

    public void combineCityWeatherWarning(List<CityWeatherWarning> warnings) {
        if (!warnings.isEmpty()) {
            this.mCityWeatherWarnings.clear();
            this.mCityWeatherWarnings.addAll(warnings);
        }
    }

    public boolean hasRequestCityName() {
        return !TextUtils.isEmpty(province) || !TextUtils.isEmpty(cityName) || !TextUtils.isEmpty(countyName);
    }

    public String getRequestCityName() {
        if (!TextUtils.isEmpty(countyName))
            return countyName;
        if (!TextUtils.isEmpty(cityName))
            return cityName;
        return province;
    }

    public boolean hasLocationKey() {
        return !TextUtils.isEmpty(locationKey);
    }

    public String getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(String locationKey) {
        if (TextUtils.isEmpty(locationKey))
            return;
        this.locationKey = locationKey;
        if (!hasRequestCityName()) {
            String[] items = locationKey.split(SKIP_SIGN);
            if (items.length >= 4) {
                country = items[0];
                province = items[1];
                cityName = items[2];
                countyName = items[3];
            }
        }
    }

    public void setLanguage(String language) {
        if (TextUtils.isEmpty(language))
            language = "zh-cn";
        launguage = language;
    }

    public String getLanguage() {
        return launguage;
    }


    @Override
    public City clone() {
        City city = new City();
        city.setCity(this);
        return city;
    }


    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }


    public void setCity(String country, String province, String cityName, String countyName) {
        if (TextUtils.isEmpty(country))
            country = "";
        if (TextUtils.isEmpty(province))
            province = "";
        this.country = country;
        this.province = province;
        if (TextUtils.isEmpty(cityName))
            this.cityName = province;
        else
            this.cityName = cityName;

        if (TextUtils.isEmpty(countyName))
            this.countyName = cityName;
        else
            this.countyName = countyName;
    }


    public static String createLocationKeyByLocated(String country, String province, String cityName, String countyName) {
        StringBuffer builder = new StringBuffer();
        builder.append(country);
        builder.append(SKIP_SIGN);
        builder.append(province);
        builder.append(SKIP_SIGN);
        builder.append(cityName);
        builder.append(SKIP_SIGN);
        builder.append(countyName);
        return builder.toString();
    }


    public boolean isExternalCity() {
        return CITY_TYPE_EXTERNAL == cityType;
    }

    public int getCityType() {
        return cityType;
    }

    public void setCityType(int cityType) {
        this.cityType = cityType;
    }

}
