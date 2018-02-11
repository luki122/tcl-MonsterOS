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

public class SimStateReceiver extends BroadcastReceiver {
	private static boolean DBG = true;
	private String TAG = "SimStateReceiver";
	private final int PROVISIONED = 1;
	private final int NOT_PROVISIONED = 0;
	private final int INVALID_STATE = -1;
	private final int CARD_NOT_PRESENT = -2;
	private Context mContext;
	public static int sim0State=-2;
	public static int sim1State=-2;
	public static int sim0ReadContactsState=-2;
	public static int sim1ReadContactsState=-2;

	public final int SIM_STATE_READY=1;
	public final int SIM_STATE_ERROR=-1;
	public final int SIM_STATE_NOT_READY=-2;
	public static boolean isForBootCompleted=false;
	public static boolean singleSimEnable=false;
	public static boolean isSim0Absent=false;
	public static boolean isSim1Absent=false;
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
				if(slotId==0) isSim0Absent=false;
				else if(slotId==1) isSim1Absent=false;
			}
			else if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(stateExtra)
					|| IccCardConstants.INTENT_VALUE_ICC_UNKNOWN.equals(stateExtra)
					|| IccCardConstants.INTENT_VALUE_ICC_CARD_IO_ERROR.equals(stateExtra)) {
				simState = SIM_STATE_ERROR;
				if(IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(stateExtra)){
					if(slotId==0) isSim0Absent=true;
					else if(slotId==1) isSim1Absent=true;
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
			sendSimRefreshUpdate(slotId);
		} else if ("android.intent.action.ACTION_ADN_INIT_DONE".equals(action)) {
			final int slotId = intent.getIntExtra(PhoneConstants.SLOT_KEY,
					SubscriptionManager.getPhoneId(SubscriptionManager.getDefaultSubscriptionId()));
			if (DBG)
				log("ACTION_ADN_INIT_DONE intent received on sub = " + slotId);
			sendSimRefreshUpdate(slotId);
		}
	}

	private long beginTime;
	private void sendPhoneBoot() {
		Log.d(TAG,"sendPhoneBoot");
		isForBootCompleted=true;
		beginTime=System.currentTimeMillis();
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
	public boolean reQueryisMultiSimEnabled(){
		Log.d(TAG,"reQueryisMultiSimEnabled");
		int slot0Status=getSlotProvisionStatus(0);
		int slot1Status=getSlotProvisionStatus(1);
		Log.d(TAG,"slot0Status:"+slot0Status+" slot1Status:"+slot1Status);
		if(slot0Status==1&&slot1Status==1) {
			isMultiSimEnabled=true;
			lastSimCount=2;
		}else {
			isMultiSimEnabled=false;
			if(lastSimCount==2) isFromMultiToSingleCard=true;
			lastSimCount=1;
		}
		Log.d(TAG,"isMultiSimEnabled:"+isMultiSimEnabled);
		return isMultiSimEnabled;
	}

	public static boolean hasSimCardReady;//是否有卡准备好读SIM卡联系人
	public static boolean isMultiSimReady;//是否双卡都准备好读SIM卡联系人

	private boolean isFirst=true;
	private void sendSimState(int slotId, int state) {		
		if(isFirst){
			mHandler.removeCallbacks(runnable);
			mHandler.postDelayed(runnable, 10000);
			isFirst=false;
			return;
		}
		if(slotId==0){
			if(state==SIM_STATE_READY) sim0State=1;
			else {
				sim0State=-2;
				sim0ReadContactsState=-2;
			}
		}else if(slotId==1){
			if(state==SIM_STATE_READY) sim1State=1;
			else {
				sim1State=-2;
				sim1ReadContactsState=-2;
			}
		}

		Log.d(TAG,"isFromMultiToSingleCard:"+isFromMultiToSingleCard+" sim0state:"+sim0State+" sim1state:"+sim1State+" issim0absent:"+isSim0Absent+" issim1absent:"+isSim1Absent);
		if(isFromMultiToSingleCard&&((sim0State==1&&isSim1Absent)||(sim1State==1&&isSim0Absent))){
			mHandler.removeCallbacks(runnable1);
			mHandler.postDelayed(runnable1, 10000);
			isFromMultiToSingleCard=false;
			return;
		}

		hasSimCardReady=sim0ReadContactsState==1||sim1ReadContactsState==1;
		isMultiSimReady=sim0ReadContactsState==1&&sim1ReadContactsState==1;
		isMultiSimEnabled=reQueryisMultiSimEnabled();
		
		updateSharedPreference();
		
		Intent intent=new Intent(MST_ACTION_SIM_STATE_CHANGED);
		mContext.sendBroadcast(intent);
		Log.d(TAG,"sendSimState,slotId:"+slotId+" state:"+state+" sim0State:"+sim0State+" sim1State:"+sim1State+" singleSimEnable:"+singleSimEnable);
	}
	private void updateSharedPreference() {
		// TODO Auto-generated method stub
		if(sharedPreferences==null){
			sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		}
		final SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("isMultiSimEnabled",isMultiSimEnabled);
		editor.putBoolean("isMultiSimReady",isMultiSimReady);
		editor.putBoolean("hasSimCardReady",hasSimCardReady);
		editor.putBoolean("isSim0Absent",isSim0Absent);
		editor.putBoolean("isSim1Absent",isSim1Absent);
		editor.putInt("sim0ReadContactsState",sim0ReadContactsState);
		editor.putInt("sim1ReadContactsState",sim1ReadContactsState);
		editor.commit();
	}
	private final static String MST_ACTION_SIM_STATE_CHANGED = "mst.intent.action.SIM_STATE_CHANGED";

	private void sendSimRefreshUpdate(int slotId) {
		Log.d(TAG,"isForBootCompleted:"+isForBootCompleted);
		isMultiSimEnabled=reQueryisMultiSimEnabled();

		if(slotId==0){
			sim0ReadContactsState=1;
		}else if(slotId==1){
			sim1ReadContactsState=1;
		}
		if(isForBootCompleted){
			mHandler.removeCallbacks(runnable);
			mHandler.postDelayed(runnable, 10000);
			isForBootCompleted=false;
			return;
		}

		hasSimCardReady=sim0ReadContactsState==1||sim1ReadContactsState==1;
		isMultiSimReady=sim0ReadContactsState==1&&sim1ReadContactsState==1;

		updateSharedPreference();
		
		Log.d(TAG,"sendSimRefreshUpdate,slotId:"+slotId+" sim0ReadContactsState:"+sim0ReadContactsState+" sim1ReadContactsState:"+sim1ReadContactsState+" singleSimEnable:"+singleSimEnable);
		Intent intent=new Intent(MST_ACTION_SIM_STATE_CHANGED);
		mContext.sendBroadcast(intent); 
	}

	private static int lastSimCount=1;
	private static boolean isFromMultiToSingleCard=false;
	protected void log(String msg) {
		Log.d(TAG, msg);
	}
	private Handler mHandler = new Handler();
	private Runnable runnable=new Runnable() {

		@Override
		public void run() {
			Log.d(TAG,"runnable");
			if(isMultiSimEnabled){
				sim0ReadContactsState=1;
				sim1ReadContactsState=1;
			}
			hasSimCardReady=sim0ReadContactsState==1||sim1ReadContactsState==1;
			isMultiSimReady=sim0ReadContactsState==1&&sim1ReadContactsState==1;
			
			updateSharedPreference();
			
			Intent intent=new Intent(MST_ACTION_SIM_STATE_CHANGED);
			mContext.sendBroadcast(intent); 
		}
	};
	private Runnable runnable1=new Runnable() {

		@Override
		public void run() {
			Log.d(TAG,"runnable1");
			if(sim0State==1&&isSim1Absent){
				sim0ReadContactsState=1;
				sim1ReadContactsState=-2;
			}else if(sim1State==1&&isSim0Absent){
				sim0ReadContactsState=-2;
				sim1ReadContactsState=1;
			}
			hasSimCardReady=sim0ReadContactsState==1||sim1ReadContactsState==1;
			isMultiSimReady=sim0ReadContactsState==1&&sim1ReadContactsState==1;
			
			updateSharedPreference();
			
			Intent intent=new Intent(MST_ACTION_SIM_STATE_CHANGED);
			mContext.sendBroadcast(intent); 
		}
	};
}
