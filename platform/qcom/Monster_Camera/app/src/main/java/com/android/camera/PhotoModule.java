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

/* MODIFIED-BEGIN by fei.hui, 2016-09-29,BUG-2994050*/
/* MODIFIED-END by fei.hui,BUG-2994050*/

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraCharacteristics;
import android.location.Location;
import android.media.CameraProfile;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.camera.PhotoModule.NamedImages.NamedEntity;
import com.android.camera.app.AppController;
import com.android.camera.app.CameraAppUI;
import com.android.camera.app.CameraProvider;
import com.android.camera.app.MediaSaver;
import com.android.camera.app.MemoryManager;
import com.android.camera.app.MemoryManager.MemoryListener;
import com.android.camera.app.MotionManager;
import com.android.camera.debug.Log;
import com.android.camera.exif.ExifInterface;
import com.android.camera.exif.ExifTag;
import com.android.camera.exif.Rational;
import com.android.camera.hardware.HardwareSpec;
import com.android.camera.hardware.HardwareSpecImpl;
import com.android.camera.module.ModuleController;
import com.android.camera.remote.RemoteCameraModule;
import com.android.camera.settings.Keys;
import com.android.camera.settings.ResolutionUtil;
import com.android.camera.settings.SettingsManager;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.test.TestUtils; // MODIFIED by wenhua.tu, 2016-08-11,BUG-2710178
import com.android.camera.ui.BottomBar;
import com.android.camera.ui.CountDownView;
import com.android.camera.ui.TouchCoordinate;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.BoostUtil;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.util.ExternalExifInterface;
import com.android.camera.util.GcamHelper;
import com.android.camera.util.GservicesHelper;
import com.android.camera.util.SessionStatsCollector;
import com.android.camera.util.SnackbarToast; // MODIFIED by fei.hui, 2016-09-09,BUG-2868515
import com.android.camera.util.ToastUtil;
import com.android.camera.util.UsageStatistics;
import com.android.camera.widget.AspectRatioSelector;
import com.android.camera.widget.TopMenus;
import com.android.ex.camera2.portability.CameraAgent;
import com.android.ex.camera2.portability.CameraAgent.CameraAFCallback;
import com.android.ex.camera2.portability.CameraAgent.CameraAFMoveCallback;
import com.android.ex.camera2.portability.CameraAgent.CameraPictureCallback;
import com.android.ex.camera2.portability.CameraAgent.CameraProxy;
import com.android.ex.camera2.portability.CameraAgent.CameraShutterCallback;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.ex.camera2.portability.CameraDeviceInfo.Characteristics;
import com.android.ex.camera2.portability.CameraSettings;
import com.android.ex.camera2.portability.Size;
import com.android.external.plantform.ExtBuild;
import com.crunchfish.core.GestureDetectionCallback;
import com.crunchfish.core.GestureInstructionWrapper;
import com.google.common.logging.eventprotos;
import com.tct.camera.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class PhotoModule
        extends CameraModule
        implements PhotoController,
        ModuleController,
        MemoryListener,
        FocusOverlayManager.Listener,
        SensorEventListener,
        SettingsManager.OnSettingChangedListener,
        RemoteCameraModule,
        CountDownView.OnCountDownStatusListener,CameraAgent.CameraPreviewDataCallback { //MODIFIED by shunyin.zhang, 2016-04-12,BUG-1892480

    private static final String PHOTO_MODULE_STRING_ID = "PhotoModule";

    private static final Log.Tag TAG = new Log.Tag(PHOTO_MODULE_STRING_ID);

    private static final int HIDE_GESTURE_DELAY=1000;
    private static final int GESTURE_SKIP_TIMER=100;

    protected static final int BURST_STOP_DELAY=150;

    private static final int BURST_DELAY=0;
    protected static final int BURST_MAX=CustomUtil.getInstance().getInt(CustomFields.DEF_CAMERA_BURST_MAX,10);//Upper number of photos received from a single burst-shot
    // We number the request code from 1000 to avoid collision with Gallery.
    private static final int REQUEST_CROP = 1000;

    // Messages defined for the UI thread handler.
    private static final int MSG_FIRST_TIME_INIT = 1;
    private static final int MSG_SET_CAMERA_PARAMETERS_WHEN_IDLE = 2;
    private static final int MSG_CAPTURE_BURST=3;
    private static final int MSG_HIDE_GESTURE=4;

    private static final int MSG_UPDATE_FACE_BEAUTY = 5;
    protected static final int MSG_CLEAR_ASPECT_RATIO_VIEW = 6;
    // The subset of parameters we need to update in setCameraParameters().
    protected static final int UPDATE_PARAM_INITIALIZE = 1;
    protected static final int UPDATE_PARAM_ZOOM = 2;
    protected static final int UPDATE_PARAM_PREFERENCE = 4;
    protected static final int UPDATE_PARAM_VISIDON = 8;
    protected static final int UPDATE_PARAM_ALL = -1;

    private static final int MIN_CAMERA_LAUNCHING_TIMES = 3;

    private static final int DEFAULT_GESTURE_SHOT_COUNT_DURATION = 3;

    private static final int SHUTTER_PROGRESS_INIT=0;

    private static final int SHUTTER_PROGRESS_MAX=100;

    private static final int SHUTTER_PROGRESS_FAKE_END=90;

    private static final int SHUTTER_PROGRESS_ACCELERATE_THRESHOLD=30;

    private static final int SHUTTER_DELAY_LOW=80;

    private static final int SHUTTER_DELAY_UP=100;

    private static final int SHUTTER_PROGRESS_STEP=5;

    private static final int SHUTTER_PROGRESS_HIDE_DELAY=80;

    private static final int EXPOSURE_TIME = 0; // MODIFIED by peixin, 2016-05-09,BUG-2011866

    private final static int MODE_OPTIONS_TIP_TIME = 2000;

    private static final String DEBUG_IMAGE_PREFIX = "DEBUG_";

    protected CameraActivity mActivity;
    protected CameraProxy mCameraDevice;
    /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
    protected int mCameraId;
    protected CameraCapabilities mCameraCapabilities;
    protected CameraSettings mCameraSettings;
    protected boolean mPaused;

    //whether to enable optimization of capturing picture
//    private boolean OPT_CAPTURE = CustomUtil.getInstance()
//            .getBoolean(CustomFields.DEF_CAMERA_ENABLE_OPTIMIZE_CAPTURE, false);
    private static final  boolean OPT_CAPTURE =true;
    /* MODIFIED-END by sichao.hu,BUG-2743263*/
    private final boolean isOptimizeSwitchCamera = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_ENABLE_OPTIMIZE_SWITCH, false);
//    private boolean isOptimeizeSnapshot = CustomUtil.getInstance()
//            .getBoolean(CustomFields.DEF_CAMERA_ENABLE_OPTIMEIZE_SNAPSHOT, false);
    private boolean isOptimeizeSnapshot = false;
    private boolean isFlashOnSnapshot   = false;// MODIFIED by yongsheng.shan, 2016-03-24,BUG-1861478

    private PhotoUI mUI;

    // The activity is going to switch to the specified camera id. This is
    // needed because texture copy is done in GL thread. -1 means camera is not
    // switching.
    protected int mPendingSwitchCameraId = -1;

    // When setCameraParametersWhenIdle() is called, we accumulate the subsets
    // needed to be updated in mUpdateSet.
    private int mUpdateSet;

    protected float mZoomValue; // The current zoom ratio. // MODIFIED by jianying.zhang, 2016-11-04,BUG-3330109
    private final float mSuperZoomThreshold = 1.5f;
    private int mTimerDuration;
    /** Set when a volume button is clicked to take photo */
    protected boolean mVolumeButtonClickedFlag = false;

    private boolean mCameraKeyLongPressed = false;

    private boolean mFocusAreaSupported;
    private boolean mMeteringAreaSupported;
    private boolean mAeLockSupported;
    private boolean mAwbLockSupported;
    private boolean mContinuousFocusSupported;

    // The degrees of the device rotated clockwise from its natural orientation.
    protected int mOrientation = 0;

    private static final String sTempCropFilename = "crop-temp";

    private boolean mFaceDetectionStarted = false;

    // mCropValue and mSaveUri are used only if isImageCaptureIntent() is true.
    private String mCropValue;
    private Uri mSaveUri;

    private Uri mDebugUri;

    // We use a queue to generated names of the images to be used later
    // when the image is ready to be saved.
    protected NamedImages mNamedImages;


    //Sound player thread is used for Burst shutter sound ,
    // it's more suitable for burst shot case
    // and it can avoid unnecessary shutter sound playing of aborted on flight burst shot
    protected SoundPlay mSoundPlayer;

    private final Runnable mDoSnapRunnable = new Runnable() {
        @Override
        public void run() {
            onShutterButtonClick();
        }
    };

    /**
     * An unpublished intent flag requesting to return as soon as capturing is
     * completed. TODO: consider publishing by moving into MediaStore.
     */
    private static final String EXTRA_QUICK_CAPTURE =
            "android.intent.extra.quickCapture";

    // The display rotation in degrees. This is only valid when mCameraState is
    // not PREVIEW_STOPPED.
    protected int mDisplayRotation;
    // The value for UI components like indicators.
    private int mDisplayOrientation;
    private int mSensorOrientation;
    // The value for cameradevice.CameraSettings.setPhotoRotationDegrees.
    private int mJpegRotation;
    // Indicates whether we are using front camera
    private boolean mMirror;
    private boolean mFirstTimeInitialized;
    private boolean mIsImageCaptureIntent;

    protected int mCameraState = PREVIEW_STOPPED;
    private boolean mSnapshotOnIdle = false;

    protected ContentResolver mContentResolver;

    protected AppController mAppController;


    private final PostViewPictureCallback mPostViewPictureCallback =
            new PostViewPictureCallback();
    private final RawPictureCallback mRawPictureCallback =
            new RawPictureCallback();
    private final AutoFocusCallback mAutoFocusCallback =
            new AutoFocusCallback();
    protected final Object mAutoFocusMoveCallback =
            ApiHelper.HAS_AUTO_FOCUS_MOVE_CALLBACK
                    ? new AutoFocusMoveCallback()
                    : null;

    private long mFocusStartTime;
    private long mShutterCallbackTime;
    private long mPostViewPictureCallbackTime;
    private long mRawPictureCallbackTime;
    private long mJpegPictureCallbackTime;
    private long mOnResumeTime;
    private byte[] mJpegImageData;
    protected int mReceivedBurstNum=0;
    private int mBurstNumForOneSingleBurst=0;
    /** Touch coordinate for shutter button press. */
    private TouchCoordinate mShutterTouchCoordinate;


    // These latency time are for the CameraLatency test.
    public long mAutoFocusTime;
    public long mShutterLag;
    public long mShutterToPictureDisplayedTime;
    public long mPictureDisplayedToJpegCallbackTime;
    public long mJpegCallbackFinishTime;
    public long mCaptureStartTime;

    // This handles everything about focus.
    protected FocusOverlayManager mFocusManager;

    private final int mGcamModeIndex;
    private SoundPlayer mCountdownSoundPlayer;
    private CameraCapabilities.SceneMode mSceneMode = CameraCapabilities.SceneMode.AUTO; // MODIFIED by bin-liu3, 2016-10-28,BUG-3159519


    protected final Handler mHandler = new MainHandler(this);

    private boolean mQuickCapture;
    private SensorManager mSensorManager;
    private final float[] mGData = new float[3];
    private final float[] mMData = new float[3];
    private final float[] mR = new float[16];
    private int mHeading = -1;
    private boolean mIsInIntentReviewUI = false;

    /** Used to detect motion. We use this to release focus lock early. */
    private MotionManager mMotionManager;

    /** True if all the parameters needed to start preview is ready. */
    private boolean mCameraPreviewParamsReady = false;

    private boolean mUnderLowMemory=false;


    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-10,BUG-3398235*/
    private Integer mZoomFlashLock = null;
    private Integer mAeFlashLock = null;
    /* MODIFIED-END by jianying.zhang,BUG-3398235*/
    protected final MediaSaver.OnMediaSavedListener mOnMediaSavedListener =
            new MediaSaver.OnMediaSavedListener() {
                @Override
                public void onMediaSaved(Uri uri) {
                    if (uri != null) {
                        int notifyAction=AppController.NOTIFY_NEW_MEDIA_ACTION_ANIMATION|AppController.NOTIFY_NEW_MEDIA_ACTION_UPDATETHUMB;
                        if(isOptimizeCapture()){ // MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
                            notifyAction|=AppController.NOTIFY_NEW_MEDIA_ACTION_OPTIMIZECAPTURE;
                        }
                        mActivity.notifyNewMedia(uri,notifyAction);
                    }
                }
            };

    /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
    protected boolean isOptimizeCapture(){
        return OPT_CAPTURE;
    }
    /* MODIFIED-END by sichao.hu,BUG-2743263*/

    protected final BurstShotCheckQueue mBurstShotCheckQueue=new BurstShotCheckQueue();
    private boolean mBurstShotNotifyHelpTip = false;//burst shot just nofitys help tip only once

    private final class BurstShotSaveListener implements MediaSaver.OnMediaSavedListener{
        private int mCount=0;
        public BurstShotSaveListener(int currentCount){
            mCount=currentCount;
            mBurstShotCheckQueue.pushToJobQueue(mCount);
        }
        @Override
        public void onMediaSaved(final Uri uri) {
            Log.w(TAG, "Burst Waiting for " + mBurstShotCheckQueue.getCapacity() + " current count is " + mCount);
            mBurstShotCheckQueue.popToResultQueue(mCount);
            if(mCount<mBurstShotCheckQueue.getCapacity()){
                Log.v(TAG, "burst image saved without updating thumbnail, uri="+uri.toString());
                mActivity.notifyNewMedia(uri,AppController.NOTIFY_NEW_MEDIA_ACTION_NONE);
                // When picture is saved rather fast , stopBust could be called after all photos saved,
                // and we should rather call the picture taken animation on burst stopped
                Runnable supposeLastAction=new Runnable() {
                    @Override
                    public void run() {
                        mActivity.notifyNewMedia(uri,PhotoModule.this.getBurstShotMediaSaveAction());
                    }
                };
                mBurstShotCheckQueue.setPictureTakenActionCache(supposeLastAction);
                return;
            }else{
                if(uri!=null) {
                    Log.v(TAG, "update thumbnail with burst image, uri="+uri.toString());
                    mActivity.notifyNewMedia(uri,PhotoModule.this.getBurstShotMediaSaveAction());
                }
                dismissSavingHint();
            }
        }
    }


    protected int getBurstShotMediaSaveAction(){
        return AppController.NOTIFY_NEW_MEDIA_ACTION_ANIMATION|AppController.NOTIFY_NEW_MEDIA_ACTION_UPDATETHUMB;
    }

    private boolean mShouldResizeTo16x9 = false;

    /**
     * We keep the flash setting before entering scene modes (HDR)
     * and restore it after HDR is off.
     */
    private String mFlashModeBeforeSceneMode;

    /**
     * This callback gets called when user select whether or not to
     * turn on geo-tagging.
     */
    public interface LocationDialogCallback {
        /**
         * Gets called after user selected/unselected geo-tagging feature.
         *
         * @param selected whether or not geo-tagging feature is selected
         */
        public void onLocationTaggingSelected(boolean selected);
    }

    /**
     * This callback defines the text that is shown in the aspect ratio selection
     * dialog, provides the current aspect ratio, and gets notified when user changes
     * aspect ratio selection in the dialog.
     */
    public interface AspectRatioDialogCallback {
        /**
         * Returns current aspect ratio that is being used to set as default.
         */
        public AspectRatioSelector.AspectRatio getCurrentAspectRatio();

        /**
         * Gets notified when user has made the aspect ratio selection.
         *
         * @param newAspectRatio aspect ratio that user has selected
         * @param dialogHandlingFinishedRunnable runnable to run when the operations
         *                                       needed to handle changes from dialog
         *                                       are finished.
         */
        public void onAspectRatioSelected(AspectRatioSelector.AspectRatio newAspectRatio,
                Runnable dialogHandlingFinishedRunnable);
    }

    private void checkDisplayRotation() {
        // Need to just be a no-op for the quick resume-pause scenario.
        if (mPaused) {
            return;
        }
        // Set the display orientation if display rotation has changed.
        // Sometimes this happens when the device is held upside
        // down and camera app is opened. Rotation animation will
        // take some time and the rotation value we have got may be
        // wrong. Framework does not have a callback for this now.
        if (CameraUtil.getDisplayRotation(mActivity) != mDisplayRotation) {
            setDisplayOrientation();
        }
        if (SystemClock.uptimeMillis() - mOnResumeTime < 5000) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkDisplayRotation();
                }
            }, 100);
        }
    }

    protected interface MainHandlerCallback {
        public void handleMessageEx(Message msg);
    }

    private MainHandlerCallback mMainHandlerCallback = null;


    public void setMainHandlerCallback(MainHandlerCallback l) {
        mMainHandlerCallback = l;
    }

    /**
     * This Handler is used to post message back onto the main thread of the
     * application
     */
    private class MainHandler extends Handler {
        private final WeakReference<PhotoModule> mModule;

        public MainHandler(PhotoModule module) {
            super(Looper.getMainLooper());
            mModule = new WeakReference<PhotoModule>(module);
        }

        @Override
        public void handleMessage(Message msg) {
            PhotoModule module = mModule.get();
            if (module == null) {
                return;
            }
            switch (msg.what) {
                case MSG_FIRST_TIME_INIT: {
                    module.initializeFirstTime();
                    break;
                }

                case MSG_SET_CAMERA_PARAMETERS_WHEN_IDLE: {
                    module.setCameraParametersWhenIdle(0);
                    break;
                }
                case MSG_HIDE_GESTURE:{
                    mUI.hideGesture();
                    break;
                }
                case MSG_CAPTURE_BURST:{
                    if(mReceivedBurstNum>=BURST_MAX){
                        break;
                    }
                    mCameraDevice.burstShot(mHandler,
                            mLongshotShutterCallback,
                            mRawPictureCallback, mPostViewPictureCallback,
                            mLongshotPictureTakenCallback);
                    mHandler.sendEmptyMessageDelayed(MSG_CAPTURE_BURST, 300);
                    break;
                }
                case MSG_UPDATE_FACE_BEAUTY: {
                    if (isCameraFrontFacing() && !mPaused && mCameraSettings != null) { //MODIFIED by yuanxing.tan, 2016-03-30,BUG-1874767
                        Log.e(TAG, "update facebeauty");
                        SettingsManager settingsManager = mActivity.getSettingsManager();
                        int skinSmoothing = mActivity.getSettingsManager().getInteger(SettingsManager.SCOPE_GLOBAL, Keys.KEY_FACEBEAUTY_SKIN_SMOOTHING, SKIN_SMOOTHING_DEFAULT);
                        mCameraSettings.setFaceBeauty(Keys.isFacebeautyOn(settingsManager), skinSmoothing * 90 / 100);
                        updateFaceBeautySetting(Keys.KEY_FACEBEAUTY_SKIN_SMOOTHING, skinSmoothing);
                    }
                    break;
                }
                case MSG_CLEAR_ASPECT_RATIO_VIEW: {
                    clearAspectRatioViewer(true);
                    break;
                }
                default: {
                    if (mMainHandlerCallback != null) {
                        mMainHandlerCallback.handleMessageEx(msg);
                    }
                }
            }
        }
    }

    private void switchToGcamCapture() {
        if (mActivity != null && mGcamModeIndex != 0) {
            SettingsManager settingsManager = mActivity.getSettingsManager();
            settingsManager.set(SettingsManager.SCOPE_GLOBAL,
                                Keys.KEY_CAMERA_HDR_PLUS, true);

            // Disable the HDR+ button to prevent callbacks from being
            // queued before the correct callback is attached to the button
            // in the new module.  The new module will set the enabled/disabled
            // of this button when the module's preferred camera becomes available.
            ButtonManager buttonManager = mActivity.getButtonManager();

            buttonManager.disableButtonClick(ButtonManager.BUTTON_HDR_PLUS);

            mAppController.getCameraAppUI().freezeScreenUntilPreviewReady();

            // Do not post this to avoid this module switch getting interleaved with
            // other button callbacks.
            mActivity.onModeSelected(mGcamModeIndex);

            buttonManager.enableButtonClick(ButtonManager.BUTTON_HDR_PLUS);
        }
    }

    private static final String GESTURE_HANDLER_NAME="gesture_handler";
    private GestureHandlerThread mGesturehandlerThread;
    private Handler mGestureHandler;

    /**
     * Constructs a new photo module.
     */
    public PhotoModule(AppController app) {
        super(app);
        mGcamModeIndex = app.getAndroidContext().getResources()
                .getInteger(R.integer.camera_mode_gcam);
    }

    @Override
    public String getPeekAccessibilityString() {
        return mAppController.getAndroidContext()
            .getResources().getString(R.string.photo_accessibility_peek);
    }

    @Override
    public String getModuleStringIdentifier() {
        return PHOTO_MODULE_STRING_ID;
    }

    protected PhotoUI getPhotoUI(){
        if (mUI == null) {
            mUI = new PhotoUI(mActivity, this, mActivity.getModuleLayoutRoot());
        }
        return mUI;
    }
    @Override
    public void init(CameraActivity activity, boolean isSecureCamera, boolean isCaptureIntent) {
        mActivity = activity;
        // TODO: Need to look at the controller interface to see if we can get
        // rid of passing in the activity directly.
        mAppController = mActivity;

        mUI = getPhotoUI();
        mActivity.setPreviewStatusListener(mUI);

        SettingsManager settingsManager = mActivity.getSettingsManager();
        mCameraId = settingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,
                                               Keys.KEY_CAMERA_ID);

        // TODO: Move this to SettingsManager as a part of upgrade procedure.
