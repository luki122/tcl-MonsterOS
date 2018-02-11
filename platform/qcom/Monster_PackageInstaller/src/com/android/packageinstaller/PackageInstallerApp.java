package com.android.packageinstaller;

import java.util.HashMap;
import java.util.Map;

import tmsdk.common.ITMSApplicaionConfig;
import tmsdk.common.TMSDKContext;
import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.android.packageinstaller.adplugin.DemoSecureService;
import com.android.packageinstaller.adplugin.LauncherSettings;
import com.android.packageinstaller.adplugin.MulwareProvider.DatabaseHelper;

public class PackageInstallerApp extends Application {
	public volatile static boolean mBresult  = false;

	@Override
	public void onCreate() {
		super.onCreate();
		DatabaseHelper h = new DatabaseHelper(this);
		/**
		 * setTMSDKLogEnable（true）会把tmsdk的log打开，release时可以去掉这个接口调用。
		 */
		TMSDKContext.setTMSDKLogEnable(true);
		long start = System.currentTimeMillis();
		/**
		 * setAutoConnectionSwitch（）影响渠道号上报这个自动联网项是否运行。请不要一直设置为false，影响激活量和活跃量统计
		 */
		boolean nFlag = true;//这里厂商应该用自己保存的用户设置
		TMSDKContext.setAutoConnectionSwitch(nFlag);
		// TMSDK初始化
		mBresult   = TMSDKContext.init(this, DemoSecureService.class,  new ITMSApplicaionConfig() {

					@Override
					public HashMap<String, String> config(
							Map<String, String> src) {
						HashMap<String, String> ret = new HashMap<String, String>(src);
//						ret.put(TMSDKContext.TCP_SERVER_ADDRESS, "mazutest.3g.qq.com");
//						ret.put(TMSDKContext.CON_IS_TEST, "true");
//						ret.put(TMSDKContext.CON_HOST_URL, "http://wuptest.cs0309.3g.qq.com");
//						// 如厂商有自己服务器（如国外）中转需求，需配置自己服务器域名
//						// http 服务器
//						ret.put(TMSDKContext.CON_HOST_URL, "http://pmir.sec.miui.com");
//						// tcp 服务器
//						ret.put(TMSDKContext.TCP_SERVER_ADDRESS, "mazu.sec.miui.com");
//						ret.put(TMSDKContext.USE_IP_LIST, "false");
						return ret;
					}

				});
		long end = System.currentTimeMillis();
		Log.v("demo", "TMSDK init spend ="+(end-start));
		Log.v("demo", "init result =" + mBresult);

	}
}
