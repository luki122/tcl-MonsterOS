/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity; // MODIFIED by songlin.qi, 2016-06-14,BUG-2269190
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import cn.tcl.filemanager.manager.MountManager;

import java.lang.reflect.Method; // MODIFIED by songlin.qi, 2016-06-14,BUG-2269190

import mst.app.dialog.AlertDialog;


public class CommonUtils {
    private static final String FONT_ROBOTO_MEDIUM_FILE = "/system/fonts/Roboto-Medium.ttf";
    private static final String FONT_ROBOTO_REGULAR_FILE = "/system/fonts/Roboto-Regular.ttf";
    public static final String BOOSTER_PACKAGE_NAME = "com.tct.onetouchbooster";
    public static final String BOOSTER_PACKAGE_MAIN_CLASS = "com.tct.onetouchbooster.ui.MainActivity";
    public static final String CN_PHONE_KEEPER_PACKAGE_NAME = "com.tct.securitycenter";
    public static final String CN_PHONE_KEEPER_PACKAGE_MAIN_CLASS  = "com.tct.securitycenter.main.MainActivity";
    public static final String CN_PHONE_KEEPER_PACKAGE_CLEAN_CLASS  = "com.tct.securitycenter.storageclearup.StorageClearUpActivity"; // MODIFIED by songlin.qi, 2016-06-06,BUG-2223767
    public static final String PHONE_KEEPER_PACKAGE_NAME = CN_PHONE_KEEPER_PACKAGE_NAME;
    public static final String PHONE_KEEPER_PACKAGE_MAIN_CLASS  = CN_PHONE_KEEPER_PACKAGE_MAIN_CLASS;


    public static Typeface getRobotoMedium() {
        Typeface tf = Typeface.createFromFile(FONT_ROBOTO_MEDIUM_FILE);

        return tf;
    }

    public static Typeface getRobotoRegular() {
        Typeface tf = Typeface.createFromFile(FONT_ROBOTO_REGULAR_FILE);

        return tf;
    }

    public static int getTotalWidthofListView(AdapterView<ListAdapter> listView) {
        ListAdapter mAdapter = listView.getAdapter();
        if (mAdapter == null) {
            return 0;
        }
        int totalWidth = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View mView = mAdapter.getView(i, null, listView);
            mView.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            // mView.measure(0, 0);
            totalWidth += mView.getMeasuredWidth();

        }
        Log.w("SNS", "listview width" + String.valueOf(totalWidth));

        return totalWidth;
    }

    // get screen wid and hei
    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowMgr = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowMgr.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        return width;
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowMgr = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowMgr.getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        return height;
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }

    public static long getYesterdayTime() {
        return (System.currentTimeMillis() - 86400000 * 2) / 1000;
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean checkApkExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(packageName,
                            PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static boolean hasM() {
        return Build.VERSION.SDK_INT >= 23;
    }

    // ADD START FOR PR980890,978687 BY Wenjing.ni 20151126
    public static boolean isPhoneStorageZero() {
        MountManager mMountManager = MountManager.getInstance();
        long blocSize = 0;
        long blockCount = 0;
        try {
            String filePath = mMountManager.getPhonePath();
            if (filePath != null) {/*PR 1308449 zibin.wang add 2016.01.08*/
                StatFs statfs = new StatFs(filePath);
                try {
                    blocSize = statfs.getBlockSizeLong();
                    blockCount = statfs.getBlockCountLong();
                } catch (NoSuchMethodError e) {
                    blocSize = statfs.getBlockSizeLong();
                    blockCount = statfs.getBlockCountLong();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (blocSize * blockCount == 0) {
            return true;
        }
        return false;
    }

    // ADD END FOR PR980890,978687 BY Wenjing.ni 20151126
    /*PR 958557 zibin.wang add Start*/
    public static boolean isMemory512(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(info);
        if (info.totalMem <= 536870912) {
            return true;
        } else {
            return false;
        }
    }
   /*PR 958557 zibin.wang add End*/

    public static boolean hasShortcut(Activity activity, String appName) {
        boolean isInstallShortcut = false;
        try {
            final ContentResolver cr = activity.getContentResolver();
            final String AUTHORITY = "com.tct.launcher.settings";
            final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites");
            Cursor c = cr.query(CONTENT_URI, new String[]{
                    "title", "iconResource"
            }, "title=?", new String[]{
                    appName
            }, null);
            if (c != null && c.getCount() > 0) {
                isInstallShortcut = true;
            }
        } catch (Exception e) {

        }
        return isInstallShortcut;
    }

    public static String getLauncherPackageName(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null) {
            // should not happen. A home is always installed, isn't it?
            return null;
        }
        if (res.activityInfo.packageName.equals("android")) {
            return null;
        } else {
            return res.activityInfo.packageName;
        }
    }

    /* MODIFIED-BEGIN by songlin.qi, 2016-06-06,BUG-2223767*/
    /**
     *  Launch Activiy to clean space
     * @param context
     */
    public static void launchPhoneKeeperActivity(Context context) {
        if (context == null) {
            return;
        }

        try {
            Intent intent = new Intent();
            intent.setClassName(CommonUtils.PHONE_KEEPER_PACKAGE_NAME, CommonUtils.CN_PHONE_KEEPER_PACKAGE_CLEAN_CLASS);
            context.startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent = new Intent();
                intent.setClassName(CommonUtils.PHONE_KEEPER_PACKAGE_NAME, CommonUtils.PHONE_KEEPER_PACKAGE_MAIN_CLASS);
                context.startActivity(intent);
            } catch (Exception e1) {
                e.printStackTrace();
            }
        }
    }
    /* MODIFIED-END by songlin.qi,BUG-2223767*/


    /* MODIFIED-BEGIN by songlin.qi, 2016-06-14,BUG-2269190*/
    public static void setDialogTitleInCenter(AlertDialog alertDialog) {
        if (alertDialog == null) return;

        try {
            Class<?> reflectClassInfo = Class.forName("com.tct.util.AlertDialogHelper");
            if (reflectClassInfo != null) {
                Method method = reflectClassInfo.getDeclaredMethod("setTitleGravity", AlertDialog.class, int.class);
                method.invoke(null, alertDialog, Gravity.CENTER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /* MODIFIED-END by songlin.qi,BUG-2269190*/
}
