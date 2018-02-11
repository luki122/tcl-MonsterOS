package com.monster.market.utils;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.monster.market.R;
import com.monster.market.bean.InstalledAppInfo;
import com.monster.market.constants.Constant;
import com.monster.market.constants.WandoujiaDownloadConstant;
import com.monster.market.download.AppDownloadData;
import com.monster.market.download.AppDownloadService;
import com.monster.market.download.AppDownloader;
import com.monster.market.http.RequestHelper;
import com.monster.market.http.data.ReportDownloadInfoRequestData;
import com.monster.market.install.AppInstallService;
import com.monster.market.install.InstallAppManager;
import com.monster.market.views.ProgressBtn;

import java.io.File;

import mst.app.dialog.AlertDialog;


public class ProgressBtnUtil {
	
	public static final String TAG = "ProgressBtnUtil";
	
	public void updateProgressBtn(ProgressBtn btn, AppDownloadData data) {
		updateProgressBtn(btn, data, null);
	}
	
	public void updateProgressBtn(ProgressBtn btn, AppDownloadData data, OnClickListener onClickListener) {
		// 检测是否安装
		InstalledAppInfo installedAppInfo = InstallAppManager
				.getInstalledAppInfo(btn.getContext(),
						data.getPackageName());

		// 未安装的情况
		if (installedAppInfo == null) {
			AppDownloader downloader = AppDownloadService.getDownloaders()
					.get(data.getTaskId());
			// 如果下载器任务存在, 显示各状态信息
			if (downloader != null) {
				int status = downloader.getStatus();
				if (status == AppDownloader.STATUS_PAUSE
						||status == AppDownloader.STATUS_PAUSE_NEED_CONTINUE) {
					showOperationContinue(btn, downloader);
				} else if (status == AppDownloader.STATUS_DOWNLOADING
						|| status == AppDownloader.STATUS_CONNECTING
						|| status == AppDownloader.STATUS_NO_NETWORK
						|| status == AppDownloader.STATUS_WAIT) {
					showOperationDownloading(btn, downloader);
				} else if (status == AppDownloader.STATUS_FAIL) {
					showOperationRetry(btn, downloader);
				} else if (status == AppDownloader.STATUS_INSTALL_WAIT) {
					showWaitInstall(btn, data);
				} else {
					if (status < AppDownloader.STATUS_INSTALL_WAIT) {
						showOperationDownload(btn, data);
					}
				}
			} else { // 任务完成或者没有记录
				AppDownloadData tempData = AppDownloadService.getAppDownloadDao()
						.getAppDownloadData(data.getTaskId());
				if (tempData == null) {
					showOperationDownload(btn, data);
				} else {
					if (tempData.getVersionCode() == data.getVersionCode()) {
						int status = tempData.getStatus();
						if (status == AppDownloader.STATUS_INSTALL_WAIT) {		// 等待安装
							showWaitInstall(btn, data);
						} else if (status == AppDownloader.STATUS_INSTALLING) {	// 安装中
							showInstalling(btn, data);
						} else if (status == AppDownloader.STATUS_INSTALLFAILED
								|| status == AppDownloader.STATUS_INSTALLED) {	// 安装成功或者安装失败
							
							String id = btn.getTag() == null ? null : (String) btn.getTag();
							if (id != null && id.equals(tempData.getTaskId()) && status == AppDownloader.STATUS_INSTALLED
									&& btn.getStatus() == ProgressBtn.STATUS_PROGRESSING_INSTALLING) {
								return;
							}
							
							String fileDir = tempData.getFileDir();
							fileDir = fileDir == null ? "" : fileDir;
							String fileName = tempData.getFileName();
							fileName = fileName == null ? "" : fileName;
							final File file = new File(fileDir, fileName);
							if (file.exists()) {	// 查看数据库中该任务状态是否为完成, 并且文件是存在的
								InstalledAppInfo info = InstallAppManager
									.getInstalledAppInfo(btn.getContext(),
											data.getPackageName());
								if (info == null) {
									showOperationInstall(btn, data, file);
								} else {
									showOperationOpen(btn, data);
								}
							} else {
								showOperationDownload(btn, data);
							}
						} else {	// 条件不符合则显示下载
							showOperationDownload(btn, data);
						}
					} else {
						showOperationDownload(btn, data);
					}
				}
			}
		} else {
			// 这里判断是否为最新版本
			if (data.getVersionCode() > installedAppInfo.getVersionCode()) { // 不是最新版本
				AppDownloader downloader = AppDownloadService.getDownloaders()
						.get(data.getTaskId());
				// 如果下载器任务存在, 显示各状态信息
				if (downloader != null) {
					int status = downloader.getStatus();
					if (status == AppDownloader.STATUS_PAUSE
							||status == AppDownloader.STATUS_PAUSE_NEED_CONTINUE) {
						showOperationContinue(btn, downloader);
					} else if (status == AppDownloader.STATUS_DOWNLOADING
							|| status == AppDownloader.STATUS_CONNECTING
							|| status == AppDownloader.STATUS_NO_NETWORK
							|| status == AppDownloader.STATUS_WAIT) {
						showOperationDownloading(btn, downloader);
					} else if (status == AppDownloader.STATUS_FAIL) {
						showOperationRetry(btn, downloader);
					} else {
						if (status < AppDownloader.STATUS_INSTALL_WAIT) {
							showOperationUpdate(btn, data, onClickListener);
						}
					}
				} else { // 任务完成或者没有记录
					AppDownloadData tempData = AppDownloadService.getAppDownloadDao()
							.getAppDownloadData(data.getTaskId());
					if (tempData == null) {
						showOperationUpdate(btn, data, onClickListener);
					} else {
						if (tempData.getVersionCode() == data.getVersionCode()) {
							int status = tempData.getStatus();
							if (status == AppDownloader.STATUS_INSTALL_WAIT) {		// 等待安装
								showWaitInstall(btn, data);
							} else if (status == AppDownloader.STATUS_INSTALLING) {	// 安装中
								showInstalling(btn, data);
							}/* else if (status == FileDownloader.STATUS_INSTALLED) {	// 安装成功
								showOperationOpen(btn, data);
							} else if (status == FileDownloader.STATUS_INSTALLFAILED) {	// 安装失败
								String fileDir = tempData.getFileDir();
								fileDir = fileDir == null ? "" : fileDir;
								String fileName = tempData.getFileName();
								fileName = fileName == null ? "" : fileName;
								final File file = new File(fileDir, fileName);
								if (file.exists()) {
									showOperationInstall(btn, data, file);
								} else {
									showOperationUpdate(btn, data, onClickListener);
								}
							}*/ else if (status == AppDownloader.STATUS_INSTALLFAILED
									|| status == AppDownloader.STATUS_INSTALLED) {	// 安装成功或者安装失败
								
								String fileDir = tempData.getFileDir();
								fileDir = fileDir == null ? "" : fileDir;
								String fileName = tempData.getFileName();
								fileName = fileName == null ? "" : fileName;
								final File file = new File(fileDir, fileName);
								if (file.exists()) {	// 查看数据库中该任务状态是否为完成, 并且文件是存在的
									InstalledAppInfo info = InstallAppManager
										.getInstalledAppInfo(btn.getContext(),
												data.getPackageName());
									if (info != null && data.getVersionCode() > info.getVersionCode()) {
										showOperationInstall(btn, data, file);
									} else {
										showOperationOpen(btn, data);
									}
								} else {
									showOperationUpdate(btn, data, onClickListener);
								}
							} else {	// 条件不符合则显示更新
								showOperationUpdate(btn, data, onClickListener);
							}
						} else {
							showOperationUpdate(btn, data, onClickListener);
						}
					}
				}
			} else { // 如果是最新版本
				showOperationOpen(btn, data);
			}
		}
		
		btn.setTag(data.getTaskId());
	}
	
