package com.tcl.monster.fota;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.WindowManager;

import com.tcl.monster.fota.downloadengine.DownloadEngine;
import com.tcl.monster.fota.downloadengine.DownloadTask;
import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.misc.State;
import com.tcl.monster.fota.provider.Fota;
import com.tcl.monster.fota.service.DownloadService;
import com.tcl.monster.fota.service.FotaCheckService;
import com.tcl.monster.fota.service.FotaUpdateService;
import com.tcl.monster.fota.utils.FotaLog;
import com.tcl.monster.fota.utils.FotaPref;
import com.tcl.monster.fota.utils.FotaUtil;
import com.tcl.monster.fota.utils.ReportUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.CountDownLatch;

import mst.app.dialog.AlertDialog;

/**
 * Fota UI Presenter.
 */
public class FotaUIPresenter {
    /**
     * TAG for Log
     */
    private static final String TAG = "FotaUIPresenter";

    /**
     * Default Result Code
     */
    public static final int FOTA_RESULT_TYPE_OK = 0;

    /**
     * Check Result Code
     *
     * FOTA_CHECK_RESULT_NO_NEW_VERSION : no version found when no download
     * FOTA_CHECK_RESULT_GET_NEW_VERSION : find new version when no download
     * FOTA_CHECK_RESULT_DOWNLOAD_VERSION_INVALID : no version found when downloading
     * FOTA_CHECK_RESULT_DOWNLOAD_VERSION_DISCARD : download version > server version
     * FOTA_CHECK_RESULT_DOWNLOAD_VERSION_EXPIRED : download version < server version
     * FOTA_CHECK_RESULT_DOWNLOAD_VERSION_VALID :  download version = server version
     */
    public static final int FOTA_CHECK_RESULT_BEGIN = 1;
    public static final int FOTA_CHECK_RESULT_NO_NEW_VERSION = 1;
    public static final int FOTA_CHECK_RESULT_GET_NEW_VERSION = 2;
    public static final int FOTA_CHECK_RESULT_DOWNLOAD_VERSION_INVALID = 3;
    public static final int FOTA_CHECK_RESULT_DOWNLOAD_VERSION_DISCARD = 4;
    public static final int FOTA_CHECK_RESULT_DOWNLOAD_VERSION_EXPIRED = 5;
    public static final int FOTA_CHECK_RESULT_DOWNLOAD_VERSION_VALID = 6;
    public static final int FOTA_CHECK_RESULT_NO_NETWORK_CONNECTED = 7;
    public static final int FOTA_CHECK_RESULT_CONNECT_TIMEOUT = 8;
    public static final int FOTA_CHECK_RESULT_SERVER_EXCEPTION = 9;
    public static final int FOTA_CHECK_RESULT_CHECK_ERROR = 10;
    public static final int FOTA_CHECK_RESULT_END = 10;

    /**
     * Download Result Code
     */
    public static final int FOTA_DOWNLOAD_RESULT_NO_NETWORK_CONNECTED = 11;
    public static final int FOTA_DOWNLOAD_RESULT_WIFI_WARNING = 12;
    public static final int FOTA_DOWNLOAD_RESULT_STORAGE_NOT_ENOUGH = 13;
    public static final int FOTA_DOWNLOAD_RESULT_STORAGE_NOT_AVAILABLE = 14;
    public static final int FOTA_DOWNLOAD_RESULT_DOWNLOAD_FAIL_NETWORK_REASON = 15;
    public static final int FOTA_DOWNLOAD_RESULT_DOWNLOAD_FAIL = 16;
    public static final int FOTA_DOWNLOAD_RESULT_DOWNLOAD_DELETED = 17;

    /**
     * Install Result Code
     *
     * FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_INVALID : no version found when install
     * FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_DISCARD : download version > server version
     * FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_EXPIRED : download version < server version
     * FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_VALID :  download version = server version or check exception
     */
    public static final int FOTA_INSTALL_RESULT_LOW_BATTERY = 21;
    public static final int FOTA_INSTALL_RESULT_VERIFY_FAIL = 22;
    public static final int FOTA_INSTALL_RESULT_UPDATE_FAIL = 23;
    public static final int FOTA_INSTALL_RESULT_UPDATE_WAIT = 24;
    public static final int FOTA_INSTALL_RESULT_STORAGE_NOT_ENOUGH = 25;
    public static final int FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_INVALID = 26;
    public static final int FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_DISCARD = 27;
    public static final int FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_EXPIRED = 28;
    public static final int FOTA_INSTALL_RESULT_DOWNLOAD_VERSION_VALID = 29;

