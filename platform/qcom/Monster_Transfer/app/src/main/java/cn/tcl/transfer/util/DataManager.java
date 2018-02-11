/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import cn.tcl.transfer.R;


public class DataManager {

    private static final String TAG = "DataManager";

    public static List<PackageInfo> mSysApps = new ArrayList<>();
    public static List<PackageInfo> mUserApps = new ArrayList<>();

    public static ArrayList<String> mSelectSysApps = new ArrayList<>();
    public static ArrayList<String> mSelectUserApps = new ArrayList<>();

    public static HashMap<String, PackageDetailInfo> mSizeInfo = new HashMap<>();
    public static HashSet<String> mSelectPackage = new HashSet<>();

    public static HashSet<String> BACKUP_SYS_APP = new HashSet<>();

    public static int selectItem = 0;

    private Context mContext = null;
    private static DataManager mInstance = null;

    private DataManager(Context context) {
        mContext = context;
    }


    public static synchronized DataManager getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new DataManager(context);
            String[] pkgs = context.getResources().getStringArray(R.array.backup_system_app_pkg);
            BACKUP_SYS_APP.addAll(Arrays.asList(pkgs));
        }
        return mInstance;
    }

    public static void reset() {
        mSysApps.clear();
        mUserApps.clear();

        mSelectSysApps.clear();
        mSelectUserApps.clear();

        mSizeInfo.clear();
        mSelectPackage.clear();

        BACKUP_SYS_APP.clear();
    }

    public void init() {
        PackageManager pckMan = mContext.getPackageManager();
        List<PackageInfo> packs = pckMan.getInstalledPackages(0);
        int count = packs.size();
        for (int i = 0; i < count; i++) {
            PackageInfo p = packs.get(i);
            ApplicationInfo appInfo = p.applicationInfo;
            if(TextUtils.equals(p.packageName, mContext.getPackageName())) {
                continue;
            }
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0 ) {
                if(BACKUP_SYS_APP.contains(p.packageName)) {
                    mSysApps.add(p);
                    mSelectSysApps.add(p.packageName);
                    DataManager.mSizeInfo.put(p.packageName, new PackageDetailInfo(p));
                    mSelectPackage.add(p.packageName);
                    LogUtils.d(TAG, getAppName(p)  + ": [" + p.packageName + "]" );
                }
            } else if((appInfo.flags & ApplicationInfo.FLAG_INSTALLED) > 0) {
                mUserApps.add(p);
                mSelectUserApps.add(p.packageName);
                DataManager.mSizeInfo.put(p.packageName, new PackageDetailInfo(p));
                mSelectPackage.add(p.packageName);
            }
        }
    }

    public static boolean isThirdApp(Context context, String pkg) {
        try {
            PackageManager pckMan = context.getPackageManager();
            PackageInfo packageInfo = pckMan.getPackageInfo(pkg, 0);
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                return false;
            } else if((appInfo.flags & ApplicationInfo.FLAG_INSTALLED) > 0) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "isThirdApp " + pkg, e);
        }
        return false;
    }

    private String getAppName(PackageInfo info) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = mContext.getPackageManager();
            String applicationName =
                    (String) packageManager.getApplicationLabel(info.applicationInfo);
            return applicationName;
        } catch (Exception e) {
            applicationInfo = null;
            Log.e(TAG, "getAppName:", e);
            return  "";
        }
    }

    public static long getSysSize() {
        long size = 0;
        for(String name : mSelectSysApps) {
            if(mSizeInfo.containsKey(name)) {
                PackageDetailInfo info = mSizeInfo.get(name);
                size += info.sysDataSize + info.externalDataSize;
            }
        }
        return size;
    }

    public static long getAppSize() {
        long size = 0;
        for(String name : mSelectUserApps) {

            if(mSizeInfo.containsKey(name)) {
                PackageDetailInfo info = mSizeInfo.get(name);
                size += info.sysDataSize + info.externalDataSize + info.apkSize;
            }

        }
        return size;
    }

    public static boolean isSysSelect() {
        if((DataManager.selectItem & (1 << Utils.CATEGORY_SYS)) == 0) {
            return false;
        }
        return true;
    }

    public static boolean isAppSelect() {
        if((DataManager.selectItem & (1 << Utils.CATEGORY_APP)) == 0) {
            return false;
        }
        return true;
    }

    public static boolean isImageSelect() {
        if((DataManager.selectItem & (1 << Utils.CATEGORY_IMAGE)) == 0) {
            return false;
        }
        return true;
    }

    public static boolean isVideoSelect() {
        if((DataManager.selectItem & (1 << Utils.CATEGORY_VIDEO)) == 0) {
            return false;
        }
        return true;
    }

    public static boolean isAudioSelect() {
        if((DataManager.selectItem & (1 << Utils.CATEGORY_AUDIO)) == 0) {
            return false;
        }
        return true;
    }

    public static boolean isDocSelect() {
        if((DataManager.selectItem & (1 << Utils.CATEGORY_DOCUMENT)) == 0) {
            return false;
        }
        return true;
    }

    public static long getSelectSysSize() {
        long size = 0;
        for (String packageName: mSelectSysApps) {
            if(DataManager.mSizeInfo.containsKey(packageName)) {
                size += DataManager.mSizeInfo.get(packageName).sysDataSize + DataManager.mSizeInfo.get(packageName).externalDataSize;
            } else {
                Log.e(TAG, packageName + " is not exist!");
            }
        }
        return size;
    }

    public static long getSelectAppSize() {
        long size = 0;
        for (String packageName: mSelectUserApps) {
            if(DataManager.mSizeInfo.containsKey(packageName)) {
                size += DataManager.mSizeInfo.get(packageName).sysDataSize
                        + DataManager.mSizeInfo.get(packageName).externalDataSize
                        + DataManager.mSizeInfo.get(packageName).apkSize;
            } else {
                Log.e(TAG, packageName + " is not exist!");
            }
        }
        return size;
    }

}
