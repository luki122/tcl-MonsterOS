package com.monster.cloud.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;


import com.monster.cloud.service.SyncService;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 同步任务控制器
 * <p>
 * Created by logic on 16-12-15.
 */
public class TCLSyncManager {
    public static final String APPID = "101181845";

    //工作线程使用的消息
    private static final int SYNC_MSG_EXECUTE_ONE_KEY_ALL = 100;//一键同步
    private static final int SYNC_MSG_EXECUTE_ONE_TASK = 101;
    private static final int SYNC_MSG_EXECUTE_BACKGROUND_SYNC = 102;//后台同步

    //UI消息
    private static final int MAIN_MSG_TASK_BEGIN = 1000;
    public static final int MAIN_MSG_TASK_PROGRESS_CHANGED = 1001;
    private static final int MAIN_MSG_TASK_FINISHED = 1003;
    private static final int MAIN_MSG_SYNC_MGR_STATE = 1004;

    @IntDef(value = {TASK_STATE_BEGIN, TASK_STATE_FINSIHED, TASK_STATE_RUNNING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TaskState {
    }

    public static final int TASK_STATE_BEGIN = 0;
    public static final int TASK_STATE_FINSIHED = 1;
    public static final int TASK_STATE_RUNNING = 2;

    @IntDef(value = {MGR_STATE_START, MGR_STATE_STOP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MgrState {
    }

    public static final int MGR_STATE_START = 0;
    public static final int MGR_STATE_STOP = 1;

    private Context mContext;

    private HandlerThread mSyncThread;
    private SyncHandler mSyncHandler;
    private UIHandler mUiHandler;

    private ArraySet<SyncTaskProgressObserver> allObservers = new ArraySet<>();
    private ArrayMap<SyncTaskProgressObserver, List<BaseSyncTask>> obsToTasks = new ArrayMap();
    private List<BaseSyncTask> allTasks = new ArrayList<>();

    private SyncMgrStateObserver mgrStateObserver;

    public interface SyncTaskProgressObserver {
        void onSyncProgressChange(BaseSyncTask task, int progress);

        void onSyncTaskState(BaseSyncTask task, @TaskState int taskState);
    }

    public interface SyncMgrStateObserver {
        void notifySyncMgrStateChange(@MgrState int mgrState);
    }

    public TCLSyncManager(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void start() {
        if (mSyncThread != null && mSyncThread.isAlive()) {
            mSyncThread.quitSafely();
        }
        mSyncThread = new HandlerThread("SyncThread", HandlerThread.MIN_PRIORITY);
        mSyncThread.start();
        mSyncHandler = new SyncHandler(mSyncThread.getLooper());
        mUiHandler = new UIHandler();
        mUiHandler.sendMessageAtFrontOfQueue(mUiHandler.obtainMessage(MAIN_MSG_SYNC_MGR_STATE, MGR_STATE_START));
    }

    public void stop() {
        synchronized (this) {
            allObservers.clear();
            obsToTasks.clear();
            for (BaseSyncTask task:allTasks) {
                task.cancel();
            }
            allTasks.clear();
        }
        mgrStateObserver = null;
        mSyncThread.quitSafely();
        mUiHandler.sendMessageAtFrontOfQueue(mUiHandler.obtainMessage(MAIN_MSG_SYNC_MGR_STATE, MGR_STATE_STOP));
    }

    public void stop(SyncTaskProgressObserver observer) {
        synchronized (this) {
            allObservers.remove(observer);
            List<BaseSyncTask> tasks = obsToTasks.remove(observer);
            if (tasks != null) {
                for (BaseSyncTask task:tasks) {
                    task.cancel();
                }
                allTasks.removeAll(tasks);
            }
        }
    }

    public void setMgrStateObserver(SyncMgrStateObserver mgrStateObserver) {
        this.mgrStateObserver = mgrStateObserver;
    }

    private final class SyncHandler extends Handler {
        SyncHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SYNC_MSG_EXECUTE_ONE_KEY_ALL:
                    handleOneKeyAllMsgLocked((ObTasksPair) msg.obj);
                    break;
                case SYNC_MSG_EXECUTE_ONE_TASK:
                    handleOneSyncTaskMsgLocked((ObTaskPair) msg.obj);
                    break;
                case SYNC_MSG_EXECUTE_BACKGROUND_SYNC:
                    handleBackgroundSyncTaskMsgLocked((List<BaseSyncTask>) msg.obj);
                    break;
            }
        }
    }

    //一键同步
    public void onekeySync(SyncTaskProgressObserver ob) {
        List<BaseSyncTask> tasks;
        synchronized (this) {
            allObservers.add(ob);
            if (!obsToTasks.containsKey(ob)) {
                obsToTasks.put(ob, new ArrayList<BaseSyncTask>());
            }
            tasks = obsToTasks.get(ob);
            //add all kinds of tasks
            tasks.addAll(SyncHelper.getAllSyncTasks(mContext, mUiHandler));
            allTasks.clear();
            allTasks.addAll(tasks);
        }

        //一键同步，任务插入到最前面
        mSyncHandler.sendMessageAtFrontOfQueue(mSyncHandler.obtainMessage(SYNC_MSG_EXECUTE_ONE_KEY_ALL,
                new ObTasksPair(ob, tasks)));
    }

    //单项同步
    public void syncSignleTask(@BaseSyncTask.SyncTaskType int taskType,  SyncTaskProgressObserver ob) {
        final BaseSyncTask task;
        synchronized (this) {
            allObservers.add(ob);
            task = SyncHelper.getSyncTask(taskType, mContext, mUiHandler);
            if (!obsToTasks.containsKey(ob)) {
                obsToTasks.put(ob, new ArrayList<BaseSyncTask>(){
                    {
                        add(task);
                    }
                });
            }
            allTasks.add(task);
            //单项同步，任务插入到最前面
            mSyncHandler.sendMessageAtFrontOfQueue(mSyncHandler.obtainMessage(SYNC_MSG_EXECUTE_ONE_TASK,
                    new ObTaskPair(ob, task)));
        }
    }

    //后台同步
    public void backgroundSync() {
        List<BaseSyncTask> tasks ;
        synchronized (this) {
            tasks = SyncHelper.getBackgroundSyncTasks(mContext, mUiHandler);
            allTasks.addAll(tasks);
        }
        //后台同步，任务插入到最前面
        mSyncHandler.sendMessage(mSyncHandler.obtainMessage(SYNC_MSG_EXECUTE_BACKGROUND_SYNC,
                 tasks));
    }

    //run in handler thread
    private void handleOneKeyAllMsgLocked(ObTasksPair pair) {
        final List<BaseSyncTask> tasks = pair.tasks;
        final SyncTaskProgressObserver ob = pair.observer;
        synchronized (this) {
            if (!allObservers.contains(ob)) {
                return;
            }
        }
        for (int i = tasks.size()-1; i >= 0; --i) {
            //一键同步，任务插入到最前面
            mSyncHandler.sendMessageAtFrontOfQueue(mSyncHandler.obtainMessage(SYNC_MSG_EXECUTE_ONE_TASK,
                    new ObTaskPair(ob, tasks.get(i))));
        }
    }

    //run in handler thread
    private void handleOneSyncTaskMsgLocked(ObTaskPair pair) {
        final BaseSyncTask task = pair.task;
        final SyncTaskProgressObserver ob = pair.ob;

        if (task.isCanceled() || (ob !=null && !obsToTasks.containsKey(ob)))
            return;

        int resultCode;
        if (ob != null) {
            mUiHandler.sendMessage(mUiHandler.obtainMessage(MAIN_MSG_TASK_BEGIN, pair));
        }

        try {
            task.run();
            resultCode = task.getResultCode();
        } catch (Exception e) {
            e.printStackTrace();
            resultCode = BaseSyncTask.SYNC_ERR_TYPE_UNKNOW;
        }

        synchronized (this){
            removeTaskAndOb(ob, task);
        }

        if (task.isCanceled() || (ob !=null && !obsToTasks.containsKey(ob))) {
            mSyncHandler.removeMessages(SYNC_MSG_EXECUTE_ONE_TASK);
            return;
        }

        Log.v("BaseSyncTask", "resultCode =" +resultCode + ", type =" + task.getTaskType());
        if (ob != null) {
            mUiHandler.sendMessage(mUiHandler.obtainMessage(MAIN_MSG_TASK_FINISHED, resultCode, 0, pair));
        }

        handleSyncTaskErrorCode(resultCode);
    }

    //run in handler thread
    private void handleBackgroundSyncTaskMsgLocked(List<BaseSyncTask> tasks) {
        for (int i = 0; i < tasks.size(); i++) {
            //后台同步，任务延迟执行，优先级低
            mSyncHandler.sendMessageDelayed(mSyncHandler.obtainMessage(SYNC_MSG_EXECUTE_ONE_TASK,
                    new ObTaskPair(null, tasks.get(i))), i * 1000 * 60);
        }
    }

    //run in handler thread
    private void removeTaskAndOb(SyncTaskProgressObserver ob, BaseSyncTask task) {
        if (ob != null) {
            List<BaseSyncTask> curTasks =obsToTasks.get(ob);
            if (curTasks != null) {
                curTasks.remove(task);
            }
            if (curTasks == null || curTasks.size() == 0){
                allObservers.remove(ob);
            }
        }
        allTasks.remove(task);
    }

    //run in handler thread
    private void handleSyncTaskErrorCode(@BaseSyncTask.ResultCode int resultCode) {
        if (resultCode == BaseSyncTask.SYNC_ERR_TYPE_RELOGIN
                || resultCode == BaseSyncTask.SYNC_ERR_TYPE_BACK_SOFT_LOGINKEY_EXPIRE) {
            //登陆失效
            if (!SyncHelper.qqSdkLogin(mContext)) {
                mSyncHandler.getLooper().quitSafely();
                mUiHandler.sendMessageAtFrontOfQueue(mUiHandler.obtainMessage(MAIN_MSG_SYNC_MGR_STATE, MGR_STATE_STOP));
            }
        } else if (resultCode == BaseSyncTask.SYNC_ERR_TYPE_TIME_OUT) {//链接超时
            stop();
            mUiHandler.sendMessageAtFrontOfQueue(mUiHandler.obtainMessage(MAIN_MSG_SYNC_MGR_STATE, MGR_STATE_STOP));
        }
    }

    private static final class ObTasksPair {
        final SyncTaskProgressObserver observer;
        final List<BaseSyncTask> tasks;

        private ObTasksPair(SyncTaskProgressObserver observer, List<BaseSyncTask> tasks) {
            this.observer = observer;
            this.tasks = tasks;
        }
    }

    private static final class ObTaskPair {
        final SyncTaskProgressObserver ob;
        final BaseSyncTask task;

        private ObTaskPair(SyncTaskProgressObserver observer, BaseSyncTask task) {
            this.ob = observer;
            this.task = task;
        }
    }

    public final class UIHandler extends Handler {

        UIHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MAIN_MSG_TASK_BEGIN: {
                    ObTaskPair pair = (ObTaskPair) msg.obj;
                    SyncTaskProgressObserver ob = pair.ob;
                    ob.onSyncTaskState(pair.task, TASK_STATE_BEGIN);
                    break;
                }
                case MAIN_MSG_TASK_PROGRESS_CHANGED: {
                    BaseSyncTask task = (BaseSyncTask) msg.obj;
                    SyncTaskProgressObserver ob = findObserver(task);
                    if (ob != null) {
                        ob.onSyncProgressChange(task, msg.arg1);
                        ob.onSyncTaskState(task, TASK_STATE_RUNNING);
                    }
                    break;
                }
                case MAIN_MSG_TASK_FINISHED: {
                    ObTaskPair pair = (ObTaskPair) msg.obj;
                    SyncTaskProgressObserver ob = pair.ob;
                    ob.onSyncTaskState(pair.task, TASK_STATE_FINSIHED);
                    break;
                }
                case MAIN_MSG_SYNC_MGR_STATE: {
                    if (mgrStateObserver != null) {
                        mgrStateObserver.notifySyncMgrStateChange(msg.arg1);
                    }
                    break;
                }
            }
        }
    }

    private synchronized SyncTaskProgressObserver findObserver(BaseSyncTask task) {
        if (allObservers.size() == 0) {
            return null;
        }
        for (SyncTaskProgressObserver ob : allObservers) {
            List<BaseSyncTask> tasks = obsToTasks.get(ob);
            if (tasks == null) continue;
            if (tasks.contains(task)) return ob;
            continue;
        }
        return null;
    }

    public int getTasksNum() {
        return allTasks.size();
    }
}
