package com.monster.netmanage.utils;

import java.text.SimpleDateFormat;

import com.monster.netmanage.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.RemoteViews;

/**
 * 通知栏信息管理类
 * @author zhaolaichao
 *
 */
public class NotificationUtil {
	 public static final int TYPE_NORMAL = 1;
	 public static final int TYPE_PROGRESS = 2;
	 public static final int TYPE_BIGTEXT = 3;
	 public static final int TYPE_INBOX = 4;
	 public static final int TYPE_BIGPICTURE = 5;
	 public static final int TYPE_HANGUP = 6;
	 public static final int TYPE_MEDIA = 7;
	 public static final int TYPE_CUSTOMER = 8;
	 
	/**
	 * 弹框提示警醒信息
	 * 
	 * @param context
	 * @param tickerInfo
	 * @param title
	 * @param content
	 * @param intent
	 */
	public static void showNotification(Context context, String tickerInfo, String title, String content,	Intent intent) {
		Notification.Builder builder = new Notification.Builder(context);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentTitle(title);
		builder.setContentText(content);
		builder.setAutoCancel(true);
		// 设置状态栏中的小图片
		//builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.netmanage_logo));
		builder.setSmallIcon(R.drawable.ic_notification);
		//点击跳转的intent
        builder.setContentIntent(pendingIntent);
	    builder.setDefaults(NotificationCompat.DEFAULT_ALL);
	   // 设置优先级
	    builder .setPriority(NotificationCompat.PRIORITY_DEFAULT);
	    // 设置通知类别
	    builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
	    Notification notification = builder.build();
	    NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	    notifyManager.notify(TYPE_NORMAL, notification);
	}

	/**
	 * 自定义通知栏模式
	 * @param context
	 * @param tickerInfo
	 * @param title
	 * @param content
	 * @param intent
	 */
	public static void showCustomNotification(Context context, String tickerInfo, String title, String content, Intent intent) {
		// 先设定RemoteViews
		RemoteViews view_custom = new RemoteViews(context.getPackageName(), R.layout.lay_notification);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		String format = sdf.format(System.currentTimeMillis());
		// 设置对应IMAGEVIEW的ID的资源图片
		view_custom.setImageViewResource(R.id.custom_icon, R.drawable.netmanage_logo);
		view_custom.setTextViewText(R.id.tv_custom_title, title);// 设置显示的标题
		view_custom.setTextViewText(R.id.tv_custom_time, format);// 通知产生的时间，会在通知信息里显示 24小时模式
		view_custom.setTextViewText(R.id.tv_custom_content, content);// 消息的详细内容
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder mBuilder = new Builder(context);
		mBuilder.setContent(view_custom)
		        .setSmallIcon(R.drawable.netmanage_logo) // 设置状态栏中的小图片，尺寸一般建议在24×24，
																										// 这里也可以设置大图标
				.setTicker(tickerInfo)// 设置显示的提示文字
				.setContentIntent(pendingIntent) // 关联PendingIntent
				.setNumber(1) // 在TextView的右方显示的数字，可以在外部定义一个变量，点击累加setNumber(count),这时显示的和
				// 设置优先级
				.setPriority(Notification.PRIORITY_DEFAULT)
				// 设置通知类别
				.setCategory(Notification.CATEGORY_MESSAGE);
		Notification notify = mBuilder.build();
		notify.flags |= Notification.FLAG_AUTO_CANCEL;
		notify.defaults = Notification.DEFAULT_ALL;
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(TYPE_CUSTOMER, notify);
	}
	
	/**
	 * 清除通知栏
	 * @param notifyId
	 */
	public static void clearNotify(Context context, int notifyId) {
		NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notifyManager.cancel(notifyId);
	}
	
	public static void showBigTextNotify(Context context, String tickerInfo, String title, String content,	Intent intent) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		android.support.v4.app.NotificationCompat.BigTextStyle style = new android.support.v4.app.NotificationCompat.BigTextStyle();
	    style.setBigContentTitle(title);
	    style.bigText(content);
	    //SummaryText没什么用 可以不设置
	    style.setSummaryText("");
		builder.setStyle(style);
		builder.setAutoCancel(true);
		// 设置状态栏中的小图片
		//builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.netmanage_logo));
		builder.setSmallIcon(R.drawable.ic_notification);
		//点击跳转的intent
        builder.setContentIntent(pendingIntent);
	    builder.setDefaults(NotificationCompat.DEFAULT_ALL);
	   // 设置优先级
	    builder .setPriority(NotificationCompat.PRIORITY_DEFAULT);
	    // 设置通知类别
	    builder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
	    Notification notification = builder.build();
	    NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	    notifyManager.notify(TYPE_BIGTEXT, notification);
	}
}
