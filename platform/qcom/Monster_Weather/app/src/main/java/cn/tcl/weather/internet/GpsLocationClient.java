/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.internet;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import java.util.HashSet;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-4.
 * $desc
 */
public class GpsLocationClient implements ILocationClient {
    private LocationManager mLocationManager;

    private HashSet<OnLocationClientListener> mLocationListeners = new HashSet<>(8);
    private Context mContext;
    private boolean isNetworkProvideEnable = true;
    private boolean isGPSProvideEnable = true;

    public GpsLocationClient(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void startLocate() {
        PackageManager pm = mContext.getApplicationContext().getPackageManager();
        isNetworkProvideEnable = (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                && pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK));
        isGPSProvideEnable = (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS));
        if (isNetworkProvideEnable
//                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED
                ) {
//            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 1, mLocationListener);
        } else if (isGPSProvideEnable
//                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)                == PackageManager.PERMISSION_GRANTED
                ) {
//            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 1, mLocationListener);
        } else {
            for (OnLocationClientListener l : mLocationListeners) {
//                l.onFailed("gps location permission denied");
            }
        }
    }

    @Override
    public void stopLocate() {
//        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//            mLocationManager.removeUpdates(mLocationListener);
//        }
    }

    @Override
    public void regiestOnLocationClientListener(OnLocationClientListener l) {
        mLocationListeners.add(l);
    }

    @Override
    public void unregiestOnLocationClientListener(OnLocationClientListener l) {
        mLocationListeners.remove(l);
    }

    @Override
    public void init() {
    }

    @Override
    public void recycle() {

    }

    @Override
    public void onTrimMemory(int level) {

    }


    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            for (OnLocationClientListener l : mLocationListeners) {
                l.onLocated(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
