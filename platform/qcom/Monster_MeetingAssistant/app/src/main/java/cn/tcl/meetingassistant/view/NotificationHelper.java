/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import cn.tcl.meetingassistant.EditImportPointActivity;
import cn.tcl.meetingassistant.R;

public class NotificationHelper {
    private static final int NOTIFY_ID = 1;

    public static void showNotification(Context context, int iconId, int contentTitleId, int contentId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(iconId);
        //builder.setContentTitle(context.getString(contentTitleId));
        builder.setContentText(context.getString(contentId));
        Intent intent = new Intent(context, EditImportPointActivity.class);
        PendingIntent mPendIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(mPendIntent);
        builder.setAutoCancel(true);
        builder.setOngoing(true);
        notificationManager.notify(NOTIFY_ID, builder.build());
    }

    public static void showNotification(Context context, int content) {
        int iconId = R.mipmap.ic_launcher;
        int contentTitleId = R.string.MeetingAssistant;
        showNotification(context, iconId, contentTitleId, content);
    }

    public static void cancelNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFY_ID);
    }

}
