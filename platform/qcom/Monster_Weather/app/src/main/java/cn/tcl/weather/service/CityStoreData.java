/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.service;


import android.os.Handler;
import android.text.TextUtils;


import com.leon.tools.JSONBean;
import com.leon.tools.JSONBeanField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.tcl.weather.bean.City;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-3.
 * the city data store
 */
class CityStoreData extends JSONBean {
    private final static int POST_DELAY_TIME = 100;
    private final static String KEY = "city_store_data_city_key_list";
    private HashMap<String, City> mCitys = new HashMap<>(12);

    @JSONBeanField(name = "locateCityKey")
    private String mLocateCityKey = "";

    @JSONBeanField(name = "cityKeys")
    private ArrayList<String> mCityKeyList = new ArrayList<>(8);// the order of cities
    private Handler mMainHandler;
    private String mLanguage;
    private DefaultCityManager mCityManager;

    CityStoreData() {
    }

    CityStoreData(DefaultCityManager cityManager, Handler mainHandler) {
        initStoreData(cityManager, mainHandler);
    }

    void initStoreData(DefaultCityManager cityManager, Handler mainHandler) {
        mCityManager = cityManager;
        mMainHandler = mainHandler;
    }

    City getCity(City city) {
        City nCity;
        nCity = getCityByLocationKey(city.getLocationKey());
        return nCity;
    }


    boolean hasAddedCity() {
        if (mCityKeyList.isEmpty()) {
            return !TextUtils.isEmpty(mLocateCityKey);
        }
        return true;
    }


    boolean isFull() {
        return mCityKeyList.size() >= 8;
    }

    boolean moveCityToPosition(City city, int dstIndex) {
        if(!TextUtils.isEmpty(mLocateCityKey)){
            dstIndex = dstIndex - 1;// except locatecity
        }
        if (dstIndex < 0)
            dstIndex = 0;
        else if (dstIndex >= mCityKeyList.size())
            dstIndex = mCityKeyList.size() - 1;
        int oldIndex = mCityKeyList.indexOf(city.getLocationKey());
        if (oldIndex != dstIndex) {
            mCityKeyList.remove(oldIndex);
//            if (dstIndex > oldIndex) {
//                dstIndex--;
//            }
            mCityKeyList.add(dstIndex, city.getLocationKey());
            return true;
        }
        return false;

    }


    boolean addCity(City city, int index) {
        boolean isAdded = false;
        if (city.isLocateCity() || mLocateCityKey.equals(city.getLocationKey())) {// if the city is located city
            if (!mLocateCityKey.equals(city.getLocationKey())) {
                mLocateCityKey = city.getLocationKey();
                isAdded = true;
            }
            city.setLocateCity(true);
            mCityKeyList.remove(city.getLocationKey());//remove located city in city list
        } else if (!mCityKeyList.contains(city.getLocationKey())) {
            if (mCityKeyList.isEmpty()) {
                mCityKeyList.add(city.getLocationKey());
            } else {
                index -= 1;// except locate city
                if (index < 0)
                    index = 0;
                else if (index > mCityKeyList.size())
                    index = mCityKeyList.size();
                mCityKeyList.add(index, city.getLocationKey());
            }
            isAdded = true;
        }
        mCitys.put(city.getLocationKey(), city);
        return isAdded;
    }


    void changeCity(City firstCity, City secondCity) {
        int firstIndex = mCityKeyList.indexOf(firstCity.getLocationKey());
        int secondIndex = mCityKeyList.indexOf(secondCity.getLocationKey());

        if (firstIndex > secondIndex) {
            int index = firstIndex;
            firstIndex = secondIndex;
            secondIndex = index;

            City city = firstCity;
            firstCity = secondCity;
            secondCity = city;
        }

        mCityKeyList.remove(secondIndex);
        mCityKeyList.remove(firstIndex);

        mCityKeyList.add(firstIndex, secondCity.getLocationKey());
        if (mCityKeyList.size() > secondIndex) {
            mCityKeyList.add(secondIndex, firstCity.getLocationKey());
        } else {
            mCityKeyList.add(firstCity.getLocationKey());
        }

    }

