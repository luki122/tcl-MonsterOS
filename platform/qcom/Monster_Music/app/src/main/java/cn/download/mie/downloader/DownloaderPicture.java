/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.download.mie.downloader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import cn.download.mie.util.DBUtils;
import cn.tcl.music.R;
import cn.tcl.music.app.MusicApplication;
import cn.tcl.music.common.CommonConstants;
import cn.tcl.music.model.live.SongDetailBean;
import cn.tcl.music.network.DownloadSongDetailTask;
import cn.tcl.music.network.IDownloadData;
import cn.tcl.music.util.FileManager;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.util.PreferenceUtil;
import cn.tcl.music.util.Util;

import com.tcl.framework.db.EntityManager;
import com.tcl.framework.db.sqlite.Selector;

import cn.download.mie.downloader.core.HttpDownloader;
import cn.download.mie.downloader.core.INetworkDownloader;
import cn.download.mie.downloader.core.TaskThread;
import cn.download.mie.downloader.util.DLog;
import cn.download.mie.downloader.util.PriorityUtils;
import cn.download.mie.downloader.util.Tools;
import cn.tcl.music.view.DownloadAlertDialog;
import cn.tcl.music.util.ToastUtil;
import cn.tcl.music.view.striptab.DownloadNotification;

import com.xiami.sdk.utils.Encryptor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 通用多优先级下载器
 * Created by difei.zou on 2015/6/4.
 */
public class DownloaderPicture implements IDownloader, IDownloadData {
    public static final String TAG = DownloaderPicture.class.getSimpleName();
    private DownloaderConfig mDownloaderConfig;
    private Context mContext;
    private boolean mIsBreak = true;
    private boolean mIsFirstNetworkChange = true;

    public DownloaderPicture(Context context, String song_id) {
        super();
        mContext = context;
    }

    public DownloaderConfig getDownloaderConfig() {
        return mDownloaderConfig;
    }

    /**
     * 等待任务队列
     */
    private final BlockingQueue<DownloadTask> mWaitingTasks = new PriorityBlockingQueue<DownloadTask>();

    /**
     * 下载中的任务
     */
    private final LinkedList<DownloadTask> mDownloadingTasks = new LinkedList<>();

    /**
     * 所有任务
     */
    private final Set<DownloadTask> mCurrentTasks = new HashSet<DownloadTask>();

    /**
     * 断点重试的任务
     */
    private final LinkedList<DownloadTask> mRetryTasks = new LinkedList<>();


    private ArrayList<DownloadTask> mResumeLowTasks = new ArrayList<>();


    private List<DownloadTask> mAllTasks = new ArrayList<DownloadTask>();

    private DownloadEventCenter mEventCenter = new DownloadEventCenter();

    private HandlerThread mHandlerThread;
    private Handler mHandlerThreadHandler;
    private TaskThread[] mThreads;
    private Context mContext1;
    private INetworkDownloader mHttpDownloader;
    private EntityManager<DownloadTask> mDbManager;

    public void init(DownloaderConfig config, Context context, ILoadListener loadListener) {
        mContext1 = context;
        mContext = MusicApplication.getApp();

        if (config == null) {
            config = DownloaderConfig.getDefaultConfig(mContext);
        }
        mDbManager = DBUtils.getDownloadTaskManager(mContext, null);

        mDownloaderConfig = config;
        mHttpDownloader = new HttpDownloader();
        try {
            mContext.registerReceiver(mNetworkMonitorReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        mThreads = new TaskThread[mDownloaderConfig.mRunningTask];
        for (int i = 0; i < mThreads.length; i++) {
            mThreads[i] = new TaskThread(mWaitingTasks, mHttpDownloader);
            mThreads[i].start();
        }
        mHandlerThread = new HandlerThread("download");
        mHandlerThread.start();

        mHandlerThreadHandler = new Handler(mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                LogUtil.d("testDownload", "Handler");
                final DownloaderPicture downloader = (DownloaderPicture) msg.obj;
                ConnectivityManager cm = (ConnectivityManager) downloader.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    NetworkInfo ni = cm.getActiveNetworkInfo();
                    if (ni != null) {
                        final List<DownloadTask> downloadTasks = new ArrayList<>(downloader.mRetryTasks);
                        for (int i = 0; i < downloadTasks.size(); i++) {
                            if (downloadTasks.get(i).isLyric) {
                                startDownload(downloadTasks.get(i));
                                downloader.mRetryTasks.remove(downloadTasks.get(i));
                                downloadTasks.remove(i);
                                i--;
                            }
                        }
                        synchronized (downloader.mRetryTasks) {
                            if (downloadTasks.size() > 0) {
                                startBatchMusicDownload1(downloadTasks);
                                LogUtil.d("testDownload", "startBatchMusicDownload1");
                            }

                           /* for (int i = 0; i < downloader.mRetryTasks.size(); i++) {
                                final DownloadTask task = downloader.mRetryTasks.get(i);

                                if (mAllTasks.contains(task)) {
                                    NLog.d("testDownload", "addTaskFirst");
                                    //downloader.addTaskFirst(task);
                                    downloader.startDownloadInNetworkNotShow(task);
                                } else {
                                    downloadTasks.remove(task);
                                }
                            }*/

                        }
                    }
                }
            }
        };

        loadData(loadListener);
    }


