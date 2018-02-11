/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import cn.tcl.weather.service.UpdateService;
import cn.tcl.weather.utils.LogUtils;
import cn.tcl.weather.viewhelper.AbsWeatherCityManagerVh;
import cn.tcl.weather.viewhelper.VhFactory;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-9.
 * City manager
 */
public class TclWeatherManagerActivity extends TclBaseActivity {

    private final static String TAG = "TclWeatherManagerActivity";

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mWeatherCityManagerVh.setUpdateService(((UpdateService.UpdateBinder) service).getService());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    private AbsWeatherCityManagerVh mWeatherCityManagerVh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initWindow();
        mWeatherCityManagerVh = VhFactory.newVhInstance(VhFactory.CITY_MANAGER_VH, this);
        mWeatherCityManagerVh.init();
        setContentView(mWeatherCityManagerVh.getView());
        bindUpdateService();
    }

    private void bindUpdateService() {
        Intent bindServiceIntent = new Intent(TclWeatherManagerActivity.this, UpdateService.class);
        bindService(bindServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindUpdateService() {
        unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy() {
        unbindUpdateService();
        mWeatherCityManagerVh.recycle();
        super.onDestroy();
    }

    /**
     * Initialize window
     */
    private void initWindow() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            final int action = ev.getAction() & MotionEvent.ACTION_MASK;
            if (action == MotionEvent.ACTION_DOWN)
                mWeatherCityManagerVh.onDonwTouch();
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            LogUtils.d(TAG, "dispatchTouchEvent:" + ev.toString());
        }
        return false;
    }

}
