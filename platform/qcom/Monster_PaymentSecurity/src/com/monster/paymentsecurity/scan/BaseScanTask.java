package com.monster.paymentsecurity.scan;

import android.support.annotation.CheckResult;
import android.support.annotation.IntDef;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by logic on 16-11-21.
 */


@SuppressWarnings("unchecked")
public abstract class BaseScanTask<T> implements Runnable {
    private static final String TAG = "SecurityScan";
    private float weight; //任务权重，用于计算扫描进度

    //Android定义枚举方式，方便Lint检查代码错误
    @IntDef(value = {PRIORITY_ONE,PRIORITY_TWO,PRIORITY_THREE,PRIORITY_FOUR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Priority {}
    protected static final int PRIORITY_ONE = 1;
    protected static final int PRIORITY_TWO = 2;
    protected static final int PRIORITY_THREE = 3;
    protected static final int PRIORITY_FOUR = 4;

    @IntDef(value = {
            SCAN_TYPE_WIFI_STATE,
            SCAN_TYPE_WIFI_SECURITY,
            SCAN_TYPE_WIFI_DNS,
            SCAN_TYPE_WIFI_ARP,
            SCAN_TYPE_SMS_APP,
            SCAN_TYPE_SYSTEM_PAYMENT_ENV,
            SCAN_TYPE_SYSTEM_UPDATE,
            SCAN_TYPE_QSCANER_INSTALLED_APK,
            SCAN_TYPE_QSCANER_UNINSTALLED_APK,
            SCAN_TYPE_QSCANER_UNINSTALLED_APKS
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScanType{}
    public static final int SCAN_TYPE_WIFI_STATE = 1;
    public static final int SCAN_TYPE_WIFI_SECURITY = 2;
    public static final int SCAN_TYPE_WIFI_DNS = 3;
    public static final int SCAN_TYPE_WIFI_ARP = 4;

    public static final int SCAN_TYPE_SMS_APP = 5;

    public static final int SCAN_TYPE_SYSTEM_PAYMENT_ENV = 6;
    public static final int SCAN_TYPE_SYSTEM_UPDATE = 7;

    public static final int SCAN_TYPE_QSCANER_INSTALLED_APK = 8;
    public static final int SCAN_TYPE_QSCANER_UNINSTALLED_APK = 9;
    public static final int SCAN_TYPE_QSCANER_UNINSTALLED_APKS = 10;

    @IntDef(value = {
            SCAN_ERRCODE_DEFAULT,
            SCAN_ERRCODE_WIFI_DNS_PHISHING,
            SCAN_ERRCODE_WIFI_ARP_EXCEPTION,
            SCAN_ERRCODE_WIFI_SECURITY_BSSID_NOT_FOUND,
            SCAN_ERRCODE_WIFI_SECURITY_EXCEPTION,
            SCAN_ERRCODE_QSCANNER_APK_FILE_NOT_FOUND,
            SCAN_ERRCODE_QSCANNER_SCAN_ERROR,
            SCAN_ERRCODE_INIT_QSCANNER_ERROR,
            SCAN_ERRCODE_PREPARE_ERROR
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScanErrCode{}
    public static final int SCAN_ERRCODE_DEFAULT = Integer.MAX_VALUE;
    public static final int SCAN_ERRCODE_WIFI_DNS_PHISHING = 1;
    public static final int SCAN_ERRCODE_WIFI_ARP_EXCEPTION = 2;
    public static final int SCAN_ERRCODE_WIFI_SECURITY_BSSID_NOT_FOUND = 3;
    public static final int SCAN_ERRCODE_WIFI_SECURITY_EXCEPTION = 4;
    public static final int SCAN_ERRCODE_QSCANNER_APK_FILE_NOT_FOUND = 5;
    public static final int SCAN_ERRCODE_QSCANNER_SCAN_ERROR = 6;
    public static final int SCAN_ERRCODE_INIT_QSCANNER_ERROR = 7;
    public static final int SCAN_ERRCODE_PREPARE_ERROR = 8;

    private volatile boolean isCanceled ;
    private boolean started;

    protected Result result;

    protected  BaseScanTask(){
        this.isCanceled = false;
        this.started = false;
    }

    @Override
    public void run() {
        if (started) {
            throw new RuntimeException("task has started!");
        }

        //先初始化result，保证nonull
        result= new Result(getScanType());

        debugLog("prepare...");
        if (!onPrepare()) {
            if (result.getErrCode() == SCAN_ERRCODE_DEFAULT) {
                result.setErrCode(SCAN_ERRCODE_PREPARE_ERROR);
            }
            Log.w(TAG, getClass().getCanonicalName() +  " prepare fail!");
            return;
        }

        synchronized (this){
            if (isCanceled) {
                debugLog("cancel...");
                onCancel();
                return;
            }
        }

        started = true;
        debugLog("started...");

        T rawData = onStart();
        result.setRawData(rawData);

        if (isCanceled) {
           debugLog("cancel...");
            result = null;
            onCancel();
        }else {
            debugLog("finished...");
            onFinished();
        }
    }

    public  void cancel(){
        synchronized (this) {
            this.isCanceled = true;
        }
    }

    public boolean isCanceled(){
        return  isCanceled ;
    }

    public  Result getResult() {
        return result;
    }

    protected  abstract  boolean onPrepare();

    @CheckResult
    protected  abstract T onStart();

    protected  abstract  void onFinished();

    protected  abstract  void onCancel();

    /**
     *  任务优先级， 用于排序
     * @return @Priority
     */
    public abstract @Priority int getPriority();

    protected abstract @ScanType int getScanType();

    private void debugLog(String log){
        Log.v(TAG,getClass().getSimpleName() + " "+ log);
    }
}
