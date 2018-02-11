/*
 * Copyright (c) 2013 The Linux Foundation. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 * IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mst.manager;

import com.android.incallui.Call;
import com.android.incallui.CallList;
import com.android.incallui.InCallApp;
import com.android.incallui.InCallPresenter;
import com.android.incallui.InCallPresenter.InCallState;

import android.telecom.DisconnectCause;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.provider.Settings;

public class VibratorManager extends MstManagerBase {
	private static final String LOG_TAG = "VibratorManager";

	private static VibratorManager sVibratorManager;

	private InCallState mPreviousCallState = InCallState.NO_CALLS;

	private AudioManager mAudioManager;
	private Vibrator mVibrator;
	private final static int vduration = 300;

	public VibratorManager() {
		mAudioManager = (AudioManager) InCallApp.getInstance()
				.getSystemService(Context.AUDIO_SERVICE);
		mVibrator = (Vibrator) InCallApp.getInstance().getSystemService(
				Context.VIBRATOR_SERVICE);

	}

	public static synchronized VibratorManager getInstance() {
		if (sVibratorManager == null) {
			sVibratorManager = new VibratorManager();
		}
		return sVibratorManager;
	}
	
	   public void tearDown() {
	        super.tearDown();
	        mPreviousCallState = InCallState.NO_CALLS;
	    }

	@Override
	public void onStateChange(InCallState oldState, InCallState newState,
			CallList callList) {
		// TODO Auto-generated method stub
		InCallState state = InCallPresenter.getInstance().getInCallState();

		log("onCallListChange state = " + state + " mPreviousCallState = " + mPreviousCallState);
		if (state == InCallState.INCALL
				&& mPreviousCallState == InCallState.OUTGOING
				&& callList.hasLiveCall()) {
			vibrate();
		}
		mPreviousCallState = InCallPresenter.getInstance().getInCallState();
	}

	@Override
	public void onDisconnect(Call call) {
		DisconnectCause cause = call.getDisconnectCause();
		log("onDisconnect cause = " + cause + " mPreviousCallState = " + mPreviousCallState) ;
//		if (cause.getCode() != DisconnectCause.MISSED
//				&& cause.getCode() != DisconnectCause.LOCAL
//				&& cause.getCode() != DisconnectCause.REJECTED
	     if((mPreviousCallState != InCallState.WAITING_FOR_ACCOUNT)
				&& (mPreviousCallState != InCallState.OUTGOING)
				&& ((mPreviousCallState != InCallState.PENDING_OUTGOING))) {
	         if(cause.getCode() != DisconnectCause.CANCELED) {
	             vibrate();
	         }
		}
	}

	private boolean mIsSilent;

	public boolean isSilentMode() {
		return mIsSilent;
	}

	public void vibrate() {
		log("vibrate");
		if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
			mIsSilent = true;
			boolean canVibrate = Settings.System.getInt(
					mContext.getContentResolver(),
					Settings.System.VIBRATE_IN_SILENT, 1) == 1;
			if (canVibrate) {
				mIsSilent = false;
				mVibrator.vibrate(vduration);
			} else {
				mIsSilent = true;
				Settings.System.putInt(mContext.getContentResolver(),
						Settings.System.VIBRATE_IN_SILENT, 1);
				mVibrator.vibrate(vduration);
				mHandler.postDelayed(new Runnable() {

					public void run() {
						log("restore ringer mode");
						Settings.System.putInt(mContext.getContentResolver(),
								Settings.System.VIBRATE_IN_SILENT, 0);
						mIsSilent = false;
					}
				}, vduration);

			}
		} else {
			mIsSilent = false;
			mVibrator.vibrate(vduration);
		}
	}

	private void log(String msg) {
		Log.d(LOG_TAG, msg);
	}

}
