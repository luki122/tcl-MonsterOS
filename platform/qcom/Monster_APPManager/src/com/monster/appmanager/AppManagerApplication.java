package com.monster.appmanager;

import java.util.HashMap;
import java.util.Map;
import tmsdk.common.ITMSApplicaionConfig;
import tmsdk.common.TMSDKContext;
import android.app.Application;
import android.util.Log;

public final class AppManagerApplication extends Application {
	public volatile static boolean mBresult = false;

	@Override
	public void onCreate() {
		super.onCreate();
		TMSDKContext.setTMSDKLogEnable(true);
		long start = System.currentTimeMillis();
		boolean nFlag = true;
		TMSDKContext.setAutoConnectionSwitch(nFlag);
		mBresult = TMSDKContext.init(this, TmsSecureService.class,
				new ITMSApplicaionConfig() {

					@Override
					public HashMap<String, String> config(
							Map<String, String> src) {
						HashMap<String, String> ret = new HashMap<String, String>(
								src);
						return ret;
					}

				});
		long end = System.currentTimeMillis();
		Log.v("demo", "TMSDK init spend =" + (end - start));
		Log.v("demo", "init result =" + mBresult);
	}
}
