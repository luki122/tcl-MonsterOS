/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.operator.wifi;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import cn.tcl.transfer.util.LogUtils;

public class WIFIAdmin {

    private static final String TAG = "WIFIAdmin";
    private WifiManager mWifiManager = null;
    public static final int WIFI_CONNECTED = 0x01;
    public static final int WIFI_CONNECT_FAILED = 0x02;
    public static final int WIFI_CONNECTING = 0x03;
    public static final int WIFI_DISCONNECTED = 0x04;

    Context mContext = null;
    Util util = null;

    private ArrayList<WIFIAdminListener> listeners = new ArrayList<WIFIAdminListener>();

    public WIFIAdmin(Context mContext) {
        this.mContext = mContext;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        util = Util.getInstance(mContext, mWifiManager);
    }

    public void addWIFIAdminListener(WIFIAdminListener l) {
        listeners.add(l);
    }

    public void removeWIFIAdminListener(WIFIAdminListener l) {
        listeners.remove(l);
    }

    public void removeAllListener() {
        listeners.clear();
    }

    private void fireWIFIConnectDoneEvent(WIFIConnectDoneEvent evt) {
        for(int i = 0; i < listeners.size(); i ++) {
            listeners.get(i).onWIFIConnectDone(evt);
        }
    }

    private void fireWIFIConnectingEvent(WIFIConnectingEvent evt) {
        for(int i = 0; i < listeners.size(); i ++) {
            listeners.get(i).onWIFIConnecting(evt);
        }
    }

