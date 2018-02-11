/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.setupwizard.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import cn.tcl.setupwizard.utils.LogUtils; // MODIFIED by xinlei.sheng, 2016-11-18,BUG-3356295
import cn.tcl.setupwizard.utils.SystemBarHelper;
/* MODIFIED-BEGIN by xinlei.sheng, 2016-10-27,BUG-2669930*/
import mst.app.MstActivity;


public abstract class BaseActivity extends MstActivity {
/* MODIFIED-END by xinlei.sheng,BUG-2669930*/

    private static final String TAG = "BaseActivity";

    public final static String ACTION_SETUP_FINISHED = "cn.tcl.setupwizard.action.finished";

    private SetupFinishedReceiver mSetupFinishedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSetupFinishedReceiver = new SetupFinishedReceiver();
        registerReceiver(mSetupFinishedReceiver, new IntentFilter(ACTION_SETUP_FINISHED));

        // hide navigation bar
        SystemBarHelper.hideSystemBars(getWindow());
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemBarHelper.hideSystemBars(getWindow());
    }

    @Override
    /* MODIFIED-BEGIN by xinlei.sheng, 2016-11-18,BUG-3356295*/
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            SystemBarHelper.hideSystemBars(getWindow());
        }
    }

    @Override
    /* MODIFIED-END by xinlei.sheng,BUG-3356295*/
    protected void onDestroy() {
        super.onDestroy();
        if (mSetupFinishedReceiver != null) {
            unregisterReceiver(mSetupFinishedReceiver);
        }
    }

    public abstract void onSetupFinished();

    class SetupFinishedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            onSetupFinished();
        }
    }
}
