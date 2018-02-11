package com.monster.paymentsecurity.tmsdk;

import android.content.Context;

import java.lang.ref.WeakReference;

import tmsdk.common.module.update.CheckResult;
import tmsdk.common.module.update.ICheckListener;
import tmsdk.common.module.update.UpdateManager;

/**
 * Created by logic on 16-12-9.
 */
final class TMSUpdateCheckListener implements ICheckListener {

    private final WeakReference<Context> weakContext;
    private final UpdateManager manager;

    TMSUpdateCheckListener(Context cxt, UpdateManager updateManager){
        this.weakContext = new WeakReference<>(cxt);
        manager = updateManager;
    }

    @Override
    public void onCheckStarted() {

    }

    @Override
    public void onCheckEvent(int i) {

    }

    @Override
    public void onCheckCanceled() {

    }

    @Override
    public void onCheckFinished(CheckResult result) {
        if(null == result || result.mUpdateInfoList == null
                || result.mUpdateInfoList.size() == 0 )
            return;
        Context cxt = weakContext.get();
        if (null != cxt) {
            manager.update(result.mUpdateInfoList, new TMSUpdateListener(cxt));
        }
    }
}
