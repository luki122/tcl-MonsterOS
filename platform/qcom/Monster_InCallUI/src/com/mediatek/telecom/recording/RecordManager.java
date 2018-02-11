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

package com.mediatek.telecom.recording;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.os.AsyncResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.incallui.Call;
import com.android.incallui.CallList;
import com.android.incallui.CallTimer;
import com.android.incallui.ContactInfoCache;
import com.android.incallui.InCallActivity;
import com.android.incallui.InCallApp;
import com.android.incallui.InCallCsRedialHandler;
import com.android.incallui.InCallPresenter;
import com.android.incallui.ContactInfoCache.ContactCacheEntry;
import com.android.incallui.InCallPresenter.InCallState;
import com.android.incallui.InCallPresenter.InCallStateListener;

import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import android.os.UserHandle;
import android.preference.PreferenceManager;
import com.android.incallui.R;
import com.mst.manager.MstManagerBase;
import com.mst.ui.InCallCompoundButton;

public class RecordManager extends MstManagerBase implements PhoneRecorderHandler.Listener {
	private static final String LOG_TAG = "RecordManager";

	private static RecordManager sRecordManager;
    private CallTimer mCallTimer;
    private static final long CALL_TIME_UPDATE_INTERVAL_MS = 500;

	private boolean mIsStopAutoRecord = false;
    private long mStartTime;

	public RecordManager() {
	    mCallTimer = new CallTimer(new Runnable() {
            @Override
            public void run() {
            	updateRecordTime(true);           	 
            }
        });
	}
	
	
	public void updateRecordTime(boolean isShowText) {
		InCallActivity a = InCallPresenter.getInstance().getInCallActivity();
		if (a == null || a.getCallButtonFragment() == null) {
			return;
		}
		long now = System.currentTimeMillis();
		long showTime = now - mStartTime;
		String time = DateUtils.formatElapsedTime(showTime / 1000);
		InCallCompoundButton mRecordButton = (InCallCompoundButton) a
				.getCallButtonFragment().mRecordButton;
		TextView mRecordTime = a.getCallButtonFragment().mRecordTime;
		if (isShowText) {
			mRecordButton.setChecked(true);
			mRecordButton.setDrawableEnabled(false);
			mRecordTime.setText(time);
		} else {
			mRecordButton.setChecked(false);
			mRecordButton.setDrawableEnabled(true);
			mRecordTime.setText("");
		}
		mRecordButton.setCustomText(R.string.onscreenRecordText);
	}

	public static synchronized RecordManager getInstance() {
		if (sRecordManager == null) {
			sRecordManager = new RecordManager();
		}
		return sRecordManager;
	}

	public void setUp(Context context) {
		super.setUp(context);
		PhoneRecorderHandler.getInstance().setListener(this);
	}

	public void tearDown() {
		super.tearDown();
		PhoneRecorderHandler.getInstance().setListener(null);
		//add by lgy for 3235180
	     if (PhoneRecorder.isRecording()) {
             stopRecord();
         }
        setAutoRecordStop(false);
	}

	private int getRecordMode() {
		String CALL_RECORD_TYPE = "call.record.type";
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		return sp.getInt(CALL_RECORD_TYPE, 0);
	}

	public void requestUpdateRecordState(final int state, final int customValue) {
	}

	public void onStorageFull() {
		log("onStorageFull");
		// handleStorageFull(false); // false for recording case
	}

	public void handleStorageFull(final boolean isForCheckingOrRecording) {
	}


