package com.monster.autostart.receiver;

import com.monster.autostart.bean.AppManagerState;
import com.monster.autostart.bean.AppsChangeController;
import com.monster.autostart.interfaces.IAppsChangeCallBack;
import com.monster.autostart.utils.Utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

public class AppsChangeReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		AppManagerState mState = AppManagerState.getInstance();
		
		AppsChangeController sController = mState.getController();
		
		
		final String packageName = intent.getData().getSchemeSpecificPart();
		Log.e(Utilities.TAG, "_CLS_:"+"PackageChangeReceiver"+";"+"_FUNCTION_:"+"onReceive"+";"+"act="+intent.getAction()+";"+"packageName="+packageName);  

		final String action = intent.getAction();
		final boolean replacing = intent.getBooleanExtra(
				Intent.EXTRA_REPLACING, false);
		if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
				|| Intent.ACTION_PACKAGE_REMOVED.equals(action)
				|| Intent.ACTION_PACKAGE_ADDED.equals(action)) {
			if (Intent.ACTION_PACKAGE_CHANGED.equals(action)) {
				for (IAppsChangeCallBack callback : sController.getCallbacks()) {
					callback.onPackageChanged(packageName);
				}
			} else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
				if (!replacing) {
					for (IAppsChangeCallBack callback : sController.getCallbacks()) {
						callback.onPackageRemoved(packageName);
					}
				}
				// else, we are replacing the package, so a PACKAGE_ADDED
				// will be sent
				// later, we will update the package at this time
			} else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				if (!replacing) {
					for (IAppsChangeCallBack callback : sController.getCallbacks()) {
						callback.onPackageAdded(packageName);
					}
				} else {
					for (IAppsChangeCallBack callback : sController.getCallbacks()) {
						callback.onPackageChanged(packageName);
					}
				}
			}
		}
	}

}
