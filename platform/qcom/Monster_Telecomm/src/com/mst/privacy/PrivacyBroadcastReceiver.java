package com.mst.privacy;

import com.android.server.telecom.TelecomBroadcastIntentProcessor;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.Log;

public class PrivacyBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "PrivacyBroadcastReceiver";

    public static final String ACTION_CLEAR_HANGUP_PRIVATE_RINGING_CALLS = "com.android.phone.intent.CLEAR_HANGUP_PRIVATE_RINGING_CALLS";

    public static final String ACTION_CLEAR_MISSED_CALLS = "com.android.phone.intent.CLEAR_MISSED_CALLS";


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        // TODO: use "if (VDBG)" here.
        Log.d(TAG, "Broadcast from Notification: " + action);

        UserHandle userHandle = intent.getParcelableExtra(TelecomBroadcastIntentProcessor.EXTRA_USERHANDLE);
        if (userHandle == null) {
            Log.d(TAG, "user handle can't be null, not processing the broadcast");
            return;
        }
        if (ACTION_CLEAR_HANGUP_PRIVATE_RINGING_CALLS
                .equals(intent.getAction())) {
            Log.i(TAG, "ACTION_CLEAR_HANGUP_PRIVATE_RINGING_CALLS");
            ManagePrivacy.getInstance().notificationMgr
                    .cancelHangupPrivateRingingCallNotification();
        } else if (ACTION_CLEAR_MISSED_CALLS.equals(intent.getAction())) {
            Log.i(TAG, "ACTION_CLEAR_MISSED_CALLS");
            ManagePrivacy.getInstance().notificationMgr
                    .clearMissedCalls(userHandle);
        }else {
            Log.w(TAG, "Received hang-up request from notification,"
                    + " but there's no call the system can hang up.");
        }
    }

    private void closeSystemDialogs(Context context) {
        Intent intent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

}