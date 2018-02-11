package com.monster.paymentsecurity.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.UserHandle;
import android.util.Log;

import com.monster.paymentsecurity.detection.PackageWatchService;


public class PackageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, final Intent intent) {
        Log.d("sandysheny","PackageReceiver receive");
        Intent serviceIntent = new Intent(context, PackageWatchService.class);
        serviceIntent.setData(Uri.parse(intent.getDataString()));
        serviceIntent.putExtra(PackageWatchService.PKG_ACTION_STATE, intent.getAction());
        context.startServiceAsUser(serviceIntent, UserHandle.CURRENT);
    }
}
