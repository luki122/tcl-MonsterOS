package com.monster.paymentsecurity.scan;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.IntDef;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;

import com.monster.paymentsecurity.diagnostic.RiskOrError;
import com.monster.paymentsecurity.util.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 扫描Engine
 * Created by logic on 16-11-21.
 */
public class ScanningEngine {
    private static final String TAG = "ScanningEngine";

    @IntDef(value = {SCANNER_MSG_EXECUTE_A_TASK_FOR_OB,
            SCANNER_MSG_EXECUTE_SET_OF_TASKS_FOR_OB,
            SCANNER_MSG_PAYMENT_SECURE_TASKS,
            SCANNER_MSG_PAYMENT_SECURE_SINGLE_TASK})
    @Retention(RetentionPolicy.SOURCE)
    @interface ScanHandlerType {
    }

    private static final int SCANNER_MSG_EXECUTE_A_TASK_FOR_OB = 100;
    private static final int SCANNER_MSG_EXECUTE_SET_OF_TASKS_FOR_OB = 101;
    private static final int SCANNER_MSG_PAYMENT_SECURE_TASKS = 102;
    private static final int SCANNER_MSG_PAYMENT_SECURE_SINGLE_TASK = 103;

    private static final int MAIN_MSG_HANDLE_RESULT_FINISHED = 1000;
    private static final int MAIN_MSG_HANDLE_RESULT_CANCELED = 1001;
    private static final int MAIN_MSG_HANDLE_RESULT_SCANNING = 1002;
    private static final int MAIN_MSG_HANDLE_RESULT_PROGRESS = 1004;