    private void loadData(ILoadListener loadListener) {
        List<DownloadTask> allTask = mDbManager.findAll();
        if (loadListener != null) {
            allTask = loadListener.onLoad(mAllTasks);
        }
        LogUtil.d("downloadtest", "loadData");
        for (DownloadTask task : allTask) {
            if (Util.getNetworkType() == Util.NETTYPE_WIFI) {
                if (task.mStatus != DownloadStatus.DOWNLOADED) {
                    task.mStatus = DownloadStatus.NEW;
                    startDownloadInNetwork(task);
                }
            } else {
                if (task.mStatus != DownloadStatus.DOWNLOADED) {
                    addTaskMobileNetWork(task);
                }
            }
        }

    }


    @Override
    public List<DownloadTask> getAllTask() {
        return mAllTasks;
    }

    public void init(Context context) {
        init(null, context, null);
    }

    public void setContext(Context context) {
        mContext1 = context;
    }

    /**
     * 手动下载
     *
     * @param item
     */
    public void startDownload(DownloadTask item) {
        LogUtil.d("downloadtest", "startDownload " + item.song_name);
        //过滤已添加的下载任务
        if (isTaskExist(item)) return;
        item.mSequence = PriorityUtils.getMaxSequence(mWaitingTasks) + 1;
        item.setPriority(DownloadTask.PRORITY_NORMAL);
        synchronized (mRetryTasks) {
            mRetryTasks.remove(item);
        }
        addTaskFirst(item);

        //在开始之前，把低优先级下载的暂停掉
        pauseAutoTask();
    }

    private void pauseAutoTask() {
        if (mDownloadingTasks.size() < mDownloaderConfig.mRunningTask) {
            //有空余线程
            return;
        } else {
            synchronized (mAllTasks) {
                for (int i = 0; i < mAllTasks.size(); i++) {
                    DownloadTask task = mAllTasks.get(i);
                    if (task.mPriority > DownloadTask.PRORITY_NORMAL) {
                        //找到自动下载的，停掉。
                        pauseDownload(task);
                        //这句话加在这儿可能会有问题
                        onTaskStop1(task);
                        //重新加入队列
                        synchronized (mResumeLowTasks) {
                            mResumeLowTasks.add(task);
                        }
                        break;
                    }
                }
            }
        }
    }


    private boolean isTaskExistAlltask(DownloadTask item) {
        if (mAllTasks == null) {
            return false;
        }
        for (int i = 0; i < mAllTasks.size(); i++) {
            if (mAllTasks.get(i).mKey.equalsIgnoreCase(item.mKey)) {
                return true;
            }
        }

        return false;
    }


    private boolean isTaskExist(DownloadTask item) {
        return mCurrentTasks.contains(item);
    }

    public void resumeLowTasks() {
        if (mDownloadingTasks.size() == 0) {
            synchronized (mResumeLowTasks) {
                for (int i = 0; i < mResumeLowTasks.size(); i++) {
                    startDownloadInLow(mResumeLowTasks.get(i));
                }
                mResumeLowTasks.clear();
            }
        }
    }

