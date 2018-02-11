package com.tcl.monster.fota.downloadengine;

public interface DownloadListener {

    public void onDownloadUpdated(DownloadTask task);

    public void onDownloadPaused(DownloadTask task);

    public void onDownloadSuccessed(DownloadTask task);

    public void onDownloadFailed(DownloadTask task);

    public void onDownloadDeleted(DownloadTask task);
}
