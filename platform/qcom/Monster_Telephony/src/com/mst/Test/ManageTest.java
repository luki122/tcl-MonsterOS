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

package com.mst.test;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.telephony.test.SimulatedRadioControl;
import com.android.phone.PhoneGlobals;

import android.os.AsyncResult;

public class ManageTest {
	private static final String LOG_TAG = "ManageTest";

	private PhoneGlobals mApp;

	public ManageTest(PhoneGlobals app) {
		mApp = app;
		IntentFilter testFilter = new IntentFilter(
				"com.mst.phone.test");
		mApp.registerReceiver(mTestReceiver, testFilter);

	}

	protected final TestReceiver mTestReceiver = new TestReceiver();

	protected class TestReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (!TestUtils.isInTestMode()) {
				return;
			}
			String action = intent.getAction();
			if (action.equals("com.mst.phone.test")) {
				String number = intent.getStringExtra("number");
				SimulatedRadioControl mRadioControl = mApp.getPhone()
						.getSimulatedRadioControl();
				if (mRadioControl == null) {
					Log.e("Phone",
							"SimulatedRadioControl not available, abort!");
					NotificationManager nm = (NotificationManager) mApp
							.getSystemService(Context.NOTIFICATION_SERVICE);
					Toast.makeText(mApp, "null mRadioControl!",
							Toast.LENGTH_SHORT).show();
					return;
				}
				PhoneGlobals.getInstance().getPhone().setRadioPower(true);
				int myaction = intent.getIntExtra("action", 0);
				boolean b = intent.getBooleanExtra("value", true);
				Log.i("Phone", "SimulatedRadioControl action = " + myaction);
				switch (myaction) {
				case 0:
					mRadioControl.triggerRing(number);
					break;
				case 1:
					mRadioControl.progressConnectingCallState();
					break;
				case 2:
					mRadioControl.progressConnectingToActive();
					break;
				case 3:
					mRadioControl.setAutoProgressConnectingCall(b);
					break;
				case 4:
					mRadioControl.setNextDialFailImmediately(b);
					break;
				case 5:
					mRadioControl.triggerHangupForeground();
					break;
				case 6:
					mRadioControl.triggerHangupBackground();
					break;
				case 7:
					mRadioControl.triggerHangupAll();
					break;
				case 8:
					mRadioControl.triggerIncomingSMS(number);
					break;
				}
			}
		}
	}

	private void log(String msg) {
		Log.d(LOG_TAG, msg);
	}

}
