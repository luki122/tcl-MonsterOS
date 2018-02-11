package com.monster.cloud.sync;

import android.content.Context;
import android.util.Log;

import com.tencent.qqpim.softbox.SoftBoxProtocolModel;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by logic on 16-12-15.
 */
public class SyncSoftTask extends BaseSyncTask {

    private final WeakReference<Context> mWeakContext;
    private final WeakReference<TCLSyncManager.UIHandler> mWeakHandler;
    private StoppableThread fakeProgressThread;
    private volatile AtomicInteger fakeProgress = new AtomicInteger(0);

    SyncSoftTask(Context context, TCLSyncManager.UIHandler handler){
        super();
        this.mWeakContext = new WeakReference<Context>(context);
        this.mWeakHandler = new WeakReference<TCLSyncManager.UIHandler>(handler);
    }

    @Override
    protected boolean onPrepare() {
        start();
        return true;
    }

    @Override
    protected void onStart() {
        Context context = mWeakContext.get();
        if (context == null) {
            resultCode = SYNC_ERR_TYPE_TASK_PREPARE_FAIL;
        }

        int result = SoftBoxProtocolModel.backupSoft(context);

        if (result == SoftBoxProtocolModel.RESULT_FAIL){
            resultCode = SYNC_ERR_TYPE_BACKUP_SOFT_FAIL;
        }else if (result == SoftBoxProtocolModel.RESULT_LOGINKEY_EXPIRE){
            resultCode = SYNC_ERR_TYPE_BACK_SOFT_LOGINKEY_EXPIRE;
        }

        fakeProgress.set(100);
        setFinished();
        TCLSyncManager.UIHandler handler = mWeakHandler.get();
        handler.sendMessage(handler.obtainMessage(
                TCLSyncManager.MAIN_MSG_TASK_PROGRESS_CHANGED,
                fakeProgress.get(), -1, SyncSoftTask.this));
    }

    @Override
    protected void onFinished() {
        //nothing
    }

    @Override
    public @SyncTaskType  int getTaskType() {
        return TASK_TYPE_SYNC_SOFT;
    }

    abstract class StoppableThread extends Thread {
        @Override
        public void run() {
            taskBody();
        }

        abstract void taskBody();
    }

    private void start() {
        fakeProgressThread = new StoppableThread() {
            @Override
            void taskBody() {
                try {
                    int progress = fakeProgress.get();
                    while (progress >= 0 && progress < 99 && !isCanceled()) {
                        progress = fakeProgress.getAndAdd(1);
                        // TODO: 16-12-20
                        TCLSyncManager.UIHandler handler = mWeakHandler.get();
                        if (handler == null)
                            return;
                        Log.v("BaseSyncTask", "fake progress: " + progress);
                        handler.sendMessage(handler.obtainMessage(
                                TCLSyncManager.MAIN_MSG_TASK_PROGRESS_CHANGED,
                                progress, -1, SyncSoftTask.this));
                        Thread.sleep(300);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        fakeProgressThread.start();
    }
}
