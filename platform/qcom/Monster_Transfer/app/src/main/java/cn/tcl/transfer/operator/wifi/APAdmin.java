/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.operator.wifi;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import cn.tcl.transfer.util.LogUtils;

public class APAdmin {
    private static final String TAG = "APAdmin";
    private static final int WIFI_AP_STATE_UNKNOWN = -1;
    private static final int WIFI_AP_STATE_DISABLING = 0;
    private static final int WIFI_AP_STATE_DISABLED = 1;
    private static final int WIFI_AP_STATE_ENABLING = 2;
    private static final int WIFI_AP_STATE_ENABLED = 3;
    private static final int WIFI_AP_STATE_FAILED = 4;
    private final String[] WIFI_STATE_TEXTSTATE = new String[] {
         "DISABLING","DISABLED","ENABLING","ENABLED","FAILED"
    };
    private WifiManager mWifiManager = null;
    private Context mContext;
    public boolean cancelOpenAP = false;

    private ArrayList<APAdminListener> listeners = new ArrayList<APAdminListener>();

    Util util;

    public APAdmin(Context mContext) {
        this.mContext = mContext;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        util = Util.getInstance(mContext, mWifiManager);
    }

    public void addAPAdminListener(APAdminListener l) {
        listeners.add(l);
    }

    public void removeAPAdminListener(APAdminListener l) {
        listeners.remove(l);
    }

    public void removeAllListener() {
        listeners.clear();
    }

