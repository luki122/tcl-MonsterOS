package com.monster.cloud.sync;

import android.util.Log;

import com.tencent.qqpim.sdk.accesslayer.def.PMessage;
import com.tencent.qqpim.sdk.accesslayer.interfaces.basic.ISyncProcessorObsv;
import com.tencent.qqpim.sdk.defines.DataSyncResult;
import com.tencent.qqpim.sdk.defines.ISyncMsgDef;

import java.util.List;

/**
 * 腾讯SDK同步处理器的观察者封装
 *
 * Created by logic on 16-12-15.
 */
public class SyncProcessorObserver implements ISyncProcessorObsv {

    private final TCLSyncManager.UIHandler mUiHandler;
    private final BaseSyncTask task;


    public SyncProcessorObserver(BaseSyncTask task, TCLSyncManager.UIHandler handler) {
        this.mUiHandler = handler;
        this.task = task;
    }

    @Override
    public void onSyncStateChanged(PMessage pmsg) {
        switch (pmsg.msgId) {
            case ISyncMsgDef.ESTATE_SYNC_PROGRESS_CHANGED:
                Log.v("BaseSyncTask", "progress " + pmsg.arg1);
                mUiHandler.sendMessage(mUiHandler.obtainMessage(
                        TCLSyncManager.MAIN_MSG_TASK_PROGRESS_CHANGED,
                        pmsg.arg1, -1, task));
                break;
            case ISyncMsgDef.ESTATE_SYNC_ALL_FINISHED:
                Log.v("BaseSyncTask", "progress " + pmsg.arg1 + ", type =" + task.getTaskType());
                task.setFinished();
                handleSyncFinished(pmsg);
                break;
            case ISyncMsgDef.ESTATE_SYNC_ALL_BEGIN:
            case ISyncMsgDef.ESTATE_SYNC_SCAN_BEGIN:
            case ISyncMsgDef.ESTATE_SYNC_SCAN_FINISHED:
            case ISyncMsgDef.ESTATE_SYNC_DATA_REARRANGEMENT_BEGIN:
            case ISyncMsgDef.ESTATE_SYNC_DATA_REARRANGEMENT_FINISHED:
            default:
                break;
        }
    }

    @Override
    public void onLoginkeyExpired() {
        task.setFinished();
        task.setResultCode(BaseSyncTask.SYNC_ERR_TYPE_RELOGIN);
    }

    private void handleSyncFinished(PMessage msg) {
        List<DataSyncResult> resultList = null;
        if (null != msg.obj1) {
            resultList = (List<DataSyncResult>) msg.obj1;
        }
        if (null == resultList) {
            return;
        }
        int size = resultList.size();
        DataSyncResult result;
        for (int i = 0; i < size; i++) {
            result = resultList.get(i);
            task.setResultCode(result.getResult());
            // TODO: 16-12-15 log显示下一个同步任务到底有多少个 DataSyncResult。
        }
    }
}
