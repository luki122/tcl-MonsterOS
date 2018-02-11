package com.monster.market.install;

import android.content.Context;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.monster.market.R;
import com.monster.market.download.AppDownloadData;
import com.monster.market.download.AppDownloadService;
import com.monster.market.download.AppDownloader;
import com.monster.market.http.RequestHelper;
import com.monster.market.http.data.ReportInstallRequestData;
import com.monster.market.utils.LogUtil;
import com.monster.market.utils.SettingUtil;
import com.monster.market.utils.SystemUtil;

import java.io.File;
import java.util.concurrent.ThreadPoolExecutor;

public class AppInstaller implements Runnable {

	public static final String TAG = "AppInstall";
	public static final int HANDLE_UPDATE = 100;
	public static final int HANDLE_SHOW_TOAST = 101;

	private AppDownloadData appDownloadData;
	private Context mContext;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case HANDLE_UPDATE:
				AppDownloadService.updateDownloadProgress();
				break;
			case HANDLE_SHOW_TOAST:
				Toast.makeText(mContext, (String) msg.obj, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	public AppInstaller(AppDownloadData appDownloadData, Context mContext) {
		init(appDownloadData, mContext);
	}

	@Override
	public void run() {
		if ((null != appDownloadData) && (appDownloadData.getStatus() == AppDownloader.STATUS_INSTALL_WAIT)) {

			if (null == AppDownloadService.getAppDownloadDao())
				return;
			AppDownloadService.getAppDownloadDao().updateStatus(appDownloadData.getTaskId(),
					AppDownloader.STATUS_INSTALLING);
			handler.sendEmptyMessage(HANDLE_UPDATE);
			appDownloadData.setStatus(AppDownloader.STATUS_INSTALLING);
		}

		startInstall();
	}

	private void init(AppDownloadData appDownloadData, Context mContext) {
		LogUtil.i(TAG, "Install init: taskId: " + appDownloadData.getTaskId() +
				" id->" + appDownloadData.getApkId() +
				" name->" + appDownloadData.getApkName() + " packageName->" +
				appDownloadData.getPackageName() + " status->" + appDownloadData.getStatus());

		this.appDownloadData = appDownloadData;
		this.mContext = mContext;
	}

	/**
	 * 开始安装
	 *
	 */
	private void startInstall() {
		 InstallNotification.sendInstallingNotify();
		LogUtil.i(TAG, appDownloadData.getApkName() + "->startInstall()");

		String fileDir = appDownloadData.getFileDir();
		fileDir = fileDir == null ? "" : fileDir;
		String fileName = appDownloadData.getFileName();
		fileName = fileName == null ? "" : fileName;

		LogUtil.i(TAG, "fileName=" + fileName + "fileName=" + fileName);
		final File file = new File(fileDir, fileName);
		PackageInstallObserver observer = new PackageInstallObserver();

		int result = SystemUtil.intstallApp(mContext, appDownloadData.getPackageName(), file, observer);
		appDownloadData.setReportInstallType(result);
	}

	class PackageInstallObserver extends IPackageInstallObserver.Stub {
		public void packageInstalled(String packageName, int returnCode) {
			LogUtil.i(TAG, "callback PackageInstallObserver the returnCode=" + returnCode);
			if ((null != appDownloadData) && (appDownloadData.getStatus() == AppDownloader.STATUS_INSTALLING)) {

				AppInstallService.getInstalls().keySet().remove(appDownloadData.getTaskId());
				if (AppInstallService.getInstalls().size() == 0)
					InstallNotification.cancelInstallingNotify();

				String reportState = "";
				if (returnCode != PackageManager.INSTALL_SUCCEEDED) {
					appDownloadData.setStatus(AppDownloader.STATUS_INSTALLFAILED);
					AppDownloadService.getAppDownloadDao().updateStatus(appDownloadData.getTaskId(),
							AppDownloader.STATUS_INSTALLFAILED);

					if (appDownloadData.getReportInstallType() == 0) {    // 0 安装 1 更新
						if (!InstallNotification.install_failed.contains(appDownloadData.getApkName())) {
							InstallNotification.install_failed.add(appDownloadData.getApkName());
							InstallNotification.sendInstallFailedNotify(appDownloadData.getApkName(),
									appDownloadData.getPackageName());
						}
					} else {
						if (!InstallNotification.update_failed.contains(appDownloadData.getApkName())) {
							InstallNotification.update_failed.add(appDownloadData.getApkName());
							InstallNotification.sendUpdateInstallFailedNotify(appDownloadData.getApkName(),
									appDownloadData.getPackageName());
						}
					}

					String msg = appDownloadData.getApkName() + mContext.getString(R.string.downloadman_install_failed);
					reportState = "0";
					handler.sendMessage(handler.obtainMessage(HANDLE_SHOW_TOAST, msg));
				} else {
					if (!SettingUtil.isHold(mContext)) {
						String fileName = appDownloadData.getFileDir() + "/" + appDownloadData.getFileName();
						if (!TextUtils.isEmpty(fileName)) {
							File file = new File(fileName);
							file.delete();
						}
					}
					appDownloadData.setStatus(AppDownloader.STATUS_INSTALLED);
					AppDownloadService.getAppDownloadDao().updateStatus(appDownloadData.getTaskId(),
							AppDownloader.STATUS_INSTALLED);

					if (appDownloadData.getReportInstallType() == 0) {    // 0 安装 1 更新
						if (!InstallNotification.install_success.contains(appDownloadData.getApkName())) {
							InstallNotification.install_success.add(appDownloadData.getApkName());
							InstallNotification.sendInstalledNotify(appDownloadData.getApkName(),
									appDownloadData.getPackageName());
						}
					} else {
						if (!InstallNotification.update_success.contains(appDownloadData.getApkName())) {
							InstallNotification.update_success.add(appDownloadData.getApkName());
							InstallNotification.sendUpdateInstalledNotify(appDownloadData.getApkName(),
									appDownloadData.getPackageName());
						}
					}

					reportState = "1";
				}

				handler.sendEmptyMessage(HANDLE_UPDATE);

				// 上报安装
				ReportInstallRequestData data = new ReportInstallRequestData();
				data.setAppId(appDownloadData.getApkId());
				data.setState(reportState);
				data.setInstallType(String.valueOf(appDownloadData.getReportInstallType()));
				data.setInstallTime(String.valueOf(System.currentTimeMillis()));
				data.setPackageName(appDownloadData.getPackageName());
				RequestHelper.reportInstall(mContext, data);
			}

		}
	}

	/**
	 * 获取DownloadData
	 * 
	 * @return
	 */
	public AppDownloadData getAppDownloadData() {
		return appDownloadData;
	}

	// ============对外控制方法开始=============//

	/**
	 * 下载文件
	 * 
	 */
	public void InstallApp() {
		ThreadPoolExecutor threadPool = AppInstallThreadPool.getThreadPoolExecutor();
		threadPool.execute(this);
	}

}
