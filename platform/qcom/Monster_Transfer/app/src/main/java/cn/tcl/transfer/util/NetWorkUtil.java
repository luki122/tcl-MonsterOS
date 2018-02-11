/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.util;


import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;

/**
 * Util class deal with network. You can use this class to check features
 * related to network.
 *
 */
public class NetWorkUtil {
    private static final String TAG = "NetWorkUtil";

    public static final int TYPE_SIMPLE = 100000;

    public static final int TYPE_SYS_DATA = 200000;
    public static final int TYPE_SYS_SQL_DATA = 200001;
    public static final int TYPE_SYS_OTHER_DATA = 200002;

    public static final int TYPE_APK = 300000;
    public static final int TYPE_APP_DATA = 300001;
    public static final int TYPE_APP_SD_DATA = 300002;
    public static final int TYPE_APP_SD_CREATE_DATA = 300003;

    public static final int TYPE_CATEGOY_START = 400000;
    public static final int TYPE_CATEGOY_END = 400001;

    public static final int TYPE_SEND_INFO = 500000;
    public static final int TYPE_SEND_SIZE = 500001;

    public static final int TYPE_END = 600000;

    public static final int TYPE_HEART_BEAT = 900000;

    public static final long DEFAULT_SPEED = 5 * 1024 *1024;

    public static final int TIME_OUT = 5 * 1000;


    /**
     * Check if GPS is connected.
     * @param context from which to check
     * @return true if GPS feature is allowed.
     */
    public static boolean checkGPSConnection(Context context) {
        LocationManager lm;
        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // boolean NETWORK_status =
        // lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        // LogUtils.d("SNS", "gps status:"+gps);
        return gps;
    }

    /**
     * Open the activity to let user allow GPS feature in Settings app.
     * @param context from which invoke this method
     */
    public static void openGPSSettingsActivity(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Open the activity to let user allow wifi feature in Settings app.
     * @param context from which invoke this method
     */
    public static void openWIFISettingsActivity(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_WIFI_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Check if the handset has a network connection . One of wifi or mobile is
     * available will return true.
     * @param context from which invoke this method
     * @return true if network is available.
     */
    public static boolean checkNetworkConnection(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifi.isConnected() || mobile.isConnected())
            return true;
        else
            return false;
    }

    /**
     * Check if phone mobile network is available.
     * @param context from which invoke this method
     * @return true is phone mobile network is available.
     */
    public static boolean checkMobileConnection(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobile.isConnected())
            return true;
        else
            return false;
    }

    /**
     * Check if Wifi is available.
     * @param context from which invoke this method
     * @return true is wifi is available.
     */
    public static boolean checkWifiConnection(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi.isConnected())
            return true;
        else
            return false;
    }

    /**
     * Check if Wifi is available.
     * @param context from which invoke this method
     * @return true is wifi is available.
     */
    public static boolean checkandwaitWifiConnection(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        try {
            for (int i = 0; i < 3; i++) {
                if (wifi.isConnected()) {
                    Thread.sleep(500);
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "checkandwaitWifiConnection:", e);
        }
        return true;
    }

    public static boolean checkNetworkIsAvailable(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    public static boolean checkNetworkIsMobile(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo info = manager.getActiveNetworkInfo();
        NetworkInfo bluetooth = manager.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
        if (info != null && info.isAvailable()) {
            if (true) {
                LogUtils.d(TAG, "is info.isAvailable():  " + info.isAvailable());
                return false;
            } else {
                if ((wifi != null && wifi.isAvailable()) || (bluetooth != null && bluetooth.isAvailable())) {
                    return false;
                }
                LogUtils.d(TAG, "is wifi Connected:  " + wifi.isAvailable() + "   is bluetooth :  " + bluetooth.isAvailable());
            }
        }
        return true;
    }

    public static boolean checkNetworkIsAvailableNotBluetooth(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo info = manager.getActiveNetworkInfo();
        NetworkInfo gprd = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (info != null && info.isAvailable()) {
            if (true) {
                return false;
            } else {
                if ((wifi != null && wifi.isAvailable()) || (gprd != null && gprd.isAvailable())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static ArrayList<String> getConnectedIP() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        connectedIP.add(Utils.IP);
//        try {
//            BufferedReader br = new BufferedReader(new FileReader(
//                    "/proc/net/arp"));
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] splitted = line.split(" +");
//                if (splitted != null && splitted.length >= 4) {
//                    String ip = splitted[0];
//                    connectedIP.add(ip);
//                    LogUtils.d("SSS", "connect ap ip" + ip);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return connectedIP;
    }
}