    public String getIP() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        return ip;
    }

    private String intToIp(int i) {

        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }

    public boolean connect2AP(String ssid) {
        fireWIFIConnectingEvent(new WIFIConnectingEvent(this, ssid));
        if(!util.openWifi()) {
            fireWIFIConnectDoneEvent(new WIFIConnectDoneEvent(this, false, ssid));
            return false;
        }

        WifiConfiguration apConfig = new WifiConfiguration();

        WifiConfiguration tempConfig = this.IsExsits(ssid);
        if (tempConfig != null) {
            mWifiManager.disableNetwork(tempConfig.networkId);
            mWifiManager.disconnect();
            try {
                waitWifiDisonnect(10 * 1000);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "waitWifiDisonnect time out");
                fireWIFIConnectDoneEvent(new WIFIConnectDoneEvent(this, false, ssid));
                return false;
            }
        }

        apConfig.allowedAuthAlgorithms.clear();
        apConfig.allowedGroupCiphers.clear();
        apConfig.allowedKeyManagement.clear();
        apConfig.allowedPairwiseCiphers.clear();
        apConfig.allowedProtocols.clear();
        apConfig.SSID = "\"" + ssid + "\"";

        apConfig.hiddenSSID = true;
        apConfig.allowedAuthAlgorithms
                .set(WifiConfiguration.AuthAlgorithm.OPEN);
        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        apConfig.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.TKIP);

        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        apConfig.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.CCMP);
        apConfig.status = WifiConfiguration.Status.ENABLED;

        int wcgID = mWifiManager.addNetwork(apConfig);
        mWifiManager.enableNetwork(wcgID, true);

        try {
            waitWifiConnected(5 * 60 * 1000, ssid);
            fireWIFIConnectDoneEvent(new WIFIConnectDoneEvent(this, true, ssid));
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            fireWIFIConnectDoneEvent(new WIFIConnectDoneEvent(this, false, ssid));
            return false;
        }
    }

    public boolean connect2AP(String ssid, String pw) {
        LogUtils.v(TAG, "STEP 1");
        fireWIFIConnectingEvent(new WIFIConnectingEvent(this, ssid));
        if(!util.openWifi()) {
            fireWIFIConnectDoneEvent(new WIFIConnectDoneEvent(this, false, ssid));
            return false;
        }

        WifiConfiguration apConfig = new WifiConfiguration();

        WifiConfiguration tempConfig = this.IsExsits(ssid);
        LogUtils.v(TAG, "STEP 2");
        if (tempConfig != null) {
            LogUtils.v(TAG, "STEP 2.1");
            mWifiManager.disableNetwork(tempConfig.networkId);
            mWifiManager.disconnect();
            try {
                waitWifiDisonnect(10 * 1000);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                fireWIFIConnectDoneEvent(new WIFIConnectDoneEvent(this, false, ssid));
                Log.e(TAG, "waitWifiDisonnect time out");
                return false;
            }
        }
        LogUtils.v(TAG, "STEP 3");

        apConfig.allowedAuthAlgorithms.clear();
        apConfig.allowedGroupCiphers.clear();
        apConfig.allowedKeyManagement.clear();
        apConfig.allowedPairwiseCiphers.clear();
        apConfig.allowedProtocols.clear();
        apConfig.SSID = "\"" + ssid + "\"";
        apConfig.preSharedKey = "\"" + pw + "\"";

        apConfig.hiddenSSID = true;
        apConfig.allowedAuthAlgorithms
                .set(WifiConfiguration.AuthAlgorithm.OPEN);
        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        apConfig.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.TKIP);

        apConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        apConfig.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.CCMP);
        apConfig.status = WifiConfiguration.Status.ENABLED;

        int wcgID = mWifiManager.addNetwork(apConfig);
        mWifiManager.enableNetwork(wcgID, true);
        LogUtils.v(TAG, "STEP 4");
        try {
            waitWifiConnected(20 * 1000, apConfig.SSID);
            fireWIFIConnectDoneEvent(new WIFIConnectDoneEvent(this, true, ssid));
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "EXCEPTION " + e.toString());
            fireWIFIConnectDoneEvent(new WIFIConnectDoneEvent(this, false, ssid));
            return false;
        }
    }

    private WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    public int isWifiContected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifiNetworkInfo.getDetailedState() == DetailedState.OBTAINING_IPADDR
                || wifiNetworkInfo.getDetailedState() == DetailedState.CONNECTING) {
            return WIFI_CONNECTING;
        } else if (wifiNetworkInfo.getDetailedState() == DetailedState.CONNECTED) {
            return WIFI_CONNECTED;
        } else if (wifiNetworkInfo.getDetailedState() == DetailedState.DISCONNECTED) {
            return WIFI_DISCONNECTED;
        } else {
            return WIFI_CONNECT_FAILED;
        }
    }

    private void waitWifiDisonnect(int timeout) throws Exception {
        int i = 0;
        for(i = 0; i < timeout / 200; i ++) {
            Thread.sleep(200);
            if(isWifiContected(mContext) == WIFI_DISCONNECTED) {
                break;
            }
        }

        if(i == timeout / 200) {
            throw new Exception();
        }
    }

    private void waitWifiConnected(int timeout, String ssid) throws Exception {
        int i = 0;
        for(i = 0; i < timeout / 200; i ++) {
            Thread.sleep(200);
            if(isWifiContected(mContext) == WIFI_CONNECTED) {
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                LogUtils.v(TAG, "connected to " + wifiInfo.getSSID());
                if(!wifiInfo.getSSID().startsWith("\"")) {
                    if(("\"" + wifiInfo.getSSID() + "\"").equals(ssid)) {
                        break;
                    }
                } else {
                    if(wifiInfo.getSSID().equals(ssid)) {
                        break;
                    }
                }

            }
        }

        if(i == timeout / 200) {
            throw new Exception();
        }
    }

    public static String intToString(int a) {
        StringBuffer sb = new StringBuffer();
        int b = (a >> 0) & 0xff;
        sb.append(b + ".");
        b = (a >> 8) & 0xff;
        sb.append(b + ".");
        b = (a >> 16) & 0xff;
        sb.append(b + ".");
        b = (a >> 24) & 0xff;
        sb.append(b);
        return sb.toString();
    }


}
