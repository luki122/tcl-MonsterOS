package cn.download.mie.downloader.core;

import cn.download.mie.downloader.DownloadException;
import cn.download.mie.downloader.DownloadTask;

public interface INetworkDownloader {
    void download(DownloadTask task) throws DownloadException;
}