    @IntDef(value = {CANCEL, SCANNING, FINISHED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScanningState {
    }

    public static final int CANCEL = 1;
    public static final int SCANNING = 2;//扫描开始
    public static final int FINISHED = 3;

    private Context context;

    private HandlerThread workThread;
    private WorkHandler workHandler;
    private UIHandler uiHandler;
    private AtomicBoolean shutdown = new AtomicBoolean(true);

    //记录all allObservers
    private final ArraySet<ScanningResultObserver> allObservers = new ArraySet<>(4);
    //记录all tasks
    private final ArrayList<BaseScanTask> allTasks = new ArrayList<>();
    //observer 和　tasks map
    private final ArrayMap<ScanningResultObserver, ArrayList<BaseScanTask>> obMapTasks = new ArrayMap<>(4);

    public interface ScanningResultObserver {
        /**
         * 某扫描完成，通知观察者显示结果
         *
         * @param result   当前扫描完成
         * @param progress 0~1 , 1表示扫描结束
         */
        void notifyScanningResult(Result result, float progress);

        /**
         * 通知扫描状态变化
         *
         * @param state 　扫描器状态
         */
        void notifyScanningState(@ScanningState int state);
    }

//    private static ScanningEngine instance;

//    public static ScanningEngine getInstance(Context context){
//        if (instance != null)
//            return instance;
//        synchronized (ScanningEngine.class){
//            if (instance == null){
//                instance = new ScanningEngine(context);
//            }
//            return instance;
//        }
//    }

    public ScanningEngine(Context context) {
        this.context = context.getApplicationContext();
        init(context);
    }


    private void init(Context context) {
        Log.v(TAG, "ScanningEngine start...");
        if (workThread != null && workThread.isAlive()) {
            Log.w(TAG, "stop old scanning thread!!");
            stopEngine();
        }
        this.workThread = new ScanerThread(context);
        workThread.start();//先start才能拿到looper
        this.workHandler = new WorkHandler(workThread.getLooper());
        this.uiHandler = new UIHandler(this);
        shutdown.set(false);
    }


    /**
     * 启动扫描任务：全盘扫描，执行所有任务
     *
     * @param ob 观察者
     */
    public void startScanning(ScanningResultObserver ob) {
        List<BaseScanTask> tasks;
        synchronized (this) {
            allObservers.add(ob);
            if (!obMapTasks.containsKey(ob)) {
                obMapTasks.put(ob, new ArrayList<>());
            }
            tasks = obMapTasks.get(ob);
            tasks.addAll(ScanningHelper.getAllScanTasks(context));
            Collections.sort(tasks, new ScanTaskComparator());
            allTasks.addAll(tasks);
        }
        sendScanMessage(SCANNER_MSG_PAYMENT_SECURE_TASKS, new ObTasksPair(ob, tasks));
    }

    /***
     * 　启动扫描任务：插入到队列前执行任务
     *
     * @param observer 此次任务的监听
     * @param task     　一次扫描任务
     */
    public void startScanning(ScanningResultObserver observer, BaseScanTask task) {
        synchronized (this) {
            allObservers.add(observer);
            ArrayList<BaseScanTask> tasks;
            if ((tasks = obMapTasks.get(observer)) == null) {
                tasks = new ArrayList<>();
                obMapTasks.put(observer, tasks);
            }
            tasks.add(task);
            allTasks.add(task);
        }
        sendScanMessageAtFrontOfQueue(SCANNER_MSG_EXECUTE_A_TASK_FOR_OB, new ObTaskPair(observer, task));
    }

    /**
     * 启动扫描任务：插入到队列尾部执行一组任务
     *
     * @param observer 此组任务监听者
     * @param tasks    　一组任务
     */
    public void startScanning(ScanningResultObserver observer, List<BaseScanTask> tasks) {
        if (tasks == null)
            return;
        ArrayList<BaseScanTask> queueTasks;
        synchronized (this) {
            allObservers.add(observer);
            queueTasks = obMapTasks.get(observer);
            if (queueTasks == null) {
                queueTasks = new ArrayList<>(tasks);
                obMapTasks.put(observer, queueTasks);
            } else {
                queueTasks.addAll(tasks);
            }
            allTasks.addAll(tasks);
        }
        sendScanMessage(SCANNER_MSG_EXECUTE_SET_OF_TASKS_FOR_OB, new ObTasksPair(observer, queueTasks));
    }

    /**
     * 停止该监听器下所有扫描任务
     *
     * @param ob 观察者
     */
    public boolean stopScanning(ScanningResultObserver ob) {
        synchronized (this) {
            boolean ret = allObservers.remove(ob);
            ArrayList<BaseScanTask> queueTasks = obMapTasks.get(ob);
            if (queueTasks != null) {
                queueTasks.forEach(BaseScanTask::cancel);
                allTasks.removeAll(queueTasks);
            }
            obMapTasks.remove(ob);
            return ret;
        }
    }

    /**
     * 停止所有扫描任务, 线程停止
     *
     * @return true or false
     */
    public boolean stopScanning() {
        return stopEngine();
    }

    private boolean stopEngine() {
        synchronized (this) {
            allObservers.clear();
            allTasks.forEach(BaseScanTask::cancel);
            allTasks.clear();
            obMapTasks.clear();

        }
        Log.v(TAG, "ScanningEngine stop...");
        context = null;
        boolean ret = workThread.quitSafely();
        shutdown.set(true);
        Runtime.getRuntime().gc();
        return ret;
    }


    private void sendScanMessage(int msgType, ObTasksPair pair) {
        Message msg = Message.obtain(workHandler, msgType, pair);
        workHandler.sendMessage(msg);
    }

    private void sendScanMessageAtFrontOfQueue(int msgType, ObTaskPair pair) {
        Message msg = Message.obtain(workHandler, msgType, pair);
        workHandler.sendMessageAtFrontOfQueue(msg);
    }

    private void sendMainMessage(int mainMsgType, int arg1, int arg2, ObResultPair pair) {
        Message msg = uiHandler.obtainMessage(mainMsgType, arg1, arg2, pair);
        uiHandler.sendMessage(msg);
    }

    private static class ScanerThread extends HandlerThread {
        private final WeakReference<Context> mContext;

        ScanerThread(Context context) {
            super("SecurityScanner", Process.THREAD_PRIORITY_BACKGROUND);
            this.mContext = new WeakReference<>(context);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();

            final Context context = mContext.get();
            if (context == null) return;
            Utils.initTMSDK(context.getApplicationContext());
        }
    }

    private final class WorkHandler extends Handler {

        private WorkHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            @ScanHandlerType int what = msg.what;
            switch (what) {
                case SCANNER_MSG_EXECUTE_A_TASK_FOR_OB: {//单个Task执行
                    handleSingleTaskMsgLocked(msg);
                    break;
                }
                case SCANNER_MSG_EXECUTE_SET_OF_TASKS_FOR_OB: {//一组task
                    handleTasksMsgLocked(msg);
                    break;
                }
                case SCANNER_MSG_PAYMENT_SECURE_TASKS: {//支付安全扫描
                    handlePaymentSecureTasksMsgLocked(msg);
                    break;
                }
                case SCANNER_MSG_PAYMENT_SECURE_SINGLE_TASK: {//支付安全扫描
                    handlePaymentSecureSingleTaskMsgLocked(msg);
                    break;
                }
                default:
                    Log.w(TAG, "unknown scan message type!");
                    break;
            }

        }
    }

    //处理单个任务
    private void handleSingleTaskMsgLocked(Message msg) {
        ObTaskPair pair = (ObTaskPair) msg.obj;
        final BaseScanTask curTask = pair.task;
        final ScanningResultObserver observer = pair.ob;

        if (curTask.isCanceled()) {
            sendMainMessage(MAIN_MSG_HANDLE_RESULT_CANCELED, -1, -1, new ObResultPair(observer, null));
            synchronized (this) {
                removeObserverAndTask(observer, curTask);
            }
            return;
        }
        sendMainMessage(MAIN_MSG_HANDLE_RESULT_SCANNING, 0, 1, new ObResultPair(observer, null));
        try{
            curTask.run();
        }catch (Exception e) {
            e.printStackTrace();
        }
        Result result = curTask.getResult();
        if (!curTask.isCanceled()) {
            sendMainMessage(MAIN_MSG_HANDLE_RESULT_PROGRESS, 1, 1, new ObResultPair(observer, result));
            sendMainMessage(MAIN_MSG_HANDLE_RESULT_FINISHED, 1, 1, new ObResultPair(observer, null));
        } else {
            sendMainMessage(MAIN_MSG_HANDLE_RESULT_CANCELED, -1, -1, new ObResultPair(observer, null));
        }
        synchronized (this) {
            removeObserverAndTask(observer, curTask);
        }
    }

    //处理一组任务
    private void handleTasksMsgLocked(Message msg) {
        ObTasksPair pair = (ObTasksPair) msg.obj;
        final ScanningResultObserver ob = pair.observer;
        final List<BaseScanTask> tasks = pair.tasks;

        synchronized (this) {
            if (!allObservers.contains(ob)) {
                sendMainMessage(MAIN_MSG_HANDLE_RESULT_CANCELED, -1, -1, new ObResultPair(ob, null));
                removeObserverAndTasks(ob, tasks);
                return;
            }
        }

        sendMainMessage(MAIN_MSG_HANDLE_RESULT_SCANNING, 0, 1, new ObResultPair(ob, null));
        for (int i = 0; i < tasks.size(); i++) {
            BaseScanTask curTask = tasks.get(i);
            try{
                curTask.run();
            }catch (Exception e) {
                e.printStackTrace();
            }
            Result result = curTask.getResult();
            if (allObservers.contains(ob)) {
                if (!curTask.isCanceled())
                    sendMainMessage(MAIN_MSG_HANDLE_RESULT_PROGRESS, i + 1, tasks.size(), new ObResultPair(ob, result));
            } else {
                break;
            }
        }

        synchronized (this) {
            if (allObservers.contains(ob)) {
                int size = tasks.size();
                sendMainMessage(MAIN_MSG_HANDLE_RESULT_FINISHED, size, size, new ObResultPair(ob, null));
            } else {
                sendMainMessage(MAIN_MSG_HANDLE_RESULT_CANCELED, -1, -1, new ObResultPair(ob, null));
            }
            removeObserverAndTasks(ob, tasks);
        }
    }

    //处理支付安全扫描任务组: 支付安全扫描时，进度按权重来计算，total = 100
    private void handlePaymentSecureTasksMsgLocked(Message msg) {
        ObTasksPair pair = (ObTasksPair) msg.obj;
        final List<BaseScanTask> tasks = pair.tasks;
        final ScanningResultObserver ob = pair.observer;

        synchronized (this) {
            if (!allObservers.contains(ob)) {
                sendMainMessage(MAIN_MSG_HANDLE_RESULT_CANCELED, -1, -1, new ObResultPair(ob, null));
                removeObserverAndTasks(ob, tasks);
                return;
            }
        }

        int size = tasks.size();
        int progress = 0;
        for (int i = 0; i < size; i++) {
            BaseScanTask task = tasks.get(i);
            ObTaskPair newPair = new ObTaskPair(ob, task);
            progress += calculateWeight(tasks, task);
            Message newMsg = workHandler.obtainMessage(
                    SCANNER_MSG_PAYMENT_SECURE_SINGLE_TASK,
                    (i ==0 ? 1 : (i == (size-1) ? 100 : -1)),//1,-1,100
                    progress,
                    newPair);
            workHandler.sendMessageDelayed(newMsg, i == 0 ? 0 : 10);
        }
    }

    //处理支付安全扫描任务: 支付安全扫描时，进度按权重来计算
    private void handlePaymentSecureSingleTaskMsgLocked(Message msg) {
        ObTaskPair pair = (ObTaskPair) msg.obj;
        final BaseScanTask curTask = pair.task;
        final ScanningResultObserver ob = pair.ob;

        synchronized (this) {
            if (!allObservers.contains(ob)) {
                removeObserverAndTasks(ob, obMapTasks.get(ob));
                sendMainMessage(MAIN_MSG_HANDLE_RESULT_CANCELED, -1, -1, new ObResultPair(ob, null));
                workHandler.removeMessages(SCANNER_MSG_PAYMENT_SECURE_SINGLE_TASK);
                return;
            }
        }

        int index = msg.arg1;
        int progress = msg.arg2;
        progress = progress >= 100? 100 : progress;
        if (progress == 100 && index < 100)
            progress -= 1;
        if (index == -1) {
            sendMainMessage(MAIN_MSG_HANDLE_RESULT_SCANNING, 0, 1, new ObResultPair(ob, null));
        }

        try{
            curTask.run();
        }catch (Exception e) {
            e.printStackTrace();
        }

        Result result = curTask.getResult();

        synchronized (this) {
            if (allObservers.contains(ob)) {
                if (!curTask.isCanceled()) {
                    sendMainMessage(MAIN_MSG_HANDLE_RESULT_PROGRESS,
                            progress,
                            100,
                            new ObResultPair(ob, result));
                }
                removeObserverAndTask(ob, curTask);
            } else {
                sendMainMessage(MAIN_MSG_HANDLE_RESULT_CANCELED, -1, -1, new ObResultPair(ob, null));
                removeObserverAndTasks(ob, obMapTasks.get(ob));
                workHandler.removeMessages(SCANNER_MSG_PAYMENT_SECURE_SINGLE_TASK);
                return;
            }
        }

        //progress存在一定误差, 所以需index判断
        if (index == 100) {
            sendMainMessage(MAIN_MSG_HANDLE_RESULT_FINISHED, 1, 1, new ObResultPair(ob, null));
        }
    }


    private int calculateWeight(List<BaseScanTask> tasks, BaseScanTask task){
        //除数不能为0, 由于返回int，存在一定误差
        int weight = Math.round((100 / getCategoryCount(tasks)) / getCategoryTasksCount(tasks, task));
        return weight == 0? 1: weight;
    }

    //计算task 在tasks中同类除自己外的同类任务数， tasks中包含了task, 所以返回值不可能为0
    private int getCategoryTasksCount(List<BaseScanTask>tasks, BaseScanTask task){
        int count = 0;
        @RiskOrError.RiskCategory int category = ScanningHelper.convertScanTypeToCategory(task.getScanType());
        for (BaseScanTask t: tasks){
            if (category ==
                    ScanningHelper.convertScanTypeToCategory(t.getScanType()))
                count++;
        }
        return count;
    }

    //计算tasks中有多少个category
    private float getCategoryCount(List<BaseScanTask>tasks){
        int count = 0;
        @RiskOrError.RiskCategory int category = 0;
        int temp;
        for (BaseScanTask t: tasks){
            temp = ScanningHelper.convertScanTypeToCategory(t.getScanType());
            if (temp != category) {
                count++;
                category = temp ;
            }
        }
        return count;
    }

    private void removeObserverAndTask(ScanningResultObserver observer, BaseScanTask curTask) {
        allTasks.remove(curTask);
        List<BaseScanTask> tasks = obMapTasks.get(observer);
        if (tasks != null) {
            tasks.remove(curTask);
            if (tasks.size() == 0) {
                allObservers.remove(observer);
                obMapTasks.remove(observer);
            }
        }
        allTasks.remove(curTask);
    }

    private void removeObserverAndTasks(ScanningResultObserver ob, List<BaseScanTask> tasks) {
        allTasks.removeAll(tasks);
        List<BaseScanTask> mapTasks = obMapTasks.get(ob);
        if (mapTasks != null) {
            mapTasks.removeAll(tasks);
            if (mapTasks.size() == 0) {
                allObservers.remove(ob);
                obMapTasks.remove(ob);
            }
        }
        allTasks.removeAll(tasks);
    }

    private final static class UIHandler extends Handler {
        WeakReference<ScanningEngine> weakEngine;

        private UIHandler(ScanningEngine engine) {
            super(Looper.getMainLooper());
            weakEngine = new WeakReference<ScanningEngine>(engine);
        }

        @Override
        public void handleMessage(Message msg) {
            ScanningEngine engine = weakEngine.get();
            if (null == engine || engine.shutdown.get()) return;
            int what = msg.what;
            switch (what) {
                case MAIN_MSG_HANDLE_RESULT_FINISHED: {
                    ObResultPair pair = (ObResultPair) msg.obj;
                    final ScanningResultObserver ob = pair.ob;
                    ob.notifyScanningState(FINISHED);
                    break;
                }
                case MAIN_MSG_HANDLE_RESULT_CANCELED: {
                    // 有可能页面已释放 
                    ObResultPair pair = (ObResultPair) msg.obj;
                    final ScanningResultObserver ob = pair.ob;
                    ob.notifyScanningState(CANCEL);
                    break;
                }
                case MAIN_MSG_HANDLE_RESULT_SCANNING: {
                    ObResultPair pair = (ObResultPair) msg.obj;
                    final ScanningResultObserver ob = pair.ob;
                    ob.notifyScanningState(SCANNING);
                    break;
                }
                case MAIN_MSG_HANDLE_RESULT_PROGRESS: {
                    ObResultPair pair = (ObResultPair) msg.obj;
                    final ScanningResultObserver ob = pair.ob;
                    final Result result = pair.result;
                    ob.notifyScanningResult(result, (msg.arg1 * 1.0f) / msg.arg2);
                    break;
                }
                default:
                    Log.w(TAG, "unknown main message type!");
            }
        }
    }

    private static final class ObTasksPair {
        final ScanningResultObserver observer;
        final List<BaseScanTask> tasks;

        private ObTasksPair(ScanningResultObserver observer, List<BaseScanTask> tasks) {
            this.observer = observer;
            this.tasks = tasks;
        }
    }

    private static final class ObTaskPair {
        final ScanningResultObserver ob;
        final BaseScanTask task;

        private ObTaskPair(ScanningResultObserver observer, BaseScanTask task) {
            this.ob = observer;
            this.task = task;
        }
    }

    private static final class ObResultPair {
        final ScanningResultObserver ob;
        final Result result;

        private ObResultPair(ScanningResultObserver ob, Result result) {
            this.ob = ob;
            this.result = result;
        }
    }

    private static class ScanTaskComparator implements Comparator<BaseScanTask> {

        @Override
        public int compare(BaseScanTask o1, BaseScanTask o2) {
            return o1.getPriority() - o2.getPriority();
        }
    }
}
