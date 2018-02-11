/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.internet;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.leon.tools.view.AndroidUtils;

import java.util.HashSet;

import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.utils.IBoardcaster;
import cn.tcl.weather.utils.LogUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-4.
 * use baidu to locate current location
 */
public class BaiduLocationClient implements ILocationClient {
    private final static String TAG = "BaiduLocationClient";


    private final static int LOCATE_TIME_MILLS = 45 * 1000;

    private LocationClient mLocationClient;
    private HashSet<OnLocationClientListener> mLocationListeners = new HashSet<>(8);
    private Context mContext;

    private int mNotifyState;

    private Handler mHandler = new Handler();

    private Runnable mFailedRunnable = new Runnable() {
        @Override
        public void run() {
            notifyState(STATE_FAILED_LOCATE_FAILED);
            stopLocate();
        }
    };

    public BaiduLocationClient(Context context) {
        mLocationClient = new LocationClient(context);
        mContext = context;
    }

    @Override
    public void init() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        option.setCoorType("bd09ll");
        option.setScanSpan(10 * 1000);
        option.setIsNeedAddress(false);
        option.setOpenGps(false);
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(mlocationListener);
    }

    @Override
    public void recycle() {
        stopLocate();
        mLocationClient.unRegisterLocationListener(mlocationListener);
    }

    @Override
    public void onTrimMemory(int level) {

    }

    private BDLocationListener mlocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            try {
                if (bdLocation != null) {
                    String locType = null;
                    if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                        locType = " network (" + bdLocation.getNetworkLocationType() + ")";
                    } else if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                        locType = " gps (" + bdLocation.getSatelliteNumber() + " satellites)";
                    }

                    if (!TextUtils.isEmpty(locType)) {
                        String provider = "baidu: " + locType;
                        Location location = new Location(provider);
                        location.setLongitude(bdLocation.getLongitude());
                        location.setLatitude(bdLocation.getLatitude());
                        location.setAccuracy(bdLocation.getRadius());
                        location.setTime(System.currentTimeMillis());
                        location.setElapsedRealtimeNanos(System.nanoTime());

                        for (OnLocationClientListener l : mLocationListeners) {
                            l.onLocated(location);
                        }
                        notifyState(STATE_SUCCEED);
                        mHandler.removeCallbacks(mFailedRunnable);
                        stopLocate();
                    }
                }
            } catch (Exception e) {
                LogUtils.e(TAG, "BDLocationListener onReceiveLocation ", e);
            }
        }
    };

    @Override
    public void startLocate() {
//        LogUtils.d(TAG, "start locate");
        if (AndroidUtils.netWorkUseable(mContext)) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (AndroidUtils.grantedPermission(mContext, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    notifyState(STATE_START);
                    mLocationClient.start();
                    mHandler.removeCallbacks(mFailedRunnable);
                    mHandler.postDelayed(mFailedRunnable, LOCATE_TIME_MILLS);
                } else {
                    notifyState(STATE_FAILED_NO_PERMISSION);
                }
            } else {
                notifyState(STATE_START);
                mLocationClient.start();
                mHandler.removeCallbacks(mFailedRunnable);
                mHandler.postDelayed(mFailedRunnable, LOCATE_TIME_MILLS);
            }
        } else {
            notifyState(STATE_FAILED_NETWORK_USELESS);
        }
    }

    private void notifyState(int state) {
//        LogUtils.d(TAG, "notifyState: " + state);
        mNotifyState = state;
        for (OnLocationClientListener l : mLocationListeners) {
            l.onLocating(state);
        }
    }


    @Override
    public void stopLocate() {
        if (mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
    }

    @Override
    public void regiestOnLocationClientListener(OnLocationClientListener l) {
        mLocationListeners.add(l);
        if (mNotifyState != STATE_NULL) {
            l.onLocating(STATE_START);
            if (mNotifyState != STATE_START)
                l.onLocating(mNotifyState);
        }
    }

    @Override
    public void unregiestOnLocationClientListener(OnLocationClientListener l) {
        mLocationListeners.remove(l);
    }

}
