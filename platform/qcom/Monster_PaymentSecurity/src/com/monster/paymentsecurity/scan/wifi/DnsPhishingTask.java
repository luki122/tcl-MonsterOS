package com.monster.paymentsecurity.scan.wifi;

import android.content.Context;

import com.monster.paymentsecurity.scan.Result;

import tmsdk.bg.module.wifidetect.IWifiDetectListener;

/**
 * Created by logic on 16-11-21.
 */

public class DnsPhishingTask extends WifiDetectTask {

    public DnsPhishingTask(Context context) {
        super(context);
    }

    @Override
    protected Integer onStart() {
        mWifiDetectManager.detectDnsAndPhishing(new MyIWifiDetectListener(result));
        result.setErrCode(SCAN_ERRCODE_DEFAULT);
        return (Integer)result.getRawData();
    }

    private static final class MyIWifiDetectListener implements IWifiDetectListener{
        private final Result result;
        private MyIWifiDetectListener(Result re){
            this.result =re;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void onResult(int i) {
            result.setRawData(i);
            result.setErrCode(SCAN_ERRCODE_DEFAULT);
        }
    }

    @Override
    protected @ScanType int getScanType() {
        return SCAN_TYPE_WIFI_DNS;
    }

}
