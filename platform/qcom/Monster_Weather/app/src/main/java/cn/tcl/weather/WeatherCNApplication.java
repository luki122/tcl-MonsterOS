package cn.tcl.weather;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import cn.tcl.weather.service.UpdateService;
import cn.tcl.weather.utils.IBoardcaster;
import cn.tcl.weather.utils.InnerBoardcaster;
import cn.tcl.weather.utils.bitmap.ActivityBmpLoadManager;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-31.
 * $desc
 */
public class WeatherCNApplication extends Application implements IBoardcaster {
    private final static String LONDON_MOD_NAME = "London";

//    private final static String DEF_SYSTEM_PLATEFORM_TYPE = "def_system_plateform_type";
//    private final static String DEF_SYSTEM_PLATEFORM_TYPE = "def_system_plateform_type";


    public final static int SYSTEM_TYPE_NULL = 0;
    public final static int SYSTEM_TYPE_LONDON = 1;


    private static WeatherCNApplication mApplication;

    public static WeatherCNApplication getWeatherCnApplication() {
        return mApplication;
    }

    private IBoardcaster mBoardcaster;

    private SystemTypeManager mSystemTypeManager = new SystemTypeManager();

    private ActivityBmpLoadManager mActivityBmpLoadManager;


    public WeatherCNApplication() {
        mApplication = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mActivityBmpLoadManager = new ActivityBmpLoadManager(this);
        mActivityBmpLoadManager.init();
        mSystemTypeManager.init();
        Intent intent = new Intent(this, UpdateService.class);
        startService(intent);

        mBoardcaster = new InnerBoardcaster(new Handler());
        mBoardcaster.init();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (null != mActivityBmpLoadManager)
            mActivityBmpLoadManager.onTrimMemory(level);
    }

    @Override
    public void onTerminate() {
        mBoardcaster.recycle();
        if (null != mActivityBmpLoadManager)
            mActivityBmpLoadManager.recycle();
        super.onTerminate();
    }


    public ActivityBmpLoadManager getActivityBmpLoadManager() {
        return mActivityBmpLoadManager;
    }

    /**
     * get current system type
     *
     * @return
     */
    public int getCurrentSystemType() {
        return mSystemTypeManager.getCurrentSystemType();
    }

    @Override
    public void regiestOnReceiver(String action, Receiver receiver) {
        mBoardcaster.regiestOnReceiver(action, receiver);
    }

    @Override
    public void unregiestOnReceiver(String action, Receiver receiver) {
        mBoardcaster.unregiestOnReceiver(action, receiver);
    }

    @Override
    public void sendMessage(String action, Message msg) {
        mBoardcaster.sendMessage(action, msg);
    }

    @Override
    public void sendMessage(String action, Message msg, boolean isMainThread) {
        mBoardcaster.sendMessage(action, msg, isMainThread);
    }

    @Override
    public void sendMessage(String action, Message msg, int delayTimeMills) {
        mBoardcaster.sendMessage(action, msg, delayTimeMills);
    }

    @Deprecated
    @Override
    public void init() {

    }

    @Deprecated
    @Override
    public void recycle() {

    }


    private class SystemTypeManager {
        private int mSystemType;

        void init() {
            //setCurrentSystemType(PLFUtils.getInteger(this, DEF_SYSTEM_PLATEFORM_TYPE));
            String modelName = getDevModelName();
            if (!TextUtils.isEmpty(modelName) && modelName.contains(LONDON_MOD_NAME)) {
                setCurrentSystemType(SYSTEM_TYPE_LONDON);
            } else {
                setCurrentSystemType(SYSTEM_TYPE_NULL);
            }
        }

        void setCurrentSystemType(int type) {
            mSystemType = type;
        }

        int getCurrentSystemType() {
            return mSystemType;
        }


        String getDevModelName() {
            return Build.MODEL;
        }
    }
}