    /**
     * 保持原来的下载方式下载
     *
     * @param item
     */
    private void addTask(DownloadTask item) {
//        item.setDownloader();
        if (isTaskExist(item)) return;
        LogUtil.d("downloadtest", "addTask " + item.song_name);

        synchronized (mCurrentTasks) {
            mCurrentTasks.add(item);
        }
        item.mStatus = DownloadStatus.WAITING;
        item.setDefaultConfig(mDownloaderConfig, mContext);
        item.getDownloader().getEventCenter().onDownloadStatusChange(item);
        item.isCancel = false;
        mDbManager.saveOrUpdate(item);
        mWaitingTasks.offer(item);
    }

    private void addTaskFirst(DownloadTask item) {
//        item.setDownloader(this);
        if (!isTaskExistAlltask(item) && !item.isLyric) {
            synchronized (mAllTasks) {
                mAllTasks.add(item);
            }
        } else {
            if (isTaskExistAlltask(item) && !item.isLyric) {
                for (int i = 0; i < mAllTasks.size(); i++) {
                    if (mAllTasks.get(i).mKey.equals(item.mKey)) {
                        if (mAllTasks.get(i).mUrl == null) {
                            mAllTasks.remove(i);
                            mAllTasks.add(i, item);
                        }
                    }
                }
            }
        }
        if (!item.isLyric && item.mStatus == DownloadStatus.NEW) {
            DownloadNotification.getInstance().setNotification(1, 0, null);
        }
        item.mStatus = DownloadStatus.WAITING;
        item.setDefaultConfig(mDownloaderConfig, mContext);
        if (!item.isLyric) {
            item.getDownloader().getEventCenter().onDownloadStatusChange(item);
        }
        item.isCancel = false;
        mWaitingTasks.offer(item);
        if (!item.isLyric) {
            mDbManager.saveOrUpdate(item);
        }
    }


    private void addTaskFirstInMobileNetWork(DownloadTask item) {
//        item.setDownloader(this);
        if (!isTaskExistAlltask(item) && !item.isLyric) {
            synchronized (mAllTasks) {
                mAllTasks.add(item);
                ToastUtil.showToast(mContext, R.string.download_have_alarm_cancel);
            }
        } else {
            ToastUtil.showToast(mContext, R.string.download_have_alarm_cancel);
            return;
        }
        if (!mRetryTasks.contains(item)) {
            mRetryTasks.add(item);
        }
        item.mStatus = DownloadStatus.NEW;
        item.getDownloader().getEventCenter().onDownloadStatusChange(item);
        item.isCancel = true;
        if (!item.isLyric) {
            mDbManager.saveOrUpdate(item);
        }
    }

    private void addTaskMobileNetWork(DownloadTask item) {
//        item.setDownloader();
        if (!isTaskExistAlltask(item) && !item.isLyric) {
            synchronized (mAllTasks) {
                mAllTasks.add(item);
            }
        }
        if (!mRetryTasks.contains(item)) {
            mRetryTasks.add(item);
        }
        item.mStatus = DownloadStatus.NEW;
        item.getDownloader().getEventCenter().onDownloadStatusChange(item);
        DownloadNotification.getInstance().cancleNotifaction();
        item.isCancel = true;
        if (!item.isLyric) {
            mDbManager.saveOrUpdate(item);
        }
    }


    /**
     * 自动下载,优先级低于手动下载
     *
     * @param item
     */
    public void startDownloadInLow(DownloadTask item) {
        item.mSequence = PriorityUtils.getMaxSequence(mWaitingTasks) + 1;
        item.setPriority(DownloadTask.PRORITY_LOW);
        synchronized (mRetryTasks) {
            mRetryTasks.remove(item);
        }
        addTaskFirst(item);
    }

