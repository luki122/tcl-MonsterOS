package cn.download.mie.downloader;

/**
 * Created by Rex on 2015/6/3.
 */
public interface IDownloadListener {

    void onDownloadStatusChange(DownloadTask item);

    void onDownloadProgress(DownloadTask item, long downloadSize, long totalSize, int speed, int maxSpeed, long timeCost);

    boolean acceptItem(DownloadTask item);

}
