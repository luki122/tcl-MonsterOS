package com.monster.interception.util;

import android.net.Uri;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class InterceptionUtils {

    public static final int SLIDER_BTN_POSITION_DELETE = 1;

    public static final int SUB1 = 0;  // for DSDS product of slot one
    public static final int SUB2 = 1;  // for DSDS product of slot two

    public static boolean isNoneDigit(String number) {
        boolean isDigit = false;
        for (int i = 0; i < number.length(); i++) {
            if (Character.isDigit(number.charAt(i))) {
                isDigit = true;
            }
        }
        if (number.indexOf('+', 1) > 0) {
            isDigit = false;
        }
        if (!isDigit) {
            return true;
        }
        return false;
    }

    public static boolean isMultiSimEnabledMms() {
        //any diffrence with (TelephonyManager.getDefault().getPhoneCount()) > 1 ?
        return TelephonyManager.getDefault().isMultiSimEnabled();
    }

    public static boolean isMsimIccCardActive() {
        if (isMultiSimEnabledMms()) {
            if (isIccCardActivated(SUB1) && isIccCardActivated(SUB2)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return whether the card is activated according to Subscription
     * used for DSDS
     */
    public static boolean isIccCardActivated(int subscription) {
        TelephonyManager tm = TelephonyManager.getDefault();
        return (tm.getSimState(subscription) != TelephonyManager.SIM_STATE_ABSENT)
                    && (tm.getSimState(subscription) != TelephonyManager.SIM_STATE_UNKNOWN);
    }

    public static boolean isTwoSimCardActived() {
        boolean slot1 = isIccCardEnabled(0);
        boolean slot2 = isIccCardEnabled(1);
        boolean twoSimCardActived = slot1 && slot2;
        return twoSimCardActived;
    }

    public static boolean isIccCardEnabled(int subscription) {
        TelephonyManager tm = TelephonyManager.getDefault();
        int simState = tm.getSimState(subscription);
        /*return (simState != TelephonyManager.SIM_STATE_ABSENT)
                    && (simState != TelephonyManager.SIM_STATE_UNKNOWN)
                    && (simState != TelephonyManager.SIM_STATE_NOT_READY)
                    && (simState != TelephonyManager.SIM_STATE_PERM_DISABLED)
                    && (simState != TelephonyManager.SIM_STATE_CARD_IO_ERROR);*/
        return (simState != TelephonyManager.SIM_STATE_ABSENT)
            && (simState != TelephonyManager.SIM_STATE_UNKNOWN)
            && (simState != TelephonyManager.SIM_STATE_NOT_READY);
    }
}