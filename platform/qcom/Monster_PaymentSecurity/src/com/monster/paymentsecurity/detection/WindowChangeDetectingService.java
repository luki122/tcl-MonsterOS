package com.monster.paymentsecurity.detection;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.IntDef;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.monster.paymentsecurity.PayAppRiskActivity;
import com.monster.paymentsecurity.R;
import com.monster.paymentsecurity.bean.PayAppInfo;
import com.monster.paymentsecurity.constant.Constant;
import com.monster.paymentsecurity.db.PayListDao;
import com.monster.paymentsecurity.diagnostic.DiagnosticReport;
import com.monster.paymentsecurity.diagnostic.Diagnostor;
import com.monster.paymentsecurity.scan.BaseScanTask;
import com.monster.paymentsecurity.scan.Result;
import com.monster.paymentsecurity.scan.ScanningEngine;
import com.monster.paymentsecurity.scan.ScanningHelper;
import com.monster.paymentsecurity.scan.qscanner.ScanInstalledApk;
import com.monster.paymentsecurity.util.PackageUtils;
import com.monster.paymentsecurity.util.SettingUtil;
import com.monster.paymentsecurity.util.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import tmsdk.common.module.qscanner.QScanResultEntity;

import static android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;

/**
 * Created by sandysheny on 16-11-21.
 */

public class WindowChangeDetectingService extends AccessibilityService {
    private final static String TAG = "sandysheny";