    private void fireAPCreateDoneEvent(APCreateDoneEvent evt) {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onAPCreateDone(evt);
        }
    }

    private void fireAPCreatingEvent(APCreatingEvent evt) {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onAPCreating(evt);
        }
    }

    public void fireAPSSIDExistEvent(APSSIDExistEvent evt) {
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onAPSSIDExist(evt);
        }
    }

    public String startAP(String ssid) {
        LogUtils.v(TAG, "Creating ssid = " + ssid);
        fireAPCreatingEvent(new APCreatingEvent(this));
        if (mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(false);
        WifiConfiguration netConfig = getWifiConfiguration(ssid);
        try {
            String result = null;
            if (getWifiAPState() == WIFI_AP_STATE_DISABLED) {
                result = ssid;
            }
            Method setWifiApMethod = mWifiManager.getClass().getMethod(
                        "setWifiApEnabled", WifiConfiguration.class, boolean.class);
            Method getWifiApConfigurationMethod = mWifiManager.getClass()
                    .getMethod("getWifiApConfiguration");
//            WifiConfiguration oldConfig = (WifiConfiguration) getWifiApConfigurationMethod
//                    .invoke(mWifiManager);
            setWifiApMethod.invoke(mWifiManager, netConfig, true);
            netConfig = (WifiConfiguration) getWifiApConfigurationMethod
                    .invoke(mWifiManager);
            LogUtils.d(TAG, "SSID:" + netConfig.SSID + "\nPassword:"
                    + netConfig.preSharedKey + "\n");
            if (TextUtils.isEmpty(result)) {
                result = getApSSID(mWifiManager);
            }
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Failed to create Wifi Ap",e);
            return null;
        }
    }

    private int getWifiAPState() {
        int state = WIFI_AP_STATE_UNKNOWN;
        try {
            Method method2 = mWifiManager.getClass().getMethod("getWifiApState");
            state = (Integer) method2.invoke(mWifiManager);
            if (state > 10) {
                state = state - 10;
            }
        } catch (Exception e) {
            Log.e(TAG,"Exception when getWifiAPState:",e);
        }
        LogUtils.d(TAG, "getWifiAPState.state " + (state==-1?"UNKNOWN":WIFI_STATE_TEXTSTATE[state]));
        return state;
    }

    public static String getApSSID(WifiManager wifiManager) {
        try {
            Method localMethod = wifiManager.getClass().getDeclaredMethod(
                    "getWifiApConfiguration", new Class[0]);
            if (localMethod == null)
                return null;
            Object localObject1 = localMethod
                    .invoke(wifiManager, new Object[0]);
            if (localObject1 == null)
                return null;
            WifiConfiguration localWifiConfiguration = (WifiConfiguration) localObject1;
            if (localWifiConfiguration.SSID != null)
                return localWifiConfiguration.SSID;
            Field localField1 = WifiConfiguration.class
                    .getDeclaredField("mWifiApProfile");
            if (localField1 == null)
                return null;
            localField1.setAccessible(true);
            Object localObject2 = localField1.get(localWifiConfiguration);
            localField1.setAccessible(false);
            if (localObject2 == null)
                return null;
            Field localField2 = localObject2.getClass()
                    .getDeclaredField("SSID");
            localField2.setAccessible(true);
            Object localObject3 = localField2.get(localObject2);
            if (localObject3 == null)
                return null;
            localField2.setAccessible(false);
            String str = (String) localObject3;
            return str;
        } catch (Exception localException) {
        }
        return null;
    }

    public void startAP(String ssid, int module) {
        LogUtils.v(TAG, "Creating ssid = " + ssid);
        fireAPCreatingEvent(new APCreatingEvent(this));

        if (mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(false);

        WifiConfiguration netConfig = getWifiConfiguration(ssid);
        try {
            Method setWifiApMethod = mWifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, boolean.class);
            boolean resault = (boolean)setWifiApMethod.invoke(mWifiManager, netConfig, true);

            Method getWifiApConfigurationMethod = mWifiManager.getClass()
                    .getMethod("getWifiApConfiguration");
            netConfig = (WifiConfiguration) getWifiApConfigurationMethod
                    .invoke(mWifiManager);
            LogUtils.d(TAG, "SSID:" + netConfig.SSID + "\nPassword:"
                    + netConfig.preSharedKey + "\n");
        } catch (Exception e) {
            Log.e(TAG, "Failed to create Wifi Ap");
        }
    }

    /**
     * @return
     */
    public WifiConfiguration getWifiConfiguration(String SSID) {

        WifiConfiguration netConfig = new WifiConfiguration();

        netConfig.SSID = SSID;
        //netConfig.preSharedKey = PASSWORD;
        netConfig.allowedAuthAlgorithms
                .set(WifiConfiguration.AuthAlgorithm.OPEN);
        //netConfig.hiddenSSID = true;
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        return netConfig;
    }

    /**
     * Try to connect to a Wifi Ap
     *
     * @param ssid
     * @param context
     */
    public boolean connectToAP(Context context, String ssid) {

        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        } else {
            mWifiManager.disconnect();
        }

        WifiConfiguration wifiConfig = getWifiConfiguration(ssid);
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        //wifiConfig.preSharedKey = String.format("\"%s\"", password);

        int netId = mWifiManager.addNetwork(wifiConfig);
        // mWifiManager.reconnect();
        return mWifiManager.enableNetwork(netId, true);
    }

    /**
     * @return
     */
    public WifiConfiguration getWifiConfiguration(String SSID, String PASSWORD) {

        WifiConfiguration netConfig = new WifiConfiguration();

        netConfig.SSID = SSID;
        netConfig.preSharedKey = PASSWORD;
        netConfig.allowedAuthAlgorithms
                .set(WifiConfiguration.AuthAlgorithm.OPEN);
        netConfig.hiddenSSID = true;
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        return netConfig;
    }

    public void resetCancelStartAP() {
        util.cancelCloseWifi = false;
        util.cancelCloseAP = false;
        cancelOpenAP = false;
    }

    public void cancelStartAP() {
        util.cancelCloseWifi = true;
        util.cancelCloseAP = true;
        cancelOpenAP = true;
    }

    public void startAP(String ssid, String pw, int module) {

        if (mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(false);

        WifiConfiguration netConfig = getWifiConfiguration(ssid, pw);
        try {
            Method setWifiApMethod = mWifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, boolean.class);
            setWifiApMethod.invoke(mWifiManager, netConfig, true);

            Method getWifiApConfigurationMethod = mWifiManager.getClass()
                    .getMethod("getWifiApConfiguration");
            netConfig = (WifiConfiguration) getWifiApConfigurationMethod
                    .invoke(mWifiManager);
            LogUtils.d(TAG, "SSID:" + netConfig.SSID + "\nPassword:"
                    + netConfig.preSharedKey + "\n");
        } catch (Exception e) {
            Log.e(TAG, "Failed to create Wifi Ap", e);
            e.printStackTrace();
        }


    }

    private void waitAPOpened(int timeout) throws Exception {
        int i = 0;
        for (i = 0; i < timeout / 200; i++) {
            if (cancelOpenAP) {
                throw new Exception();
            }

            Thread.sleep(200);
            if (util.isAPEnabled()) {
                break;
            }
        }

        if (i == timeout / 200) {
            throw new Exception();
        }
    }

    public void closeWifiAp(Context context, String ssid) {
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(false);

        WifiConfiguration netConfig = this.getWifiConfiguration(ssid);

        try {
            Method setWifiApMethod = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, boolean.class);
            setWifiApMethod.invoke(wifiManager, netConfig, false);

        } catch (Exception e) {
            Log.e(TAG, "Failed to close Wifi Ap");
        }
    }

    public void closeWifiAp() {
        if (isWifiApEnabled()) {
            try {
                Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
                method.setAccessible(true);
                WifiConfiguration config = (WifiConfiguration) method.invoke(mWifiManager);
                Method method2 = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                method2.invoke(mWifiManager, config, false);
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "Failed to close Wifi Ap:NoSuchMethodException");
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Failed to close Wifi Ap:IllegalArgumentException");
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Failed to close Wifi Ap:IllegalAccessException");
            } catch (InvocationTargetException e) {
                Log.e(TAG, "Failed to close Wifi Ap:InvocationTargetException");
            }
        }
    }
    public boolean isWifiApEnabled() {
        try {
            Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(mWifiManager);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "get WiFiAp status fail:NoSuchMethodException");
        } catch (Exception e) {
            Log.e(TAG, "get WiFiAp status fail");
        }
        return false;
    }

    private void saveOldConfig(WifiConfiguration config) {
        SharedPreferences apSharedPreferences= mContext.getSharedPreferences("WifiAp",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = apSharedPreferences.edit();
        editor.putString("wifi_ap_last_ssid", config.SSID);
        editor.putString("wifi_ap_last_shared_key", config.preSharedKey);
        editor.putInt("wifi_ap_last_cipher_type", 0);
        if (config.allowedKeyManagement.get(1)) {
            editor.putInt("wifi_ap_last_cipher_type", 1);
        }
        editor.commit();
    }
    private WifiConfiguration getOldConfig() {
        WifiConfiguration config = new WifiConfiguration();
        SharedPreferences apSharedPreferences= mContext.getSharedPreferences("WifiAp",
                Activity.MODE_PRIVATE);
        String ssid = apSharedPreferences.getString("wifi_ap_last_ssid",null);
        String sharedkey = apSharedPreferences.getString("wifi_ap_last_shared_key",null);
        int type = apSharedPreferences.getInt("wifi_ap_last_cipher_type", 0);
        config.SSID = ssid;

        if (type == 0) {
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else {
            config.preSharedKey = sharedkey;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.hiddenSSID = true;
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        }
        return config;
    }
}
