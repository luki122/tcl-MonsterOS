/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera.app;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;

import com.android.camera.CameraActivity;
import com.android.camera.DeviceInfo;
import com.android.camera.MediaSaverImpl;
import com.android.camera.SecureCameraActivity;
import com.android.camera.debug.Log;
import com.android.camera.debug.LogHelper;
import com.android.camera.instantcapture.InstantViewImageActivity;
import com.android.camera.remote.RemoteShutterListener;
import com.android.camera.settings.SettingsManager;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.CustomUtil;
import com.android.camera.util.RemoteShutterHelper;
import com.android.camera.util.SessionStatsCollector;
import com.android.camera.util.UsageStatistics;
import com.android.classloader.ExternalLoader;

/**
 * The Camera application class containing important services and functionality
 * to be used across modules.
 */
public class CameraApp extends Application implements CameraServices
        ,Application.ActivityLifecycleCallbacks{
    private MediaSaver mMediaSaver;
    private MemoryManagerImpl mMemoryManager;
    private RemoteShutterListener mRemoteShutterListener;
    private MotionManager mMotionManager;
    private SettingsManager mSettingsManager;
    private ExternalLoader mExternalLoader;
    private Log.Tag TAG=new Log.Tag("CameraApp");


    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();
        LogHelper.initialize(context);
        Log.v(TAG,"onCreate CameraApplication");

        // It is important that this gets called early in execution before the
        // app has had the opportunity to create any shared preferences.
        UsageStatistics.instance().initialize(this);
        SessionStatsCollector.instance().initialize(this);
        CameraUtil.initialize(this);
        CustomUtil.getInstance(context).setCustomFromSystem();

        mMediaSaver = new MediaSaverImpl();

        mMemoryManager = MemoryManagerImpl.create(getApplicationContext(), mMediaSaver);
        mRemoteShutterListener = RemoteShutterHelper.create(this);
        mSettingsManager = new SettingsManager(this);

        clearNotifications();

        mMotionManager = new MotionManager(context);
        mExternalLoader = new ExternalLoader(this.getCodeCacheDir().getAbsolutePath(), this.getClassLoader());

        Uri uri=DeviceInfo.getReversibleSettingUri();

        if(uri!=null) {
            Log.v(TAG,"start observing reversible");
            getContentResolver().registerContentObserver(uri, false, new SettingObserver());
        }
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public ExternalLoader getExternalLoader() {
        return mExternalLoader;
    }

    @Override
    public MemoryManager getMemoryManager() {
        return mMemoryManager;
    }

    @Override
    public MotionManager getMotionManager() {
        return mMotionManager;
    }

    @Override
    @Deprecated
    public MediaSaver getMediaSaver() {
        return mMediaSaver;
    }

    @Override
    public RemoteShutterListener getRemoteShutterListener() {
        return mRemoteShutterListener;
    }

    @Override
    public SettingsManager getSettingsManager() {
        return mSettingsManager;
    }



    /**
     * Clears all notifications. This cleans up notifications that we might have
     * created earlier but remained after a crash.
     */
    private void clearNotifications() {
        NotificationManager manager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancelAll();
        }
    }


    @Override
    public boolean isReversibleEnabled() {
        if(mIsReversibleEnabled==null){
            mIsReversibleEnabled=DeviceInfo.isReversibleOn(CameraApp.this.getContentResolver());
        }
        return mIsReversibleEnabled;
    }

    private Boolean mIsReversibleEnabled=null;

    private class SettingObserver extends  ContentObserver{
        public SettingObserver(){
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            Log.v(TAG, "reversible setting changed");
            mIsReversibleEnabled=DeviceInfo.isReversibleOn(CameraApp.this.getContentResolver());
        }
    }
    private boolean mMainActivityActive;
    private boolean mInstantViewActivityActive;
    @Override
    public void onActivityCreated (Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivityDestroyed (Activity activity) {
    }

    @Override
    public void onActivityPaused (Activity activity) {
    }

    @Override
    public void onActivityResumed (Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState (Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityStarted (Activity activity) {
        if (activity instanceof CameraActivity || activity instanceof SecureCameraActivity) {
            mMainActivityActive = true;
        } else if (activity instanceof InstantViewImageActivity) {
            mInstantViewActivityActive = true;
        }
    }

    @Override
    public void onActivityStopped (Activity activity) {
        if (activity instanceof CameraActivity || activity instanceof SecureCameraActivity) {
            mMainActivityActive = false;
        } else if (activity instanceof InstantViewImageActivity) {
            mInstantViewActivityActive = false;
        }
    }
    public boolean isMainActivityActive() {
        return mMainActivityActive;
    }
    public boolean isInstantViewActivityActive() {
        return mInstantViewActivityActive;
    }

}
