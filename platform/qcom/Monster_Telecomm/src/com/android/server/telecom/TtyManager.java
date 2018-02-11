/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.telecom;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
/* MODIFIED-BEGIN by chunzhi.sun, 2016-10-08,BUG-2831181*/
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.telecom.TelecomManager;

import com.android.internal.util.IndentingPrintWriter;
import android.widget.RemoteViews;

import java.util.List;
/* MODIFIED-END by chunzhi.sun,BUG-2831181*/

// TODO: Needed for move to system service: import com.android.internal.R;

final class TtyManager implements WiredHeadsetManager.Listener {
    private final TtyBroadcastReceiver mReceiver = new TtyBroadcastReceiver();
    private final Context mContext;
    private final WiredHeadsetManager mWiredHeadsetManager;
    private int mPreferredTtyMode = TelecomManager.TTY_MODE_OFF;
    private int mCurrentTtyMode = TelecomManager.TTY_MODE_OFF;
    protected NotificationManager mNotificationManager;

    /* MODIFIED-BEGIN by chunzhi.sun, 2016-10-08,BUG-2831181*/
    private boolean isHeadsetPlugIn = false;
    boolean isHeadsetNotificationOn = false;
    /* MODIFIED-END by chunzhi.sun,BUG-2831181*/

    static final int HEADSET_PLUGIN_NOTIFICATION = 1000;

    TtyManager(Context context, WiredHeadsetManager wiredHeadsetManager) {
        mContext = context;
        mWiredHeadsetManager = wiredHeadsetManager;
        mWiredHeadsetManager.addListener(this);

        mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mPreferredTtyMode = Settings.Secure.getInt(
                mContext.getContentResolver(),
                Settings.Secure.PREFERRED_TTY_MODE,
                TelecomManager.TTY_MODE_OFF);

        IntentFilter intentFilter = new IntentFilter(
                TelecomManager.ACTION_TTY_PREFERRED_MODE_CHANGED);
            /* MODIFIED-BEGIN by chunzhi.sun, 2016-10-08,BUG-2831181*/
            intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
            intentFilter.addAction("android.intent.action.HEADSET_NOTIFICATION_POPUP");
            intentFilter.addAction("android.intent.action.HEADSET_NOTIFICATION_MISS");
            /* MODIFIED-END by chunzhi.sun,BUG-2831181*/
        mContext.registerReceiver(mReceiver, intentFilter);

        updateCurrentTtyMode();
    }

    boolean isTtySupported() {
        boolean isEnabled = mContext.getResources().getBoolean(R.bool.tty_enabled);
        Log.v(this, "isTtySupported: %b", isEnabled);
        return isEnabled;
    }

    int getCurrentTtyMode() {
        return mCurrentTtyMode;
    }

    @Override
    public void onWiredHeadsetPluggedInChanged(boolean oldIsPluggedIn, boolean newIsPluggedIn) {
        Log.v(this, "onWiredHeadsetPluggedInChanged");
        updateCurrentTtyMode();
        /* MODIFIED-BEGIN by chunzhi.sun, 2016-10-08,BUG-2831181*/
        isHeadsetPlugIn = newIsPluggedIn;
        if (hasWavesActivity()) {
            try {
                isHeadsetNotificationOn = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.HEADSET_NOTIFICATION_SWITCH) == 1 ? true : false;
            } catch (Settings.SettingNotFoundException e) {
                Log.v(this, "SettingNotFoundException");
            }

            if (newIsPluggedIn && isHeadsetNotificationOn) {
                showHeadSetPlugin();
            } else {
                cancelHeadSetPlugin();
            }
        }
        /* MODIFIED-END by chunzhi.sun,BUG-2831181*/

