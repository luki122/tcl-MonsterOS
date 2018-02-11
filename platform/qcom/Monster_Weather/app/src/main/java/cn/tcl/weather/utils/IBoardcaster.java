package cn.tcl.weather.utils;

import android.os.Message;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-5.
 * inner board cast receiver
 */
public interface IBoardcaster extends IManager {

    void regiestOnReceiver(String action, Receiver receiver);

    void unregiestOnReceiver(String action, Receiver receiver);

    /**
     * send message to receiver
     *
     * @param action
     * @param msg
     */
    void sendMessage(String action, Message msg);


    /**
     * send message to receiver
     *
     * @param action
     * @param msg
     * @param isMainThread
     */
    void sendMessage(String action, Message msg, boolean isMainThread);

    /**
     * send message to receiver
     *
     * @param action
     * @param msg
     * @param delayTimeMills
     */
    void sendMessage(String action, Message msg, int delayTimeMills);

    interface Receiver {
        void onReceived(String action, Message msg);
    }

}
