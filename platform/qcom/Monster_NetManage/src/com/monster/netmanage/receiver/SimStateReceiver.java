package com.monster.netmanage.receiver;

import com.monster.netmanage.DataManagerApplication;
import com.monster.netmanage.DataManagerManager;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.ToolsUtil;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * 插拔手机卡监听广播
 * @author zhaolaichao
 */
public class SimStateReceiver extends BroadcastReceiver {
	private final static String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
	/**
	 * 提示设置套餐action
	 */
	public final static String ACTION_NOTIFY_SET_DATAPLAN = "com.monster.netmanage.action.NOTIFY_SET_DATAPLAN";
	
	public final static int SIM_VALID = 0;
	public final static int SIM_INVALID = 1;
	private int simState = SIM_INVALID;
	private static ISimStateChangeListener mChangeListener;
	
	public int getSimState() {
		return simState;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("SimStateReceiver", "sim state changed>>" + intent.getAction());
		if (intent.getAction().equals(ACTION_SIM_STATE_CHANGED)) {
			TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
			int state = tm.getSimState();
			switch (state) {
			case TelephonyManager.SIM_STATE_READY:
				simState = SIM_VALID;
				break;
			case TelephonyManager.SIM_STATE_UNKNOWN:
			case TelephonyManager.SIM_STATE_ABSENT:
			case TelephonyManager.SIM_STATE_PIN_REQUIRED:
			case TelephonyManager.SIM_STATE_PUK_REQUIRED:
			case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
			default:
				simState = SIM_INVALID;
				break;
			}
			if (simState == SIM_VALID) {
				//更新当前上网卡的imsi
				//更新IMSI信息
				DataManagerApplication.mImsiArray = ToolsUtil.getIMSI(context);
				String activeSimImsi = ToolsUtil.getActiveSimImsi(context);
				String saveNetSimImsi = PreferenceUtil.getString(context, "", PreferenceUtil.CURRENT_ACTIVE_IMSI_KEY, null);
				if (!TextUtils.equals(activeSimImsi, saveNetSimImsi)) {
					if (!TextUtils.isEmpty(activeSimImsi)) {
						PreferenceUtil.putString(context, "", PreferenceUtil.CURRENT_ACTIVE_IMSI_KEY, activeSimImsi);
						//当sim卡的状态发生改变时且不前上网卡没有设置套餐
					}
				}
				PreferenceUtil.putString(context, PreferenceUtil.SIM_1, PreferenceUtil.IMSI_KEY, DataManagerApplication.mImsiArray[0]);
				PreferenceUtil.putString(context, PreferenceUtil.SIM_2, PreferenceUtil.IMSI_KEY, DataManagerApplication.mImsiArray[1]);
				try {
						//当sim卡的状态发生改变时初始化数据
					    DataManagerManager.setDualPhoneInfoFetcher(DataManagerApplication.mImsiArray);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}  
		if (null != mChangeListener) {
			mChangeListener.onSimStateChange(simState);
		}
	}
	
	/**
	 * 监听sim卡状态变化,更新界面
	 * @param listener
	 */
	public static void setSimStateChangeListener(ISimStateChangeListener listener) {
		mChangeListener = listener;
	}
	
	/**
	 * 监听sim卡状态变化
	 * @author zhaolaichao
	 *
	 */
	public interface ISimStateChangeListener {
		public void onSimStateChange(int simState);
	}
}
