package com.monster.netmanage.receiver;

import static android.net.NetworkStats.SET_DEFAULT;
import static android.net.NetworkStats.SET_FOREGROUND;
import static android.net.NetworkStats.TAG_NONE;
import static android.net.NetworkStatsHistory.FIELD_RX_BYTES;
import static android.net.NetworkStatsHistory.FIELD_TX_BYTES;
import static android.net.NetworkTemplate.buildTemplateMobileAll;
import static android.net.NetworkTemplate.buildTemplateWifiWildcard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.monster.netmanage.DataManagerApplication;
import com.monster.netmanage.adapter.RangeAppAdapter;
import com.monster.netmanage.entity.AppItem;
import com.monster.netmanage.service.NetManagerService;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.StringUtil;
import com.monster.netmanage.utils.ToolsUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.os.AsyncTask;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

/**
 * 广播接收
 * 
 * @author zhaolaichao
 */
public class NetManagerReceiver extends BroadcastReceiver {

	private static final String TAG = "NetManagerReceiver" ;
	
	public static NetManagerReceiver mReceiver = new NetManagerReceiver();
	/**
	 * 更新应用管理流量数据
	 */
	private static final String ACTION_UPDATE_DATA = "com.monster.netmanage.action.updatedata";
	private static final String ACTION_SEND_UPDATE_DATA = "com.monster.netmanage.action.send.updatedata";
	public static final String ACTION_UPDATE_DATA_STATE = "com.monster.netmanage.action.updatedata_state";
	private static final String TAB_MOBILE = "mobile";
	private static final String TAB_WIFI = "wifi";
	 
	private Context mContext;
	 private NetworkTemplate mTemplate;
	 private INetworkStatsSession mStatsSession;
	 private INetworkStatsService mStatsService;
	 private INetworkManagementService mNetworkService;
	 private TelephonyManager mTelephonyManager;
	 private String[] mIMSIArray;
	 
	 private long mMobileData;
	 private long mWifiData;
	 private int mSlotIndex = -1;
	 private int mUid;
	 private boolean mMobileOk;
	 private boolean mWifiOk;
	 
	public static synchronized NetManagerReceiver getInstance() {
		return mReceiver;
	}
	
	public NetManagerReceiver() {
		super();
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		String action = intent.getAction();
		if (Intent.ACTION_DATE_CHANGED.equals(action)) {
			// 当Day发生变化时回调
			 String activeDataImsi = ToolsUtil.getActiveSimImsi(context);
			 PreferenceUtil.putBoolean(context, activeDataImsi, PreferenceUtil.NOTIFY_WARN_DAY_KEY, false);
			 PreferenceUtil.putBoolean(context, "", PreferenceUtil.DATE_CHANGE_KEY, false);
			 //当前月天数
			 int days = StringUtil.getMonthDays();
			 //清除日使用流量
			 for (int i = 0; i < DataManagerApplication.mImsiArray.length; i++) {
				 if (!TextUtils.isEmpty(DataManagerApplication.mImsiArray[i])) {
					 PreferenceUtil.putInt(context, DataManagerApplication.mImsiArray[i], PreferenceUtil.DAY_USED_STATS_KEY, 0);
					 //月结日
					 int closeDay = PreferenceUtil.getInt(context, DataManagerApplication.mImsiArray[i], PreferenceUtil.CLOSEDAY_KEY, 1);
					 if (closeDay == days) {
						 clearMonth(context, DataManagerApplication.mImsiArray[i]);
					 }
				 }
			}
		} else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
			//用于更新图标
			ToolsUtil.updateIconReceiver();
		} else if (ACTION_UPDATE_DATA_STATE.equals(action)) {
			try {
				
				mNetworkService = INetworkManagementService.Stub.asInterface(
						ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
				int uid = intent.getIntExtra("CHAGE_STATE_UID", 0);
				boolean state = intent.getBooleanExtra("CHAGE_STATE", false);
				if (!state) {
					mNetworkService.setUidDataRules(uid, true); //传入true代表要禁止其联网。
				} else {
					mNetworkService.setUidDataRules(uid, false); 
				}
				String data = PreferenceUtil.getString(context, "", RangeAppAdapter.TYPE_DATA, null);
				String[] uidArray = null;
				ArrayList<String> userList = new ArrayList<String>();
				if (!TextUtils.isEmpty(data)) {
					uidArray = data.split(",");
					Collections.addAll(userList, uidArray);
				}
				if (userList.contains("" + uid)) {
					if (!state) {
						return;
					} else {
						userList.remove("" + uid);
					}
				} else {
					userList.add("" + uid);
				}
				save(userList);
			} catch (RemoteException e) {
			      throw new RuntimeException(e);
			}
		} else if (ACTION_UPDATE_DATA.equals(action)) {
			try {
				 mUid = intent.getIntExtra("MOBILE_POLICY_UID", 0);
				 if (mUid == 0) return;
				 mIMSIArray = ToolsUtil.getIMSI(mContext);
				 mTelephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
				 mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
			     mStatsSession = mStatsService.openSession();
			     mSlotIndex = ToolsUtil.getCurrentNetSimSubInfo(mContext);
			     if ( mSlotIndex == -1) {
			    	 initStatus(0, TAB_WIFI);
			    	 mMobileOk = true;
			     } else {
			    	 initStatus(0, TAB_MOBILE);
			    	 initStatus(0, TAB_WIFI);
			     }
			} catch (RemoteException e) {
			      throw new RuntimeException(e);
			}
		}
	}

