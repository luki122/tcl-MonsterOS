/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.operator.wifi;

import java.lang.reflect.Method;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiAdmins {
    private final static String TAG = "WifiAdmin";
    private final static String SSID = "Onetouch_Connect_AP_";
    private final static String PASSWORD = "Onetouch_Connect";

    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    private Context mContext;

    public WifiAdmins(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    public String getPassword() {
        return PASSWORD;
    }

    public String getConnectedSSid() {
        return SSID;
    }

    public String getCurrentSSid() {
        ConnectivityManager manager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        State wifiState = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .getState();
        String ssid = "";
        if (wifiState == State.CONNECTED) {
            ssid = mWifiManager.getConnectionInfo().getSSID().replace("\"", "");
        }
        Log.e(TAG, "ConnectivityManager connected->" + ssid);
        return ssid;
        // return mWifiInfo.getSSID();
    }

    public void enableWifi() {
        mWifiManager.setWifiEnabled(true);
    }

    /**
     *
     * @return
     */
    public String getMacAddress() {
        String macAddress = null;
        if (mWifiInfo != null) {
            macAddress = mWifiInfo.getMacAddress();
        }
        return macAddress;
    }

    /**
     *
     * @return
     */
    public String genSSID() {
        String mac = getMacAddress();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length(); i++) {
            char c = mac.charAt(i);
            if (c != '-' && c != ':')
                sb.append(c);
        }
        return SSID + sb.toString();
    }

    /**
     *
     * @return
     */
    public WifiConfiguration getWifiConfiguration() {

        WifiConfiguration netConfig = new WifiConfiguration();

        netConfig.SSID = genSSID();
        netConfig.preSharedKey = PASSWORD;
        netConfig.allowedAuthAlgorithms
                .set(WifiConfiguration.AuthAlgorithm.OPEN);
        netConfig.hiddenSSID = true;
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        return netConfig;
    }

    /**
     * Enable wifi ap and generate BTPacket<br>
     * The BTPacket contains Wifi Ap information
     *
     * @param context
     * @return
     */
    public void enableWifiAp(Context context) {

        if (mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(false);

        WifiConfiguration netConfig = getWifiConfiguration();
        try {
            Method setWifiApMethod = mWifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, boolean.class);
            setWifiApMethod.invoke(mWifiManager, netConfig, true);

            Method getWifiApConfigurationMethod = mWifiManager.getClass()
                    .getMethod("getWifiApConfiguration");
            netConfig = (WifiConfiguration) getWifiApConfigurationMethod
                    .invoke(mWifiManager);
            Log.e(TAG, "SSID:" + netConfig.SSID + "\nPassword:"
                    + netConfig.preSharedKey + "\n");
        } catch (Exception e) {
            Log.e(TAG, "Failed to create Wifi Ap");
            e.printStackTrace();
        }
    }

    /**
     * Close wifi ap and generate BTPacket<br>
     * The BTPacket contains Wifi Ap information
     *
     * @param context
     * @return
     */

    public void closeWifiAp(Context context) {
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(false);

        WifiConfiguration netConfig = this.getWifiConfiguration();

        try {
            Method setWifiApMethod = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, boolean.class);
            setWifiApMethod.invoke(wifiManager, netConfig, false);

        } catch (Exception e) {
            Log.e(TAG, "Failed to close Wifi Ap");
        }
    }

    public boolean isWifiAPEnabled(Context context) {

        boolean value = false;
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        try {
            Method isWifiApEnabledmethod = wifiManager.getClass().getMethod(
                    "isWifiApEnabled");
            value = (Boolean) isWifiApEnabledmethod.invoke(wifiManager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public String getWifiDirectMacAddress(Context context) {

        // mac adress need to add 2(not know the reason yet)
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);

        String macAddress = wifiManager.getConnectionInfo().getMacAddress();
        String first = macAddress.substring(0, 2);
        int mac = Integer.parseInt(first, 16) + 2;
        if (mac < 255) {
            first = Integer.toHexString(mac);
            macAddress = first + macAddress.substring(2);
        }
        return macAddress;
    }

    /**
     * isEnabled
     */
    public boolean isWifiEnable(Context context) {
        return mWifiManager.isWifiEnabled();
    }

    /**
     * Try to connect to a Wifi Ap
     *
     * @param ssid
     * @param password
     */
    public void connectToAP(Context context, String ssid, String password) {

        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        } else {
            mWifiManager.disconnect();
        }

        WifiConfiguration wifiConfig = this.getWifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        //wifiConfig.preSharedKey = String.format("\"%s\"", password);

        int netId = mWifiManager.addNetwork(wifiConfig);
        mWifiManager.enableNetwork(netId, true);
        // mWifiManager.reconnect();
    }
}
