package cn.download.mie.downloader;

/**
 * Created by Rex on 2015/6/3.
 */
public interface IDownloadStrategy {

    /**
     * 当网络改变时，是否继续下载
     * @param networkType 网络类型, @see ConnectiveManager
     * @return true 可以继续下载，false 不用下载
     */
    boolean onNetworkChange(int networkType, DownloadTask task);

    boolean onRetry(DownloadTask task);

    boolean canAutoStart(DownloadTask task);
}