    @IntDef(value = {START, NETWORK_UNAVAILABLE, OTHER_RISK, FINISHED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DetectState {
    }

    public static final int START = 1;
    public static final int NETWORK_UNAVAILABLE = 2;
    public static final int OTHER_RISK = 3;
    public static final int FINISHED = 4;


    private String lastPackage;
    private ScanningEngine mScanningEngine;
    private PayListDao mPayListDao;

    private ArrayMap<String, ScanningEngine.ScanningResultObserver> allActiveTasks = new ArrayMap<>();
    private ArrayMap<ScanningEngine.ScanningResultObserver, ArrayList<Result>> allScanPayAppResults = new ArrayMap<>();


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "WindowChangeDetectingService onCreate");
        mScanningEngine = new ScanningEngine(this);
        mPayListDao = new PayListDao(this);

        //执行一次全部安装应用扫描，支付应用数据insert到数据库
        mScanningEngine.startScanning(mScanAllInstalledApkObserver, ScanningHelper.getInstalledApksScanTasks(this));
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "WindowChangeDetectingService onServiceConnected");
        //Configure these here for compatibility with API 13 and below.
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (Build.VERSION.SDK_INT >= 16)
            //Just in case this helps
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName().toString();
            String className = event.getClassName().toString();
            ComponentName componentName = new ComponentName(packageName, className);
            ActivityInfo activityInfo = PackageUtils.tryGetActivity(this, componentName);
            boolean isActivity = activityInfo != null;
            if (isActivity) {
                Log.d(TAG, packageName + "/" + className);
                // 判断是否跟上一个activity属于同一package,同一package返回不做操作
                boolean isDifferent = !packageName.equals(lastPackage);
                if (!isDifferent) {
                    return;
                }

                lastPackage = packageName;

                if (allActiveTasks.get(packageName) != null) {
                    Log.e(TAG, "Scan task is running :" + packageName);
                    return;
                }

                //扫描判断是否属于支付应用
                Log.e(TAG, "start scan app is in payList :" + packageName);
                mScanningEngine.startScanning(mScanSingleApkObserver, new ScanInstalledApk(this, packageName));

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "WindowChangeDetectingService onDestroy");
        mScanningEngine.stopScanning();
        mScanningEngine = null;
    }


    /**
     * 判断支付应用检测是否开启，且当前应用是否开启检测
     *
     * @param packageName
     * @return
     */
    private boolean isNeedDetect(String packageName) {
        return mPayListDao.isNeedDetect(packageName) && SettingUtil.isPayAppDetectionEnable(this);
    }

    @Override
    public void onInterrupt() {
    }

    // 扫描全盘第三方应用，发现支付应用并保存数据库
    private ScanningEngine.ScanningResultObserver mScanAllInstalledApkObserver = new ScanningEngine.ScanningResultObserver() {
        @Override
        public void notifyScanningResult(Result result, float progress) {
            QScanResultEntity entity = (QScanResultEntity) result.getRawData();
            if (entity == null) {
                return;
            }
            if (entity.isInPayList) {
                ApplicationInfo applicationInfo = PackageUtils.tryGetApp(WindowChangeDetectingService.this, entity.packageName);
                PayAppInfo payAppInfo = PayAppInfo.from(WindowChangeDetectingService.this, applicationInfo);
                mPayListDao.insert(payAppInfo);
            }
            Log.d(TAG, "scan progress：" + progress + ":::::" + entity.packageName);

            if (progress >= 1) {
                Log.d(TAG, "finish scan broadcast");
                Intent intent = new Intent(Constant.ACTION_APP_CHANGE);
                sendBroadcast(intent);
            }
        }

        @Override
        public void notifyScanningState(@ScanningEngine.ScanningState int state) {

        }
    };

    // 扫描是否命中支付应用
    private ScanningEngine.ScanningResultObserver mScanSingleApkObserver = new ScanningEngine.ScanningResultObserver() {
        @Override
        public void notifyScanningResult(Result result, float progress) {
            QScanResultEntity entity = (QScanResultEntity) result.getRawData();
            if (entity == null || !entity.isInPayList) {
                return;
            }

            if (isNeedDetect(entity.packageName)) {
                Log.e(TAG, " app is in payList and need detect:" + entity.packageName);
                detect(entity.packageName);
            }
        }

        @Override
        public void notifyScanningState(@ScanningEngine.ScanningState int state) {

        }
    };


    private void detect(String pkg) {
        handleDetect(START);
        if (!Utils.isNetworkOk(this)) {
            handleDetect(NETWORK_UNAVAILABLE);
            return;
        }

        // 扫描是否联网/wifi/sms
        ScanningEngine.ScanningResultObserver scanPayAppObserver = new ScanningEngine.ScanningResultObserver() {
            @Override
            public void notifyScanningResult(Result result, float progress) {
                Log.i(TAG, "detect pay app :" + progress);

                ArrayList<Result> results = allScanPayAppResults.get(this);
                if (results == null) {
                    results = new ArrayList<>();
                }
                results.add(result);
                allScanPayAppResults.put(this, results);
            }

            @Override
            public void notifyScanningState(@ScanningEngine.ScanningState int state) {
                if (state == ScanningEngine.FINISHED) {
                    Log.i(TAG, "finish detect pay app");
                    ArrayList<Result> results = allScanPayAppResults.get(this);
                    Diagnostor diagnostor = new Diagnostor(WindowChangeDetectingService.this);
                    diagnostor.diagnose(results);
                    DiagnosticReport report = diagnostor.getReport();
                    if (report.hasRisk()) {
                        handleDetect(OTHER_RISK);
                    } else {
                        handleDetect(FINISHED);
                    }

                    Set<Map.Entry<String, ScanningEngine.ScanningResultObserver>> entrySet = allActiveTasks.entrySet();
                    for (Map.Entry<String, ScanningEngine.ScanningResultObserver> entry : entrySet) {
                        String packageName = entry.getKey();
                        ScanningEngine.ScanningResultObserver observer = entry.getValue();
                        if (this.equals(observer)) {
                            allActiveTasks.remove(packageName);
                            break;
                        }
                    }
                }
            }
        };

        Log.i(TAG, "start detect pay app");
        allActiveTasks.put(pkg, scanPayAppObserver);

        ArrayList<BaseScanTask> scanTaskList = new ArrayList<BaseScanTask>() {
            {
                addAll(ScanningHelper.getWifiScanTasks(WindowChangeDetectingService.this));
                add(ScanningHelper.getSmsScanTask(WindowChangeDetectingService.this));
            }
        };
        mScanningEngine.startScanning(scanPayAppObserver, scanTaskList);

    }

    private void showDialog() {
        Intent intent = new Intent(this, PayAppRiskActivity.class);
        startActivity(intent);
    }

    private void showStatusBar(int resId, int color) {
        StatusBarManager mStatusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        mStatusBarManager.updatePayState(true, getString(resId), color);
    }

    private void dismissStatusBar(int delay) {
        new Handler().postDelayed(() -> {
            StatusBarManager mStatusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
            mStatusBarManager.updatePayState(false, "", 0);
        }, delay);
    }

    private void handleDetect(@DetectState int state) {
        switch (state) {
            case START:
                showStatusBar(R.string.pay_app_detecting, getResources().getColor(R.color.bg_color_blue, getTheme()));
                break;
            case NETWORK_UNAVAILABLE:
                showStatusBar(R.string.pay_app_network_unavailable, getResources().getColor(R.color.bg_color_red, getTheme()));
                dismissStatusBar(1000);
                break;
            case OTHER_RISK:
                showStatusBar(R.string.pay_app_find_risk, getResources().getColor(R.color.bg_color_red, getTheme()));
                showDialog();
                dismissStatusBar(1000);
                break;
            case FINISHED:
                showStatusBar(R.string.pay_app_safe, getResources().getColor(R.color.bg_color_blue, getTheme()));
                dismissStatusBar(1000);
                break;
        }
    }

}