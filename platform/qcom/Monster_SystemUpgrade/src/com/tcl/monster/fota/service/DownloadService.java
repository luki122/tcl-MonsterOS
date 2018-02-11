package com.tcl.monster.fota.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.tcl.monster.fota.FotaUIPresenter;
import com.tcl.monster.fota.R;
import com.tcl.monster.fota.downloadengine.DownloadEngine;
import com.tcl.monster.fota.downloadengine.DownloadListener;
import com.tcl.monster.fota.downloadengine.DownloadTask;
import com.tcl.monster.fota.misc.State;
import com.tcl.monster.fota.provider.Fota;
import com.tcl.monster.fota.utils.FotaLog;
import com.tcl.monster.fota.utils.ReportUtil;

/**
 * This class handles download, it interacts with DownloadEngine and Main activity and notifications.
 */
public class DownloadService extends Service {
	/**
	 * TAG for Log 
	 */
	private static final String TAG = DownloadService.class.getSimpleName();

	/**
	 * actions and extras to receive.
	 */
	public static final String ACTION_START_DOWNLOAD = "com.tcl.fota.action.START_DOWNLOAD";
	public static final String ACTION_PAUSE_DOWNLOAD = "com.tcl.fota.action.PAUSE_DOWNLOAD";
	public static final String ACTION_RESUME_DOWNLOAD = "com.tcl.fota.action.RESUME_DOWNLOAD";
	public static final String ACTION_DELETE_DOWNLOAD = "com.tcl.fota.action.DELETE_DOWNLOAD";

    /**
     * This class is a listener which listens download status.
     */
    private DownloadListener mDownloadListener = new DownloadListener() {

        /**
         * This method will be invoked when downloading. Calculate current
         * progress and tell the UI.
         */
        @Override
        public void onDownloadUpdated(DownloadTask task) {
            FotaLog.v(TAG, "DownloadListener -> onDownloadUpdated");
            FotaUIPresenter.getInstance(getApplicationContext()).updateCurrentDownloadTask(task);
        }

        /**
         * When the download completes, this method will be invoked. It tells
         * the FotaMainActivity to change UI. And show a finished notification.
         * More over, it will check if this downloaded package need auto install.
         */
        @Override
        public void onDownloadSuccessed(DownloadTask task) {
            FotaLog.v(TAG, "DownloadListener -> onDownloadSuccessed");
            FotaUIPresenter.getInstance(getApplicationContext()).updateCurrentDownloadTask(task);
            // Report download complete status
            ReportUtil.recordOperation(getApplicationContext(),
                    ReportUtil.OP_DOWNLOAD, ReportUtil.DEFAULT_FOTA_STATUS);
        }

        /**
         * When download is paused, this method will be invoked. Show a toast
         * when network condition is not good or the server is not reachable.
         * And then tells the FotaMainActivity to change UI.
         */
        @Override
        public void onDownloadPaused(DownloadTask task) {
            FotaLog.v(TAG, "DownloadListener -> onDownloadPaused reason: " + task.getPausedReason());
            switch (task.getPausedReason()) {
                case Fota.Firmware.PAUSED_REASON_NETWORK:
                    Toast.makeText(getApplicationContext(),
                            R.string.toast_show_network_ko_for_download,
                            Toast.LENGTH_LONG).show();
                    break;
                case Fota.Firmware.PAUSED_REASON_SERVER_ERROR:
                    Toast.makeText(getApplicationContext(),
                            R.string.toast_show_server_ko_for_download,
                            Toast.LENGTH_LONG).show();
                    break;
                case Fota.Firmware.PAUSED_REASON_USER:
                    break;
            }
            FotaUIPresenter.getInstance(getApplicationContext()).updateCurrentDownloadTask(task);
        }

        /**
         * When download is failed ,this method is invoked. Show a notification
         * and tell the FotaMainActivity to change UI.
         */
        @Override
        public void onDownloadFailed(DownloadTask task) {
            int pauseReason = task.getPausedReason();
            FotaLog.v(TAG, "DownloadListener -> onDownloadFailed reason: " + pauseReason);
            FotaUIPresenter fotaUIPresenter = FotaUIPresenter.getInstance(getApplicationContext());
            if (pauseReason == Fota.Firmware.PAUSED_REASON_NETWORK
                    || pauseReason == Fota.Firmware.PAUSED_REASON_SERVER_ERROR) {
                fotaUIPresenter.updateCurrentDownloadTask(task);
                fotaUIPresenter.showDownloadResult(
                        FotaUIPresenter.FOTA_DOWNLOAD_RESULT_DOWNLOAD_FAIL_NETWORK_REASON);
            } else if(pauseReason == Fota.Firmware.PAUSE_REASON_STORAGE_NOT_ENOUGH) {
                fotaUIPresenter.updateCurrentDownloadTask(task);
                fotaUIPresenter.showDownloadResult(
                        FotaUIPresenter.FOTA_DOWNLOAD_RESULT_STORAGE_NOT_ENOUGH);
            } else {
                fotaUIPresenter.deleteUpdatePackage();
                fotaUIPresenter.showDownloadResult(
                        FotaUIPresenter.FOTA_DOWNLOAD_RESULT_DOWNLOAD_FAIL);
            }
        }

        @Override
        public void onDownloadDeleted(DownloadTask task) {
            FotaLog.d(TAG, "DownloadListener -> onDownloadDeleted");
            FotaUIPresenter.getInstance(getApplicationContext()).deleteTask();
            FotaUIPresenter.getInstance(getApplicationContext()).showDeleteResult(
                    FotaUIPresenter.FOTA_DOWNLOAD_RESULT_DOWNLOAD_DELETED);
        }
    };

