package com.monster.market.download;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.monster.market.MarketApplication;
import com.monster.market.R;
import com.monster.market.db.AppDownloadDao;
import com.monster.market.http.RequestHelper;
import com.monster.market.utils.FileUtil;
import com.monster.market.utils.LogUtil;
import com.monster.market.utils.SystemUtil;
import com.monster.market.utils.TimeUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.ThreadPoolExecutor;

public class AppDownloader implements Runnable {

	public static final String TAG = "AppDownloader";

	public static final int STATUS_DEFAULT = 0; // 默认
	public static final int STATUS_WAIT = 1; // 等待
	public static final int STATUS_CONNECTING = 2; // 连接
	public static final int STATUS_DOWNLOADING = 3; // 正在下载
	public static final int STATUS_PAUSE = 4; // 暂停
	public static final int STATUS_PAUSE_NEED_CONTINUE = 5; // 暂停需要继续
	public static final int STATUS_NO_NETWORK = 6; // 暂无网络
	public static final int STATUS_CONNECT_TIMEOUT = 7; // 连接超时
	public static final int STATUS_CONNECT_RETRY = 8; // 重试
	public static final int STATUS_FAIL = 9; // 下载失败
	public static final int STATUS_INSTALL_WAIT = 10; // 安装等待
	public static final int STATUS_INSTALLING = 11; // 正在安装
	public static final int STATUS_INSTALLFAILED = 12; // 安装失败
	public static final int STATUS_INSTALLED = 13; // 安装完成

	private AppDownloadData downloadData;
	private String downloadUrl;
	private long downloadSize = 0;
	private long fileSize = 0;
	private String fileSaveDirStr;
	private String fileName;
	private File saveFile;
	private long createTime = 0;

	private int status = STATUS_DEFAULT;
	private boolean cancelFlag = false; // 取消标识
	private boolean retred = false; // 是否已经连接重试
	private boolean needRetry = false; // 是否需要重试
	private boolean preparePause = false; // 是否在准备阶段点下的暂停

	private long speed = 0; // 当前下载速度
	private boolean getSpeedThreadRun = false; // 测速线程是否进行中

	private DownloadStatusListener listener;

	private AppDownloadDao dao;

	public AppDownloader(AppDownloadData downloadData, File fileSaveDir,
						 DownloadStatusListener listener) {
		init(downloadData, fileSaveDir, listener);
	}

	@Override
	public void run() {
		startDownload();
	}

	private void init(AppDownloadData downloadData, File fileSaveDir, DownloadStatusListener listener) {
		LogUtil.i(TAG, "AppDownloader init: taskId-> " + downloadData.getTaskId() +
				" id->" + downloadData.getApkId() + " name->" + downloadData.getApkName()
				+ " packageName->" + downloadData.getPackageName());

		this.downloadData = downloadData;
		this.listener = listener;
		downloadUrl = downloadData.getApkDownloadPath();

		dao = AppDownloadService.getAppDownloadDao();

		if (!fileSaveDir.exists()) {
			fileSaveDir.mkdirs();
		}
		this.fileSaveDirStr = fileSaveDir.getPath();

		if (dao != null) {
			if (dao.isExist(downloadData.getTaskId())) {
				AppDownloadData dataFromDb = dao.getAppDownloadData(downloadData.getTaskId());
				if (!dataFromDb.getApkDownloadPath().equals(downloadData.getApkDownloadPath())
						|| dataFromDb.getVersionCode() != downloadData.getVersionCode()) {
					dao.delete(downloadData.getTaskId());
					File f = new File(dataFromDb.getFileDir(), dataFromDb.getFileName());
					f.delete();
				}
			}

			if (dao.isExist(downloadData.getTaskId())) {
				fileName = dao.getFileName(downloadData.getTaskId());
				if (!TextUtils.isEmpty(fileName)) {
					saveFile = new File(this.fileSaveDirStr, fileName);
				}

				if (saveFile == null || !saveFile.exists()) {
					dao.delete(downloadData.getTaskId());
					createNewRecord();
				} else {
					downloadSize = dao.getDownloadSize(downloadData.getTaskId());
					fileSize = dao.getFileSize(downloadData.getTaskId());
					fileName = dao.getFileName(downloadData.getTaskId());
					status = dao.getStatus(downloadData.getTaskId());
					createTime = dao.getCreateTime(downloadData.getTaskId());
				}
			} else {
				createNewRecord();
			}
		}

		// 豌豆荚下载相关
		String wandoujia_data = "&download_type=" + downloadData.getDownload_type()
				+ "&phone_imei=" + SystemUtil.getImei(MarketApplication.getInstance());
//				+ "&mac_address=" + mac_address +
//				+ "&phone_model=" + Build.MODEL;

		if (!downloadUrl.contains("&pos=")) {
			wandoujia_data += "&pos=" + downloadData.getPos();;
		}

		if (downloadUrl.contains("apps.wandoujia.com")) {
			downloadUrl += wandoujia_data;
		}
	}

