package com.monster.market.activity;

import com.monster.market.MarketApplication;

import android.app.Activity;
import android.os.Bundle;

import mst.app.MstActivity;
import mst.widget.toolbar.Toolbar;

public abstract class BaseActivity extends MstActivity {

	protected String TAG = getClass().getSimpleName();

	protected Toolbar mToolbar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		MarketApplication.getInstance().addActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		MarketApplication.getInstance().removeActivity(this);
	}

	public abstract void initViews();
	
	public abstract void initData();

}
