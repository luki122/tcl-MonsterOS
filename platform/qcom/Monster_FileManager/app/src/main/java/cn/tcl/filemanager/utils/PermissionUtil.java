/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.filemanager.utils;

import android.annotation.NonNull;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;


public class PermissionUtil {

    // Default result to {@link
    // OnRequestPermissionsResultCallback#onRequestPermissionsResult(int,
    // String[], int[])}.
    public static final int CHECK_REQUEST_PERMISSION_RESULT = 3;
    protected static PopupWindow permissionPop;
    private final static String CHECK_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static boolean isSecondRequest = false;
    public final static int JUMPTOSETTINGFORSTORAGE = 0X11;
    public static boolean isShowPermissionDialog = false;

    /*
     * Add PermissionLifecycleCallbacks on application.oncreate to check
     * permissions.
     */
    @Deprecated
    public static Application.ActivityLifecycleCallbacks getActivityLifecycleCallbacks(
            String[] permissions) {
        return new PermissionLifecycleCallbacks(permissions);
    }

    /*
     * @param activity The target activity.
     * @param permissions The requested permissions.
     * @param requestCode Application specific request code to match with a
     * result reported to {@link
     * OnRequestPermissionsResultCallback#onRequestPermissionsResult( int,
     * String[], int[])}.
     */
    public static void checkAndRequestPermissions(final @NonNull
    Activity activity,
            final @NonNull
            String[] permissions, final int requestCode) {
        List<String> requestList = new ArrayList<String>();
        for (String perm : permissions) {
            Log.d("PER", "this is check permission" + perm);
            if (true){//activity.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
                requestList.add(perm);
            }
        }

        if (requestList.size() > 0) {
            isShowPermissionDialog = true;
            Log.d("PER", "this is check permission" + requestList.size());
            //activity.requestPermissions(
                   // requestList.toArray(new String[requestList.size()]), requestCode);
        }
        isSecondRequest = true;
    }

    /*
     * Check all permissions when resume the activity.
     */
    @Deprecated
    static class PermissionLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

        String[] permissions;

        public PermissionLifecycleCallbacks(@NonNull
        String[] permissions) {
            this.permissions = permissions;
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            checkAndRequestPermissions(activity, permissions, CHECK_REQUEST_PERMISSION_RESULT);
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }

    public static boolean isAllowPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity.getBaseContext(), CHECK_PERMISSION) != PackageManager.PERMISSION_GRANTED;
    }


    public static boolean isSecondRequestPermission(Context context) {
        SharedPreferences sp = context.getSharedPreferences(
                "firstTimeEnterApp", Context.MODE_PRIVATE);
        return sp.getBoolean("secondrequestpermission", false);
    }

    public static void setSecondRequestPermission(Context context) {
        SharedPreferences sp = context.getSharedPreferences("firstTimeEnterApp",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("secondrequestpermission", true);
        editor.commit();
    }
}
