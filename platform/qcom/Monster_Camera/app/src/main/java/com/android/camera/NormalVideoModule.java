package com.android.camera;

/* MODIFIED-BEGIN by jianying.zhang, 2016-10-15,BUG-2715761*/
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
/* MODIFIED-END by jianying.zhang,BUG-2715761*/
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;

import com.android.camera.app.AppController;
import com.android.camera.app.CameraAppUI; //MODIFIED by peixin, 2016-04-19,BUG-1950652
import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.util.ApiHelper; // MODIFIED by jianying.zhang, 2016-10-15,BUG-2715761
import com.android.camera.util.CameraUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.ex.camera2.portability.CameraSettings;
import com.android.ex.camera2.portability.Size;
import com.android.external.plantform.ExtBuild; //MODIFIED by peixin, 2016-04-19,BUG-1950652
import com.tct.camera.R;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by sichao.hu on 12/23/15.
 */
public class NormalVideoModule extends VideoModule {

    public static final String NORMAL_VIDEO_MODULE_STRING_ID="NormalVideoModule";

    private static final Log.Tag TAG = new Log.Tag(NORMAL_VIDEO_MODULE_STRING_ID);

    private static final int VIDEO_HIGH_FRAME_RATE = 60; //hsr60
    private boolean mIsHsrVideo;
    /**
     * Construct a new video module.
     *
     * @param app
     */
    public NormalVideoModule(AppController app) {
        super(app);
    }

