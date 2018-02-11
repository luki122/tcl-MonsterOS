package com.android.camera;

import android.hardware.Camera;
import android.location.Location;

import com.android.camera.app.AppController;
import com.android.camera.debug.Log;
import com.android.camera.exif.ExifInterface;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager; // MODIFIED by jianying.zhang, 2016-05-30,BUG-2202266
import com.android.ex.camera2.portability.CameraAgent;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.external.plantform.ExtBuild;
import com.tct.camera.R;

import java.lang.reflect.Method;
import java.util.LinkedList;


/**
 * Created by bin.zhang2-nb on 1/21/16.
 */
public class MT_BurstPhotoModule extends NormalPhotoModule {

    private static final String MT_BURST_PHOTO_MODULE_STRING_ID="MT_BurstPhotoModule";
    private Log.Tag TAG=new Log.Tag(MT_BURST_PHOTO_MODULE_STRING_ID);

    private MtSoundPlay mMtSoundPlayer = null;
    private static final int BURST_UPDATE_INTERVAL=150;
    private final static int GESTURE_HIDE_TIME_DELAY = 3000; // MODIFIED by jianying.zhang, 2016-05-30,BUG-2202266
    private MtLongshotPictureCallback mMtLongshotCallback;

    public MT_BurstPhotoModule(AppController app) {
        super(app);
    }

    private boolean mResumed; //MODIFIED by peixin, 2016-03-29,BUG-1872206

    /* MODIFIED-BEGIN by jianying.zhang, 2016-05-30,BUG-2202266*/
    protected boolean isGestureDetectionOn; // MODIFIED by peixin, 2016-04-25,BUG-1983708
    protected boolean mShowGestureDetectionUI;
    /* MODIFIED-END by jianying.zhang,BUG-2202266*/
    @Override
    protected boolean takeOptimizedBurstShot(Location loc) {
        Log.d(TAG, "[takeOptimizedBurstShot]......");
        mMtProcessor.start();
        mMtLongshotCallback = new MtLongshotPictureCallback(loc);
        if (Keys.isShutterSoundOn(mAppController.getSettingsManager())) {
            mCameraDevice.enableShutterSound(false);
        }
        setCameraState(SNAPSHOT_LONGSHOT_PENDING_START);
        mCameraDevice.burstShot(null, null, null, null, null);
        mCameraDevice.applySettings(mCameraSettings);
        mCameraDevice.takePicture(mHandler, new MtBurstShutterCallback(), null, null, mMtLongshotCallback);
        return true;
    }

    @Override
    protected void abortOptimizedBurstShot() {
        Log.d(TAG, "[abortOptimizedBurstShot]");
        if (Keys.isShutterSoundOn(mAppController.getSettingsManager())) {
            mCameraDevice.enableShutterSound(true);
            if (mMtSoundPlayer != null) {
                mMtSoundPlayer.stop();
            }
        }
        mCameraDevice.abortBurstShot();
        mCameraDevice.applySettings(mCameraSettings);
        mMtProcessor.stop();

        // mtk6755 device, flash and zsl set ON at same time, need to restart preview.
        if (CameraCapabilities.FlashMode.OFF.equals(mCameraSettings.getCurrentFlashMode()) == false ) {
            if (isZslOn()) {
                getPhotoUI().clearEvoPendingUI();
                getPhotoUI().clearFocus();
                mFocusManager.resetTouchFocus();
                setupPreview();
            }
        }
    }

    @Override
    public void resume() {
        mMtSoundPlayer = new MtSoundPlay();
        mMtSoundPlayer.load();
        super.resume();
         /*MODIFIED-BEGIN by peixin, 2016-03-29,BUG-1872206*/
        mResumed = true;
        /* MODIFIED-BEGIN by jianying.zhang, 2016-05-30,BUG-2202266*/
        /* MODIFIED-BEGIN by peixin, 2016-04-25,BUG-1983708*/
        SettingsManager settingsManager = mActivity.getSettingsManager();
        boolean isGestureDetectionOnTemp = Keys.isGestureDetectionOn(settingsManager);
        if((isGestureDetectionOnTemp != isGestureDetectionOn) && isEnableGestureRecognization())
        {
            showInfo();
        }
        isGestureDetectionOn = isGestureDetectionOnTemp;
        /* MODIFIED-END by peixin,BUG-1983708*/
        /* MODIFIED-END by jianying.zhang,BUG-2202266*/
    }

    @Override
    protected void showGestureGuideInfo() {
        super.showGestureGuideInfo();
        if (ExtBuild.device() == ExtBuild.MTK_MT6755 && isEnableGestureRecognization()) {
            showInfo();
        }
    }

