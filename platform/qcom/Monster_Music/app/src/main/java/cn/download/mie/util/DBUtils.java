package cn.download.mie.util;

import android.content.Context;
import android.support.annotation.Nullable;

import com.tcl.framework.db.EntityManager;
import com.tcl.framework.db.EntityManagerFactory;

import cn.download.mie.downloader.DownloadTask;

/**
 * Created by REXZOU on 2015/12/5.
 */
public class DBUtils {

    private volatile static EntityManager<DownloadTask> mdbManager;

    /**
     * 获取下载任务管理的类
     * @param context
     * @param useAccount 为将来可能有多帐号准备
     * @return
     */
    public static EntityManager<DownloadTask> getDownloadTaskManager (Context context, @Nullable String useAccount) {
        if( mdbManager == null)
        synchronized (DBUtils.class) {
            if (mdbManager == null) {
                mdbManager = EntityManagerFactory.getInstance(context, 1, useAccount, null, null).getEntityManager(DownloadTask.class, null);
            }
        }
        return mdbManager;
    }
}
