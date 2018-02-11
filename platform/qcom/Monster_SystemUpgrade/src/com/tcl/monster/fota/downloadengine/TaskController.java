package com.tcl.monster.fota.downloadengine;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.tcl.monster.fota.model.DownloadInfo;
import com.tcl.monster.fota.misc.FotaConstants;
import com.tcl.monster.fota.misc.State;
import com.tcl.monster.fota.utils.FotaLog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is a Download Task Controller. It contains a DownloadTask instance,
 * a ConcurrentHashMap to store SmallDownloadTask and DownloadMultiThread.
 */
public class TaskController {

    private static final String TAG = TaskController.class.getSimpleName();

    /**
     * The current DownloadTask
     */
    private DownloadTask mDownloadTask;

    /**
     * Total bytes of the DownloadTask.
     */
    private long mTotalBytes;

    /**
     * All SmallDownloadTask and its DownloadMultiThread
     */
    private ConcurrentHashMap<SmallDownloadTask, DownloadMultiThread> mSmallTaskThreads = new ConcurrentHashMap<SmallDownloadTask, DownloadMultiThread>();

    /**
     * Download urls that is accessible.
     */
    private List<String> mGoodUrls;

    /**
     * The engine.
     */
    private DownloadEngine mEngine;

    private ExecutorService mPool;

    private String mFastestUrl = "";

    public TaskController(DownloadEngine engine, DownloadTask task) {
        this.mDownloadTask = task;
        mTotalBytes = mDownloadTask.getTotalBytes();
        this.mEngine = engine;
        mGoodUrls = new ArrayList<String>();
        mPool = Executors.newCachedThreadPool();
    }

    /**
     * Calculate all urls available.
     * @return
     */
    private Set<String> calculateUrls() {
        DownloadInfo info = mDownloadTask.getDownloadInfo();
        Set<String> urls = new HashSet<String>();
        for (String server : info.mServers) {
            for (DownloadInfo.FileInfo fileInfo : info.mFiles) {
                String u = "";
                if (server.startsWith("http")) {
                    u = server + fileInfo.mUrl;

                } else {
                    u = "http://" + server + fileInfo.mUrl;
                }
                urls.add(u);
                // remove duplicated
                if (!mGoodUrls.contains(u)) {
                    mGoodUrls.add(u);
                    FotaLog.d(TAG, "calculateUrls -> add GoodUrls = " + u);
                }
                FotaLog.d(TAG, "calculateUrls -> add url = " + u);
            }
        }
        FotaLog.d(TAG, "calculateUrls -> urls.size = " + urls.size()
                + ", mGoodUrls.size = " + mGoodUrls.size());
        return urls;
    }

    /**
     * Generate all the SmallDownloadTask according to the urls.
     * @return
     */
    private Set<SmallDownloadTask> generateSmallTasks() {
        Set<SmallDownloadTask> smallTasks = mDownloadTask.getSmallTasks();
        FotaLog.v(TAG, "generateSmallTasks -> smallTasks = " + smallTasks);
        if (smallTasks != null && smallTasks.size() != 0) {
            FotaLog.v(TAG, "generateSmallTasks -> Resume smalltasks from mDownloadTask"
                    + ", smallTasks.size = " + smallTasks.size());
            // also call calculateUrils() for good urls
            calculateUrls();
            return smallTasks;
        } else {
            smallTasks = new HashSet<SmallDownloadTask>();
        }
        FotaLog.v(TAG, "generateSmallTasks -> create new smalltasks");
        List<String> urls = new ArrayList<String>(calculateUrls());
        int threads = urls.size();
        long tail = mTotalBytes % threads;
        long perSize = (mTotalBytes - tail) / threads;

        FotaLog.v(TAG, "generateSmallTasks -> Total threads = " + threads + ", perSize = "
                + perSize + ", tail = " + tail);
        for (int i = 0; i < threads - 1; i++) {
            SmallDownloadTask tk = new SmallDownloadTask(urls.get(i), i * perSize, perSize);
            smallTasks.add(tk);
        }
        // the last one should download more.
        SmallDownloadTask last = new SmallDownloadTask(urls.get(threads - 1), (threads - 1)
                * perSize, (perSize + tail));
        smallTasks.add(last);
        return smallTasks;
    }

