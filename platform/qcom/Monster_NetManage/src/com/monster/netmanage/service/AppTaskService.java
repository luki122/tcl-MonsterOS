package com.monster.netmanage.service;

import static android.net.NetworkPolicyManager.POLICY_NONE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.monster.netmanage.DataCorrect;
import com.monster.netmanage.R;
import com.monster.netmanage.adapter.RangeAppAdapter;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.ToolsUtil;
import com.monster.netmanage.view.NetManageDialogView;
import com.monster.netmanage.view.NetManageDialogView.ICheckListener;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.NetworkPolicyManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.CompoundButton;
import mst.app.dialog.AlertDialog;

/**
 * 后台服务　监听打开应用动作
 * 
 * @author zhaolaichao
 *
 */
public class AppTaskService extends Service {
	private static final String TAG = "AppTaskService";
	//更新桌面应用图标
	public static final String UPDATE_DATAPLAN_ICON_ACTION = "com.monster.netmanage.update_dataplan_icon.action";
	private static boolean flag = true;// 线程退出的标记
	private static final int MSG_TAG = 0;
	private static final long SLEEP_TIME = 10 * 1000;
	private NetworkPolicyManager mPolicyManager;
	private RecentUseComparator mRecentComp;
	private UsageStatsManager mUsageStatsManager;
	private INetworkManagementService mNetworkService;
	private PackageManager mPm;
	private AlertDialog mAlertDialog;
	private String[] mDataLimiteArray = null;
	private String[] mWlanLimiteArray = null;
	
	private boolean mIsContainData;
	private boolean mIsContainWlan;
	private ArrayList<Integer> mDataList;
	private ArrayList<Integer> mWlanList;
	private static Bundle mBundle = new Bundle();
	private UpdateIconReceiver mIconReceiver;
	
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_TAG:
				Bundle bundle = (Bundle) msg.getData();
				String content = bundle.getString("dialog_content");
				String net_type = bundle.getString("net_type");
				int uid = bundle.getInt("uid");
				boolean dataInfoState = PreferenceUtil.getBoolean(AppTaskService.this, "", PreferenceUtil.WARN_DATA_USED_KEY, false);
				if (!dataInfoState) {
					//没有勾选默认选中
					warnInfoDialog(content, net_type, uid);
				}
				break;

