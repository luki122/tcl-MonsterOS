package com.tcl.monster.fota;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;

import com.tcl.monster.fota.utils.FotaLog;

import static com.tcl.monster.fota.FotaUIPresenter.DIALOG_TYPE_DOWNLOAD_PAUSED_WARM;

/**
 * This is main application class. This class will record main activity's lifecycle.
 */
public class FotaApp extends Application implements Application.ActivityLifecycleCallbacks {
	/**
	 * This TAG can retrieve all the logs about this application.
	 */
	public static final String TAG = FotaApp.class.getSimpleName();

    /**
     * Flag for debug.
     */
    private static final boolean DEBUG = true;

    /**
     * FotaApp Instance.
     */
    private static FotaApp sInstance ;

    /**
     * Flag FotaMainActivity is in the foreground.
     */
    private boolean mMainActivityActive;

    /**
     * Flag FotaVersionDetailActivity is in the foreground.
     */
    private boolean mVersionDetailActivityActive;

    @Override
    public void onCreate() {
        FotaLog.d(TAG, "--------------------> MONSTER SYSTEM UPGRADE START <--------------------");
        super.onCreate();
        sInstance = this;
        enableStrictMode();
        dumpDefSettings();
        mMainActivityActive = false;
        mVersionDetailActivityActive = false;
        AppCrashHandler.getInstance().init(this);
        FotaUIPresenter.getInstance(this);
        registerActivityLifecycleCallbacks(this);
        registerReceiver(connectivityChangeReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    /**
     * Get FotaApp Instance.
     * @return
     */
    public static FotaApp getApp(){
    	return sInstance;
    }

    /**
     * Enable strict mode logging
     */
    private void enableStrictMode() {
        if (DEBUG) {
            final StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy
                    .Builder().detectAll().penaltyLog();
            final StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
                    .detectAll().penaltyLog();

            threadPolicyBuilder.penaltyFlashScreen();
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    /**
     * Print default settings.
     */
    private void dumpDefSettings() {
        if (DEBUG) {
            Resources r = getResources();
            FotaLog.d(TAG, "defSettings def_jrdfota_auto_check_interval = "
                    + r.getString(R.string.def_jrdfota_auto_check_interval));
            FotaLog.d(TAG, "defSettings def_jrdfota_is_auto_download_on_wifi = "
                    + r.getBoolean(R.bool.def_jrdfota_is_auto_download_on_wifi));
            FotaLog.d(TAG, "defSettings def_jrdfota_is_auto_install = "
                    + r.getBoolean(R.bool.def_jrdfota_is_auto_install));
            FotaLog.d(TAG, "defSettings def_jrdfota_root_upgrade = "
                    + r.getBoolean(R.bool.def_jrdfota_root_upgrade));
            FotaLog.d(TAG, "defSettings def_jrdfota_is_clear_status = "
                    + r.getBoolean(R.bool.def_jrdfota_is_clear_status));
            FotaLog.d(TAG, "defSettings def_jrdfota_custom_version = "
                    + r.getString(R.string.def_jrdfota_custom_version));
        }
    }

    /**
     * Monitoring network connectivity while the Fota is running.
     */
    private BroadcastReceiver connectivityChangeReceiver = new BroadcastReceiver() {
        String preNetType = "NULL";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            FotaLog.d(TAG, "connectivityChangeReceiver -> action = " + action);
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                FotaUIPresenter presenter = FotaUIPresenter.getInstance(context);
                if (info != null && info.isConnected()) {
                    FotaLog.d(TAG, "connectivityChangeReceiver -> network connected"
                            + ", type = " + info.getTypeName() + ", preType = " + preNetType);
                    boolean wifiSwitchMobile = false;
                    if (TextUtils.equals(info.getTypeName(), "MOBILE")
                            && TextUtils.equals(preNetType, "WIFI")) {
                        wifiSwitchMobile = true;
                    }
                    preNetType = info.getTypeName();
                    if (TextUtils.equals(info.getTypeName(), "WIFI") || wifiSwitchMobile) {
                        presenter.scheduleCheckNeedResume(wifiSwitchMobile);
                    } else if (TextUtils.equals(info.getTypeName(), "MOBILE")) {
                        presenter.dismissWarmDialog(DIALOG_TYPE_DOWNLOAD_PAUSED_WARM);
                    }
                } else {
                    FotaLog.d(TAG, "connectivityChangeReceiver -> network disconnected");
                    presenter.showNetworkDisconnectedWarning();
                }
            }
        }
    };

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted (Activity activity) {
        if (activity instanceof FotaMainActivity) {
            mMainActivityActive = true;
        }

        if (activity instanceof FotaVersionDetailActivity) {
            mVersionDetailActivityActive = true;
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped (Activity activity) {
        if (activity instanceof FotaMainActivity) {
            mMainActivityActive = false;
        }

        if (activity instanceof FotaVersionDetailActivity) {
            mVersionDetailActivityActive = false;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    /**
     * Flag Fota is in the foreground.
     * @return
     */
    public boolean IsForeground() {
        return mMainActivityActive || mVersionDetailActivityActive;
    }
}