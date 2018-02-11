package com.mst.thememanager.job;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import android.util.Log;


public class MultiTaskDealer {

    public static final String TAG = "MultiTaskDealer";
    public static final String LOAD_THEME_FROM_DATABASE_TASK = "load_theme_from_db";

    private static HashMap<String, WeakReference<MultiTaskDealer>> map = new HashMap<String, WeakReference<MultiTaskDealer>>();
    private boolean mIsRunning = false;
    public static MultiTaskDealer getDealer(String name) {
        WeakReference<MultiTaskDealer> ref = map.get(name);
        MultiTaskDealer dealer = ref!=null?ref.get():null;
        return dealer;
    }

    public static MultiTaskDealer startDealer(String name,int taskCount) {
        MultiTaskDealer dealer = getDealer(name);
        if(dealer==null) {
            dealer = new MultiTaskDealer(name,taskCount);
            WeakReference<MultiTaskDealer> ref = new WeakReference<MultiTaskDealer>(dealer);
            map.put(name,ref);
        }
        return dealer;
    }

    
    
    public void startLock() {
        mLock.lock();
    }

    public void endLock() {
        mLock.unlock();
    }

    private ThreadPoolExecutor mExecutor;
    private int mTaskCount = 0;
    private boolean mNeedNotifyEnd = false;
    private Object mObjWaitAll = new Object();
    private ReentrantLock mLock = new ReentrantLock();

    public MultiTaskDealer(String name,int taskCount) {
        final String taskName = name;
        ThreadFactory factory = new ThreadFactory()
        {
            private final AtomicInteger mCount = new AtomicInteger(1);

            public Thread newThread(final Runnable r) {
                Log.d(TAG, "create a new thread:" + taskName);
                return new Thread(r, taskName + "-" + mCount.getAndIncrement());
            }
        };
        mExecutor = new ThreadPoolExecutor(taskCount, taskCount, 5, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), factory){
            protected void afterExecute(Runnable r, Throwable t) {
                if(t!=null) {
                    t.printStackTrace();
                }
                MultiTaskDealer.this.TaskCompleteNotify(r);
                mIsRunning = false;
                Log.d(TAG, "end task");
                super.afterExecute(r,t);
            }
            protected void beforeExecute(Thread t, Runnable r) {
                Log.d(TAG, "start task");
                super.beforeExecute(t,r);
            }
        };
    }

    public boolean isRunning(){
    	return mIsRunning;
    }
    
    public void addTask(Runnable task) {
        synchronized (mObjWaitAll) {
            mTaskCount+=1;
        }
        mIsRunning = true;
        mExecutor.execute(task);
        Log.d(TAG, "addTask");
    }

    private void TaskCompleteNotify(Runnable task) {
        synchronized (mObjWaitAll) {
            mTaskCount-=1;
            if(mTaskCount<=0 && mNeedNotifyEnd) {
                Log.d(TAG, "complete notify");
                mObjWaitAll.notify();
            }
        }
    }

    public void waitAll() {
        Log.d(TAG, "start wait all");
        synchronized (mObjWaitAll) {
            if(mTaskCount>0) {
                mNeedNotifyEnd = true;
                try {
                    mObjWaitAll.wait();
                } catch (Exception e) {
                }
                mNeedNotifyEnd = false;
            }
            Log.d(TAG, "wait finish");
            return;
        }
    }
}
