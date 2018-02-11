package com.monster.appmanager.virusscan;

import com.monster.appmanager.applications.ManageApplications;
import com.monster.appmanager.db.MulwareProvider.MulwareTable;
import com.monster.permission.ui.ManagePermissionsInfoActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.telecom.Log;

public class AppUnInstalledReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {

		if (ManageApplications.manageApplications != null){
			ManageApplications.manageApplications.rebuild();
		}

		if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) { // install
			String packageName = intent.getDataString();

			Log.i("homer", "安装了 :" + packageName);
		}

		if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) { // uninstall
			String packageName = intent.getDataString();
			context.getContentResolver().delete(MulwareTable.CONTENT_URI,MulwareTable.AD_PACKAGENAME+"=?", 
					new String[] {packageName});
			Log.i("homer", "卸载了 :" + packageName);
			if(ManagePermissionsInfoActivity.managePermissionsInfoActivity!=null){
				ManagePermissionsInfoActivity.managePermissionsInfoActivity.checkAndDestroy(packageName);
			}
		}
	}
}