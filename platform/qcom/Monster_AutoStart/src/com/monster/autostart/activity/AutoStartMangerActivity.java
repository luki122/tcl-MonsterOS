package com.monster.autostart.activity;

import java.util.ArrayList;
import java.util.List;

import mst.view.menu.MstActionMenuView.OnMenuItemClickListener;

import com.monster.autostart.R;
import com.monster.autostart.adapter.AutoStartManagerAdapter;
import com.monster.autostart.bean.AppInfo;
import com.monster.autostart.bean.AppManagerState;
import com.monster.autostart.bean.AppsChangeController;
import com.monster.autostart.db.MulwareProvider;
import com.monster.autostart.loader.AutoStartLoader;
import com.monster.autostart.utils.Utilities;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AutoStartMangerActivity extends BaseActivity implements
		AutoStartLoader.Callbacks, OnMenuItemClickListener {

	AutoStartManagerAdapter adapter;

	RelativeLayout sContentMask;

	TextView sAutoAppsCount;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		sContentMask = (RelativeLayout) findViewById(R.id.rel_content_mask);
		sList = (ListView) findViewById(R.id.lv1);
	
		sAutoAppsCount = (TextView)findViewById(R.id.tv_totalcount);
		
		getToolbar().inflateMenu(R.menu.add_item);
		getToolbar().setOnMenuItemClickListener(this);

		sLoader.initialize(this);

		sLoader.startLoader();

	}

	@Override
	public void bintItems() {
		// TODO Auto-generated method stub
		Log.e(Utilities.TAG, "_CLS_:" + "AutoStartMangerActivity" + ";"
				+ "_FUNCTION_:" + "bintItems" + ";");
		Runnable r = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				bintItems();
			}
		};

		if (waitUntilResume(r)) {
			return;
		}

		if (adapter == null) {
			//TODO sContenList needs remove the same title app
			sContenList = getAppsList(Utilities.COMPONENT_AUTO_START_ENABLE);

			//sResultList.addAll(sContenList);
			
			//Then start to filter the duplicate data 
			sResultList = Utilities.getInstance().removeDuplicate(sContenList);
			
			adapter = new AutoStartManagerAdapter(this, sResultList);
			
			sList.setAdapter(adapter);

			if (sResultList.size() > 0){
				hideMask();
				showContentSize(sResultList.size());
			}
				

		} else {
			Log.e("sunset", "adapter.notifyDataSetChanged()");
			adapter.notifyDataSetChanged();
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Log.e(Utilities.TAG, "_CLS_:" + "AutoStartMangerActivity" + ";"
				+ "_FUNCTION_:" + "onActivityResult" + ";");
		if (sLoader.getCallback() != this) {
			sLoader.initialize(this);
		}
		if (data != null) {
			boolean result = data.getExtras().getBoolean("update", false);
			if (result) {
				hideMask();
				//TODO sContenList needs remove the same title app
				sContenList.clear();
				sContenList
						.addAll(getAppsList(Utilities.COMPONENT_AUTO_START_ENABLE));
				
				//Then start to filter the duplicate data 
				sResultList.clear();
				sResultList.addAll(Utilities.getInstance().removeDuplicate(sContenList));
			//	sResultList = Utilities.getInstance().removeDuplicate(sContenList);
				
				showContentSize(sResultList.size());
				
				adapter.notifyDataSetChanged();
				
			}
		}

	}

	private void hideMask() {
		sContentMask.setVisibility(View.INVISIBLE);
		sList.setVisibility(View.VISIBLE);
		sAutoAppsCount.setVisibility(View.VISIBLE);
	}

	public void showMask() {
		sList.setVisibility(View.INVISIBLE);
		sContentMask.setVisibility(View.VISIBLE);
		sAutoAppsCount.setVisibility(View.GONE);
	}
	
	
	public void showContentSize(int size){
		String text = String.format(getResources().getString(R.string.str_auto_app_count), size);  
		sAutoAppsCount.setText(text);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		startActivityForResult(new Intent(AutoStartMangerActivity.this,
				AddAutoStartAppActivity.class), 1);

		return false;
	}
	
	@Override
	public void onNavigationClicked(View view) {
		// TODO Auto-generated method stub
		this.finish();
	}

}
