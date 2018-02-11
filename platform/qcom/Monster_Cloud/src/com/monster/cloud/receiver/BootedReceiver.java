package com.monster.cloud.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;

import com.monster.cloud.service.SyncService;

/**
 * Created by yubai on 16-11-14.
 */
public class BootedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startServiceAsUser(new Intent(context, SyncService.class), UserHandle.CURRENT);
    }
}
