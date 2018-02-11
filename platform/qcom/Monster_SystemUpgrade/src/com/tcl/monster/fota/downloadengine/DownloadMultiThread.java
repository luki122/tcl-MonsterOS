package com.tcl.monster.fota.downloadengine;

import android.os.Process;
import android.util.Log;

import com.tcl.monster.fota.FotaApp;
import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.provider.Fota;
import com.tcl.monster.fota.utils.FotaLog;
import com.tcl.monster.fota.utils.FotaUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_PARTIAL;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;

public class DownloadMultiThread implements Runnable {

    private static final String TAG = "DownloadMultiThread";
    // 100 kb
    private static final long REFRESH_INTEVAL_SIZE = 100 * 1024;
    private static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;

    // private DownloadEngine mEngine;
    private TaskController mController;

    private SmallDownloadTask mSmalltask;

    private volatile boolean mPauseNow;
    private volatile boolean mCancelNow;
    private volatile boolean mKillNow;
    private volatile boolean mDeleteNow;

    private long mFrom;
    private long mPerSize;

    private int times = 1;

    private static int i = 0;

    DownloadMultiThread(TaskController controller, SmallDownloadTask sTask) {
        this.mController = controller;
        this.mSmalltask = sTask;
        this.mFrom = sTask.getFrom();
        this.mPerSize = sTask.getTotalBytes();
        FotaLog.v(TAG, "DownloadMultiThread -> " + "mSmalltaskId = " + mSmalltask.getId()
                + ", mFrom = " + mFrom + ", mPerSize = " + mPerSize);
        i++;
    }

    void pauseDownload() {
        FotaLog.v(TAG, "pauseDownload -> mPauseNow = " + mPauseNow);
        if (mPauseNow) {
            return;
        }
        mPauseNow = true;
    }

    void resumeDownload() {
        FotaLog.v(TAG, "resumeDownload -> mPauseNow = " + mPauseNow);
        if (!mPauseNow) {
            return;
        }
        mPauseNow = false;
        synchronized (this) {
            notify();
        }
    }

    void killDownload() {
        mKillNow = true;
        synchronized (this) {
            notify();
        }
    }

    void cancelDownload() {
        FotaLog.v(TAG, "cancelDownload -> mCancelNow = " + mCancelNow);
        mCancelNow = true;
        synchronized (this) {
            notify();
        }
    }

    void deleteDownload() {
        FotaLog.v(TAG, "deleteDownload -> mDeleteNow = " + mDeleteNow);
        mDeleteNow = true;
    }

    public SmallDownloadTask getSmallDownloadTask() {
        return mSmalltask;
    }

    public boolean checkFinished() {
        return mSmalltask.getCurrentBytes() >= mPerSize;
    }

