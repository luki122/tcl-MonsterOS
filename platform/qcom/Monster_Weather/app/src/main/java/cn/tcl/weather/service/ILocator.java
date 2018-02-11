package cn.tcl.weather.service;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-20.
 */
public interface ILocator {

    int STATE_START = 1;
    int STATE_LOCATING = 2;
    int STATE_SUCCEED = 3;

    int STATE_FAILED_NO_PERMISSION = 4;
    int STATE_FAILED_USELESS_NETWORK = 5;
    int STATE_FAILED_LOCATE_FAILED = 6;
    int STATE_REQUEST_FAILED = 7;


    void regiestLocateObserver(LocateObserver observer);

    void unregiestLocateObserver(LocateObserver observer);

    interface LocateObserver {
        void onLocating(int state);
    }
}
