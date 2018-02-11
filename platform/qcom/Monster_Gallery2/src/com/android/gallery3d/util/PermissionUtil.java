/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* ----------|----------------------|----------------------|------------------------------------------------------------------*/
/* 11/06/2015|chengbin.du-nb        |ALM-871912            |[Android6.0][Gallery_v5.2.3.1.0310.0]update sdkTargetVersion to 23*/
/* ----------|----------------------|----------------------|------------------------------------------------------------------*/
/* 11/17/2015|chengbin.du-nb        |ALM-913700            |[DRM] Gallery force closed happen when open gif DRM file*/
/* ----------|----------------------|----------------------|------------------------------------------------------------------*/
/* 11/23/2015|chengbin.du-nb        |ALM-975835            |[Android6.0][Gallery_v5.2.4.1.0315.0]Add permission flow*/
/* ----------|----------------------|----------------------|------------------------------------------------------------------*/
/* 12/10/2015|dongliang.feng        |ALM-1096821           |[Android 6.0][Gallery_v5.2.5.1.0319.0][Monitor]It pop up FC information when enter the guest mode */
/* ----------|----------------------|----------------------|------------------------------------------------------------------*/
/* 12/23/2015|chengbin.du-nb        |ALM-1162997           |[Gallery][App permission]Not Jump to Gallery permission screen when tap setting in gallery */
/* ----------|----------------------|----------------------|------------------------------------------------------------------*/

package com.android.gallery3d.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.AlbumPicker;
import com.android.gallery3d.app.BatchService;
import com.android.gallery3d.app.DialogPicker;
import com.android.gallery3d.app.Gallery;
import com.android.gallery3d.app.GalleryActivity;
import com.android.gallery3d.app.MovieActivity;
import com.android.gallery3d.app.PackagesMonitor;
import com.android.gallery3d.app.TrimVideo;
import com.android.gallery3d.app.Wallpaper;
import com.android.gallery3d.filtershow.FilterShowActivity;
import com.android.gallery3d.filtershow.crop.CropActivity;
import com.android.gallery3d.filtershow.pipeline.ProcessingService;
import com.android.gallery3d.gadget.WidgetClickHandler;
import com.android.gallery3d.gadget.WidgetConfigure;
import com.android.gallery3d.gadget.WidgetService;
import com.android.gallery3d.gadget.WidgetTypeChooser;
import com.android.gallery3d.ingest.IngestActivity;
import com.android.gallery3d.ingest.IngestService;
import com.android.gallery3d.settings.GallerySettings;

/**
 *  gallery permissions level

    dangerous <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" /> *
    dangerous <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" /> *
    dangerous <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /> *
    dangerous <uses-permission android:name="android.permission.READ_PHONE_STATE" />

    unknown   <uses-permission android:name="android.permission.MANAGE_ACCOUNTS" />
    unknown   <uses-permission android:name="android.permission.USE_CREDENTIALS" />
    unknown   <uses-permission android:name="com.android.gallery3d.permission.GALLERY_PROVIDER" />
    unknown   <uses-permission android:name="android.permission.WRITE_MEDIA_STORAGE" />
    unknown   <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" /> **

    signature <uses-permission android:name="android.permission.MANAGE_DOCUMENTS" />
    signature <uses-permission android:name="android.permission.WRITE_SETTINGS" /> **

    normal    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    normal    <uses-permission android:name="android.permission.INTERNET" />
    normal    <uses-permission android:name="android.permission.NFC" />
    normal    <uses-permission android:name="android.permission.READ_SYNC_SETTINGS" />
    normal    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    normal    <uses-permission android:name="android.permission.SET_WALLPAPER" />
    normal    <uses-permission android:name="android.permission.VIBRATE" />
    normal    <uses-permission android:name="android.permission.WAKE_LOCK" />
    normal    <uses-permission android:name="android.permission.WRITE_SYNC_SETTINGS" />
    normal    <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
    normal    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    normal    <uses-permission android:name="android.permission.BLUETOOTH" />
    normal    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    normal    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
 *
 */
public class PermissionUtil {

