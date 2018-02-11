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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.BatteryManager;// MODIFIED by nie.lei, 2016-03-21, BUG-1845068
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.CameraPerformanceTracker;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.camera.app.AppController;
import com.android.camera.app.CameraAppUI;
import com.android.camera.app.CameraController;
import com.android.camera.app.CameraProvider;
import com.android.camera.app.CameraServices;
import com.android.camera.app.LocationManager;
import com.android.camera.app.MemoryManager;
import com.android.camera.app.MemoryQuery;
import com.android.camera.app.ModuleManager;
import com.android.camera.app.ModuleManagerImpl;
import com.android.camera.app.MotionManager;
import com.android.camera.app.OrientationManager;
import com.android.camera.app.OrientationManagerImpl;
import com.android.camera.data.LocalData;
import com.android.camera.data.LocalDataUtil;
import com.android.camera.data.LocalMediaData;
import com.android.camera.data.LocalMediaObserver;
import com.android.camera.data.MetadataLoader;
import com.android.camera.debug.Log;
import com.android.camera.hardware.HardwareSpec;
import com.android.camera.hardware.HardwareSpecImpl;
import com.android.camera.module.ModuleController;
import com.android.camera.module.ModulesInfo;
import com.android.camera.one.OneCameraManager;
import com.android.camera.permission.PermissionActivity;
import com.android.camera.permission.PermissionUtil;
import com.android.camera.permission.PermsInfo;
import com.android.camera.settings.AppUpgrader;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.test.TestUtils; // MODIFIED by wenhua.tu, 2016-08-11,BUG-2710178
import com.android.camera.ui.AbstractTutorialOverlay;
import com.android.camera.ui.Lockable;
import com.android.camera.ui.MainActivityLayout;
import com.android.camera.ui.ModeStrip;
import com.android.camera.ui.ModeTransitionView;
import com.android.camera.ui.PreviewStatusListener;
import com.android.camera.ui.Rotatable;
import com.android.camera.ui.StereoModeStripView;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.BlurUtil;
import com.android.camera.util.Callback;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.util.GcamHelper;
import com.android.camera.util.GservicesHelper;
import com.android.camera.util.IntentHelper;
import com.android.camera.util.PhotoSphereHelper.PanoramaViewHelper;
import com.android.camera.util.QuickActivity;
import com.android.camera.util.ReleaseHelper;
import com.android.camera.util.ToastUtil;
import com.android.camera.util.SnackbarToast; // MODIFIED by fei.hui, 2016-09-09,BUG-2868515
import com.android.camera.util.UsageStatistics;
import com.android.camera.widget.ButtonGroup;
import com.android.camera.widget.TopMenus;
import com.android.ex.camera2.portability.CameraAgent;
import com.android.ex.camera2.portability.CameraAgentFactory;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.ex.camera2.portability.CameraExceptionHandler;
import com.android.ex.camera2.portability.CameraSettings;
import com.android.external.plantform.ExtBuild; //MODIFIED by shunyin.zhang, 2016-04-12,BUG-1892480
import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.load.engine.executor.FifoPriorityThreadPoolExecutor;
import com.google.common.logging.eventprotos;
import com.google.common.logging.eventprotos.ForegroundEvent.ForegroundSource;
import com.google.common.logging.eventprotos.NavigationChange;
import com.tct.camera.R;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.BatteryManager.EXTRA_LEVEL;

