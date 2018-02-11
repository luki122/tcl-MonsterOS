package com.monster.paymentsecurity.tmsdk;

import android.content.Context;
import android.content.Intent;

import com.monster.paymentsecurity.detection.AccessibilityUtils;
import com.monster.paymentsecurity.util.SettingUtil;
import com.monster.paymentsecurity.util.Utils;

import tmsdk.common.TMSBootReceiver;

/**
 * Created by logic on 16-11-24.
 */
public class PaymentSecureBootReceiver extends TMSBootReceiver {
    @Override
    public void doOnRecv(final Context context, Intent intent) {
        Utils.initTMSDK(context);
        if (SettingUtil.isPayAppDetectionEnable(context)) {
            AccessibilityUtils.setAccessibilityServiceEnabled(context);
        }
        super.doOnRecv(context, intent);
    }
}
