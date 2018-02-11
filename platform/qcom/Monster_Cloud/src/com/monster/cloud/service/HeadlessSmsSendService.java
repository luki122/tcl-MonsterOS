package com.monster.cloud.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telecom.Log;

import com.monster.cloud.utils.LoginUtil;
import com.tencent.qqpim.sdk.accesslayer.def.PMessage;
import com.tencent.qqpim.sdk.accesslayer.interfaces.basic.ISyncProcessorObsv;

/**
 * Android4.4需要注册短信权限，防止am startservice -n com.tencent.qqpim/com.tencent.qqpim.HeadlessSmsSendService挂掉，做个空实现
 *
 * @author gzjaychen
 */
public class HeadlessSmsSendService extends Service {

    private static final String TAG = HeadlessSmsSendService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
