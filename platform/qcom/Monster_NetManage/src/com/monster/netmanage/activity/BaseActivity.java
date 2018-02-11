package com.monster.netmanage.activity;

import com.monster.netmanage.receiver.SimStateReceiver;
import com.monster.netmanage.receiver.SimStateReceiver.ISimStateChangeListener;
import com.monster.netmanage.utils.ToolsUtil;

import android.os.Bundle;
import mst.app.MstActivity;

public abstract class BaseActivity extends MstActivity {
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setSimStateChange();
	}
	
    @Override
    protected void onResume() {
    	super.onResume();
    	ToolsUtil.registerHomeKeyReceiver(this);
    }
    
	@Override
	protected void onPause() {
		super.onPause();
		ToolsUtil.unregisterHomeKeyReceiver(this);
	}
	
	/**
	 * 监听sim状态变化
	 */
	private void setSimStateChange() {
		SimStateReceiver.setSimStateChangeListener(new ISimStateChangeListener() {

			@Override
			public void onSimStateChange() {
				// 监听sim状态变化, 可以用来更新UI
				setSimStateChangeListener();
			}
		});
	}

	/**
	 * 监听sim状态变化, 可以用来更新UI
	 */
	public abstract void setSimStateChangeListener();
}
