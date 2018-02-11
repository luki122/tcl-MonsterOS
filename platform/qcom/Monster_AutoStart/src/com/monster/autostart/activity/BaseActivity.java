package com.monster.autostart.activity;

import java.util.ArrayList;
import java.util.List;

import com.monster.autostart.bean.AppInfo;
import com.monster.autostart.bean.AppManagerState;
import com.monster.autostart.db.MulwareProvider;
import com.monster.autostart.loader.AutoStartLoader;
import com.monster.autostart.utils.Utilities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

import mst.app.MstActivity;

public class BaseActivity extends MstActivity {

	private ArrayList<Runnable> mOnResumeCallbacks = new ArrayList<Runnable>();
	private ArrayList<Runnable> mBindOnResumeCallbacks = new ArrayList<Runnable>();

	private boolean mPaused = true;

	protected AppManagerState mState;

	protected AutoStartLoader sLoader;

	protected ListView sList;

	/**full of data list*/
	protected List<AppInfo> sContenList;
	
	/**remove the duplicate data list*/
	protected List<AppInfo> sResultList = new ArrayList<AppInfo>();
	
	protected MulwareProvider sProvider;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mState = AppManagerState.getInstance();

		sLoader = mState.getLoader();

		sProvider = mState.getAppProvider();
		
		mPaused = false;
		
		super.onCreate(savedInstanceState);
	

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.e(Utilities.TAG, "_CLS_:" + "BaseActivity" + ";" + "_FUNCTION_:"
				+ "onResume" + ";" + "mBindOnResumeCallbacks="
				+ mBindOnResumeCallbacks.size());
		mPaused = false;

		if (mBindOnResumeCallbacks.size() > 0) {
			for (int i = 0; i < mBindOnResumeCallbacks.size(); i++) {
				mBindOnResumeCallbacks.get(i).run();
			}
			mBindOnResumeCallbacks.clear();
		}

		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.e(Utilities.TAG, "_CLS_:" + "BaseActivity" + ";" + "_FUNCTION_:"
				+ "onPause");
		super.onPause();
		mPaused = true;
	}

	protected boolean waitUntilResume(Runnable run) {
		return waitUntilResume(run, false);
	}

	protected boolean waitUntilResume(Runnable run,
			boolean deletePreviousRunnables) {
		Log.e(Utilities.TAG, "_CLS_:" + "BaseActivity" + ";"
				+ "_FUNCTION_:" + "waitUntilResume" + ";" + "mPaused="
				+ mPaused);
		if (mPaused) {

			if (deletePreviousRunnables) {
				while (mBindOnResumeCallbacks.remove(run)) {
				}
			}
			mBindOnResumeCallbacks.add(run);
			return true;
		} else {
			return false;
		}
	}

	private void initState() {

		getWindow()
				.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

	}
	
	public List<AppInfo> getAppsList(int status) {
		List<AppInfo> list = new ArrayList<AppInfo>();

		MulwareProvider pv = AppManagerState.getInstance().getAppProvider();

		String[] params = new String[] { String.valueOf(status) };
		String[] projection = new String[] {"intent", "status" };
		list = pv.query(projection, "status=?", params);

		return list;
	}
	
	@Override
	public void setContentView(int layoutResID) {
		// TODO Auto-generated method stub
		super.setMstContentView(layoutResID);
	}
	
	
	public List<AppInfo> getList(){
		return this.sContenList;
	}
}
