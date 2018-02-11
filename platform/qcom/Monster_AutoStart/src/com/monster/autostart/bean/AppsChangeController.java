package com.monster.autostart.bean;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.monster.autostart.interfaces.IAppsChangeCallBack;
import com.monster.autostart.utils.Utilities;

public class AppsChangeController {

	Context sContext;

	private List<IAppsChangeCallBack> mCallbacks = new ArrayList<IAppsChangeCallBack>();

	private PackageMonitor mPackageMonitor;

	static final boolean USE_DYNAMIC_REGISTER_BROCASTRECEIVER = false;
	
	public AppsChangeController(Context cx) {
		sContext = cx;
		mPackageMonitor = new PackageMonitor();
	}

	public void addOnAppsChangedCallback(IAppsChangeCallBack cb) {
		if (cb != null && !mCallbacks.contains(cb)) {
			mCallbacks.add(cb);
			if(USE_DYNAMIC_REGISTER_BROCASTRECEIVER){
				if (mCallbacks.size() == 1) {
					registerForPackageIntents();
				}
			}

		}
	}

	public synchronized void removeOnAppsChangedCallback(
			IAppsChangeCallBack callback) {
		mCallbacks.remove(callback);
		if(USE_DYNAMIC_REGISTER_BROCASTRECEIVER){
			if (mCallbacks.size() == 0) {
				unregisterForPackageIntents();
			}
		}

	}

	private void unregisterForPackageIntents() {
		sContext.unregisterReceiver(mPackageMonitor);
	}

	private void registerForPackageIntents() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		filter.addDataScheme("package");
		sContext.registerReceiver(mPackageMonitor, filter);
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
		filter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
		sContext.registerReceiver(mPackageMonitor, filter);
	}

	public List<IAppsChangeCallBack> getCallbacks() {
		return mCallbacks;
	}

	class PackageMonitor extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			final String packageName = intent.getData().getSchemeSpecificPart();
			final String action = intent.getAction();
			final boolean replacing = intent.getBooleanExtra(
					Intent.EXTRA_REPLACING, false);
			if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
					|| Intent.ACTION_PACKAGE_REMOVED.equals(action)
					|| Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
					for (IAppsChangeCallBack callback : getCallbacks()) {
						callback.onPackageChanged(packageName);
					}
				} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
					if (!replacing) {
						for (IAppsChangeCallBack callback : getCallbacks()) {
							callback.onPackageRemoved(packageName);
						}
					}
					// else, we are replacing the package, so a PACKAGE_ADDED
					// will be sent
					// later, we will update the package at this time
				} else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
					if (!replacing) {
						for (IAppsChangeCallBack callback : getCallbacks()) {
							callback.onPackageAdded(packageName);
						}
					} else {
						for (IAppsChangeCallBack callback : getCallbacks()) {
							callback.onPackageChanged(packageName);
						}
					}
				}
			}
		}
	}
}
