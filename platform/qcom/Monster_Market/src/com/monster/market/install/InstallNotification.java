package com.monster.market.install;

import java.util.ArrayList;

import com.monster.market.R;
import com.monster.market.activity.AppUpgradeActivity;
import com.monster.market.activity.DownloadManagerActivity;
import com.monster.market.download.AppDownloadData;
import com.monster.market.download.AppDownloader;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.widget.RemoteViews;

public class InstallNotification {

	private static String TAG = "InstallNotification";
	private static Context mContext;

	private static int notiId = 0x23456789;
	private static int notiId_finished = 0x22456789;
	private static int notiId_failed = 0x24456789;
	private static int notiId_update_finished = 0x25456789;
	private static int notiId_update_failed = 0x26456789;
	private static int notiId_can_update = 0x27456789;
//	private static Notification notification;
	private static Notification notification_finished;
	private static Notification notification_failed;
	private static Notification notification_update_finished;
	private static Notification notification_update_failed;
	private static Notification notification_can_update;
	public static ArrayList<String> install_success = new ArrayList<String>();
	public static ArrayList<String> install_failed = new ArrayList<String>();
	public static ArrayList<String> update_success = new ArrayList<String>();
	public static ArrayList<String> update_failed = new ArrayList<String>();

	public static void init(Context context) {
		mContext = context;
	}

	/**
	 * 发送正在安装的广播
	 */
	public static void sendInstallingNotify() {
		Notification.Builder mNotifyBuilder = new Notification.Builder(mContext);
		mNotifyBuilder.setSmallIcon(R.drawable.ic_launcher)
				.setOngoing(true);

		StringBuffer title = new StringBuffer();
		int appCount = 0;
		for (String key : AppInstallService.getInstalls().keySet()) {
			AppInstaller appInstaller = AppInstallService.getInstalls().get(key);

			if (title.length() != 0) {
				title.append("，");
			}
			appCount++;
			title.append(appInstaller.getAppDownloadData().getApkName());
		}

		mNotifyBuilder.setContentTitle(appCount + mContext.getString(R.string.notification_status_dis_install));
		mNotifyBuilder.setContentText(title.toString());
		mNotifyBuilder.setProgress(100, 100, true);

		Intent intent = new Intent(mContext, DownloadManagerActivity.class);

		mNotifyBuilder.setContentIntent(PendingIntent.getActivity(mContext, 1, intent, 0));

		Notification notification = mNotifyBuilder.build();

		if (title.length() != 0) {
			NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(
					Context.NOTIFICATION_SERVICE);
			notificationManager.notify(notiId, notification);
		}


//		if (notification == null) {
//			RemoteViews bigView = new RemoteViews(mContext.getPackageName(),
//					R.layout.notification_install);
//
//			Notification.Builder mNotifyBuilder = new Notification.Builder(mContext);
//			Icon mIcon = Icon.createWithResource(mContext, R.drawable.ic_launcher);
//			notification = mNotifyBuilder.setSmallIcon(mIcon)
//					.setContentTitle("应用正在安装")
//					.setContentText(mContext.getString(R.string.notification_status_click_dis))
//					.setLargeIcon(mIcon)
//					.build();
//
//			notification.bigContentView = bigView;
//
//			// 将此通知放到通知栏的"Ongoing"即"正在运行"组中
//			notification.flags = Notification.FLAG_ONGOING_EVENT;
//			// 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
//			// notification.flags |= Notification.FLAG_NO_CLEAR;
//			notification.flags |= Notification.FLAG_AUTO_CANCEL;
//		}
//		StringBuffer title = new StringBuffer();
//		int downloadSize = 0;
//		int fileSize = 0;
//		int appCount = 0;
//		for (String key : AppInstallService.getInstalls().keySet()) {
//			AppInstaller appInstaller = AppInstallService.getInstalls().get(key);
//
//			if (title.length() != 0) {
//				title.append("，");
//			}
//			appCount++;
//			title.append(appInstaller.getAppDownloadData().getApkName());
//		}
//
//		notification.bigContentView.setTextViewText(R.id.app_sum,
//				appCount + mContext.getString(R.string.notification_status_dis_install));
//		notification.bigContentView.setTextViewText(R.id.title, title.toString());
//
//		Intent intent = new Intent(mContext, DownloadManagerActivity.class);
//
//		notification.contentIntent = PendingIntent.getActivity(mContext, 1, intent, 0);
//		if (title.length() != 0) {
//			NotificationManager notificationManager = (NotificationManager) mContext
//					.getSystemService(Context.NOTIFICATION_SERVICE);
//			notificationManager.notify(notiId, notification);
//		}
	}