    /** Dialog type for network state change */
    public static final int DIALOG_TYPE_MOBILE_WARM = 1;
    public static final int DIALOG_TYPE_DOWNLOAD_PAUSED_WARM = 2;

    /**
     * AlertDialog type
     */
    public int dialogWarmType = 0;

    /**
     * AlertDialog for network state change.
     */
    private AlertDialog mDialog;

    /**
     * Application Context.
     */
    private Context mContext;

    /**
     * Attach activity.
     */
    private FotaMainActivity mActivity;

    /**
     * Currently check state.
     */
    private State mCheckState = State.IDLE;

    /**
     * Currently ongoing task.
     */
    private DownloadTask mCurrentTask = null;

    /**
     * Currently ongoing task state.
     */
    private State mCurrentTaskState = State.IDLE;

    /**
     * Lock for resume download.
     */
    private Object resumeLock = new Object();

    /**
     * FotaUIPresenter instance.
     */
    private static FotaUIPresenter sFotaUIPresenter;

    /**
     * Pending result code.
     */
    private int mPendingWarningType = FOTA_RESULT_TYPE_OK;

    /**
     * flag, if delete download task after check
     */
    private boolean mDeleteAfterCheck;

    /**
     * Pending check result code.
     */
    private int mPendingCheckResult = FOTA_RESULT_TYPE_OK;

    /**
     * delete flag
     */
    private volatile boolean mDeleteFlag = false;

    /**
     * Checked DownloadTask.
     */
    private DownloadTask mCheckDownloadTask = null;

    /**
     * Lock for initialization.
     */
    private static final Object mInitializationLock = new Object();

    /**
     * CountDownLatch for initialization.
     */
    private volatile CountDownLatch mInitializationLatch = new CountDownLatch(1);

    /**
     * Singleton
     */
    public static FotaUIPresenter getInstance(Context context) {
        synchronized (mInitializationLock) {
            if (sFotaUIPresenter == null) {
                context = context.getApplicationContext();
                sFotaUIPresenter = new FotaUIPresenter(context);
            }
        }
        return sFotaUIPresenter;
    }

    private FotaUIPresenter(Context context) {
        mContext = context;
        new InitializationTask().execute();
    }

