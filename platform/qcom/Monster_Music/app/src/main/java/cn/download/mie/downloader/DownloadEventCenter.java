package cn.download.mie.downloader;

import android.os.Handler;
import android.os.Looper;

import java.util.LinkedList;


public class DownloadEventCenter implements IDownloadListener{

    private final LinkedList<IDownloadListener> mDownloadListener = new LinkedList<>();
    private Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
    public synchronized void addDownloadListener(IDownloadListener downloadListener) {
        if( !mDownloadListener.contains(downloadListener)) {
            mDownloadListener.add(downloadListener);
        }
    }

    public synchronized  void removeDownloadListener(IDownloadListener downloadListener) {
        mDownloadListener.remove(downloadListener);
    }


    @Override
    public void onDownloadStatusChange(final DownloadTask item) {
        for(final IDownloadListener listener : mDownloadListener ) {
            if( !listener.acceptItem(item)) continue;
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onDownloadStatusChange(item);
                }
            });
        }
    }

    @Override
    public  synchronized void onDownloadProgress(final DownloadTask item,final long downloadSize,final long totalSize,final int speed,final int maxSpeed,final long timeCost) {
        for(final IDownloadListener listener : mDownloadListener ) {
            if( !listener.acceptItem(item)) continue;
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onDownloadProgress(item, downloadSize, totalSize, speed, maxSpeed, timeCost);
                }
            });
        }
    }

    @Override
    public boolean acceptItem(DownloadTask item) {
        return true;
    }
}
