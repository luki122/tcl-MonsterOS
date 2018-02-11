package com.monster.netmanage;

import java.util.Arrays;

import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.ToolsUtil;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

/**
 * 流量管理应用
 * 
 * @author zhaolaichao
 *
 */
public class DataManagerApplication extends Application {

	private static DataManagerApplication mApplication;
	/**
	 * 获得sim卡的IMSI
	 */
	public static String[] mImsiArray;
	
	public DataManagerApplication() {
		mApplication = this;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		String activeSimImsi = ToolsUtil.getActiveSimImsi(getApplicationContext());
		PreferenceUtil.putString(this, "", PreferenceUtil.CURRENT_ACTIVE_IMSI_KEY, activeSimImsi);
		mImsiArray = ToolsUtil.getIMSI(this);
		if (mImsiArray.length == 1 && !TextUtils.isEmpty(mImsiArray[0])) {
			PreferenceUtil.putString(this, PreferenceUtil.SIM_1, PreferenceUtil.IMSI_KEY, mImsiArray[0]);
		} else if (mImsiArray.length == 2) {
			if (!TextUtils.isEmpty(mImsiArray[0])) {
				PreferenceUtil.putString(this, PreferenceUtil.SIM_1, PreferenceUtil.IMSI_KEY, mImsiArray[0]);
			}
			if (!TextUtils.isEmpty(mImsiArray[1])) {
				PreferenceUtil.putString(this, PreferenceUtil.SIM_2, PreferenceUtil.IMSI_KEY, mImsiArray[1]);
			}
		}
		Log.e("DataManagerApplication", "imsiArray>>>" + Arrays.toString(mImsiArray));
		DataManagerManager.setDualPhoneInfoFetcher(mImsiArray);
		DataManagerManager.init();
	}

	public static DataManagerApplication getInstance() {

		return mApplication;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}
}
