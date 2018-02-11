package com.monster.cloud.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.monster.cloud.constants.Constant;
import com.monster.cloud.service.SyncService;

/**
 * Created by yubai on 16-11-16.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //TODO inform the SyncService to synchronize the msg, calllog, applist
        Intent i = new Intent();
        i.setAction(Constant.AUTO_SYNC_ALL);
        context.sendBroadcast(i);
    }
}
