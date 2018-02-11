package com.monster.netmanage.activity;

import static android.net.NetworkTemplate.buildTemplateMobileAll;
import static android.net.NetworkTemplate.buildTemplateWifiWildcard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.monster.netmanage.R;
import com.monster.netmanage.adapter.OrientAppAdapter;
import com.monster.netmanage.entity.AppItem;
import com.monster.netmanage.net.SummaryForAllUidLoader;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.StringUtil;
import com.monster.netmanage.utils.ToolsUtil;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
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
import mst.widget.recycleview.LinearLayoutManager;
import mst.widget.recycleview.RecyclerView;
import mst.widget.recycleview.RecyclerViewDivider;
import mst.widget.toolbar.Toolbar;

/**
 * 定向流量应用设置界面
 * 
 * @author zhaolaichao
 */
public class OrientAppActivity extends BaseActivity {

	private static final String TAG = "OrientAppActivity";
	private static final int LOADER_SUMMARY = 1;
	private static final String TAB_MOBILE = "mobile";
	private static final String TAB_WIFI = "wifi";
	
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
    /**
	  * 存储app使用的流量大小
	  */
	private ArrayList<AppItem> mAppDatas = new ArrayList<AppItem>();
    private ArrayList<PackageInfo> mAppAllInfos;
    
    private String mCurrectImsi; 
    /**
	  * 当前选中的网络数据类型
	  */
    private String mCurrentNetType = null;
    
    private int mSelectedIndex;
    
    Handler mHandler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		switch (msg.what) {
			case OrientAppAdapter.UPDATE_UI_TAG:
				mTvNoApp.setVisibility(View.VISIBLE);
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
		//初始化数据
		initSimInfo();
		initView();
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				mAppAllInfos = ToolsUtil.getPackageInfos(OrientAppActivity.this);
				getAddApps();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				mFoldProgressBar.setVisibility(View.GONE);
				initStatus();
			}
			
		}.execute();
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
				mOrientAppAdapter.setAddAppList(appItems);
			}
			Log.v(TAG, "appItems>>" + appItems.size());
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != mAddAppInfos) {
			mAddAppInfos.clear();
		}
		getLoaderManager().destroyLoader(LOADER_SUMMARY);
	}
	
	@Override
	public void setSimStateChangeListener() {
		
	}
	
	/**
	 * 获得已添加的应用
	 */
	private void getAddApps() {
		String addUids = PreferenceUtil.getString(this, mCurrectImsi, PreferenceUtil.ORIENT_APP_ADDED_KEY, "");
		Log.v(TAG, "getAddApps>>" + addUids);
		if (TextUtils.isEmpty(addUids)) {
			if (mAddAppInfos.size() == 0) {
				mTvNoApp.setVisibility(View.VISIBLE);
			} 
			return;
		}
		if (addUids.contains(",")) {
			String[] addUidsArray = addUids.split(",");
			for (String addUid : addUidsArray) {
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
	
	/**
	 * 初始化流量统计查询
	 */
	private void initStatus() {
		Loader<NetworkStats> loader = getLoaderManager().getLoader(LOADER_SUMMARY);
		if (null != loader && !loader.cancelLoad()) {
			return;
		}
		mCurrentNetType = TAB_MOBILE;
        mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
        mTelephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
	    try {
	           mStatsSession = mStatsService.openSession();
	    } catch (RemoteException e) {
	          throw new RuntimeException(e);
	    }
	    String simImsi = ToolsUtil.getActiveSubscriberId(this, ToolsUtil.getIdInDbBySimId(this, mSelectedIndex));
	    if (TAB_MOBILE.equals(mCurrentNetType)) {
            Log.d(TAG, "updateBody() mobile tab");

            // Match mobile traffic for this subscriber, but normalize it to
            // catch any other merged subscribers.
            mTemplate = buildTemplateMobileAll(simImsi);
            mTemplate = NetworkTemplate.normalize(mTemplate, mTelephonyManager.getMergedSubscriberIds());

       } else if (TAB_WIFI.equals(mCurrentNetType)) {
            // wifi doesn't have any controls
            mTemplate = buildTemplateWifiWildcard();

        } else {
            throw new IllegalStateException("unknown tab: " + mSelectedIndex);
        }
		//当前月结日
		int closeDay = PreferenceUtil.getInt(this, simImsi, PreferenceUtil.CLOSEDAY_KEY, 1);
		long start = StringUtil.getDayByMonth(closeDay);
		long end = StringUtil.getDayByNextMonth(closeDay);
        getLoaderManager().initLoader(LOADER_SUMMARY,
                SummaryForAllUidLoader.buildArgs(mTemplate, start, end), mSummaryCallbacks);
	}
	
	private final LoaderCallbacks<NetworkStats> mSummaryCallbacks = new LoaderCallbacks<NetworkStats>() {
        @Override
        public Loader<NetworkStats> onCreateLoader(int id, Bundle args) {
            return new SummaryForAllUidLoader(OrientAppActivity.this, mStatsSession, args);
        }

        @Override
        public void onLoadFinished(Loader<NetworkStats> loader, NetworkStats data) {
        	  NetworkStats.Entry entry = null;
        	  NetworkStats stats = null;
        	  stats = data;
              final int size = stats != null ? stats.size() : 0;
              long usedData = 0;
              for (int i = 0; i < size; i++) {
                  entry = stats.getValues(i, entry);
                  final int uid = entry.uid;
                  AppItem appItem = new AppItem();
                  usedData = entry.rxBytes + entry.txBytes;
                  appItem.setAppData(usedData);
                  appItem.setAppUid(uid);
                  mAppDatas.add(appItem);
                  Log.v(TAG, "entry.rxBytes>>>" + entry + ">>>" + entry.rxBytes + ">>>>entry.txBytes>>" + entry.txBytes);
              }
              mOrientAppAdapter.setStatusApps(mAppDatas);
              mOrientAppAdapter.setAddAppList(mAddAppInfos);
        }

        @Override
        public void onLoaderReset(Loader<NetworkStats> loader) {
        	if(null != mOrientAppAdapter) {
        		mOrientAppAdapter.clear();
        	}
        }
    };
	
}
