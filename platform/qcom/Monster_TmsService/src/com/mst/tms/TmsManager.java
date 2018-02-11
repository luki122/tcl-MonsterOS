package com.mst.tms;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import tmsdk.common.IDualPhoneInfoFetcher;
import tmsdk.common.ITMSApplicaionConfig;
import tmsdk.common.TMSDKContext;

public class TmsManager {
	
	public static void setDualPhoneInfoFetcher(final String[] imsiArray) {
		// TMSDKContext.setDualPhoneInfoFetcher()方法为流量校准支持双卡情况设置，其它情况不需要调用该函数。
		// 该函数中需要返回第一卡槽和第二卡槽imsi的读取内容。
		// 实现此方法时。一定在TMSDKContext.init前调用
		TMSDKContext.setDualPhoneInfoFetcher(new tmsdk.common.IDualPhoneInfoFetcher() {
			@Override
			public String getIMSI(int simIndex) {
				String imsi = "";
				if (simIndex == IDualPhoneInfoFetcher.FIRST_SIM_INDEX) {
					imsi = imsiArray[0]; // 卡槽1的imsi，需要厂商自己实现获取方法
					// PreferenceUtil.putString(DataManagerApplication.this,
					// PreferenceUtil.SIM_1, PreferenceUtil.IMSI_KEY, imsi);
				} else if (simIndex == IDualPhoneInfoFetcher.SECOND_SIM_INDEX) {
					imsi = imsiArray[1]; // 卡槽2的imsi，需要厂商自己实现获取方法
					// PreferenceUtil.putString(DataManagerApplication.this,
					// PreferenceUtil.SIM_2, PreferenceUtil.IMSI_KEY, imsi);
				}
				Log.e("imsi", "??>>imsi>>>>>>" + imsi);
				return imsi;
			}
		});
	}

	public static void init() {
		TMSDKContext.init(TmsApp.getInstance(), PermanentService.class, new ITMSApplicaionConfig() {

			@Override
			public HashMap<String, String> config(Map<String, String> src) {
				return new HashMap<String, String>(src);
			}

		});
	}

}
