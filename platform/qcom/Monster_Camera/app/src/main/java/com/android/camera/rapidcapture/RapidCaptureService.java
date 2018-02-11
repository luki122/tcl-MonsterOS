
package com.android.camera.rapidcapture;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.android.camera.permission.PermissionActivity;
import com.android.camera.permission.PermissionUtil;
import com.android.camera.permission.PermsInfo;
import com.android.camera.util.ApiHelper;
import com.tct.camera.R;

public class RapidCaptureService extends Service{
    private static final String TAG = "RapidCaptureService";

    private static final String CMD_ONESHOT_CAPTURE = "shortpress";
    private static final String CMD_BURSTSHOT_CAPTURE = "longpress";
    private static final String CMD_BURSTSHOT_CAPTURE_STOP = "stop";

    public static final int IDLE = 0;
    public static final int ONESHOT_IN_PROGRESS = 1;
    public static final int BURSTSHOT_IN_PROGRESS = 2;

    private static final int TYPE_LOW_BATTERY = 0;
    private static final int TYPE_LOW_STORAGE = 1;
    private static final int TYPE_PERMISSION_NEED = 2;

    private int mCameraState = IDLE;

    RapidCaptureHelper mRapidCaptureHelper;

    public static long mServiceOncreateTime;
    public static long mServiceOnstartTime;


    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return null;
    }

    @Override
    public void onCreate() {
        mServiceOncreateTime = System.currentTimeMillis();
        super.onCreate();
        mRapidCaptureHelper = RapidCaptureHelper.getInstance();
        mRapidCaptureHelper.init(getApplication(), mServiceCallback);
        Log.d(TAG, "onCreate");
        mCameraState = IDLE;
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (mRapidCaptureHelper != null) {
            mRapidCaptureHelper.destroy();
            mRapidCaptureHelper = null;
        }
        super.onDestroy();
    }

    private void showWarningNotification(Context context, int type) {
        if (mRapidCaptureHelper == null) {
            mRapidCaptureHelper = RapidCaptureHelper.getInstance();
        }
        mRapidCaptureHelper.acquireScreenWakeLock(context);
        Resources res = context.getResources();
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder;
        switch (type) {
            case TYPE_PERMISSION_NEED:
                Intent i = new Intent(context, PermissionActivity.class);
                i.putExtra(PermsInfo.TAG_RATIONALIZE, true);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i,
                        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);

                // Get the notification ready
                builder = new Notification.Builder(context)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(res.getString(R.string.permission_title))
                        .setContentText(res.getString(R.string.permission_content))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);
                // Trigger the notification
                nm.notify(0, builder.build());
                break;
            case TYPE_LOW_BATTERY:
                // Get the notification ready
                builder = new Notification.Builder(context)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(res.getString(R.string.battery_warning_title))
                        .setContentText(res.getString(R.string.battery_warning_content))
                        .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0))
                        .setAutoCancel(true);
                // Trigger the notification

                nm.notify(0, builder.build());
                break;
            default:
                break;
        }
    }
    public static final int LOW_BATTERY_LEVEL = 5;
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "startId:" + startId);
        if (!checkPreconditions()) {
            return START_NOT_STICKY;
        }
        parseCommand(intent);
        return START_STICKY;
    }

    public boolean checkPreconditions() {
        if (!PermissionUtil.isCriticalPermissionGranted(getApplicationContext())) {
            showWarningNotification(getApplicationContext(), TYPE_PERMISSION_NEED);
            return false;
        }
        if (ApiHelper.isLOrHigher()) {
            BatteryManager batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
            int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            Log.d(TAG, "batteryLevel:" + batteryLevel);
            if (batteryLevel > 0 && batteryLevel <= LOW_BATTERY_LEVEL) {
                showWarningNotification(getApplicationContext(), TYPE_LOW_BATTERY);
                return false;
            }
        }
        return true;
    }
    public void parseCommand(Intent intent) {
        if (intent != null) {
            Bundle b = intent.getExtras();
            if (b != null) {
                String cmd = b.getString("command");
                Log.d(TAG, "parseCommand:" + cmd);
                if (CMD_ONESHOT_CAPTURE.equalsIgnoreCase(cmd)) {
                    commandOneshotCapture();
                } else if (CMD_BURSTSHOT_CAPTURE.equalsIgnoreCase(cmd)) {
                    commandBurstCapture();
                } else if (CMD_BURSTSHOT_CAPTURE_STOP.equalsIgnoreCase(cmd)) {
                    commandStopCapture();
                }
            }
        }
    }

    private boolean checkCapturesPreconditions(Context context) {
        Log.i(TAG, "checkCapturesPreconditions "+mCameraState);
        if (mCameraState != IDLE) {
            return false;
        }
        boolean isScreenOn = ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).isScreenOn();
        boolean isInKeyguard = ((KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked();
        Log.i(TAG, "checkCapturesPreconditions "+isScreenOn+",  "+isInKeyguard+", "+RapidViewImageActivity.mIsRunning);
        if (isScreenOn && !isInKeyguard) {
//            return false;
        }
        if (RapidViewImageActivity.mIsRunning) {
            return false;
        }

        return true;
    }

    private void commandOneshotCapture() {
        mServiceOnstartTime = System.currentTimeMillis();
        Log.i(TAG, "commandOneshotCapture");
        if (!checkCapturesPreconditions(this)) {
            return;
        }

        if (mRapidCaptureHelper == null) {
            mRapidCaptureHelper = RapidCaptureHelper.getInstance();
            mRapidCaptureHelper.init(getApplication(), mServiceCallback);
        }
        mCameraState = ONESHOT_IN_PROGRESS;
        mRapidCaptureHelper.resume(RapidCaptureHelper.TYPE_ONESHOT);
    }

    private void commandBurstCapture() {
        Log.i(TAG, "commandBurstCapture");
        if (!checkCapturesPreconditions(this)) {
            return;
        }
        if (mRapidCaptureHelper == null) {
            mRapidCaptureHelper = RapidCaptureHelper.getInstance();
            mRapidCaptureHelper.init(getApplication(), mServiceCallback);
        }
        mCameraState = BURSTSHOT_IN_PROGRESS;
        mRapidCaptureHelper.resume(RapidCaptureHelper.TYPE_BURSTSHOT);
    }

    private void commandStopCapture() {
        Log.i(TAG, "commandStopCapture  mCameraState:" + mCameraState);
        if (mCameraState != BURSTSHOT_IN_PROGRESS) {
            return;
        }
        mRapidCaptureHelper.stopBurst();
    }
    private ServiceCallback mServiceCallback = new ServiceCallback();
    private class ServiceCallback implements
            RapidCaptureHelper.Callback {
        @Override
        public void onCaptureDone(boolean reset) {
            if (mRapidCaptureHelper != null) {
                mRapidCaptureHelper.pause();
            }
            if (reset) {
                mRapidCaptureHelper = null;
            }
//            stopSelf();
            mCameraState = IDLE;
        }
    }
}
