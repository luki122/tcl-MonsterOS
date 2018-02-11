package com.mst.tms;

import java.io.File;

import com.monster.appmanager.utils.AppPermissionGroup;
import com.monster.appmanager.utils.AppPermissions;

import tmsdk.common.TMSBootReceiver;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

/**
 * 开机事件监听
 * @author boyliang
 */
public final class TmsBootReceiver extends TMSBootReceiver {

	private static final String TAG = "TmsBootReceiver";
	private static final String StoragePermissionName="android.permission-group.STORAGE";

	@Override
	public void doOnRecv(final Context context, Intent intent) {
		super.doOnRecv(context, intent);
		Log.d(TAG,"doOnRecv,intent:"+intent+" action:"+intent.getAction());

		//add by liyang  为"全能名片王"添加存储空间权限 name:android.permission-group.STORAGE label:存储空间
		if(TextUtils.equals(intent.getAction(), "com.android.contacts.MST_GRANT_BUSINESS_CARD_PERMISSION")){
			PackageInfo packageInfo = getPackageInfo(context, "com.intsig.BizCardReader");
			AppPermissions mAppPermissions = new AppPermissions(context, packageInfo, null, true, null);
			for (AppPermissionGroup group : mAppPermissions.getPermissionGroups()) {
				Log.d(TAG,"group:"+group+" name:"+group.getName()+" label:"+group.getLabel()+" permissions:"+group.getPermissions());
				if(TextUtils.equals(group.getName(), StoragePermissionName)){
					boolean hasPermission=group.areRuntimePermissionsGranted();
					Log.d(TAG,"hasPermission:"+hasPermission);
					if(!hasPermission){
						group.grantRuntimePermissions(false);
					}

					
					File file =new File(Environment.getExternalStorageDirectory()+"/bcr/imgs/");
					//如果bcr/imgs文件夹不存在则创建
					if  (!file.exists()  && !file.isDirectory()) {
						Log.d(TAG,"mkdirs");
						file .mkdirs();    
					}
					break;
				}
			}
		}
	}

	private PackageInfo getPackageInfo(Context context, String packageName) {
		try {
			return context.getPackageManager().getPackageInfo(
					packageName, PackageManager.GET_PERMISSIONS);
		} catch (PackageManager.NameNotFoundException e) {
			Log.i(TAG, "No package", e);
			return null;
		}
	}

}
