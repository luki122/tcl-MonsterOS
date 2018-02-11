package com.monster.netmanage.activity;

import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED_BACKGROUND;
import static android.net.NetworkTemplate.buildTemplateMobileAll;
import static android.net.NetworkTemplate.buildTemplateWifiWildcard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import com.monster.netmanage.DataManagerApplication;
import com.monster.netmanage.R;
import com.monster.netmanage.adapter.RangeAppAdapter;
import com.monster.netmanage.entity.AppItem;
import com.monster.netmanage.net.SummaryForAllUidLoader;
import com.monster.netmanage.receiver.AppReceiver;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.StringUtil;
import com.monster.netmanage.utils.ToolsUtil;
import com.monster.netmanage.view.ListViewAuto;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkPolicyManager;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.renderscript.Program.TextureType;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import mst.view.menu.PopupMenu;
import mst.widget.FoldProgressBar;
import mst.widget.toolbar.Toolbar;
/**
 * 流量排行展示界面
 * @author zhaolaichao
 */
public class DataRangeActivity extends BaseActivity implements OnClickListener{
	private final static String TAG = "DataRangeActivity";
	 private static final boolean LOGD = false;
	 private static final int LOADER_SUMMARY = 11;
	 private static final int DIALOG_TAG = 10000;
	 /**
	  * 默认int值
	  */
	 private static final int DEFAULT_INT = -1;
	 private static final String TAB_MOBILE = "mobile";
	 private static final String TAB_WIFI = "wifi";
	 
	 private ListViewAuto mLvStopData;
	 private ListViewAuto mLvUseData;
	 private RelativeLayout mLayType;
	 private LinearLayout mLayStop;
	 private LinearLayout mLayUsed;
	 private TextView mTvDataType;
	 private TextView mTvStopData;
	 private TextView mTvUseData;
	 private FoldProgressBar mFoldProgressBar;
	 
	 /**
	  * 当前选中的网络数据类型
	  */
     private String mCurrentNetType = null;
     
     private NetworkPolicyManager mPolicyManager;
     private NetworkTemplate mTemplate;
	 private INetworkStatsSession mStatsSession;
	 private INetworkStatsService mStatsService;
	 private TelephonyManager mTelephonyManager;
	 
	 private RangeAppAdapter mStopDataAdapter;
	 private RangeAppAdapter mUseDataAdapter;
	 
	 private ArrayList<PackageInfo> mAppInfos = new ArrayList<PackageInfo>();
	 /**
	  * 移动数据网络
	  */
	 private ArrayList<AppItem> mAppInfosByPolicy = new ArrayList<AppItem>();
	 private ArrayList<AppItem> mAppInfosNoPolicy = new ArrayList<AppItem>();
	 /**
	  * 存储app使用的流量大小
	  */
	 private ArrayList<AppItem> mAppDefaultDatas = new ArrayList<AppItem>();
	 private ArrayList<AppItem> mAppForegroundDatas = new ArrayList<AppItem>();
	 /**
	  * 显示标题
	  */
	 private String mTitle = null;
	 /**
	  * 联网类型
	  */
	 private String mNetType = null;
	 /**
	  * 流量统计策略
	  */
	 private static int mPolicy = DEFAULT_INT;
	 /**
	  *当前使用卡
	  */
	 private int mCurrentIndex = DEFAULT_INT;
	 /**
	  * 统计流量次数
	  */
	 private int mStatsCount = 0;
	 /**
	  * 是否要统计两个卡流量总和
	  */
	 private boolean mIsStatsTotal = false;
	 
