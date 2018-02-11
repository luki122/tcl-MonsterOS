package com.monster.paymentsecurity.scan.wifi;

import android.content.Context;


/**
 * Created by logic on 16-11-21.
 */
public class WifiStateTask extends WifiDetectTask {

    public WifiStateTask(Context context) {
        super(context);
    }

    @Override
    protected Integer onStart() {
        result.setErrCode(SCAN_ERRCODE_DEFAULT);
        return mWifiDetectManager.detectNetworkState();
    }

    @Override
    protected @ScanType int getScanType() {
        return SCAN_TYPE_WIFI_STATE;
    }

}
