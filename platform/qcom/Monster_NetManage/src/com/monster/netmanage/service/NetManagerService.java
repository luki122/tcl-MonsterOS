package com.monster.netmanage.service;

import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.monster.netmanage.DataCorrect;
import com.monster.netmanage.DataManagerApplication;
import com.monster.netmanage.NotifyInfo;
import com.monster.netmanage.R;
import com.monster.netmanage.activity.DataRangeActivity;
import com.monster.netmanage.activity.MainActivity;
import com.monster.netmanage.activity.SimDataSetActivity;
import com.monster.netmanage.entity.AppItem;
import com.monster.netmanage.net.SummaryForAllUidLoader;
import com.monster.netmanage.utils.NotificationUtil;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.StringUtil;
import com.monster.netmanage.utils.ToolsUtil;
import com.monster.netmanage.view.NetManageDialogView;
import com.monster.netmanage.view.NetManageDialogView.ICheckListener;
import com.mst.tms.NetInfoEntity;

import android.net.NetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicy;
import android.net.NetworkStats;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.net.Uri;

//import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED;
import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED_BACKGROUND;
import static android.net.NetworkStats.SET_DEFAULT;
import static android.net.NetworkStats.SET_FOREGROUND;
import static android.net.NetworkTemplate.buildTemplateMobileAll;

import java.text.SimpleDateFormat;
import java.util.Arrays;

import android.app.AppOpsManager;
import android.app.Service;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Intents;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Toast;
import mst.app.dialog.AlertDialog;
import mst.app.dialog.AlertDialog.Builder;
import tmsdkobf.mc;

/**
 * 后台服务
 * 
 * @author zhaolaichao
 *
 */
public class NetManagerService extends Service {

	private static final String TAG = "NetManagerService";
	private static final Uri SMS_INBOX_URI = Uri.parse("content://sms/inbox");
	private static final Uri SMS_SENT_URI = Uri.parse("content://sms/sent");
	private static final Uri SMS_URI = Uri.parse("content://sms/");
	/**
	 * 切换上网卡
	 */
	private static final Uri DATA_CHANGED_URI = Uri.parse("content://settings/global");
	/**
	 * 单位时间内（1分钟）后台跑了至少10M流量
	 */
	private static final int MAX_MINUTE_BGDATA = 10 * 1024 * 1024; 
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
	
	private NetInfoEntity mInfoEntity = new NetInfoEntity();
	
	private Callback callback;  
	
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
					}
				    callback.onDataChange(activeSimImsi, mInfoEntity);  
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
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		correctMsg(intent);
		registerObserver();
