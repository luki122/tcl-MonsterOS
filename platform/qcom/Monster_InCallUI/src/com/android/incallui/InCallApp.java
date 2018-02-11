/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.incallui;



import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.mst.tms.TmsServiceManager;


import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telecom.VideoProfile;
import android.util.Log;
import android.os.SystemProperties;

/**
 * Top-level Application class for the InCall app.
 */
public class InCallApp extends Application {


	static InCallApp sMe;

	public static InCallApp getInstance() {
		return sMe;
	}

	public InCallApp() {
		sMe = this;
	}

    @Override
    public void onCreate() {
    	init();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    
    
    private void init() {
    	TmsServiceManager.getInstance(this).bindService();
    }    

	InCallActivity mInCallActivity;
	void setInCallActivityInstance(InCallActivity a) {
		mInCallActivity = a;
	}
	
	public static InCallActivity getInCallActivity () {
		return getInstance().mInCallActivity;
	}
	
    public void displayCallScreen() {
        Log.i("InCallApp", "displayCallScreen");
        Intent intent = InCallPresenter.getInstance().getInCallIntent(false, false);
        startActivity(intent);
    }
	
    public void displayCallScreenforSpeed() {
        if(mInCallActivity != null) {
            return;
        }
        Log.i("InCallApp", "displayCallScreenforSpeed");
        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(this, InCallActivity.class);
        intent.putExtra(InCallActivity.NEW_OUTGOING_CALL_EXTRA, false);
        intent.putExtra("speed", true);
        startActivity(intent);
    }
	
}
