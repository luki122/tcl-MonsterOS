package com.tcl.monster.fota.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tcl.monster.fota.FotaMainActivity;
import com.tcl.monster.fota.FotaUIPresenter;
import com.tcl.monster.fota.FotaVersionDetailActivity;
import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.misc.State;
import com.tcl.monster.fota.utils.FotaLog;
import com.tcl.monster.fota.utils.FotaPref;

/**
 * The receiver handle notification click event.
 */
public class NotificationClickReceiver extends BroadcastReceiver{
    private static final String TAG = "NotificationClickReceiver";

    // Notification Action
    public static final String ACTION_UPDATE_NOTIFICATION = "com.tcl.fota.action.UPDATE_NOTIFICATION";
    public static final String ACTION_DOWNLOAD_NOTIFICATION = "com.tcl.fota.action.DOWNLOAD_NOTIFICATION";

    // Notification Postpone time: 24 hours
    public static final long NOTIFICATION_POSTPONE_TIME = 86400000;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        FotaLog.d(TAG, "onReceive -> action = " + action);

        Intent updateIntent = new Intent();
        updateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (action.equals(ACTION_UPDATE_NOTIFICATION)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            long lastCheck = prefs.getLong(FotaConstants.LAST_UPDATE_NOTIFICATION_TIME, 0);
            FotaLog.d(TAG, "onReceive -> last_update_notification_time = " + lastCheck
                    + ", currentTime = " + System.currentTimeMillis());
            // Notification over 24 hours, check again.
            if (lastCheck + NOTIFICATION_POSTPONE_TIME < System.currentTimeMillis()) {
                updateIntent.setClass(context, FotaMainActivity.class);
            } else {
                //Notification in 24 hours, no download task active, go to FotaVersionDetailActivity.
                State state = FotaUIPresenter.getInstance(context).getCurrentTaskState();
                FotaLog.d(TAG, "onReceive -> getCurrentTaskState = " + state);
                if (state == State.IDLE || state == State.CHECKED) {
                    updateIntent.setClass(context, FotaVersionDetailActivity.class);
                    String downloadId = FotaPref.getInstance(context)
                            .getString(FotaConstants.DOWNLOAD_ID, "");
                    updateIntent.putExtra(FotaVersionDetailActivity.EXTRA_ID, downloadId);
                } else {
                    //Notification in 24 hours, has a download task active, go to FotaMainActivity.
                    updateIntent.setClass(context, FotaMainActivity.class);
                }
            }
        } else if (action.equals(ACTION_DOWNLOAD_NOTIFICATION)) {
            updateIntent.setClass(context, FotaMainActivity.class);
        } else {
            return;
        }
        context.startActivity(updateIntent);
    }
}