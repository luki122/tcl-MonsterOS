package com.android.camera.instantcapture;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;// MODIFIED by yuanxing.tan, 2016-03-28,BUG-1861691
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
/* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
import android.os.Vibrator;
import android.text.TextUtils;

import com.android.camera.Storage;
import com.android.camera.app.CameraApp;
import com.android.camera.app.CameraServices;
import com.android.camera.permission.PermissionActivity;
import com.android.camera.permission.PermissionUtil;
import com.android.camera.permission.PermsInfo;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.BoostUtil;
import com.android.ex.camera2.portability.debug.Log;
import com.android.external.plantform.ExtBuild;
/* MODIFIED-END by yuanxing.tan,BUG-1861691 */
import com.tct.camera.R;


public class InstantCaptureService extends Service {
    private static final Log.Tag TAG = new Log.Tag("InstantService");

    //vf key event
    private final static String BOOMKEY_DOWN_SCREEN_OFF = "down_screen_off";
    private final static String BOOMKEY_DOWN_SCREEN_ON = "down";
    private final static String BOOMKEY_UP = "up";
//    private final static String BOOMKEY_DOWN_SCREEN_OFF = "global_down_screen_off";
/* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
//    private final static String BOOMKEY_DOWN_SCREEN_ON = "global_down";
//    private final static String BOOMKEY_UP = "global_up";
    // global key event
    private final static String BOOMKEY_DOWN_SCREEN_OFF_GLOBAL = "global_down_screen_off";
    private final static String BOOMKEY_DOWN_SCREEN_ON_GLOBAL = "global_down";
    private final static String BOOMKEY_UP_GLOBAL = "global_up";
    private static final String BOOMKEY_SINGLE_PRESSS = "boom_single_press";
    private static final String BOOMKEY_DOUBLE_PRESSS = "boom_double_press";
    private static final String BOOMKEY_LONG_PRESSS = "boom_long_press";
//    private final static String BOOMKEY_DOWN_SCREEN_OFF_GLOBAL = "down_screen_off";
//    private final static String BOOMKEY_DOWN_SCREEN_ON_GLOBAL = "down";
//    private final static String BOOMKEY_UP_GLOBAL = "up";

    // service handler event
    public final static int PREPARE_CAMERA = 1;
//    public final static int START_CAPTURE = 2;
    public final static int RELEASE_CAMERA = 3;
    public final static int SET_DISPLAY_ORIENTATION = 4;
//    public final static int SET_JPEG_ORIENTATION = 5;
    public final static int NO_DATA_RETURN_ERROR = 6;
    public final static int CAPTURE_TIMEOUT = 7;

    // main handler event
    public final static int PICTURE_CAPTURED_MSG = 1;
    public final static int ACTIVITY_LAUNCH_PROTECT = 2;
    public final static int JUDGE_TIMEOUT = 3;

    public static final int CAPTURE_TIMEOUT_DELAY = 3500;
    public static final int NO_DATA_RETURN_TIMEOUT = 500;
    public final static int ACTIVITY_LAUNCH_PROTECT_TIMEOUT = 400;
    public final static int JUDGE_TIMEOUT_DELAY = 1000;
    private static final int CAMERA_RELEASE_TIMEOUT = 3500;

    private final CaptureState mCaptureState = new CaptureState();

    public class CaptureState {
        // camera state
        public static final int CAMERA_UNOPENED = 1;
        public static final int CAMERA_PREPAREING = 2;
        public static final int CAMERA_PREPARED = 3;
        public static final int CAMERA_ERROR = 4;
        public static final int CAMERA_JPEG_TIMEOUT = 5;
        public static final int CAMERA_SNAPSHOT_IN_PROGRESS = 6;
        public static final int CAMERA_SNAPSHOT_LONGSHOT = 7;
        public static final int CAMERA_SNAPSHOT_LONGSHOT_PENDING_STOP = 8;
        public static final int CAMERA_RELEASING = 9;
        public static final int CAMERA_PENDING_STOP = 10;

        // key behaviour
        public static final int FIRST_KEY_DOWN = 1;
        public static final int SINGLE_CLICK = 2;
        public static final int SINGLE_CLICK_WITH_OPEN_APP = 3;
        public static final int DOUBLE_CLICK = 4;
        public static final int LONG_PRESS_BURST = 5;
        public static final int LONG_PRESS_STOP = 6;
        public static final int CAPTURE_WITH_SINGLE_CLICK = 7; // for single click capture
        public static final int CAPTURE_WITH_SINGLE_CLICK_DONE = 8; // for single click capture
        public static final int DEFINITIVE_KEY_TIMEOUT = 9; //MODIFIED by yuanxing.tan, 2016-04-11,BUG-1933361
        private int mCameraState;
        private int mKeyState;

        public CaptureState() {
            mCameraState = CAMERA_UNOPENED;
        }

        private void onCurrentState() {
            switch (mCameraState) {
                case CAMERA_UNOPENED:
                    if (mKeyState == FIRST_KEY_DOWN) {
                        mCameraState = CAMERA_PREPAREING;
                        mServiceHandler.sendEmptyMessage(PREPARE_CAMERA);
                    } else if (mKeyState == SINGLE_CLICK_WITH_OPEN_APP) {
                        mInstantCaptureHelper.startCameraActivity(InstantCaptureService.this, null);
                    } else if (mKeyState == CAPTURE_WITH_SINGLE_CLICK){
                        mKeyState = CAPTURE_WITH_SINGLE_CLICK_DONE;
                        mCameraState = CAMERA_PREPAREING;
                        mServiceHandler.sendEmptyMessage(PREPARE_CAMERA);
                    }
                    break;
                case CAMERA_PREPARED:
                    if (mKeyState == DOUBLE_CLICK || mKeyState == LONG_PRESS_BURST || mKeyState == LONG_PRESS_STOP || mKeyState == CAPTURE_WITH_SINGLE_CLICK_DONE) {
                        if (mKeyState == DOUBLE_CLICK || mKeyState == CAPTURE_WITH_SINGLE_CLICK_DONE) {
                            mCameraState = CAMERA_SNAPSHOT_IN_PROGRESS;
                        } else if (mKeyState == LONG_PRESS_BURST) {
                            mCameraState = CAMERA_SNAPSHOT_LONGSHOT;
                        } else {
                            mCameraState = CAMERA_SNAPSHOT_LONGSHOT_PENDING_STOP;
                        }
                        try {
                            mInstantCaptureHelper.capture();
                        } catch (Exception e) {
                            Log.i(TAG, "capture exception  ", e);
                            changeCameraState(CaptureState.CAMERA_ERROR);
                        };
                    } else if (mKeyState == FIRST_KEY_DOWN) {
                        Log.i(TAG, "camera prepared, waiting more user input");
                    } else {
                        mCameraState = CAMERA_RELEASING;
                        mServiceHandler.sendEmptyMessage(RELEASE_CAMERA);
                    }
                    break;
                case CAMERA_SNAPSHOT_LONGSHOT:
                    if (mKeyState == LONG_PRESS_STOP) {
                        mCameraState = CAMERA_SNAPSHOT_LONGSHOT_PENDING_STOP;
                    }
                    break;
                case CAMERA_ERROR:
                    if (mKeyState != FIRST_KEY_DOWN) {
                        mInstantCaptureHelper.setForbidStartViewImageActivity(true);
                        mInstantCaptureHelper.dismissViewImageActivity();
                        mCameraState = CAMERA_RELEASING;
                        mServiceHandler.sendEmptyMessage(RELEASE_CAMERA);
                    }
                    break;
                case CAMERA_PENDING_STOP:
                    mCameraState = CAMERA_RELEASING;
                    Runnable forceReleaseRunnable=new Runnable() {
                        @Override
                        public void run() {
                            BoostUtil.getInstance().releaseCpuLock();
                            mInstantCaptureHelper.releaseCpuWakeLock();
                            changeCameraState(CaptureState.CAMERA_UNOPENED);
                            Log.w(TAG,"Tinma force close camera");
                        }
                    };
                    mMainHandler.postDelayed(forceReleaseRunnable,CAMERA_RELEASE_TIMEOUT);
                    Message.obtain(mServiceHandler,RELEASE_CAMERA,forceReleaseRunnable).sendToTarget();
                    break;
                case CAMERA_JPEG_TIMEOUT:
                    mCameraState = CAMERA_RELEASING;
                    mServiceHandler.sendEmptyMessage(RELEASE_CAMERA);
                    break;
            }
        }

        public boolean  isInvalidCameraState() {
            synchronized (InstantCaptureService.this) {
                return mCameraState == CAMERA_UNOPENED;
            }
        }

        public void changeCameraState(int state) {
            synchronized (InstantCaptureService.this) {
                Log.i(TAG, "changeCameraState from " + mCameraState + " to " + state + ",  key state " + mKeyState);
                mCameraState = state;
                onCurrentState();
            }
        }

        public void changeKeyState(int keyState) {
            synchronized (InstantCaptureService.this) {
                Log.i(TAG, "changeKeyState from "+mKeyState+" to "+keyState+", camera state "+mCameraState);
                mKeyState = keyState;
                onCurrentState();
            }
        }

        public boolean isKeyState(int state) {
            Log.i(TAG, "isKeyState "+mKeyState+" ,"+state);
            synchronized (InstantCaptureService.this) {
                return mKeyState == state;
            }
        }

        public boolean isCameraState(int state) {
            Log.i(TAG, "isCameraState "+mCameraState+" ,"+state);
            synchronized (InstantCaptureService.this) {
                return mCameraState == state;
            }
        }
        public boolean isCaptureDone() {
            synchronized (InstantCaptureService.this) {
                return mCameraState == CAMERA_UNOPENED
                        || mCameraState == CAMERA_ERROR
                        || mCameraState == CAMERA_JPEG_TIMEOUT
                        || mCameraState == CAMERA_RELEASING
                        || mCameraState == CAMERA_PENDING_STOP;
            }
        }

        public boolean isInCaptureProgress() {
            synchronized (InstantCaptureService.this) {
                return mCameraState == CAMERA_SNAPSHOT_IN_PROGRESS
                        || mCameraState == CAMERA_SNAPSHOT_LONGSHOT
                        || mCameraState == CAMERA_SNAPSHOT_LONGSHOT_PENDING_STOP;
            }
        }

        public boolean isSingleShot() {
            synchronized (InstantCaptureService.this) {
                return mKeyState == DOUBLE_CLICK
                        || mKeyState == CAPTURE_WITH_SINGLE_CLICK_DONE;
            }
        }
        public int getCameraState() {
            synchronized (InstantCaptureService.this) {
                return mCameraState;
            }
        }
        public int getKeyState() {
            synchronized (InstantCaptureService.this) {
                return mKeyState;
            }
        }
    }

    public void changeCameraState(int state) {
        mCaptureState.changeCameraState(state);
    }
    private final class CameraConflictException extends RuntimeException {

        public CameraConflictException() {
            super();
        }

        public CameraConflictException(String msg) {
            super(msg);
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PREPARE_CAMERA:
                    try {
                        if (!CameraLock.getInstance().block(CameraLock.CAMERA_BLOCK_TIMEOUT)) {
                            throw new CameraConflictException();
                        }
                        CameraLock.getInstance().close();
                        mInstantCaptureHelper.start();
                        changeCameraState(CaptureState.CAMERA_PREPARED);
                    } catch (CameraConflictException ex) {
                        Log.i(TAG, "camera already owned by other ");
                        CameraLock.getInstance().open();
                        changeCameraState(CaptureState.CAMERA_ERROR);
                    } catch (Exception e) {
                        Log.i(TAG, "prepare exception  ", e);
                        CameraLock.getInstance().open();
                        changeCameraState(CaptureState.CAMERA_ERROR);
                    };
                    break;
                case RELEASE_CAMERA:
                    Runnable forceReleaseRunnable=(Runnable)msg.obj;
                    mInstantCaptureHelper.stop();
                    changeCameraState(CaptureState.CAMERA_UNOPENED);
                    mMainHandler.removeCallbacks(forceReleaseRunnable);
                    Log.w(TAG,"Tinma normal close camera");
                    break;
                case SET_DISPLAY_ORIENTATION:
                    try {
                        mInstantCaptureHelper.setDisplayOrientation();
                    } catch (Exception e) {
                        Log.i(TAG, "SET_DISPLAY_ORIENTATION exception  ", e);
                    };
                    break;
                case NO_DATA_RETURN_ERROR:
                    mCaptureState.changeCameraState(CaptureState.CAMERA_JPEG_TIMEOUT);
                    break;
                case CAPTURE_TIMEOUT:
                    if (mInstantCaptureHelper.isCaptureDone()) {
                        return;
                    }
                    mInstantCaptureHelper.setForbidStartViewImageActivity(true);
                    mInstantCaptureHelper.dismissViewImageActivity();
                    changeCameraState(CaptureState.CAMERA_PENDING_STOP);
                    break;
            }
        }
    }

    private InstantCaptureHelper mInstantCaptureHelper;

    private ServiceHandler mServiceHandler;
    private Handler mMainHandler;

    public Handler getMainHandler() {
        return mMainHandler;
    }
    public Handler getServiceHandler() {
        return mServiceHandler;
    }

    public boolean checkInCaptureProgress() {
        return mCaptureState.isInCaptureProgress();
    }

    public boolean checkCameraState(int state) {
        return mCaptureState.isCameraState(state);
    }

    public boolean isCaptureDone() {
        return mCaptureState.isCaptureDone();
    }

    public boolean isSingleShot() {
        return mCaptureState.isSingleShot();
    }
    /* MODIFIED-END by yuanxing.tan,BUG-1861691 */

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread serviceThread = new HandlerThread("InstantCaptureService", Thread.MAX_PRIORITY);
        serviceThread.start();
        mServiceHandler = new ServiceHandler(serviceThread.getLooper());
        mMainHandler = new MainHandler();// MODIFIED by yuanxing.tan, 2016-03-28,BUG-1861691
        mInstantCaptureHelper = InstantCaptureHelper.getInstance();
        mInstantCaptureHelper.init(this);
        Log.i(TAG, "onCreate");
    }

    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        mInstantCaptureHelper.destroy();
        mInstantCaptureHelper = null;
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand "+startId);
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
        parseCommand(intent);
        return START_NOT_STICKY;
        /* MODIFIED-END by yuanxing.tan,BUG-1861691 */
    }

    private String validateIntent(Intent intent) {
        if (intent == null || intent.getExtras() == null) {
            Log.i(TAG, "validateIntent invalid intent");
            return null;
        }

        Bundle b = intent.getExtras();
        final String cmd = b.getString("command");
        if (cmd == null ||
                !(cmd.equals(BOOMKEY_DOWN_SCREEN_OFF_GLOBAL)
                || cmd.equals(BOOMKEY_DOWN_SCREEN_ON_GLOBAL)
                || cmd.equals(BOOMKEY_UP_GLOBAL)
                || cmd.equals(BOOMKEY_DOWN_SCREEN_OFF)
                || cmd.equals(BOOMKEY_DOWN_SCREEN_ON)
                /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
                || cmd.equals(BOOMKEY_UP)
                || cmd.equals(BOOMKEY_SINGLE_PRESSS)
                || cmd.equals(BOOMKEY_DOUBLE_PRESSS)
                || cmd.equals(BOOMKEY_LONG_PRESSS))) {
            Log.i(TAG, "parseCommand invalid cmd " + cmd);
            return null;
        }
        return cmd;
    }

    private boolean mPendingOpenApp = false;

    public void parseCommand(Intent intent) {
        final String cmd = validateIntent(intent);
        if (cmd == null) {
            return;
        }
        Log.i(TAG, "parseCommand cmd: " + cmd+", "+mWarningType+", "+mCaptureState.getCameraState()+", "+mCaptureState.getKeyState());

        switch (cmd) {
            case BOOMKEY_DOWN_SCREEN_ON_GLOBAL:
            case BOOMKEY_DOWN_SCREEN_OFF_GLOBAL:
                if (mCaptureState.isInvalidCameraState() && mWarningType == -1) {
                    if (isInvalidUiState() || mMainHandler.hasMessages(ACTIVITY_LAUNCH_PROTECT)) {
                        return;
                    }
                    if (TextUtils.equals(cmd, BOOMKEY_DOWN_SCREEN_ON_GLOBAL)) {
                        mPendingOpenApp = true;
                    }

                    Log.i(TAG, "instant capture kpi, first boom key down");
                    if (checkInstantCapturePreconditions()) {
                        mCaptureState.changeKeyState(CaptureState.FIRST_KEY_DOWN);
                        mInstantCaptureHelper.dismissViewImageActivity();
                    }
                    mMainHandler.sendEmptyMessageDelayed(JUDGE_TIMEOUT, JUDGE_TIMEOUT_DELAY);
                }
                break;
            case BOOMKEY_UP_GLOBAL:
                if (mCaptureState.isKeyState(CaptureState.LONG_PRESS_BURST)) {
                    Log.i(TAG, "instant capture kpi, boom key long press up");
                    mCaptureState.changeKeyState(CaptureState.LONG_PRESS_STOP);
                }
                break;
            case BOOMKEY_SINGLE_PRESSS:
                mMainHandler.removeMessages(JUDGE_TIMEOUT);
                if (mCaptureState.isKeyState(CaptureState.FIRST_KEY_DOWN) || mWarningType != -1) {
                    Log.i(TAG, "instant capture kpi, single click "+mPendingOpenApp);
                    if (mPendingOpenApp) {
                        mCaptureState.changeKeyState(CaptureState.SINGLE_CLICK_WITH_OPEN_APP);
                    } else {
                        mCaptureState.changeKeyState(CaptureState.SINGLE_CLICK);
                    }
                    mWarningType = -1;
                    mPendingOpenApp = false;
                }
                break;
            case BOOMKEY_DOUBLE_PRESSS:
                mPendingOpenApp = false;
                mMainHandler.removeMessages(JUDGE_TIMEOUT);
                if (mCaptureState.isKeyState(CaptureState.FIRST_KEY_DOWN) || mWarningType != -1) {
                    Log.i(TAG, "instant capture kpi,second boom key down");
                    if (mWarningType != -1) {
                        showWarningNotification(InstantCaptureService.this);
                        mWarningType = -1;
                    } else {
                        mCaptureState.changeKeyState(CaptureState.DOUBLE_CLICK);
                        if (!mInstantCaptureHelper.getForbidStartViewImageActivity() && !mCaptureState.isCaptureDone()) {
                            mInstantCaptureHelper.startViewImageActivity(InstantCaptureService.this);
                        }
                    }
                }
                break;
            case BOOMKEY_LONG_PRESSS:
                mPendingOpenApp = false;
                mMainHandler.removeMessages(JUDGE_TIMEOUT);
                if (mCaptureState.isKeyState(CaptureState.FIRST_KEY_DOWN) || mWarningType != -1) {
                    Log.i(TAG, "instant capture kpi,enter long press and launch view ui");
                    if (mWarningType != -1) {
                        showWarningNotification(InstantCaptureService.this);
                        mWarningType = -1;
                    } else {
                        mCaptureState.changeKeyState(CaptureState.LONG_PRESS_BURST);
                        if (!mInstantCaptureHelper.getForbidStartViewImageActivity() && !mCaptureState.isCaptureDone()) {
                            mInstantCaptureHelper.startViewImageActivity(InstantCaptureService.this);
                        }
                    }
                }
                break;

            case BOOMKEY_DOWN_SCREEN_ON:
            case BOOMKEY_DOWN_SCREEN_OFF:
                if (mCaptureState.isInvalidCameraState()) {
                    if (isInvalidUiState()) {
                        return;
                    }
                    if (checkInstantCapturePreconditions()) {
                        mCaptureState.changeKeyState(CaptureState.CAPTURE_WITH_SINGLE_CLICK);
                        mInstantCaptureHelper.startViewImageActivity(InstantCaptureService.this);
                    } else {
                        showWarningNotification(this);
                    }
                }
                break;
            case BOOMKEY_UP:
                break;
        }
    }

    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PICTURE_CAPTURED_MSG:
                    if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
                        AudioManager mAudioManager =(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                        if(mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            if (vibrator != null) {
                                vibrator.vibrate(50); // MODIFIED by shunyin.zhang, 2016-05-09,BUG-2119714
                            } else {
                                Log.e(TAG, "vibrator == null");
                            }
                        }
                    }
                    break;
                case JUDGE_TIMEOUT:
                    Log.i(TAG, "instant capture kpi,timeout for next cmd");
                    /*MODIFIED-BEGIN by yuanxing.tan, 2016-04-11,BUG-1933361*/
                    mCaptureState.changeKeyState(CaptureState.DEFINITIVE_KEY_TIMEOUT);
