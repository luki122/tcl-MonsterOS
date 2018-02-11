package com.monster.market.download;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AppDownloadThreadPool {

	private static Object sync = new Object();
	private static ThreadPoolExecutor threadPool = null;
	private static int corePoolSize = 3;
	private static BlockingQueue<Runnable> workQueue;
	private static RejectedExecutionHandler handler;

	public static ThreadPoolExecutor getThreadPoolExecutor() {
		synchronized (sync) {
			if (threadPool == null) {
				workQueue = new LinkedBlockingQueue<Runnable>();
				handler = new ThreadPoolExecutor.DiscardOldestPolicy();
				threadPool = new ThreadPoolExecutor(corePoolSize, corePoolSize, 1, TimeUnit.SECONDS, workQueue,
						handler);
			}
		}
		return threadPool;
	}

	public static void setCorePoolSize(int size) {
		if (threadPool != null) {
			corePoolSize = size;
			threadPool.setCorePoolSize(corePoolSize);
		}
	}

}
