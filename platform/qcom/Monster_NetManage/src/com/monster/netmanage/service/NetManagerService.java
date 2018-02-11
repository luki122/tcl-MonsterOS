package com.monster.netmanage.service;

//import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED;
import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED_BACKGROUND;
import static android.net.NetworkStats.SET_DEFAULT;
import static android.net.NetworkTemplate.buildTemplateMobileAll;

import java.util.ArrayList;
import java.util.Collections;

import com.monster.netmanage.DataCorrect;
import com.monster.netmanage.DataManagerApplication;
import com.monster.netmanage.NotifyInfo;
import com.monster.netmanage.R;
import com.monster.netmanage.activity.DataRangeActivity;
import com.monster.netmanage.activity.MainActivity;
import com.monster.netmanage.receiver.NetManagerReceiver;
import com.monster.netmanage.utils.NotificationUtil;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.StringUtil;
import com.monster.netmanage.utils.ToolsUtil;
import com.monster.netmanage.view.NetManageDialogView;
import com.monster.netmanage.view.NetManageDialogView.ICheckListener;
import com.mst.tms.NetInfoEntity;

import android.app.AppOpsManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicyManager;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Telephony.Sms;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Toast;
import mst.app.dialog.AlertDialog.Builder;
import tmsdkobf.ha;

/**
 * 后台服务
 * 
 * @author zhaolaichao
 *
 */
public class NetManagerService extends Service {

	private static final String TAG = "NetManagerService";
	public static final String UPDATE_UI = "com.monster.netmanage.update.ui";
	private static final Uri SMS_URI = Uri.parse("content://sms/");
	/**
	 * 切换上网卡
	 */
	private static final Uri DATA_CHANGED_URI = Uri.parse("content://settings/global");
	/**
	 * 单位时间内（1分钟）后台跑了至少10M流量
	 */
	private static final long MAX_MINUTE_BGDATA = 10 * 1024 * 1024; 
	/**
	 * 单位时间内（1分钟）
	 */
	private static final int UNIT_MINUTE = 60 * 1000; 
	/**
	 * 提示单位时间内（1分钟）
	 */
	private static final int WARN_INFO_DIALOG = 1000; 
	/**
	 * 流量监控提示
	 */
	private static final int DATA_NOTIFI  = 1001;
	/**
	 * 解析短信
	 */
	private static final int SMS_NOTIFI  = 1002;
	
	/**
	 * 统计月使用量
	 */
	private static final int DATA_STATUS_MONTH = 1;
	/**
	 * 统计日使用量
	 */
	private static final int DATA_STATUS_DAY = 2;
	/**
	 * 统计每分钟后台使用量
	 */
	private static final int DATA_STATUS_BG = 3;
	/**
	 * 每日已用流量的最大限制
	 */
	private static final float LIMIT_RATE_DAY = 0.5f;
	/**
	 * 每月已用流量的最大限制
	 */
	private static final float LIMIT_RATE_MONTH = 0.99F;
	
	private NetworkPolicyManager mPolicyManager;
	private NetworkTemplate mTemplate;
	private INetworkStatsSession mStatsSession;
	private INetworkStatsService mStatsService;
	private TelephonyManager mTelephonyManager;
	
	private DataCorrect mDataCorrect;

	private mst.app.dialog.AlertDialog.Builder mBuilder;
	private mst.app.dialog.AlertDialog mAlertDialog;
	
	private StatsDataTask mTaskBg;
	private StatsDataTask mTaskDay;
	private StatsDataTask mTaskMonth;
	private StatsFreeDataTask mTaskFreeMonth;
	
	private boolean mFreeStaus;
	private boolean mUpdateMain;
	private static NetInfoEntity mInfoEntity = new NetInfoEntity();
	
	private Callback callback;  
	
