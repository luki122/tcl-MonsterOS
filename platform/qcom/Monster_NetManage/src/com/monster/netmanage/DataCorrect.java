package com.monster.netmanage;

import java.util.Arrays;

import com.monster.netmanage.service.NetManagerService;
import com.monster.netmanage.service.NetManagerService.Callback;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.ToolsUtil;
import com.mst.tms.NetInfoEntity;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Telephony.Sms.Intents;
import android.telephony.SmsManager;
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
	public static NetInfoEntity mInfoEntity = new NetInfoEntity();
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
			if (simIndex == -1) return;
			//需要发查询短信校正
			mQueryCode = queryCode;
			mQueryPort = queryPort;
			Log.e("mQueryCode", "----mQueryCode-----" + mQueryCode + "---mQueryPort---" + mQueryPort);
			// 发送查询流量短信
			sendCorrectMsg(simIndex, mQueryPort, mQueryCode);
		}

		@Override
		public void onTrafficInfoNotify(int simIndex, int trafficClass, int subClass, int kBytes) {
			if (simIndex == -1) return;
			//保存套餐信息
			saveDataSet(simIndex);
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
			if (simIndex == -1) return;
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

	private void initCorrectData() {
		if (null == mContext)
			return;
		TrafficCorrectionWrapper.getInstance().init(DataManagerApplication.getInstance());
		TrafficCorrectionWrapper.getInstance().setTrafficCorrectionListener(new TrafficCorrectListener());
		Intent intent = new Intent(mContext, NetManagerService.class);
		mContext.startService(intent);
		mContext.bindService(intent, netManageServiceConnection, Context.BIND_AUTO_CREATE);
	}

	public void destory(Context context) {
		mContext = context;
		if (null != mContext) {
			mContext.unbindService(netManageServiceConnection);
		}
	}
	
	/**
	 * 发送查询流量短信
	 * @param selectSimIndex   当前使用哪种sim卡发短信校正流量
	 * @param phoneNo   运营商号码
	 * @param message    短信内容
	 */
	private void sendCorrectMsg(int selectSimIndex, String phoneNo, String message) {
	     try {
	    	 int subId = ToolsUtil.getIdInDbBySimId(mContext, selectSimIndex);//通过simId来获得subId
	    	 SmsManager sm = SmsManager.getSmsManagerForSubscriptionId(subId);
	    	 Intent smsIntent = new Intent();
	    	 smsIntent.setAction(Intents.SMS_RECEIVED_ACTION);
	    	 Log.e(TAG, TAG + ">>selectedIMSI>>>----subId---->>>>" + subId + "----->>>" + selectSimIndex);
	    	 PendingIntent sentIntent = PendingIntent.getActivity(mContext, 0, smsIntent, 0);
	    	 sm.sendTextMessage(phoneNo, null, message, sentIntent, null);
	    	 String imsi = ToolsUtil.getActiveSubscriberId(mContext, subId);
	    	 PreferenceUtil.putLong(mContext, imsi, PreferenceUtil.SIM_SUBID_SMS_KEY, System.currentTimeMillis());
	    	 PreferenceUtil.putString(mContext, imsi, PreferenceUtil.SMS_BODY_KEY, message + "," + phoneNo);
	    	 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
   public void analysisSMS(final int simIndex, final String smsBody) {
		 new AsyncTask<String, String, String>(){

				@Override
				protected String doInBackground(String... params) {
					if (null != TrafficCorrectionWrapper.getInstance()) {
						int state = TrafficCorrectionWrapper.getInstance().analysisSMS(simIndex, mQueryCode, mQueryPort, smsBody);
						Log.v(TAG, "analysisSMS>>>>" + state);
					}
					return null;
				}
				 
			 }.execute();
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
//			[599531, -1, -1, 1056542, -1, -1, -1, -1, -1]

			String currentIMSI = DataManagerApplication.mImsiArray[simIndex];
			//返回流量校正后的值以KB为单位
			//常规-总量
			if (trafficInfo[2] > 0) {
				PreferenceUtil.putInt(mContext, currentIMSI, PreferenceUtil.DATAPLAN_COMMON_KEY, (trafficInfo[2]));
			}
			//常规-剩余
			int commonTotal = PreferenceUtil.getInt(mContext, currentIMSI, PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
			if (trafficInfo[0] >= 0) {
				PreferenceUtil.putInt(mContext, currentIMSI, PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, trafficInfo[0]);
			}
			int commonRemain = PreferenceUtil.getInt(mContext, currentIMSI, PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, 0);
			//常规-已用
			if (trafficInfo[1] >= 0) {
				PreferenceUtil.putInt(mContext, currentIMSI, PreferenceUtil.USED_DATAPLAN_COMMON_KEY, trafficInfo[1]);
			} else {
				PreferenceUtil.putInt(mContext, currentIMSI, PreferenceUtil.USED_DATAPLAN_COMMON_KEY, commonTotal - commonRemain);
			}
			
			//闲时-总量
			if (trafficInfo[5] > 0) {
				PreferenceUtil.putInt(mContext, currentIMSI, PreferenceUtil.FREE_DATA_TOTAL_KEY, (trafficInfo[5]));
			}
			//闲时-剩余
			int freeTotal = PreferenceUtil.getInt(mContext, currentIMSI, PreferenceUtil.FREE_DATA_TOTAL_KEY, 0);
			if (trafficInfo[3] >= 0) {
				PreferenceUtil.putInt(mContext, currentIMSI, PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY, (trafficInfo[3]));
			} 
			int reamainFree = PreferenceUtil.getInt(mContext, currentIMSI, PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY, 0);
			//闲时-已用
			if (trafficInfo[4] >= 0) {
				PreferenceUtil.putInt(mContext, currentIMSI, PreferenceUtil.USED_FREE_DATA_TOTAL_KEY, trafficInfo[4]);
			} else {
				PreferenceUtil.putInt(mContext, currentIMSI, PreferenceUtil.USED_FREE_DATA_TOTAL_KEY, freeTotal - reamainFree);
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
