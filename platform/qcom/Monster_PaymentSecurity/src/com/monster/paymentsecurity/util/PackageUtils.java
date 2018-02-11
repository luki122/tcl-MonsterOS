package com.monster.paymentsecurity.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

/**
 * Created by sandysheny on 16-11-25.
 */

public class PackageUtils {


    public static ActivityInfo tryGetActivity(Context context, ComponentName componentName) {
        try {
            return context.getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static ApplicationInfo tryGetApp(Context context, String packageName) {
        try {
            return context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /*
    * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过
    * appInfo.publicSourceDir = apkPath;来修正这个问题，详情参见:
    * http://code.google.com/p/android/issues/detail?id=9151
    */
    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath,
                PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                Log.e("ApkIconLoader", e.toString());
            }
        }
        return null;
    }

    public static Drawable getAppIcon(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName,PackageManager.GET_META_DATA);
            return info.loadIcon(pm);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }


    @SuppressWarnings("deprecation")
    public static void uninstallApp(String pkgName){
        //这里能Import是因为替换了原生sdk.jar
        IPackageManager packageManager =
                IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
        try {
            packageManager.deletePackageAsUser(pkgName, new IPackageDeleteObserver() {
                        @Override
                        public void packageDeleted(String s, int i) throws RemoteException {

                        }

                        @Override
                        public IBinder asBinder() {
                            return null;
                        }
                    },
                    android.os.Process.myUserHandle().getIdentifier(),
                    PackageManager.DELETE_ALL_USERS);
        } catch (RemoteException e) {
            Log.e("--------------", "Failed to talk to package");
        }
    }
}