    /**
     * split the task and download
     * @param
     */
    public void downloadTask() {
        FotaLog.v(TAG, "downloadTask -> downloadTaskId = " + mDownloadTask.getId());
        Set<SmallDownloadTask> smallTasks = generateSmallTasks();

        FotaLog.v(TAG, "downloadTask -> smallTasks = " + smallTasks);

        for (SmallDownloadTask task : smallTasks) {
            DownloadMultiThread t = new DownloadMultiThread(this, task);
            mSmallTaskThreads.put(task, t);
            mPool.submit(t);
        }
        FotaLog.v(TAG, "downloadTask -> mSmallTaskThreads = " + mSmallTaskThreads);
    }

    /**
     * Calculate currently downloaded bytes.
     * @return
     */
    public synchronized long getCurrentBytes() {
        long currentBytes = 0;
        if (mSmallTaskThreads.keySet() == null) {
            FotaLog.v(TAG, "getCurrentBytes -> mSmallTaskThreads is null");
            return 0;
        }
        for (SmallDownloadTask t : mSmallTaskThreads.keySet()) {
            if (t != null) {
                currentBytes += t.getCurrentBytes();
                FotaLog.v(TAG, "getCurrentBytes -> mTask " + t.getId() + " = " + t.getCurrentBytes());
            }
        }
        FotaLog.v(TAG, "getCurrentBytes -> Total download = " + currentBytes);
        return currentBytes;
    }

    public String retryUrl() {
        Random random = new Random();
        int index = random.nextInt(mGoodUrls.size());
        return mGoodUrls.get(index);
    }

    public synchronized void pauseDownload() {
        FotaLog.v(TAG, "pauseDownload");
        for (DownloadMultiThread t : mSmallTaskThreads.values()) {
            t.pauseDownload();
        }
        onDownloadPaused();
    }

    public synchronized void cancelDownload() {
        FotaLog.v(TAG, "cancelDownload");
        for (DownloadMultiThread t : mSmallTaskThreads.values()) {
            t.cancelDownload();
        }
		clear();
    }

    public synchronized void deleteDownload() {
        FotaLog.v(TAG, "deleteDownload");
        for (DownloadMultiThread t : mSmallTaskThreads.values()) {
            t.deleteDownload();
        }
        onDownloadDelete();
    }

    public synchronized boolean checkFinished() {
        boolean finished = true;
        for (DownloadMultiThread dt : mSmallTaskThreads.values()) {
            if (!dt.checkFinished()) {
                finished = false;
            }
        }
        return finished;
    }

    public synchronized void resumeDownload() {
        FotaLog.v(TAG, "resumeDownload");

        for (DownloadMultiThread t : mSmallTaskThreads.values()) {
            t.resumeDownload();
        }
    }

    public synchronized boolean checkAllThreadsPaused() {
        for (SmallDownloadTask t : mSmallTaskThreads.keySet()) {
            if (t.getStatus() == SmallDownloadTask.STATUS_RUNNING) {
                return false;
            }
        }
        return true;
    }

    public synchronized void onDownloadPaused() {
        if (checkAllThreadsPaused()) {
            mDownloadTask.setState(State.PAUSED.name());
            mEngine.onDownloadPaused(mDownloadTask);
            mPool.shutdown();
        }
    }

    public synchronized void onDownloadSuccessed(SmallDownloadTask task) {
        // we need record last bytes here
        mDownloadTask.setCurrentBytes(getCurrentBytes());
        if (TextUtils.isEmpty(mFastestUrl)) {
            mFastestUrl = task.getUrl();
        }
        boolean finished = true;
        for (SmallDownloadTask st : mSmallTaskThreads.keySet()) {
            if (st.getStatus() != SmallDownloadTask.STATUS_FINISHED) {
                finished = false;
                st.setUrl(mFastestUrl);
                mSmallTaskThreads.get(st).killDownload();
                break;
            }
        }

        if (finished) {
            mDownloadTask.setState(State.DOWNLOADED.name());
            mEngine.onDownloadSuccessed(mDownloadTask);
            return;
        }
    }

