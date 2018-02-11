/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.internet;

import android.location.Location;

import cn.tcl.weather.utils.IManager;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-4.
 * interface of location client
 */
public interface ILocationClient extends IManager {
    int STATE_NULL = 0;
    int STATE_START = 1;

    int STATE_FAILED_NO_PERMISSION = 2;
    int STATE_FAILED_LOCATE_FAILED = 3;
    int STATE_FAILED_NETWORK_USELESS = 4;

    int STATE_SUCCEED = 5;

    /**
     * start locate
     */
    void startLocate();

    /**
     * stop locate
     */
    void stopLocate();

    /**
     * regiest {@link OnLocationClientListener}
     *
     * @param l
     */
    void regiestOnLocationClientListener(OnLocationClientListener l);

    /**
     * unregiest {@link OnLocationClientListener}
     *
     * @param l
     */
    void unregiestOnLocationClientListener(OnLocationClientListener l);

    /**
     * the listener of {@link ILocationClient},</br>
     * you can use {@link ILocationClient#regiestOnLocationClientListener(OnLocationClientListener)} regiest ,</br>
     * you can use {@link ILocationClient#unregiestOnLocationClientListener(OnLocationClientListener)} unregiest it
     */
    interface OnLocationClientListener {

        /**
         * if locate succeed this method will be called
         */
        void onLocated(Location location);


        /**
         * if locating this will be called
         *
         * @param state
         */
        void onLocating(int state);
    }

}
