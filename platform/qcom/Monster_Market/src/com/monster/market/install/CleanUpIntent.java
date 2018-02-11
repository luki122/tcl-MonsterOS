package com.monster.market.install;

import com.monster.market.utils.ApkUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CleanUpIntent extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		String action = intent.getAction();

		if (action.equals("notification_installed_cancelled")) {

			InstallNotification.cancelInstalledNotify();

		} else if (action.equals("notification_installed_one")) {

			InstallNotification.cancelInstalledNotify();
			String packageName = intent.getStringExtra("pkgName");
			ApkUtil.openApp(context, packageName);

		} else if (action.equals("notification_failed_cancelled")) {

			InstallNotification.cancelInstallFailedNotify();

		} else if (action.equals("notification_update_installed_cancelled")) {

			InstallNotification.cancelUpdateInstalledNotify();

		} else if (action.equals("notification_update_installed_one")) {

			InstallNotification.cancelUpdateInstalledNotify();
			String packageName = intent.getStringExtra("pkgName");
			ApkUtil.openApp(context, packageName);

		} else if (action.equals("notification_update_failed_cancelled")) {

			InstallNotification.cancelUpdateInstallFailedNotify();

		}

	}

}
