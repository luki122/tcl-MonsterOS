package cn.download.mie.downloader;

import android.content.Context;

import java.util.List;

import cn.tcl.music.model.live.SongDetailBean;


public interface IDownloader {
    void init(Context context);
    void init(DownloaderConfig config, Context context, ILoadListener loadListener);
    void startDownload(DownloadTask item);
    void startDownloadInLow(DownloadTask item);
    void pauseDownload(DownloadTask item);
    void deleteDownload(DownloadTask item);
    void stopAllDownload();
    void addDownloadListener(IDownloadListener downloadListener);
    void removeDownloadListener(IDownloadListener downloadListener);
    void quit();
    void startMusicDownload(String id);
    void startDownloadInNetwork(DownloadTask downloadTask);
    void setContext(Context context);
    List<DownloadTask> getAllTask();
    void startLyricDownload(String path,String name);
    void startPictureDownload(DownloadTask downloadTask);
    void startBatchMusicDownload(List<SongDetailBean> listData);
}
