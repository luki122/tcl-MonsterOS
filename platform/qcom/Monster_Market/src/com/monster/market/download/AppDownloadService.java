package com.monster.market.download;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.monster.market.MarketApplication;
import com.monster.market.R;
import com.monster.market.activity.DownloadManagerActivity;
import com.monster.market.activity.WifiBlockDialogActivity;
import com.monster.market.constants.Constant;
import com.monster.market.db.AppDownloadDao;
import com.monster.market.install.AppInstallService;
import com.monster.market.utils.FileUtil;
import com.monster.market.utils.LogUtil;
import com.monster.market.utils.SettingUtil;
import com.monster.market.utils.SystemUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.Toast;

public class AppDownloadService extends Service {

	public static final String TAG = "AppDownloadService";

	public static final String DOWNLOAD_DATA = "download_data";
	public static final String DWONLOAD_DATA_LIST = "download_data_list";

	public static final String DOWNLOAD_OPERATION = "download_operation"; // 下载操作
	public static final int OPERATION_START_DOWNLOAD = 100; // 开始下载
	public static final int OPERATION_PAUSE_DOWNLOAD = 101; // 暂停下载
	public static final int OPERATION_CONTINUE_DOWNLOAD = 102; // 继续下载
	public static final int OPERATION_PAUSE_CONTINUE_DOWNLOAD = 103; // 继续或暂停下载
	public static final int OPERATION_CANCEL_DOWNLOAD = 104; // 取消下载
	public static final int OPERATION_NETWORK_CHANGE = 105; // 网络改变
	public static final int OPERATION_NETWORK_MOBILE_PAUSE = 106; // 网络改变为手机网络需要暂停
	public static final int OPERATION_NETWORK_MOBILE_CONTINUE = 107; // 网络改变为手机网络需要继续
	public static final int OPERATION_CHECK = 108;	// 检查并打开服务(会把处于下载中的任务全部暂停)
	public static final int OPERATION_START_DOWNLOAD_LIST_CLOUD = 109;	// 从云服务过来的列表下载
	private static final int HANDEL_CHECK = 200; // 处理检查操作

	private static Context context;

	private static Map<String, AppDownloader> downloaders;
	private AppDownloader currentDownloader;
	private AppDownloadData currentDownloadData;
	private static AppDownloadDao appDownloadDao;

	private boolean downloading = false;
	private int noDownloadingSend = 0;
	private boolean notificationFlag = false;
	private int notiId = 0x12345789;
	private Notification notification;

	private static List<DownloadInitListener> initListenerList;
	private static List<DownloadUpdateListener> updateListenerList;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.i(TAG, "AppDownloadService onCreate()");

		if (isNeedInitData()) {
			context = this;
			downloaders = new ConcurrentHashMap<String, AppDownloader>();
			appDownloadDao = new AppDownloadDao(this);
			appDownloadDao.openDatabase();

			// Service被创建时, 先从数据库找到已有任务, 并放入到列表中
			List<String> taskIds = appDownloadDao.getAllTaskId();
			for (String taskId : taskIds) {
				AppDownloadData downloadData = appDownloadDao.getAppDownloadData(taskId);
				int status = appDownloadDao.getStatus(taskId);
				if (status < AppDownloader.STATUS_INSTALL_WAIT) {
					DownloadStatusListener listener = new DownloadStatusListener() {
						@Override
						public void onDownload(String taskId, int status, long downloadSize, long fileSize) {
							// 当下载完成时, 发送下载完成广播
							if (status == AppDownloader.STATUS_INSTALL_WAIT
									|| (downloadSize == fileSize && fileSize != 0)) {
								Intent finish = new Intent(Constant.ACTION_APP_DOWNLOAD_FINISH);
								context.sendBroadcast(finish);
								
								// 开始安装
								AppInstallService.startInstall(context, appDownloadDao.getAppDownloadData(taskId));
							}
						}
					};
					String dirPath = "";
					String dirFromDb = appDownloadDao.getFileDirWithId(taskId);
					if (!TextUtils.isEmpty(dirFromDb)) {
						dirPath = dirFromDb;
					} else {
						dirPath = FileUtil.getAPKFilePath(this);
					}
					AppDownloader downloader = new AppDownloader(downloadData, new File(dirPath), listener);
					downloaders.put(downloadData.getTaskId(), downloader);
				}
			}

			// 告知Service已经加载完成
			if (initListenerList != null) {
				for (DownloadInitListener listener : initListenerList) {
					listener.onFinishInit();
				}
				initListenerList.clear();
				initListenerList = null;
			}
		}

