package com.monster.cloud.activity;

import android.os.Bundle;

import com.monster.cloud.CloudApplication;

import mst.app.MstActivity;
import mst.widget.toolbar.Toolbar;

public abstract class BaseActivity extends MstActivity {

	protected String TAG = getClass().getSimpleName();

	protected Toolbar mToolbar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		CloudApplication.getInstance().addActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		CloudApplication.getInstance().removeActivity(this);
	}

	public abstract void initViews();
	
	public abstract void initData();

}
