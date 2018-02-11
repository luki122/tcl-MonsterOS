/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.media.AudioManager; // MODIFIED by jianying.zhang, 2016-11-15,BUG-3470155
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.MediaMetadataRetriever; // MODIFIED by xuan.zhou, 2016-10-24,BUG-3201872
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.KeyEvent; // MODIFIED by xuan.zhou, 2016-10-24,BUG-3201872
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View; // MODIFIED by jianying.zhang, 2016-10-22,BUG-3201399
import android.widget.Toast;

import com.android.Encoder.Encoder;
import com.android.Encoder.FilterVideoEncoder;
import com.android.camera.app.AppController;
/* MODIFIED-BEGIN by feifei.xu, 2016-11-08,BUG-3374880*/
import com.android.camera.app.CameraAppUI;
import com.android.camera.app.MediaSaver;
import com.android.camera.app.MemoryManager; // MODIFIED by jianying.zhang, 2016-11-04,BUG-3258603
import com.android.camera.debug.Log;
import com.android.camera.exif.ExifInterface;
import com.android.camera.module.ModuleController;
/* MODIFIED-END by feifei.xu,BUG-3374880*/
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.test.TestUtils;
import com.android.camera.ui.BottomBar;
import com.android.camera.util.CameraUtil;
/* MODIFIED-BEGIN by jianying.zhang, 2016-10-22,BUG-3201399*/
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
/* MODIFIED-END by jianying.zhang,BUG-3201399*/
import com.android.camera.util.SnackbarToast;
import com.android.camera.util.UsageStatistics;
import com.android.camera.widget.TopMenus;
import com.android.ex.camera2.portability.CameraAgent;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.ex.camera2.portability.CameraDeviceInfo;
import com.android.ex.camera2.portability.CameraSettings;
import com.android.ex.camera2.portability.Size;
import com.android.gl_component.NormalGLComposer; // MODIFIED by jianying.zhang, 2016-10-28,BUG-3137073
import com.google.common.logging.eventprotos;
import com.tct.camera.R;

import java.io.File; // MODIFIED by xuan.zhou, 2016-10-24,BUG-3201872
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by sichao.hu on 8/26/16.
 */
