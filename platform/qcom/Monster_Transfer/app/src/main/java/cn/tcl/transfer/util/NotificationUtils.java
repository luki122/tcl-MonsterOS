/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.util;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;

import cn.tcl.transfer.R;
import cn.tcl.transfer.activity.MainActivity;

public class NotificationUtils {

    public static final int NOTIFICATION_ID = 1;
    public static final String MAIN_ACTIVITY = ".activity.MainActivity";
    public static final String MAIN_ACTION = "cn.tcl.transfer.main";
    public static final String PACKAGE_NAME = "cn.tcl.transfer";
    public static final String CLASS_NAME = "cn.tcl.transfer.activity.MainActivity";

    /**
     * Connect success notification
     *
     * @param context
     */
    public static void showConnectSuccessNotification(Context context) {
        Intent intent=new Intent();
        intent.setComponent(new ComponentName(PACKAGE_NAME,CLASS_NAME));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent
                .FLAG_CANCEL_CURRENT);
        showNotification(context, R.drawable.notify, R.string.app_name, R.string
                .notify_connect_success, true, pendingIntent, false);
    }

    /**
     * Sending Notification
     *
     * @param context
     */
    public static void showSendingNotification(Context context) {
        Intent intent=new Intent();
        intent.setComponent(new ComponentName(PACKAGE_NAME,CLASS_NAME));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent
                .FLAG_CANCEL_CURRENT);
        showNotification(context, R.drawable.notify, R.string.app_name, R.string
                .notify_sending, true, pendingIntent, false);
    }

    /**
     * Send Success Notification
     *
     * @param context
     */
    public static void showSendSuccessNotification(Context context) {
        Intent intent=new Intent();
        intent.setComponent(new ComponentName(PACKAGE_NAME,CLASS_NAME));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent
                .FLAG_ONE_SHOT);
        showNotification(context, R.drawable.notify, R.string.app_name, R.string
                .notify_send_success, false, pendingIntent, true);
    }

    /**
     * Send Success Notification
     *
     * @param context
     */
    public static void showDisconnectNotification(Context context) {
        Intent intent=new Intent();
        intent.setComponent(new ComponentName(PACKAGE_NAME,CLASS_NAME));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent
                .FLAG_ONE_SHOT);
        showNotification(context, R.drawable.notify, R.string.app_name, R.string
                .text_disconnected, false, pendingIntent, true);
    }

    /**
     * receive notification
     *
     * @param context
     */
    public static void showReceivingNotification(Context context) {
        Intent intent=new Intent();
        intent.setComponent(new ComponentName(PACKAGE_NAME,CLASS_NAME));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent
                .FLAG_CANCEL_CURRENT);
        showNotification(context, R.drawable.notify, R.string.app_name, R.string
                .notify_receiving, true, pendingIntent, false);

    }

    /**
     * receive notification
     *
     * @param context
     */
    public static void showReceiveFailNotification(Context context) {
        Intent intent=new Intent();
        intent.setComponent(new ComponentName(PACKAGE_NAME,CLASS_NAME));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent
                .FLAG_CANCEL_CURRENT);
        showNotification(context, R.drawable.notify, R.string.app_name, R.string
                .qq_text_receive_fail, true, pendingIntent, false);

    }

    /**
     * receive success notification
     *
     * @param context
     */
    public static void showReceiveSuccessNotification(Context context) {
        Intent intent=new Intent();
        intent.setComponent(new ComponentName(PACKAGE_NAME,CLASS_NAME));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent
                .FLAG_ONE_SHOT);
        showNotification(context, R.drawable.notify, R.string.app_name, R.string
                .notify_receive_success, false, pendingIntent, true);
    }

    public static void showInstallingNotification(Context context) {
        Intent intent=new Intent();
        intent.setComponent(new ComponentName(PACKAGE_NAME,CLASS_NAME));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent
                .FLAG_CANCEL_CURRENT);
        showNotification(context, R.drawable.notify, R.string.app_name, R.string
                .notify_installing, true, pendingIntent, false);
    }

    public static void showRestoringNotification(Context context) {
        Intent intent=new Intent();
        intent.setComponent(new ComponentName(PACKAGE_NAME,CLASS_NAME));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent
                .FLAG_CANCEL_CURRENT);
        showNotification(context, R.drawable.notify, R.string.app_name, R.string
                .text_restore, true, pendingIntent, false);
    }

    public static void showInstallSuccessNotification(Context context) {
        Intent intent=new Intent();
        intent.setComponent(new ComponentName(PACKAGE_NAME,CLASS_NAME));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent
                .FLAG_CANCEL_CURRENT);
        showNotification(context, R.drawable.notify, R.string.app_name, R.string
                .notify_install_success, false, pendingIntent, false);
    }

    public static void cancelNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService
                (Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }


    private static void showNotification(Context context, int iconId, int contentTitleId,
                                         int contentId, boolean isOngoing) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService
                (Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(iconId);
        builder.setContentTitle(context.getString(contentTitleId));
        builder.setContentText(context.getString(contentId));
        builder.setOngoing(isOngoing);
        Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }


    private static void showNotification(Context context, int iconId, int contentTitleId,
                                         int contentId, boolean isOngoing, PendingIntent intent,
                                         boolean isAutoCancel) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService
                (Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(iconId);
        builder.setContentTitle(context.getString(contentTitleId));
        builder.setContentText(context.getString(contentId));
        builder.setContentIntent(intent);
        builder.setAutoCancel(isAutoCancel);
        builder.setOngoing(isOngoing);
        builder.setFullScreenIntent(intent,false);
        Notification notification = builder.build();
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
