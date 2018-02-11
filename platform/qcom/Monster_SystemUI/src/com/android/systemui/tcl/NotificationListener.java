package com.android.systemui.tcl;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.wandoujia.nisdk.core.NIFilter.FilterResult;

import java.util.List;

/**
 * Created by duguguiyu on 16/7/6.
 */
@SuppressLint({"Override", "NewApi"})
public class NotificationListener extends NotificationListenerService {
    private Context mContext;
    private PackageIntentReceiver packageIntentReceiver;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        mContext = this;
        try {
            registerAsSystemService(this,
                    new ComponentName(this.getPackageName(), getClass().getCanonicalName()), UserHandle.USER_ALL);
        } catch (RemoteException e) {

        }
        packageIntentReceiver = new PackageIntentReceiver();
        packageIntentReceiver.registerReceiver();
        super.onCreate();
        setScripWhiteListDefault();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn.isOngoing()) {
            //不清理ongoing通知
            return;
        }

        new Thread() {
            @Override
            public void run() {
                FilterResult result = WdjNotifyClassify.getInstance(mContext).filter(sbn);
                if (result != null && true == SpamNotifyHandle.getInstance(mContext).handleNotify(result, sbn)) {
                    // 如果处理了则删除该通知
                    cancelNotification(sbn.getKey());
                }
            }
        }.start();


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterAsSystemService();
        } catch (RemoteException e) {
            // Ignore.
        }
        packageIntentReceiver.unregisterReceiver();
    }

    private void setScripWhiteListDefault() {
        SharedPreferences preferences = mContext.getSharedPreferences("scrip_default_setting", MODE_PRIVATE);
        boolean hasSet = preferences.getBoolean("has_set", false);
        if (!hasSet) {
            List<String> whiteList = Utils.getScripWhiteList();
            NotificationBackend backend = new NotificationBackend();
            for (String pkg : whiteList) {
                ApplicationInfo info = Utils.getAppInfoByPackageName(mContext, pkg);
                if (info != null) {
                    backend.setSuperScript(pkg, info.uid, false);
                }
            }
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("has_set", true);
            editor.commit();
        }
    }

    private class PackageIntentReceiver extends BroadcastReceiver {
        void registerReceiver() {
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
            filter.addDataScheme("package");
            mContext.registerReceiver(this, filter);
        }

        void unregisterReceiver() {
            mContext.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            if (Intent.ACTION_PACKAGE_ADDED.equals(actionStr)) {
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(actionStr)) {
                Uri data = intent.getData();
                String pkgName = data.getEncodedSchemeSpecificPart();
                SpamNotifyHandle.getInstance(mContext).updateNotifyMap(pkgName);
            }
        }
    }
}
