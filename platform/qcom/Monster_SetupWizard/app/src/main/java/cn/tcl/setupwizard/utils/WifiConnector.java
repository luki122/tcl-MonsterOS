/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.setupwizard.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Used to connect to a wifi hotspot
 */
public class WifiConnector {
    private static final String TAG = "WifiConnector";
    private static final int WIFI_CONNECT_TIMEOUT = 20;

    private Context mContext;
    private WifiManager mWifiManager;
    private Lock mLock;
    private Condition mCondition;
    private WifiConnectReceiver mWifiConnectReceiver;
    private WifiConnectListener mWifiConnectListener;
    private boolean mIsConnected = false;
    private boolean mIsConnecting = false;
    private int mNetworkID = -1;

    public enum SecurityMode {
        OPEN, WEP, WPA, WPA2
    }

    public interface WifiConnectListener {
        void OnWifiConnectCompleted( boolean isConnected );
    }

    public WifiConnector( Context context , WifiConnectListener listener ) {

        mContext = context;

        mLock = new ReentrantLock();
        mCondition = mLock.newCondition();
        mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);

        mWifiConnectReceiver = new WifiConnectReceiver();

        mWifiConnectListener = listener;
    }

    /**
     * call this to connect a wifi hotspot with ssid, password and SecurityMode
     * @param ssid
     * @param password
     * @param mode
     */
    public void connect( final String ssid, final String password, final int mode ) {

        new Thread(new Runnable() {

            @Override
            public void run() {
                LogUtils.i(TAG, "start connect thread---->");
                mIsConnecting = true;

                if( !mWifiManager.isWifiEnabled() ) {
                    mWifiManager.setWifiEnabled(true);
                }

                mContext.registerReceiver(mWifiConnectReceiver, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));

                if(!onConnect(ssid, password, mode)) {
                    mWifiConnectListener.OnWifiConnectCompleted(false);
                } else {
                    mWifiConnectListener.OnWifiConnectCompleted(true);
                }

                mContext.unregisterReceiver(mWifiConnectReceiver);

                mIsConnecting = false;
                LogUtils.i(TAG, "connect thread has completed---->");
            }
        }).start();
    }

    protected boolean onConnect( String ssid, String password, int mode ) {

        mNetworkID = mWifiManager.addNetwork(createWifiConfiguration(ssid, password, mode));

        mLock.lock();

        mIsConnected = false;

        if( !mWifiManager.enableNetwork(mNetworkID , true) ) {
            mLock.unlock();
            return false;
        }

        try {
            //wait for the connection result
            mCondition.await(WIFI_CONNECT_TIMEOUT, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        mLock.unlock();

        return mIsConnected;
    }

    public void disconnect() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            LogUtils.i(TAG, "current ssid: " + wifiInfo.getSSID());
            mWifiManager.disconnect();
        }
    }
    /**
     * whether currently connecting to a hotspot
     * @return
     */
    public boolean isConnecting() {
        return mIsConnecting;
    }

    /**
     * stop the current connection
     */
    public void stopConnect(){
        mLock.lock();
        mIsConnected = false;
        mCondition.signalAll();
        mLock.unlock();
    }

    private WifiConfiguration createWifiConfiguration(String SSID, String Password, int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = this.isExsits(SSID);

        if (tempConfig != null) {
            mWifiManager.removeNetwork(tempConfig.networkId);
        }

        if (Type == 1) {// WIFICIPHER_NOPASS
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        } else if (Type == 2) {// WIFICIPHER_WEP
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + Password + "\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;

        } else if (Type == 3) {// WIFICIPHER_WPA
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        return config;
    }

    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    protected class WifiConnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (!WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                return;
            }
            LogUtils.i(TAG, "onReceive------->");
            mLock.lock();

            WifiInfo info = mWifiManager.getConnectionInfo();
            if ( info.getNetworkId()==mNetworkID && info.getSupplicantState() == SupplicantState.COMPLETED ) {
                LogUtils.i(TAG, "onReceive: signallAll");
                mIsConnected = true;
                mCondition.signalAll();
            }

            mLock.unlock();
        }
    }
}