    /**
     * set the language
     *
     * @param language
     */
    void setCurrentLanguage(String language) {
        if (!language.equals(mLanguage)) {
            mLanguage = language;
            if (TextUtils.isEmpty(mLanguage))
                mLanguage = ICityManager.LANGUAGE_CN;
            updateAllCitiesRunnable(true);
        }
    }

    String getCurrentLanguage() {
        return mLanguage;
    }

    /**
     * add city to cache
     *
     * @param city
     * @return ture added false update
     */
    boolean addCity(City city) {
        boolean isAdded = false;
        if (city.isLocateCity() || mLocateCityKey.equals(city.getLocationKey())) {// if the city is located city
            if (!mLocateCityKey.equals(city.getLocationKey())) {
                mLocateCityKey = city.getLocationKey();
                isAdded = true;
            }
            city.setLocateCity(true);
            mCityKeyList.remove(city.getLocationKey());//remove located city in city list
        } else if (!mCityKeyList.contains(city.getLocationKey())) {
            mCityKeyList.add(city.getLocationKey());
            isAdded = true;
        }
        mCitys.put(city.getLocationKey(), city);
        return isAdded;
    }

    boolean hasCity(City city) {
        boolean hasCity;
        hasCity = mCityKeyList.contains(city.getLocationKey());
        if (!hasCity) {//locate city
            hasCity = mLocateCityKey.equals(city.getLocationKey());
        }
        return hasCity;
    }

    void removeCity(City city) {
        mCityKeyList.remove(city.getLocationKey());
        mCitys.remove(city.getLocationKey());
    }

    City getCityByLocationKey(String locationKey) {
        City nCity = mCitys.get(locationKey);
        if (null != nCity && mLanguage.equals(nCity.getLanguage()))
            return nCity;
        return null;
    }

    List<City> listAllCity() {
        List<City> cities = new ArrayList<>(mCityKeyList.size());
        City city = getCityByLocationKey(mLocateCityKey);
        if (null != city) {
            cities.add(city);
        }
        for (String loactionKey : mCityKeyList) {
            city = getCityByLocationKey(loactionKey);
            if (null != city) {
                cities.add(city);
            }
        }
        return cities;
    }

    /**
     * update the cities
     */
    void updateAllCitiesRunnable() {
        updateAllCitiesRunnable(false);
    }

    /*
     * update cities
     * @param isUpdateAll true all Cities, otherwise not all
     */
    void updateAllCitiesRunnable(boolean isUpdateAll) {
        if (isUpdateAll) {
            mMainHandler.removeCallbacks(mUpdateNotExitesCitiesRunnable);
            mMainHandler.removeCallbacks(mUpdateAllCitiesRunnable);
            mMainHandler.postDelayed(mUpdateAllCitiesRunnable, POST_DELAY_TIME);
        } else {
            mMainHandler.removeCallbacks(mUpdateNotExitesCitiesRunnable);
            mMainHandler.postDelayed(mUpdateNotExitesCitiesRunnable, POST_DELAY_TIME);
        }
    }


    private Runnable mUpdateNotExitesCitiesRunnable = new Runnable() {
        @Override
        public void run() {
            if (null == getCityByLocationKey(mLocateCityKey)) {
                postUpdateCityRunnable(mLocateCityKey, 0, true);
            }
            for (String locationKey : mCityKeyList) {
                if (null == getCityByLocationKey(locationKey)) {
                    postUpdateCityRunnable(locationKey, 0);
                }
            }
        }
    };

    private void postUpdateCityRunnable(final String locationKey, int delayTime) {
        postUpdateCityRunnable(locationKey, delayTime, false);
    }

    private void postUpdateCityRunnable(final String locationKey, int delayTime, final boolean isLocatedCity) {
        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                City city = new City();
                city.setLocateCity(isLocatedCity);
                city.setLanguage(mLanguage);
                city.setLocationKey(locationKey);
                mCityManager.updateCityFromeDb(city);
            }
        }, delayTime);
    }

    private Runnable mUpdateAllCitiesRunnable = new Runnable() {
        @Override
        public void run() {
            if (!TextUtils.isEmpty(mLocateCityKey))
                postUpdateCityRunnable(mLocateCityKey, 0, true);
            for (final String locationKey : mCityKeyList) {
                postUpdateCityRunnable(locationKey, 0);
            }
        }
    };

}
