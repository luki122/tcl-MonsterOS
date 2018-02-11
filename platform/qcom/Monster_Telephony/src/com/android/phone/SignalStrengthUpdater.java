/* Copyright (C) 2016 Tcl Corporation Limited */
/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.phone;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.util.Log;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.services.telephony.TelephonyGlobals;

public class SignalStrengthUpdater extends Service {
    private static String TAG = "SignalStrengthUpdater";

    private static final String ACTION_POLL =
            "com.android.phone.SignalStrengthService.action.POLL";

    private static final int EVENT_POLL_SIGNAL_STRENGTH = 1;
    private static int POLL_REQUEST = 0;

    private AlarmManager mAlarmManager;
    private PendingIntent mPendingPollIntent;

    private Handler mHandler;

//    private final PowerManager.WakeLock mWakeLock;

    private static final long SIGNAL_STRENTCH_POLL_INTERVAL = 30 * 60 * 1000l; // 30min

    public SignalStrengthUpdater() {
        Log.d(TAG, "SignalStrengthUpdater()");

        mAlarmManager = (AlarmManager) TelephonyGlobals.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent pollIntent = new Intent(ACTION_POLL, null);
        mPendingPollIntent = PendingIntent.getBroadcast(TelephonyGlobals.getApplicationContext(), POLL_REQUEST, pollIntent, 0);

//        mWakeLock = ((PowerManager) TelephonyGlobals.getApplicationContext().getSystemService(Context.POWER_SERVICE)).newWakeLock(
//                PowerManager.PARTIAL_WAKE_LOCK, TAG);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        registerForAlarms();

        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        mHandler = new MyHandler(thread.getLooper());

        resetAlarm(SIGNAL_STRENTCH_POLL_INTERVAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }


    private void onPollSignalStrength() {
        Log.d(TAG, "onPollSignalStrength");
//        mWakeLock.acquire();
        try {
            onPollSignalStrengthUnderWakeLock();
        } finally {
//            mWakeLock.release();
        }
    }

    private void onPollSignalStrengthUnderWakeLock() {
        Phone mPhone = null;
        try {
            mPhone = PhoneFactory.getPhone(0);
        } catch (Exception e) {
            Log.d(TAG, "e: " + e);
            resetAlarm(SIGNAL_STRENTCH_POLL_INTERVAL);
            return;
        }

        SignalStrength mSignalStrength = mPhone.getSignalStrength();

        if (mSignalStrength != null) {
            final int state = mPhone.getServiceState().getState();
            final int dataState = mPhone.getServiceState().getDataRegState();

            Log.d(TAG, "onPollSignalStrengthUnderWakeLock mSignalStrength not null");

            if (((ServiceState.STATE_OUT_OF_SERVICE == state) &&
                    (ServiceState.STATE_OUT_OF_SERVICE == dataState)) ||
                    (ServiceState.STATE_POWER_OFF == state)) {
                //无信号
            } else {
                int level = mSignalStrength.getLevel();
                String name = null;
                switch (level) {
                case 0:
                    name = Settings.System.SIGNAL_STRENGTH_UNKNOWN_TIMES;
                    break;
                case 1:
                    name = Settings.System.SIGNAL_STRENGTH_POOR_TIMES;
                    break;
                case 2:
                    name = Settings.System.SIGNAL_STRENGTH_MODERATE_TIMES;
                    break;
                case 3:
                    name = Settings.System.SIGNAL_STRENGTH_GOOD_TIMES;
                    break;
                case 4:
                    name = Settings.System.SIGNAL_STRENGTH_GREAT_TIMES;
                    break;
                default:
                    return;
                }
                long count = Settings.System.getLong(TelephonyGlobals.getApplicationContext().getContentResolver(), name, 0l);
                Settings.System.putLong(TelephonyGlobals.getApplicationContext().getContentResolver(), name, count + 1);
                Log.d(TAG, "signal level: " + level + ", count: " + count);
            }
        }

        resetAlarm(SIGNAL_STRENTCH_POLL_INTERVAL);
    }

    private void registerForAlarms() {
        TelephonyGlobals.getApplicationContext().registerReceiver(
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    mHandler.obtainMessage(EVENT_POLL_SIGNAL_STRENGTH).sendToTarget();
                }
            }, new IntentFilter(ACTION_POLL));
    }

    private void resetAlarm(long interval) {
        mAlarmManager.cancel(mPendingPollIntent);
        long now = SystemClock.elapsedRealtime();
        long next = now + interval;
        mAlarmManager.set(AlarmManager.ELAPSED_REALTIME, next, mPendingPollIntent);
        Log.d(TAG, "resetAlarm");
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper l) {
            super(l);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_POLL_SIGNAL_STRENGTH:
                    Log.d(TAG, "EVENT_POLL_SIGNAL_STRENGTH");
                    onPollSignalStrength();
                    break;
            }
        }
    }
}