    public synchronized void onDownloading() {
        long updateBefore = mDownloadTask.getCurrentBytes();
        long after = getCurrentBytes();
        if(after >= updateBefore){
            mDownloadTask.setCurrentBytes(after);
        } else {
           FotaLog.w(TAG, "onDownloading -> SIZE ERROR!!! before = " + updateBefore + ", after = " + after);
        }
        String json = new Gson().toJson(mSmallTaskThreads.keySet());
        mDownloadTask.setSmallTasksJson(json);
        String preState = mDownloadTask.getState();
        if(!TextUtils.equals(preState,State.PAUSING.name())){
            mDownloadTask.setState(State.DOWNLOADING.name());
        }
        mEngine.onDownloading(mDownloadTask);
    }

    public synchronized boolean onRetry(SmallDownloadTask task) {
        FotaLog.v(TAG, "onRetry -> " + "mDownloadTaskState = " + mDownloadTask.getState()
                + ", mSmallTaskId = " + task.getId() + ", mRetryTimes = " + task.mRetryTimes
                + ", url = " + task.getUrl());
        if (task.mRetryTimes <= FotaConstants.DOWNLOAD_RETRY_TIMES) {
            task.mRetryTimes++;
            return true;
        } else if (task.mRetryTimes < 2 * FotaConstants.DOWNLOAD_RETRY_TIMES) {
            //task.mRetryTimes = 0;
            return onRetryGoodUrl(task);
        } else {
            return false;
        }
    }

    public synchronized boolean onRetryGoodUrl(SmallDownloadTask task) {
        FotaLog.v(TAG, "onRetryGoodUrl -> mSmallTaskId = " + task.getId()
                + ", mGoodUrls.size = " + mGoodUrls.size());
        if (mGoodUrls.size() <= 0) {
            FotaLog.v(TAG, "onRetryGoodUrl -> no left url");
            return false;
        }
        if (mGoodUrls.size() == 1 && mGoodUrls.get(0).equals(task.getUrl())) {
            return false;
        }
        mGoodUrls.remove(task.getUrl());
        task.setUrl(retryUrl());
        return true;
    }

    public synchronized void onDownloadDelete() {
        boolean deleted = true;

        for (SmallDownloadTask t : mSmallTaskThreads.keySet()) {
            if (t.getStatus() == SmallDownloadTask.STATUS_RUNNING) {
                FotaLog.d(TAG, "onDownloadDelete -> mSmallTaskId = " + t.getId()
                        + ", status = " + t.getStatus());
                deleted = false;
                break;
            }
        }

        FotaLog.d(TAG, "onDownloadDelete -> delteted = " + deleted);
        if (deleted) {
            clear();
            mPool.shutdown();
            mEngine.onDownloadDeleted(mDownloadTask);
        }
    }

    public synchronized void onDownloadFailed(SmallDownloadTask task) {
        FotaLog.v(TAG, "onDownloadFailed -> mSmallTaskID = " + task.getId()
                + ", reason = " + mDownloadTask.getPausedReason());
        for (SmallDownloadTask st : mSmallTaskThreads.keySet()) {
            mSmallTaskThreads.get(st).cancelDownload();
        }
        mDownloadTask.setPausedReason(task.getPausedReason());
        mDownloadTask.setState(State.PAUSED.name());
        FotaLog.v(TAG, "onDownloadFailed -> reason = " + mDownloadTask.getPausedReason());
        mEngine.onDownloadFailed(mDownloadTask);
        mPool.shutdown();
    }

    private void clear() {
        mSmallTaskThreads.clear();
    }

    public int getPausedReason() {
        return mDownloadTask.getPausedReason();
    }

    public String status() {
        StringBuilder sb = new StringBuilder();
        sb.append("---------TaskController status begin----------\n");
        sb.append("threads :" + mSmallTaskThreads.size() + "\n");
        for (SmallDownloadTask t : mSmallTaskThreads.keySet()) {
            DownloadMultiThread dt = mSmallTaskThreads.get(t);
            sb.append("---small task :").append(t.getId()).append("\n");
        }
        sb.append("---------TaskController status end----------\n");
        return sb.toString();
    }
}