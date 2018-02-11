package cn.download.mie.downloader;

import java.util.List;

/**
 * 加载时做一些事情
 */
public interface ILoadListener {
    List<DownloadTask> onLoad(List<DownloadTask> tasks);
}
