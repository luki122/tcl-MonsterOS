package com.android.calculator2.exchange.net;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MyThreadPool {

    private static final int CPU_COUNT = Runtime.getRuntime()
            .availableProcessors();

    private final static int POOL_SIZE = CPU_COUNT * 2;//池中所保存的线程数，包括空闲线程。
    private final static int MAX_POOL_SIZE = POOL_SIZE + 2;//池中允许的最大线程数。
    private final static int KEEP_ALIVE_TIME = 1;//当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间。

    private static final BlockingQueue<Runnable> mPoolWorkQueue = new ArrayBlockingQueue<Runnable>(128);

    private static final ThreadFactory mThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "DentalThreadPool #"
                    + mCount.getAndIncrement());
        }
    };

    private static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
            mPoolWorkQueue, mThreadFactory,new ThreadPoolExecutor.DiscardOldestPolicy());

    /** 执行Runnable */
    public static void dentalThreadExecute(Runnable runnable) {
        THREAD_POOL_EXECUTOR.execute(runnable);
    }

    /** 退出时清除队列 */
    public static void removeQueueThread() {
        mPoolWorkQueue.clear();
    }
    
    public static Executor getExecutor(){
        return THREAD_POOL_EXECUTOR;
    }

}
