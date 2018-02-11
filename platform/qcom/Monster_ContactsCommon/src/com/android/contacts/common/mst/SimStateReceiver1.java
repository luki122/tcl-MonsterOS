//add by liyang 2016-11-22

package com.android.contacts.common.mst;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SubscriptionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import org.codeaurora.internal.IExtTelephony;
import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;

public class SimStateReceiver1 extends BroadcastReceiver {
	private static boolean DBG = true;
	private static String TAG = "SimStateReceiver1";
	private final int PROVISIONED = 1;
	private final int NOT_PROVISIONED = 0;
	private final int INVALID_STATE = -1;
	private final int CARD_NOT_PRESENT = -2;
	private static Context mContext;

	public final int SIM_STATE_READY=1;
	public final int SIM_STATE_ERROR=-1;
	public final int SIM_STATE_NOT_READY=-2;
	public static boolean isSim0Ready=false;
	public static boolean isSim1Ready=false;
	private static SharedPreferences sharedPreferences=null;
	@Override
	public void onReceive(Context context, Intent intent) {	

		final String action = intent.getAction();
		mContext = context;
		if (DBG)
			log("received broadcast " + action);
		if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)) {
			final int slotId = intent.getIntExtra(PhoneConstants.SLOT_KEY,
					SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultSubscriptionId()));
			final String stateExtra = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
			final int simState;
			if (DBG)
				log("ACTION_SIM_STATE_CHANGED intent received on sub = " + slotId
						+ "SIM STATE IS " + stateExtra);

			if (IccCardConstants.INTENT_VALUE_ICC_LOADED.equals(stateExtra)
					|| IccCardConstants.INTENT_VALUE_ICC_IMSI.equals(stateExtra)
					|| IccCardConstants.INTENT_VALUE_ICC_READY.equals(stateExtra)) {
				simState = SIM_STATE_READY;
				if(slotId==0) {
					isSim0Ready=true;
				}
				else if(slotId==1) {
					isSim1Ready=true;
				}
			}
			else if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(stateExtra)
					|| IccCardConstants.INTENT_VALUE_ICC_UNKNOWN.equals(stateExtra)
					|| IccCardConstants.INTENT_VALUE_ICC_CARD_IO_ERROR.equals(stateExtra)) {
				simState = SIM_STATE_ERROR;
				if(slotId==0) {
					isSim0Ready=false;
				}
				else if(slotId==1) {
					isSim1Ready=false;
				}

			} else {
				simState = SIM_STATE_NOT_READY;
			}
			sendSimState(slotId, simState);
		} else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			sendPhoneBoot();
		} else if ("org.codeaurora.intent.action.ACTION_SIM_REFRESH_UPDATE".equals(action)) {
			final int slotId = intent.getIntExtra(PhoneConstants.SLOT_KEY,
					SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultSubscriptionId()));
			if (DBG)
				log("ACTION_SIM_REFRESH_UPDATE intent received on sub = " + slotId);
			//			sendSimRefreshUpdate(slotId);
		} else if ("android.intent.action.ACTION_ADN_INIT_DONE".equals(action)) {
			final int slotId = intent.getIntExtra(PhoneConstants.SLOT_KEY,
					SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultSubscriptionId()));
			if (DBG)
				log("ACTION_ADN_INIT_DONE intent received on sub = " + slotId);
			sendSimRefreshUpdate(slotId);
		}
	}

	private void sendPhoneBoot() {
		Log.d(TAG,"sendPhoneBoot");
	}

	private int getSlotProvisionStatus(int slot) {
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
	public static boolean isMultiSimEnabled;//是否启用双卡
	private static boolean isSimContactsReady;//是否可以读取sim卡联系人了
	private static int slot0Status=0;
	private static int slot1Status=0;
	public boolean reQueryisMultiSimEnabled(){
		Log.d(TAG,"reQueryisMultiSimEnabled");
		slot0Status=getSlotProvisionStatus(0);
		slot1Status=getSlotProvisionStatus(1);
		Log.d(TAG,"slot0Status:"+slot0Status+" slot1Status:"+slot1Status);
		if(slot0Status==1&&slot1Status==1) {
			isMultiSimEnabled=true;
		}else {
			isMultiSimEnabled=false;
		}
		Log.d(TAG,"isMultiSimEnabled:"+isMultiSimEnabled);
		return isMultiSimEnabled;
	}


	private void sendSimState(int slotId, int state) {
		isSimContactsReady=false;
		isMultiSimEnabled=reQueryisMultiSimEnabled();
		updateSharedPreference();
		Intent intent=new Intent(MST_ACTION_SIM_STATE_CHANGED);
		mContext.sendBroadcast(intent); 

		if(isSim0Ready||isSim1Ready){
			Log.d(TAG,"sendSimState");
			mHandler.removeCallbacks(runnable1);
			mHandler.postDelayed(runnable1, 30000);
		}
	}
	private static void updateSharedPreference() {
		// TODO Auto-generated method stub
		if(sharedPreferences==null){
			sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		}
		final SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("isMultiSimEnabled",isMultiSimEnabled);
		editor.putBoolean("isSimContactsReady",isSimContactsReady);
		editor.putBoolean("isSim0Ready",isSim0Ready);
		editor.putBoolean("isSim1Ready",isSim1Ready);
		editor.putInt("slot0Status",slot0Status);
		editor.putInt("slot1Status",slot1Status);
		editor.commit();
	}
	private final static String MST_ACTION_SIM_STATE_CHANGED = "mst.intent.action.SIM_STATE_CHANGED";

	private void sendSimRefreshUpdate(int slotId) {
		isMultiSimEnabled=reQueryisMultiSimEnabled();
		if(!isMultiSimEnabled){
			mHandler.removeCallbacks(runnable1);
			mHandler.postDelayed(runnable1, 1000);
		}
	}

	protected void log(String msg) {
		Log.d(TAG, msg);
	}
	private static Handler mHandler = new Handler();
	private static Runnable runnable1=new Runnable() {

		@Override
		public void run() {
			Log.d(TAG,"runnable1");

			isSimContactsReady=true;
			updateSharedPreference();

			Intent intent=new Intent(MST_ACTION_SIM_STATE_CHANGED);
			mContext.sendBroadcast(intent); 
		}
	};
}
