package com.monster.autostart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.monster.autostart.bean.AppManagerState;
import com.monster.autostart.bean.BroadcastSolution;
import com.monster.autostart.interfaces.IBaseSolution;
import com.monster.autostart.utils.Utilities;

import android.app.Application;
import android.util.Log;

public final class AppManagerApplication extends Application {

	AppManagerState mState;
	 
	@Override
	public void onCreate() {
		Log.e(Utilities.TAG, "_CLS_:"+"AppManagerApplication"+";"+"_FUNCTION_:"+"onCreate");  
		
		AppManagerState.setApplicationContext(this);
		mState = AppManagerState.getInstance();
		super.onCreate();
		
	}
	
}
