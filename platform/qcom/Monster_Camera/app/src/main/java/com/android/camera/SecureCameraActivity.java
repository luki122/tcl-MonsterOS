/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.camera;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.android.camera.debug.Log;
import com.android.camera.permission.PermissionActivity;
import com.android.camera.permission.PermissionUtil;
import com.android.camera.permission.PermsInfo;
import com.android.camera.util.CameraUtil;
import com.tct.camera.R;

// Use a different activity for secure camera only. So it can have a different
// task affinity from others. This makes sure non-secure camera activity is not
// started in secure lock screen.
public class SecureCameraActivity extends CameraActivity {

    Log.Tag TAG = new Log.Tag("SecureCameraActivity");

    private boolean mIsKeyguardLocked = false;
    private boolean mNoPermsGranted = false;

    @Override
    public void onCreateTasks(Bundle state) {
        mIsKeyguardLocked = CameraUtil.isKeyguardLocked(this);
        mNoPermsGranted = !PermissionUtil.isCriticalPermissionGranted(this);
        if (mIsKeyguardLocked && mNoPermsGranted) {
            sendNotificationForPerms();
            // If finish() is called here, onDestroy() will be called later without any of the rest
            // of the activity lifecycle, but it may cost about 10s before onDestroy so I'd prefer
            // to finish SecureCameraActivity in onResume and don't call these super methods
            // of the activity lifecycle in CameraActivity.
            // SecureCameraActivity.this.finish();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            return;
        } else if (mNoPermsGranted) {
            ComponentName last = CameraUtil.getComponentNameByOrder(this, 1);
            if (PermissionUtil.isPermissionRequestWindow(last) ||
                    getComponentName().getPackageName().equals(last.getPackageName())) {
                finish();
                return;
            }
        }
        super.onCreateTasks(state);
    }

    @Override
    public void onNewIntentTasks(Intent intent) {
        if (mNoPermsGranted) return;
        super.onNewIntentTasks(intent);
    }

    @Override
    public void onStartTasks() {
        if (mNoPermsGranted) return;
        super.onStartTasks();
    }

    @Override
    public void onResumeTasks() {
        if (mNoPermsGranted) {
            SecureCameraActivity.this.finish();
            return;
        }
        super.onResumeTasks();
    }

    @Override
    protected void onStopTasks() {
        if (mNoPermsGranted) return;
        super.onStopTasks();
    }

    @Override
    public void onPauseTasks() {
        if (mNoPermsGranted) return;
        super.onPauseTasks();
    }

    @Override
    public void onDestroyTasks() {
        if (mNoPermsGranted) return;
        super.onDestroyTasks();
    }

    private void sendNotificationForPerms() {
        final Intent intent = new Intent(this, PermissionActivity.class);
        intent.putExtra(PermsInfo.TAG_RATIONALIZE, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        final Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentTitle(getResources().getString(R.string.permission_title))
                .setContentText(getResources().getString(R.string.permission_content))
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .build();
        final int id = 1; // the identifier for notification, dummy here.
        mNotificationManager.notify(id, notification);
    }
}