	public void onRecordClick() {
		if (!RecorderUtils.isExternalStorageMounted(mContext)) {
			Toast.makeText(
					mContext,
					mContext.getResources().getString(
							R.string.error_sdcard_access), Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (!RecorderUtils
				.diskSpaceAvailable(PhoneRecorderHandler.PHONE_RECORD_LOW_STORAGE_THRESHOLD)) {
		      InCallActivity a = InCallPresenter.getInstance().getInCallActivity();
		      if (a != null && a.getCallButtonFragment() != null) {
		            a.getCallButtonFragment().mRecordButton.setChecked(false);
		        }
			Toast.makeText(
					mContext,
					mContext.getResources().getString(
							R.string.confirm_device_info_full),
					Toast.LENGTH_SHORT).show();
			return;
		}

		PhoneRecorder phoneRecorder = PhoneRecorder.getInstance(mContext);
		if (!phoneRecorder.isRecording()) {
			startRecord();
		} else {
			setAutoRecordStop(true);
			stopRecord();
		}
	}

	private void startRecord() {
		PhoneRecorderHandler.getInstance().startVoiceRecord(
				PhoneRecorderHandler.PHONE_RECORDING_VOICE_CALL_CUSTOM_VALUE);
		mStartTime = System.currentTimeMillis();
        mCallTimer.start(CALL_TIME_UPDATE_INTERVAL_MS);
		updateRecordTime(true);
	}

	/**
	 * Stop recording service.
	 */
	private void stopRecord() {
		PhoneRecorderHandler.getInstance().stopVoiceRecord();
		mStartTime = 0;
        mCallTimer.cancel();
        updateRecordTime(false);
	}

	public void handleRecordProc() {
		log("handleRecordProc");
		InCallState state = InCallPresenter.getInstance().getInCallState();
		PhoneRecorder phoneRecorder = PhoneRecorder.getInstance(mContext);
		if (state == InCallState.NO_CALLS) {
			log("the record custom value is "
					+ PhoneRecorderHandler.getInstance().getCustomValue());
			if (PhoneRecorderHandler.PHONE_RECORDING_VOICE_CALL_CUSTOM_VALUE == PhoneRecorderHandler
					.getInstance().getCustomValue()) {
				if (PhoneRecorder.isRecording()) {
					stopRecord();
				}
			}
			setAutoRecordStop(false);
		}
		handleAutoRecord();
	}

	public void handleAutoRecord() {
		InCallState state = InCallPresenter.getInstance().getInCallState();
		if (state == InCallState.INCALL && mCallList.getDisconnectedCall() == null && mCallList.getDisconnectingCall() == null) {
			boolean auto = false;
			if (getRecordMode() == 0) {
				auto = false;
			} else if (getRecordMode() == 1) {
				auto = true;
			} else {
				Call call = mCallList.getActiveCall();
				String number = "";
				if (call != null) {
					number = call.getNumber();
				}
				log("-handleAutoRecord c.getAddress() = " + number);
				if (RecordUtils.containRecordNumber(number)) {
					auto = true;
					RecordUtils.mRecordMap.put(number, false);
				}
			}
			PhoneRecorder phoneRecorder = PhoneRecorder.getInstance(mContext);
			log("handleAutoRecord isAutoRecord = " + auto
					+ " mIsStopAutoRecord = " + mIsStopAutoRecord
					+ " phoneRecorder.isRecording() = "
					+ phoneRecorder.isRecording());
			if (!phoneRecorder.isRecording() && auto && !mIsStopAutoRecord) {
				log("-handleAutoRecord start");
				// mContext.notifier.playRecordTone();
				onRecordClick();
	              InCallActivity a = InCallPresenter.getInstance().getInCallActivity();
	              if (a != null && a.getCallButtonFragment() != null) {
	                    a.getCallButtonFragment().updateRecordBtnState();
	                }
			}
		}
		InCallState phonestate = InCallPresenter.getInstance().getInCallState();
		if (phonestate == InCallState.NO_CALLS) {
			RecordUtils.mRecordMap.clear();
		}
	}

	public void handleHoldClick() {
		if (PhoneRecorder.isRecording()) {
			mIsStopAutoRecord = true;
			stopRecord();
		}
	}

	private void setAutoRecordStop(boolean value) {
		log("setAutoRecordStop = " + value);
		mIsStopAutoRecord = value;
	}

	private void log(String msg) {
		Log.d(LOG_TAG, msg);
	}


	@Override
	public void onStateChange(InCallState oldState, InCallState newState,
			CallList callList) {
		// TODO Auto-generated method stub
		handleRecordProc();
	}

}
