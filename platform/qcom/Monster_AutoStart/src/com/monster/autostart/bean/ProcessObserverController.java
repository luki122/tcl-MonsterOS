package com.monster.autostart.bean;

import java.util.ArrayList;
import java.util.List;

import com.monster.autostart.db.MulwareProvider;
import com.monster.autostart.interfaces.IBaseSolution;
import com.monster.autostart.utils.Utilities;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.IProcessObserver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.util.Log;

public class ProcessObserverController {

	AppManagerState state;
	PackageManager pm;
	int record = 0;

	public ProcessObserverController() {
		// TODO Auto-generated constructor stub
		Log.e(Utilities.TAG, "_CLS_:" + "ProcessObserverController" + ";"
				+ "_FUNCTION_:" + "ProcessObserverController");
		state = AppManagerState.getInstance();
		pm = state.getContext().getPackageManager();
	}

	public void registerProcessObserver() {
		Log.e(Utilities.TAG, "_CLS_:" + "ProcessObserverController" + ";"
				+ "_FUNCTION_:" + "registerProcessObserver");
		try {
			ActivityManagerNative.getDefault().registerProcessObserver(
					mProcessObserver);
		} catch (Exception e) {
			Log.e(Utilities.TAG, "_CLS_:" + "ProcessObserverController" + ";"
					+ "_FUNCTION_:" + "registerProcessObserver"
					+ "#Exception#=" + e);
		}
	}

	public void unregisterProcessObserver() {
		try {
			ActivityManagerNative.getDefault().unregisterProcessObserver(
					mProcessObserver);
		} catch (Exception e) {
			Log.e(Utilities.TAG, "_CLS_:" + "ProcessObserverController" + ";"
					+ "_FUNCTION_:" + "unregisterProcessObserver"
					+ "#Exception#=" + e);
		}
	}

	public void onDestroy() {
		unregisterProcessObserver();
	}

	private IProcessObserver mProcessObserver = new IProcessObserver.Stub() {
		@Override
		public void onForegroundActivitiesChanged(int pid, int uid,
				boolean foregroundActivities) {

//			try {
//
//				String packageName = Utilities.getInstance().getPackageName(
//						state.getContext(), uid);
//
//				if (uid != record) {
//					ApplicationInfo mInfo = pm.getApplicationInfo(packageName,
//							0);
//					
//					String title = pm.getApplicationLabel(mInfo).toString();
//
//					List<AppInfo> list = findAppInfo(packageName);
//					if (list != null) {
//						for (AppInfo info : list) {
//							ComponentName cp = info.getIntent().getComponent();
//							if (foregroundActivities) {
//								Utilities
//										.getInstance()
//										.setComponentEnabledSetting(
//												pm,
//												cp,
//												PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
//								Log.e(Utilities.TAG, "_CLS_:"
//										+ "ProcessObserverController" + ";"
//										+ "_FUNCTION_:"
//										+ "onForegroundActivitiesChanged" + ":"
//										+ "App running ENABLE" + ";" + "title="
//										+ info.getTitle());
//							}
//						}
//						record = uid;
//					} else
//						Log.e(Utilities.TAG, "_CLS_:"
//								+ "ProcessObserverController" + ";"
//								+ "info is null");
//				}else{
//					Log.e(Utilities.TAG, "_CLS_:"
//							+ "ProcessObserverController" + ";"
//							+ "#No Change!");
//				}
//
////				Log.e(Utilities.TAG, "_CLS_:" + "ProcessObserverController"
////						+ ";" + "_FUNCTION_:" + "onForegroundActivitiesChanged"
////						+ ";" + "pid=" + pid + ";" + "uid=" + uid + ";"
////						+ "foregroundActivities=" + foregroundActivities + ";"
////						+ "packageName=" + packageName + ";" + "title=" + title
////						+ ";");
//
//			} catch (Exception e) {
//				// TODO: handle exception
//			}

		}

		/* @Override */
		public void onImportanceChanged(int pid, int uid, int importance) {
			Log.e(Utilities.TAG, "_CLS_:" + "ProcessObserverController" + ";"
					+ "_FUNCTION_:" + "onImportanceChanged" + ";" + "pid="
					+ pid + ";" + "uid=" + uid + ";" + "importance="
					+ importance);
		}

		@Override
		public void onProcessDied(int pid, int uid) {

//			try {
//				String packageName = Utilities.getInstance().getPackageName(
//						state.getContext(), uid);
//				ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
//
//				String title = pm.getApplicationLabel(info).toString();
//
//				List<AppInfo> list = findAppInfo(packageName);
//
//				if (list != null) {
//					for (AppInfo die : list) {
//						ComponentName cp = die.getIntent().getComponent();
//						Utilities
//								.getInstance()
//								.setComponentEnabledSetting(
//										pm,
//										cp,
//										PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
//						Log.e(Utilities.TAG, "_CLS_:"
//								+ "ProcessObserverController" + ";"
//								+ "_FUNCTION_:" + "#onProcessDied#" + ";"
//								+ "pid=" + pid + ";" + "uid=" + uid + ";"
//								+ "App running DISABLE" + "packageName="
//								+ packageName + ";" + "title=" + title + ";"
//								+ "#cp.packageName=" + cp.getPackageName()
//								+ ";" + "#name" + cp.getClassName());
//						record = uid;
//					}
//					/** M:Hazel just force stop this package and */
//					ActivityManager activityMgr = (ActivityManager) state
//							.getContext().getSystemService(
//									state.getContext().ACTIVITY_SERVICE);
//					activityMgr.forceStopPackage(packageName);
//				}
//			} catch (Exception e) {
//				// TODO: handle exception
//				Log.e(Utilities.TAG,
//						"###############onProcessDied###########Exception" + e);
//			}
			record = 0;
		}

		/* @Override */
		public void onProcessStateChanged(int pid, int uid, int procState)
				throws RemoteException {
			// Log.e(Utilities.TAG, "_CLS_:" + "BootReceiverServices" + ";"
			// + "_FUNCTION_:" + "onProcessStateChanged" + ";" + "pid="
			// + pid + ";" + "uid=" + uid + ";" + "procState=" + procState);
		}
	};

	/**
	 * M:Hazel need to change this and return array list app info to support
	 * muti broadcast
	 */
	List<AppInfo> findAppInfo(String packageName) {
		MulwareProvider p = state.getAppProvider();

		List<AppInfo> list = p.query(null, null, null);

		List<AppInfo> result = new ArrayList<AppInfo>();

		if (list.size() <= 0)
			return null;

		for (AppInfo info : list) {
			String pkg = info.intent.getComponent().getPackageName();
			if (pkg.equals(packageName)) {
				result.add(info);
			}
		}
		return result;
	}

}
