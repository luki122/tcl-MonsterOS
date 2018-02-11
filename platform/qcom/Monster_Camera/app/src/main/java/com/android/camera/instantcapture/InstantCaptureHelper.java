package com.android.camera.instantcapture;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.CameraProfile;
import android.media.MediaActionSound;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.view.OrientationEventListener;

import com.android.camera.CameraActivity;
import com.android.camera.Exif;
import com.android.camera.PhotoModule;
import com.android.camera.SecureCameraActivity;
import com.android.camera.app.CameraServices;
import com.android.camera.app.MediaSaver;
import com.android.camera.exif.ExifInterface;
import com.android.camera.exif.ExifTag;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.util.BoostUtil;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.util.ExternalExifInterface;
import com.android.camera.util.PictureSizePerso;
import com.android.ex.camera2.portability.Size;
import com.android.ex.camera2.portability.debug.Log;
import com.android.external.ExtSystemProperties;
import com.android.external.ExtendKey;
import com.android.external.ExtendParameters;
import com.android.external.plantform.ExtBuild;
import com.tct.camera.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
/* MODIFIED-BEGIN by yuanxing.tan, 2016-05-05,BUG-2011611*/
import java.util.Collections;
import java.util.Comparator;
/* MODIFIED-END by yuanxing.tan,BUG-2011611*/
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InstantCaptureHelper {
    private static final Log.Tag TAG = new Log.Tag("InstantHelper");

    private static final String INSTANT_CAPTURE = "instant-capture";
    private static final String INSTANT_CAPTURE_ON = "1";
    private static final String INSTANT_CAPTURE_OFF = "0";
    private static final String INSTANT_AEC = "instant-aec";
    private static final String SNAPSHOT_BURST_NUM = "snapshot-burst-num";
    private static final String ZSL = "zsl";
    private static final String ZSL_ON = "on";
    private static final String CAMERA_STOP_DIAPLAY_MODE = "stop-display";
    private static final int CAMERA_DIAPLAY_MODE_CONTINUE = 2;
    private static final int CAMERA_DIAPLAY_MODE_STOP = 1;
    private static final int CAMERA_ID = Camera.CameraInfo.CAMERA_FACING_BACK;
    private static final int CAMERA_HAL_API_VERSION_1_0 = 0x100;
    /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-24,BUG-1861955 */
    private static int BURST_MAX= 10;//CustomUtil.getInstance().getInt(CustomFields.DEF_CAMERA_BURST_MAX,10);//Upper number of photos received from a single burst-shot
    private int mBurstCount = 0;
    public static boolean USE_JPEG_AS_PICTURE_DISLAY= false;//CustomUtil.getInstance().getBoolean(CustomFields.DEF_SHOW_JPEG_FOR_INSTANT_CAPTURE,true);
    /* MODIFIED-END by yuanxing.tan,BUG-1861955 */
    private static final String VIEW_BACK = "persist.sys.view_back"; // MODIFIED by yongsheng.shan, 2016-04-22,BUG-1967247

    private int mOrientation = 0;
    private int mDisplayRotation = 0;
    private int mJpegRotation = 0;// MODIFIED by yuanxing.tan, 2016-03-21, BUG-1845001
    private boolean mFreezePreview;
    private boolean mDeferUpdateDisplay;
    private Context mContext;
    private InstantCaptureService mService;
    private PhotoModule.NamedImages mNamedImages;
    private MyOrientationListener mOrientationListener;
    private PowerManager mPowerManager;
    private KeyguardManager mKeyguardManager;

    private Method mCameraOpenMethod;
    private Method mDisableSoundMethod;

    private int mPicWidth;
    private int mPicHeight;
    private boolean mShutterSoundOn;

    private volatile SurfaceTexture mSurfaceTexture;

    private static InstantCaptureHelper instantCaptureHelper;

    private Camera  mCamera;
    private Camera.Parameters mParameters;

    private MediaActionSound mMediaActionSound;

    private InstantCaptureHelper() {
    }

    private boolean mInitialized = false;

    public boolean isInitialized() {
        return mInitialized;
    }

     /*MODIFIED-BEGIN by yuanxing.tan, 2016-04-05,BUG-1911947*/
    public boolean needLowBatteryCheck(Context context) {
        boolean needCheck = true;
        if (!CustomUtil.getInstance(context).getBoolean(
                CustomFields.DEF_CAMERA_LOW_BATTERY_FEATURE_INDEPENDENT, false) &&
                !CameraUtil.isBatterySaverEnabled(context)){
            needCheck = false;
        }
        Log.i(TAG, "needLowBatteryCheck "+needCheck);
        return needCheck;
    }
     /*MODIFIED-END by yuanxing.tan,BUG-1911947*/

    public static InstantCaptureHelper getInstance() {
        if (instantCaptureHelper == null)
            instantCaptureHelper = new InstantCaptureHelper();
        return instantCaptureHelper;
    }

    public void init(InstantCaptureService service) {
        mService = service;
        mContext = service.getApplicationContext();
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-24,BUG-1861955 */
        BURST_MAX = CustomUtil.getInstance(mContext).getInt(CustomFields.DEF_CAMERA_BURST_MAX, 10);//Upper number of photos received from a single burst-shot
        USE_JPEG_AS_PICTURE_DISLAY = CustomUtil.getInstance(mContext).getBoolean(CustomFields.DEF_SHOW_JPEG_FOR_INSTANT_CAPTURE, true);// MODIFIED by yuanxing.tan, 2016-03-28,BUG-1861691
        /* MODIFIED-END by yuanxing.tan,BUG-1861955 */

        mOrientationListener = new MyOrientationListener(mContext, SensorManager.SENSOR_DELAY_FASTEST);
        try {
            mCameraOpenMethod = Class.forName("android.hardware.Camera").getMethod("openLegacy", int.class, int.class);
            mDisableSoundMethod = Class.forName("android.hardware.Camera").getMethod("disableShutterSound");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        mMediaActionSound = new MediaActionSound();
        mMediaActionSound.load(MediaActionSound.SHUTTER_CLICK);
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        SettingsManager settingsManager = ((CameraServices) mContext).getSettingsManager();
        Keys.setDefaults(settingsManager, mContext);
        mInitialized = true;
    }

    class MyOrientationListener extends OrientationEventListener {
        public MyOrientationListener(Context context) {
            super(context);
        }
        public MyOrientationListener(Context context, int rate) {
            super(context, rate);
        }
        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != OrientationEventListener.ORIENTATION_UNKNOWN) {
                mOrientation = roundOrientation(orientation, mOrientation);
            }
        }


        public int roundOrientation(int orientation, int orientationHistory) {
            boolean changeOrientation = false;
            if (orientationHistory == -1) {
                changeOrientation = true;
            } else {
                int dist = Math.abs(orientation - orientationHistory);
                if (Math.min(dist, 360 - dist) >= 50) {
                    changeOrientation = true;
                } else {
                    changeOrientation = false;
                }
            }

            if (changeOrientation) {
                orientation = 90 * ((orientation + 45) / 90) % 360;
                return orientation;
            }

            return orientationHistory;
        }
    }
    public boolean gFirstFrame = false;// MODIFIED by yuanxing.tan, 2016-03-21, BUG-1845001
    private void createSurfaceTexture() {
        mSurfaceTexture=new SurfaceTexture(0);
        mSurfaceTexture.detachFromGLContext();
    }

    public void openCamera() throws Exception {
        Log.i(TAG, "instant capture kpi, openCamera");
        mCamera = (Camera) mCameraOpenMethod.invoke(null, CAMERA_ID, CAMERA_HAL_API_VERSION_1_0);
        mDisableSoundMethod.invoke(mCamera);
        Log.i(TAG, "instant capture kpi, has open camera");
        mCamera.setPreviewTexture(mSurfaceTexture);
    }

    private void startPreview() {
        Log.i(TAG, "instant capture kpi, startPreview");
        mCamera.startPreview();
        Log.i(TAG, "instant capture kpi, has start preview");
    }
    private Object mCameraLock = new Object();// MODIFIED by yuanxing.tan, 2016-03-28,BUG-1861691

    public void start() throws Exception {
        Log.w(TAG,"instant capture kpi, Try start instantCaptureHelper");
        mBurstCount = 0;
        mUris.clear();
        mPictureDatas.clear();
        mSurfacetextureAttachedToActivity = false;
        mDisplayRotation = 0;
        mOrientation = 0;
        mCanStartViewImageActivity = false;
         /*MODIFIED-BEGIN by yuanxing.tan, 2016-03-29,BUG-1872723*/
        mNamedImages = new PhotoModule.NamedImages();
        mFreezePreview = false;
        mDeferUpdateDisplay = false;
        mAspectRatio = MATCH_SCREEN; // MODIFIED by yuanxing.tan, 2016-05-05,BUG-2011611
         /*MODIFIED-END by yuanxing.tan,BUG-1872723*/
        createSurfaceTexture();
        BoostUtil.getInstance().acquireCpuLock(); // boost
        acquireCpuWakeLock();
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
        mOrientationListener.enable();
        synchronized (mCameraLock) {
            openCamera();
            if (mViewImageActivityInstance != null) {
                mViewImageActivityInstance.setSurfaceTexture();
            }
            setCameraParameters();
            startPreview();
            gFirstFrame = true;
        }
    }

    /* MODIFIED-END by yuanxing.tan,BUG-1861691 */
    private boolean mSurfacetextureAttachedToActivity = false;
    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }
    public void setSurfaceTextureAttached() {
        mSurfacetextureAttachedToActivity = true;
    }

    private void setCameraParameters() {
        Log.i(TAG, "setCameraParameters");
        if (mCamera == null) {
            Log.i(TAG, "Camera is null.");
            return;
        }
        mParameters = mCamera.getParameters();
        getCameraParametersFromSetting();
        mParameters.set(SNAPSHOT_BURST_NUM, BURST_MAX);
        mParameters.set(ZSL, ZSL_ON);
        mParameters.setRotation(90);
        mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        Log.w(TAG, "setFocusMode for " + mParameters.getFocusMode());
        mParameters.setFlashMode(mContext.getString(R.string.pref_camera_flashmode_off));
        mParameters.set(INSTANT_CAPTURE, INSTANT_CAPTURE_ON);// To enable instant capture
//        mParameters.set(INSTANT_AEC, "1"); // To enable instant aec .// MODIFIED by yuanxing.tan, 2016-03-23,BUG-1855757
        mParameters.set(ExtendKey.VISIDON_MODE, "off");//For MTK: Face beauty is a global setting of Settings App
        ExtendParameters extParams=ExtendParameters.getInstance(mParameters);
        extParams.setZSLMode("on");//For MTK:  open ZSD mode
        mCamera.setParameters(mParameters);
        setDisplayOrientation();// MODIFIED by yuanxing.tan, 2016-03-28,BUG-1861691
    }

    private void getCameraParametersFromSetting() {
        SettingsManager settingsManager = ((CameraServices) mContext).getSettingsManager();
        mShutterSoundOn = Keys.isShutterSoundOn(settingsManager);
        /* MODIFIED-BEGIN by yongsheng.shan, 2016-04-22,BUG-1967247*/
        if(ExtBuild.device() == ExtBuild.MTK_MT6755)
        {
            String view_back = ExtSystemProperties.get(VIEW_BACK);
            if (view_back != null && !view_back.equals("1")) {
                mShutterSoundOn = false;
            }
        }
        /* MODIFIED-END by yongsheng.shan,BUG-1967247*/
        Log.e(TAG, "enable sound:" + mShutterSoundOn);
        String pictureSizeKey = Keys.KEY_PICTURE_SIZE_BACK;
        String defaultPicSize = null;//SettingsUtil.getDefaultPictureSize(false);
        String pictureSize = settingsManager.getString(SettingsManager.SCOPE_GLOBAL,
                pictureSizeKey, defaultPicSize);
        if (pictureSize == null) {
            Log.i(TAG, "pictureSize null, perso init");
            PictureSizePerso perso = PictureSizePerso.getInstance();
            List<Camera.Size> supportedPictureSizes = mParameters.getSupportedPictureSizes();
            List<Size> supportedSizes = new ArrayList<>();
            if (supportedPictureSizes != null) {
                for (Camera.Size s : supportedPictureSizes) {
                    supportedSizes.add(new Size(s.width, s.height));
                }
            }
            perso.init(mContext, supportedSizes, CAMERA_ID);
            pictureSize = settingsManager.getString(SettingsManager.SCOPE_GLOBAL,
                    pictureSizeKey, defaultPicSize);
        }
        Log.i(TAG, "pictureSize from settingsmanager:" + pictureSize);
        if (pictureSize != null) {
            Size size = SettingsUtil.sizeFromString(pictureSize);
            mPicWidth = size.width();
            mPicHeight = size.height();
        } else {
            Camera.Size a = mParameters.getSupportedPictureSizes().get(0);
            mPicWidth = a.width;
            mPicHeight = a.height;
        }
        Log.i(TAG, "final PictureSize = " + mPicWidth + "," + mPicHeight);
        mParameters.setPictureSize(mPicWidth, mPicHeight);
        int jpegQuality = CameraProfile.getJpegEncodingQualityParameter(CAMERA_ID, CameraProfile.QUALITY_HIGH);
        Log.i(TAG, "jpegQuality: " + jpegQuality);
        mParameters.setJpegQuality(jpegQuality);
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-05-05,BUG-2011611*/
        Camera.Size optimalThumbSize=CameraUtil.getOptimalExifSize(mParameters.getSupportedJpegThumbnailSizes(), new Size(mPicWidth, mPicHeight));
        if(optimalThumbSize!=null){
            Log.i(TAG, "jpegThumbSize: " + optimalThumbSize.width+", "+optimalThumbSize.height);
            mParameters.setJpegThumbnailSize(optimalThumbSize.width,optimalThumbSize.height);
        }
        List<Size> sizes = buildPreviewSizes(mParameters);
        Size optimalSize = CameraUtil.getOptimalPreviewSize(mContext, sizes,
                (double) mPicWidth / mPicHeight);
        Log.i(TAG, "previewsize: " + optimalSize);
        mParameters.setPreviewSize(optimalSize.width(), optimalSize.height());
        updatePreviewAspectRatio((float) optimalSize.width()
                / (float) optimalSize.height());
    }
    public static final float MATCH_SCREEN = 0f;
    public float mAspectRatio = MATCH_SCREEN;
    public float getAspectRatio() {
        return mAspectRatio;
    }
    private void updatePreviewAspectRatio(float aspectRatio) {
        if (aspectRatio <= 0) {
            Log.e(TAG, "Invalid aspect ratio: " + aspectRatio);
            return;
        }
        if (aspectRatio < 1f) {
            aspectRatio = 1f / aspectRatio;
        }
        if (mAspectRatio != aspectRatio) {
            mAspectRatio = aspectRatio;
        }
    }
    private ArrayList<Size> buildPreviewSizes(Camera.Parameters p) {
        ArrayList<Size> mSupportedPreviewSizes = new ArrayList<Size>();
        List<Camera.Size> supportedPreviewSizes = p.getSupportedPreviewSizes();
        if (supportedPreviewSizes != null) {
            for (Camera.Size s : supportedPreviewSizes) {
                mSupportedPreviewSizes.add(new Size(s.width, s.height));
            }
        }
        Collections.sort(mSupportedPreviewSizes, new Comparator<Size>() {
            @Override
            public int compare(Size size1, Size size2) {
                return (size1.width() == size2.width() ? size1.height() - size2.height() :
                        size1.width() - size2.width());
            }
        });
        return mSupportedPreviewSizes;
        /* MODIFIED-END by yuanxing.tan,BUG-2011611*/
    }
    public void capture() {
        if (mCamera == null) {
            return;
        }
        Log.i(TAG, "instant capture kpi, capture");
        mNamedImages.nameNewImage(System.currentTimeMillis());
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
        synchronized (mCameraLock) {
            setJpegRotation();
            mCamera.takePicture(new InstantShutterCallback(), null, new InstantPictureCallback());
        }
        mService.getServiceHandler().sendEmptyMessageDelayed(InstantCaptureService.CAPTURE_TIMEOUT, InstantCaptureService.CAPTURE_TIMEOUT_DELAY);
    }

    public void changeDisplayOrientation(int orientation) {
        if (USE_JPEG_AS_PICTURE_DISLAY || mDisplayRotation == orientation) {
            return;
        }
        mDisplayRotation = orientation;
        mService.getServiceHandler().sendEmptyMessage(InstantCaptureService.SET_DISPLAY_ORIENTATION);
    }

    public void setDisplayOrientation() {
         /*MODIFIED-BEGIN by yuanxing.tan, 2016-03-29,BUG-1872723*/
        if (isCaptureDone() && mViewImageActivityInstance != null) {
            mViewImageActivityInstance.showResultImageView(true);
            return;
        } else if (mFreezePreview) {
            Log.i(TAG, "setDisplayOrientation after stop display,no need");
            mDeferUpdateDisplay = true;
            return;
        }
         /*MODIFIED-END by yuanxing.tan,BUG-1872723*/
        if (USE_JPEG_AS_PICTURE_DISLAY || mCamera == null) {
            return;
        }
        int rotation = 0;
        switch (mDisplayRotation) {
        /* MODIFIED-END by yuanxing.tan,BUG-1861691 */
            case 0:
                rotation = 90;
                break;
            case 1:
                rotation = 0;
                break;
            case 2:
                rotation = 270;
                break;
            case 3:
                rotation = 180;
                break;
        }
        Log.i(TAG, "change display orientation to " + rotation);
        synchronized (mCameraLock) {
            try {
                if (mCamera != null) {
                    mCamera.setDisplayOrientation(rotation);
                }
            } catch (Exception e) {
                Log.e(TAG, "changeDisplayOrientation fail ",e);
            }
        }
    }

    /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
    public void setJpegRotation() {
        synchronized (mCameraLock) {
            try {
                if (mCamera == null) {
                    return;
                }
                mParameters = mCamera.getParameters();
                int tmp = mOrientation + 90;
                if (tmp >= 360)
                    tmp -= 360;
                mParameters.setRotation(tmp);
                mJpegRotation = tmp;
                Log.i(TAG, "picture rotation = " + tmp);
                mCamera.setParameters(mParameters);
            } catch (Exception e) {
                Log.e(TAG, "setJpegRotation fail ",e);
            }
        }
    }

    public void stop() {
        Log.i(TAG, "instant capture kpi, stop");
        mService.getMainHandler().removeMessages(InstantCaptureService.NO_DATA_RETURN_ERROR);
        mService.getServiceHandler().removeMessages(InstantCaptureService.CAPTURE_TIMEOUT);
        mOrientationListener.disable();
        mNamedImages = null; //MODIFIED by yuanxing.tan, 2016-03-29,BUG-1872723
        if (mCamera == null) {
            if (!mSurfacetextureAttachedToActivity && mSurfaceTexture != null) {
                mSurfaceTexture.release();
            }
            mSurfaceTexture = null;
            BoostUtil.getInstance().releaseCpuLock();
            releaseCpuWakeLock();
            return;
        }
        synchronized (mCameraLock) {
            try {
                mParameters = mCamera.getParameters();
                mParameters.set(CAMERA_STOP_DIAPLAY_MODE, CAMERA_DIAPLAY_MODE_STOP); //1:stop display; 2:continue display
                mParameters.set(SNAPSHOT_BURST_NUM, 0);
                mCamera.setParameters(mParameters);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                if (mCamera != null) {
                    Log.i(TAG, "Release camera since mCamera is not null.");
                    try {
                        mCamera.release();
                    } catch (Exception ex) {
                        Log.e(TAG, "Fail when calling Camera.release().", e);
                    } finally {
                        mCamera = null;
                    }
                }
            } finally {
                CameraLock.getInstance().open();
            }

        }
        if (!mSurfacetextureAttachedToActivity && mSurfaceTexture != null) {
            mSurfaceTexture.release();
        }
        mSurfaceTexture = null;

        BoostUtil.getInstance().releaseCpuLock();

        releaseCpuWakeLock();
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-06-18,BUG-2379660*/
        // for jpeg timeout case,make sure burst icon show
        if (mListener != null) {
            mListener.onUiUpdated(mBurstCount);
        }
        /* MODIFIED-END by yuanxing.tan,BUG-2379660*/
        Log.i(TAG, "stop end");
    }

    public void destroy() {
        mMediaActionSound.release();
        mInitialized = false;
        mService = null;
    }

    public boolean isSingleShot() {
        return mService.isSingleShot();
    }

    public boolean isCaptureDone() {
        return mService.isCaptureDone();
    }

    public boolean hasSaveDone() {
        return isCaptureDone() && getPictureDatas().size() > 0 && getPictureDatas().size() == getPictureUris().size();
    }

    class InstantShutterCallback implements Camera.ShutterCallback {
        @Override
        public void onShutter() {
            Log.i(TAG, "instant capture kpi, onShutter " + mShutterSoundOn);
            if (mShutterSoundOn) {
                mMediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
            }
            updateInstantCaptureAndStopDisplay(true, !USE_JPEG_AS_PICTURE_DISLAY && isSingleShot());
        }
    }

    public void updateInstantCaptureAndStopDisplay(boolean cancelInstantCapture, boolean stopDisplay) {
        Log.i(TAG, "updateInstantCaptureAndStopDisplay");
        synchronized (mCameraLock){
            try{
                if (mCamera == null) {
                    return;
                }
                mParameters = mCamera.getParameters();
                if (cancelInstantCapture) {
                    mParameters.set(INSTANT_CAPTURE, INSTANT_CAPTURE_OFF);// To disable instant capture
//                    mParameters.set(INSTANT_AEC, "0"); // To disable instant aec .
                }
                if (stopDisplay) {
                    mFreezePreview = true; //MODIFIED by yuanxing.tan, 2016-03-29,BUG-1872723
                    mParameters.set(CAMERA_STOP_DIAPLAY_MODE, CAMERA_DIAPLAY_MODE_STOP); //1:stop display; 2:continue display
                }
                mCamera.setParameters(mParameters);
            } catch (Exception e) {
            /* MODIFIED-END by yuanxing.tan,BUG-1861691 */
            }
        }
    }

    public int getBurstCount() {
        return mBurstCount;
    }

    public ArrayList<Uri> getPictureUris() {
        return mUris;
    }

    public ArrayList<byte[]> getPictureDatas() {
        return mPictureDatas;
    }
    private ArrayList<Uri> mUris = new ArrayList<>();

    public boolean isScreenOn() {
        return mPowerManager.isScreenOn();
    }

    public void wakeUpScreen() {
        Log.i(TAG, "wakeUpScreen before");
        if (!isScreenOn()) {
            Log.i(TAG, "wakeUpScreen");
            PowerManager.WakeLock screenWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, "screenwakelock");
            screenWakeLock.acquire(500L);
            screenWakeLock.setReferenceCounted(false);
        }
    }
    private PowerManager.WakeLock mCPUWakeLock;
    public void acquireCpuWakeLock() {
        if (mCPUWakeLock == null) {
            mCPUWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "cpuwakelock");
            mCPUWakeLock.acquire();
            mCPUWakeLock.setReferenceCounted(false);
        }
    }

    public void releaseCpuWakeLock() {
        if (mCPUWakeLock != null) {
            mCPUWakeLock.release();
            mCPUWakeLock = null;
        }
    }

    private ArrayList<byte[]> mPictureDatas = new ArrayList<>();

    private InstantViewImageActivity.OnUiUpdateListener mListener;
    private InstantViewImageActivity mViewImageActivityInstance;

    public void registerOnUiUpdateListener(InstantViewImageActivity.OnUiUpdateListener listener, InstantViewImageActivity instance) {
        mListener = listener;
        mViewImageActivityInstance = instance;
    }

    public void unRegisterOnUiUpdateListener() {
        mListener = null;
        mViewImageActivityInstance = null;
    }

    /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
    public boolean isInCaptureProgress() {
        return mService.checkInCaptureProgress();
    }

    class InstantPictureCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mService.getServiceHandler().removeMessages(InstantCaptureService.CAPTURE_TIMEOUT);
            mService.getServiceHandler().removeMessages(InstantCaptureService.NO_DATA_RETURN_ERROR);
            if (!isInCaptureProgress()) {
                Log.i(TAG, "onPictureTaken, state invalid ");
                return;
            }

            if (mService.checkCameraState(InstantCaptureService.CaptureState.CAMERA_SNAPSHOT_LONGSHOT_PENDING_STOP) && mBurstCount > 0) {
                mService.changeCameraState(InstantCaptureService.CaptureState.CAMERA_PENDING_STOP);
                 /*MODIFIED-BEGIN by yuanxing.tan, 2016-03-29,BUG-1872723*/
                if (mListener != null) {
                    mListener.onUiUpdated(mBurstCount);
                }
                return;
            }
            mPictureDatas.add(data);
            /* MODIFIED-BEGIN by yuanxing.tan, 2016-05-05,BUG-2051734*/
            if ((USE_JPEG_AS_PICTURE_DISLAY || !mSurfacetextureAttachedToActivity || mDeferUpdateDisplay || gFirstFrame) && mViewImageActivityInstance != null && !mViewImageActivityInstance.isFinishing()) {
                gFirstFrame = false;
                /* MODIFIED-END by yuanxing.tan,BUG-2051734*/
                Log.i(TAG, "update picture");
                 /*MODIFIED-END by yuanxing.tan,BUG-1872723*/
                mViewImageActivityInstance.showResultImageView(false);
            }
            if (mService.checkCameraState(InstantCaptureService.CaptureState.CAMERA_SNAPSHOT_IN_PROGRESS)) {
                Log.i(TAG, "instant capture kpi, onPictureTaken for single capture");
                mService.changeCameraState(InstantCaptureService.CaptureState.CAMERA_PENDING_STOP);
                saveFinalPhoto(data, null);
                mService.getMainHandler().sendEmptyMessage(InstantCaptureService.PICTURE_CAPTURED_MSG);
            } else if (mService.checkCameraState(InstantCaptureService.CaptureState.CAMERA_SNAPSHOT_LONGSHOT)
                    || mService.checkCameraState(InstantCaptureService.CaptureState.CAMERA_SNAPSHOT_LONGSHOT_PENDING_STOP)) {
                if (mShutterSoundOn && mBurstCount != 0) {
                    mMediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
                    Log.i(TAG, "Playing sound");
                }
                Log.i(TAG, "instant capture kpi, onPictureTaken for burst capture " + mBurstCount);
                /* MODIFIED-END by yuanxing.tan,BUG-1861691 */
                final Map<String, Object> externalBundle = new HashMap<>();
                externalBundle.put(ExternalExifInterface.BURST_SHOT_ID, InstantPictureCallback.this.hashCode());
                externalBundle.put(ExternalExifInterface.BURST_SHOT_INDEX, mBurstCount++);
                if (mListener != null) {
                    mListener.onUiUpdating(mBurstCount);
                }
                saveFinalPhoto(data, externalBundle);
                /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
                if (mService.checkCameraState(InstantCaptureService.CaptureState.CAMERA_SNAPSHOT_LONGSHOT_PENDING_STOP) || mBurstCount >= BURST_MAX) {
                    mService.changeCameraState(InstantCaptureService.CaptureState.CAMERA_PENDING_STOP);
                    return;
                }
                setJpegRotation();
                mService.getServiceHandler().sendEmptyMessageDelayed(InstantCaptureService.NO_DATA_RETURN_ERROR, InstantCaptureService.NO_DATA_RETURN_TIMEOUT);
            } else {
                Log.i(TAG, "invalid onPictureTaken, skip");
                /* MODIFIED-END by yuanxing.tan,BUG-1861691 */
            }
        }
    }

    void saveFinalPhoto(final byte[] jpegData, Map<String,Object> externalInfos) {
        final ExifInterface exif = Exif.getExif(jpegData);
        final PhotoModule.NamedImages.NamedEntity name = mNamedImages.getNextNameEntity();

        if (externalInfos != null) {
            String externalJson = CameraUtil.serializeToJson(externalInfos);
            Log.i(TAG, "saving burst shot info:"+externalJson);
            ExifTag externalTag = exif.buildTag(ExifInterface.TAG_USER_COMMENT, externalJson);
            exif.setTag(externalTag);
        }

        int orientation = Exif.getOrientation(exif);
        String title = (name == null) ? null : name.title;
        long date = (name == null) ? -1 : name.date;
        int width = mPicWidth;
        int height = mPicHeight;
        if ((mJpegRotation + orientation) % 180 != 0) {
            width = mPicHeight;
            height = mPicWidth;
        }
        ((CameraServices) mContext).getMediaSaver().addImage(
                jpegData, title, date, null, width, height,
                orientation, exif, mOnMediaSavedListener, mContext.getContentResolver());
    }

    private MediaSaver.OnMediaSavedListener mOnMediaSavedListener = new MediaSaver.OnMediaSavedListener() {
        @Override
        public void onMediaSaved(final Uri uri) {
            if (uri != null) {
                CameraUtil.broadcastNewPicture(mContext, uri);
                mUris.add(uri);
                Log.i(TAG, "onMediaSaved "+uri);
                if ((mBurstCount > 0 && mBurstCount == mUris.size()) || mBurstCount == 0){
                    if (mListener != null) {
                        mListener.onUiUpdated(mBurstCount);
                    }
                }
            }
        }
    };
    private boolean mCanStartViewImageActivity = false;

    public void setForbidStartViewImageActivity(boolean forbidStartViewImageActivity) {
        mCanStartViewImageActivity = forbidStartViewImageActivity;
    }

    public boolean getForbidStartViewImageActivity() {
        return mCanStartViewImageActivity;
    }
    public void startCameraActivity(Context context, ArrayList<Uri> uris) {
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-28,BUG-1861691 */
        mService.getMainHandler().removeMessages(InstantCaptureService.ACTIVITY_LAUNCH_PROTECT);
        mService.getMainHandler().sendEmptyMessageDelayed(InstantCaptureService.ACTIVITY_LAUNCH_PROTECT, InstantCaptureService.ACTIVITY_LAUNCH_PROTECT_TIMEOUT);
        /* MODIFIED-END by yuanxing.tan,BUG-1861691 */
        Intent intent;
        if (!(mKeyguardManager.isKeyguardSecure() && mKeyguardManager.isKeyguardLocked())) {
            dismissKeyguard();
            intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
            intent.setClass(context, CameraActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Log.i(TAG, "start CameraActivity");
        } else {
            intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
            intent.setClass(context, SecureCameraActivity.class);
            if (uris != null && uris.size() > 0)  {
                intent.putParcelableArrayListExtra("uris", uris);
            }
            Log.i(TAG, "start SecureCameraActivity "+uris);
        }
        intent.addFlags(/*Intent.FLAG_ACTIVITY_CLEAR_TASK | */Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void dismissKeyguard() {
        if(!mKeyguardManager.isKeyguardSecure() && mKeyguardManager.isKeyguardLocked()){
            try {
                Class ServiceManager = Class.forName("android.os.ServiceManager");
                Method getService = ServiceManager.getMethod("getService", String.class);
                Object oRemoteService = getService.invoke(null, Context.WINDOW_SERVICE);
                Class cStub = Class.forName("android.view.IWindowManager$Stub");
                Method asInterface = cStub.getMethod("asInterface", IBinder.class);
                Object oIWindowManager = asInterface.invoke(null, oRemoteService);
                Method DismissKeyguard = oIWindowManager.getClass().getMethod("dismissKeyguard");
                DismissKeyguard.invoke(oIWindowManager);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void startViewImageActivity(Context context){
        Log.i(TAG, "startViewImageActivity");
        Intent cameraIntent = new Intent();
        cameraIntent.setClass(context, InstantViewImageActivity.class);
        cameraIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NEW_TASK/* | Intent.FLAG_ACTIVITY_CLEAR_TASK*/| Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        context.startActivity(cameraIntent);
    }
    public void dismissViewImageActivity(){
        Log.i(TAG, "dismissViewImageActivity");
        if (mViewImageActivityInstance != null) {
            mViewImageActivityInstance.finish();
        }
    }
}