    @Override
    public void pause() {
        super.pause();
        if (!mResumed) return;
        mMtSoundPlayer.unLoad();
        mMtProcessor.stop();
        mResumed = false;
         /*MODIFIED-END by peixin,BUG-1872206*/
        /* MODIFIED-BEGIN by jianying.zhang, 2016-05-30,BUG-2202266*/
        Log.w(TAG, "on Pause");
        if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
            hideInfo();
        }
    }

    public void showInfo() {
        Log.d(TAG, "showInfo");
        mShowGestureDetectionUI = true ;
        getPhotoUI().setGestureToastLayoutVisibility(true);
        Runnable mRunnable = new Runnable() {
            @Override
            public void run() {
                hideInfo();
            }
        };
        mHandler.postDelayed(mRunnable,GESTURE_HIDE_TIME_DELAY);
    }
    protected void hideInfo() {
        if (mShowGestureDetectionUI) {
            mShowGestureDetectionUI = false;
            getPhotoUI().setGestureToastLayoutVisibility(false);
        }
    }
    /* MODIFIED-END by jianying.zhang,BUG-2202266*/

    @Override
    protected void stopBurst() {
        Log.v(TAG,"[stopBurst]");
        super.stopBurst();
    }

    private MtLongshotPictureCbProcessor mMtProcessor = new MtLongshotPictureCbProcessor();

    // run in main thread ...
    protected class MtLongshotPictureCbProcessor {

        private class MtMessage {
            private byte[] mOriginalJpegData;
            private CameraAgent.CameraProxy mCamera;
            private MtLongshotPictureCallback mCallback = null;
        }
        private LinkedList<MtMessage> mQueue = new LinkedList<MtMessage>();

        private final int IDLE = 1;
        private final int RUNNING = 2;
        private int mStatus = IDLE;

        private Runnable mLoop = new Runnable() {
            @Override
            public void run() {
                doRunOne();
                if (mStatus == RUNNING) {
                    mHandler.postDelayed(mLoop, BURST_UPDATE_INTERVAL);
                }
            }

            private void doRunOne() {
                MtMessage msg = mQueue.poll();
                if (msg == null) {
                    Log.v(TAG, "[MtLongshotPictureCbProcessor.Runnable.doRunOne] mQueue is empty.");
                    return;
                }
                Log.v(TAG, "[MtLongshotPictureCbProcessor.Runnable.doRunOne] timestamp= " + System.currentTimeMillis());

                if (msg.mCamera == null || msg.mOriginalJpegData == null || msg.mCallback == null) {
                    Log.e(TAG, "[MtLongshotPictureCbProcessor.Runnable.doRunOne] should not come here.");
                } else {
                    msg.mCallback.doPictureTaken(msg.mOriginalJpegData, msg.mCamera);
                }
            }
        };

        public void start() {
            mStatus = RUNNING;
            mQueue.clear();
            mHandler.removeCallbacks(mLoop);
            mHandler.post(mLoop);
        }

        public void stop() {
            mStatus = IDLE;
            mHandler.removeCallbacks(mLoop);
            mQueue.clear();
        }

        public void add(final byte[] originalJpegData, final CameraAgent.CameraProxy camera, MtLongshotPictureCallback callback) {
            if (mStatus != RUNNING) {
                return ;
            }
            MtMessage msg = new MtMessage();
            msg.mCamera = camera;
            msg.mOriginalJpegData = originalJpegData;
            msg.mCallback = callback;
            mQueue.push(msg);
        }
    }

    private class MtLongshotPictureCallback extends LongshotPictureCallback{

        private int mReceivedShotNum = 0;

        public MtLongshotPictureCallback(Location loc) {
            super(loc);
        }


        private void doPictureTaken(final byte[] originalJpegData, final CameraAgent.CameraProxy camera) {
            // Running in main thread
            Log.d(TAG, "[MtLongshotPictureCallback.doPictureTaken]");
            if (mPaused) {
                return;
            }
            if(mCameraState!= SNAPSHOT_LONGSHOT_PENDING_START &&mCameraState!=SNAPSHOT_LONGSHOT){
                Log.i(TAG, "[MtLongshotPictureCallback.doPictureTaken] stop burst in picture taken");
                stopBurst();
                return;
            }
            // Do not take the picture if there is not enough storage.
            if (mActivity.getStorageSpaceBytes() <= Storage.LOW_STORAGE_THRESHOLD_BYTES) {
                Log.i(TAG, "[MtLongshotPictureCallback.doPictureTaken] Not enough space or storage not ready. remaining="
                        + mActivity.getStorageSpaceBytes());
                mVolumeButtonClickedFlag = false;
                stopBurst();
                return;
            }
            if(mCameraState== SNAPSHOT_LONGSHOT_PENDING_START){
                setCameraState(SNAPSHOT_LONGSHOT);
            }

            /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-28,BUG-1998630*/
            if (mReceivedBurstNum == 1 && mCameraState == SNAPSHOT_LONGSHOT) {
                Log.d(TAG, "[MtLongshotPictureCallback.doPictureTaken] start sound");
                if (mMtSoundPlayer != null && Keys.isShutterSoundOn(mAppController.getSettingsManager())) {
                    mMtSoundPlayer.play();
                }
            }
            /* MODIFIED-END by bin.zhang2-nb,BUG-1998630*/

            mReceivedBurstNum++;
//            Log.d(TAG, "[MtLongshotPictureCallback.doPictureTaken] mReceivedBurstNum = " + mReceivedBurstNum);
//            if (mReceivedBurstNum <= 1) {
//               doSetBurstshotSpeed(camera, 3);
//            }

            Log.i(TAG, "[MtLongshotPictureCallback.doPictureTaken] mReceivedBurstNum= " + mReceivedBurstNum);
            getPhotoUI().updateBurstCount(mReceivedBurstNum, BURST_MAX);
            final ExifInterface exif = Exif.getExif(originalJpegData);
            updateExifAndSave(exif,originalJpegData, camera);

            if(mReceivedBurstNum>=BURST_MAX){
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopBurst();
                    }
                }, BURST_STOP_DELAY);//Give
                return;
            }
        }


        @Override
        public void onPictureTaken(final byte[] originalJpegData, final CameraAgent.CameraProxy camera) {
            // Running in main thread
            ++mReceivedShotNum;
            Log.d(TAG, "[MtLongshotPictureCallback.onPictureTaken] mReceivedShotNum = " + mReceivedShotNum);
            if (mReceivedShotNum <= BURST_MAX) {
                mMtProcessor.add(originalJpegData, camera, this);
            }
        }
    }

    // control burst shot output data speed
    private void doSetBurstshotSpeed(CameraAgent.CameraProxy camera, int fps) {
        try {
            Class<Camera> cameraClz = Camera.class;
            Method method = cameraClz.getDeclaredMethod("setContinuousShotSpeed", int.class);
            method.invoke(camera.getCamera(), fps);
            Log.d(TAG, "[doSetBurstshotSpeed] OK, speed = " + fps);
        } catch (Exception e) {
            Log.d(TAG, "[doSetBurstshotSpeed] FAIL : " + e);
        }
    }

    @Override
    public void onShutterButtonFocus(boolean pressed) {
        super.onShutterButtonFocus(pressed);
    }

    private final class MtBurstShutterCallback implements CameraAgent.CameraShutterCallback {
        @Override
        public void onShutter(CameraAgent.CameraProxy camera) {
            Log.v(TAG, "[MtBurstShutterCallback.onShutter]");
            /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-28,BUG-1998630*/
            //if (mMtSoundPlayer != null && Keys.isShutterSoundOn(mAppController.getSettingsManager())) {
            //    mMtSoundPlayer.play();
            //}
            /* MODIFIED-END by bin.zhang2-nb,BUG-1998630*/
        }
    }

    // all work in main thread.
    private class MtSoundPlay {
        private android.media.SoundPool mSoundPool = null;
        private int mSoundID = 0;
        private int mStreamID = 0;

        private MtSoundPlay() {
        }

        private void load() {
            Log.v(TAG, "[MtSoundPlay.load]");
            mSoundPool = new android.media.SoundPool(10, SoundClips.getAudioTypeForSoundPool(), 0);
            mSoundID = mSoundPool.load(mActivity, R.raw.continuous_shot_x1, 1);
        }

        private void play() {
            Log.v(TAG, "[MtSoundPlay.play]");
            if (mSoundPool == null) {
                load();
            }

            mStreamID = mSoundPool.play(mSoundID, 1.0f, 1.0f, 1, -1, 1.0f);
            Log.v(TAG, "[MtSoundPlay.play] mSoundID =" + mSoundID + ", mStreamID=" + mStreamID);
            if (mStreamID == 0) {
                Log.v(TAG, "[MtSoundPlay.play] Try load sound file");
                load(); // try it again.
                mStreamID = mSoundPool.play(mSoundID, 1.0f, 1.0f, 1, -1, 1.0f);
            }
        }

        private void stop() {
            Log.v(TAG, "[MtSoundPlay.stop]");
            if (mSoundPool != null && mStreamID != 0) {
                mSoundPool.stop(mStreamID);
            }
        }

        private void unLoad() {
            Log.v(TAG, "[MtSoundPlay.unLoad]");
            if (mSoundPool != null) {
                mSoundPool.unload(mSoundID);
                mSoundPool.release();
                mSoundPool = null;
            }
        }
    }
}
