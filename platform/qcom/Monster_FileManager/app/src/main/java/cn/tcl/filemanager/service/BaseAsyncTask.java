/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.os.AsyncTask;

import cn.tcl.filemanager.manager.FileInfoManager;

public abstract class BaseAsyncTask extends AsyncTask<Void, ProgressInfo, Integer> {

    protected FileManagerService.OperationEventListener mListener;
    protected FileInfoManager mFileInfoManager;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;
    /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
    /*MODIFIED-BEGIN by jian.xu, 2016-04-18,BUG-1868328*/
    public static final int NEED_UPDATE_TIME = 200;
    protected long mStartOperationTime;
    /*MODIFIED-END by jian.xu,BUG-1868328*/
    /* MODIFIED-END by haifeng.tang,BUG-1987329*/

    protected Context mContext;

    protected boolean mCancelled;

    public void setCancel(boolean cancel) {
    	mCancelled = cancel;
    }

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }
    };

    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new LinkedBlockingQueue<Runnable>(128);

    public static final Executor THREAD_POOL_EXECUTOR =
            new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory,new ThreadPoolExecutor.DiscardOldestPolicy());

    /**
     * Constructor of BaseAsyncTask
     *
     * @param fileInfoManager a instance of FileInfoManager, which manages
     *            information of files in FileManager.
     * @param listener a instance of OperationEventListener, which is a
     *            interface doing things before/in/after the task.
     */
    public BaseAsyncTask(Context context, FileInfoManager fileInfoManager, FileManagerService.OperationEventListener listener) {
        if (fileInfoManager == null) {
            throw new IllegalArgumentException();
        }
        mContext = context;
        mFileInfoManager = fileInfoManager;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        if (mListener != null) {
            mListener.onTaskPrepare();
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
		if (mCancelled) {
			return;
		}
        if (mListener != null) {
            mListener.onTaskResult(result);
            mListener = null;
        }
    }

    @Override
    protected void onCancelled() {
        if (mListener != null) {
            mListener.onTaskResult(FileManagerService.OperationEventListener.ERROR_CODE_USER_CANCEL);
            mListener = null;
        }
    };

    @Override
    protected void onProgressUpdate(ProgressInfo... values) {
        if (mListener != null && values != null && values[0] != null) {
            mListener.onTaskProgress(values[0]);
        }
    }

    /**
     * This method remove listener from task. Set listener associate with task
     * to be null.
     */
    protected void removeListener() {
        if (mListener != null) {
            mListener = null;
        }
    }

    /**
     * This method set mListener with certain listener.
     *
     * @param listener the certain listener, which will be set to be mListener.
     */
    public void setListener(FileManagerService.OperationEventListener listener) {
        mListener = listener;
    }


    /* MODIFIED-BEGIN by haifeng.tang, 2016-05-05,BUG-1987329*/
    /*MODIFIED-BEGIN by jian.xu, 2016-04-18,BUG-1868328*/
    public boolean needUpdate() {
        long operationTime = System.currentTimeMillis() - mStartOperationTime;
        if (operationTime > NEED_UPDATE_TIME) {
            mStartOperationTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }
    /*MODIFIED-END by jian.xu,BUG-1868328*/
    /* MODIFIED-END by haifeng.tang,BUG-1987329*/

}
