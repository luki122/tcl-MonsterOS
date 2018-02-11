package cn.download.mie.base.util;
import android.content.Context;
import cn.download.mie.downloader.Downloader;
import cn.download.mie.downloader.IDownloader;
import cn.tcl.music.util.LogUtil;

/**
 * Created by Rex on 2015/7/2.
 */
public class DownloadManager {

    private static DownloadManager instance;
    private IDownloader downloader;


    private DownloadManager(Context context) {
        downloader = new Downloader();
        LogUtil.d("downloadtest","downloader.init(context)");
        downloader.init(context);
    }


    public static DownloadManager getInstance(Context context) {
        if ( instance == null) {
            synchronized (DownloadManager.class) {
                if( instance == null) {
                    instance = new DownloadManager(context);
                }
            }
        }else{
            if(instance.getDownloader()!=null){
                instance.getDownloader().setContext(context);
            }
        }
        return instance;
    }

    public IDownloader getDownloader() {
        return downloader;
    }
}