	/**
	 * 月结日到时清除上个月的数据
	 * @param context
	 * @param imsi
	 */
   private void clearMonth(Context context, String imsi) {
	    //已用套餐流量
	 	PreferenceUtil.putInt(context, imsi, PreferenceUtil.USED_DATAPLAN_COMMON_KEY, 0);
	 	//已用闲时流量
	 	PreferenceUtil.putInt(context, imsi, PreferenceUtil.USED_FREE_DATA_TOTAL_KEY, 0);
	 	//常规-剩余
	 	PreferenceUtil.putInt(context, imsi, PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, 0);
	 	//闲时-剩余
	 	PreferenceUtil.putInt(context, imsi, PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY, 0);
		  //清除日使用流量
	   	PreferenceUtil.putInt(context, imsi, PreferenceUtil.DAY_USED_STATS_KEY, 0);
   }
   
	private void save( ArrayList<String> saveList) {
		   if (saveList.size() == 0) {
			   PreferenceUtil.putString(mContext, "", RangeAppAdapter.TYPE_DATA, null);
	           return;
	       }
	       StringBuilder sb = new StringBuilder();
	       for (String item : saveList) {
	           sb.append(item).append(",");
	       }
	       Log.d(TAG, "sb:" + sb);
	       PreferenceUtil.putString(mContext, "", RangeAppAdapter.TYPE_DATA, sb.substring(0, sb.length() - 1));
    }
	/**
	 * @param isInit    是否要初始化
	 * @param currentIndex 当前卡索引
	 */
	private void initStatus(int currentIndex, String netType) {
		String simImsi = null;
		  if (TAB_MOBILE.equals(netType)) {
	            Log.d(TAG, "updateBody() mobile tab");
	            // Match mobile traffic for this subscriber, but normalize it to
	            // catch any other merged subscribers.
	            if (mIMSIArray.length  == 1) {
	            	simImsi = mIMSIArray[0];
	            } else {
	            	simImsi = ToolsUtil.getActiveSubscriberId(mContext, ToolsUtil.getIdInDbBySimId(mContext, currentIndex));
	            }
	            mTemplate = buildTemplateMobileAll(simImsi);
	            mTemplate = NetworkTemplate.normalize(mTemplate, mTelephonyManager.getMergedSubscriberIds());
	       } else if (TAB_WIFI.equals(netType)) {
	            // wifi doesn't have any controls
	            Log.d(TAG, "updateBody() wifi tab");
	            mTemplate = buildTemplateWifiWildcard();

	        } else {
	            Log.d(TAG, "updateBody() unknown tab");
	            throw new IllegalStateException("unknown tab: " + currentIndex);
	        }
		   // kick off loader for network history
	        // TODO: consider chaining two loaders together instead of reloading
	        // network history when showing app detail.
	        // kick off loader for detailed stats
		  //当前月结日
		  int closeDay = PreferenceUtil.getInt(mContext, simImsi, PreferenceUtil.CLOSEDAY_KEY, 1);
		  long start = StringUtil.getDayByMonth(closeDay);
		  long end = StringUtil.getDayByNextMonth(closeDay);
		  StatusTask statusTask = new StatusTask(mUid, start, end, mTemplate, netType, currentIndex);
		  statusTask.execute();
	}
	
