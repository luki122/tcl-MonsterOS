package com.monster.netmanage.activity;

import java.util.ArrayList;

import com.monster.netmanage.R;
import com.monster.netmanage.adapter.AddOrientAppAdapter;
import com.monster.netmanage.entity.AppItem;
import com.monster.netmanage.receiver.AppReceiver;
import com.monster.netmanage.utils.PreferenceUtil;
import com.monster.netmanage.utils.ToolsUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import mst.widget.FoldProgressBar;
import mst.widget.recycleview.RecyclerView;
import mst.widget.toolbar.Toolbar;

/**
 * 添加定向流量应用界面
 * 
 * @author zhaolaichao
 */
public class AddOrientAppActivity extends BaseActivity{

	private AddOrientAppAdapter mAddAppAdapter;
	/**
	 * 定向应用列表
	 */
    private RecyclerView mRvAddOrientApp;
    private FoldProgressBar mFoldProgressBar;
    
    private ArrayList<PackageInfo> mAppInfos;
    private ArrayList<PackageInfo> mUnAppInfos = new ArrayList<PackageInfo>();
    private String mCurrectImsi;
    private int mSelectedIndex;
    private AsyncTask<Void, Void, Void> mTask;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setMstContentView(R.layout.activity_add_orient_app);
		initSimInfo();
		initView();
		registerUpadateApp();
		mTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				mAppInfos = ToolsUtil.getPackageInfos(AddOrientAppActivity.this);
				//初始化数据
				getUnAddApps();
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				mFoldProgressBar.setVisibility(View.GONE);
				mAddAppAdapter.setAppList(mUnAppInfos);
				mRvAddOrientApp.setAdapter(mAddAppAdapter);
			}
			
		}.execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != mTask) {
			mTask.cancel(true);
		}
		mAddAppAdapter = null;
		unRegisterUpdateApp();
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
		 toolbar.setTitle(getString(R.string.add_data_orient_app));
		 toolbar.setElevation(1);
		mFoldProgressBar = (FoldProgressBar) findViewById(R.id.progressbar);
		mFoldProgressBar.setVisibility(View.VISIBLE);
		mRvAddOrientApp = (RecyclerView) findViewById(R.id.recycler_add_orient_app);
		mAddAppAdapter = new AddOrientAppAdapter(this, mCurrectImsi);
	}

	private void getUnAddApps() {
		//取出已添加过的UID
		String addedAppUids = PreferenceUtil.getString(this, mCurrectImsi, PreferenceUtil.ORIENT_APP_ADDED_KEY, "");
	   Log.e("addedAppUids", "addedAppUids>>>" + addedAppUids);
		if (!TextUtils.isEmpty(addedAppUids) && addedAppUids.length() > 0) {
	    	if (addedAppUids.contains(",")) {
	    		String[] uidsArray = addedAppUids.split(",");
	    		for (int i = 0; i < mAppInfos.size(); i++) {
	    			PackageInfo packageInfo = mAppInfos.get(i);
	    			int uid = packageInfo.applicationInfo.uid;
	    			boolean isexit = false;
	    			for (int j = 0; j < uidsArray.length; j++) {
	    				if (uid == Integer.parseInt(uidsArray[j])){
	    					isexit = true;
	    					break;
	    				} 
	    			}
	    			//添加没有添加过的应用
	    			if (!isexit) {
	    				mUnAppInfos.add(packageInfo);
	    			}
	    		}
	    	}
	    } else {
	    	mUnAppInfos.addAll(mAppInfos);
	    }
	}
	
	@Override
	public void onNavigationClicked(View view) {
		super.onNavigationClicked(view);
		Intent intent = new Intent();
		ArrayList<AppItem> addAppList = mAddAppAdapter.getAddAppList();
		intent.putParcelableArrayListExtra("ADD_ORIENT_APPS", addAppList);
		setResult(1000, intent);
		this.finish();
	}
	
	@Override
	public void onBackPressed() {
		ArrayList<AppItem> addAppList = mAddAppAdapter.getAddAppList();
		if (null != addAppList && addAppList.size() > 0) {
			Intent intent = new Intent();
			intent.putParcelableArrayListExtra("ADD_ORIENT_APPS", addAppList);
			setResult(1000, intent);
			this.finish();
		} else {
			super.onBackPressed();
		}
	}

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
					mUnAppInfos.add(packageInfo);
					mAddAppAdapter.setAppList(mUnAppInfos);
					mAddAppAdapter.notifyItemInserted(mUnAppInfos.size() == 0 ? 0 : mUnAppInfos.size() -1);
				} else if (AppReceiver.PACKAGEREMOVED == updateTag) {
					for (int i = 0; i < mUnAppInfos.size(); i++) {
						packageInfo = mUnAppInfos.get(i);
						if (TextUtils.equals(updateAppName, packageInfo.packageName)) {
							mUnAppInfos.remove(i);
							mAddAppAdapter.setAppList(mUnAppInfos);
							mAddAppAdapter.notifyItemRemoved(i);
							break;
						}
					}
				}
				Log.v("AddOrientAppActivity", "updateAppName>>>" + updateAppName + ">mAppInfos>>>>>" + mAppInfos.size());
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		 
	 };
	@Override
	public void setSimStateChangeListener(int simState) {
		
	}
}
