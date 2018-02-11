package com.monster.market.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import com.monster.market.MarketApplication;
import com.monster.market.bean.InstalledAppInfo;
import com.monster.market.db.IgnoreAppDao;
import com.monster.market.db.InstalledAppDao;
import com.monster.market.download.AppDownloadService;
import com.monster.market.http.DataResponse;
import com.monster.market.http.RequestError;
import com.monster.market.http.RequestHelper;
import com.monster.market.http.data.AppUpgradeInfoRequestData;
import com.monster.market.http.data.AppUpgradeListResultData;
import com.monster.market.install.InstallAppManager;

public class ApkUtil {

	public static final String TAG = "ApkUtil";

	/**
	 * 安装APK
	 * 
	 * @param context
	 * @param file
	 */
	public static void installApp(Context context, File file) {
		// execMethod(ctx,file.getAbsolutePath());
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri data = Uri.fromFile(file);
		String type = "application/vnd.android.package-archive";
		intent.setDataAndType(data, type);
		context.startActivity(intent);
	}

	/**
	 * 根据包名打开软件
	 * 
	 * @param context
	 * @param packagename
	 */
	public static void openApp(Context context, String packagename) {
		Intent intent;
		PackageManager packageManager = context.getPackageManager();
		try {
			intent = packageManager.getLaunchIntentForPackage(packagename);
			if (intent != null) {
				context.startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<AppUpgradeInfoRequestData> getUpgradeList(Context context) {
		List<AppUpgradeInfoRequestData> instApps = new ArrayList<AppUpgradeInfoRequestData>();
		List<InstalledAppInfo> appInfoList = InstallAppManager.getInstalledAppList(context);

		IgnoreAppDao ignoreAppDao = new IgnoreAppDao(context);
		ignoreAppDao.open();
		ArrayList<String> packs = ignoreAppDao.queryAllPackageData();
		ignoreAppDao.close();

		for (InstalledAppInfo appInfo : appInfoList) {
			if (appInfo.getAppFlag() == InstalledAppInfo.FLAG_USER) {
				AppUpgradeInfoRequestData tmp = new AppUpgradeInfoRequestData();
				String packageName = appInfo.getPackageName();

				if ((null != packs) && (packs.indexOf(packageName) != -1)) {
					continue;
				}

				tmp.setPackageName(packageName);
				tmp.setVersionCode(appInfo.getVersionCode());
				tmp.setVersionName(appInfo.getVersion());
				if (TextUtils.isEmpty(appInfo.getMd5())) {
					String md5 = SystemUtil.getApkMD5(context, packageName);
					tmp.setMd5(md5);
					InstallAppManager.updateFileMd5(context, packageName, md5);
				} else {
					tmp.setMd5(appInfo.getMd5());
				}
				if (TextUtils.isEmpty(appInfo.getCerStrMd5())) {
					String cerStrMd5 = SystemUtil.getInstallPackageSignature(context, packageName);
					tmp.setCerStrMd5(cerStrMd5);
					InstallAppManager.updateCerStrMd5(context, packageName, cerStrMd5);
				} else {
					tmp.setCerStrMd5(appInfo.getCerStrMd5());
				}
				instApps.add(tmp);
			}
		}
		return instApps;
	}

	public static void checkAndSetCerMd5Asyn(final Context context) {
		new Thread() {
			@Override
			public void run() {
				List<InstalledAppInfo> appInfoList = InstallAppManager.getInstalledAppList(context);
				InstalledAppDao dao = new InstalledAppDao(context);
				dao.openDatabase();

				for (InstalledAppInfo appInfo : appInfoList) {
					if (TextUtils.isEmpty(appInfo.getMd5())) {
						String md5 = SystemUtil.getApkMD5(context, appInfo.getPackageName());
						dao.updateInstalledAppMd5(appInfo.getPackageName(), md5);
						InstallAppManager.updateFileMd5(context, appInfo.getPackageName(), md5);
					}
					if (TextUtils.isEmpty(appInfo.getCerStrMd5())) {
						String cerStrMd5 = SystemUtil.getInstallPackageSignature(context, appInfo.getPackageName());
						dao.updateInstalledAppCerStrMd5(appInfo.getPackageName(), cerStrMd5);
						InstallAppManager.updateCerStrMd5(context, appInfo.getPackageName(), cerStrMd5);
					}
				}

				dao.closeDatabase();
			}
		}.start();
	}

	/**
	 * 检测可更新数量
	 * @param context
	 */
	public static void checkUpdateApp(final Context context) {
		LogUtil.i(TAG, "checkUpdateApp call!");
		new Thread() {
			@Override
			public void run() {
				long start = System.currentTimeMillis();
				final List<AppUpgradeInfoRequestData> infoList = ApkUtil.getUpgradeList(context);
				long end = System.currentTimeMillis();
				LogUtil.i(TAG, "getUpgradeList time: " + (end - start));

				RequestHelper.getAppUpdateList(context, infoList,
						new DataResponse<AppUpgradeListResultData>() {
							@Override
							public void onResponse(AppUpgradeListResultData value) {

								if (value.getAppList() != null) {
									SettingUtil.setLastUpdateAppCount(context,
											value.getAppList().size());

									AppDownloadService.updateDownloadProgress();

									MarketApplication.appUpgradeNeedCheck = false;
								}
							}

							@Override
							public void onErrorResponse(RequestError error) {
								LogUtil.i(TAG, "checkUpdateApp error.");
							}
						});
			}
		}.start();
	}

}
