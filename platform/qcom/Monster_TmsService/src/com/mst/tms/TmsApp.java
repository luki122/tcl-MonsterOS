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

package com.mst.tms;

import java.lang.reflect.Method;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

/**
 * Top-level Application class for the InCall app.
 */
public class TmsApp extends Application {

	static TmsApp sMe;

	public static TmsApp getInstance() {
		return sMe;
	}

	public TmsApp() {
		sMe = this;
	}

	@Override
	public void onCreate() {
		init();
	}

	private void init() {
		// TMSDKContext.setDualPhoneInfoFetcher()方法为流量校准支持双卡情况设置，其它情况不需要调用该函数。
		// 该函数中需要返回第一卡槽和第二卡槽imsi的读取内容。
		// 实现此方法时。一定在TMSDKContext.init前调用
		TmsManager.setDualPhoneInfoFetcher(getIMSI(this));
		TmsManager.init();

	}

	/**
	 * 获取双卡手机的两个卡的IMSI
	 * 
	 * @param context
	 * @return
	 */
	private String[] getIMSI(Context context) {
		// 双卡imsi的数组
		String[] imsis = new String[2];
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		List<SubscriptionInfo> mSelectableSubInfos = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
		if (null == mSelectableSubInfos || mSelectableSubInfos.size() == 0) {
			return imsis;
		}
		for (int i = 0; i < mSelectableSubInfos.size(); i++) {
			SubscriptionInfo subscriptionInfo = mSelectableSubInfos.get(i);
			// 获得subId;
			int subscriptionId = subscriptionInfo.getSubscriptionId();
			try {
				Method addMethod = tm.getClass().getDeclaredMethod("getSubscriberId", int.class);
				addMethod.setAccessible(true);
				imsis[i] = (String) addMethod.invoke(tm, subscriptionId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return imsis;
	}
}
