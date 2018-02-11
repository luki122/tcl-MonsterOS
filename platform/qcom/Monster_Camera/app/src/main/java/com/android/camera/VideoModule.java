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
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.location.Location;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Toast;

import com.android.camera.app.AppController;
import com.android.camera.app.CameraAppUI;
import com.android.camera.app.LocationManager;
import com.android.camera.app.MediaSaver;
import com.android.camera.app.MemoryManager;
import com.android.camera.app.MemoryManager.MemoryListener;
import com.android.camera.debug.Log;
import com.android.camera.exif.ExifInterface;
import com.android.camera.hardware.HardwareSpec;
import com.android.camera.hardware.HardwareSpecImpl;
import com.android.camera.module.ModuleController;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.test.TestUtils; // MODIFIED by wenhua.tu, 2016-08-11,BUG-2710178
import com.android.camera.ui.BottomBar;
import com.android.camera.ui.TouchCoordinate;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.util.SnackbarToast; // MODIFIED by fei.hui, 2016-09-09,BUG-2868515
import com.android.camera.util.ToastUtil;
import com.android.camera.util.UsageStatistics;
import com.android.camera.widget.ButtonGroup;
import com.android.camera.widget.TopMenus;
import com.android.ex.camera2.portability.CameraAgent;
import com.android.ex.camera2.portability.CameraAgent.CameraPictureCallback;
import com.android.ex.camera2.portability.CameraAgent.CameraProxy;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.ex.camera2.portability.CameraDeviceInfo.Characteristics;
import com.android.ex.camera2.portability.CameraSettings;
import com.android.ex.camera2.portability.Size;
import com.android.external.plantform.ExtBuild; //MODIFIED by peixin, 2016-04-06,BUG-1913360
import com.google.common.logging.eventprotos;
import com.tct.camera.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class VideoModule extends CameraModule
    implements ModuleController,
    VideoController,
    MemoryListener,
    MediaRecorder.OnErrorListener,
    SettingsManager.OnSettingChangedListener,
    MediaRecorder.OnInfoListener, FocusOverlayManager.Listener {

    private static final String VIDEO_MODULE_STRING_ID = "VideoModule";

    protected static final String VIDEO_TEMP_SUFFIXES =".tmp";

    private static final Log.Tag TAG = new Log.Tag(VIDEO_MODULE_STRING_ID);

    // Messages defined for the UI thread handler.
    protected static final int MSG_CHECK_DISPLAY_ROTATION = 4;
    protected static final int MSG_UPDATE_RECORD_TIME = 5;
    protected static final int MSG_ENABLE_SHUTTER_BUTTON = 6;
    protected static final int MSG_SWITCH_CAMERA = 8;
    protected static final int MSG_SWITCH_CAMERA_START_ANIMATION = 9;
    protected static final int MSG_STOP_RECORDING=10;

    protected static final long SHUTTER_BUTTON_TIMEOUT = 500L; // 500ms
    protected static final long MIN_VIDEO_RECODER_DURATION = 1000L; // 1s

    /**
     * An unpublished intent flag requesting to start recording straight away
     * and return as soon as recording is stopped.
     * TODO: consider publishing by moving into MediaStore.
     */
    private static final String EXTRA_QUICK_CAPTURE =
            "android.intent.extra.quickCapture";

    // module fields
    protected CameraActivity mActivity;
    protected boolean mPaused;

    // if, during and intent capture, the activity is paused (e.g. when app switching or reviewing a
    // shot video), we don't want the bottom bar intent ui to reset to the capture button
    private boolean mDontResetIntentUiOnResume;

    private int mCameraId;
    protected CameraSettings mCameraSettings;
    protected CameraCapabilities mCameraCapabilities;

    private boolean mIsInReviewMode;
    private boolean mSnapshotInProgress = false;

    // Preference must be read before starting preview. We check this before starting
    // preview.
    private boolean mPreferenceRead;

    protected boolean mIsVideoCaptureIntent;
    private boolean mQuickCapture;

    protected MediaRecorder mMediaRecorder;

    protected boolean mSwitchingCamera;
    protected boolean mMediaRecorderRecording = false;
    protected long mRecordingStartTime;
    private boolean mRecordingTimeCountsDown = false;
    private long mOnResumeTime;
    // The video file that the hardware camera is about to record into
    // (or is recording into.
    protected String mVideoFilename;
    private ParcelFileDescriptor mVideoFileDescriptor;

    // The video file that has already been recorded, and that is being
    // examined by the user.
    protected String mCurrentVideoFilename;
    protected Uri mCurrentVideoUri;
    protected boolean mCurrentVideoUriFromMediaSaved;
    private ContentValues mCurrentVideoValues;

    protected CamcorderProfile mProfile;
    private long mBytePerMs;
    private long mTwoMinBytes;

    // The video duration limit. 0 means no limit.
    private int mMaxVideoDurationInMs;

//    boolean mPreviewing = false; // True if preview is started.
    // The display rotation in degrees. This is only valid when mPreviewing is
    // true.
    protected int mDisplayRotation;
    protected int mCameraDisplayOrientation;
    protected AppController mAppController;

    private int mDesiredPreviewWidth;
    private int mDesiredPreviewHeight;
    protected ContentResolver mContentResolver;

    private LocationManager mLocationManager;

    private int mPendingSwitchCameraId;
    protected final Handler mHandler = new MainHandler();
    private VideoUI mUI;
    protected CameraProxy mCameraDevice;

    // The degrees of the device rotated clockwise from its natural orientation.
    private int mOrientation = 0;

    protected Object mCameraDeviceLock =new Object();

    private float mZoomValue;  // The current zoom ratio.

    protected boolean mVideoBoomKeyFlags = false; // video Boom Key
    private final MediaSaver.OnMediaSavedListener mOnVideoSavedListener =
            new MediaSaver.OnMediaSavedListener() {
                @Override
                public void onMediaSaved(Uri uri) {
                    if (uri != null) {
                        mCurrentVideoUri = uri;
                        mCurrentVideoUriFromMediaSaved = true;
                        onVideoSaved();
                        mActivity.notifyNewMedia(uri);

                        if(mVideoBoomKeyFlags){
                            final SettingsManager settingsManager = mActivity.getSettingsManager();
                            boolean bTizrPrompt = !settingsManager.isSet(SettingsManager.SCOPE_GLOBAL,
                                    Keys.KEY_TIZR_PROMPT);
                            try{
                                mVideoBoomKeyFlags = false;
                                if(bTizrPrompt){
                                    // TizrShareVideoActivity only shows once
                                    Intent i = new Intent(mActivity, TizrShareVideoActivity.class);
                                    i.setData(mCurrentVideoUri);
                                    mActivity.startActivity(i);
                                    settingsManager.set(SettingsManager.SCOPE_GLOBAL, Keys.KEY_TIZR_PROMPT, true);
                                }else {
                                    //notify TiZR
                                    Log.e(TAG, "Tony startActivity to TiZR");
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(CameraUtil.TIZR_URI));
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    mActivity.startActivity(intent);
                                }
                            }catch (Exception e){
                                Log.e(TAG, "Tony VideoBoom exception");
                                e.printStackTrace();
                            }

                        }
                    }
                }
            };

    protected MediaSaver.OnMediaSavedListener getVideoSavedListener(){
        return mOnVideoSavedListener;
    }

    private final MediaSaver.OnMediaSavedListener mOnPhotoSavedListener =
            new MediaSaver.OnMediaSavedListener() {
                @Override
                public void onMediaSaved(Uri uri) {
                    if (uri != null) {
                        TestUtils.sendMessage(R.id.video_snap_button, TestUtils.MESSAGE.MEDIA_SAVED); // MODIFIED by wenhua.tu, 2016-08-11,BUG-2710178
                        mActivity.notifyNewMedia(uri,AppController.NOTIFY_NEW_MEDIA_ACTION_UPDATETHUMB);
                    }
                }
            };
    protected FocusOverlayManager mFocusManager;
    private boolean mMirror;
    private boolean mFocusAreaSupported;
    private boolean mMeteringAreaSupported;

    private final CameraAgent.CameraAFCallback mAutoFocusCallback =
            new CameraAgent.CameraAFCallback() {
        @Override
        public void onAutoFocus(boolean focused, CameraProxy camera) {
            if (mPaused) {
                return;
            }
            int action=FocusOverlayManager.ACTION_NONE;
            if(!isCameraStateRecording()){
                action|=FocusOverlayManager.ACTION_RESTORE_CAF_LATER;
            }
            Log.v(TAG,"on auto focus call back , focused: "+focused+" action:"+action);
            mFocusManager.onAutoFocus(focused, action);
        }
    };

    private final Object mAutoFocusMoveCallback =
            ApiHelper.HAS_AUTO_FOCUS_MOVE_CALLBACK
                    ? new CameraAgent.CameraAFMoveCallback() {
                @Override
                public void onAutoFocusMoving(boolean moving, CameraProxy camera) {
                    mFocusManager.onAutoFocusMoving(moving);
                }
            } : null;

    @Override
    public void onSettingChanged(SettingsManager settingsManager, String key) {
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-06-01,BUG-2231480*/
        if (mCameraDevice == null) {
            Log.e(TAG, "onSettingChanged but camera not opened,set param later");
            return;
        }
        /* MODIFIED-END by yuanxing.tan,BUG-2231480*/
        if (key.equals(Keys.KEY_VIDEOCAMERA_FLASH_MODE)) {
            String videoFlashMode = settingsManager.getString(mAppController.getCameraScope(),
                    Keys.KEY_VIDEOCAMERA_FLASH_MODE);
            CameraCapabilities.Stringifier stringifier = mCameraCapabilities.getStringifier();
            CameraCapabilities.FlashMode mode;
            if (stringifier.stringify(CameraCapabilities.FlashMode.OFF).equals(videoFlashMode)) {
                mode = CameraCapabilities.FlashMode.OFF;
                if (mCameraCapabilities.supports(mode)) {
                    mCameraSettings.setFlashMode(mode);
                    //this could happen during camera switching .
                    // Under this case , the setting change is supposed to be cached in memory(mCameraSettings) and would take effect on preview started
                    if(mCameraDevice!=null) {
                        mCameraDevice.applySettings(mCameraSettings);
                    }
                }
            }
            enableTorchMode(true);
        }

        if (key.equals(Keys.KEY_CAMERA_GRID_LINES)) {
            if (Keys.areGridLinesOn(mAppController.getSettingsManager()) &&
                    isGridLinesEnabled()) {
                mAppController.getCameraAppUI().showGridLines();
            } else {
                mAppController.getCameraAppUI().hideGridLines();
            }
        }

        if (key.equals(Keys.KEY_CAMERA_HDR_AUTO)) {
            Keys.setHdrState(settingsManager, mActivity,
                    settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL, key, false));
        }
    }

    protected boolean isGridLinesEnabled() {
        return true;
    }

    /**
     * This Handler is used to post message back onto the main thread of the
     * application.
     */
    private class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case MSG_ENABLE_SHUTTER_BUTTON:
                    mAppController.setShutterEnabled(true);
                    break;

                case MSG_UPDATE_RECORD_TIME: {
                    updateRecordingTime();
                    break;
                }

                case MSG_CHECK_DISPLAY_ROTATION: {
                    // Restart the preview if display rotation has changed.
                    // Sometimes this happens when the device is held upside
                    // down and camera app is opened. Rotation animation will
                    // take some time and the rotation value we have got may be
                    // wrong. Framework does not have a callback for this now.
                    if ((CameraUtil.getDisplayRotation(mActivity) != mDisplayRotation)
                            && !mMediaRecorderRecording && !mSwitchingCamera) {
                        startPreview();
                    }
                    if (SystemClock.uptimeMillis() - mOnResumeTime < 5000) {
                        mHandler.sendEmptyMessageDelayed(MSG_CHECK_DISPLAY_ROTATION, 100);
                    }
                    break;
                }

                case MSG_SWITCH_CAMERA: {
                    switchCamera();
                    break;
                }

                case MSG_SWITCH_CAMERA_START_ANIMATION: {
                    //TODO:
                    //((CameraScreenNail) mActivity.mCameraScreenNail).animateSwitchCamera();

                    // Enable all camera controls.
                    mSwitchingCamera = false;
                    break;
                }

                case MSG_STOP_RECORDING:{
                    onStopVideoRecording();
                    break;
                }
                default:
                    Log.v(TAG, "Unhandled message: " + msg.what);
                    break;
            }
        }
    }

    private BroadcastReceiver mReceiver = null;

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                String currentPath = Storage.getSavePath();
                if (currentPath != null && currentPath.equals(Storage.SDCARD_STORAGE)) {
                    stopVideoRecording();
                }
            } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
                /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
                SnackbarToast.getSnackbarToast().showToast(mActivity,
                        mActivity.getResources().getString(R.string.wait)
                        ,SnackbarToast.LENGTH_LONG,SnackbarToast.DEFAULT_Y_OFFSET);
                        /* MODIFIED-END by bin-liu3,BUG-3253898*/
            }
        }
    }

    private boolean mRecordingInterrupted = false;

    @Override
    protected void onMediaAction(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
            String currentPath = Storage.getSavePath();
            if (currentPath != null && currentPath.equals(Storage.SDCARD_STORAGE) &&
                    mMediaRecorderRecording) {
                mRecordingInterrupted = true;
                onStopVideoRecording();
            }
        } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
            /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
            SnackbarToast.getSnackbarToast().showToast(mActivity,
                    mActivity.getResources().getString(R.string.wait)
                    ,SnackbarToast.LENGTH_LONG,SnackbarToast.DEFAULT_Y_OFFSET);
                    /* MODIFIED-END by bin-liu3,BUG-3253898*/
        }
    }

    private int mShutterIconId;


    /**
     * Construct a new video module.
     */
    public VideoModule(AppController app) {
        super(app);
    }

    @Override
    public String getPeekAccessibilityString() {
        return mAppController.getAndroidContext()
            .getResources().getString(R.string.video_accessibility_peek);
    }

    private String createName(long dateTaken) {
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                mActivity.getString(R.string.video_file_name_format));

        return dateFormat.format(date);
    }

    @Override
    public String getModuleStringIdentifier() {
        return VIDEO_MODULE_STRING_ID;
    }

    protected VideoUI getVideoUI(){
        return new VideoUI(mActivity, this,  mActivity.getModuleLayoutRoot());
    }

    @Override
    public void init(CameraActivity activity, boolean isSecureCamera, boolean isCaptureIntent) {
        mActivity = activity;
        mAppController = mActivity;
        setCameraState(PREVIEW_STOPPED);
        // TODO: Need to look at the controller interface to see if we can get
        // rid of passing in the activity directly.

        mActivity.updateStorageSpaceAndHint(null);

        mUI = getVideoUI();
        mActivity.setPreviewStatusListener(mUI);

        SettingsManager settingsManager = mActivity.getSettingsManager();
        mCameraId = settingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,
                                               Keys.KEY_CAMERA_ID);

        /*
         * To reduce startup time, we start the preview in another thread.
         * We make sure the preview is started at the end of onCreate.
         */
        requestCamera(mCameraId);

        mContentResolver = mActivity.getContentResolver();

        // Surface texture is from camera screen nail and startPreview needs it.
        // This must be done before startPreview.
        mIsVideoCaptureIntent = isVideoCaptureIntent();

        mQuickCapture = mActivity.getIntent().getBooleanExtra(EXTRA_QUICK_CAPTURE, false);
        mLocationManager = mActivity.getLocationManager();

        mUI.setOrientationIndicator(0, false);
        setDisplayOrientation();

        mPendingSwitchCameraId = -1;

        mShutterIconId = CameraUtil.getCameraShutterIconId(
                mAppController.getCurrentModuleIndex(), mAppController.getAndroidContext());
    }

    @Override
    public boolean isUsingBottomBar() {
        return true;
    }

    private void initializeControlByIntent() {
        if (isVideoCaptureIntent()) {
            if (!mDontResetIntentUiOnResume) {
                mActivity.getCameraAppUI().transitionToIntentCaptureLayout();
            }
            // reset the flag
            mDontResetIntentUiOnResume = false;
        }
    }

    private void setTopModeOptionVisibility(boolean videoStop) {
        if (videoStop) {
            mAppController.getCameraAppUI().getTopMenus().setTopModeOptionVisibility(true);
        } else {
            mAppController.getCameraAppUI().getTopMenus().setTopModeOptionVisibility(false);
        }
    }

    protected boolean dismissButtonGroupBar(boolean needAnimation) {
        if (mAppController !=null
                && mAppController.getCameraAppUI()!= null
                && mAppController.getCameraAppUI().getTopMenus().buttonGroupBarVisible()) {
            mAppController.getCameraAppUI().dismissButtonGroupBar(needAnimation, ButtonGroup.OUT_LEFT);
            return true;
        }
        return false;
    }

    @Override
    public void onSingleTapUp(View view, int x, int y) {
        if (mPaused || mCameraDevice == null) {
            return;
        }
        if (dismissButtonGroupBar(true)) {
            return;
        }
//        if (mMediaRecorderRecording) {
//            if (!mSnapshotInProgress) {
//                takeASnapshot();
//            }
//            return;
//        }
        // Check if metering area or focus area is supported.
        if (!mFocusAreaSupported && !mMeteringAreaSupported) {
            return;
        }
        // Tap to focus.
        if (mFocusManager != null) {
            // If the focus mode is continuous autofocus, call cancelAutoFocus
            // to clear any pending focus and make sure the focus command is set to camera immediately.
            CameraCapabilities.FocusMode focusMode =
                    mFocusManager.getFocusMode(mCameraSettings.getCurrentFocusMode());
            /* MODIFIED-BEGIN by jianying.zhang, 2016-06-03,BUG-2244998*/
            if (focusMode == CameraCapabilities.FocusMode.CONTINUOUS_PICTURE
                    || focusMode == CameraCapabilities.FocusMode.AUTO) {
                    /* MODIFIED-END by jianying.zhang,BUG-2244998*/
                mCameraDevice.cancelAutoFocus();
            }
        }
        mFocusManager.onSingleTapUp(x, y);

    }

    private boolean isCameraStateRecording(){
        return mCameraState==RECORDING_PENDING_START||mCameraState==RECORDING||mCameraState==RECORDING_PENDING_STOP;
    }

    private void takeASnapshot() {
        // Only take snapshots if video snapshot is supported by device
        if(!mCameraCapabilities.supports(CameraCapabilities.Feature.VIDEO_SNAPSHOT)) {
            Log.w(TAG, "Cannot take a video snapshot - not supported by hardware");
            return;
        }
        if (!mIsVideoCaptureIntent) {
            if (!mMediaRecorderRecording || mPaused || mSnapshotInProgress
                    || !mAppController.isShutterEnabled() || mCameraDevice == null) {
                return;
            }

            Location loc = mLocationManager.getCurrentLocation();
            CameraUtil.setGpsParameters(mCameraSettings, loc);
            mCameraDevice.applySettings(mCameraSettings);

            int orientation = getJpegRotation(mOrientation);
            Log.d(TAG, "Video snapshot orientation is " + orientation);
            mCameraDevice.setJpegOrientation(orientation);

            Log.i(TAG, "Video snapshot start");
            mCameraDevice.takePicture(mHandler,
                    null, null, null, new JpegPictureCallback(loc));
            showVideoSnapshotUI(true);
            mSnapshotInProgress = true;
        }
    }

    private int getJpegRotation(int orientation) {
        int mJpegRotation = 0;

        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return mJpegRotation;
        }

        try {
            orientation = (360 - orientation) % 360;
            Characteristics info = mActivity.getCameraProvider().getCharacteristics(mCameraId);
            mJpegRotation = info.getJpegOrientation(orientation);
        } catch (Exception e) {
            Log.e(TAG, "Error when getJpegOrientation");
        }

        return mJpegRotation;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
     private void updateAutoFocusMoveCallback() {
        if (mPaused || mCameraDevice == null) {
            return;
        }

        if (mCameraSettings.getCurrentFocusMode() == CameraCapabilities.FocusMode.CONTINUOUS_PICTURE) {
            mCameraDevice.setAutoFocusMoveCallback(mHandler,
                    (CameraAgent.CameraAFMoveCallback) mAutoFocusMoveCallback);
        } else {
            mCameraDevice.setAutoFocusMoveCallback(null, null);
        }
    }

    /**
     * @return Whether the currently active camera is front-facing.
     */
    protected boolean isCameraFrontFacing() {
        return mAppController.getCameraProvider().getCharacteristics(mCameraId)
                .isFacingFront();
    }

    /**
     * @return Whether the currently active camera is back-facing.
     */
    private boolean isCameraBackFacing() {
        return mAppController.getCameraProvider().getCharacteristics(mCameraId)
                .isFacingBack();
    }

    /**
     * The focus manager gets initialized after camera is available.
     */
    private void initializeFocusManager() {
        // Create FocusManager object. startPreview needs it.
        // if mFocusManager not null, reuse it
        // otherwise create a new instance
        if (mFocusManager != null) {
            mFocusManager.removeMessages();
        } else {
            mMirror = isCameraFrontFacing();
            String[] defaultFocusModesStrings = mActivity.getResources().getStringArray(
                    R.array.pref_camera_focusmode_default_array);
            CameraCapabilities.Stringifier stringifier = mCameraCapabilities.getStringifier();
            ArrayList<CameraCapabilities.FocusMode> defaultFocusModes =
                    new ArrayList<CameraCapabilities.FocusMode>();
            for (String modeString : defaultFocusModesStrings) {
                CameraCapabilities.FocusMode mode = stringifier.focusModeFromString(modeString);
                if (mode != null) {
                    defaultFocusModes.add(mode);
                }
            }
            mFocusManager = new FocusOverlayManager(mAppController,
                    defaultFocusModes, mCameraCapabilities, this, mMirror,
                    mActivity.getMainLooper(), mUI.getFocusUI());
        }
        mAppController.addPreviewAreaSizeChangedListener(mFocusManager);
    }

    @Override
    public void onOrientationChanged(int orientation) {
        // We keep the last known orientation. So if the user first orient
        // the camera then point the camera to floor or sky, we still have
        // the correct orientation.
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return;
        }
        int newOrientation = CameraUtil.roundOrientation(orientation, mOrientation);

        if (mOrientation != newOrientation) {
            mOrientation = newOrientation;
        }
        mUI.onOrientationChanged(orientation);

    }

    private final ButtonManager.ButtonCallback mFlashCallback =
        new ButtonManager.ButtonCallback() {
            @Override
            public void onStateChanged(int state) {
                // Update flash parameters.
                enableTorchMode(true);
            }

        };

    private final BottomBar.SwitchButtonCallback mToggleCallback =
            new BottomBar.SwitchButtonCallback() {
                @Override
                public void onToggleStateChanged(int state) {
                    if (mPaused || mAppController.getCameraProvider().waitingForCamera()) {
                        return;
                    }
                    mPendingSwitchCameraId = state;
                    Log.d(TAG, "Start to copy texture.");

                    // Disable all camera controls.
                    mSwitchingCamera = true;
                    switchCamera();
                }

            };

    private final View.OnClickListener mCancelCallback = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onReviewCancelClicked(v);
        }
    };

    private final View.OnClickListener mDoneCallback = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onReviewDoneClicked(v);
        }
    };
    private final View.OnClickListener mReviewCallback = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onReviewPlayClicked(v);
        }
    };

    public void intentReviewCancel() {
        onReviewCancelClicked(null);
    }

    public void intentReviewDone() {
        onReviewDoneClicked(null);
    }

    public void intentReviewPlay() {
        onReviewPlayClicked(null);
    }

    @Override
    public void hardResetSettings(SettingsManager settingsManager) {
        // VideoModule does not need to hard reset any settings.
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
        bottomBarSpec.hideCameraForced = false;
        bottomBarSpec.hideSetting = isVideoCaptureIntent();
        bottomBarSpec.showPose = false;
        bottomBarSpec.showFilter = showFilter();
//        bottomBarSpec.cameraCallback = mCameraCallback;
        bottomBarSpec.switchButtonCallback = mToggleCallback;
        bottomBarSpec.enableTorchFlash = mActivity.currentBatteryStatusOK();// MODIFIED by nie.lei, 2016-03-21, BUG-1845068
        bottomBarSpec.flashCallback = mFlashCallback;
        bottomBarSpec.hideHdr = true;
//        bottomBarSpec.enableGridLines = true;
        bottomBarSpec.hideGridLines=true;
        bottomBarSpec.hideLowlight = true;
        if(isCameraFrontFacing()){
            ModuleController controller=mAppController.getCurrentModuleController();
            if(controller.getHardwareSpec()!=null/*Could happened in case of pause*/
                    &&!controller.getHardwareSpec().isFlashSupported()){
                bottomBarSpec.hideFlash=true;
            }
        }

        if (isVideoCaptureIntent()) {
            bottomBarSpec.showCancel = true;
            bottomBarSpec.cancelCallback = mCancelCallback;
            bottomBarSpec.showDone = true;
            bottomBarSpec.doneCallback = mDoneCallback;
            bottomBarSpec.showReview = true;
            bottomBarSpec.reviewCallback = mReviewCallback;
        }

        return bottomBarSpec;
    }

    @Override
    public void onCameraAvailable(CameraProxy cameraProxy) {
        Log.w(TAG,"On CameraAvailable");
        if (cameraProxy == null) {
            Log.w(TAG, "onCameraAvailable returns a null CameraProxy object");
            return;
        }
        mCameraDevice = cameraProxy;
        mCameraCapabilities = mCameraDevice.getCapabilities();
        mCameraSettings = mCameraDevice.getSettings();
        mFocusAreaSupported = mCameraCapabilities.supports(CameraCapabilities.Feature.FOCUS_AREA);
        mMeteringAreaSupported =
                mCameraCapabilities.supports(CameraCapabilities.Feature.METERING_AREA);
        readVideoPreferences();
        updateDesiredPreviewSize();
        resizeForPreviewAspectRatio();
        initializeFocusManager();
        // TODO: Having focus overlay manager caching the parameters is prone to error,
        // we should consider passing the parameters to focus overlay to ensure the
        // parameters are up to date.
        mFocusManager.updateCapabilities(mCameraCapabilities);

        startPreview();
        initializeVideoSnapshot();
        mUI.initializeZoom(mCameraSettings, mCameraCapabilities);
        initializeControlByIntent();
    }

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

        // Almost the same with the method in PhotoModule except the id ButtonManager.BUTTON_TORCH,
        // actually the button got through both ids is flash_toggle_button.
        mAppController.getCameraAppUI().getTopMenus().disableButtonClick(
                TopMenus.BUTTON_TORCH);
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
                TopMenus.BUTTON_TORCH);
    }
    /* MODIFIED-END by xuan.zhou,BUG-2251935*/

    private void startPlayVideoActivity() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(mCurrentVideoUri, convertOutputFormatToMimeType(mProfile.fileFormat));
        try {
            mActivity.launchActivityByIntent(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e(TAG, "Couldn't view video " + mCurrentVideoUri, ex);
        }
    }

    @Override
    @OnClickAttr
    public void onReviewPlayClicked(View v) {
        startPlayVideoActivity();
    }

    @Override
    @OnClickAttr
    public void onReviewDoneClicked(View v) {
        mIsInReviewMode = false;
        doReturnToCaller(true);
    }

    @Override
    @OnClickAttr
    public void onReviewCancelClicked(View v) {
        // TODO: It should be better to not even insert the URI at all before we
        // confirm done in review, which means we need to handle temporary video
        // files in a quite different way than we currently had.
        // Make sure we don't delete the Uri sent from the video capture intent.
        if (mCurrentVideoUriFromMediaSaved) {
            mContentResolver.delete(mCurrentVideoUri, null, null);
        }
        mIsInReviewMode = false;
        doReturnToCaller(false);
    }

    @Override
    public boolean isInReviewMode() {
        return mIsInReviewMode;
    }

    protected boolean onStopVideoRecording() {
        mAppController.getCameraAppUI().setSwipeEnabled(true);
        mActivity.stopBatteryInfoChecking();
        mActivity.stopInnerStorageChecking();
        boolean recordFail = stopVideoRecording();
        releaseAudioFocus();
        if (mIsVideoCaptureIntent) {
            if (mQuickCapture) {
                doReturnToCaller(!recordFail);
            } else if (!recordFail) {
                /* MODIFIED-BEGIN by xuan.zhou, 2016-05-27,BUG-1840762*/
                // The result may be shown already in stopVideoRecording.
                if (!mIsInReviewMode) {
                    showCaptureResult();
                }
                mAppController.getCameraAppUI().getTopMenus().setTopModeOptionVisibility(false);
                /* MODIFIED-END by xuan.zhou,BUG-1840762*/
            } else {
                // If stop fail in capture intent, show mode options again.
                mAppController.getCameraAppUI().getTopMenus().setTopModeOptionVisibility(true);
                mAppController.getCameraAppUI().showModeOptions();
                mHandler.sendEmptyMessageDelayed(MSG_ENABLE_SHUTTER_BUTTON, SHUTTER_BUTTON_TIMEOUT);
            }
        } else if (!recordFail){
            // Start capture animation.
            if (!mPaused && ApiHelper.HAS_SURFACE_TEXTURE_RECORDING) {
                // The capture animation is disabled on ICS because we use SurfaceView
                // for preview during recording. When the recording is done, we switch
                // back to use SurfaceTexture for preview and we need to stop then start
                // the preview. This will cause the preview flicker since the preview
                // will not be continuous for a short period of time.

//                mUI.animateFlash();
            }
        }
        return recordFail;
    }

    public void onVideoSaved() {
        if (mIsVideoCaptureIntent) {
            showCaptureResult();
        }
    }

    public void onProtectiveCurtainClick(View v) {
        // Consume clicks
    }

    protected void startVideoNotityHelpTip(){
    }

    @Override
    public void onShutterButtonClick() {
        if (mSwitchingCamera || mCameraState == PREVIEW_STOPPED
                             || mCameraState == RECORDING_PENDING_START
                             /* MODIFIED-BEGIN by guodong.zhang, 2016-11-14,BUG-3330877*/
                             || mCameraState == RECORDING_PENDING_STOP
                             || mAppController.getCameraAppUI().isShutterLocked()) {
                             /* MODIFIED-END by guodong.zhang,BUG-3330877*/
            return;
        }

        dismissButtonGroupBar(false);

//        boolean needStop = mMediaRecorderRecording;
        boolean needStop = (mCameraState == RECORDING);

        if (needStop) {
            // CameraAppUI mishandles mode option enable/disable
            // for video, override that
            mAppController.getCameraAppUI().enableModeOptions();
            onStopVideoRecording();
        } else {
            //notify helptip
            startVideoNotityHelpTip();

            // CameraAppUI mishandles mode option enable/disable
            // for video, override that
            mAppController.getCameraAppUI().disableModeOptions();
            startVideoRecording();
        }
        mAppController.setShutterEnabled(false);
        if (mCameraSettings != null) {
            mFocusManager.onShutterUp(mCameraSettings.getCurrentFocusMode());
        }

        // Keep the shutter button disabled when in video capture intent
        // mode and recording is stopped. It'll be re-enabled when
        // re-take button is clicked.
        if ((needStop && !mIsVideoCaptureIntent) ||
                (!needStop && !shouldHoldRecorderForSecond())) {
            mHandler.sendEmptyMessageDelayed(MSG_ENABLE_SHUTTER_BUTTON, SHUTTER_BUTTON_TIMEOUT);
        }
    }


    @Override
    public void onShutterButtonLongClick() {

    }

    @Override
    public void onShutterCoordinate(TouchCoordinate coord) {
        // Do nothing.
    }

    @Override
    public void onShutterButtonFocus(boolean pressed) {
        // TODO: Remove this when old camera controls are removed from the UI.
    }

    protected int getOverrodeVideoDuration(){
        return mMaxVideoDurationInMs;
    }

    private void readVideoPreferences() {
        // The preference stores values from ListPreference and is thus string type for all values.
        // We need to convert it to int manually.
        int quality = getProfileQuality();

        // Set video quality.
        Intent intent = mActivity.getIntent();
        if (intent.hasExtra(MediaStore.EXTRA_VIDEO_QUALITY)) {
            int extraVideoQuality =
                    intent.getIntExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
            if (extraVideoQuality > 0) {
                quality = CamcorderProfile.QUALITY_HIGH;
            } else {  // 0 is mms.
                quality = CamcorderProfile.QUALITY_LOW;
            }
        }

        // Set video duration limit. The limit is read from the preference,
        // unless it is specified in the intent.
        if (intent.hasExtra(MediaStore.EXTRA_DURATION_LIMIT)) {
            int seconds =
                    intent.getIntExtra(MediaStore.EXTRA_DURATION_LIMIT, 0);
            mMaxVideoDurationInMs = 1000 * seconds;
        } else {
            mMaxVideoDurationInMs = SettingsUtil.getMaxVideoDuration(mActivity
                    .getAndroidContext());
        }

        // If quality is not supported, request QUALITY_HIGH which is always supported.
        if (CamcorderProfile.hasProfile(mCameraId, quality) == false) {
            quality = CamcorderProfile.QUALITY_HIGH;
        }
        mProfile = CamcorderProfile.get(mCameraId, quality);
        if (mProfile != null) {
            mBytePerMs = ((mProfile.videoBitRate + mProfile.videoFrameRate
                    + mProfile.audioBitRate) >> LEFT_SHIFT_NUMBER) /
                    MINUTE_TO_MINI_SECONEDS;
            mTwoMinBytes = mBytePerMs * 60 * 2 * MINUTE_TO_MINI_SECONEDS;
        }
        overrideProfileSize();
        mPreferenceRead = true;
    }

    protected void overrideProfileSize() {}

    /**
     * Calculates and sets local class variables for Desired Preview sizes.
     * This function should be called after every change in preview camera
     * resolution and/or before the preview starts. Note that these values still
     * need to be pushed to the CameraSettings to actually change the preview
     * resolution.  Does nothing when camera pointer is null.
     */
    private void updateDesiredPreviewSize() {
        if (mCameraDevice == null) {
            return;
        }

        mCameraSettings = mCameraDevice.getSettings();
        Point desiredPreviewSize = getDesiredPreviewSize(mAppController.getAndroidContext(),
                mCameraSettings, mCameraCapabilities, mProfile, mUI.getPreviewScreenSize());
        mDesiredPreviewWidth = desiredPreviewSize.x;
        mDesiredPreviewHeight = desiredPreviewSize.y;
        mUI.setPreviewSize(mDesiredPreviewWidth, mDesiredPreviewHeight);
        Log.v(TAG, "Updated DesiredPreview=" + mDesiredPreviewWidth + "x"
                + mDesiredPreviewHeight);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    /**
     * Calculates the preview size and stores it in mDesiredPreviewWidth and
     * mDesiredPreviewHeight.
     *
     * <p>This function checks {@link
     * com.android.camera.cameradevice.CameraCapabilities#getPreferredPreviewSizeForVideo()}
     * but also considers the current preview area size on screen and make sure
     * the final preview size will not be smaller than 1/2 of the current
     * on screen preview area in terms of their short sides.  This function has
     * highest priority of WYSIWYG, 1:1 matching as its best match, even if
     * there's a larger preview that meets the condition above. </p>
     *
     * @return The preferred preview size or {@code null} if the camera is not
     *         opened yet.
     */
    private static Point getDesiredPreviewSize(Context context, CameraSettings settings,
            CameraCapabilities capabilities, CamcorderProfile profile, Point previewScreenSize) {
        if (capabilities.getSupportedVideoSizes() == null) {
            // Driver doesn't support separate outputs for preview and video.
            return new Point(profile.videoFrameWidth, profile.videoFrameHeight);
        }

        final int previewScreenShortSide = (previewScreenSize.x < previewScreenSize.y ?
                previewScreenSize.x : previewScreenSize.y);
        List<Size> sizes = capabilities.getSupportedPreviewSizes();
        Size preferred = capabilities.getPreferredPreviewSizeForVideo();
        final int preferredPreviewSizeShortSide = (preferred.width() < preferred.height() ?
                preferred.width() : preferred.height());
        if (preferredPreviewSizeShortSide * 2 < previewScreenShortSide) {
            preferred = new Size(profile.videoFrameWidth, profile.videoFrameHeight);
        }
        int product = preferred.width() * preferred.height();
        Iterator<Size> it = sizes.iterator();
        // Remove the preview sizes that are not preferred.
        while (it.hasNext()) {
            Size size = it.next();
            if (size.width() * size.height() > product) {
                it.remove();
            }
        }

        // Take highest priority for WYSIWYG when the preview exactly matches
        // video frame size.  The variable sizes is assumed to be filtered
        // for sizes beyond the UI size.
        for (Size size : sizes) {
            if (size.width() == profile.videoFrameWidth
                    && size.height() == profile.videoFrameHeight) {
                Log.v(TAG, "Selected =" + size.width() + "x" + size.height()
                           + " on WYSIWYG Priority");
                return new Point(profile.videoFrameWidth, profile.videoFrameHeight);
            }
        }

        Size optimalSize = CameraUtil.getOptimalPreviewSize(context, sizes,
                (double) profile.videoFrameWidth / profile.videoFrameHeight);
        return new Point(optimalSize.width(), optimalSize.height());
    }

    private void resizeForPreviewAspectRatio() {
        mUI.setAspectRatio((float) mProfile.videoFrameWidth / mProfile.videoFrameHeight);
    }

    private void installIntentFilter() {
        // install an intent filter to receive SD card related events.
        IntentFilter intentFilter =
                new IntentFilter(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addDataScheme("file");
        mReceiver = new MyBroadcastReceiver();
        mActivity.registerReceiver(mReceiver, intentFilter);
    }

    private void setDisplayOrientation() {
        mDisplayRotation = CameraUtil.getDisplayRotation(mActivity);
        Characteristics info =
                mActivity.getCameraProvider().getCharacteristics(mCameraId);
        mCameraDisplayOrientation = info.getPreviewOrientation(mDisplayRotation);
        // Change the camera display orientation
        if (mCameraDevice != null) {
            mCameraDevice.setDisplayOrientation(mDisplayRotation);
        }
        if (mFocusManager != null) {
            mFocusManager.setDisplayOrientation(mCameraDisplayOrientation);
        }
    }

    @Override
    public void updateCameraOrientation() {
        if (mMediaRecorderRecording) {
            return;
        }
        if (mDisplayRotation != CameraUtil.getDisplayRotation(mActivity)) {
            setDisplayOrientation();
        }
    }

    @Override
    public void updatePreviewAspectRatio(float aspectRatio) {
        mAppController.updatePreviewAspectRatio(aspectRatio);
    }

    /**
     * Returns current Zoom value, with 1.0 as the value for no zoom.
     */
    private float currentZoomValue() {
        return mCameraSettings.getCurrentZoomRatio();
    }

    @Override
    public void onZoomChanged(float ratio) {
        // Not useful to change zoom value when the activity is paused.
        if (mPaused) {
            return;
        }
        mZoomValue = ratio;
        if (mCameraSettings == null || mCameraDevice == null) {
            return;
        }
        // Set zoom parameters asynchronously
        mCameraSettings.setZoomRatio(mZoomValue);
        mCameraDevice.applySettings(mCameraSettings);
    }

    private void startPreview() {
        Log.i(TAG, "startPreview");

        SurfaceTexture surfaceTexture = mActivity.getCameraAppUI().getSurfaceTexture();
        if (!mPreferenceRead || surfaceTexture == null || mPaused == true ||
                mCameraDevice == null) {
            Log.w(TAG,"mPreferenceRead = "+mPreferenceRead+" surfaceTexture is null ?"+(surfaceTexture==null)+" mCameraDevice==null?"+(mCameraDevice==null));
            return;
        }

//        if (mPreviewing == true) {
        if(mCameraState!=PREVIEW_STOPPED){
            stopPreview();
        }

        setDisplayOrientation();
        mCameraDevice.setDisplayOrientation(mDisplayRotation);
        setCameraParameters();

        if (mFocusManager != null) {
            // If the focus mode is continuous autofocus, call cancelAutoFocus
            // to resume it because it may have been paused by autoFocus call.
            CameraCapabilities.FocusMode focusMode =
                    mFocusManager.getFocusMode(mCameraSettings.getCurrentFocusMode());
            if (focusMode == CameraCapabilities.FocusMode.CONTINUOUS_PICTURE) {
                mCameraDevice.cancelAutoFocus();
            }
        }

        // This is to notify app controller that preview will start next, so app
        // controller can set preview callbacks if needed. This has to happen before
        // preview is started as a workaround of the framework issue related to preview
        // callbacks that causes preview stretch and crash. (More details see b/12210027
        // and b/12591410. Don't apply this to L, see b/16649297.
        if (!ApiHelper.isLOrHigher()) {
            Log.v(TAG, "calling onPreviewReadyToStart to set one shot callback");
            mAppController.onPreviewReadyToStart();
        } else {
            Log.v(TAG, "on L, no one shot callback necessary");
        }
        try {
            CameraAgent.CameraStartPreviewCallback cameraStartPreviewCallback=new CameraAgent.CameraStartPreviewCallback() {
                @Override
                public void onPreviewStarted() {
                    VideoModule.this.onPreviewStarted();
                }
            };
            if(mActivity.getCameraProvider().isBoostPreview()){
                mCameraDevice.waitPreviewWithCallback(new Handler(Looper.getMainLooper()),cameraStartPreviewCallback);
            }else {
                mCameraDevice.setPreviewTexture(surfaceTexture);
                mCameraDevice.startPreviewWithCallback(new Handler(Looper.getMainLooper()),cameraStartPreviewCallback);

            }
//            mPreviewing = true;
            setCameraState(IDLE);
        } catch (Throwable ex) {
            closeCamera();
            throw new RuntimeException("startPreview failed", ex);
        }
    }

    protected void onPreviewStarted() {
        Log.w(TAG,"KPI video preview started");
        mAppController.setShutterEnabled(true);
        mAppController.onPreviewStarted();
        if (mFocusManager != null) {
            mFocusManager.onPreviewStarted();
        }

        if(isNeedStartRecordingOnSwitching()) {
            // startVideoRecording();
            onShutterButtonClick();
        }
    }

    @Override
    public int getModuleId() {
        return mAppController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_video);
    }

    protected boolean isNeedStartRecordingOnSwitching(){
        return false;
    }

    @Override
    public void onPreviewInitialDataReceived() {
    }

    @Override
    public void stopPreview() {
        if (/*!mPreviewing*/
                mCameraState==PREVIEW_STOPPED) {
            Log.v(TAG, "Skip stopPreview since it's not mPreviewing");
            return;
        }
        if (mCameraDevice == null) {
            Log.v(TAG, "Skip stopPreview since mCameraDevice is null");
            return;
        }

        mCameraDevice.cancelAutoFocus();//always need to cancel any focus on flight before stop preview or release camera
        Log.v(TAG, "stopPreview");
        mCameraDevice.stopPreview();
        mActivity.clearBoost();
        if (mFocusManager != null) {
            mFocusManager.onPreviewStopped();
        }
//        mPreviewing = false;
        setCameraState(PREVIEW_STOPPED);
    }

    private void closeCamera() {
        Log.i(TAG, "closeCamera");

        if (mCameraDevice == null) {
            Log.d(TAG, "already stopped.");
            return;
        }
        mCameraDevice.setZoomChangeListener(null);
        mActivity.getCameraProvider().releaseCamera(mCameraDevice.getCameraId());
        mCameraDevice = null;
//        mPreviewing = false;
        setCameraState(PREVIEW_STOPPED);
        mSnapshotInProgress = false;
        if (mFocusManager != null) {
            mFocusManager.onCameraReleased();
        }

        /* MODIFIED-BEGIN by xuan.zhou, 2016-06-06,BUG-2251935*/
        mSpecificUIReady = false;
        disableFlashButtonClick();
        /* MODIFIED-END by xuan.zhou,BUG-2251935*/
    }

    @Override
    public boolean onBackPressed() {
        if (mPaused) {
            return true;
        }

        if (dismissButtonGroupBar(true)) {
            return true;
        }

        if (mMediaRecorderRecording) {
            onStopVideoRecording();
            //mActivity.finish();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Do not handle any key if the activity is paused.
        if (mPaused) {
            return true;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (event.getRepeatCount() == 0 &&
                        !mActivity.getCameraAppUI().isInIntentReview()
                            && mAppController.isShutterEnabled()) {
                    onShutterButtonClick();
                }
                return true;
            case KeyEvent.KEYCODE_CAMERA:
                if (event.getRepeatCount() == 0) {
                    onShutterButtonClick();
                    return true;
                }
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (event.getRepeatCount() == 0) {
                    onShutterButtonClick();
                    return true;
                }
            case KeyEvent.KEYCODE_MENU:
                // Consume menu button presses during capture.
                return mMediaRecorderRecording;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                return true;
            case KeyEvent.KEYCODE_CAMERA:
                onShutterButtonClick();
                return true;
            case KeyEvent.KEYCODE_MENU:
                // Consume menu button presses during capture.
                return mMediaRecorderRecording;
        }
        return false;
    }

    @Override
    public boolean isVideoCaptureIntent() {
        String action = mActivity.getIntent().getAction();
        return (MediaStore.ACTION_VIDEO_CAPTURE.equals(action));
    }

    protected boolean showFilter() {
        return !isVideoCaptureIntent() && CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_SUPPORT_VIDEO_FILTER_MODULE, false);
    }

    private void doReturnToCaller(boolean valid) {
        Intent resultIntent = new Intent();
        int resultCode;
        if (valid) {
            resultCode = Activity.RESULT_OK;
            resultIntent.setData(mCurrentVideoUri);
            resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            resultCode = Activity.RESULT_CANCELED;
        }
        mActivity.setResultEx(resultCode, resultIntent);
        mActivity.finish();
    }

    private void cleanupEmptyFile() {
        if (mVideoFilename != null) {
            File f = new File(mVideoFilename);
            if (f.length() == 0 && f.delete()) {
                Log.v(TAG, "Empty video file deleted: " + mVideoFilename);
                mVideoFilename = null;
            }
        }
    }

    private int mMediaRemcoderRotation;
    protected int getMediaRecorderRotation(){
        return mMediaRemcoderRotation;
    }

    // Prepares media recorder.
    private void initializeRecorder() {
        Log.i(TAG, "initializeRecorder: " + Thread.currentThread());
        // If the mCameraDevice is null, then this activity is going to finish
        if (mCameraDevice == null) {
            return;
        }

        mRecordingInterrupted = false;

        Intent intent = mActivity.getIntent();
        Bundle myExtras = intent.getExtras();

        long requestedSizeLimit = 0;
        closeVideoFileDescriptor();
        mCurrentVideoUriFromMediaSaved = false;
        if (mIsVideoCaptureIntent && myExtras != null) {
            Uri saveUri = (Uri) myExtras.getParcelable(MediaStore.EXTRA_OUTPUT);
            if (saveUri != null) {
                try {
                    mVideoFileDescriptor =
                            mContentResolver.openFileDescriptor(saveUri, "rw");
                    mCurrentVideoUri = saveUri;
                } catch (java.io.FileNotFoundException ex) {
                    // invalid uri
                    Log.e(TAG, ex.toString());
                    /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-05-07,BUG-2042163*/
                    // Req uri is invalid, if continue recording, it will make camera crash !
                    return ;
                    /* MODIFIED-END by bin.zhang2-nb,BUG-2042163*/
                }
            }
            requestedSizeLimit = myExtras.getLong(MediaStore.EXTRA_SIZE_LIMIT);
        }
        mMediaRecorder = new MediaRecorder();
        // Unlock the camera object before passing it to media recorder.
        mCameraDevice.unlock();

        try {
            mMediaRecorder.setCamera(mCameraDevice.getCamera());
        } catch (Exception e) {
            Log.e(TAG, "MediaRecorder setCamera failed");
            e.printStackTrace();
            releaseMediaRecorder();
            return;
        }

        mediaRecorderParameterFetching(mMediaRecorder);

        setRecordLocation();

        // Set output file.
        // Try Uri in the intent first. If it doesn't exist, use our own
        // instead.
        if (mVideoFileDescriptor != null) {
            mMediaRecorder.setOutputFile(mVideoFileDescriptor.getFileDescriptor());
        } else {
            generateVideoFilename(mProfile.fileFormat);
            mMediaRecorder.setOutputFile(mVideoFilename);
        }

        // Set maximum file size.
        long maxFileSize = mActivity.getStorageSpaceBytes() - Storage.LOW_STORAGE_THRESHOLD_BYTES;
        if (requestedSizeLimit > 0 && requestedSizeLimit < maxFileSize) {
            maxFileSize = requestedSizeLimit;
        }

        try {
            mMediaRecorder.setMaxFileSize(maxFileSize);
        } catch (RuntimeException exception) {
            // We are going to ignore failure of setMaxFileSize here, as
            // a) The composer selected may simply not support it, or
            // b) The underlying media framework may not handle 64-bit range
            // on the size restriction.
        }

        // See com.android.camera.cameradevice.CameraSettings.setPhotoRotationDegrees
        // for documentation.
        // Note that mOrientation here is the device orientation, which is the opposite of
        // what activity.getWindowManager().getDefaultDisplay().getRotation() would return,
        // which is the orientation the graphics need to rotate in order to render correctly.
        int rotation = 0;
        if (mOrientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
            Characteristics info =
                    mActivity.getCameraProvider().getCharacteristics(mCameraId);
            if (isCameraFrontFacing()) {
                rotation = (info.getSensorOrientation() - mOrientation + 360) % 360;
            } else if (isCameraBackFacing()) {
                rotation = (info.getSensorOrientation() + mOrientation) % 360;
            } else {
                Log.e(TAG, "Camera is facing unhandled direction");
            }
        }
        mMediaRemcoderRotation = rotation; //MODIFIED by yuanxing.tan, 2016-04-12,BUG-1938552
        Log.w(TAG, "rotation is " + rotation);
        mMediaRecorder.setOrientationHint(rotation);

        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare failed for " + mVideoFilename, e);
            releaseMediaRecorder();
            throw new RuntimeException(e);
        }

        mMediaRecorder.setOnErrorListener(this);
        mMediaRecorder.setOnInfoListener(this);
    }

    /**
     * override by slow motion
     * @return
     */
    protected boolean hideCamera(){
        return false;
    }

    /**
     * overriden by slow motion
     * @return
     */
    protected void mediaRecorderParameterFetching(MediaRecorder recorder){
        recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        recorder.setProfile(mProfile);
        recorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
        recorder.setMaxDuration(getOverrodeVideoDuration());
    }

    /**
     * override by slow motion
     * @return
     */
    protected boolean isSupported(int width, int height){
        return true;
    }

    /**
     * override by slow motion
     * @return
     */
    protected void setHsr(CameraSettings cameraSettings){
        cameraSettings.setHsr("off");
    }

    /**
     * override by slow motion
     * @return
     */
    protected int getProfileQuality(){
        SettingsManager settingsManager = mActivity.getSettingsManager();
        String videoQualityKey = isCameraFrontFacing() ? Keys.KEY_VIDEO_QUALITY_FRONT
                : Keys.KEY_VIDEO_QUALITY_BACK;
        int videoQuality = settingsManager
                .getInteger(SettingsManager.SCOPE_GLOBAL, videoQualityKey);
//        int quality = SettingsUtil.getVideoQuality(videoQuality, mCameraId);
        Log.d(TAG, "Selected video quality for '" + videoQuality);
        return videoQuality;
    }

    private static void setCaptureRate(MediaRecorder recorder, double fps) {
        recorder.setCaptureRate(fps);
    }

    private void setRecordLocation() {
        Location loc = mLocationManager.getCurrentLocation();
        if (loc != null) {
            mMediaRecorder.setLocation((float) loc.getLatitude(),
                    (float) loc.getLongitude());
        }
    }

    private void releaseMediaRecorder() {
        Log.i(TAG, "Releasing media recorder.");
        if (mMediaRecorder != null) {
            cleanupEmptyFile();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        mVideoFilename = null;
    }

    protected void generateVideoFilename(int outputFileFormat) {
        long dateTaken = System.currentTimeMillis();
        String title = createName(dateTaken);
        // Used when emailing.
        String filename = title + convertOutputFormatToFileExt(outputFileFormat);
        String mime = convertOutputFormatToMimeType(outputFileFormat);
        String path = Storage.DIRECTORY + '/' + filename;
        String tmpPath = path + VIDEO_TEMP_SUFFIXES;
        mCurrentVideoValues = new ContentValues(9);
        mCurrentVideoValues.put(Video.Media.TITLE, title);
        mCurrentVideoValues.put(Video.Media.DISPLAY_NAME, filename);
        mCurrentVideoValues.put(Video.Media.DATE_TAKEN, dateTaken);
        mCurrentVideoValues.put(MediaColumns.DATE_MODIFIED, dateTaken / 1000);
        mCurrentVideoValues.put(Video.Media.MIME_TYPE, mime);
        mCurrentVideoValues.put(Video.Media.DATA, path);
        mCurrentVideoValues.put(Video.Media.WIDTH, mProfile.videoFrameWidth);
        mCurrentVideoValues.put(Video.Media.HEIGHT, mProfile.videoFrameHeight);
        mCurrentVideoValues.put(Video.Media.RESOLUTION,
                Integer.toString(mProfile.videoFrameWidth) + "x" +
                Integer.toString(mProfile.videoFrameHeight));
        Location loc = mLocationManager.getCurrentLocation();
        if (loc != null) {
            mCurrentVideoValues.put(Video.Media.LATITUDE, loc.getLatitude());
            mCurrentVideoValues.put(Video.Media.LONGITUDE, loc.getLongitude());
        }
        mVideoFilename = tmpPath;
        Log.v(TAG, "New video filename: " + mVideoFilename);
    }

    private void logVideoCapture(long duration) {
        String flashSetting = mActivity.getSettingsManager()
                .getString(mAppController.getCameraScope(),
                        Keys.KEY_VIDEOCAMERA_FLASH_MODE);
        boolean gridLinesOn = Keys.areGridLinesOn(mActivity.getSettingsManager());
        int width = (Integer) mCurrentVideoValues.get(Video.Media.WIDTH);
        int height = (Integer) mCurrentVideoValues.get(Video.Media.HEIGHT);
        long size = new File(mCurrentVideoFilename).length();
        String name = new File(mCurrentVideoValues.getAsString(Video.Media.DATA)).getName();
        UsageStatistics.instance().videoCaptureDoneEvent(name, duration, isCameraFrontFacing(),
                currentZoomValue(), width, height, size, flashSetting, gridLinesOn);
    }

    protected void saveVideo() {
        if (mVideoFileDescriptor == null) {
            long duration = SystemClock.uptimeMillis() - mRecordingStartTime;

            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(mCurrentVideoFilename);
                final String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                mmr.release();
                duration = Integer.parseInt(durationStr);
            }catch(Exception e){
                Log.e(TAG,"MediaMetadataRetriever error, use estimated duration");
            }

            if (duration > 0) {
                //
            } else {
                Log.w(TAG, "Video duration <= 0 : " + duration);
            }
            mCurrentVideoValues.put(Video.Media.SIZE, new File(mCurrentVideoFilename).length());
            mCurrentVideoValues.put(Video.Media.DURATION, duration);
            if(needAddToMediaSaver()) {
                getServices().getMediaSaver().addVideo(mCurrentVideoFilename,
                        mCurrentVideoValues, getVideoSavedListener(), mContentResolver);
            }
            logVideoCapture(duration);
        }
        mCurrentVideoValues = null;
    }

    /**
     * May be overriden by sub-mode
     * @return
     */
    protected boolean needAddToMediaSaver(){
        return true;
    }

    private void deleteVideoFile(String fileName) {
        Log.v(TAG, "Deleting video " + fileName);
        File f = new File(fileName);
        if (!f.delete()) {
            Log.v(TAG, "Could not delete " + fileName);
        }
    }

    // from MediaRecorder.OnErrorListener
    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        Log.e(TAG, "MediaRecorder error. what=" + what + ". extra=" + extra);
        if (what == MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN) {
            // We may have run out of space on the sdcard.
            stopVideoRecording();
            mActivity.updateStorageSpaceAndHint(null);
        }
    }

    // from MediaRecorder.OnInfoListener
    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            if (mMediaRecorderRecording) {
                mHandler.sendEmptyMessage(MSG_STOP_RECORDING);
            }
        } else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
            if (mMediaRecorderRecording) {
                mHandler.sendEmptyMessage(MSG_STOP_RECORDING);
            }

            // Show the toast.
            /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
            SnackbarToast.getSnackbarToast().showToast(mActivity, mActivity.getString(R.string.video_reach_size_limit)
                    ,SnackbarToast.LENGTH_LONG,SnackbarToast.DEFAULT_Y_OFFSET);
                    /* MODIFIED-END by bin-liu3,BUG-3253898*/
        }
    }

    /*
     * Make sure we're not recording music playing in the background, ask the
     * MediaPlaybackService to pause playback.
     */
    private void pauseAudioPlayback() {
        Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        mActivity.sendBroadcast(i);

        AudioManager am = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(mAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    private void releaseAudioFocus(){
        AudioManager am = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(mAudioFocusChangeListener);
    }

    private final AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    /* MODIFIED-BEGIN by xuan.zhou, 2016-05-18,BUG-2193375*/
                    boolean ignoreAudioFocus = CustomUtil.getInstance().getBoolean(
                            CustomFields.DEF_RECORDING_IGNORE_AUDIOFOCUS, false);
                    Log.i(TAG, "AudioFocus change, " + focusChange + ", ignore " + ignoreAudioFocus);
                    if (!ignoreAudioFocus) {
                        stopVideoWhileAudioFocusLoss();
                    }
                    /* MODIFIED-END by xuan.zhou,BUG-2193375*/
                    break;
            }
        }
    };

    protected void stopVideoWhileAudioFocusLoss(){
        mHandler.sendEmptyMessage(MSG_STOP_RECORDING);
    }

    // For testing.
    public boolean isRecording() {
        return mMediaRecorderRecording;
    }



    protected void tryLockFocus(){
        //do nothing here , only valid in slow motion
    }

    private Integer mModeSelectionLockToken=null;
    protected void startVideoRecording() {
        if(mCameraState!=IDLE){
            return;
        }
        setCameraState(RECORDING_PENDING_START);
        Log.i(TAG, "startVideoRecording: " + Thread.currentThread());
        mAppController.onVideoRecordingStarted();
        if(mModeSelectionLockToken==null) {
            mModeSelectionLockToken = mAppController.lockModuleSelection();
        }
        mUI.showVideoRecordingHints(false);
        mUI.cancelAnimations();
        mUI.setSwipingEnabled(false);
        mUI.showFocusUI(false);
        setTopModeOptionVisibility(false);
        mActivity.updateStorageSpaceAndHint(new CameraActivity.OnStorageUpdateDoneListener() {
            @Override
            public void onStorageUpdateDone(long bytes) {
                if (mCameraState != RECORDING_PENDING_START) {
                    // Set camera state in pendingRecordFailed().
                    // setCameraState(IDLE);
                    pendingRecordFailed();
                    return;
                }
                if (bytes <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
                    Log.w(TAG, "Storage issue, ignore the start request");
                    pendingRecordFailed();
                /* MODIFIED-BEGIN by fei.hui, 2016-09-09,BUG-2868515*/
                } else if(mActivity.isBatteryCriticalLow()){
                    /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
                    SnackbarToast.getSnackbarToast().showToast(mActivity,
                            mActivity.getString(R.string.battery_info_video_low_toast_message)
                            , SnackbarToast.LENGTH_SHORT,SnackbarToast.DEFAULT_Y_OFFSET);
                            /* MODIFIED-END by bin-liu3,BUG-3253898*/
                    pendingRecordFailed();
                }else {
                /* MODIFIED-END by fei.hui,BUG-2868515*/
                    if (mCameraDevice == null) {
                        Log.v(TAG, "in storage callback after camera closed");
                        pendingRecordFailed();
                        return;
                    }
                    if (mPaused == true) {
                        Log.v(TAG, "in storage callback after module paused");
                        pendingRecordFailed();
                        return;
                    }

                    // Monkey is so fast so it could trigger startVideoRecording twice. To prevent
                    // app crash (b/17313985), do nothing here for the second storage-checking
                    // callback because recording is already started.
                    if (mMediaRecorderRecording) {
                        Log.v(TAG, "in storage callback after recording started");
                        return;
                    }

                    if (!isSupported(mProfile.videoFrameWidth, mProfile.videoFrameHeight)) {
                        Log.e(TAG, "Unsupported parameters");
                        pendingRecordFailed();
                        return;
                    }

                    mCurrentVideoUri = null;


                    mCameraDevice.enableShutterSound(Keys.isShutterSoundOn(mAppController
                            .getSettingsManager()));

                    initializeRecorder();
                    if (mMediaRecorder == null) {
                        Log.e(TAG, "Fail to initialize media recorder");
                        pendingRecordFailed();
                        return;
                    }

                    pauseAudioPlayback();

                    try {
                        mMediaRecorder.start(); // Recording is now started
                    } catch (RuntimeException e) {
                        Log.e(TAG, "Could not start media recorder. ", e);
                        releaseMediaRecorder();
                        // If start fails, frameworks will not lock the camera for us.
                        mCameraDevice.lock();
//                        onStopVideoRecording();
                        releaseAudioFocus();
                        if (mModeSelectionLockToken != null) {
                            mAppController.unlockModuleSelection(mModeSelectionLockToken);
                        }
                        setCameraState(IDLE);
                        if (shouldHoldRecorderForSecond()) {
                            mAppController.setShutterEnabled(true);
                        }
                        mAppController.getCameraAppUI().showModeOptions();
                        if (updateModeSwitchUIinModule()) {
                            mAppController.getCameraAppUI().setModeSwitchUIVisibility(true);
                        }
                        if (isNeedStartRecordingOnSwitching()) {
                            mAppController.onVideoRecordingStop();
                        }
                        /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
                        SnackbarToast.getSnackbarToast().showToast(mActivity.getApplicationContext(), mActivity.getString(R.string.video_record_start_failed)
                                , SnackbarToast.LENGTH_SHORT,SnackbarToast.DEFAULT_Y_OFFSET);
                                /* MODIFIED-END by bin-liu3,BUG-3253898*/
                        return;
                    }

                    mAppController.getCameraAppUI().setSwipeEnabled(false);

                    // The parameters might have been altered by MediaRecorder already.
                    // We need to force mCameraDevice to refresh before getting it.
                    mCameraDevice.refreshSettings();
                    // The parameters may have been changed by MediaRecorder upon starting
                    // recording. We need to alter the parameters if we support camcorder
                    // zoom. To reduce latency when setting the parameters during zoom, we
                    // update the settings here once.
                    mCameraSettings = mCameraDevice.getSettings();

                    setCameraState(RECORDING);
                    tryLockFocus();
                    mMediaRecorderRecording = true;
//                    mActivity.lockOrientation();
                    mRecordingStartTime = SystemClock.uptimeMillis();

                    // A special case of mode options closing: during capture it should
                    // not be possible to change mode state.
                    mAppController.getCameraAppUI().hideModeOptions();
                    if (updateModeSwitchUIinModule()) {
                        mAppController.getCameraAppUI().setModeSwitchUIVisibility(false);
                    }
                    if (isVideoShutterAnimationEnssential()) {
                        mAppController.getCameraAppUI()
                                .animateBottomBarToVideoStop(R.drawable.ic_video_recording);
                    }
                    if (isNeedStartRecordingOnSwitching()) {
                        mAppController.getCameraAppUI().showVideoCaptureButton(true);
                    }
                    mUI.showRecordingUI(true);

                    resetPauseButton(); // MODIFIED by guodong.zhang, 2016-11-01,BUG-3272008
                    mAppController.getCameraAppUI().setVideoBottomBarVisible(false);

                    showBoomKeyTip();

                    setFocusParameters();

                    updateRecordingTime();

//                    mUI.lockRecordingOrientation();

                    if (isSendMsgEnableShutterButton()) {
                        mHandler.sendEmptyMessageDelayed(MSG_ENABLE_SHUTTER_BUTTON, MIN_VIDEO_RECODER_DURATION);
                    }

                    mActivity.enableKeepScreenOn(true);

                    // Checking when recording start successful.
                    mActivity.startInnerStorageChecking(new CameraActivity.OnInnerStorageLowListener() {
                        @Override
                        public void onInnerStorageLow(long bytes) {
                            mActivity.stopInnerStorageChecking();
                            if (/*mMediaRecorderRecording*/
                                    /* MODIFIED-BEGIN by fei.hui, 2016-09-09,BUG-2868515*/
                                    mCameraState == RECORDING) {
//                                showQuitDialog(R.string.quit_dialog_title_storage_low,
//                                        R.string.quit_dialog_msg, saveAndQuit);
                                /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
                                SnackbarToast.getSnackbarToast().showToast(mActivity,
                                        mActivity.getString(R.string.storage_low_video_critical_toast_message)
                                        ,SnackbarToast.LENGTH_LONG,SnackbarToast.DEFAULT_Y_OFFSET);
                                        /* MODIFIED-END by bin-liu3,BUG-3253898*/
                                stopVideoRecording();
                            }
                        }
                            @Override
                            public void onInnerStorage ( long bytes){
                                showRemainTime(bytes - Storage.LOW_STORAGE_THRESHOLD_BYTES);
                            }
                    });
                    mActivity.startBatteryInfoChecking(new CameraActivity.OnBatteryLowListener() {
                        @Override
                        public void onBatteryLow(int level) {
                            mActivity.stopBatteryInfoChecking();
                            //if (/*mMediaRecorderRecording*/
                                    /* MODIFIED-BEGIN by feifei.xu, 2016-09-01,BUG-2828702*/
                                   // mCameraState == RECORDING && level < 2) {
//                                showQuitDialog(R.string.quit_dialog_title_battery_low,
//                                        R.string.quit_dialog_msg, saveAndQuit);
                              //  ToastUtil.showToast(mActivity,
                               //         mActivity.getString(R.string.battery_info_video_low_toast_message), Toast.LENGTH_SHORT);
                              //  stopVideoRecording();
                          //  }

                            if (mCameraState == RECORDING ) {
                                /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
                                SnackbarToast.getSnackbarToast().showToast(mActivity,
                                        mActivity.getString(R.string.battery_info_video_low_less_8)
                                        ,SnackbarToast.LENGTH_LONG,SnackbarToast.DEFAULT_Y_OFFSET);
                                        /* MODIFIED-END by bin-liu3,BUG-3253898*/
                                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                                builder.setCancelable(false);
                                builder.setTitle(R.string.battery_info_dialog_tile);
                                builder.setMessage(R.string.battery_info_video_low_less_8_dialog);
                                builder.setNegativeButton(R.string.battery_info_video_low_cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        stopVideoRecording();
                                    }
                                });
                                builder.setPositiveButton(R.string.battery_info_video_low_continue, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                builder.create().show();
                            }
                            /* MODIFIED-END by fei.hui,BUG-2868515*/
                        }
                    });
                }
            }
        });
    }

    protected void showBoomKeyTip() {

    }

    protected void hideBoomKeyTip() {

    }

    protected void pendingRecordFailed() {
        pendingRecordFailed(0);
    }

    protected void pendingRecordFailed(int toastId) {
        if(mModeSelectionLockToken!=null) {
            mAppController.unlockModuleSelection(mModeSelectionLockToken);
        }
        if(mCameraDevice!=null) {
            mCameraDevice.lock();
        }
        mVideoFilename = null;
        setCameraState(IDLE);
        if (shouldHoldRecorderForSecond()) {
            mAppController.setShutterEnabled(true);
        }
        mAppController.getCameraAppUI().getTopMenus().setTopModeOptionVisibility(true); // MODIFIED by guodong.zhang, 2016-11-14,BUG-3330877
        mAppController.getCameraAppUI().showModeOptions();
        if (updateModeSwitchUIinModule()) {
            mAppController.getCameraAppUI().setModeSwitchUIVisibility(true);
        }
        if (isNeedStartRecordingOnSwitching()) {
            mAppController.onVideoRecordingStop();
        }
        if (toastId != 0) {
            ToastUtil.showToast(mActivity.getApplicationContext(),
                    toastId, Toast.LENGTH_SHORT);
        } else {
            /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
            SnackbarToast.getSnackbarToast().showToast(mActivity.getApplicationContext(),
                    mActivity.getString(R.string.video_record_start_failed)
                    , SnackbarToast.LENGTH_SHORT,SnackbarToast.DEFAULT_Y_OFFSET);
                    /* MODIFIED-END by bin-liu3,BUG-3253898*/
        }
    }

    protected boolean isVideoShutterAnimationEnssential(){
        return true;
    }

    private Bitmap getVideoThumbnail() {
        Bitmap bitmap = null;
        if (mVideoFileDescriptor != null) {
            bitmap = Thumbnail.createVideoThumbnailBitmap(mVideoFileDescriptor.getFileDescriptor(),
                    mDesiredPreviewWidth);
        } else if (mCurrentVideoUri != null) {
            try {
                mVideoFileDescriptor = mContentResolver.openFileDescriptor(mCurrentVideoUri, "r");
                bitmap = Thumbnail.createVideoThumbnailBitmap(
                        mVideoFileDescriptor.getFileDescriptor(), mDesiredPreviewWidth);
            } catch (java.io.FileNotFoundException ex) {
                // invalid uri
                Log.e(TAG, ex.toString());
            }
        }

        if (bitmap != null) {
            // MetadataRetriever already rotates the thumbnail. We should rotate
            // it to match the UI orientation (and mirror if it is front-facing camera).
            bitmap = CameraUtil.rotateAndMirror(bitmap, 0, isCameraFrontFacing());
        }
        return bitmap;
    }

    private void showCaptureResult() {
        mIsInReviewMode = true;
        Bitmap bitmap = getVideoThumbnail();
        if (bitmap != null) {
            mUI.showReviewImage(bitmap);
        }
        mUI.showReviewControls();
        /* MODIFIED-BEGIN by xuan.zhou, 2016-05-27,BUG-1840762*/
        // disable torch now.
        enableTorchMode(false);
        /* MODIFIED-END by xuan.zhou,BUG-1840762*/
    }

    private boolean stopVideoRecording() {
        // Do nothing if camera device is still capturing photo. Monkey test can trigger app crashes
        // (b/17313985) without this check. Crash could also be reproduced by continuously tapping
        // on shutter button and preview with two fingers.
//        if (mSnapshotInProgress) {
//            Log.v(TAG, "Skip stopVideoRecording since snapshot in progress");
//            return true;
//        }
        //It's not clarified why we need ignore stopVideoRecording during video-snap .
        //It's necessary to stop mediaRecorder in any case we've input a stop action, thus we can re-lock camera object in Camera Application Process
        //Or re-lock would get failed because the camera object is still occupied by MediaRecorder.
        Log.v(TAG, "stopVideoRecording");
        if (mFocusManager != null) {
            mFocusManager.overrideFocusMode(null);
        }

        mUI.setSwipingEnabled(true);
        mUI.showFocusUI(true);
        mUI.showVideoRecordingHints(true);

//        mUI.unlockRecordingOrientation();

        boolean fail = false;
        if (/*mMediaRecorderRecording*/
                mCameraState==RECORDING) {
            boolean shouldAddToMediaStoreNow = false;

            try {
                mMediaRecorder.setOnErrorListener(null);
                mMediaRecorder.setOnInfoListener(null);
                mMediaRecorder.stop();
                shouldAddToMediaStoreNow = !mRecordingInterrupted;
                mCurrentVideoFilename = mVideoFilename;
                Log.v(TAG, "stopVideoRecording: current video filename: " + mCurrentVideoFilename);
            } catch (RuntimeException e) {
                Log.e(TAG, "stop fail",  e);
                if (mVideoFilename != null) {
                    deleteVideoFile(mVideoFilename);
                }
                fail = true;
            }
            setCameraState(RECORDING_PENDING_STOP);
            mMediaRecorderRecording = false;
//            mActivity.unlockOrientation();

            // If the activity is paused, this means activity is interrupted
            // during recording. Release the camera as soon as possible because
            // face unlock or other applications may need to use the camera.
            if (mPaused) {
                // b/16300704: Monkey is fast so it could pause the module while recording.
                // stopPreview should definitely be called before switching off.
                stopPreview();

                closeCamera();
            }

            mUI.showRecordingUI(false);
            hideBoomKeyTip();
            // The orientation was fixed during video recording. Now make it
            // reflect the device orientation as video recording is stopped.
            mUI.setOrientationIndicator(0, true);
            mActivity.enableKeepScreenOn(false);
            if (shouldAddToMediaStoreNow && !fail) {
                if (mVideoFileDescriptor == null) {
                    saveVideo();
                } else if (mIsVideoCaptureIntent) {
                    // if no file save is needed, we can show the post capture UI now
                    showCaptureResult();
                }
            }
        }
        // release media recorder
        releaseMediaRecorder();



        if (!mPaused && mCameraDevice != null) {
            setFocusParameters();
            mCameraDevice.lock();
            if (!ApiHelper.HAS_SURFACE_TEXTURE_RECORDING) {
                stopPreview();
                // Switch back to use SurfaceTexture for preview.
                startPreview();
            }
            // Update the parameters here because the parameters might have been altered
            // by MediaRecorder.
            clearFocus();
            mCameraSettings = mCameraDevice.getSettings();
        }

        // Check this in advance of each shot so we don't add to shutter
        // latency. It's true that someone else could write to the SD card
        // in the mean time and fill it, but that could have happened
        // between the shutter press and saving the file too.
        mActivity.updateStorageSpaceAndHint(null);

        final Runnable resetRunnable=new Runnable() {
            @Override
            public void run() {
                if(mModeSelectionLockToken!=null) {
                    mAppController.unlockModuleSelection(mModeSelectionLockToken);
                }
                if(mCameraState==RECORDING_PENDING_STOP) {//Could be PREVIEW_STOPPED in case of paused ,thus it would be stopped and camera would be closed in former case
                    setCameraState(IDLE);
                    cancelAutoFocus();
                }
                if (!isVideoCaptureIntent()) {
                    mAppController.getCameraAppUI().showModeOptions();
                }
                if (updateModeSwitchUIinModule()) {
                    mAppController.getCameraAppUI().setModeSwitchUIVisibility(true);
                }
            }
        };

        if(isVideoShutterAnimationEnssential()) {
            BottomBar.BottomBarSizeListener animateDoneListener=new BottomBar.BottomBarSizeListener() {
                @Override
                public void onFullSizeReached() {
                    resetRunnable.run();
                    if (isNeedStartRecordingOnSwitching()) {
                        mAppController.onVideoRecordingStop();
                    }
                }
            };
            if(mPaused){
                resetRunnable.run();
            }
            mAppController.getCameraAppUI().animateBottomBarToFullSize(mShutterIconId, mPaused?null:animateDoneListener);

        }else{
            resetRunnable.run();
        }

        mAppController.getCameraAppUI().hideVideoCaptureButton(false);

        setTopModeOptionVisibility(true);
        mAppController.getCameraAppUI().setVideoBottomBarVisible(true);
        mUI.setTimeLeftUI(false, "");
        return fail;
    }


    private static String millisecondToTimeString(long milliSeconds, boolean displayCentiSeconds) {
        long seconds = milliSeconds / 1000; // round down to compute seconds
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long remainderMinutes = minutes - (hours * 60);
        long remainderSeconds = seconds - (minutes * 60);

        StringBuilder timeStringBuilder = new StringBuilder();

        // Hours
        if (hours > 0) {
            if (hours < 10) {
                timeStringBuilder.append('0');
            }
            timeStringBuilder.append(hours);

            timeStringBuilder.append(':');
        }

        // Minutes
        if (remainderMinutes < 10) {
            timeStringBuilder.append('0');
        }
        timeStringBuilder.append(remainderMinutes);
        timeStringBuilder.append(':');

        // Seconds
        if (remainderSeconds < 10) {
            timeStringBuilder.append('0');
        }
        timeStringBuilder.append(remainderSeconds);

        // Centi seconds
        if (displayCentiSeconds) {
            timeStringBuilder.append('.');
            long remainderCentiSeconds = (milliSeconds - seconds * 1000) / 10;
            if (remainderCentiSeconds < 10) {
                timeStringBuilder.append('0');
            }
            timeStringBuilder.append(remainderCentiSeconds);
        }

        return timeStringBuilder.toString();
    }

    protected boolean isVideoRecordingPaused() {
        return false;
    }

    protected long getDeltaTime() {
        return (SystemClock.uptimeMillis() - mRecordingStartTime);
    }

    private void updateRecordingTime() {
        if (isVideoRecordingPaused()) {
            mUI.setDotbBlink(isVideoRecordingPaused());
            return;
        }
        if (!mMediaRecorderRecording) {
            return;
        }
//        long now = SystemClock.uptimeMillis();
//        long delta = now - mRecordingStartTime;
        long delta = getDeltaTime();;

        // Starting a minute before reaching the max duration
        // limit, we'll countdown the remaining time instead.
        boolean countdownRemainingTime = (mMaxVideoDurationInMs != 0
                && delta >= mMaxVideoDurationInMs - 60000);

        long deltaAdjusted = delta;
        if (countdownRemainingTime) {
            deltaAdjusted = Math.max(0, mMaxVideoDurationInMs - deltaAdjusted) + 999;
        }
        String text;

        long targetNextUpdateDelay;

        text = millisecondToTimeString(deltaAdjusted, false);
        targetNextUpdateDelay = 1000;

        mUI.setRecordingTime(text);

        mUI.setDotbBlink(isVideoRecordingPaused());

        if (mRecordingTimeCountsDown != countdownRemainingTime) {
            // Avoid setting the color on every update, do it only
            // when it needs changing.
            mRecordingTimeCountsDown = countdownRemainingTime;

            int color = mActivity.getResources().getColor(R.color.recording_time_remaining_text);

            mUI.setRecordingTimeTextColor(color);
        }

        long actualNextUpdateDelay = targetNextUpdateDelay - (delta % targetNextUpdateDelay);
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_RECORD_TIME, actualNextUpdateDelay);
        onVideoRecordingStarted();
    }

    protected void onVideoRecordingStarted(){
        mUI.unlockCaptureView();
    }

    private static boolean isSupported(String value, List<String> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }

    @SuppressWarnings("deprecation")
    private void setCameraParameters() {
        SettingsManager settingsManager = mActivity.getSettingsManager();

        // Update Desired Preview size in case video camera resolution has changed.
        updateDesiredPreviewSize();

        mCameraSettings.setPreviewSize(new Size(mDesiredPreviewWidth, mDesiredPreviewHeight));
        // This is required for Samsung SGH-I337 and probably other Samsung S4 versions
        if (Build.BRAND.toLowerCase().contains("samsung")) {
            mCameraSettings.setSetting("video-size",
                    mProfile.videoFrameWidth + "x" + mProfile.videoFrameHeight);
        }

        mCameraSettings.setVideoSize(new Size(mProfile.videoFrameWidth, mProfile.videoFrameHeight));

//        int[] fpsRange =
//                CameraUtil.getMaxPreviewFpsRange(mCameraCapabilities.getSupportedPreviewFpsRange());
        //The former implement fixed the frame rate to the MAX, which would make the exposure-time much lower thus the recorded video would be rather dark in darker space.
        //The very first intent to fixed the frame rate may be to make sure the sample rate of video recording is higher than the encoding rate.
        int[] fpsRange = CameraUtil.getPhotoPreviewFpsRange(mCameraCapabilities);
        if (fpsRange.length > 0) {
            mCameraSettings.setPreviewFpsRange(fpsRange[0], fpsRange[1]);
        } else {
            mCameraSettings.setPreviewFrameRate(mProfile.videoFrameRate);
        }

        //enableTorchMode(Keys.isCameraBackFacing(settingsManager, SettingsManager.SCOPE_GLOBAL));

        // Set zoom.
        if (mCameraCapabilities.supports(CameraCapabilities.Feature.ZOOM)) {
            mCameraSettings.setZoomRatio(mZoomValue);
        }
        updateFocusParameters();

        mCameraSettings.setRecordingHintEnabled(true);

        if (mCameraCapabilities.supports(CameraCapabilities.Feature.VIDEO_STABILIZATION)) {
            mCameraSettings.setVideoStabilization(isVideoStabilizationEnabled());
        }

        // Set picture size.
        // The logic here is different from the logic in still-mode camera.
        // There we determine the preview size based on the picture size, but
        // here we determine the picture size based on the preview size.
        List<Size> supported = mCameraCapabilities.getSupportedPhotoSizes();
        Size optimalSize = CameraUtil.getOptimalVideoSnapshotPictureSize(supported,
                mDesiredPreviewWidth, mDesiredPreviewHeight);
        Size original = new Size(mCameraSettings.getCurrentPhotoSize());
        if (!original.equals(optimalSize)) {
            mCameraSettings.setPhotoSize(optimalSize);
        }
        Log.d(TAG, "Video snapshot size is " + optimalSize);

        // Set JPEG quality.
        int jpegQuality = CameraProfile.getJpegEncodingQualityParameter(mCameraId,
                CameraProfile.QUALITY_HIGH);
        mCameraSettings.setPhotoJpegCompressionQuality(jpegQuality);

        setHsr(mCameraSettings);
        updateParametersAntibanding();
        updateFacebeauty();
        // update flash parameters
        enableTorchMode(true);
        if (mCameraDevice != null) {
            mCameraDevice.applySettings(mCameraSettings);
            // Nexus 5 through KitKat 4.4.2 requires a second call to
            // .setParameters() for frame rate settings to take effect.
            mCameraDevice.applySettings(mCameraSettings);
        }

        // Update UI based on the new parameters.
        mUI.updateOnScreenIndicators(mCameraSettings);
    }

    protected boolean isFacebeautyEnabled(){
        return false;
    }

    private void updateFacebeauty(){
        if (!isFacebeautyEnabled()) {
            return;
        }
        SettingsManager settingsManager = mActivity.getSettingsManager();
        if (isCameraFrontFacing()) {
            int defSkinSmoothing = CustomUtil.getInstance().getInt(CustomFields.DEF_CAMERA_SKIN_SMOOTHING, PhotoModule.SKIN_SMOOTHING_DEFAULT);
            int skinSmoothing = mActivity.getSettingsManager().getInteger(SettingsManager.SCOPE_GLOBAL, Keys.KEY_FACEBEAUTY_SKIN_SMOOTHING, defSkinSmoothing);
            /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-05-27,BUG-2212748*/
            //mCameraSettings.setFaceBeauty(Keys.isFacebeautyOn(settingsManager), skinSmoothing * PhotoModule.SKIN_SMOOTHING_MAX / PhotoModule.SKIN_SMOOTHING_RANGE);

            int defSkinWhitening = CustomUtil.getInstance().getInt(CustomFields.DEF_CAMERA_SKIN_WHITE, PhotoModule.SKIN_WHITE_DEFAULT);
            int skinWhitening = mActivity.getSettingsManager().getInteger(SettingsManager.SCOPE_GLOBAL, Keys.KEY_FACEBEAUTY_SKIN_WHITE, defSkinWhitening);

            mCameraSettings.setFaceBeauty(Keys.isFacebeautyOn(settingsManager),
                    skinSmoothing * PhotoModule.SKIN_SMOOTHING_MAX / PhotoModule.SKIN_SMOOTHING_RANGE,
                    skinWhitening * PhotoModule.SKIN_WHITE_MAX / PhotoModule.SKIN_WHITE_RANGE);
                    /* MODIFIED-END by bin.zhang2-nb,BUG-2212748*/
        }
    }

    protected boolean isVideoStabilizationEnabled() {
        return Keys.isVideoStabilizationEnabled(mAppController.getSettingsManager());
    }

    private void updateParametersAntibanding() {
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

    private void updateFocusParameters() {
        // Set continuous autofocus. During recording, we use "continuous-video"
        // auto focus mode to ensure smooth focusing. Whereas during preview (i.e.
        // before recording starts) we use "continuous-picture" auto focus mode
        // for faster but slightly jittery focusing.
//        Set<CameraCapabilities.FocusMode> supportedFocus = mCameraCapabilities
//                .getSupportedFocusModes();
//        if (mMediaRecorderRecording) {
//            if (mCameraCapabilities.supports(CameraCapabilities.FocusMode.CONTINUOUS_VIDEO)) {
//                mCameraSettings.setFocusMode(CameraCapabilities.FocusMode.CONTINUOUS_VIDEO);
//                mFocusManager.overrideFocusMode(CameraCapabilities.FocusMode.CONTINUOUS_VIDEO);
//            } else {
//                mFocusManager.overrideFocusMode(null);
//            }
//        } else {
            // FIXME(b/16984793): This is broken. For some reasons, CONTINUOUS_PICTURE is not on
            // when preview starts.
//            mFocusManager.overrideFocusMode(null);
            if (mCameraCapabilities.supports(CameraCapabilities.FocusMode.CONTINUOUS_PICTURE)) {
                mCameraSettings.setFocusMode(
                        mFocusManager.getFocusMode(mCameraSettings.getCurrentFocusMode()));
                if (mFocusAreaSupported) {
                    mCameraSettings.setFocusAreas(mFocusManager.getFocusAreas());
                }
            }
//        }
        updateAutoFocusMoveCallback();
    }

    protected void disableShutterDuringResume(){
        mAppController.setShutterEnabledWithNormalAppearence(false);// MODIFIED by sichao.hu, 2016-03-22, BUG-1027573
    }

    @Override
    public void resume() {
        Log.w(TAG,"KPI video resume");
        Log.v(TAG, "resume in video");
        if (isVideoCaptureIntent()) {
            mDontResetIntentUiOnResume = mPaused;
        }

        mPaused = false;
        if(mActivity!=null) {
            SettingsManager settingsManager = mActivity.getSettingsManager();
            settingsManager.addListener(this);
            mCameraId = settingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,
                    Keys.KEY_CAMERA_ID);
        }
        // installIntentFilter();
        disableShutterDuringResume();
        mZoomValue = 1.0f;
        mAppController.getCameraAppUI().resetZoomBar(); // MODIFIED by jianying.zhang, 2016-11-01,BUG-3271894
        showVideoSnapshotUI(false);

        Log.v(TAG, "mCameraState is " + mCameraState);
        if (/*!mPreviewing*/
                mCameraState==PREVIEW_STOPPED) {
            Log.v(TAG, "isCameraBoosted?" + mActivity.getCameraProvider().isCameraRequestBoosted());
            if(!mActivity.getCameraProvider().isCameraRequestBoosted()) {
                requestCamera(mCameraId);
            }
        } else {
            // preview already started
            mAppController.setShutterEnabled(true);
        }

        /* MODIFIED-BEGIN by xuan.zhou, 2016-06-06,BUG-2251935*/
        if (!mSpecificUIReady) {
            disableFlashButtonClick();
        }
        /* MODIFIED-END by xuan.zhou,BUG-2251935*/

        if (mFocusManager != null) {
            // If camera is not open when resume is called, focus manager will not
            // be initialized yet, in which case it will start listening to
            // preview area size change later in the initialization.
            mAppController.addPreviewAreaSizeChangedListener(mFocusManager);
        }

        if (/*mPreviewing*/
                mCameraState!=PREVIEW_STOPPED) {
            mOnResumeTime = SystemClock.uptimeMillis();
            mHandler.sendEmptyMessageDelayed(MSG_CHECK_DISPLAY_ROTATION, 100);
        }
        getServices().getMemoryManager().addListener(this);
    }

    @Override
    public void preparePause() {
        if (mCameraState != IDLE) {
            return;
        }
        stopPreview();
    }

    @Override
    public boolean isPaused() {
        return mPaused;
    }

    @Override
    public void pause() {
        mPaused = true;

        Log.w(TAG,"KPI video pause E");
        mAppController.getLockEventListener().onIdle();
        if (mFocusManager != null) {
            // If camera is not open when resume is called, focus manager will not
            // be initialized yet, in which case it will start listening to
            // preview area size change later in the initialization.
            mAppController.removePreviewAreaSizeChangedListener(mFocusManager);
            mFocusManager.removeMessages();
        }

        if (mMediaRecorderRecording) {
            // Camera will be released in onStopVideoRecording.
            onStopVideoRecording();
        } else {
            stopPreview();
            closeCamera();
            releaseMediaRecorder();
        }

        if (quitDialog != null && quitDialog.isShowing()) {
            quitDialog.dismiss();
        }

        closeVideoFileDescriptor();

        if (mReceiver != null) {
            mActivity.unregisterReceiver(mReceiver);
            mReceiver = null;
        }

        mHandler.removeMessages(MSG_CHECK_DISPLAY_ROTATION);
        mHandler.removeMessages(MSG_SWITCH_CAMERA);
        mHandler.removeMessages(MSG_SWITCH_CAMERA_START_ANIMATION);
        mPendingSwitchCameraId = -1;
        mSwitchingCamera = false;
        mPreferenceRead = false;
        getServices().getMemoryManager().removeListener(this);
        mUI.onPause();

        hideBoomKeyTip();
        SettingsManager settingsManager = mActivity.getSettingsManager();
        settingsManager.removeListener(this);

        ToastUtil.cancelToast();
        Log.w(TAG, "KPI video pause X");
    }

    @Override
    public void destroy() {

    }

    @Override
    public void onLayoutOrientationChanged(boolean isLandscape) {
        setDisplayOrientation();
    }

    // TODO: integrate this into the SettingsManager listeners.
    public void onSharedPreferenceChanged() {

    }

    private void switchCamera() {
        if (mPaused)  {
            return;
        }

        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
        if (mAppController.validateFilterSelected(mAppController.getCurrentModuleIndex(),
                mCameraId != mPendingSwitchCameraId)) {
            return;
        }
        /* MODIFIED-END by jianying.zhang,BUG-3255060*/

        mAppController.freezeScreenUntilPreviewReady();
        SettingsManager settingsManager = mActivity.getSettingsManager();

        Log.d(TAG, "Start to switch camera.");
        mCameraId = mPendingSwitchCameraId;
        mPendingSwitchCameraId = -1;
        settingsManager.set(SettingsManager.SCOPE_GLOBAL,
                            Keys.KEY_CAMERA_ID, mCameraId);

        if (mFocusManager != null) {
            mFocusManager.removeMessages();
        }
        closeCamera();
        requestCamera(mCameraId);

        mMirror = isCameraFrontFacing();
        if (mFocusManager != null) {
            mFocusManager.setMirror(mMirror);
        }

        // From onResume
        mZoomValue = 1.0f;
        mUI.setOrientationIndicator(0, false);

        // Start switch camera animation. Post a message because
        // onFrameAvailable from the old camera may already exist.
        mHandler.sendEmptyMessage(MSG_SWITCH_CAMERA_START_ANIMATION);
        mUI.updateOnScreenIndicators(mCameraSettings);
    }

    private void initializeVideoSnapshot() {
        if (mCameraSettings == null) {
            return;
        }
    }

    void showVideoSnapshotUI(boolean enabled) {
        if (mCameraSettings == null) {
            return;
        }
        if (mCameraCapabilities.supports(CameraCapabilities.Feature.VIDEO_SNAPSHOT) &&
                !mIsVideoCaptureIntent) {
            if (enabled) {
                mUI.animateFlash();
            } else {
                mUI.showPreviewBorder(enabled);
            }
            mAppController.setShutterEnabled(!enabled);
        }
    }

    /**
     * Used to update the flash mode. Video mode can turn on the flash as torch
     * mode, which we would like to turn on and off when we switching in and
     * out to the preview.
     *
     * @param enable Whether torch mode can be enabled.
     */
    protected void enableTorchMode(boolean enable) {
        if (mCameraSettings.getCurrentFlashMode() == null) {
            return;
        }

        SettingsManager settingsManager = mActivity.getSettingsManager();

        CameraCapabilities.Stringifier stringifier = mCameraCapabilities.getStringifier();
        CameraCapabilities.FlashMode flashMode;
        if (enable) {
            flashMode = stringifier
                .flashModeFromString(settingsManager.getString(mAppController.getCameraScope(),
                                                               Keys.KEY_VIDEOCAMERA_FLASH_MODE));
        } else {
            flashMode = CameraCapabilities.FlashMode.OFF;
        }
        if (mCameraCapabilities.supports(flashMode)) {
            mCameraSettings.setFlashMode(flashMode);
        }
        /* TODO: Find out how to deal with the following code piece:
        else {
            flashMode = mCameraSettings.getCurrentFlashMode();
            if (flashMode == null) {
                flashMode = mActivity.getString(
                        R.string.pref_camera_flashmode_no_flash);
                mParameters.setFlashMode(flashMode);
            }
        }*/
        if (mCameraDevice != null) {
            mCameraDevice.applySettings(mCameraSettings);
        }
        mUI.updateOnScreenIndicators(mCameraSettings);
    }

    @Override
    public void onPreviewVisibilityChanged(int visibility) {
        if (/*mPreviewing*/
                mCameraState!=PREVIEW_STOPPED) {
            //enableTorchMode(visibility == ModuleController.VISIBILITY_VISIBLE);
        }
    }

    private final class JpegPictureCallback implements CameraPictureCallback {
        Location mLocation;

        public JpegPictureCallback(Location loc) {
            mLocation = loc;
        }

        @Override
        public void onPictureTaken(byte [] jpegData, CameraProxy camera) {
            Log.i(TAG, "Video snapshot taken.");
            TestUtils.sendMessage(R.id.video_snap_button, TestUtils.MESSAGE.PICTURE_TAKEN); // MODIFIED by wenhua.tu, 2016-08-11,BUG-2710178
            mSnapshotInProgress = false;
            if(mPaused){
                return;
            }
            showVideoSnapshotUI(false);
            storeImage(jpegData, mLocation);
        }
    }

    private void storeImage(final byte[] data, Location loc) {
        long dateTaken = System.currentTimeMillis();
        String title = CameraUtil.createJpegName(dateTaken);
        ExifInterface exif = Exif.getExif(data);
        int orientation = Exif.getOrientation(exif);

        String flashSetting = mActivity.getSettingsManager()
            .getString(mAppController.getCameraScope(), Keys.KEY_VIDEOCAMERA_FLASH_MODE);
        Boolean gridLinesOn = Keys.areGridLinesOn(mActivity.getSettingsManager());
        UsageStatistics.instance().photoCaptureDoneEvent(
                eventprotos.NavigationChange.Mode.VIDEO_STILL, title + ".jpeg", exif,
                isCameraFrontFacing(), false, currentZoomValue(), flashSetting, gridLinesOn,
                null, null, null);

        getServices().getMediaSaver().addImage(
                data, title, dateTaken, loc, orientation,
                exif, mOnPhotoSavedListener, mContentResolver);
    }

    private String convertOutputFormatToMimeType(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            return "video/mp4";
        }
        return "video/3gpp";
    }

    private String convertOutputFormatToFileExt(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            return ".mp4";
        }
        return ".3gp";
    }

    private void closeVideoFileDescriptor() {
        if (mVideoFileDescriptor != null) {
            try {
                mVideoFileDescriptor.close();
            } catch (IOException e) {
                Log.e(TAG, "Fail to close fd", e);
            }
            mVideoFileDescriptor = null;
        }
    }

    @Override
    public void onPreviewUIReady() {
        startPreview();
    }

    @Override
    public void onPreviewUIDestroyed() {
        stopPreview();
    }

    @Override
    public void startPreCaptureAnimation() {
        mAppController.startPreCaptureAnimation();
    }

    private void requestCamera(int id) {
        mActivity.getCameraProvider().requestCamera(id);
    }

    @Override
    public void onMemoryStateChanged(int state) {
        mAppController.setShutterEnabled(state == MemoryManager.STATE_OK);
    }

    @Override
    public void onLowMemory() {
        // Not much we can do in the video module.
    }

    /***********************FocusOverlayManager Listener****************************/
    @Override
    public void autoFocus() {
        if (mCameraDevice != null) {
            Log.v(TAG,"auto focus , is state recording ? "+isCameraStateRecording());
            mCameraDevice.autoFocus(mHandler, mAutoFocusCallback);
        }
    }

    @Override
    public boolean cancelAutoFocus() {
        clearFocus();
        return true;
    }



    private void clearFocus(){
        if (mCameraDevice != null) {
            mCameraDevice.cancelAutoFocus();
            setFocusParameters();
        }
    }

    @Override
    public boolean capture() {
        return false;
    }

    @Override
    public void startFaceDetection() {

    }

    @Override
    public void stopFaceDetection() {

    }

    @Override
    public void setFocusParameters() {
        if (mCameraDevice != null) {
            updateFocusParameters();
            mCameraDevice.applySettings(mCameraSettings);
        }
    }

    @Override
    public void doVideoCapture(){
        if (mPaused || mCameraDevice == null) {
            return;
        }
        if (mMediaRecorderRecording) {
            if (!mSnapshotInProgress) {
                takeASnapshot();
            }
            return;
        }
    }

    private AlertDialog quitDialog;
    private void showQuitDialog(int titleId, int msgId, final Runnable runnable) {
        mAppController.getCameraAppUI().setViewFinderLayoutVisibile(true);
        // If quit dialog is showing already, ignore the new one.
        if (quitDialog != null && quitDialog.isShowing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setCancelable(false);
        builder.setTitle(titleId);
        builder.setMessage(msgId);
        builder.setNegativeButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (runnable != null) runnable.run();
            }
        });
        quitDialog = builder.show();
    }

    private Runnable saveAndQuit = new Runnable() {
        @Override
        public void run() {
            onStopVideoRecording();
            mActivity.finish();
        }
    };

    public boolean updateModeSwitchUIinModule() {
        return !isVideoCaptureIntent();
    }

    protected int mCameraState=PREVIEW_STOPPED;

    protected void setCameraState(int state){
        switch(state){
            case RECORDING_PENDING_START:
                mAppController.getLockEventListener().onShutter();
                break;
            case IDLE:
                mAppController.getLockEventListener().onIdle();
                break;
            default:
                break;
        }
        mCameraState=state;
    }

    // For normal video module and slo-mo, only active ‘stop’ when recording duration
    // time exceeds 1s, so send enable message after MediaRecorder.start
    protected boolean shouldHoldRecorderForSecond() {
        return true;
    }

    protected boolean isSendMsgEnableShutterButton() {
        return true;
    }

    /*MODIFIED-BEGIN by peixin, 2016-04-06,BUG-1913360*/
    @Override
    public boolean isZslOn(){
        if(ExtBuild.device() == ExtBuild.MTK_MT6755){
            return false;
        }
        return true;

    }
    /*MODIFIED-END by peixin,BUG-1913360*/

    /* MODIFIED-BEGIN by wenhua.tu, 2016-08-11,BUG-2710178*/
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

    @Override
    public void onFlashClicked() {
        mAppController.getCameraAppUI().getTopMenus().initializeButtonGroupWithAnimationDirection(TopMenus.BUTTON_TORCH, true);
    }

    @Override
    public void onFilterClicked() {
        Log.d(TAG, "onFilterClicked");
    }

    @Override
    public void onVideoPauseButtonClicked() {
        Log.d(TAG,"onVideoPauseButtonClicked");
    }

    private static final int LEFT_SHIFT_NUMBER = 3;
    private static final int MINUTE_TO_MINI_SECONEDS = 1000;
    /**
     * Show left times can be recorded.
     * The bytes of one millisecond recording
     * @param maxFileSize
     */
    private void showRemainTime(long maxFileSize) {
        Log.d(TAG, "mTwoMinBytes : " + mTwoMinBytes + " maxFileSize : " + maxFileSize);
        if (mTwoMinBytes > 0 && mBytePerMs > 0 && maxFileSize <= mTwoMinBytes && maxFileSize > 0) {
            long leftTime = maxFileSize / mBytePerMs;
            String mRemainingText = (leftTime < 0) ? stringForTime(0) : stringForTime(leftTime);
            Log.i(TAG, "[showRemainTime], leftTime:"
                    + leftTime + ", mRemainingText:" + mRemainingText);
            mUI.setTimeLeftUI(true, mRemainingText);
        }
    }

    private static String stringForTime(final long millis) {
        final int totalSeconds = (int) millis / 1000;
        final int seconds = totalSeconds % 60;
        final int minutes = (totalSeconds / 60) % 60;
        final int hours = totalSeconds / 3600;
        if (hours > 0) {
            return String.format(Locale.ENGLISH, "%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds);
        }
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-26,BUG-3212745*/
    @Override
    public void onZoomBarVisibilityChanged(boolean visible) {
        super.onZoomBarVisibilityChanged(visible);
        if (isCameraStateRecording() || isVideoCaptureIntent()) {
            return;
        }
        mAppController.getCameraAppUI().setModeSwitchUIVisibility(!visible);
    }
    /* MODIFIED-END by jianying.zhang,BUG-3212745*/

    /* MODIFIED-BEGIN by guodong.zhang, 2016-11-01,BUG-3272008*/
    protected void resetPauseButton() {
    }
    /* MODIFIED-END by guodong.zhang,BUG-3272008*/
}
