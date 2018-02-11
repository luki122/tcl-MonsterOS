package com.android.camera;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask; // MODIFIED by xuan.zhou, 2016-04-27,BUG-1997433
import android.os.Environment;
import android.os.Message;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.camera.app.AppController;
import com.android.camera.app.MediaSaver;
import com.android.camera.debug.Log;
import com.android.camera.exif.ExifInterface;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.util.CameraUtil;
/* MODIFIED-BEGIN by xuan.zhou, 2016-05-25,BUG-2167363*/
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
/* MODIFIED-END by xuan.zhou,BUG-2167363*/
import com.android.ex.camera2.portability.CameraAgent;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.ex.camera2.portability.CameraDeviceInfo;
import com.android.ex.camera2.portability.Size;
import com.morpho.core.InitParam;
import com.morpho.core.wrapper.MorphoPanoramaGPWrapper;
import com.tct.camera.R;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.media.MediaActionSound;

/**
 * Created by bin.zhang2-nb on 12/24/15.
 */
public class MT_PanoramaModule extends PhotoModule implements PhotoModule.MainHandlerCallback{
    private MT_PanoramaUI mMtPanoramaUI;
    public static final String MTPANORAMA_MODULE_STRING_ID = "MT_PanoramaModule";
    private static final Log.Tag TAG = new Log.Tag(MTPANORAMA_MODULE_STRING_ID);

    private static final int PIXEL_UPPER_BOUND=5000000;

/* MODIFIED-BEGIN by bin.zhang2-nb, 2016-03-22,BUG-1850559 */
//    private int mCameraPreviewW, mCameraPreviewH;
//    private int mCameraPictureW, mCameraPictureH;
/* MODIFIED-END by bin.zhang2-nb,BUG-1850559 */

    public static final int INFO_UPDATE_PROGRESS = 0;
    public static final int INFO_UPDATE_MOVING = 1;
    public static final int INFO_START_ANIMATION = 2;
    public static final int INFO_IN_CAPTURING = 3;
    public static final int INFO_OUTOF_CAPTURING = 4;

    private final static int STATE_UNKNOWN = 0;
    private final static int STATE_CAPTURING = 1;
    private final static int STATE_IDLE = 2;
    private final static int STATE_CLOSED = 4;

    private static final int NUM_AUTORAMA_CAPTURE = 9;

    private int mCurrentNum = 0;
    private long mCaptureTime = 0;

    private byte[] mJpegImageData = null;
    int mJpegRotation = 0;

    private boolean mIsShowingCollimatedDrawable;
    private boolean mIsInStopProcess = false;
    private boolean mIsMerging = false;
    private boolean mPanoPicSavedDone = true; // MODIFIED by jianying.zhang, 2016-06-14,BUG-2241996

    private static final int MSG_FINAL_IMAGE_READY = 6000;
    private static final int MSG_ORIENTATION_CHANGED = 6001;
    private static final int MSG_CLEAR_SCREEN_DELAY = 6002;
    private static final int MSG_LOCK_ORIENTATION = 6003;
    private static final int MSG_SAVE_FILE = 6004;
    private static final int MSG_UPDATE_MOVINE = 6005;
    private static final int MSG_UPDATE_PROGRESS = 6006;
    private static final int MSG_START_ANIMATION = 6007;
    private static final int MSG_HIDE_VIEW = 6008;
    private static final int MSG_IN_CAPTURING = 6009;
    private static final int MSG_OUTOF_CAPTURING = 6010;
    private static final int MSG_INIT = 6011;
    private static final int MSG_UNINIT = 6012;
    private static final int MSG_SHOW_INFO = 6013;

    private int mCurrentModeState = STATE_UNKNOWN;

    private int mPanoOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;

    private MediaActionSound mCameraSound;
    private Thread mLoadSoundTread;

    public MT_PanoramaModule(AppController app) {
        super(app);
        Log.i(TAG, "MT_PanoramaModule"); // MODIFIED by bin.zhang2-nb, 2016-05-06,BUG-2009467

//        File ex_strage = Environment.getExternalStorageDirectory();
//        mSaveBaseDirPath = ex_strage.getPath() + "/DCIM/Camera";
//        mSaveInputDirPath = mSaveBaseDirPath + "/input";
//        File save_dir = new File(mSaveInputDirPath);
//        if (!save_dir.exists()) {
//            save_dir.mkdirs();
//        }
    }

    public int getModeState() {
        return mCurrentModeState;
    }

    public void setModeState(int state) {
        mCurrentModeState = state;
        Log.i(TAG, "[setModeState] state = " + state);
    }

    @Override
    public void init(CameraActivity activity, boolean isSecureCamera, boolean isCaptureIntent) {
        Log.i(TAG, "[init] isSecureCamera = " + isSecureCamera + ", isCaptureIntent=" + isCaptureIntent);
        super.init(activity, isSecureCamera, isCaptureIntent);
        /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-05-06,BUG-2009467*/
        //mPanoOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
        mPanoOrientation = 0;
        /* MODIFIED-END by bin.zhang2-nb,BUG-2009467*/
        mMtPanoramaUI.init(mActivity); // app.getOrientationManager();
        super.setMainHandlerCallback(this); // extend mHandler callback
    }