    private class InitializationTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Boolean upgradeFlag = checkUpgradeFlag();
            FotaLog.d(TAG, "InitializationTask doInBackground -> upgradeFlag = " + upgradeFlag);
            if (upgradeFlag) {
                FotaLog.d(TAG, "InitializationTask doInBackground -> Update Done!!!");
                File file = FotaUtil.fetchUpdateStatusFile();
                if (file != null) {
                    verifyUpgradeResult(file);
                }
                FotaUtil.clearUpdateFolder();
                FotaUtil.clearStatus();
                FotaPref.getInstance(mContext).clear();
            }
            DownloadTask task = findCurrentDownloadTask();
            if (task != null && TextUtils.equals(task.getState(), String.valueOf(State.DOWNLOADING))
                    && DownloadEngine.getInstance().checkThreadsMissing(mCurrentTask)) {
                task.setState(State.PAUSED.name());
                task.setPausedReason(Fota.Firmware.PAUSED_REASON_NETWORK);
                Fota.Firmware.updateDownloadTask(mContext.getContentResolver(), task);
                FotaLog.d(TAG, "InitializationTask -> Correct Task State: DOWNLOADING to PAUSED");
            }
            setCurrentDownloadTask(task);
            if (mInitializationLatch != null) {
                mInitializationLatch.countDown();
                mInitializationLatch = null;
            }
            return null;
        }
    }

    public boolean checkUpgradeFlag() {
        File file = FotaUtil.fetchUpdateStatusFile();
        if (file != null) {
            return true;
        } else {
            return false;
        }
    }

    public void verifyUpgradeResult(File sFile) {
        if (sFile == null) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(sFile))) {
            // step 1 parse /cache/recovery/last_fota.status
            String status = null;
            String kind;
            String line = reader.readLine();
            do {
                if (!TextUtils.isEmpty(line) && line.indexOf(':') > 0) {
                    String[] pair = line.split(":", 2);
                    switch (pair[0]) {
                        case FotaUtil.FLAG_UPDATE_RESULT:
                            status = pair[1];
                            break;
                        case FotaUtil.FLAG_UPDATE_KIND:
                            kind = pair[1];
                            break;
                        default:
                            FotaLog.w(TAG, "wrong status file format : " + line);
                            break;
                    }
                }
            } while ((line = reader.readLine()) != null);

            // step 2 detect status to final code which should be sent to server.
            FotaLog.d(TAG, "verifyUpgradeResult -> status = " + status);
            int code = UpgradeResultCode.detectCode(status);
            FotaLog.d(TAG, "verifyUpgradeResult -> code = " + code);

            // step 3 update ui.
            if (code == UpgradeResultCode.CODE_SUCCESS) {
//                notifyUpdateSuccess();
                PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                        .putString(FotaConstants.INSTALLED_DOWNLOAD_ID,
                                FotaPref.getInstance(mContext).getString(FotaConstants.DOWNLOAD_ID, ""))
                        .apply();
            } else {
//                notifyUpdateFailed();
            }

            // step 4 send to upgrade result to server.
            ReportUtil.recordOperation(mContext, ReportUtil.OP_UPGRADE, String.valueOf(code));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sFile.delete();
        }
    }

    private DownloadTask findCurrentDownloadTask() {
        String downloadId = FotaPref.getInstance(mContext).getString(FotaConstants.DOWNLOAD_ID, "");
        FotaLog.d(TAG, "findCurrentDownloadTask -> downloadId = " + downloadId);
        if (TextUtils.isEmpty(downloadId)) {
            return null;
        }
        DownloadTask task;
        DownloadEngine downloadEngine = DownloadEngine.getInstance();
        downloadEngine.init(mContext);
        task = downloadEngine.findDownloadTaskByTaskId(downloadId);
        FotaLog.d(TAG, "findCurrentDownloadTask -> task = " + task);
        return task;
    }

    private void ensureInitializationDone() {
        CountDownLatch latch = mInitializationLatch;
        if (latch == null) {
            return;
        }
        FotaLog.d(TAG, "ensureInitializationDone begin -> " + System.currentTimeMillis());
        while (true) {
            try {
                latch.await();
                FotaLog.d(TAG, "ensureInitializationDone end -> " + System.currentTimeMillis());
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public boolean isInitializationDone() {
        CountDownLatch latch = mInitializationLatch;
        if (latch == null) {
            FotaLog.d(TAG, "isInitializationDone -> true");
            return true;
        }
        FotaLog.d(TAG, "isInitializationDone -> false");
        return false;
    }

    public DownloadTask getCurrentDownloadTask() {
        ensureInitializationDone();
        return mCurrentTask;
    }

    public State getCurrentTaskState() {
        ensureInitializationDone();
        return mCurrentTaskState;
    }

    public void setCurrentDownloadTask(DownloadTask task) {
        if (task == null) {
            mCurrentTaskState = State.IDLE;
        } else {
            mCurrentTaskState = State.valueOf(task.getState());
        }
        mCurrentTask = task;
        FotaLog.d(TAG, "setCurrentDownloadTask -> mCurrentTaskState = " + mCurrentTaskState
                + ", mCurrentTask = " + task);
        if (task != null) {
            int type = -1;
            Object extra = null;
            int p = (int) (100 * task.getCurrentBytes() / task.getTotalBytes());
            switch (mCurrentTaskState) {
                case DOWNLOADING:
                    extra = p;
                    type = FotaNotification.TYPE_DOWNLOADING;
                    break;
                case PAUSED:
                    extra = p;
                    type = FotaNotification.TYPE_DOWNLOAD_PAUSED;
                    break;
                case DOWNLOADED:
                    type = FotaNotification.TYPE_DOWNLOAD_COMPLETE;
                    break;
                default:
                    break;
            }
            FotaLog.d(TAG, "setCurrentDownloadTask -> updateFotaNotification type = " + type
                    + ", progress = " + p);
            if (type != -1) {
                FotaNotification.updateFotaNotification(mContext, type, extra);
            }
        }
    }

    public synchronized void updateCurrentDownloadTask(DownloadTask task) {
        if (task == null) {
            mCurrentTaskState = State.IDLE;
        } else {
            mCurrentTaskState = State.valueOf(task.getState());
        }
        mCurrentTask = task;
        FotaLog.d(TAG, "updateCurrentDownloadTask -> mCurrentTask = " + task
                + ", mCurrentTaskState = " + mCurrentTaskState);
        if (mActivity != null) {
            mActivity.updateDownloadStatus();
        }

        if (task != null) {
            int type = -1;
            Object extra = null;
            int p = (int) (100 * task.getCurrentBytes() / task.getTotalBytes());
            switch (mCurrentTaskState) {
                case DOWNLOADING:
                    extra = p;
                    type = FotaNotification.TYPE_DOWNLOADING;
                    break;
                case PAUSED:
                    extra = p;
                    type = FotaNotification.TYPE_DOWNLOAD_PAUSED;
                    break;
                case DOWNLOADED:
                    type = FotaNotification.TYPE_DOWNLOAD_COMPLETE;
                    break;
                default:
                    break;
            }
            FotaLog.d(TAG, "updateFotaNotification -> type = " + type + ", progress = " + p);
            if (type != -1) {
                FotaNotification.updateFotaNotification(mContext, type, extra);
                if (type == FotaNotification.TYPE_DOWNLOAD_PAUSED) {
                    int reason = task.getPausedReason();
                    if (reason == Fota.Firmware.PAUSED_REASON_NETWORK
                            || reason == Fota.Firmware.PAUSED_REASON_SERVER_ERROR) {
                        showWarmDialog(DIALOG_TYPE_DOWNLOAD_PAUSED_WARM);
                    }
                }
            }
        }
    }

    public boolean haveActvieDownloadTask() {
        FotaLog.d(TAG, "haveActvieDownloadTask -> mCurrentTaskState = " + mCurrentTaskState);
        if (mCurrentTaskState == State.STARTING || mCurrentTaskState == State.DOWNLOADING
                || mCurrentTaskState == State.PAUSING || mCurrentTaskState == State.PAUSED
                || mCurrentTaskState == State.RESUMING || mCurrentTaskState == State.DOWNLOADED
                || mCurrentTaskState == State.INSTALLING) {
            return true;
        } else {
            return false;
        }
    }

    public synchronized void updateCheckState(State state) {
        FotaLog.d(TAG, "updateCheckState -> " + mCheckState + " to " + state);
        if (mCheckState != state) {
            mCheckState = state;
        }
    }

    public synchronized void correctCheckingState() {
        FotaLog.d(TAG, "correctCheckingState mCheckState = " + mCheckState);
        if (mCheckState != State.IDLE) {
            mCheckState = State.IDLE;
        }
    }

    public synchronized void updateCurrentTaskState(State state) {
        FotaLog.d(TAG, "updateCurrentTaskState -> mCurrentTaskState = " + mCurrentTaskState
                + ", updateCurrentTaskState = " + state);
        if (mCurrentTaskState != state) {
            mCurrentTaskState = state;
            if (mActivity != null) {
                mActivity.updateDownloadStatus();
            }
        }
    }

    public void attatchActivity(FotaMainActivity activity) {
        FotaLog.d(TAG, "attatchActivity -> activity = " + activity);
        if (mActivity == activity) {
            return;
        }
        mActivity = activity;
        if (mActivity != null && isInitializationDone()) {
            FotaLog.d(TAG, "attatchActivity -> mPendingWarningType = " + mPendingWarningType);
            if (mPendingWarningType != FOTA_RESULT_TYPE_OK) {
                mActivity.updateResult(mPendingWarningType);
                mPendingWarningType = FOTA_RESULT_TYPE_OK;
            }
        }
    }

    public boolean isMainActivityActive() {
        return mActivity != null;
    }

    public void notifyActivity(int type) {
        if (mActivity != null) {
            mActivity.updateResult(type);
        } else {
            mPendingWarningType = type;
        }
    }

    public void showCheckResult(int type) {
        updateCheckState(State.IDLE);
        notifyActivity(type);
    }

    public void showDownloadResult(int type) {
        if (mCurrentTask == null) {
            updateCurrentTaskState(State.IDLE);
        } else {
            updateCurrentTaskState(State.valueOf(mCurrentTask.getState()));
        }
        notifyActivity(type);
    }

    public void showUpdateResult(int type) {
        if (mCurrentTask == null) {
            updateCurrentTaskState(State.IDLE);
        } else {
            updateCurrentTaskState(State.valueOf(mCurrentTask.getState()));
        }
        notifyActivity(type);
    }

    public void showDeleteResult(int type) {
        mDeleteFlag = false;
        if (mDeleteAfterCheck) {
            setCurrentDownloadTask(mCheckDownloadTask);
            if (mCheckDownloadTask != null) {
                FotaLog.d(TAG, "showDeleteResult -> setTaskId = " + mCheckDownloadTask.getId());
                FotaPref.getInstance(mContext)
                        .setString(FotaConstants.DOWNLOAD_ID, mCheckDownloadTask.getId());
            } else {
                FotaLog.d(TAG, "showDeleteResult -> setTaskId = null");
                FotaPref.getInstance(mContext).setString(FotaConstants.DOWNLOAD_ID, "");
            }
            notifyActivity(mPendingCheckResult);
        } else {
            setCurrentDownloadTask(null);
            notifyActivity(type);
        }
    }

    /**
     * Schedule check updates service.
     * @param chkType
     */
    public void scheduleCheck(String chkType) {
        FotaLog.d(TAG, "scheduleCheck -> chkType = " + chkType + ", mCheckState = " + mCheckState
                + ", mCurrentTaskState = " + mCurrentTaskState );
        ensureInitializationDone();

        if (mCheckState != State.IDLE) {
            FotaLog.d(TAG, "scheduleCheck -> mCheckState is not idle, invalid check");
            return;
        }

        boolean isAutoCheck = chkType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_AUTO);
        FotaApp app = (FotaApp) mContext.getApplicationContext();
        if (isAutoCheck) {
            if (!(mCurrentTaskState == State.IDLE || mCurrentTaskState == State.CHECKED)) {
                FotaLog.d(TAG, "scheduleCheck -> autocheck invalid, mCurrentTaskState = "
                        + mCurrentTaskState);
                return;
            }

            if (app.IsForeground()) {
                FotaLog.d(TAG, "scheduleCheck -> autocheck invalid, app is Foreground");
                return;
            }
        }

        int type = FOTA_RESULT_TYPE_OK;
        if (!FotaUtil.isOnline(mContext)) {
            type = FOTA_CHECK_RESULT_NO_NETWORK_CONNECTED;
        }

        if (type != FOTA_RESULT_TYPE_OK) {
            if (chkType.equals(FotaConstants.FOTA_CHECK_TYPE_VALUE_MANUAL)) {
                showCheckResult(type);
            }
            return;
        }

        fireCheck(chkType);
    }

    private void fireCheck(String chkType) {
        updateCheckState(State.CHECKING);
        Intent checkIntent = new Intent(mContext, FotaCheckService.class);
        checkIntent.setAction(FotaCheckService.ACTION_CHECK);
        checkIntent.putExtra(FotaCheckService.EXTRA_CHECK_TYPE, chkType);
        mContext.startService(checkIntent);

        // Report user action
        ReportUtil.recordOperation(mContext, ReportUtil.OP_CHECK,
                ReportUtil.DEFAULT_FOTA_STATUS);
    }

    /**
     * Schedule start download update package.
     * @param onlyWifi
     */
    public void scheduleStartDownload(boolean onlyWifi) {
        FotaLog.d(TAG, "scheduleStartDownload -> mCurrentTaskState = " + mCurrentTaskState
                + ", onlyWifi = " + onlyWifi);
        if (mCurrentTaskState != State.CHECKED) {
            return;
        }

        int type;
        if (!FotaUtil.isOnline(mContext)) {
            type = FOTA_DOWNLOAD_RESULT_NO_NETWORK_CONNECTED;
        } else if ((!FotaUtil.isWifiOnline(mContext)) && onlyWifi) {
            type = FOTA_DOWNLOAD_RESULT_WIFI_WARNING;
        } else {
            type = createUpdateFile();
        }

        FotaLog.d(TAG, "scheduleStartDownload -> check predownload result = " + type);
        if (type != FOTA_RESULT_TYPE_OK) {
            showDownloadResult(type);
            return;
        }

        int defaultUpdateFrequency = PreferenceManager.getDefaultSharedPreferences(mContext).
                getInt(FotaConstants.DEFAULT_UPDATE_CHECK_PREF, FotaUtil.getDefaultAutoCheckVal());
        FotaUtil.setCheckFrequency(mContext, defaultUpdateFrequency);

        FotaNotification.cancelFotaNotification(mContext);
        fireStartOrResumeDownload(DownloadService.ACTION_START_DOWNLOAD);
    }

    private int createUpdateFile() {
        String path = FotaUtil.chooseSaveLocation(mContext, mCurrentTask);
        FotaLog.d(TAG, "createUpdateFile -> path = " + path);

        int type = FOTA_RESULT_TYPE_OK;
        if (path.equals(FotaConstants.NO_AVAILABLE_STORAGE)
                || FotaConstants.STORAGE_NOT_AVAILABLE.equals(path)) {
            type = FOTA_DOWNLOAD_RESULT_STORAGE_NOT_AVAILABLE;
        } else if (FotaConstants.STORAGE_SPACE_NOT_ENOUGH.equals(path)) {
            type = FOTA_DOWNLOAD_RESULT_STORAGE_NOT_ENOUGH;
        } else {
            FotaPref.getInstance(mContext).setString(FotaConstants.PATH_SAVING_UPDATE_PACKAGE, path);
            FotaUtil.makeUpdateFolder(path);
            File file = FotaUtil.updateZip();
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                type = FOTA_DOWNLOAD_RESULT_STORAGE_NOT_ENOUGH;
            }
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(file, "rw");
                raf.setLength(mCurrentTask.getTotalBytes());
            } catch (FileNotFoundException e) {
                type = FOTA_DOWNLOAD_RESULT_STORAGE_NOT_ENOUGH;
                e.printStackTrace();
            } catch (IOException e) {
                type = FOTA_DOWNLOAD_RESULT_STORAGE_NOT_ENOUGH;
                e.printStackTrace();
            } finally {
                try {
                    raf.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return type;
    }

    private void fireStartOrResumeDownload(String action) {
        FotaLog.d(TAG, "fireStartOrResumeDownload -> action = " + action
                + ", mCurrentTaskState = " + mCurrentTaskState);
        if (TextUtils.equals(action, DownloadService.ACTION_START_DOWNLOAD)) {
            String path = FotaPref.getInstance(mContext).getString(
                    FotaConstants.PATH_SAVING_UPDATE_PACKAGE, "");
            if (TextUtils.isEmpty(path)) {
                int type = createUpdateFile();
                if (type != FOTA_RESULT_TYPE_OK) {
                    showDownloadResult(type);
                    return;
                }
            }
            updateCurrentTaskState(State.STARTING);
        } else {
            updateCurrentTaskState(State.RESUMING);
            if (mDialog != null && mDialog.isShowing()) {
                dialogWarmType = 0;
                mDialog.dismiss();
            }
        }
        Intent downloadintent = new Intent(mContext, DownloadService.class);
        downloadintent.setAction(action);
        mContext.startService(downloadintent);
        // Report user action
        ReportUtil.recordOperation(mContext, ReportUtil.OP_STARTDOWNLOAD_POP,
                ReportUtil.DEFAULT_FOTA_STATUS);
    }

    /**
     * Schedule resume download update package.
     * @param onlyWifi
     */
    public void scheduleResumeDownload(boolean onlyWifi) {
        FotaLog.d(TAG, "scheduleResumeDownload -> " + "onlyWifi = " + onlyWifi
                + ", mCurrentTaskState = " + mCurrentTaskState);

        if (mCurrentTaskState != State.PAUSED) {
            FotaLog.d(TAG, "scheduleResumeDownload -> Warning!!! mCurrentTaskState = " + mCurrentTaskState);
            return;
        }

        int type = FOTA_RESULT_TYPE_OK;
        if (!FotaUtil.isOnline(mContext)) {
            type = FOTA_DOWNLOAD_RESULT_NO_NETWORK_CONNECTED;
        } else if ((!FotaUtil.isWifiOnline(mContext)) && onlyWifi) {
            type = FOTA_DOWNLOAD_RESULT_WIFI_WARNING;
        }

        if (type != FOTA_RESULT_TYPE_OK) {
            showDownloadResult(type);
            return;
        }

        fireStartOrResumeDownload(DownloadService.ACTION_RESUME_DOWNLOAD);
    }

    /**
     * Show network changed warning dialog when downloading.
     */
    public void showNetWorkChangedWarning() {
        ensureInitializationDone();
        if (mCurrentTask == null || mDeleteFlag) {
            FotaLog.d(TAG, "showNetWorkChangedWarning -> Deleteing!!!, not need show warning!!!");
            return;
        }

        FotaLog.d(TAG, "showNetWorkChangedWarning -> mCurrentTaskState = " + mCurrentTaskState
                + ", PausedReason = " + mCurrentTask.getPausedReason()
                + ", isOnline = " + FotaUtil.isOnline(mContext)
                + ", isMobileOnline = " + FotaUtil.isMobileOnline(mContext)
                + ", isWifiOnline = " + FotaUtil.isWifiOnline(mContext));

        if (mCurrentTaskState != State.DOWNLOADING && mCurrentTaskState != State.RESUMING) {
            return;
        }

        if (!FotaUtil.isWifiOnline(mContext) && mCurrentTask != null) {
            DownloadEngine.getInstance().pauseDownloadTaskbyNet(mCurrentTask);
            mCurrentTask.setPausedReason(Fota.Firmware.PAUSED_REASON_NOT_PAUSED);
            if (FotaUtil.isMobileOnline(mContext)) {
                showWarmDialog(DIALOG_TYPE_MOBILE_WARM);
            } else {
                showWarmDialog(DIALOG_TYPE_DOWNLOAD_PAUSED_WARM);
            }
        }
    }

    /**
     * Show network disconnected warning dialog when downloading.
     */
    public void showNetworkDisconnectedWarning() {
        if (mCurrentTask == null || mDeleteFlag) {
            FotaLog.d(TAG, "showNetworkDisconnectedWarning -> Deleteing!!!, not need show warning!!!");
            return;
        }

        if (dialogWarmType == DIALOG_TYPE_MOBILE_WARM && mDialog != null && mDialog.isShowing()) {
            dialogWarmType = 0;
            mDialog.dismiss();
        }

        FotaLog.d(TAG, "showNetworkDisconnectedWarning -> mCurrentTaskState = " + mCurrentTaskState);
        if (mCurrentTaskState == State.DOWNLOADING) {
            DownloadEngine.getInstance().pauseDownloadTaskbyNet(mCurrentTask);
            showWarmDialog(DIALOG_TYPE_DOWNLOAD_PAUSED_WARM);
        }
    }

    private void showWarmDialog(int type) {
        FotaLog.d(TAG, "showWarmDialog -> dialogWarmType = " + dialogWarmType + ", type = " + type);
        if (mDialog != null && mDialog.isShowing()) {
            dialogWarmType = 0;
            mDialog.dismiss();
        }

        if (type == DIALOG_TYPE_MOBILE_WARM) {
            dialogWarmType = DIALOG_TYPE_MOBILE_WARM;
            mDialog = new AlertDialog.Builder(mContext)
                    .setTitle(R.string.dialog_title_warm)
                    .setMessage(R.string.dialog_msg_network_warn)
                    .setPositiveButton(R.string.start_download,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    fireStartOrResumeDownload(DownloadService.ACTION_RESUME_DOWNLOAD);
                                }
                            }
                    )
                    .setNegativeButton(R.string.cancel_download,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    mDialog.dismiss();
                                }
                            }
                    ).setCancelable(false)
                    .create();
        } else if (type == DIALOG_TYPE_DOWNLOAD_PAUSED_WARM) {
            dialogWarmType = DIALOG_TYPE_DOWNLOAD_PAUSED_WARM;
            mDialog = new AlertDialog.Builder(mContext)
                    .setTitle(R.string.dialog_title_warm)
                    .setMessage(R.string.dialog_msg_network_disconnected)
                    .setPositiveButton(R.string.dialog_msg_download_setting_network,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    mContext.startActivity(intent);
                                }
                            }
                    )
                    .setNegativeButton(R.string.cancel_download,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    mDialog.dismiss();
                                }
                            }
                    ).setCancelable(false)
                    .create();
        }
        mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        mDialog.show();
    }

    public void dismissWarmDialog(int type) {
        FotaLog.d(TAG, "dismissWarmDialog -> dialogWarmType = " + dialogWarmType + ", type = " + type);
        if (dialogWarmType == type && mDialog != null && mDialog.isShowing()) {
            dialogWarmType = 0;
            mDialog.dismiss();
        }
    }

    /**
     * Schedule pause download update package.
     */
    public void schedulePauseDownload() {
        FotaLog.d(TAG, "schedulePauseDownload -> mCurrentTaskState = " + mCurrentTaskState);
        if (mCurrentTaskState != State.STARTING && mCurrentTaskState != State.DOWNLOADING) {
            return;
        }
        updateCurrentTaskState(State.PAUSING);
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.setAction(DownloadService.ACTION_PAUSE_DOWNLOAD);
        mContext.startService(intent);
        // Report user action
        ReportUtil.recordOperation(mContext, ReportUtil.OP_PAUSEDOWNLOAD,
                ReportUtil.DEFAULT_FOTA_STATUS);
    }

    /**
     * Schedule restart download update package.
     */
    public void scheduleRestartDownload() {
        ensureInitializationDone();
        if (mCurrentTask != null) {
            FotaLog.d(TAG, "scheduleRestartDownload -> mCurrentTask = " + mCurrentTask +
                    "mCurrentTask.getState() = " + mCurrentTask.getState());
        }
        if ((mCurrentTask != null) && (State.valueOf(mCurrentTask.getState()) == State.DOWNLOADING)
                && DownloadEngine.getInstance().checkThreadsMissing(mCurrentTask)) {
            fireStartOrResumeDownload(DownloadService.ACTION_RESUME_DOWNLOAD);
        }
    }

    /**
     * Check if download task need resume when network is connected.
     */
    public void scheduleCheckNeedResume(boolean wifiSwitchMobile) {
        ensureInitializationDone();
        FotaLog.d(TAG, "scheduleCheckNeedResume -> mCurrentTask = " + mCurrentTask
                + ", mCurrentTaskState = " + mCurrentTaskState);
        if (mCurrentTask == null) {
            return;
        }
        if (mDialog != null && mDialog.isShowing()) {
            dialogWarmType = 0;
            mDialog.dismiss();
        }
        boolean needResume = false;
        if (TextUtils.equals(mCurrentTask.getState(), String.valueOf(State.DOWNLOADING))
                && DownloadEngine.getInstance().checkThreadsMissing(mCurrentTask)) {
            needResume = true;
        }
        if (TextUtils.equals(mCurrentTask.getState(), String.valueOf(State.PAUSED))
                && mCurrentTask.getPausedReason() != Fota.Firmware.PAUSED_REASON_USER) {
            needResume = true;
        }
        if (mCurrentTaskState == State.RESUMING) {
            FotaLog.d(TAG, "scheduleCheckNeedResume -> already resuming!!!!!!");
            needResume = false;
        }
        FotaLog.d(TAG, "scheduleCheckNeedResume -> taskState = " + mCurrentTask.getState()
                + ", PausedReason = " + mCurrentTask.getPausedReason()
                + ", needResume = " + needResume);
        if (needResume) {
            FotaLog.d(TAG, "scheduleCheckNeedResume -> updateDownloadTask to paused status");
            mCurrentTask.setState(State.PAUSED.name());
            mCurrentTask.setPausedReason(Fota.Firmware.PAUSED_REASON_NETWORK);
            Fota.Firmware.updateDownloadTask(mContext.getContentResolver(), mCurrentTask);
            updateCurrentTaskState(State.PAUSED);
        }
        synchronized (resumeLock) {
            if (needResume && mCurrentTaskState == State.PAUSED) {
                if (FotaUtil.isWifiOnline(mContext) && FotaUtil.isAutoDownload(mContext)) {
                    scheduleResumeDownload(true);
                } else if (wifiSwitchMobile) {
                    FotaLog.d(TAG, "scheduleCheckNeedResume -> switchToMobile");
                    showWarmDialog(DIALOG_TYPE_MOBILE_WARM);
                }
            }
        }
    }

    public void deleteUpdatePackage() {
        ReportUtil.recordOperation(mContext, ReportUtil.OP_DELETEPACKAGE,
                ReportUtil.DEFAULT_FOTA_STATUS);
        new Thread(mResetTask).start();
        // Report delete action
        setCurrentDownloadTask(null);
    }

    private Runnable mResetTask = new Runnable() {
        @Override
        public void run() {
            FotaUtil.clearUpdateFolder();
            FotaUtil.clearLogFolder();
            // clear the download id .
            FotaLog.d(TAG, "mResetTask -> FotaPref clear");
            FotaPref.getInstance(mContext).clear();
            FotaNotification.cancelFotaNotification(mContext);
        }
    };


    public void deleteTask() {
        ReportUtil.recordOperation(mContext, ReportUtil.OP_DELETEPACKAGE,
                ReportUtil.DEFAULT_FOTA_STATUS);
        new Thread(mDeleteTask).start();
        // Report delete action
    }

    private Runnable mDeleteTask = new Runnable() {
        @Override
        public void run() {
            FotaUtil.clearUpdateFolder();
            FotaUtil.clearLogFolder();
            // clear the download id .
            FotaNotification.cancelFotaNotification(mContext);
        }
    };

    /**
     * Delete Download File and clear status.
     */
    public void scheduleDeleteCurrentTask(boolean deleteAfterCheck) {
        FotaLog.d(TAG, "scheduleDeleteCurrentTask -> mCurrentTaskState = " + mCurrentTaskState
                + ", deleteAfterCheck = " +  deleteAfterCheck);

        mDeleteFlag = true;
        mDeleteAfterCheck = deleteAfterCheck;
        FotaPref.getInstance(mContext).setString(FotaConstants.DOWNLOAD_ID, "");
        FotaNotification.cancelFotaNotification(mContext);

        if (mCurrentTaskState == State.IDLE || mCurrentTaskState == State.CHECKED) {
            return;
        } else if (mCurrentTaskState == State.DOWNLOADED || mCurrentTaskState == State.INSTALLING) {
            deleteUpdatePackage();
            showDeleteResult(FotaUIPresenter.FOTA_DOWNLOAD_RESULT_DOWNLOAD_DELETED);
        } else {
            Intent intent = new Intent(mContext, DownloadService.class);
            intent.setAction(DownloadService.ACTION_DELETE_DOWNLOAD);
            mContext.startService(intent);
        }
    }

    /**
     * Delete download when check
     * @param task
     */
    public void scheduleDeleteAfterCheck(DownloadTask task, int type) {
        mCheckDownloadTask = task;
        mPendingCheckResult = type;
        FotaLog.d(TAG, "scheduleDeleteAfterCheck -> mCheckDownloadTask = " + mCheckDownloadTask
                + ", mPendingCheckResult = " + mPendingCheckResult);
        scheduleDeleteCurrentTask(true);
    }

    /**
     * Schedule update service.
     */
    public void scheduleStartInstall() {
        FotaLog.d(TAG, "scheduleStartInstall -> mCurrentTaskState = " + mCurrentTaskState);
        if (mCurrentTaskState != State.DOWNLOADED) {
            return;
        }
        updateCurrentTaskState(State.INSTALLING);
        Intent i = new Intent(mContext, FotaUpdateService.class);
        i.setAction(FotaUpdateService.ACTION_DO_UPDATE);
        mContext.startService(i);
    }
}