	private  long mFreeTotalForMonth = 0;
	private long mDataForMinute = 0;
	
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case WARN_INFO_DIALOG:
				//单位时间内（1分钟)提示
				long usedDataBg = (Long) msg.obj;
				showInfoMinute(NetManagerService.this, usedDataBg);
				break;
			case DATA_NOTIFI:
				showNotifyMsg(NetManagerService.this, mInfoEntity);
				showDataDialogInfo(NetManagerService.this, mInfoEntity);
				if (callback != null){  
					String activeSimImsi = null;
					if (msg.obj != null) {
						activeSimImsi = (String) msg.obj;
						callback.onDataChange(activeSimImsi, mInfoEntity);  
					}
				}  
			case SMS_NOTIFI:
				int subId = msg.arg1;
				String  imsi = ToolsUtil.getActiveSubscriberId(NetManagerService.this, subId);
				String[] imsiArray = DataManagerApplication.getInstance().mImsiArray;
				if (null == imsiArray) {
					imsiArray = ToolsUtil.getIMSI(NetManagerService.this);
				}
				int smsIndex = -1;
				for (int i = 0; i < imsiArray.length; i++) {
					if (TextUtils.equals(imsi, imsiArray[i])) {
						smsIndex = i;
						break;
					}
				}
				Log.v(TAG, "subId>>>" + subId + ">>imsi>>" + imsi);
				DataCorrect.getInstance().analysisSMS(smsIndex, String.valueOf(msg.obj));
				break;
			default:
				break;
			}
		};
	};
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return new Binder();
	}

	public class Binder extends android.os.Binder{  
        public NetManagerService getMyService(){ 
        	handler.removeCallbacks(statsRn);
    		//初始化更新主界面
    		mUpdateMain = false;
    		handler.post(statsRn);
            return NetManagerService.this;  
        }  
    }  
	
	@Override
	public void onCreate() {
		super.onCreate();
		initStats();
		if (mDataCorrect == null) {
			mDataCorrect = DataCorrect.getInstance();
		}
		registerSmsObserver();
		registerObserver();
//		handler.postDelayed(statsRn, UNIT_MINUTE);
		handler.removeCallbacks(statsRn);
		new Thread(statsRn).start();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		correctMsg(intent);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != mAlertDialog) {
			mAlertDialog.dismiss();
			mAlertDialog = null;
		}
		unRegisterObserver();
		unRegisterSmsObserver();
		if (mTaskMonth != null) {
			mTaskMonth.cancel(true);
			mTaskMonth = null;
		}
		if (mTaskDay != null) {
			mTaskDay.cancel(true);
			mTaskDay = null;
		}
		if (mTaskBg != null) {
			mTaskBg.cancel(true);
			mTaskBg = null;
		}
		if (mTaskFreeMonth != null) {
			mTaskFreeMonth.cancel(true);
			mTaskFreeMonth = null;
		}
		statsRn = null;
		TrafficStats.closeQuietly(mStatsSession);
	}
	
	public void setCallback(Callback callback) {  
	    this.callback = callback;  
	}  
	  
	public Callback getCallback() {  
	    return callback;  
	}  
	
	public static interface Callback{  
        void onDataChange(String simImsi, NetInfoEntity netInfoEntity);  
    }  
	
	/**
	 * 发送流量校正短信
	 */
	private void correctMsg(Intent intent) {
		if (null != intent) {
			String imsi = intent.getStringExtra("CURRENT_IMSI");
			Log.v(TAG, "correctMsg>>" + imsi);
			if ( !TextUtils.isEmpty(imsi)) {
				int simSlotIndex = ToolsUtil.getCurrentNetSimSubInfo(this);
				if (simSlotIndex != -1) {
					mDataCorrect.startCorrect(NetManagerService.this, false, simSlotIndex);
				}
			}
		}
	}
	
	/**
	 * 监听切换上网卡
	 */
	private void registerObserver() {
		// 在这里启动ToolsUtil
	     ContentResolver resolver = getContentResolver();
	     resolver.registerContentObserver(DATA_CHANGED_URI, true, observer);
	}
	
	private void unRegisterObserver() {
		ContentResolver resolver = getContentResolver();
		resolver.unregisterContentObserver(observer);
	}
	
	private ContentObserver observer = new ContentObserver(new Handler()){

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			try {
				//切换上网卡
				String activeSimImsi = ToolsUtil.getActiveSimImsi(NetManagerService.this);
				if (!TextUtils.isEmpty(activeSimImsi)) {
					Intent updateIntent = new Intent(UPDATE_UI);
					updateIntent.putExtra("NET_IMSI", activeSimImsi);
					Thread.sleep(500);
					sendBroadcast(updateIntent);
				}
				String saveActiveSimImsi = PreferenceUtil.getString(NetManagerService.this, "", PreferenceUtil.CURRENT_ACTIVE_IMSI_KEY, null);
				if (!TextUtils.equals(saveActiveSimImsi, activeSimImsi)) {
					Log.v(TAG, "ContentObserver>>mContext>>" + activeSimImsi);
					NotificationUtil.clearNotify(NetManagerService.this, NotificationUtil.TYPE_NORMAL);
					NotifyInfo.showNotify(NetManagerService.this);
					//更新统计流量信息
					handler.removeCallbacks(statsRn);
					initStats();
					mInfoEntity = new NetInfoEntity();
					if (null != callback) {
						callback.onDataChange(activeSimImsi, mInfoEntity);  
					}
					//初始化更新主界面
					handler.post(statsRn);
					mUpdateMain = false;
				}
				PreferenceUtil.putString(NetManagerService.this, "", PreferenceUtil.CURRENT_ACTIVE_IMSI_KEY, activeSimImsi);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	};
	
	/**
	 * 监听短信
	 */
	private void registerSmsObserver() {
		// 在这里启动
	     ContentResolver resolver = this.getContentResolver();
	     resolver.registerContentObserver(SMS_URI, true, smsObserver);
	}
	
	private void unRegisterSmsObserver() {
		ContentResolver resolver = getContentResolver();
		resolver.unregisterContentObserver(smsObserver);
	}
	
	public ContentObserver smsObserver = new ContentObserver(handler){
	 
	   @Override
	    public void onChange(boolean selfChange, Uri uri) { 
	        super.onChange(selfChange, uri); 
	        Log.v(TAG, "onchange>>uri>>" + uri);
	        if (uri.toString().equals("content://sms/raw")) {  
	            return;  
	        } 
	        Cursor cursor = null; 
	        AppOpsManager appOpsManager = null;
	        int subId = 0;
	        String imsi = null;
	        try {
	          //读取收件箱中的短信  请求默认短信应用权限
	          String where = " date >  " + (System.currentTimeMillis() - 5 * 1000);
	          String[] projection = new String[] { "body", "address", "person", "thread_id","_id", "sub_id", "type" };
	          cursor = getContentResolver().query(uri, projection, where, null, "date desc"); 
	          Log.v(TAG, "cursor>>getCount>>" + cursor.getCount());
	          if (cursor != null && cursor.getCount() > 0) { 
	        	  appOpsManager = (AppOpsManager) getSystemService("appops");
	        	  appOpsManager.setMode(15, android.os.Process.myUid(), getPackageName(), 0);
//	            	 _id, thread_id, address, person, date, date_sent, protocol, read, status, type, reply_path_present, subject, body, service_center, locked, sub_id, error_code, creator, seen, priority, timezone, car_code]
	               if (cursor.moveToNext()) {
	                    String number = cursor.getString(cursor.getColumnIndex("address"));//手机号
	                    subId = cursor.getInt(cursor.getColumnIndex("sub_id"));
	                    int id = cursor.getInt(cursor.getColumnIndex("_id"));
	                    int type = cursor.getInt(cursor.getColumnIndex("type"));
	                    String body = cursor.getString(cursor.getColumnIndex("body"));
	                    imsi = ToolsUtil.getActiveSubscriberId(NetManagerService.this, subId);
	                    long sentMsgTime = PreferenceUtil.getLong(NetManagerService.this, imsi, PreferenceUtil.SIM_SUBID_SMS_KEY, System.currentTimeMillis());
	                    String bodyPhoneNo = PreferenceUtil.getString(NetManagerService.this, imsi, PreferenceUtil.SMS_BODY_KEY, null);
	                    if (TextUtils.isEmpty(bodyPhoneNo) || !bodyPhoneNo.contains(",")) {
	                    	return;
	                    }
	                    String[] smsContent = bodyPhoneNo.split(",");
	                    if (!TextUtils.isEmpty(body) && number.equals(smsContent[1])){
	                    	if (number.startsWith("10086") || number.startsWith("1000") || number.startsWith("10010")) {
	                    		Log.v(TAG, "onchange>>type>>" + type);
	                    		if (type == Sms.MESSAGE_TYPE_INBOX) {
	                    			//收件箱
	                    			Message msg = handler.obtainMessage(SMS_NOTIFI);
	                    			msg.obj = body;
	                    			msg.arg1 = subId;
	                    			msg.sendToTarget();	
	                    		}
	                    		int count = 0;
	                    		if (type == Sms.MESSAGE_TYPE_SENT && body.equals(smsContent[0]) ) {
	                    			Log.v(TAG, "发送相隔时间>>>" + (System.currentTimeMillis() - sentMsgTime));
	                    			if (System.currentTimeMillis() - sentMsgTime < 8 * 1000) {
	                    				//删除发送短信间隔为8s 
	                    				count = getContentResolver().delete(SMS_URI, "_id=?", new String[]{String.valueOf(id)});
	                    			}
	                    		} else if (type == Sms.MESSAGE_TYPE_INBOX) {
	                    			Log.v(TAG, "接收相隔时间>>1111>" + (System.currentTimeMillis() - sentMsgTime));
	                    			if (System.currentTimeMillis() - sentMsgTime < 2 * 60 * 1000) {
	                    				//删除接收校正短信间隔为2min 
	                    				count = getContentResolver().delete(SMS_URI, "_id=?", new String[]{String.valueOf(id)});
	                    			}
	        	                   PreferenceUtil.putLong(NetManagerService.this, "" + subId, PreferenceUtil.SIM_SUBID_SMS_KEY, System.currentTimeMillis());
	        	      	    	   PreferenceUtil.putString(NetManagerService.this, "" + subId, PreferenceUtil.SMS_BODY_KEY, null);
	                    		}
	                    		Log.v(TAG, "count>>>>>" + count);
	                    	}
	                    }
	                    Log.e(TAG, "监听短信内容>>" + body + ">>>>subId>>>" + subId + ">>>>>number>>" + number);
	               } 
	               //释放默认短信应用权限
	               appOpsManager.setMode(15, android.os.Process.myUid(), getPackageName(), 2);
	           } 
	        } catch(Exception e) {
	            e.printStackTrace();
	            PreferenceUtil.putLong(NetManagerService.this, imsi, PreferenceUtil.SIM_SUBID_SMS_KEY, System.currentTimeMillis());
   	    	    PreferenceUtil.putString(NetManagerService.this, imsi, PreferenceUtil.SMS_BODY_KEY, null);
	        } finally {
	            if (cursor != null) cursor.close();
	        }
	    }
	};
	
	private void initStats() {
		int simSlotIndex = ToolsUtil.getCurrentNetSimSubInfo(NetManagerService.this);
		if (simSlotIndex == -1) {
		    return;
		}
		if (null == mPolicyManager) {
			mPolicyManager = NetworkPolicyManager.from(this);
		}
		if (null == mStatsService) {
			mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
		}
		if (null == mTelephonyManager) {
			mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		}
	    try {
	    	if (null == mStatsSession) {
	    		mStatsSession = mStatsService.openSession();
	    	}
	    } catch (RemoteException e) {
	          throw new RuntimeException(e);
	    }
		Log.d(TAG, "updateBody() mobile tab>>>>>" + mStatsSession);
        
		// Match mobile traffic for this subscriber, but normalize it to
		// catch any other merged subscribers.
		mTemplate = buildTemplateMobileAll(ToolsUtil.getActiveSimImsi(NetManagerService.this));
		mTemplate = NetworkTemplate.normalize(mTemplate, mTelephonyManager.getMergedSubscriberIds());
	}
	
	private Runnable statsRn = new Runnable() {
		
		@Override
		public void run() {
			String netWorkType = ToolsUtil.getNetWorkType(NetManagerService.this);
			if (null != mTemplate) {
				Log.v(TAG, "开始发送统计");
				handler.removeCallbacks(statsRn);
				String activeDataImsi = ToolsUtil.getActiveSimImsi(NetManagerService.this);
				mFreeStaus = PreferenceUtil.getBoolean(NetManagerService.this, activeDataImsi, PreferenceUtil.FREE_DATA_STATE_KEY, false);
				try {
					if (!mUpdateMain) {
						//初始化统计闲时流量
						mTaskFreeMonth = new StatsFreeDataTask();
						mTaskFreeMonth.execute();
						Thread.sleep(80);
					}
					if (mUpdateMain) {
						//非移动网络下不统计
						if (!ToolsUtil.NET_TYPE_MOBILE.equals(netWorkType)) {
							handler.postDelayed(statsRn, UNIT_MINUTE);
							return;
						}
					}
					if (mUpdateMain) {
						mDataForMinute = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes() - mDataForMinute;
						Message message = handler.obtainMessage();
						mInfoEntity.setmUsedForMinute(mDataForMinute);
						message.obj = activeDataImsi;
						message.what = DATA_NOTIFI;
						handler.sendMessage(message);
						Log.v(TAG, "每分钟数据" + "entry111111111>>>" + Formatter.formatFileSize(NetManagerService.this, mDataForMinute));
					}
					mTaskDay = new StatsDataTask(DATA_STATUS_DAY, activeDataImsi);
					mTaskDay.execute();
					Thread.sleep(80);
					mTaskMonth = new StatsDataTask(DATA_STATUS_MONTH, activeDataImsi);
				    mTaskMonth.execute();
					Thread.sleep(80);
					mTaskBg = new StatsDataTask(DATA_STATUS_BG, activeDataImsi);
					mTaskBg.execute();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (mUpdateMain) {
					mDataForMinute = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();
					handler.postDelayed(statsRn, UNIT_MINUTE);
				} else {
					//初始化更新主界面
					mDataForMinute = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();
					handler.post(statsRn);
				}
				mUpdateMain = true;
		      }
		}
	};
	
	private class StatsDataTask extends AsyncTask<NetworkStats, Void, NetworkStats> {

		private int statsType;
		private String activeDataImsi;
		public StatsDataTask(int statsType, String imsi) {
			super();
			this.statsType = statsType;
			this.activeDataImsi = imsi;
		}

		@Override
		protected NetworkStats doInBackground(NetworkStats... params) {
		    int simSlotIndex = ToolsUtil.getCurrentNetSimSubInfo(NetManagerService.this);
			if (simSlotIndex == -1) {
			  return null;
			}
			long start = 0;
			long end = 0;
			switch (statsType) {
			case DATA_STATUS_MONTH:
				int closeDay = PreferenceUtil.getInt(NetManagerService.this,  activeDataImsi, PreferenceUtil.CLOSEDAY_KEY, 1);
				start = StringUtil.getDayByMonth(closeDay);
				end = StringUtil.getDayByNextMonth(closeDay);
				break;
			case DATA_STATUS_DAY:
				if (mFreeStaus) {
					//开始时间
					String freeStartTime = PreferenceUtil.getString(NetManagerService.this, activeDataImsi, PreferenceUtil.FREE_DATA_START_TIME_KEY, "00:00");
					//结束时间
					String freeEndTime = PreferenceUtil.getString(NetManagerService.this, activeDataImsi, PreferenceUtil.FREE_DATA_END_TIME_KEY, "5:00");
					int startHour = Integer.parseInt(freeStartTime.split(":")[0]);
					int startMinute = Integer.parseInt(freeStartTime.split(":")[1]);
					int endHour = Integer.parseInt(freeEndTime.split(":")[0]);
					int endMinute = Integer.parseInt(freeEndTime.split(":")[1]);
					//闲时时间
					start = StringUtil.getStartTime(endHour, endMinute, 0);
					end = StringUtil.getEndTime(startHour, startMinute, 0);
				} else {
					start = StringUtil.getStartTime(0, 0, 0);
					end = StringUtil.getEndTime(23, 59, 59);
				}
				break;
			case DATA_STATUS_BG:
				boolean dataInfoState = PreferenceUtil.getBoolean(NetManagerService.this, activeDataImsi, PreferenceUtil.MINUTE_DATA_USED_DIALOG_KEY, false);
				if (dataInfoState) {
					return null;
				}
				end = System.currentTimeMillis();
				start = end - UNIT_MINUTE;
				break;
			default:
				break;
			}
			NetworkStats networkStats = null;
			try {
				  mStatsService.forceUpdate();
				  networkStats = mStatsSession.getSummaryForAllUid(mTemplate, start, end, false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return networkStats;
		}

		@Override
		protected void onPostExecute(NetworkStats result) {
			super.onPostExecute(result);
			NetworkStats.Entry entry = null;
			NetworkStats networkStats = result;
			long usedDataBg = 0;
			long usedDataTotal = 0;
//			synchronized (NetManagerService.class) {
				int size = networkStats != null ? networkStats.size() : 0;
				if (null != networkStats) {
					for (int i = 0; i < size; i++) {
						entry = networkStats.getValues(i, entry);
						//去除定向流量
						boolean isExit = false;
						if (DATA_STATUS_DAY == statsType || DATA_STATUS_MONTH == statsType) {
							ArrayList<String> orientAppUids = getOrientApps(activeDataImsi);
							//统计前台和后台数据
							if (null != orientAppUids && orientAppUids.size() > 0) {
								//过滤定向应用流量
								for (int j = 0; j < orientAppUids.size(); j++) {
									if (entry.uid == Integer.parseInt(orientAppUids.get(j))) {
										isExit = true;
										break;
									}
								}
							}
						}
						if (!isExit) {
							usedDataTotal = usedDataTotal + entry.rxBytes + entry.txBytes;
						}
						if (statsType == DATA_STATUS_BG) {
							if (SET_DEFAULT == entry.set) {
								//统计后台数据
								long  usedData = entry.rxBytes + entry.txBytes;
								usedDataBg = usedData + usedDataBg;
							}
						}
					}
					Message msg = handler.obtainMessage();
					switch(statsType) {
					case DATA_STATUS_MONTH:
						usedDataTotal = usedDataTotal - mInfoEntity.mFreeUsedForMonth;
						mInfoEntity.setmUsedForMonth(usedDataTotal);
						msg.obj = activeDataImsi;
						msg.what = DATA_NOTIFI;
						Log.v(TAG, "当月所用数据" + "entry.rxBytes>>>" + Formatter.formatFileSize(NetManagerService.this, usedDataTotal));
						int commonTotalData = PreferenceUtil.getInt(NetManagerService.this, activeDataImsi, PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
						int commonUsedData = PreferenceUtil.getInt(NetManagerService.this, activeDataImsi, PreferenceUtil.USED_DATAPLAN_COMMON_KEY, 0);
						if (commonUsedData == 0) {
							PreferenceUtil.putInt(NetManagerService.this, activeDataImsi, PreferenceUtil.USED_DATAPLAN_COMMON_KEY, (int)usedDataTotal / 1024);
							PreferenceUtil.putInt(NetManagerService.this, activeDataImsi, PreferenceUtil.REMAIN_DATAPLAN_COMMON_KEY, commonTotalData - (int)usedDataTotal / 1024);
						}
						handler.sendMessage(msg);
						break;
					case DATA_STATUS_DAY:
						mInfoEntity.setmUsedForDay(usedDataTotal);
						msg.obj = activeDataImsi;
						msg.what = DATA_NOTIFI;
						Log.v(TAG, "当天所用数据" + "entry>>>" + Formatter.formatFileSize(NetManagerService.this, usedDataTotal));
						PreferenceUtil.putInt(NetManagerService.this, activeDataImsi, PreferenceUtil.DAY_USED_STATS_KEY, (int)usedDataTotal / 1024);
						handler.sendMessage(msg);
						break;
					case DATA_STATUS_BG:
						msg.obj = usedDataBg;
						msg.what = WARN_INFO_DIALOG;
						handler.sendMessage(msg);
						Log.v(TAG, "统计后台数据" + "entry>>>" + Formatter.formatFileSize(NetManagerService.this, usedDataBg));
						break;
					}
				}
//			}
		}
		
	}
	
	/**
	 * 统计闲时流量
	 * @author zhaolaichao
	 *
	 */
	private class StatsFreeDataTask extends AsyncTask<NetworkStats, Void, NetworkStats> {

		@Override
		protected NetworkStats doInBackground(NetworkStats... params) {
			String activeDataImsi = ToolsUtil.getActiveSimImsi(NetManagerService.this);
			int closeDay = PreferenceUtil.getInt(NetManagerService.this, activeDataImsi, PreferenceUtil.CLOSEDAY_KEY, 1);
			//从月结日到当前一共多少天
		    int count = StringUtil.getDaysByCloseDay(closeDay);
		    Log.v(TAG, "从月结日到当前一共多少天>>" + count);
		    int total = 0;
		    mFreeTotalForMonth = 0;
			try {
				//开始时间
				String freeStartTime = PreferenceUtil.getString(NetManagerService.this, activeDataImsi, PreferenceUtil.FREE_DATA_START_TIME_KEY, "23:00");
				//结束时间
				String freeEndTime = PreferenceUtil.getString(NetManagerService.this, activeDataImsi, PreferenceUtil.FREE_DATA_END_TIME_KEY, "5:00");
				int startHour = Integer.parseInt(freeStartTime.split(":")[0]);
				int startMinute = Integer.parseInt(freeStartTime.split(":")[1]);
				int endHour = Integer.parseInt(freeEndTime.split(":")[0]);
				int endMinute = Integer.parseInt(freeEndTime.split(":")[1]);
				if (total <= count) {
					statsFree(closeDay, count, total, startHour, startMinute, endHour, endMinute);
					Log.v(TAG, "开时统计每天>>" + total);
				}
				mInfoEntity.setmFreeUsedForMonth(mFreeTotalForMonth);
				int freeTotalData = PreferenceUtil.getInt(NetManagerService.this, activeDataImsi, PreferenceUtil.FREE_DATA_TOTAL_KEY, 0);
				int freeUsedData = PreferenceUtil.getInt(NetManagerService.this, activeDataImsi, PreferenceUtil.USED_FREE_DATA_TOTAL_KEY, 0);
				if (freeUsedData == 0) {
					PreferenceUtil.putInt(NetManagerService.this, activeDataImsi, PreferenceUtil.USED_FREE_DATA_TOTAL_KEY, (int)mFreeTotalForMonth / 1024);
					PreferenceUtil.putInt(NetManagerService.this, activeDataImsi, PreferenceUtil.REMAIN_FREE_DATA_TOTAL_KEY, freeTotalData - (int)mFreeTotalForMonth / 1024);
				}
				Log.v(TAG, "当月所用闲时流量数据" + "entry.rxBytes>>>" + Formatter.formatFileSize(NetManagerService.this, mFreeTotalForMonth));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(NetworkStats result) {
			super.onPostExecute(result);
		}
		
	}
	
	/**
	 * 统计闲时流量
	 * @param closeDay
	 * @param count
	 * @param total
	 * @param startHour
	 * @param startMinute
	 * @param endHour
	 * @param endMinute
	 * @param freeTotalForMonth
	 */
	private void statsFree(int closeDay, int count, int total, int startHour, int startMinute, int endHour, int endMinute) {
		try {
			NetworkStats.Entry entry = null;
			//统计每天的闲时流量
			long startMorning = StringUtil.getDayByCloseDay(closeDay + total, 0, 0, 0);
			long endMorning  = StringUtil.getDayByCloseDay(closeDay + total, endHour, endMinute, 999);
			NetworkStats statsMorning = mStatsSession.getSummaryForAllUid(mTemplate, startMorning, endMorning, false);
			int size = statsMorning != null ? statsMorning.size() : 0;
			if (null != statsMorning) {
				for (int i = 0; i < size; i++) {
					entry = statsMorning.getValues(i, entry);
					mFreeTotalForMonth = mFreeTotalForMonth + entry.rxBytes + entry.txBytes;
				}
			}
			long startNight = StringUtil.getDayByCloseDay(closeDay + total, startHour, startMinute, 0);
			long endNight  = StringUtil.getDayByCloseDay(closeDay + total, 23, 59, 999);
			NetworkStats statsNight = mStatsSession.getSummaryForAllUid(mTemplate, startNight, endNight, false);
			size = statsNight != null ? statsNight.size() : 0;
			if (null != statsNight) {
				for (int i = 0; i < size; i++) {
					entry = statsNight.getValues(i, entry);
					mFreeTotalForMonth = mFreeTotalForMonth + entry.rxBytes + entry.txBytes;
				}
			}
			total ++;
			if (total <= count) {
				statsFree(closeDay, count, total, startHour, startMinute, endHour, endMinute);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	/**
	 * 获得定向应用
	 * @param imsi
	 * @return
	 */
	private ArrayList<String> getOrientApps(String imsi) {
		  ArrayList<String> addUidList = null;
		  String addUids = PreferenceUtil.getString(this, imsi, PreferenceUtil.ORIENT_APP_ADDED_KEY, "");
		  if (addUids.contains(",")) {
			  String[] addUidsArray = addUids.split(",");
			  if (null != addUidsArray &&  addUidsArray.length > 0) {
				  addUidList = new ArrayList<String>();
				  Collections.addAll(addUidList, addUidsArray);
			  }
		  }
		  return addUidList;
	}
	
	/**
	 * 每分钟统计一次
	 * 后台数据流量提示:单位时间内（1分钟）后台跑了至少10M流量则满足提示条件
	 */
	private void showInfoMinute(Context context, long usedDataBg) {
		String activeSimImsi = ToolsUtil.getActiveSimImsi(context);
        long lastMinuteData = PreferenceUtil.getLong(context, activeSimImsi, PreferenceUtil.MINUTE_DATA_USED_KEY, 0);
        if (usedDataBg != lastMinuteData &&  usedDataBg >= MAX_MINUTE_BGDATA) {
        	handler.removeCallbacks(statsRn);
        	warnInfoDialog(context, String.format(context.getString(R.string.data_bg_use_info), Formatter.formatFileSize(context, MAX_MINUTE_BGDATA)));
        }
        PreferenceUtil.putLong(context, activeSimImsi, PreferenceUtil.MINUTE_DATA_USED_KEY, usedDataBg);
	}
	
	/**
	 * 每分钟统计一次
	 * @param msg
	 * @param uid
	 */
    private void warnInfoDialog(Context context, String msg) {
    	final String activeSimImsi = ToolsUtil.getActiveSimImsi(context);
    	boolean dataInfoState = PreferenceUtil.getBoolean(context, activeSimImsi, PreferenceUtil.MINUTE_DATA_USED_DIALOG_KEY, false);
    	if (dataInfoState) {
    		return;
    	}
    	if (mAlertDialog != null && mAlertDialog.isShowing()){
    		return;
    	}
        context = ((Context)NetManagerService.this);
    	context.setTheme(com.mst.R.style.Theme_Mst_Material_Light);
    	mBuilder = new mst.app.dialog.AlertDialog.Builder(context);
    	mBuilder.setCancelable(false);
    	mBuilder.setTitle(context.getString(R.string.data_bg_warning_info));
    	NetManageDialogView dialogView = new NetManageDialogView(context);
    	dialogView.setMessage(msg);
    	dialogView.setOnCheckListener(new ICheckListener() {
			
			@Override
			public void setOnCheckListener(CompoundButton buttonView, boolean isChecked) {
				Log.v(TAG, "不再提示>>" + isChecked);
				if (!isChecked) {
					handler.postDelayed(statsRn, UNIT_MINUTE);
				}
				PreferenceUtil.putBoolean(NetManagerService.this, "", PreferenceUtil.WARN_DATA_USED_KEY, isChecked);
			}
		});
    	mBuilder.setView(dialogView); 
    	mBuilder.setPositiveButton(context.getString(R.string.konw), new mst.app.dialog.AlertDialog.OnClickListener() {
 	          @Override
 	          public void onClick(DialogInterface dialog, int which) {
 	        	 handler.postDelayed(statsRn, UNIT_MINUTE);
 	             dialog.dismiss();
 	          }
 	      })
 	      .setNegativeButton(context.getString(R.string.see), new mst.app.dialog.AlertDialog.OnClickListener() {
 	           @Override
 	           public void onClick(DialogInterface dialog, int which) {
 	        	   int currentNetSimIndex = ToolsUtil.getCurrentNetSimSubInfo(NetManagerService.this);
 	        	   if (currentNetSimIndex != -1) {
 	        		   //进入后台消耗流量详情界面
 	        		   Intent bgIntent = new Intent(NetManagerService.this, DataRangeActivity.class);
 	        		   bgIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 	        		   bgIntent.putExtra("SIM_TITLE", currentNetSimIndex == 0 ? NetManagerService.this.getString(R.string.sim1) : NetManagerService.this.getString(R.string.sim2));
 	        		   bgIntent.putExtra("STATS_POLICY", POLICY_REJECT_METERED_BACKGROUND);
 	        		   bgIntent.putExtra("CURRENT_INDEX", currentNetSimIndex);
 	        		  NetManagerService.this.startActivity(bgIntent);
 	        	   }
 	        	  handler.postDelayed(statsRn, UNIT_MINUTE);
 	              dialog.dismiss();
 	            }
 	      });
    	if (null == mAlertDialog) {
    		mAlertDialog = mBuilder.create();
    		mAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    	}
    	if (null != mAlertDialog && !mAlertDialog.isShowing()) {
    		mAlertDialog.show();
    	}
     }
    
    /**
	 * 通知栏中的提示
	 * @param networkInfoEntity
	 */
	private void showNotifyMsg(Context context, NetInfoEntity networkInfoEntity) {
		 int simSlotIndex = ToolsUtil.getCurrentNetSimSubInfo(context);
		 String activeDataImsi = ToolsUtil.getActiveSimImsi(context);
         boolean warnState = PreferenceUtil.getBoolean(context, activeDataImsi, PreferenceUtil.PASS_WARNING_STATE_KEY, false);
         //全部流量:通用 KB为单位
          int total = PreferenceUtil.getInt(context, activeDataImsi, PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
          int usedTotal = PreferenceUtil.getInt(context, activeDataImsi, PreferenceUtil.USED_DATAPLAN_COMMON_KEY, 0);
          String warnValue = PreferenceUtil.getString(context, activeDataImsi, PreferenceUtil.PASS_WARNING_VALUE_KEY, "85%");
          String rateStr = warnValue.substring(0, warnValue.indexOf("%"));
          long usedForMonth =  (usedTotal * 1024  - networkInfoEntity.mUsedForMonth) > 0 ?  usedTotal * 1024 : networkInfoEntity.mUsedForMonth;
          if (warnState && total > 0 && usedForMonth * 100 / (total * 1024) >= Integer.parseInt(rateStr)) {
        	  //月结日
	          int closeDay = PreferenceUtil.getInt(context, activeDataImsi, PreferenceUtil.CLOSEDAY_KEY, 1);
        	  long timeByNextMonth = StringUtil.getDayByNextMonth(closeDay);
        	  long timeNow = System.currentTimeMillis();
        	  if (timeNow >= timeByNextMonth) {
        		  //超过当前月结日周期清除提示框标志
	        	  PreferenceUtil.putBoolean(context, activeDataImsi, PreferenceUtil.NOTIFY_WARN_MONTH_KEY, false);
        	  } else {
        		  //当在此月结日周期内所用流量超过用户设置的最大阀值时则提示,每个月最多提示一次
        		  //跳转到对应的流量排行界面
        		  boolean notify = PreferenceUtil.getBoolean(context, activeDataImsi, PreferenceUtil.NOTIFY_WARN_MONTH_KEY, false);
        		  if (notify) {
        			  return;
        		  }
        		  Intent intent = new Intent(context, DataRangeActivity.class);
        		  intent.putExtra("SIM_TITLE", simSlotIndex == 0 ? context.getString(R.string.sim1) : context.getString(R.string.sim2));
        		  intent.putExtra("CURRENT_INDEX", simSlotIndex);
        		  NotificationUtil.showNotification(context, context.getString(R.string.data_pass_warning_info), context.getString(R.string.data_pass_warning_info), String.format(context.getString(R.string.data_warning_notifyinfo), rateStr + "%"), intent);
        		  PreferenceUtil.putBoolean(context, activeDataImsi, PreferenceUtil.NOTIFY_WARN_MONTH_KEY, true);
        	  }
          }
          
          //每日提醒:当日流量超过5%时提示
          if (warnState && total > 0 && networkInfoEntity.mUsedForDay / (total * 1024) >= LIMIT_RATE_DAY) {
        	  //每天最多一次
    		  //跳转到对应的流量管理主界面
    		  Intent intent = new Intent(context, MainActivity.class);
    		  String info = String.format(context.getString(R.string.data_day_notifyinfo), StringUtil.formatDataFlowSize(context, networkInfoEntity.mUsedForDay / 1024) , StringUtil.formatDataFlowSize(context, total));
    		  NotificationUtil.showNotification(context, context.getString(R.string.data_pass_warning_info), context.getString(R.string.data_pass_warning_info), info, intent);
    		  PreferenceUtil.putBoolean(context, activeDataImsi, PreferenceUtil.NOTIFY_WARN_DAY_KEY, true);
          }
	}
	
	/**
	 * 系统中断式对话框提示
	 * @param context
	 * @param networkInfoEntity
	 */
	private void showDataDialogInfo(Context context, NetInfoEntity networkInfoEntity) {
		String activeSimImsi = ToolsUtil.getActiveSimImsi(context);
		String contentInfo = null;
		int commDataTotal = PreferenceUtil.getInt(context, activeSimImsi, PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
		if (commDataTotal <= 0) {
		   return;
		}
		int commUsedTotal = PreferenceUtil.getInt(context, activeSimImsi, PreferenceUtil.USED_DATAPLAN_COMMON_KEY, 0);
		long usedMonth = (commUsedTotal * 1024 - networkInfoEntity.mUsedForMonth) > 0 ? commUsedTotal * 1024 : networkInfoEntity.mUsedForMonth;
		if (usedMonth / (commDataTotal * 1024) >= LIMIT_RATE_MONTH) {
		      ToolsUtil.setMobileDataState(context, false);//关闭数据接连
		      //月结日
		      int closeDay = PreferenceUtil.getInt(context, activeSimImsi, PreferenceUtil.CLOSEDAY_KEY, 1);
	     	  long timeByNextMonth = StringUtil.getDayByNextMonth(closeDay);
	     	  long timeNow = System.currentTimeMillis();
	     	  if (timeNow >= timeByNextMonth) {
	     		  //超过当前月结日周期清除提示框标志
		     	  PreferenceUtil.putBoolean(context, activeSimImsi, PreferenceUtil.NOTIFY_WARN_MONTH_99_KEY, false);
	     	  } else {
	     		   boolean isNotify = PreferenceUtil.getBoolean(context, activeSimImsi, PreferenceUtil.NOTIFY_WARN_MONTH_99_KEY, false);
	     		   if (isNotify) {
	     			  //提示频率：每月一次
	     			   return;
	     		   }
		           if (!TextUtils.isEmpty(DataManagerApplication.mImsiArray[0]) && !TextUtils.isEmpty(DataManagerApplication.mImsiArray[1])) {
		  	          //双卡弹框提示
		  	          contentInfo = context.getString(R.string.data_warning_double_info);
		  	          dialogForDouble(context, contentInfo);
		            } else {
		  	          //单卡弹框提示
		  	          contentInfo = context.getString(R.string.data_warning_single_info);
		  	          dialogForSingle(context, contentInfo);
		            }
		            PreferenceUtil.putBoolean(context, activeSimImsi, PreferenceUtil.NOTIFY_WARN_MONTH_99_KEY, true);
	     	  }
		}
	}
	
	protected void dialogForSingle(Context context, String message) {
		  context = ((Context)NetManagerService.this);
	      context.setTheme(com.mst.R.style.Theme_Mst_Material_Light);
	      Builder builder = new mst.app.dialog.AlertDialog.Builder(context);
	      builder.setMessage(message);
	      builder.setCancelable(false);
	      builder.setTitle(context.getString(R.string.data_pass_warning_info));
	      builder.setPositiveButton(context.getString(R.string.use_till), new OnClickListener() {
	          @Override
	          public void onClick(DialogInterface dialog, int which) {
	        	  ToolsUtil.setMobileDataState(NetManagerService.this, true);
	              dialog.dismiss();
	          }
	      });
	      builder.setNegativeButton(context.getString(R.string.data_close), new OnClickListener() {
	           @Override
	           public void onClick(DialogInterface dialog, int which) {
	        	   ToolsUtil.setMobileDataState(NetManagerService.this, false);
	                dialog.dismiss();
	            }
	      });
	      if (null == mAlertDialog) {
	    	  mAlertDialog = builder.create();
	      }
	      if (null != mAlertDialog && !mAlertDialog.isShowing()) {
	    	  mAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	    	  mAlertDialog.show();
		  }
	 }
	
	/**
	 * 双卡切换
	 * @param context
	 * @param message
	 */
	protected void dialogForDouble(Context context, String message) {
		  context = ((Context)NetManagerService.this);
	      context.setTheme(com.mst.R.style.Theme_Mst_Material_Light);
	      mst.app.dialog.AlertDialog.Builder builder = new mst.app.dialog.AlertDialog.Builder(context);
	      builder.setMessage(message);
	      builder.setCancelable(false);
	      builder.setTitle(context.getString(R.string.data_pass_warning_info));
	      builder.setPositiveButton(context.getString(R.string.use_till), new mst.app.dialog.AlertDialog.OnClickListener() {
	          @Override
	          public void onClick(DialogInterface dialog, int which) {
	        	  ToolsUtil.setMobileDataState(NetManagerService.this, true);
	              dialog.dismiss();
	          }
	      });
	      builder.setNeutralButton(context.getString(R.string.change_sim), new mst.app.dialog.AlertDialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int currentSimIndex = ToolsUtil.getCurrentNetSimSubInfo(NetManagerService.this);
				ToolsUtil.changeNetSim(NetManagerService.this, currentSimIndex == 0 ? 1 : 0);
				Toast.makeText(NetManagerService.this, R.string.change_sim_ok, Toast.LENGTH_SHORT).show();
				dialog.dismiss();
				
			}
		});
	    builder.setNegativeButton(context.getString(R.string.data_close), new mst.app.dialog.AlertDialog.OnClickListener() {
	           @Override
	           public void onClick(DialogInterface dialog, int which) {
	        	   ToolsUtil.setMobileDataState(NetManagerService.this, false);
	                dialog.dismiss();
	            }
	     });
	    if (null == mAlertDialog) {
	    	  mAlertDialog = builder.create();
	      }
	    if (null != mAlertDialog && !mAlertDialog.isShowing()) {
	         mAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	         mAlertDialog.show();
		}
	 }
}
