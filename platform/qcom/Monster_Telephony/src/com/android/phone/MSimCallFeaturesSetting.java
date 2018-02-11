/*
 * Copyright (C) 2008 The Android Open Source Project
 * Copyright (c) 2011-2013 The Linux Foundation. All rights reserved.
 *
 * Not a Contribution.
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

import android.app.ActionBar;
import android.app.Activity;
import mst.app.dialog.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.AsyncResult;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.PhoneFactory;

import android.os.Build;
import android.telephony.SubscriptionManager;
import mst.preference.*;

/**
 * Top level "Call settings" UI; see res/xml/call_feature_setting.xml
 * 
 * This preference screen is the root of the "MSim Call settings" hierarchy
 * available from the Phone app; the settings here let you control various
 * features related to phone calls (including voicemail settings, SIP settings,
 * the "Respond via SMS" feature, and others.) It's used only on voice-capable
 * phone devices.
 * 
 * Note that this activity is part of the package com.android.phone, even though
 * you reach it from the "Phone" app (i.e. DialtactsActivity) which is from the
 * package com.android.contacts.
 * 
 * For the "MSim Mobile network settings" screen under the main Settings app,
 * See {@link MSimMobileNetworkSettings}.
 * 
 * @see com.android.phone.MSimMobileNetworkSettings
 */
public class MSimCallFeaturesSetting extends PreferenceActivity implements Preference.OnPreferenceChangeListener  {
	private static final String LOG_TAG = "MSimCallFeaturesSetting";
	private static final boolean DBG = true;

	// Information about logical "up" Activity
	private static final String UP_ACTIVITY_PACKAGE = "com.android.dialer";
	private static final String UP_ACTIVITY_CLASS = "com.android.dialer.DialtactsActivity";

	private Phone mPhone;
	private boolean mForeground;
	private AudioManager mAudioManager;

	private Preference[] mCallForward, mCallWaiting, mSimCategory;
    private SwitchPreference mOverTurn, mRinger, mTouch;
    private SharedPreferences mSp;

	private int mNumPhones;
	private TelephonyManager mTelephonyManager;

	/*
	 * Activity class methods
	 */

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if (DBG)
			log("onCreate(). Intent: " + getIntent());

		mPhone = PhoneGlobals.getPhone();

		addPreferencesFromResource(R.xml.msim_call_feature_setting);

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		// get buttons
		PreferenceScreen prefSet = getPreferenceScreen();

		final ContentResolver contentResolver = getContentResolver();

//		ActionBar actionBar = getActionBar();
//		if (actionBar != null) {
//			// android.R.id.home will be triggered in onOptionsItemSelected()
//			actionBar.setDisplayHomeAsUpEnabled(true);
//		}

		mTelephonyManager = TelephonyManager.from(this);
		mNumPhones = mTelephonyManager.getPhoneCount();
		mCallForward = new Preference[mNumPhones];
		mCallWaiting = new Preference[mNumPhones];
		mSimCategory = new Preference[mNumPhones];

		for (int i = 0; i < mNumPhones; i++) {
			log("init preference  i =" + i);
			mSimCategory[i] = findPreference("sim_category_key" + i);
			
			mCallForward[i] = findPreference("button_cf_expand_key" + i);	
			int subid = PhoneFactory.getPhone(i).getSubId();
		     mCallForward[i].getIntent().putExtra(SubscriptionInfoHelper.SUB_ID_EXTRA, subid);
//			SubscriptionManager.putPhoneIdAndSubIdExtra(
//					mCallForward[i].getIntent(), i, subid);
			int phonetype = PhoneFactory.getPhone(i).getPhoneType();
			log("init preference  PhoneFactory.getPhone("+ i +") =" + PhoneFactory.getPhone(i) );

			Class<?> callforwardClass = phonetype == PhoneConstants.PHONE_TYPE_CDMA ? CdmaCallForwardOptions.class
					: GsmUmtsCallForwardOptions.class;
			mCallForward[i].getIntent().setClass(this, callforwardClass);

			mCallWaiting[i] = findPreference("button_more_expand_key" + i);
//			SubscriptionManager.putPhoneIdAndSubIdExtra(
//					mCallWaiting[i].getIntent(), i, subid);
			mCallWaiting[i].getIntent().putExtra(SubscriptionInfoHelper.SUB_ID_EXTRA, subid);
			Class<?> callWaitingClass = phonetype == PhoneConstants.PHONE_TYPE_CDMA ? CdmaCallOptions.class
					: GsmUmtsAdditionalCallOptions.class;
			mCallWaiting[i].getIntent().setClass(this, callWaitingClass);
		}
		
