package com.monster.netmanage.receiver;

import static android.net.NetworkTemplate.buildTemplateMobileAll;
import static android.net.NetworkTemplate.buildTemplateWifiWildcard;

import com.monster.netmanage.adapter.RangeAppAdapter;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.StringUtil;
import com.monster.netmanage.utils.ToolsUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;
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
	public static final String ACTION_UPDATE_DATA = "com.monster.netmanage.action.updatedata";
	private static final String ACTION_SEND_UPDATE_DATA = "com.monster.netmanage.action.send.updatedata";
	private static final String TAB_MOBILE = "mobile";
	private static final String TAB_WIFI = "wifi";
	 
	private Context mContext;
	 private NetworkTemplate mTemplate;
	 private INetworkStatsSession mStatsSession;
	 private INetworkStatsService mStatsService;
	 private TelephonyManager mTelephonyManager;
	 private String[] mIMSIArray;
	 
	 private long mMobileData;
	 private long mWifiData;
	 private int mSlotIndex = -1;
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
		} else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
			//用于更新图标
			ToolsUtil.updateIconReceiver();
		} else if (ACTION_UPDATE_DATA.equals(action)) {
			try {
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
		  StatusTask statusTask = new StatusTask(start, end, mTemplate, netType, currentIndex);
		  statusTask.execute();
	}
	
	private class StatusTask extends AsyncTask<NetworkStats, Void, NetworkStats> {

		private long start;
		private long end;
		private NetworkTemplate template;
		private String type;
		private int statusIndex;
		public StatusTask(long start, long end, NetworkTemplate template, String type, int index) {
			super();
			this.start = start;
			this.end = end;
			this.template = template;
			this.type = type;
			this.statusIndex = index;
		}

		@Override
		protected NetworkStats doInBackground(NetworkStats... params) {
			NetworkStats networkStats = null;
			try {
				networkStats = mStatsSession.getSummaryForAllUid(template, start, end, false);
			} catch (RemoteException e) {
				e.printStackTrace();
				return networkStats;
			}
			return networkStats;
		}

		@Override
		protected void onPostExecute(NetworkStats result) {
			super.onPostExecute(result);
			synchronized (NetManagerReceiver.class) {
				
				NetworkStats.Entry entry = null;
				NetworkStats stats = null;
				String mobile = null;
				String wifi = null;
				stats = result;
				final int size = stats != null ? stats.size() : 0;
				long usedData = 0;
				for (int i = 0; i < size; i++) {
					entry = stats.getValues(i, entry);
					Log.e(TAG, ">>>SET_DEFAULT>>>>>" + entry);
					usedData = entry.rxBytes + entry.txBytes;
					if (TAB_MOBILE.equals(type)) {
						mMobileData = mMobileData + usedData;
					} else if (TAB_WIFI.equals(type)) {
						mWifiData = mWifiData + usedData;
					}
				}
				
				if (TAB_MOBILE.equals(type)) {
					mMobileOk = true;
					if (mIMSIArray.length > 1 && statusIndex < mIMSIArray.length - 1) {
						mMobileOk = false;
						statusIndex++;
						initStatus(statusIndex, TAB_MOBILE);
					} else if (mIMSIArray.length == 1) {
						mMobileOk = true;
					}
				} else if (TAB_WIFI.equals(type)) {
					mWifiOk = true;
				}
				
				if (mMobileOk && mWifiOk) {
					mMobileOk = false;
					mWifiOk = false;
					//发送到应用管理流量数据
					wifi =  Formatter.formatFileSize(mContext, mWifiData);
					mobile =  Formatter.formatFileSize(mContext, mMobileData);
					String policyUids = PreferenceUtil.getString(mContext, "", RangeAppAdapter.TYPE_DATA, null);
					Intent intent = new Intent(ACTION_SEND_UPDATE_DATA);
					intent.putExtra("MOBILE_DATA", mobile);
					intent.putExtra("WIFI_DATA", wifi);
					intent.putExtra("MOBILE_POLICY_UIDS", policyUids);
					mContext.sendBroadcast(intent);
					Log.v(TAG, "MOBILE_DATA>>" +  mMobileData + ">>WIFI_DATA>>" + mWifiData );
				}
			}
		}
	}
	
}
