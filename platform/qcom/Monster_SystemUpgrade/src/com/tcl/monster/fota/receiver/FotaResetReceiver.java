
package com.tcl.monster.fota.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.tcl.monster.fota.utils.FotaLog;
import com.tcl.monster.fota.utils.FotaPref;
import com.tcl.monster.fota.utils.FotaUtil;


public class FotaResetReceiver extends BroadcastReceiver{
    private final String TAG = "FotaResetReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String action = intent.getAction();

        int count = this.getResultCode() + 1;
        this.setResultCode(count);

        FotaLog.i(TAG, "Receive " + action);

        FotaUtil.clearUpdateFolder();
        FotaPref.getInstance(context).clear();
        FotaUtil.clearLogFolder();

        final SharedPreferences defaultValueSp = context.getSharedPreferences(
                PreferenceManager.KEY_HAS_SET_DEFAULT_VALUES, Context.MODE_PRIVATE);
        defaultValueSp.edit().clear().apply();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().clear().apply();
    }
}