public class CameraActivity extends QuickActivity
        implements AppController, CameraAgent.CameraOpenCallback,
        /* MODIFIED-BEGIN by fei.hui, 2016-10-25,BUG-3167899*/
        OrientationManager.OnOrientationChangeListener,
        PoseFragment.PoseSelectorShowingCallback {
        /* MODIFIED-END by fei.hui,BUG-3167899*/

    private static final Log.Tag TAG = new Log.Tag("CameraActivity");

    private static final String INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE =
            "android.media.action.STILL_IMAGE_CAMERA_SECURE";
    public static final String ACTION_IMAGE_CAPTURE_SECURE =
            "android.media.action.IMAGE_CAPTURE_SECURE";
    public static final String MST_SCAN_BUSINESSCARD_ACTION =
            "com.android.contacts.MST_SCAN_BUSINESSCARD_ACTION";
    public static final String ACTION_START_FRONT_CAMERA =
            "com.tct.camera.STARTFRONTCAMERA";
    /* MODIFIED-BEGIN by xuan.zhou, 2016-04-28,BUG-2005112*/
    public static final String ACTION_START_FYUSE =
            "com.tct.camera.STARTFYUSE";
            /* MODIFIED-END by xuan.zhou,BUG-2005112*/

    private static final String ACTION_GOTOGALLERY = "GoToGallery";
    // The intent extra for camera from secure lock screen. True if the gallery
    // should only show newly captured pictures. sSecureAlbumId does not
    // increment. This is used when switching between camera, camcorder, and
    // panorama. If the extra is not set, it is in the normal camera mode.
    public static final String SECURE_CAMERA_EXTRA = "secure_camera";

    public static final String MODULE_SCOPE_PREFIX = "_preferences_module_";
    public static final String CAMERA_SCOPE_PREFIX = "_preferences_camera_";
    private static final int MSG_CLEAR_SCREEN_ON_FLAG = 2;
    private static final long SCREEN_NORMAL_DELAY_MS = 2 * 60 * 1000; // 2 mins.
    private static final long SCREEN_POWER_SAVE_MODE_DELAY_MS = 30 * 1000; // 30s.
    private static final int MAX_PEEK_BITMAP_PIXELS = 1600000; // 1.6 * 4 MBs.
    private static long SCREEN_DELAY_MS = SCREEN_NORMAL_DELAY_MS;


    /**
     * Should be used wherever a context is needed.
     */
    private Context mAppContext;

    private SurfaceTexture mSurface;

    /**
     * Camera fatal error handling:
     * 1) Present error dialog to guide users to exit the app.
     * 2) If users hit home button, onPause should just call finish() to exit the app.
     */
    private boolean mCameraFatalError = false;

    /**
     * Whether onResume should reset the view to the preview.
     */
    private boolean mResetToPreviewOnResume = true;

    private OneCameraManager mCameraManager;
    private SettingsManager mSettingsManager;
    private ModeStrip mModeStripView;
    private int mCurrentModeIndex;
    private CameraModule mCurrentModule;
    private ModuleManagerImpl mModuleManager;

    private int mResultCodeForTesting;
    private Intent mResultDataForTesting;
    private OnScreenHint mStorageHint;
    private final Object mStorageSpaceLock = new Object();
    private long mStorageSpaceBytes = Storage.LOW_STORAGE_THRESHOLD_BYTES;
    private boolean mAutoRotateScreen;
    private boolean mSecureCamera;
    private int mLastRawOrientation;
    private OrientationManagerImpl mOrientationManager;
    private LocationManager mLocationManager;
    private ButtonManager mButtonManager;
    private Handler mMainHandler;
    private PanoramaViewHelper mPanoramaViewHelper;
    public static boolean gIsCameraActivityRunning = false;

    private final Uri[] mNfcPushUris = new Uri[1];

    private LocalMediaObserver mLocalImagesObserver;
    private LocalMediaObserver mLocalVideosObserver;

    private CameraController mCameraController;
    private boolean mPaused;
    private CameraAppUI mCameraAppUI;

    private PeekAnimationHandler mPeekAnimationHandler;
    private HandlerThread mPeekAnimationThread;

    private long mOnCreateTime;

    private Runnable mThumbUpdateRunnable;

    /**
     * Can be used to play custom sounds.
     */
    private SoundPlayer mSoundPlayer;
    private SoundClips.Player mSoundClipsPlayer;

    private static final int LIGHTS_OUT_DELAY_MS = 4000;
    private final int BASE_SYS_UI_VISIBILITY =
            View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
    private final Runnable mLightsOutRunnable = new Runnable() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(BASE_SYS_UI_VISIBILITY);
            if (isPhotoContactsIntent()) {
                window.setNavigationBarColor(Color.TRANSPARENT);
            } else {
                window.setNavigationBarColor(getResources().getColor(R.color.bottombar_background_overlay));
            }
        }
    };
    private MemoryManager mMemoryManager;
    private MotionManager mMotionManager;

    private ArrayList<Uri> mSecureUris;

    private boolean mKeepSecureModule = false;
    private boolean mSecureFyuseModule = false;

    // Battery check
    private BatteryBroadcastReceiver mBatteryChangedReceiver;

    /* MODIFIED-BEGIN by fei.hui, 2016-09-09,BUG-2868515*/
    public static final int CRITICAL_LOW_BATTERY_LEVEL = 2;
    public static final int LOW_BATTERY_LEVEL = 8;
    /* MODIFIED-END by fei.hui,BUG-2868515*/
    /*MODIFIED-BEGIN by peixin, 2016-04-19,BUG-1950778*/
    public int WARNING_BATTERY_LEVEL = 10;
    public static final int WARNING_BATTERY_LEVEL_MTK = 15; //MODIFIED by peixin, 2016-04-15,BUG-1950778
    /*MODIFIED-END by peixin,BUG-1950778*/
    public static final int BATTERY_STATUS_OK = 0;
    public static final int BATTERY_STATUS_WARNING = 1;
    public static final int BATTERY_STATUS_LOW = 2;
    private int currentBatteryStatus = BATTERY_STATUS_OK;

    private AlertDialog mBatteryWarningDialog;
    private AlertDialog mBatteryLowDialog;
    private AlertDialog mStorageLowDialog;

    private int mBatteryLevel;
    private OnBatteryLowListener mBatteryLowListener;
    private boolean mBatteryLevelLowFirst = true;
    private boolean mBatteryLevelLowFirstEnterCamera = true; // MODIFIED by feifei.xu, 2016-11-03,BUG-3304401

    // Inner storage check
    /* MODIFIED-BEGIN by fei.hui, 2016-09-09,BUG-2868515*/
    private final long INNER_STORAGE_THRESHOLD = 50 * 1024 * 1024L;
    private final long INNER_STORAGE_CRITICAL_LOW = 25 * 1024 * 1024L;
    /* MODIFIED-END by fei.hui,BUG-2868515*/
    private long freeInnerStorage;
    private OnInnerStorageLowListener mInnerStorageLowListener;

    private static final String FLASH_OFF = "off";
    private static final String TIZR_PACKAGE_NAME = "com.app_tizr.app.in_house";

    private HelpTipsManager mHelpTipsManager;
    boolean mRequestPermissionsFinished = false;//Permissions finished flag
    private boolean mBatterySaveOn = false;//battery save value in Phone Settings// MODIFIED by nie.lei, 2016-03-21, BUG-1845068
    private ControlPoseCallback mCallback; //MODIFIED by shunyin.zhang, 2016-04-12,BUG-1892480
    /* MODIFIED-BEGIN by xuan.zhou, 2016-07-11,BUG-2481700*/
    private boolean mShowLocationPrompt;
    private Dialog mLocationPrompt;

    /* MODIFIED-BEGIN by fei.hui, 2016-10-25,BUG-3167899*/
    @Override
    public void setIsPoseSelectorShowing(Boolean boomkeyEnable) {
        mCameraAppUI.setIsPoseSelectorShowing(boomkeyEnable);
    }
    /* MODIFIED-END by fei.hui,BUG-3167899*/

    public interface LocationDialogCallback {
        /**
         * Gets called after user selected/unselected geo-tagging feature.
         *
         * @param selected whether or not geo-tagging feature is selected
         */
        public void onLocationTaggingSelected(boolean selected);
    }
    /* MODIFIED-END by xuan.zhou,BUG-2481700*/

    @Override
    public CameraAppUI getCameraAppUI() {
        return mCameraAppUI;
    }

    @Override
    public CameraAppUI.LockEventListener getLockEventListener() {
        return mCameraAppUI.gLockFSM;
    }

    @Override
    public ModuleManager getModuleManager() {
        return mModuleManager;
    }

    /**
     * Close activity when secure app passes lock screen or screen turns
     * off.
     */
    private final BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /* MODIFIED-BEGIN by xuan.zhou, 2016-04-26,BUG-1998180*/
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()) &&
                    !CameraUtil.isCameraDoubleTapPowerGestureDisbled(CameraActivity.this) &&
                    !CameraUtil.isScreenOff(CameraActivity.this)) {
                // If Camera double tap power gesture enabled and the screen is already on,
                // ignore the finish.
                return;
            }
            /* MODIFIED-END by xuan.zhou,BUG-1998180*/
            finish();
        }
    };

    private final BroadcastReceiver mPowerSaveModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(intent.getAction())) {
                keepScreenOnForAWhile();
            }
        }
    };

    @Override
    public void onVideoRecordingStarted() {
        mCameraAppUI.onVideoRecordingStateChanged(true);
    }

    @Override
    public void onVideoRecordingStop() {
        if (mPaused) {
            return;
        }

        onModeSelecting(true, new ModeTransitionView.OnTransAnimationListener() {
            @Override
            public void onAnimationDone() {
                mCameraAppUI.onModeSelected(getResources().getInteger(R.integer.camera_mode_photo));
                mCameraAppUI.onVideoRecordingStateChanged(false);
                if (mHelpTipsManager != null) {
                    mHelpTipsManager.checkAlarmTaskHelpTip();
                }
            }
        });
    }

    /**
     * Whether the screen is kept turned on.
     */
    private boolean mKeepScreenOn;
    private int mLastLayoutOrientation;

    @Override
    public void onCameraOpened(CameraAgent.CameraProxy camera) {
        Log.v(TAG, "onCameraOpened");
        Log.w(TAG, "on Camera opened in camera activity");
        if (mPaused) {
            // We've paused, but just asynchronously opened the camera. Close it
            // because we should be releasing the camera when paused to allow
            // other apps to access it.
            Log.v(TAG, "received onCameraOpened but activity is paused, closing Camera");
            mCameraController.closeCamera(false);
            return;
        }
        /**
         * The current UI requires that the flash option visibility in front-facing
         * camera be
         *   * disabled if back facing camera supports flash
         *   * hidden if back facing camera does not support flash
         * We save whether back facing camera supports flash because we cannot get
         * this in front facing camera without a camera switch.
         *
         * If this preference is cleared, we also need to clear the camera facing
         * setting so we default to opening the camera in back facing camera, and
         * can save this flash support value again.
         */
        if (!mSettingsManager.isSet(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_FLASH_SUPPORTED_BACK_CAMERA)) {
            HardwareSpec hardware =
                    new HardwareSpecImpl(getCameraProvider(), camera.getCapabilities());
            mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                    Keys.KEY_FLASH_SUPPORTED_BACK_CAMERA,
                    hardware.isFlashSupported());
        }

        if (!mModuleManager.getModuleAgent(mCurrentModeIndex).requestAppForCamera()) {
            // We shouldn't be here. Just close the camera and leave.
            mCameraController.closeCamera(false);
            throw new IllegalStateException("Camera opened but the module shouldn't be " +
                    "requesting");
        }
        if (mCurrentModule != null) {
            resetParametersToDefault(camera);
            mCurrentModule.onCameraAvailable(camera);
        } else {
            Log.v(TAG, "mCurrentModule null, not invoking onCameraAvailable");
            mCameraReadyListener = new OnCameraReady(camera) {
                @Override
                public void onCameraReady() {
                    mCurrentModule.onCameraAvailable(mCameraProxy.get());
                }
            };
        }
        Log.v(TAG, "invoking onChangeCamera");
        mCameraAppUI.onChangeCamera();

        /* MODIFIED-BEGIN by xuan.zhou, 2016-06-06,BUG-2251935*/
        if (mCurrentModule != null) {
            mCurrentModule.onSpecificUIApplied();
        }
        /* MODIFIED-END by xuan.zhou,BUG-2251935*/
    }

    @Override
    public void onCameraOpenedBoost(CameraAgent.CameraProxy camera) {
        //dummy , interface required
    }

    @Override
    public boolean isBoostPreview() {
        return false;//dummy
    }

    @Override
    public Context getCallbackContext() {
        return this.getApplicationContext();
    }

    @Override
    public CameraSettings.BoostParameters getBoostParam() {
        return null;//dummy , interface required
    }

    private void resetParametersToDefault(CameraAgent.CameraProxy camera) {
        // Reset the exposure compensation before handing the camera to module.
        if (mCameraController.isBoostPreview()) {
            return;
        }
        CameraSettings cameraSettings = camera.getSettings();
        cameraSettings.setExposureCompensationIndex(0);
        /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-26,BUG-1996450*/
        if (ExtBuild.device() != ExtBuild.MTK_MT6755) {
            cameraSettings.setFaceBeauty(false, 0);
        } else {
            cameraSettings.setFaceBeauty(false, 0, 0);
        }
        /* MODIFIED-END by bin.zhang2-nb,BUG-1996450*/
        cameraSettings.setLowLight(false);
        CameraCapabilities cameraCapabilities = camera.getCapabilities();
        if (Keys.isHdrOn(mSettingsManager, this) || Keys.isHdrAuto(mSettingsManager, this)) { // MODIFIED by xuyang.liu, 2016-10-13,BUG-3110198
            if (cameraCapabilities != null && cameraCapabilities.supports(CameraCapabilities.SceneMode.AUTO)) {
                cameraSettings.setSceneMode(CameraCapabilities.SceneMode.AUTO);
            }
        }
        cameraSettings.setSuperResolutionOn(false);
        camera.applySettings(cameraSettings);
    }

    @Override
    public void onCameraDisabled(int cameraId) {
        UsageStatistics.instance().cameraFailure(
                eventprotos.CameraFailure.FailureReason.SECURITY, null,
                UsageStatistics.NONE, UsageStatistics.NONE);
        Log.w(TAG, "Camera disabled: " + cameraId);
        CameraUtil.showErrorAndFinish(this, R.string.camera_disabled);
    }

    @Override
    public void onDeviceOpenFailure(int cameraId, String info) {
        UsageStatistics.instance().cameraFailure(
                eventprotos.CameraFailure.FailureReason.OPEN_FAILURE, info,
                UsageStatistics.NONE, UsageStatistics.NONE);
        Log.w(TAG, "Camera open failure: " + info);
        CameraUtil.showErrorAndFinish(this, R.string.cannot_connect_camera);
    }

    @Override
    public void onDeviceOpenedAlready(int cameraId, String info) {
        Log.w(TAG, "Camera open already: " + cameraId + "," + info);
        CameraUtil.showErrorAndFinish(this, R.string.cannot_connect_camera);
    }

    @Override
    public void onReconnectionFailure(CameraAgent mgr, String info) {
        UsageStatistics.instance().cameraFailure(
                eventprotos.CameraFailure.FailureReason.RECONNECT_FAILURE, null,
                UsageStatistics.NONE, UsageStatistics.NONE);
        Log.w(TAG, "Camera reconnection failure:" + info);
        CameraUtil.showErrorAndFinish(this, R.string.cannot_connect_camera);
    }

    @Override
    public void onCameraRequested() {
    }

    @Override
    public void onCameraClosed() {
    }

    @Override
    public boolean isReleased() {
        return false;
    }

    private static class MainHandler extends Handler {
        final WeakReference<CameraActivity> mActivity;

        public MainHandler(CameraActivity activity, Looper looper) {
            super(looper);
            mActivity = new WeakReference<CameraActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            CameraActivity activity = mActivity.get();
            if (activity == null) {
                return;
            }
            switch (msg.what) {

                case MSG_CLEAR_SCREEN_ON_FLAG: {
                    if (!activity.mPaused) {
                        activity.getWindow().clearFlags(
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        activity.finish();
                    }
                    break;
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setupNfcBeamPush() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(mAppContext);
        if (adapter == null) {
            return;
        }

        if (!ApiHelper.HAS_SET_BEAM_PUSH_URIS) {
            // Disable beaming
            adapter.setNdefPushMessage(null, CameraActivity.this);
            return;
        }

        adapter.setBeamPushUris(null, CameraActivity.this);
        adapter.setBeamPushUrisCallback(new CreateBeamUrisCallback() {
            @Override
            public Uri[] createBeamUris(NfcEvent event) {
                return mNfcPushUris;
            }
        }, CameraActivity.this);
    }

    @Override
    public Context getAndroidContext() {
        return mAppContext;
    }

    @Override
    public void launchActivityByIntent(Intent intent) {
        // Starting from L, we prefer not to start edit activity within camera's task.
        mResetToPreviewOnResume = false;
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        startActivity(intent);
    }

    @Override
    public int getCurrentModuleIndex() {
        return mCurrentModeIndex;
    }

    @Override
    public int getCurrentCameraId() {
        return mCameraController.getCurrentCameraId();
    }

    @Override
    public String getModuleScope() {
        return MODULE_SCOPE_PREFIX + mCurrentModule.getModuleStringIdentifier();
    }

    @Override
    public String getCameraScope() {
        int currentCameraId = getCurrentCameraId();
        if (currentCameraId < 0) {
            // if an unopen camera i.e. negative ID is returned, which we've observed in
            // some automated scenarios, just return it as a valid separate scope
            // this could cause user issues, so log a stack trace noting the call path
            // which resulted in this scenario.
            Log.w(TAG, "getting camera scope with no open camera, using id: " + currentCameraId);
        }
        return CAMERA_SCOPE_PREFIX + Integer.toString(currentCameraId);
    }

    @Override
    public ModuleController getCurrentModuleController() {
        return mCurrentModule;
    }

    @Override
    public int getQuickSwitchToModuleId(int currentModuleIndex) {
        return mModuleManager.getQuickSwitchToModuleId(currentModuleIndex, mSettingsManager,
                mAppContext);
    }

    @Override
    public SurfaceTexture getPreviewBuffer() {
        // TODO: implement this
        return null;
    }

    @Override
    public void onPreviewReadyToStart() {
        mCameraAppUI.onPreviewReadyToStart();
    }

    @Override
    public void onPreviewStarted() {
        mCameraAppUI.onPreviewStarted();
    }

    @Override
    public void addPreviewAreaSizeChangedListener(
            PreviewStatusListener.PreviewAreaChangedListener listener) {
        mCameraAppUI.addPreviewAreaChangedListener(listener);
    }

    @Override
    public void removePreviewAreaSizeChangedListener(
            PreviewStatusListener.PreviewAreaChangedListener listener) {
        mCameraAppUI.removePreviewAreaChangedListener(listener);
    }

    @Override
    public void setupOneShotPreviewListener() {
        mCameraController.setOneShotPreviewCallback(mMainHandler,
                new CameraAgent.CameraPreviewDataCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, CameraAgent.CameraProxy camera) {
                        mCurrentModule.onPreviewInitialDataReceived();
                        mCameraAppUI.onNewPreviewFrame();
                    }
                }
        );
    }

    @Override
    public void updatePreviewAspectRatio(float aspectRatio) {
        mCameraAppUI.updatePreviewAspectRatio(aspectRatio);
    }

    @Override
    public void updatePreviewTransformFullscreen(Matrix matrix, float aspectRatio) {
        mCameraAppUI.updatePreviewTransformFullscreen(matrix, aspectRatio);
    }

    @Override
    public RectF getFullscreenRect() {
        return mCameraAppUI.getFullscreenRect();
    }

    @Override
    public void updatePreviewTransform(Matrix matrix) {
        mCameraAppUI.updatePreviewTransform(matrix);
    }

    @Override
    public void setPreviewStatusListener(PreviewStatusListener previewStatusListener) {
        mCameraAppUI.setPreviewStatusListener(previewStatusListener);
    }

    @Override
    public FrameLayout getModuleLayoutRoot() {
        return mCameraAppUI.getModuleRootView();
    }

    @Override
    public void setShutterEventsListener(ShutterEventsListener listener) {
        // TODO: implement this
    }

    @Override
    public void setShutterEnabled(boolean enabled) {
        mCameraAppUI.setShutterButtonEnabled(enabled);
    }

    @Override
    /* MODIFIED-BEGIN by sichao.hu, 2016-03-22, BUG-1027573 */
    public void setShutterEnabledWithNormalAppearence(boolean enabled) {
        mCameraAppUI.setShutterButtonEnabled(enabled, false);
    }

    @Override
    /* MODIFIED-END by sichao.hu,BUG-1027573 */
    public void setShutterPress(boolean press) {
        mCameraAppUI.setShutterButtonPress(press);
    }

    @Override
    public void setShutterButtonLongClickable(boolean enabled) {
        mCameraAppUI.setShutterButtonLongClickable(enabled);
    }

    @Override
    public boolean isShutterEnabled() {
        return mCameraAppUI.isShutterButtonEnabled();
    }

    @Override
    public void startPreCaptureAnimation(boolean shortFlash) {
        mCameraAppUI.startPreCaptureAnimation(shortFlash);
    }

    @Override
    public void startPreCaptureAnimation() {
        mCameraAppUI.startPreCaptureAnimation(false);
    }

    @Override
    public void cancelPreCaptureAnimation() {
        // TODO: implement this
    }

    @Override
    public void startPostCaptureAnimation() {
        // TODO: implement this
    }

    @Override
    public void startPostCaptureAnimation(Bitmap thumbnail) {
        // TODO: implement this
    }

    @Override
    public void cancelPostCaptureAnimation() {
        // TODO: implement this
    }

    @Override
    public OrientationManager getOrientationManager() {
        return mOrientationManager;
    }

    @Override
    public LocationManager getLocationManager() {
        return mLocationManager;
    }

    @Override
    public void lockOrientation() {
        if (mOrientationManager != null) {
            mOrientationManager.lockOrientation();
        }
    }

    @Override
    public void unlockOrientation() {
        if (mOrientationManager != null) {
            mOrientationManager.unlockOrientation();
        }
    }

    /**
     * Starts the filmstrip peek animation if the filmstrip is not visible.
     * Only {@link LocalData#LOCAL_IMAGE}, {@link
     * LocalData#LOCAL_IN_PROGRESS_DATA} and {@link
     * LocalData#LOCAL_VIDEO} are supported.
     *
     * @param data                The data to peek.
     * @param accessibilityString Accessibility string to announce on peek animation.
     */
    private void startPeekAnimation(final LocalData data, final String accessibilityString) {
        if (mPeekAnimationHandler == null) {
            mThumbUpdateRunnable = new Runnable() {
                @Override
                public void run() {
                    mPeekAnimationHandler.startDecodingJob(data, new Callback<Bitmap>() {
                        @Override
                        public void onCallback(Bitmap result) {
                            Log.w(TAG, " cached last thumb update for " + data.getUri());
                            final Thumbnail lastThumb = Thumbnail.createThumbnail(data.getUri(), result, data.getRotation());
                            mCameraAppUI.updatePeekThumbContent(lastThumb);
                        }
                    });
                }
            };
        }
        if (mPeekAnimationHandler == null) {
            return;
        }

        int dataType = data.getLocalDataType();
        if (dataType != LocalData.LOCAL_IMAGE && dataType != LocalData.LOCAL_IN_PROGRESS_DATA &&
                dataType != LocalData.LOCAL_VIDEO) {
            return;
        }

        mPeekAnimationHandler.startDecodingJob(data, new Callback<Bitmap>() {
            @Override
            public void onCallback(Bitmap result) {
                int rotation = data.getRotation();
                if (data.getLocalDataType() == LocalData.LOCAL_IMAGE) {
                    // here, don't need to rotate again, cause it has been done in startDecodingJob().
                    rotation = 0;
                }
                Log.w(TAG, "update last thumb " + data.getUri());
                final Thumbnail lastThumb = Thumbnail.createThumbnail(data.getUri(), result, rotation);
                if (mPaused) {
                    mCameraAppUI.updatePeekThumbContent(lastThumb);
                } else {
//                    mCameraAppUI.startScalingPeekAnimation(result, true, accessibilityString, new PeekView.OnCaptureStateListener() {
//                        @Override
//                        public void onCaptureAnimationComplete() {
//                            mCameraAppUI.updatePeekThumbContent(lastThumb);
//                        }
//                    });
                    mCameraAppUI.updatePeekThumbBitmapWithAnimation(lastThumb.getBitmap());
                    mCameraAppUI.updatePeekThumbUri(data.getUri());
                }
            }
        });
    }

    private static final int NOTIFY_NEW_MEDIA_ACTION_ANIMATION = AppController.NOTIFY_NEW_MEDIA_ACTION_ANIMATION;
    private static final int NOTIFY_NEW_MEDIA_ACTION_UPDATETHUMB = AppController.NOTIFY_NEW_MEDIA_ACTION_UPDATETHUMB;
    private static final int NOTIFY_NEW_MEDIA_ACTION_OPTIMIZECAPTURE = AppController.NOTIFY_NEW_MEDIA_ACTION_OPTIMIZECAPTURE;
    private static final int NOTIFY_NEW_MEDIA_DEFALT_ACTION = AppController.NOTIFY_NEW_MEDIA_DEFALT_ACTION;

    @Override
    public void notifyNewMedia(Uri uri, int action) {
        boolean needAnimation = ((action & NOTIFY_NEW_MEDIA_ACTION_ANIMATION) != 0);
        boolean needUpdateThumb = ((action & NOTIFY_NEW_MEDIA_ACTION_UPDATETHUMB) != 0);
        boolean optimizeCapture = ((action & NOTIFY_NEW_MEDIA_ACTION_OPTIMIZECAPTURE) != 0);
        notifyNewMedia(uri, needAnimation, needUpdateThumb, optimizeCapture); // MODIFIED by jianying.zhang, 2016-06-17,BUG-2377110
    }

    @Override
    public void notifyNewMedia(Uri uri) {
        notifyNewMedia(uri, NOTIFY_NEW_MEDIA_DEFALT_ACTION);
    }

    private ArrayList<Uri> getSecureUris() {
        if (mSecureUris == null) {
            mSecureUris = new ArrayList<>();
        }
        return mSecureUris;
    }

    private void clearSecureUris() {
        if (mSecureUris != null) {
            mSecureUris.clear();
            ;
            mSecureUris = null;
        }
    }

    private void notifyNewMedia(final Uri uri, final boolean needAnimation, final boolean needUpdateThumb, final boolean optimizeCapture) { //MODIFIED by sichao.hu, 2016-04-05,BUG-1910923
        // TODO: This method is running on the main thread. Also we should get
        // rid of that AsyncTask.

        if (isSecureCamera()) {
            mSecureUris = getSecureUris();
            mSecureUris.add(uri);
        }

        updateStorageSpaceAndHint(null);
        ContentResolver cr = getContentResolver();
         /*MODIFIED-BEGIN by sichao.hu, 2016-04-05,BUG-1910923*/
        final String mimeType = cr.getType(uri);

        // We are preloading the metadata for new video since we need the
        // rotation info for the thumbnail.
        new AsyncTask<Void, Void, LocalData>() {
            @Override
            protected LocalData doInBackground(Void... params) {

                LocalData newData = null;
                if (LocalDataUtil.isMimeTypeVideo(mimeType)) {
                    sendBroadcast(new Intent(CameraUtil.ACTION_NEW_VIDEO, uri));
                    newData = LocalMediaData.VideoData.fromContentUri(getContentResolver(), uri);
                    if (newData == null) {
                        Log.e(TAG, "Can't find video data in content resolver:" + uri);
                        return null;
                    }
                } else if (LocalDataUtil.isMimeTypeImage(mimeType)) {
                    CameraUtil.broadcastNewPicture(mAppContext, uri);
                    newData = LocalMediaData.PhotoData.fromContentUri(getContentResolver(), uri);
                    if (newData == null) {
                        Log.e(TAG, "Can't find photo data in content resolver:" + uri);
                        return null;
                    }
                } else {
                    Log.w(TAG, "Unknown new media with MIME type:" + mimeType + ", uri:" + uri);
                    return null;
                }

                if (!needUpdateThumb) {
                    return null;
                }
                LocalData data = newData;
                MetadataLoader.loadMetadata(getAndroidContext(), data);
                return data;
            }

            @Override
            protected void onPostExecute(final LocalData data) {
                if (data == null) {
                    return;
                }
                 /*MODIFIED-END by sichao.hu,BUG-1910923*/

                if (!needAnimation) {
                    mThumbUpdateRunnable = new Runnable() {
                        @Override
                        public void run() {
                            mPeekAnimationHandler.startDecodingJob(data, new Callback<Bitmap>() {
                                @Override
                                public void onCallback(Bitmap result) {
                                    Log.w(TAG, "last thumb url is " + data.getUri());
                                    final Thumbnail lastThumb = Thumbnail.createThumbnail(data.getUri(), result, data.getRotation());
                                    mCameraAppUI.updatePeekThumbContent(lastThumb);
                                }
                            }, getContentResolver()); // MODIFIED by jianying.zhang, 2016-06-17,BUG-2377110
                        }
                    };
                    if (mPeekAnimationHandler == null) {
                        return;//Could happen in case of paused  , happens in UI thread , just skip it
                    }
                    mThumbUpdateRunnable.run();
                    mThumbUpdateRunnable = null;

                    return;
                }

                if (optimizeCapture) {//Check customization
                    ContentResolver cr = getContentResolver();
                    String mimeType = cr.getType(data.getUri());
                    if (LocalDataUtil.isMimeTypeImage(mimeType)) {
                        mCameraAppUI.updatePeekThumbUri(data.getUri());
                    } else if (LocalDataUtil.isMimeTypeVideo(mimeType)) {
                        startPeekAnimation(data, mCurrentModule != null ? mCurrentModule.getPeekAccessibilityString() : "");
                    } else {
                        Log.w(TAG, "Unknown new media with MIME type:" + mimeType);
                    }
                } else {
                    startPeekAnimation(data, mCurrentModule != null ? mCurrentModule.getPeekAccessibilityString() : "");
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); //MODIFIED by sichao.hu, 2016-04-05,BUG-1910923
    }

    @Override
    public void enableKeepScreenOn(boolean enabled) {
        if (mPaused) {
            return;
        }

        mKeepScreenOn = enabled;
        if (mKeepScreenOn) {
            mMainHandler.removeMessages(MSG_CLEAR_SCREEN_ON_FLAG);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            keepScreenOnForAWhile();
        }
    }

    @Override
    public CameraProvider getCameraProvider() {
        return mCameraController;
    }

    @Override
    public OneCameraManager getCameraManager() {
        return mCameraManager;
    }

    private boolean isCaptureIntent() {
        if (MediaStore.ACTION_VIDEO_CAPTURE.equals(getIntent().getAction())
                || MediaStore.ACTION_IMAGE_CAPTURE.equals(getIntent().getAction())
                || isPhotoContactsIntent()
                || MediaStore.ACTION_IMAGE_CAPTURE_SECURE.equals(getIntent().getAction())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isVideoCaptureIntent() {
        return MediaStore.ACTION_VIDEO_CAPTURE.equals(getIntent().getAction());
    }

    private boolean isPhotoCaptureIntent() {
        return MediaStore.ACTION_IMAGE_CAPTURE.equals(getIntent().getAction())
                || isPhotoContactsIntent();
    }

    public boolean isPhotoContactsIntent() {
        return MST_SCAN_BUSINESSCARD_ACTION.equals(getIntent().getAction());
    }

    private boolean isFrontPhotoIntent() {
        return ACTION_START_FRONT_CAMERA.equals(getIntent().getAction());
    }

    /**
     * Note: Make sure this callback is unregistered properly when the activity
     * is destroyed since we're otherwise leaking the Activity reference.
     */
    private final CameraExceptionHandler.CameraExceptionCallback mCameraExceptionCallback
            = new CameraExceptionHandler.CameraExceptionCallback() {
        @Override
        public void onCameraError(int errorCode) {
            // Not a fatal error. only do Log.e().
            Log.e(TAG, "Camera error callback. error=" + errorCode);
        }

        @Override
        public void onCameraException(
                RuntimeException ex, String commandHistory, int action, int state) {
            Log.e(TAG, "Camera Exception", ex);
            UsageStatistics.instance().cameraFailure(
                    eventprotos.CameraFailure.FailureReason.API_RUNTIME_EXCEPTION,
                    commandHistory, action, state);
            onFatalError();
        }

        @Override
        public void onDispatchThreadException(RuntimeException ex) {
            Log.e(TAG, "DispatchThread Exception", ex);
            UsageStatistics.instance().cameraFailure(
                    eventprotos.CameraFailure.FailureReason.API_TIMEOUT,
                    null, UsageStatistics.NONE, UsageStatistics.NONE);
            onFatalError();
        }

        private void onFatalError() {
            if (mCameraFatalError) {
                return;
            }
            mCameraFatalError = true;

            // If the activity receives exception during onPause, just exit the app.
            if (mPaused && !isFinishing()) {
                Log.e(TAG, "Fatal error during onPause, call Activity.finish()");
                finishAndQuitProcess();
            } else {
                CameraUtil.showErrorAndFinish(CameraActivity.this,
                        R.string.cannot_connect_camera);
            }
        }
    };

    public void finishAndQuitProcess() {
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public Integer lockModuleSelection() {
        Log.v(TAG, "lock moduleSelection ", new Throwable());
        return mModeStripView.lockView();
    }

    @Override
    public boolean unlockModuleSelection(Integer token) {
        Log.v(TAG, "unlock moduleSelection ", new Throwable());
        return mModeStripView.unLockView(token);
    }

    @Override
    public boolean onPeekThumbClicked(Uri uri) {
        Log.v(TAG, "onPeekThumbClicked");
        Uri mUri = uri;
        final String GALLERY_PACKAGE_NAME = "com.tct.gallery3d";
        final String GALLERY_ACTIVITY_CLASS = "com.tct.gallery3d.app.PermissionActivity";
        final String REVIEW_ACTION = "com.android.camera.action.REVIEW";
        mResetToPreviewOnResume = false;
        if (mUri != null && !isSecureCamera()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW).setClassName(
                        GALLERY_PACKAGE_NAME, GALLERY_ACTIVITY_CLASS);
                intent.setFlags(0);
                intent.setType("*/*");
                intent.setData(mUri);
                startActivity(intent);
//              launchActivityByIntent(intent);
                getLockEventListener().onModeSwitching();
                ;//All lock here would be unlocked in case of video mode / photo mode idle
            } catch (ActivityNotFoundException e) {
                try {
                    Log.w(TAG, "com.tct.gallery3d not found");
                    Intent intent = new Intent(Intent.ACTION_VIEW, mUri);
                    startActivity(intent);
//                  launchActivityByIntent(intent);
                    getLockEventListener().onModeSwitching();
                } catch (Exception ex) {
                    Log.w(TAG, "No Activity could be found to open image or video" + ex);
                }
            }
        } else if (mUri != null) {
            try {
                Intent intent = new Intent(REVIEW_ACTION);
                intent.setClassName(
                        GALLERY_PACKAGE_NAME, GALLERY_ACTIVITY_CLASS);
                intent.setFlags(1);
                intent.setType("*/*");
                intent.setData(mUri);
                intent.putParcelableArrayListExtra("uriarray", mSecureUris);
                startActivity(intent);
//              launchActivityByIntent(intent);
                getLockEventListener().onModeSwitching();
                mKeepSecureModule = true;
            } catch (Exception ex) {
                Log.w(TAG, "Secure camera, com.tct.gallery3d not found");
            }
        }
        TestUtils.sendMessage(R.id.peek_thumb, TestUtils.MESSAGE.THUMBNAIL_CLICKED); // MODIFIED by wenhua.tu, 2016-08-11,BUG-2710178
        return true;
    }

    @Override
    public void onPeekThumbClicked() {
        setResult(Activity.RESULT_OK,
                new Intent().putExtra(ACTION_GOTOGALLERY, true));
        finish();
    }

    public Uri getPeekThumbUri() {
        return mCameraAppUI == null ? null :
                mCameraAppUI.getPeekThumbUri();
    }

    private Map<Integer, Rotatable.RotateEntity> mListeningRotatableMap = new HashMap<>();

    @Override
    public void addRotatableToListenerPool(Rotatable.RotateEntity rotatableEntity) {
        if (!mListeningRotatableMap.containsKey(rotatableEntity.rotatableHashCode)) {
            mListeningRotatableMap.put(rotatableEntity.rotatableHashCode, rotatableEntity);
        }
    }

    @Override
    public void addLockableToListenerPool(Lockable lockable) {
        if (mCameraAppUI != null) {
            mCameraAppUI.addLockableToListenerPool(lockable);
        }
    }

    @Override
    public void removeLockableFromListenerPool(Lockable lockable) {
        if (mCameraAppUI != null) {
            mCameraAppUI.removeLockableFromListenerPool(lockable);
        }
    }

    @Override
    public void removeRotatableFromListenerPool(int hashCode) {
        mListeningRotatableMap.remove(hashCode);
    }

    @Override
    public void onNewIntentTasks(Intent intent) {
        String action = intent.getAction();
        Log.v(TAG, "onNewIntent " + action);
        if (INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(action)
                || ACTION_IMAGE_CAPTURE_SECURE.equals(action)) {
            mSecureCamera = true;
        } else {
            mSecureCamera = intent.getBooleanExtra(SECURE_CAMERA_EXTRA, false);
        }

        if (mSecureCamera) {
            Keys.resetSecureModuleIndex(mSettingsManager,
                    getResources().getInteger(R.integer.camera_mode_photo));
        }

        // If activity pause in secure mode and be launched again,
        // it's better to read camera id here.
        updateCameraForFunc();

        int modeIndex = getModeIndex();
        Log.w(TAG, "last mode is " + modeIndex + " currentMode index is " + mCurrentModeIndex);
        if (!mSecureCamera && !isCaptureIntent() && mCurrentModeIndex != modeIndex) {
            mModeSelectingOnStart = true;
            switchToMode(modeIndex);
//        onModeSelected(getModeIndex());
        } else if (mSecureCamera) {
            // This happens when secure camera starts again after it's paused via home pressed.
            // Clear the peek thumb here.
            clearSecureUris();
            mCameraAppUI.updatePeekThumbContent(null);
            /* MODIFIED-BEGIN by xuan.zhou, 2016-10-15,BUG-3133741*/
            // mThumbUpdateRunnable may be not null for skipped during pause, and then compensate
            // thumbnail update later in resume. Ignore it if it's secure camera.
            mThumbUpdateRunnable = null;
            /* MODIFIED-END by xuan.zhou,BUG-3133741*/
        }
    }

    @Override
    public void onCreateTasks(Bundle state) {
        Log.w(TAG, "KPI onCreateTasks");
        mAppContext = getApplication().getBaseContext();

        /* MODIFIED-BEGIN by xuan.zhou, 2016-07-11,BUG-2481700*/
        mSettingsManager = getServices().getSettingsManager();
        boolean locationPrompt = !mSettingsManager.isSet(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_RECORD_LOCATION);
        mShowLocationPrompt = locationPrompt && Intent.ACTION_MAIN.equals(getIntent().getAction()) &&
                CustomUtil.getInstance().getBoolean(
                        CustomFields.DEF_CAMERA_SHOW_LOCATION_PROMPT, false);

        boolean mPermissionsGranted;
        if (mShowLocationPrompt) {
            showLocationPrompt();
            // Check the permissions after gps tagging confirmed.
            mPermissionsGranted = false;
        } else {
            mPermissionsGranted = checkPermissions();
        }

        mOnCreateTime = System.currentTimeMillis();
        mSoundPlayer = new SoundPlayer(mAppContext);
        mMainHandler = new MainHandler(this, getMainLooper());
        mCameraController = new CameraController(mAppContext, this, mMainHandler,
                CameraAgentFactory.getAndroidCameraAgent(mAppContext,
                        CameraAgentFactory.CameraApi.API_1),
                CameraAgentFactory.getAndroidCameraAgent(mAppContext,
                        GservicesHelper.useCamera2ApiThroughPortabilityLayer(mAppContext) ?
                                CameraAgentFactory.CameraApi.AUTO :
                                CameraAgentFactory.CameraApi.API_1));
        mCameraController.setCameraExceptionHandler(
                new CameraExceptionHandler(mCameraExceptionCallback, mMainHandler));


        // TODO: Try to move all the resources allocation to happen as soon as
        // possible so we can call module.init() at the earliest time.
        mModuleManager = new ModuleManagerImpl();
        GcamHelper.init(getContentResolver());
        if (isPhotoCaptureIntent()) {
            Log.d(TAG, "action : " + getIntent().getAction());
            if (isPhotoContactsIntent()) {
                ModulesInfo.setupPhotoContactsIntentModules(mAppContext, mModuleManager);
            } else {
                ModulesInfo.setupPhotoCaptureIntentModules(mAppContext, mModuleManager);
            }
        } else if (isVideoCaptureIntent()) {
            ModulesInfo.setupVideoCaptureIntentModules(mAppContext, mModuleManager);
        } else {
            ModulesInfo.setupModules(mAppContext, mModuleManager);
        }

        // mSettingsManager = getServices().getSettingsManager(); // MODIFIED by xuan.zhou, 2016-07-11,BUG-2481700
        AppUpgrader appUpgrader = new AppUpgrader(this);
        appUpgrader.upgrade(mSettingsManager);
        Keys.setDefaults(mSettingsManager, mAppContext);
        Keys.setNewLaunchingForMicroguide(mSettingsManager, true);
        Keys.setNewLaunchingForMicrotip(mSettingsManager, true);
        Keys.setNewLaunchingForHdrtoast(mSettingsManager, true);
        Keys.setNewLaunchingForNighttoast(mSettingsManager, true);
        Keys.resetSecureModuleIndex(mSettingsManager,
                getResources().getInteger(R.integer.camera_mode_photo));

        // Update module before getModeIndex().
        updateCameraForFunc();

        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
        if (!isCaptureIntent()) {
            Log.d(TAG, "setFilterValueDefaults");
            Keys.setFilterValueDefaults(mSettingsManager, mAppContext);
        }
        /* MODIFIED-END by jianying.zhang,BUG-3255060*/

         /*MODIFIED-BEGIN by yuanxing.tan, 2016-03-31,BUG-1887448*/
        int startIndex = getModeIndex();
        ModuleManagerImpl.ModuleAgent agent = mModuleManager.getModuleAgent(startIndex);
        if (!isCaptureIntent() && agent != null && (!agent.needAddToStrip() || startIndex != agent.getModuleId())) {
            int photoIndex = getResources().getInteger(R.integer.camera_mode_photo);
            /* MODIFIED-BEGIN by feifei.xu, 2016-10-31,BUG-3275225*/
            int videoIndex = getResources().getInteger(R.integer.camera_mode_video);
            if (startIndex == getResources().getInteger(R.integer.camera_mode_videofilter)) {
                startIndex = videoIndex;
            } else {
                startIndex = photoIndex;
            }
            mSettingsManager.set(SettingsManager.SCOPE_GLOBAL, Keys.KEY_STARTUP_MODULE_INDEX,
                    startIndex);
                    /* MODIFIED-END by feifei.xu,BUG-3275225*/
        }
        setModuleFromModeIndex(startIndex);
         /*MODIFIED-END by yuanxing.tan,BUG-1887448*/
        if (mCurrentModule != null) {
            mCurrentModule.hardResetSettings(mSettingsManager);
        }

        int cameraId = mSettingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_CAMERA_ID);

        mSurface = new SurfaceTexture(0);
        mSurface.detachFromGLContext();
        if (!(mCurrentModule != null && mCurrentModule instanceof FyuseModule)) {
            if (mPermissionsGranted && !locationPrompt && !mShowLocationPrompt) { // MODIFIED by xuan.zhou, 2016-07-11,BUG-2481700
                boostCamera(cameraId);
            }
        }

        setContentView(R.layout.activity_main);

//        try {
//            mCameraManager = OneCameraManager.get(this);
//        } catch (OneCameraException e) {
//            Log.d(TAG, "Creating camera manager failed.", e);
//            CameraUtil.showErrorAndFinish(this, R.string.cannot_connect_camera);
//            return;
//        }

        if (ApiHelper.HAS_ROTATION_ANIMATION) {
            setRotationAnimation();
        }

        mModeStripView = (StereoModeStripView) findViewById(R.id.mode_strip_view);
        mModeStripView.init(mModuleManager);
        int initialModeIndex = getModeIndex();
        Log.v(TAG, "expected mode index is " + initialModeIndex);
        mModeStripView.setCurrentModeWithModeIndex(initialModeIndex);

        // Check if this is in the secure camera mode.
        Intent intent = getIntent();
        String action = intent.getAction();
        if (INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(action)
                || ACTION_IMAGE_CAPTURE_SECURE.equals(action)) {
            mSecureCamera = true;
        } else {
            mSecureCamera = intent.getBooleanExtra(SECURE_CAMERA_EXTRA, false);
        }
        if (mSecureCamera) {
            // Change the window flags so that secure camera can show when locked
            Window win = getWindow();
            WindowManager.LayoutParams params = win.getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
            win.setAttributes(params);
            // Filter for screen off so that we can finish secure camera
            // activity when screen is off.
            IntentFilter filter_screen_off = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            registerReceiver(mShutdownReceiver, filter_screen_off);
            // Filter for phone unlock so that we can finish secure camera
            // via this UI path:
            //    1. from secure lock screen, user starts secure camera
            //    2. user presses home button
            //    3. user unlocks phone
            IntentFilter filter_user_unlock = new IntentFilter(Intent.ACTION_USER_PRESENT);
            registerReceiver(mShutdownReceiver, filter_user_unlock);
        }
        IntentFilter power_save_mode_filter =
                new IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
        registerReceiver(mPowerSaveModeReceiver, power_save_mode_filter);

        mCameraAppUI = new CameraAppUI(this,
                (MainActivityLayout) findViewById(R.id.activity_root_view), isCaptureIntent());

        CameraPerformanceTracker.onEvent(CameraPerformanceTracker.ACTIVITY_START);

        if (!Glide.isSetup()) {
            Glide.setup(new GlideBuilder(getAndroidContext())
                    .setResizeService(new FifoPriorityThreadPoolExecutor(2)));
            Glide.get(getAndroidContext()).setMemoryCategory(MemoryCategory.HIGH);
        }
        BlurUtil.initialize(this.getApplicationContext());

        IntentFilter filter_media_action = new IntentFilter(Intent.ACTION_MEDIA_UNMOUNTED);
        filter_media_action.addAction(Intent.ACTION_MEDIA_EJECT);
        filter_media_action.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter_media_action.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        filter_media_action.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        filter_media_action.addDataScheme("file");
        registerReceiver(mMediaActionReceiver, filter_media_action);

        /* MODIFIED-BEGIN by nie.lei, 2016-05-05,BUG-2073514*/
        IntentFilter filter_shutdown_action = new IntentFilter(Intent.ACTION_SHUTDOWN);
        registerReceiver(mRestartPhoneReceiver, filter_shutdown_action);
        /* MODIFIED-END by nie.lei,BUG-2073514*/

        // Add the session listener so we can track the session progress
        // updates.
        mPanoramaViewHelper = new PanoramaViewHelper(this);
        mPanoramaViewHelper.onCreate();

        Log.i(TAG, "Intent action = " + intent.getAction());
        if (Intent.ACTION_MAIN.equals(intent.getAction()) && CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_HELP_TIP_TUTORIAL, false)) {// MODIFIED by yuanxing.tan, 2016-03-18, BUG-1839819
            mHelpTipsManager = new HelpTipsManager(this);
            mHelpTipsManager.startAlarmTask();
        }
        mCameraAppUI.prepareModuleUI(mSurface, isPhotoContactsIntent());

        if (mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_SHOULD_SHOW_REFOCUS_VIEWER_CLING)) {
            mCameraAppUI.setupClingForViewer(CameraAppUI.BottomPanel.VIEWER_REFOCUS);
        }

        mLocationManager = new LocationManager(mAppContext);

        mOrientationManager = new OrientationManagerImpl(this);
        mOrientationManager.addOnOrientationChangeListener(mMainHandler, this);

        mCurrentModule.init(this, isSecureCamera(), isCaptureIntent());

        if (mCameraReadyListener != null) {
            mCameraReadyListener.onCameraReady();
        }

        if (!mSecureCamera) {
            if (!isCaptureIntent() && PermissionUtil.isStoragePermissionGranted(this)) {
                onLastMediaDataUpdated();
            }
        }

        setupNfcBeamPush();

        mLocalImagesObserver = new LocalMediaObserver();
        mLocalVideosObserver = new LocalMediaObserver();

        getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true,
                mLocalImagesObserver);
        getContentResolver().registerContentObserver(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true,
                mLocalVideosObserver);
        mMemoryManager = getServices().getMemoryManager();

        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                HashMap memoryData = mMemoryManager.queryMemory();
                UsageStatistics.instance().reportMemoryConsumed(memoryData,
                        MemoryQuery.REPORT_LABEL_LAUNCH);
            }
        });
        mMotionManager = getServices().getMotionManager();

        TestUtils.getInstances(this); // MODIFIED by wenhua.tu, 2016-08-11,BUG-2710178
    }

    /* MODIFIED-BEGIN by xuan.zhou, 2016-07-11,BUG-2481700*/
    private void showLocationPrompt() {
        if (!mShowLocationPrompt) {
            return;
        }
        mLocationPrompt = CameraUtil.getLocationPrompt(CameraActivity.this,
                new LocationDialogCallback() {
                    @Override
                    public void onLocationTaggingSelected(boolean selected) {
                        boolean locationPermissionGranted =
                                PermissionUtil.isNoncriticalPermissionGranted(CameraActivity.this);
                        if (selected) {
                            if (locationPermissionGranted) {
                                Keys.setLocation(getSettingsManager(), true, getLocationManager());
                            }
                        } else {
                            Keys.setLocation(getSettingsManager(), false, getLocationManager());
                        }
                        checkPermissions();
                        mLocationPrompt = null;
                    }
                });
        if (mLocationPrompt != null) {
            mLocationPrompt.show();
        }
    }
    /* MODIFIED-END by xuan.zhou,BUG-2481700*/


    private void boostCamera(int cameraId) {
        /* MODIFIED-BEGIN by sichao.hu, 2016-05-07,BUG-2003510*/
        if (isVideoCaptureIntent() || isPhotoCaptureIntent() || isCaptureIntent()) {//Preview size could get varied from intent attribute
            return;
        }
        if (mCurrentModule instanceof FilterModule) {
            return;
        }
        /* MODIFIED-END by sichao.hu,BUG-2003510*/
        CameraSettings.BoostParameters boostParam = new CameraSettings.BoostParameters();
        boostParam.context = this.getApplicationContext();
        boostParam.isZslOn = mCurrentModule.isZslOn();
        boostParam.settingsManager = mSettingsManager;
        boostParam.surfaceTexture = mSurface;
        boostParam.cameraId = cameraId;
        Log.w(TAG, "KPI boost request Camera");
        mCameraController.requestCamera(cameraId, GservicesHelper.useCamera2ApiThroughPortabilityLayer(mAppContext), true, boostParam);
        mCameraController.boostApplySettings(boostParam);
        mCameraController.boostSetPreviewTexture(mSurface);
        mCameraController.boostStartPreview(null);
    }

    public void clearBoost() {
        mCameraController.clearBoostPreview();
    }

    private final String FUNC_SELFIE = "func_selfie";
    private final String CAMERA_ID = "cameraid";

    private void updateCameraForFunc() {
        Intent intent = getIntent();
        String action = intent.getAction();

        final int BACK_CAMERA = Integer.parseInt(getString(R.string.pref_camera_id_entry_back_value));
        final int FONT_CAMERA = Integer.parseInt(getString(R.string.pref_camera_id_entry_front_value));
        final int INVALID = -1;

        boolean updateModule = false;
        int requestCameraId = INVALID;
        int requestCameraMode = INVALID; // MODIFIED by xuan.zhou, 2016-04-28,BUG-2005112

        if (INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(action) &&
                intent.getBooleanExtra(FUNC_SELFIE, false)) {
            // In this case camera module will be updated from KEY_SECURE_MODULE_INDEX
            // later in getModeIndex().
            requestCameraId = FONT_CAMERA;
        } else if (ACTION_START_FRONT_CAMERA.equals(action)) {
            updateModule = true;
            requestCameraId = FONT_CAMERA;
        } else if (intent.hasExtra(CAMERA_ID)) {
            updateModule = true;
            requestCameraId = intent.getIntExtra(CAMERA_ID, INVALID);
        /* MODIFIED-BEGIN by xuan.zhou, 2016-04-28,BUG-2005112*/
        } else if (ACTION_START_FYUSE.equals(action) && isFyuseModuleRegistered()) {
            updateModule = true;
            requestCameraId = BACK_CAMERA;
            requestCameraMode = getResources().getInteger(R.integer.camera_mode_parallax);
            /* MODIFIED-END by xuan.zhou,BUG-2005112*/
        }

        if (updateModule ||
                (requestCameraId == BACK_CAMERA || requestCameraId == FONT_CAMERA)) {
            if (mSettingsManager == null) {
                mSettingsManager = getServices().getSettingsManager();
            }

            if (updateModule) {
                /* MODIFIED-BEGIN by xuan.zhou, 2016-04-28,BUG-2005112*/
                if (requestCameraMode != INVALID) {
                    mSettingsManager.set(SettingsManager.SCOPE_GLOBAL, Keys.KEY_STARTUP_MODULE_INDEX,
                            requestCameraMode);
                } else {
                    mSettingsManager.set(SettingsManager.SCOPE_GLOBAL, Keys.KEY_STARTUP_MODULE_INDEX,
                            getResources().getInteger(R.integer.camera_mode_photo));
                }
                /* MODIFIED-END by xuan.zhou,BUG-2005112*/
            }

            if (requestCameraId == BACK_CAMERA || requestCameraId == FONT_CAMERA) {
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL, Keys.KEY_CAMERA_ID,
                        requestCameraId);
            }
        }
    }

    /* MODIFIED-BEGIN by xuan.zhou, 2016-04-28,BUG-2005112*/
    public boolean isFyuseModuleRegistered() {
        return CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_SUPPORT_PARALLAX_MODULE, false);
    }

    public boolean isPermsRequestFinished() {
        return PermissionUtil.isCriticalPermissionGranted(this) &&
                (mSettingsManager.isSet(SettingsManager.SCOPE_GLOBAL, Keys.KEY_RECORD_LOCATION) ||
                        PermissionUtil.isNoncriticalPermissionGranted(this));
    }

    public boolean isFyuseEnabled() {
        return isFyuseModuleRegistered() && isPermsRequestFinished();
    }
    /* MODIFIED-END by xuan.zhou,BUG-2005112*/

    private static abstract class OnCameraReady {

        protected WeakReference<CameraAgent.CameraProxy> mCameraProxy;

        public OnCameraReady(CameraAgent.CameraProxy cameraProxy) {
            mCameraProxy = new WeakReference<CameraAgent.CameraProxy>(cameraProxy);
        }

        public void onCameraReady() {

        }
    }

    private OnCameraReady mCameraReadyListener;

    /**
     * Get the current mode index from the Intent or from persistent
     * settings.
     */
    public int getModeIndex() {
        int modeIndex = -1;
        int photoIndex = getResources().getInteger(R.integer.camera_mode_photo);
        int videoIndex = getResources().getInteger(R.integer.camera_mode_video);
        int videoCaptureIndex = getResources().getInteger(R.integer.camera_mode_video_capture);
        int gcamIndex = getResources().getInteger(R.integer.camera_mode_gcam);
        int contactsIntentIndex = getResources().getInteger(R.integer.camera_mode_contacts_intent);
        if (MediaStore.INTENT_ACTION_VIDEO_CAMERA.equals(getIntent().getAction())) {
            modeIndex = videoIndex;
        } else if (MediaStore.ACTION_VIDEO_CAPTURE.equals(getIntent().getAction())) {
            modeIndex = videoCaptureIndex;
        } else if (MediaStore.ACTION_IMAGE_CAPTURE.equals(getIntent().getAction())) {
            // Capture intent.
            modeIndex = photoIndex;
        } else if (isPhotoContactsIntent()) {
            modeIndex = contactsIntentIndex;
        } else if (/*MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA.equals(getIntent().getAction())
                || */MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(getIntent()
                .getAction())
                || MediaStore.ACTION_IMAGE_CAPTURE_SECURE.equals(getIntent().getAction())) {
            modeIndex = mSettingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,
                    Keys.KEY_CAMERA_MODULE_LAST_USED);

            // For upgraders who have not seen the aspect ratio selection screen,
            // we need to drop them back in the photo module and have them select
            // aspect ratio.
            // TODO: Move this to SettingsManager as an upgrade procedure.
            if (!mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                    Keys.KEY_USER_SELECTED_ASPECT_RATIO)) {
                modeIndex = photoIndex;
            }

            modeIndex = mSettingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,
                    Keys.KEY_SECURE_MODULE_INDEX, modeIndex);
        } else {
            // If the activity has not been started using an explicit intent,
            // read the module index from the last time the user changed modes
            modeIndex = mSettingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,
                    Keys.KEY_STARTUP_MODULE_INDEX);
            Log.v(TAG, "last startup mode index is " + modeIndex);
            if ((modeIndex == gcamIndex &&
                    !GcamHelper.hasGcamAsSeparateModule()) || modeIndex < 0) {
                modeIndex = photoIndex;
            }
