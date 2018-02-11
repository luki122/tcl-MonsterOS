package cn.download.mie.downloader.core;

import cn.download.mie.downloader.DownloadException;
import cn.download.mie.downloader.DownloadStatus;
import cn.download.mie.downloader.DownloadTask;

import java.util.concurrent.BlockingQueue;

public class TaskThread extends Thread {

    private BlockingQueue<DownloadTask> mWaitTasks;
    private INetworkDownloader mNetworkDownloader;
    public volatile boolean mCancel = false;

    public TaskThread(BlockingQueue<DownloadTask> waitTasks, INetworkDownloader networkDownloader) {
        super("TaskThread" + System.nanoTime());
        this.mWaitTasks = waitTasks;
        this.mNetworkDownloader = networkDownloader;
    }

    @Override
    public void run() {
        while (!mCancel) {
            try {
                DownloadTask task = mWaitTasks.take();
                if (mCancel) {
                    break;
                }

                if (task != null) {
                    try {
                        task.getDownloader().onTaskGoing(task);
                        mNetworkDownloader.download(task);
                    } catch (DownloadException e) {
                        e.printStackTrace();
                        if (e.mErrorCode == DownloadException.ECODE_PAUSE) {
                            if (task.mPriority == DownloadTask.PRORITY_LOW) {
                                //自动下载的。重新加入
                                //task.getDownloader().startDownloadInLow(task);
                            }
                            task.mStatus = DownloadStatus.STOP;
                            task.getDownloader().getEventCenter().onDownloadStatusChange(task);
                            continue;

                        } else if (e.mErrorCode == DownloadException.ECODE_NETWORK) {
                            //如果是网络下载失败的,缓存到任务队列中，等有网络的时候再继续下载
                            task.getDownloader().retry(task);

                        } else if (e.mErrorCode == DownloadException.ECODE_SERVER) {
                            task.mStatus = DownloadStatus.ERROR;
                            task.getDownloader().getEventCenter().onDownloadStatusChange(task);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        task.mStatus = DownloadStatus.ERROR;
                        task.getDownloader().getEventCenter().onDownloadStatusChange(task);
                    } finally {
                        task.getDownloader().onTaskStop(task);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }


}
