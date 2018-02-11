package com.android.camera.permission;

import android.Manifest;

/**
 * Created by Sean Scott on 8/31/16.
 */
public class PermsInfo {

    public static final boolean DEBUG = true;

    // Permissions
    public static final String PERMS_CAMERA = Manifest.permission.CAMERA;
    public static final String PERMS_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String PERMS_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String PERMS_RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;
    public static final String PERMS_ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String PERMS_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;

    // Request codes used when requestPermissions.
    public static final int REQUEST_CAMERA = 1;
    public static final int REQUEST_EXTERNAL_STORAGE = 1<<1;
    public static final int REQUEST_MICROPHONE = 1<<2;
    public static final int REQUEST_LOCATION = 1<<3;

    // The requestCode used in startActivityForResult, it can be any number >= 0
    // except CameraUtil.SETGPS(1000) or FyuseRequest.START_CAMERA_MODE(3).
    public static final int ACTIVITY_REQUEST_CODE = 10;

    // The intent extra tag.
    public static final String TAG_REQUEST_CODE = "request_code";
    public static final String TAG_REQUEST_RESULT = "request_result";
    public static final String TAG_RATIONALIZE = "rationalize";

    // The request result(not resultCode).
    public static final int RESULT_GRANTED = 100;
    public static final int RESULT_LOCATION_DENIED = 101;
    public static final int RESULT_CRITICAL_DENIED = 102;

    public static final String PERMS_REQUEST_PACKAGE_NAME = "com.google.android.packageinstaller";
    public static final String PERMS_REQUEST_CLASS_NAME =
            "com.android.packageinstaller.permission.ui.GrantPermissionsActivity";
}