    /**
     * This method is invoked when the Service is started. Register network
     * receiver to monitor network status.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        DownloadEngine downloadEngine = DownloadEngine.getInstance();
        downloadEngine.init(this);
    }

    /**
     * This method is invoked when the Service is going to die. unregister
     * network receiver to let it die peacefully.
     */
    public void onDestroy() {
        super.onDestroy();
        FotaUIPresenter.getInstance(this).scheduleRestartDownload();
    }

    /**
     * Currently ongoing task.
     */
    DownloadTask mCurrentTask;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FotaUIPresenter fotaUIPresenter = FotaUIPresenter.getInstance(this);
        if (intent == null) {
            fotaUIPresenter.scheduleRestartDownload();
            return START_NOT_STICKY;
        }
        mCurrentTask = fotaUIPresenter.getCurrentDownloadTask();
        String action = intent.getAction();
        FotaLog.d(TAG, "onStartCommand -> action = " + action);
        if (action == null) {
            return START_NOT_STICKY;
        }
        if (action.equals(ACTION_START_DOWNLOAD)) {
            mCurrentTask.setPausedReason(Fota.Firmware.PAUSED_REASON_NOT_PAUSED);
            handleRealDownload(mCurrentTask);
        } else if (action.equals(ACTION_PAUSE_DOWNLOAD)) {
            pauseDownload(Fota.Firmware.PAUSED_REASON_USER);
        } else if (action.equals(ACTION_RESUME_DOWNLOAD)) {
            mCurrentTask.setPausedReason(Fota.Firmware.PAUSED_REASON_NOT_PAUSED);
            resumeDownload();
        } else if (action.equals(ACTION_DELETE_DOWNLOAD)) {
            deleteDownload();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void handleRealDownload(DownloadTask task) {
        DownloadEngine.getInstance().addDownloadTask(task, mDownloadListener);
    }

    private void resumeDownload() {
        if (mCurrentTask != null) {
            FotaLog.d(TAG, "resumeDownload");
            DownloadEngine.getInstance().resumeDownloadTask(mCurrentTask, mDownloadListener);
        }
    }

    private void pauseDownload(int reason) {
        if (mCurrentTask == null) {
            Toast.makeText(getApplicationContext(), R.string.toast_show_task_missing,
                    Toast.LENGTH_SHORT).show();
        } else {
            FotaLog.d(TAG, "pauseDownload");
            mCurrentTask.setState(State.PAUSING.name());
            mCurrentTask.setPausedReason(reason);
            DownloadEngine.getInstance().pauseDownloadTask(mCurrentTask, mDownloadListener);
        }
    }

    private void deleteDownload() {
        if (mCurrentTask != null) {
            FotaLog.d(TAG, "deleteDownload");
            DownloadEngine.getInstance().deleteDownloadTask(mCurrentTask, mDownloadListener);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}