    @Override
    public void resume() {
        super.resume();
        mCameraSound = new MediaActionSound();
        mLoadSoundTread = new LoadSoundTread();
        mLoadSoundTread.start();
    }

    private class LoadSoundTread extends Thread {
        @Override
        public void run() {
            if (mCameraSound != null)
                mCameraSound.load(MediaActionSound.SHUTTER_CLICK);
        }
    }

    @Override
    protected PhotoUI getPhotoUI() {
        if( mMtPanoramaUI == null) {
            mMtPanoramaUI = new MT_PanoramaUI(mActivity, this, mActivity.getModuleLayoutRoot());
        }
        return mMtPanoramaUI;
    }

    @Override
    public void onOrientationChanged(int orientation) {
        Log.i(TAG, "[onOrientationChanged] orientation = " + orientation);
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return;
        }
        mPanoOrientation = CameraUtil.roundOrientation(orientation, mPanoOrientation);
    }

    @Override
    public void hardResetSettings(SettingsManager settingsManager) {
        super.hardResetSettings(settingsManager);
        settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL, Keys.KEY_CAMERA_ID);
    }

    @Override
    public String getModuleStringIdentifier() {
        return MTPANORAMA_MODULE_STRING_ID;
    }

    @Override
    public int getModuleId() {
        return mAppController.getAndroidContext().getResources()
                .getInteger(R.integer.camera_mode_pano);
    }

    @Override
    public void onCameraAvailable(CameraAgent.CameraProxy cameraProxy) {
        Log.i(TAG, "[onCameraAvailable]");
        super.onCameraAvailable(cameraProxy);
        /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-03-22,BUG-1850559 */
        if (mPaused) {
            Log.w(TAG, "[onCameraAvailable] mPaused is true!");
            return;
        }
//        Size previewSZ = mCameraSettings.getCurrentPreviewSize();
//        Size pictureSZ = mCameraSettings.getCurrentPhotoSize();
//        mCameraPreviewW = previewSZ.width();
//        mCameraPreviewH = previewSZ.height();
//        mCameraPictureW = pictureSZ.width();
//        mCameraPictureH = pictureSZ.height();
/* MODIFIED-END by bin.zhang2-nb,BUG-1850559 */
//        mStillProcList = new ArrayList<StillImageData>();
//        mCameraDevice.setJpegOrientation(mDisplayRotation);
/*MODIFIED-BEGIN by peixin, 2016-04-06,BUG-1845449*/
//        setAutoExposureLock(false);
//        setAutoWhiteBalanceLock(false);
/*MODIFIED-END by peixin,BUG-1845449*/
    }

    @Override
    public void onSingleTapUp(View view, int x, int y) {
        //dummy here , manual focus not supported in panroama
    }

    private final Comparator<Size> mSizeComparator=new Comparator<Size>() {
        @Override
        public int compare(Size size, Size t1) {
            int sizePixel=size.width()*size.height();
            int t1Pixel=t1.width()*t1.height();

            return t1Pixel-sizePixel;
        }
    };

    @Override
    protected void updateParametersPictureSize() {
        if (mCameraDevice == null) {
            Log.e(TAG, "attempting to set picture size without caemra device");
            return;
        }

        SettingsManager settingsManager = mActivity.getSettingsManager();
        String pictureSizeKey = isCameraFrontFacing() ? Keys.KEY_PICTURE_SIZE_FRONT
                : Keys.KEY_PICTURE_SIZE_BACK;
        String defaultPicSize = SettingsUtil.getDefaultPictureSize(isCameraFrontFacing());
        String pictureSize = settingsManager.getString(SettingsManager.SCOPE_GLOBAL,
                pictureSizeKey, defaultPicSize);
//        CameraPictureSizesCacher.updateSizesForCamera(mAppController.getAndroidContext(),
//                mCameraDevice.getCameraId(), supported);
//        SettingsUtil.setCameraPictureSize(pictureSize, supported, mCameraSettings,
//                mCameraDevice.getCameraId());
//        Size size = SettingsUtil.getPhotoSize(pictureSize, supported,
//                mCameraDevice.getCameraId());
        Size size = SettingsUtil.sizeFromString(pictureSize);
        if(size.width()*size.height()>=PIXEL_UPPER_BOUND){
            List<Size> photoSizes=mCameraCapabilities.getSupportedPhotoSizes();
            Collections.sort(photoSizes, mSizeComparator);
            for(Size candidatePhotoSize:photoSizes){
                if(candidatePhotoSize.width()*candidatePhotoSize.height()<PIXEL_UPPER_BOUND){
                    size=candidatePhotoSize;
                    break;
                }
            }
        }
        Log.d(TAG, "Take picture with size :" + size);

        mCameraSettings.setPhotoSize(size);

        // Set a preview size that is closest to the viewfinder height and has
        // the right aspect ratio.
        List<Size> sizes = mCameraCapabilities.getSupportedPreviewSizes();
        Size optimalSize = CameraUtil.getOptimalPreviewSize(mActivity, sizes,
                (double) size.width() / size.height());
        Size original = mCameraSettings.getCurrentPreviewSize();
        if (!optimalSize.equals(original)) {
            Log.v(TAG, "setting preview size. optimal: " + optimalSize + "original: " + original);
            mCameraSettings.setPreviewSize(optimalSize);
            mCameraDevice.applySettings(mCameraSettings);
            mCameraSettings = mCameraDevice.getSettings();
        }

        if (optimalSize.width() != 0 && optimalSize.height() != 0) {
            Log.v(TAG, "updating aspect ratio");
            getPhotoUI().updatePreviewAspectRatio((float) optimalSize.width()
                    / (float) optimalSize.height());
        }
        Log.d(TAG, "Preview size is " + optimalSize);
    }

    @Override
    public void onPreviewStarted() {
        super.onPreviewStarted();
        /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-03-22,BUG-1850559 */
        if(mPaused){
            return;
        }
        /* MODIFIED-END by bin.zhang2-nb,BUG-1850559 */
        setModeState(STATE_IDLE);

//        mIsShooting = false;
//        if (!hasShownNoticeOfSlowly) {
//            ToastUtil.makeText(mActivity, mActivity.getString(R.string.MSG_MOVE_CAMERA_SLOWLY), Toast.LENGTH_LONG);
//            ToastUtil.setGravity(Gravity.CENTER, 0, 0);
//            ToastUtil.show();
//            hasShownNoticeOfSlowly = true;
//        }
//        if(mCameraDevice != null) {
//            mCameraPreviewBuff = new byte[mCameraPreviewW * mCameraPreviewH * 3 / 2];
//            mCameraDevice.addCallbackBuffer(mCameraPreviewBuff);
//            mCameraDevice.setPreviewDataCallbackWithBuffer(mHandler, this);
//            mCameraDevice.setPreviewDataCallback(mHandler, this);
//        }
//        mMt6755PanoramaUI.showSelectDirectionUI(mForce_PanoramaDirection_HORIZONTAL_RIGHT);
//        mMt6755PanoramaUI.setTipTextViewVisiblity(true);
//        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
//        android.hardware.Camera.getCameraInfo(0, info);
//        int degrees = 0;
//        switch (mActivity.getWindowManager().getDefaultDisplay().getRotation()) {
//            case Surface.ROTATION_0: degrees = 0; break;
//            case Surface.ROTATION_90: degrees = 90; break;
//            case Surface.ROTATION_180: degrees = 180; break;
//            case Surface.ROTATION_270: degrees = 270; break;
//        }
//        picRotation = (info.orientation - degrees + 360) % 360;
    }

    @Override
    public boolean onBackPressed() {
        if (getModeState() == STATE_CAPTURING) {
            stopCapture(false);
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void pause(){
        Log.i(TAG, "[pasue]mMainHandler = " + mHandler);
        if (mHandler != null) {
            mHandler.removeMessages(MSG_HIDE_VIEW);
            mHandler.sendEmptyMessage(MSG_HIDE_VIEW);
        }
        /* MODIFIED-BEGIN by yongsheng.shan, 2016-03-22, BUG-1840444 */
        if (getModeState() == STATE_CAPTURING) {
            mMtPanoramaUI.reset();
            mMtPanoramaUI.hide();
            resetCapture();
        }
        /* MODIFIED-END by yongsheng.shan,BUG-1840444 */
        stopCapture(false);
        safeStop();
        super.pause();

        if (mCameraSound != null) {
            mCameraSound.release();
            mCameraSound = null;
        }
    }

    @Override
    public void onShutterButtonClick() {
         /*MODIFIED-BEGIN by bin.zhang2-nb, 2016-03-28,BUG-1867775*/
        Log.d(TAG, "[onShutterButtonClick] modeState : " + getModeState()); // MODIFIED by jianying.zhang, 2016-06-14,BUG-2241996
        if (getModeState() == STATE_CAPTURING ) {
            if (mCurrentNum <= 0) {
                Log.d(TAG, "[onShutterButtonClick] ERROR: mCurrentNum =" + mCurrentNum);
                return ;
            }
            Log.d(TAG, "[onShutterButtonClick] mCurrentNum =" + mCurrentNum);
             /*MODIFIED-END by bin.zhang2-nb,BUG-1867775*/
            stopCapture(true);
        } else if (mPanoPicSavedDone) {// MODIFIED by jianying.zhang, 2016-06-14,BUG-2241996
            startPanoramaCapture();
        }
//        synchronized (mSyncObj) {
//            if (mIsShooting) {
//                finishPanoramaShooting(true);
//            } else {
//                startPanoramaShooting();
//            }
//        }
    }

    protected boolean isEnoughSpace() {
        return true; // TODO
    }

    private void setAutoFocusMoveCallback(boolean val) {
        if (mCameraDevice == null) {
            return;
        }
        if (val && mCameraSettings.getCurrentFocusMode() == CameraCapabilities.FocusMode.CONTINUOUS_PICTURE) {
            mCameraDevice.setAutoFocusMoveCallback(mHandler, (CameraAgent.CameraAFMoveCallback) mAutoFocusMoveCallback);
        } else {
            mCameraDevice.setAutoFocusMoveCallback(mHandler,null);
        }
    }


    private boolean startPanoramaCapture() {
        Log.i(TAG, "[startPanoramaCapture] current state = " + getModeState() + ",mIsMerging = " + mIsMerging);
        if (!isEnoughSpace() || STATE_IDLE != getModeState() || mIsMerging) {
            Log.w(TAG, "[startPanoramaCapture]return,mIsCameraClosed = " + getModeState());
            return false;
        }

        if (!startCapture()) {
            Log.w(TAG, "[startPanoramaCapture]not capture.");
            return false;
        }

        mAppController.getCameraAppUI().getTopMenus().setTopModeOptionVisibility(false);
        // make sure focus UI be cleared before capture.
//        mIFocusManager.resetTouchFocus();
//        mIFocusManager.updateFocusUI();
//        mIFocusManager.setAwbLock(true);
//        mIModuleCtrl.applyFocusParameters(false);
//        mICameraAppUi.switchShutterType(ShutterButtonType.SHUTTER_TYPE_OK_CANCEL);
//        mIFileSaver.init(FILE_TYPE.PANORAMA, 0, null, -1);
        mCaptureTime = System.currentTimeMillis();

//        mICameraAppUi.setSwipeEnabled(false);
//        mICameraAppUi.showRemaining();
//        mICameraAppUi.setViewState(ViewState.VIEW_STATE_CONTINUOUS_CAPTURE);
//        ICameraView thumbnailView = mICameraAppUi.getCameraView(CommonUiType.THUMBNAIL);
//        if (thumbnailView != null) {
//            thumbnailView.hide();
//        }
        mHandler.sendEmptyMessage(MSG_IN_CAPTURING);
//        mIModuleCtrl.stopFaceDetection();
//        mCameraDevice.setAutoFocusMoveCallback(mHandler, null);
        setAutoFocusMoveCallback(false);
//        showGuideString(GUIDE_MOVE);
//        mMainHandler.postDelayed(mFalseShutterCallback, 300);

        return true;
    }

    /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-06-12,BUG-2153984*/
    private boolean mPanoramaMVCallbackEnable = false;

    private CameraAgent.CameraPanoramaMoveCallback mPanoramaMVCallback = new CameraAgent.CameraPanoramaMoveCallback() {
        @Override
        public void onFrame(int xy, int direction) {
            if (mPanoramaMVCallbackEnable) {
                mHandler.obtainMessage(MSG_UPDATE_MOVINE, xy, direction).sendToTarget();
            }
            /* MODIFIED-END by bin.zhang2-nb,BUG-2153984*/
        }
    };

    private CameraAgent.CameraPanoramaCallback mPanoramaCallback = new CameraAgent.CameraPanoramaCallback() {
        @Override
        /*MODIFIED-BEGIN by peixin, 2016-04-06,BUG-1845449*/
        public void onCapture(final byte[] jpegData) {
            Log.d(TAG, "BZDB: [onCapture] tid=" + Thread.currentThread().getId());
            //onPictureTaken(jpegData);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onPictureTaken(jpegData);
                }
            });
            /*MODIFIED-END by peixin,BUG-1845449*/
        }
    };

    private Runnable mRestartCaptureView;

    private void onPictureTaken(byte[] jpegData) {

        Log.d(TAG, "[onPictureTaken]modeState = " + getModeState() + ",mCurrentNum = "
                + mCurrentNum + ",[onPictureTaken] tid=" + Thread.currentThread().getId()); //MODIFIED by peixin, 2016-04-06,BUG-1845449
        if (STATE_IDLE == getModeState()) {
            Log.w(TAG, "[onPictureTaken]modeState is STATE_IDLE,return.");
            return;
        }

        if (mCurrentNum == NUM_AUTORAMA_CAPTURE || mIsMerging) {
            Log.w(TAG, "[onPictureTaken]autorama done1,mCurrentNum = " + mCurrentNum);
            mJpegImageData = jpegData;
            mIsMerging = false;
            onHardwareStopped(true);

        } else if (mCurrentNum >= 0 && mCurrentNum < NUM_AUTORAMA_CAPTURE) {
            Log.w(TAG, "[onPictureTaken]autorama done2,mCurrentNum = " + mCurrentNum);
            playRecordSoud();
            mHandler.obtainMessage(MSG_UPDATE_PROGRESS, mCurrentNum, 0).sendToTarget();

            if (0 < mCurrentNum) {
                if (mIsShowingCollimatedDrawable) {
                    mHandler.removeCallbacks(mRestartCaptureView);
                    mHandler.removeCallbacks(mOnHardwareStop);
                }
                mIsShowingCollimatedDrawable = true;
                mRestartCaptureView = new Runnable() {
                    public void run() {
                        mIsShowingCollimatedDrawable = false;
                        mHandler.obtainMessage(MSG_START_ANIMATION, mCurrentNum, 0)
                                .sendToTarget();
                    }
                };
                mHandler.postDelayed(mRestartCaptureView, 500);
            }
        }

        mCurrentNum++;

        if (mCurrentNum == 2) {
            mAppController.getCameraAppUI().setShutterButtonEnabled(true);
        }
        if (mCurrentNum == NUM_AUTORAMA_CAPTURE) {
            stop(true);
        }
    }

    private boolean startCapture() {
        Log.d(TAG, "[startCapture]modeState = " + getModeState() + ",mIsInStopProcess = "
                + mIsInStopProcess);
        /* MODIFIED-BEGIN by yongsheng.shan, 2016-03-25,BUG-1863651 */
        if(mCameraDevice == null)
            return false;//It may be null while switching front/rear camera
            /* MODIFIED-END by yongsheng.shan,BUG-1863651 */
        if (STATE_IDLE == getModeState() && !mIsInStopProcess) {
            mAppController.getCameraAppUI().changeBottomBarInCapturePanorama();
            setModeState(STATE_CAPTURING);
            mCurrentNum = 0;
            mIsShowingCollimatedDrawable = false;

            // fix jpeg file display problem.
            int orientation = mActivity.isAutoRotateScreen() ? mDisplayRotation : mPanoOrientation;
            CameraDeviceInfo.Characteristics info = mActivity.getCameraProvider().getCharacteristics(this.getCameraId());

            switch (orientation) {
                case 0: break;
                case 270: orientation = 90; break;
                case 90: orientation = 270; break;
                case 180: break;
            }

            mJpegRotation = info.getJpegOrientation(orientation);
            mCameraDevice.setJpegOrientation(mJpegRotation);

            Log.d(TAG, "[startCapture]mPanoOrientation = " + mPanoOrientation + ",mJpegRotation = "
                    + mJpegRotation);
            mPanoramaMVCallbackEnable =  true; // MODIFIED by bin.zhang2-nb, 2016-06-12,BUG-2153984
            mCameraDevice.setRamaCallback(mHandler, mPanoramaCallback);
            mCameraDevice.setRamaMoveCallback(mHandler, mPanoramaMVCallback);
            mCameraDevice.startRama(mHandler, NUM_AUTORAMA_CAPTURE);
//            mAppController.getCameraAppUI().setShutterButtonEnabled(false);
            mMtPanoramaUI.show(mActivity, mPanoOrientation);
            return true;
        }

        return false;
    }

    private void resetCapture() {
        Log.d(TAG, "[resetCapture]...current mode state = " + getModeState());
//        if (ModeState.STATE_CLOSED != getModeState()) {
//            mIFocusManager.setAeLock(false);
//            mIFocusManager.setAwbLock(false);
//            mIModuleCtrl.applyFocusParameters(false);
//            mIModuleCtrl.startFaceDetection();
//            mICameraDevice.setAutoFocusMoveCallback(mAutoFocusMoveCallback);
              setAutoFocusMoveCallback(true);
//            showGuideString(GUIDE_SHUTTER);
//        }
//        mICameraAppUi.switchShutterType(ShutterButtonType.SHUTTER_TYPE_PHOTO_VIDEO);
//        mICameraAppUi.restoreViewState();
//        mICameraAppUi.setSwipeEnabled(true);

        mAppController.getCameraAppUI().restoreBottomBarFinishPanorama();
    }


    private void stopCapture(boolean isMerge) {
        Log.d(TAG, "[stopCapture]isMerge = " + isMerge + ",current mode state = " + getModeState());
        if (STATE_CAPTURING == getModeState()) {
            mHandler.sendEmptyMessage(MSG_OUTOF_CAPTURING);
            /* MODIFIED-BEGIN by xuan.zhou, 2016-05-25,BUG-2167363*/
            boolean muteEnd = CustomUtil.getInstance().getBoolean(
                    CustomFields.DEF_CAMERA_PANORAMA_MUTE_END, false);
            if (!muteEnd || isMerge) {
                playRecordSoud();
            }
            /* MODIFIED-END by xuan.zhou,BUG-2167363*/
            stop(isMerge);
            mAppController.getCameraAppUI().getTopMenus().setTopModeOptionVisibility(true);
        }
    }
    private void playRecordSoud(){
        if (mCameraSound != null && Keys.isShutterSoundOn(mAppController.getSettingsManager())) {
            mCameraSound.play(MediaActionSound.SHUTTER_CLICK);
        }
    }

    public void handleMessageEx(Message msg) {
        if (msg.what != MSG_UPDATE_MOVINE) {
            Log.i(TAG, "[handleMessage]msg.what = " + msg.what);
        }

        switch (msg.what) {
            case MSG_FINAL_IMAGE_READY:
//                    mMtPanoramaUI.dismissProgress();
//                    mICameraAppUi.setSwipeEnabled(true);
                resetCapture();
                mPanoPicSavedDone = true ; // MODIFIED by jianying.zhang, 2016-06-14,BUG-2241996
                break;

            case MSG_CLEAR_SCREEN_DELAY:
                mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                break;

            case MSG_LOCK_ORIENTATION:
//                    mIModuleCtrl.lockOrientation();
                break;

            case MSG_SAVE_FILE:
                saveFile();
                break;

            case MSG_UPDATE_MOVINE:
//                boolean shown = mIsShowingCollimatedDrawable
//                           || ModeState.STATE_CAPTURING != getModeState() || mCurrentNum < 1;
                /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-06-12,BUG-2153984*/
                if (mPanoramaMVCallbackEnable == false) {
                    Log.w(TAG, "should not run INFO_UPDATE_MOVING message");
                    /* MODIFIED-END by bin.zhang2-nb,BUG-2153984*/
                    break;
                }
                boolean shown = mIsShowingCollimatedDrawable || mCurrentNum < 1;
                mMtPanoramaUI.update(INFO_UPDATE_MOVING, msg.arg1, msg.arg2, shown);
                break;

            case MSG_UPDATE_PROGRESS:
                mMtPanoramaUI.update(INFO_UPDATE_PROGRESS, msg.arg1);
                break;

            case MSG_START_ANIMATION:
                mMtPanoramaUI.update(INFO_START_ANIMATION, msg.arg1);
                break;

            case MSG_HIDE_VIEW:
                mMtPanoramaUI.reset();
                mMtPanoramaUI.hide();
                break;

            case MSG_IN_CAPTURING:
                mMtPanoramaUI.update(INFO_IN_CAPTURING, msg.arg1);
                mMtPanoramaUI.showFocusUI(false); // MODIFIED by peixin, 2016-05-03,BUG-2011831
                break;

            case MSG_OUTOF_CAPTURING:
                mMtPanoramaUI.update(INFO_OUTOF_CAPTURING, msg.arg1);
                break;

            case MSG_INIT:
                mMtPanoramaUI.init(mActivity);
                break;

            case MSG_UNINIT:
                mMtPanoramaUI.uninit();
                break;

            case MSG_SHOW_INFO:
//                    String showInfoStr = mActivity.getString(R.string.pano_dialog_title)
//                            + mActivity.getString(R.string.camera_continuous_not_supported);
//                    mICameraAppUi.showInfo(showInfoStr);
                break;

            case MSG_ORIENTATION_CHANGED:
                mMtPanoramaUI.onOrientationChanged(mOrientation);
                break;

            default:
                break;
        }
    }

    private void saveFile() {
        Log.d(TAG, "[saveFile]...");
//        Location location = mIModuleCtrl.getLocation();
//        mIFileSaver.savePhotoFile(mJpegImageData, null, mCaptureTime, location, 0,
//                mFileSaverListener);

        mNamedImages.nameNewImage(mCaptureTime);

        final ExifInterface exif = Exif.getExif(mJpegImageData);
        final NamedImages.NamedEntity name = mNamedImages.getNextNameEntity();
        saveFinalPhoto(mJpegImageData, name, exif);

        mHandler.sendEmptyMessage(MSG_OUTOF_CAPTURING);
    }

    private final MediaSaver.OnMediaSavedListener mOnMediaSavedListener =
            new MediaSaver.OnMediaSavedListener() {
                @Override
                /* MODIFIED-BEGIN by xuan.zhou, 2016-04-27,BUG-1997433*/
                public void onMediaSaved(final Uri uri) {
                    Log.d(TAG, "[MediaSaver.OnMediaSavedListener.onMediaSaved]");
                    if (uri != null) {
                        // mHandler.sendEmptyMessage(MSG_FINAL_IMAGE_READY);
                        // mActivity.notifyNewMedia(uri);
                        mActivity.notifyNewMedia(uri, AppController.NOTIFY_NEW_MEDIA_ACTION_NONE);
                        new AsyncTask<Void, Void, Bitmap>() {

                            @Override
                            protected Bitmap doInBackground(Void... params) {
                                return Thumbnail.getBitmapFromUri(mActivity.getContentResolver(), uri, mJpegRotation);
                            }

                            @Override
                            protected void onPostExecute(Bitmap bitmap) {
                                if (mPaused) return;
                                mHandler.sendEmptyMessage(MSG_FINAL_IMAGE_READY);
                                if (bitmap != null) {
                                    mActivity.getCameraAppUI().updatePeekThumbBitmapWithAnimation(bitmap);
                                    mActivity.getCameraAppUI().updatePeekThumbUri(uri);
                                }
                            }

                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        /* MODIFIED-END by xuan.zhou,BUG-1997433*/
                    }
                }
            };

    private void saveFinalPhoto(final byte[] jpegData, NamedImages.NamedEntity name, final ExifInterface exif) {
        Log.d(TAG, "[saveFinalPhoto]");
        int orientation = Exif.getOrientation(exif);
        Integer exifWidth = exif.getTagIntValue(ExifInterface.TAG_PIXEL_X_DIMENSION);
        Integer exifHeight = exif.getTagIntValue(ExifInterface.TAG_PIXEL_Y_DIMENSION);
        int width, height;
        if (exifWidth != null && exifHeight != null) {
            width = exifWidth;
            height = exifHeight;
        } else {
            /*
            Size s;
            s = mCameraSettings.getCurrentPhotoSize();
            if ((mJpegRotation + orientation) % 180 == 0) {
                width = s.width();
                height = s.height();
            } else {
                width = s.height();
                height = s.width();
            }
            */
            height = 1;
            width = 1;
        }


        String title = (name == null) ? null : name.title;
        long date = (name == null) ? -1 : name.date;

        if (title == null) {
            Log.e(TAG, "Unbalanced name/data pair");
        } else {
            if (date == -1) {
                date = mCaptureStartTime;
            }
//            if (mHeading >= 0) {
//                // heading direction has been updated by the sensor.
//                ExifTag directionRefTag = exif.buildTag(
//                        ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
//                        ExifInterface.GpsTrackRef.MAGNETIC_DIRECTION);
//                ExifTag directionTag = exif.buildTag(
//                        ExifInterface.TAG_GPS_IMG_DIRECTION,
//                        new Rational(mHeading, 1));
//                exif.setTag(directionRefTag);
//                exif.setTag(directionTag);
//                if(externalInfos!=null){
//                    String externalJson=CameraUtil.serializeToJson(externalInfos);
//                    ExifTag externalTag=exif.buildTag(ExifInterface.TAG_USER_COMMENT, externalJson);
//                    exif.setTag(externalTag);
//                }
//            }

            Location location = mActivity.getLocationManager().getCurrentLocation();
            getServices().getMediaSaver().addImage(
                    jpegData, title, date, location, width, height,
                    orientation, exif, mOnMediaSavedListener, mActivity.getContentResolver());
        }
    }


    private void stop(boolean isMerge) {
        Log.d(TAG, "[stop]isMerge = " + isMerge + ",modeState=" + getModeState() + ",mIsMerging = "
                + mIsMerging);

        if (STATE_CAPTURING != getModeState()) {
            Log.i(TAG, "[stop] current mode state is not capturing,so return");
            return;
        }
        if (mIsMerging) {
            // if current is in the progress merging,means before have stopped
            // the panorama, so can directly return.
            Log.i(TAG, "[stop] current is also in merging,so cancle this time");
            return;
        } else {
            mIsMerging = isMerge;
            if (!isMerge) {
                /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-27,BUG-2003525*/
                if (mCameraDevice != null) {
                    mCameraDevice.setRamaCallback(mHandler, null);
                }
            } else {
//                mICameraAppUi.showProgress(mActivity.getString(R.string.saving));
//                mICameraAppUi.dismissInfo();
            }
            if (mCameraDevice != null) {
                mCameraDevice.setRamaMoveCallback(mHandler, null);
            }
            /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-06-12,BUG-2153984*/
            mPanoramaMVCallbackEnable = false;
            Log.w(TAG, "stop move callback, mCameraDevice=" + mCameraDevice);
            /* MODIFIED-END by bin.zhang2-nb,BUG-2153984*/
            /* MODIFIED-END by bin.zhang2-nb,BUG-2003525*/
            mHandler.removeMessages(MSG_UPDATE_MOVINE);
            mHandler.removeMessages(MSG_HIDE_VIEW);
            mHandler.sendEmptyMessage(MSG_HIDE_VIEW);
            stopAsync(isMerge);
//            mICameraAppUi.setSwipeEnabled(true);
//            mIModuleCtrl.unlockOrientation();
        }
    }


    private Runnable mOnHardwareStop;
    private Object mLock = new Object();

    private void stopAsync(final boolean isMerge) {
        Log.i(TAG, "[stopAsync]isMerge=" + isMerge + ",mIsInStopProcess = " + mIsInStopProcess);

        if (mIsInStopProcess) {
            return;
        }

        Thread stopThread = new Thread(new Runnable() {
            public void run() {
                doStop(isMerge);
                mOnHardwareStop = new Runnable() {
                    public void run() {
                        if (!isMerge) {
                            // if isMerge is true, onHardwareStopped
                            // will be called in onCapture.
                            onHardwareStopped(false);
                        }
                    }
                };
                mHandler.post(mOnHardwareStop);

                synchronized (mLock) {
                    mIsInStopProcess = false;
                    mLock.notifyAll();
                }
            }
        });
        synchronized (mLock) {
            mIsInStopProcess = true;
        }
        stopThread.start();
    }

    private void doStop(boolean isMerge) {
        Log.d(TAG, "[doStop]isMerge=" + isMerge);
        /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-27,BUG-2003525*/
        if (mCameraDevice != null) {
            mCameraDevice.stopRama(mHandler, isMerge ? 1 : 0);
        }
    }

    private void onHardwareStopped(boolean isMerge) {
        Log.d(TAG, "[onHardwareStopped]isMerge = " + isMerge);
        if (isMerge) {
            if (mCameraDevice != null) {
                mCameraDevice.setRamaCallback(mHandler, null);
            }
            /* MODIFIED-END by bin.zhang2-nb,BUG-2003525*/
        }

        onCaptureDone(isMerge);
    }

    private void onCaptureDone(boolean isMerge) {
        Log.d(TAG, "[onCaptureDone]isMerge = " + isMerge);
        if (isMerge && mJpegImageData != null) {
            mHandler.sendEmptyMessage(MSG_SAVE_FILE);
            mPanoPicSavedDone = false; // MODIFIED by jianying.zhang, 2016-06-14,BUG-2241996
        } else {
            resetCapture();
        }
        setModeState(STATE_IDLE);
    }

    // do the stop sequence carefully in order not to cause driver crash.
    private void safeStop() {
        Log.i(TAG, "[safeStop] check stopAsync thread state, if running,we must wait");
        while (mIsInStopProcess) {
            try {
                synchronized (mLock) {
                    mLock.wait();
                }
            } catch (InterruptedException e) {
                Log.w(TAG, "InterruptedException in waitLock");
            }
        }
    }


    private boolean isAutoExposureLockSupported() {
        Camera.Parameters parameters = mCameraDevice.getCamera().getParameters();
        Method methodIsAutoExposureLockSupported;
        boolean isSupported = false;
        try {
            methodIsAutoExposureLockSupported = parameters.getClass().getMethod("isAutoExposureLockSupported", new Class[] {});
        } catch (Exception e) {
            methodIsAutoExposureLockSupported = null;
        }

        if (methodIsAutoExposureLockSupported != null) {
            try {
                isSupported = (Boolean) methodIsAutoExposureLockSupported.invoke(parameters);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        else {
        }
        return isSupported;
    }

    private boolean isAutoWhiteBalanceLockSupported() {
        Camera.Parameters parameters = mCameraDevice.getCamera().getParameters();
        Method methodIsAutoWhiteBalanceLockSupported;
        boolean isSupported = false;
        try {
            methodIsAutoWhiteBalanceLockSupported = parameters.getClass().getMethod("isAutoWhiteBalanceLockSupported", new Class[] {});
        } catch (Exception e) {
            methodIsAutoWhiteBalanceLockSupported = null;
        }

        if (methodIsAutoWhiteBalanceLockSupported != null) {
            try {
                isSupported = (Boolean) methodIsAutoWhiteBalanceLockSupported.invoke(parameters);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        else {
        }
        return isSupported;
    }

    public void setAutoExposureLock(boolean lock) {
        if (mCameraDevice.getCamera() == null) return;
        if (!isAutoExposureLockSupported()) {
            return;
        }
        Camera.Parameters parameters = mCameraDevice.getCamera().getParameters();
        Method methodSetAutoExposureLock;
        try {
            methodSetAutoExposureLock = parameters.getClass().getMethod("setAutoExposureLock", new Class[] {boolean.class});
        } catch (Exception e) {
            methodSetAutoExposureLock = null;
        }

        if (methodSetAutoExposureLock != null) {
            try {
                methodSetAutoExposureLock.invoke(parameters, lock);
                mCameraDevice.getCamera().setParameters(parameters);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        else {
        }
    }

    public void setAutoWhiteBalanceLock(boolean lock) {
//        if (mCameraDevice.getCamera() == null) return;
//        if (!isAutoWhiteBalanceLockSupported()) {
//            return;
//        }
//        Camera.Parameters parameters = mCameraDevice.getCamera().getParameters();
//        Method methodSetAutoWhiteBalanceLock;
//        try {
//            methodSetAutoWhiteBalanceLock = parameters.getClass().getMethod("setAutoWhiteBalanceLock", new Class[] {boolean.class});
//        } catch (Exception e) {
//            methodSetAutoWhiteBalanceLock = null;
//        }
//
//        if (methodSetAutoWhiteBalanceLock != null) {
//            try {
//                methodSetAutoWhiteBalanceLock.invoke(parameters, lock);
//                mCameraDevice.getCamera().setParameters(parameters);
//            } catch (IllegalArgumentException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }
//        }
//        else {
//        }
    }

    @Override
    protected CameraCapabilities.FocusMode getOverrideFocusMode(){
        //return CameraCapabilities.FocusMode.INFINITY;
        return CameraCapabilities.FocusMode.CONTINUOUS_PICTURE;
    }
    protected boolean hideCamera(){
        return true;
    }
    @Override
    protected boolean isHdrShow() {
        return false;
    }
    @Override
    protected boolean isFlashShow() {
        return false;
    }
    @Override
    public void onShutterButtonLongClick() {
    }
    @Override
    protected int getRemodeShutterIcon() {
        return -1;
    }
}
