package com.monster.netmanage.activity;

import static android.net.NetworkTemplate.buildTemplateMobileAll;

import java.util.ArrayList;
import java.util.Collections;

import com.monster.netmanage.R;
import com.monster.netmanage.adapter.OrientAppAdapter;
import com.monster.netmanage.entity.AppItem;
import com.monster.netmanage.receiver.AppReceiver;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.StringUtil;
import com.monster.netmanage.utils.ToolsUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import mst.widget.FoldProgressBar;
import mst.widget.recycleview.RecyclerView;
import mst.widget.toolbar.Toolbar;

/**
 * 定向流量应用设置界面
 * 
 * @author zhaolaichao
 */
public class OrientAppActivity extends BaseActivity {

	private static final String TAG = "OrientAppActivity";
	private static final String TAB_MOBILE = "mobile";
	private static final int STATS_APP = 10000;
	private TextView mTvNoApp;
	private FoldProgressBar mFoldProgressBar;
	/**
	 * 定向应用列表
	 */
	private RecyclerView mRvOrientApp;
    
    private NetworkTemplate mTemplate;
	private INetworkStatsSession mStatsSession;
	private INetworkStatsService mStatsService;
	private TelephonyManager mTelephonyManager;
	
	private OrientAppAdapter mOrientAppAdapter;
    /**
     * 已添加的定向应用集合
     */
    private ArrayList<AppItem> mAddAppInfos = new ArrayList<AppItem>();
    private ArrayList<PackageInfo> mAppAllInfos = new ArrayList<PackageInfo>();
    
    private String mCurrectImsi; 
    /**
	  * 当前选中的网络数据类型
	  */
    private String mCurrentNetType = null;
    
    private int mSelectedIndex;
    private String[] mAddUidsArray;
    private long mStart;
    private long mEnd;
    
    Handler mHandler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		switch (msg.what) {
			case OrientAppAdapter.UPDATE_UI_TAG:
				mTvNoApp.setVisibility(View.VISIBLE);
				break;
			case STATS_APP:
				statsAppData();
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
		setMstContentView(R.layout.activity_orient_app);
		mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
		mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		try {
			mStatsSession = mStatsService.openSession();
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		//初始化数据
		initSimInfo();
		initView();
		registerUpadateApp();
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				mAppAllInfos.addAll(ToolsUtil.getPackageInfos(OrientAppActivity.this));
				getAddApps();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				mFoldProgressBar.setVisibility(View.GONE);
				if (mAddAppInfos.size() == 0) {
					mHandler.sendEmptyMessage(OrientAppAdapter.UPDATE_UI_TAG);
				} 
				mHandler.sendEmptyMessage(STATS_APP);
			}
			
		}.execute();
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
	 
	private void initView() {
		Toolbar toolbar = getToolbar();
		 toolbar.setTitle(getString(R.string.data_orient_app));
		 toolbar.inflateMenu(R.menu.toolbar_action_add_button);
		 toolbar.hideOverflowMenu();
		 toolbar.setElevation(1);
		 
		mFoldProgressBar = (FoldProgressBar) findViewById(R.id.progressbar);
		mFoldProgressBar.setVisibility(View.VISIBLE);
		mRvOrientApp = (RecyclerView) findViewById(R.id.recycler_orient_app);
		mTvNoApp = (TextView) findViewById(R.id.tv_no_orient_app);
		mOrientAppAdapter = new OrientAppAdapter(this, mHandler, mCurrectImsi);
		mRvOrientApp.setAdapter(mOrientAppAdapter);
	}

	/**
	 * 初始化数据
	 */
	private void initSimInfo() {
		mSelectedIndex = getIntent().getIntExtra("CURRENT_INDEX", 0);
    	if (mSelectedIndex == 0) {
	    	//卡1
    		mCurrectImsi = PreferenceUtil.getString(this, PreferenceUtil.SIM_1, PreferenceUtil.IMSI_KEY, "");
	    } else if (mSelectedIndex == 1) {
	    	//卡2
	    	mCurrectImsi = PreferenceUtil.getString(this, PreferenceUtil.SIM_2, PreferenceUtil.IMSI_KEY, "");
	    }
	}
	
