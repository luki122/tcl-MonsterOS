package com.monster.market.install;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.monster.market.bean.InstalledAppInfo;
import com.monster.market.db.InstalledAppDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class InstallAppManager {

	private static List<InstalledAppInfo> installedAppList;
	private static Map<String, InstalledAppInfo> installedAppMap;

	public static void initInstalledAppList(Context context) {
		installedAppList = new ArrayList<InstalledAppInfo>();
		installedAppMap = new HashMap<String, InstalledAppInfo>();
		InstalledAppDao appDao = new InstalledAppDao(context);
		appDao.openDatabase();
		int dbAppCount = appDao.getInstalledCount();
		if (dbAppCount > 0) {
			installedAppList = appDao.getInstalledAppList();
			for (InstalledAppInfo info : installedAppList) {
				installedAppMap.put(info.getPackageName(), info);
			}
		} else {
			PackageManager pm = context.getPackageManager();
			List<PackageInfo> list = pm.getInstalledPackages(0);
			for (PackageInfo pinfo : list) {
				addInstallAppInfo(pinfo, appDao, pm);
			}
		}
		appDao.closeDatabase();
	}

	public static List<InstalledAppInfo> getInstalledAppList(Context context) {
		if (installedAppList == null) {
			initInstalledAppList(context);
		}
		return installedAppList;
	}

	public static InstalledAppInfo getInstalledAppInfo(Context context,
			String packageName) {
		if (installedAppMap == null) {
			initInstalledAppList(context);
		}
		if (installedAppMap.containsKey(packageName)) {
			return installedAppMap.get(packageName);
		}
		return null;
	}

	public static void updateFileMd5(Context context, String packageName, String md5) {
		if (installedAppMap == null) {
			initInstalledAppList(context);
		}
		if (installedAppMap.containsKey(packageName)) {
			installedAppMap.get(packageName).setMd5(md5);
		}
	}

	public static void updateCerStrMd5(Context context, String packageName, String cerStrMd5) {
		if (installedAppMap == null) {
			initInstalledAppList(context);
		}
		if (installedAppMap.containsKey(packageName)) {
			installedAppMap.get(packageName).setCerStrMd5(cerStrMd5);
		}
	}

	public static void setInstalledAppList(
			List<InstalledAppInfo> installedAppList) {
		InstallAppManager.installedAppList = installedAppList;
	}

	public static void setInstalledAppMap(
			Map<String, InstalledAppInfo> installedAppMap) {
		InstallAppManager.installedAppMap = installedAppMap;
	}

	public static void clearAll() {
		if (installedAppList != null) {
			installedAppList.clear();
		}
		if (installedAppMap != null) {
			installedAppMap.clear();
		}
	}

	public static void addInstallAppInfo(PackageInfo pinfo,
			InstalledAppDao appDao, PackageManager pm) {
		InstalledAppInfo installedAppInfo = new InstalledAppInfo();
		ApplicationInfo appInfo = pinfo.applicationInfo;
//		boolean add = true;

		if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
			// 代表的是系统的应用
//			add = false;
			installedAppInfo.setAppFlag(InstalledAppInfo.FLAG_SYSTEM);
		} else if ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			// 代表的是系统的应用,但是被用户升级了. 用户应用
//			add = false;
			installedAppInfo.setAppFlag(InstalledAppInfo.FLAG_UPDATE);
		} else {
			// 代表的用户的应用
			installedAppInfo.setAppFlag(InstalledAppInfo.FLAG_USER);
		}

//		if (add) {
			installedAppInfo.setName(pinfo.applicationInfo.loadLabel(pm).toString());
			installedAppInfo.setIconId(appInfo.icon);
			installedAppInfo.setPackageName(appInfo.packageName);
			installedAppInfo.setVersionCode(pinfo.versionCode);
			installedAppInfo.setVersion(pinfo.versionName);
			installedAppInfo.setApkPath(appInfo.sourceDir);
			installedAppList.add(installedAppInfo);
			installedAppMap.put(appInfo.packageName, installedAppInfo);
			appDao.insert(installedAppInfo);
//		}
	}

}