	private class StatusTask extends AsyncTask<String, Void, Long> {

		private long start;
		private long end;
		private NetworkTemplate template;
		private String type;
		private int statusIndex;
		private int uid;
		public StatusTask(int uid, long start, long end, NetworkTemplate template, String type, int index) {
			super();
			this.uid = uid;
			this.start = start;
			this.end = end;
			this.template = template;
			this.type = type;
			this.statusIndex = index;
		}

		@Override
		protected Long doInBackground(String... params) {
			Long totalBytes = 0l;
			try {
				final long now = System.currentTimeMillis();
				NetworkStatsHistory.Entry entry = null;
				NetworkStatsHistory historyDefault = mStatsSession.getHistoryForUid(template, uid, SET_DEFAULT, TAG_NONE, FIELD_RX_BYTES | FIELD_TX_BYTES);
				NetworkStatsHistory historyForeGround = mStatsSession.getHistoryForUid(template, uid, SET_FOREGROUND, TAG_NONE, FIELD_RX_BYTES | FIELD_TX_BYTES);
				entry = historyDefault.getValues(start, end, now, entry);
				long backgroundBytes = entry.rxBytes + entry.txBytes;
				entry = null;
				entry = historyForeGround.getValues(start, end, now, entry);
				long foreGroundBytes = entry.rxBytes + entry.txBytes;
				totalBytes = backgroundBytes + foreGroundBytes;
			} catch (RemoteException e) {
				e.printStackTrace();
				return 0l;
			}
			return totalBytes;
		}

		@Override
		protected void onPostExecute(Long result) {
			super.onPostExecute(result);
			synchronized (NetManagerReceiver.class) {
				
				if (TAB_MOBILE.equals(type)) {
					mMobileOk = true;
					if (mIMSIArray.length > 1 && statusIndex < mIMSIArray.length - 1) {
						mMobileOk = false;
						statusIndex++;
						initStatus(statusIndex, TAB_MOBILE);
					} else if (mIMSIArray.length == 1) {
						mMobileOk = true;
					}
					mMobileData = result + mMobileData;
				} else if (TAB_WIFI.equals(type)) {
					mWifiOk = true;
					mWifiData = result + mWifiData;
				}
				
				if (mMobileOk && mWifiOk) {
					mMobileOk = false;
					mWifiOk = false;
					//发送到应用管理流量数据
					String wifi =  Formatter.formatFileSize(mContext, mWifiData);
					String mobile =  Formatter.formatFileSize(mContext, mMobileData);
					Intent intent = new Intent(ACTION_SEND_UPDATE_DATA);
					intent.putExtra("MOBILE_DATA", mobile);
					intent.putExtra("WIFI_DATA", wifi);
					mContext.sendBroadcast(intent);
					Log.v(TAG, "MOBILE_DATA>>" +  mMobileData + ">>WIFI_DATA>>" + mWifiData );
				}
			}
		}
	}
	
}