		initSwtichs();

	}

	@Override
	protected void onResume() {
		super.onResume();
		mForeground = true;

		if (isAirplaneModeOn()) {
			PreferenceScreen screen = getPreferenceScreen();
			int count = screen.getPreferenceCount();
			for (int i = 0; i < count; ++i) {
				Preference pref = screen.getPreference(i);
				pref.setEnabled(false);
			}
			return;
		}

		updateUiState();
	}

	private boolean isAirplaneModeOn() {
		return Settings.System.getInt(getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}

	private static void log(String msg) {
		Log.d(LOG_TAG, msg);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int itemId = item.getItemId();
		if (itemId == android.R.id.home) { // See
											// ActionBar#setDisplayHomeAsUpEnabled()
			Intent intent = new Intent();
			intent.setClassName(UP_ACTIVITY_PACKAGE, UP_ACTIVITY_CLASS);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private final int EVENT_SIM_STATE_CHANGED = 1002;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			AsyncResult ar;

			switch (msg.what) {
			case EVENT_SIM_STATE_CHANGED:
				Log.d(LOG_TAG, "EVENT_SIM_STATE_CHANGED");
				updateUiState();
				break;
			default:
				Log.w(LOG_TAG, "Unknown Event " + msg.what);
				break;
			}
		}
	};

	private void updateUiState() {
		Log.d(LOG_TAG, "updateUiState");

		for (int i = 0; i < mNumPhones; i++) {
			boolean isCardEnable = mTelephonyManager.getSimState(i) != TelephonyManager.SIM_STATE_ABSENT;
			mSimCategory[i].setEnabled(isCardEnable);
		}
	}	
	
	private void initSwtichs() {
//		mSp = getSharedPreferences("com.android.phone.settings",
//				Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);
		mOverTurn = (SwitchPreference) findPreference("overturn_to_mute");
		mRinger = (SwitchPreference) findPreference("smart_ringer");
		mTouch = (SwitchPreference) findPreference("anti_touch");

		Uri uri = Uri.parse("content://com.mst.phone/phone_setting");
		Cursor c = getContentResolver().query(uri, null, null, null, null);
		if (c != null) {
			while (c.moveToNext()) {
				String name = c.getString(c.getColumnIndex("name"));
				boolean value = c.getInt(c.getColumnIndex("value")) > 0;
				if (name.equalsIgnoreCase("overturn")) {
					mOverTurn.setChecked(value);
				} else if (name.equalsIgnoreCase("ringermode")) {
					mRinger.setChecked(value);
				} else if (name.equalsIgnoreCase("touch")) {
					mTouch.setChecked(value);
				}
			}
			c.close();
		}
		
		mOverTurn.setOnPreferenceChangeListener(this);
		mRinger.setOnPreferenceChangeListener(this);
		mTouch.setOnPreferenceChangeListener(this);	        
	}
	
	  @Override
	    public boolean onPreferenceChange(Preference preference, Object objValue) {
			if (preference == mOverTurn) {
				updateValue("overturn", (Boolean) objValue);
			} else if (preference == mRinger) {
				updateValue("ringermode", (Boolean) objValue);
			} else if (preference == mTouch) {
				updateValue("touch", (Boolean) objValue);
			}
			return true;		  	  
	  }
	  
	  private void updateValue(String name, boolean value) {
			Uri uri = Uri.parse("content://com.mst.phone/phone_setting");
			ContentResolver cr = getContentResolver();
			ContentValues cv = new ContentValues();
			cv.put("value", value);
			cr.update(uri, cv, "name = '" + name + "'" , null);
	  }


}