	private void createNewRecord() {
		LogUtil.i(TAG, "createNewRecord()");
		createTime = System.currentTimeMillis();
		fileName = getFileName();
		saveFile = null;
		dao.insert(downloadData, createTime, status, fileSize);
		dao.updateFileDirAndName(downloadData.getTaskId(), fileSaveDirStr, fileName);

		// 新建任务上报下载
		RequestHelper.reportDownloadInfo(MarketApplication.getInstance(),
				SystemUtil.buildReportDownloadInfoRequestData(downloadData));
	}

	private String getFileName() {
		String filename = "." + downloadData.getApkName() + "_" + TimeUtil.getTimeString(createTime) + ".apk" + ".tmp";
		return filename;
	}

	private void startDownload() {
		LogUtil.i(TAG, downloadData.getApkName() + "->startDownload()");

		speed = 0;
		needRetry = false;

		if (cancelFlag)
			return;
		
		if (status == STATUS_PAUSE || status == STATUS_NO_NETWORK)
			return;

		if (fileSize <= 0) {
			connectToGetFileSize();
		}
		
		if (fileSize <= 0) {
			if (SystemUtil.hasNetwork()) {
				setStatus(STATUS_FAIL);
			} else {
				setStatus(STATUS_NO_NETWORK);
			}
			dao.updateStatus(downloadData.getTaskId(), status);

			return;
		}

		// 检查存储空间
		if (fileSaveDirStr.contains(Environment.getExternalStorageDirectory().getPath())) {	//sdcard
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());

			// 空间不足
			if ((fileSize - downloadSize) > sf.getAvailableBytes()) {
				setStatus(STATUS_FAIL);
				dao.updateStatus(downloadData.getTaskId(), status);

				AppDownloadService.showToast(MarketApplication.getInstance().getString(R.string.downloadman_memory_is_full));
				return;
			}

		} else if (fileSaveDirStr.contains("/data/data/")) {
			File dataDir = Environment.getDataDirectory();
			StatFs sf = new StatFs(dataDir.getPath());

			if ((fileSize - downloadSize) > sf.getAvailableBytes()) {
				setStatus(STATUS_FAIL);
				dao.updateStatus(downloadData.getTaskId(), status);

				AppDownloadService.showToast(MarketApplication.getInstance().getString(R.string.downloadman_memory_is_full));
				return;
			}
		}

		if (preparePause) {
			preparePause = false;
			setStatus(STATUS_PAUSE);
			return;
		}

		if (saveFile == null) {
			saveFile = new File(fileSaveDirStr, fileName);
		}

