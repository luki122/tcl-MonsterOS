package com.monster.netmanage;

import java.util.Arrays;

import com.monster.netmanage.net.AccessibilityUtils;
import com.monster.netmanage.service.AppTaskService;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.ToolsUtil;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.Log;

/**
 * 流量管理应用
 * 
 * @author zhaolaichao
 *
 */
public class DataManagerApplication extends Application {
    private final String TAG = "DataManagerApplication";
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
		if (!TextUtils.isEmpty(activeSimImsi)) {
			PreferenceUtil.putString(this, "", PreferenceUtil.CURRENT_ACTIVE_IMSI_KEY, activeSimImsi);
		}
		
		mImsiArray = ToolsUtil.getIMSI(this);
		PreferenceUtil.putString(this, PreferenceUtil.SIM_1, PreferenceUtil.IMSI_KEY, mImsiArray[0]);
		PreferenceUtil.putString(this, PreferenceUtil.SIM_2, PreferenceUtil.IMSI_KEY, mImsiArray[1]);
		Log.e("DataManagerApplication", "imsiArray>>>" + Arrays.toString(mImsiArray));
		DataManagerManager.setDualPhoneInfoFetcher(mImsiArray);
		DataManagerManager.init();
		if (!isAccessibilitySettingsOn(this)) {
			startAccessService(this);
		}
	}

	public static DataManagerApplication getInstance() {

		return mApplication;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}
   
	/**
	 * to check if service is enabled
	 * @param mContext
	 * @return
	 */
	private boolean isAccessibilitySettingsOn(Context mContext) {
	     int accessibilityEnabled = 0;
	     boolean accessibilityFound = false;
	     try {
	         accessibilityEnabled = Settings.Secure.getInt(
	                 mContext.getApplicationContext().getContentResolver(),
	                 android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
	     } catch (SettingNotFoundException e) {
	         Log.e(TAG, "Error finding setting, default accessibility to not found: "
	                         + e.getMessage());
	     }
	     TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

	     if (accessibilityEnabled == 1) {
	         String settingValue = Settings.Secure.getString(
	                 mContext.getApplicationContext().getContentResolver(),
	                 Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
	         Log.v(TAG, "settingValue>>>" + settingValue);
	         if (settingValue != null) {
	             TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
	             splitter.setString(settingValue);
	             while (splitter.hasNext()) {
	                 String accessabilityService = splitter.next();
	                 if (accessabilityService.equalsIgnoreCase(AppTaskService.APPTASK_SERVICE)) {
	                     return true;
	                 }
	             }
	         }
	     }
	     Log.v(TAG, "accessibilityFound>>>" + accessibilityFound);
	     return accessibilityFound;      
    }
	
	public void startAccessService(Context context) {
		 String settingValue = Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
                 Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
		 StringBuilder builder = new StringBuilder();
		 builder.append(settingValue).append(":").append(AppTaskService.APPTASK_SERVICE);
		   Settings.Secure.putString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, builder.toString());
	}
}
