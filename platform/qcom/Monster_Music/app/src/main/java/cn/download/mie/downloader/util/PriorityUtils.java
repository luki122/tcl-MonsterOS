package cn.download.mie.downloader.util;

import cn.download.mie.downloader.DownloadTask;

import java.util.Collection;

public class PriorityUtils {

    public static int getMinPriority(Collection<DownloadTask> data) {
        int min = -1;
        synchronized (data) {
            for(DownloadTask task:data) {
                if( task.mPriority < min) {
                    min = task.mPriority;
                }
            }
        }
        return min;
    }

    public static int getMaxSequence(Collection<DownloadTask> data) {
        int max = 0;
        synchronized (data) {
            for(DownloadTask task:data) {
                if( task.mSequence > max) {
                    max = task.mSequence;
                }
            }
        }
        return max;
    }
}
