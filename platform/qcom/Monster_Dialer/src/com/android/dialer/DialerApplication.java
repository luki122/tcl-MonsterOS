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
 * limitations under the License
 */

package com.android.dialer;

import java.util.ArrayList;
import java.util.List;

import android.os.ServiceManager;
import android.os.RemoteException;
import android.app.Activity;
//import com.android.contacts.common.util.MstUtils;
import android.app.Application;
import android.os.RemoteException;
import android.os.Trace;
import android.util.Log;
import org.codeaurora.internal.IExtTelephony;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
import android.os.ServiceManager;
import com.android.contacts.common.extensions.ExtensionsFactory;
import com.android.contacts.commonbind.analytics.AnalyticsUtil;
import com.mediatek.dialer.dialersearch.DialerSearchHelper;
import com.android.contacts.common.util.PermissionsUtil;
import com.mst.privacy.PrivacyUtils;
import com.mst.tms.TmsServiceManager;

public class DialerApplication extends Application {
	private static final int PROVISIONED = 1;
	private static final int NOT_PROVISIONED = 0;
	private static final int INVALID_STATE = -1;
	private static final int CARD_NOT_PRESENT = -2;

	private static final String TAG = "DialerApplication";
	public static boolean isMultiSimEnabled;//是否启用双卡
	public static int slot0Status;//卡槽1状态
	public static int slot1Status;//卡槽2状态
	public static List<Activity> mPrivacyActivityList = new ArrayList<Activity>();
	@Override
	public void onCreate() {
		Trace.beginSection(TAG + " onCreate");
		super.onCreate();
	    sMe = this;
		Trace.beginSection(TAG + " ExtensionsFactory initialization");
		ExtensionsFactory.init(getApplicationContext());
		Trace.endSection();
		Trace.beginSection(TAG + " Analytics initialization");
		AnalyticsUtil.initialize(this);
		Trace.endSection();
		/// M: [MTK Dialer Search] fix ALPS01762713 @{
		  if (PermissionsUtil.hasContactsPermissions(this)) {
		      DialerSearchHelper.initContactsPreferences(getApplicationContext());
		  }
		Trace.endSection();

	    TmsServiceManager.getInstance(this).bindService();//绑定TMS
		//        isMultiSimEnabled=MstUtils.isMsimIccCardActive();
		reQueryisMultiSimEnabled();
		
		PrivacyUtils.bindService(this);
	}

	public static boolean reQueryisMultiSimEnabled(){
		Log.d(TAG,"reQueryisMultiSimEnabled");
		slot0Status=getSlotProvisionStatus(0);
		slot1Status=getSlotProvisionStatus(1);
		Log.d(TAG,"slot0Status:"+slot0Status+" slot1Status:"+slot1Status);
		if(slot0Status==1&&slot1Status==1) isMultiSimEnabled=true;
		else isMultiSimEnabled=false;
		Log.d(TAG,"isMultiSimEnabled:"+isMultiSimEnabled);
		return isMultiSimEnabled;
	}
	private static int getSlotProvisionStatus(int slot) {
		int provisionStatus = -1;
		try {
			//get current provision state of the SIM.
			IExtTelephony extTelephony =
					IExtTelephony.Stub.asInterface(ServiceManager.getService("extphone"));
			provisionStatus =  extTelephony.getCurrentUiccCardProvisioningStatus(slot);
		} catch (RemoteException ex) {
			provisionStatus = INVALID_STATE;
			Log.e(TAG,"Failed to get slotId: "+ slot +" Exception: " + ex);
		} catch (NullPointerException ex) {
			provisionStatus = INVALID_STATE;
			Log.e(TAG,"Failed to get slotId: "+ slot +" Exception: " + ex);
		}
		return provisionStatus;
	}
	

	   private  static DialerApplication sMe;
	   public static DialerApplication getInstance() {
	        return sMe;
	   }
}
