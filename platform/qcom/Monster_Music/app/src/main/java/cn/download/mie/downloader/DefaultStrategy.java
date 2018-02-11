package cn.download.mie.downloader;

import android.net.ConnectivityManager;

public class DefaultStrategy implements IDownloadStrategy {
    public static boolean canDownloadInMobile=false ;


    @Override
    public boolean onNetworkChange(int networkType, DownloadTask task) {
        if(networkType == ConnectivityManager.TYPE_WIFI) {
            return true;
        }else{
            return canDownloadInMobile;
        }

    }

    @Override
    public boolean onRetry(DownloadTask task) {
        return !task.isCancel;
    }

    @Override
    public boolean canAutoStart(DownloadTask task) {
        return true;
    }
}
