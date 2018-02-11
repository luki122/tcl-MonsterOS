/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.server.telecom;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.android.internal.annotations.VisibleForTesting;
import android.database.Cursor;
import com.mst.smartringer.VolumeManager;

/**
 * Controls the ringtone player.
 */
@VisibleForTesting
public class Ringer {
    private static final long[] VIBRATION_PATTERN = new long[] {
        0, // No delay before starting
        1000, // How long to vibrate
        1000, // How long to wait before vibrating again
    };

    private static final AudioAttributes VIBRATION_ATTRIBUTES = new AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build();

    /** Indicate that we want the pattern to repeat at the step which turns on vibration. */
    private static final int VIBRATION_PATTERN_REPEAT = 1;

    /**
     * Used to keep ordering of unanswered incoming calls. There can easily exist multiple incoming
     * calls and explicit ordering is useful for maintaining the proper state of the ringer.
     */

    private final SystemSettingsUtil mSystemSettingsUtil;
    private final InCallTonePlayer.Factory mPlayerFactory;
    private final AsyncRingtonePlayer mRingtonePlayer;
    private final Context mContext;
    private final Vibrator mVibrator;
    private final InCallController mInCallController;

    private InCallTonePlayer mCallWaitingPlayer;
    private RingtoneFactory mRingtoneFactory;

    /**
     * Call objects that are ringing or call-waiting. These are used only for logging purposes.
     */
    private Call mRingingCall;
    private Call mCallWaitingCall;

    /**
     * Used to track the status of {@link #mVibrator} in the case of simultaneous incoming calls.
     */
    private boolean mIsVibrating = false;

    /** Initializes the Ringer. */
    @VisibleForTesting
    public Ringer(
            InCallTonePlayer.Factory playerFactory,
            Context context,
            SystemSettingsUtil systemSettingsUtil,
            AsyncRingtonePlayer asyncRingtonePlayer,
            RingtoneFactory ringtoneFactory,
            Vibrator vibrator,
            InCallController inCallController) {

        mSystemSettingsUtil = systemSettingsUtil;
        mPlayerFactory = playerFactory;
        mContext = context;
        // We don't rely on getSystemService(Context.VIBRATOR_SERVICE) to make sure this
        // vibrator object will be isolated from others.
        mVibrator = vibrator;
        mRingtonePlayer = asyncRingtonePlayer;
        mRingtoneFactory = ringtoneFactory;
        mInCallController = inCallController;
    }

