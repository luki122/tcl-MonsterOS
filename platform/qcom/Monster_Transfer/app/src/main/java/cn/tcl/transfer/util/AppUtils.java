/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.util;


import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;

public class AppUtils {

    public static boolean isAppInstalled(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        boolean installed =false;
        try {
            pm.getPackageInfo(uri,PackageManager.GET_ACTIVITIES);
            installed =true;
        } catch(PackageManager.NameNotFoundException e) {
            installed =false;
        }
        return installed;
    }

    public static boolean isPackageInstalled(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return false;
        }
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static PackageInfo getApkInfo(Context context, final String apkPath) {
        String archiveFilePath = apkPath;
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(archiveFilePath, 0);
        if(info != null){
            return info;
        }
        return null;
    }

    public static boolean checkNeedInstall(Context context, String apkPath) {
        int loc = apkPath.lastIndexOf("/");
        int loc1 = apkPath.lastIndexOf(".");

        String packageName = apkPath.substring(loc + 1, loc1);

        if(isPackageInstalled(context, packageName)) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
                PackageInfo apkPackageInfo = getApkInfo(context, apkPath);
                if(apkPackageInfo == null) {
                    return false;
                }
                if(packageInfo.versionCode < apkPackageInfo.versionCode) {
                    return true;
                }
                return false;

            } catch(PackageManager.NameNotFoundException e) {
                return true;
            }
        }
        return true;
    }
    public static void setDefaultSms(Context context, final String pkgName) {
        if(context == null || TextUtils.isEmpty(pkgName)) {
            return;
        }
        try {
            AppOpsManager appOpsManager = (AppOpsManager)context.getSystemService(Context.APP_OPS_SERVICE);
            appOpsManager.setMode(AppOpsManager.OP_WRITE_SMS,android.os.Process.myUid(),pkgName,AppOpsManager.MODE_ALLOWED);
        } catch (Exception e) {
            Log.e("AppUtils","set sms exception:",e);
        }
    }
    private static Class<?>[] getParamTypes(Class<?> cls, String mName) {
        Class<?> cs[] = null;

        Method[] mtd = cls.getMethods();

        for (int i = 0; i < mtd.length; i++) {
            if (!mtd[i].getName().equals(mName)) {
                continue;
            }
            cs = mtd[i].getParameterTypes();
        }
        return cs;
    }
    public static boolean checkNeedInstallByPackageName(Context context, String apkPath, String packageName) {

        if(isPackageInstalled(context, packageName)) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
                PackageInfo apkPackageInfo = getApkInfo(context, apkPath);
                if(apkPackageInfo == null) {
                    return false;
                }
                if(packageInfo.versionCode < apkPackageInfo.versionCode) {
                    return true;
                }
                return false;

            } catch(PackageManager.NameNotFoundException e) {
                return true;
            }
        }
        return true;
    }
}
