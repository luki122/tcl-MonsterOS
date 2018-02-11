package com.tcl.monster.fota;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.tcl.monster.fota.receiver.NotificationClickReceiver;
import com.tcl.monster.fota.utils.FotaLog;

public class FotaNotification {
    private static final String TAG = "FotaNotification";

    public static final int FOTA_NOTIFICATION_ID = 100;

    public static final int TYPE_NEW_VERSION = 1;
    public static final int TYPE_DOWNLOADING = 2;
    public static final int TYPE_DOWNLOAD_PAUSED = 3;
    public static final int TYPE_DOWNLOAD_COMPLETE = 4;

    public static void updateFotaNotification(Context context, int type, Object extra) {
        FotaLog.v(TAG, "updateFotaNotification -> type = " + type);

        Resources res = context.getResources();
        final Notification.Builder builder = new Notification.Builder(context);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.drawable.fota_notify);

        Intent intentDownlolad = new Intent(context, FotaMainActivity.class);
        intentDownlolad.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (type == TYPE_NEW_VERSION) {
            builder.setContentTitle(context.getString(R.string.notification_msg_new_update));
            builder.setTicker(res.getString(R.string.notification_msg_new_update));
            builder.setContentText(res.getString(R.string.notification_content_new_update));
            builder.setLights(0xff00ff00, 500, 2000);
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            builder.setAutoCancel(true);
            builder.setShowWhen(true);
            Intent i = new Intent(context, NotificationClickReceiver.class);
            i.setAction(NotificationClickReceiver.ACTION_UPDATE_NOTIFICATION);
            PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, i,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);
        } else if (type == TYPE_DOWNLOADING) {
            Integer progress = (Integer) extra;
            builder.setProgress(100, progress, false);
            FotaLog.d(TAG, "updateFotaNotification -> progress = " + progress);
            builder.setContentTitle(context.getString(R.string.notification_downloading));
            builder.setContentText(String.valueOf(progress) + "%");
            builder.setOngoing(true);
            builder.setAutoCancel(false);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intentDownlolad,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);
        } else if (type == TYPE_DOWNLOAD_PAUSED) {
            Integer progress = (Integer) extra;
            builder.setProgress(100, progress, false);
            FotaLog.d(TAG, "updateFotaNotification -> progress = " + progress);
            builder.setContentTitle(context.getString(R.string.notification_download_paused));
            builder.setContentText(String.valueOf(progress) + "%");
            builder.setOngoing(true);
            builder.setAutoCancel(false);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intentDownlolad,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);
        } else if (type == TYPE_DOWNLOAD_COMPLETE) {
            builder.setContentTitle(context.getString(R.string.notification_download_completed));
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            builder.setOngoing(true);
            builder.setAutoCancel(false);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intentDownlolad,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);
        }

        // Trigger the notification
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(FOTA_NOTIFICATION_ID, builder.build());
    }

    public static void cancelFotaNotification(Context context) {
        FotaLog.v(TAG, "cancelFotaNotification");
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(FOTA_NOTIFICATION_ID);
    }
}