/* MODIFIED-BEGIN by jianying.zhang, 2016-11-04,BUG-3258603*/
public class VideoFilterModule extends FilterModule implements FilterVideoUI.ButtonClickListener,
        MemoryManager.MemoryListener{
        /* MODIFIED-END by jianying.zhang,BUG-3258603*/
    private static final String PHOTO_MODULE_STRING_ID = "VideoFilterModule";

    private static final Log.Tag TAG = new Log.Tag(PHOTO_MODULE_STRING_ID);

    protected static final int MSG_ENABLE_SHUTTER_BUTTON = 1;
    protected static final int MSG_UPDATE_RECORD_TIME = 2;
    protected static final int MSG_UPDATE_RECORDINGUI = 3;
    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-04,BUG-3258603*/
    private static final int PREVIEW_STOPPED = 0;
    private static final int IDLE = PREVIEW_STOPPED + 1;
    private static final int RECORDING_PENDING_START = IDLE + 1;
    private static final int RECORDING = RECORDING_PENDING_START + 1;
    private static final int RECORDING_PENDING_STOP = RECORDING + 1;
    /* MODIFIED-END by jianying.zhang,BUG-3258603*/
    public static final int SWITCHING_CAMERA = RECORDING_PENDING_STOP +1; // MODIFIED by guodong.zhang, 2016-11-14,BUG-3330877
    private Encoder mEncoder;
    private Surface mEncoderSurface;
    private String mCurrentVideoFileName;
    private ContentValues mCurrentVideoValues;
    private FilterVideoUI mFilterVideoUI;

    private int mShutterIconId;

    /* MODIFIED-BEGIN by guodong.zhang, 2016-11-08,BUG-3358744*/
    private long mRecordingStartTime;
    private long mRecordingPausedTime;
    private long mSkippedTime;
    /* MODIFIED-END by guodong.zhang,BUG-3358744*/


    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-28,BUG-3137073*/
    private long mRecordingTime = 0L;
    private boolean mIsRecordingPaused = false;
    /* MODIFIED-END by jianying.zhang,BUG-3137073*/
    private Rect mInputSurfaceSize;
    protected static final long MIN_VIDEO_RECODER_DURATION = 1000L; // 1s
    protected static final long SHUTTER_BUTTON_TIMEOUT = 500L; // 500ms // MODIFIED by jianying.zhang, 2016-11-04,BUG-3258603
    // The video duration limit. 0 means no limit.
    private int mMaxVideoDurationInMs;
    private boolean mRecordingTimeCountsDown = false;
    protected CamcorderProfile mProfile;
    private long mBytePerMs;
    private long mTwoMinBytes;
    private static final int LEFT_SHIFT_NUMBER = 3;
    private static final int MINUTE_TO_MINI_SECONEDS = 1000;
    private int mDesiredPreviewWidth;
    private int mDesiredPreviewHeight;
    private boolean mIsHsrVideo = false;
    private static final int VIDEO_HIGH_FRAME_RATE = 60; //hsr60

    /* MODIFIED-BEGIN by xuan.zhou, 2016-10-24,BUG-3201872*/
    // Play the sound effect in the app layer because no MediaRecorder used here.
    private SoundClips.Player mSoundPlayer;
    /* MODIFIED-END by xuan.zhou,BUG-3201872*/
    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-04,BUG-3258603*/
    private int mVideoFilterState = PREVIEW_STOPPED;
    private Integer mModeSelectionLockToken=null;


    public VideoFilterModule(AppController app) {
        super(app);
        mEncoder = buildEncoder();

    }

    private void setVideoFilterState(int state) { // MODIFIED by jianying.zhang, 2016-11-05,BUG-3358973
        mVideoFilterState = state;
    }
    /* MODIFIED-END by jianying.zhang,BUG-3258603*/

    protected Encoder buildEncoder() {
        Encoder encoder=new FilterVideoEncoder();
        return encoder;
    }

    @Override
    /* MODIFIED-BEGIN by feifei.xu, 2016-11-08,BUG-3374880*/
    public CameraAppUI.BottomBarUISpec getBottomBarSpec() {
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-10,BUG-3412070*/
        CameraAppUI.BottomBarUISpec bottomBarSpec = super.getBottomBarSpec();
        bottomBarSpec.enableFlash = false;
        bottomBarSpec.enableTorchFlash = mActivity.currentBatteryStatusOK();
        /* MODIFIED-END by jianying.zhang,BUG-3412070*/
        return bottomBarSpec;
    }

    @Override
    /* MODIFIED-END by feifei.xu,BUG-3374880*/
    public void init(CameraActivity activity, boolean isSecureCamera, boolean isCaptureIntent) {
        super.init(activity, isSecureCamera, isCaptureIntent);
        setVideoFilterState(PREVIEW_STOPPED); // MODIFIED by jianying.zhang, 2016-11-04,BUG-3258603
        mFilterVideoUI = getFilterVideoUI();
        mFilterVideoUI.setListener(this);
        mShutterIconId = CameraUtil.getCameraShutterIconId(
                mAppController.getCurrentModuleIndex(), mAppController.getAndroidContext());
    }

    @Override
    public void onCameraAvailable(CameraAgent.CameraProxy cameraProxy) {
        super.onCameraAvailable(cameraProxy);
        updateDesiredPreviewSize();
        getFilterVideoUI().setAspectRatio((float) mProfile.videoFrameWidth / mProfile.videoFrameHeight);
    }

    protected FilterVideoUI getFilterVideoUI() { // MODIFIED by jianying.zhang, 2016-11-04,BUG-3258603
        if (mFilterVideoUI == null) {
            mFilterVideoUI = new FilterVideoUI(mActivity, this,  mActivity.getModuleLayoutRoot());
        }
        return mFilterVideoUI;
    }
    @Override
    public void onShutterButtonClick() {
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-04,BUG-3258603*/
        Log.d(TAG, "mVideoFilterState : " + mVideoFilterState + " mIsRecording : " + isRecording());
        if (mVideoFilterState == PREVIEW_STOPPED
            || mVideoFilterState == RECORDING_PENDING_START
            /* MODIFIED-BEGIN by guodong.zhang, 2016-11-14,BUG-3330877*/
            || mVideoFilterState == RECORDING_PENDING_STOP
            || mVideoFilterState == SWITCHING_CAMERA
            || mAppController.getCameraAppUI().isShutterLocked()) {
            /* MODIFIED-END by guodong.zhang,BUG-3330877*/
            return;
        }
        if (mFilterState == FILTER_STATE_IDLE_ENLARGED) {
            if (!isRecording()) {
                startRecording();
            } else {
                stopRecording();
            }
            mAppController.setShutterEnabled(false);
            /* MODIFIED-END by jianying.zhang,BUG-3258603*/
        }
    }

    private boolean isRecording() {
        return mVideoFilterState == RECORDING;
    }

    public void onFilterClicked() {
        if(isRecording()){
            return;
        }
        super.onFilterClicked();
    }

    @Override
    public boolean onBackPressed() {
        if(isRecording()){ // MODIFIED by jianying.zhang, 2016-11-04,BUG-3258603
            stopRecording();
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    /* MODIFIED-BEGIN by xuan.zhou, 2016-10-24,BUG-3201872*/
    public void resume() {
        super.resume();

        if (mSoundPlayer == null) {
            mSoundPlayer = mActivity.getSoundClipPlayer();
        }
    }

    @Override
    /* MODIFIED-END by xuan.zhou,BUG-3201872*/
    public void pause() {
        if(isRecording()) { // MODIFIED by jianying.zhang, 2016-11-04,BUG-3258603
            stopRecording();
        }
        getFilterVideoUI().onPause();
        super.pause();
    }

    /* MODIFIED-BEGIN by xuan.zhou, 2016-10-24,BUG-3201872*/
    // Copy from NormalVideoModule.java and ignore the settings about boom key(for TiZR and VDF).
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Do not handle any key if the activity is paused.
        if (mPaused) {
            return true;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case CameraUtil.BOOM_KEY:
                if (event.getRepeatCount() == 0
                        && !mActivity.getCameraAppUI().isInIntentReview()
                        && mAppController.isShutterEnabled()) {
                    onShutterButtonClick();
                }
                return true;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (mPaused) {
            return true;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case CameraUtil.BOOM_KEY:
                return true;
        }
        return false;
    }

    private void playRecordSound(int action) {
        if (!Keys.isShutterSoundOn(mAppController.getSettingsManager())) {
            return;
        }
        if (mSoundPlayer == null) {
            mSoundPlayer = mActivity.getSoundClipPlayer();
        }
        mSoundPlayer.play(action);
    }
    /* MODIFIED-END by xuan.zhou,BUG-3201872*/

    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-04,BUG-3258603*/
    private void startRecording() {
        setVideoFilterState(RECORDING_PENDING_START); // MODIFIED by jianying.zhang, 2016-11-04,BUG-3258603
        /* MODIFIED-END by jianying.zhang,BUG-3258603*/
        CamcorderProfile profile=CamcorderProfile.get(mCameraId,CamcorderProfile.QUALITY_HIGH);
        int bitRate=profile.videoBitRate;
        Size size=mCameraSettings.getCurrentPreviewSize();
        int width=size.width();
        int height=size.height();
        Log.w(TAG, String.format("preview size is %dx%d%d%d", width, height,mDesiredPreviewWidth,mDesiredPreviewHeight));
        mCurrentVideoFileName=generateVideoFilename(MediaRecorder.OutputFormat.MPEG_4);
        mEncoder.setProgressListener(getVideoProgressListener());
        playRecordSound(SoundClips.START_VIDEO_RECORDING); // MODIFIED by jianying.zhang, 2016-10-28,BUG-3137073
        mRecordingStartTime = SystemClock.uptimeMillis(); // MODIFIED by guodong.zhang, 2016-11-08,BUG-3358744
        mEncoder.prepare(mDesiredPreviewHeight, mDesiredPreviewWidth, bitRate,mCurrentVideoFileName, new Encoder.OnEncodeStateCallback() {
            @Override
            public void onEncoderInputSurfaceReady(Surface surface,int width,int height) {
                mInputSurfaceSize=new Rect(0,0,width,height);
                updateVideoDimension(width, height);
                mEncoderSurface = surface;
                mGLComponent.attachRecordSurface(mEncoderSurface);
                /* MODIFIED-BEGIN by jianying.zhang, 2016-11-04,BUG-3258603*/
                mEncoder.start(new Encoder.OnEncoderStartCallback() {
                    @Override
                    public void onSuccess() {
                        mRecordingHandler.sendEmptyMessage(MSG_UPDATE_RECORDINGUI);
                        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-05,BUG-3358973*/
                        setVideoFilterState(RECORDING);
                    }

                    @Override
                    public void onError() {
                        setVideoFilterState(IDLE);
                        /* MODIFIED-END by jianying.zhang,BUG-3358973*/
                    }
                });
                /* MODIFIED-END by jianying.zhang,BUG-3258603*/
                mRecordingHandler.sendEmptyMessageDelayed(MSG_ENABLE_SHUTTER_BUTTON, MIN_VIDEO_RECODER_DURATION);
            }
        });
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-15,BUG-3470155*/
        pauseAudioPlayback();
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
                    boolean ignoreAudioFocus = CustomUtil.getInstance().getBoolean(
                            CustomFields.DEF_RECORDING_IGNORE_AUDIOFOCUS, false);
                    Log.i(TAG, "AudioFocus change, " + focusChange + ", ignore " + ignoreAudioFocus);
                    if (!ignoreAudioFocus) {
                        stopRecording();
                    }
                    break;
            }
        }
    };
    /* MODIFIED-END by jianying.zhang,BUG-3470155*/

    public boolean updateModeSwitchUIinModule() {
        return !isImageCaptureIntent();
    }
    private final Encoder.OnEncoderProgressListener mVideoProgressListener=new Encoder.OnEncoderProgressListener() {
        @Override
        public void onEncodeProgressUpdate(long videoDuration, long fileSize) {
            mRecordingTime = videoDuration; // MODIFIED by jianying.zhang, 2016-10-31,BUG-3137073
        }
    };

    protected Encoder.OnEncoderProgressListener getVideoProgressListener(){
        return mVideoProgressListener;
    }
    private void stopRecording(){
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-04,BUG-3258603*/
        if(isRecording()){
            setVideoFilterState(RECORDING_PENDING_STOP); // MODIFIED by jianying.zhang, 2016-11-04,BUG-3258603
            /* MODIFIED-END by jianying.zhang,BUG-3258603*/
            resetRecordingUI();
            Log.w(TAG, "stop recording");
            releaseAudioFocus(); // MODIFIED by jianying.zhang, 2016-11-15,BUG-3470155
            mEncoder.stop(new Encoder.OnEncoderStopCallback() {
                @Override
                public void onEncoderStopped() {
                    mSkippedTime = 0; // MODIFIED by guodong.zhang, 2016-11-08,BUG-3358744
                    /* MODIFIED-BEGIN by xuan.zhou, 2016-10-24,BUG-3201872*/
                    // Copy from saveVideo() in VideoModule.java and ignore the MediaMetadataRetriever.
                    // The duration should be recalculated after filter video pause realize.
                    long duration = getRecordingTime(); // MODIFIED by jianying.zhang, 2016-10-31,BUG-3137073

                    try {
                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(mCurrentVideoFileName);
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

                    mCurrentVideoValues.put(MediaStore.Video.Media.SIZE,
                            new File(mCurrentVideoFileName).length());
                    mCurrentVideoValues.put(MediaStore.Video.Media.DURATION,
                            duration);

                    Log.w(TAG, "push " + mCurrentVideoFileName + " to media store");
                    getServices().getMediaSaver().addVideo(mCurrentVideoFileName,
                            mCurrentVideoValues, mOnVideoSavedListener, mActivity.getContentResolver());
                    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-04,BUG-3258603*/
                    mRecordingHandler.sendEmptyMessageDelayed(MSG_ENABLE_SHUTTER_BUTTON,
                            SHUTTER_BUTTON_TIMEOUT);
                    setVideoFilterState(IDLE); // MODIFIED by jianying.zhang, 2016-11-05,BUG-3358973
                    /* MODIFIED-END by jianying.zhang,BUG-3258603*/
                }
            });
            mGLComponent.attachRecordSurface(null);
            playRecordSound(SoundClips.STOP_VIDEO_RECORDING);
            /* MODIFIED-END by xuan.zhou,BUG-3201872*/

            /* MODIFIED-BEGIN by jianying.zhang, 2016-10-28,BUG-3137073*/
            mIsRecordingPaused = false;
            /* MODIFIED-END by jianying.zhang,BUG-3137073*/
        }
    }

    private void resetRecordingUI() {
        mAppController.getCameraAppUI().setSwipeEnabled(true);
        mAppController.getCameraAppUI().hideVideoCaptureButton(false);
        setTopModeOptionVisibility(true);
        mAppController.getCameraAppUI().setVideoBottomBarVisible(true);
        getFilterVideoUI().stopRecording();
        final Runnable resetRunnable=new Runnable() {
            @Override
            public void run() {
                if(mModeSelectionLockToken!=null) {
                    mAppController.unlockModuleSelection(mModeSelectionLockToken);
                }
                if (updateModeSwitchUIinModule()) {
                    mAppController.getCameraAppUI().setModeSwitchUIVisibility(true);
                }
            }
        };

        BottomBar.BottomBarSizeListener animateDoneListener=new BottomBar.BottomBarSizeListener() {
            @Override
            public void onFullSizeReached() {
                resetRunnable.run();
            }
        };
        if (mPaused) {
            resetRunnable.run();
        }
        mAppController.getCameraAppUI().animateBottomBarToFullSize(mShutterIconId, mPaused?null:animateDoneListener);
    }
    @Override
    protected Rect getRecorderSurfaceArea() {
        return mInputSurfaceSize;
    }

    protected String generateVideoFilename(int outputFileFormat) {
        long dateTaken = System.currentTimeMillis();
        String title = createName(dateTaken);
        // Used when emailing.
        String filename = title + convertOutputFormatToFileExt(outputFileFormat);
        String mime = convertOutputFormatToMimeType(outputFileFormat);
        String path = Storage.DIRECTORY + '/' + filename;
        String tmpPath = path ;
        mCurrentVideoValues = new ContentValues(11); // MODIFIED by xuan.zhou, 2016-10-24,BUG-3201872
        mCurrentVideoValues.put(MediaStore.Video.Media.TITLE, title);
        mCurrentVideoValues.put(MediaStore.Video.Media.DISPLAY_NAME, filename);
        mCurrentVideoValues.put(MediaStore.Video.Media.DATE_TAKEN, dateTaken);
        mCurrentVideoValues.put(MediaStore.MediaColumns.DATE_MODIFIED, dateTaken / 1000);
        mCurrentVideoValues.put(MediaStore.Video.Media.MIME_TYPE, mime);
        mCurrentVideoValues.put(MediaStore.Video.Media.DATA, path);
        mCurrentVideoValues.put(MediaStore.Video.Media.WIDTH, mCameraSettings.getCurrentPreviewSize().height());
        mCurrentVideoValues.put(MediaStore.Video.Media.HEIGHT, mCameraSettings.getCurrentPreviewSize().width());
        mCurrentVideoValues.put(MediaStore.Video.Media.RESOLUTION,
                Integer.toString(mCameraSettings.getCurrentPreviewSize().width()) + "x" +
                        Integer.toString(mCameraSettings.getCurrentPreviewSize().height()));
//        Location loc = mLocationManager.getCurrentLocation();
//        if (loc != null) {
//            mCurrentVideoValues.put(MediaStore.Video.Media.LATITUDE, loc.getLatitude());
//            mCurrentVideoValues.put(MediaStore.Video.Media.LONGITUDE, loc.getLongitude());
//        }

        Log.v(TAG, "New video filename: " + tmpPath);
        return tmpPath;
    }

    protected void updateVideoDimension(int width, int height){
        mCurrentVideoValues.put(MediaStore.Video.Media.WIDTH, width);
        mCurrentVideoValues.put(MediaStore.Video.Media.HEIGHT, height);
        mCurrentVideoValues.put(MediaStore.Video.Media.RESOLUTION,
                Integer.toString(width) + "x" +
                        Integer.toString(height));
    }

    private String createName(long dateTaken) {
        Date date = new Date(dateTaken);
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                mActivity.getString(R.string.video_file_name_format));

        return dateFormat.format(date);
    }

    private String convertOutputFormatToFileExt(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            return ".mp4";
        }
        return ".3gp";
    }

    private String convertOutputFormatToMimeType(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
            return "video/mp4";
        }
        return "video/3gpp";
    }

    @Override
    protected boolean aspectRatioVisible() {
        return false;
    }

    @Override
    protected void updateParametersPictureSize() {
        initMatrix();
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
        if (mProfile == null) {
            readPreferences();
        }
        /* MODIFIED-END by jianying.zhang,BUG-3255060*/
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


        if (mIsHsrVideo) {
            mCameraSettings.setHsr("" + VIDEO_HIGH_FRAME_RATE);
        } else {
            mCameraSettings.setHsr("off");
        }
        updateParametersAntibanding();
        // update flash parameters
        updateParametersFlashMode();
        if (mCameraDevice != null) {
            mCameraDevice.applySettings(mCameraSettings);
            // Nexus 5 through KitKat 4.4.2 requires a second call to
            // .setParameters() for frame rate settings to take effect.
            mCameraDevice.applySettings(mCameraSettings);
        }
    }

    @Override
    protected boolean updateParametersFlashMode() {
        if (mCameraSettings.getCurrentFlashMode() == null) {
            return false;
        }

        SettingsManager settingsManager = mActivity.getSettingsManager();

        CameraCapabilities.Stringifier stringifier = mCameraCapabilities.getStringifier();
        CameraCapabilities.FlashMode flashMode;
        flashMode = stringifier
                .flashModeFromString(settingsManager.getString(mAppController.getCameraScope(),
                        Keys.KEY_VIDEOCAMERA_FLASH_MODE));

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
        return true;
    }

    protected boolean isVideoStabilizationEnabled() {
        return Keys.isVideoStabilizationEnabled(mAppController.getSettingsManager());
    }

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
                mCameraSettings, mCameraCapabilities, mProfile, getFilterVideoUI().getPreviewScreenSize());
        mDesiredPreviewWidth = desiredPreviewSize.x;
        mDesiredPreviewHeight = desiredPreviewSize.y;
        getFilterVideoUI().setPreviewSize(mDesiredPreviewWidth, mDesiredPreviewHeight);
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

    private final MediaSaver.OnMediaSavedListener mOnVideoSavedListener =
            new MediaSaver.OnMediaSavedListener() {
                @Override
                public void onMediaSaved(Uri uri) {
                    if (uri != null) {
                        mActivity.notifyNewMedia(uri);
                    }
                }
            };

    @Override
    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-22,BUG-3201399*/
    public void onSingleTapUp(View view, int x, int y) {
        if(mFilterState == FILTER_STATE_IDLE_ENLARGED
                && isCameraFrontFacing() && CustomUtil.getInstance()
                .getBoolean(CustomFields.DEF_CAMCORDER_ENABLE_FRONT_TOUCH_CAPTURE, true)) {
            doVideoCapture();
            return;
        }
        super.onSingleTapUp(view, x, y);
    }

    @Override
    protected boolean needEnableExposureAdjustment() {
        return false;
    }

    @Override
    public boolean isShowPose() {
        return false;
    }
    /* MODIFIED-END by jianying.zhang,BUG-3201399*/

    @Override
    public void doVideoCapture() {
        if (mPaused || mCameraDevice == null) {
            return;
        }
        if (isRecording() && !mSnapshotInProgress) { // MODIFIED by jianying.zhang, 2016-11-04,BUG-3258603
            takeASnapshot();
        }
    }

    @Override
    public void onVideoPauseButtonClicked() {
        Log.d(TAG,"onVideoPauseButtonClicked");
        /* MODIFIED-BEGIN by jianying.zhang, 2016-10-28,BUG-3137073*/
        if (isRecording()) {
            mIsRecordingPaused = !mIsRecordingPaused;
            pauseRecording(mIsRecordingPaused);
        }
        if (!mIsRecordingPaused) {
            /* MODIFIED-BEGIN by guodong.zhang, 2016-11-08,BUG-3358744*/
            mSkippedTime += System.currentTimeMillis() - mRecordingPausedTime;
            mRecordingHandler.sendEmptyMessage(MSG_UPDATE_RECORD_TIME);
        }
        mFilterVideoUI.showPausedUI(mIsRecordingPaused);
    }

    private void pauseRecording(boolean isPauseRecording) {
        if (mIsRecordingPaused) {
            mRecordingPausedTime = System.currentTimeMillis();
        }
        /* MODIFIED-END by guodong.zhang,BUG-3358744*/
        if (mGLComponent!=null) {
            ((NormalGLComposer)mGLComponent).pauseRecording(isPauseRecording);
            ((FilterVideoEncoder)mEncoder).pauseRecording(isPauseRecording);
        }
        /* MODIFIED-END by jianying.zhang,BUG-3137073*/
    }

    private boolean mSnapshotInProgress = false;
    private void takeASnapshot() {
        // Only take snapshots if video snapshot is supported by device
        if(!mCameraCapabilities.supports(CameraCapabilities.Feature.VIDEO_SNAPSHOT)) {
            Log.w(TAG, "Cannot take a video snapshot - not supported by hardware");
            return;
        }
        if (!isImageCaptureIntent()) {
            if (!isRecording() || mPaused || mSnapshotInProgress // MODIFIED by jianying.zhang, 2016-11-04,BUG-3258603
                    || !mAppController.isShutterEnabled() || mCameraDevice == null) {
                return;
            }

            Location loc = mActivity.getLocationManager().getCurrentLocation();
            CameraUtil.setGpsParameters(mCameraSettings, loc);
            mCameraDevice.applySettings(mCameraSettings);

            int orientation = getJpegRotation(mOrientation);
            Log.d(TAG, "Video snapshot orientation is " + orientation);
            mCameraDevice.setJpegOrientation(orientation);

            Log.i(TAG, "Video snapshot start");
            mCameraDevice.takePicture(mHandler,
                    null, null, null, new JpegPictureCallback(loc),getFilterId());
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
            CameraDeviceInfo.Characteristics info = mActivity.getCameraProvider().getCharacteristics(mCameraId);
            mJpegRotation = info.getJpegOrientation(orientation);
        } catch (Exception e) {
            Log.e(TAG, "Error when getJpegOrientation");
        }

        return mJpegRotation;
    }

    void showVideoSnapshotUI(boolean enabled) {
        if (mCameraSettings == null) {
            return;
        }
        if (mCameraCapabilities.supports(CameraCapabilities.Feature.VIDEO_SNAPSHOT) &&
                !isImageCaptureIntent()) {
            if (enabled) {
                getFilterVideoUI().animateFlash();
            }
            mAppController.setShutterEnabled(!enabled);
        }
    }

    private final class JpegPictureCallback implements CameraAgent.CameraPictureCallback {
        Location mLocation;

        public JpegPictureCallback(Location loc) {
            mLocation = loc;
        }

        @Override
        public void onPictureTaken(byte [] jpegData, CameraAgent.CameraProxy camera) {
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

    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-04,BUG-3258603*/
    @Override
    protected void onPreviewStarted() {
        super.onPreviewStarted();
        mAppController.setShutterEnabled(true);
        setVideoFilterState(IDLE); // MODIFIED by jianying.zhang, 2016-11-05,BUG-3358973
    }
    /* MODIFIED-END by jianying.zhang,BUG-3258603*/

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

    /**
     * Returns current Zoom value, with 1.0 as the value for no zoom.
     */
    private float currentZoomValue() {
        return mCameraSettings.getCurrentZoomRatio();
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

    private void setTopModeOptionVisibility(boolean videoStop) {
        if (videoStop) {
            mAppController.getCameraAppUI().getTopMenus().setTopModeOptionVisibility(true);
        } else {
            mAppController.getCameraAppUI().getTopMenus().setTopModeOptionVisibility(false);
        }
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
        mIsHsrVideo = false;
        if (String.valueOf(videoQuality).equals(SettingsUtil.QUALITY_1080P_60FPS)) {
            mIsHsrVideo = true;
            videoQuality = SettingsUtil.VIDEO_QUALITY_VALUE_TABLE.get(String.valueOf(videoQuality));
        }
        return videoQuality;
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
    @Override
    protected void readPreferences() {
    /* MODIFIED-END by jianying.zhang,BUG-3255060*/
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
    }

    protected boolean isVideoRecordingPaused() {
        /* MODIFIED-BEGIN by jianying.zhang, 2016-10-28,BUG-3137073*/
        return mIsRecordingPaused;
    }

    protected long getRecordingTime() {
        return mRecordingTime / 1000;
        /* MODIFIED-END by jianying.zhang,BUG-3137073*/
    }

    private void updateRecordingTime() {
        if (isVideoRecordingPaused()) {
            getFilterVideoUI().setDotbBlink(isVideoRecordingPaused());
            return;
        }
        if (!isRecording()) { // MODIFIED by jianying.zhang, 2016-11-04,BUG-3258603
            return;
        }
//        long now = SystemClock.uptimeMillis();
//        long delta = now - mRecordingStartTime;
        long delta = getRecordingTime();; // MODIFIED by jianying.zhang, 2016-10-31,BUG-3137073

        /* MODIFIED-BEGIN by guodong.zhang, 2016-11-08,BUG-3358744*/
        long delta_New = SystemClock.uptimeMillis() - mRecordingStartTime - mSkippedTime;

        // Starting a minute before reaching the max duration
        // limit, we'll countdown the remaining time instead.
        boolean countdownRemainingTime = (mMaxVideoDurationInMs != 0
                && delta >= mMaxVideoDurationInMs - 60000);

        long deltaAdjusted = delta_New;
        /* MODIFIED-END by guodong.zhang,BUG-3358744*/
        if (countdownRemainingTime) {
            deltaAdjusted = Math.max(0, mMaxVideoDurationInMs - deltaAdjusted) + 999;
        }
        String text;

        long targetNextUpdateDelay;

        text = millisecondToTimeString(deltaAdjusted, false);
        targetNextUpdateDelay = 1000;

        getFilterVideoUI().setRecordingTime(text);

        getFilterVideoUI().setDotbBlink(isVideoRecordingPaused());

        if (mRecordingTimeCountsDown != countdownRemainingTime) {
            // Avoid setting the color on every update, do it only
            // when it needs changing.
            mRecordingTimeCountsDown = countdownRemainingTime;

            int color = mActivity.getResources().getColor(R.color.recording_time_remaining_text);

            getFilterVideoUI().setRecordingTimeTextColor(color);
        }

        long actualNextUpdateDelay = targetNextUpdateDelay - (delta_New % targetNextUpdateDelay); // MODIFIED by guodong.zhang, 2016-11-08,BUG-3358744
        mRecordingHandler.sendEmptyMessageDelayed(MSG_UPDATE_RECORD_TIME, actualNextUpdateDelay);
        onVideoRecordingStarted();
    }
    protected void onVideoRecordingStarted(){
        getFilterVideoUI().unlockCaptureView();
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

    private Handler mRecordingHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            switch (what){
                case MSG_ENABLE_SHUTTER_BUTTON:
                    mAppController.setShutterEnabled(true);
                    break;

                case MSG_UPDATE_RECORD_TIME: {
                    updateRecordingTime();
                    break;
                }
                case MSG_UPDATE_RECORDINGUI: {
                    if(mModeSelectionLockToken==null) {
                        mModeSelectionLockToken = mAppController.lockModuleSelection();
                    }

                    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-04,BUG-3258603*/
                    getFilterVideoUI().cancelAnimations();
                    setTopModeOptionVisibility(false);
                    /* MODIFIED-END by jianying.zhang,BUG-3258603*/

                    mAppController.getCameraAppUI().setSwipeEnabled(false);
//                    mActivity.lockOrientation();
                    setFocusParameters();
//                    getFilterVideoUI().lockRecordingOrientation();
                    if (updateModeSwitchUIinModule()) {
                        mAppController.getCameraAppUI().setModeSwitchUIVisibility(false);
                        mAppController.getCameraAppUI().showVideoCaptureButton(true);
                        getFilterVideoUI().showPausedUI(false); // MODIFIED by guodong.zhang, 2016-11-01,BUG-3272008
                        mAppController.getCameraAppUI().setVideoBottomBarVisible(false);
                        getFilterVideoUI().showRecordingUI(true);
                    }
                    mAppController.getCameraAppUI()
                            .animateBottomBarToVideoStop(R.drawable.ic_video_recording);
                    updateRecordingTime();
                    mActivity.enableKeepScreenOn(true);
                    // Checking when recording start successful.
                    mActivity.startInnerStorageChecking(new CameraActivity.OnInnerStorageLowListener() {
                        @Override
                        public void onInnerStorageLow(long bytes) {
                            mActivity.stopInnerStorageChecking();
                            if (isRecording()) { // MODIFIED by jianying.zhang, 2016-11-04,BUG-3258603
                                /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
                                SnackbarToast.getSnackbarToast().showToast(mActivity,
                                        mActivity.getString(R.string.storage_low_video_critical_toast_message)
                                        ,SnackbarToast.LENGTH_LONG,SnackbarToast.DEFAULT_Y_OFFSET);
                                        /* MODIFIED-END by bin-liu3,BUG-3253898*/
                                stopRecording();
                            }
                        }

                        @Override
                        public void onInnerStorage(long bytes) {
                            showRemainTime(bytes - Storage.LOW_STORAGE_THRESHOLD_BYTES);
                        }
                    });
                    mActivity.startBatteryInfoChecking(new CameraActivity.OnBatteryLowListener() {
                        @Override
                        public void onBatteryLow(int level) {
                            mActivity.stopBatteryInfoChecking();
                            if (isRecording()) { // MODIFIED by jianying.zhang, 2016-11-04,BUG-3258603
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
                                        stopRecording();
                                    }
                                });
                                builder.setPositiveButton(R.string.battery_info_video_low_continue, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                builder.create().show();
                            }
                        }
                    });
                }
            }
        }
    };

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
            getFilterVideoUI().setTimeLeftUI(true, mRemainingText);
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

    @Override
    public void onFlashClicked() {
        mAppController.getCameraAppUI().getTopMenus().initializeButtonGroupWithAnimationDirection(TopMenus.BUTTON_TORCH, true);
    }

    @Override
    public boolean isFacebeautyEnabled() {
        return false;
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-26,BUG-3212745*/
    @Override
    public void onZoomBarVisibilityChanged(boolean visible) {
        if (isRecording()) { // MODIFIED by jianying.zhang, 2016-11-04,BUG-3258603
            return;
        }
        mAppController.getCameraAppUI().setModeSwitchUIVisibility(!visible);
    }
    /* MODIFIED-END by jianying.zhang,BUG-3212745*/

    @Override
    public boolean isMeteringEnabled() {
        return false;
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-01,BUG-3255237*/
    @Override
    protected boolean needFaceDetection() {
        return false;
    }
    /* MODIFIED-END by jianying.zhang,BUG-3255237*/

    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-03,BUG-3314679*/
    @Override
    protected boolean isSuperResolutionEnabled() {
        return false;
    }
    /* MODIFIED-END by jianying.zhang,BUG-3314679*/

    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-04,BUG-3258603*/
    @Override
    public void onMemoryStateChanged(int state) {
        super.onMemoryStateChanged(state);
        mAppController.setShutterEnabled(state == MemoryManager.STATE_OK);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void stopPreview() {
        super.stopPreview();
        setVideoFilterState(PREVIEW_STOPPED); // MODIFIED by jianying.zhang, 2016-11-05,BUG-3358973
    }
    /* MODIFIED-END by jianying.zhang,BUG-3258603*/

    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
    @Override
    protected void saveSelectedFilterIndex() {
        Log.d(TAG,"stopPreview mChosenFilterIndex : " + mChosenFilterIndex);
        mActivity.getSettingsManager().setChosenFilterIndex(mActivity.getCameraScope(),
                Keys.KEY_VIDEO_FILTER_MODULE_SELECTED, mChosenFilterIndex);
    }

    @Override
    protected int getSelectedFilterIndex(String scope) {
        return  mActivity.getSettingsManager().getChosenFilterIndex(scope,
                Keys.KEY_VIDEO_FILTER_MODULE_SELECTED, INDEX_NONE_FILTER);
    }

    @Override
    protected void switchCamera() {
        /* MODIFIED-BEGIN by guodong.zhang, 2016-11-14,BUG-3330877*/
        if (mVideoFilterState == RECORDING_PENDING_START
                || mVideoFilterState == RECORDING
                || mVideoFilterState == RECORDING_PENDING_STOP){
            return;
        }
        setVideoFilterState(SWITCHING_CAMERA);
        /* MODIFIED-END by guodong.zhang,BUG-3330877*/
        String scope = mAppController.getCameraScopeByID(Integer.toString(mPendingSwitchCameraId));
        mChosenFilterIndex = getSelectedFilterIndex(scope);
        Log.d(TAG,"switchCamera mChosenFilterIndex : " + mChosenFilterIndex);
        int videoFilterModeId = mAppController.getAndroidContext().getResources()
                .getInteger(R.integer.camera_mode_videofilter);
        int videoModeId = mAppController.getAndroidContext().getResources()
                .getInteger(R.integer.camera_mode_video);
        validateFilterSelected(videoFilterModeId, videoModeId);
    }
    /* MODIFIED-END by jianying.zhang,BUG-3255060*/
}