//            modeIndex=photoIndex;
        }
        return modeIndex;
    }

    private void setRotationAnimation() {
        int rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_ROTATE;
        rotationAnimation = WindowManager.LayoutParams.ROTATION_ANIMATION_CROSSFADE;
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        winParams.rotationAnimation = rotationAnimation;
        win.setAttributes(winParams);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        if (!isFinishing()) {
            keepScreenOnForAWhile();
        }
    }

    @Override
    public void onPauseTasks() {
        CameraPerformanceTracker.onEvent(CameraPerformanceTracker.ACTIVITY_PAUSE);
        mModeSelectingOnStart = false;
        Log.w(TAG, "onPause");
        /*
         * Save the last module index after all secure camera and icon launches,
         * not just on mode switches.
         *
         * Right now we exclude capture intents from this logic, because we also
         * ignore the cross-Activity recovery logic in onStart for capture intents.
         */
        if (mRequestPermissionsFinished && mHelpTipsManager != null) {
            if (mHelpTipsManager != null) {
                mHelpTipsManager.pause();
            }
        }
        if (mModeStripView != null) {
            mModeStripView.pause();
        }
        dismissButtonGroupBar();
        int photoIndex = getResources().getInteger(R.integer.camera_mode_photo);
        int videoIndex = getResources().getInteger(R.integer.camera_mode_video);
        int videoCaptureIndex = getResources().getInteger(R.integer.camera_mode_video_capture);
        if (!isCaptureIntent()) {
            int modeIndexToSave = mCurrentModeIndex;
//            if(mCurrentModeIndex==videoIndex){
//                if(mCurrentModule!=null){
////                    closeModule(mCurrentModule);
////                    mCurrentModule=null;
//                    Log.w(TAG,"pause video recording");
//                    onModeSelecting(true);
//                    mCameraAppUI.onModeSelected(getResources().getInteger(R.integer.camera_mode_photo));
//                    mCameraAppUI.onVideoRecordingStateChanged(false);
//                }
//                modeIndexToSave=photoIndex;
//            }
            mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                    Keys.KEY_STARTUP_MODULE_INDEX,
                    modeIndexToSave);

            // For STILL_IMAGE_CAMERA and Secure Camera only,
            // KEY_CAMERA_MODULE_LAST_USED makes no sense.
            if (mKeepSecureModule || mSecureFyuseModule) {
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_SECURE_MODULE_INDEX, modeIndexToSave);
                mKeepSecureModule = false;
            } else {
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_SECURE_MODULE_INDEX, photoIndex);
            }
        }

        mPaused = true;
        mPeekAnimationHandler = null;
        mPeekAnimationThread.quitSafely();
        mPeekAnimationThread = null;

        if (mCurrentModule != null) {
            mCurrentModule.pause();
        }
        mOrientationManager.pause();
        mPanoramaViewHelper.onPause();

        pauseLocationManager();

        mCameraAppUI.getTopMenus().unregisterOnSharedPreferenceChangeListener(); // MODIFIED by jianying.zhang, 2016-10-18,BUG-2715761
        mLocalImagesObserver.setForegroundChangeListener(null);
        mLocalVideosObserver.setForegroundChangeListener(null);
        mLocalImagesObserver.setActivityPaused(true);
        mLocalVideosObserver.setActivityPaused(true);

        resetScreenOn();

        mMotionManager.stop();

        if (mBatteryWarningDialog != null && mBatteryWarningDialog.isShowing()) {
            mCameraAppUI.setViewFinderLayoutVisibile(false);
            mBatteryWarningDialog.dismiss();
            mBatteryWarningDialog = null;
        }
        if (mBatteryLowDialog != null && mBatteryLowDialog.isShowing()) {
            mCameraAppUI.setViewFinderLayoutVisibile(false);
            mBatteryLowDialog.dismiss();
            mBatteryLowDialog = null;
        }
        if (mStorageLowDialog != null && mStorageLowDialog.isShowing()) {
            mCameraAppUI.setViewFinderLayoutVisibile(false);
            mStorageLowDialog.dismiss();
            mStorageLowDialog = null;
        }

        if (mBatteryChangedReceiver != null) {
            unregisterReceiver(mBatteryChangedReceiver);
            mBatteryChangedReceiver = null;
        }
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-06-18,BUG-2202739*/
        if (mBatteryFlashLock != null) {
            getCameraAppUI().getTopMenus()
                    .enableButtonWithToken(TopMenus.BUTTON_FLASH, mBatteryFlashLock);
            mBatteryFlashLock = null;
        }
        /* MODIFIED-END by yuanxing.tan,BUG-2202739*/
        UsageStatistics.instance().backgrounded();

        // Camera is in fatal state. A fatal dialog is presented to users, but users just hit home
        // button. Let's just kill the process.
        if (mCameraFatalError && !isFinishing()) {
            Log.v(TAG, "onPause when camera is in fatal state, call Activity.finish()");
            finish();
        } else {
            // Close the camera and wait for the operation done.
            mCameraController.closeCamera(true);
        }
        restoreReversible();
        SnackbarToast.getSnackbarToast().cancle(); // MODIFIED by bin-liu3, 2016-11-09,BUG-3253898
    }

    @Override
    public void onResumeTasks() {
        CameraPerformanceTracker.onEvent(CameraPerformanceTracker.ACTIVITY_RESUME);
        Log.v(TAG, "Build info: " + Build.DISPLAY);
        CustomUtil.getInstance(getApplicationContext()).setCustomFromSystem();
        Keys.setToDefaults(mSettingsManager, mAppContext);
        mPaused = false;
        if (mHelpTipsManager != null && mRequestPermissionsFinished) {
            mHelpTipsManager.calcCameraUseTimes();
        }

        if (mModeStripView != null) {
            mModeStripView.resume();
        }
        closeReverisble();
        String savePath = getSettingsManager().
                getString(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_CAMERA_SAVEPATH,
                        getResources().getString(R.string.pref_camera_savepath_default));
        String oriPath = Storage.getSavePath();
        Storage.setSavePath(savePath);
        // If save path changed, mediadata will be updated later.
        boolean isSavePathChanged = !oriPath.equals(Storage.getSavePath());
        Log.i(TAG, "current save path is " + Storage.getSavePath());
        updateStorageSpaceAndHint(null);

        mLastLayoutOrientation = getResources().getConfiguration().orientation;

        // TODO: Handle this in OrientationManager.
        // Auto-rotate off
//        if (Settings.System.getInt(getContentResolver(),
//                Settings.System.ACCELEROMETER_ROTATION, 0) == 0) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
//            mAutoRotateScreen = false;
//        } else {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
//            mAutoRotateScreen = true;
//        }

        // Foreground event logging.  ACTION_STILL_IMAGE_CAMERA and
        // INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE are double logged due to
        // lockscreen onResume->onPause->onResume sequence.
        int source;
        String action = getIntent().getAction();
        if (action == null) {
            source = ForegroundSource.UNKNOWN_SOURCE;
        } else {
            switch (action) {
                case MST_SCAN_BUSINESSCARD_ACTION:
                case MediaStore.ACTION_IMAGE_CAPTURE:
                    source = ForegroundSource.ACTION_IMAGE_CAPTURE;
                    break;
                case MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA:
                    // was UNKNOWN_SOURCE in Fishlake.
                    source = ForegroundSource.ACTION_STILL_IMAGE_CAMERA;
                    break;
                case MediaStore.INTENT_ACTION_VIDEO_CAMERA:
                    // was UNKNOWN_SOURCE in Fishlake.
                    source = ForegroundSource.ACTION_VIDEO_CAMERA;
                    break;
                case MediaStore.ACTION_VIDEO_CAPTURE:
                    source = ForegroundSource.ACTION_VIDEO_CAPTURE;
                    break;
                case MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE:
                    // was ACTION_IMAGE_CAPTURE_SECURE in Fishlake.
                    source = ForegroundSource.ACTION_STILL_IMAGE_CAMERA_SECURE;
                    break;
                case MediaStore.ACTION_IMAGE_CAPTURE_SECURE:
                    source = ForegroundSource.ACTION_IMAGE_CAPTURE_SECURE;
                    break;
                case Intent.ACTION_MAIN:
                    source = ForegroundSource.ACTION_MAIN;
                    break;
                default:
                    source = ForegroundSource.UNKNOWN_SOURCE;
                    break;
            }
        }
        UsageStatistics.instance().foregrounded(source, currentUserInterfaceMode());

        mOrientationManager.resume();
        mPeekAnimationThread = new HandlerThread("Peek animation");
        mPeekAnimationThread.start();
        mPeekAnimationHandler = new PeekAnimationHandler(mPeekAnimationThread.getLooper(),
                mMainHandler, (MainActivityLayout) findViewById(R.id.activity_root_view));
        if (!mModeSelectingOnStart) {
            mCurrentModule.hardResetSettings(mSettingsManager);
            mCurrentModule.resume();
        } else if (mModuleOpenBeforeResume) {
            mModuleOpenBeforeResume = false;
            mCurrentModule.resume();
            UsageStatistics.instance().changeScreen(currentUserInterfaceMode(),
                    NavigationChange.InteractionCause.BUTTON);
        }
        UsageStatistics.instance().changeScreen(currentUserInterfaceMode(),
                NavigationChange.InteractionCause.BUTTON);
        setSwipingEnabled(true);

        // Default is showing the preview, unless disabled by explicitly
        // starting an activity we want to return from to the filmstrip rather
        // than the preview.
        mResetToPreviewOnResume = true;

        if (mThumbUpdateRunnable != null) {
            mThumbUpdateRunnable.run();
            mThumbUpdateRunnable = null;
        }

        if (needUpdatesForInstanceCapture() || mLocalVideosObserver.isMediaDataChangedDuringPause()
                || mLocalImagesObserver.isMediaDataChangedDuringPause()
                || isSavePathChanged) {
            if (!mSecureCamera && PermissionUtil.isStoragePermissionGranted(this)) {
                onLastMediaDataUpdated();
            } else if (mSecureCamera && PermissionUtil.isStoragePermissionGranted(this)) {
                // Secure Camera, update peek thumb only.
                onLastMediaDataUpdated();
            }
        }
        mLocalImagesObserver.setActivityPaused(false);
        mLocalVideosObserver.setActivityPaused(false);
//        if (!mSecureCamera) {
        mLocalImagesObserver.setForegroundChangeListener(
                new LocalMediaObserver.ChangeListener() {
                    @Override
                    public void onChange() {
//                    mDataAdapter.requestLoadNewPhotos();

//                        onLastMediaDataUpdated();
//                        mDataAdapter.requestLoad(new Callback<Void>() {
//                            @Override
//                            public void onCallback(Void result) {
//
//                            }
//                        });
                    }
                });

        mLocalVideosObserver.setForegroundChangeListener(
                new LocalMediaObserver.ChangeListener() {
                    @Override
                    public void onChange() {
//                        onLastMediaDataUpdated();
                    }
                }
        );
//        }

        keepScreenOnForAWhile();

        // Lights-out mode at all times.
        final View rootView = findViewById(R.id.activity_root_view);

        mLightsOutRunnable.run();
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
                new OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        mMainHandler.removeCallbacks(mLightsOutRunnable);
                        if (getWindow().getDecorView().getSystemUiVisibility() != getCameraAppUI().HIDE_NAVIGATION_VIEW) {
                            mMainHandler.postDelayed(mLightsOutRunnable, LIGHTS_OUT_DELAY_MS);
                        }
                    }
                });

        mPanoramaViewHelper.onResume();
        ReleaseHelper.showReleaseInfoDialogOnStart(this, mSettingsManager);

        /* MODIFIED-BEGIN by xuan.zhou, 2016-07-11,BUG-2481700*/
        if (PermissionUtil.isNoncriticalPermissionGranted(this)) {
            syncLocationManagerSetting();
        }
        /* MODIFIED-END by xuan.zhou,BUG-2481700*/

        mMotionManager.start();

        if (mBatteryChangedReceiver == null) {
            IntentFilter filter_battery_changed = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            mBatteryChangedReceiver = new BatteryBroadcastReceiver();
            registerReceiver(mBatteryChangedReceiver, filter_battery_changed);
        }
        getButtonManager().registerOnSharedPreferenceChangeListener();
        mCameraAppUI.getTopMenus().registerOnSharedPreferenceChangeListener(); // MODIFIED by jianying.zhang, 2016-10-18,BUG-2715761
        /* MODIFIED-BEGIN by nie.lei, 2016-03-21, BUG-1845068 */
        if (!CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_LOW_BATTERY_FEATURE_INDEPENDENT, false)) {
            mBatterySaveOn = CameraUtil.isBatterySaverEnabled(CameraActivity.this);
        }
        /* MODIFIED-END by nie.lei,BUG-1845068 */

        /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-12,BUG-1892480*/
        if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
            if (mCallback != null) {
                mCallback.onResetVisibility(true);
            }
        }
        /*MODIFIED-END by shunyin.zhang,BUG-1892480*/
    }

    private boolean needUpdatesForInstanceCapture() {
        Intent intent = getIntent();
        String action = intent.getAction();
        if (!INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(action)) {
            return false;
        }
        ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra("uris");
        if (uris == null || uris.isEmpty()) {
            return false;
        } else {
            mSecureUris = getSecureUris();
            mSecureUris.addAll(uris);
            return true;
        }
    }

    public void onLastMediaDataUpdated() { //MODIFIED by wenhua.tu, 2016-04-09,BUG-1911880
        Log.w(TAG, "last mediaData updated", new Throwable());
        new AsyncTask<Void, Void, Thumbnail>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Log.w(TAG, "update media,onPreExecute");
            }

            @Override
            protected Thumbnail doInBackground(Void[] params) {
                Thumbnail[] thumb = new Thumbnail[1];
                Thumbnail.getLastThumbnailFromContentResolver(CameraActivity.this.getContentResolver(), thumb);
                Log.w(TAG, "update media,doInBackground:" + thumb[0]);
                return thumb[0];
            }

            @Override
            protected void onPostExecute(final Thumbnail o) {
                super.onPostExecute(o);
                Log.w(TAG, "need to update Thumb");
                if (o == null || o.getBitmap() == null) {
                    mCameraAppUI.updatePeekThumbContent(null);
                    return;
                }

                if (isSecureCamera() && (mSecureUris == null ||
                        (mSecureUris != null && !mSecureUris.contains(o.getUri())))) {
                    // The last media isn't captured in secure camera, ignore it and set null thumb.
                    mCameraAppUI.updatePeekThumbContent(null);
                    return;
                }

                Log.w(TAG, "latest thumbnail: " + o.getUri());
                mCameraAppUI.updatePeekThumbContent(o);
                Log.w(TAG, "peekThumbUpdated");
//                mCaptureResultState.setThumbnail(o);
//                mCaptureResultState.setProgress(CaptureResultState.CaptureProgress.THUMB_DONE);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    private boolean mModeSelectingOnStart = false;

    @Override
    public void onStartTasks() {
        gIsCameraActivityRunning = true;
        mPanoramaViewHelper.onStart();

        /*
         * If we're starting after launching a different Activity (lockscreen),
         * we need to use the last mode used in the other Activity, and
         * not the old one from this Activity.
         *
         * This needs to happen before CameraAppUI.resume() in order to set the
         * mode cover icon to the actual last mode used.
         *
         * Right now we exclude capture intents from this logic.
         */
        int modeIndex = getModeIndex();
        if (!isCaptureIntent() && mCurrentModeIndex != modeIndex) {
            mModeSelectingOnStart = true;
            switchToMode(modeIndex);
        }

        if (mResetToPreviewOnResume) {
            mCameraAppUI.resume();
            mResetToPreviewOnResume = false;
        }
    }

    // It should be called in onActivityResult before onStart,
    // also set mModeSelectingOnStart true.
    public void updateModeForFyusion(int index) {
        mModeSelectingOnStart = true;
        switchToMode(index);
    }

    @Override
    protected void onStopTasks() {
        gIsCameraActivityRunning = false;
        mPanoramaViewHelper.onStop();

        mLocationManager.disconnect();
    }

    @Override
    public void onDestroyTasks() {
        Log.w(TAG, "Destroy task");

        /* MODIFIED-BEGIN by xuan.zhou, 2016-07-11,BUG-2481700*/
        if (mLocationPrompt != null) {
            mLocationPrompt.dismiss();
            mLocationPrompt = null;
        }
        /* MODIFIED-END by xuan.zhou,BUG-2481700*/

        if (mCurrentModule != null) {
            mCurrentModule.destroy();
        }
        if (mRequestPermissionsFinished && mHelpTipsManager != null) {
            mHelpTipsManager.destroy();
            mHelpTipsManager = null;
        }

        if (mSecureCamera) {
            unregisterReceiver(mShutdownReceiver);
        }

        unregisterReceiver(mPowerSaveModeReceiver);

        if (mBatteryChangedReceiver != null) {
            unregisterReceiver(mBatteryChangedReceiver);
            mBatteryChangedReceiver = null;
        }
        unregisterReceiver(mMediaActionReceiver);
        unregisterReceiver(mRestartPhoneReceiver); // MODIFIED by nie.lei, 2016-05-05,BUG-2073514
        clearSecureUris();
        mSettingsManager.removeAllListeners();
        mCameraController.removeCallbackReceiver();
        mCameraController.setCameraExceptionHandler(null);
        getContentResolver().unregisterContentObserver(mLocalImagesObserver);
        getContentResolver().unregisterContentObserver(mLocalVideosObserver);
        mCameraAppUI.onDestroy();

        //Simply remove the writer to null to avoid the crash in rare case , under this case , it may cause object instance leak in a short term , which is much better than crash
        //The resource would get released automatically if activity instance no longer available
//        mCameraController = null;
//        mSettingsManager = null;
//        mOrientationManager = null;
//        mButtonManager = null;
        mSoundPlayer.release();
        if (mSoundClipsPlayer != null)
            mSoundClipsPlayer.release();
        CameraAgentFactory.recycle(CameraAgentFactory.CameraApi.API_1);
        CameraAgentFactory.recycle(GservicesHelper.useCamera2ApiThroughPortabilityLayer(mAppContext) ?
                CameraAgentFactory.CameraApi.AUTO :
                CameraAgentFactory.CameraApi.API_1);
        BlurUtil.destroy();
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        Log.v(TAG, "onConfigurationChanged");
        if (config.orientation == Configuration.ORIENTATION_UNDEFINED) {
            return;
        }

        if (mLastLayoutOrientation != config.orientation) {
            mLastLayoutOrientation = config.orientation;
            mCurrentModule.onLayoutOrientationChanged(
                    mLastLayoutOrientation == Configuration.ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //if help tip exsits ,consume all events except KEYCODE_BACK && BOOM_KEY
        if (CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_HELP_TIP_TUTORIAL, false)) {
            if (mRequestPermissionsFinished && mHelpTipsManager != null && mHelpTipsManager.isHelpTipShowExist()) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return super.onKeyDown(keyCode, event);
                } else if (keyCode == CameraUtil.BOOM_KEY) {
                    if (!mHelpTipsManager.isNeedBoomKeyResponse()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }

        //if DEF_CAMERA_MODULE_BOOMKEY_RESPONSE is false, module does not response boom key.
        if (!CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_MODULE_BOOMKEY_RESPONSE, false)) {
            if (keyCode == CameraUtil.BOOM_KEY) {
                return super.onKeyDown(keyCode, event);
            }
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (!CustomUtil.getInstance().getBoolean(
                    CustomFields.DEF_CAMERA_VOLUME_KEY_RESPONSE, true)) {
                if (CustomUtil.getInstance().getBoolean(
                        CustomFields.DEF_CAMERA_VOLUME_KEY_FOLLOW_SYS, false)) {
                    return super.onKeyDown(keyCode, event);
                } else {
                    return true;
                }
            }
        }

        if (keyCode == CameraUtil.BOOM_KEY) {
            Log.e(TAG, "onKeyDown begin to handle boom key events");
            if (!CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_VDF_BOOMKEY_CUSTOMIZE, false)) {
                //idol4 & idol4s
                int iCameraKeySetting = Settings.System.getInt(getContentResolver(), CameraUtil.CAMERA_KEY_SETTINGS, CameraUtil.CAMERA_KEY_ON);
                int iBoomEffectSetting = Settings.System.getInt(getContentResolver(), CameraUtil.BOOM_EFFECT_SETTINGS, CameraUtil.BOOM_EFFECT_OFF);
                Log.e(TAG, "onKeyDown iCameraKeySetting = " + iCameraKeySetting + ",iBoomEffectSetting = " + iBoomEffectSetting);
                if (iBoomEffectSetting != CameraUtil.BOOM_EFFECT_ON && iCameraKeySetting != CameraUtil.CAMERA_KEY_ON) {
                    return true;
                }
            } else {
                //vdf
                int iCameraKeySetting = Settings.System.getInt(getContentResolver(), CameraUtil.CAMERA_KEY_SETTINGS, CameraUtil.CAMERA_KEY_ON);
                Log.e(TAG, "vdf BoomKey customize onKeyDown iCameraKeySetting = " + iCameraKeySetting);
                if (iCameraKeySetting != CameraUtil.CAMERA_KEY_ON) {
                    return true;
                }
            }
        }

        if (mCameraAppUI.isCameraSettingVisible()) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return super.onKeyDown(keyCode, event);
            }
            return true;
        }

        if (mCurrentModule != null && mCurrentModule.onKeyDown(keyCode, event)) {
            return true;
        }
        // Prevent software keyboard or voice search from showing up.
        if (keyCode == KeyEvent.KEYCODE_SEARCH
                || keyCode == KeyEvent.KEYCODE_MENU) {
            if (event.isLongPress()) {
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //if help tip exsits ,consume all events except KEYCODE_BACK && BOOM_KEY
        if (CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_HELP_TIP_TUTORIAL, false)) {
            if (mRequestPermissionsFinished && mHelpTipsManager != null && mHelpTipsManager.isHelpTipShowExist()) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return super.onKeyUp(keyCode, event);
                } else if (keyCode == CameraUtil.BOOM_KEY) {
                    if (!mHelpTipsManager.isNeedBoomKeyResponse()) {
                        return true;
                    }
                } else {
                    return true;
                }
            }
        }

        //if DEF_CAMERA_MODULE_BOOMKEY_RESPONSE is false, module does not response boom key.
        if (!CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_MODULE_BOOMKEY_RESPONSE, false)) {
            if (keyCode == CameraUtil.BOOM_KEY) {
                return super.onKeyUp(keyCode, event);
            }
        }

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (!CustomUtil.getInstance().getBoolean(
                    CustomFields.DEF_CAMERA_VOLUME_KEY_RESPONSE, true)) {
                if (CustomUtil.getInstance().getBoolean(
                        CustomFields.DEF_CAMERA_VOLUME_KEY_FOLLOW_SYS, false)) {
                    return super.onKeyUp(keyCode, event);
                } else {
                    return true;
                }
            }
        }

        if (keyCode == CameraUtil.BOOM_KEY) {
            Log.e(TAG, "onKeyUp begin to handle boom key events");
            if (!CustomUtil.getInstance().getBoolean(
                    CustomFields.DEF_CAMERA_VDF_BOOMKEY_CUSTOMIZE, false)) {
                //idol4 & idol4s
                int iCameraKeySetting = Settings.System.getInt(getContentResolver(), CameraUtil.CAMERA_KEY_SETTINGS, CameraUtil.CAMERA_KEY_ON);
                int iBoomEffectSetting = Settings.System.getInt(getContentResolver(), CameraUtil.BOOM_EFFECT_SETTINGS, CameraUtil.BOOM_EFFECT_OFF);
                Log.e(TAG, "onKeyUp iCameraKeySetting = " + iCameraKeySetting + ",iBoomEffectSetting = " + iBoomEffectSetting);
                if (iBoomEffectSetting != CameraUtil.BOOM_EFFECT_ON && iCameraKeySetting != CameraUtil.CAMERA_KEY_ON) {
                    return true;
                }
            } else {
                //vdf
                int iCameraKeySetting = Settings.System.getInt(getContentResolver(), CameraUtil.CAMERA_KEY_SETTINGS, CameraUtil.CAMERA_KEY_ON);
                Log.e(TAG, "onKeyUp iCameraKeySetting = " + iCameraKeySetting);
                if (iCameraKeySetting != CameraUtil.CAMERA_KEY_ON) {
                    return true;
                }
            }
        }

        if (mCameraAppUI.isCameraSettingVisible()) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return super.onKeyUp(keyCode, event);
            }
            return true;
        }

        // If a module is in the middle of capture, it should
        // consume the key event.
        if (mCurrentModule != null && mCurrentModule.onKeyUp(keyCode, event)) {
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU
                || keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            // Let the mode list view consume the event.
//                mCameraAppUI.openModeList();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
//                mCameraAppUI.showFilmstrip();
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        Log.e(TAG, "onBackPressed E ");
        if (!mCameraAppUI.onBackPressed()) {
            if (!mCurrentModule.onBackPressed()) {
                super.onBackPressed();
            }
        }

        if (CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_HELP_TIP_TUTORIAL, false)) {
            if (mRequestPermissionsFinished && mHelpTipsManager != null && mHelpTipsManager.isHelpTipShowExist()) {
                mHelpTipsManager.pause();
                finish();
            }
        }
    }

    @Override
    public boolean isAutoRotateScreen() {
        // TODO: Move to OrientationManager.
        return mAutoRotateScreen;
    }

    protected long getStorageSpaceBytes() {
        synchronized (mStorageSpaceLock) {
            return mStorageSpaceBytes;
        }
    }

    protected interface OnStorageUpdateDoneListener {
        public void onStorageUpdateDone(long bytes);
    }

    protected void updateStorageSpaceAndHint(final OnStorageUpdateDoneListener callback) {
        /*
         * We execute disk operations on a background thread in order to
         * free up the UI thread.  Synchronizing on the lock below ensures
         * that when getStorageSpaceBytes is called, the main thread waits
         * until this method has completed.
         *
         * However, .execute() does not ensure this execution block will be
         * run right away (.execute() schedules this AsyncTask for sometime
         * in the future. executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
         * tries to execute the task in parellel with other AsyncTasks, but
         * there's still no guarantee).
         * e.g. don't call this then immediately call getStorageSpaceBytes().
         * Instead, pass in an OnStorageUpdateDoneListener.
         */
        (new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... arg) {
                Long availableStorageBytes = Storage.getAvailableSpace();
                synchronized (mStorageSpaceLock) {
                    mStorageSpaceBytes = availableStorageBytes;
                    return mStorageSpaceBytes;
                }
            }

            @Override
            protected void onPostExecute(Long bytes) {
                updateStorageHint(bytes);
                // This callback returns after I/O to check disk, so we could be
                // pausing and shutting down. If so, don't bother invoking.
                if (callback != null && !mPaused) {
                    callback.onStorageUpdateDone(bytes);
                } else {
                    Log.v(TAG, "ignoring storage callback after activity pause");
                }
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected void updateStorageHint(long storageSpace) {
        if (!gIsCameraActivityRunning) {
            return;
        }

        if (!PermissionUtil.isStoragePermissionGranted(this)) {
            return;
        }

        String message = null;
        String mAlertDialogMessage = null;
        /* MODIFIED-BEGIN by nie.lei, 2016-05-06,BUG-2104974*/
        boolean bStorageLowCustomFlag = false;
        int changeButtonID = 0;
        /* MODIFIED-END by nie.lei,BUG-2104974*/
        if (storageSpace == Storage.UNAVAILABLE) {
            message = getString(R.string.no_storage);
        } else if (storageSpace == Storage.PREPARING) {
            message = getString(R.string.preparing_sd);
        } else if (storageSpace == Storage.UNKNOWN_SIZE) {
            message = getString(R.string.access_sd_fail);
        } else if (storageSpace <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
            if (Storage.getSavePath().equals(Storage.PHONE_STORAGE)) {
                /* MODIFIED-BEGIN by nie.lei, 2016-05-06,BUG-2104974*/
                if (Storage.isSDCardAvailable()) {
                    boolean storageLow = Storage.getSpecialAvailableSpace(Storage.SDCARD_STORAGE) <= Storage.LOW_STORAGE_THRESHOLD_BYTES;
                    if (storageLow) {
                        mAlertDialogMessage = getString(R.string.storage_low_phone_sdcard_full_alert_dialog);
                    } else {
                        mAlertDialogMessage = getString(R.string.storage_low_phone_full_sdcard_ok_alert_dialog);
                        bStorageLowCustomFlag = true;
                        changeButtonID = R.string.storage_low_phone_full_sdcard_ok_button;
                    }
                } else {
                    /* MODIFIED-BEGIN by fei.hui, 2016-09-09,BUG-2868515*/
                    /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
                    SnackbarToast.getSnackbarToast().showToast(mAppContext,
                            mAppContext.getString(R.string.storage_low_storage_critical_toast_message)
                            ,SnackbarToast.LENGTH_LONG,SnackbarToast.DEFAULT_Y_OFFSET);
                            /* MODIFIED-END by bin-liu3,BUG-3253898*/
                    //mAlertDialogMessage = getString(R.string.storage_low_phone_full_no_sdcard_alert_dialog);
                    /* MODIFIED-END by fei.hui,BUG-2868515*/
                }

            } else if (Storage.getSavePath().equals(Storage.SDCARD_STORAGE)) {
                boolean storageLow = Storage.getSpecialAvailableSpace(Storage.PHONE_STORAGE) <= Storage.LOW_STORAGE_THRESHOLD_BYTES;
                if (storageLow) {
                    mAlertDialogMessage = getString(R.string.storage_low_phone_sdcard_full_alert_dialog);
                } else {
                    mAlertDialogMessage = getString(R.string.storage_low_storage_full_phone_ok_alert_dialog);
                    bStorageLowCustomFlag = true;
                    changeButtonID = R.string.storage_low_storage_full_phone_ok_button;
                }
            }

            Log.e(TAG, "Storage.getSavePath() = " + Storage.getSavePath() + ", bStorageLowCustomFlag = " + bStorageLowCustomFlag
                    + ",Storage.isSDCardAvailable() = " + Storage.isSDCardAvailable());
                    /* MODIFIED-END by nie.lei,BUG-2104974*/
        }

        if (message != null) {
            Log.w(TAG, "Storage warning: " + message);
            if (mStorageHint == null) {
                mStorageHint = OnScreenHint.makeText(CameraActivity.this, message);
            } else {
                mStorageHint.setText(message);
            }
            mStorageHint.show();
            UsageStatistics.instance().storageWarning(storageSpace);

            // Disable all user interactions,
            mCameraAppUI.setDisableAllUserInteractions(true);
        } else if (mStorageHint != null) {
            mStorageHint.cancel();
            mStorageHint = null;

            // Re-enable all user interactions.
            mCameraAppUI.setDisableAllUserInteractions(false);
        }

        if (mAlertDialogMessage != null) {
            Log.w(TAG, "Storage warning mAlertDialogMessage");
            if (mStorageLowDialog != null && mStorageLowDialog.isShowing()) {
                mCameraAppUI.setViewFinderLayoutVisibile(false);
                mStorageLowDialog.dismiss();
                mStorageLowDialog = null;
            }
            if (mStorageLowDialog == null || !mStorageLowDialog.isShowing()) {
                /* MODIFIED-BEGIN by nie.lei, 2016-05-06,BUG-2104974*/
                mCameraAppUI.setViewFinderLayoutVisibile(!bStorageLowCustomFlag);
                mStorageLowDialog = CameraUtil.showStorageLowAndFinish(CameraActivity.this
                        , mAlertDialogMessage, bStorageLowCustomFlag, changeButtonID, storageLowCallback); // MODIFIED by nie.lei, 2016-05-26,BUG-2208223
                        /* MODIFIED-END by nie.lei,BUG-2104974*/
            }
        } else {
            if (mStorageLowDialog != null && mStorageLowDialog.isShowing()) {
                mCameraAppUI.setViewFinderLayoutVisibile(false);
                mStorageLowDialog.dismiss();
                mStorageLowDialog = null;
            }
        }
    }

    /* MODIFIED-BEGIN by nie.lei, 2016-05-26,BUG-2208223*/
    Runnable storageLowCallback = new Runnable() {
        @Override
        public void run() {
            synchronized (mStorageSpaceLock) {
                mStorageSpaceBytes = Storage.getAvailableSpace();
            }

            if (!isSecureCamera() && !isCaptureIntent() &&
                    PermissionUtil.isStoragePermissionGranted(CameraActivity.this)) {
                onLastMediaDataUpdated();
            }

            //update pano storage path
            if (mCurrentModule != null) {
                mCurrentModule.onStoragePathChanged();
            }
        }
    };
    /* MODIFIED-END by nie.lei,BUG-2208223*/

    protected void setResultEx(int resultCode) {
        mResultCodeForTesting = resultCode;
        setResult(resultCode);
    }

    protected void setResultEx(int resultCode, Intent data) {
        mResultCodeForTesting = resultCode;
        mResultDataForTesting = data;
        setResult(resultCode, data);
    }

    public int getResultCode() {
        return mResultCodeForTesting;
    }

    public Intent getResultData() {
        return mResultDataForTesting;
    }

    public boolean isSecureCamera() {
        return mSecureCamera;
    }

    @Override
    public boolean isPaused() {
        return mPaused;
    }

    @Override
    public int getPreferredChildModeIndex(int modeIndex) {
        if (modeIndex == getResources().getInteger(R.integer.camera_mode_photo)) {
            boolean hdrPlusOn = Keys.isHdrPlusOn(mSettingsManager);
            if (hdrPlusOn && GcamHelper.hasGcamAsSeparateModule()) {
                modeIndex = getResources().getInteger(R.integer.camera_mode_gcam);
            }
        }
        return modeIndex;
    }

    @Override
    public void onModeSelecting() {
        onModeSelecting(false);
    }

    @Override
    public void onModeSelecting(boolean disableAnimation) {
        onModeSelecting(disableAnimation, null);
    }

    @Override
    public void onModeSelecting(boolean disableAnimation, ModeTransitionView.OnTransAnimationListener listener) {
        if (!disableAnimation) {
            freezeScreenUntilPreviewReady(listener);
        } else if (listener == null) {
            freezeScreenUntilWithoutBlur();
        } else {
            freezeScreenWithoutBlurUntilAnimationDone(listener);
        }
        if (mCurrentModule != null) {
            mCurrentModule.preparePause();
        }

        /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-19,BUG-1892480*/
        if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
            if (mCallback != null) {
                mCallback.onVisibilityChange(true, false);
            }
        }
        /*MODIFIED-END by shunyin.zhang,BUG-1892480*/

    }

    @Override
    public void onModeSelected(final int modeIndex) {
        CameraPerformanceTracker.onEvent(CameraPerformanceTracker.MODE_SWITCH_START);
        // Record last used camera mode for quick switching
        if (modeIndex == getResources().getInteger(R.integer.camera_mode_photo)
                || modeIndex == getResources().getInteger(R.integer.camera_mode_gcam)) {
            mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                    Keys.KEY_CAMERA_MODULE_LAST_USED,
                    modeIndex);
        }
        /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-12,BUG-1892480*/
        if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
            if (mCallback != null) {
                mCallback.onVisibilityChange(false, modeIndex == getResources().getInteger(R.integer.camera_mode_photo)); //MODIFIED by shunyin.zhang, 2016-04-19,BUG-1892480
            }
        }
        /*MODIFIED-END by shunyin.zhang,BUG-1892480*/

        mModeChangeRunnable.setTargetIndex(modeIndex);
        if (CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_HELP_TIP_TUTORIAL, false)
                && (mHelpTipsManager != null)) {
            mHelpTipsManager.notifyModeChanged(modeIndex, mModeChangeRunnable);
        } else {
            mModeChangeRunnable.run();
        }
    }

    private abstract class ModeChangeRunnable implements Runnable {
        protected int mTargetIndex;

        public void setTargetIndex(int index) {
            mTargetIndex = index;
        }
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
    /**
     * get camera scope by cameraID;
     * @param cameraId
     * @return
     */
    @Override
    public String getCameraScopeByID(String cameraId) {
        return CAMERA_SCOPE_PREFIX + cameraId;
    }

    boolean mIndenticalCameraQuiteFilter = false;

    /**
     * filter nine-rectangle-grid interface chose NONE Filter,
     * set mIndenticalCameraQuiteFilter is true
     * @param indenticalCameraQuiteFilter
     */
    @Override
    public void setIndenticalCameraQuiteFilter(boolean indenticalCameraQuiteFilter) {
        mIndenticalCameraQuiteFilter = indenticalCameraQuiteFilter;
    }
    /* MODIFIED-END by jianying.zhang,BUG-3255060*/

    private final ModeChangeRunnable mModeChangeRunnable = new ModeChangeRunnable() {
        int updateModeIndex;
        @Override
        public void run() {
            if (mCurrentModule == null) {//may be released in pause
                return;
            }
            Log.w(TAG, "close module " + mCurrentModule);
            /* MODIFIED-BEGIN by xuan.zhou, 2016-05-23,BUG-2167404*/
            // Lock zoom before pause.
//            mCameraAppUI.lockZoom();
            /* MODIFIED-END by xuan.zhou,BUG-2167404*/
            dismissButtonGroupBar();
            /* MODIFIED-BEGIN by jianying.zhang, 2016-10-25,BUG-3224009*/
            if (mCameraAppUI != null) {
                mCameraAppUI.setSwipeEnabled(true);
            }

            /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
            /** when is not filter nine-rectangle-grid interface chose NONE
             * Filter (mIndenticalCameraQuiteFilter = false) and
             * mCurrentModeIndex == filterModeId/videoFilterModeId
             * && mTargetIndex == photoModeId/videoModeId,
             * judgment is photoModule/VideoModule switch camera,
             * so set isIndenticalModuleSwitchCamera is true; */
            boolean isIndenticalModuleSwitchCamera = false;
            Log.d(TAG,"mCurrentModeIndex : " + mCurrentModeIndex
                    + " mTargetIndex : " + mTargetIndex
                    + " mIndenticalCameraQuiteFilter : " + mIndenticalCameraQuiteFilter);
            int photoModeId = getResources().getInteger(R.integer.camera_mode_photo);
            int videoModeId = getResources().getInteger(R.integer.camera_mode_video);
            int filterModeId = getResources().getInteger(R.integer.camera_mode_filter);
            int videoFilterModeId = getResources().getInteger(R.integer.camera_mode_videofilter);
            if (!mIndenticalCameraQuiteFilter && ((mCurrentModeIndex == filterModeId && mTargetIndex == photoModeId)
                    || (mCurrentModeIndex == videoFilterModeId && mTargetIndex == videoModeId))) {
                isIndenticalModuleSwitchCamera = true;
            }
            mIndenticalCameraQuiteFilter = false;

            /* MODIFIED-END by jianying.zhang,BUG-3224009*/
            closeModule(mCurrentModule);
            // Select the correct module index from the mode switcher index.
            updateModeIndex = getPreferredChildModeIndex(mTargetIndex);
            if (!validateFilterSelected(updateModeIndex, isIndenticalModuleSwitchCamera)) {
                setModuleFromModeIndex(updateModeIndex);
                mCameraAppUI.resetBottomControls(mCurrentModule, updateModeIndex);
                mCameraAppUI.addShutterListener(mCurrentModule);
                Log.w(TAG, "openModule");
                openModule(mCurrentModule);
                mCurrentModule.onOrientationChanged(mLastRawOrientation);
                Log.w(TAG, "open module " + mCurrentModule);
                // Store the module index so we can use it the next time the Camera
                // starts up.
                if(updateModeIndex == videoModeId
                        || updateModeIndex == filterModeId
                        || updateModeIndex == videoFilterModeId){
                    updateModeIndex=photoModeId;
                }
                mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_STARTUP_MODULE_INDEX, updateModeIndex);
            }
        }
    };

    @Override
    public boolean validateFilterSelected(int updateModeIndex,
                                           boolean isIndenticalModuleSwitchCamera) {
        if (mSettingsManager == null) {
            return false;
        }
        int photoModeId = getResources().getInteger(R.integer.camera_mode_photo);
        int videoModeId = getResources().getInteger(R.integer.camera_mode_video);
        int filterModeId = getResources().getInteger(R.integer.camera_mode_filter);
        int videoFilterModeId = getResources().getInteger(R.integer.camera_mode_videofilter);
        String scope = getCameraScope();
        if (isIndenticalModuleSwitchCamera) {
            int backCameraId = Integer.parseInt(getResources()
                    .getString(R.string.pref_camera_id_index_back));
            int frontCameraId = Integer.parseInt(getResources()
                    .getString(R.string.pref_camera_id_index_front));
            int targetCameraId = getCurrentCameraId() == backCameraId ? frontCameraId : backCameraId;
            Log.d(TAG,"updateModeIndex : " + updateModeIndex
                    + " getCurrentCameraId() : " + getCurrentCameraId()
                    + " targetCameraId : " + targetCameraId);
            scope = getCameraScopeByID(Integer.toString(targetCameraId));
        }
        if (updateModeIndex == photoModeId) {
            int choseFilterIndex = mSettingsManager.getChosenFilterIndex(scope,
                    Keys.KEY_FILTER_MODULE_SELECTED, CameraAgent.INDEX_NONE_FILTER);
            Log.d(TAG,"photoModeId mModeChangeRunnable  choseFilterIndex : " + choseFilterIndex);
            if (choseFilterIndex != CameraAgent.INDEX_NONE_FILTER) {
                mCameraAppUI.onFilterModuleSelected(filterModeId);
                return true;
            }
        } else if (updateModeIndex == videoModeId) {
            int choseFilterIndex =  mSettingsManager.getChosenFilterIndex(scope,
                    Keys.KEY_VIDEO_FILTER_MODULE_SELECTED, CameraAgent.INDEX_NONE_FILTER);
            Log.d(TAG,"videoModeId mModeChangeRunnable choseFilterIndex : " + choseFilterIndex);
            if (choseFilterIndex != CameraAgent.INDEX_NONE_FILTER) {
                mCameraAppUI.onFilterModuleSelected(videoFilterModeId);
                return true;
            }
        }

        return false;
    }
    /* MODIFIED-END by jianying.zhang,BUG-3255060*/

    /**
     * Shows the settings dialog.
     */
    @Override
    public void onSettingsSelected() {
        UsageStatistics.instance().controlUsed(
                eventprotos.ControlEvent.ControlType.OVERALL_SETTINGS);
        mKeepSecureModule = true;
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-27,BUG-3212745*/
    @Override
    public void onZoomBarVisibilityChanged(boolean visible) {
        if (mCurrentModule == null) {
            return;
        }
        mCurrentModule.onZoomBarVisibilityChanged(visible);
    }
    /* MODIFIED-END by jianying.zhang,BUG-3212745*/

    private void dismissButtonGroupBar() {
        if (getCameraAppUI() == null
                || getCameraAppUI().getTopMenus() == null) {
            return;
        }
        if (getCameraAppUI().getTopMenus().buttonGroupBarVisible()) {
            getCameraAppUI().getTopMenus().dismissButtonGroupBar(false, ButtonGroup.OUT_LEFT);
        }
    }

    @Override
    public void onFlashClicked() {
        if (mCurrentModule == null) {
            return;
        }
        mCurrentModule.onFlashClicked();
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-25,BUG-3205846*/
    @Override
    public boolean isFilterModule() {
        int filterModeId = getResources().getInteger(R.integer.camera_mode_filter);
        int videofilterModeId = getResources().getInteger(R.integer.camera_mode_videofilter);
        if (mCurrentModeIndex == videofilterModeId
                || mCurrentModeIndex == filterModeId) {
            return true;
        }
        return false;
    }
    /* MODIFIED-END by jianying.zhang,BUG-3205846*/

    @Override
    public void onBackClicked() {
        onBackPressed();
    }

    @Override
    public void onPoseClicked() {
        Log.d(TAG, "onPoseClicked");
    }

    @Override
    public void onFilterClicked() {
        if (mCurrentModule == null) {
            return;
        }
        mCurrentModule.onFilterClicked();
    }

    @Override
    public void freezeScreenUntilPreviewReady() {
        mCameraAppUI.freezeScreenUntilPreviewReady();
    }

    @Override
    public void freezeScreenUntilPreviewReady(ModeTransitionView.OnTransAnimationListener listeners) {
        mCameraAppUI.freezeScreenUntilPreviewReady(true, listeners);
    }

    @Override
    public void freezeScreenUntilWithoutBlur() {
        mCameraAppUI.freezeScreenUntilPreviewReady(false);
    }

    @Override
    public void freezeScreenWithoutBlurUntilAnimationDone(ModeTransitionView.OnTransAnimationListener listeners) {
        mCameraAppUI.freezeScreenUntilPreviewReady(false, listeners);
    }

    /**
     * Sets the mCurrentModuleIndex, creates a new module instance for the given
     * index an sets it as mCurrentModule.
     */
    private void setModuleFromModeIndex(int modeIndex) {
        ModuleManagerImpl.ModuleAgent agent = mModuleManager.getModuleAgent(modeIndex);
        if (agent == null) {
            return;
        }
        if (!agent.requestAppForCamera()) {
            mCameraController.closeCamera(true);
        }
        mCurrentModeIndex = agent.getModuleId();
        Log.w(TAG, "update mode for " + mCurrentModeIndex);
        mCurrentModule = (CameraModule) agent.createModule(this);
    }

    @Override
    public SettingsManager getSettingsManager() {
        return mSettingsManager;
    }

    @Override
    public CameraServices getServices() {
        return (CameraServices) getApplication();
    }

    public List<String> getSupportedModeNames() {
        List<Integer> indices = mModuleManager.getSupportedModeIndexList();
        List<String> supported = new ArrayList<String>();

        for (Integer modeIndex : indices) {
            String name = CameraUtil.getCameraModeText(modeIndex, mAppContext);
            if (name != null && !name.equals("")) {
                supported.add(name);
            }
        }
        return supported;
    }

    @Override
    public ButtonManager getButtonManager() {
        if (mButtonManager == null) {
            mButtonManager = new ButtonManager(this);
        }
        return mButtonManager;
    }

    @Override
    public SoundPlayer getSoundPlayer() {
        return mSoundPlayer;
    }

    public SoundClips.Player getSoundClipPlayer() {
        if (mSoundClipsPlayer == null) {
            mSoundClipsPlayer = SoundClips.getPlayer(this);
        }
        return mSoundClipsPlayer;
    }

    /**
     * Creates an AlertDialog appropriate for choosing whether to enable
     * location on the first run of the app.
     */
    public AlertDialog getFirstTimeLocationAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder = SettingsUtil.getFirstTimeLocationAlertBuilder(builder, new Callback<Boolean>() {
            @Override
            public void onCallback(Boolean locationOn) {
                Keys.setLocation(mSettingsManager, locationOn, mLocationManager);
            }
        });
        if (builder != null) {
            return builder.create();
        } else {
            return null;
        }
    }

    /**
     * Launches an ACTION_EDIT intent for the given local data item. If
     * 'withTinyPlanet' is set, this will show a disambig dialog first to let
     * the user start either the tiny planet editor or another photo edior.
     *
     * @param data The data item to edit.
     */
    public void launchEditor(LocalData data) {
        Intent intent = new Intent(Intent.ACTION_EDIT)
                .setDataAndType(data.getUri(), data.getMimeType())
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            launchActivityByIntent(intent);
        } catch (ActivityNotFoundException e) {
            final String msgEditWith = getResources().getString(R.string.edit_with);
            launchActivityByIntent(Intent.createChooser(intent, msgEditWith));
        }
    }

    /**
     * Returns what UI mode (capture mode or filmstrip) we are in.
     * Returned number one of {@link com.google.common.logging.eventprotos.NavigationChange.Mode}
     */
    private int currentUserInterfaceMode() {
        int mode = NavigationChange.Mode.UNKNOWN_MODE;
        if (mCurrentModeIndex == getResources().getInteger(R.integer.camera_mode_photo)) {
            mode = NavigationChange.Mode.PHOTO_CAPTURE;
        }
        if (mCurrentModeIndex == getResources().getInteger(R.integer.camera_mode_video)) {
            mode = NavigationChange.Mode.VIDEO_CAPTURE;
        }
        if (mCurrentModeIndex == getResources().getInteger(R.integer.camera_mode_refocus)) {
            mode = NavigationChange.Mode.LENS_BLUR;
        }
        if (mCurrentModeIndex == getResources().getInteger(R.integer.camera_mode_gcam)) {
            mode = NavigationChange.Mode.HDR_PLUS;
        }
        if (mCurrentModeIndex == getResources().getInteger(R.integer.camera_mode_photosphere)) {
            mode = NavigationChange.Mode.PHOTO_SPHERE;
        }
        if (mCurrentModeIndex == getResources().getInteger(R.integer.camera_mode_panorama)) {
            mode = NavigationChange.Mode.PANORAMA;
        }
        return mode;
    }

    private boolean mModuleOpenBeforeResume = false;

    private void openModule(CameraModule module) {
        module.hardResetSettings(mSettingsManager);
        module.init(this, isSecureCamera(), isCaptureIntent());
        if (!mPaused) {
            module.resume();
            UsageStatistics.instance().changeScreen(currentUserInterfaceMode(),
                    NavigationChange.InteractionCause.BUTTON);
        } else if (mModeSelectingOnStart) {
            // Sometimes openModule is called before onResume when mode selecting in onStart,
            // thus mPaused is false and module.resume here will not be called.
            // Give another chance to do this when mModuleOpenBeforeResume is true.
            mModuleOpenBeforeResume = true;
        }
    }

    private void closeModule(CameraModule module) {
        if (!module.isPaused()) {
            module.pause();
        }
        mCameraAppUI.clearModuleUI();
    }

    @Override
    public boolean isReversibleWorking() {
        return getServices().isReversibleEnabled() && mCameraAppUI != null && mCameraAppUI.isScreenReversed();
    }

    @Override
    public void lockRotatableOrientation(int hashCode) {
        if (mListeningRotatableMap != null && mListeningRotatableMap.containsKey(hashCode)) {
            if (!mListeningRotatableMap.get(hashCode).isOrientationLocked()) {
                mListeningRotatableMap.get(hashCode).setOrientationLocked(true);
            }
        }
    }

    @Override
    public void unlockRotatableOrientation(int hashCode) {
        if (mListeningRotatableMap != null && mListeningRotatableMap.containsKey(hashCode)) {
            if (mListeningRotatableMap.get(hashCode).isOrientationLocked()) {
                mListeningRotatableMap.get(hashCode).setOrientationLocked(false);
            }
        }
    }

    private boolean mNeedRestoreReversible = false;

    private void closeReverisble() {
//        boolean isReversibleOn=DeviceInfo.isReversibleOn(getContentResolver());
//        if(isReversibleOn){
//            if(DeviceInfo.updateReversibleSetting(getContentResolver(),false)){
//                mNeedRestoreReversible=true;
//            }
//        }
    }

    private void restoreReversible() {
//        if(mNeedRestoreReversible){
//            DeviceInfo.updateReversibleSetting(getContentResolver(),true);
//            mNeedRestoreReversible = false;
//        }
    }


    @Override
    public void onOrientationChanged(int orientation) {
        boolean isOrientationChanged = false;
        if (orientation != mLastRawOrientation) {
            Log.w(TAG, "orientation changed (from:to) " + mLastRawOrientation +
                    ":" + orientation);
            isOrientationChanged = true;
        }

        // We keep the last known orientation. So if the user first orient
        // the camera then point the camera to floor or sky, we still have
        // the correct orientation.
        if (orientation == OrientationManager.ORIENTATION_UNKNOWN) {
            return;
        }
        mLastRawOrientation = orientation;
        if (mCurrentModule != null) {
            mCurrentModule.onOrientationChanged(orientation);
        }

        if (isReversibleWorking()) {
            orientation = (180 + orientation) % 360;
        }
        for (Rotatable.RotateEntity rotateEntity : mListeningRotatableMap.values()) {
            if (!rotateEntity.isOrientationLocked()) {
                rotateEntity.rotatable.setOrientation(orientation, rotateEntity.animation);
            }
        }
    }

    /**
     * Enable/disable swipe-to-filmstrip. Will always disable swipe if in
     * capture intent.
     *
     * @param enable {@code true} to enable swipe.
     */
    public void setSwipingEnabled(boolean enable) {
        // TODO: Bring back the functionality.
        if (isCaptureIntent()) {
            // lockPreview(true);
        } else {
            // lockPreview(!enable);
        }
    }

    // Accessor methods for getting latency times used in performance testing
    public long getFirstPreviewTime() {
        if (mCurrentModule instanceof PhotoModule) {
            long coverHiddenTime = getCameraAppUI().getCoverHiddenTime();
            if (coverHiddenTime != -1) {
                return coverHiddenTime - mOnCreateTime;
            }
        }
        return -1;
    }

    public long getAutoFocusTime() {
        return (mCurrentModule instanceof PhotoModule) ?
                ((PhotoModule) mCurrentModule).mAutoFocusTime : -1;
    }

    public long getShutterLag() {
        return (mCurrentModule instanceof PhotoModule) ?
                ((PhotoModule) mCurrentModule).mShutterLag : -1;
    }

    public long getShutterToPictureDisplayedTime() {
        return (mCurrentModule instanceof PhotoModule) ?
                ((PhotoModule) mCurrentModule).mShutterToPictureDisplayedTime : -1;
    }

    public long getPictureDisplayedToJpegCallbackTime() {
        return (mCurrentModule instanceof PhotoModule) ?
                ((PhotoModule) mCurrentModule).mPictureDisplayedToJpegCallbackTime : -1;
    }

    public long getJpegCallbackFinishTime() {
        return (mCurrentModule instanceof PhotoModule) ?
                ((PhotoModule) mCurrentModule).mJpegCallbackFinishTime : -1;
    }

    public long getCaptureStartTime() {
        return (mCurrentModule instanceof PhotoModule) ?
                ((PhotoModule) mCurrentModule).mCaptureStartTime : -1;
    }

    public boolean isRecording() {
        return (mCurrentModule instanceof VideoModule) ?
                ((VideoModule) mCurrentModule).isRecording() : false;
    }

    public CameraAgent.CameraOpenCallback getCameraOpenErrorCallback() {
        return mCameraController;
    }

    // For debugging purposes only.
    public CameraModule getCurrentModule() {
        return mCurrentModule;
    }

    @Override
    public void showTutorial(AbstractTutorialOverlay tutorial) {
        mCameraAppUI.showTutorial(tutorial, getLayoutInflater());
    }

    @Override
    public void showErrorAndFinish(int messageId) {
        CameraUtil.showErrorAndFinish(this, messageId);
    }

    @Override
    public int getSupportedHardwarelevel(int id) {
        return mCameraController.getSupportedHardwareLevel(id);
    }

    /**
     * Reads the current location recording settings and passes it on to the
     * location manager.
     */
    public void syncLocationManagerSetting() {
        Keys.syncLocationManager(mSettingsManager, mLocationManager);
    }

    public void pauseLocationManager() {
        if (mLocationManager != null) {
            Keys.pauseLocationManager(mLocationManager);
        }
    }

    private void keepScreenOnForAWhile() {
        if (mKeepScreenOn) {
            return;
        }
        if (CameraUtil.isBatterySaverEnabled(this)) {
            SCREEN_DELAY_MS = SCREEN_POWER_SAVE_MODE_DELAY_MS;
        } else {
            SCREEN_DELAY_MS = SCREEN_NORMAL_DELAY_MS;
        }
        mMainHandler.removeMessages(MSG_CLEAR_SCREEN_ON_FLAG);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mMainHandler.sendEmptyMessageDelayed(MSG_CLEAR_SCREEN_ON_FLAG, SCREEN_DELAY_MS);
    }

    private void resetScreenOn() {
        mKeepScreenOn = false;
        mMainHandler.removeMessages(MSG_CLEAR_SCREEN_ON_FLAG);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setNfcBeamPushUriFromData(LocalData data) {
        final Uri uri = data.getUri();
        if (uri != Uri.EMPTY) {
            mNfcPushUris[0] = uri;
        } else {
            mNfcPushUris[0] = null;
        }
    }

    /**
     * called only for support Help tip function
     * if support ,it needs to set DEF_CAMERA_SUPPORT_HELP_TIP_TUTORIAL true
     */
    @Override
    public HelpTipsManager getHelpTipsManager() {
        return mHelpTipsManager;
    }

    /**
     * onWindowFocusChanged activity has focus
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        /* MODIFIED-BEGIN by nie.lei, 2016-03-21, BUG-1845068 */
        if (hasFocus) {
            if (!CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_LOW_BATTERY_FEATURE_INDEPENDENT, true)) {
                boolean currentBatterySave = !CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_LOW_BATTERY_FEATURE_INDEPENDENT, true)
                        && CameraUtil.isBatterySaverEnabled(CameraActivity.this);
                if (currentBatterySave != mBatterySaveOn) {
                    mBatterySaveOn = currentBatterySave;
                    BatteryManager batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
                    int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                    Log.i(TAG, "mBatterySaveOn changed and current batteryLevel = " + batteryLevel);
                    mBatteryLevel = batteryLevel;
                    /*MODIFIED-BEGIN by peixin, 2016-04-15,BUG-1950778*/
                    if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
                        WARNING_BATTERY_LEVEL = WARNING_BATTERY_LEVEL_MTK; //MODIFIED by peixin, 2016-04-19,BUG-1950778
                    }
                    /*MODIFIED-END by peixin,BUG-1950778*/

                    if (mBatteryLevel > WARNING_BATTERY_LEVEL) {
                        if (currentBatteryStatus != BATTERY_STATUS_OK) {
                            currentBatteryStatus = BATTERY_STATUS_OK;
                        }
                    } else if (mBatteryLevel <= WARNING_BATTERY_LEVEL
                            && mBatteryLevel > LOW_BATTERY_LEVEL) {
                        if (currentBatteryStatus != BATTERY_STATUS_WARNING) {
                            String photoFlashMode = mSettingsManager.getString(getCameraScope(),
                                    Keys.KEY_FLASH_MODE);
                            if (!FLASH_OFF.equals(photoFlashMode)) {
                                mSettingsManager.set(getCameraScope(),
                                        Keys.KEY_FLASH_MODE, FLASH_OFF);
                            }
                            String videoFlashMode = mSettingsManager.getString(getCameraScope(),
                                    Keys.KEY_VIDEOCAMERA_FLASH_MODE);
                            if (!FLASH_OFF.equals(videoFlashMode)) {
                                mSettingsManager.set(getCameraScope(),
                                        Keys.KEY_VIDEOCAMERA_FLASH_MODE, FLASH_OFF);
                            }
                            currentBatteryStatus = BATTERY_STATUS_WARNING;

                        }
                    } else if (mBatteryLevel <= LOW_BATTERY_LEVEL) {
                        if (currentBatteryStatus != BATTERY_STATUS_LOW) {
                            currentBatteryStatus = BATTERY_STATUS_LOW;
                        }
                    }

                    ModuleController moduleController = getCurrentModuleController();
                    mCameraAppUI.applyModuleSpecs(moduleController.getHardwareSpec(),
                            moduleController.getBottomBarSpec());
                    mCameraAppUI.setViewFinderLayoutVisibile(false);
                }
            }
        }

        if (CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_HELP_TIP_TUTORIAL, false)) {
            //make sure after request Permissions Finished
            if (hasFocus && mRequestPermissionsFinished && mHelpTipsManager != null) {
            /* MODIFIED-END by nie.lei,BUG-1845068 */
                takeHelpTipTutorial();
            }
        }
    }

    /**
     * Take Help Tip Tutorial
     */
    private void takeHelpTipTutorial() {
        boolean isPromptWelcome = !mSettingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_WELCOME_FINISHED, false);
        Log.e(TAG, "helptip isPromptWelcome = " + isPromptWelcome + ",mCurrentModeIndex =" + mCurrentModeIndex);
        if (isPromptWelcome && mCurrentModeIndex == getResources().getInteger(R.integer.camera_mode_photo)) {
            if (mHelpTipsManager != null) {
                mHelpTipsManager.createAndShowHelpTip(HelpTipsManager.WELCOME_GROUP, true);
            }
        } else {
            if (mHelpTipsManager != null) {
                mHelpTipsManager.checkAlarmTaskHelpTip();
            }
        }
    }

    private static class PeekAnimationHandler extends Handler {
        private class DataAndCallback {
            ContentResolver mResolver; // MODIFIED by jianying.zhang, 2016-06-17,BUG-2377110
            LocalData mData;
            com.android.camera.util.Callback<Bitmap> mCallback;

            public DataAndCallback(LocalData data, com.android.camera.util.Callback<Bitmap>
                    callback) {
                mData = data;
                mCallback = callback;
            }

            /* MODIFIED-BEGIN by jianying.zhang, 2016-06-17,BUG-2377110*/
            public DataAndCallback(LocalData data, com.android.camera.util.Callback<Bitmap>
                    callback, ContentResolver resolver) {
                mData = data;
                mCallback = callback;
                mResolver = resolver;
            }
            /* MODIFIED-END by jianying.zhang,BUG-2377110*/
        }

        private final Handler mMainHandler;
        private final MainActivityLayout mMainActivityLayout;

        public PeekAnimationHandler(Looper looper, Handler mainHandler,
                                    MainActivityLayout mainActivityLayout) {
            super(looper);
            mMainHandler = mainHandler;
            mMainActivityLayout = mainActivityLayout;
        }

        /**
         * Starts the animation decoding job and posts a {@code Runnable} back
         * when when the decoding is done.
         *
         * @param data     The data item to decode the thumbnail for.
         * @param callback {@link com.android.camera.util.Callback} after the
         *                 decoding is done.
         */
        public void startDecodingJob(final LocalData data,
                                     final com.android.camera.util.Callback<Bitmap> callback) {
            PeekAnimationHandler.this.obtainMessage(0 /** dummy integer **/,
                    new DataAndCallback(data, callback)).sendToTarget();
        }

        /* MODIFIED-BEGIN by jianying.zhang, 2016-06-17,BUG-2377110*/
        public void startDecodingJob(final LocalData data,
                                     final com.android.camera.util.Callback<Bitmap> callback, ContentResolver resolver) {
            PeekAnimationHandler.this.obtainMessage(0 /** dummy integer **/,
                    new DataAndCallback(data, callback, resolver)).sendToTarget();
        }
        /* MODIFIED-END by jianying.zhang,BUG-2377110*/

        @Override
        public void handleMessage(Message msg) {
            final LocalData data = ((DataAndCallback) msg.obj).mData;
            final com.android.camera.util.Callback<Bitmap> callback =
                    ((DataAndCallback) msg.obj).mCallback;
            if (data == null || callback == null) {
                return;
            }

            final Bitmap bitmap;
            switch (data.getLocalDataType()) {
                case LocalData.LOCAL_IN_PROGRESS_DATA:
                    byte[] jpegData = Storage.getJpegForSession(data.getUri());
                    if (jpegData != null) {
                        bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
                    } else {
                        bitmap = null;
                    }
                    break;

                case LocalData.LOCAL_IMAGE:
                    FileInputStream stream;
                    try {
                        stream = new FileInputStream(data.getPath());
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "File not found:" + data.getPath());
                        return;
                    }
                    Point dim = CameraUtil.resizeToFill(data.getWidth(), data.getHeight(),
                            data.getRotation(), mMainActivityLayout.getWidth(),
                            mMainActivityLayout.getMeasuredHeight());
                    if (data.getRotation() % 180 != 0) {
                        int dummy = dim.x;
                        dim.x = dim.y;
                        dim.y = dummy;
                    }
                    bitmap = LocalDataUtil
                            .loadImageThumbnailFromStream(stream, data.getWidth(), data.getHeight(),
                                    (int) (dim.x * 0.7f), (int) (dim.y * 0.7),
                                    data.getRotation(), MAX_PEEK_BITMAP_PIXELS);
                    break;

                case LocalData.LOCAL_VIDEO:
                    /* MODIFIED-BEGIN by jianying.zhang, 2016-06-17,BUG-2377110*/
                    ContentResolver resolver = ((DataAndCallback) msg.obj).mResolver;
                    if (resolver != null) {
                        bitmap = MediaStore.Video.Thumbnails.getThumbnail(resolver, data.getContentId(),
                                MediaStore.Video.Thumbnails.MINI_KIND, null);
                    } else {
                        Log.d(TAG, "ContentResolver is null");
                        bitmap = LocalDataUtil.loadVideoThumbnail(data.getPath());
                    }
                    /* MODIFIED-END by jianying.zhang,BUG-2377110*/
                    break;

                default:
                    bitmap = null;
                    break;
            }

            if (bitmap == null) {
                return;
            }

            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    callback.onCallback(bitmap);
                }
            });
        }
    }

    public interface OnBatteryLowListener {
        public void onBatteryLow(int level);
    }

    public void startBatteryInfoChecking(OnBatteryLowListener l) {
        mBatteryLowListener = l;
        batteryStatusChange(currentBatteryStatus); // MODIFIED by fei.hui, 2016-09-14,BUG-2868515
    }

    public void stopBatteryInfoChecking() {
        mBatteryLowListener = null;
    }

    private class BatteryBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!CustomUtil.getInstance().getBoolean(
                    CustomFields.DEF_CAMERA_LOW_BATTERY_FEATURE_INDEPENDENT, false) && //MODIFIED by yuanxing.tan, 2016-04-05,BUG-1911947
                    !CameraUtil.isBatterySaverEnabled(CameraActivity.this)) {// MODIFIED by nie.lei, 2016-03-21, BUG-1845068
                return;
            }
            if (!Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                return;
            }
            int batteryLevel = intent.getIntExtra(EXTRA_LEVEL, -1);
            if (batteryLevel == -1) {
                Log.e(TAG, "Bad Battery Changed intent: " + batteryLevel);
                return;
            }

            mBatteryLevel = batteryLevel;
            boolean bNeedPromptBatteryChanged = false;

            /*MODIFIED-BEGIN by peixin, 2016-04-15,BUG-1950778*/
            if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
                WARNING_BATTERY_LEVEL = WARNING_BATTERY_LEVEL_MTK; //MODIFIED by peixin, 2016-04-19,BUG-1950778
            }
            /*MODIFIED-END by peixin,BUG-1950778*/

            if (mBatteryLevel > WARNING_BATTERY_LEVEL) {
                if (currentBatteryStatus != BATTERY_STATUS_OK) {
                    currentBatteryStatus = BATTERY_STATUS_OK;
                    bNeedPromptBatteryChanged = true;
                    mBatteryLevelLowFirst = true;
                    /* MODIFIED-BEGIN by feifei.xu, 2016-11-03,BUG-3304401*/
                    mBatteryLevelLowFirstEnterCamera = true;
                }
            } else {
                if (mBatteryLevelLowFirstEnterCamera) {
                    /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
                    SnackbarToast.getSnackbarToast().showToast(mAppContext,
                            mAppContext.getString(R.string.battery_info_low_toast_message)
                            ,SnackbarToast.LENGTH_LONG,SnackbarToast.DEFAULT_Y_OFFSET);
                            /* MODIFIED-END by bin-liu3,BUG-3253898*/
                    mBatteryLevelLowFirstEnterCamera = false;
                }
                if (mBatteryLevel <= WARNING_BATTERY_LEVEL
                        && mBatteryLevel > LOW_BATTERY_LEVEL && mBatteryLevelLowFirst) {
                    String photoFlashMode = mSettingsManager.getString(getCameraScope(),
                            Keys.KEY_FLASH_MODE);
                    if (!FLASH_OFF.equals(photoFlashMode)) {
                        mSettingsManager.set(getCameraScope(),
                                Keys.KEY_FLASH_MODE, FLASH_OFF);
                    }
                    String videoFlashMode = mSettingsManager.getString(getCameraScope(),
                            Keys.KEY_VIDEOCAMERA_FLASH_MODE);
                    if (!FLASH_OFF.equals(videoFlashMode)) {
                        mSettingsManager.set(getCameraScope(),
                                Keys.KEY_VIDEOCAMERA_FLASH_MODE, FLASH_OFF);
                    }
                    currentBatteryStatus = BATTERY_STATUS_WARNING;
                    bNeedPromptBatteryChanged = true;
                    mBatteryLevelLowFirst = false;
                } else if (mBatteryLevel <= LOW_BATTERY_LEVEL) {
                    currentBatteryStatus = BATTERY_STATUS_LOW;
                    bNeedPromptBatteryChanged = true;
                    /* MODIFIED-END by feifei.xu,BUG-3304401*/
                }
            }

            if (bNeedPromptBatteryChanged) {
                batteryStatusChange(currentBatteryStatus);
            }
        }
    }

    ;

    /* MODIFIED-BEGIN by fei.hui, 2016-09-14,BUG-2868515*/
    public boolean isBatteryCriticalLow() {
        /* MODIFIED-BEGIN by feifei.xu, 2016-11-04,BUG-3345198*/
        if (!CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_LOW_BATTERY_FEATURE_INDEPENDENT, false) &&
                !CameraUtil.isBatterySaverEnabled(CameraActivity.this)) {
            return false;
        } else {
            return mBatteryLevel <= CRITICAL_LOW_BATTERY_LEVEL;
        }
    }
    /* MODIFIED-END by fei.hui,BUG-2868515*/

    /* MODIFIED-BEGIN by fei.hui, 2016-10-27,BUG-3201458*/
    @Override
    public boolean isBatteryWarningOrLow() {
        if (!CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_LOW_BATTERY_FEATURE_INDEPENDENT, false) &&
                !CameraUtil.isBatterySaverEnabled(CameraActivity.this)) {
            return false;
        }
        /* MODIFIED-END by feifei.xu,BUG-3345198*/
        return mBatteryLevel <= WARNING_BATTERY_LEVEL;
    }
    /* MODIFIED-END by fei.hui,BUG-3201458*/

    public int getCurrentBatteryStatus() {
        return currentBatteryStatus;
    }

    public boolean currentBatteryStatusOK() {
        /* MODIFIED-BEGIN by nie.lei, 2016-03-21, BUG-1845068 */
        if (!CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_LOW_BATTERY_FEATURE_INDEPENDENT, true) &&
                !CameraUtil.isBatterySaverEnabled(CameraActivity.this)) {
            return true;
        }
        /* MODIFIED-END by nie.lei,BUG-1845068 */
        return currentBatteryStatus == BATTERY_STATUS_OK;
    }

    private Integer mBatteryFlashLock = null; // MODIFIED by yuanxing.tan, 2016-06-18,BUG-2202739

    private void batteryStatusChange(int status) {
        switch (status) {

            case BATTERY_STATUS_OK:
                // Enable flash, dismiss battery warning dialog
                // and battery low dialog if exist.
                if (mBatteryWarningDialog != null && mBatteryWarningDialog.isShowing()) {
                    mCameraAppUI.setViewFinderLayoutVisibile(false);
                    mBatteryWarningDialog.dismiss();
                }
                if (mBatteryLowDialog != null && mBatteryLowDialog.isShowing()) {
                    mCameraAppUI.setViewFinderLayoutVisibile(false);
                    mBatteryLowDialog.dismiss();
                }
               /* MODIFIED-BEGIN by yuanxing.tan, 2016-06-18,BUG-2202739*/
                if (mBatteryFlashLock != null) {
                    getCameraAppUI().getTopMenus()
                            .enableButtonWithToken(TopMenus.BUTTON_FLASH, mBatteryFlashLock);
                    mBatteryFlashLock = null;
                }
                /* MODIFIED-END by yuanxing.tan,BUG-2202739*/

                // Should set flash enable/disable in getBottomBarSpec.
                ModuleController moduleController = getCurrentModuleController();
                mCameraAppUI.applyModuleSpecs(moduleController.getHardwareSpec(),
                        moduleController.getBottomBarSpec());
                break;

            case BATTERY_STATUS_WARNING:
                // Disable flash, show battery warning dialog
                // and dismiss battery low dialog if exist.
                if (mBatteryLowDialog != null && mBatteryLowDialog.isShowing()) {
                    mCameraAppUI.setViewFinderLayoutVisibile(false);
                    mBatteryLowDialog.dismiss();
                }
                /* MODIFIED-BEGIN by yuanxing.tan, 2016-06-18,BUG-2202739*/
                if (mBatteryFlashLock == null) {
                    mBatteryFlashLock = getCameraAppUI().getTopMenus()
                            .disableButtonWithLock(TopMenus.BUTTON_FLASH);
                }
                /* MODIFIED-END by yuanxing.tan,BUG-2202739*/
                if (mBatteryWarningDialog == null || !mBatteryWarningDialog.isShowing()) {
                    mCameraAppUI.setViewFinderLayoutVisibile(true);
/* MODIFIED-BEGIN by fei.hui, 2016-09-09,BUG-2868515*/
//                    mBatteryWarningDialog = CameraUtil.showBatteryInfoDialog(
//                            CameraActivity.this, false,
//                            R.string.battery_info_dialog_tile,
//                            R.string.battery_info_dialog_msg_disable_flash,
//                            R.string.battery_info_dialog_button_ok,
//                            new Runnable() {
//                                @Override
//                                public void run() {
//                                    ModuleController moduleController = getCurrentModuleController();
//                                    mCameraAppUI.applyModuleSpecs(moduleController.getHardwareSpec(),
//                                            moduleController.getBottomBarSpec());
//                                    mCameraAppUI.setViewFinderLayoutVisibile(false);
//                                }
//                            });
                }
                break;

            case BATTERY_STATUS_LOW:
                if (mBatteryFlashLock == null) {
                    mBatteryFlashLock = getCameraAppUI().getTopMenus().disableButtonWithLock(TopMenus.BUTTON_FLASH);
                }
                // Show battery low dialog
                if (mBatteryWarningDialog != null && mBatteryWarningDialog.isShowing()) {
                    mCameraAppUI.setViewFinderLayoutVisibile(false);
                    mBatteryWarningDialog.dismiss();
                }
                // if mBatteryLowListener not null, low battery will be disposed in onBatteryLow.
//                if (mBatteryLowListener == null &&
//                        (mBatteryLowDialog == null || !mBatteryLowDialog.isShowing())) {
//                    mCameraAppUI.setViewFinderLayoutVisibile(true);
//                    mBatteryLowDialog = CameraUtil.showBatteryInfoDialog(
//                            CameraActivity.this, false,
//                            R.string.battery_info_dialog_tile,
//                            R.string.battery_info_dialog_msg_close_camera,
//                            R.string.battery_info_dialog_button_close,
//                            new Runnable() {
//                                @Override
//                                public void run() {
//                                    CameraActivity.this.finish();
//                                }
//                            }
//                    );
//                }else if (mBatteryLowListener != null) {
                if (mBatteryLowListener != null) {
                    /* MODIFIED-END by fei.hui,BUG-2868515*/
                    mBatteryLowListener.onBatteryLow(mBatteryLevel);
                }
                break;
        }
    }

    public interface OnInnerStorageLowListener {
        public void onInnerStorageLow(long bytes);

        public void onInnerStorage(long bytes);
    }

    protected void startInnerStorageChecking(OnInnerStorageLowListener listener) {
        mInnerStorageLowListener = listener;
        new Thread(new Runnable() {
            @Override
            public void run() {
                final long INTERVAL = 5000; // MODIFIED by fei.hui, 2016-09-09,BUG-2868515
                while (mInnerStorageLowListener != null) {
                    new innerStorageCheckTask(mInnerStorageLowListener).execute();
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    protected void stopInnerStorageChecking() {
        mInnerStorageLowListener = null;
    }

    protected class innerStorageCheckTask extends AsyncTask<Void, Void, Long> {

        OnInnerStorageLowListener mListener;

        public innerStorageCheckTask(OnInnerStorageLowListener l) {
            mListener = l;
        }

        @Override
        protected Long doInBackground(Void... params) {
            synchronized (mStorageSpaceLock) {
                return Storage.getAvailableSpace();
            }
        }

        @Override
        protected void onPostExecute(Long bytes) {
            freeInnerStorage = bytes;
            if (freeInnerStorage < INNER_STORAGE_THRESHOLD) {
                mListener.onInnerStorageLow(freeInnerStorage);
            }
            mListener.onInnerStorage(freeInnerStorage);
        }
    }

    private boolean needUpdateWhenScanFinished = false;
    /* MODIFIED-BEGIN by nie.lei, 2016-05-05,BUG-2073514*/
    private boolean mShutDown = false;
    private final BroadcastReceiver mRestartPhoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "intent Action, " + action);
            if (action.equals(Intent.ACTION_SHUTDOWN)) {
                mShutDown = true;
            }
        }
    };
    /* MODIFIED-END by nie.lei,BUG-2073514*/

    private final BroadcastReceiver mMediaActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mCurrentModule != null) {
                mCurrentModule.onMediaAction(context, intent);
            }
            String action = intent.getAction();
            Log.e(TAG, "MediaAction, " + action);
            boolean isSavePathChanged = false;
            if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_EJECT)) {

                /* MODIFIED-BEGIN by nie.lei, 2016-05-05,BUG-2073514*/
                if (mShutDown) {
                    Log.e(TAG, "shutdown and ingore media broadcast of unmounted and eject ");
                    return;
                }
                /* MODIFIED-END by nie.lei,BUG-2073514*/

                String currentPath = Storage.getSavePath();
                if (currentPath.equals(Storage.SDCARD_STORAGE)) {
                    Storage.setSavePath(Storage.PHONE_STORAGE);
                    isSavePathChanged = true;
                    getSettingsManager().set(SettingsManager.SCOPE_GLOBAL,
                            Keys.KEY_CAMERA_SAVEPATH,
                            Storage.PHONE_STORAGE);
                    if (mStorageLowDialog != null && mStorageLowDialog.isShowing()) {
                        mStorageLowDialog.dismiss();
                        mStorageLowDialog = null;
                    }
                }
            } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-28,BUG-1718612*/
                if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
                    // mtk6755 sdcard root directory will be changed when mount sdcard.
                    // so need update it.
                    Storage.resetSDCardRootDirectory();
                }
                /* MODIFIED-END by bin.zhang2-nb,BUG-1718612*/
            } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                if (needUpdateWhenScanFinished || Storage.getSavePath().equals(Storage.SDCARD_STORAGE)) {
                    if (!isSecureCamera() && !isCaptureIntent() &&
                            PermissionUtil.isStoragePermissionGranted(CameraActivity.this)) {
                        onLastMediaDataUpdated();
                    }
                    needUpdateWhenScanFinished = false;
                }
            }

            if (isSavePathChanged) {
                if (mCurrentModule != null) {
                    mCurrentModule.onMediaPathChanged(context, intent);
                }
                updateStorageSpaceAndHint(null);
                needUpdateWhenScanFinished = true;
            }
        }
    };

    public void intentReviewCancel() {
        if (mCurrentModule != null && mCurrentModule instanceof PhotoModule) {
            ((PhotoModule) mCurrentModule).intentReviewCancel();
        } else if (mCurrentModule != null && mCurrentModule instanceof VideoModule) {
            ((VideoModule) mCurrentModule).intentReviewCancel();
        }
    }

    public void intentReviewDone() {
        if (mCurrentModule != null && mCurrentModule instanceof PhotoModule) {
            ((PhotoModule) mCurrentModule).intentReviewDone();
        } else if (mCurrentModule != null && mCurrentModule instanceof VideoModule) {
            ((VideoModule) mCurrentModule).intentReviewDone();
        }
    }

    public void intentReviewRetake() {
        if (mCurrentModule != null && mCurrentModule instanceof PhotoModule) {
            ((PhotoModule) mCurrentModule).intentReviewRetake();
        }
    }

    public void intentReviewPlay() {
        if (mCurrentModule != null && mCurrentModule instanceof VideoModule) {
            ((VideoModule) mCurrentModule).intentReviewPlay();
        }
    }

    public void onBoomPressed() {
        // Don't response boom key here, Moudule will consume all boom key events
        Log.d(TAG, "Document onBoomPressed");
    }

    public void onBoomLongPress() {
        Log.d(TAG, "Document onBoomLongPress");
    }

    public void onBoomDoublePress() {
        Log.d(TAG, "Document onBoomDoublePress");
    }

    @Override
    public void switchToMode(int index) {
        switchToMode(index, true);
    }

    @Override
    public void switchToMode(int index, boolean disableAnimation) {
        if (mCurrentModeIndex != index && mModeStripView != null) {
            Log.e(TAG, "Switch to mode " + index);
            /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
            int photoFilterIndex = getResources().getInteger(R.integer.camera_mode_filter);
            int videoFilterIndex = getResources().getInteger(R.integer.camera_mode_videofilter);
            if (mCurrentModeIndex == photoFilterIndex || mCurrentModeIndex == videoFilterIndex) {
                onModeSelected(index);
            } else {
                onModeSelecting(disableAnimation);
            }
            /* MODIFIED-END by jianying.zhang,BUG-3255060*/
            mCameraAppUI.setModeStripViewVisibility(true);
            mCameraAppUI.setModeScrollBarVisibility(true);
            mModeStripView.setCurrentModeWithModeIndex(index);
        }
    }

    // When onStart called for FullScreenActivity in Fyusion, I don't want to
    // reset the module, so set mSecureFyuseModule true here.
    public void setSecureFyuseModule(boolean isFyuse) {
        mSecureFyuseModule = isFyuse;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Now for Fyuse function only.
        if (mCurrentModeIndex == getResources().getInteger(R.integer.camera_mode_parallax)) {
            if (mCurrentModule != null && mCurrentModule instanceof FyuseModule) {
                ((FyuseModule) mCurrentModule).onActivityResult(requestCode, resultCode, data);
            }
        }
        switch (requestCode) {
            case CameraUtil.SETGPS: {
                CameraUtil.backFromGpsSetting(this, getSettingsManager());
                break;
            }

            case PermsInfo.ACTIVITY_REQUEST_CODE:
                handlePermissionResult(resultCode, data);
                break;
        }
    }

    public CaptureLayoutHelper getCaptureLayoutHelper() {
        return mCameraAppUI.getCaptureLayoutHelper();
    }

    /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-12,BUG-1892480*/
    public interface ControlPoseCallback {
        /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-19,BUG-1892480*/
        void onVisibilityChange(boolean isModeSelecting, boolean isAutomode);

        void onResetVisibility(boolean isResume);
        /*MODIFIED-END by shunyin.zhang,BUG-1892480*/
    }

    public void setControlPoseCallback(ControlPoseCallback callback) {
        mCallback = callback;
    }

    public void resetControlPoseCallback() {
        if (mCallback != null) {
            mCallback = null;
        }
    }
    /*MODIFIED-END by shunyin.zhang,BUG-1892480*/

    /* MODIFIED-BEGIN by wenhua.tu, 2016-08-11,BUG-2710178*/
    public CameraSettings getCameraSettings() {
        if (TestUtils.IS_TEST) {
            return mCurrentModule.getCameraSettings();
        }
        return null;
    }

    public CameraCapabilities getCameraCapabilities() {
        if (TestUtils.IS_TEST) {
            return mCurrentModule.getCameraCapabilities();
        }
        return null;
    }
    /* MODIFIED-END by wenhua.tu,BUG-2710178*/

    private boolean checkPermissions() {
        if (!ApiHelper.isMOrHigher()) {
            return true;
        }

        if (mSettingsManager == null) {
            mSettingsManager = getServices().getSettingsManager();
        }
        boolean isLocationSetAlready = mSettingsManager.isSet(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_RECORD_LOCATION);
        Intent mIntent = getIntent();
        String action = mIntent.getAction();
        boolean isSecureCamera = INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(action) ||
                ACTION_IMAGE_CAPTURE_SECURE.equals(action) ||
                mIntent.getBooleanExtra(SECURE_CAMERA_EXTRA, false);
        int requestCode = PermissionUtil.getRequestCode(this, isLocationSetAlready || isSecureCamera);
        if (requestCode == 0) {
            return true;
        }
        Intent intent = new Intent(this, PermissionActivity.class);
        intent.putExtra(PermsInfo.TAG_REQUEST_CODE, requestCode);
        startActivityForResult(intent, PermsInfo.ACTIVITY_REQUEST_CODE);
        return false;
    }

    private void handlePermissionResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data == null) {
            // Permissions request may be paused, check again here.
            checkPermissions();
            return;
        }

        int result = data.getIntExtra(PermsInfo.TAG_REQUEST_RESULT, 0);

        if (result == PermsInfo.RESULT_CRITICAL_DENIED) {
            finish();
            return;
        } else if (result == PermsInfo.RESULT_LOCATION_DENIED ||
                result == PermsInfo.RESULT_GRANTED) {
            int requestCode = data.getIntExtra(PermsInfo.TAG_REQUEST_CODE, 0);

            boolean cameraGranted = ((requestCode & PermsInfo.REQUEST_CAMERA) != 0);
            boolean storageGranted = ((requestCode & PermsInfo.REQUEST_EXTERNAL_STORAGE) != 0);
            boolean microphoneGranted = ((requestCode & PermsInfo.REQUEST_MICROPHONE) != 0);
            boolean locationGranted = ((requestCode & PermsInfo.REQUEST_LOCATION) != 0);

            if (cameraGranted) {
                onCameraPermsGranted();
            }

            if (storageGranted) {
                onStoragePermsGranted();
            }

            if (microphoneGranted) {
                onMicrophonePermsGranted();
            }

            if (result == PermsInfo.RESULT_LOCATION_DENIED) {
                onLocationPermsDenied();
            } else if (locationGranted) {
                onLocationPermsGranted();
            }
        } else {
            if (PermsInfo.DEBUG) {
                Log.e(TAG, "Unknown permission result " + result);
            }
        }
    }

    private void onCameraPermsGranted() {
        // No need to request Camera now.
    }

    private void onStoragePermsGranted() {
        if (!isSecureCamera() && !isCaptureIntent()) {
            onLastMediaDataUpdated();
        }
        updateStorageSpaceAndHint(null);
    }

    private void onMicrophonePermsGranted() {
        // Do nothing.
    }

    private void onLocationPermsGranted() {
        Keys.setLocation(getSettingsManager(), true, getLocationManager());
        CameraUtil.gotoGpsSetting(this, getSettingsManager(), R.drawable.gps_grey);
    }

    private void onLocationPermsDenied() {
        Keys.setLocation(getSettingsManager(), false, getLocationManager());
        /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
        SnackbarToast.getSnackbarToast().showToast(this, getString(R.string.location_toast)
                , SnackbarToast.LENGTH_LONG,SnackbarToast.DEFAULT_Y_OFFSET);
                /* MODIFIED-END by bin-liu3,BUG-3253898*/

    }

    @Override
    public FragmentManager getFragmentManager() {
        return super.getFragmentManager();
    }

}