	public void updateFinishProgressBtn(ProgressBtn btn, AppDownloadData data) {
		// 检测是否安装
		InstalledAppInfo installedAppInfo = InstallAppManager
				.getInstalledAppInfo(btn.getContext(),
						data.getPackageName());

		// 未安装的情况
		if (installedAppInfo == null) {
			AppDownloadData tempData = AppDownloadService.getAppDownloadDao()
					.getAppDownloadData(data.getTaskId());
			if (tempData == null) {
				showOperationDownload(btn, data);
			} else {
				if (tempData.getVersionCode() == data.getVersionCode()) {
					int status = tempData.getStatus();
					if (status == AppDownloader.STATUS_INSTALL_WAIT) {		// 等待安装
						showWaitInstall(btn, data);
					} else if (status == AppDownloader.STATUS_INSTALLING) {	// 安装中
						showInstalling(btn, data);
					} else if (status == AppDownloader.STATUS_INSTALLFAILED
							|| status == AppDownloader.STATUS_INSTALLED) {	// 安装成功或者安装失败
						
						String id = btn.getTag() == null ? null : (String) btn.getTag();
						if (id != null && id.equals(tempData.getTaskId()) && status == AppDownloader.STATUS_INSTALLED
								&& btn.getStatus() == ProgressBtn.STATUS_PROGRESSING_INSTALLING) {
							return;
						}
						
						String fileDir = tempData.getFileDir();
						fileDir = fileDir == null ? "" : fileDir;
						String fileName = tempData.getFileName();
						fileName = fileName == null ? "" : fileName;
						final File file = new File(fileDir, fileName);
						showOperationInstall(btn, data, file);
					} else {	// 条件不符合则显示安装
						String fileDir = tempData.getFileDir();
						fileDir = fileDir == null ? "" : fileDir;
						String fileName = tempData.getFileName();
						fileName = fileName == null ? "" : fileName;
						final File file = new File(fileDir, fileName);
						showOperationInstall(btn, data, file);
					}
				} else {
					String fileDir = tempData.getFileDir();
					fileDir = fileDir == null ? "" : fileDir;
					String fileName = tempData.getFileName();
					fileName = fileName == null ? "" : fileName;
					final File file = new File(fileDir, fileName);
					showOperationInstall(btn, data, file);
				}
			}
		} else {
			// 这里判断是否为最新版本
			if (data.getVersionCode() > installedAppInfo.getVersionCode()) { // 不是最新版本
				AppDownloadData tempData = AppDownloadService.getAppDownloadDao()
						.getAppDownloadData(data.getTaskId());
				if (tempData == null) {
					showOperationOpen(btn, data);
				} else {
					if (tempData.getVersionCode() == data.getVersionCode()) {
						int status = tempData.getStatus();
						if (status == AppDownloader.STATUS_INSTALL_WAIT) {		// 等待安装
							showWaitInstall(btn, data);
						} else if (status == AppDownloader.STATUS_INSTALLING) {	// 安装中
							showInstalling(btn, data);
						} else if (status == AppDownloader.STATUS_INSTALLFAILED
								|| status == AppDownloader.STATUS_INSTALLED) {	// 安装成功或者安装失败
							
							String id = btn.getTag() == null ? null : (String) btn.getTag();
							if (id != null && id.equals(tempData.getTaskId()) && status == AppDownloader.STATUS_INSTALLED
									&& btn.getStatus() == ProgressBtn.STATUS_PROGRESSING_INSTALLING) {
								return;
							}
							
							String fileDir = tempData.getFileDir();
							fileDir = fileDir == null ? "" : fileDir;
							String fileName = tempData.getFileName();
							fileName = fileName == null ? "" : fileName;
							final File file = new File(fileDir, fileName);
							showOperationInstall(btn, data, file);
						} else {	// 条件不符合则显示更新
							showOperationOpen(btn, data);
						}
					} else {
						showOperationOpen(btn, data);
					}
				}
			} else { // 如果是最新版本
				showOperationOpen(btn, data);
			}
		}
		
		btn.setTag(data.getTaskId());
	}
	
	
	/**
	 * 显示下载操作
	 * 
	 */
	private void showOperationDownload(final ProgressBtn progressBtn,
			final AppDownloadData downloadData) {
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				AppDownloadService.startDownload(progressBtn.getContext(),
						downloadData);
			}
		};
		
		// ProgressBtn
		progressBtn.setBtnText(progressBtn.getResources().getString(R.string.downloadman_install));
		progressBtn.setStatus(ProgressBtn.STATUS_NORMAL);
		progressBtn.setOnNormalClickListener(clickListener);
		final String taskId = downloadData.getTaskId();
		progressBtn.setOnBeginAnimListener(new ProgressBtn.OnAnimListener() {
			@Override
			public void onEnd(ProgressBtn view) {
				AppDownloader downloader = AppDownloadService.getDownloaders()
						.get(taskId);
				if (downloader != null) {
					int status = downloader.getStatus();
					if (status == AppDownloader.STATUS_CONNECTING
							|| status == AppDownloader.STATUS_DOWNLOADING) {
						view.setStatus(ProgressBtn.STATUS_PROGRESSING_DOWNLOAD);
					}
				}
			}
		});
	}

	/**
	 * 显示更新操作
	 * 
	 */
	private void showOperationUpdate(final ProgressBtn progressBtn,
			final AppDownloadData downloadData, OnClickListener onClickListener) {
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				AppDownloadService.startDownload(progressBtn.getContext(),
						downloadData);
			}
		};
		
		// ProgressBtn
		progressBtn.setBtnText(progressBtn.getResources().getString(R.string.donwloadman_update));
		progressBtn.setStatus(ProgressBtn.STATUS_NORMAL);
		progressBtn.setOnNormalClickListener(clickListener);
		/*if (onClickListener != null) {
			progressBtn.setOnButtonClickListener(onClickListener);
		} else {
			progressBtn.setOnButtonClickListener(null);
		}*/
		final String taskId = downloadData.getTaskId();
		progressBtn.setOnBeginAnimListener(new ProgressBtn.OnAnimListener() {
			@Override
			public void onEnd(ProgressBtn view) {
				AppDownloader downloader = AppDownloadService.getDownloaders()
						.get(taskId);
				if (downloader != null) {
					int status = downloader.getStatus();
					if (status == AppDownloader.STATUS_CONNECTING
							|| status == AppDownloader.STATUS_DOWNLOADING) {
						view.setStatus(ProgressBtn.STATUS_PROGRESSING_DOWNLOAD);
					}
				}
			}
		});
	}

	/**
	 * 显示正在下载
	 * 
	 * @param downloader
	 */
	private void showOperationDownloading(final ProgressBtn progressBtn,
			final AppDownloader downloader) {
		int status = downloader.getStatus();
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				AppDownloadService.pauseOrContinueDownload(
						progressBtn.getContext(), downloader.getAppDownloadData());
			}
		};
		
		// ProgressBtn
		if (status == AppDownloader.STATUS_WAIT) {
			if (!progressBtn.isRuningStartAnim()) {
				progressBtn.setStatus(ProgressBtn.STATUS_WAIT_DOWNLOAD);
			}
		} else {
			if (!progressBtn.isRuningStartAnim()) {
				progressBtn.setStatus(ProgressBtn.STATUS_PROGRESSING_DOWNLOAD);
			}
			long downloadSize = downloader.getDownloadSize();
			long fileSize = downloader.getFileSize();
			int progress = 0;
			if (fileSize != 0) {
				progress = (int) ((downloadSize * 1.0) / fileSize * 100);
			}
			String id =  progressBtn.getTag() == null ? "" : (String) progressBtn.getTag();
			if (!progressBtn.isRuningStartAnim()) {
				if (id != null && id.equals(downloader.getAppDownloadData().getTaskId())) {
					progressBtn.setProgressAnim(progress);
				} else {
					progressBtn.setProgress(progress);
				}
			}
			progressBtn.setOnProgressClickListener(clickListener);
			progressBtn.setProgressBackground(R.drawable.button_stop_selector);
		}
	}

	/**
	 * 显示继续操作
	 * 
	 */
	private void showOperationContinue(final ProgressBtn progressBtn,
			final AppDownloader downloader) {
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (!SettingUtil.canDownload(progressBtn.getContext())) {
					AlertDialog mWifiConDialog = new AlertDialog.Builder(
							progressBtn.getContext())
							.setTitle(
									progressBtn.getContext().getResources().getString(
											R.string.dialog_prompt))
							.setMessage(
									progressBtn.getContext().getResources().getString(
											R.string.no_wifi_download_message))
							.setNegativeButton(android.R.string.cancel, null)
							.setPositiveButton(android.R.string.ok,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											
											SharedPreferences sp = PreferenceManager
													.getDefaultSharedPreferences(progressBtn.getContext());
											Editor ed = sp.edit();
											ed.putBoolean(Constant.SP_WIFI_DOWNLOAD_KEY, false);
											ed.commit();
											doOperationContinue(progressBtn,downloader);
										}

									}).create();
					mWifiConDialog.show();

				} else if (!SystemUtil.hasNetwork()) {
					Toast.makeText(progressBtn.getContext(), progressBtn.getContext()
							.getString(R.string.no_network_download_toast), Toast.LENGTH_SHORT).show();
				} else {
					doOperationContinue(progressBtn, downloader);
				}
			}
		};
		
		// ProgressBtn
		long downloadSize = downloader.getDownloadSize();
		long fileSize = downloader.getFileSize();
		int progress = 0;
		if (fileSize != 0) {
			progress = (int) ((downloadSize * 1.0) / fileSize * 100);
		}
		progressBtn.setStatus(ProgressBtn.STATUS_PROGRESSING_DOWNLOAD);
		progressBtn.setProgress(progress);
		progressBtn.setOnProgressClickListener(clickListener);
		progressBtn.setProgressBackground(R.drawable.button_goon_selector);
	}

	private void doOperationContinue(final ProgressBtn progressBtn,
			final AppDownloader downloader)
	{
		int status = downloader.getStatus();
		if (status == AppDownloader.STATUS_PAUSE_NEED_CONTINUE) {
			AlertDialog mWifiConDialog = new AlertDialog.Builder(
					progressBtn.getContext())
					.setTitle(progressBtn.getContext().getResources().getString(R.string.dialog_prompt))
					.setMessage(progressBtn.getContext().getResources().getString(
									R.string.downloadman_continue_download_by_mobile))
					.setNegativeButton(android.R.string.cancel, null)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										DialogInterface dialog,
										int which) {
									
									AppDownloadService.pauseOrContinueDownload(progressBtn.getContext(),
											downloader.getAppDownloadData());
								}

							}).create();
			mWifiConDialog.show();
		} else {
			AppDownloadService.pauseOrContinueDownload(progressBtn.getContext(),
					downloader.getAppDownloadData());
		}
	}
	
	
	/**
	 * 显示重试操作
	 * 
	 */
	private void showOperationRetry(final ProgressBtn progressBtn,
			final AppDownloader downloader) {
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!SystemUtil.hasNetwork()) {
					Toast.makeText(progressBtn.getContext(), progressBtn.getContext()
							.getString(R.string.no_network_download_toast), Toast.LENGTH_SHORT).show();
				} else {
					AppDownloadService.pauseOrContinueDownload(
							progressBtn.getContext(), downloader.getAppDownloadData());
				}
			}
		};
		
		// ProgressBtn
		long downloadSize = downloader.getDownloadSize();
		long fileSize = downloader.getFileSize();
		int progress = 0;
		if (fileSize != 0) {
			progress = (int) ((downloadSize * 1.0) / fileSize * 100);
		}
		progressBtn.setStatus(ProgressBtn.STATUS_PROGRESSING_DOWNLOAD);
		progressBtn.setProgress(progress);
		progressBtn.setOnProgressClickListener(clickListener);
		progressBtn.setProgressBackground(R.drawable.button_goon_selector);
	}

	/**
	 * 显示安装操作
	 * 
	 * @param downloadData
	 */
	private void showOperationInstall(final ProgressBtn progressBtn,
			final AppDownloadData downloadData, final File file) {
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (file == null || !file.exists()) {
					AlertDialog dialog = new AlertDialog.Builder(
							progressBtn.getContext())
							.setTitle(progressBtn.getContext().getResources().getString(R.string.dialog_prompt))
							.setMessage(progressBtn.getContext().getResources().getString(
											R.string.downloadman_noakkfile_redownload))
							.setNegativeButton(android.R.string.cancel, null)
							.setPositiveButton(android.R.string.ok,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											AppDownloadService.getAppDownloadDao().delete(downloadData.getTaskId());
											AppDownloadService.startDownload(progressBtn.getContext(), downloadData);

//											RequestHelper.reportDownloadInfo(progressBtn.getContext(),
//													SystemUtil.buildReportDownloadInfoRequestData(downloadData));
										}

									}).create();
					dialog.show();
					return;
				}
				
				downloadData.setStatus(AppDownloader.STATUS_INSTALL_WAIT);
				AppDownloadService.getAppDownloadDao().updateStatus(downloadData.getTaskId(),
						AppDownloader.STATUS_INSTALL_WAIT);
				AppDownloadData d = AppDownloadService.getAppDownloadDao().getAppDownloadData(downloadData.getTaskId());
				AppInstallService.startInstall(progressBtn.getContext(), d);
				AppDownloadService.updateDownloadProgress();
			}
		};
		
		// ProgressBtn
		progressBtn.setFoucesBtnText(progressBtn.getResources().getString(R.string.downloadman_install));
		progressBtn.setFouceNormalStyle();
		String id =  progressBtn.getTag() == null ? null : (String) progressBtn.getTag();
		if (id != null && id.equals(downloadData.getTaskId())) {
			if (!progressBtn.isRuningEndAnim()) {
				if (progressBtn.getStatus() == ProgressBtn.STATUS_PROGRESSING_INSTALLING 
						|| progressBtn.getStatus() == ProgressBtn.STATUS_WAIT_INSTALL) {
					progressBtn.startEndAnim(false);
				} else {
					progressBtn.setStatus(ProgressBtn.STATUS_FOUCE_NORMAL);
				}
			}
		} else {
			progressBtn.setStatus(ProgressBtn.STATUS_FOUCE_NORMAL);
		}
		progressBtn.setOnFoucsClickListener(clickListener);
	}
	
	/**
	 * 显示等待安装操作
	 * 
	 * @param downloadData
	 */
	private void showWaitInstall(final ProgressBtn progressBtn,
			final AppDownloadData downloadData) {
		// ProgressBtn
		progressBtn.setStatus(ProgressBtn.STATUS_WAIT_INSTALL);
	}
	
	/**
	 * 显示正在安装操作
	 * 
	 * @param downloadData
	 */
	private void showInstalling(final ProgressBtn progressBtn,
			final AppDownloadData downloadData) {
		LogUtil.i(TAG, downloadData.getApkName() + " showInstalling");
		
		// ProgressBtn
		progressBtn.setStatus(ProgressBtn.STATUS_PROGRESSING_INSTALLING);
	}

	/**
	 * 显示打开操作
	 * 
	 * @param downloadData
	 */
	private void showOperationOpen(final ProgressBtn progressBtn,
			final AppDownloadData downloadData) {
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				ApkUtil.openApp(progressBtn.getContext(),
						downloadData.getPackageName());
			}
		};
		
		// ProgressBtn
		progressBtn.setFoucesBtnText(progressBtn.getResources().getString(R.string.downloadman_open));
		progressBtn.setFouceStyle();
		String id =  progressBtn.getTag() == null ? null : (String) progressBtn.getTag();
		if (id != null && id.equals(downloadData.getTaskId())) {
			if (!progressBtn.isRuningEndAnim()) {
				if (progressBtn.getStatus() == ProgressBtn.STATUS_PROGRESSING_INSTALLING 
						|| progressBtn.getStatus() == ProgressBtn.STATUS_WAIT_INSTALL) {
					progressBtn.startEndAnim(true);
				} else {
					progressBtn.setStatus(ProgressBtn.STATUS_FOUCE);
				}
			}
		} else {
			progressBtn.setStatus(ProgressBtn.STATUS_FOUCE);
		}
		progressBtn.setOnFoucsClickListener(clickListener);
	}

	public static void clearProgressBtnTag(ListView listView, int btnId) {
		if (listView == null) {
			return;
		}

		int count = listView.getChildCount();
		for (int i = 0; i < count; i++) {
			View view = listView.getChildAt(i);
			ProgressBtn progressBtn = (ProgressBtn) view.findViewById(btnId);
			if (progressBtn != null) {
				progressBtn.setTag("");
			}
		}
	}

}
