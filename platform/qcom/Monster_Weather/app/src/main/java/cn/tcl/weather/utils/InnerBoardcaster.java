package cn.tcl.weather.utils;

import android.os.Handler;
import android.os.Message;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-5.
 * $desc
 */
public class InnerBoardcaster implements IBoardcaster {

    private HashMap<String, Set<Receiver>> mReceivers = new HashMap<>(20);

    private Handler mMainHandler;

    public InnerBoardcaster(Handler mainHandler) {
        mMainHandler = mainHandler;
    }

    @Override
    public void regiestOnReceiver(final String action, final Receiver receiver) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                addReceiver(action, receiver);
            }
        });
    }

    private void addReceiver(String action, Receiver receiver) {
        Set<Receiver> receivers = mReceivers.get(action);
        if (null == receivers) {
            receivers = new HashSet<>(7);
            mReceivers.put(action, receivers);
        }
        receivers.add(receiver);
    }

    private void removeReceiver(String action, Receiver receiver) {
        Set<Receiver> receivers = mReceivers.get(action);
        if (null != receivers) {
            receivers.remove(receiver);
        }
    }

    @Override
    public void unregiestOnReceiver(final String action, final Receiver receiver) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                removeReceiver(action, receiver);
            }
        });
    }

    @Override
    public void sendMessage(final String action, final Message msg) {
        sendMessage(action, msg, true);
    }

    @Override
    public void sendMessage(final String action, final Message msg, boolean isMainThread) {
        if (isMainThread) {
            Set<Receiver> receivers = mReceivers.get(action);
            if (null != receivers) {
                for (Receiver receiver : receivers) {
                    receiver.onReceived(action, msg);
                }
            }
        } else {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Set<Receiver> receivers = mReceivers.get(action);
                    if (null != receivers) {
                        for (Receiver receiver : receivers) {
                            receiver.onReceived(action, msg);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void sendMessage(String action, Message msg, int delayTimeMills) {
        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Set<Receiver> receivers = mReceivers.get(action);
                if (null != receivers) {
                    for (Receiver receiver : receivers) {
                        receiver.onReceived(action, msg);
                    }
                }
            }
        }, delayTimeMills);
    }


    @Override
    public void init() {

    }

    @Override
    public void recycle() {
        for (Set<Receiver> receivers : mReceivers.values()) {
            receivers.clear();
        }
        mReceivers.clear();
    }

    @Override
    public void onTrimMemory(int level) {

    }
}