			default:
				break;
			}
		};
	};
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mNetworkService = INetworkManagementService.Stub.asInterface(
                ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
		mPolicyManager = NetworkPolicyManager.from(this);
		mPm = getPackageManager();
		mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats");
		// 一旦启动要在后台监视任务栈最顶端应用
		mRecentComp = new RecentUseComparator();
	}
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	registerReceiver();
    	statsTopTaskApp();
    	return START_STICKY;
    }
	@Override
	public void onDestroy() {
		super.onDestroy();
		unRegisterReceiver();
		flag = true;
		mAlertDialog = null;
		startService(new Intent(this,AppTaskService.class));
	}
	
	private void registerReceiver() {
		mIconReceiver = new UpdateIconReceiver();
		IntentFilter filter = new IntentFilter(UPDATE_DATAPLAN_ICON_ACTION);
		AppTaskService.this.registerReceiver(mIconReceiver, filter);
	}
	
	private void unRegisterReceiver() {
		if (null != mIconReceiver) {
			AppTaskService.this.unregisterReceiver(mIconReceiver);
		}
	}
	/**
	 * 获得栈顶app
	 */
	private void statsTopTaskApp() {
		new Thread() {
			@Override
			public void run() {
				super.run();
				while (flag) {
					synchronized (AppTaskService.class) {
						   String packageName = queryUsageStats(AppTaskService.this);
						   if (!TextUtils.isEmpty(packageName) && ToolsUtil.isNetworkApp(AppTaskService.this, packageName)) {
							   ApplicationInfo ai;
							   try {
								   mBundle.clear();
								   ai = mPm.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);
								   int uidPolicy = mPolicyManager.getUidPolicy(ai.uid);
								   int uid = ai.uid;
								   //判断是否处于禁止上网状态
								   String netType = ToolsUtil.getNetWorkType(AppTaskService.this);
								   if (ToolsUtil.NET_TYPE_MOBILE.equals(netType)) {
									   String data = PreferenceUtil.getString(AppTaskService.this, "", RangeAppAdapter.TYPE_DATA, null);
									   if (!TextUtils.isEmpty(data)) {
										   mDataLimiteArray = data.split(",");
										   ArrayList<String> uidDataList = new ArrayList<String>( Arrays.asList(mDataLimiteArray));
										   if (uidDataList.contains("" + uid)) {
											   mIsContainData = true;
										   }
										}
									   mBundle.putString("dialog_content", AppTaskService.this.getString(R.string.data_stop_mobile_info));
									   mBundle.putString("net_type", RangeAppAdapter.TYPE_DATA);
								   } else if (ToolsUtil.NET_TYPE_WIFI.equals(netType)) {
									   String wlan = PreferenceUtil.getString(AppTaskService.this, "", RangeAppAdapter.TYPE_WLAN, null);
										Log.d(TAG, "initSetting:" + wlan);
										if (!TextUtils.isEmpty(wlan)) {
											mWlanLimiteArray = wlan.split(",");
											 ArrayList<String> uidWlanList = new ArrayList<String>( Arrays.asList(mWlanLimiteArray));
											   if (uidWlanList.contains("" + uid)) {
												   mIsContainWlan = true;
											   }
										}
									   mBundle.putString("dialog_content", AppTaskService.this.getString(R.string.wifi_stop_info));
//									   msg.obj = AppTaskService.this.getString(R.string.wifi_stop_info);
									   mBundle.putString("net_type", RangeAppAdapter.TYPE_WLAN);
								   }
								   if (mIsContainData || mIsContainWlan) {
									   String topAppName = PreferenceUtil.getString(AppTaskService.this, "", PreferenceUtil.TOP_APP_NAME_KEY, null);
									   //当前栈顶应用只弹框提醒一次
									   if (!TextUtils.equals(packageName, topAppName)) {
											boolean dataInfoState = PreferenceUtil.getBoolean(AppTaskService.this, "", PreferenceUtil.WARN_DATA_USED_KEY, false);
                                           if (dataInfoState) {
                                        	   //默认不勾选，若用户勾选后下次达到条件不再进行提示
                                        	   flag = false;
                                           } else {
                                        	   Message msg = mHandler.obtainMessage();
                                        	   msg.what = MSG_TAG;
                                        	   mBundle.putInt("uid", uid);
                                        	   msg.setData(mBundle);
                                        	   mHandler.sendMessage(msg);
                                           }
									   }
								   }
								   Log.d("TAG", "!!>>>" + uidPolicy);
							   } catch (Exception e) {
								   e.printStackTrace();
								   flag = false;
							   }
						   }
						   PreferenceUtil.putString(AppTaskService.this, "", PreferenceUtil.TOP_APP_NAME_KEY, packageName);
						   try {
							   Thread.sleep(SLEEP_TIME);
						   } catch (InterruptedException e) {
							   e.printStackTrace();
						   }
					}
				}
			}
		}.start();
	}
	 /**
     * 通过使用UsageStatsManager获取，此方法是ndroid5.0A之后提供的API
     * 必须：
     * 1. 此方法只在android5.0以上有效
     * 2. AndroidManifest中加入此权限<uses-permission xmlns:tools="http://schemas.android.com/tools" android:name="android.permission.PACKAGE_USAGE_STATS"
     * tools:ignore="ProtectedPermissions" />
     * 3. 打开手机设置，点击安全-高级，在有权查看使用情况的应用中，打开app的权限
     *
     * @param context     上下文参数
     * @param packageName 需要检查是否位于栈顶的App的包名
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public String queryUsageStats(Context context) {
        long ts = System.currentTimeMillis();
        List<UsageStats> usageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, ts - 1000 * 10, ts);
        if (usageStats == null || usageStats.size() == 0) {
        	setPermission(context);
            return null;
        }
		Collections.sort(usageStats, mRecentComp);
		String currentTopPackage = usageStats.get(0).getPackageName();
        return currentTopPackage;
    }
    
   class RecentUseComparator implements Comparator<UsageStats> {
        @Override
        public int compare(UsageStats lhs, UsageStats rhs) {
            return (lhs.getLastTimeUsed() > rhs.getLastTimeUsed()) ? -1 : (lhs.getLastTimeUsed() == rhs.getLastTimeUsed()) ? 0 : 1;
        }
    }
    
    /**
     * 判断是否有用权限
     *
     * @param context 上下文参数
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void setPermission(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            appOpsManager.setMode(AppOpsManager.OP_GET_USAGE_STATS, applicationInfo.uid,context.getPackageName(), AppOpsManager.MODE_ALLOWED);
        } catch (PackageManager.NameNotFoundException e) {
        	e.printStackTrace();
        }
    }
    
    /**
	 * 联网被禁提示
	 * @param msg
	 * @param uid
	 */
    private void warnInfoDialog(String msg, final String type, final int uid) {
    	Context context = ((Context)AppTaskService.this);
    	context.setTheme(com.mst.R.style.Theme_Mst_Material_Light);
    	mst.app.dialog.AlertDialog.Builder builder = new mst.app.dialog.AlertDialog.Builder(context);
    	builder.setTitle(AppTaskService.this.getString(R.string.data_pass_warning_info));
    	NetManageDialogView dialogView = new NetManageDialogView(AppTaskService.this);
    	dialogView.setMessage(msg);
    	dialogView.setOnCheckListener(new ICheckListener() {
			
			@Override
			public void setOnCheckListener(CompoundButton buttonView, boolean isChecked) {
				Log.v(TAG, "不再提示>>" + isChecked);
				PreferenceUtil.putBoolean(AppTaskService.this, "", PreferenceUtil.WARN_DATA_USED_KEY, isChecked);
			}
		});
        builder.setView(dialogView); 
    	builder.setPositiveButton(AppTaskService.this.getString(com.mst.R.string.yes), new mst.app.dialog.AlertDialog.OnClickListener() {
 	          @Override
 	          public void onClick(DialogInterface dialog, int which) {
 	        	    //允许使用移动数据
 	        	    try {
// 	 			    	mPolicyManager.setUidPolicy(uid, POLICY_NONE);
 	        	    	applyChange(type, uid, false);
 	 	            } catch (Exception e) {
 	 	        	     e.printStackTrace();
 	 	                 Log.e("ttt", "No bandwidth control; leaving>>>" + e.getMessage());
 	 	           }
 	               dialog.dismiss();
 	          }
 	      });
    	builder.setNegativeButton(AppTaskService.this.getString(com.mst.R.string.no), new mst.app.dialog.AlertDialog.OnClickListener() {
 	           @Override
 	           public void onClick(DialogInterface dialog, int which) {
 	              dialog.dismiss();
 	           }
 	      });
    	builder.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
			}
		});
    	if (null == mAlertDialog) {
    		mAlertDialog = builder.create();
    	}
    	if (null != mAlertDialog && !mAlertDialog.isShowing()) {
    		mAlertDialog.setCanceledOnTouchOutside(false);
    		mAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    		mAlertDialog.show();
    	}
    }
    
	 /**
	   * 设置上网类型
	   * @param type
	   * @param uid
	   * @param isReject
	   */
 private void applyChange(String type, int uid, boolean isReject) {
	 String data = PreferenceUtil.getString(AppTaskService.this, "", RangeAppAdapter.TYPE_DATA, null);
	   if (!TextUtils.isEmpty(data)) {
		   mDataLimiteArray = data.split(",");
		   for (int i = 0; i < mDataLimiteArray.length; i++) {
			    mDataList.add(Integer.parseInt(mDataLimiteArray[i]));
		   }
		}
	   String wlan = PreferenceUtil.getString(AppTaskService.this, "", RangeAppAdapter.TYPE_WLAN, null);
		Log.d(TAG, "initSetting:" + wlan);
		if (!TextUtils.isEmpty(wlan)) {
			mWlanLimiteArray = wlan.split(",");
			 for (int i = 0; i < mWlanLimiteArray.length; i++) {
				    mWlanList.add(Integer.parseInt(mWlanLimiteArray[i]));
			   }
		}
		try {
			switch (type) {
			case RangeAppAdapter.TYPE_DATA:
				mNetworkService.setUidDataRules(uid, isReject); //传入true代表要禁止其联网。
				if (isReject) {
					if (!mDataList.contains(uid)) {
						mDataList.add(uid);
					}
				} else {
					if (!mDataList.contains(uid)) {
						mDataList.remove((Integer)uid);
					}
				}
				save(RangeAppAdapter.TYPE_DATA, mDataList);
				break;
			case RangeAppAdapter.TYPE_WLAN:
				mNetworkService.setUidWlanRules(uid, isReject);
				if (isReject) {
					if (!mWlanList.contains(uid)) {
						mWlanList.add(uid);
					}
				} else {
					if (mWlanList.contains(uid)) {
						mWlanList.remove((Integer) uid);
					}
				}
				save(RangeAppAdapter.TYPE_WLAN, mWlanList);
				break;
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
 }
 
  private void save(String type, ArrayList<Integer> saveList) {
	 if (saveList.size() == 0) {
         return;
     }
     StringBuilder sb = new StringBuilder();
     for (Integer i : saveList) {
         sb.append(i).append(",");
     }
     Log.d(TAG, "sb:" + sb);
     switch (type) {
         case RangeAppAdapter.TYPE_DATA:
      	     PreferenceUtil.putString(AppTaskService.this, "", RangeAppAdapter.TYPE_DATA, sb.substring(0, sb.length() - 1));
             break;
         case RangeAppAdapter.TYPE_WLAN:
      	     PreferenceUtil.putString(AppTaskService.this, "", RangeAppAdapter.TYPE_WLAN, sb.substring(0, sb.length() - 1));
             break;
     }
  }
  
  class UpdateIconReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (UPDATE_DATAPLAN_ICON_ACTION.equals(intent.getAction())) {
			//用于更新图标
			ToolsUtil.updateIconReceiver();
		}
	}
  }
}