    private static final String TAG = "PermissionUtil";
    //Default result to {@link OnRequestPermissionsResultCallback#onRequestPermissionsResult(int, String[], int[])}.
    public static final int CHECK_REQUEST_PERMISSION_RESULT = 3;
    public static final String TCT_ACTION_MANAGE_APP = "android.intent.action.tct.MANAGE_PERMISSIONS";
    public static final String TCT_EXTRA_PACKAGE_NAME = "android.intent.extra.tct.PACKAGE_NAME";
    public static final int MATCH_ALL = 0x00020000;//the definition only in android 6.0 PackageManager class

    private static HashMap<String, String[]> PermissonMap = new HashMap<String, String[]>();
    private static HashMap<String, Integer> SignaturePermissonMap = new HashMap<String, Integer>();

    public static String com_tct_gallery3d_app_AbstractGalleryActivity = AbstractGalleryActivity.class.getName();
    public static String com_tct_gallery3d_app_AlbumPicker = AlbumPicker.class.getName();
    public static String com_tct_gallery3d_app_BatchService = BatchService.class.getName();
    public static String com_tct_gallery3d_app_DialogPicker = DialogPicker.class.getName();
    public static String com_tct_gallery3d_app_Gallery  = Gallery.class.getName();
    public static String com_tct_gallery3d_app_GalleryActivity = GalleryActivity.class.getName();
    public static String com_tct_gallery3d_app_MovieActivity = MovieActivity.class.getName();
    public static String com_tct_gallery3d_app_PackagesMonitor = PackagesMonitor.class.getName();
    public static String com_tct_gallery3d_app_PackagesMonitor$AsyncService = PackagesMonitor.AsyncService.class.getName();
    public static String com_tct_gallery3d_app_TrimVideo = TrimVideo.class.getName();
    public static String com_tct_gallery3d_app_Wallpaper = Wallpaper.class.getName();
    public static String com_tct_gallery3d_filtershow_crop_CropActivity = CropActivity.class.getName();
    public static String com_tct_gallery3d_filtershow_FilterShowActivity = FilterShowActivity.class.getName();
    public static String com_tct_gallery3d_filtershow_pipeline_ProcessingService = ProcessingService.class.getName();
    public static String com_tct_gallery3d_gadget_WidgetClickHandler = WidgetClickHandler.class.getName();
    public static String com_tct_gallery3d_gadget_WidgetConfigure = WidgetConfigure.class.getName();
    public static String com_tct_gallery3d_gadget_WidgetService  = WidgetService.class.getName();
    public static String com_tct_gallery3d_gadget_WidgetTypeChooser  = WidgetTypeChooser.class.getName();
    public static String com_tct_gallery3d_ingest_IngestActivity = IngestActivity.class.getName();
    public static String com_tct_gallery3d_ingest_IngestService  = IngestService.class.getName();
    public static String com_tct_gallery3d_settings_GallerySettings = GallerySettings.class.getName();

    static {
        PermissonMap.put(com_tct_gallery3d_app_GalleryActivity,  new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE } );

        PermissonMap.put(com_tct_gallery3d_app_AbstractGalleryActivity, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE } );

        PermissonMap.put(com_tct_gallery3d_app_MovieActivity, new String[]{""});

        //MODIFY-BEGIN by tao.wang, 2016-05-23, ALM-1899728
        PermissonMap.put(com_tct_gallery3d_app_Gallery, new String[] { "" });
        PermissonMap.put(com_tct_gallery3d_app_MovieActivity, new String[] { Manifest.permission.READ_PHONE_STATE });
        //MODIFY-END by tao.wang, 2016-05-23, ALM-1899728

        PermissonMap.put(com_tct_gallery3d_ingest_IngestActivity, new String[]{""});

        PermissonMap.put(com_tct_gallery3d_ingest_IngestService, new String[]{""});

        PermissonMap.put(com_tct_gallery3d_app_Wallpaper, new String[]{""});

        PermissonMap.put(com_tct_gallery3d_app_TrimVideo, new String[]{""});

        PermissonMap.put(com_tct_gallery3d_filtershow_pipeline_ProcessingService, new String[]{""});

        PermissonMap.put(com_tct_gallery3d_filtershow_FilterShowActivity, new String[]{""});

        PermissonMap.put(com_tct_gallery3d_filtershow_crop_CropActivity, new String[]{""});

        PermissonMap.put(com_tct_gallery3d_settings_GallerySettings, new String[]{""});

        PermissonMap.put(com_tct_gallery3d_gadget_WidgetClickHandler, new String[]{""});

        PermissonMap.put(com_tct_gallery3d_app_DialogPicker, new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE } );

        PermissonMap.put(com_tct_gallery3d_app_AlbumPicker, new String[]{""});

        PermissonMap.put(com_tct_gallery3d_gadget_WidgetTypeChooser, new String[]{""});

        PermissonMap.put(com_tct_gallery3d_app_PackagesMonitor, new String[]{""});

        PermissonMap.put(com_tct_gallery3d_app_PackagesMonitor$AsyncService, new String[]{""});

        PermissonMap.put(com_tct_gallery3d_gadget_WidgetService, new String[]{""});

        PermissonMap.put(com_tct_gallery3d_gadget_WidgetConfigure, new String[]{""});

        PermissonMap.put(com_tct_gallery3d_app_BatchService, new String[]{""});

    }

    public static boolean checkAndRequestPermissions(final @NonNull Activity activity,
                                          final @NonNull String[] permissions, final int requestCode){
        if(permissions != null && permissions.length > 0) {
            List<String> deniedPermissionsList = new ArrayList<String>();
            for(String perm : permissions) {
                if(PermissionChecker.checkSelfPermission(activity, perm) != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissionsList.add(perm);
                    Log.e(TAG, activity.getClass().getName() + " permission " + perm + " denied");
                }
            }
            if(deniedPermissionsList.size() > 0) {
                Log.d(TAG, "request denied permissions");
                ActivityCompat.requestPermissions(activity,
                        deniedPermissionsList.toArray(new String[deniedPermissionsList.size()]), requestCode);
                return true;
            }
        } else {
            Log.e(TAG, activity.getClass().getName() + " have no permissions");
        }
        return false;
    }

    public static boolean checkAndRequestPermissions(final @NonNull Activity activity,
            final @NonNull String className, final int requestCode) {
        String[] permissions = PermissonMap.get(className);
        return checkAndRequestPermissions(activity, permissions, requestCode);
    }

    public static boolean checkPermissions(final @NonNull Context context,
            final @NonNull String className) {
        String[] permissions = PermissonMap.get(className);
        int[] grantResult = checkPermissions(context, permissions);
        boolean checkResult = true;
        if(grantResult != null && grantResult.length > 0) {
            for(int i = 0; i < grantResult.length; i++) {
                if(grantResult[i] == PackageManager.PERMISSION_DENIED) {
                    checkResult = false;
                    break;
                }
            }
        } else {
            checkResult = false;
        }
        Log.d(TAG, "PermissionUtil.checkPermissions result = " + checkResult);
        return checkResult;
    }

    public static int[] checkPermissions(final @NonNull Context context,
            final @NonNull String[] permissions) {
        if(permissions != null && permissions.length > 0) {
            int[] grantResult = new int[permissions.length];
            for(int i = 0; i < permissions.length; i++) {
                grantResult[i] = PermissionChecker.checkSelfPermission(context, permissions[i]);
                Log.d(TAG, context.getClass().getName() + " permission " + permissions[i] + " is " + grantResult[i]);
            }
            return grantResult;
        } else {
            return new int[] { PackageManager.PERMISSION_GRANTED };
        }
    }

    public static int checkSignaturePermisson(final @NonNull Context context,
            final @NonNull String permission) {
      //[BUGFIX]-Mod-BEGIN by TCTNJ.wencan.wu,08/10/2016,Defect 2514132
        try {
            Integer grantResult = SignaturePermissonMap.get(permission);
            if (grantResult == null) {
                grantResult = PermissionChecker.checkSelfPermission(context, permission);
                SignaturePermissonMap.put(permission, grantResult);
            }
            return grantResult;
        } catch (SecurityException e) {
            Log.d(TAG, "checkSignaturePermisson SecurityException----- ");
            return PackageManager.PERMISSION_DENIED;
        }
      //[BUGFIX]-Mod-BEGIN by TCTNJ.wencan.wu,08/10/2016,Defect 2514132
    }

    public static boolean checkPermissionIntentAction(Context context) {
        if(Build.VERSION.SDK_INT >= 23) {
            final PackageManager packageManager = context.getPackageManager();
            final Intent intent = new Intent(TCT_ACTION_MANAGE_APP);
            List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, MATCH_ALL);
            return (resolveInfo.size() > 0);
        } else
            return false;
    }
}

