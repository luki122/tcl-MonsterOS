/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.utils;

import android.os.*;
import android.os.Process;

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
     * Asynchronous thread running
     *
     * @param runnable
     */
    public void post(Runnable runnable) {
        if (null != mHandler)
            mHandler.post(runnable);
    }

    /**
     * Asynchronous thread running
     *
     * @param runnable
     * @param timeMills
     */
    public void post(Runnable runnable, int timeMills) {
        if (null != mHandler)
            mHandler.postDelayed(runnable, timeMills);
    }

    /**
     * Remove not executed Runnable
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
