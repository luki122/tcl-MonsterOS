/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 08/04/2016|     Fuqiang.Song     |       2670355        |For Russian Beel- */
/*           |                      |                      |ine, Notify user  */
/*           |                      |                      |when preferred n- */
/*           |                      |                      |etwork mode is L- */
/*           |                      |                      |te Only.          */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/
package com.android.phone;

import com.android.internal.telephony.Phone;

import android.os.SystemProperties;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionManager;
import android.util.TctLog;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;

import com.android.internal.telephony.SubscriptionController;


public class DisabledLteOnlyReceiver extends BroadcastReceiver {
    private final String TAG = "DisabledLteOnlyReceiver";
    private static Context mContext;
    public static final String ACTION_DISABLE_LTEONLYMODE = "intent.notification.disabledlteonly";
    public static final String UPDATE_NETWORK_MODE_AS_AUTO = "update.network.mode.as.auto";

    static final int preferredNetworkMode = SystemProperties.getInt("ro.telephony.default_network", Phone.PREFERRED_NT_MODE);

    private Phone mPhone;
    private MyHandler mHandler;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        mContext = context;
        if (null != intent) {
            String action = intent.getAction();
            if (ACTION_DISABLE_LTEONLYMODE.equals(action)) {
                 TctLog.i(TAG,"Received Intent: ACTION_DISABLE_LTEONLYMODE,then set network mode as auto.");
                 int mDds = SubscriptionController.getInstance().getDefaultDataSubId();
                 TctLog.i(TAG,"mDds = "+mDds);
                 mPhone=PhoneGlobals.getPhone(mDds);
                 mHandler = new MyHandler();
                 mPhone.setPreferredNetworkType(
                         preferredNetworkMode,
                            mHandler.obtainMessage(MyHandler.MESSAGE_SET_AUTO_NETWORK_MODE));
            }
        }
    }

    private class MyHandler extends Handler {
        static final int MESSAGE_SET_AUTO_NETWORK_MODE = 0;

        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg != null) {
                switch (msg.what) {
                case MESSAGE_SET_AUTO_NETWORK_MODE:
                    handleSetAutoNetworkModeResponse(msg);
                    break;
                default:
                    return;
                }
            }
        }

        private void handleSetAutoNetworkModeResponse(Message msg) {
            AsyncResult ar = (AsyncResult) msg.obj;

            if (ar.exception == null) {
                TctLog.i(TAG,"set auto network mode ,successful.");
                final int phoneSubId = mPhone.getSubId();
                final int phoneId = mPhone.getPhoneId();
                TctLog.i(TAG," phoneSubId = " + phoneSubId + " ,phoneId = " + phoneId);
                android.provider.Settings.Global
                        .putInt(mPhone.getContext().getContentResolver(),
                                android.provider.Settings.Global.PREFERRED_NETWORK_MODE + phoneSubId,
                                preferredNetworkMode);
                TelephonyManager.putIntAtIndex(mPhone.getContext().getContentResolver(),
                        android.provider.Settings.Global.PREFERRED_NETWORK_MODE, phoneId,
                        preferredNetworkMode);

                 Intent intent = new Intent(UPDATE_NETWORK_MODE_AS_AUTO);
                 mContext.sendBroadcast(intent);

                final PhoneGlobals app = PhoneGlobals.getInstance();
                app.notificationMgr.updateLteOnlyIcon(false);
            }else{
                TctLog.i(TAG,"set auto network mode ,fail. ar.exception " + ar.exception);
            }
        }
    };
}