    public void startDownloadInNetwork(final DownloadTask item) {
        if (Util.getNetworkType() == 0) {
            ToastUtil.showToast(mContext, R.string.tip_download_network_error);
            return;
        } else if (Util.getNetworkType() == Util.NETTYPE_MOBILE) {
            ((Activity) mContext1).runOnUiThread(new Runnable() {
                public void run() {
                    DownloadAlertDialog downloadAlertDialog = new DownloadAlertDialog(mContext1);
                    downloadAlertDialog.setOnButtonClickListener(new DownloadAlertDialog.OnDialogButtonClickListener() {
                        @Override
                        public void okButtonClick() {
                            if (item.mUrl != null && !item.mUrl.isEmpty()) {
                                startDownloadInLow(item);
                            } else {
                                new DownloadSongDetailTask(mContext, DownloaderPicture.this, item.mKey, false, true).executeMultiTask();
                            }
                        }

                        @Override
                        public void cancelButtonClick() {
                            addTaskMobileNetWork(item);
                        }
                    });
                    downloadAlertDialog.showWrapper();
                }
            });


        } else if (Util.getNetworkType() == Util.NETTYPE_WIFI) {
            if (item.mUrl == null || item.mUrl.isEmpty()) {
                LogUtil.d("getNetworkType", "item.mUrl == null || item.mUrl.isEmpty()" + "item.mKey=" + item.mKey);
                new DownloadSongDetailTask(mContext, this, item.mKey, false, true).executeMultiTask();
            } else {
                startDownloadInLow(item);
            }
        }
    }


