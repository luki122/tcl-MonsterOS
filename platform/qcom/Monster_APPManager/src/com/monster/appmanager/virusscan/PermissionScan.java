package com.monster.appmanager.virusscan;

import com.monster.appmanager.utils.PermissionApps;
import com.monster.appmanager.utils.PermissionApps.Callback;
import com.monster.appmanager.utils.Utils;
import com.monster.appmanager.widget.PermissionsSelectPreference;

import android.Manifest;
import android.content.Context;
import android.util.ArraySet;

public class PermissionScan implements Callback{
    //private PermissionApps mAlertWindowPermissions;
    //private PermissionApps mShortcutPermissions;
    private int mEnabledAlertWindowCounts = -1;
    private int mEnabledShortcutCounts = -1;
    private OnPermissionScanListener listener;
    private ArraySet<String> mLauncherPkgs;
    
    public void scan(Context context, OnPermissionScanListener listener) {
    	this.listener = listener;
    	//mAlertWindowPermissions = new PermissionApps(context, Manifest.permission_group.SYSTEM_ALERT_WINDOW, this);
    	//mShortcutPermissions = new PermissionApps(context, Manifest.permission_group.INSTALL_SHORTCUT, this);
    	mLauncherPkgs = Utils.getLauncherPackages(context);
    	//mAlertWindowPermissions.refresh(true);
    	//mShortcutPermissions.refresh(true);
    }

	@Override
	public void onPermissionsLoaded(PermissionApps permissionApps) {
		updateCount(permissionApps);
		if(mEnabledAlertWindowCounts != -1 && mEnabledShortcutCounts != -1) {
			if(listener != null) {
				listener.onPermissionScanResult(mEnabledAlertWindowCounts, mEnabledShortcutCounts);
			}
		}
	}
	
	private void updateCount(PermissionApps permissionApps) {
		int[] indexCount;
		int enabledCount; 
		indexCount = permissionApps.getAllTypeCount(mLauncherPkgs);
		enabledCount = indexCount[PermissionsSelectPreference.OPEN];
		enabledCount += indexCount[PermissionsSelectPreference.ASK];
		/*if(permissionApps == mAlertWindowPermissions) {
			mEnabledAlertWindowCounts = enabledCount;
		} else {
			mEnabledShortcutCounts = enabledCount;
		}*/
	}
	
	public static interface OnPermissionScanListener{
		public void onPermissionScanResult(int enabledAlertWindowCounts, int enabledShortcutCounts);
	}
}
