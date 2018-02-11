package com.monster.cloud.sync;

import android.content.Context;

import com.tencent.tclsdk.sync.SyncCallLog;
import com.tencent.tclsdk.sync.SyncContact;

import java.lang.ref.WeakReference;

/**
 * Created by logic on 16-12-15.
 */
public class SyncCallLogTask extends BaseSyncTask {

    private final WeakReference<Context> mWeakContext;
    private final WeakReference<TCLSyncManager.UIHandler> mWeakHandler;

    private SyncCallLog task;

    SyncCallLogTask(Context context, TCLSyncManager.UIHandler handler){
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
        task = new SyncCallLog(context, new SyncProcessorObserver(this, handler));
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
        return TASK_TYPE_SYNC_CALLLOG;
    }
}
