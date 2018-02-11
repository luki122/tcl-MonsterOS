package cn.tcl.music.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.activities.CheckPermissonActivity;

public class PermissionsUtil {
    public static final String TAG = "PermissionsUtil";
    public static final String TCT_ACTION_MANAGE_APP = "android.intent.action.tct.MANAGE_PERMISSIONS";
    public static final String TCT_EXTRA_PACKAGE_NAME = "android.intent.extra.tct.PACKAGE_NAME";

    public static boolean shouldCheckPermissions() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    @TargetApi(23)
    public static String[] getRequestPermissions(Activity activity) {
        ArrayList<String> permissonList = new ArrayList<>();
        //SD card write permission
        int haspermission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (haspermission != PackageManager.PERMISSION_GRANTED) {
            permissonList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        //Phone status permissions
        haspermission = activity.checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
        if (haspermission != PackageManager.PERMISSION_GRANTED) {
            permissonList.add(Manifest.permission.READ_PHONE_STATE);
        }
        //Listen to songs need to know the song function
        haspermission = activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO);
        if (haspermission != PackageManager.PERMISSION_GRANTED) {
            permissonList.add(Manifest.permission.RECORD_AUDIO);
        }

        return permissonList.toArray(new String[permissonList.size()]);
    }

    @TargetApi(23)
    public static boolean checkSelfPermission(Context context, String permission) {
        return (PackageManager.PERMISSION_GRANTED == context.checkSelfPermission(permission));
    }

    @TargetApi(23)
    public static boolean shouldExplainPermissions(Activity activity, String[] requestPermissions) {
        for (int i = 0; i < requestPermissions.length; i++) {
            if (!activity.shouldShowRequestPermissionRationale(requestPermissions[i])) {
                return true;
            }
        }
        return false;
    }

    public static void gotoExplainActivity(Activity context, Class<?> cls) {
        Intent intent = new Intent(context, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivityForResult(intent, CheckPermissonActivity.CHECK_PERMISSION_REQUEST);
    }

    public static void gotoSettings(Activity context) {
        Intent intent;
        if (isIntentExisting(context, TCT_ACTION_MANAGE_APP)) {
            // Goto setting application permission
            intent = new Intent(TCT_ACTION_MANAGE_APP);
            intent.putExtra(TCT_EXTRA_PACKAGE_NAME, context.getPackageName());
        } else {
            // Goto settings details
            final Uri packageURI = Uri.parse("package:" + context.getPackageName());
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
        }
        context.startActivityForResult(intent, 0);
    }

    @TargetApi(23)
    public static boolean shouldRequestPermissions(Activity activity) {
        //SD card write permission
        int haspermission = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (haspermission != PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        //Phone status permissions
        haspermission = activity.checkSelfPermission(Manifest.permission.READ_PHONE_STATE);
        if (haspermission != PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        //Listen to songs need to know the song function
        haspermission = activity.checkSelfPermission(Manifest.permission.RECORD_AUDIO);
        if (haspermission != PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    @TargetApi(23)
    public static boolean isIntentExisting(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        if (resolveInfo.size() > 0) {
            return true;
        }
        Log.e(TAG, "intent not exist");
        return false;
    }
}