    @Override
    public void run() {
        boolean tryAgain = false;
        mSmalltask.setStatus(SmallDownloadTask.STATUS_RUNNING);
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        if (mDeleteNow) {
            mSmalltask.setStatus(SmallDownloadTask.STATUS_CANCELED);
            mController.onDownloadDelete();
            return;
        }
        if (checkFinished()) {
            FotaLog.v(TAG, "checkFinished -> mSmalltaskId = " + mSmalltask.getId() +" already done "
                    + mSmalltask.getCurrentBytes() + "/" + mPerSize);
            mSmalltask.setStatus(SmallDownloadTask.STATUS_FINISHED);
            mController.onDownloadSuccessed(mSmalltask);
            return;
        }
        if (mPauseNow) {
            mSmalltask.setStatus(SmallDownloadTask.STATUS_PAUSED);
            mController.onDownloadPaused();
            return;
        }
        FotaLog.d(TAG, "beforeDownloading -> mSmalltaskId = " + mSmalltask.sTaskId
                + ", mDeleteNow = " + mDeleteNow + ", mCancelNow = " + mCancelNow
                + ", mKillNow = " + mKillNow + ", mPauseNow = " + mPauseNow);
        RandomAccessFile raf = null;
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            raf = buildDownloadFile();
            conn = initConnection();
            conn.connect();
            final int responseCode = conn.getResponseCode();
            FotaLog.d(TAG, "startDownloading -> mSmalltaskId = " + mSmalltask.getId()
                    + ", responseCode = " + responseCode);
            switch (responseCode) {
                //case HTTP_OK:
                case HTTP_PARTIAL:
                    mController.onDownloading();
                    is = conn.getInputStream();
                    byte[] buffer = new byte[8192];
                    int count = 0;
                    long dSize = mSmalltask.getCurrentBytes();
                    long prevTime = System.currentTimeMillis();
                    long achieveSize = dSize;
                    while (!mDeleteNow && !mCancelNow && !mKillNow && (dSize < mPerSize)
                            && (count = is.read(buffer)) != -1) {
                        raf.write(buffer, 0, count);
                        dSize += count;
                        mSmalltask.setCurrentBytes(dSize);
                        if (mPauseNow) {
                            mSmalltask.setStatus(SmallDownloadTask.STATUS_PAUSED);
                            mController.onDownloadPaused();
                            return;
                        }
                        long tempSize = dSize - achieveSize;
                        if (tempSize > REFRESH_INTEVAL_SIZE) {
                            long tempTime = System.currentTimeMillis() - prevTime;
                            long speed = tempSize * 1000 / tempTime;
                            achieveSize = dSize;
                            prevTime = System.currentTimeMillis();
                            mSmalltask.setDownloadSpeed(speed);
                            mController.onDownloading();
                            // mEngine.updateDownloadTask(mTask, dSize, speed);
                            FotaLog.v("downloadSpeed -> mSmalltaskId = " + mSmalltask.sTaskId
                                    + ", speed = " + speed);
                        }
                    }
                    FotaLog.v(TAG, "exitDownload -> mSmalltaskId = " + mSmalltask.sTaskId
                            + ", dSize = " + dSize + ", mPerSize = " + mPerSize + ", mDeleteNow = "
                            + mDeleteNow + ", mKillNow = " + mKillNow + ", mCancelNow = " + mCancelNow
                            + ", isFinished = " + checkFinished());
                    if (mDeleteNow) {
                        return;
                    }
                    mSmalltask.setCurrentBytes(dSize);
                    mController.onDownloading();
                    if (mKillNow) {
                        mKillNow = false;
                        tryAgain = true;
                        FotaLog.d(TAG, "Kill and tryAgain -> mSmalltaskId = " + mSmalltask.sTaskId);
                        return;
                    }
                    if (mCancelNow) {
                        mSmalltask.setStatus(SmallDownloadTask.STATUS_CANCELED);
                    } else if(checkFinished()) {
                        mSmalltask.setStatus(SmallDownloadTask.STATUS_FINISHED);
                        mController.onDownloadSuccessed(mSmalltask);
                    }
                    break;

                case HTTP_REQUESTED_RANGE_NOT_SATISFIABLE:
                    mSmalltask.setStatus(SmallDownloadTask.STATUS_ERROR);
                    mController.onDownloadFailed(mSmalltask);
                    break;

                case HTTP_UNAVAILABLE:
                case HTTP_INTERNAL_ERROR:
                default:
                    // this is server error, we need to try other server.
                    mSmalltask.setStatus(SmallDownloadTask.STATUS_PAUSED);
                    mSmalltask.setPausedReason(Fota.Firmware.PAUSED_REASON_SERVER_ERROR);
                    if (mController.onRetryGoodUrl(mSmalltask)) {
                        FotaLog.d(TAG, "HTTP_INTERNAL_ERROR-> tryAgain, mSmalltaskId = " + mSmalltask.sTaskId);
                        tryAgain = true;
                    } else {
                        FotaLog.d(TAG, "HTTP_INTERNAL_ERROR -> tryAgain Failed, mSmalltaskId = " + mSmalltask.sTaskId);
                        mController.onDownloadFailed(mSmalltask);
                    }
                    return;
            }
        } catch (IOException e) {
            FotaLog.w(TAG, "Exception -> mSmalltaskId = " + mSmalltask.sTaskId + ", Exception = "
                    + e.getClass().getName() + ", stacktrace = " + Log.getStackTraceString(e));
            if (mDeleteNow || mCancelNow) {
                return;
            }
            mSmalltask.setStatus(SmallDownloadTask.STATUS_PAUSED);
            mSmalltask.setPausedReason(Fota.Firmware.PAUSED_REASON_NETWORK);
            if (e instanceof FileNotFoundException) {
                mSmalltask.setStatus(SmallDownloadTask.STATUS_ERROR);
                mSmalltask.setPausedReason(Fota.Firmware.PAUSED_REASON_NOT_PAUSED);
                mController.onDownloadFailed(mSmalltask);
                return;
            } else if(Log.getStackTraceString(e).contains("No space left on device")){//PR1163313 [FOTA]When memory low,MS can still download the diffpakcage with wrong prompt message.1207583
                FotaLog.d(TAG, "No space left on device!!!");
                mSmalltask.setStatus(SmallDownloadTask.STATUS_ERROR);
                mSmalltask.setPausedReason(Fota.Firmware.PAUSE_REASON_STORAGE_NOT_ENOUGH);
                mController.onDownloadFailed(mSmalltask);
                return ;
            }
            FotaLog.v(TAG, "Exception -> Network status = " + FotaUtil.isOnline(FotaApp.getApp())
                    + ", mPauseNow = " + mPauseNow);
            if (FotaUtil.isOnline(FotaApp.getApp()) && !mPauseNow) {
                if (mController.onRetry(mSmalltask)) {
                    FotaLog.d(TAG, "Exception -> tryAgain = true, mSmalltaskId = " + mSmalltask.sTaskId);
                    tryAgain = true;
                } else {
                    FotaLog.v(TAG, "mSmalltaskId = " +  mSmalltask.sTaskId + ", Retry failed!!!!!!");
                    mController.onDownloadFailed(mSmalltask);
                }
            } else if (mPauseNow) {
                tryAgain = true;
            } else {
                mSmalltask.setStatus(SmallDownloadTask.STATUS_PAUSED);
                mSmalltask.setPausedReason(Fota.Firmware.PAUSED_REASON_NETWORK);
                mController.onDownloadFailed(mSmalltask);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mDeleteNow) {
                FotaLog.d(TAG, "finally -> mSmalltaskId = " + mSmalltask.sTaskId
                        + ", Exit by delete !!!!!!");
                mSmalltask.setStatus(SmallDownloadTask.STATUS_CANCELED);
                mController.onDownloadDelete();
            } else if (tryAgain && !mCancelNow) {
                times++;
                FotaLog.d(TAG, "finally ->  smallTask " + mSmalltask.sTaskId
                        + " tryagain " + times + " times, will execute run() again!!!");
                run();
            }
        }
    }

    private RandomAccessFile buildDownloadFile() throws IOException {
        File file = FotaUtil.updateZip();
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        long from = mFrom + mSmalltask.getCurrentBytes();
        raf.seek(from);
        return raf;
    }

    private HttpURLConnection initConnection() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(mSmalltask.getUrl()).openConnection();
        conn.setConnectTimeout(FotaConstants.DEFAULT_TIMEOUT);
        conn.setReadTimeout(FotaConstants.DEFAULT_TIMEOUT);
        conn.setInstanceFollowRedirects(false);
        conn.setUseCaches(true);

        long from = mFrom + mSmalltask.getCurrentBytes();
        long to = mFrom + mPerSize - 1;
        conn.setRequestProperty("Range", "bytes=" + from + "-" + to);
        return conn;
    }
}