		mHandle.sendEmptyMessage(HANDEL_CHECK);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 获取要操作的数据信息
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				currentDownloadData = (AppDownloadData) bundle.get(DOWNLOAD_DATA);

				// 获取操作指令
				int operation = bundle.getInt(DOWNLOAD_OPERATION);

				if (operation == OPERATION_START_DOWNLOAD_LIST_CLOUD) {
					ArrayList<AppDownloadData> listData = bundle.getParcelableArrayList(DWONLOAD_DATA_LIST);
					handleOperationOther(operation, listData);
				} else {
					handleOperation(operation);
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LogUtil.i(TAG, "service onDestroy!");

		if (initListenerList != null) {
			initListenerList.clear();
		}
		initListenerList = null;

		mHandle.removeMessages(HANDEL_CHECK);
	}

	private Handler mHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDEL_CHECK:
				notificationFlag = !notificationFlag;
				downloading = false;

				List<String> finishKey = new ArrayList<String>(); // 已完成任务的所有key列表

				// 遍历任务列表, 进行相应操作
				for (String key : downloaders.keySet()) {
					AppDownloader downloader = downloaders.get(key);
					// 如果有正在下载的任务, 则把标识设置为true
					int status = downloader.getStatus();
					if (status == AppDownloader.STATUS_DOWNLOADING || status == AppDownloader.STATUS_CONNECTING
							|| status == AppDownloader.STATUS_WAIT || status == AppDownloader.STATUS_CONNECT_RETRY) {
						downloading = true;
						noDownloadingSend = 0;
					}

					// 把已完成的任务key加入到已完成列表中
					if (downloader.getStatus() >= AppDownloader.STATUS_INSTALL_WAIT) {
						finishKey.add(key);
					}
				}
				// 对已完成的任务进行操作
				for (String key : finishKey) {
					downloaders.remove(key);
				}

				// 发送更新广播
				if (downloading) {
					noDownloadingSend = 0;
					updateDownloadProgress();
					// 如果标示为true, 则发送通知
					if (notificationFlag) {
						sendDownloadingNotify();
					}
				} else if (noDownloadingSend < 1) { // 没有任务正在下载, 且广播发送次数小于1时
					noDownloadingSend++;
					updateDownloadProgress();
					cancelDownloadingNotify();
				} else {
					stopSelf();
					return;
				}

