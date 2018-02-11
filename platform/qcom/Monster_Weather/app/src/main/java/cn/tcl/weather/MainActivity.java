/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;

import com.leon.tools.view.AndroidUtils;

import cn.tcl.weather.service.ICityManager;
import cn.tcl.weather.service.UpdateService;
import cn.tcl.weather.utils.CommonUtils;
import cn.tcl.weather.utils.LogUtils;
import cn.tcl.weather.viewhelper.MainLoaddingVh;
import cn.tcl.weather.viewhelper.VhFactory;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created on 16-8-5.
 */
public class MainActivity extends OtherBaseActivity {

    public static final String TAG = MainActivity.class.getName();
    public final static int REQUEST_CODE_LOCATION = 101;
    /*get data service*/
    private UpdateService mUpdateService;

    private IMainVh mCurrentViewHelper;

    private int mCurrentVhType = -1;


    private ServiceConnection mSerivceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (null != mUpdateService) {
                mUpdateService = null;
            }
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mUpdateService = ((UpdateService.UpdateBinder) service).getService();
            if (null != mUpdateService) {
                if (mUpdateService.hasAddedCity()) {
                    setViewHelper(VhFactory.MAIN_ACTIVITY_VH, true);
                } else {
                    setViewHelper(VhFactory.FIRST_RUN_VH, true);
                    mUpdateService.addCityObserver(mObserver);
                }
            }
        }

        private ICityManager.CityObserver mObserver = new ICityManager.CityObserver() {
            @Override
            protected void onCityListChanged() {
                if (mUpdateService.hasAddedCity()) {
                    setViewHelper(VhFactory.MAIN_ACTIVITY_VH, true);
                    mUpdateService.removeCityObserver(mObserver);
                }
            }
        };
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        disableStatuBar();
        super.onCreate(savedInstanceState);
        if (!CommonUtils.isSupportHorizontal(this)) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        bindUpdateService();

        mCurrentViewHelper = new MainLoaddingVh(this);
        mCurrentViewHelper.init();
        setContentView(mCurrentViewHelper.getView());
    }

    public boolean requestPermissions() {
        return AndroidUtils.requestPermission(this, REQUEST_CODE_LOCATION, android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private void setViewHelper(int vhType, boolean setUpdateService) {
        if (mCurrentVhType != vhType) {
            mCurrentVhType = vhType;
            IMainVh vh = VhFactory.newVhInstance(vhType, MainActivity.this);
            changeViewHelper(vh, setUpdateService);
        }
    }

    private void changeViewHelper(IMainVh vh, boolean setUpdateService) {
        mCurrentViewHelper.pause();
        mCurrentViewHelper.recycle();
        mCurrentViewHelper = vh;
        mCurrentViewHelper.init();
        if (setUpdateService)
            mCurrentViewHelper.setUpdateService(mUpdateService);
        setContentView(mCurrentViewHelper.getView());
        mCurrentViewHelper.resume();
    }


    private void bindUpdateService() {
        Intent bindServiceIntent = new Intent(MainActivity.this, UpdateService.class);
        bindService(bindServiceIntent, mSerivceConn, Context.BIND_AUTO_CREATE);
    }

    private void unbindUpdateService() {
        unbindService(mSerivceConn);
    }

    @Override
    protected void onDestroy() {
        mCurrentViewHelper.recycle();
        unbindUpdateService();
        super.onDestroy();
    }


    @Override
    public void onResume() {
        mCurrentViewHelper.resume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCurrentViewHelper.pause();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_LOCATION:
                AndroidUtils.sendMessageCallback(requestCode, UpdateService.START_LOCATING, grantResults);
                break;
        }
    }

    private boolean isInterceptEvent;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final boolean isIntercept = isInterceptEvent;
        if (ev.getPointerCount() > 1) {
            if (!isIntercept) {
                isInterceptEvent = true;
                MotionEvent evup = MotionEvent.obtain(ev);
                evup.setAction(MotionEvent.ACTION_UP);
                superDispatchTouchEvent(evup);
                evup.recycle();
            }
            return false;
        }

        if (isIntercept) {
            final int action = ev.getAction() & MotionEvent.ACTION_MASK;
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
                isInterceptEvent = false;
        }
        if (isIntercept)
            return true;

        return superDispatchTouchEvent(ev);
    }

    private boolean superDispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            LogUtils.e(TAG, "dispatchTouchEvent:", e);
        }
        return false;
    }


    public static interface IMainVh {

        void init();

        void setUpdateService(UpdateService updateService);

        void pause();

        void resume();

        void recycle();

        View getView();
    }


}

