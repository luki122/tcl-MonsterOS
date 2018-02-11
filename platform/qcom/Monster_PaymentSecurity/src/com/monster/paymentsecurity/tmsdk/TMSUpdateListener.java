package com.monster.paymentsecurity.tmsdk;

import android.content.Context;
import android.content.Intent;

import com.monster.paymentsecurity.constant.Constant;
import com.monster.paymentsecurity.util.SettingUtil;
import com.monster.paymentsecurity.util.Utils;

import java.lang.ref.WeakReference;

import tmsdk.common.module.update.IUpdateListener;
import tmsdk.common.module.update.UpdateInfo;

/**
 * TMS更新回调
 * Created by logic on 16-12-9.
 */
final class TMSUpdateListener implements IUpdateListener {

    private final WeakReference<Context> weakContext;

    TMSUpdateListener(Context cxt){
        this.weakContext = new WeakReference<>(cxt);
    }

    @Override
    public void onUpdateStarted() {

    }

    @Override
    public void onProgressChanged(UpdateInfo updateInfo, int i) {

    }

    @Override
    public void onUpdateEvent(UpdateInfo updateInfo, int i) {

    }

    @Override
    public void onUpdateCanceled() {

    }

    @Override
    public void onUpdateFinished() {
        String updateTime = Utils.formatTime(System.currentTimeMillis());
        SettingUtil.setVirusLibUpdateTime(weakContext.get(), updateTime);
        Intent intent = new Intent(Constant.ACTION_VIRUS_LIB_CHANGE);
        intent.putExtra(TMSDKUpdateService.VIRUS_LIB_UPDATE_TIME, updateTime);
        Context cxt = weakContext.get();
        if (cxt != null) {
            cxt.sendBroadcast(intent);
        }
    }
}
