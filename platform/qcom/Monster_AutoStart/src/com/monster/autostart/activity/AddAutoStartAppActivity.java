package com.monster.autostart.activity;

import java.util.ArrayList;
import java.util.List;

import com.monster.autostart.R;
import com.monster.autostart.adapter.AddAppsAdapter;
import com.monster.autostart.bean.AppInfo;
import com.monster.autostart.db.MulwareProvider;
import com.monster.autostart.loader.AutoStartLoader;
import com.monster.autostart.utils.Utilities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import mst.widget.FoldProgressBar;
public class AddAutoStartAppActivity extends BaseActivity implements
		AutoStartLoader.Callbacks {

	AddAppsAdapter adapter;
	
	RelativeLayout  sContentMask;

	
	FoldProgressBar sWidgetProgressBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		sLoader.initialize(this);
		setContentView(R.layout.activity_add_apps);

		sList = (ListView) findViewById(R.id.add_list);

		sContentMask = (RelativeLayout)findViewById(R.id.rel_add_apps_content_mask);
		
		sWidgetProgressBar  = (FoldProgressBar)findViewById(R.id.tips_widget_progressbar);
		
		sProvider = mState.getAppProvider();
		

		sLoader.runOnWorkerThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				bindContentList();
			}
		});
	}

	@Override
	public void bintItems() {
		// TODO Auto-generated method stub
		Log.e(Utilities.TAG, "_CLS_:" + "AddAutoStartAppActivity" + ";"
				+ "_FUNCTION_:" + "bintItems" + ";");

		Runnable r = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				bintItems();
			}
		};

		if (waitUntilResume(r)) {
			Log.e(Utilities.TAG, "_CLS_:" + "AddAutoStartAppActivity" + ";"
					+ "_FUNCTION_:" + "bintItems waitUntilResume!" + ";");
			return;
		}
		bindContentList();
	}
 
	public void bindContentList() {
		//TODO sContenList needs remove the same title app
		if (adapter == null) {
			sWidgetProgressBar.setVisibility(View.VISIBLE);
			sContenList = getAppsList(Utilities.COMPONENT_AUTO_START_DISABLE);

			// sResultList.addAll(sContenList);
			sResultList = Utilities.getInstance().removeDuplicate(sContenList);
			Log.e("sunset1", "sContenList.szie=" + sContenList.size() + ";"
					+ "sResultList.size=" + sResultList.size());
			adapter = new AddAppsAdapter(AddAutoStartAppActivity.this,
					sResultList);
			sLoader.runOnMainThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					sList.setAdapter(adapter);
					sWidgetProgressBar.setVisibility(View.GONE);
				}
			});
			
		} else {
			sContenList.clear();
			sContenList
					.addAll(getAppsList(Utilities.COMPONENT_AUTO_START_DISABLE));

			sResultList.clear();
			sResultList.addAll(Utilities.getInstance().removeDuplicate(
					sContenList));

			adapter.notifyDataSetChanged();
		}

		/** M:Hazel add for fix BUG ID:3231943 begin */
		if (sResultList.size() <= 0) {
			sContentMask.setVisibility(View.VISIBLE);
		} else
			sContentMask.setVisibility(View.GONE);
		/** M:Hazel add for fix BUG ID:3231943 end */
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finished();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onNavigationClicked(View view) {
		// TODO Auto-generated method stub
		finished();		
	}

	public void finished() {
		this.finish();


	}
	

}
