package com.monster.paymentsecurity.scan.wifi;


import android.content.Context;

/**
 * Created by logic on 16-11-21.
 */

public class DetectARPTask extends WifiDetectTask {

    public DetectARPTask(Context context) {
        super(context);
    }

    @Override
    protected Integer onStart() {
        result.setErrCode(SCAN_ERRCODE_DEFAULT);
        int ret = mWifiDetectManager.detectARP("mdetector");
        if (ret < 0) {
            result.setErrCode(SCAN_ERRCODE_WIFI_ARP_EXCEPTION);
        }
        return ret;
    }

    @Override
    protected @ScanType  int getScanType() {
        return SCAN_TYPE_WIFI_ARP;
    }
}
