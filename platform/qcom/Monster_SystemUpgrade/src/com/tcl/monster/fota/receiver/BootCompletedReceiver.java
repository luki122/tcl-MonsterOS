package com.tcl.monster.fota.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.utils.FotaLog;
import com.tcl.monster.fota.utils.FotaUtil;

/**
 * The receiver handle ACTION_BOOT_COMPLETED broadcasts.
 */
public class BootCompletedReceiver extends BroadcastReceiver{
    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        FotaLog.d(TAG, "onReceive -> action = " + action);
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            int updateFrequency = prefs.getInt(FotaConstants.UPDATE_CHECK_PREF,
                    FotaUtil.getDefaultAutoCheckVal());
            long lastCheck = prefs.getLong(FotaConstants.LAST_AUTO_UPDATE_CHECK_PREF, 0);
            if (lastCheck > System.currentTimeMillis()){
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                        .putLong(FotaConstants.LAST_AUTO_UPDATE_CHECK_PREF, 0)
                        .apply();
                lastCheck = 0;
            }

            if (updateFrequency > 0) {
                FotaLog.d(TAG, "onReceive -> Scheduling update checks, updateFrequency = "
                        + updateFrequency + ", lastCheck = " + lastCheck);
                FotaUtil.scheduleUpdateService(context, (long)updateFrequency * 1000);
            }
        }
    }
}