/* MODIFIED-BEGIN by nie.lei, 2016-03-25,BUG-1863635 */
//        if (!settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
//                                        Keys.KEY_USER_SELECTED_ASPECT_RATIO)) {
//            // Switch to back camera to set aspect ratio.
//            mCameraId = settingsManager.getIntegerDefault(Keys.KEY_CAMERA_ID);
//        }
/* MODIFIED-END by nie.lei,BUG-1863635 */

        mContentResolver = mActivity.getContentResolver();

        // Surface texture is from camera screen nail and startPreview needs it.
        // This must be done before startPreview.
        mIsImageCaptureIntent = isImageCaptureIntent();

        if (activity!=null && activity.isPhotoContactsIntent()) {
            mQuickCapture = true;
        } else {
            mQuickCapture = mActivity.getIntent().getBooleanExtra(EXTRA_QUICK_CAPTURE, false);
        }
        mSensorManager = (SensorManager) (mActivity.getSystemService(Context.SENSOR_SERVICE));
        mUI.setCountdownFinishedListener(this);
        mCountdownSoundPlayer = new SoundPlayer(mAppController.getAndroidContext());


        // TODO: Make this a part of app controller API.
        View cancelButton = mActivity.findViewById(R.id.shutter_cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelCountDown();
            }
        });

    }

    protected boolean aspectRatioVisible(){
        return false;
    }

    private void cancelCountDown() {
        if (mUI.isCountingDown()) {
            // Cancel on-going countdown.
            mUI.cancelCountDown();
        }

        // If it's image capture review layout, don't make the transition.
        if (mIsInIntentReviewUI) {
            return;
        }

        setCaptureView(false);
        mAppController.getCameraAppUI().showPoseBackView(); // MODIFIED by feifei.xu, 2016-11-02,BUG-3299499
        mAppController.getCameraAppUI().transitionToCapture();
        mAppController.setShutterEnabled(true);
        transitionToTimer(true);
        activeFilterButton();
    }

    @Override
    public boolean isUsingBottomBar() {
        return true;
    }

    private void initializeControlByIntent() {
        if (mIsImageCaptureIntent) {
            if (mJpegImageData == null) {
                mActivity.getCameraAppUI().transitionToIntentCaptureLayout();
            }
            setupCaptureParams();
        }
    }

    protected void transitionToTimer(boolean isShow) {}

    protected void onPreviewStarted() {
        if(mPaused){
            return;
        }
        Log.w(TAG, "KPI photo preview started");
        setCameraParameters(UPDATE_PARAM_ALL);//to clear instant aec
        mAppController.onPreviewStarted();
        mAppController.setShutterEnabled(true);
        mAppController.setShutterButtonLongClickable(!mIsImageCaptureIntent);
        mAppController.getCameraAppUI().enableModeOptions();
        mUI.clearEvoPendingUI();
        if (mCameraState == SNAPSHOT_IN_PROGRESS_DURING_LOCKED) {
            if (mResetFocusAfterSnapshot) {
                setCameraState(IDLE);
                cancelAutoFocus();
                mResetFocusAfterSnapshot = false;
            } else {
                setCameraState(AE_AF_LOCKED);
            }
        } else {
            setCameraState(IDLE);
        }
        startFaceDetection();
        BoostUtil.getInstance().releaseCpuLock();
        // settingsFirstRun();
    }

    /**
     * Prompt the user to pick to record location and choose aspect ratio for the
     * very first run of camera only.
     */
    private void settingsFirstRun() {
        final SettingsManager settingsManager = mActivity.getSettingsManager();

        if (mActivity.isSecureCamera() || isImageCaptureIntent()) {
            return;
        }

        boolean locationPrompt = !settingsManager.isSet(SettingsManager.SCOPE_GLOBAL,
                                                        Keys.KEY_RECORD_LOCATION);
        boolean aspectRatioPrompt = !settingsManager.getBoolean(
            SettingsManager.SCOPE_GLOBAL, Keys.KEY_USER_SELECTED_ASPECT_RATIO);
        if (!locationPrompt && !aspectRatioPrompt) {
            return;
        }

        // Check if the back camera exists
        int backCameraId = mAppController.getCameraProvider().getFirstBackCameraId();
        if (backCameraId == -1) {
            // If there is no back camera, do not show the prompt.
            return;
        }

        if (locationPrompt) {
            // Show both location and aspect ratio selection dialog.
            mUI.showLocationAndAspectRatioDialog(new LocationDialogCallback(){
                @Override
                public void onLocationTaggingSelected(boolean selected) {
                    Keys.setLocation(mActivity.getSettingsManager(), selected,
                                     mActivity.getLocationManager());
                }
            }, createAspectRatioDialogCallback());
        } else {
            // App upgrade. Only show aspect ratio selection.
            boolean wasShown = mUI.showAspectRatioDialog(createAspectRatioDialogCallback());
            if (!wasShown) {
                // If the dialog was not shown, set this flag to true so that we
                // never have to check for it again. It means that we don't need
                // to show the dialog on this device.
                mActivity.getSettingsManager().set(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_USER_SELECTED_ASPECT_RATIO, true);
            }
        }
    }

    private AspectRatioDialogCallback createAspectRatioDialogCallback() {
        Size currentSize = mCameraSettings.getCurrentPhotoSize();
        float aspectRatio = (float) currentSize.width() / (float) currentSize.height();
        if (aspectRatio < 1f) {
            aspectRatio = 1 / aspectRatio;
        }
        final AspectRatioSelector.AspectRatio currentAspectRatio;
        if (Math.abs(aspectRatio - 4f / 3f) <= 0.1f) {
            currentAspectRatio = AspectRatioSelector.AspectRatio.ASPECT_RATIO_4x3;
        } else if (Math.abs(aspectRatio - 16f / 9f) <= 0.1f) {
            currentAspectRatio = AspectRatioSelector.AspectRatio.ASPECT_RATIO_16x9;
        } else {
            // TODO: Log error and not show dialog.
            return null;
        }

        List<Size> sizes = mCameraCapabilities.getSupportedPhotoSizes();
        List<Size> pictureSizes = ResolutionUtil
                .getDisplayableSizesFromSupported(sizes, true);

        // This logic below finds the largest resolution for each aspect ratio.
        // TODO: Move this somewhere that can be shared with SettingsActivity
        int aspectRatio4x3Resolution = 0;
        int aspectRatio16x9Resolution = 0;
        Size largestSize4x3 = new Size(0, 0);
        Size largestSize16x9 = new Size(0, 0);
        for (Size size : pictureSizes) {
            float pictureAspectRatio = (float) size.width() / (float) size.height();
            pictureAspectRatio = pictureAspectRatio < 1 ?
                    1f / pictureAspectRatio : pictureAspectRatio;
            int resolution = size.width() * size.height();
            if (Math.abs(pictureAspectRatio - 4f / 3f) < 0.1f) {
                if (resolution > aspectRatio4x3Resolution) {
                    aspectRatio4x3Resolution = resolution;
                    largestSize4x3 = size;
                }
            } else if (Math.abs(pictureAspectRatio - 16f / 9f) < 0.1f) {
                if (resolution > aspectRatio16x9Resolution) {
                    aspectRatio16x9Resolution = resolution;
                    largestSize16x9 = size;
                }
            }
        }

        // Use the largest 4x3 and 16x9 sizes as candidates for picture size selection.
        final Size size4x3ToSelect = largestSize4x3;
        final Size size16x9ToSelect = largestSize16x9;

        AspectRatioDialogCallback callback = new AspectRatioDialogCallback() {

            @Override
            public AspectRatioSelector.AspectRatio getCurrentAspectRatio() {
                return currentAspectRatio;
            }

            @Override
            public void onAspectRatioSelected(AspectRatioSelector.AspectRatio newAspectRatio,
                    Runnable dialogHandlingFinishedRunnable) {
                if (newAspectRatio == AspectRatioSelector.AspectRatio.ASPECT_RATIO_4x3) {
                    String largestSize4x3Text = SettingsUtil.sizeToSetting(size4x3ToSelect);
                    mActivity.getSettingsManager().set(SettingsManager.SCOPE_GLOBAL,
                                                       Keys.KEY_PICTURE_SIZE_BACK,
                                                       largestSize4x3Text);
                } else if (newAspectRatio == AspectRatioSelector.AspectRatio.ASPECT_RATIO_16x9) {
                    String largestSize16x9Text = SettingsUtil.sizeToSetting(size16x9ToSelect);
                    mActivity.getSettingsManager().set(SettingsManager.SCOPE_GLOBAL,
                                                       Keys.KEY_PICTURE_SIZE_BACK,
                                                       largestSize16x9Text);
                }
                mActivity.getSettingsManager().set(SettingsManager.SCOPE_GLOBAL,
                                                   Keys.KEY_USER_SELECTED_ASPECT_RATIO, true);
                String aspectRatio = mActivity.getSettingsManager().getString(
                    SettingsManager.SCOPE_GLOBAL,
                    Keys.KEY_USER_SELECTED_ASPECT_RATIO);
                Log.e(TAG, "aspect ratio after setting it to true=" + aspectRatio);
                if (newAspectRatio != currentAspectRatio) {
                    Log.i(TAG, "changing aspect ratio from dialog");
                    stopPreview();
                    startPreview();
                    mUI.setRunnableForNextFrame(dialogHandlingFinishedRunnable);
                } else {
                    mHandler.post(dialogHandlingFinishedRunnable);
                }
            }
        };
        return callback;
    }

    @Override
    public void onPreviewUIReady() {
        Log.i(TAG, "onPreviewUIReady");
        startPreview();
    }

    @Override
    public void onPreviewUIDestroyed() {
        if (mCameraDevice == null) {
            return;
        }
        mCameraDevice.setPreviewTexture(null);
        stopPreview();
    }

    @Override
    public void startPreCaptureAnimation() {
        mAppController.startPreCaptureAnimation();
    }

    protected void onCameraOpened() {
        openCameraCommon();
        initializeControlByIntent();
    }


    protected void switchCamera() { // MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
        if (mPaused) {
            return;
        }

        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
        if (mAppController.validateFilterSelected(mAppController.getCurrentModuleIndex(),
                mCameraId != mPendingSwitchCameraId)) {
            return;
        }
        /* MODIFIED-END by jianying.zhang,BUG-3255060*/

        BoostUtil.getInstance().acquireCpuLock();

//        mAppController.getCameraAppUI().lockZoom(); // MODIFIED by xuan.zhou, 2016-05-23,BUG-2167404
        setCameraState(SWITCHING_CAMERA);
        cancelCountDown();

        if (!isOptimizeSwitchCamera)
            mAppController.freezeScreenUntilPreviewReady();
        SettingsManager settingsManager = mActivity.getSettingsManager();

        Log.i(TAG, "Start to switch camera. id=" + mPendingSwitchCameraId);
        closeCamera();
        mCameraId = mPendingSwitchCameraId;

        settingsManager.set(SettingsManager.SCOPE_GLOBAL, Keys.KEY_CAMERA_ID, mCameraId);

        requestCameraOpen();
        mUI.clearFaces();
        if (mFocusManager != null) {
            mUI.clearFocus();
            mFocusManager.removeMessages();
        }

        mMirror = isCameraFrontFacing();
        mFocusManager.setMirror(mMirror);
        // Start switch camera animation. Post a message because
        // onFrameAvailable from the old camera may already exist.
        if (isOptimizeSwitchCamera)
            mAppController.freezeScreenUntilPreviewReady();
        if (isCameraFrontFacing()) {
            showGestureGuideInfo();
        } else {
            /* MODIFIED-BEGIN by jianying.zhang, 2016-05-25,BUG-2202266*/
            if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
                hideInfo();
            }
        }
    }
    protected void hideInfo(){};
    protected void showGestureGuideInfo(){}
    /**
     * Uses the {@link CameraProvider} to open the currently-selected camera
     * device, using {@link GservicesHelper} to choose between API-1 and API-2.
     */
    protected void requestCameraOpen() { // MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
        Log.w(TAG, "requestCameraOpen " + mCameraId);
        mActivity.getCameraProvider().requestCamera(mCameraId,
                GservicesHelper.useCamera2ApiThroughPortabilityLayer(mActivity.getAndroidContext()));
    }

    private final BottomBar.SwitchButtonCallback mToggleCallback =
            new BottomBar.SwitchButtonCallback() {
                @Override
                public void onToggleStateChanged(int state) {
                    // At the time this callback is fired, the camera id
                    // has be set to the desired camera.

                    if (mPaused || mAppController.getCameraProvider().waitingForCamera()) {
                        return;
                    }
                    /* MODIFIED-BEGIN by shunyin.zhang, 2016-06-15,BUG-2307658*/
                    if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
                        if (mCameraState == SNAPSHOT_IN_PROGRESS) {
                            mAppController.getCameraAppUI().setSwitchBtnEnabled(true);
                            return;
                        }
                    }
                    /* MODIFIED-END by shunyin.zhang,BUG-2307658*/
                    // If switching to back camera, and HDR+ is still on,
                    // switch back to gcam, otherwise handle callback normally.
                    SettingsManager settingsManager = mActivity.getSettingsManager();
                    if (Keys.isCameraBackFacing(settingsManager,
                            SettingsManager.SCOPE_GLOBAL)) {
                        if (Keys.requestsReturnToHdrPlus(settingsManager,
                                mAppController.getModuleScope())) {
                            switchToGcamCapture();
                            return;
                        }
                    }

                    mPendingSwitchCameraId = state;

                    Log.d(TAG, "Start to switch camera. cameraId=" + state);
                    // We need to keep a preview frame for the animation before
                    // releasing the camera. This will trigger
                    // onPreviewTextureCopied.
                    // TODO: Need to animate the camera switch
                    switchCamera();
                }

            };
    private final ButtonManager.ButtonCallback mFlashCallback =
            new ButtonManager.ExtendButtonCallback() {
                @Override
                public void onStateChanged(int state) {
                    // Update flash parameters.
                }
                @Override
                public void onUnhandledClick() {
                    if (mZoomValue > mSuperZoomThreshold && isSuperResolutionEnabled()) {
                        /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
                        SnackbarToast.getSnackbarToast().showToast(mActivity,
                                mActivity.getString(R.string.flash_with_superzoom_toast)
                                ,SnackbarToast.LENGTH_LONG,SnackbarToast.DEFAULT_Y_OFFSET);
                                /* MODIFIED-END by bin-liu3,BUG-3253898*/
                    }
                    /* MODIFIED-BEGIN by fei.hui, 2016-09-09,BUG-2868515*/
//                    if (mActivity != null) {
//                        int batteryState = mActivity.getCurrentBatteryStatus();
//                        if (batteryState == CameraActivity.BATTERY_STATUS_WARNING
//                                || batteryState == CameraActivity.BATTERY_STATUS_LOW) {
//                            ToastUtil.showToast(mActivity,
//                                    mActivity.getString(R.string.battery_info_low_toast_message), Toast.LENGTH_LONG);
//                        }
//                    }
                    /* MODIFIED-END by fei.hui,BUG-2868515*/
                }

            };

    private Runnable mHideModeOptionsTipRunnable = new Runnable() {
        @Override
        public void run() {
            mUI.hideModeOptionsTip();
        }
    };

    private final ButtonManager.ButtonCallback mLowlightCallback =
            new ButtonManager.ButtonCallback() {
                @Override
                public void onStateChanged(int state) {
                    SettingsManager settingsManager = mAppController.getSettingsManager();
                    if (Keys.isLowlightOn(settingsManager, mAppController.getCameraScope()) && isNightToastShow()) {
                        if (CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_NIGHT_MODE_TOAST_ON, false)) {
                            mHandler.removeCallbacks(mHideModeOptionsTipRunnable);
                            mUI.showModeOptionsTip(mActivity.getResources().getString(R.string.night_mode_on_toast));
                            mHandler.postDelayed(mHideModeOptionsTipRunnable, MODE_OPTIONS_TIP_TIME);
                        }
                    }else {
                        mUI.hideModeOptionsTip();
                        mHandler.removeCallbacks(mHideModeOptionsTipRunnable);
                    }
                    //Cancel on-flight focus state
                    if(mCameraState==AE_AF_LOCKED||mCameraState==FOCUSING||mCameraState==SCANNING_FOR_AE_AF_LOCK){
                        setCameraState(IDLE);
                        mFocusManager.cancelAutoFocus();
                    }
                    updateVisidionMode();
                }

            };

    private boolean isHdrToastShow() {
        /*SettingsManager settingsManager = mActivity.getSettingsManager();
        if (settingsManager == null) {
            return false;
        }
        int launchingTimes = settingsManager.getInteger(SettingsManager.SCOPE_GLOBAL, Keys.KEY_NEW_LAUNCHING_TIMES_FOR_HDRTOAST);
        boolean isHdrToastShow = launchingTimes < MIN_CAMERA_LAUNCHING_TIMES ||
                (launchingTimes == MIN_CAMERA_LAUNCHING_TIMES && !Keys.isNewLaunchingForHdrtoast(settingsManager));

        if (isHdrToastShow) {
            if (Keys.isNewLaunchingForHdrtoast(settingsManager)) {
                // add launch times showing hdr toast
                settingsManager.setValueByIndex(SettingsManager.SCOPE_GLOBAL, Keys.KEY_NEW_LAUNCHING_TIMES_FOR_HDRTOAST, launchingTimes + 1);
                Keys.setNewLaunchingForHdrtoast(settingsManager, false);
            }
            return true;
        }*/
        return false;
    }

    private boolean isNightToastShow() {
        SettingsManager settingsManager = mActivity.getSettingsManager();
        if (settingsManager == null) {
            return false;
        }
        int launchingTimes = settingsManager.getInteger(SettingsManager.SCOPE_GLOBAL, Keys.KEY_NEW_LAUNCHING_TIMES_FOR_NIGHTTOAST);
        boolean isNightToastShow = launchingTimes < MIN_CAMERA_LAUNCHING_TIMES ||
                (launchingTimes == MIN_CAMERA_LAUNCHING_TIMES && !Keys.isNewLaunchingForNighttoast(settingsManager));

        if (isNightToastShow) {
            if (Keys.isNewLaunchingForNighttoast(settingsManager)) {
                // add launch times showing night toast
                settingsManager.setValueByIndex(SettingsManager.SCOPE_GLOBAL, Keys.KEY_NEW_LAUNCHING_TIMES_FOR_NIGHTTOAST, launchingTimes + 1);
                Keys.setNewLaunchingForNighttoast(settingsManager, false);
            }
            return true;
        }
        return false;
    }

    private final View.OnClickListener mCancelCallback = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onCaptureCancelled();
        }
    };

    private final View.OnClickListener mDoneCallback = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onCaptureDone();
        }
    };

    private final View.OnClickListener mRetakeCallback = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mActivity.getCameraAppUI().transitionToIntentCaptureLayout();
            onCaptureRetake();
        }
    };

    public void intentReviewCancel() {
        onCaptureCancelled();
    }

    public void intentReviewDone() {
        onCaptureDone();
    }

    public void intentReviewRetake() {
        mActivity.getCameraAppUI().transitionToIntentCaptureLayout();
        mAppController.getCameraAppUI().getTopMenus().setTopModeOptionVisibility(true);
        onCaptureRetake();
    }

    @Override
    public void hardResetSettings(SettingsManager settingsManager) {
        // PhotoModule should hard reset HDR+ to off,
        // and HDR to off if HDR+ is supported.
        settingsManager.set(SettingsManager.SCOPE_GLOBAL, Keys.KEY_CAMERA_HDR_PLUS, false);
        if (GcamHelper.hasGcamAsSeparateModule()) {
            settingsManager.set(SettingsManager.SCOPE_GLOBAL, Keys.KEY_CAMERA_HDR_AUTO, false);
        }
    }

    @Override
    public HardwareSpec getHardwareSpec() {
        return (mCameraSettings != null ?
                new HardwareSpecImpl(getCameraProvider(), mCameraCapabilities) : null);
    }

    @Override
    public CameraAppUI.BottomBarUISpec getBottomBarSpec() {
        CameraAppUI.BottomBarUISpec bottomBarSpec = new CameraAppUI.BottomBarUISpec();

        bottomBarSpec.enableCamera = true;
        bottomBarSpec.hideCamera = hideCamera();
        bottomBarSpec.hideCameraForced = hideCameraForced();
        bottomBarSpec.hideSetting = hideSetting();
        bottomBarSpec.showPose = isShowPose();
        bottomBarSpec.showFilter = showFilter();
        bottomBarSpec.showTimeIndicator = needCountDownIndicatorShow();
//        bottomBarSpec.cameraCallback = mCameraCallback;
        bottomBarSpec.switchButtonCallback = mToggleCallback;
        bottomBarSpec.enableFlash = mActivity.currentBatteryStatusOK() && (mZoomValue <= mSuperZoomThreshold || !isSuperResolutionEnabled());
        bottomBarSpec.flashCallback = mFlashCallback;
        bottomBarSpec.hideHdr = !isHdrShow();
        bottomBarSpec.enableHdr = true;
        bottomBarSpec.hdrCallback = null;
        bottomBarSpec.enableGridLines = isGridLinesEnabled();
        bottomBarSpec.hideGridLines=true;
        bottomBarSpec.hideLowlight = isLowLightShow() ? false : true;
        bottomBarSpec.lowlightCallback = mLowlightCallback;

        if (mCameraCapabilities != null) {
            bottomBarSpec.enableExposureCompensation = true;
            bottomBarSpec.exposureCompensationSetCallback =
                new CameraAppUI.BottomBarUISpec.ExposureCompensationSetCallback() {
                @Override
                public void setExposure(int value) {
                    setExposureCompensation(value);
                }
            };
            bottomBarSpec.minExposureCompensation =
                mCameraCapabilities.getMinExposureCompensation();
            bottomBarSpec.maxExposureCompensation =
                mCameraCapabilities.getMaxExposureCompensation();
            bottomBarSpec.exposureCompensationStep =
                mCameraCapabilities.getExposureCompensationStep();
        }

//        bottomBarSpec.enableSelfTimer = !isCameraFrontFacing();
        bottomBarSpec.showSelfTimer =bottomBarSpec.enableSelfTimer= isCountDownShow();//Adapt to TCT Camera Ergo 5.2.2.
        bottomBarSpec.hideFlash = !isFlashShow();
        if(isCameraFrontFacing()){
            ModuleController controller=mAppController.getCurrentModuleController();
            if(controller.getHardwareSpec()!=null/*Could happened in case of pause*/
                    &&!controller.getHardwareSpec().isFlashSupported()){
                bottomBarSpec.hideFlash=true;
            }
        }

        bottomBarSpec.hideContactsFlash = isContactsShow();
        bottomBarSpec.hideContactsBack = isContactsShow();

        if (isImageCaptureIntent()) {
            bottomBarSpec.showCancel = true;
            bottomBarSpec.cancelCallback = mCancelCallback;
            bottomBarSpec.showDone = true;
            bottomBarSpec.doneCallback = mDoneCallback;
            bottomBarSpec.showRetake = true;
            bottomBarSpec.retakeCallback = mRetakeCallback;
        }
        bottomBarSpec.showWrapperButton = isWrapperButtonShow();
        return bottomBarSpec;
    }

    protected boolean isGridLinesEnabled() {
        return true;
    }

    // either open a new camera or switch cameras
    private void openCameraCommon() {
        mUI.onCameraOpened(mCameraCapabilities, mCameraSettings);
        if (mIsImageCaptureIntent) {
            // Set hdr plus to default: off.
            SettingsManager settingsManager = mActivity.getSettingsManager();
            settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL,
                                         Keys.KEY_CAMERA_HDR_PLUS);
        }
        updateSceneMode();
    }

    @Override
    public void updatePreviewAspectRatio(float aspectRatio) {
        mAppController.updatePreviewAspectRatio(aspectRatio);
    }

    private void resetExposureCompensation() {
        SettingsManager settingsManager = mActivity.getSettingsManager();
        if (settingsManager == null) {
            Log.e(TAG, "Settings manager is null!");
            return;
        }
        settingsManager.setToDefault(mAppController.getCameraScope(),
                                     Keys.KEY_EXPOSURE);
    }

    // Snapshots can only be taken after this is called. It should be called
    // once only. We could have done these things in onCreate() but we want to
    // make preview screen appear as soon as possible.
    private void initializeFirstTime() {
        if (mFirstTimeInitialized || mPaused) {
            return;
        }

        mUI.initializeFirstTime();

        // We set the listener only when both service and shutterbutton
        // are initialized.
        getServices().getMemoryManager().addListener(this);
        /* MODIFIED-BEGIN by nie.lei, 2016-04-25,BUG-1945381*/
        //sync the status of low memory if mediasaver is saving.
        mUnderLowMemory = getServices().getMediaSaver().isQueueFull();
        Log.w(TAG,"initializeFirstTime mUnderLowMemory = " + mUnderLowMemory);
        /* MODIFIED-END by nie.lei,BUG-1945381*/
        mNamedImages = new NamedImages();

        mFirstTimeInitialized = true;
        addIdleHandler();

        mActivity.updateStorageSpaceAndHint(null);
    }

    // If the activity is paused and resumed, this method will be called in
    // onResume.
    private void initializeSecondTime() {
        getServices().getMemoryManager().addListener(this);
        /* MODIFIED-BEGIN by nie.lei, 2016-04-25,BUG-1945381*/
        //sync the status of low memory if mediasaver is saving.
        mUnderLowMemory = getServices().getMediaSaver().isQueueFull();
        Log.w(TAG, "initializeSecondTime mUnderLowMemory = " + mUnderLowMemory);
        /* MODIFIED-END by nie.lei,BUG-1945381*/
        mNamedImages = new NamedImages();
        mUI.initializeSecondTime(mCameraCapabilities, mCameraSettings);
    }

    private void addIdleHandler() {
        MessageQueue queue = Looper.myQueue();
        queue.addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                Storage.ensureOSXCompatible();
                return false;
            }
        });
    }

    protected boolean needFaceDetection() {
        return true;
    }

    @Override
    public void startFaceDetection() {
        if (mFaceDetectionStarted || mCameraDevice == null || !needFaceDetection()) {
            return;
        }
        if (mCameraCapabilities.getMaxNumOfFacesSupported() > 0) {
            mFaceDetectionStarted = true;
            mUI.setFaceDetectionStarted(mFaceDetectionStarted); // MODIFIED by jianying.zhang, 2016-11-17,BUG-3501961
            mUI.onStartFaceDetection(mDisplayOrientation, isCameraFrontFacing());
            mCameraDevice.setFaceDetectionCallback(mHandler, mUI);
            Log.w(TAG,"startFaceDetection");
            mCameraDevice.startFaceDetection();
            SessionStatsCollector.instance().faceScanActive(true);
        }
    }

    @Override
    public void stopFaceDetection() {
        if (!mFaceDetectionStarted || mCameraDevice == null) {
            return;
        }
        if (mCameraCapabilities.getMaxNumOfFacesSupported() > 0) {
            mFaceDetectionStarted = false;
            mUI.setFaceDetectionStarted(mFaceDetectionStarted); // MODIFIED by jianying.zhang, 2016-11-17,BUG-3501961
            mCameraDevice.setFaceDetectionCallback(null, null);
            Log.w(TAG, "stopFaceDetection");
            mCameraDevice.stopFaceDetection();
            mUI.clearFaces();
            mUI.resumeFocusFrame(mFaceDetectPauseKeyGenerator.hashCode()); //MODIFIED by sichao.hu, 2016-04-15,BUG-1951866
            SessionStatsCollector.instance().faceScanActive(false);
        }
    }

    private final class ShutterCallback
            implements CameraShutterCallback {

        private final boolean mNeedsAnimation;

        private boolean isFromLongshot=false;
        public ShutterCallback(boolean needsAnimation,boolean fromLongshot){
            this(needsAnimation);
            isFromLongshot=fromLongshot;
        }

        public ShutterCallback(boolean needsAnimation) {
            mNeedsAnimation = needsAnimation;
        }

        @Override
        public void onShutter(CameraProxy camera) {
            mShutterCallbackTime = System.currentTimeMillis();
            if(isFromLongshot){
                mNamedImages.nameNewImage(mShutterCallbackTime);
            }
            mShutterLag = mShutterCallbackTime - mCaptureStartTime;
            Log.v(TAG, "mShutterLag = " + mShutterLag + "ms");
            if (mNeedsAnimation) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        animateAfterShutter();
                    }
                });
            }
            if (isOptimizeCapture()) { // MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
                mAppController.setShutterEnabled(true);
            }
        }
    }


    private final class BurstShutterCallback
            implements CameraShutterCallback {

        @Override
        public void onShutter(CameraProxy camera) {
            Log.v(TAG, "burst onShutterCallback");
            if(mPaused||mCameraState==PREVIEW_STOPPED){
                return;
            }
            if(mCameraState!= SNAPSHOT_LONGSHOT_PENDING_START &&mCameraState!=SNAPSHOT_LONGSHOT){
                Log.w(TAG, "stop burst in shutter callback");
//                stopBurst();
                return;
            }
            mShutterCallbackTime = System.currentTimeMillis();
            mNamedImages.nameNewImage(mShutterCallbackTime);
            mShutterLag = mShutterCallbackTime - mCaptureStartTime;
            Log.v(TAG, "burst mShutterLag = " + mShutterLag + "ms");

            mCameraDevice.takePictureWithoutWaiting(mHandler,
                    BurstShutterCallback.this,
                    mRawPictureCallback, mPostViewPictureCallback,
                    mLongshotPictureTakenCallback);
        }
    }

    private final class PostViewPictureCallback
            implements CameraPictureCallback {
        @Override
        public void onPictureTaken(byte[] data, CameraProxy camera) {
            mPostViewPictureCallbackTime = System.currentTimeMillis();
            Log.v(TAG, "mShutterToPostViewCallonbackTime = "
                    + (mPostViewPictureCallbackTime - mShutterCallbackTime)
                    + "ms");
        }
    }

    private final class RawPictureCallback
            implements CameraPictureCallback {
        @Override
        public void onPictureTaken(byte[] rawData, CameraProxy camera) {
            mRawPictureCallbackTime = System.currentTimeMillis();
            Log.v(TAG, "mShutterToRawCallbackTime = "
                    + (mRawPictureCallbackTime - mShutterCallbackTime) + "ms");
        }
    }

    private static class ResizeBundle {
        byte[] jpegData;
        float targetAspectRatio;
        ExifInterface exif;
    }

    /**
     * @return Cropped image if the target aspect ratio is larger than the jpeg
     *         aspect ratio on the long axis. The original jpeg otherwise.
     */
    private ResizeBundle cropJpegDataToAspectRatio(ResizeBundle dataBundle) {

        final byte[] jpegData = dataBundle.jpegData;
        final ExifInterface exif = dataBundle.exif;
        float targetAspectRatio = dataBundle.targetAspectRatio;

        Bitmap original = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        int newWidth;
        int newHeight;

        if (originalWidth > originalHeight) {
            newHeight = (int) (originalWidth / targetAspectRatio);
            newWidth = originalWidth;
        } else {
            newWidth = (int) (originalHeight / targetAspectRatio);
            newHeight = originalHeight;
        }
        int xOffset = (originalWidth - newWidth)/2;
        int yOffset = (originalHeight - newHeight)/2;

        if (xOffset < 0 || yOffset < 0) {
            return dataBundle;
        }

        Bitmap resized = Bitmap.createBitmap(original,xOffset,yOffset,newWidth, newHeight);
        exif.setTagValue(ExifInterface.TAG_PIXEL_X_DIMENSION, new Integer(newWidth));
        exif.setTagValue(ExifInterface.TAG_PIXEL_Y_DIMENSION, new Integer(newHeight));

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        resized.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        dataBundle.jpegData = stream.toByteArray();
        return dataBundle;
    }


    protected class LongshotPictureCallback implements CameraPictureCallback{

        Location mLocation;
        private short mLongshotCount=0;
        public LongshotPictureCallback(Location loc) {
            mLocation = loc;
        }

        @Override
        public void onPictureTaken(byte[] originalJpegData, final CameraProxy camera) {
            Log.w(TAG,"OnPictureTaken in burst");
            if (mPaused) {
                return;
            }
            if(mCameraState!= SNAPSHOT_LONGSHOT_PENDING_START &&mCameraState!=SNAPSHOT_LONGSHOT){
                Log.w(TAG, "stop burst in picture taken");
                stopBurst();
                return;
            }
            // Do not take the picture if there is not enough storage.
            if (mActivity.getStorageSpaceBytes() <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
                Log.i(TAG, "Not enough space or storage not ready. remaining="
                        + mActivity.getStorageSpaceBytes());
                mVolumeButtonClickedFlag = false;
                stopBurst();
                return;
            }
            if(mCameraState== SNAPSHOT_LONGSHOT_PENDING_START){
                setCameraState(SNAPSHOT_LONGSHOT);
                boolean needOptimizeBurst = CustomUtil.getInstance()
                        .getBoolean(CustomFields.DEF_CAMERA_ENABLE_OPTIMEIZE_SNAPSHOT,false);
                if (!needOptimizeBurst) {
                    mAppController.getCameraAppUI().setModeStripViewVisibility(false);
                    setCaptureView(true);
                }
            }
            mReceivedBurstNum++;
            Log.v(TAG,"update burst count");
            mUI.updateBurstCount(mReceivedBurstNum, BURST_MAX);
            // check mSoundPlayer does not equal null.
            if (mSoundPlayer != null) {
                mSoundPlayer.play();
            }

            Log.w(TAG, "burst receiveNum is " + mReceivedBurstNum);
            final ExifInterface exif = Exif.getExif(originalJpegData);
            updateExifAndSave(exif,originalJpegData,camera);

            if(mReceivedBurstNum>=BURST_MAX){
                setCameraState(SNAPSHOT_LONGSHOT_PENDING_STOP);//need to make sure it's stopped and no later jpeg would get saved
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopBurst();
                    }
                },BURST_STOP_DELAY);//Give enough delay to make sure the last number is shown
                return;
            }
        }

        /* MODIFIED-BEGIN by yuanxing.tan, 2016-04-20,BUG-1972920*/
        protected void updateExifAndSave(NamedEntity namedEntity,ExifInterface exif,byte[] originalJpegData,final CameraProxy camera){
            final NamedEntity name = namedEntity;
            final Map<String,Object> externalBundle=new HashMap<>();
            externalBundle.put(ExternalExifInterface.BURST_SHOT_ID, LongshotPictureCallback.this.hashCode());
            externalBundle.put(ExternalExifInterface.BURST_SHOT_INDEX, mLongshotCount++);
            Log.w(TAG, "long shot taken for " + mLongshotCount);
            /* MODIFIED-END by yuanxing.tan,BUG-1972920*/
            if (mShouldResizeTo16x9) {
                final ResizeBundle dataBundle = new ResizeBundle();
                dataBundle.jpegData = originalJpegData;
                dataBundle.targetAspectRatio = ResolutionUtil.NEXUS_5_LARGE_16_BY_9_ASPECT_RATIO;
                dataBundle.exif = exif;

                new AsyncTask<ResizeBundle, Void, ResizeBundle>() {

                    @Override
                    protected ResizeBundle doInBackground(ResizeBundle... resizeBundles) {
                        return cropJpegDataToAspectRatio(resizeBundles[0]);
                    }

                    @Override
                    protected void onPostExecute(ResizeBundle result) {
                        saveFinalPhoto(result.jpegData, name, result.exif, camera,externalBundle);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataBundle);

            } else {
                saveFinalPhoto(originalJpegData, name, exif, camera, externalBundle);
            }
        }
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-04-20,BUG-1972920*/
        protected void updateExifAndSave(ExifInterface exif,byte[] originalJpegData,final CameraProxy camera){
            final NamedEntity name = mNamedImages.getNextNameEntity();
            updateExifAndSave(name, exif, originalJpegData, camera);
        }
        /* MODIFIED-END by yuanxing.tan,BUG-1972920*/

        protected final void saveFinalPhoto(final byte[] jpegData, NamedEntity name, final ExifInterface exif,
                CameraProxy camera) {
            saveFinalPhoto(jpegData,name,exif,camera,null);
        }

        protected final void saveFinalPhoto(final byte[] jpegData, NamedEntity name, final ExifInterface exif,
                CameraProxy camera,Map<String,Object> externalInfos) {
            int orientation = Exif.getOrientation(exif);

            float zoomValue = 1.0f;
            if (mCameraCapabilities.supports(CameraCapabilities.Feature.ZOOM)) {
                zoomValue = mCameraSettings.getCurrentZoomRatio();
            }
            boolean hdrOn = CameraCapabilities.SceneMode.HDR == mSceneMode;
            String flashSetting =
                    mActivity.getSettingsManager().getString(mAppController.getCameraScope(),
                                                             Keys.KEY_FLASH_MODE);
            boolean gridLinesOn = Keys.areGridLinesOn(mActivity.getSettingsManager());
            UsageStatistics.instance().photoCaptureDoneEvent(
                    eventprotos.NavigationChange.Mode.PHOTO_CAPTURE,
                    name.title + ".jpg", exif,
                    isCameraFrontFacing(), hdrOn, zoomValue, flashSetting, gridLinesOn,
                    (float) mTimerDuration, mShutterTouchCoordinate, mVolumeButtonClickedFlag);
            mShutterTouchCoordinate = null;
            mVolumeButtonClickedFlag = false;

            if (!mIsImageCaptureIntent) {
                // Calculate the width and the height of the jpeg.
                Integer exifWidth = exif.getTagIntValue(ExifInterface.TAG_PIXEL_X_DIMENSION);
                Integer exifHeight = exif.getTagIntValue(ExifInterface.TAG_PIXEL_Y_DIMENSION);
                int width, height;
                if (mShouldResizeTo16x9 && exifWidth != null && exifHeight != null) {
                    width = exifWidth;
                    height = exifHeight;
                } else {
                    Size s;
                    s = mCameraSettings.getCurrentPhotoSize();
                    if ((mJpegRotation + orientation) % 180 == 0) {
                        width = s.width();
                        height = s.height();
                    } else {
                        width = s.height();
                        height = s.width();
                    }
                }
                String title = (name == null) ? null : name.title;
                long date = (name == null) ? -1 : name.date;

                // Handle debug mode outputs
                if (mDebugUri != null) {
                    // If using a debug uri, save jpeg there.
                    saveToDebugUri(jpegData);

                    // Adjust the title of the debug image shown in mediastore.
                    if (title != null) {
                        title = DEBUG_IMAGE_PREFIX + title;
                    }
                }

                if (title == null) {
                    Log.e(TAG, "Unbalanced name/data pair");
                } else {
                    if (date == -1) {
                        date = mCaptureStartTime;
                    }
                    if (mHeading >= 0) {
                        // heading direction has been updated by the sensor.
                        ExifTag directionRefTag = exif.buildTag(
                                ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
                                ExifInterface.GpsTrackRef.MAGNETIC_DIRECTION);
                        ExifTag directionTag = exif.buildTag(
                                ExifInterface.TAG_GPS_IMG_DIRECTION,
                                new Rational(mHeading, 1));
                        exif.setTag(directionRefTag);
                        exif.setTag(directionTag);

                    }
                    if(externalInfos!=null){
                        String externalJson=CameraUtil.serializeToJson(externalInfos);
                        ExifTag externalTag=exif.buildTag(ExifInterface.TAG_USER_COMMENT, externalJson);
                        exif.setTag(externalTag);
                    }
                    getServices().getMediaSaver().addImage(
                            jpegData, title, date, mLocation, width, height,
                            orientation, exif, new BurstShotSaveListener(mLongshotCount), mContentResolver);
                }
                // Animate capture with real jpeg data instead of a preview
                // frame.
                //mUI.animateCapture(jpegData, orientation, mMirror);
            } else {
                mJpegImageData = jpegData;
                if (!mQuickCapture) {
                    Log.v(TAG, "showing UI");
                    mUI.showCapturedImageForReview(jpegData, orientation, mMirror);
                } else {
                    onCaptureDone();
                }
            }

            // Send the taken photo to remote shutter listeners, if any are
            // registered.
            getServices().getRemoteShutterListener().onPictureTaken(jpegData);

            // Check this in advance of each shot so we don't add to shutter
            // latency. It's true that someone else could write to the SD card
            // in the mean time and fill it, but that could have happened
            // between the shutter press and saving the JPEG too.
            mActivity.updateStorageSpaceAndHint(null);
        }

    }

    public int getCameraId() {
        return mCameraId;
    }

    private final class JpegPictureCallback
            implements CameraPictureCallback {
        Location mLocation;

        public JpegPictureCallback(Location loc) {
            mLocation = loc;
        }

        @Override
        public void onPictureTaken(final byte[] originalJpegData, final CameraProxy camera) {
            Log.w(TAG, "onPictureTaken, camera state is "+mCameraState);
            mAppController.setShutterEnabled(true);
            if (mPaused) {
                return;
            }
            //response to show recent tip when taking picture callback is called
            HelpTipsManager helpTipsManager = mAppController.getHelpTipsManager();
            if(helpTipsManager != null){
                helpTipsManager.onRecentTipResponse();
            }

            dismissOptimisingPhotoHint();
            if (mIsImageCaptureIntent) {
                stopPreview();
            }
            if (mSceneMode == CameraCapabilities.SceneMode.HDR) {
                mUI.setSwipingEnabled(true);
            }

            mJpegPictureCallbackTime = System.currentTimeMillis();
            // If postview callback has arrived, the captured image is displayed
            // in postview callback. If not, the captured image is displayed in
            // raw picture callback.
            if (mPostViewPictureCallbackTime != 0) {
                mShutterToPictureDisplayedTime =
                        mPostViewPictureCallbackTime - mShutterCallbackTime;
                mPictureDisplayedToJpegCallbackTime =
                        mJpegPictureCallbackTime - mPostViewPictureCallbackTime;
            } else {
                mShutterToPictureDisplayedTime =
                        mRawPictureCallbackTime - mShutterCallbackTime;
                mPictureDisplayedToJpegCallbackTime =
                        mJpegPictureCallbackTime - mRawPictureCallbackTime;
            }
            Log.v(TAG, "mPictureDisplayedToJpegCallbackTime = "
                    + mPictureDisplayedToJpegCallbackTime + "ms");

//            mFocusManager.updateFocusUI(); // Ensure focus indicator is hidden.
            boolean needRestart = false;
            if(mCameraSettings!=null&& !mCameraSettings.isZslOn){
                needRestart = true;
            }
            /* MODIFIED-BEGIN by yongsheng.shan, 2016-03-24,BUG-1861478 */
            if(ExtBuild.device() == ExtBuild.MTK_MT6755 && isFlashOnSnapshot){//It's software resolution for MTK, because MTK can't support Flash + ZSL
                isFlashOnSnapshot = false;
                needRestart = true;
                /* MODIFIED-END by yongsheng.shan,BUG-1861478 */
            }
            if(needRestart){
                setCameraState(PREVIEW_STOPPED);
                if (!mIsImageCaptureIntent) {
                    mUI.clearEvoPendingUI();
                    mUI.clearFocus();
                    mFocusManager.resetTouchFocus();
                    setupPreview();
                }
            }else{
                if (mCameraState == SNAPSHOT_IN_PROGRESS_DURING_LOCKED) {
                    if (mResetFocusAfterSnapshot) {
                        setCameraState(IDLE);
                        cancelAutoFocus();
                        mResetFocusAfterSnapshot = false;
                    } else {
                        setCameraState(AE_AF_LOCKED);
                    }
                    mAppController.getLockEventListener().onIdle();//Modestrip is former locked when state changed to snapshot_in_progress_during_locked , it's not supposed to be locked any more here .
                    mAppController.getCameraAppUI().showModeOptions();
                } else if (!mIsImageCaptureIntent) { // if it's capture intent, preview is stopped
                    setCameraState(IDLE);
                }
                if (mCameraState != AE_AF_LOCKED) {
                    mFocusManager.cancelAutoFocus();
                }
                mHandler.removeMessages(MSG_HIDE_GESTURE);
                mUI.hideGesture();
                //TODO: This is a workaround modification for PreviewFrame stopped issue , need to be remove after fixed
                if (null != mCameraDevice && !mIsImageCaptureIntent &&
                        isEnableGestureRecognization() && ExtBuild.device() != ExtBuild.MTK_MT6755) {

                    if (mGesturehandlerThread == null || !mGesturehandlerThread.isAlive()) {
                        Log.w(TAG, "GestureCore open looper , tray start thread");
                        mGesturehandlerThread = new GestureHandlerThread(
                                GESTURE_HANDLER_NAME);
                        mGesturehandlerThread.start();
                        mGestureHandler = new Handler(mGesturehandlerThread.getLooper());
                    }
                    mCameraDevice.setPreviewDataCallback(mGestureHandler, PhotoModule.this);
                    mCameraDevice.startPreview();
                }
                //TODO: This is a workaround modification for PreviewFrame stopped issue , need to be remove after fixed
            }

            long now = System.currentTimeMillis();
            mJpegCallbackFinishTime = now - mJpegPictureCallbackTime;
            Log.v(TAG, "mJpegCallbackFinishTime = " + mJpegCallbackFinishTime + "ms");
            mJpegPictureCallbackTime = 0;

            final ExifInterface exif = Exif.getExif(originalJpegData);

            final NamedEntity name = mNamedImages.getNextNameEntity();
            if (mShouldResizeTo16x9) {
                final ResizeBundle dataBundle = new ResizeBundle();
                dataBundle.jpegData = originalJpegData;
                dataBundle.targetAspectRatio = ResolutionUtil.NEXUS_5_LARGE_16_BY_9_ASPECT_RATIO;
                dataBundle.exif = exif;
                new AsyncTask<ResizeBundle, Void, ResizeBundle>() {

                    @Override
                    protected ResizeBundle doInBackground(ResizeBundle... resizeBundles) {
                        return cropJpegDataToAspectRatio(resizeBundles[0]);
                    }

                    @Override
                    protected void onPostExecute(ResizeBundle result) {
                        saveFinalPhoto(result.jpegData, name, result.exif, camera);
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, dataBundle);

            } else {
                saveFinalPhoto(originalJpegData, name, exif, camera);
            }

            if (isOptimizeCapture()) { // MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
                if(exif.hasThumbnail()) {
                    updateThumbnail(exif);
                }
            }

            /* MODIFIED-BEGIN by peixin, 2016-06-08,BUG-2281968*/
            if (ExtBuild.device() != ExtBuild.MTK_MT6755) {
                int mExposureTemp = mCameraSettings.getExposureCompensationIndex();
                if (mUI.getFocusUIVisibility() && EXPOSURE_TIME != mExposureTemp
                        // If it's exposure sidebar, no need to disable flash here.
                        && !isExposureSidebarEnabled()) {
                    mAppController.getCameraAppUI().getTopMenus()
                            .disableButton(TopMenus.BUTTON_FLASH);
                }
                /* MODIFIED-END by peixin,BUG-2281968*/

            }
        }


        void saveFinalPhoto(final byte[] jpegData, NamedEntity name, final ExifInterface exif,CameraProxy camera){
            saveFinalPhoto(jpegData,name,exif,camera,buildExternalBundle());
        }

        void saveFinalPhoto(final byte[] jpegData, final NamedEntity name, final ExifInterface exif,
                CameraProxy camera,final Map<String,Object> externalInfo) {
            final int orientation = Exif.getOrientation(exif);

            float zoomValue = 1.0f;
            if (mCameraCapabilities.supports(CameraCapabilities.Feature.ZOOM)) {
                zoomValue = mCameraSettings.getCurrentZoomRatio();
            }
            boolean hdrOn = CameraCapabilities.SceneMode.HDR == mSceneMode;
            String flashSetting =
                    mActivity.getSettingsManager().getString(mAppController.getCameraScope(),
                                                             Keys.KEY_FLASH_MODE);
            boolean gridLinesOn = Keys.areGridLinesOn(mActivity.getSettingsManager());
            UsageStatistics.instance().photoCaptureDoneEvent(
                    eventprotos.NavigationChange.Mode.PHOTO_CAPTURE,
                    name.title + ".jpg", exif,
                    isCameraFrontFacing(), hdrOn, zoomValue, flashSetting, gridLinesOn,
                    (float) mTimerDuration, mShutterTouchCoordinate, mVolumeButtonClickedFlag);
            mShutterTouchCoordinate = null;
            mVolumeButtonClickedFlag = false;

            if (!mIsImageCaptureIntent) {
                // Calculate the width and the height of the jpeg.
                Integer exifWidth = exif.getTagIntValue(ExifInterface.TAG_PIXEL_X_DIMENSION);
                Integer exifHeight = exif.getTagIntValue(ExifInterface.TAG_PIXEL_Y_DIMENSION);
                int width, height;
                if (mShouldResizeTo16x9 && exifWidth != null && exifHeight != null) {
                    width = exifWidth;
                    height = exifHeight;
                } else {
                    Size s;
                    s = mCameraSettings.getCurrentPhotoSize();
                    if ((mJpegRotation + orientation) % 180 == 0) {
                        width = s.width();
                        height = s.height();
                    } else {
                        width = s.height();
                        height = s.width();
                    }
                }
                String title = (name == null) ? null : name.title;
                long date = (name == null) ? -1 : name.date;

                // Handle debug mode outputs
                if (mDebugUri != null) {
                    // If using a debug uri, save jpeg there.
                    saveToDebugUri(jpegData);

                    // Adjust the title of the debug image shown in mediastore.
                    if (title != null) {
                        title = DEBUG_IMAGE_PREFIX + title;
                    }
                }

                if (title == null) {
                    Log.e(TAG, "Unbalanced name/data pair");
                } else {
                    if (date == -1) {
                        date = mCaptureStartTime;
                    }
                    if (mHeading >= 0) {
                        // heading direction has been updated by the sensor.
                        ExifTag directionRefTag = exif.buildTag(
                                ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
                                ExifInterface.GpsTrackRef.MAGNETIC_DIRECTION);
                        ExifTag directionTag = exif.buildTag(
                                ExifInterface.TAG_GPS_IMG_DIRECTION,
                                new Rational(mHeading, 1));
                        exif.setTag(directionRefTag);
                        exif.setTag(directionTag);

                    }
                    if(externalInfo!=null){
                        String externalJson=CameraUtil.serializeToJson(externalInfo);
                        ExifTag userTag=exif.buildTag(ExifInterface.TAG_USER_COMMENT, externalJson);
                        exif.setTag(userTag);
                    }
                    getServices().getMediaSaver().addImage(
                            jpegData, title, date, mLocation, width, height,
                            orientation, exif, mOnMediaSavedListener, mContentResolver);
                }

                // Animate capture with real jpeg data instead of a preview
                // frame.
                //mUI.animateCapture(jpegData, orientation, mMirror);
            } else {
                mAppController.getCameraAppUI().getTopMenus().setTopModeOptionVisibility(false);
                mJpegImageData = jpegData;
                mUI.disableZoom();
                if (!mQuickCapture) {
                    Log.v(TAG, "showing UI");
                    mUI.showCapturedImageForReview(jpegData, orientation, false);
                    mIsInIntentReviewUI = true;
                } else {
                    onCaptureDone();
                }
            }

            // Send the taken photo to remote shutter listeners, if any are
            // registered.
            getServices().getRemoteShutterListener().onPictureTaken(jpegData);

            // Check this in advance of each shot so we don't add to shutter
            // latency. It's true that someone else could write to the SD card
            // in the mean time and fill it, but that could have happened
            // between the shutter press and saving the JPEG too.
            mActivity.updateStorageSpaceAndHint(null);
        }
    }

    public Map<String,Object> buildExternalBundle(){
        Map<String,Object> externalInfo=new HashMap<>();
        List<String> faces=CameraUtil.getCompensatedFaceRects(mFaces);
        externalInfo.put(ExternalExifInterface.FACE_RECTS,faces);
        if(faces.size()>0&&mCameraId!=0) {//Front camera
            externalInfo.put(ExternalExifInterface.MODULE_NAME, ExternalExifInterface.FACE_SHOW_TAG);
        }else {
            String moduleString = getModuleStringIdentifier();
            if (moduleString.equals(TS_PanoramaGPModule.TSPANORAMAGP_MODULE_STRING_ID)) {
                externalInfo.put(ExternalExifInterface.MODULE_NAME, ExternalExifInterface.PANORAMA_TAG);
            } else if (moduleString.equals(CylindricalPanoramaModule.CYL_PANO_MODULE_STRING_ID)) {
                externalInfo.put(ExternalExifInterface.MODULE_NAME, ExternalExifInterface.PHOTO_360);
            } else {
                externalInfo.put(ExternalExifInterface.MODULE_NAME, getModuleStringIdentifier());
            }
        }
        return externalInfo;
    }

    protected void updateThumbnail(final ExifInterface exif) {
        if(exif.hasThumbnail()) {
            mActivity.getCameraAppUI().updatePeekThumbUri(null);//clear pending uri ,  to disable thumbnail click
            mActivity.getCameraAppUI().updatePeekThumbBitmapWithAnimation(exif.getThumbnailBitmap());
//            mActivity.playPeekAnimation(exif.getThumbnailBitmap(),this.getPeekAccessibilityString());
        }
    }

    @Override
    public int getModuleId() {
        return mAppController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_photo);
    }

    protected boolean needEnableExposureAdjustment(){
        if(CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_ENABLE_COMPENSATION_OTHER_THAN_AUTO,true)){
            return true;
        }
        boolean needEnableExposureCompensation=(mSceneMode==CameraCapabilities.SceneMode.AUTO &&
                (!Keys.isLowlightOn(mAppController.getSettingsManager(), mAppController.getCameraScope())));
        return needEnableExposureCompensation;
    }

    private final class AutoFocusCallback implements CameraAFCallback {
        @Override
        public void onAutoFocus(boolean focused, CameraProxy camera) {
            SessionStatsCollector.instance().autofocusResult(focused);
            if (mPaused||isInBurstshot()) {
                return;
            }

            mAutoFocusTime = System.currentTimeMillis() - mFocusStartTime;
            Log.v(TAG, "mAutoFocusTime = " + mAutoFocusTime + "ms   focused = " + focused);
//            if (mCameraState == SCANING_FOR_AE_AF_LOCK) {
//                mFocusManager.onAutoFocus(focused, false, false);
//                setCameraState(AE_AF_LOCKED);
//                mUI.resetEvoSlider(0);
//            } else {

            // If focus success and it's metering, lock ae util metering circle dragging, and if
            // it's exposure adjustment, lock ae after sidebar progress changed. But if focus fail,
            // hide metering ui and fade out exposure side bar.
            if (focused) {
                if (mPreivewLongPressed) {
                    setCameraState(AE_AF_LOCKED);
                }
                if (mUI.isMeteringShowing()) {
                    mUI.enableMetering();
                }
            } else if (mUI.isMeteringShowing()) {
                mUI.hideMeteringUI();
            }
            mPreivewLongPressed = false;

            if (needEnableExposureAdjustment() && isExposureSidebarEnabled() &&
                    mFocusManager != null && mFocusManager.getFocusAreas() != null) {
                if (focused) {
                    mUI.setExposureSidebarPrepared(true);
                    // state may set to AE_AF_LOCKED for metering.
                    if (mCameraState == FOCUSING) {
                        setCameraState(SCANNING_FOR_AE_AF_LOCK);
                    }
                } else {
                    if (mHandler != null) {
                        mHandler.postDelayed(hideExposureSidebar,
                                FocusOverlayManager.RESET_TOUCH_FOCUS_FAILED_DELAY_MILLIS);
                    }
                    setCameraState(IDLE);
                }
            } else if (needEnableExposureAdjustment() && !isExposureSidebarEnabled() && focused &&
                    mFocusManager != null && mFocusManager.getFocusAreas() != null) {
                Log.v(TAG, "focus succeed , show exposure slider");
                if(mCameraState==FOCUSING) {
                    mUI.showEvoSlider();
                }
                setCameraState(SCANNING_FOR_AE_AF_LOCK);
            } else {
                Log.v(TAG,"focus failed , set camera state back to IDLE");
                setCameraState(IDLE);
            }
            int action = FocusOverlayManager.ACTION_RESTORE_CAF_LATER;
            if (needEnableExposureAdjustment()) {
                action |= FocusOverlayManager.ACTION_KEEP_FOCUS_FRAME;
            }
            mFocusManager.onAutoFocus(focused, action);

            if (mCameraState == AE_AF_LOCKED) {
                startMotionChecking();
                // If AE_AF_LOCKED for metering, lock ae and keep focus frame after onAutoFocus.
                mFocusManager.setAeAwbLock(true);
                mFocusManager.keepFocusFrame();
                setCameraParameters(UPDATE_PARAM_PREFERENCE);
            }
//            }
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private final class AutoFocusMoveCallback
            implements CameraAFMoveCallback {
        @Override
        public void onAutoFocusMoving(
                boolean moving, CameraProxy camera) {
            if(mCameraState==SWITCHING_CAMERA){
                return;
            }
            if (isExposureSidebarEnabled()) {
                // Do nothing here now, I believe no moving event received when AE_AF_LOCKED.
            } else {
                mUI.clearEvoPendingUI();
            }
            if(!mUI.hasFaces()){
                mUI.resumeFocusFrame(mFaceDetectPauseKeyGenerator.hashCode()); // MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
            }
            /*MODIFIED-END by sichao.hu,BUG-1951866*/
            mFocusManager.onAutoFocusMoving(moving);
            SessionStatsCollector.instance().autofocusMoving(moving);
        }
    }


    /**
     * This class is just a thread-safe queue for name,date holder objects.
     */
    public static class NamedImages {
        private final Vector<NamedEntity> mQueue;

        public NamedImages() {
            mQueue = new Vector<NamedEntity>();
        }

        public void nameNewImage(long date) {
            NamedEntity r = new NamedEntity();
            r.title = CameraUtil.createJpegName(date);
            r.date = date;
            mQueue.add(r);
        }

        public NamedEntity getNextNameEntity() {
            synchronized (mQueue) {
                if (!mQueue.isEmpty()) {
                    return mQueue.remove(0);
                }
            }
            // TODO: new NamedEntity to avoid crash
            NamedEntity r = new NamedEntity();
            long date = System.currentTimeMillis();
            r.title = CameraUtil.createJpegName(date);
            r.date = date;
            return r;
        }

        public static class NamedEntity {
            public String title;
            public long date;
        }
    }


    protected void setCameraState(int state) {
        Log.w(TAG,String.format("set camera State: %d",state));
        mCameraState = state;
        switch (state) {
            case SCANNING_FOR_AE_AF_LOCK:
            case AE_AF_LOCKED:
                break;
            case PREVIEW_STOPPED:
                break;
            case SWITCHING_CAMERA:
                mAppController.getLockEventListener().onSwitching();
                break;
            case SNAPSHOT_IN_PROGRESS:
            case SNAPSHOT_IN_PROGRESS_DURING_LOCKED:
            case SNAPSHOT_LONGSHOT_PENDING_START:
            case SNAPSHOT_LONGSHOT:
            case SNAPSHOT_LONGSHOT_PENDING_STOP:
//                mAppController.lockPool(mAppController.getCameraAppUI().getShutterHash());
                mAppController.getLockEventListener().onShutter();
                //TODO:Disable UI Swiping
                mAppController.getCameraAppUI().hideModeOptions();
                break;
            case PhotoController.IDLE:
                mAppController.getLockEventListener().onIdle();
                // It happens when user pause and resume activity in image capture review layout when zsl on.
                if (!mIsInIntentReviewUI && !mUI.isCountingDown()) {
                    mAppController.getCameraAppUI().showModeOptions();
                }
                break;
        }
    }

    private void animateAfterShutter() {
        // Only animate when in full screen capture mode
        // i.e. If monkey/a user swipes to the gallery during picture taking,
        // don't show animation
        if (!mIsImageCaptureIntent) {
            Log.v(TAG,"show preCapture animation");
            mUI.animateFlash();
        }
    }

    public int getJpegRotation(boolean isNeedMirrorSelfie) {
        int orientation = mActivity.isAutoRotateScreen() ? mDisplayRotation : mOrientation;
        Characteristics info = mActivity.getCameraProvider().getCharacteristics(mCameraId);
        mJpegRotation = info.getRelativeImageOrientation(orientation, isNeedMirrorSelfie);
        return mJpegRotation;
    }

    private void setJpegRotation(boolean isNeedMirrorSelfie){
        int orientation = mActivity.isAutoRotateScreen() ? mDisplayRotation : mOrientation;
        Characteristics info = mActivity.getCameraProvider().getCharacteristics(mCameraId);
        mJpegRotation = info.getRelativeImageOrientation(orientation, isNeedMirrorSelfie);
        mCameraDevice.setJpegOrientation(mJpegRotation);
        Log.v(TAG, "capture orientation (screen:device:used:jpeg) " +
                mDisplayRotation + ":" + mOrientation + ":" +
                orientation + ":" + mJpegRotation);
    }

    protected boolean isNeedMirrorSelfie(){
        return false;
    }

    protected void updateFrontPhotoFlipMode(){
    }

    private ShutterCallback mLongshotShutterCallback=new ShutterCallback(false,true);

    protected LongshotPictureCallback mLongshotPictureTakenCallback;
    private ContinueShot snapShot;

    private ArrayList<RectF> mFaces;
    @Override
    public boolean capture() {
        Log.w(TAG, "capture");
        // If we are already in the middle of taking a snapshot or the image
        // save request is full then ignore.
        if (mCameraDevice == null || mCameraState == SNAPSHOT_IN_PROGRESS||mCameraState==SNAPSHOT_IN_PROGRESS_DURING_LOCKED
                || mCameraState == SWITCHING_CAMERA) {
            return false;
        }


        if(mCameraState==AE_AF_LOCKED){
            setCameraState(SNAPSHOT_IN_PROGRESS_DURING_LOCKED);
        }else {
            if(mCameraState!= SNAPSHOT_LONGSHOT_PENDING_START) {
                setCameraState(SNAPSHOT_IN_PROGRESS);
            }
        }

        mCaptureStartTime = System.currentTimeMillis();

        mPostViewPictureCallbackTime = 0;
        mJpegImageData = null;

        final boolean animateBefore = (mSceneMode == CameraCapabilities.SceneMode.HDR) || mAutoHdrEnable ; // MODIFIED by xuyang.liu, 2016-10-13,BUG-3110198

        if (animateBefore) {
            animateAfterShutter();
        }

        if(ExtBuild.device() == ExtBuild.MTK_MT6755){
		 /* MODIFIED-BEGIN by yuanxing.tan, 2016-05-11,BUG-2127821*/
		 // Add MTK Camera AIS function, close this feature temporarily
//	        mCameraSettings.setCameraAisEnabled(Keys.isCameraAisOn(mAppController.getSettingsManager()));
/* MODIFIED-END by yuanxing.tan,BUG-2127821*/
        }

        final Location loc = mActivity.getLocationManager().getCurrentLocation();
        CameraUtil.setGpsParameters(mCameraSettings, loc);

        // Set JPEG orientation. Even if screen UI is locked in portrait, camera orientation should
        // still match device orientation (e.g., users should always get landscape photos while
        // capturing by putting device in landscape.)
        setJpegRotation(isNeedMirrorSelfie());

        if(mCameraState== SNAPSHOT_LONGSHOT_PENDING_START){
            mLongshotPictureTakenCallback=new LongshotPictureCallback(loc);
            mReceivedBurstNum=0;
            mCameraDevice.enableShutterSound(false);
            if (mCameraSettings.getCurrentFlashMode() == CameraCapabilities.FlashMode.ON
                    && mCameraCapabilities.supports(CameraCapabilities.FlashMode.TORCH)) {
                mCameraSettings.setFlashMode(CameraCapabilities.FlashMode.TORCH);
            } else {
                mCameraSettings.setFlashMode(CameraCapabilities.FlashMode.OFF);// MODIFIED by sichao.hu, 2016-03-21, BUG-1779801
            }
            if(takeOptimizedBurstShot(loc)){//In optimized burst shot
                Log.w(TAG, "burst shot started");
                return true;
            }

            mCameraDevice.applySettings(mCameraSettings);

            if(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL!=mAppController.getSupportedHardwarelevel(mCameraId)){
                mCameraDevice.burstShot(null, null, null, null, null);
                mCameraDevice.takePicture(mHandler,
                        new BurstShutterCallback(),
                        mRawPictureCallback, mPostViewPictureCallback,
                        mLongshotPictureTakenCallback);
                mNamedImages.nameNewImage(mCaptureStartTime);
                Log.w(TAG, "burst shot started");
                return true;
            }
            mHandler.sendEmptyMessage(MSG_CAPTURE_BURST);
        }else{
            mCameraDevice.enableShutterSound(Keys.isShutterSoundOn(mAppController.getSettingsManager()));
            Log.w(TAG,"takePictures");
            mCameraDevice.applySettings(mCameraSettings);
            /* MODIFIED-BEGIN by yongsheng.shan, 2016-03-24,BUG-1861478 */
            if(mCameraSettings.getCurrentFlashMode()!= CameraCapabilities.FlashMode.OFF){
                isFlashOnSnapshot = true;
            }
            /* MODIFIED-END by yongsheng.shan,BUG-1861478 */
            mFaces = mUI.filterAndAdjustFaces(isNeedMirrorSelfie(), mJpegRotation);
            if (!mActivity.isPhotoContactsIntent()) {
                showOptimisingPhotoHint();
            }
            mCameraDevice.takePicture(mHandler,
                    new ShutterCallback(!animateBefore),
                    mRawPictureCallback, mPostViewPictureCallback,
                    getJpegPictureCallback(),getFilterId());//new JpegPictureCallback(loc)); // MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
                    mNamedImages.nameNewImage(mCaptureStartTime);
        }

        return true;
    }


    /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
    protected int getFilterId(){
        return CameraAgent.INDEX_NONE_FILTER;
    }
    /* MODIFIED-END by sichao.hu,BUG-2743263*/

    protected boolean takeOptimizedBurstShot(Location loc){
        stopFaceDetection();
        mSoundPlayer = new SoundPlay(Keys.isShutterSoundOn(mAppController.getSettingsManager()));
        mSoundPlayer.load();
        return false;
    }

    public  CameraPictureCallback getJpegPictureCallback(){
        Location loc = mActivity.getLocationManager().getCurrentLocation();
        return new JpegPictureCallback(loc);
    }

    @Override
    public void setFocusParameters() {
        setCameraParameters(UPDATE_PARAM_PREFERENCE);
    }

    private void updateSceneMode() {
        // If scene mode is set, we cannot set flash mode, white balance, and
        // focus mode, instead, we read it from driver
        if (CameraCapabilities.SceneMode.AUTO != mSceneMode) {
            overrideCameraSettings(mCameraSettings.getCurrentFlashMode(),
                    mFocusManager.getFocusMode(mCameraSettings.getCurrentFocusMode()));
        }
    }

    private void overrideCameraSettings(CameraCapabilities.FlashMode flashMode,
            CameraCapabilities.FocusMode focusMode) {
        CameraCapabilities.Stringifier stringifier = mCameraCapabilities.getStringifier();
        SettingsManager settingsManager = mAppController.getSettingsManager();
        if (!CameraCapabilities.FlashMode.NO_FLASH.equals(flashMode)) {
            if (Keys.isHdrOn(settingsManager,mActivity) || Keys.isLowlightOn(settingsManager, mAppController.getCameraScope())) {
                settingsManager.set(mAppController.getCameraScope(), Keys.KEY_FLASH_MODE,
                        stringifier.stringify(CameraCapabilities.FlashMode.OFF));
            } else {
                if (Keys.isHdrAuto(settingsManager, mActivity) && CameraCapabilities.FlashMode.ON.equals(flashMode)) {
                    settingsManager.set(mAppController.getCameraScope(), Keys.KEY_FLASH_MODE,
                            stringifier.stringify(CameraCapabilities.FlashMode.AUTO));
                } else {
                    if (isFlashShow()) {
                        settingsManager.set(mAppController.getCameraScope(), Keys.KEY_FLASH_MODE,
                                stringifier.stringify(flashMode));
                    }
                }
            }
        }
        if(focusMode!=null) {
            Log.v(TAG, "override focus mode for " + focusMode.name());
        }
        settingsManager.set(mAppController.getCameraScope(), Keys.KEY_FOCUS_MODE,
                stringifier.stringify(focusMode));
    }

    /* MODIFIED-BEGIN by peixin, 2016-04-26,BUG-2000931*/
    int curRotation = OrientationEventListener.ORIENTATION_UNKNOWN;
    int curCameraId  = 0;
    /* MODIFIED-END by peixin,BUG-2000931*/
    @Override
    public void onOrientationChanged(int orientation) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return;
        }
        int newOrientation = CameraUtil.roundOrientation(orientation, mOrientation);
        mUI.onOrientationChanged(newOrientation);
        // TODO: Document orientation compute logic and unify them in OrientationManagerImpl.
        // b/17443789
        // Flip to counter-clockwise orientation.
        mOrientation = (360 - orientation) % 360;

        mUI.setPostGestureOrientation((mSensorOrientation + mOrientation) % 360);
        mUI.setGestureDisplayOrientation(mOrientation);

        /* MODIFIED-BEGIN by peixin, 2016-04-26,BUG-2000931*/
        if (ExtBuild.device() == ExtBuild.MTK_MT6755 && mCameraDevice != null
                && mCameraSettings != null) {
            Log.w(TAG, "mCameraId = " + mCameraId);

            orientation = getJpegRotation(mCameraId, orientation);
            if (mCameraSettings.getRotation() != orientation || curCameraId != mCameraId) { // MODIFIED by peixin, 2016-04-28,BUG-2000931
                curRotation = orientation;
                curCameraId = mCameraId;

                mCameraSettings.setRotation(curRotation);
                mCameraDevice.applySettings(mCameraSettings);
            }

        } else {
            Log.e(TAG, "CameraDevice == null,/ mCameraSettings == nul;  Can't set Parameter.setRotation");
            /* MODIFIED-END by peixin,BUG-2000931*/
        }
    }

    public static int getJpegRotation(int cameraId, int orientation) {
        // See android.hardware.Camera.Parameters.setRotation for
        // documentation.
        int rotation = 0;
        Camera.CameraInfo info =  new Camera.CameraInfo();
	    android.hardware.Camera.getCameraInfo(cameraId, info);
        if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - orientation + 360) % 360;
            } else { // back-facing camera
                rotation = (info.orientation + orientation) % 360;
            }
        } else {
            // Get the right original orientation
            rotation = info.orientation;
        }
        return rotation;
    }

    @Override
    public void onCameraAvailable(CameraProxy cameraProxy) {
        Log.i(TAG, "onCameraAvailable");
        if (mPaused) {
            return;
        }
        mCameraDevice = cameraProxy;
        mCameraDevice.initExtCamera(mActivity);

        initializeCapabilities();

        // Reset zoom value index.
        mZoomValue = 1.0f;
        if (mFocusManager == null) {
            initializeFocusManager();
        }
        mFocusManager.updateCapabilities(mCameraCapabilities);
        // Do camera parameter dependent initialization.
        mCameraSettings = mCameraDevice.getSettings();
        if(mCameraSettings==null){
            Log.e(TAG,"camera setting is null");
        }

        readCameraInitialParameters();

        //Would be done in startPreview() , not necessary here
//        if(!isOptimizeSwitchCamera)
//            setCameraParameters(UPDATE_PARAM_ALL);
        // Set a listener which updates camera parameters based
        // on changed settings.
        SettingsManager settingsManager = mActivity.getSettingsManager();
        settingsManager.addListener(this);
        mCameraPreviewParamsReady = true;
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
        readPreferences();
        startPreview();

        onCameraOpened();
    }

    protected void readPreferences() {
    }
    /* MODIFIED-END by jianying.zhang,BUG-3255060*/
    /* MODIFIED-BEGIN by xuan.zhou, 2016-06-06,BUG-2251935*/
    private boolean mSpecificUIReady = false;
    @Override
    public void onSpecificUIApplied() {
        if (!mSpecificUIReady) {
            enableFlashButtonClick();
        }
        mSpecificUIReady = true;
    }

    private void disableFlashButtonClick() {
        if (mAppController == null) {
            return;
        }

        // Disable the flash button click reactions before the module ui specific applied to avoid
        // the images overridden during the combine animation. I don't want to affect the visual
        // state nor conflict with the logic of evo/zoom, so I'd like to use disableButtonClick
        // rather than disableButton here.
        mAppController.getCameraAppUI().getTopMenus().disableButtonClick(
                TopMenus.BUTTON_FLASH);
    }

    private void enableFlashButtonClick() {
        if (mAppController == null) {
            return;
        }
        /* MODIFIED-BEGIN by fei.hui, 2016-10-27,BUG-3201458*/
        if(mActivity.isBatteryWarningOrLow()){
            return;
        }
        /* MODIFIED-END by fei.hui,BUG-3201458*/
        mAppController.getCameraAppUI().getTopMenus().enableButtonClick(
                TopMenus.BUTTON_FLASH);
    }
    /* MODIFIED-END by xuan.zhou,BUG-2251935*/

    @Override
    public void onCaptureCancelled() {
        mActivity.setResultEx(Activity.RESULT_CANCELED, new Intent());
        mActivity.finish();
    }

    @Override
    public void onCaptureRetake() {
        Log.i(TAG, "onCaptureRetake");
        if (mPaused) {
            return;
        }
        mIsInIntentReviewUI = false;
        mUI.hidePostCaptureAlert();
        mUI.hideIntentReviewImageView();
        mUI.clearReviewImage();
        mJpegImageData = null;
        setupPreview();
    }

    @Override
    public void onCaptureDone() {
        Log.i(TAG, "onCaptureDone");
        if (mPaused) {
            return;
        }

        byte[] data = mJpegImageData;

        if (mCropValue == null) {
            // First handle the no crop case -- just return the value. If the
            // caller specifies a "save uri" then write the data to its
            // stream. Otherwise, pass back a scaled down version of the bitmap
            // directly in the extras.
            if (mSaveUri != null) {
                OutputStream outputStream = null;
                try {
                    outputStream = mContentResolver.openOutputStream(mSaveUri);
                    outputStream.write(data);
                    outputStream.close();

                    Log.v(TAG, "saved result to URI: " + mSaveUri);
                    mActivity.setResultEx(Activity.RESULT_OK);
                    mActivity.finish();
                } catch (IOException ex) {
                    Log.w(TAG, "exception saving result to URI: " + mSaveUri, ex);
                    // ignore exception
                } finally {
                    CameraUtil.closeSilently(outputStream);
                }
            } else {
                ExifInterface exif = Exif.getExif(data);
                int orientation = Exif.getOrientation(exif);
                Bitmap bitmap = CameraUtil.makeBitmap(data, 50 * 1024);
                bitmap = CameraUtil.rotate(bitmap, orientation);
                Log.v(TAG, "inlined bitmap into capture intent result");
                mActivity.setResultEx(Activity.RESULT_OK,
                        new Intent("inline-data").putExtra("data", bitmap));
                mActivity.finish();
            }
        } else {
            // Save the image to a temp file and invoke the cropper
            Uri tempUri = null;
            FileOutputStream tempStream = null;
            try {
                File path = mActivity.getFileStreamPath(sTempCropFilename);
                path.delete();
                tempStream = mActivity.openFileOutput(sTempCropFilename, 0);
                tempStream.write(data);
                tempStream.close();
                tempUri = Uri.fromFile(path);
                Log.v(TAG, "wrote temp file for cropping to: " + sTempCropFilename);
            } catch (FileNotFoundException ex) {
                Log.w(TAG, "error writing temp cropping file to: " + sTempCropFilename, ex);
                mActivity.setResultEx(Activity.RESULT_CANCELED);
                mActivity.finish();
                return;
            } catch (IOException ex) {
                Log.w(TAG, "error writing temp cropping file to: " + sTempCropFilename, ex);
                mActivity.setResultEx(Activity.RESULT_CANCELED);
                mActivity.finish();
                return;
            } finally {
                CameraUtil.closeSilently(tempStream);
            }

            Bundle newExtras = new Bundle();
            if (mCropValue.equals("circle")) {
                newExtras.putString("circleCrop", "true");
            }
            if (mSaveUri != null) {
                Log.v(TAG, "setting output of cropped file to: " + mSaveUri);
                newExtras.putParcelable(MediaStore.EXTRA_OUTPUT, mSaveUri);
            } else {
                newExtras.putBoolean(CameraUtil.KEY_RETURN_DATA, true);
            }
            if (mActivity.isSecureCamera()) {
                newExtras.putBoolean(CameraUtil.KEY_SHOW_WHEN_LOCKED, true);
            }

            // TODO: Share this constant.
            final String CROP_ACTION = "com.android.camera.action.CROP";
            Intent cropIntent = new Intent(CROP_ACTION);

            cropIntent.setData(tempUri);
            cropIntent.putExtras(newExtras);
            Log.v(TAG, "starting CROP intent for capture");
            mActivity.startActivityForResult(cropIntent, REQUEST_CROP);
        }
    }

    @Override
    public void onShutterCoordinate(TouchCoordinate coord) {
        mShutterTouchCoordinate = coord;
    }

    @Override
    public void onShutterButtonFocus(boolean pressed) {
        Log.w(TAG, "ShutterButtonFocus ,pressed=" + pressed);
        if(!pressed) {
            if (isOptimeizeSnapshot) {
                if (mCameraState == SNAPSHOT_LONGSHOT_PENDING_START || mCameraState == SNAPSHOT_LONGSHOT) {
                    Log.w(TAG, "ShutterButtonFocus ,bustShot.close()");
                    snapShot.stop();
                }

            } else {
                if (mCameraState == SNAPSHOT_LONGSHOT_PENDING_START || mCameraState == SNAPSHOT_LONGSHOT) {
                    setCameraState(SNAPSHOT_LONGSHOT_PENDING_STOP);
                }
            }
            unloadSoundPlayer();
        }
    }

    protected void unloadSoundPlayer(){
        if (mSoundPlayer != null) {
            mSoundPlayer.unLoad();
            mSoundPlayer = null;
        }
    }

    protected void stopBurst(){
        Log.w(TAG, "stop burst shot ,camera state is " + mCameraState);
        while(true) {
            mAppController.setShutterEnabled(true);
            int receivedCount = mReceivedBurstNum;
            mReceivedBurstNum = 0;
            mHandler.removeMessages(MSG_CAPTURE_BURST);

            updateParametersFlashMode();// MODIFIED by sichao.hu, 2016-03-21, BUG-1779801
            //Reset focus to be continuous auto focus
            clearFocusWithoutChangingState();
            ///////////////////////////////////////////

            if (mCameraState != SNAPSHOT_LONGSHOT_PENDING_START && mCameraState != SNAPSHOT_LONGSHOT && mCameraState != SNAPSHOT_LONGSHOT_PENDING_STOP) {
                break;
            }
            //this part can be done both during SNAPSHOT_LONGSHOT_PENDING and SNAP_SHOT_LONGSHOT
            abortOptimizedBurstShot();
            Log.w(TAG,"parameters post update");
            /////////////////////////////////////////////////////////////////////////////
            if (mCameraState != SNAPSHOT_LONGSHOT && mCameraState != SNAPSHOT_LONGSHOT_PENDING_STOP) {
                break;
            }
            //The later part could only display at SNAPSHOT_LONGSHOT state to prevent saving progress show again in case of dismissed and IDLE
            if (receivedCount == 0) {
                break;
            }
            Log.w(TAG, "Burst current burst num is " + mBurstNumForOneSingleBurst);
            if (mBurstShotCheckQueue.setCapacity(receivedCount)) {
                showSavingHint(receivedCount);
            }

            mUI.updateBurstCount(0, BURST_MAX);//Clear burst shot tip
            mAppController.getCameraAppUI().setModeStripViewVisibility(true);
            setCaptureView(false);
            break;
        }
        /* MODIFIED-BEGIN by xuan.zhou, 2016-06-17,BUG-2377722*/
        // Camera may not open now.
        if (mCameraDevice != null) {
            setCameraState(IDLE);
        }
        /* MODIFIED-END by xuan.zhou,BUG-2377722*/

        HelpTipsManager helpTipsManager = mAppController.getHelpTipsManager();
        if(!mBurstShotNotifyHelpTip && helpTipsManager != null){
            helpTipsManager.onBurstShotResponse();
            mBurstShotNotifyHelpTip = true;
        }

    }

    protected void abortOptimizedBurstShot(){
        /* MODIFIED-BEGIN by xuan.zhou, 2016-06-17,BUG-2377722*/
        if (mCameraDevice == null) {
            return;
        }
        /* MODIFIED-END by xuan.zhou,BUG-2377722*/
        mCameraDevice.abortBurstShot();
        if (CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL != mAppController.getSupportedHardwarelevel(mCameraId)) {
            lightlyRestartPreview();
        }
        return;
    }

    private ProgressDialog mProgressDialog;

    private final DialogInterface.OnKeyListener mProgressDialogKeyListener = new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent keyEvent) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                case KeyEvent.KEYCODE_FOCUS:
                case CameraUtil.BOOM_KEY:
                    return true;
            }
            return false;
        }
    };



    protected void showSavingHint(int count){
        if(count==0){
            return;
        }

        if(mProgressDialog==null){
            mProgressDialog=new ProgressDialog(mActivity);
            mProgressDialog.setOnKeyListener(mProgressDialogKeyListener);

            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(String.format(mActivity.getAndroidContext().getResources().getString(R.string.burst_saving_hint), count));
        mProgressDialog.show();
    }

    protected void dismissSavingHint(){
        if(mProgressDialog!=null&&mProgressDialog.isShowing()){
            mProgressDialog.dismiss();
        }
    }

    private ProgressDialog mOptimisingPhotoDialog;

    private void showOptimisingPhotoHint(){
        SettingsManager settingsManager = mAppController.getSettingsManager();
        boolean superZoomOn = mZoomValue > mSuperZoomThreshold && isSuperResolutionEnabled();
        /* MODIFIED-BEGIN by xuyang.liu, 2016-10-13,BUG-3110198*/
        boolean hdrOn = Keys.isHdrOn(settingsManager,mActivity) && isHdrShow();
        boolean lowLightOn = Keys.isLowlightOn(settingsManager, mAppController.getCameraScope()) && isLowLightShow();
        boolean facebeautyOn = isFacebeautyEnabled();

        if (!superZoomOn && !hdrOn && !lowLightOn && !facebeautyOn && !mAutoHdrEnable) {
        /* MODIFIED-END by xuyang.liu,BUG-3110198*/
            return;
        }

        if(mOptimisingPhotoDialog == null){
            mOptimisingPhotoDialog = new ProgressDialog(mActivity);
            mOptimisingPhotoDialog.setOnKeyListener(mProgressDialogKeyListener);
            mOptimisingPhotoDialog.setCancelable(false);
            mOptimisingPhotoDialog.show();
//            WindowManager wm=mActivity.getWindowManager();
            WindowManager.LayoutParams params = mOptimisingPhotoDialog.getWindow().getAttributes();
            Window w = mOptimisingPhotoDialog.getWindow();
            w.setContentView(R.layout.optimise_photo_layout);
//            w.setGravity(Gravity.BOTTOM);
//            w.setBackgroundDrawableResource(R.drawable.optimise_photo_background);
//            Resources res = mActivity.getResources();
            params.dimAmount=0;
//            params.y = (int) res.getDimension(R.dimen.optimise_margin_bottom);;
////            Point windowSize=new Point();
////            wm.getDefaultDisplay().getSize(windowSize);
//            params.width=(int) res.getDimension(R.dimen.optimise_photo_width);
//            params.height = (int) res.getDimension(R.dimen.optimise_photo_height);
            w.setAttributes(params);
        }


        mUI.startSaveProgress();
        mHandler.removeCallbacks(mProgressUpdateRunnable);
        //mHandler.removeCallbacks(mHideProgressRunnable);
        mHandler.postDelayed(mProgressUpdateRunnable.setProgress(SHUTTER_PROGRESS_INIT),SHUTTER_PROGRESS_MAX);

    }


    private ProgressUpdateRunnable mProgressUpdateRunnable=new ProgressUpdateRunnable(SHUTTER_PROGRESS_INIT);

    private class ProgressUpdateRunnable implements Runnable{
        private int mProgress;
        public ProgressUpdateRunnable(int progress){
            mProgress=progress;
        }

        public ProgressUpdateRunnable setProgress(int progress){
            mProgress=progress;
            return this;
        }

        @Override
        public void run() {
            mUI.setSaveProgress(mProgress);
            int delay=SHUTTER_DELAY_LOW;
            if(mProgress>SHUTTER_PROGRESS_ACCELERATE_THRESHOLD){
                delay=SHUTTER_DELAY_UP;
            }
            if(mProgress<SHUTTER_PROGRESS_FAKE_END) {
                mHandler.postDelayed(this.setProgress(mProgress + SHUTTER_PROGRESS_STEP), delay);
            }
        }
    }

    private Runnable mHideProgressRunnable=new Runnable() {
        @Override
        public void run() {
            mUI.stopSaveProgress();
        }
    };

    private void dismissOptimisingPhotoHint(){
        if(mOptimisingPhotoDialog != null && mOptimisingPhotoDialog.isShowing()) {
            mOptimisingPhotoDialog.dismiss();
        }
        mOptimisingPhotoDialog = null;
        mHandler.removeCallbacks(mProgressUpdateRunnable);
        mUI.setSaveProgress(SHUTTER_PROGRESS_MAX);
        mHideProgressRunnable.run();

    }

    @Override
    public void onShutterButtonClick() {
        doShutterButtonClick(false);
    }

    private void doShutterButtonClick(boolean isGestureShot) {
        Log.w(TAG, "KPI shutter click"); // MODIFIED by yuanxing.tan, 2016-06-18,BUG-2202739
        if (mPaused || (mCameraState == SWITCHING_CAMERA)||(mCameraState==SNAPSHOT_IN_PROGRESS)||isInBurstshot()// MODIFIED by sichao.hu, 2016-03-21, BUG-1844237
                || (mCameraState == PREVIEW_STOPPED)
                || !mAppController.isShutterEnabled()||mAppController.getCameraAppUI().isShutterLocked()||mUnderLowMemory) {
            mVolumeButtonClickedFlag = false;
            return;
        }

        // Do not take the picture if there is not enough storage.
        if (mActivity.getStorageSpaceBytes() <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
            Log.i(TAG, "Not enough space or storage not ready. remaining="
                    + mActivity.getStorageSpaceBytes());
            /* MODIFIED-BEGIN by fei.hui, 2016-09-09,BUG-2868515*/
            /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
            SnackbarToast.getSnackbarToast().showToast(mActivity,
                    mActivity.getString(R.string.storage_low_storage_critical_toast_message)
                    ,SnackbarToast.LENGTH_LONG,SnackbarToast.DEFAULT_Y_OFFSET);
                    /* MODIFIED-END by bin-liu3,BUG-3253898*/
                    /* MODIFIED-END by fei.hui,BUG-2868515*/
            mVolumeButtonClickedFlag = false;
            return;
        }
        Log.d(TAG, "onShutterButtonClick: mCameraState=" + mCameraState +
                " mVolumeButtonClickedFlag=" + mVolumeButtonClickedFlag);

        dismissButtonGroupBar(false);
        clearAspectRatioViewer(true);

        mAppController.setShutterEnabled(false);
        int countDownDuration = mActivity.getSettingsManager()
            .getInteger(mAppController.getCameraScope(), Keys.KEY_COUNTDOWN_DURATION);
        mTimerDuration = countDownDuration;
        boolean isCanCountDown = isCountDownShow(); //MODIFIED by yuanxing.tan, 2016-04-12,BUG-1938868
        if (isGestureShot) {
            if (countDownDuration <= 0) {
                countDownDuration = DEFAULT_GESTURE_SHOT_COUNT_DURATION; // 3 sec
                mTimerDuration = countDownDuration;
            }
            isCanCountDown = true;
        }
        if(isCanCountDown){
            if (countDownDuration > 0) {
                // Start count down.
                mAppController.getCameraAppUI().hidePoseBackView(); // MODIFIED by feifei.xu, 2016-11-02,BUG-3299499
                mAppController.getCameraAppUI().transitionToCancel();
                setCaptureView(true);
                transitionToTimer(false);
                mUI.startCountdown(countDownDuration);
            } else {
                focusAndCapture();
            }
        }else {
            focusAndCapture();
        }
    }

    protected void setCaptureView(boolean captureStart) {
        if (captureStart) {
            mAppController.getCameraAppUI().hideModeOptions();
            mAppController.getCameraAppUI().getTopMenus().setTopModeOptionVisibility(false);
            mUI.setAspectRatioVisible(false);
        } else {
            mAppController.getCameraAppUI().showModeOptions();
            mAppController.getCameraAppUI().getTopMenus().setTopModeOptionVisibility(true);
            if (aspectRatioVisible()) {
                mUI.setAspectRatioVisible(true);
            }
        }
    }

    @Override
    public void onShutterButtonLongClick() {
        if (mPaused || (mCameraState == SWITCHING_CAMERA)
                || (mCameraState == PREVIEW_STOPPED)
                || !mAppController.isShutterEnabled() || isImageCaptureIntent()||mUnderLowMemory) {
            mVolumeButtonClickedFlag = false;
            return;
        }
        if(mSceneMode!= CameraCapabilities.SceneMode.AUTO || mZoomValue > 1.0f ||
                Keys.isLowlightOn(mAppController.getSettingsManager(), mAppController.getCameraScope())
                || isFacebeautyEnabled()){
            return;
        }
        int countDownDuration = mActivity.getSettingsManager()
                .getInteger(mAppController.getCameraScope(), Keys.KEY_COUNTDOWN_DURATION);
        // when timer on,forbid burst shot
        if (isCountDownShow() && countDownDuration > 0) {
            return;
        }
        // Do not take the picture if there is not enough storage.
        if (mActivity.getStorageSpaceBytes() <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
            Log.i(TAG, "Not enough space or storage not ready. remaining="
                    + mActivity.getStorageSpaceBytes());
            mVolumeButtonClickedFlag = false;
            return;
        }
        Log.d(TAG, "onShutterButtonClick: mCameraState=" + mCameraState +
                " mVolumeButtonClickedFlag=" + mVolumeButtonClickedFlag);

        dismissButtonGroupBar(false);
        clearAspectRatioViewer(false);

        mAppController.setShutterEnabled(false);
        Log.w(TAG, "Longpress sucess and wait for burst shot");
        if (mCameraState==AE_AF_LOCKED) {
           clearFocusWithoutChangingState();
        } else if (mCameraState == SCANNING_FOR_AE_AF_LOCK) {
            if (isExposureSidebarEnabled()) {
                mUI.hideExposureSidebar();
            }
        }
        setCameraState(SNAPSHOT_LONGSHOT_PENDING_START);
        mBurstShotCheckQueue.clearCheckQueue();
        mBurstShotNotifyHelpTip = false;
        mUI.disableZoom();//Whenever the camera state is set to IDLE , the burst shot is supposed to be stopped ,
        if (isOptimeizeSnapshot) {
            Log.d(TAG,"OptimeizeSnapshot");
            bustShot();
        }else {
            capture();
        }
    }

    private void bustShot() {
        snapShot = ContinueShot.create(this);
        snapShot.prepare();
        snapShot.takePicture(new ContinueShot.onContinueShotFinishListener() {
            @Override
            public void onFinish() {
                mAppController.setShutterEnabled(true);
                mHandler.removeMessages(MSG_CAPTURE_BURST);
                setCameraState(IDLE);
            }
        });
    }

    protected void clearFocusWithoutChangingState(){
        /* MODIFIED-BEGIN by xuan.zhou, 2016-06-17,BUG-2377722*/
        if (mCameraDevice == null) {
            return;
        }
        /* MODIFIED-END by xuan.zhou,BUG-2377722*/
        mFocusManager.removeMessages();
        mUI.clearFocus();
        mCameraDevice.cancelAutoFocus();
        mPreivewLongPressed = false;
        if (mUI.isMeteringShowing()) {
            mUI.hideMeteringUI();
            mFocusManager.setMeteringArea(null);
        }
        if (isExposureSidebarEnabled()) {
            resetSideEV();
            mUI.hideExposureSidebar();
        } else {
            mUI.clearEvoPendingUI();
        }
        stopMotionChecking();
        mFocusManager.setAeAwbLock(false);
        setCameraParameters(UPDATE_PARAM_PREFERENCE);
    }

    private void focusAndCapture() {
        if(mFocusManager==null){
            return;
        }
        if (mSceneMode == CameraCapabilities.SceneMode.HDR) {
            mUI.setSwipingEnabled(false);
        }
        // If the user wants to do a snapshot while the previous one is still
        // in progress, remember the fact and do it after we finish the previous
        // one and re-start the preview. Snapshot in progress also includes the
        // state that autofocus is focusing and a picture will be taken when
        // focus callback arrives.
        if ((mFocusManager.isFocusingSnapOnFinish() || mCameraState == SNAPSHOT_IN_PROGRESS||mCameraState==SNAPSHOT_IN_PROGRESS_DURING_LOCKED)) {
            if (!mIsImageCaptureIntent) {
                mSnapshotOnIdle = true;
            }
            return;
        }

        mSnapshotOnIdle = false;
        if(mCameraState!=AE_AF_LOCKED) {
            mFocusManager.focusAndCapture(mCameraSettings.getCurrentFocusMode());
        }else{
            capture();
        }
    }

    protected void readCameraInitialParameters(){
        if(mCameraDevice==null){
            return;
        }
        int maxEvo=mCameraDevice.getCapabilities().getMaxExposureCompensation();
        int minEvo=mCameraDevice.getCapabilities().getMinExposureCompensation();

        Log.w(TAG, String.format("max Evo is %d and min Evo is %d", maxEvo, minEvo));
        if (isExposureSidebarEnabled()) {
            resetSideEV();
            mUI.loadExposureCompensation(minEvo, maxEvo);
        } else {
            mUI.parseEvoBound(maxEvo, minEvo);
        }
        initializeFocusModeSettings();
    }

    protected void initializeFocusModeSettings(){
        //dummy , do the initialization in NormalPhotoModule
    }

    @Override
    public void onRemainingSecondsChanged(int remainingSeconds) {
        if (mActivity.getSettingsManager().getBoolean(SettingsManager.SCOPE_GLOBAL, Keys.KEY_SOUND)) {
            if (remainingSeconds == 1) {
                mCountdownSoundPlayer.play(R.raw.timer_final_second, 0.6f);
            } else if (remainingSeconds == 2 || remainingSeconds == 3) {
                mCountdownSoundPlayer.play(R.raw.timer_increment, 0.6f);
            }
        }
    }

    @Override
    public void onCountDownFinished() {
        transitionToTimer(true);
        mAppController.getCameraAppUI().showPoseBackView(); // MODIFIED by feifei.xu, 2016-11-02,BUG-3299499
        mAppController.getCameraAppUI().transitionToCapture();
        setCaptureView(false);
        if (aspectRatioVisible()) {
            mUI.setAspectRatioVisible(true);
        }
        activeFilterButton();
        if (mPaused) {
            return;
        }
        focusAndCapture();
    }

    @Override
    public void resume() {
        mPaused = false;

        Log.w(TAG,"KPI Track photo resume E : ");
        if(mActivity!=null) {
            SettingsManager settingsManager = mActivity.getSettingsManager();
            mCameraId = settingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,
                    Keys.KEY_CAMERA_ID);
        }

        mCountdownSoundPlayer.loadSound(R.raw.timer_final_second);
        mCountdownSoundPlayer.loadSound(R.raw.timer_increment);
        if (mFocusManager != null) {
            // If camera is not open when resume is called, focus manager will
            // not be initialized yet, in which case it will start listening to
            // preview area size change later in the initialization.
            mAppController.addPreviewAreaSizeChangedListener(mFocusManager);
        }
        mAppController.addPreviewAreaSizeChangedListener(mUI);

        CameraProvider camProvider = mActivity.getCameraProvider();
        if (camProvider == null) {
            // No camera provider, the Activity is destroyed already.
            return;
        }
        if(!mActivity.getCameraProvider().isCameraRequestBoosted()) {
            requestCameraOpen();
        }

        /* MODIFIED-BEGIN by xuan.zhou, 2016-06-06,BUG-2251935*/
        if (!mSpecificUIReady) {
            disableFlashButtonClick();
        }
        /* MODIFIED-END by xuan.zhou,BUG-2251935*/

        mJpegPictureCallbackTime = 0;
        mZoomValue = 1.0f;

        mOnResumeTime = SystemClock.uptimeMillis();
        checkDisplayRotation();

        // If first time initialization is not finished, put it in the
        // message queue.
        if (!mFirstTimeInitialized) {
            mHandler.sendEmptyMessage(MSG_FIRST_TIME_INIT);
        } else {
            initializeSecondTime();
        }

        Sensor gsensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (gsensor != null) {
            mSensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        Sensor msensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (msensor != null) {
            mSensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        getServices().getRemoteShutterListener().onModuleReady(this, getRemodeShutterIcon());
        SessionStatsCollector.instance().sessionActive(true);
        if (mUI != null && aspectRatioVisible()) {
            mUI.setAspectRatioVisible(true);
        }
        if (needShowBottomLine()) {
            mUI.showBottomBarLine();
        } else {
            mUI.hideBottomBarLine();
        }

        Log.w(TAG, "KPI Track photo resume X");
    }

    protected boolean needShowBottomLine () {
        return false;
    }

    protected boolean isContactsShow () {
        return false;
    }
    @Override
    public void preparePause() {
        if (mCameraState != IDLE) {
            return;
        }
        stopPreview();
    }

    protected int getRemodeShutterIcon() {
        return CameraUtil.getCameraShutterNormalStateIconId(
                mAppController.getCurrentModuleIndex(), mAppController.getAndroidContext());
    }

    /**
     * @return Whether the currently active camera is front-facing.
     */
    protected boolean isCameraFrontFacing() {
        return mAppController.getCameraProvider().getCharacteristics(mCameraId)
                .isFacingFront();
    }
    protected boolean isLowLightShow() {
        return false;
    }

    protected boolean isSuperResolutionEnabled(){
        return false;
    }

    protected boolean isVisidonModeEnabled(){
        return false;
    }

    protected boolean isHdrShow() {
        SettingsManager settingsManager = mActivity.getSettingsManager();
        return Keys.isCameraBackFacing(settingsManager, SettingsManager.SCOPE_GLOBAL);
    }
    protected boolean isFlashShow() {
        return true;
    }
    protected boolean isWrapperButtonShow() {
        return false;
    }
    protected boolean isCountDownShow() {
        /* MODIFIED-BEGIN by jianying.zhang, 2016-10-18,BUG-2715761*/
        if (isImageCaptureIntent()) {
            return false;
        }
        /* MODIFIED-END by jianying.zhang,BUG-2715761*/
        boolean isCanCountDownForBack = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_BACKFACING_COUNT_DOWN, false);
        boolean isCanCountDownForFront = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_FRONTFACING_COUNT_DOWN, false);
        boolean isCanCountDown = isCameraFrontFacing() ? isCanCountDownForFront : isCanCountDownForBack ;
        if(CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_COUNT_DOWN_ONLY_AUTO_MODE,false)){
            if(mActivity.getCurrentModuleIndex() != mActivity.getResources().getInteger(R.integer.camera_mode_photo)){ //MODIFIED by yuanxing.tan, 2016-04-12,BUG-1938868
                isCanCountDown = false;
            }
        }
        return isCanCountDown;
    }
    /**
     * The focus manager is the first UI related element to get initialized, and
     * it requires the RenderOverlay, so initialize it here
     */
    private void initializeFocusManager() {
        // Create FocusManager object. startPreview needs it.
        // if mFocusManager not null, reuse it
        // otherwise create a new instance
        if (mFocusManager != null) {
            mUI.clearFocus();
            mFocusManager.removeMessages();
        } else {
            mMirror = isCameraFrontFacing();
            String[] defaultFocusModesStrings = mActivity.getResources().getStringArray(
                    R.array.pref_camera_focusmode_default_array);
            ArrayList<CameraCapabilities.FocusMode> defaultFocusModes =
                    new ArrayList<CameraCapabilities.FocusMode>();
            CameraCapabilities.Stringifier stringifier = mCameraCapabilities.getStringifier();
            for (String modeString : defaultFocusModesStrings) {
                CameraCapabilities.FocusMode mode = stringifier.focusModeFromString(modeString);
                if (mode != null) {
                    defaultFocusModes.add(mode);
                }
            }
            mFocusManager =
                    new FocusOverlayManager(mAppController, defaultFocusModes,
                            mCameraCapabilities, this, mMirror, mActivity.getMainLooper(),
                            mUI.getFocusUI());
            mMotionManager = getServices().getMotionManager();
            if (mMotionManager != null) {
                mMotionManager.addListener(mFocusManager);
            }
        }
        mAppController.addPreviewAreaSizeChangedListener(mFocusManager);
    }

    /**
     * @return Whether we are resuming from within the lockscreen.
     */
    private boolean isResumeFromLockscreen() {
        String action = mActivity.getIntent().getAction();
        return (MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA.equals(action)
                || MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE.equals(action));
    }

    @Override
    public boolean isPaused() {
        return mPaused;
    }

    @Override
    public void pause() {
        Log.v(TAG, "KPI photo pause E");
        mPaused = true;
        if(mCameraState==SNAPSHOT_LONGSHOT_PENDING_START||mCameraState==SNAPSHOT_LONGSHOT||mCameraState==SNAPSHOT_LONGSHOT_PENDING_STOP){
            stopBurst();
            /* MODIFIED-BEGIN by xuan.zhou, 2016-06-17,BUG-2377722*/
            // pause during long shots, set pressed false here.
            mActivity.getCameraAppUI().cancelShutterButtonClick();
            /* MODIFIED-END by xuan.zhou,BUG-2377722*/
        }
        dismissSavingHint();
        dismissOptimisingPhotoHint();
        mProgressDialog=null;
        mUI.updateBurstCount(0, BURST_MAX);//Clear burst shot tip
        if (mUI.isGestureViewShow()) {
            mHandler.removeMessages(MSG_HIDE_GESTURE);
            mUI.hideGesture();
        }
        mUI.hideModeOptionsTip();
        mHandler.removeCallbacks(mHideModeOptionsTipRunnable);

        if (mHandler.hasMessages(MSG_UPDATE_FACE_BEAUTY)) {
            mHandler.removeMessages(MSG_UPDATE_FACE_BEAUTY);
        }
        firstFrame = true; //MODIFIED by yuanxing.tan, 2016-03-30,BUG-1874767
        getServices().getRemoteShutterListener().onModuleExit();
        SessionStatsCollector.instance().sessionActive(false);


        Sensor gsensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (gsensor != null) {
            mSensorManager.unregisterListener(this, gsensor);
        }

        Sensor msensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (msensor != null) {
            mSensorManager.unregisterListener(this, msensor);
        }

        // Reset the focus first. Camera CTS does not guarantee that
        // cancelAutoFocus is allowed after preview stops.
        if (mCameraDevice != null && mCameraState != PREVIEW_STOPPED) {
            mCameraDevice.cancelAutoFocus();
        }

        // If the camera has not been opened asynchronously yet,
        // and startPreview hasn't been called, then this is a no-op.
        // (e.g. onResume -> onPause -> onResume).
        if(mCameraSettings!=null) {
//            mCameraSettings.isZslOn = false;
//            mCameraDevice.applySettings(mCameraSettings);
        }
        if(mZoomFlashLock!=null) {
            mAppController.getCameraAppUI()
                    .getTopMenus().enableButtonWithToken(TopMenus.BUTTON_FLASH, mZoomFlashLock);
            mZoomFlashLock=null;
        }
        setAeFlashEnableWithLock(false); // MODIFIED by jianying.zhang, 2016-11-10,BUG-3398235
        stopPreview();
        cancelCountDown();
        mCountdownSoundPlayer.unloadSound(R.raw.timer_final_second);
        mCountdownSoundPlayer.unloadSound(R.raw.timer_increment);

        mNamedImages = null;
        // If we are in an image capture intent and has taken
        // a picture, we just clear it in onPause.
        // mJpegImageData = null;

        // Remove the messages and runnables in the queue.
        mHandler.removeCallbacksAndMessages(null);

        if (mMotionManager != null) {
            mMotionManager.removeListener(mFocusManager);
            mMotionManager = null;
        }

        closeCamera();
        mActivity.enableKeepScreenOn(false);
        mUI.onPause();
        mPendingSwitchCameraId = -1;
        if (mFocusManager != null) {
            mUI.clearFocus();
            mFocusManager.removeMessages();
        }
        getServices().getMemoryManager().removeListener(this);
        mAppController.removePreviewAreaSizeChangedListener(mFocusManager);
        mAppController.removePreviewAreaSizeChangedListener(mUI);

        SettingsManager settingsManager = mActivity.getSettingsManager();
        settingsManager.removeListener(this);

        ToastUtil.cancelToast();
        if (needShowBottomLine()) {
            mUI.hideBottomBarLine();
        }
        Log.w(TAG, "KPI photo pause X");
    }

    @Override
    public void destroy() {
        mJpegImageData = null;
        if (mUI != null) {
            mUI.clearReviewImage();
        }
        mCountdownSoundPlayer.release();
        mActivity.resetControlPoseCallback(); //MODIFIED by shunyin.zhang, 2016-04-12,BUG-1892480
    }

    @Override
    public void onLayoutOrientationChanged(boolean isLandscape) {
        setDisplayOrientation();
    }

    @Override
    public void updateCameraOrientation() {
        if (mDisplayRotation != CameraUtil.getDisplayRotation(mActivity)) {
            setDisplayOrientation();
        }
    }

    private boolean canTakePicture() {
        return isCameraIdle()
                && (mActivity.getStorageSpaceBytes() > Storage.LOW_STORAGE_THRESHOLD_BYTES);
    }

    @Override
    public void autoFocus() {
        if (mCameraDevice == null||isInBurstshot()) {
            return;
        }

        if(mFocusManager.getFocusAreas()==null){
            mAutoFocusCallback.onAutoFocus(true, mCameraDevice);
            return;
        }

        Log.v(TAG,"Starting auto focus");
        mFocusStartTime = System.currentTimeMillis();
        mCameraDevice.autoFocus(mHandler, mAutoFocusCallback);
        SessionStatsCollector.instance().autofocusManualTrigger();
        if(mCameraState!=SNAPSHOT_IN_PROGRESS) {// if called from SNAPSHOT_IN_PROGRESS , it's in shutter , not supposed to change state
            setCameraState(FOCUSING);
        }
    }

    @Override
    public boolean cancelAutoFocus() {
        if (mCameraDevice == null) {
            return false;
        }
        if(isInBurstshot()){
            mPreivewLongPressed = false;
            mUI.clearEvoPendingUI();
        }
        if (mCameraState != AE_AF_LOCKED
                &&!isInBurstshot()) {
            Log.w(TAG,"cancel auto focus");
            mPreivewLongPressed = false;
            stopMotionChecking();
            mCameraDevice.cancelAutoFocus();
            mFocusManager.setAeAwbLock(false);
            if (mUI.isMeteringShowing()) {
                mUI.hideMeteringUI();
            }
            if (isExposureSidebarEnabled()) {
                resetSideEV();
                mHandler.removeCallbacks(hideExposureSidebar);
                if (mUI.isExposureSidebarVisible()) {
                    mUI.setExposureSidebarPrepared(false);
                    mUI.fadeOutExposureSidebar();
                }
            } else {
                mUI.clearEvoPendingUI();
            }
            if (mCameraState != PREVIEW_STOPPED) { // capture intent
                setCameraState(IDLE);
            }
            setCameraParameters(UPDATE_PARAM_PREFERENCE);
            return true;
        }
        return false;
    }

    protected boolean isInBurstshot(){
        return (mCameraState==SNAPSHOT_LONGSHOT_PENDING_START
                ||mCameraState==SNAPSHOT_LONGSHOT
                ||mCameraState==SNAPSHOT_LONGSHOT_PENDING_STOP);
    }

    // To mark the long press event, set true when onLongPress and reset false when focus completes
    // or be cancelled. If focus success and it's true, lock af and start metering if support.
    private boolean mPreivewLongPressed = false;

    @Override
    public void onLongPress(int x, int y) {
        //LongPress is no longer intended for manually adjusting exposure compensation, remove the implement here
//        Log.w(TAG, "hardware level is " + mAppController.getSupportedHardwarelevel(mCameraId));
//        if(mCameraState==IDLE){
//            mUI.initEvoSlider(x,y);
//            setCameraState(SCANING_FOR_AE_AF_LOCK);
//            mFocusManager.onSingleTapUp((int) x, (int) y);//Call TAF to lock AF after AF_FOCUSED_LOCKED
//        }
        if (mPaused || mCameraDevice == null || !mFirstTimeInitialized) {
            return;
        }
        if (isCameraFrontFacing()) {
            return;
        }
        if (mIsInIntentReviewUI) {
            return;
        }
        if (clearAspectRatioViewer(true) || dismissButtonGroupBar(true)) {
            return;
        }

        if (mCameraState == IDLE || mCameraState == FOCUSING ||
                mCameraState == SCANNING_FOR_AE_AF_LOCK || mCameraState == AE_AF_LOCKED) {
            onSingleTapUp(null, x, y);

            mPreivewLongPressed = true;

            if (mMeteringAreaSupported && isMeteringEnabled()) {
                mUI.disableMetering();
                mUI.showMeteringUI();
                mUI.onMeteringStart();
            }
        }
    }

    private int mLockedEvoIndex =0;
    @Override
    public void onEvoChanged(int index) {
        if(mCameraState==SCANNING_FOR_AE_AF_LOCK&&index!=0/*if index is 0, it should be caused by resetEVOslider*/){
            setCameraState(AE_AF_LOCKED);
        }
        if(mCameraState!=AE_AF_LOCKED||mFocusManager==null){
            return;
        }
        Log.w(TAG, "evo index is " + index);
        mLockedEvoIndex =index;
        startMotionChecking();
        mFocusManager.setAeAwbLock(true);
        mFocusManager.keepFocusFrame();
        setExposureCompensation(index, false);
        setCameraParameters(UPDATE_PARAM_PREFERENCE);

    }

    protected boolean clearAspectRatioViewer(boolean needAnimation) {
        return false;
    }

    protected boolean dismissButtonGroupBar(boolean needAnimation) {
        return false;
    }

    @Override
    public void onSingleTapUp(View view, int x, int y) {
        if (clearAspectRatioViewer(true) || dismissButtonGroupBar(true)) {
            return;
        }
        if (mPaused || mCameraDevice == null || !mFirstTimeInitialized
                || mCameraState == SNAPSHOT_IN_PROGRESS
                || mCameraState == SNAPSHOT_IN_PROGRESS_DURING_LOCKED
                || mCameraState ==SNAPSHOT_LONGSHOT_PENDING_START
                || mCameraState ==SNAPSHOT_LONGSHOT
                || mCameraState ==SNAPSHOT_LONGSHOT_PENDING_STOP
                || mCameraState == SWITCHING_CAMERA
                || mCameraState == PREVIEW_STOPPED) {
            return;
        }
        if (mIsInIntentReviewUI) {
            return;
        }

        if (isCameraFrontFacing()) {
            RectF rectF = mAppController.getCameraAppUI().getPreviewArea();
            HelpTipsManager helpTipsManager = mAppController.getHelpTipsManager();
            if(helpTipsManager != null && helpTipsManager.isHelpTipShowExist()){
                Log.e(TAG, "helptip exists and cancels shutterbutton click"); // MODIFIED by peixin, 2016-06-07,BUG-2281968
                return;
            }

            if (rectF.contains(x, y)) {
                onShutterButtonClick();
            }
            return;
        }

        // Reset the tag when single tap up.
        mPreivewLongPressed = false;

        // Clear metering and exposure adjustment.
        if (mUI.isMeteringShowing()) {
            mUI.hideMeteringUI();
        }
        if (needEnableExposureAdjustment() && isExposureSidebarEnabled()) {
            resetSideEV();
        } else {
            mUI.clearEvoPendingUI();
            mUI.initEvoSlider(x, y);
        }

        // Check if metering area or focus area is supported.
        if (!mFocusAreaSupported && !mMeteringAreaSupported) {
            return;
        }
        stopMotionChecking();
        mFocusManager.setAeAwbLock(false);
        mFocusManager.onSingleTapUp(x, y);

        // Show exposure sidebar after cancelAutoFocus().
        if (needEnableExposureAdjustment() && isExposureSidebarEnabled()) {
            mUI.setExposureSidebarPrepared(false);
            mUI.fadeInExposureSidebar(x, y); // MODIFIED by xuan.zhou, 2016-10-22,BUG-3178291
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mUI.isCountingDown()) {
            cancelCountDown();
            return true;
        }

        if (clearAspectRatioViewer(true) || dismissButtonGroupBar(true)) {
            return true;
        }
        return mUI.onBackPressed();
    }

    /**Occasionally onKeyUp  called  when long press, Which consume {@link #mCameraKeyLongPressed} to false,
     so Using event.getRepeatCount() > 0 represent in long press*/
    private boolean mIsInLongPressed = false;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                boolean systemBehaving = isVolumeKeySystemBehaving();
                if (systemBehaving) {
                    return false;
                }
            case KeyEvent.KEYCODE_FOCUS:
            case CameraUtil.BOOM_KEY:
                // When the pose select view is showing,return false to disable the key to take picture
                // otherwise return true
                if(!mAppController.getCameraAppUI().isPoseSelectorShowing()){
                    if (/* TODO: mActivity.isInCameraApp() && */mFirstTimeInitialized &&
                            !mActivity.getCameraAppUI().isInIntentReview()) {
                        if (event.getRepeatCount() == 0) {
                            mAppController.setShutterPress(true);
                            onShutterButtonFocus(true);
                            mIsInLongPressed = false;
                        }else {
                            mIsInLongPressed = true;
                        }
                        if(event.isLongPress() && !mIsImageCaptureIntent) {
                            mCameraKeyLongPressed = true;
                            mAppController.setShutterPress(true);
                            onShutterButtonLongClick();
                        }
                    }
                }
                return true;
            case KeyEvent.KEYCODE_CAMERA:
                if (mFirstTimeInitialized && event.getRepeatCount() == 0) {
                    onShutterButtonClick();
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                // If we get a dpad center event without any focused view, move
                // the focus to the shutter button and press it.
                if (mFirstTimeInitialized && event.getRepeatCount() == 0) {
                    // Start auto-focus immediately to reduce shutter lag. After
                    // the shutter button gets the focus, onShutterButtonFocus()
                    // will be called again but it is fine.
                    onShutterButtonFocus(true);
                }
                return true;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                boolean systemBehaving = isVolumeKeySystemBehaving();
                if (systemBehaving) {
                    /* MODIFIED-BEGIN by jianying.zhang, 2016-05-25,BUG-2201091*/
                    if (mCameraKeyLongPressed) {
                        mAppController.setShutterPress(false);
                        onShutterButtonFocus(false);
                        mCameraKeyLongPressed = false;
                    }
                    /* MODIFIED-END by jianying.zhang,BUG-2201091*/
                    return false;
                }
            case CameraUtil.BOOM_KEY:
                if(!mAppController.getCameraAppUI().isPoseSelectorShowing()){
                    mAppController.setShutterPress(false);
                    if (/* mActivity.isInCameraApp() && */mFirstTimeInitialized &&
                            !mActivity.getCameraAppUI().isInIntentReview() && !mCameraKeyLongPressed
                            && !mIsInLongPressed) {
                        if (mUI.isCountingDown()) {
                            cancelCountDown();
                        } else {
                            mVolumeButtonClickedFlag = true;
                            onShutterButtonClick();
                            HelpTipsManager helpTipsManager = mAppController.getHelpTipsManager();
                            if(helpTipsManager != null && helpTipsManager.isHelpTipShowExist()){
                                helpTipsManager.onBoomKeySingleShotResponse();
                            }
                        }
                    }
                    if (mCameraKeyLongPressed) {
                        onShutterButtonFocus(false);
                        mCameraKeyLongPressed = false;
                    }
                }

                return true;
            case KeyEvent.KEYCODE_FOCUS:
                mAppController.setShutterPress(false);
                if (mFirstTimeInitialized) {
                    onShutterButtonFocus(false);
                }
                return true;
        }
        return false;
    }

    protected boolean isVolumeKeySystemBehaving() {
        return false;
    }

    protected void closeCamera() {
        mPreivewLongPressed = false;
        if (mUI.isMeteringShowing()) {
            mUI.hideMeteringUI();
            mAppController.getCameraAppUI().setTouchObstruct(false); // MODIFIED by xuan.zhou, 2016-10-31,BUG-3271995
        }
        if (isExposureSidebarEnabled()) {
            resetSideEV();
            mUI.hideExposureSidebar();
        } else {
            mUI.clearEvoPendingUI();
        }
        mSnapshotOnIdle=false;
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-06-18,BUG-2202739*/
        if (mZoomFlashLock != null) {
            mAppController.getCameraAppUI().getTopMenus()
                    .enableButtonWithToken(TopMenus.BUTTON_FLASH, mZoomFlashLock);
            mZoomFlashLock=null;
        }
        /* MODIFIED-END by yuanxing.tan,BUG-2202739*/
        stopPreview();
        if (mCameraDevice != null) {
            mCameraDevice.setZoomChangeListener(null);
            mCameraDevice.setFaceDetectionCallback(null, null);
            //mCameraDevice.setMetadataCb(null,null);
            mFaceDetectionStarted = false;
            mActivity.getCameraProvider().releaseCamera(mCameraDevice.getCameraId());
            mCameraDevice = null;
            setCameraState(PREVIEW_STOPPED);
            mFocusManager.onCameraReleased();
        }
        /* MODIFIED-BEGIN by xuan.zhou, 2016-06-06,BUG-2251935*/
        mSpecificUIReady = false;
        disableFlashButtonClick();
        /* MODIFIED-END by xuan.zhou,BUG-2251935*/
    }

    protected void setDisplayOrientation() { // MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
        mDisplayRotation = CameraUtil.getDisplayRotation(mActivity);
        Characteristics info =
                mActivity.getCameraProvider().getCharacteristics(mCameraId);
        mDisplayOrientation = info.getPreviewOrientation(mDisplayRotation);
        mSensorOrientation=info.getSensorOrientation();
        mUI.setSensorOrientation(mSensorOrientation);
        mUI.setDisplayOrientation(mDisplayOrientation);
        mUI.setGestureMirrored(isCameraFrontFacing());
        if (mFocusManager != null) {
            mFocusManager.setDisplayOrientation(mDisplayOrientation);
        }
        // Change the camera display orientation
        if (mCameraDevice != null) {
            mCameraDevice.setDisplayOrientation(mDisplayRotation,
                    isCaptureOrientationFollowPreview());
        }
        Log.v(TAG, "setPostGestureRotation (screen:preview) " +
                mDisplayRotation + ":" + mDisplayOrientation);
    }

    protected boolean isCaptureOrientationFollowPreview() {
        return true;
    }

    /** Only called by UI thread. */
    protected void setupPreview() {
        Log.i(TAG, "setupPreview");
        mFocusManager.resetTouchFocus();
        if(mAppController.getCameraProvider().isBoostPreview()) {
            mActivity.clearBoost();
        }
        startPreview();
    }

    private void lightlyRestartPreview(){
        if(!mPaused){
            mAppController.setShutterEnabled(false);
            mCameraDevice.stopPreview();
            CameraAgent.CameraStartPreviewCallback startPreviewCallback =
                    new CameraAgent.CameraStartPreviewCallback() {
                        @Override
                        public void onPreviewStarted() {
                            mFocusManager.onPreviewStarted();
                            PhotoModule.this.onPreviewStarted();
                        }
                    };
            if (GservicesHelper.useCamera2ApiThroughPortabilityLayer(mActivity)) {
                mCameraDevice.startPreview();
                startPreviewCallback.onPreviewStarted();
            } else {
                mCameraDevice.startPreviewWithCallback(new Handler(Looper.getMainLooper()),
                        startPreviewCallback);
            }
        }
    }

    /**
     * Returns whether we can/should start the preview or not.
     */
    private boolean checkPreviewPreconditions() {
        if (mPaused) {
            return false;
        }

        if (mCameraDevice == null) {
            Log.w(TAG, "startPreview: camera device not ready yet.");
            return false;
        }

        SurfaceTexture st = getTexture(); // MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
        if (st == null) {
            Log.w(TAG, "startPreview: surfaceTexture is not ready.");
            return false;
        }

        if (!mCameraPreviewParamsReady) {
            Log.w(TAG, "startPreview: parameters for preview is not ready.");
            return false;
        }

        return true;
    }

    protected boolean isEnableGestureRecognization(){
        return false;
    }


    private boolean mIsRecognizationRunning=false;
    private GestureInstructionWrapper mGestureInstructions;
    private final Object GESTURE_LOCK=new Object();
    private long mGestureFrameSkipTimer=0;
    @Override
    public void onPreviewFrame(final byte[] data, CameraProxy camera) {
        synchronized (GESTURE_LOCK) {
            if (mPaused||mCameraState==PREVIEW_STOPPED) {
                return;
            }
            if (isEnableGestureRecognization() && mGestureInstructions == null &&
                    !mIsRecognizationRunning && ExtBuild.device() != ExtBuild.MTK_MT6755) {
                mIsRecognizationRunning = true;
                initGestureSuite();
                mGestureFrameSkipTimer = System.currentTimeMillis();
            }
            if (mGestureInstructions != null) {
//                if(mPreviewFrameRunnable==null){
//                    mPreviewFrameRunnable=new Runnable() {
//                        @Override
//                        public void run() {
//                            savePreviewFrame("/sdcard/dumpImg.nv21",data);
//                        }
//                    };
//                    new AsyncTask<Void,Void,Void>(){
//                        @Override
//                        protected Void doInBackground(Void... voids) {
//                            mPreviewFrameRunnable.run();
//                            return null;
//                        }
//                    }.execute();
//                }

                int rotation=0;
                //0 stands for rotate_0, 1 for rotate_90, 2 for rotate_180, 3 for rotate_270
                int displayOrientation=isCameraFrontFacing()?mOrientation:-mOrientation;
                int orientation=(mSensorOrientation+displayOrientation+360)%360;
                switch(orientation){
                    case 0:
                        rotation=0;
                        break;
                    case 90:
                        rotation=1;
                        break;
                    case 180:
                        rotation=2;
                        break;
                    case 270:
                        rotation=3;
                        break;

                }
                long currentSystemTime=System.currentTimeMillis();
                if(Math.abs(currentSystemTime-mGestureFrameSkipTimer)>GESTURE_SKIP_TIMER){
                    mGestureFrameSkipTimer=currentSystemTime;
                    mGestureInstructions.handleImage(SystemClock.elapsedRealtime(), data, rotation);
                }
            }
        }
    }


    private Runnable mPreviewFrameRunnable=null;

    private void savePreviewFrame(String name,byte[] data){
        File file=new File(name);
        if(file.exists()){
            file.delete();
        }
        FileOutputStream fos;
        try {
            fos=new FileOutputStream(name,false);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*MODIFIED-BEGIN by sichao.hu, 2016-04-15,BUG-1951866*/
    Object mFaceDetectPauseKeyGenerator=new Object();
    @Override
    public void onFaceDetected(boolean detected) {
        if(detected){
            mUI.pauseFocusFrame(mFaceDetectPauseKeyGenerator.hashCode());
        }else{
            mUI.resumeFocusFrame(mFaceDetectPauseKeyGenerator.hashCode());
        }
    }
    /*MODIFIED-END by sichao.hu,BUG-1951866*/

    private final GestureDetectionCallback mGestureCallback=new GestureDetectionCallback() {
        @Override
        public void onGestureDetected(final Boolean detected,final GestureType type, final Rect bound) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mIsInIntentReviewUI || mUI.isCountingDown()) {
                        Log.v(TAG, "in intent review UI or counting down");
                        return;
                    }
                    if(bound!= null) {
                        Log.v(TAG, "bound is " + bound);
                        mHandler.removeMessages(MSG_HIDE_GESTURE);
                        mUI.showGesture(bound,mCameraSettings.getCurrentPreviewSize());
                        mHandler.sendEmptyMessageDelayed(MSG_HIDE_GESTURE,HIDE_GESTURE_DELAY);
                    }else{
                        mHandler.removeMessages(MSG_HIDE_GESTURE);
                        mUI.hideGesture();
                    }
                    if(detected) {
                        if (mCameraState == IDLE) {
                            Log.w(TAG,"GestureCore type is "+type.name());
                            doShutterButtonClick(true);
                            HelpTipsManager helpTipsManager = mAppController.getHelpTipsManager();
                            if(helpTipsManager != null && helpTipsManager.isHelpTipShowExist()){
                                helpTipsManager.gestureShotResponse();
                            }
                        }
                    }
                }
            });
        }
    };

    private void initGestureSuite(){
        mGestureInstructions=new GestureInstructionWrapper(mActivity.getApplicationContext());
        mGestureInstructions.initGestureWrapper();
        Size previewSize=mCameraSettings.getCurrentPreviewSize();
        Log.w(TAG,"preview size is "+previewSize.toString());
        mGestureInstructions.updateParameters(previewSize.width(), previewSize.height());
        mGestureInstructions.setGestureDetectionCallback(mGestureCallback);
    }

    private static class GestureHandlerThread extends HandlerThread{


        public GestureHandlerThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            try {
                super.run();
            }catch(Exception e){
                Log.e(TAG,"Gesture engine encounter a fatal error , ignore it");
            }
        }
    }

    public static boolean firstFrame = true;
    /**
     * The start/stop preview should only run on the UI thread.
     */
    protected void startPreview() { // MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
        if (mCameraDevice == null) {
            Log.i(TAG, "attempted to start preview before camera device");
            // do nothing
            return;
        }

        android.util.Log.w("AndCamAgntImp", "setup preview");
        if (!checkPreviewPreconditions()) {
            return;
        }

        setDisplayOrientation();


        if (!mSnapshotOnIdle) {
            // If the focus mode is continuous autofocus, call cancelAutoFocus
            // to resume it because it may have been paused by autoFocus call.
            if (mFocusManager.getFocusMode(mCameraSettings.getCurrentFocusMode()) ==
                    CameraCapabilities.FocusMode.CONTINUOUS_PICTURE) {
                if(mCameraState!=AE_AF_LOCKED) {
                    mCameraDevice.cancelAutoFocus();
                }
            }
            mFocusManager.setAeAwbLock(false); // Unlock AE and AWB.
        }
        if(!mActivity.getCameraProvider().isBoostPreview()) {
            mCameraDevice.setPreviewTexture(getTexture()); // MODIFIED by sichao.hu, 2016-08-16,BUG-2743263
        }

        firstFrame = false;
        // Nexus 4 must have picture size set to > 640x480 before other
        // parameters are set in setCameraParameters, b/18227551. This call to
        // updateParametersPictureSize should occur before setCameraParameters
        // to address the issue.
        updateParametersPictureSize();

        mCameraSettings.isInstantAEC=true;
        setCameraParameters(UPDATE_PARAM_ALL);
        mCameraSettings.isInstantAEC=false;

        if (isEnableGestureRecognization() && ExtBuild.device() != ExtBuild.MTK_MT6755) {
            synchronized (GESTURE_LOCK) {
                if (mGesturehandlerThread == null || !mGesturehandlerThread.isAlive()) {
                    Log.w(TAG, "GestureCore open looper , tray start thread");
                    mGesturehandlerThread = new GestureHandlerThread(
                            GESTURE_HANDLER_NAME);
                    mGesturehandlerThread.start();
                    mGestureHandler = new Handler(mGesturehandlerThread.getLooper());
                }
                mCameraDevice.setPreviewDataCallback(mGestureHandler, this);
            }
        }

        Log.i(TAG, "startPreview");
        // If we're using API2 in portability layers, don't use startPreviewWithCallback()
        // b/17576554
        CameraAgent.CameraStartPreviewCallback startPreviewCallback =
            new CameraAgent.CameraStartPreviewCallback() {
                @Override
                public void onPreviewStarted() {
                    mFocusManager.onPreviewStarted();
                    PhotoModule.this.onPreviewStarted();
                    SessionStatsCollector.instance().previewActive(true);
                    if (mSnapshotOnIdle) {
                        Log.v(TAG,"postSnapRunnable");
                        mHandler.post(mDoSnapRunnable);
                    }
                }
            };
        if (GservicesHelper.useCamera2ApiThroughPortabilityLayer(mActivity)) {
            mCameraDevice.startPreview();
            startPreviewCallback.onPreviewStarted();
        } else {
            if(!mActivity.getCameraProvider().isBoostPreview()) {
                Log.w(TAG,"KPI normal start preview");
                mCameraDevice.startPreviewWithCallback(new Handler(Looper.getMainLooper()),
                        startPreviewCallback);
            }else {
                Log.w(TAG,"KPI boost start preview");
                mCameraDevice.waitPreviewWithCallback(new Handler(Looper.getMainLooper()),
                        startPreviewCallback);
            }
        }
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-05-21,BUG-2197382*/
        if (mCameraState == PREVIEW_STOPPED) {
            setCameraState(PREVIEW_PENDING_START);//once set to this state , photoModule is responsible for stopping preview // MODIFIED by sichao.hu, 2016-05-18,BUG-2145791
        }
        /* MODIFIED-END by yuanxing.tan,BUG-2197382*/

        if (isEnableGestureRecognization() && ExtBuild.device() == ExtBuild.MTK_MT6755) {
            // use system default gesture.
            mCameraDevice.setGestureCallback(mHandler, new CameraAgent.CameraGDCallBack() {
                public void onGesture() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mIsInIntentReviewUI || mUI.isCountingDown()) {
                                Log.v(TAG, "in intent review UI or counting down");
                                return;
                            }
                            if (mCameraState == IDLE) {
                                // ToastUtil.showToast(mActivity,"GESTURE CALLBACK",Toast.LENGTH_LONG);
                                doShutterButtonClick(true);
                            }
                        }
                    });
                }
            });
            mCameraDevice.startGestureDetection();
        }
    }

    /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
    protected SurfaceTexture getTexture(){
        return mActivity.getCameraAppUI().getSurfaceTexture();
    }

    @Override
    public void onSurfaceAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        //dummy
    }

    @Override
    public void onSurfaceTextureChanged(SurfaceTexture surfaceTexture, int w, int h) {
        //dummy, used in case of OpenGL in using
    }
    /* MODIFIED-END by sichao.hu,BUG-2743263*/

    @Override
    public void stopPreview() {
        if (mCameraDevice != null && mCameraState != PREVIEW_STOPPED) {
            Log.i(TAG, "stopPreview");
            stopFaceDetection();
            mCameraDevice.stopPreview();
            mActivity.clearBoost();
            if (ExtBuild.device() == ExtBuild.MTK_MT6755&&isEnableGestureRecognization()) {
                mCameraDevice.stopGestureDetection();
            }
        }

        dismissButtonGroupBar(false);
        clearAspectRatioViewer(false);

        setCameraState(PREVIEW_STOPPED);
        synchronized (GESTURE_LOCK) {
            if (mGesturehandlerThread != null) {
                mCameraDevice.setPreviewDataCallback(mGestureHandler, null);
                mGesturehandlerThread.quitSafely();
                mGesturehandlerThread = null;
                mGestureHandler = null;
                mIsRecognizationRunning = false;
                if (mGestureInstructions != null) {
                    Log.w(TAG,"release engine in app");
                    mGestureInstructions.releaseEngine();
                    mGestureInstructions = null;
                }
            }
        }
        if (mFocusManager != null) {
            mFocusManager.onPreviewStopped();
        }
        SessionStatsCollector.instance().previewActive(false);
    }

    @Override
    public void onSettingChanged(SettingsManager settingsManager, String key) {

        String moduleScope = mAppController.getModuleScope();
        switch (key) {
            case Keys.KEY_CAMERA_ID:
                if (isOptimizeSwitchCamera)
                    return;
            case Keys.KEY_CAMERA_POSE:
                getPhotoUI().showPose();
                return;
            case Keys.KEY_CAMERA_GRID_LINES:
                if (Keys.areGridLinesOn(mAppController.getSettingsManager()) &&
                        isGridLinesEnabled()) {
                    mAppController.getCameraAppUI().showGridLines();
                } else {
                    mAppController.getCameraAppUI().hideGridLines();
                }
                return;
            case Keys.KEY_CAMERA_MIRROR_SELFIE:
                // Not every module needs mirror effect, use updateFrontPhotoFlipMode() here instead.
                // if (isCameraFrontFacing()) {
                //     mCameraSettings.setMirrorSelfieOn(Keys.isMirrorSelfieOn(mAppController.getSettingsManager()));
                // }
                updateFrontPhotoFlipMode();
                return;
            case Keys.KEY_CAMERA_HDR_AUTO:
                Keys.setHdrState(settingsManager, mActivity,
                        settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL, key, false));
                return;
            case Keys.KEY_RECORD_LOCATION:
            case Keys.KEY_COUNTDOWN_DURATION:
            case Keys.KEY_SOUND:
                return;
            case Keys.KEY_CAMERA_FACEBEAUTY:
                if (Keys.isFacebeautyOn(mAppController.getSettingsManager()) &&
                        (moduleScope.endsWith(NormalPhotoModule.AUTO_MODULE_STRING_ID)) && isCameraFrontFacing()) {
                    getPhotoUI().showFacebeauty();
                } else {
                    getPhotoUI().hideFacebeauty();
                }
                break;
            case Keys.KEY_CAMERA_HDR:
                onAutoHdrSwitching();
                break;
        }

        if (key.equals(Keys.KEY_FLASH_MODE)) {
            updateParametersFlashMode();
        }

        String pictureSizeKey = isCameraFrontFacing() ?
                Keys.KEY_PICTURE_SIZE_FRONT
                : Keys.KEY_PICTURE_SIZE_BACK;
        if (key.equals(pictureSizeKey)) {
            clearLockedFocus(); // MODIFIED by xuan.zhou, 2016-11-03,BUG-3311864
            updateParametersPictureSize();
        }
        updateParametersSceneMode();
        updateVisidionMode();
        if (mCameraDevice != null) {
            mCameraDevice.applySettings(mCameraSettings);
        }
    }

    /* MODIFIED-BEGIN by xuan.zhou, 2016-11-03,BUG-3311864*/
    protected void clearLockedFocus() {
        if (mCameraState == AE_AF_LOCKED
                || mCameraState == SCANNING_FOR_AE_AF_LOCK
                || mCameraState == FOCUSING) {
            setCameraState(IDLE);
            if (mFocusManager != null) {
                mFocusManager.cancelAutoFocus();
            }
        }
    }
    /* MODIFIED-END by xuan.zhou,BUG-3311864*/

    /* MODIFIED-BEGIN by xuyang.liu, 2016-10-13,BUG-3110198*/
    private void onAutoHdrSwitching() {
        SettingsManager settingsManager = mActivity.getSettingsManager();
        if (GcamHelper.hasGcamAsSeparateModule()) {
            // Set the camera setting to default backfacing.
            settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL, Keys.KEY_CAMERA_ID);
            switchToGcamCapture();
        } else {
            if (isHdrShow()) {
                if (Keys.isHdrOn(mAppController.getSettingsManager(), mActivity)) {
                    settingsManager.set(mAppController.getCameraScope(), Keys.KEY_SCENE_MODE,
                            mCameraCapabilities.getStringifier().stringify(
                                    CameraCapabilities.SceneMode.HDR));
                } else if (Keys.isHdrAuto(mAppController.getSettingsManager(), mActivity)) {
                    settingsManager.set(mAppController.getCameraScope(), Keys.KEY_SCENE_MODE,
                            mCameraCapabilities.getStringifier().stringify(
                                    CameraCapabilities.SceneMode.HDR_AUTO));
                } else {
                    settingsManager.set(mAppController.getCameraScope(), Keys.KEY_SCENE_MODE,
                            mCameraCapabilities.getStringifier().stringify(
                                    CameraCapabilities.SceneMode.AUTO));
                }
            }
            if (mCameraState == AE_AF_LOCKED || mCameraState == FOCUSING || mCameraState == SCANNING_FOR_AE_AF_LOCK) {
                setCameraState(IDLE);
                mFocusManager.cancelAutoFocus();
            }
            updateSceneMode();
        }
    }
    /* MODIFIED-END by xuyang.liu,BUG-3110198*/

    private void updateCameraParametersInitialize() {
        // Reset preview frame rate to the maximum because it may be lowered by
        // video camera application.
        int[] fpsRange = CameraUtil.getPhotoPreviewFpsRange(mCameraCapabilities);
        if (fpsRange != null && fpsRange.length > 0) {
            mCameraSettings.setPreviewFpsRange(fpsRange[0], fpsRange[1]);
        }

        mCameraSettings.setRecordingHintEnabled(false);

        if (mCameraCapabilities.supports(CameraCapabilities.Feature.VIDEO_STABILIZATION)) {
            mCameraSettings.setVideoStabilization(false);
        }
    }

    private void updateCameraParametersZoom() {
        // Set zoom.
        if (mCameraCapabilities.supports(CameraCapabilities.Feature.ZOOM)) {
            mCameraSettings.setZoomRatio(mZoomValue);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setAutoExposureLockIfSupported() {
        if (mAeLockSupported) {
            /* MODIFIED-BEGIN by jianying.zhang, 2016-11-10,BUG-3398235*/
            Log.w(TAG,"lock ae awb ? "+mFocusManager.getAeAwbLock() + " " + !mUI.isMeteringDragging());
            mCameraSettings.setAutoExposureLock(mFocusManager.getAeAwbLock());
            if (mFocusManager.getAeAwbLock()) {
                setAeFlashEnableWithLock(true);
            } else {
                if (!mUI.isMeteringDragging()) {
                    setAeFlashEnableWithLock(false);
                }
            }
        }
    }

    private void setAeFlashEnableWithLock(boolean lock) {
        if (ExtBuild.isPlatformMTK()) {
            Log.d(TAG, "It's MTK Plat. reture");
            return;
        }
        if (lock) {
            if (mAeFlashLock == null) {
                dismissButtonGroupBar(false);
                mAeFlashLock = mAppController.getCameraAppUI().getTopMenus()
                        .disableButtonWithLock(TopMenus.BUTTON_FLASH);
            }
        } else {
            if (mAeFlashLock != null) {
                mAppController.getCameraAppUI().getTopMenus()
                        .enableButtonWithToken(TopMenus.BUTTON_FLASH, mAeFlashLock);
                mAeFlashLock = null;
            }
        }
    }
    /* MODIFIED-END by jianying.zhang,BUG-3398235*/
    private void unlockAutoExposureLockIfSupported(){
        if (mAeLockSupported) {
            mCameraSettings.setAutoExposureLock(mFocusManager.getAeAwbLock());
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setAutoWhiteBalanceLockIfSupported() {
        if (mAwbLockSupported) {
            mCameraSettings.setAutoWhiteBalanceLock(mFocusManager.getAeAwbLock());
        }
    }

    private void setFocusAreasIfSupported() {
        if (mFocusAreaSupported) {
            mCameraSettings.setFocusAreas(mFocusManager.getFocusAreas());
        }
    }

    private void setMeteringAreasIfSupported() {
        if (mMeteringAreaSupported) {
            mCameraSettings.setMeteringAreas(mFocusManager.getMeteringAreas());
        }
    }
    public boolean isZslOn() {
        return true;
    }
    private void updateCameraParametersPreference() {
        // some monkey tests can get here when shutting the app down
        // make sure mCameraDevice is still valid, b/17580046
        if (mCameraDevice == null) {
            return;
        }

        if(mCameraCapabilities.isZslSupported()) {
            mCameraSettings.isZslOn = isZslOn();
        }
        mCameraSettings.setHsr("off");
        setAutoExposureLockIfSupported();
        setAutoWhiteBalanceLockIfSupported();
        setFocusAreasIfSupported();
        setMeteringAreasIfSupported();

        // Initialize focus mode.
        if(mCameraState!=AE_AF_LOCKED) {
            Log.w(TAG,"focus mode is "+mFocusManager.getFocusMode(mCameraSettings.getCurrentFocusMode()));
            updateParametersFocusMode();
            SessionStatsCollector.instance().autofocusActive(
                    mFocusManager.getFocusMode(mCameraSettings.getCurrentFocusMode()) ==
                            CameraCapabilities.FocusMode.CONTINUOUS_PICTURE
            );
        }

        // Set JPEG quality.
        updateParametersPictureQuality();

        // For the following settings, we need to check if the settings are
        // still supported by latest driver, if not, ignore the settings.

        // Set exposure compensation
        updateParametersExposureCompensation();

        // Set the scene mode: also sets flash and white balance.
        updateParametersSceneMode();

        updateParametersAntibanding();

        updateFrontPhotoFlipMode();

        if (mContinuousFocusSupported && ApiHelper.HAS_AUTO_FOCUS_MOVE_CALLBACK) {
            updateAutoFocusMoveCallback();
        }
    }
    public static final int SKIN_SMOOTHING_DEFAULT = 50;
    public static final int SKIN_SMOOTHING_MAX = 90;
    public static final int SKIN_SMOOTHING_RANGE = 100;

    /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-26,BUG-1996450*/
    public static final int SKIN_WHITE_DEFAULT = 50;
    public static final int SKIN_WHITE_MAX = 90;
    public static final int SKIN_WHITE_RANGE = 100;
    /* MODIFIED-END by bin.zhang2-nb,BUG-1996450*/

    public void updateFaceBeautyWhenFrameReady() {
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_FACE_BEAUTY, 250);
    }
    final private void updateVisidionMode(){
        //coherent settings in settingsManager and cameraSettings here
        if (!isVisidonModeEnabled()) {
            return;
        }
        SettingsManager settingsManager = mAppController.getSettingsManager();
        if (null == mCameraSettings) return;// MODIFIED by bin.zhang2-nb, 2016-03-24,BUG-1858745
        if (isCameraFrontFacing()) {
            /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-26,BUG-1996450*/
            if (ExtBuild.device() != ExtBuild.MTK_MT6755) {
                int defSkinSmoothing = CustomUtil.getInstance().getInt(CustomFields.DEF_CAMERA_SKIN_SMOOTHING, SKIN_SMOOTHING_DEFAULT);
                int skinSmoothing = mActivity.getSettingsManager().getInteger(SettingsManager.SCOPE_GLOBAL, Keys.KEY_FACEBEAUTY_SKIN_SMOOTHING, defSkinSmoothing);
                if (isOptimizeSwitchCamera) {
                    if (firstFrame)
                        mCameraSettings.setFaceBeauty(Keys.isFacebeautyOn(settingsManager), skinSmoothing * SKIN_SMOOTHING_MAX / SKIN_SMOOTHING_RANGE);
                    else
                        mCameraSettings.setFaceBeauty(false, SKIN_SMOOTHING_DEFAULT);
                } else
                    mCameraSettings.setFaceBeauty(Keys.isFacebeautyOn(settingsManager), skinSmoothing * SKIN_SMOOTHING_MAX / SKIN_SMOOTHING_RANGE);
            } else {
                int defSkinSmoothing = CustomUtil.getInstance().getInt(CustomFields.DEF_CAMERA_SKIN_SMOOTHING, SKIN_SMOOTHING_DEFAULT);
                int skinSmoothing = mActivity.getSettingsManager().getInteger(SettingsManager.SCOPE_GLOBAL, Keys.KEY_FACEBEAUTY_SKIN_SMOOTHING, defSkinSmoothing);
                Log.d(TAG, "[updateVisidionMode] defSkinSmoothing=" + defSkinSmoothing + ", skinSmoothing=" + skinSmoothing);
                if (isOptimizeSwitchCamera) {
                    if (firstFrame)
                        mCameraSettings.setFaceBeautySmoothing(Keys.isFacebeautyOn(settingsManager), skinSmoothing * SKIN_SMOOTHING_MAX / SKIN_SMOOTHING_RANGE);
                    else
                        mCameraSettings.setFaceBeautySmoothing(false, SKIN_SMOOTHING_DEFAULT);
                } else
                    mCameraSettings.setFaceBeautySmoothing(Keys.isFacebeautyOn(settingsManager), skinSmoothing * SKIN_SMOOTHING_MAX / SKIN_SMOOTHING_RANGE);

                int defSkinWhitening = CustomUtil.getInstance().getInt(CustomFields.DEF_CAMERA_SKIN_WHITE, SKIN_WHITE_DEFAULT);
                int skinWhitening = mActivity.getSettingsManager().getInteger(SettingsManager.SCOPE_GLOBAL, Keys.KEY_FACEBEAUTY_SKIN_WHITE, defSkinWhitening);
                Log.d(TAG, "[updateVisidionMode] defSkinWhitening=" + defSkinWhitening + ", skinWhitening=" + skinWhitening);
                if (isOptimizeSwitchCamera) {
                    if (firstFrame)
                        mCameraSettings.setFaceBeautyWhitening(Keys.isFacebeautyOn(settingsManager), skinWhitening * SKIN_WHITE_MAX / SKIN_WHITE_RANGE);
                    else
                        mCameraSettings.setFaceBeautyWhitening(false, SKIN_WHITE_DEFAULT);
                } else
                    mCameraSettings.setFaceBeautyWhitening(Keys.isFacebeautyOn(settingsManager), skinWhitening * SKIN_WHITE_MAX / SKIN_WHITE_RANGE);
            }
            /* MODIFIED-END by bin.zhang2-nb,BUG-1996450*/
            mCameraSettings.setLowLight(false);
            mCameraSettings.setSuperResolutionOn(false);
        } else {
            if (Keys.isHdrOn(settingsManager,mActivity) || Keys.isHdrAuto(settingsManager,mActivity)) { // MODIFIED by xuyang.liu, 2016-10-13,BUG-3110198
                mCameraSettings.setLowLight(false);
            } else if (Keys.isLowlightOn(settingsManager, mAppController.getCameraScope())) {
                mCameraSettings.setLowLight(true);
            } else {
                mCameraSettings.setLowLight(false);
            }
            /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-26,BUG-1996450*/
            if (ExtBuild.device() != ExtBuild.MTK_MT6755) {
                mCameraSettings.setFaceBeauty(false, SKIN_SMOOTHING_DEFAULT);
            } else {
                mCameraSettings.setFaceBeautySmoothing(false, SKIN_SMOOTHING_DEFAULT);
                mCameraSettings.setFaceBeautyWhitening(false, SKIN_WHITE_DEFAULT);
            }
            /* MODIFIED-END by bin.zhang2-nb,BUG-1996450*/
            if (mZoomValue > mSuperZoomThreshold && isSuperResolutionEnabled()) {
                mCameraSettings.setSuperResolutionOn(true);
            } else {
                mCameraSettings.setSuperResolutionOn(false);
            }
        }
    }

    protected CameraCapabilities.FocusMode getOverrideFocusMode(){
        return null;
    }

    protected String getPictureSize() {
        SettingsManager settingsManager = mActivity.getSettingsManager();
        String pictureSizeKey = isCameraFrontFacing() ? Keys.KEY_PICTURE_SIZE_FRONT
                : Keys.KEY_PICTURE_SIZE_BACK;
        // If the size from pictureSizeKey is null, use the default size from plf instead.
        String defaultPicSize = SettingsUtil.getDefaultPictureSize(isCameraFrontFacing());
        return settingsManager.getString(SettingsManager.SCOPE_GLOBAL,
                pictureSizeKey, defaultPicSize);
    }

    protected Size getPreviewSize(Size size) {
        // Set a preview size that is closest to the viewfinder height and has
        // the right aspect ratio.
        List<Size> sizes = mCameraCapabilities.getSupportedPreviewSizes();
        Size optimalSize = CameraUtil.getOptimalPreviewSize(mActivity, sizes,
                (double) size.width() / size.height());
        return optimalSize;
    }
    /**
     * This method sets picture size parameters. Size parameters should only be
     * set when the preview is stopped, and so this method is only invoked in
     * {@link #startPreview()} just before starting the preview.
     */
    protected void updateParametersPictureSize() {
        if (mCameraDevice == null) {
            Log.w(TAG, "attempting to set picture size without camera device");
            return;
        }


        String pictureSize = getPictureSize();
//        CameraPictureSizesCacher.updateSizesForCamera(mAppController.getAndroidContext(),
//                mCameraDevice.getCameraId(), supported);
//        SettingsUtil.setCameraPictureSize(pictureSize, supported, mCameraSettings,
//                mCameraDevice.getCameraId());

//        Size size = SettingsUtil.getPhotoSize(pictureSize, supported,
//                mCameraDevice.getCameraId());
        Size size = SettingsUtil.sizeFromString(pictureSize);
        mCameraSettings.setPhotoSize(size);
        if (ApiHelper.IS_NEXUS_5) {
            if (ResolutionUtil.NEXUS_5_LARGE_16_BY_9.equals(pictureSize)) {
                mShouldResizeTo16x9 = true;
            } else {
                mShouldResizeTo16x9 = false;
            }
        }

        Size optimalSize = getPreviewSize(size);
        Size original = mCameraSettings.getCurrentPreviewSize();
        Log.w(TAG,String.format("KPI original size is %s, optimal size is %s",original.toString(),optimalSize.toString()));
        if (!optimalSize.equals(original)) {
            Log.v(TAG, "setting preview size. optimal: " + optimalSize + "original: " + original);
            mCameraSettings.setPreviewSize(optimalSize);
            mCameraDevice.applySettings(mCameraSettings);
            mCameraSettings = mCameraDevice.getSettings();
            if(mCameraSettings==null){
                Log.e(TAG,"camera setting is null ?");
            }
        }

        if (optimalSize.width() != 0 && optimalSize.height() != 0) {
            Log.v(TAG, "updating aspect ratio");
            mUI.updatePreviewAspectRatio((float) optimalSize.width()
                    / (float) optimalSize.height());
        }
        Log.d(TAG, "Preview size is " + optimalSize);
    }

    private void updateParametersPictureQuality() {
        int jpegQuality = CameraProfile.getJpegEncodingQualityParameter(mCameraId,
                CameraProfile.QUALITY_HIGH);
        mCameraSettings.setPhotoJpegCompressionQuality(jpegQuality);
    }

    private void updateParametersExposureCompensation() {
        SettingsManager settingsManager = mActivity.getSettingsManager();
        if (isExposureSidebarEnabled()) {
            setExposureCompensation(mSideEV, false);
        } else if (settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_EXPOSURE_COMPENSATION_ENABLED)) {
            int value = settingsManager.getInteger(mAppController.getCameraScope(),
                    Keys.KEY_EXPOSURE);
            int max = mCameraCapabilities.getMaxExposureCompensation();
            int min = mCameraCapabilities.getMinExposureCompensation();
            if (value >= min && value <= max) {
                mCameraSettings.setExposureCompensationIndex(value);
            } else {
                Log.w(TAG, "invalid exposure range: " + value);
            }
        } else {
            // If exposure compensation is not enabled, reset the exposure compensation value.
            setExposureCompensation(0);
        }

    }

    protected void updateParametersAntibanding() {
        SettingsManager settingsManager = mActivity.getSettingsManager();
        String defAntibanding=settingsManager.getString(SettingsManager.SCOPE_GLOBAL, Keys.KEY_ANTIBANDING).toLowerCase().replace(" ", "");
        String auto = mActivity.getResources().getString(R.string.pref_camera_antibanding_default);
        List<String> supported = mCameraCapabilities.getSupportedAntibanding();
        if (supported != null && supported.indexOf(defAntibanding) >= 0) {
            mCameraSettings.setAntibanding(defAntibanding);
        } else if (supported != null && supported.indexOf(auto) >= 0) {
            mCameraSettings.setAntibanding(auto);
        }
    }

    protected void updateParametersSceneMode() {
        CameraCapabilities.Stringifier stringifier = mCameraCapabilities.getStringifier();
        SettingsManager settingsManager = mActivity.getSettingsManager();

        mSceneMode = stringifier.
            sceneModeFromString(settingsManager.getString(mAppController.getCameraScope(),
                    Keys.KEY_SCENE_MODE));
        if(isHdrShow()) {
            if(Keys.isHdrOn(settingsManager,mActivity)) {
                if (mSceneMode != CameraCapabilities.SceneMode.HDR) {
                    mSceneMode = CameraCapabilities.SceneMode.HDR;
                    settingsManager.set(mAppController.getCameraScope(), Keys.KEY_SCENE_MODE,
                            mCameraCapabilities.getStringifier().stringify(
                                    mSceneMode));
                }
            } else if(Keys.isHdrAuto(settingsManager,mActivity)){
                if (mSceneMode != CameraCapabilities.SceneMode.HDR_AUTO) {
                    mSceneMode = CameraCapabilities.SceneMode.HDR_AUTO;
                    settingsManager.set(mAppController.getCameraScope(), Keys.KEY_SCENE_MODE,
                            mCameraCapabilities.getStringifier().stringify(
                                    mSceneMode));
                }
            } else {
                if (mSceneMode == CameraCapabilities.SceneMode.HDR
                        || mSceneMode == CameraCapabilities.SceneMode.HDR_AUTO ) {
                    mSceneMode = CameraCapabilities.SceneMode.AUTO;
                    settingsManager.set(mAppController.getCameraScope(), Keys.KEY_SCENE_MODE,
                            mCameraCapabilities.getStringifier().stringify(
                                    mSceneMode));
                }
            }
        } else {
            if (mSceneMode == CameraCapabilities.SceneMode.HDR
                    || mSceneMode == CameraCapabilities.SceneMode.HDR_AUTO) {
                mSceneMode = CameraCapabilities.SceneMode.AUTO;
                settingsManager.set(mAppController.getCameraScope(), Keys.KEY_SCENE_MODE,
                        mCameraCapabilities.getStringifier().stringify(
                                mSceneMode));
            }
        }
        if (mCameraCapabilities.supports(mSceneMode)) {
            if (mCameraSettings.getCurrentSceneMode() != mSceneMode) {
                mCameraSettings.setSceneMode(mSceneMode);

                // Setting scene mode will change the settings of flash mode,
                // white balance, and focus mode. Here we read back the
                // parameters, so we can know those settings.
                if (mCameraDevice != null) {
                    mCameraDevice.applySettings(mCameraSettings);
                    mCameraSettings = mCameraDevice.getSettings();
                }
                mCameraSettings.setSceneMode(mSceneMode); // MODIFIED by yuanxing.tan, 2016-04-23,BUG-1987407
            }
        } else {
            mSceneMode = mCameraSettings.getCurrentSceneMode();
            if (mSceneMode == null) {
                mSceneMode = CameraCapabilities.SceneMode.AUTO;
            }
        }
        if (CameraCapabilities.SceneMode.AUTO == mSceneMode || CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_ENABLE_TS_HDR,false)) { // MODIFIED by yuanxing.tan, 2016-04-21,BUG-1985199
            // Set flash mode.
            updateParametersFlashMode();

            // Set focus mode.
            updateParametersFocusMode();
        } else {
            mFocusManager.overrideFocusMode(mCameraSettings.getCurrentFocusMode());
        }
        //updateMetaDataCallback(); // MODIFIED by xuyang.liu, 2016-10-13,BUG-3110198
    }
    protected void updateParametersFocusMode() {
        mFocusManager.overrideFocusMode(getOverrideFocusMode());
        mCameraSettings.setFocusMode(
                mFocusManager.getFocusMode(mCameraSettings.getCurrentFocusMode()));
    }
    protected boolean updateParametersFlashMode() {
        boolean bNeedUpdate = false;
        SettingsManager settingsManager = mActivity.getSettingsManager();

        CameraCapabilities.FlashMode flashMode = mCameraCapabilities.getStringifier()
                .flashModeFromString(settingsManager.getString(mAppController.getCameraScope(),
                        Keys.KEY_FLASH_MODE));
        if (flashMode != CameraCapabilities.FlashMode.OFF) {
            bNeedUpdate = true;
        }

        if (bNeedUpdate) {
            // If current flash mode is torch, set it off first.
            if (mCameraSettings != null) {
                CameraCapabilities.FlashMode mCurrentFlashMode = mCameraSettings.getCurrentFlashMode();
                if (CameraCapabilities.FlashMode.TORCH == mCurrentFlashMode) {
                    mCameraSettings.setFlashMode(CameraCapabilities.FlashMode.OFF);
                    if (mCameraDevice != null) {
                        mCameraDevice.applySettings(mCameraSettings);
                    }
                }
            }
        }

        if (mCameraCapabilities.supports(flashMode) && mActivity.currentBatteryStatusOK() && isFlashShow() && (mZoomValue <= mSuperZoomThreshold || !isSuperResolutionEnabled())) {
            mCameraSettings.setFlashMode(flashMode);
        } else {
            if(mCameraCapabilities.supports(CameraCapabilities.FlashMode.OFF)) {
                mCameraSettings.setFlashMode(CameraCapabilities.FlashMode.OFF);
            }else{
                mCameraSettings.setFlashMode(CameraCapabilities.FlashMode.NO_FLASH);
            }
        }
        return bNeedUpdate;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void updateAutoFocusMoveCallback() {
        if (mCameraDevice == null||mCameraState==AE_AF_LOCKED) {
            return;
        }
        if (mCameraSettings.getCurrentFocusMode() ==
                CameraCapabilities.FocusMode.CONTINUOUS_PICTURE) {
            mCameraDevice.setAutoFocusMoveCallback(mHandler,
                    (CameraAFMoveCallback) mAutoFocusMoveCallback);
        } else {
            mCameraDevice.setAutoFocusMoveCallback(null, null);
        }
    }

    /**
     * Sets the exposure compensation to the given value and also updates settings.
     *
     * @param value exposure compensation value to be set
     */
    public void setExposureCompensation(int value,boolean needCache) {
        int max = mCameraCapabilities.getMaxExposureCompensation();
        int min = mCameraCapabilities.getMinExposureCompensation();
        if (value >= min && value <= max) {
            Log.w(TAG, "setExposureCompensation for " + value);
            mCameraSettings.setExposureCompensationIndex(value);
            SettingsManager settingsManager = mActivity.getSettingsManager();
            if(needCache) {
                settingsManager.set(mAppController.getCameraScope(),
                        Keys.KEY_EXPOSURE, value);
            }
        } else {
            Log.w(TAG, "invalid exposure range: " + value);
        }
    }

    private void setExposureCompensation(int value){
        if(mCameraState==AE_AF_LOCKED||mCameraState==SCANNING_FOR_AE_AF_LOCK) {
            setExposureCompensation(mLockedEvoIndex, false);
        } else {
            setExposureCompensation(value, true);
        }
    }

    // We separate the parameters into several subsets, so we can update only
    // the subsets actually need updating. The PREFERENCE set needs extra
    // locking because the preference can be changed from GLThread as well.
    protected void setCameraParameters(int updateSet) {
        if ((updateSet & UPDATE_PARAM_INITIALIZE) != 0) {
            updateCameraParametersInitialize();
        }

        if ((updateSet & UPDATE_PARAM_ZOOM) != 0) {
            updateCameraParametersZoom();
        }

        if ((updateSet & UPDATE_PARAM_PREFERENCE) != 0) {
            updateCameraParametersPreference();
        }

        if ((updateSet & UPDATE_PARAM_VISIDON) != 0) {
            updateVisidionMode();
        }

        if (mCameraDevice != null) {
            mCameraDevice.applySettings(mCameraSettings);
        }
    }

    // If the Camera is idle, update the parameters immediately, otherwise
    // accumulate them in mUpdateSet and update later.
    private void setCameraParametersWhenIdle(int additionalUpdateSet) {
        mUpdateSet |= additionalUpdateSet;
        if (mCameraDevice == null) {
            // We will update all the parameters when we open the device, so
            // we don't need to do anything now.
            mUpdateSet = 0;
            return;
        } else if (isCameraIdle()) {
            setCameraParameters(mUpdateSet);
            updateSceneMode();
            mUpdateSet = 0;
        } else {
            if (!mHandler.hasMessages(MSG_SET_CAMERA_PARAMETERS_WHEN_IDLE)) {
                mHandler.sendEmptyMessageDelayed(MSG_SET_CAMERA_PARAMETERS_WHEN_IDLE, 1000);
            }
        }
    }

    @Override
    public boolean isCameraIdle() {
        return (mCameraState == IDLE) ||
                (mCameraState == PREVIEW_STOPPED) ||
                (mCameraState == AE_AF_LOCKED)||
                (mCameraState == SCANNING_FOR_AE_AF_LOCK)||
                (((mFocusManager != null) && mFocusManager.isFocusCompleted())
                && (mCameraState != SWITCHING_CAMERA));
    }

    public boolean canCloseCamera(){//It's suggested that we can exit camera even it's doing focus
        return isCameraIdle()||mCameraState==FOCUSING;
    }

    @Override
    public boolean isImageCaptureIntent() {
        String action = mActivity.getIntent().getAction();
        return (MediaStore.ACTION_IMAGE_CAPTURE.equals(action)
                || CameraActivity.MST_SCAN_BUSINESSCARD_ACTION.equals(action)
                || CameraActivity.ACTION_IMAGE_CAPTURE_SECURE.equals(action));
    }

    private void setupCaptureParams() {
        Bundle myExtras = mActivity.getIntent().getExtras();
        if (myExtras != null) {
            mSaveUri = (Uri) myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
            mCropValue = myExtras.getString("crop");
        }
    }

    private void initializeCapabilities() {
        mCameraCapabilities = mCameraDevice.getCapabilities();
        mFocusAreaSupported = mCameraCapabilities.supports(CameraCapabilities.Feature.FOCUS_AREA);
        mMeteringAreaSupported = mCameraCapabilities.supports(CameraCapabilities.Feature.METERING_AREA);
        mAeLockSupported = mCameraCapabilities.supports(CameraCapabilities.Feature.AUTO_EXPOSURE_LOCK);
        Log.w(TAG,"support AE Lock ? "+mAeLockSupported);
        mAwbLockSupported = mCameraCapabilities.supports(CameraCapabilities.Feature.AUTO_WHITE_BALANCE_LOCK);
        mContinuousFocusSupported =
                mCameraCapabilities.supports(CameraCapabilities.FocusMode.CONTINUOUS_PICTURE);
    }

    @Override
    public void onZoomChanged(float ratio) {
        // Not useful to change zoom value when the activity is paused.
        if (mPaused) {
            return;
        }
        clearAspectRatioViewer(false);
        float lastRatio = mZoomValue;
        mZoomValue = ratio;
        if (mCameraSettings == null || mCameraDevice == null) {
            return;
        }
        // Set zoom parameters asynchronously
        mCameraSettings.setZoomRatio(mZoomValue);
        if (mZoomValue > mSuperZoomThreshold && isSuperResolutionEnabled()) {
            mCameraSettings.setSuperResolutionOn(true);

            if(mZoomFlashLock==null) {
                mZoomFlashLock=mAppController.getCameraAppUI()
                        .getTopMenus().disableButtonWithLock(TopMenus.BUTTON_FLASH);
            }
            SettingsManager settingsManager = mActivity.getSettingsManager();
            settingsManager.set(mAppController.getCameraScope(),
                    Keys.KEY_FLASH_MODE, mCameraCapabilities.getStringifier().stringify(
                    CameraCapabilities.FlashMode.OFF));
            // Remove the toast tentatively.
//            if (lastRatio <= mSuperZoomThreshold) {
//                ToastUtil.showToast(mActivity,
//                        mActivity.getString(R.string.super_zoom_on_toast), Toast.LENGTH_SHORT);
//            }
        } else {
            mCameraSettings.setSuperResolutionOn(false);
//            ToastUtil.cancelToast();
/* MODIFIED-BEGIN by yuanxing.tan, 2016-06-18,BUG-2202739*/
//            if (mActivity.currentBatteryStatusOK() && !mAppController.getButtonManager().isEnabled(ButtonManager.BUTTON_FLASH)) {
                if(mZoomFlashLock!=null) {
                    mAppController.getCameraAppUI()
                            .getTopMenus()
                            .enableButtonWithToken(TopMenus.BUTTON_FLASH, mZoomFlashLock);
                    mZoomFlashLock=null;
                }
//            }
/* MODIFIED-END by yuanxing.tan,BUG-2202739*/
        }

        mCameraDevice.applySettings(mCameraSettings);
    }

    @Override
    public int getCameraState() {
        return mCameraState;
    }

    @Override
    public void onMemoryStateChanged(int state) {
        mUnderLowMemory=(state != MemoryManager.STATE_OK);
        mAppController.setShutterEnabled(state == MemoryManager.STATE_OK);
    }

    @Override
    public void onLowMemory() {
        // Not much we can do in the photo module.
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        float[] data;
        if (type == Sensor.TYPE_ACCELEROMETER) {
            data = mGData;
        } else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
            data = mMData;
        } else {
            // we should not be here.
            return;
        }
        for (int i = 0; i < 3; i++) {
            data[i] = event.values[i];
        }
        float[] orientation = new float[3];
        SensorManager.getRotationMatrix(mR, null, mGData, mMData);
        SensorManager.getOrientation(mR, orientation);
        mHeading = (int) (orientation[0] * 180f / Math.PI) % 360;
        if (mHeading < 0) {
            mHeading += 360;
        }
    }

    // For debugging only.
    public void setDebugUri(Uri uri) {
        mDebugUri = uri;
    }

    // For debugging only.
    private void saveToDebugUri(byte[] data) {
        if (mDebugUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = mContentResolver.openOutputStream(mDebugUri);
                outputStream.write(data);
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Exception while writing debug jpeg file", e);
            } finally {
                CameraUtil.closeSilently(outputStream);
            }
        }
    }

    @Override
    public void onRemoteShutterPress() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
