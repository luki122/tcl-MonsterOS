package com.monster.netmanage;

import java.util.Arrays;

import com.monster.netmanage.service.NetManagerService;
import com.monster.netmanage.service.NetManagerService.Callback;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.ToolsUtil;
import com.mst.tms.NetInfoEntity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Telephony.Sms.Intents;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import tmsdk.bg.module.network.ITrafficCorrectionListener;

public class DataCorrect {
	private static final String TAG = "DataCorrect";
	public static final String UPDATE_DATAPLAN_ACTION = "com.monster.netmanage.update_dataplan.action";
	private static final int FIRST_SIM_INDEX = 0;
	private static final int SECOND_SIM_INDEX = 1;
	/**
	 * 校正设置成功
	 */
	public static final int ERR_NONE = 0;
	/**
	 * 流量短信解析成功
	 */
	public static final int MSG_TRAFFICT_NOTIFY = 10001;
	/**
	 * 流量校正失败
	 */
	public static final int MSG_TRAFFICT_ERROR = 10002;
	/**
	 * 删除发送的短信
	 */
	public static final int SMS_SENT_DELETE_TAG = 10003;
	/**
	 * 单例模式
	 */
	public static DataCorrect mDataCorrect = new DataCorrect();
	private static NetInfoEntity mInfoEntity = new NetInfoEntity();
	private Handler mHandler;
	private Context mContext;
	/**
	 * 运营商查询号码
	 */
	private String mQueryCode;
	/**
	 * 向运营商发送流量查询端口
	 */
	private String mQueryPort;

	private NetManagerService.Binder  netManageBinder = null;  
	/**
	 * 短信接收广播是否注销
	 */
	private boolean mSmgReceiverState = false;
	/**
	 * 是否更新UI
	 */
	private static boolean mIsUpdateUI;
	
	private int mSimIndex;
	
	private IDataChangeListener mDataChangeListener;
	
	private DataCorrect() {
		super();
	}

	public static synchronized DataCorrect getInstance() {
		return mDataCorrect;
	}

