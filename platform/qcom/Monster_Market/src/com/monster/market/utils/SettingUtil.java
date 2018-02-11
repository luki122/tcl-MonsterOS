package com.monster.market.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.CheckBox;

import com.monster.market.activity.SettingPreferenceActivity;
import com.monster.market.constants.Constant;

public class SettingUtil {
	
	/** 
	* @Title: isHold
	* @Description: 判断下载的应用安装文件是否保留
	* @param @param cxt
	* @param @return
	* @return boolean
	* @throws 
	*/ 
	public static boolean isHold(Context cxt) {
		return SettingPreferenceActivity.getPreferenceValue(cxt,
				SettingPreferenceActivity.HOLD_APP_KEY);
	}

	/**
	 * 是否加载图片
	 * @param cxt
	 * @return
     */
	public static boolean isLoadingImage(Context cxt) {

		boolean nFlag = SettingPreferenceActivity.getPreferenceValue(cxt,
				SettingPreferenceActivity.NONE_DOWNLOAD_PIC_KEY);
		
		SharedPreferences sp = cxt.getSharedPreferences(Constant.SHARED_WIFI_UPDATE,
				cxt.MODE_APPEND);
		int isWifi = sp.getInt(Constant.SHARED_NETSTATUS_KEY, 0);

		if (nFlag && (isWifi == 2)) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @Title: canDownload
	 * @Description: wifi下才能下载
	 * @param @param cxt
	 * @param @return
	 * @return boolean
	 * @throws
	 */
	public static boolean canDownload(Context cxt) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(cxt);
		boolean nFlag = sp.getBoolean(Constant.SP_WIFI_DOWNLOAD_KEY, false);

		boolean isMobile = SystemUtil.isMobileNetworkConnected(cxt);

		if (nFlag && isMobile) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * 设置是否仅在Wifi网络下载
	 * @param cxt
	 * @param value
     */
	public static void setOnlyWifiDownload(Context cxt, boolean value) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(cxt);
		SharedPreferences.Editor ed = sp.edit();
		ed.putBoolean(Constant.SP_WIFI_DOWNLOAD_KEY,
				value);
		ed.commit();
	}

	/**
	 * 获取是否仅在Wifi网络下载
	 * @param cxt
	 * @return
     */
	public static boolean getOnlyWifiDownload(Context cxt) {
		boolean only = false;
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(cxt);
		only = sp.getBoolean(Constant.SP_WIFI_DOWNLOAD_KEY, false);
		return only;
	}

	/**
	 * 设置最后一次检查更新数量
	 * @param context
	 * @param appCount
     */
	public static void setLastUpdateAppCount(Context context, int appCount) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor ed = sp.edit();
		ed.putInt(Constant.SP_LAST_APP_UPDATE_COUNT_KEY, appCount);
		ed.commit();
	}

	/**
	 * 获取最后一次检查更新数量
	 * @param context
	 * @return
     */
	public static int getLastUpdateAppCount(Context context) {
		int count;
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		count = sp.getInt(Constant.SP_LAST_APP_UPDATE_COUNT_KEY, 0);
		return count;
	}

	/**
	 * 设置是否弹出WIFI断开后提示下载对话框
	 * @param context
	 * @param block
     */
	public static void setWifiBlockAlert(Context context, boolean block) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor ed = sp.edit();
		ed.putBoolean(Constant.SP_WIFI_DISCONNECT_ALERT_KEY, block);
		ed.commit();
	}

	/**
	 * 获取是否弹出WIFI断开后提示下载对话框
	 * @param context
	 * @return
     */
	public static boolean getWifiBlockAlert(Context context) {
		boolean block = false;
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		block = sp.getBoolean(Constant.SP_WIFI_DISCONNECT_ALERT_KEY, false);
		return block;
	}

	/**
	 * 设置弹出WIFI断开后提示下载对话框后操作(是或否)
	 * @param context
	 * @param operation
	 */
	public static void setWifiBlockAlertOperation(Context context, boolean operation) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		SharedPreferences.Editor ed = sp.edit();
		ed.putBoolean(Constant.SP_WIFI_DISCONNECT_ALERT_OPERATION_KEY, operation);
		ed.commit();
	}

	/**
	 * 获取弹出WIFI断开后提示下载对话框后操作(是或否)
	 * @param context
	 * @return
	 */
	public static boolean getWifiBlockAlertOperation(Context context) {
		boolean block = false;
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(context);
		block = sp.getBoolean(Constant.SP_WIFI_DISCONNECT_ALERT_OPERATION_KEY, false);
		return block;
	}


}
