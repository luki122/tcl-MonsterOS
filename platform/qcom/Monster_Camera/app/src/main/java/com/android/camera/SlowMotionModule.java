package com.android.camera;

import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.view.KeyEvent;

import com.android.camera.app.AppController;
import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.util.CameraUtil;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.ex.camera2.portability.CameraSettings;
import com.android.ex.camera2.portability.Size;
import com.tct.camera.R;


import java.util.List;
import java.util.Set;

/**
 * Created by sichao.hu on 9/21/15.
 */
public class SlowMotionModule extends  VideoModule {


    private static final String SLOW_MOTION_MODULE_STRING_ID="SlowMotionModule";
    private static final Log.Tag TAG = new Log.Tag(SLOW_MOTION_MODULE_STRING_ID);
    private static final int VIDEO_HIGH_FRAME_RATE = 120; //hsr120
    private Size mHsrSize = null;


    /**
     * Construct a new SlowMotion module.
     *
     * @param app
     */
    public SlowMotionModule(AppController app) {
        super(app);
    }

    @Override
    protected boolean isNeedStartRecordingOnSwitching() {
        return false;
    }

    @Override
    public int getModuleId() {
        return mAppController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_slowmotion);
    }

    @Override
    protected boolean isVideoStabilizationEnabled() {
        return false;
    }


    @Override
    public String getModuleStringIdentifier() {
        return SLOW_MOTION_MODULE_STRING_ID;
    }

    @Override
    protected VideoUI getVideoUI() {
        return new SlowMotionUI(mActivity, this,  mActivity.getModuleLayoutRoot());
    }

    @Override
    protected void mediaRecorderParameterFetching(MediaRecorder recorder) {
        Log.w(TAG, "set slow motion mediaRecorder parameters");
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
    }

    @Override
    protected void tryLockFocus() {
        if(mCameraCapabilities.supports(CameraCapabilities.FocusMode.AUTO)&&mCameraState==RECORDING) {
            if(mFocusManager.getFocusMode(mCameraSettings.getCurrentFocusMode())!= CameraCapabilities.FocusMode.AUTO) {
                mFocusManager.overrideFocusMode(CameraCapabilities.FocusMode.AUTO);
                mCameraSettings.setFocusMode(CameraCapabilities.FocusMode.AUTO);
                mCameraDevice.applySettings(mCameraSettings);
            }
        }
    }

    @Override
    protected boolean isSupported(int width, int height) {
        // is resolution supported
        if (mHsrSize == null || !(width <= mHsrSize.width() && height <= mHsrSize.height())) {
            Log.e(TAG, "Unsupported HSR and video size combinations");
            return false;
        }

        // is fileSize supported
        int expectedMBsPerSec = width * height * VIDEO_HIGH_FRAME_RATE;

        return true;
    }

    @Override
    protected void setHsr(CameraSettings cameraSettings) {
        cameraSettings.setHsr("" + VIDEO_HIGH_FRAME_RATE);
    }

    @Override
    protected int getProfileQuality() {
        int quality = CamcorderProfile.QUALITY_LOW;
        List<String> supportedVideoHighFrameRates = mCameraCapabilities.getSupportedVideoHighFrameRates();
        boolean isSupported = supportedVideoHighFrameRates == null ? false : supportedVideoHighFrameRates.indexOf(String.valueOf(VIDEO_HIGH_FRAME_RATE)) >= 0;
        if (isSupported) {
            int index = supportedVideoHighFrameRates.indexOf(""+VIDEO_HIGH_FRAME_RATE);
            if (index != -1){
                mHsrSize = mCameraCapabilities.getSupportedHsrSizes().get(index);
            }
        }
        if (mHsrSize != null) {
            String hsrQuality = mHsrSize.width() + "x" + mHsrSize.height();
            if (SettingsUtil.VIDEO_QUALITY_TABLE.containsKey(hsrQuality)) {
                quality = SettingsUtil.VIDEO_QUALITY_TABLE.get(hsrQuality);
                Log.w(TAG, "Selected video quality for '" + hsrQuality);
            } else {
                quality = getProximateQuality(mHsrSize.width(), mHsrSize.height());
            }
        }

        Log.w(TAG, "Profile quality is " + quality);
        return quality;
    }

    private int getProximateQuality(int hsrWidth, int hsrHeight) {
        Set set = SettingsUtil.VIDEO_QUALITY_TABLE.keySet();
        String proximateResolution = CameraUtil.getProximateResolution(set, hsrWidth, hsrHeight);
        if (proximateResolution != null) {
            Log.w(TAG, "Selected video quality for '" + proximateResolution);
            return SettingsUtil.VIDEO_QUALITY_TABLE.get(proximateResolution);
        }
        else {
            return CamcorderProfile.QUALITY_LOW;
        }
    }

    @Override
    protected void overrideProfileSize() {
        Log.w(TAG, "override profile size");
        if (mHsrSize == null) {
            return;
        }
        int hsrWidth = mHsrSize.width();
        int hsrHeight = mHsrSize.height();
        if (mProfile.videoFrameWidth == hsrWidth && mProfile.videoFrameHeight == hsrHeight) {
            return;
        }
        if (mCameraCapabilities.getSupportedVideoSizes().contains(new Size(hsrWidth, hsrHeight))) {
            mProfile.videoFrameWidth = hsrWidth;
            mProfile.videoFrameHeight = hsrHeight;
        } else {
            List<Size> sizes = mCameraCapabilities.getSupportedVideoSizes();
            Size maxSize = CameraUtil.getProximateSize(sizes, hsrWidth, hsrHeight);
            if (maxSize != null) {
                mProfile.videoFrameWidth = maxSize.width();
                mProfile.videoFrameHeight = maxSize.height();
            }
        }
        Log.w(TAG, "video size is:" + mProfile.videoFrameWidth + "x" + mProfile.videoFrameHeight);
    }

    @Override
    protected boolean hideCamera() {
        return true;
    }

    @Override
    public void onShutterButtonClick() {
        if (mMediaRecorderRecording){
            // Only active ‘stop’ when recording duration time exceeds 1s.
            long duration = SystemClock.uptimeMillis() - mRecordingStartTime;
            if (duration <= 1000){
                return;
            }
        }

        super.onShutterButtonClick();
    }

    @Override
    public void pause() {
        super.pause();
    }

    @Override
    public void hardResetSettings(SettingsManager settingsManager) {
        super.hardResetSettings(settingsManager);
        settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL, Keys.KEY_CAMERA_ID);
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
            case CameraUtil.BOOM_KEY:
                if (event.getRepeatCount() == 0) {
                    onShutterButtonClick();
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

}