    public boolean fileIsExists(String s) {
        try {
            File f = new File(s);
            if (!f.exists()) {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void setDownloadToast(DownloadTask item) {
        if (!item.isLyric && !item.isBatchDownload) {
            Selector selector = Selector.create().where("mStatus", "=", DownloadStatus.DOWNLOADED);
            List<DownloadTask> downloadedTaskList = mDbManager.findAll(selector); //SqliteUtility.getInstance().select(DownloadTask.class, selection, selectionArgs);
            for (int i = 0; i < downloadedTaskList.size(); i++) {
                Log.d("test", " downloadedTaskList.size() = " + downloadedTaskList.size());
                if (fileIsExists(downloadedTaskList.get(i).getFinalFilePath()) && item.mKey.equals(downloadedTaskList.get(i).mKey)) {
                    Log.d("test", "11111");
                    ToastUtil.showToast(mContext, R.string.download_have_alarm);
                    return;
                }
            }
            for (int i = 0; i < mAllTasks.size(); i++) {
                if (isTaskExistAlltask(item)) {
                    Log.d("test", "2222");
                    ToastUtil.showToast(mContext, R.string.download_have_alarm);
                    return;
                }
            }

            ToastUtil.showToast(mContext, R.string.download_alarm);
            new DownloadSongDetailTask(mContext, DownloaderPicture.this, item.mKey, false, true).executeMultiTask();

        }

    }

    public void startDownloadInLowFirst(DownloadTask item) {
        LogUtil.d("mytest", "startDownloadInLowFirst");

        item.mSequence = PriorityUtils.getMaxSequence(mWaitingTasks) + 1;
        item.setPriority(DownloadTask.PRORITY_LOW);
        synchronized (mRetryTasks) {
            mRetryTasks.remove(item);
        }
        addTaskFirst(item);
    }

    public void startMusicDownload(final String id) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (Util.getNetworkType() == 0) {
            ToastUtil.showToast(mContext, R.string.tip_download_network_error
            );
            return;
        } else if (PreferenceUtil.getValue(mContext, PreferenceUtil.NODE_NETWORK_SWITCH, PreferenceUtil.KEY_NETWORK_SWITCH, CommonConstants.NO_OPEN) == CommonConstants.NO_OPEN) {
            LogUtil.d(TAG, "try only under WLAN");
            if (Util.getNetworkType() == Util.NETTYPE_WIFI) {
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.mKey = id;
                setDownloadToast(downloadTask);
            } else {
                ToastUtil.showToast(mContext, R.string.settings_open_WLAN);
                return;
            }
        } else {
            if (Util.getNetworkType() == Util.NETTYPE_MOBILE) {
                ((Activity) mContext1).runOnUiThread(new Runnable() {
                    public void run() {
                        DownloadAlertDialog downloadAlertDialog = new DownloadAlertDialog(mContext1);
                        downloadAlertDialog.setOnButtonClickListener(new DownloadAlertDialog.OnDialogButtonClickListener() {
                            @Override
                            public void okButtonClick() {
                                DownloadTask downloadTask = new DownloadTask();
                                downloadTask.mKey = id;
                                setDownloadToast(downloadTask);
                            }

                            @Override
                            public void cancelButtonClick() {
                                new DownloadSongDetailTask(mContext, DownloaderPicture.this, id, false, false).executeMultiTask();
                            }
                        });
                        downloadAlertDialog.showWrapper();
                    }
                });
            } else if (Util.getNetworkType() == Util.NETTYPE_WIFI) {
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.mKey = id;
                setDownloadToast(downloadTask);
            }
        }
    }

    public void startBatchMusicDownload(final List<SongDetailBean> listData) {
        Log.d("test", "startBatchMusicDownload 1");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (Util.getNetworkType() == 0) {
            ToastUtil.showToast(mContext, R.string.tip_download_network_error);
            return;
        } else if (Util.getNetworkType() == Util.NETTYPE_MOBILE) {
            if (PreferenceUtil.getValue(mContext, PreferenceUtil.NODE_NETWORK_SWITCH, PreferenceUtil.KEY_NETWORK_SWITCH, CommonConstants.NO_OPEN) == CommonConstants.NO_OPEN) {
                ToastUtil.showToast(mContext, R.string.settings_open_WLAN);
                return;
            } else {
                ((Activity) mContext1).runOnUiThread(new Runnable() {
                    public void run() {
                        DownloadAlertDialog downloadAlertDialog = new DownloadAlertDialog(mContext1);
                        downloadAlertDialog.setOnButtonClickListener(new DownloadAlertDialog.OnDialogButtonClickListener() {
                            @Override
                            public void okButtonClick() {
                                List<DownloadTask> allTask = mDbManager.findAll();
                                boolean isExit = false;
                                Ok:
                                for (int i = 0; i < listData.size(); i++) {
                                    if (allTask != null && !allTask.isEmpty()) {
                                        for (int j = 0; j < allTask.size(); j++) {
                                            if (listData.get(i).song_id.equals(allTask.get(j).mKey)) {
                                                isExit = true;
                                                continue Ok;
                                            } else {
                                                isExit = false;
                                            }
                                        }
                                        new DownloadSongDetailTask(mContext, DownloaderPicture.this, listData.get(i).song_id, true, true).executeMultiTask();
                                    } else {
                                        new DownloadSongDetailTask(mContext, DownloaderPicture.this, listData.get(i).song_id, true, true).executeMultiTask();
                                    }
                                }
                                if (isExit) {
                                    ToastUtil.showToast(mContext, R.string.download_have_alarm);
                                } else {
                                    ToastUtil.showToast(mContext, R.string.download_alarm);
                                }

                            }

                            @Override
                            public void cancelButtonClick() {
                                for (int i = 0; i < listData.size(); i++) {
                                    new DownloadSongDetailTask(mContext, DownloaderPicture.this, listData.get(i).song_id, true, false).executeMultiTask();
                                }

                            }
                        });
                        downloadAlertDialog.showWrapper();
                    }
                });
            }
        } else if (Util.getNetworkType() == Util.NETTYPE_WIFI) {
            List<DownloadTask> allTask = mDbManager.findAll();
            boolean isExit = false;
            Ok:
            for (int i = 0; i < listData.size(); i++) {
                if (allTask != null && !allTask.isEmpty()) {
                    for (int j = 0; j < allTask.size(); j++) {
                        if (listData.get(i).song_id.equals(allTask.get(j).mKey)) {
                            isExit = true;
                            continue Ok;
                        } else {
                            isExit = false;
                        }
                    }
                    new DownloadSongDetailTask(mContext, this, listData.get(i).song_id, true, true).executeMultiTask();
                } else {
                    new DownloadSongDetailTask(mContext, this, listData.get(i).song_id, true, true).executeMultiTask();
                }
            }
            if (isExit) {
                Log.d("test", "4444");
                ToastUtil.showToast(mContext, R.string.download_have_alarm);
            } else {
                ToastUtil.showToast(mContext, R.string.download_alarm);
            }
        }
    }


    public static final int SHOW_DIALOG = 100;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null && SHOW_DIALOG == msg.what) {
                showDialog((List<DownloadTask>) msg.obj);
            }
        }
    };


    private void showDialog(final List<DownloadTask> listData) {
        if (mContext1 instanceof Activity) {
            Activity activity = (Activity) mContext1;
            if (activity.isDestroyed()) {
                return;
            }
            DownloadAlertDialog downloadAlertDialog = new DownloadAlertDialog(mContext1);
            downloadAlertDialog.setOnButtonClickListener(new DownloadAlertDialog.OnDialogButtonClickListener() {
                @Override
                public void okButtonClick() {
                    for (int i = 0; i < listData.size(); i++) {
                        if (listData.get(i).mUrl != null && !listData.get(i).mUrl.isEmpty()) {
                            startDownloadInLow(listData.get(i));
                        } else {
                            new DownloadSongDetailTask(mContext, DownloaderPicture.this, listData.get(i).mKey, false, true).executeMultiTask();
                        }
                    }
                    if (mRetryTasks != null && mRetryTasks.contains(listData)) {
                        mRetryTasks.removeAll(listData);
                    }
                }

                @Override
                public void cancelButtonClick() {
                    for (int i = 0; i < listData.size(); i++) {
                        addTaskMobileNetWork(listData.get(i));
                    }
                }
            });
            downloadAlertDialog.showWrapper();
        }

    }


    public void startBatchMusicDownload1(final List<DownloadTask> listData) {
        Log.d("test", "startBatchMusicDownload1(final List<DownloadTask> listData)");

        if (Util.getNetworkType() == 0) {
            ToastUtil.showToast(mContext, R.string.tip_download_network_error);
            return;
        } else if (Util.getNetworkType() == Util.NETTYPE_MOBILE) {
            Message msg = new Message();
            msg.what = SHOW_DIALOG;
            msg.obj = listData;
            mHandler.sendMessage(msg);

        } else if (Util.getNetworkType() == Util.NETTYPE_WIFI) {
            for (int i = 0; i < listData.size(); i++) {
                if (listData.get(i).mUrl != null && !listData.get(i).mUrl.isEmpty()) {
                    startDownloadInLow(listData.get(i));
                    LogUtil.d("testDownload", "startBatchMusicDownload1" + "   Util.NETTYPE_WIFI");
                } else {
                    new DownloadSongDetailTask(mContext, DownloaderPicture.this, listData.get(i).mKey, false, true).executeMultiTask();
                }
            }
            if (mRetryTasks != null && mRetryTasks.contains(listData)) {
                mRetryTasks.removeAll(listData);
            }
        }

    }


    public void startLyricDownload(String url, String name) {
        //mDownloaderConfig.mDefaultDownloadPath = Tools.getCommonDownloadPathLyric(mContext);
        mDownloaderConfig.mDefaultDownloadPath = FileManager.getLiricPath();
        Log.d("test", " url == " + url + "  name == " + name);
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.song_name = name;
        downloadTask.mUrl = url;
        downloadTask.isLyric = true;
        startDownloadInLowFirst(downloadTask);
    }


    public void pauseDownload(DownloadTask item) {
        item.isCancel = true;
        mDbManager.saveOrUpdate(item);
    }

    public void deleteDownload(DownloadTask item) {
        item.isCancel = true;
        if (mAllTasks.contains(item)) {
            synchronized (mAllTasks) {
                mAllTasks.remove(item);
            }
        }
        if (mRetryTasks.contains(item)) {
            synchronized (mRetryTasks) {
                mRetryTasks.remove(item);
            }
        }
        mDbManager.deleteById(item.mKey);
        item.resetTask();

    }

    @Override
    public void stopAllDownload() {
        synchronized (mCurrentTasks) {
            mCurrentTasks.clear();
        }
        synchronized (mResumeLowTasks) {
            mResumeLowTasks.clear();
        }

        mWaitingTasks.clear();
        synchronized (mAllTasks) {
            mAllTasks.clear();
        }
        ArrayList<DownloadTask> runningTask = new ArrayList<>(mDownloadingTasks);
        for (int i = 0; i < runningTask.size(); i++) {
            runningTask.get(i).isCancel = true;
        }
        mDbManager.updateAll(runningTask);
//        SqliteUtility.getInstance().update(null, runningTask);
    }

    public void onTaskGoing(DownloadTask task) {
        mDownloadingTasks.add(task);
    }

    public void onTaskStop(DownloadTask item) {
        mDownloadingTasks.remove(item);
        synchronized (mCurrentTasks) {
            mCurrentTasks.remove(item);
        }
       /* synchronized (mAllTasks) {
            mAllTasks.remove(item);
        }*/
        mDbManager.update(item);
//        SqliteUtility.getInstance().update(null, item);
        //需要时，恢复自动下载
        // resumeLowTasks();
    }

    public void onTaskStop1(DownloadTask item) {
        mDownloadingTasks.remove(item);
        synchronized (mCurrentTasks) {
            mCurrentTasks.remove(item);
        }

        mWaitingTasks.remove(item);

//        SqliteUtility.getInstance().update(null, item);
        mDbManager.update(item);
        //需要时，恢复自动下载
        resumeLowTasks();
    }

    public void addDownloadListener(IDownloadListener downloadListener) {
        mEventCenter.addDownloadListener(downloadListener);
    }

    public void removeDownloadListener(IDownloadListener downloadListener) {
        mEventCenter.removeDownloadListener(downloadListener);
    }


    public DownloadEventCenter getEventCenter() {
        return mEventCenter;
    }


    public void retry(DownloadTask task) {
        synchronized (mRetryTasks) {
            if (!mRetryTasks.contains(task)) {
                mRetryTasks.add(task);
            }
        }
    }

    public void quit() {
        for (int i = 0; i < mThreads.length; i++) {
            mThreads[i].mCancel = true;
        }
        stopAllDownload();
        mContext.unregisterReceiver(mNetworkMonitorReceiver);
    }


    private BroadcastReceiver mNetworkMonitorReceiver = new BroadcastReceiver() {
        private static final String TAG = "mNetworkMonitorReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mContext == null) {
                mContext = context.getApplicationContext();
            }
            if (!intent.getAction().equalsIgnoreCase(
                    ConnectivityManager.CONNECTIVITY_ACTION)) {
                return;
            }

            DLog.v("网络变化");
            // isBreak为false 有网
            mIsBreak = intent.getBooleanExtra(
                    ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            DLog.d("网络变化" + mIsBreak);
            if (mIsBreak || mIsFirstNetworkChange) {
                mIsFirstNetworkChange = false;
                return;
            }
            mHandlerThreadHandler.removeMessages(0);
            LogUtil.d("testDownload", "handler.sendMessageDelayed");
            mHandlerThreadHandler.sendMessageDelayed(Message.obtain(mHandlerThreadHandler, 0, DownloaderPicture.this), 3000);
        }

    };

    @Override
    public void onLoadSuccess(int dataType, List datas, boolean isBatchDownload, boolean isDownloadMusic) {
        List<SongDetailBean> list = datas;
        String url = Encryptor.decryptUrl(list.get(0).listen_file);
        LogUtil.i("getNetworkType", "song url =" + url + " list.get(0).song_id" + list.get(0).song_id);
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.mStatus = DownloadStatus.NEW;
        downloadTask.isBatchDownload = isBatchDownload;
        downloadTask.mKey = list.get(0).song_id;
        downloadTask.mName = list.get(0).song_name;
        downloadTask.song_name = list.get(0).song_name;
        downloadTask.mUrl = Util.getMediaUrl(list.get(0).listen_file);
        downloadTask.artist_name = list.get(0).artist_name;
        downloadTask.album_logo = list.get(0).album_logo;
        downloadTask.album_name = list.get(0).album_name;
        downloadTask.artist_logo = list.get(0).artist_logo;
        downloadTask.length = list.get(0).length;
        downloadTask.isLyric = false;
        mDownloaderConfig.mDefaultDownloadPath = Tools.getCommonDownloadPath1(mContext);
        if (isDownloadMusic) {
            startDownloadInLowFirst(downloadTask);
        } else {
            addTaskFirstInMobileNetWork(downloadTask);
        }

        if (!TextUtils.isEmpty(list.get(0).lyric)) {
            LogUtil.d("lyric...", list.get(0).lyric);
            if (!FileManager.isLrcFileExisted(list.get(0).song_id)) {
                //开始下载歌词
                LogUtil.d("lyric...", "开始下载歌词" + list.get(0).lyric);
                startLyricDownload(list.get(0).lyric, list.get(0).song_id);
            }
        }

    }

    @Override
    public void onLoadFail(int dataType, String message) {
        LogUtil.d("mytest", "onLoadFail" + message);
        ToastUtil.showToast(mContext, message);
    }

    @Override
    public void startPictureDownload(DownloadTask task) {

    }
}