//		handler.postDelayed(statsRn, UNIT_MINUTE);
		new Thread(statsRn).start();
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
				if (DataManagerApplication.mImsiArray.length == 1) {
					//当且仅当只有一张sim卡时
					simSlotIndex = 0;
				}
				mDataCorrect.startCorrect(NetManagerService.this, false, simSlotIndex);
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
				String saveActiveSimImsi = ToolsUtil.getActiveSimImsi(NetManagerService.this);
				String activeSimImsi = PreferenceUtil.getString(NetManagerService.this, "", PreferenceUtil.CURRENT_ACTIVE_IMSI_KEY, "");
				if (TextUtils.isEmpty(saveActiveSimImsi) || TextUtils.isEmpty(activeSimImsi))return;
				if (!saveActiveSimImsi.equals(activeSimImsi)) {
					Log.v(TAG, "ContentObserver>>mContext>>" + NetManagerService.this);
					NotifyInfo.showNotify(NetManagerService.this);
					//更新统计流量信息
					handler.removeCallbacks(statsRn);
					initStats();
					handler.postDelayed(statsRn, UNIT_MINUTE);
				}
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
	        try {
	          //读取收件箱中的短信  请求默认短信应用权限
	          String where = " date >  " + (System.currentTimeMillis() - 5 * 1000);
	          String[] projection = new String[] { "body", "address", "person", "thread_id","_id", "sub_id", "type" };
	          cursor = getContentResolver().query(uri, projection, where, null, "date desc"); 
	          if (cursor !=null && cursor.getCount() > 0) { 
	        	  appOpsManager = (AppOpsManager) getSystemService("appops");
	        	  appOpsManager.setMode(15, android.os.Process.myUid(), getPackageName(), 0);
//	            	 _id, thread_id, address, person, date, date_sent, protocol, read, status, type, reply_path_present, subject, body, service_center, locked, sub_id, error_code, creator, seen, priority, timezone, car_code]
	               if (cursor.moveToNext()) {
	                    String number = cursor.getString(cursor.getColumnIndex("address"));//手机号
	                    int subId = cursor.getInt(cursor.getColumnIndex("sub_id"));
	                    String body = cursor.getString(cursor.getColumnIndex("body"));
	                    int id = cursor.getInt(cursor.getColumnIndex("_id"));
	                    int type = cursor.getInt(cursor.getColumnIndex("type"));
	                    if (!TextUtils.isEmpty(body)){
	                    	if (number.startsWith("10086") || number.startsWith("1000") || number.startsWith("10010")) {
	                    		Log.v(TAG, "onchange>>type>>" + type);
	                    		if (type == Sms.MESSAGE_TYPE_INBOX) {
	                    			//收件箱
	                    			Message msg = handler.obtainMessage(SMS_NOTIFI);
	                    			msg.obj = body;
	                    			msg.arg1 = subId;
	                    			msg.sendToTarget();	
	                    		}
	                    		int count = getContentResolver().delete(SMS_URI, "_id=?", new String[]{String.valueOf(id)});
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
			mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);;
		}
        int currentNetSimSubIno = ToolsUtil.getCurrentNetSimSubInfo(this);
	    try {
	    	if (null == mStatsSession) {
	    		mStatsSession = mStatsService.openSession();
	    	}
	    } catch (RemoteException e) {
	          throw new RuntimeException(e);
	    }
		if (!ToolsUtil.NET_TYPE_MOBILE.equals(ToolsUtil.getNetWorkType(this))) {
			return;
		}
		Log.d(TAG, "updateBody() mobile tab>>>>>" + mStatsSession);
        
		// Match mobile traffic for this subscriber, but normalize it to
		// catch any other merged subscribers.
		mTemplate = buildTemplateMobileAll(ToolsUtil.getActiveSubscriberId(this, ToolsUtil.getIdInDbBySimId(this, currentNetSimSubIno)));
		mTemplate = NetworkTemplate.normalize(mTemplate, mTelephonyManager.getMergedSubscriberIds());
	}
	
	private void statsByBg( long startTime, long endTime) {
		 String activeDataImsi = ToolsUtil.getActiveSimImsi(this);
    	boolean dataInfoState = PreferenceUtil.getBoolean(NetManagerService.this, activeDataImsi, PreferenceUtil.MINUTE_DATA_USED_DIALOG_KEY, false);
        if (dataInfoState) {
        	return;
        }
        int simSlotIndex = ToolsUtil.getCurrentNetSimSubInfo(NetManagerService.this);
	    if (simSlotIndex == -1) {
	     return;
	    }
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String format = sdf.format(startTime);
		Log.v(TAG, "start>>" + format);
		format = sdf.format(endTime);
		Log.v(TAG, "end>>" + format);
		NetworkStats networkStats = null;
		try {
			  networkStats = mStatsSession.getSummaryForAllUid(mTemplate, startTime, endTime, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		NetworkStats.Entry entry = null;
		long usedDataBg = 0;
		int size = networkStats != null ? networkStats.size() : 0;
		if (null != networkStats) {
			for (int i = 0; i < size; i++) {
				entry = networkStats.getValues(i, entry);
				if (SET_DEFAULT == entry.set) {
					//统计后台数据
					long  usedData = entry.rxBytes + entry.txBytes;
					usedDataBg = usedData + usedDataBg;
				}
				Log.v(TAG, "统计后台数据" + "entry.rxBytes>>>" + entry.rxBytes + ">>>>entry.txBytes>>" + entry.txBytes);
			}
		}
	    
	    Log.v(TAG, "每分钟统计后台数据:" + Formatter.formatFileSize(NetManagerService.this, usedDataBg));
	    Message msg = handler.obtainMessage();
	    msg.obj = usedDataBg;
	    msg.what = WARN_INFO_DIALOG;
	    handler.sendMessage(msg);
	}
	
	private void statsDataPlan(int index) {
		int simSlotIndex = ToolsUtil.getCurrentNetSimSubInfo(NetManagerService.this);
	    if (simSlotIndex == -1) {
		     return;
		}
	    String activeSimImsi = ToolsUtil.getActiveSimImsi(this);
	    long startTime = 0;
	    long endTime = 0;
		switch (index) {
		case DATA_STATUS_MONTH:
			int closeDay = PreferenceUtil.getInt(NetManagerService.this,  activeSimImsi, PreferenceUtil.CLOSEDAY_KEY, 1);
			startTime = StringUtil.getDayByMonth(closeDay);
			endTime = StringUtil.getDayByNextMonth(closeDay);
			break;
		case DATA_STATUS_DAY:
			startTime = StringUtil.getStartTime();
			endTime = StringUtil.getEndTime();
			break;
		default:
			break;
		}
		NetworkStats networkStats = null;
		try {
			  networkStats = mStatsSession.getSummaryForAllUid(mTemplate, startTime, endTime, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		NetworkStats.Entry entry = null;
		long usedData = 0;
		int size = networkStats != null ? networkStats.size() : 0;
		if (null != networkStats) {
			for (int i = 0; i < size; i++) {
				entry = networkStats.getValues(i, entry);
				usedData = usedData + entry.rxBytes + entry.txBytes;
			}
			if (index == DATA_STATUS_MONTH) {
				mInfoEntity.setmUsedForMonth(usedData);
			} else if (index == DATA_STATUS_DAY) {
				mInfoEntity.setmUsedForDay(usedData);
			}
		}
	   
	   Log.v(TAG, "统计月数据:>>>" + mInfoEntity.mUsedForDay + ">>mUsedForMonth>>" + mInfoEntity.mUsedForMonth);
	   handler.obtainMessage(DATA_NOTIFI, activeSimImsi).sendToTarget();
	}
	
	private Runnable statsRn = new Runnable() {
		
		@Override
		public void run() {
			if (!ToolsUtil.NET_TYPE_MOBILE .equals(ToolsUtil.getNetWorkType(NetManagerService.this))) {
				return;
			}
			if (null != mTemplate) {
				Log.v(TAG, "开始发送统计");
				handler.removeCallbacks(statsRn);
				long end = System.currentTimeMillis();
				long start = end - UNIT_MINUTE;
				try {
					statsByBg(start, end);
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					statsDataPlan(DATA_STATUS_DAY);
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					statsDataPlan(DATA_STATUS_MONTH);
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler.postDelayed(statsRn, UNIT_MINUTE);
		      }
		}
	};
	
	
	/**
	 * 每分钟统计一次
	 * 后台数据流量提示:单位时间内（1分钟）后台跑了至少10M流量则满足提示条件
	 */
	private void showInfoMinute(Context context, long usedDataBg) {
		String activeSimImsi = ToolsUtil.getActiveSimImsi(context);
        long lastMinuteData = PreferenceUtil.getLong(context, activeSimImsi, PreferenceUtil.MINUTE_DATA_USED_KEY, 0);
        if (usedDataBg - lastMinuteData >= MAX_MINUTE_BGDATA) {
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
          String warnValue = PreferenceUtil.getString(context, activeDataImsi, PreferenceUtil.PASS_WARNING_VALUE_KEY, "85%");
          String rateStr = warnValue.substring(0, warnValue.indexOf("%"));
          long usedForMonth = networkInfoEntity.mUsedForMonth;
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
//        		  intent.putExtra("STATS_POLICY", POLICY_REJECT_METERED);
        		  NotificationUtil.showNotification(context, context.getString(R.string.data_pass_warning_info), context.getString(R.string.data_pass_warning_info), String.format(context.getString(R.string.data_warning_notifyinfo), rateStr + "%"), intent);
        		  PreferenceUtil.putBoolean(context, activeDataImsi, PreferenceUtil.NOTIFY_WARN_MONTH_KEY, true);
        	  }
          }
          
          //每日提醒:当日流量超过5%时提示
          if (warnState && total > 0 && networkInfoEntity.mUsedForDay / (total * 1024) >= LIMIT_RATE_DAY) {
        	  //每天最多一次
    		  //跳转到对应的流量管理主界面
    		  Intent intent = new Intent(context, MainActivity.class);
    		  String info = String.format(context.getString(R.string.data_day_notifyinfo), Formatter.formatFileSize(context, networkInfoEntity.mUsedForDay) , StringUtil.formatDataFlowSize(context, total));
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
		String[] imsi = ToolsUtil.getIMSI(context);
		String activeSimImsi = ToolsUtil.getActiveSimImsi(context);
		String contentInfo = null;
		int commDataTotal = PreferenceUtil.getInt(context, activeSimImsi, PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
		if (commDataTotal <= 0) {
		   return;
		}
		if (networkInfoEntity.mUsedForMonth / (commDataTotal * 1024) >= LIMIT_RATE_MONTH) {
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
		           if (imsi.length > 1 && !TextUtils.isEmpty(DataManagerApplication.mImsiArray[0])) {
		  	          //双卡弹框提示
		  	          contentInfo = context.getString(R.string.data_warning_double_info);
		  	          dialogForDouble(context, contentInfo);
		            } else if (imsi.length == 1) {
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