    @Override
    public int getModuleId() {
        return mAppController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_video);
    }

    @Override
    protected void disableShutterDuringResume() {
//        if(mCameraState==PREVIEW_STOPPED) {
//           mAppController.lockPool(null);
//        }
        if(mCameraState==PREVIEW_STOPPED){
            mAppController.getLockEventListener().forceBlocking();
        }
    }

    @Override
    public String getModuleStringIdentifier() {
        return NORMAL_VIDEO_MODULE_STRING_ID;
    }

    @Override
    protected boolean isFacebeautyEnabled(){
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-22, BUG-1849045 */
        return CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_PHOTO_FACEBEAUTY_SUPPORT, true) && CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_VIDEO_FACEBEAUTY_SUPPORT, true);
                /* MODIFIED-END by yuanxing.tan,BUG-1849045 */
    }

    @Override
    public void onSingleTapUp(View view, int x, int y) {
        if (dismissButtonGroupBar(true)) {
            return;
        }
        if (isCameraFrontFacing() && CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMCORDER_ENABLE_FRONT_TOUCH_CAPTURE, true)) {
            doVideoCapture();
            return;
        }
        super.onSingleTapUp(view, x, y);
    }


    @Override
    protected void showBoomKeyTip() {
        /* MODIFIED-BEGIN by nie.lei, 2016-03-21, BUG-1804101 */
        int iBoomEffectSetting = Settings.System.getInt(mContentResolver, CameraUtil.BOOM_EFFECT_SETTINGS, CameraUtil.BOOM_EFFECT_OFF);
        if(iBoomEffectSetting == CameraUtil.BOOM_EFFECT_ON){
            getVideoUI().showBoomKeyTipUI();
        }
        /* MODIFIED-END by nie.lei,BUG-1804101 */
    }

    @Override
    protected void hideBoomKeyTip() {
        getVideoUI().hideBoomKeyTipUI();
    }

    private void onBoomPressed(){
        if(!CustomUtil.getInstance().getBoolean(CustomFields.DEF_VIDEO_BOOMKEY_TIZR_SHARE_ON,false)){
            return;
        }

        try {
            String packageName = CameraUtil.TIZR_PACKAGE_NAME;
            PackageManager manager = mActivity.getPackageManager();
            Intent intent = manager.getLaunchIntentForPackage(packageName);
            if (intent == null) {
                Log.e(TAG, "No " + packageName + " installed.");
                mVideoBoomKeyFlags = false;
            }else {
                mVideoBoomKeyFlags = true;
                AudioManager mAudioManager =(AudioManager)mActivity.getSystemService(Context.AUDIO_SERVICE);
                if(mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT){
                    Vibrator vibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        vibrator.vibrate(CameraUtil.TIZR_VIBRATOR_DURATION);
                    }
                }
            }

            if(!mPaused && !mIsVideoCaptureIntent && mMediaRecorderRecording){
                Log.i(TAG, "onBoomPressed stop video recording");
                onStopVideoRecording();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void startVideoNotityHelpTip(){
        //notify help tip when recording a video
        HelpTipsManager helpTipsManager = mAppController.getHelpTipsManager();
        if(helpTipsManager != null){
            helpTipsManager.startRecordVideoResponse();
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
            case CameraUtil.BOOM_KEY:
                Boolean bVdfCustomizeBoomKey = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_VDF_BOOMKEY_CUSTOMIZE, false);
                int iBoomEffectSetting = Settings.System.getInt(mContentResolver, CameraUtil.BOOM_EFFECT_SETTINGS, CameraUtil.BOOM_EFFECT_OFF);
                Log.i(TAG, "onKeyDown mMediaRecorderRecording = " + mMediaRecorderRecording
                        + ",bVdfCustomizeBoomKey = " + bVdfCustomizeBoomKey
                        + ",iBoomEffectSetting = " + iBoomEffectSetting);
                if(!bVdfCustomizeBoomKey){
                    //idol4 & idol4s
                    if(!mActivity.isSecureCamera() && iBoomEffectSetting == CameraUtil.BOOM_EFFECT_ON
                            && mMediaRecorderRecording){
                        onBoomPressed();
                    }else {
                        Log.e(TAG, "onKeyDown BOOM_KEY is handled as shuttonbutton clicking");
                        if (event.getRepeatCount() == 0 &&
                                !mActivity.getCameraAppUI().isInIntentReview()
                                && mAppController.isShutterEnabled()) {
                            onShutterButtonClick();
                        }
                    }
                }else {
                    //vdf
                    if(mMediaRecorderRecording){
                        Log.e(TAG, "onKeyDown doVideoCapture when MediaRecorder is Recording");
                        doVideoCapture();
                    }else {
                        if (event.getRepeatCount() == 0 &&
                                !mActivity.getCameraAppUI().isInIntentReview()
                                && mAppController.isShutterEnabled()) {
                            onShutterButtonClick();
                        }
                    }
                }
                return true;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case CameraUtil.BOOM_KEY:
                return true;
        }
        return false;
    }

    @Override
    protected int getProfileQuality() {
        int quality = super.getProfileQuality();
        mIsHsrVideo = false;
        if (String.valueOf(quality).equals(SettingsUtil.QUALITY_1080P_60FPS)) {
            mIsHsrVideo = true;
            quality = SettingsUtil.VIDEO_QUALITY_VALUE_TABLE.get(String.valueOf(quality));
        }
        return quality;
    }

    @Override
    protected void setHsr(CameraSettings cameraSettings) {
        if (mIsHsrVideo) {
            cameraSettings.setHsr("" + VIDEO_HIGH_FRAME_RATE);
        } else {
            super.setHsr(cameraSettings);
        }
    }

    @Override
    protected boolean isSupported(int width, int height) {
        if (mIsHsrVideo) {
            Size maxHsrSize = null;
            List<String> supportedVideoHighFrameRates = mCameraCapabilities.getSupportedVideoHighFrameRates();
            boolean isSupported = supportedVideoHighFrameRates == null ? false : supportedVideoHighFrameRates.indexOf(String.valueOf(VIDEO_HIGH_FRAME_RATE)) >= 0;
            if (isSupported) {
                int index = supportedVideoHighFrameRates.indexOf(""+VIDEO_HIGH_FRAME_RATE);
                if (index != -1){
                    maxHsrSize = mCameraCapabilities.getSupportedHsrSizes().get(index);
                }
            }

            // is resolution supported
            if (maxHsrSize == null || !(width <= maxHsrSize.width() && height <= maxHsrSize.height())) {
                Log.e(TAG, "Unsupported HSR and video size combinations");
                return false;
            }
            return true;
        } else {
            return super.isSupported(width, height);
        }
    }

    @Override
    protected void mediaRecorderParameterFetching(MediaRecorder recorder) {
        if (mIsHsrVideo) {
            Log.w(TAG, "set mediaRecorder parameters");
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            recorder.setOutputFormat(mProfile.fileFormat);

            int scaledBitrate = mProfile.videoBitRate * VIDEO_HIGH_FRAME_RATE / mProfile.videoFrameRate;
            Log.i(TAG, "Scaled Video bitrate : " + scaledBitrate);
            recorder.setVideoEncodingBitRate(scaledBitrate);

            recorder.setAudioEncodingBitRate(mProfile.audioBitRate);
            recorder.setAudioChannels(mProfile.audioChannels);
            recorder.setAudioSamplingRate(mProfile.audioSampleRate);
            recorder.setVideoEncoder(mProfile.videoCodec);
            recorder.setAudioEncoder(mProfile.audioCodec);
            recorder.setVideoSize(mProfile.videoFrameWidth, mProfile.videoFrameHeight);
            recorder.setMaxDuration(getOverrodeVideoDuration());
            recorder.setCaptureRate(VIDEO_HIGH_FRAME_RATE);
            recorder.setVideoFrameRate(VIDEO_HIGH_FRAME_RATE);
        } else {
            super.mediaRecorderParameterFetching(recorder);
        }
    }

    @Override
    public void onVideoPauseButtonClicked() {
        super.onVideoPauseButtonClicked();
        Log.e(TAG, "onVideoPauseButtonClicked mVideoRecordingPaused : " + mVideoRecordingPaused);
        if (mCameraState != RECORDING) {
            return;
        }
        if (mVideoRecordingPaused) {
            resumeVideoRecording();
        } else {
            pauseVideoRecording();
        }
        getVideoUI().showPausedUI(mVideoRecordingPaused);
//        if (mVideoRecordingPaused) {
//            mAppController.getCameraAppUI().hideVideoCaptureButton(true);
//        } else {
//            mAppController.getCameraAppUI().showVideoCaptureButton(true);
//        }
    }

    private boolean mVideoRecordingPaused = false;

    @Override
    protected boolean isVideoRecordingPaused() {
        return mVideoRecordingPaused;
    }

    private long mRecordingPausedTime;
    private long mSkippedTime;

    @Override
    protected long getDeltaTime() {
        return (SystemClock.uptimeMillis() - mRecordingStartTime - mSkippedTime);
    }

    @TargetApi(Build.VERSION_CODES.N) // MODIFIED by jianying.zhang, 2016-10-15,BUG-2715761
    private void pauseVideoRecording() {
        if (mCameraState != RECORDING) {
            return;
        }
        if (mMediaRecorder == null || !mMediaRecorderRecording) {
            return;
        }
        try {
            /* MODIFIED-BEGIN by jianying.zhang, 2016-10-15,BUG-2715761*/
            if (ApiHelper.isNOrHigher()) {
                mMediaRecorder.pause();
            } else {
                Class cls = Class.forName("android.media.MediaRecorder");
                if (!invoke(cls, "pause", mMediaRecorder)) {
                    if (!invoke(cls, "setParametersExtra", mMediaRecorder)) {
                        if (!invoke(cls, "tct_pause", mMediaRecorder)) {
                            return;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        mRecordingPausedTime = System.currentTimeMillis();
        mVideoRecordingPaused = true;
        /* MODIFIED-END by jianying.zhang,BUG-2715761*/
    }

    private boolean invoke(Class cls, String mtdname, MediaRecorder recorder) {
        try {
            if (mtdname.equals("setParametersExtra")) {
                Method mtd = cls.getDeclaredMethod(mtdname, String.class);
                mtd.invoke(recorder, "media-param-pause=1");
            } else {
                Method mtd = cls.getDeclaredMethod(mtdname);
                mtd.invoke(recorder);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-15,BUG-2715761*/
    @TargetApi(Build.VERSION_CODES.N)
    private void resumeVideoRecording() {
        if (mCameraState != RECORDING || !mVideoRecordingPaused
                || mMediaRecorder == null) {
            return;
        }

        try {
            if (ApiHelper.isNOrHigher()) {
                mMediaRecorder.resume();
            } else {
                mMediaRecorder.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            /* MODIFIED-END by jianying.zhang,BUG-2715761*/
        }

        mVideoRecordingPaused = false;
        mSkippedTime += System.currentTimeMillis() - mRecordingPausedTime;
        mHandler.sendEmptyMessage(MSG_UPDATE_RECORD_TIME);
    }

    @Override
    protected boolean onStopVideoRecording() {
        mSkippedTime = 0;
        mVideoRecordingPaused = false;
        return super.onStopVideoRecording();
    }

    /* MODIFIED-BEGIN by guodong.zhang, 2016-11-01,BUG-3272008*/
    @Override
    protected void resetPauseButton() {
        super.resetPauseButton();
        getVideoUI().showPausedUI(mVideoRecordingPaused);
    }
    /* MODIFIED-END by guodong.zhang,BUG-3272008*/
}