		if (!saveFile.exists()) {
			try {
				// 如果是之前下载过,中途文件被删除
				if (downloadSize > 0) {
					downloadSize = 0;
					File fileSaveDir = new File(fileSaveDirStr);
					if (!fileSaveDir.exists()) {
						fileSaveDir.mkdirs();
					}
				}
				saveFile.createNewFile();
			} catch (IOException e) {
				LogUtil.e(TAG, e.toString());
				e.printStackTrace();
			}
		}
		
		setStatus(STATUS_CONNECTING);

		RandomAccessFile accessFile = null;
		URL url = null;
		HttpURLConnection http = null;
		InputStream inputStream = null;
		try {

			url = new URL(downloadUrl);
			http = (HttpURLConnection) url.openConnection();
			http.setConnectTimeout(8 * 1000);
//			http.setDoOutput(true);
			http.setRequestMethod("GET");
			http.setRequestProperty("Range", "bytes=" + downloadSize + "-" + (fileSize - 1));
			http.connect();
			int code = http.getResponseCode();

			accessFile = new RandomAccessFile(saveFile, "rw");
			accessFile.seek(downloadSize);
			
			LogUtil.i(TAG, "code: " + code);

			// 注：加入断点下载返回码为206
			if (code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_PARTIAL	)
			/* && oldHost.equals(targetHost.getHostName()) */ {
				inputStream = http.getInputStream();
				byte[] buffer = new byte[4096];
				int offset = 0;

				getSpeedThreadRun = true;
				new GetSpeedThread().start();

				while ((offset = inputStream.read(buffer)) != -1) {
					if (cancelFlag) {
						break;
					}

					if (status == AppDownloader.STATUS_PAUSE || status == AppDownloader.STATUS_NO_NETWORK
							|| status == AppDownloader.STATUS_CONNECT_TIMEOUT) {
						break;
					}

					if (status != AppDownloader.STATUS_DOWNLOADING) {
						setStatus(AppDownloader.STATUS_DOWNLOADING);
					}

					if (downloadSize >= fileSize) {
						break;
					}

					accessFile.write(buffer, 0, offset);
					downloadSize += offset;

					setRetred(false);
				}
			} else {
				if (SystemUtil.hasNetwork()) {
					setStatus(STATUS_FAIL);
				} else {
					setStatus(STATUS_NO_NETWORK);
				}
			}
		}  catch (IOException e) {
			if (e instanceof SocketTimeoutException) {
				setStatus(STATUS_CONNECT_TIMEOUT);
				if (!retred) {
					retred = true;
					setStatus(STATUS_CONNECT_RETRY);
					needRetry = true;
				} else {
					setRetred(false);
					setStatus(STATUS_FAIL);
				}
			} else {
				// if (e instanceof ConnectException ||
				// e instanceof SocketTimeoutException ||
				// e instanceof UnknownHostException) {
				// }
				if (SystemUtil.hasNetwork()) {
					if (!retred) {
						retred = true;
						setStatus(STATUS_CONNECT_RETRY);
						needRetry = true;
					} else {
						setRetred(false);
						setStatus(STATUS_FAIL);
					}
				} else {
					setStatus(STATUS_NO_NETWORK);
				}
			}
			LogUtil.e(TAG, e.toString());
			e.printStackTrace();
		} finally {
			getSpeedThreadRun = false;
			if (http != null) {
				http.disconnect();
			}

			try {
				if (accessFile != null) {
					accessFile.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e) {
				LogUtil.e(TAG, e.toString());
				e.printStackTrace();
			}
		}

		if (needRetry) {
			startDownload();
			return;
		}

		// 如果已经下载完成, 删除正在下载数据库中数据
		if (downloadSize >= fileSize) {
			File finishFile = new File(fileSaveDirStr, fileName);
			finishFile.renameTo(new File(fileSaveDirStr, fileName.substring(1, fileName.lastIndexOf("."))));
			dao.updateFileName(downloadData.getTaskId(), fileName.substring(1, fileName.lastIndexOf(".")));

			setStatus(STATUS_INSTALL_WAIT);
			dao.updateFileFinishTime(downloadData.getTaskId(), System.currentTimeMillis());
		}

		dao.updateStatus(downloadData.getTaskId(), status);
		dao.updateDownloadSize(downloadData.getTaskId(), downloadSize);

		if (listener != null) {
			listener.onDownload(downloadData.getTaskId(), status, downloadSize, fileSize);
		}
	}