        //[BUGFIX]-Mod-BEGIN by TCTNB.yubin.ying,09/30/2016,3014709,
        else {
            if (newIsPluggedIn) {
                showHeadSetPlugin();
            } else {
                cancelHeadSetPlugin();
            }
        }
        //[BUGFIX]-Mod-END by TCTNB.yubin.ying
    }

    private void updateCurrentTtyMode() {
        int newTtyMode = TelecomManager.TTY_MODE_OFF;
        if (isTtySupported() && mWiredHeadsetManager.isPluggedIn()) {
            newTtyMode = mPreferredTtyMode;
        }
        Log.v(this, "updateCurrentTtyMode, %d -> %d", mCurrentTtyMode, newTtyMode);

        if (mCurrentTtyMode != newTtyMode) {
            mCurrentTtyMode = newTtyMode;
            Intent ttyModeChanged = new Intent(TelecomManager.ACTION_CURRENT_TTY_MODE_CHANGED);
            ttyModeChanged.putExtra(TelecomManager.EXTRA_CURRENT_TTY_MODE, mCurrentTtyMode);
            mContext.sendBroadcastAsUser(ttyModeChanged, UserHandle.ALL);

            updateAudioTtyMode();
        }
    }

    private void updateAudioTtyMode() {
        String audioTtyMode;
        switch (mCurrentTtyMode) {
            case TelecomManager.TTY_MODE_FULL:
                audioTtyMode = "tty_full";
                break;
            case TelecomManager.TTY_MODE_VCO:
                audioTtyMode = "tty_vco";
                break;
            case TelecomManager.TTY_MODE_HCO:
                audioTtyMode = "tty_hco";
                break;
            case TelecomManager.TTY_MODE_OFF:
            default:
                audioTtyMode = "tty_off";
                break;
        }
        Log.v(this, "updateAudioTtyMode, %s", audioTtyMode);

        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setParameters("tty_mode=" + audioTtyMode);
    }

    void showHeadSetPlugin() {
        Log.v(TtyManager.this, "showHeadSetPlugin()...");
        android.util.Log.e("hasMaxxAudio","showHeadSetPlugin whether has maxxAudio " + hasWavesActivity());
        /* MODIFIED-BEGIN by chunzhi.sun, 2016-10-08,BUG-2831181*/
        if (hasWavesActivity()) {
            Notification notification = new Notification();
            notification.icon = R.drawable.icon_headset;
            notification.flags |= Notification.FLAG_NO_CLEAR;

            Intent intent = new Intent("com.waves.maxxaudio.action.headset_notification_not_show");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.headset_notification_layout);
            remoteViews.setOnClickPendingIntent(R.id.left_icon,  pendingIntent);

            notification.contentView = remoteViews;
            mNotificationManager.notify(HEADSET_PLUGIN_NOTIFICATION,notification);
        } else {

            String titleText = mContext.getString(
                    R.string.headset_plugin_view_title);
            String expandedText = mContext.getString(
                    R.string.headset_plugin_view_text);

            Notification notification = new Notification();
            //[BUGFIX]-Mod-BEGIN by TCTNB.yubin.ying,09/30/2016,3014709,
            notification.icon = R.drawable.ic_headset;
            //[BUGFIX]-Mod-END by TCTNB.yubin.ying
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notification.tickerText = titleText;

            // create the target network operators settings intent
            Intent intent = new Intent("android.intent.action.NO_ACTION");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pi = PendingIntent.getActivity(mContext, 0, intent, 0);

            notification.setLatestEventInfo(mContext, titleText, expandedText, pi);

            mNotificationManager.notify(HEADSET_PLUGIN_NOTIFICATION, notification);
        }
        /* MODIFIED-END by chunzhi.sun,BUG-2831181*/
    }

    void cancelHeadSetPlugin() {
        Log.v(TtyManager.this, "cancelHeadSetPlugin()...");
        mNotificationManager.cancel(HEADSET_PLUGIN_NOTIFICATION);
    }

    private final class TtyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.startSession("TBR.oR");
            try {
                String action = intent.getAction();
                Log.v(TtyManager.this, "onReceive, action: %s", action);
                /* MODIFIED-BEGIN by chunzhi.sun, 2016-10-08,BUG-2831181*/
                if (hasWavesActivity()) {
                    String changeSwitchOn = intent.getStringExtra("isHeadsetNotificationON");
                    if (null != changeSwitchOn) {
                        isHeadsetNotificationOn = Integer.parseInt(changeSwitchOn) == 1 ? true : false;
                    }
                }
                /* MODIFIED-END by chunzhi.sun,BUG-2831181*/
                if (action.equals(TelecomManager.ACTION_TTY_PREFERRED_MODE_CHANGED)) {
                    int newPreferredTtyMode = intent.getIntExtra(
                            TelecomManager.EXTRA_TTY_PREFERRED_MODE, TelecomManager.TTY_MODE_OFF);
                    if (mPreferredTtyMode != newPreferredTtyMode) {
                        mPreferredTtyMode = newPreferredTtyMode;
                        updateCurrentTtyMode();
                    }
                /* MODIFIED-BEGIN by chunzhi.sun, 2016-10-08,BUG-2831181*/
                } else  if (hasWavesActivity()) {
                    if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
                        // updateHeadSet(intent);
                        if (isHeadsetNotificationOn && isHeadsetPlugIn) {
                            showHeadSetPlugin();
                        }
                    }
                }
                if (hasWavesActivity()) {
                    if (action.equals("android.intent.action.HEADSET_NOTIFICATION_POPUP")) {
                        if (isHeadsetNotificationOn && isHeadsetPlugIn) {
                            showHeadSetPlugin();
                        }
                    } else if (action.equals("android.intent.action.HEADSET_NOTIFICATION_MISS")) {
                        if (!isHeadsetNotificationOn && isHeadsetPlugIn) {
                            cancelHeadSetPlugin();
                        }
                    }
                    /* MODIFIED-END by chunzhi.sun,BUG-2831181*/
                }

            } finally {
                Log.endSession();
            }
        }
    }

    /**
     * Dumps the state of the {@link TtyManager}.
     *
     * @param pw The {@code IndentingPrintWriter} to write the state to.
     */
    public void dump(IndentingPrintWriter pw) {
        pw.println("mCurrentTtyMode: " + mCurrentTtyMode);
    }

    /* MODIFIED-BEGIN by chunzhi.sun, 2016-10-08,BUG-2831181*/
    /*should adjuge maxxaudio service whether exist*/
    private boolean hasWavesActivity() {
        /*final Intent intent = new Intent ("com.waves.maxxservice.ACTION_START_MAXXAUDIO");
        final PackageManager pm = mContext.getPackageManager();
        final List<ResolveInfo> rList = pm.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
        if (rList.size() > 0) {
            return true;
        } else {
            return false;
        }*/
        boolean hasMaxxAudio =  mContext.getResources().getBoolean(com.android.internal.R.bool.feature_MaxxAudio_for_headset_notification);
        android.util.Log.e("hasMaxxAudio","whether has maxxAudio " + hasMaxxAudio);
        return hasMaxxAudio;
    }
    /* MODIFIED-END by chunzhi.sun,BUG-2831181*/

}
