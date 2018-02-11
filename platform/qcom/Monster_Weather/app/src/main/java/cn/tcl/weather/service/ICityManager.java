/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.service;

import java.util.List;

import cn.tcl.weather.utils.IManager;
import cn.tcl.weather.bean.City;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-7-28.
 */
public interface ICityManager extends IManager {
    String LANGUAGE_CN = "zh-cn";


    boolean hasAddedCity();

    boolean isCityFull();

    /**
     * get current location city
     *
     * @return
     */
    City getLocationCity();

    /**
     * list the cities all we have
     *
     * @return
     */
    List<City> listAllCity();

    /**
     * add a city to storage
     *
     * @param city
     */
    void addCity(City city);

    /**
     * remove a city from storage
     *
     * @param city
     */
    void removeCity(City city);


    /**
     * add city to storage
     *
     * @param city
     * @param index
     */
    void addCity(City city, int index);

    /**
     * exchange city position
     *
     * @param firstCity
     * @param secondCity
     */
    void exchangeCity(City firstCity, City secondCity);


    /**
     * move city to position
     *
     * @param city
     * @param dstPosition
     */
    void changeCityPosition(City city, int dstPosition);

    /**
     * add a observer for city
     *
     * @param observer
     */
    void addCityObserver(CityObserver observer);

    /**
     * remove a observer for city
     *
     * @param observer
     */
    void removeCityObserver(CityObserver observer);

    /**
     * check current language
     *
     * @return
     */
    String checkCurrentLanguage();

    /**
     * update the info of cities you added
     */
    void updateAllCitiesInfos();


    class CityObserver {

        public final static int ADD_STATE_START = 1;
        public final static int ADD_STATE_ADDING = 2;
        public final static int ADD_STATE_ADDED = 3;
        public final static int ADD_STATE_FAILED = 4;
        public final static int ADD_STATE_HAS_ADDED = 5;
        public final static int ADD_STATE_LOCATE_ADDED = 6;
        public final static int ADD_STATE_FULL = 7;

        public final static int REMOVE_STATE_START = 1;
        public final static int REMOVE_STATE_REMOVING = 2;
        public final static int REMOVE_STATE_REMOVED = 3;
        public final static int REMOVE_STATE_FIALED = 4;


        public final static int UPDATE_STATE_SUCCEED = 1;
        public final static int UPDATE_STATE_FAILED = 2;

        /**
         * it will be callback when city is adding
         *
         * @param city
         * @param state
         */
        protected void onCityAdding(City city, int state) {

        }

        /**
         * it will be callback when city's adding failed
         *
         * @param city
         * @param state
         */
        protected void addCityFailed(City city, int state) {

        }

        /**
         * it will be callback when city's adding succeed
         *
         * @param city
         * @param state
         */
        protected void addCitySucceed(City city, int state) {

        }

        /**
         * it will be callback when city is removing
         *
         * @param city
         * @param state
         */
        protected void onCityRemoving(City city, int state) {
        }

        /**
         * it will be callback when city is update
         *
         * @param city
         * @param state
         */
        protected void onCityUpdate(City city, int state) {
        }

        /**
         * itm will be callback when city is added or removed or city is update
         */
        protected void onCityListChanged() {
        }
    }
}

