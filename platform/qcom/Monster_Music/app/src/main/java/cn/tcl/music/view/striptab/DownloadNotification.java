package cn.tcl.music.view.striptab;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import cn.tcl.music.R;
import cn.tcl.music.activities.DownloadManagerActivity;
import cn.tcl.music.app.MusicApplication;

/**
 * Created by Administrator on 2015/11/26.
 */
public class DownloadNotification {
    public Notification mNotification = null;
    public static DownloadNotification mDownloadNotification;
    Context mContext;
    int mToatal;
    int mDownload;
    NotificationManager mNotificationManager = null;
    private String cancelNotification;

    public static DownloadNotification getInstance() {
        if (mDownloadNotification == null) {
            mDownloadNotification = new DownloadNotification();
            return mDownloadNotification;
        } else {
            return mDownloadNotification;
        }
    }

    public synchronized void setNotification(int toatal, int download, String cancel) {
        cancelNotification = cancel;
        if (toatal != 0) {
            mToatal++;
        }
        if (download != 0) {
            if (mToatal > mDownload) {
                mDownload++;
            }
        }
        mContext = MusicApplication.getApp();
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.drawable.icon_notification);
        mBuilder.setContentTitle(mContext.getString(R.string.app_name));
        mBuilder.setAutoCancel(true);
        mBuilder.setWhen(System.currentTimeMillis());
        Intent intent = new Intent(mContext, DownloadManagerActivity.class);

        if (mToatal == mDownload) {
            if (cancelNotification != null) {
                if (mNotificationManager != null) {
                    mNotificationManager.cancel(0);
                    return;
                }
            }
            mBuilder.setContentText(mContext.getString(R.string.downloaded_notifaction));
            intent.putExtra("page", "downloaded");
            mDownload = 0;
            mToatal = 0;
        } else {
            if(mToatal > mDownload){
                mBuilder.setContentText(String.valueOf(mToatal - mDownload) + mContext.getString(R.string.downloading_notifaction));
                intent.putExtra("page", "downloading");
            }
        }

        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(0, mBuilder.build());
    }

    public void cancleNotifaction(){
        if(mNotificationManager!=null){
            mToatal=0 ;
            mNotificationManager.cancel(0);
        }
    }
}