	@Override
	public void onNavigationClicked(View view) {
		super.onNavigationClicked(view);
		this.finish();
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		Intent addIntent = new Intent(OrientAppActivity.this, AddOrientAppActivity.class);
		addIntent.putExtra("CURRENT_INDEX", mSelectedIndex);
		startActivityForResult(addIntent, 0);
		return super.onMenuItemClick(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == 1000) {
			ArrayList<AppItem> appItems = data.getParcelableArrayListExtra("ADD_ORIENT_APPS");
			if (appItems.size() > 0) {
				mTvNoApp.setVisibility(View.GONE);
				mAddAppInfos.addAll(appItems);
				mHandler.sendEmptyMessage(STATS_APP);
			}
			Log.v(TAG, "appItems>>" + appItems.size());
		}
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != mAddAppInfos) {
			mAddAppInfos.clear();
		}
		unRegisterUpdateApp();
	}
	
	/**
	 * 获得已添加的应用
	 */
	private void getAddApps() {
		String addUids = PreferenceUtil.getString(this, mCurrectImsi, PreferenceUtil.ORIENT_APP_ADDED_KEY, "");
		Log.v(TAG, "getAddApps>>" + addUids);
		if (!TextUtils.isEmpty(addUids)) {
			if (addUids.contains(",")) {
				mAddUidsArray = addUids.split(",");
				for (String addUid : mAddUidsArray) {
					AppItem appItem = new AppItem();
					for (int i = 0; i < mAppAllInfos.size(); i++) {
						PackageInfo packageInfo = mAppAllInfos.get(i);
						int uid = packageInfo.applicationInfo.uid;
						if (uid == Integer.parseInt(addUid)) {
							appItem.setAppUid(uid);
							appItem.setPackageInfo(packageInfo);
							mAddAppInfos.add(appItem);
							break;
						}
					}
				}
			}
		}
	}
	
	/**
	 * 初始化流量统计查询
	 */
	private void initStatus() {
		mCurrentNetType = TAB_MOBILE;
	    String simImsi = ToolsUtil.getActiveSubscriberId(this, ToolsUtil.getIdInDbBySimId(this, mSelectedIndex));
	    if (TAB_MOBILE.equals(mCurrentNetType)) {
            Log.d(TAG, "updateBody() mobile tab");

            // Match mobile traffic for this subscriber, but normalize it to
            // catch any other merged subscribers.
            mTemplate = buildTemplateMobileAll(simImsi);
            mTemplate = NetworkTemplate.normalize(mTemplate, mTelephonyManager.getMergedSubscriberIds());

       } else {
            throw new IllegalStateException("unknown tab: " + mSelectedIndex);
        }
		//当前月结日
		int closeDay = PreferenceUtil.getInt(this, simImsi, PreferenceUtil.CLOSEDAY_KEY, 1);
		mStart = StringUtil.getDayByMonth(closeDay);
		mEnd = StringUtil.getDayByNextMonth(closeDay);
	}
	
	/**
	 * 统计所用流量
	 */
	private void statsAppData() {
		new AsyncTask<NetworkStats, Void, NetworkStats>(){

			@Override
			protected NetworkStats doInBackground(NetworkStats... params) {
				NetworkStats networkStats = null;
				try {
					initStatus();
				    networkStats = mStatsSession.getSummaryForAllUid(mTemplate, mStart, mEnd, false);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				return networkStats;
			}

			@Override
			protected void onPostExecute(NetworkStats result) {
				super.onPostExecute(result);
	        	  NetworkStats.Entry entry = null;
	        	  NetworkStats stats = null;
	        	  stats = result;
	              final int size = stats != null ? stats.size() : 0;
	              long usedData = 0;
	              for (int i = 0; i < size; i++) {
	                  entry = stats.getValues(i, entry);
	                  final int uid = entry.uid;
	                  for (int j = 0; j < mAddAppInfos.size(); j++) {
	                	  AppItem appItem = mAddAppInfos.get(j);
						  if (TextUtils.equals("" + uid, "" + appItem.getAppUid())) {
							  usedData = entry.rxBytes + entry.txBytes;
							  appItem.setAppData(usedData);
							  break;
						  }
					  }
	                  Log.v(TAG, "entry.rxBytes>>>" + entry + ">>>" + entry.rxBytes + ">>>>entry.txBytes>>" + entry.txBytes);
	              }
	              Collections.sort(mAddAppInfos);
	              mOrientAppAdapter.setAddAppList(mAddAppInfos);
	              mOrientAppAdapter.notifyDataSetChanged();
			}
		}.execute();

    }

    /**
	  * 添加或删除应用广播
	  */
	 private BroadcastReceiver updateAppReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String updateAppName = intent.getStringExtra("UPDATE_APP_NAME");
			int updateTag = intent.getIntExtra("UPDATE_APP_TAG", 0);
			try {
			    if (AppReceiver.PACKAGEREMOVED == updateTag) {
			    	for (int i = 0; i < mAddAppInfos.size(); i++) {
			    		AppItem appItem = mAddAppInfos.get(i);
			    		PackageInfo packageInfo = appItem.getPackageInfo();
			    		if (TextUtils.equals(updateAppName, packageInfo.packageName)) {
			    			mOrientAppAdapter.removeApp(i);
			    			break;
			    		}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		 
	 };
	 
	@Override
	public void setSimStateChangeListener(int simState) {
		// TODO Auto-generated method stub
		
	}
	
}