//                focusAndCapture();
                onShutterButtonClick();
            }
        });
    }

    /**
     * override by manual mode
     * @return
     */
    protected boolean hideCamera(){
        return false;
    }

    /**
     * override by fyuse mode
     * @return
     */
    protected boolean hideCameraForced(){
        return false;
    }

    protected boolean hideSetting() {
        return isImageCaptureIntent();
    }

    public boolean isShowPose() {
        return false;
    }

    public boolean isShowCompose() {
        return false;
    }

    protected boolean showFilter() {
        return false;
    }

    protected boolean needCountDownIndicatorShow() {
        return false;
    }

    @Override
    public void updateFaceBeautySetting(String key, int value) {
        saveFaceBeautySBValue(key, value);
        setCameraParameters(UPDATE_PARAM_VISIDON);
    }

    private void saveFaceBeautySBValue(String key, int value){
        mActivity.getSettingsManager().set(SettingsManager.SCOPE_GLOBAL, key, value);
    }

    @Override
    public boolean isFacebeautyEnabled() {
        return false;
    }

    @Override
    public boolean isAttentionSeekerShow() {
        return false;
    }

    @Override
    public boolean isGesturePalmShow() {
        return false;
    }

    protected final class BurstShotCheckQueue {
        private SparseArray<Integer> mJobQueue =new SparseArray<>();
        private SparseArray<Integer> mResultQueue =new SparseArray<>();
        private Runnable mSupposePictureTakenAction =null;
        private static final int INVALID_VALUE=-1;
        public void pushToJobQueue(int index){
            mJobQueue.put(index, index);
        }

        public void popToResultQueue(int index){
            int value= mJobQueue.get(index,INVALID_VALUE);
            if(value!=INVALID_VALUE){
                mJobQueue.remove(index);
                mResultQueue.put(index, value);

            }
        }

        private int mCapacity=BURST_MAX;

        /**
         *
         * @param capcacity
         * @return true means still burst saving job in flight , false means all saving job done
         */
        public boolean setCapacity(int capcacity){
            mCapacity=capcacity;
            if(mResultQueue.size()==mCapacity){
                if(mSupposePictureTakenAction !=null){
                    mSupposePictureTakenAction.run();
                    mSupposePictureTakenAction =null;
                }
                mCapacity=BURST_MAX;
                mJobQueue.clear();
                mResultQueue.clear();
                return false;
            }
            return true;
        }

        public void setPictureTakenActionCache(Runnable runnable){
            mSupposePictureTakenAction =runnable;
        }

        public void clearCheckQueue(){
            mCapacity=BURST_MAX;
            mSupposePictureTakenAction =null;
            mJobQueue.clear();
            mResultQueue.clear();
        }

        public int getCapacity(){
            return mCapacity;
        }

    }

    protected class SoundPlay {
        private android.media.SoundPool soundPool = null;
        private int soundID = 0;
        private int streamID = 0;
        private boolean soundEnable;

        protected SoundPlay(boolean s) {
            soundEnable = s;
        }

        public void load() {
            if (!soundEnable) {
                return;
            }
            soundPool = new android.media.SoundPool(10,
                    SoundClips.getAudioTypeForSoundPool(), 0);
            soundID = soundPool.load(mActivity, R.raw.continuous_shot, 1);
        }

        public void play() {
            if (!soundEnable) {
                return;
            }
            if (streamID != 0) {
                soundPool.stop(streamID);
            }
            streamID = soundPool.play(soundID, 0.5f, 0.5f, 1, 0, 1.5f);
        }

        public void unLoad() {
            if (soundEnable && soundPool != null) {
                soundPool.unload(soundID);
                soundPool.release();
                soundPool = null;
            }
        }
    }

    /* MODIFIED-BEGIN by wenhua.tu, 2016-08-11,BUG-2710178*/
    public boolean hdrNightToastEnable(boolean isHdr) {
        if (TestUtils.IS_TEST) {
            return isHdr ? isHdrToastShow() : isNightToastShow();
        }
        return false;
    }

    @Override
    public CameraSettings getCameraSettings() {
        // TODO Auto-generated method stub
        return mCameraSettings;
    }

    @Override
    public CameraCapabilities getCameraCapabilities() {
        // TODO Auto-generated method stub
        return mCameraCapabilities;
    }
    /* MODIFIED-END by wenhua.tu,BUG-2710178*/

    public boolean isExposureSidebarEnabled() {
        return false;
    }

    public boolean isMeteringEnabled() {
        return false;
    }

    // The exposure compensation set in the sidebar.
    private final int DEFAULT_SIDE_EV = 0;
    private int mSideEV = DEFAULT_SIDE_EV;

    private void resetSideEV() {
        if (mHandler != null) {
            mHandler.removeCallbacks(hideExposureSidebar);
        }
        if (mUI != null) {
            mUI.resetExposureSidebar();
        }
        if (mSideEV != DEFAULT_SIDE_EV) {
            mSideEV = DEFAULT_SIDE_EV;
            setCameraParameters(UPDATE_PARAM_PREFERENCE);
        }
    }

    // I wanna do the auto hide in module but not in bar itself.
    Runnable hideExposureSidebar = new Runnable() {
        @Override
        public void run() {
            if (mPaused || mUI == null) {
                return;
            }

            if (mUI.isExposureSidebarVisible()) {
                mUI.setExposureSidebarPrepared(false);
                mUI.fadeOutExposureSidebar();
            }
        }
    };

    @Override
    public void onExposureCompensationChanged(int value) {
        // Almost the same with onEvoChanged except the EvoFlashLock. I don't want to disable flash
        // button now even though it won't be fired when ae locked on Qualcomm device.
        if (mPaused || mCameraDevice == null || mFocusManager == null || !mFirstTimeInitialized) {
            return;
        }

        if (mCameraState == SCANNING_FOR_AE_AF_LOCK && value != DEFAULT_SIDE_EV) {
            setCameraState(AE_AF_LOCKED);
        }

        if (mCameraState != AE_AF_LOCKED ) {
            return;
        }


        clearAspectRatioViewer(false);
        dismissButtonGroupBar(false);

        mHandler.removeCallbacks(hideExposureSidebar);
        mSideEV = value;
        startMotionChecking();
        mFocusManager.setAeAwbLock(true);
        mFocusManager.keepFocusFrame();
        setCameraParameters(UPDATE_PARAM_PREFERENCE);
    }

    @Override
    public void onMeteringStart() {
        if (mSideEV != DEFAULT_SIDE_EV) {
            resetSideEV();
        }

        clearAspectRatioViewer(false);
        dismissButtonGroupBar(false);

        mUI.setExposureSidebarPrepared(false);
        mAppController.getCameraAppUI().setTouchObstruct(true);
    }

    @Override
    public void onMeteringStop() {
        mUI.setExposureSidebarPrepared(true);
        startMotionChecking();
        // I wonder whether should I lock ae here...
        mFocusManager.setAeAwbLock(true);
        setCameraParameters(UPDATE_PARAM_PREFERENCE);
        mAppController.getCameraAppUI().setTouchObstruct(false);
    }

    @Override
    public void onMeteringAreaChanged(int x, int y) {
        // During metering, I think it's OK to stop the checking.
        stopMotionChecking();
        // Make sure ae is not locked when trying to change metering area.
        mFocusManager.setAeAwbLock(false);
        mFocusManager.setMeteringArea(new Point(x, y));
        setCameraParameters(UPDATE_PARAM_PREFERENCE);
    }

    private boolean mResetFocusAfterSnapshot = false;
    @Override
    public void onDeviceMoving() {
        Log.d(TAG, "onDeviceMoving " + mCameraState);
        if (mCameraState == AE_AF_LOCKED) {
            setCameraState(IDLE);
        } else if (mCameraState == SNAPSHOT_IN_PROGRESS_DURING_LOCKED) {
            // If camera state is SNAPSHOT_IN_PROGRESS_DURING_LOCKED, cancel auto focus after the
            // snapshot.
            mResetFocusAfterSnapshot = true;
        }
    }

    private void startMotionChecking() {
        if (mMotionManager != null) {
            mMotionManager.startChecking();
        }
    }

    private void stopMotionChecking() {
        if (mMotionManager != null) {
            mMotionManager.stopChecking();
        }
    }

    @Override
    public void onFlashClicked() {
        super.onFlashClicked();
        clearAspectRatioViewer(true);
        mAppController.getCameraAppUI().getTopMenus().initializeButtonGroupWithAnimationDirection(TopMenus.BUTTON_FLASH, true);
    }

    @Override
    public void onFilterClicked() {
        super.onFilterClicked();
        /* MODIFIED-BEGIN by jianying.zhang, 2016-10-21,BUG-3178065*/
        Log.d(TAG, "FilterSelected mCameraState : " + mCameraState);
        if (mCameraState == AE_AF_LOCKED
                || mCameraState == SCANNING_FOR_AE_AF_LOCK
                || mCameraState == FOCUSING) {
            setCameraState(IDLE);
            if (mFocusManager != null) {
                mFocusManager.cancelAutoFocus();
            }
        }
        /* MODIFIED-END by jianying.zhang,BUG-3178065*/
        mUI.onFilterClicked();
    }

    @Override
    public void onAspectRatioClicked() {
    }

    /* MODIFIED-BEGIN by xuyang.liu, 2016-10-13,BUG-3110198*/
    protected void updateMetaDataCallback() {
        if (mCameraDevice == null) {
            return;
        }
        Log.i(TAG, "updateMetaDataCallback " + mCameraSettings.getCurrentSceneMode());
        if (mCameraSettings.getCurrentSceneMode() ==
                CameraCapabilities.SceneMode.HDR_AUTO) {
            mCameraDevice.setMetadataCb(mHandler, mMetaDataCallback);
        } else {
            mCameraDevice.setMetadataCb(null, null);
            mAutoHdrEnable = false;
        }
    }

    private boolean mAutoHdrEnable = false;

    private final class MetaDataCallback implements CameraAgent.CameraMetaDataCallback {
        @Override
        public void onCameraMetaData(boolean on) {
            if (mSceneMode == CameraCapabilities.SceneMode.HDR_AUTO) {
                mAutoHdrEnable = on;
            } else {
                mAutoHdrEnable = false;
            }
        }
    }
    private final MetaDataCallback mMetaDataCallback =
            new MetaDataCallback();
            /* MODIFIED-END by xuyang.liu,BUG-3110198*/



    protected void activeFilterButton() {
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-27,BUG-3212745*/
    @Override
    public void onZoomBarVisibilityChanged(boolean visible) {
        super.onZoomBarVisibilityChanged(visible);
        if (isImageCaptureIntent() || mUI.isCountingDown() || isInBurstshot()) {
            return;
        }
        mAppController.getCameraAppUI().setModeSwitchUIVisibility(!visible);

        if (!aspectRatioVisible()) {
            return;
        }
        mUI.setAspectRatioVisible(!visible);
    }
    /* MODIFIED-END by jianying.zhang,BUG-3212745*/

    /* MODIFIED-BEGIN by xuan.zhou, 2016-11-03,BUG-3311864*/
    @Override
    public boolean isFilterSelectorScreen() {
        return false;
    }
    /* MODIFIED-END by xuan.zhou,BUG-3311864*/
}
