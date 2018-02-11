package com.monster.market.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Message;

import com.monster.market.MarketApplication;
import com.monster.market.bean.InstalledAppInfo;
import com.monster.market.constants.Constant;
import com.monster.market.db.InstalledAppDao;
import com.monster.market.download.AppDownloadService;
import com.monster.market.install.InstallAppManager;
import com.monster.market.utils.LogUtil;
import com.monster.market.utils.SystemUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PackageReceiver extends BroadcastReceiver {
    public static final int HANDLE_UPDATE_PROGRESS = 1;

    @Override
    public void onReceive(final Context context, final Intent intent) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String action = intent.getAction();
                LogUtil.i("PackageReceiver", "onReceive:" + action);
                // 检查自身有没在列表中，没有的话加入到列表
                InstalledAppDao dao = new InstalledAppDao(context);
                dao.openDatabase();
                if (action.equals("android.intent.action.PACKAGE_ADDED")) {
                    String packageName = intent.getDataString();
                    packageName = packageName.substring(packageName.indexOf(":") + 1,
                            packageName.length());

                    PackageManager pm = context.getPackageManager();
                    PackageInfo pInfo = null;
                    try {
                        pInfo = pm.getPackageInfo(packageName, 0);
                    } catch (NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (pInfo != null) {

                        ApplicationInfo appInfo = pInfo.applicationInfo;
                        InstalledAppInfo installedAppInfo = new InstalledAppInfo();
                        installedAppInfo.setName(pInfo.applicationInfo.loadLabel(pm)
                                .toString());
                        installedAppInfo.setIconId(appInfo.icon);
                        installedAppInfo.setPackageName(appInfo.packageName);
                        installedAppInfo.setVersionCode(pInfo.versionCode);
                        installedAppInfo.setVersion(pInfo.versionName);
                        installedAppInfo.setApkPath(appInfo.sourceDir);
                        setAppFlag(installedAppInfo, appInfo);
                        installedAppInfo.setMd5(SystemUtil.getApkMD5(context, packageName));
                        installedAppInfo.setCerStrMd5(SystemUtil.getInstallPackageSignature(context, packageName));

                        if (dao.getInstalledAppInfo(packageName) != null) {
                            dao.updateInstalledApp(installedAppInfo);
                        } else {
                            dao.insert(installedAppInfo);
                        }
                    }

                } else if (action.equals("android.intent.action.PACKAGE_CHANGED")) {
                    String packageName = intent.getDataString();
                    packageName = packageName.substring(packageName.indexOf(":") + 1,
                            packageName.length());

                    PackageManager pm = context.getPackageManager();
                    PackageInfo pinfo = null;
                    try {
                        pinfo = pm.getPackageInfo(packageName, 0);
                    } catch (NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (pinfo != null) {

                        ApplicationInfo appInfo = pinfo.applicationInfo;
                        InstalledAppInfo installedAppInfo = new InstalledAppInfo();
                        installedAppInfo.setName(pinfo.applicationInfo.loadLabel(pm)
                                .toString());
                        installedAppInfo.setIconId(appInfo.icon);
                        installedAppInfo.setPackageName(appInfo.packageName);
                        installedAppInfo.setVersionCode(pinfo.versionCode);
                        installedAppInfo.setVersion(pinfo.versionName);
                        installedAppInfo.setApkPath(appInfo.sourceDir);
                        setAppFlag(installedAppInfo, appInfo);
                        installedAppInfo.setMd5(SystemUtil.getApkMD5(context, packageName));
                        installedAppInfo.setCerStrMd5(SystemUtil.getInstallPackageSignature(context, packageName));

                        if (dao.getInstalledAppInfo(packageName) != null) {
                            dao.updateInstalledApp(installedAppInfo);
                        } else {
                            dao.insert(installedAppInfo);
                        }

                        context.sendBroadcast(new Intent(Constant.ACTION_MARKET_UPDATE));
                    } else {
                        dao.deleteInstalledApp(packageName);

                        context.sendBroadcast(new Intent(Constant.ACTION_MARKET_UPDATE));
                    }

                } else if (action.equals("android.intent.action.PACKAGE_REPLACED")) {
                    String packageName = intent.getDataString();
                    packageName = packageName.substring(packageName.indexOf(":") + 1,
                            packageName.length());

                    PackageManager pm = context.getPackageManager();
                    PackageInfo pInfo = null;
                    try {
                        pInfo = pm.getPackageInfo(packageName, 0);
                    } catch (NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (pInfo != null) {

                        ApplicationInfo appInfo = pInfo.applicationInfo;
                        InstalledAppInfo installedAppInfo = new InstalledAppInfo();
                        installedAppInfo.setName(pInfo.applicationInfo.loadLabel(pm)
                                .toString());
                        installedAppInfo.setIconId(appInfo.icon);
                        installedAppInfo.setPackageName(appInfo.packageName);
                        installedAppInfo.setVersionCode(pInfo.versionCode);
                        installedAppInfo.setVersion(pInfo.versionName);
                        installedAppInfo.setApkPath(appInfo.sourceDir);
                        setAppFlag(installedAppInfo, appInfo);
                        installedAppInfo.setMd5(SystemUtil.getApkMD5(context, packageName));
                        installedAppInfo.setCerStrMd5(SystemUtil.getInstallPackageSignature(context, packageName));

                        dao.updateInstalledApp(installedAppInfo);

                        context.sendBroadcast(new Intent(Constant.ACTION_MARKET_UPDATE));
                    }

                } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
                    String packageName = intent.getDataString();
                    packageName = packageName.substring(packageName.indexOf(":") + 1,
                            packageName.length());

                    dao.deleteInstalledApp(packageName);

                    context.sendBroadcast(new Intent(Constant.ACTION_MARKET_UPDATE));
                }


                InstalledAppInfo selfInfo = dao.getInstalledAppInfo(context
                        .getPackageName());
                if (selfInfo == null) {
                    PackageManager pm = context.getPackageManager();
                    PackageInfo pInfo = null;
                    try {
                        pInfo = pm.getPackageInfo(context.getPackageName(), 0);
                    } catch (NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (pInfo != null) {
                        ApplicationInfo appInfo = pInfo.applicationInfo;
                        InstalledAppInfo installedAppInfo = new InstalledAppInfo();
                        installedAppInfo.setName(pInfo.applicationInfo.loadLabel(pm)
                                .toString());
                        installedAppInfo.setIconId(appInfo.icon);
                        installedAppInfo.setPackageName(appInfo.packageName);
                        installedAppInfo.setVersionCode(pInfo.versionCode);
                        installedAppInfo.setVersion(pInfo.versionName);
                        installedAppInfo.setApkPath(appInfo.sourceDir);
                        setAppFlag(installedAppInfo, appInfo);
                        installedAppInfo.setMd5(SystemUtil.getApkMD5(context, context
                                .getPackageName()));
                        installedAppInfo.setCerStrMd5(SystemUtil.getInstallPackageSignature(context, context
                                .getPackageName()));

                        dao.insert(installedAppInfo);
                    }
                }
                dao.closeDatabase();

                updateListData(context);
                handler.sendEmptyMessage(HANDLE_UPDATE_PROGRESS);

                // 设置需要重新检测可更新数量
                MarketApplication.appUpgradeNeedCheck = true;
            }
        }).start();

    }

    private void updateListData(Context context) {
        InstalledAppDao dao = new InstalledAppDao(context);
        dao.openDatabase();
        List<InstalledAppInfo> installedAppList = dao.getInstalledAppList();
        Map<String, InstalledAppInfo> installedAppMap = new HashMap<String, InstalledAppInfo>();
        for (InstalledAppInfo info : installedAppList) {
            installedAppMap.put(info.getPackageName(), info);
        }
        dao.closeDatabase();
        InstallAppManager.setInstalledAppList(installedAppList);
        InstallAppManager.setInstalledAppMap(installedAppMap);
    }

    private void setAppFlag(InstalledAppInfo installedAppInfo,
                            ApplicationInfo appInfo) {
        if ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            // 代表的是系统的应用,但是被用户升级了. 用户应用
            installedAppInfo.setAppFlag(InstalledAppInfo.FLAG_UPDATE);
        } else if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            // 代表的用户的应用
            installedAppInfo.setAppFlag(InstalledAppInfo.FLAG_USER);
        } else {
            // 系统应用
            installedAppInfo.setAppFlag(InstalledAppInfo.FLAG_SYSTEM);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLE_UPDATE_PROGRESS:
                    AppDownloadService.updateDownloadProgress();
                    break;
            }
        }
    };
}
