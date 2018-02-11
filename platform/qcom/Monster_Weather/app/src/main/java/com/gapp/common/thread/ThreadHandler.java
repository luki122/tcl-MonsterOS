package com.gapp.common.thread;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import com.gapp.common.obj.IManager;

/**
 * Created by thundersoft on 16-7-28.
 */
public class ThreadHandler implements IManager {
    private final String mThreadName;
    private final int mThreadPriority;

    private HandlerThread mHandlerThread;
    private Handler mHandler;


    public ThreadHandler(String threadName) {
        this(threadName, Process.THREAD_PRIORITY_DEFAULT);
    }

    public ThreadHandler(String threadName, int threadPriority) {
        mThreadName = threadName;
        mThreadPriority = threadPriority;
    }

    @Override
    public void init() {
        if (null == mHandlerThread) {
            mHandlerThread = new HandlerThread(mThreadName, mThreadPriority);
            mHandlerThread.start();
            mHandler = new Handler(mHandlerThread.getLooper());
        }
    }

    /**
     * 异步线程运行
     *
     * @param runnable
     */
    public void post(Runnable runnable) {
        if (null != mHandler)
            mHandler.post(runnable);
    }

    /**
     * 异步线程运行
     *
     * @param runnable
     * @param timeMills
     */
    public void post(Runnable runnable, int timeMills) {
        if (null != mHandler)
            mHandler.postDelayed(runnable, timeMills);
    }

    /**
     * 移除未执行的Runnable
     *
     * @param runnable
     */
    public void remove(Runnable runnable) {
        if (null != mHandler)
            mHandler.removeCallbacks(runnable);
    }

    @Override
    public void recycle() {
        if (null != mHandlerThread) {
            mHandlerThread.quitSafely();
            mHandlerThread = null;
            mHandler = null;
        }
    }

    @Override
    public void onTrimMemory(int level) {
    }
}