	/**
	 * 取消正在安装广播
	 * 
	 */
	public static void cancelInstallingNotify() {
		if (null != mContext) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(notiId);
		}
//		notification = null;
	}

	/**
	 * 发送安装完成的广播
	 * 
	 */
	public static void sendInstalledNotify(String apkname, String packageName) {
		if (notification_finished == null) {
			RemoteViews bigView = new RemoteViews(mContext.getPackageName(),
					R.layout.notification_installed);

			Notification.Builder mNotifyBuilder = new Notification.Builder(mContext);
			Icon mIcon = Icon.createWithResource(mContext, R.drawable.ic_launcher);
			notification_finished = mNotifyBuilder.setSmallIcon(mIcon)
					.setContentTitle("应用安装成功")
					.setContentText(mContext.getString(R.string.notification_status_click_dis))
					.setLargeIcon(mIcon)
					.build();

			notification_finished.bigContentView = bigView;

			install_success.clear();
			install_success.add(apkname);
		}
		StringBuffer title = new StringBuffer();

		int appCount = 0;
		for (String appName : install_success) {

			if (title.length() != 0) {
				title.append("，");
			}
			appCount++;
			title.append(appName);
		}

		if (appCount == 1) {
			notification_finished.bigContentView.setTextViewText(R.id.app_sum,
					title + mContext.getString(R.string.notification_status_installed));
			notification_finished.bigContentView.setTextViewText(R.id.title,
					mContext.getString(R.string.notification_status_click_dis));
		} else {
			notification_finished.bigContentView.setTextViewText(R.id.app_sum,
					appCount + mContext.getString(R.string.notification_status_dis_installed));
			notification_finished.bigContentView.setTextViewText(R.id.title, title.toString());
		}
		Intent intent;
		if (appCount == 1) {
			intent = new Intent(mContext, CleanUpIntent.class);
			intent.setAction("notification_installed_one");
			intent.putExtra("pkgName", packageName);
			notification_finished.contentIntent = PendingIntent.getBroadcast(mContext, 2, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);

		} else {
			intent = new Intent(mContext, DownloadManagerActivity.class);
			intent.putExtra("openinstall", 1);
			notification_finished.contentIntent = PendingIntent.getActivity(mContext, 3, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
		}

		Intent intent1 = new Intent(mContext, CleanUpIntent.class);
		intent1.setAction("notification_installed_cancelled");
		notification_finished.deleteIntent = PendingIntent.getBroadcast(mContext, 4, intent1,
				PendingIntent.FLAG_CANCEL_CURRENT);
		if (title.length() != 0) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(notiId_finished, notification_finished);
		}
	}

	/**
	 * 取消安装完成广播
	 * 
	 */
	public static void cancelInstalledNotify() {
		if (null != mContext) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(notiId_finished);
		}
		install_success.clear();
		notification_finished = null;
	}

	/**
	 * 发送安装失败的广播
	 *
	 */
	public static void sendInstallFailedNotify(String apkname, String packageName) {
		if (notification_failed == null) {
			RemoteViews bigView = new RemoteViews(mContext.getPackageName(),
					R.layout.notification_installed);

			Notification.Builder mNotifyBuilder = new Notification.Builder(mContext);
			Icon mIcon = Icon.createWithResource(mContext, R.drawable.ic_launcher);
			notification_failed = mNotifyBuilder.setSmallIcon(mIcon)
					.setContentTitle("应用安装失败")
					.setContentText("点击查看")
					.setLargeIcon(mIcon)
					.build();

			notification_failed.bigContentView = bigView;
			notification_failed.flags |= Notification.FLAG_AUTO_CANCEL;

			install_failed.clear();
			install_failed.add(apkname);
		}
		StringBuffer title = new StringBuffer();

		int appCount = 0;
		for (String appName : install_failed) {

			if (title.length() != 0) {
				title.append("，");
			}
			appCount++;
			title.append(appName);
		}

		if (appCount == 1) {
			notification_failed.bigContentView.setTextViewText(R.id.app_sum,
					title + mContext.getString(R.string.notification_status_installfailed));
			notification_failed.bigContentView.setTextViewText(R.id.title,
					mContext.getString(R.string.notification_status_click_dis));
		} else {
			notification_failed.bigContentView.setTextViewText(R.id.app_sum,
					appCount + mContext.getString(R.string.notification_status_dis_installfailed));
			notification_failed.bigContentView.setTextViewText(R.id.title, title.toString());
		}

		Intent intent = new Intent(mContext, DownloadManagerActivity.class);
		intent.putExtra("openinstall", 2);
		intent.putExtra("packageName", packageName);
		notification_failed.contentIntent = PendingIntent.getActivity(mContext, 5, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Intent intent1 = new Intent(mContext, CleanUpIntent.class);
		intent1.setAction("notification_failed_cancelled");
		notification_failed.deleteIntent = PendingIntent.getBroadcast(mContext, 6, intent1,
				PendingIntent.FLAG_CANCEL_CURRENT);
		if (title.length() != 0) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(notiId_failed, notification_failed);
		}
	}

	/**
	 * 取消安装失败的广播
	 *
	 */
	public static void cancelInstallFailedNotify() {
		if (null != mContext) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(notiId_failed);
		}
		install_failed.clear();
		notification_failed = null;
	}

	/**
	 * 发送更新完成的广播
	 *
	 */
	public static void sendUpdateInstalledNotify(String apkname, String packageName) {
		if (notification_update_finished == null) {
			RemoteViews bigView = new RemoteViews(mContext.getPackageName(),
					R.layout.notification_installed);

			Notification.Builder mNotifyBuilder = new Notification.Builder(mContext);
			Icon mIcon = Icon.createWithResource(mContext, R.drawable.ic_launcher);
			notification_update_finished = mNotifyBuilder.setSmallIcon(mIcon)
					.setContentTitle("应用更新成功")
					.setContentText(mContext.getString(R.string.notification_status_click_dis))
					.setLargeIcon(mIcon)
					.build();

			notification_update_finished.bigContentView = bigView;

			update_success.clear();
			update_success.add(apkname);
		}
		StringBuffer title = new StringBuffer();

		int appCount = 0;
		for (String appName : update_success) {

			if (title.length() != 0) {
				title.append("，");
			}
			appCount++;
			title.append(appName);
		}

		if (appCount == 1) {
			notification_update_finished.bigContentView.setTextViewText(R.id.app_sum,
					title + mContext.getString(R.string.notification_status_update_installed));
			notification_update_finished.bigContentView.setTextViewText(R.id.title,
					mContext.getString(R.string.notification_status_click_dis));
		} else {
			notification_update_finished.bigContentView.setTextViewText(R.id.app_sum,
					appCount + mContext.getString(R.string.notification_status_dis_update_installed));
			notification_update_finished.bigContentView.setTextViewText(R.id.title, title.toString());
		}
		Intent intent;
		if (appCount == 1) {
			intent = new Intent(mContext, CleanUpIntent.class);
			intent.setAction("notification_update_installed_one");
			intent.putExtra("pkgName", packageName);
			notification_update_finished.contentIntent = PendingIntent.getBroadcast(mContext, 7, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);

		} else {
			intent = new Intent(mContext, DownloadManagerActivity.class);
			intent.putExtra("openinstall", 3);
			notification_update_finished.contentIntent = PendingIntent.getActivity(mContext, 8, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
		}

		Intent intent1 = new Intent(mContext, CleanUpIntent.class);
		intent1.setAction("notification_update_installed_cancelled");
		notification_update_finished.deleteIntent = PendingIntent.getBroadcast(mContext, 9, intent1,
				PendingIntent.FLAG_CANCEL_CURRENT);
		if (title.length() != 0) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(notiId_update_finished, notification_update_finished);
		}
	}

	/**
	 * 取消更新完成广播
	 *
	 */
	public static void cancelUpdateInstalledNotify() {
		if (null != mContext) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(notiId_update_finished);
		}
		update_success.clear();
		notification_update_finished = null;
	}

	/**
	 * 发送安装失败的广播
	 *
	 */
	public static void sendUpdateInstallFailedNotify(String apkname, String packageName) {
		if (notification_update_failed == null) {
			RemoteViews bigView = new RemoteViews(mContext.getPackageName(),
					R.layout.notification_installed);

			Notification.Builder mNotifyBuilder = new Notification.Builder(mContext);
			Icon mIcon = Icon.createWithResource(mContext, R.drawable.ic_launcher);
			notification_update_failed = mNotifyBuilder.setSmallIcon(mIcon)
					.setContentTitle("应用更新失败")
					.setContentText("点击查看")
					.setLargeIcon(mIcon)
					.build();

			notification_update_failed.bigContentView = bigView;
			notification_update_failed.flags |= Notification.FLAG_AUTO_CANCEL;

			update_failed.clear();
			update_failed.add(apkname);
		}
		StringBuffer title = new StringBuffer();

		int appCount = 0;
		for (String appName : update_failed) {

			if (title.length() != 0) {
				title.append("，");
			}
			appCount++;
			title.append(appName);
		}

		if (appCount == 1) {
			notification_update_failed.bigContentView.setTextViewText(R.id.app_sum,
					title + mContext.getString(R.string.notification_status_update_installfailed));
			notification_update_failed.bigContentView.setTextViewText(R.id.title,
					mContext.getString(R.string.notification_status_click_dis));
		} else {
			notification_update_failed.bigContentView.setTextViewText(R.id.app_sum,
					appCount + mContext.getString(R.string.notification_status_dis_update_installfailed));
			notification_update_failed.bigContentView.setTextViewText(R.id.title, title.toString());
		}

		Intent intent = new Intent(mContext, DownloadManagerActivity.class);
		intent.putExtra("openinstall", 4);
		intent.putExtra("packageName", packageName);
		notification_update_failed.contentIntent = PendingIntent.getActivity(mContext, 10, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		Intent intent1 = new Intent(mContext, CleanUpIntent.class);
		intent1.setAction("notification_update_failed_cancelled");
		notification_update_failed.deleteIntent = PendingIntent.getBroadcast(mContext, 11, intent1,
				PendingIntent.FLAG_CANCEL_CURRENT);
		if (title.length() != 0) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.notify(notiId_update_failed, notification_update_failed);
		}
	}

	/**
	 * 取消安装失败的广播
	 *
	 */
	public static void cancelUpdateInstallFailedNotify() {
		if (null != mContext) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(notiId_update_failed);
		}
		update_failed.clear();
		notification_update_failed = null;
	}

	/**
	 * 发送有应用需要更新的广播
	 */
	public static void sendUpdateNotify(ArrayList<AppDownloadData> upLists) {
		if (notification_can_update == null) {
			RemoteViews bigView = new RemoteViews(mContext.getPackageName(),
					R.layout.notification_update);

			Notification.Builder mNotifyBuilder = new Notification.Builder(mContext);
			Icon mIcon = Icon.createWithResource(mContext, R.drawable.ic_launcher);
			notification_can_update = mNotifyBuilder.setSmallIcon(mIcon)
					.setContentText("有可更新应用")
					.setContentText(mContext.getString(R.string.notification_status_click_dis))
					.setLargeIcon(mIcon)
					.build();

			notification_can_update.bigContentView = bigView;
			notification_can_update.flags |= Notification.FLAG_AUTO_CANCEL;

		}
		StringBuffer title = new StringBuffer();

		int appCount = upLists.size();
		for (AppDownloadData listitem : upLists) {

			if (title.length() != 0) {
				title.append("，");
			}
			title.append(listitem.getApkName());
		}

		if (appCount == 1) {
			notification_can_update.bigContentView.setTextViewText(R.id.app_sum,
					title + mContext.getString(R.string.notification_status_update));
			notification_can_update.bigContentView.setTextViewText(R.id.title,
					mContext.getString(R.string.notification_status_click_dis));

		} else {
			notification_can_update.bigContentView.setTextViewText(R.id.app_sum,
					appCount + mContext.getString(R.string.notification_status_dis_update));
			notification_can_update.bigContentView.setTextViewText(R.id.title, title.toString());
		}

		Intent intent1 = new Intent(mContext, DownloadManagerActivity.class);
		Bundle bundle = new Bundle();

		bundle.putParcelableArrayList("updatedata", upLists);

		intent1.putExtras(bundle);

		PendingIntent pentnet = PendingIntent.getActivity(mContext, 12, intent1, PendingIntent.FLAG_UPDATE_CURRENT);

		notification_can_update.bigContentView.setOnClickPendingIntent(R.id.ll_update, pentnet);

		Intent intent = new Intent(mContext, AppUpgradeActivity.class);

		intent.putExtra("update_count", appCount);
		notification_can_update.contentIntent = PendingIntent.getActivity(mContext, 13, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationManager notificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(notiId_can_update, notification_can_update);
	}

	/**
	 * 取消有应用需要更新的广播
	 * 
	 */
	public static void cancelUpdateNotify() {
		if (null != mContext) {
			NotificationManager notificationManager = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(notiId_can_update);
		}
		notification_can_update = null;
	}

}
