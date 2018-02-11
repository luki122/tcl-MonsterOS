package com.monster.paymentsecurity.scan.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;

import java.util.List;

/**
 * Created by logic on 16-11-21.
 */

public class DetectSecurityTask extends WifiDetectTask {

    public DetectSecurityTask(Context context) {
        super(context);
    }

    @Override
    protected Integer onStart() {
        try {
            WifiInfo currWifiInfo = mWifiMgr.getConnectionInfo();
            List<ScanResult> scanList = mWifiMgr.getScanResults();
            for (ScanResult item : scanList) {
                if (currWifiInfo.getBSSID().compareTo(item.BSSID) == 0) {
                    int securityCode = mWifiDetectManager.detectSecurity(item);
                    result.setErrCode(SCAN_ERRCODE_DEFAULT);
                    return securityCode;
                }
            }
            result.setErrCode(SCAN_ERRCODE_WIFI_SECURITY_BSSID_NOT_FOUND);
            return -1;
        } catch (Exception e) {
            result.setErrCode(SCAN_ERRCODE_WIFI_SECURITY_EXCEPTION);
            return -1;
        }
    }

    @Override
    protected @ScanType  int getScanType() {
        return SCAN_TYPE_WIFI_SECURITY;
    }
}
