package com.android.camera.permission;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.view.View;
import android.widget.TextView;

import com.android.camera.debug.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sean Scott on 8/31/16.
 */
public class PermissionUtil {

    private static final Log.Tag TAG = new Log.Tag("PermissionUtil");

    public static boolean isPermissionGranted(Context context, String permission) {
        int status = PermissionChecker.checkSelfPermission(context, permission);
        if (PermsInfo.DEBUG) {
            Log.i(TAG, "Permission " + permission + ", checkSelfPermission " + status);
        }
        return (status == PermissionChecker.PERMISSION_GRANTED);
    }

    // This is not used now.
    public static boolean isExplanationNeeded(Activity activity, String permission) {
        boolean need = ActivityCompat.shouldShowRequestPermissionRationale(
                activity, permission);
        if (PermsInfo.DEBUG) {
            Log.i(TAG, "Permission " + permission + ", shouldShowRequestPermissionRationale " + need);
        }
        return need;
    }

    public static void requestPermissions(Activity activity, int requestCode,
                                          String ... permissions) {
        if (PermsInfo.DEBUG) {
            for (String permission : permissions) {
                Log.i(TAG, "requestCode:" + "requestCode, request " + permission);
            }
        }
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public static int getRequestCode(Context context, boolean criticalLimited) {
        int code = 0;

        if (!isPermissionGranted(context, PermsInfo.PERMS_CAMERA)) {
            code += PermsInfo.REQUEST_CAMERA;
        }

        if (!isPermissionGranted(context, PermsInfo.PERMS_READ_EXTERNAL_STORAGE) ||
                !isPermissionGranted(context, PermsInfo.PERMS_WRITE_EXTERNAL_STORAGE) ) {
            code += PermsInfo.REQUEST_EXTERNAL_STORAGE;
        }

        if (!isPermissionGranted(context, PermsInfo.PERMS_RECORD_AUDIO)) {
            code += PermsInfo.REQUEST_MICROPHONE;
        }

        if (!criticalLimited &&
                (!isPermissionGranted(context, PermsInfo.PERMS_ACCESS_COARSE_LOCATION) ||
                        !isPermissionGranted(context, PermsInfo.PERMS_ACCESS_FINE_LOCATION))) {
            code += PermsInfo.REQUEST_LOCATION;
        }

        return code;
    }

    public static String[] getRequestPermissions(int code, boolean criticalLimited) {
        boolean cameraGranted = ((code & PermsInfo.REQUEST_CAMERA) == 0);
        boolean storageGranted = ((code & PermsInfo.REQUEST_EXTERNAL_STORAGE) == 0);
        boolean microphoneGranted = ((code & PermsInfo.REQUEST_MICROPHONE) == 0);
        boolean locationGranted = ((code & PermsInfo.REQUEST_LOCATION) == 0);

        ArrayList<String> perms = new ArrayList<>();
        if (!cameraGranted) {
            perms.add(PermsInfo.PERMS_CAMERA);
        }
        if (!storageGranted) {
            perms.add(PermsInfo.PERMS_READ_EXTERNAL_STORAGE);
            perms.add(PermsInfo.PERMS_WRITE_EXTERNAL_STORAGE);
        }
        if (!microphoneGranted) {
            perms.add(PermsInfo.PERMS_RECORD_AUDIO);
        }
        if (!criticalLimited && !locationGranted) {
            perms.add(PermsInfo.PERMS_ACCESS_COARSE_LOCATION);
            perms.add(PermsInfo.PERMS_ACCESS_FINE_LOCATION);
        }

        return perms.toArray(new String[perms.size()]);
    }

    public static boolean isCriticalPermissionGranted(Context context) {
        int code = getRequestCode(context, true);
        return !(code > 0);
    }

    public static boolean isCriticalPermissionGranted(int code) {
        boolean cameraGranted = ((code & PermsInfo.REQUEST_CAMERA) == 0);
        boolean storageGranted = ((code & PermsInfo.REQUEST_EXTERNAL_STORAGE) == 0);
        boolean microphoneGranted = ((code & PermsInfo.REQUEST_MICROPHONE) == 0);

        return (cameraGranted && storageGranted && microphoneGranted);
    }

    public static boolean isNoncriticalPermissionGranted(Context context) {
        return isPermissionGranted(context, PermsInfo.PERMS_ACCESS_COARSE_LOCATION) &&
                isPermissionGranted(context, PermsInfo.PERMS_ACCESS_FINE_LOCATION);
    }

    public static boolean isNoncriticalPermissionGranted(int code) {
        return ((code & PermsInfo.REQUEST_LOCATION) == 0);
    }

    public static boolean isStoragePermissionGranted(Context context) {
        return isPermissionGranted(context, PermsInfo.PERMS_READ_EXTERNAL_STORAGE) &&
                isPermissionGranted(context, PermsInfo.PERMS_WRITE_EXTERNAL_STORAGE);
    }

    public static boolean isPermissionRequestWindow(ComponentName name) {
        if (name == null) {
            return false;
        }
        return PermsInfo.PERMS_REQUEST_PACKAGE_NAME.equals(name.getPackageName()) &&
                PermsInfo.PERMS_REQUEST_CLASS_NAME.equals(name.getClassName());
    }

    public static void showSnackBar(final Activity activity, View view, int textString, int actionString) {
        Snackbar bar = Snackbar.make(view, textString, Snackbar.LENGTH_LONG);
        Snackbar.SnackbarLayout sl = (Snackbar.SnackbarLayout) bar.getView();
        TextView textView = (TextView) sl.findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(3);
        bar.setAction(actionString, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoSettings(activity);
            }
        });
        bar.show();
    }

    // Goto Settings
    public static final String TCT_ACTION_MANAGE_APP = "android.intent.action.tct.MANAGE_PERMISSIONS";
    public static final String TCT_EXTRA_PACKAGE_NAME = "android.intent.extra.tct.PACKAGE_NAME";

    public static void gotoSettings(Context context){
        Intent intent;
        if(isIntentExisting(context, TCT_ACTION_MANAGE_APP)){
            //Goto setting application permission
            intent = new Intent(TCT_ACTION_MANAGE_APP);
            intent.putExtra(TCT_EXTRA_PACKAGE_NAME, context.getPackageName());
        }else {
            //Goto settings details
            final Uri packageURI = Uri.parse("package:" + context.getPackageName());
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
        }

        context.startActivity(intent);
    }

    public static boolean isIntentExisting(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> resolveInfo =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_ALL);
        if (resolveInfo.size() > 0) {
            return true;
        }
        return false;
    }
}