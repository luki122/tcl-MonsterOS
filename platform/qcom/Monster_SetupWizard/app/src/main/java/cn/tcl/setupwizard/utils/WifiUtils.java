/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.setupwizard.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;
import java.util.List;

import cn.tcl.setupwizard.adapter.CustomerWifiInfo;

/**
 * Wifi utils
 *
 */
public class WifiUtils {

    public static final String TAG = "WifiUtils";
    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    private List<ScanResult> mScanResults;
    private List<WifiConfiguration> mWifiConfigs;
    WifiLock mWifiLock;

    public WifiUtils(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    public void openWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    public void closeWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
    }

    public boolean isWifiEnabled() {
        return mWifiManager.isWifiEnabled();
    }

    public void acquireWifiLock() {
        mWifiLock.acquire();
    }

    public void releaseWifiLock() {
        if (mWifiLock.isHeld()) {
            mWifiLock.acquire();
        }

    }

    public void creatWifiLock() {
        mWifiLock = mWifiManager.createWifiLock("Test");
    }

    public List<WifiConfiguration> getConfigurations() {
        return mWifiConfigs;
    }

    /**
     * connect network with a WifiConfiguration
     * @param index
     */
    public boolean connectConfiguration(int index) {

        /*if (index > mWifiConfigs.size()) {
            return false;
        }

        return mWifiManager.enableNetwork(mWifiConfigs.get(index).networkId, true);*/
        return mWifiManager.enableNetwork(index, true);
    }

    public void startScan() {
        mWifiManager.startScan();
        /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
        //mScanResults = mWifiManager.getScanResults();
        //mWifiConfigs = mWifiManager.getConfiguredNetworks();
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    }

    /**
     * get the ScanResult list
     * @return
     */
    public List<ScanResult> getWifiList() {
        return mScanResults;
    }

    /**
     * get the customized wifi list
     * @return
     */
    public HashMap<String, CustomerWifiInfo> getWifiInfos() {
        HashMap<String, CustomerWifiInfo> wifiInfos = new HashMap<String, CustomerWifiInfo>();

        mWifiInfo = mWifiManager.getConnectionInfo();
        mScanResults = mWifiManager.getScanResults();
        if (mScanResults != null) {
            Log.e("getWifiInfos", "wifi list size= " + mScanResults.size() + " slect wifi "
                    + mWifiInfo.getSSID());
            for (int i = 0; i < mScanResults.size(); i++) {
                ScanResult scanResult = mScanResults.get(i);
                if (scanResult != null && !TextUtils.isEmpty(scanResult.SSID)) {
                    CustomerWifiInfo customerWifiInfo = new CustomerWifiInfo();
                    customerWifiInfo.setSsid(scanResult.SSID);
                    customerWifiInfo.setBssid(scanResult.BSSID);
                    customerWifiInfo.setLevel(WifiManager
                            .calculateSignalLevel(scanResult.level, 4));
                    if (mWifiInfo.getSSID().equals("\"" + scanResult.SSID + "\"")) {
                        Log.e("getWifiInfos", "select wifi name= " + scanResult.SSID);
                        customerWifiInfo.setConnected(true);
                        /* MODIFIED-BEGIN by xinlei.sheng, 2016-10-14,BUG-2669930*/
                        customerWifiInfo.setState(3);
                        customerWifiInfo.setIp(CommonUtils.intToIp(getIPAddress()));
                        customerWifiInfo.setLinkSpreed(mWifiInfo.getLinkSpeed() + "Mbps");
                    } else {
                        customerWifiInfo.setConnected(false);
                        customerWifiInfo.setState(1);
                        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
                    }
                    customerWifiInfo.setSecurity(scanResult.capabilities);
                    customerWifiInfo.setFrequency(CommonUtils.calculateFrequency(scanResult.frequency));
                    wifiInfos.put(scanResult.SSID, customerWifiInfo);
                }
            }
        }
        return wifiInfos;
    }

    /**
     * get a string about ScanResult information
     * @return
     */
    public StringBuilder lookUpScan() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < mScanResults.size(); i++) {
            stringBuilder.append("Index_" + (i + 1) + ":");
            stringBuilder.append((mScanResults.get(i)).toString());
            stringBuilder.append("\n");
        }
        return stringBuilder;
    }

    public String getMacAddress() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }

    public String getBSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }

    public int getIPAddress() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }

    public int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }

    /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
    public String getConnectedSsid() {
        String formatSsid = null;
        mWifiInfo = mWifiManager.getConnectionInfo();
        if (mWifiInfo != null) {
            String originSsid = mWifiInfo.getSSID();
            if (originSsid.charAt(0) == '"') {
                formatSsid = originSsid.substring(1, originSsid.length() - 1);
            }
        }
        LogUtils.i("xinlei", "connectionInfoSSID: " + formatSsid);
        return formatSsid;
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    }

    public int addNetwork(WifiConfiguration wcg) {
        return mWifiManager.addNetwork(wcg);
    }

    public void disconnectWifi() {
        int netId = getNetworkId();
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    /**
     * create a new WifiConfiguration with ssid, password and type
     * @param SSID
     * @param Password
     * @param Type
     * @return
     */
    public WifiConfiguration createWifiConfiguration(String SSID, String Password, int Type) {
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
            /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
            //config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            //config.wepTxKeyIndex = 0;
            /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
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
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
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

    public int getSsidStatus(String ssid) {
        for (WifiConfiguration config: mWifiConfigs) {
            if (config.SSID.equals(ssid)) {
                return config.status;
            }
        }
        return 1;
    }

    /**
     * get the security type
     * @param capabilities
     * @return
     */
    public static int getSecurity(String capabilities) {
        if (capabilities.contains("WEP")) {
            return 1;
        } else if (capabilities.contains("PSK")) {
            return 2;
        } else if (capabilities.contains("EAP")) {
            return 3;
        }
        return 0;
    }

    /**
     * whether wifi connected successfully
     * @param context
     * @return
     */
    /* MODIFIED-BEGIN by xinlei.sheng, 2016-09-12,BUG-2669930*/
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(wifiNetworkInfo.isConnected())
        {
            return true ;
        }

        return false ;
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
    }
}
