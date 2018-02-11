package com.monster.paymentsecurity.detection;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.UserHandle;

import com.monster.paymentsecurity.bean.PayAppInfo;
import com.monster.paymentsecurity.bean.WhiteListInfo;
import com.monster.paymentsecurity.constant.Constant;
import com.monster.paymentsecurity.db.PayListDao;
import com.monster.paymentsecurity.db.WhiteListDao;
import com.monster.paymentsecurity.scan.Result;
import com.monster.paymentsecurity.scan.qscanner.ScanInstalledApk;
import com.monster.paymentsecurity.util.Utils;

import tmsdk.common.module.qscanner.QScanResultEntity;

/**
 * pkg安装后，扫描是否支付应用
 * <p>
 * Created by logic on 16-12-9.
 */
public final class PackageWatchService extends IntentService {

    private static final String TAG = "PackageWatchService";
    public static final String PKG_ACTION_STATE = "pkg_change_state";

    private PayListDao mPayListDao;
    private WhiteListDao mWhiteListDao;

    public PackageWatchService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPayListDao = new PayListDao(getApplicationContext());
        mWhiteListDao = new WhiteListDao(getApplicationContext());
        Utils.initTMSDK(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPayListDao = null;
        mWhiteListDao = null;
        Runtime.getRuntime().gc();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String packageName = intent.getDataString();
        packageName = packageName.substring(packageName.indexOf(":") + 1, packageName.length());

        String action = intent.getStringExtra(PKG_ACTION_STATE);
        if (action.equals("android.intent.action.PACKAGE_ADDED")) {
            ScanInstalledApk task = new ScanInstalledApk(getApplicationContext(), packageName);
            try {
                task.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Result result = task.getResult();

            QScanResultEntity entity = (QScanResultEntity) result.getRawData();
            if (entity != null && entity.isInPayList) {
                ApplicationInfo appInfo = null;
                try {
                    appInfo = getPackageManager().getApplicationInfo(entity.packageName, PackageManager.GET_META_DATA);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                mPayListDao.insert(PayAppInfo.from(getApplicationContext(), appInfo));
                Intent broadcastIntent = new Intent(Constant.ACTION_APP_CHANGE);
                sendBroadcastAsUser(broadcastIntent, UserHandle.CURRENT);
            }

            WhiteListInfo whiteListInfo = mWhiteListDao.getWhiteListApp(packageName);
            if (whiteListInfo != null) {
                whiteListInfo.setEnabled(true);
                mWhiteListDao.insert(whiteListInfo);
            }

        } else if (action.equals("android.intent.action.PACKAGE_REMOVED")) {
            if (mPayListDao.getPayApp(packageName) != null) {
                mPayListDao.delete(packageName);
                Intent broadcastIntent = new Intent(Constant.ACTION_APP_CHANGE);
                sendBroadcastAsUser(broadcastIntent, UserHandle.CURRENT);
            }

            WhiteListInfo whiteListInfo = mWhiteListDao.getWhiteListApp(packageName);
            if (whiteListInfo != null) {
                whiteListInfo.setEnabled(false);
                mWhiteListDao.update(whiteListInfo);
            }
        }
    }
}
