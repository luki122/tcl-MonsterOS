package com.monster.cloud.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by yubai on 16-12-13.
 */
public abstract class NetworkConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isWifiConnected = false;
        boolean isGPRSConnected = false;

        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State state = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

        if (NetworkInfo.State.CONNECTED == state) {
            isWifiConnected = true;
        }

        state = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        if (NetworkInfo.State.CONNECTED == state) {
            isGPRSConnected = true;
        }

        if (!isGPRSConnected && !isWifiConnected) {
            netNotConnected();
        }
    }

    public abstract void netNotConnected();

}