				mHandle.sendEmptyMessageDelayed(HANDEL_CHECK, 1000);
				break;
			}
		}
	};

	private void handleOperation(int operation) {
		switch (operation) {
			// 开始下载
			case OPERATION_START_DOWNLOAD:
				// 判断在列表中是否存在这个下载器, 若没有的话, 创建一个下载器
				if (downloaders.containsKey(currentDownloadData.getTaskId())) {
					currentDownloader = downloaders.get(currentDownloadData.getTaskId());
					download(currentDownloader);
				} else {
					File dir = new File(FileUtil.getAPKFilePath(this));
					DownloadStatusListener listener = new DownloadStatusListener() {
						@Override
						public void onDownload(String taskId, int status, long downloadSize, long fileSize) {
							// 当下载完成时, 发送下载完成广播
							if (status == AppDownloader.STATUS_INSTALL_WAIT
									|| (downloadSize == fileSize && fileSize != 0)) {
								Intent finish = new Intent(Constant.ACTION_APP_DOWNLOAD_FINISH);
								context.sendBroadcast(finish);

								// 开始安装
								AppInstallService.startInstall(context, appDownloadDao.getAppDownloadData(taskId));
							}
						}
					};
					AppDownloader AppDownloader = new AppDownloader(currentDownloadData, dir, listener);
					currentDownloader = AppDownloader;
					if (currentDownloader.getStatus() < AppDownloader.STATUS_INSTALL_WAIT) {
						downloaders.put(currentDownloadData.getTaskId(), AppDownloader);
						download(currentDownloader);
					}
				}
				break;
			// 暂停下载
			case OPERATION_PAUSE_DOWNLOAD:
				currentDownloader = downloaders.get(currentDownloadData.getTaskId());
				if (currentDownloader != null) {
					currentDownloader.pause();
				}
				break;
			// 继续下载
			case OPERATION_CONTINUE_DOWNLOAD:
				currentDownloader = downloaders.get(currentDownloadData.getTaskId());
				if (currentDownloader != null) {
					download(currentDownloader);
				}
				break;
			// 暂停或继续下载
			case OPERATION_PAUSE_CONTINUE_DOWNLOAD:
				currentDownloader = downloaders.get(currentDownloadData.getTaskId());
				if (currentDownloader != null) {
					int status = currentDownloader.getStatus();
					if (status == AppDownloader.STATUS_DOWNLOADING || status == AppDownloader.STATUS_CONNECTING
							|| status == AppDownloader.STATUS_WAIT || status == AppDownloader.STATUS_NO_NETWORK
							|| status == AppDownloader.STATUS_CONNECT_RETRY) {
						currentDownloader.pause();
					} else {
						download(currentDownloader);
					}
				}
				updateDownloadProgress();
				break;
			// 取消下载
			case OPERATION_CANCEL_DOWNLOAD:
				currentDownloader = downloaders.get(currentDownloadData.getTaskId());
				if (currentDownloader != null) {
					currentDownloader.cancel();
				} else {
					if (currentDownloadData.getFileName() != null) {
						File file = new File(currentDownloadData.getFileDir(), currentDownloadData.getFileName());
						file.delete();
					}
					appDownloadDao.delete(currentDownloadData.getTaskId());
				}
				updateDownloadProgress();
				break;
			case OPERATION_NETWORK_CHANGE:
				if (SystemUtil.isWifiNetwork(context)) {
					List<AppDownloader> list = new ArrayList<AppDownloader>();
					for (String key : downloaders.keySet()) {
						AppDownloader downloader = downloaders.get(key);
						list.add(downloader);
					}
					sortList(list);
					for (AppDownloader downloader : list) {
						// 找到网络错误状态的任务, 开始进行下载
						if ((downloader.getStatus() == AppDownloader.STATUS_NO_NETWORK)) {
							download(downloader);
						}
					}
				} else { // 网络变为不可用时

					for (String key : downloaders.keySet()) {
						AppDownloader downloader = downloaders.get(key);
						int status = downloader.getStatus();
						if (status == AppDownloader.STATUS_CONNECTING || status == AppDownloader.STATUS_DOWNLOADING
								|| status == AppDownloader.STATUS_WAIT
								|| status == AppDownloader.STATUS_PAUSE_NEED_CONTINUE) {
							downloader.setStatus(AppDownloader.STATUS_NO_NETWORK);
						}
					}
				}
				break;
			case OPERATION_NETWORK_MOBILE_PAUSE:
				int downloadingCount = getDownloadingCountMore();

				for (String key : downloaders.keySet()) {
					AppDownloader downloader = downloaders.get(key);
					int status = downloader.getStatus();
					if (status == AppDownloader.STATUS_CONNECTING || status == AppDownloader.STATUS_DOWNLOADING
							|| status == AppDownloader.STATUS_NO_NETWORK || status == AppDownloader.STATUS_WAIT) {
						downloader.setStatus(AppDownloader.STATUS_PAUSE_NEED_CONTINUE);
					}
				}

				// 是否需要弹出中断对话框
				if (downloadingCount > 0 && SystemUtil.getNetStatus(context) == 2
						&& !SettingUtil.getOnlyWifiDownload(context)) {
					if (SettingUtil.getWifiBlockAlert(context)) {
						showWifiBlockDialog();
					} else {
						// 选择了确定
						if (SettingUtil.getWifiBlockAlertOperation(context)) {
							Intent networkChange = new Intent(AppDownloadService.this, AppDownloadService.class);
							networkChange.putExtra(AppDownloadService.DOWNLOAD_OPERATION,
									AppDownloadService.OPERATION_NETWORK_MOBILE_CONTINUE);
							startService(networkChange);
						} else {	// 选择了取消
							// 不做任何操作
						}
					}
				}

				break;
			case OPERATION_NETWORK_MOBILE_CONTINUE:
				List<AppDownloader> list = new ArrayList<AppDownloader>();
				for (String key : downloaders.keySet()) {
					AppDownloader downloader = downloaders.get(key);
					list.add(downloader);
				}
				sortList(list);

				for (AppDownloader downloader : list) {
					// 找到网络错误状态的任务, 开始进行下载
					if ((downloader.getStatus() == AppDownloader.STATUS_NO_NETWORK)
							|| (downloader.getStatus() == AppDownloader.STATUS_PAUSE_NEED_CONTINUE)) {
						download(downloader);
					}
				}

				break;
			case OPERATION_CHECK:
				for (String key : downloaders.keySet()) {
					AppDownloader downloader = downloaders.get(key);
					int status = downloader.getStatus();
					if (status == AppDownloader.STATUS_DEFAULT
							|| status == AppDownloader.STATUS_WAIT
							||status == AppDownloader.STATUS_CONNECTING
							|| status == AppDownloader.STATUS_DOWNLOADING) {
						downloader.pauseWithInitCheck();
					}
				}

				break;
		}
	}

	private void handleOperationOther(int operation, List<AppDownloadData> listData) {
		switch (operation) {
			case OPERATION_START_DOWNLOAD_LIST_CLOUD:
				if (listData != null && listData.size() > 0) {
					for (AppDownloadData data : listData) {
						if (downloaders.containsKey(data.getTaskId())) {
							AppDownloader downloader = downloaders.get(data.getTaskId());
							download(downloader);
						} else {
							File dir = new File(FileUtil.getAPKFilePath(this));
							DownloadStatusListener listener = new DownloadStatusListener() {
								@Override
								public void onDownload(String taskId, int status, long downloadSize, long fileSize) {
									// 当下载完成时, 发送下载完成广播
									if (status == AppDownloader.STATUS_INSTALL_WAIT
											|| (downloadSize == fileSize && fileSize != 0)) {
										Intent finish = new Intent(Constant.ACTION_APP_DOWNLOAD_FINISH);
										context.sendBroadcast(finish);

										// 开始安装
										AppInstallService.startInstall(context, appDownloadDao.getAppDownloadData(taskId));
									}
								}
							};
							AppDownloader AppDownloader = new AppDownloader(data, dir, listener);
							if (AppDownloader.getStatus() < AppDownloader.STATUS_INSTALL_WAIT) {
								downloaders.put(data.getTaskId(), AppDownloader);
								download(AppDownloader);
							}
						}
					}
				} else {
					LogUtil.i(TAG ,"OPERATION_START_DOWNLOAD_LIST_CLOUD listData null");
				}
				break;
		}
	}

	private void sortList(List<AppDownloader> list) {
		Collections.sort(list, new Comparator<AppDownloader>() {
			@Override
			public int compare(AppDownloader lhs, AppDownloader rhs) {
				int comparison;

				// 对创建时间进行排序
				comparison = (int) (lhs.getCreateTime() - rhs.getCreateTime());
				if (comparison != 0)
					return comparison;

				// 对ApkId进行排序
				return lhs.getAppDownloadData().getTaskId().compareTo(rhs.getAppDownloadData().getTaskId());
			}
		});
	}

	private void download(AppDownloader downloader) {
		// 开始下载
		if (downloader != null) {
			int status = downloader.getStatus();
			if (status == AppDownloader.STATUS_DOWNLOADING || status == AppDownloader.STATUS_CONNECTING
					|| status == AppDownloader.STATUS_WAIT
					/* || status == FileDownloader.STATUS_NO_NETWORK */
					|| status == AppDownloader.STATUS_CONNECT_RETRY) {
				return;
			}
			downloader.downloadFile();
		}
	}

	private static boolean isNeedInitData() {
		if (appDownloadDao == null || downloaders == null || context == null) {
			return true;
		}
		return false;
	}

	private void sendDownloadingNotify() {
		if (notification == null) {
			RemoteViews bigView = new RemoteViews(getApplicationContext().getPackageName(),
					R.layout.notification_download);

			Notification.Builder mNotifyBuilder = new Notification.Builder(this);
			notification = mNotifyBuilder.setSmallIcon(android.R.drawable.stat_sys_download)
					.setOngoing(true)
					.build();

			notification.bigContentView = bigView;
		}
		StringBuffer title = new StringBuffer();
		int downloadSize = 0;
		int fileSize = 0;
		int appCount = 0;
		for (String key : downloaders.keySet()) {
			AppDownloader downloader = downloaders.get(key);
			if (downloader.getStatus() == AppDownloader.STATUS_CONNECTING
					|| downloader.getStatus() == AppDownloader.STATUS_DOWNLOADING) {
				if (title.length() != 0) {
					title.append("，");
				}
				appCount++;
				title.append(downloader.getAppDownloadData().getApkName());
				downloadSize += downloader.getDownloadSize();
				fileSize += downloader.getFileSize();
			}
		}

		notification.bigContentView.setTextViewText(R.id.app_sum,
				appCount + getString(R.string.notification_status_dis_download));
		notification.bigContentView.setTextViewText(R.id.title, title.toString());
		int progress = (int) ((downloadSize * 1.0f) / fileSize * 100);
		notification.bigContentView.setTextViewText(R.id.tv_info, progress + "%");
		notification.bigContentView.setProgressBar(R.id.download_progress_notifi, 100, progress, false);
		Intent intent = new Intent(context, DownloadManagerActivity.class);
		notification.contentIntent = PendingIntent.getActivity(context, notiId, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		if (title.length() != 0) {
			NotificationManager notificationManager = (NotificationManager) getSystemService(
					Context.NOTIFICATION_SERVICE);
			notificationManager.notify(notiId, notification);
		}
	}

	private void cancelDownloadingNotify() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(notiId);
	}

	// =================================已下为public方法=================================//

	public static AppDownloadDao getAppDownloadDao() {
		return appDownloadDao;
	}

	public static Map<String, AppDownloader> getDownloaders() {
		if (null == downloaders) {
			downloaders = new ConcurrentHashMap<String, AppDownloader>();
			if (MarketApplication.getInstance().getApplicationContext() != null) {
				Intent i = new Intent(MarketApplication.getInstance().getApplicationContext(),
						AppDownloadService.class);
				MarketApplication.getInstance().getApplicationContext().startService(i);
			}
		}
		return downloaders;
	}

	/**
	 * 刷新进度
	 * 
	 */
	public static void updateDownloadProgress() {
		LogUtil.i(TAG, "updateDownloadProgress()");
		if (updateListenerList != null) {
			for (DownloadUpdateListener updateListener : updateListenerList) {
				if (updateListener != null) {
					updateListener.downloadProgressUpdate();
				}
			}
		}
	}

	/**
	 * 检查Service数据是否构建完成
	 * 
	 * @param context
	 * @param serviceListener
	 */
	public static void checkInit(Context context, DownloadInitListener serviceListener) {
		if (isNeedInitData()) {
			if (initListenerList == null) {
				initListenerList = new ArrayList<DownloadInitListener>();
			}
			if (serviceListener != null) {
				initListenerList.add(serviceListener);
			}
			Intent initIntent = new Intent(context, AppDownloadService.class);
			initIntent.putExtra(AppDownloadService.DOWNLOAD_OPERATION, AppDownloadService.OPERATION_CHECK);
			context.startService(initIntent);
		} else {
			if (serviceListener != null) {
				serviceListener.onFinishInit();
			}
		}
	}

	/**
	 * 注册刷新监听
	 * 
	 * @param updateListener
	 */
	public static void registerUpdateListener(DownloadUpdateListener updateListener) {
		if (updateListenerList == null) {
			updateListenerList = new ArrayList<DownloadUpdateListener>();
		}
		if (updateListener != null) {
			updateListenerList.add(updateListener);
		}
	}

	/**
	 * 取消刷新监听
	 * 
	 * @param updateListener
	 */
	public static void unRegisterUpdateListener(DownloadUpdateListener updateListener) {
		if (updateListenerList != null && updateListener != null) {
			updateListenerList.remove(updateListener);
		}
	}

	/**
	 * 开始下载请求方法
	 * 
	 * @param context
	 * @param downloadData
	 */
	public static void startDownload(Context context, AppDownloadData downloadData) {
		Intent startDownload = new Intent(context, AppDownloadService.class);
		Bundle startDownloadBundle = new Bundle();
		startDownloadBundle.putParcelable(AppDownloadService.DOWNLOAD_DATA, downloadData);
		startDownloadBundle.putInt(AppDownloadService.DOWNLOAD_OPERATION, AppDownloadService.OPERATION_START_DOWNLOAD);
		startDownload.putExtras(startDownloadBundle);
		context.startService(startDownload);
	}

	/**
	 * 暂停或继续请求方法
	 * 
	 * @param context
	 * @param downloadData
	 */
	public static void pauseOrContinueDownload(Context context, AppDownloadData downloadData) {
		Intent startDownload = new Intent(context, AppDownloadService.class);
		Bundle startDownloadBundle = new Bundle();
		startDownloadBundle.putParcelable(AppDownloadService.DOWNLOAD_DATA, downloadData);
		startDownloadBundle.putInt(AppDownloadService.DOWNLOAD_OPERATION,
				AppDownloadService.OPERATION_PAUSE_CONTINUE_DOWNLOAD);
		startDownload.putExtras(startDownloadBundle);
		context.startService(startDownload);
	}

	/**
	 * 取消任务请求方法
	 * 
	 * @param context
	 * @param downloadData
	 */
	public static void cancelDownload(Context context, AppDownloadData downloadData) {
		LogUtil.i(TAG, "cancelDownload: " + downloadData.getTaskId() + " " + downloadData.getApkName());
		Intent startDownload = new Intent(context, AppDownloadService.class);
		Bundle startDownloadBundle = new Bundle();
		startDownloadBundle.putParcelable(AppDownloadService.DOWNLOAD_DATA, downloadData);
		startDownloadBundle.putInt(AppDownloadService.DOWNLOAD_OPERATION, AppDownloadService.OPERATION_CANCEL_DOWNLOAD);
		startDownload.putExtras(startDownloadBundle);
		context.startService(startDownload);
	}

	/**
	 * 关机暂停正在下载任务
	 */
	public static void pauseAllDownloads() {
		if (downloaders != null && downloaders.size() > 0) {
			for (String key : downloaders.keySet()) {
				AppDownloader downloader = downloaders.get(key);
				downloader.shutdownPause();
			}
		}
	}

	public static int getDownloadingCount() {
		int downloadingCount = 0;
		if (null == downloaders) {
			downloaders = new ConcurrentHashMap<String, AppDownloader>();
			if (MarketApplication.getInstance().getApplicationContext() != null) {
				Intent i = new Intent(MarketApplication.getInstance().getApplicationContext(),
						AppDownloadService.class);
				MarketApplication.getInstance().getApplicationContext().startService(i);
			}
		} else {
			if (downloaders.size() > 0) {
				for (String key : downloaders.keySet()) {
					AppDownloader downloader = downloaders.get(key);
					if (downloader.getStatus() == AppDownloader.STATUS_CONNECTING
							|| downloader.getStatus() == AppDownloader.STATUS_DOWNLOADING) {
						downloadingCount++;
					}
				}
			}
		}
		return downloadingCount;
	}

	public static int getDownloadingCountMore() {
		int downloadingCount = 0;
		if (null == downloaders) {
			downloaders = new ConcurrentHashMap<String, AppDownloader>();
			if (MarketApplication.getInstance().getApplicationContext() != null) {
				Intent i = new Intent(MarketApplication.getInstance().getApplicationContext(),
						AppDownloadService.class);
				MarketApplication.getInstance().getApplicationContext().startService(i);
			}
		} else {
			if (downloaders.size() > 0) {
				for (String key : downloaders.keySet()) {
					AppDownloader downloader = downloaders.get(key);
					if (downloader.getStatus() == AppDownloader.STATUS_CONNECTING
							|| downloader.getStatus() == AppDownloader.STATUS_DOWNLOADING
							|| downloader.getStatus() == AppDownloader.STATUS_PAUSE_NEED_CONTINUE
							|| downloader.getStatus() == AppDownloader.STATUS_CONNECT_RETRY
							|| downloader.getStatus() == AppDownloader.STATUS_NO_NETWORK
							|| downloader.getStatus() == AppDownloader.STATUS_WAIT) {
						downloadingCount++;
					}
				}
			}
		}
		return downloadingCount;
	}

	public static void showToast(String msg) {
		new ToastMessageTask().execute(msg);
	}

	private static class ToastMessageTask extends AsyncTask<String, String, String> {
		String toastMessage;

		@Override
		protected String doInBackground(String... params) {
			toastMessage = params[0];
			return toastMessage;
		}

		protected void OnProgressUpdate(String... values) {
			super.onProgressUpdate(values);
		}
		// This is executed in the context of the main GUI thread
		protected void onPostExecute(String result){
			Toast toast = Toast.makeText(MarketApplication.getInstance(), result, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	public void showWifiBlockDialog() {
		if (context != null) {
			Intent i = new Intent(context, WifiBlockDialogActivity.class);
			context.startActivity(i);
		}
	}

}
