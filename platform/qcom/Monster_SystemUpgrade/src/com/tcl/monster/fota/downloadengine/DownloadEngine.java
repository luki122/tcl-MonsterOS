package com.tcl.monster.fota.downloadengine;

import android.content.Context;
import android.text.TextUtils;

import com.tcl.monster.fota.FotaUIPresenter;
import com.tcl.monster.fota.misc.State;
import com.tcl.monster.fota.provider.Fota;
import com.tcl.monster.fota.utils.FotaLog;

import java.util.HashMap;
import java.util.Iterator;

public class DownloadEngine {

    private static final String TAG = DownloadEngine.class.getSimpleName();

    private static DownloadEngine sInstance;
    private Context mContext;
    private HashMap<DownloadTask, TaskController> mTaskWithController = new HashMap<DownloadTask, TaskController>();

    private HashMap<DownloadTask, DownloadListener> mTaskListeners = new HashMap<DownloadTask, DownloadListener>();

    private DownloadEngine() {
    }

    public static synchronized DownloadEngine getInstance() {
        if (sInstance == null) {
            sInstance = new DownloadEngine();
        }

        return sInstance;
    }

    public void init(Context context) {
        mContext = context.getApplicationContext();
    }

    public void saveDownloadTask(DownloadTask task) {
        if (task.getId() == null) {
            throw new RuntimeException("a task must have an id");
        }
        Fota.Firmware.saveDownloadTask(mContext.getContentResolver(), task);
    }

    public String addDownloadTask(DownloadTask task, DownloadListener listener) {
        if (listener != null) {
            mTaskListeners.put(task, listener);
        }

        TaskController controller = new TaskController(this, task);

        controller.downloadTask();
        mTaskWithController.put(task, controller);

        FotaLog.d(TAG, "addDownloadTask -> downloadTaskId = " + task.getId());

        return task.getId();
    }

    public void removeDownloadTaskListener(DownloadTask task) {
        FotaLog.v(TAG, "try to removeDownloadTaskListener");
        if (task == null || !mTaskListeners.containsKey(task)) {
            return;
        }

        FotaLog.v(TAG, "removeDownloadTaskListener -> downloadTaskId = " + task.getId());
        mTaskListeners.remove(task);
    }

    public void pauseDownloadTask(DownloadTask task, DownloadListener listener) {
        TaskController controller = mTaskWithController.get(task);
        FotaLog.v(TAG, "pauseDownloadTask -> downloadTaskId = " + task.getId()
                + ", controller = " + controller + ", listener = " + listener);
        if (controller != null) {
            controller.pauseDownload();
        } else if (listener != null) {
            task.setState(State.PAUSED.name());
            Fota.Firmware.updateDownloadTask(mContext.getContentResolver(), task);
            listener.onDownloadPaused(task);
        }
    }

    public void resumeDownloadTask(DownloadTask task, DownloadListener listener) {
        TaskController controller = mTaskWithController.get(task);
        FotaLog.v(TAG, "resumeDownloadTask -> downloadTaskId = " + task.getId()
                + ", controller = " + controller + ", listener = " + listener);
        /*if (controller != null) {
            controller.resumeDownload();
        } else {
            addDownloadTask(task, listener);
        }*/
        if (controller != null) {
            removeTask(task);
        }
        addDownloadTask(task, listener);
    }

    public void deleteDownloadTask(DownloadTask task, DownloadListener listener) {
        TaskController controller = mTaskWithController.get(task);
        FotaLog.v(TAG, "deleteDownloadTask -> downloadTaskId = " + task.getId()
                + ", controller = " + controller + ", listener = " + listener);
        if (controller != null) {
            controller.deleteDownload();
        } else if (listener != null) {
            Fota.Firmware.deleteDownloadTask(mContext.getContentResolver(), task);
            listener.onDownloadDeleted(task);
            removeTask(task);
        }
    }

    public DownloadTask findDownloadTaskByTaskId(String id) {
        if (TextUtils.isEmpty(id)) {
            return null;
        }
        Iterator<DownloadTask> iterator = mTaskWithController.keySet().iterator();
        while (iterator.hasNext()) {
            DownloadTask task = iterator.next();
            if (!TextUtils.isEmpty(task.getId()) && task.getId().equals(id)) {
                FotaLog.v(TAG, "findDownloadTaskByTaskId -> got one task from mTaskWithController");
                TaskController controller = mTaskWithController.get(task);
                if (controller != null) {
                    FotaLog.v(TAG, "findDownloadTaskByTaskId -> ongoing TaskController status : \n"
                            + controller.status());
                }
                return task;
            }
        }

		FotaLog.v(TAG, "findDownloadTaskByTaskId -> find task from Db");
        return Fota.Firmware.findDownloadTaskById(mContext.getContentResolver(), id);
    }