	public int getmSimIndex() {
		return mSimIndex;
	}
    public NetInfoEntity geNetInfoEntity() {
    	return mInfoEntity;
    }
	/**
	 * 初始化校正
	 * 
	 * @param context
	 * @param handler
	 */
	public void initCorrect(Context context, Handler handler) {
		mContext = context;
		if (null != handler) {
			mHandler = handler;
		}
		initCorrectData();
	}
	
private ServiceConnection netManageServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.e(TAG, "mITmsService-onServiceDisconnected-->>" + name);
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			netManageBinder = (NetManagerService.Binder) service;  
			netManageBinder.getMyService().setCallback(new Callback() {

				@Override
				public void onDataChange(String simImsi, NetInfoEntity netInfoEntity) {
					mInfoEntity = netInfoEntity;
					if (null != mDataChangeListener) {
						mDataChangeListener.onDataChange(simImsi, netInfoEntity);
					}
				}
			});
		}
	};
	
	public void setOnDataChange(IDataChangeListener dataChangeListener) {
		mDataChangeListener = dataChangeListener;
	}
	public interface IDataChangeListener {
		void onDataChange(String simImsi, NetInfoEntity netInfoEntity);
	}
	
	private class TrafficCorrectListener extends ITrafficCorrectionListener{

		@Override
		public void onNeedSmsCorrection(int simIndex, String queryCode, String queryPort) {
			Log.e("", "onNeedSmsCorrection--simIndex:[" + simIndex + "]--queryCode:[" + queryCode + "]queryPort:["
					+ queryPort + "]");
			//需要发查询短信校正
			mQueryCode = queryCode;
			mQueryPort = queryPort;
			Log.e("mQueryCode", "----mQueryCode-----" + mQueryCode + "---mQueryPort---" + mQueryPort);
			// 发送查询流量短信
			sendCorrectMsg(simIndex, mQueryPort, mQueryCode);
		}

		@Override
		public void onTrafficInfoNotify(int simIndex, int trafficClass, int subClass, int kBytes) {
			//保存套餐信息
			saveDataSet(simIndex);
			 if (DataManagerApplication.mImsiArray.length == 1) {
	    		 //包含只有一张sim卡的情况
				 simIndex = 0;
	    	 }
			 if (null != mHandler && mIsUpdateUI) {
				 Message msg = mHandler.obtainMessage(MSG_TRAFFICT_NOTIFY, simIndex, 0);
				 msg.sendToTarget();
			 }
			 mIsUpdateUI = false;
			 android.util.Log.v(TAG, "onTrafficNotify->>" + simIndex + ">subClass>>" + subClass);
		}

		@Override
		public void onError(int simIndex, int errorCode) {
			String strState = "状态信息：" +  "卡：[" + simIndex + "]校正出错:[" + errorCode + "]";
			if (DataManagerApplication.mImsiArray.length == 1 ) {
	    		 //包含只有一张sim卡的情况
				simIndex = 0;
			}
			if (null != mHandler && mIsUpdateUI) {
				Message msg = mHandler.obtainMessage(MSG_TRAFFICT_ERROR , simIndex, 0);
				msg.obj = errorCode;
				msg.sendToTarget();
			}
			mIsUpdateUI = false;
			Log.v(TAG, "onError--simIndex:" + strState );
		}
		
	}
	/**
	 * 开始流量校正
	 * 
	 * @param currentIndex
	 *            当前选中的sim卡
	 */
	public void startCorrect(Context context, boolean isUpdateUI, int currentIndex) {
		mContext = context;
		if (mContext == null) return;
		int result;
		mIsUpdateUI = isUpdateUI;
		if (currentIndex == FIRST_SIM_INDEX) {
			mSimIndex = FIRST_SIM_INDEX;
		} else if (currentIndex == SECOND_SIM_INDEX) {
			mSimIndex = SECOND_SIM_INDEX;
		}
		if (DataManagerApplication.mImsiArray.length > mSimIndex) {
	    	 //包含只有一张sim卡的情况
		    int closeDay = PreferenceUtil.getInt(mContext, DataManagerApplication.mImsiArray[mSimIndex], PreferenceUtil.CLOSEDAY_KEY, 1);
	  	    String provinceCode = PreferenceUtil.getString(mContext, DataManagerApplication.mImsiArray[mSimIndex], PreferenceUtil.PROVINCE_CODE_KEY, "");
		    String cityCode = PreferenceUtil.getString(mContext, DataManagerApplication.mImsiArray[mSimIndex], PreferenceUtil.CITY_CODE_KEY, "");
		    String carryCode = PreferenceUtil.getString(mContext, DataManagerApplication.mImsiArray[mSimIndex], PreferenceUtil.OPERATOR_CODE_KEY, "");
		    String brandCode = PreferenceUtil.getString(mContext, DataManagerApplication.mImsiArray[mSimIndex], PreferenceUtil.DATAPLAN_TYPE_CODE_KEY, "");
		    result = TrafficCorrectionWrapper.getInstance().setConfig(mSimIndex, provinceCode, cityCode, carryCode, brandCode, closeDay);// 保存配置。在进行流量校正之前，必要进行设置。返回ErrorCode
		    int retCode = TrafficCorrectionWrapper.getInstance().startCorrection(mSimIndex);
		    Log.e(TAG, "-----result--setConfig-->>>>" + result + "-----retCode--->>>" + retCode);
		    if (retCode != ERR_NONE){
		    	//卡校正出错终止
		    	Message msg = mHandler.obtainMessage(MSG_TRAFFICT_ERROR , mSimIndex, 0);
		    	 msg.sendToTarget();
		    	 return;
		    };
		}
	}

	private void initCorrectData() {
		if (null == mContext)
			return;
		TrafficCorrectionWrapper.getInstance().init(DataManagerApplication.getInstance());
		TrafficCorrectionWrapper.getInstance().setTrafficCorrectionListener(new TrafficCorrectListener());
	    mContext.bindService(new Intent(mContext, NetManagerService.class), netManageServiceConnection, Context.BIND_AUTO_CREATE);
	}

	/**
	 * 发送查询流量短信
	 * @param selectSimIndex   当前使用哪种sim卡发短信校正流量
	 * @param phoneNo   运营商号码
	 * @param message    短信内容
	 */
	private void sendCorrectMsg(int selectSimIndex, String phoneNo, String message) {
	     try {
	    	 if (DataManagerApplication.mImsiArray.length == 1) {
	    		 //当前只有一张sim卡
	    		 selectSimIndex = ToolsUtil.getCurrentNetSimSubInfo(mContext);
	    	 }
	    	 int subId = ToolsUtil.getIdInDbBySimId(mContext, selectSimIndex);//通过simId来获得subId
	    	 SmsManager sm = SmsManager.getSmsManagerForSubscriptionId(subId);
	    	 Intent smsIntent = new Intent();
	    	 smsIntent.setAction(Intents.SMS_RECEIVED_ACTION);
	    	 Log.e(TAG, TAG + ">>selectedIMSI>>>----subId---->>>>" + subId + "----->>>" + selectSimIndex);
	    	 PendingIntent sentIntent = PendingIntent.getActivity(mContext, 0, smsIntent, 0);
	    	 sm.sendTextMessage(phoneNo, null, message, sentIntent, null);
//	    	 PreferenceUtil.putInt(mContext, selectIMSI, PreferenceUtil.SIM_SUBID_KEY, subId);
//			 //请求默认短信应用权限
//	    	 AppOpsManager appOpsManager = (AppOpsManager) mContext.getSystemService("appops");
//		     appOpsManager.setMode(15, android.os.Process.myUid(), mContext.getPackageName(), 0);
//	    	 Message msg = smsHandler.obtainMessage(SMS_SENT_DELETE_TAG);
//	    	 Bundle bundle = new Bundle();
//	    	 bundle.putString("MESSAGE", message);
//	    	 bundle.putString("PHONE_NO", phoneNo);
//	    	 msg.setData(bundle);
//             smsHandler.sendMessageDelayed(msg, 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
				  try {  
						this.abortBroadcast();
						Bundle bundle = intent.getExtras();
						//获取接收短信sim卡的卡槽位置
						final int slotId = bundle.getInt("slot");
						Log.e("MessageReceiver", "slotId----->>>" +  slotId);
						if (slotId < 0) {
							return;
						} 
						String format = bundle.getString("format");
						Object[] pdus = (Object[]) bundle.get("pdus");
						SmsMessage[]  smsMessageArray = new SmsMessage[pdus.length]; 
						StringBuffer msgSbf = new StringBuffer();
						for (int n = 0; n < smsMessageArray.length; n++) {  
							  smsMessageArray[n] = SmsMessage.createFromPdu((byte[]) pdus[n], format);  
							  msgSbf.append(smsMessageArray[n].getMessageBody());
					    } 
					  final String sms = msgSbf.toString();
					  Log.e("MessageReceiver", "SMS_RECEIVED------" +  msgSbf);
					  analysisSMS(slotId, sms);
				  }  catch (Exception e)   {  
					  e.printStackTrace();  
				  }  
			}
			return;
		}
	};
	
	public void analysisSMS(final int simIndex, final String smsBody) {
		 new AsyncTask<String, String, String>(){

				@Override
				protected String doInBackground(String... params) {
					int state = TrafficCorrectionWrapper.getInstance().analysisSMS(simIndex, mQueryCode, mQueryPort, smsBody);
					Log.v(TAG, "analysisSMS>>>>" + state);
					return null;
				}
				 
			 }.execute();
	}
	
	/**
	 * 注销接收短信广播
	 */
	@SuppressWarnings("unused")
	private void unRegisterSmsReceiver() {
		if (null != smsReceiver && mSmgReceiverState) {
			mContext.unregisterReceiver(smsReceiver);
			smsReceiver = null;
			mSmgReceiverState = false;
		}
	}
	
	/**
	 * 注册接收短信广播
	 */
	@SuppressWarnings("unused")
	private void registerSmsReceiver() {
		IntentFilter filter = new IntentFilter(Intents.SMS_RECEIVED_ACTION);
		mSmgReceiverState = true;
		//设置接收广播优先级
		filter.setPriority(Integer.MAX_VALUE);
		filter.addAction(Intents.SMS_DELIVER_ACTION);
		mContext.registerReceiver(smsReceiver, filter);
	}
	/**
	 * 保存套餐信息
	 * @param simIndex
	 */
	private void saveDataSet(int simIndex) {
//		logTemp += "常规-剩余[" + retTrafficInfo[0] + "]已用[" + retTrafficInfo[1] + "]总量[" +retTrafficInfo[2] +"]\n";
//		logTemp += "闲时-剩余[" + retTrafficInfo[3] + "]已用[" + retTrafficInfo[4] + "]总量[" +retTrafficInfo[5] +"]\n";
//		logTemp += "4G-剩余[" + retTrafficInfo[6] + "]已用[" + retTrafficInfo[7] + "]总量[" +retTrafficInfo[8] +"]\n";
		int[] trafficInfo;
		try {
			//返回流量校正后的值以KB为单位
			trafficInfo = TrafficCorrectionWrapper.getInstance().getTrafficInfo(simIndex);
			Log.e("trafficInfo0", "trafficInfo0------>>>" + Arrays.toString(trafficInfo) + "--->>>"+ trafficInfo.length);
			for (int i = 0; i < trafficInfo.length; i++) {
				if (trafficInfo[i] == -1) {
					trafficInfo[i] = 0;
				}
			}
		    if (DataManagerApplication.mImsiArray.length == 1) {
		    	//当只有一张卡时
		    	simIndex = 0;
		    }
			String currentIMSI = DataManagerApplication.mImsiArray[simIndex];
			//返回流量校正后的值以KB为单位
			//常规-剩余
			if (trafficInfo[0] > 0) {
				PreferenceUtil.putInt(mContext, currentIMSI, PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, (trafficInfo[0]));
			}
			//常规-已用
			if (trafficInfo[1] > 0) {
				PreferenceUtil.putInt(mContext, currentIMSI, PreferenceUtil.USED_DATAPLAN_COMMON_KEY, (trafficInfo[1]));
			}
			//常规-总量
			if (trafficInfo[2] > 0) {
				PreferenceUtil.putInt(mContext, currentIMSI, PreferenceUtil.DATAPLAN_COMMON_KEY, (trafficInfo[2]));
			}
			//闲时-剩余
			if (trafficInfo[3] > 0) {
				PreferenceUtil.putInt(mContext, currentIMSI, PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY, (trafficInfo[3]));
			}
			//闲时-已用
			if (trafficInfo[4] > 0) {
				PreferenceUtil.putInt(mContext, currentIMSI, PreferenceUtil.USED_FREE_DATA_TOTAL_KEY, (trafficInfo[4]));
			}
			//闲时-总量
			if (trafficInfo[5] > 0) {
				PreferenceUtil.putInt(mContext, currentIMSI, PreferenceUtil.FREE_DATA_TOTAL_KEY, (trafficInfo[5]));
			}
			//首次流量校正成功
			PreferenceUtil.putInt(mContext, currentIMSI, PreferenceUtil.DATAPLAN_CORRECT_FIRST_KEY, 1);
			//用于更新图标
			ToolsUtil.updateIconReceiver();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
