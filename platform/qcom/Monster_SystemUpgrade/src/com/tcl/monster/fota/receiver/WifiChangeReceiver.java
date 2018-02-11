package com.tcl.monster.fota.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

import com.tcl.monster.fota.FotaUIPresenter;
import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.utils.FotaLog;
import com.tcl.monster.fota.utils.FotaUtil;

/**
 * The receiver handle WifiManager.NETWORK_STATE_CHANGED_ACTION broadcasts.
 */
public class WifiChangeReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        FotaLog.d(TAG, "onReceive -> action = " + action);
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (info == null) {
                return;
            }
            if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                FotaLog.d(TAG, "onReceive -> Wifi disconnected!");
                FotaUIPresenter.getInstance(context).showNetWorkChangedWarning();
            } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                int updateFrequency = prefs.getInt(FotaConstants.UPDATE_CHECK_PREF,
                        FotaUtil.getDefaultAutoCheckVal());
                long lastCheck = prefs.getLong(FotaConstants.LAST_AUTO_UPDATE_CHECK_PREF, 0);
                if (lastCheck > System.currentTimeMillis()){
                    PreferenceManager.getDefaultSharedPreferences(context).edit()
                            .putLong(FotaConstants.LAST_AUTO_UPDATE_CHECK_PREF, 0)
                            .apply();
                }
                FotaLog.d(TAG, "onReceive -> Wifi connected and scheduleUpdateService"
                        + ", updateFrequency = " + updateFrequency + ", lastCheck = " + lastCheck);
                if (checkExpiredTime(context, updateFrequency)
                        || lastCheck > System.currentTimeMillis()) {
                    FotaUtil.scheduleUpdateService(context, (long) updateFrequency * 1000);
                }
            }
        }
    }

    boolean checkExpiredTime(Context context, int updateFrequency) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long lastCheck = prefs.getLong(FotaConstants.LAST_AUTO_UPDATE_CHECK_PREF, 0);
        long lCurrentMini = System.currentTimeMillis();
        FotaLog.d(TAG, "checkExpiredTime -> lastCheck = " + lastCheck + ", updateFrequency = "
                + updateFrequency * 1000 + ", currentTime = " + lCurrentMini);
        if (updateFrequency <= 0) {
            return false;
        }
        if (updateFrequency * 1000 + lastCheck <= lCurrentMini) {
            return true;
        }
        return false;
    }
}