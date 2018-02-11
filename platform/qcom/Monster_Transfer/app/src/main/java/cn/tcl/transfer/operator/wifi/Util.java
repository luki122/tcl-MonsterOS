/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.operator.wifi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import cn.tcl.transfer.util.LogUtils;

public class Util {

    private static final String TAG = "Util";

    WifiReceiver receiverWifi;
    Context mContext;
    WifiManager mWifiManager;

    Object waitForScanResult = new Object();
    private static Util onlyInst = null;

    public boolean cancelOpenWifi = false;
    public boolean cancelCloseWifi = false;
    public boolean cancelCloseAP = false;

    public static void destroy() {
//		onlyInst.mContext.unregisterReceiver(onlyInst.receiverWifi);
        onlyInst = null;
    }

    public static Util getInstance(Context mContext, WifiManager mWifiManager) {
        if(onlyInst == null) {
            onlyInst = new Util(mContext, mWifiManager);
        }
        return onlyInst;
    }

    public Util(Context mContext, WifiManager mWifiManager) {
        this.mContext = mContext;
        this.mWifiManager = mWifiManager;

//		receiverWifi = new WifiReceiver();
//		mContext.registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public WifiManager getWifiManager() {
        return this.mWifiManager;
    }

    public boolean closeAP() {
        if(isAPEnabled()) {
            try {
                Method method = mWifiManager.getClass().getMethod("getWifiApConfiguration");
                method.setAccessible(true);

                WifiConfiguration config = (WifiConfiguration) method.invoke(mWifiManager);

                Method method2 = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                method2.invoke(mWifiManager, config, false);
                try {
                    waitAPClosed(10 * 1000);
                    return true;
                } catch (Exception e) {
                    // TODO: handle exception
                    return false;
                }
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "close ap NoSuchMethodException = " + e.toString());
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "close ap IllegalArgumentException = " + e.toString());
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "close ap IllegalAccessException = " + e.toString());
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                Log.e(TAG, "close ap InvocationTargetException = " + e.toString());
            } catch (Exception e) {
                Log.e(TAG, "close ap Exception = " + e.toString());
            }
            return false;
        } else {
            return true;
        }
    }

    public boolean closeWIFI() {
//		if(mWifiManager.isWifiEnabled()) {
        mWifiManager.setWifiEnabled(false);
        try {
            waitWifiClosed(10 * 1000, mWifiManager);
            return true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            return false;
        }
//		} else {
//			return true;
//		}
    }

    public boolean openWifi() {
        //close wifi ap
        if(closeAP()) {
            if (!mWifiManager.isWifiEnabled()) {
                mWifiManager.setWifiEnabled(true);
                try {
                    waitWifiOpened(60 * 1000, mWifiManager);
                    return true;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    mWifiManager.setWifiEnabled(false);
                    return false;
                }
            } else {
                return true;
            }
        } else {
            LogUtils.v(TAG, "close ap failed");
            return false;
        }
    }

    public boolean openWifiNoWait() {
        boolean bRet = false;

        bRet = mWifiManager.setWifiEnabled(true);

        return bRet;
    }

    public boolean isAPEnabled() {
        try {
            Method method = mWifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(mWifiManager);

        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "check if ap enabled NoSuchMethodException = " + e.toString());
        } catch (InvocationTargetException e) {
            Log.e(TAG, "check if ap enabled InvocationTargetException = " + e.toString());
        } catch (Exception e) {
            Log.e(TAG, "check if ap enabled Exception = " + e.toString());
        }

        return false;
    }

    public List<ScanResult> rescanAllAP() {
        LogUtils.v(TAG, "Start scan ap ...");
        List<ScanResult> mAPList = null;

        mWifiManager.startScan();


        for(int i = 0; i < (10 * 1000) / 200; i ++) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mAPList = mWifiManager.getScanResults();
            if(mAPList == null || mAPList.isEmpty()) {
                continue;
            } else {
                break;
            }
        }

        return mAPList;
    }

    class WifiReceiver extends BroadcastReceiver {



        public void onReceive(Context c, Intent intent) {
            LogUtils.v(TAG, "receive broadcast ");

            synchronized (waitForScanResult) {
                waitForScanResult.notify();
            }
        }

    }



    private void waitWifiClosed(int timeout, WifiManager mWifiManager) throws Exception {
        int i = 0;
        for(i = 0; i < timeout / 200; i ++) {
            if(cancelCloseWifi) {
                throw new Exception();
            }
            Thread.sleep(200);
            if(!mWifiManager.isWifiEnabled()) {
                break;
            }
        }

        if(i == timeout / 200) {
            throw new Exception();
        }
    }

    private void waitAPClosed(int timeout) throws Exception {
        int i = 0;
        for(i = 0; i < timeout / 200; i ++) {
            if(cancelCloseAP) {
                throw new Exception();
            }
            Thread.sleep(200);
            if(!isAPEnabled()) {
                break;
            }
        }

        if(i == timeout / 200) {
            throw new Exception();
        }
    }

    private void waitWifiOpened(int timeout, WifiManager mWifiManager) throws Exception{
        int i = 0;
        for(i = 0; i < timeout / 200; i ++) {
            if(cancelOpenWifi) {
                throw new Exception();
            }
            Thread.sleep(200);
            if(mWifiManager.isWifiEnabled()) {
                break;
            }
        }

        if(i == timeout / 200) {
            throw new Exception();
        }
    }
}
