package com.monster.netmanage.activity;

import com.monster.netmanage.utils.ToolsUtil;

import android.os.Bundle;
import mst.preference.PreferenceActivity;

/**
 * 
 * @author zhaolaichao
 *
 */
public abstract class BasePreferenceActivity extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
	}
	
    @Override
    protected void onResume() {
    	ToolsUtil.registerHomeKeyReceiver(this);
    	super.onResume();
    }
    
	@Override
	protected void onPause() {
		ToolsUtil.unregisterHomeKeyReceiver(this);
		super.onPause();
	}
	
}
