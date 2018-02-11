/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.camera.util;

import android.content.Context;

import com.android.camera.remote.RemoteCameraModule;
import com.android.camera.remote.RemoteShutterListener;
import android.util.Log;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.app.PendingIntent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tct.camera.R;

public class RemoteShutterHelper {
    private static final String TAG = "RemoteShutterHelper";
    private static final int NOTIFICATION_ID = 001;
    private static final int TARGET_WIDTH = 300;
    private static final String REMOTE_SHUTTER_INTENT = "TclRemoteShutter";
    private static Context mContext;
    private static RemoteCameraModule mRemoteCameraModule = null;
    private static BroadcastReceiver mBroadcastReceiver = null;
    private static int mLastResouceId = -1;
    private static int mPressCount = 0;
    private static boolean mAndroidWareInstalled = false;

    public static RemoteShutterListener create(Context context) {
        mContext = context;
        mPressCount = 0;
        mRemoteCameraModule = null;
        mBroadcastReceiver = null;
        mLastResouceId = -1;
        if (checkIfAndroidWearInstalled(mContext)) {
            mAndroidWareInstalled = true;
        }
        ListenAndroidWare(mContext);
        return new RemoteShutterListener() {
            @Override
            public void onPictureTaken(byte[] photoData) {
                Log.v(TAG, "onPictureTaken start");
                if (mAndroidWareInstalled  && mPressCount > 0) {
                    mPressCount = 0;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(photoData, 0, photoData.length, null);
                    int targetWidth = TARGET_WIDTH;
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    if (width > targetWidth) {
                        float scale = (float) targetWidth / width;
                        int w = Math.round(scale * width);
                        int h = Math.round(scale * height);
                        bitmap = Bitmap.createScaledBitmap(bitmap, w, h, true);
                        Log.v(TAG, "onPictureTaken width " +width+"  height  "+height+" w "+w+ " h "+h);
                    }

                    if (bitmap != null) {
                        sendRemoteNotify(mContext, mLastResouceId, bitmap);
                    }
                }
                Log.v(TAG, "onPictureTaken end");
            }

            @Override
            public void onModuleReady(RemoteCameraModule module, int resouceId) {
                Log.v(TAG, "onModuleReady start");
                if (mAndroidWareInstalled) {
                    mRemoteCameraModule = module;
                    sendRemoteNotify(mContext, resouceId, null);
                    regestShutter(mContext);
                }
                Log.v(TAG, "onModuleReady end");
            }

            @Override
            public void onModuleExit() {
                Log.v(TAG, "onModuleExit start");
                if (mAndroidWareInstalled) {
                    unRegestShutter(mContext);
                    cancelRemoteNotify(mContext);
                    mRemoteCameraModule = null;
                }
                Log.v(TAG, "onModuleExit end");
            }

        };
    }

    private static void sendRemoteNotify (Context context, int resouceId, Bitmap bitmap) {
        if (resouceId < 0) return;
        mLastResouceId = resouceId;
        // Create an intent for the reply action
        Intent actionIntent = new Intent(REMOTE_SHUTTER_INTENT);
        PendingIntent actionPendingIntent =
                PendingIntent.getBroadcast(context, 0, actionIntent, 0);

        // Create the action
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(resouceId,
                        "", actionPendingIntent).build();

        WearableExtender wearableExtender = new WearableExtender().addAction(action);
        if (bitmap != null) {
            wearableExtender.setBackground(bitmap);
        } else {
            wearableExtender.setBackground(BitmapFactory.decodeResource(context.getResources(), R.drawable.wallpaper_wearable_default, null));
        }

        // Build the notification and add the action via WearableExtender
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(resouceId)
                        .setContentTitle(context.getString(R.string.app_name))
                        .setContentText(context.getString(R.string.remote_content))
                        .extend(wearableExtender)
                                //.setPriority(NotificationCompat.PRIORITY_MAX)
                        .setVibrate(new long[]{100});

        NotificationManagerCompat notificationManager =  NotificationManagerCompat.from(context);

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private static void cancelRemoteNotify(Context context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
    }

    private static void regestShutter(Context context) {
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (mRemoteCameraModule != null) {
                        mPressCount++;
                        mRemoteCameraModule.onRemoteShutterPress();
                    }
                }
            };
            IntentFilter remoteShutter = new IntentFilter(REMOTE_SHUTTER_INTENT);
            context.registerReceiver(mBroadcastReceiver, remoteShutter);
        }
    }

    private static void unRegestShutter(Context context) {
        if (mBroadcastReceiver != null) {
            context.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private static boolean checkIfAndroidWearInstalled (Context context) {
        try {
            context.getPackageManager().getPackageInfo("com.google.android.wearable.app", PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            //android wear app is not installed
            Log.v(TAG, "checkIfAndroidWearInstalled failed! cn checking");
            try {
                context.getPackageManager().getPackageInfo("com.google.android.wearable.app.cn", PackageManager.GET_META_DATA);
                return true;
            } catch (PackageManager.NameNotFoundException ecn) {
                //android wear app cn is not installed
                Log.v(TAG, "checkIfAndroidWearInstalled failed ecn!");
            }
        }
        return false;
    }

    private static void ListenAndroidWare(Context context) {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equalsIgnoreCase("android.intent.action.PACKAGE_ADDED")) {
                    String packageName = intent.getData().getSchemeSpecificPart();
                    Log.v(TAG,"ListenAndroidWare add " + packageName);
                    if (packageName.equalsIgnoreCase("com.google.android.wearable.app")
                            || packageName.equalsIgnoreCase("com.google.android.wearable.app.cn")) {
                        mAndroidWareInstalled = true;
                    }
                } else if (intent.getAction().equalsIgnoreCase("android.intent.action.PACKAGE_REMOVED")) {
                    String packageName = intent.getData().getSchemeSpecificPart();
                    Log.v(TAG,"ListenAndroidWare remove " + packageName);
                    if (packageName.equalsIgnoreCase("com.google.android.wearable.app")
                            || packageName.equalsIgnoreCase("com.google.android.wearable.app.cn")) {
                        mAndroidWareInstalled = checkIfAndroidWearInstalled(context);
                    }
                }
            }
        };
        IntentFilter intFilter = new IntentFilter();
        intFilter.addAction("android.intent.action.PACKAGE_ADDED");
        intFilter.addAction("android.intent.action.PACKAGE_REMOVED");
        intFilter.addDataScheme("package");
        context.registerReceiver(broadcastReceiver, intFilter);
    }
}