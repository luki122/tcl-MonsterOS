package com.monster.netmanage.activity;

import static android.net.NetworkPolicyManager.POLICY_REJECT_METERED_BACKGROUND;
import static android.net.NetworkTemplate.buildTemplateMobileAll;
import static android.net.NetworkTemplate.buildTemplateWifiWildcard;

import java.util.ArrayList;

import com.monster.netmanage.DataManagerApplication;
import com.monster.netmanage.R;
import com.monster.netmanage.adapter.RangeAppAdapter;
import com.monster.netmanage.entity.AppItem;
import com.monster.netmanage.net.SummaryForAllUidLoader;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.StringUtil;
import com.monster.netmanage.utils.ToolsUtil;
import com.monster.netmanage.view.ListViewAuto;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.content.pm.PackageInfo;
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
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
	 private TextView mTvTitle;
	 private TextView mTvDataType;
	 private TextView mTvStopData;
	 private TextView mTvUseData;
	 private FoldProgressBar mFoldProgressBar;
	 
	 private String[] mDataTypeArray = null;
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
	 
	 private ArrayList<PackageInfo> mAppInfos;
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
				mUseDataAdapter.notifyDataSetChanged();
				//禁止使用网络
				mStopDataAdapter.setAppList(appInfosByPolicy);
				mStopDataAdapter.setStopNetList(appInfosByPolicy);
				mStopDataAdapter.setUsedNetList(appInfosNoPolicy);
				mStopDataAdapter.notifyDataSetChanged();
				break;
			case DIALOG_TAG:
				mFoldProgressBar.setVisibility(View.GONE);
				break;
			default:
				break;
			}
		 };
	 };
	 
	 @Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
	}
	 
	 @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_range_data_app);
		mDataTypeArray = new String[]{ getString(R.string.data_mobile), getString(R.string.net_bg), getString(R.string.data_wifi)};
		mPolicyManager = NetworkPolicyManager.from(this);
	    mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
	    mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		try {
		       mStatsSession = mStatsService.openSession();
		} catch (RemoteException e) {
		      throw new RuntimeException(e);
		}
		mTitle = getIntent().getStringExtra("SIM_TITLE");
		mTitle = TextUtils.isEmpty(mTitle) ? getString(R.string.net_control) : mTitle;
		mIsStatsTotal = getIntent().getBooleanExtra("SIM_COUNT", false);
		mCurrentIndex = getIntent().getIntExtra("CURRENT_INDEX", 0);
		mPolicy = getIntent().getIntExtra("STATS_POLICY", mPolicy);
		updateSimChange();
		initTitle();
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				mAppInfos = ToolsUtil.getPackageInfos(DataRangeActivity.this);
				getDataMobileAppsByPolicy(mNetType, initDataNet(mNetType));
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				mHandler.obtainMessage(DIALOG_TAG).sendToTarget();
				initView();
				init(mCurrentIndex, true, mCurrentNetType);
			}
			
		}.execute();
	}

	 @Override
	protected void onResume() {
//		getLoaderManager().destroyLoader(LOADER_SUMMARY);
		super.onResume();
		
	}
	/**
	 * 监听卡的状态并更新界面
	 */
	private void updateSimChange() {
	   mStatsCount = 0;
	   int simCount = DataManagerApplication.mImsiArray.length;
	   if (simCount > 1) {
		   if (TextUtils.isEmpty(DataManagerApplication.mImsiArray[0]) || TextUtils.isEmpty(DataManagerApplication.mImsiArray[1])){
			   //显示wifi流量
			   mCurrentNetType = TAB_WIFI;
//			   mPolicy  = wifi;
			   mCurrentIndex = 0;
		   } 
	   } else {
		   //只有一张卡时
//		   mCurrentNetType = TAB_MOBILE;
		   mCurrentIndex = 0;
	   }
	   String netType = PreferenceUtil.getString(this, DataManagerApplication.mImsiArray[mCurrentIndex], PreferenceUtil.SELECTED_NETTYPE_KEY, null);
       if (TextUtils.isEmpty(netType)) {
        	if (mPolicy == POLICY_REJECT_METERED_BACKGROUND) {
        		mNetType = getString(R.string.net_bg);
        	 } else {
        		if (TextUtils.isEmpty(DataManagerApplication.mImsiArray[0])) {
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
//		mTvTitle = (TextView)findViewById(R.id.tv_title);
//		mTvTitle.setText(mTitle);
		mLayType = (RelativeLayout) findViewById(R.id.lay_net_type);
		mLayStop = (LinearLayout) findViewById(R.id.lay_stop_net);
		mLayUsed = (LinearLayout) findViewById(R.id.lay_used_net);
        mTvDataType = (TextView)findViewById(R.id.tv_data_type);
		mTvDataType.setText(mNetType);
		mTvDataType.setVisibility(View.VISIBLE);
		mLayType.setOnClickListener(this);
	}
	
	private void initView() {
		mTvStopData = (TextView) findViewById(R.id.tv_stop_data);
		if (mAppInfosByPolicy.size() == 0) {
			mLayStop.setVisibility(View.GONE);
		}
		mTvStopData.setText(String.format(getString(R.string.data_stop_info), "" + mAppInfosByPolicy.size(), mTvDataType.getText().toString()));
		mLvStopData = (ListViewAuto) findViewById(R.id.lv_stop_data);
		mStopDataAdapter = new RangeAppAdapter(this, mPolicyManager, mHandler);
		mStopDataAdapter.setUseNet(RangeAppAdapter.STOP_NET);
		mStopDataAdapter.setNetType(mDataTypeArray[0]);
		mStopDataAdapter.setAppList(mAppInfosByPolicy);
		mStopDataAdapter.setUsedNetList(mAppInfosNoPolicy);
		mStopDataAdapter.setStopNetList(mAppInfosByPolicy);
		mLvStopData.setAdapter(mStopDataAdapter);
		mLvStopData.setNestedScrollingEnabled(false);
		mLvStopData.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				/*if (null != mStopDataAdapter) {
					Switch tBtn = (Switch)view.findViewById(R.id.togglebtn);
					tBtn.setChecked(true);
					mStopDataAdapter.setOnSelectedListener(position, tBtn);
				}*/
			}
		});
		
		mTvUseData = (TextView) findViewById(R.id.tv_use_data);
		if (mAppInfosNoPolicy.size() == 0) {
			mLayUsed.setVisibility(View.GONE);
		}
		mTvUseData.setText(String.format(getString(R.string.data_use_info), "" + mAppInfosNoPolicy.size(), mTvDataType.getText().toString()));
		mLvUseData = (ListViewAuto) findViewById(R.id.lv_use_data);
		mUseDataAdapter = new RangeAppAdapter(this, mPolicyManager,  mHandler);
		mUseDataAdapter.setUseNet(RangeAppAdapter.USE_NET);
		mUseDataAdapter.setNetType(mDataTypeArray[0]);
		mUseDataAdapter.setAppList(mAppInfosNoPolicy);
		mUseDataAdapter.setUsedNetList(mAppInfosNoPolicy);
		mUseDataAdapter.setStopNetList(mAppInfosByPolicy);
		mLvUseData.setAdapter(mUseDataAdapter);
		mLvUseData.setNestedScrollingEnabled(false);
		mLvUseData.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				/*if (null != mUseDataAdapter) {
					Switch tBtn = (Switch)view.findViewById(R.id.togglebtn);
					tBtn.setChecked(false);
					mUseDataAdapter.setOnSelectedListener(position, tBtn);
				}*/
			}
		});
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
		  long start = StringUtil.getDayByMonth(closeDay);
		  long end = StringUtil.getDayByNextMonth(closeDay);
		  if (isInit) {
			  getLoaderManager().initLoader(LOADER_SUMMARY,
					  SummaryForAllUidLoader.buildArgs(mTemplate, start, end), mSummaryCallbacks);
		  } else {
			  getLoaderManager().restartLoader(LOADER_SUMMARY,
					  SummaryForAllUidLoader.buildArgs(mTemplate, start, end), mSummaryCallbacks);
		  }
	}
	
	@Override
	protected void onRestart() {
		getLoaderManager().destroyLoader(LOADER_SUMMARY);
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
		String simImsi = ToolsUtil.getActiveSubscriberId(this, ToolsUtil.getIdInDbBySimId(this, mCurrentIndex));
		PreferenceUtil.putString(this, simImsi, PreferenceUtil.SELECTED_NETTYPE_KEY, mNetType);
		clearCrash();
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
        	mAppInfosByPolicy.clear();
        	mAppInfosNoPolicy.clear();
        	getDataMobileAppsByPolicy(item, initDataNet(item));
        	
        	mStopDataAdapter.setNetType(item);
        	mStopDataAdapter.setUseNet(RangeAppAdapter.STOP_NET);
        	mStopDataAdapter.setAppList(mAppInfosByPolicy);
        	mStopDataAdapter.setStopNetList(mAppInfosByPolicy);
        	mStopDataAdapter.setUsedNetList(mAppInfosNoPolicy);
        	
        	mUseDataAdapter.setNetType(item);
        	mUseDataAdapter.setUseNet(RangeAppAdapter.USE_NET);
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
	 * @param policy
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
				} else {
					//允许联网策略
					mAppInfosNoPolicy.add(appItem);
				}
			} else {
				if (uidList == null || uidList.size() == 0) {
					mAppInfosNoPolicy.add(appItem);
				} else {
					if (!uidList.contains(uid)) {
						mAppInfosNoPolicy.add(appItem);
					} else {
						mAppInfosByPolicy.add(appItem);
					}
				}
			}
		}
	}
	
     /**
      * 清除缓存设置
      */
     private void clearCrash() {
     	if (mStopDataAdapter != null) {
     		mStopDataAdapter = null;
     	}
     	if (mUseDataAdapter != null) {
     		mUseDataAdapter = null;
     	}
     	mAppInfosByPolicy.clear();
     	mAppInfosNoPolicy.clear();
     	mAppForegroundDatas.clear();
     	mAppDefaultDatas.clear();
     	mAppInfosByPolicy = null;
     	mAppInfosNoPolicy = null;
     	mAppForegroundDatas = null;
     	mAppDefaultDatas = null;
     	TrafficStats.closeQuietly(mStatsSession);
     	getLoaderManager().destroyLoader(LOADER_SUMMARY);
     }
	
	 private final LoaderCallbacks<NetworkStats> mSummaryCallbacks = new LoaderCallbacks<NetworkStats>() {
	     @Override
	     public Loader<NetworkStats> onCreateLoader(int id, Bundle args) {
	         return new SummaryForAllUidLoader(DataRangeActivity.this, mStatsSession, args);
	     }

	     @Override
	     public void onLoadFinished(Loader<NetworkStats> loader, NetworkStats data) {
	     	  NetworkStats.Entry entry = null;
	     	  NetworkStats stats = null;
	     	  stats = data;
	     	   int uid = 0;
	           final int size = stats != null ? stats.size() : 0;
	           long usedData = 0;
	           String tvDataType = mTvDataType.getText().toString();
	           for (int i = 0; i < size; i++) {
	               entry = stats.getValues(i, entry);
	               uid = entry.uid;
	               Log.e(TAG, ">>>SET_DEFAULT>>>>>" + entry);
	               AppItem appItem = new AppItem();
	               if (tvDataType.equals(getString(R.string.data_mobile)) || tvDataType.equals(getString(R.string.data_wifi))) {
	                	//统计前台/后台数据
	                	usedData = entry.rxBytes + entry.txBytes;
	                	appItem.setAppData(usedData);
	                	appItem.setAppUid(uid);
	                	mAppForegroundDatas.add(appItem);
	               } else  if (NetworkStats.SET_DEFAULT == entry.set && tvDataType.equals(getString(R.string.net_bg))) {
	                	 //统计后台数据
	            		 AppItem appItemBg = new AppItem();
	                	 long usedDataBg = entry.rxBytes + entry.txBytes;
	                	 appItemBg.setAppData(usedDataBg);
	                	 appItemBg.setAppUid(uid);
	                	 mAppDefaultDatas.add(appItemBg);
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
	         	  if (mStatsCount >= DataManagerApplication.mImsiArray.length && !TextUtils.isEmpty(DataManagerApplication.mImsiArray[0])) {
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

	@Override
	public void setSimStateChangeListener() {
		//监听卡的状态并更新界面
	}
}
