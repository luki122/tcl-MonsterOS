package com.monster.market.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.monster.market.download.AppDownloadService;

/**
 * Created by xiaobin on 16-9-7.
 */
public class ShutdownReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AppDownloadService.pauseAllDownloads();
    }

}