    public void startRinging(Call foregroundCall) {
        if (mSystemSettingsUtil.isTheaterModeOn(mContext)) {
            return;
        }

        if (foregroundCall == null) {
            Log.wtf(this, "startRinging called with null foreground call.");
            return;
        }

        if (mInCallController.doesConnectedDialerSupportRinging()) {
            Log.event(foregroundCall, Log.Events.SKIP_RINGING);
            return;
        }

        stopCallWaiting();

        if (!shouldRingForContact(foregroundCall.getContactUri())) {
            return;
        }

        AudioManager audioManager =
                (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        //[SOLUTION]-Add-BEGIN by TCTNB.(Yubin.Ying), 08/09/2016, SOLUTION-2504470
        PowerManager mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        boolean saverMode = mPowerManager.isPowerSaveMode();
        //[SOLUTION]-Add-END by TCTNB.(Yubin.Ying)
        if (audioManager.getStreamVolume(AudioManager.STREAM_RING) > 0) {
            mRingingCall = foregroundCall;
            Log.event(foregroundCall, Log.Events.START_RINGER);
            /* ADD-BEGIN by Dingyi for dualsim 2016/08/03 FR 2655692*/
            try {
                int phoneId = SubscriptionManager.getPhoneId(Integer.valueOf(
                        foregroundCall.getTargetPhoneAccount().getId()));
                mRingtoneFactory.setPhoneId(phoneId);
            } catch (NumberFormatException e) {
                Log.w(this,"Sub Id is not a number " + e);
            }
            /* ADD-END by Dingyi for dualsim 2016/08/03 FR 2655692*/
            // Because we wait until a contact info query to complete before processing a
            // call (for the purposes of direct-to-voicemail), the information about custom
            // ringtones should be available by the time this code executes. We can safely
            // request the custom ringtone from the call and expect it to be current.
            mRingtonePlayer.play(mRingtoneFactory, foregroundCall);

                if (isUseSmartRinger() && mVolumeManager == null) {                    
                	mVolumeManager = new VolumeManager(mContext);
                	mVolumeManager.start();
                }
        } else {
            Log.v(this, "startRingingOrCallWaiting, skipping because volume is 0");
        }
        /* MODIFY-BEGIN by Dingyi for dualsim 2016/08/03 FR 2655692*/
//        if(isTowSimCard()){
//            int phoneId = SubscriptionManager.getPhoneId(Integer.valueOf(
//                    foregroundCall.getTargetPhoneAccount().getId()));
//            if(phoneId == 0 && (Settings.System.getInt(mContext.getContentResolver(),
//                    Settings.System.VIBRATE_WHEN_RINGING, 0) != 0) && !mIsVibrating && !saverMode){
//                mVibrator.vibrate(VIBRATION_PATTERN, VIBRATION_PATTERN_REPEAT,
//                        VIBRATION_ATTRIBUTES);
//                mIsVibrating = true;
//            } else if (phoneId == 1 && (Settings.System.getInt(mContext.getContentResolver(),
//                    Settings.System.VIBRATE_WHEN_RINGING2, 0) != 0) && !mIsVibrating && !saverMode) {
//                mVibrator.vibrate(VIBRATION_PATTERN, VIBRATION_PATTERN_REPEAT,
//                        VIBRATION_ATTRIBUTES);
//                mIsVibrating = true;
//            }
//
//        } else {
             //[SOLUTION]-Add-BEGIN by TCTNB.(Yubin.Ying), 08/09/2016, SOLUTION-2504470
            if (shouldVibrate(mContext) && !mIsVibrating && !saverMode) {
            //[SOLUTION]-Add-END by TCTNB.(Yubin.Ying)
//                mVibrator.vibrate(VIBRATION_PATTERN, VIBRATION_PATTERN_REPEAT,
//                        VIBRATION_ATTRIBUTES);
                mVibrator.vibrate(VIBRATION_PATTERN, VIBRATION_PATTERN_REPEAT);
                mIsVibrating = true;
            }
//        }
        /* MODIFY-END by Dingyi for dualsim 2016/08/03 FR 2655692*/
    }

    public void startCallWaiting(Call call) {
        if (mSystemSettingsUtil.isTheaterModeOn(mContext)) {
            return;
        }

        if (mInCallController.doesConnectedDialerSupportRinging()) {
            Log.event(call, Log.Events.SKIP_RINGING);
            return;
        }

        Log.v(this, "Playing call-waiting tone.");

        stopRinging();

        if (mCallWaitingPlayer == null) {
            Log.event(call, Log.Events.START_CALL_WAITING_TONE);
            mCallWaitingCall = call;
            mCallWaitingPlayer =
                    mPlayerFactory.createPlayer(InCallTonePlayer.TONE_CALL_WAITING);
            mCallWaitingPlayer.startTone();
        }
    }

    public void stopRinging() {
        if (mRingingCall != null) {
            Log.event(mRingingCall, Log.Events.STOP_RINGER);
            mRingingCall = null;
        }

        mRingtonePlayer.stop();

        if (mIsVibrating) {
            mVibrator.cancel();
            mIsVibrating = false;
        }

        if(mVolumeManager != null) {
        	mVolumeManager.end();
        	mVolumeManager = null;
        }
    }

    public void stopCallWaiting() {
        Log.v(this, "stop call waiting.");
        if (mCallWaitingPlayer != null) {
            if (mCallWaitingCall != null) {
                Log.event(mCallWaitingCall, Log.Events.STOP_CALL_WAITING_TONE);
                mCallWaitingCall = null;
            }

            mCallWaitingPlayer.stopTone();
            mCallWaitingPlayer = null;
        }
    }

    private boolean shouldRingForContact(Uri contactUri) {
        final NotificationManager manager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        final Bundle extras = new Bundle();
        if (contactUri != null) {
            extras.putStringArray(Notification.EXTRA_PEOPLE, new String[] {contactUri.toString()});
        }
        return manager.matchesCallFilter(extras);
    }

    private boolean shouldVibrate(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerModeInternal();
        if (getVibrateWhenRinging(context)) {
            //modify by lgy for 3601759
//            return ringerMode != AudioManager.RINGER_MODE_SILENT;
            return true;
        } else {
        	//modify by lgy for 2999099
//            return ringerMode == AudioManager.RINGER_MODE_VIBRATE;
        	return false;
        }
    }

    private boolean getVibrateWhenRinging(Context context) {
        if (!mVibrator.hasVibrator()) {
            return false;
        }
        return mSystemSettingsUtil.canVibrateWhenRinging(context);
    }
    /* ADD-BEGIN by Dingyi for dualsim 2016/08/03 FR 2655692*/
    private boolean isTowSimCard(){
        int mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
        boolean hasIccCard = false;
        int count = 0;
        for (int i = 0; i < mPhoneCount; i++) {
            hasIccCard = TelephonyManager.getDefault().hasIccCard(i);
            if (hasIccCard) {
                count ++;
            }
        }
        if(count != 2) {
            return false;
        }
        return true;
    }
    /* ADD-END by Dingyi for dualsim 2016/08/03 FR 2655692*/

    private boolean isUseSmartRinger() {
   	    boolean result = false;
        Uri uri = Uri.parse("content://com.mst.phone/phone_setting");  
    	Cursor c = mContext.getContentResolver().query(uri, null, " name = 'ringermode'", null, null);
    	if(c != null) {
    		if(c.moveToFirst()) {
    			result = c.getInt(c.getColumnIndex("value")) > 0;
    		}
    		c.close();
    	}
    	Log.v(this, "isUseSmartRinger result = " + result);
        return result;        
    } 
    
    private VolumeManager mVolumeManager;
}
