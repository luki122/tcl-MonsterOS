/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.setupwizard.ui;

/* MODIFIED-BEGIN by xinlei.sheng, 2016-10-20,BUG-2669930*/
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
/* MODIFIED-END by xinlei.sheng,BUG-2669930*/
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import cn.tcl.setupwizard.R;
import cn.tcl.setupwizard.utils.LogUtils;
import cn.tcl.setupwizard.utils.VersionUtils;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-10-20,BUG-2669930*/
import cn.tcl.setupwizard.utils.WifiUtils;

public class StartActivity extends BaseActivity {

    public final static String TAG = "StartActivity";
    private WifiUtils mWifiUtils;
    private boolean mReceiverTag = false;

    private BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int message = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
                if (message == WifiManager.WIFI_STATE_ENABLED) {
                    LogUtils.e(TAG, "WIFI status enabled ------>");
                    mWifiUtils.startScan();
                    if (mReceiverTag) {
                        mReceiverTag = false;
                        unregisterReceiver(mWifiStateReceiver);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiUtils = new WifiUtils(this);
        mWifiUtils.openWifi();
        LogUtils.e(TAG, "onCreate: openWifi ------->");

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiStateReceiver, filter);
        mReceiverTag = true;
        /* MODIFIED-END by xinlei.sheng,BUG-2669930*/

        setContentView(R.layout.activity_start);

        String versionNumber = VersionUtils.getVersionNumber(this);
        if (!TextUtils.isEmpty(versionNumber)) {
            LogUtils.i(TAG, "current version is: " + versionNumber);
        }

        findViewById(R.id.start_btn_begin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, WifiSetActivity.class));
            }
        });
    }

    @Override
    public void onSetupFinished() {
        if (!this.isDestroyed()) {
            this.finish();
        }
    }

    /* MODIFIED-BEGIN by xinlei.sheng, 2016-10-20,BUG-2669930*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            unregisterReceiver(mWifiStateReceiver);
        }
    }
    /* MODIFIED-END by xinlei.sheng,BUG-2669930*/
}
