package cn.download.mie.downloader;

/**
 * Created by Rex on 2015/6/3.
 */
public interface IStatisticsLogger {

    void onStartLogger(DownloadTask item);

    void onFinishLogger(DownloadTask item);

    void onPauseLogger(DownloadTask item);
    void onCancelLogger(DownloadTask item);
    void onContinueLogger(DownloadTask item);
    void onErrorLogger(DownloadTask item, DownloadException exception);
}