//                    mCaptureState.changeCameraState(CaptureState.CAMERA_PENDING_STOP);
/*MODIFIED-END by yuanxing.tan,BUG-1933361*/
                    break;
            }
        }
        /* MODIFIED-END by yuanxing.tan,BUG-1861691 */
    }

    // check low battert/low storage/permission
    private static final int TYPE_LOW_BATTERY = 0;
    private static final int TYPE_LOW_STORAGE = 1;
    private static final int TYPE_LOW_BATTERY_AND_STORAGE = 2;
    private static final int TYPE_PERMISSION_NEED = 3;

    public static final int LOW_BATTERY_LEVEL = 5;

    private int mWarningType = -1;

    public boolean checkInstantCapturePreconditions() {
        mWarningType = -1;
        boolean lowBattery = false;
        boolean lowStorage = false;
        boolean permissionNeed = false;
        if (!PermissionUtil.isCriticalPermissionGranted(getApplicationContext())) {
            permissionNeed = true;
        }
        if (ApiHelper.isLOrHigher() && mInstantCaptureHelper.needLowBatteryCheck(this)) { //MODIFIED by yuanxing.tan, 2016-04-05,BUG-1911947
            BatteryManager batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
            int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            Log.i(TAG, "batteryLevel:" + batteryLevel);
            if (batteryLevel > 0 && batteryLevel <= LOW_BATTERY_LEVEL) {
                lowBattery = true;
            }
        }
        String savePath = ((CameraServices)getApplicationContext()).getSettingsManager().
                getString(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_CAMERA_SAVEPATH,
                        getResources().getString(R.string.pref_camera_savepath_default));
        Storage.setSavePath(savePath);
        if (Storage.getAvailableSpace() < Storage.LOW_STORAGE_THRESHOLD_BYTES) {
            lowStorage = true;
        }
        if (permissionNeed || lowBattery || lowStorage) {
            Log.i(TAG, "checkInstantCapturePreconditions " + permissionNeed + ", " + lowBattery + ", " + lowStorage);
            if (permissionNeed) {
                mWarningType = TYPE_PERMISSION_NEED;
            } else {
                if (lowBattery || lowStorage) {
                    if (lowBattery && lowStorage) {
                        mWarningType = TYPE_LOW_BATTERY_AND_STORAGE;
                    } else if (lowBattery) {
                        mWarningType = TYPE_LOW_BATTERY;
                    } else {
                        mWarningType = TYPE_LOW_STORAGE;
                    }
                }
            }
            return false;
        }

        return true;
    }

    /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
    private boolean isInvalidUiState() {
        if (!mInstantCaptureHelper.isScreenOn()) {
            return false;
        }
        /* MODIFIED-END by yuanxing.tan,BUG-1861691 */
        CameraApp app = (CameraApp) getApplication();
        boolean isMainActivityActive = app.isMainActivityActive();
        boolean isInstantViewActivityActive = app.isInstantViewActivityActive();
        Log.i(TAG, "isInvalidState " + isMainActivityActive + ", " + isInstantViewActivityActive);
        if (isMainActivityActive || isInstantViewActivityActive) {
            return true;
        }
        return false;
    }

    private void showWarningNotification(Context context) {
        Log.i(TAG, "showWarningNotification "+mWarningType);
        mInstantCaptureHelper.wakeUpScreen();
        Resources res = context.getResources();
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder;
        switch (mWarningType) {
            case TYPE_PERMISSION_NEED:
                Intent i = new Intent(context, PermissionActivity.class);
                i.putExtra(PermsInfo.TAG_RATIONALIZE, true);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i,
                        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);

                builder = new Notification.Builder(context)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(res.getString(R.string.permission_title))
                        .setContentText(res.getString(R.string.permission_content))
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true);
                nm.notify(0, builder.build());
                break;
            case TYPE_LOW_BATTERY:
                builder = new Notification.Builder(context)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(res.getString(R.string.battery_warning_title))
                        .setContentText(res.getString(R.string.battery_warning_content))
                        .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0))
                        .setAutoCancel(true);

                nm.notify(0, builder.build());
                break;
            case TYPE_LOW_STORAGE:
                builder = new Notification.Builder(context)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(res.getString(R.string.battery_warning_title))
                        .setContentText(res.getString(R.string.storage_warning_content))
                        .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0))
                        .setAutoCancel(true);

                nm.notify(0, builder.build());
                break;
            case TYPE_LOW_BATTERY_AND_STORAGE:
                builder = new Notification.Builder(context)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle(res.getString(R.string.battery_warning_title))
                        .setContentText(res.getString(R.string.storage_and_battery_warning_content))
                        .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0))
                        .setAutoCancel(true);

                nm.notify(0, builder.build());
                break;
            default:
                break;
        }
    }


} //MODIFIED by yuanxing.tan, 2016-04-11,BUG-1933361
