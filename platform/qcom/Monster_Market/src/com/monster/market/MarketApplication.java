package com.monster.market;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.monster.market.constants.Constant;
import com.monster.market.constants.HttpConstant;
import com.monster.market.download.AppDownloadService;
import com.monster.market.imageloader.UnlimitedDiscCache;
import com.monster.market.install.InstallAppManager;
import com.monster.market.install.InstallNotification;
import com.monster.market.utils.ApkUtil;
import com.monster.market.utils.LogUtil;
import com.monster.market.utils.SettingUtil;
import com.monster.market.utils.SystemUtil;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MarketApplication extends Application {
	
	private static MarketApplication instance;
	
	private List<Activity> activityList;
	public static int screenWidth = 0;
	public static int screenHeight = 0;

	public static boolean appUpgradeNeedFresh = false;
	public static boolean appUpgradeNeedCheck = true;

	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.i("MarketApplication", "onCreate()");
		
		instance = this;
		
		if (activityList == null) {
			activityList = new ArrayList<Activity>();
		} else {
			activityList.clear();
		}

		// 设置当前的网络状态
		int netStatus = SystemUtil.getNetStatus(this);
		SharedPreferences sp = this.getSharedPreferences(Constant.SHARED_WIFI_UPDATE,
				Context.MODE_APPEND);
		final SharedPreferences.Editor ed = sp.edit();
		ed.putInt(Constant.SHARED_NETSTATUS_KEY, netStatus);
		ed.commit();

		loadTestUrl();
		InstallAppManager.initInstalledAppList(this);
		ApkUtil.checkAndSetCerMd5Asyn(this);
		initImageLoader(this);
		AppDownloadService.checkInit(this, null);
		InstallNotification.init(this);

		LogUtil.i("MarketApplication", "HttpConstant.HTTP_BASE:" + HttpConstant.HTTP_BASE);
	}

	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true).cacheOnDisk(true).build();
		File cacheDir = StorageUtils.getOwnCacheDirectory(context,
				"/TCLStore Download/Cache");
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).defaultDisplayImageOptions(defaultOptions)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.diskCache(new UnlimitedDiscCache(cacheDir))
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.diskCacheSize(100 * 1024 * 1024)
				.memoryCache(new LruMemoryCache(20 * 1024 * 1024))
				//.memoryCache(new WeakMemoryCache())
				.memoryCacheSize(20 * 1024 * 1024)
				// .writeDebugLogs() // Remove
				// for
				// release
				// app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);

		// 更新网络加载图片状态
		if (SettingUtil.isLoadingImage(context)) {
			ImageLoader.getInstance().denyNetworkDownloads(false);
		} else {
			ImageLoader.getInstance().denyNetworkDownloads(true);
		}
	}
	
	public static MarketApplication getInstance() {
		return instance;
	}
	
	public void addActivity(Activity activity) {
		if (activityList != null) {
			activityList.add(activity);
		}
	}
	
	public void removeActivity(Activity activity) {
		if (activityList != null) {
			activityList.remove(activity);
		}
	}
	
	public void exitApp() {
		if (activityList != null) {
			for (Activity activity : activityList) {
				if (activity != null && !activity.isFinishing()) {
					activity.finish();
				}
			}
			activityList.clear();
		}
	}

	public static String getResolutionStr() {
		return screenWidth + "x" + screenHeight;
	}

	/**
	 * 读取测试URL
	 */
	public void loadTestUrl() {
		File fl = new File(Environment.getExternalStorageDirectory()
				+ "/marketTest1234567890");
		if (fl.isDirectory()) {
			if (fl.listFiles().length == 1) {
				HttpConstant.HTTP_BASE = "http://" + fl.list()[0].toString();
			}
		}
	}

}
