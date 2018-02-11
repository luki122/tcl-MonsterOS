package com.monster.netmanage.activity;

import com.monster.netmanage.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import mst.preference.Preference;
import mst.preference.Preference.OnPreferenceClickListener;
import mst.preference.PreferenceScreen;
import mst.widget.toolbar.Toolbar;

/**
 * 流量设置界面
 * 
 * @author zhaolaichao
 */
public class DataSetActivity extends BasePreferenceActivity implements OnPreferenceClickListener {

	private PreferenceScreen mPreSim1;
	private PreferenceScreen mPreSim2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.activity_data_set);
		initView();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void addPreferencesFromResource(int preferencesResId) {
		super.addPreferencesFromResource(preferencesResId);
	}
	
	@SuppressWarnings("deprecation")
	private void initView() {
		Toolbar toolbar = getToolbar();
		toolbar.setTitle(getString(R.string.data_set));
		toolbar.setElevation(1);
		toolbar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DataSetActivity.this.finish();
			}
		});
		mPreSim1 = (PreferenceScreen) findPreference("preference_sim1");
		mPreSim1.setOnPreferenceClickListener(this);
		mPreSim2 = (PreferenceScreen) findPreference("preference_sim2");
		mPreSim2.setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String simTitle = null;
		Intent simIntent = null;
		 if (preference == mPreSim1) {
			   simTitle = getString(R.string.sim1_set);
				// 获得当前选择的sim的imsi号
				simIntent = new Intent(DataSetActivity.this, SimDataSetActivity.class);
				simIntent.putExtra("SIM_TITLE", simTitle);
				simIntent.putExtra("CURRENT_INDEX", 0);
				startActivity(simIntent);
	      } else if (preference == mPreSim2) {
	    	  simTitle = getString(R.string.sim2_set);
				// 获得当前选择的sim的imsi号
			  simIntent = new Intent(DataSetActivity.this, SimDataSetActivity.class);
			  simIntent.putExtra("SIM_TITLE", simTitle);
			  simIntent.putExtra("CURRENT_INDEX", 1);
			  startActivity(simIntent);
	      }
		return false;
	}
}