    void onDownloading(final DownloadTask task) {
        final DownloadListener listener = mTaskListeners.get(task);
        FotaLog.v(TAG, "onDownloading -> listener = " + listener);
        Fota.Firmware.updateDownloadTask(mContext.getContentResolver(), task);
        if (listener != null) {
            listener.onDownloadUpdated(task);
        }
    }

    void onDownloadPaused(final DownloadTask task) {
        final DownloadListener listener = mTaskListeners.get(task);
        FotaLog.v(TAG, "onDownloadPaused -> listener = " + listener);
        Fota.Firmware.updateDownloadTask(mContext.getContentResolver(), task);
        if (listener != null) {
            listener.onDownloadPaused(task);
        }
    }

    void onDownloadDeleted(final DownloadTask task) {
        final DownloadListener listener = mTaskListeners.get(task);
        FotaLog.v(TAG, "onDownloadDeleted -> listener = " + listener);
        Fota.Firmware.deleteDownloadTask(mContext.getContentResolver(), task);
        if (listener != null) {
            listener.onDownloadDeleted(task);
        }
        removeTask(task);
    }

    void onDownloadSuccessed(final DownloadTask task) {
        final DownloadListener listener = mTaskListeners.get(task);
        Fota.Firmware.updateDownloadTask(mContext.getContentResolver(), task);
        if (listener != null) {
            listener.onDownloadSuccessed(task);
        }
        removeTask(task);
    }

	//GAPP Bug1535113 don't modify
	void onDownloadFailed(final DownloadTask task) {
        final DownloadListener listener = mTaskListeners.get(task);
        removeTask(task);
        FotaUIPresenter fotaUIPresenter = FotaUIPresenter.getInstance(mContext);
        int pauseReason = task.getPausedReason();
        FotaLog.d(TAG, "onDownloadFailed -> listener = " + listener
                + ", pauseReason = " + pauseReason);
        if (pauseReason == Fota.Firmware.PAUSED_REASON_NETWORK
                || pauseReason == Fota.Firmware.PAUSED_REASON_SERVER_ERROR) {
            fotaUIPresenter.updateCurrentDownloadTask(task);
            //fotaUIPresenter.showWarning(FotaUIPresenter.FOTA_WARNING_TYPE_DOWNLOAD_FAIL_NETWORK_REASON);
        } else if (pauseReason == Fota.Firmware.PAUSE_REASON_STORAGE_NOT_ENOUGH) {
            fotaUIPresenter.updateCurrentDownloadTask(task);
            fotaUIPresenter.showDownloadResult(
                    FotaUIPresenter.FOTA_DOWNLOAD_RESULT_STORAGE_NOT_ENOUGH);
        } else {
            task.setPausedReason(Fota.Firmware.PAUSED_REASON_NETWORK);
            fotaUIPresenter.updateCurrentDownloadTask(task);
        }
        Fota.Firmware.updateDownloadTask(mContext.getContentResolver(), task);
    }

    private void removeTask(DownloadTask task) {
        mTaskWithController.remove(task);
        mTaskListeners.remove(task);
        FotaLog.v(TAG, "removeTask");
    }

    public boolean checkAllThreadsPaused(DownloadTask task) {
        TaskController controller = mTaskWithController.get(task);
        if (controller != null) {
            return controller.checkAllThreadsPaused();
        }
        return true;
    }

    public boolean checkThreadsMissing(DownloadTask task) {
        TaskController controller = mTaskWithController.get(task);
        return controller == null;
    }
	
    public void pauseDownloadTaskbyNet(DownloadTask task) {
        FotaLog.v(TAG, "pauseDownloadTaskbyNet -> downloadTaskId = " + task.getId());
        TaskController controller = mTaskWithController.get(task);
        if (controller != null) {
            controller.pauseDownload();
        } else  {
            task.setState(State.PAUSED.name());
            Fota.Firmware.updateDownloadTask(mContext.getContentResolver(), task);
        }
    }
}