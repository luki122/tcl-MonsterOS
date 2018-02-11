package com.monster.cloud.sync;


import android.content.Context;

import com.tencent.qqpim.sdk.object.sms.SmsTimeType;
import com.tencent.tclsdk.sync.SyncCallLog;
import com.tencent.tclsdk.sync.SyncContact;
import com.tencent.tclsdk.sync.SyncSMS;

import java.lang.ref.WeakReference;

/**
 * Created by logic on 16-12-15.
 */

public class SyncSMSTask extends BaseSyncTask {

    private final WeakReference<Context> mWeakContext;
    private final WeakReference<TCLSyncManager.UIHandler> mWeakHandler;

    private SyncSMS task;

    SyncSMSTask(Context context, TCLSyncManager.UIHandler handler){
        super();
        this.mWeakContext = new WeakReference<Context>(context);
        this.mWeakHandler = new WeakReference<TCLSyncManager.UIHandler>(handler);
    }

    @Override
    protected boolean onPrepare() {
        Context context = mWeakContext.get();
        TCLSyncManager.UIHandler handler = mWeakHandler.get();
        if (null == context || null == handler)
            return false;
        task = new SyncSMS(context, new SyncProcessorObserver(this, handler), SmsTimeType.TIME_ALL);
        return true;
    }

    @Override
    protected void onStart() {
        task.sync();
    }

    @Override
    protected void onFinished() {
        task = null;
    }
    @Override
    public @SyncTaskType  int getTaskType() {
        return TASK_TYPE_SYNC_SMS;
    }

}
