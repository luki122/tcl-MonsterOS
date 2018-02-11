package com.monster.paymentsecurity.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.monster.paymentsecurity.constant.Constant;
import com.monster.paymentsecurity.util.SettingUtil;

/**
 * 接收系统更新广播
 * Created by logic on 16-11-29.
 */
public class SystemUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constant.ACTION_GET_NEW_FIRMWARE.equalsIgnoreCase(intent.getAction())){
            SettingUtil.saveSystemVersion(context, intent.getStringExtra(Constant.KEY_VERSION_CODE));
        }
    }
}
