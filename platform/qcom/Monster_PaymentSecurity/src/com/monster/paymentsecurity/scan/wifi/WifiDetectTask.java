package com.monster.paymentsecurity.scan.wifi;

import android.annotation.IntDef;
import android.content.Context;
import android.net.wifi.WifiManager;

import com.monster.paymentsecurity.scan.BaseScanTask;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.wifidetect.WifiDetectManager;

/**
 * Created by logic on 16-11-21.
 */

public abstract class WifiDetectTask extends BaseScanTask {

    @IntDef(flag = true, value = {AVILABLE, NOTAVILABLE , NOT_APPROVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface WifiDetectState{}

    public static final int AVILABLE = WifiDetectManager.NETWORK_AVILABLE;
    public static final int NOTAVILABLE = WifiDetectManager.NETWORK_NOTAVILABLE;
    public static final int NOT_APPROVE = WifiDetectManager.NETWORK_NOTAVILABLE_APPROVE;


    private final WeakReference<Context> weakContext;
    WifiManager mWifiMgr;
    WifiDetectManager mWifiDetectManager;

    WifiDetectTask(Context context){
        super();
        this.weakContext = new WeakReference<>(context);
    }

    @Override
    protected boolean onPrepare() {
        Context context = weakContext.get();
        if (context == null)
            return false;

        mWifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiDetectManager = ManagerCreatorB.getManager(WifiDetectManager.class);
        int ret =mWifiDetectManager.init();
        if (ret != 0)
            mWifiDetectManager.free();

        return  ret == 0;
    }

    @Override
    protected void onCancel() {
        mWifiDetectManager.free();
        mWifiDetectManager = null;
        mWifiMgr = null;
    }

    @Override
    protected void onFinished() {
        mWifiDetectManager.free();
        mWifiDetectManager = null;
        mWifiMgr = null;
    }

    @Override
    public @Priority int getPriority() {
        return PRIORITY_ONE;
    }

}
