package com.monster.cloud.activity;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.monster.cloud.CloudApplication;
import com.monster.cloud.receiver.NetworkConnectionReceiver;

import mst.app.MstActivity;
import mst.widget.toolbar.Toolbar;

public abstract class BaseActivity extends MstActivity {

	protected String TAG = getClass().getSimpleName();

	protected Toolbar mToolbar;
	protected NetworkConnectionReceiver netStateReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		CloudApplication.getInstance().addActivity(this);
		netStateReceiver = new NetworkConnectionReceiver() {
			@Override
			public void netNotConnected() {
				networkNotAvailable();
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(netStateReceiver, filter);


	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		CloudApplication.getInstance().removeActivity(this);
		unregisterReceiver(netStateReceiver);
	}

	public abstract void initViews();
	
	public abstract void initData();

	public abstract void networkNotAvailable();

}
