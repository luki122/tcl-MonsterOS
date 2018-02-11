package com.monster.cloud.sync;

import android.annotation.IntDef;
import android.support.annotation.CheckResult;
import android.util.Log;

import com.tencent.qqpim.sdk.accesslayer.def.ISyncDef;
import com.tencent.tclsdk.sync.ISync;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by logic on 16-12-15.
 */

public abstract class BaseSyncTask implements Runnable {

    private static final String TAG = "BaseSyncTask";

    @IntDef(value = {TASK_TYPE_SYNC_CONTACT, TASK_TYPE_SYNC_CALLLOG, TASK_TYPE_SYNC_SMS, TASK_TYPE_SYNC_SOFT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SyncTaskType{}
    public static final int TASK_TYPE_SYNC_CONTACT = 123;
    public static final int TASK_TYPE_SYNC_SMS = 124;
    public static final int TASK_TYPE_SYNC_CALLLOG = 125;
    public static final int TASK_TYPE_SYNC_SOFT = 126;

    @IntDef(value = {SYNC_ERR_TYPE_UNKNOW,
            SYNC_ERR_TYPE_SUCCEED,
            SYNC_ERR_TYPE_RELOGIN,
            SYNC_ERR_TYPE_CLIENT_ERR,
            SYNC_ERR_TYPE_SERVER_ERR,
            SYNC_ERR_TYPE_USER_CANCEL,
            SYNC_ERR_TYPE_FAIL_CONFLICT,
            SYNC_ERR_TYPE_TIME_OUT,
            SYNC_ERR_TYPE_SERVER_ABNORMAL,
            SYNC_ERR_TYPE_PRESYNC,
            SYNC_ERR_TYPE_BACKUP_SOFT_FAIL,
            SYNC_ERR_TYPE_BACK_SOFT_LOGINKEY_EXPIRE,
            SYNC_ERR_TYPE_TASK_PREPARE_FAIL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface  ResultCode{}

    public static final int SYNC_ERR_TYPE_UNKNOW = ISyncDef.SYNC_ERR_TYPE_UNKNOW;
    public static final int SYNC_ERR_TYPE_SUCCEED = ISyncDef.SYNC_ERR_TYPE_SUCCEED;
    public static final int SYNC_ERR_TYPE_RELOGIN = ISyncDef.SYNC_ERR_TYPE_RELOGIN;
    public static final int SYNC_ERR_TYPE_CLIENT_ERR = ISyncDef.SYNC_ERR_TYPE_CLIENT_ERR;
    public static final int SYNC_ERR_TYPE_SERVER_ERR = ISyncDef.SYNC_ERR_TYPE_SERVER_ERR;
    public static final int SYNC_ERR_TYPE_USER_CANCEL = ISyncDef.SYNC_ERR_TYPE_USER_CANCEL;
    public static final int SYNC_ERR_TYPE_FAIL_CONFLICT = ISyncDef.SYNC_ERR_TYPE_FAIL_CONFLICT;
    public static final int SYNC_ERR_TYPE_TIME_OUT = ISyncDef.SYNC_ERR_TYPE_TIME_OUT;
    public static final int SYNC_ERR_TYPE_SERVER_ABNORMAL = ISyncDef.SYNC_ERR_TYPE_SERVER_ABNORMAL;
    public static final int SYNC_ERR_TYPE_PRESYNC = ISyncDef.SYNC_ERR_TYPE_PRESYNC;
    public static final int SYNC_ERR_TYPE_BACKUP_SOFT_FAIL = 9;
    public static final int SYNC_ERR_TYPE_BACK_SOFT_LOGINKEY_EXPIRE = 10;
    public static final int SYNC_ERR_TYPE_TASK_PREPARE_FAIL = 11;

    protected @ResultCode int resultCode = SYNC_ERR_TYPE_SUCCEED;

    private  AtomicBoolean finished = new AtomicBoolean(false);
    private AtomicBoolean canceled = new AtomicBoolean(false);

    @Override
    public void run() {
        debugLog("prepare...");

        if (!onPrepare()) {
            resultCode = SYNC_ERR_TYPE_TASK_PREPARE_FAIL;
            Log.w(TAG, getClass().getCanonicalName() +  " prepare fail!");
            return;
        }

        debugLog("started...");
        onStart();

            //QQ sdk callback progress 等待100
        while (!finished.get() && !canceled.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        debugLog("finished...");
        onFinished();
    }

    void setResultCode(@ResultCode int errCode){
        this.resultCode = errCode;
    }

    public @ResultCode int getResultCode(){
        return resultCode;
    }

    protected  abstract boolean onPrepare();

    protected  abstract void onStart();

    protected  abstract  void onFinished();

    public abstract @SyncTaskType int getTaskType();


    private void debugLog(String log){
        Log.v(TAG,getClass().getCanonicalName() + " "+ log);
    }


    public void setFinished(){
        finished.set(true);
    }

    public void cancel(){
        canceled.set(true);
    }

    public boolean isCanceled() {
        return canceled.get();
    }

}
