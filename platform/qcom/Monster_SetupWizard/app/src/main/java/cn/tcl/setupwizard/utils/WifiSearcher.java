/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.setupwizard.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.service.media.MediaBrowserService;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.tcl.setupwizard.adapter.CustomerWifiInfo;

/**
 * Used to searcher and obtain a list of nearby wifi hotspot
 */
public class WifiSearcher {
    private static final String TAG = "WifiSearcher";
    private static final int WIFI_SEARCH_TIMEOUT = 20;

    private Context mContext;
    private WifiManager mWifiManager;
    private WifiScanReceiver mWifiReceiver;
    private Lock mLock;
    private Condition mCondition;
    private SearchWifiListener mSearchWifiListener;
    private boolean mIsWifiScanCompleted = false;
    private boolean mIsWifiScanning = false;

    public enum ErrorType {
        SEARCH_WIFI_TIMEOUT,
        NO_WIFI_FOUND,
    }

    //callers get scan results through the interface
    public interface SearchWifiListener {
        void onSearchWifiFailed(ErrorType errorType);
        void onSearchWifiSuccess(HashMap<String, CustomerWifiInfo> customerWifiInfos);
    }

    public WifiSearcher( Context context, SearchWifiListener listener ) {

        mContext = context;
        mSearchWifiListener = listener;

        mLock = new ReentrantLock();
        mCondition = mLock.newCondition();
        mWifiManager=(WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);

        mWifiReceiver = new WifiScanReceiver();
    }

    /**
     * call this to search a list of nearby wifi hotspot
     */
    public void search() {

        if (!mIsWifiScanning) {
            LogUtils.i(TAG, "start search thread---->");
            new Thread(new Runnable() {

                @Override
                public void run() {
                    mIsWifiScanning = true;

                    if (!mWifiManager.isWifiEnabled()) {
                        mWifiManager.setWifiEnabled(true);
                    }

                    mContext.registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

                    mWifiManager.startScan();

                    mLock.lock();

                    try {
                        mIsWifiScanCompleted = false;
                        mCondition.await(WIFI_SEARCH_TIMEOUT, TimeUnit.SECONDS);
                        if (!mIsWifiScanCompleted) {
                            mSearchWifiListener.onSearchWifiFailed(ErrorType.SEARCH_WIFI_TIMEOUT);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    mLock.unlock();

                    mContext.unregisterReceiver(mWifiReceiver);

                    mIsWifiScanning = false;

                    LogUtils.i(TAG, "search thread has completed---->");
                }
            }).start();
        }
    }

    /**
     * whether currently searching the wifi hotspot
     * @return
     */
    public boolean isSearching() {
        return mIsWifiScanning;
    }

    /**
     * stop to search the wifi hotspot
     */
    public void stopSearch() {
        mLock.lock();
        mIsWifiScanCompleted = false;
        mCondition.signalAll();
        mLock.unlock();
    }

    protected class WifiScanReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context c, Intent intent) {

            if (!WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                return;
            }
            LogUtils.i(TAG, "WifiScanReceiver----->");

            HashMap<String, CustomerWifiInfo> customerWifiInfos = new HashMap<String, CustomerWifiInfo>();
            List<ScanResult> scanResults = mWifiManager.getScanResults();
            LogUtils.i(TAG, "WifiScanReceiver: scanResults.size = " + scanResults.size()); // MODIFIED by xinlei.sheng, 2016-09-30,BUG-2669930
            WifiInfo connectedWifiInfo = mWifiManager.getConnectionInfo();
            for (ScanResult scanResult : scanResults) {
                if (scanResult != null && !TextUtils.isEmpty(scanResult.SSID)) {
                    CustomerWifiInfo customerWifiInfo = new CustomerWifiInfo();
                    customerWifiInfo.setSsid(scanResult.SSID);
                    customerWifiInfo.setBssid(scanResult.BSSID);
                    customerWifiInfo.setLevel(WifiManager
                            .calculateSignalLevel(scanResult.level, 4));
                    if (connectedWifiInfo.getSSID().equals("\"" + scanResult.SSID + "\"")) {
                        customerWifiInfo.setConnected(true);
                        customerWifiInfo.setState(3);
                        customerWifiInfo.setIp(CommonUtils.intToIp(connectedWifiInfo.getIpAddress()));
                        customerWifiInfo.setLinkSpreed(connectedWifiInfo.getLinkSpeed() + "Mbps");
                    } else {
                        customerWifiInfo.setConnected(false);
                        customerWifiInfo.setState(1);
                    }
                    customerWifiInfo.setSecurity(scanResult.capabilities);
                    customerWifiInfo.setFrequency(CommonUtils.calculateFrequency(scanResult.frequency));
                    customerWifiInfos.put(scanResult.SSID, customerWifiInfo);
                }
            }

            if (customerWifiInfos.isEmpty()) {
                mSearchWifiListener.onSearchWifiFailed(ErrorType.NO_WIFI_FOUND);
            } else {
                mSearchWifiListener.onSearchWifiSuccess(customerWifiInfos);
            }

            mLock.lock();
            mIsWifiScanCompleted = true;
            mCondition.signalAll();
            mLock.unlock();
        }
    }
}
