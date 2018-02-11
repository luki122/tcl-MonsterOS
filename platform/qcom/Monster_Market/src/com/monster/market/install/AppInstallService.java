package com.monster.market.install;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.monster.market.db.AppDownloadDao;
import com.monster.market.download.AppDownloadData;
import com.monster.market.download.AppDownloadService;
import com.monster.market.utils.LogUtil;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

public class AppInstallService extends Service {

	private static final String TAG = "AppInstallService";

	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_AUTO_UPDATE = 1;

	private static Context context; // 上下文对象

	private static Map<String, AppInstaller> installers; // 正在安装的任务
	public static final String DOWNLOAD_DATA = "download_data";
	public static final String TYPE = "type";

	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.i(TAG, "onCreate()");
		if (isNeedInitData()) {
			context = this;
			installers = new ConcurrentHashMap<String, AppInstaller>();
			InstallNotification.init(context);
			AppDownloadDao appDownloadDao = new AppDownloadDao(this);
			appDownloadDao.openDatabase();
			// Service被创建时, 先从数据库找到已有任务, 并放入到列表中
			List<AppDownloadData> appIds = appDownloadDao.getUninstallApp();
			appDownloadDao.closeDatabase();
			for (AppDownloadData downloadData : appIds) {
				LogUtil.i(TAG, "the uninstall id=" + downloadData.getTaskId());
				AppInstaller install = new AppInstaller(downloadData, context);
				installers.put(downloadData.getTaskId(), install);
				install(install);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 获取要操作的数据信息
		LogUtil.i(TAG, "onStartCommand");
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				AppDownloadData currentDownloadData = (AppDownloadData) bundle.get(DOWNLOAD_DATA);
				if (null == currentDownloadData) {
					LogUtil.i(TAG, "onStartCommand the downloaddata is null");
					return super.onStartCommand(intent, flags, startId);
				}
				if (installers.containsKey(currentDownloadData.getTaskId())) {
					AppInstaller currentInstall = installers.get(currentDownloadData.getTaskId());
					install(currentInstall);
				} else {
					AppInstaller install = new AppInstaller(currentDownloadData, this);
					installers.put(currentDownloadData.getTaskId(), install);
					install(install);
				}

			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	/**
	 * 开始安装请求方法
	 * 
	 * @param context
	 * @param appDownloadData
	 */
	public static void startInstall(Context context, AppDownloadData appDownloadData) {
		Intent startInstall = new Intent(context, AppInstallService.class);
		Bundle startDownloadBundle = new Bundle();
		startDownloadBundle.putParcelable(AppDownloadService.DOWNLOAD_DATA, appDownloadData);
		startInstall.putExtras(startDownloadBundle);
		context.startService(startInstall);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 安装
	 */
	private void install(AppInstaller installer) {
		// 开始安装
		installer.InstallApp();
	}

	/**
	 * 检查Service数据是否构建完成
	 * 
	 * @param context
	 */
	public static void checkInit(Context context) {
		if (isNeedInitData()) {
			Intent initIntent = new Intent(context, AppInstallService.class);
			context.startService(initIntent);
		}
	}

	public static Map<String, AppInstaller> getInstalls() {
		return installers;
	}

	/**
	 * 获取是否需要重新构建Service的数据(静态变量可能会被回收)
	 * 
	 * @return
	 */
	private static boolean isNeedInitData() {
		if (installers == null || context == null) {
			return true;
		}
		return false;
	}

}