	private long connectToGetFileSize() {
		setStatus(STATUS_CONNECTING);

		URL url = null;
		HttpURLConnection http = null;

		try {
			url = new URL(downloadUrl);
			http = (HttpURLConnection) url.openConnection();
			http.setConnectTimeout(8 * 1000);
			http.setRequestMethod("GET");
			http.connect();
			int code = http.getResponseCode();

			if (code == HttpURLConnection.HTTP_OK
			/* && oldHost.equals(targetHost.getHostName()) */) {

				fileSize = http.getContentLength();

				dao.updateFileSize(downloadData.getTaskId(), fileSize);
			}
		} catch (IOException e) {
			LogUtil.e(TAG, e.toString());
			e.printStackTrace();
		} finally {
			if (http != null) {
				http.disconnect();
			}
		}

		return fileSize;
	}

	public void setStatus(int status) {
		this.status = status;
		dao.updateStatus(downloadData.getTaskId(), status);
	}

	public int getStatus() {
		return status;
	}

	public void setRetred(boolean retred) {
		this.retred = retred;
	}

	public long getDownloadSize() {
		return downloadSize;
	}

	public long getFileSize() {
		return fileSize;
	}

	public AppDownloadData getAppDownloadData() {
		return downloadData;
	}

	public long getCreateTime() {
		return createTime;
	}

	public long getSpeed() {
		return speed;
	}

	private class GetSpeedThread extends Thread {
		@Override
		public void run() {
			speed = 0;
			while (getSpeedThreadRun) {
				long lastSize = downloadSize;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					LogUtil.e(TAG, e.toString());
					e.printStackTrace();
				}
				speed = downloadSize - lastSize;
			}
			LogUtil.i(TAG, "GetSpeedThread end");
		}
	}

	public void downloadFile() {
		setStatus(STATUS_WAIT);

		ThreadPoolExecutor threadPool = AppDownloadThreadPool.getThreadPoolExecutor();
		threadPool.execute(this);
	}

	public void pause() {
		if (status == STATUS_CONNECTING || status == STATUS_DEFAULT) {
			preparePause = true;
		} else {
			preparePause = false;
		}

		status = STATUS_PAUSE;
		dao.updateStatus(downloadData.getTaskId(), status);

		ThreadPoolExecutor threadPool = AppDownloadThreadPool.getThreadPoolExecutor();
		threadPool.remove(this);
	}

	public void pauseWithInitCheck() {
		status = STATUS_PAUSE;
		dao.updateStatus(downloadData.getTaskId(), status);

		ThreadPoolExecutor threadPool = AppDownloadThreadPool.getThreadPoolExecutor();
		threadPool.remove(this);
	}

	public void shutdownPause() {
		dao.updateStatus(downloadData.getTaskId(), STATUS_PAUSE);
		dao.updateDownloadSize(downloadData.getTaskId(), downloadSize);

		if (status == STATUS_CONNECTING || status == STATUS_DEFAULT) {
			preparePause = true;
		} else {
			preparePause = false;
		}
		status = STATUS_PAUSE;
	}

	public void cancel() {
		status = STATUS_PAUSE;
		cancelFlag = true;

		ThreadPoolExecutor threadPool = AppDownloadThreadPool.getThreadPoolExecutor();
		threadPool.remove(this);
		dao.delete(downloadData.getTaskId());
		new Thread() {
			@Override
			public void run() {
				FileUtil.deleteFile(saveFile);
			}
		}.start();

		AppDownloadService.getDownloaders().remove(downloadData.getTaskId());
	}

}
