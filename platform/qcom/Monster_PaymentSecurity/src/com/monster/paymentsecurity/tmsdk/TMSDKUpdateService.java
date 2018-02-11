package com.monster.paymentsecurity.tmsdk;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.monster.paymentsecurity.constant.Constant;
import com.monster.paymentsecurity.util.Utils;

import java.lang.ref.WeakReference;

import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.update.UpdateManager;

/**
 * 病毒库更新服务，采用IntentService, 自动结束
 * Created by logic on 16-12-8.
 */
public final class TMSDKUpdateService extends IntentService {

    private static final String TAG = "TMSDKUpdateService";
    public static final String VIRUS_LIB_UPDATE_TIME = "virus_lib_update_time";

    private WeakReference<UpdateManager> mWeakUpdateManager;
    private volatile  boolean updating = false;

    private static final long flags = UpdateConfig.UPDATE_FLAG_SYSTEM_SCAN_CONFIG//病毒扫描模块
            | UpdateConfig.UPDATE_FLAG_ADB_DES_LIST//病毒扫描模块
            | UpdateConfig.UPDATE_FLAG_VIRUS_BASE//病毒扫描模块
            | UpdateConfig.UPDATE_FLAG_STEAL_ACCOUNT_LIST//病毒扫描模块
            | UpdateConfig.UPDATE_FLAG_PAY_LIST;//病毒扫描模块

    public TMSDKUpdateService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(!updating){
            setupManager();
            handleVirusUpdate();
        }
    }

    private void handleVirusUpdate() {
        final UpdateManager um = mWeakUpdateManager.get();
        if (null == um || !Utils.isWifiOk(this))
            return ;
        updating = true;
        //check和update是会发起网络链接，耗时操作,它们执行完了才会执行后面的语句
        Log.v(TAG, "tmsdk update start...");
        um.check(flags, new TMSUpdateCheckListener(this, um));
        updating = false;
        Log.v(TAG, "tmsdk update end...");
    }

    private void setupManager() {
       if(Utils.initTMSDK(this)) {
           if (null == mWeakUpdateManager
                   || mWeakUpdateManager.get() == null) {
               UpdateManager updateManager = ManagerCreatorC.getManager(UpdateManager.class);
               mWeakUpdateManager = new WeakReference<>(updateManager);
           }
       }
    }

    @Override
    public void onDestroy() {
        if (null != mWeakUpdateManager) {
            mWeakUpdateManager.clear();
            mWeakUpdateManager = null;
        }
        super.onDestroy();
        Runtime.getRuntime().gc();
    }
}