	 Handler mHandler = new Handler () {
		 public void handleMessage(android.os.Message msg) {
			 switch (msg.what) {
			case RangeAppAdapter.CHANGE_STATE_TAG:
				@SuppressWarnings("unchecked") 
				ArrayList<ArrayList<AppItem>> mArrayLists = (ArrayList<ArrayList<AppItem>>) msg.obj;
				ArrayList<AppItem> appInfosByPolicy = mArrayLists.get(0);
				ArrayList<AppItem> appInfosNoPolicy = mArrayLists.get(1);
				if (appInfosByPolicy.size() == 0) {
					mLayStop.setVisibility(View.GONE);
				} else {
					mLayStop.setVisibility(View.VISIBLE);
				}
				if (appInfosNoPolicy.size() == 0) {
					mLayUsed.setVisibility(View.GONE);
				} else {
					mLayUsed.setVisibility(View.VISIBLE);
				}
				mTvStopData.setText(String.format(getString(R.string.data_stop_info), "" + appInfosByPolicy.size(), mTvDataType.getText().toString()));
				mTvUseData.setText(String.format(getString(R.string.data_use_info), "" + appInfosNoPolicy.size(), mTvDataType.getText().toString()));
				//允许使用网络
				mUseDataAdapter.setAppList(appInfosNoPolicy);
				mUseDataAdapter.setUsedNetList(appInfosNoPolicy);
				mUseDataAdapter.setStopNetList(appInfosByPolicy);
				mLvUseData.setAdapter(mUseDataAdapter);
//				mUseDataAdapter.notifyDataSetChanged();
				//禁止使用网络
				mStopDataAdapter.setAppList(appInfosByPolicy);
				mStopDataAdapter.setStopNetList(appInfosByPolicy);
				mStopDataAdapter.setUsedNetList(appInfosNoPolicy);
				mLvStopData.setAdapter(mStopDataAdapter);
//				mStopDataAdapter.notifyDataSetChanged();
				break;
			case DIALOG_TAG:
				mFoldProgressBar.setVisibility(View.GONE);
				init(mCurrentIndex, true, mCurrentNetType);
				break;
			default:
				break;
			}
		 };
	 };
	 
