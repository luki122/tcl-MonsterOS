package com.monster.paymentsecurity.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.UserHandle;

import com.monster.paymentsecurity.tmsdk.TMSDKUpdateService;
import com.monster.paymentsecurity.constant.Constant;
import com.monster.paymentsecurity.util.SettingUtil;
import com.monster.paymentsecurity.util.Utils;


/**
 * 　监听网络状态，更新病毒
 * Created by logic on 16-11-29.
 */
public class WiFiNetworkStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {

            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            if(info == null || info.getState().equals(NetworkInfo.State.DISCONNECTED))
                return;

            if (!SettingUtil.isAutoUpdateVirusLib(context))
                return;
            Intent serviceIntent = new Intent(context, TMSDKUpdateService.class);
            context.startServiceAsUser(serviceIntent, UserHandle.CURRENT);
        }
    }
}