	 @Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		task.execute();
	}
	 
	 @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_range_data_app);
		mTitle = getIntent().getStringExtra("SIM_TITLE");
		mTitle = TextUtils.isEmpty(mTitle) ? getString(R.string.net_control) : mTitle;
		mIsStatsTotal = getIntent().getBooleanExtra("SIM_COUNT", false);
		if (getString(R.string.net_control).equals(mTitle)) {
			mIsStatsTotal = true;
		}
		mCurrentIndex = getIntent().getIntExtra("CURRENT_INDEX", 0);
		mPolicy = getIntent().getIntExtra("STATS_POLICY", mPolicy);
		updateSimChange();
		initTitle();
		registerUpadateApp();
	}

	 private void registerUpadateApp() {
		 IntentFilter intentFilter = new IntentFilter(AppReceiver.UPDATE_APP_ACTION);
		 registerReceiver(updateAppReceiver, intentFilter);
	 }
	 
	 private void unRegisterUpdateApp() {
		 if (updateAppReceiver != null) {
			 unregisterReceiver(updateAppReceiver);
		 }
	 }
	 
	private AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				mPolicyManager = NetworkPolicyManager.from(DataRangeActivity.this);
				mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
				mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			    mStatsSession = mStatsService.openSession();
			} catch (RemoteException e) {
			      throw new RuntimeException(e);
			}
			mAppInfos.addAll(ToolsUtil.getPackageInfos(DataRangeActivity.this));
			matchDataList(); //移除定向流量应用. 
			getDataMobileAppsByPolicy(mNetType, initDataNet(mNetType));
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			initView();
			mHandler.obtainMessage(DIALOG_TAG).sendToTarget();
		}
	};
	
	 /**
	  * 移除定向流量应用. 
	  */
	private void matchDataList() {
		ArrayList<String> orientApps = null;
		if (mIsStatsTotal) {
			if (!TextUtils.isEmpty(DataManagerApplication.mImsiArray[0]) && !TextUtils.isEmpty(DataManagerApplication.mImsiArray[1])) {
				//移除相同的定向流量应用. 
				ArrayList<String> orientAppsFirst = getOrientApps(DataManagerApplication.mImsiArray[0]);
				ArrayList<String> orientAppsSecond = getOrientApps(DataManagerApplication.mImsiArray[1]);
				orientAppsFirst.retainAll(orientAppsSecond);
				orientApps = orientAppsFirst;
			}
		} else {
			//移除定向流量应用. 
			if (!TextUtils.isEmpty(DataManagerApplication.mImsiArray[mCurrentIndex])) {
				orientApps = getOrientApps(DataManagerApplication.mImsiArray[mCurrentIndex]);
			}
		}
		if (null != orientApps) {
			for (int j = 0; j < orientApps.size(); j++) {
				String appUid = orientApps.get(j);
			    for (int i = 0; i < mAppInfos.size(); i++) {
				    ApplicationInfo applicationInfo = mAppInfos.get(i).applicationInfo;
				    int uid = applicationInfo.uid;
					if (uid == Integer.parseInt(appUid)) {
						mAppInfos.remove(i);
						break;
					}
				}
			}
		}
	}
	/**
	 * 监听卡的状态并更新界面
	 */
	private void updateSimChange() {
	   mStatsCount = 0;
	   String netType = null;
	   int currentNetSimSlot = ToolsUtil.getCurrentNetSimSubInfo(this);
	   if (currentNetSimSlot == -1){
		   //显示wifi流量
		   mCurrentNetType = TAB_WIFI;
		   mCurrentIndex = 0;
		} else {
		  mCurrentIndex = 0;
	   }
	   if (mIsStatsTotal) {
		   netType = PreferenceUtil.getString(this, "", PreferenceUtil.SELECTED_NETTYPE_KEY, null);
	   } else {
		   netType = PreferenceUtil.getString(this, DataManagerApplication.mImsiArray[mCurrentIndex], PreferenceUtil.SELECTED_NETTYPE_KEY, null);
	   }
       if (TextUtils.isEmpty(netType)) {
        	if (mPolicy == POLICY_REJECT_METERED_BACKGROUND) {
        		mNetType = getString(R.string.net_bg);
        	 } else {
        		if (currentNetSimSlot == -1) {
        			mNetType = getString(R.string.data_wifi);
        		} else {
        			mNetType = getString(R.string.data_mobile);
        		}
        	}
       } else {
        	mNetType = netType;
       }
       if (mNetType.equals(getString(R.string.data_wifi))) {
    	   mCurrentNetType = TAB_WIFI;
       } else if (mNetType.equals(getString(R.string.net_bg)) || mNetType.equals(getString(R.string.data_mobile))) {
    	   mCurrentNetType = TAB_MOBILE;
       }
	}
	
	private String[]  initDataNet(String netType) {
		String[] uidArray = null;
		mNetType = netType;
		if (mNetType.equals(getString(R.string.data_mobile))) {
			String data = PreferenceUtil.getString(this, "", RangeAppAdapter.TYPE_DATA, null);
			Log.d(TAG, "initSetting:" + data);
			if (!TextUtils.isEmpty(data)) {
				uidArray = data.split(",");
			}
		} else if (mNetType.equals(getString(R.string.data_wifi))) {
			String wlan = PreferenceUtil.getString(this, "", RangeAppAdapter.TYPE_WLAN, null);
			Log.d(TAG, "initSetting:" + wlan);
			if (!TextUtils.isEmpty(wlan)) {
				uidArray = wlan.split(",");
			}
		}
       return uidArray;
	}
	
	private void initTitle() {
		Toolbar toolbar =  (Toolbar)findViewById(R.id.app_toolbar);
		toolbar.setTitle(mTitle);
		toolbar.setNavigationIcon(com.mst.R.drawable.ic_toolbar_back);
		toolbar.setNavigationOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				DataRangeActivity.this.finish();
			}
		});
		toolbar.setElevation(1);
		mFoldProgressBar = (FoldProgressBar) findViewById(R.id.progressbar);
		mFoldProgressBar.setVisibility(View.VISIBLE);
		mLayType = (RelativeLayout) findViewById(R.id.lay_net_type);
		mLayStop = (LinearLayout) findViewById(R.id.lay_stop_net);
		mLayUsed = (LinearLayout) findViewById(R.id.lay_used_net);
        mTvDataType = (TextView)findViewById(R.id.tv_data_type);
		mTvDataType.setText(mNetType);
		mTvDataType.setVisibility(View.VISIBLE);
	}
	
	private void initView() {
		mTvStopData = (TextView) findViewById(R.id.tv_stop_data);
		if (mAppInfosByPolicy.size() == 0) {
			mLayStop.setVisibility(View.GONE);
		} else {
			mLayStop.setVisibility(View.VISIBLE);
		}
		mTvStopData.setText(String.format(getString(R.string.data_stop_info), "" + mAppInfosByPolicy.size(), mTvDataType.getText().toString()));
		mLvStopData = (ListViewAuto) findViewById(R.id.lv_stop_data);
		mStopDataAdapter = new RangeAppAdapter(this, mPolicyManager, mHandler);
		mStopDataAdapter.setNetType(mNetType);
		mStopDataAdapter.setAppList(mAppInfosByPolicy);
		mStopDataAdapter.setUsedNetList(mAppInfosNoPolicy);
		mStopDataAdapter.setStopNetList(mAppInfosByPolicy);
		mLvStopData.setAdapter(mStopDataAdapter);
		mTvUseData = (TextView) findViewById(R.id.tv_use_data);
		if (mAppInfosNoPolicy.size() == 0) {
			mLayUsed.setVisibility(View.GONE);
		} else {
			mLayUsed.setVisibility(View.VISIBLE);
		}
		mTvUseData.setText(String.format(getString(R.string.data_use_info), "" + mAppInfosNoPolicy.size(), mTvDataType.getText().toString()));
		mLvUseData = (ListViewAuto) findViewById(R.id.lv_use_data);
		mUseDataAdapter = new RangeAppAdapter(this, mPolicyManager,  mHandler);
		mUseDataAdapter.setNetType(mNetType);
		mUseDataAdapter.setAppList(mAppInfosNoPolicy);
		mUseDataAdapter.setUsedNetList(mAppInfosNoPolicy);
		mUseDataAdapter.setStopNetList(mAppInfosByPolicy);
		mLvUseData.setAdapter(mUseDataAdapter);
//		mLvUseData.setNestedScrollingEnabled(false);
		mTvStopData.setVisibility(View.VISIBLE);
		mTvUseData.setVisibility(View.VISIBLE);
		mLayType.setOnClickListener(this);
	}

	/**
	 * @param isInit    是否要初始化
	 * @param currentIndex 当前卡索引
	 */
	private void init(int currentIndex, boolean isInit, String netType) {
		mCurrentNetType = netType;
		String simImsi = ToolsUtil.getActiveSubscriberId(this, ToolsUtil.getIdInDbBySimId(this, currentIndex));
		  if (TAB_MOBILE.equals(mCurrentNetType)) {
	            if (LOGD) Log.d(TAG, "updateBody() mobile tab");

	            // Match mobile traffic for this subscriber, but normalize it to
	            // catch any other merged subscribers.
	            mTemplate = buildTemplateMobileAll(simImsi);
	            mTemplate = NetworkTemplate.normalize(mTemplate, mTelephonyManager.getMergedSubscriberIds());
	       } else if (TAB_WIFI.equals(mCurrentNetType)) {
	            // wifi doesn't have any controls
	            if (LOGD) Log.d(TAG, "updateBody() wifi tab");
	            mTemplate = buildTemplateWifiWildcard();

	        } else {
	            if (LOGD) Log.d(TAG, "updateBody() unknown tab");
	            throw new IllegalStateException("unknown tab: " + currentIndex);
	        }
		   // kick off loader for network history
	        // TODO: consider chaining two loaders together instead of reloading
	        // network history when showing app detail.
	        // kick off loader for detailed stats
		  //当前月结日
		  int closeDay = PreferenceUtil.getInt(this, simImsi, PreferenceUtil.CLOSEDAY_KEY, 1);
		  int totalData  = PreferenceUtil.getInt(this, simImsi, PreferenceUtil.DATAPLAN_COMMON_KEY, 0);
		  long start = 0;
		  //若用户没设置流量套餐，流量排行从手机开始使用当天起作统计；
          if (totalData > 0) {
        	  start = StringUtil.getDayByMonth(closeDay);
          }
		  long end = StringUtil.getDayByNextMonth(closeDay);
		  if (isInit) {
			  getLoaderManager().initLoader(LOADER_SUMMARY,
					  SummaryForAllUidLoader.buildArgs(mTemplate, start, end, simImsi), mSummaryCallbacks);
		  } else {
			  getLoaderManager().restartLoader(LOADER_SUMMARY,
					  SummaryForAllUidLoader.buildArgs(mTemplate, start, end, simImsi), mSummaryCallbacks);
		  }
	}
	
	@Override
	protected void onRestart() {
		getLoaderManager().destroyLoader(LOADER_SUMMARY);
		updateUI(mNetType);
		super.onRestart();
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lay_net_type:
			showDataType(mLayType);
			break;
		default:
			break;
		}
		
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (getString(R.string.net_control).equals(mTitle)) {
			PreferenceUtil.putString(this, "", PreferenceUtil.SELECTED_NETTYPE_KEY, mNetType);
		} else {
			String simImsi = ToolsUtil.getActiveSubscriberId(this, ToolsUtil.getIdInDbBySimId(this, mCurrentIndex));
			PreferenceUtil.putString(this, simImsi, PreferenceUtil.SELECTED_NETTYPE_KEY, mNetType);
		}
		clearCrash();
		unRegisterUpdateApp();
	}
	
	/**
	 * 更新界面
	 * @param netType  统计类别
	 */
	private void updateUI(String netType) {
	   	mAppInfosByPolicy.clear();
    	mAppInfosNoPolicy.clear();
    	getDataMobileAppsByPolicy(netType, initDataNet(netType));
    	
    	mStopDataAdapter.setNetType(netType);
    	mStopDataAdapter.setAppList(mAppInfosByPolicy);
    	mStopDataAdapter.setStopNetList(mAppInfosByPolicy);
    	mStopDataAdapter.setUsedNetList(mAppInfosNoPolicy);
    	
    	mUseDataAdapter.setNetType(netType);
    	mUseDataAdapter.setAppList(mAppInfosNoPolicy);
    	mUseDataAdapter.setStopNetList(mAppInfosByPolicy);
    	mUseDataAdapter.setUsedNetList(mAppInfosNoPolicy);
    	
    	if (mAppInfosByPolicy.size() > 0) {
    		mTvStopData.setText(String.format(getString(R.string.data_stop_info), "" + mAppInfosByPolicy.size(), mTvDataType.getText().toString()));
    		mLayStop.setVisibility(View.VISIBLE);
    	} else {
    		mLayStop.setVisibility(View.GONE);
    	}
    	if (mAppInfosNoPolicy.size() > 0) {
    		mTvUseData.setText(String.format(getString(R.string.data_use_info), "" + mAppInfosNoPolicy.size(), mTvDataType.getText().toString()));
    		mLayUsed.setVisibility(View.VISIBLE);
    	} else {
    		mLayUsed.setVisibility(View.GONE);
    	}
    	//统计流量
    	mAppForegroundDatas.clear();
    	mAppDefaultDatas.clear();
    	mStatsCount = 0;
    	init(mCurrentIndex, false, mCurrentNetType);
	}
	/**
	 * 显示数据类型
	 */
	private void showDataType(View parent) {
		//初始化PopupMenu
		final PopupMenu popupMenu = new PopupMenu(this, parent, Gravity.BOTTOM);
		//设置popupmenu 中menu的点击事件
		popupMenu.setOnMenuItemClickListener(new  mst.view.menu.PopupMenu.OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem menuItem) {
			String item = (String) menuItem.getTitle();
			if (item.equals(mTvDataType.getText().toString())) {
				return false;
			}
			getLoaderManager().destroyLoader(LOADER_SUMMARY);
			mTvDataType.setText(item);
			if (item.equals(getString(R.string.data_mobile)) 
					|| item.equals(getString(R.string.net_bg))) {
				mCurrentNetType = TAB_MOBILE;
			} else {
				mCurrentNetType = TAB_WIFI;
			}
			updateUI(item);
			popupMenu.dismiss();
			return false;
		  }
		});
		//导入menu布局
		popupMenu.inflate(R.menu.popup_menu);
		//显示popup menu
		popupMenu.show();
	}
	
	/**
	 * 初始化集合
	 * @param netType  统计类别
	 * @param dataList
	 */
	private void getDataMobileAppsByPolicy(String netType, String[] dataList) {
		mAppInfosByPolicy.clear();
		mAppInfosNoPolicy.clear();
		ArrayList<Integer> uidList = new ArrayList<Integer>();
		if (dataList != null) {
			for (int i = 0; i < dataList.length; i++) {
				uidList.add(Integer.parseInt(dataList[i]));
			}
		}
		for (int i = 0; i < mAppInfos.size(); i++) {
			PackageInfo packageInfo = mAppInfos.get(i);
			int uid = packageInfo.applicationInfo.uid;
			AppItem appItem = new AppItem();
			appItem.setAppUid(uid);
			appItem.setPackageInfo(packageInfo);
			if (netType.equals(getString(R.string.net_bg))) {
				int selectedPolicy = mPolicyManager.getUidPolicy(uid);
				if (selectedPolicy == POLICY_REJECT_METERED_BACKGROUND) {
					mAppInfosByPolicy.add(appItem);
					appItem.setPolicyStatus(false);
				} else {
					//允许联网策略
					mAppInfosNoPolicy.add(appItem);
					appItem.setPolicyStatus(true);
				}
			} else {
				if (uidList == null || uidList.size() == 0) {
					mAppInfosNoPolicy.add(appItem);
					appItem.setPolicyStatus(true);
				} else {
					if (!uidList.contains(uid)) {
						mAppInfosNoPolicy.add(appItem);
						appItem.setPolicyStatus(true);
					} else {
						mAppInfosByPolicy.add(appItem);
						appItem.setPolicyStatus(false);
					}
				}
			}
		}
	}
	
	/**
	 * 获得定向应用
	 * @param imsi
	 * @return
	 */
	private ArrayList<String> getOrientApps(String imsi) {
		  ArrayList<String> addUidList = new ArrayList<String>();
		  String addUids = PreferenceUtil.getString(this, imsi, PreferenceUtil.ORIENT_APP_ADDED_KEY, "");
		  if (addUids.contains(",")) {
			  String[] addUidsArray = addUids.split(",");
			  if (null != addUidsArray &&  addUidsArray.length > 0) {
				  Collections.addAll(addUidList, addUidsArray);
			  }
		  }
		  return addUidList;
	}
	
     /**
      * 清除缓存设置
      */
     private void clearCrash() {
    	mAppInfos.clear();
     	mAppInfosByPolicy.clear();
     	mAppInfosNoPolicy.clear();
     	mAppForegroundDatas.clear();
     	mAppDefaultDatas.clear();
     	TrafficStats.closeQuietly(mStatsSession);
     	getLoaderManager().destroyLoader(LOADER_SUMMARY);
     }
	
	 private final LoaderCallbacks<NetworkStats> mSummaryCallbacks = new LoaderCallbacks<NetworkStats>() {
        private SummaryForAllUidLoader summaryAllUidLoader;
		 
		 @Override
	        public Loader<NetworkStats> onCreateLoader(int id, Bundle args) {
			     summaryAllUidLoader = new SummaryForAllUidLoader(DataRangeActivity.this, mStatsSession, args);
	             return summaryAllUidLoader;
	        }


	     @Override
	     public void onLoadFinished(Loader<NetworkStats> loader, NetworkStats data) {
	     	  NetworkStats.Entry entry = null;
	     	  NetworkStats stats = null;
	     	  stats = data;
	     	   int uid = 0;
	           final int size = stats != null ? stats.size() : 0;
	           long usedData = 0;
	           String imsi = summaryAllUidLoader.getStatsImsi();
	           ArrayList<String> uidOrientList = getOrientApps(imsi);
	           String tvDataType = mTvDataType.getText().toString();
	           for (int i = 0; i < size; i++) {
	               entry = stats.getValues(i, entry);
	               uid = entry.uid;
	               Log.e(TAG, ">>>SET_DEFAULT>>>>>" + entry);
	                boolean isExit = false;
	                if (null != uidOrientList && uidOrientList.size() > 0) {
	  					//过滤定向应用流量
	  					for (int j = 0; j < uidOrientList.size(); j++) {
	  						if (entry.uid == Integer.parseInt(uidOrientList.get(j).trim())) {
	  							isExit = true;
	  							break;
	  						}
	  					}
	  				}
	               AppItem appItem = new AppItem();
	               if (tvDataType.equals(getString(R.string.data_mobile)) || tvDataType.equals(getString(R.string.data_wifi))) {
	                	//统计前台/后台数据
	            	  if (!isExit) {
	            	    usedData = entry.rxBytes + entry.txBytes;
	            	    appItem.setAppData(usedData);
	            	    appItem.setAppUid(uid);
	            	    mAppForegroundDatas.add(appItem);
		  			 }
	               } else  if (NetworkStats.SET_DEFAULT == entry.set && tvDataType.equals(getString(R.string.net_bg))) {
	                	 //统计后台数据
	            	   if (!isExit) {
	            	    	 AppItem appItemBg = new AppItem();
	            	    	 long usedDataBg = entry.rxBytes + entry.txBytes;
	            	    	 appItemBg.setAppData(usedDataBg);
	            	    	 appItemBg.setAppUid(uid);
	            	    	 mAppDefaultDatas.add(appItemBg);
		  			   }
	               }
	           }
	           if (tvDataType.equals(getString(R.string.data_wifi))) {
	        	   mUseDataAdapter.setAppInfosData(mAppForegroundDatas);
      			   mStopDataAdapter.setAppInfosData(mAppForegroundDatas);
        		   return;
	           }
	           if (mIsStatsTotal) {
	         	  //统计双卡流量总和
	        	  mStatsCount++;
	         	  if (mStatsCount >= DataManagerApplication.mImsiArray.length) {
	         		  if (tvDataType.equals(getString(R.string.data_mobile))) {
	         			  mUseDataAdapter.setAppInfosData(mAppForegroundDatas);
	         			  mStopDataAdapter.setAppInfosData(mAppForegroundDatas);
	         		  } else if (tvDataType.equals(getString(R.string.net_bg))) {
	         			  mUseDataAdapter.setAppInfosData(mAppDefaultDatas);
	         			  mStopDataAdapter.setAppInfosData(mAppDefaultDatas);
	         		  }
	         		  Log.v(TAG, "entry.rxBytes>>>" + Formatter.formatFileSize(DataRangeActivity.this, usedData) + ">>mStatsCount>>>" + mStatsCount);
	         	  } else {
	         		  //统计第二张sim卡流量排行
	         		 if (TAB_WIFI.equals(mCurrentNetType)) {
		        		   return;
		        	  }
	         		  init(mStatsCount, false, mCurrentNetType);
	         	  }
	           } else {
	         	  //统计单张sim卡流量排行
	         	  if (tvDataType.equals(getString(R.string.data_mobile)) || tvDataType.equals(getString(R.string.data_wifi))) {
         			   mUseDataAdapter.setAppInfosData(mAppForegroundDatas);
         			   mStopDataAdapter.setAppInfosData(mAppForegroundDatas);
         		  } else if (tvDataType.equals(getString(R.string.net_bg))) {
         			  mUseDataAdapter.setAppInfosData(mAppDefaultDatas);
         			  mStopDataAdapter.setAppInfosData(mAppDefaultDatas);
         		  } 
	           }
	     }

	     @Override
	     public void onLoaderReset(Loader<NetworkStats> loader) {
//	         mAdapter.bindStats(null, new int[0]);
//	         updateEmptyVisible();
	     }
	 };

	 /**
	  * 添加或删除应用广播
	  */
	 private BroadcastReceiver updateAppReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String updateAppName = intent.getStringExtra("UPDATE_APP_NAME");
			int updateTag = intent.getIntExtra("UPDATE_APP_TAG", 0);
			PackageManager pm = context.getPackageManager();
			PackageInfo packageInfo = null;
			try {
				if (AppReceiver.PACKAGEADDED == updateTag) {
					packageInfo = pm.getPackageInfo(updateAppName, 0);
					mAppInfos.add(packageInfo);
				} else if (AppReceiver.PACKAGEREMOVED == updateTag) {
					for (int i = 0; i < mAppInfos.size(); i++) {
						packageInfo = mAppInfos.get(i);
						if (TextUtils.equals(updateAppName, packageInfo.packageName)) {
							mAppInfos.remove(packageInfo);
							break;
						}
					}
				}
				getLoaderManager().destroyLoader(LOADER_SUMMARY);
				updateUI(mNetType);
				Log.v(TAG, "updateAppName>>>" + updateAppName + ">mAppInfos>>>>>" + mAppInfos.size());
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		 
	 };
	 
	@Override
	public void setSimStateChangeListener(int simState) {
		//监听卡的状态并更新界面
		
	}
}
