package com.android.camera.rapidcapture;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.location.Location;
import android.media.CameraProfile;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.OrientationEventListener;

import com.android.camera.Exif;
import com.android.camera.PhotoModule;
import com.android.camera.app.CameraController;
import com.android.camera.app.CameraServices;
import com.android.camera.app.LocationManager;
import com.android.camera.app.MediaSaver;
import com.android.camera.app.OrientationManager;
import com.android.camera.app.OrientationManagerImpl;
import com.android.camera.exif.ExifInterface;
import com.android.camera.exif.ExifTag;
import com.android.camera.settings.CameraPictureSizesCacher;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.ExternalExifInterface;
import com.android.camera.util.GservicesHelper;
import com.android.ex.camera2.portability.CameraAgent;
import com.android.ex.camera2.portability.CameraAgentFactory;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.ex.camera2.portability.CameraDeviceInfo;
import com.android.ex.camera2.portability.CameraExceptionHandler;
import com.android.ex.camera2.portability.CameraSettings;
import com.android.ex.camera2.portability.Size;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RapidCaptureHelper implements CameraAgent.CameraOpenCallback, OrientationManager.OnOrientationChangeListener{
    private static final String TAG = "RapidCaptureHelper";
    public static int TYPE_INVALID = 0;
    public static int TYPE_ONESHOT = 1;
    public static int TYPE_BURSTSHOT = 2;
    private int mType;
    public static final String ACTION_STOP_BURSTSHOT = "com.tct.camera.stopBurstshot";
    private OrientationManagerImpl mOrientationManager;
    private LocationManager mLocationManager;
    private Context mAppContext;
    private CameraController mCameraController;
    private CameraAgent.CameraProxy mCameraDevice;
    protected CameraCapabilities mCameraCapabilities;
    private CameraSettings mCameraSettings;
    private SettingsManager mSettingsManager;
    private int mCameraId;

    private Application mApplication;
    private int mOrientation = OrientationEventListener.ORIENTATION_UNKNOWN;
    private int mJpegRotation;

    private long mCaptureStartTime;
    private long mShutterCallbackTime;
    private long mOneshotStartTime;
    private long mOneshotendTime;
    private long mFirtstFrameTime;
    private long mAecsettledTime;
    private long mopenStartTime;
    private long mopenEndTime;
    private long mpreviewStartTime;
    private long mpreviewEndTime;
    private long mJpegPictureCallbackTime;


    private PhotoModule.NamedImages mNamedImages;
    private Callback mServiceCallback;

    private static final int MSG_AEC_SETTLE_TIMEOUT = 1;
    private static final int MSG_REMOVE_SCREEN_WAKELOCK = 2;
    private static final int MSG_ONESHOT_TIMEOUT = 3;
    private static final int MSG_BURSTSHOT_TIMEOUT = 4;
    private static final long AEC_SETTLE_TIMEOUT = 1000;
    private static final long SCREEN_WAKELOCK_TIMEOUT = 3000;
    private static final long ONESHOT_TIMEOUT = 10*1000;
    private static final long BURSTSHOT_TIMEOUT = 60*1000;
    boolean mCaptureStarted = false;
    boolean mFirstFrameReceived = false;
    private int mFrameCount;
    private Handler mMainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AEC_SETTLE_TIMEOUT:
                    Log.d(TAG, "MSG_AEC_SETTLE_TIMEOUT");
                    if (mCameraDevice == null) {
                        Log.d(TAG, "MSG_AEC_SETTLE_TIMEOUT and fail with timeout");
                        return;
                    }
                    mCameraDevice.setPreviewDataCallback(mMainHandler, null);
                    capture();
                    break;
                case MSG_REMOVE_SCREEN_WAKELOCK:
                    Log.d(TAG, "MSG_REMOVE_SCREEN_WAKELOCK");
                    releaseScreenWakeLock();
                    break;
                case MSG_ONESHOT_TIMEOUT:
                    if (mCameraDevice != null) {
                        mCameraDevice.setPreviewDataCallback(mMainHandler, null);
                    }
                    Log.d(TAG, "MSG_ONESHOT_TIMEOUT");
                    onCaptureDone(null);
                    break;
                case MSG_BURSTSHOT_TIMEOUT:
                    Log.d(TAG, "MSG_BURSTSHOT_TIMEOUT");
                    if (mCameraDevice != null) {
                        mCameraDevice.setPreviewDataCallback(mMainHandler, null);
                    }
                    onCaptureDone(null);
                    break;
            }
        }
    };
    private boolean mCameraPreviewParamsReady = false;

    private static RapidCaptureHelper sInstance;
    private SurfaceTexture mSurface;

    private static final int BURST_MAX=10;//Upper number of photos received from a single burst-shot
    private static final int BURST_DELAY=0;
    private CameraExceptionHandler mCameraExceptionHandler;
    private boolean mInitialized;

    private RapidCaptureHelper() {
    }

    public static RapidCaptureHelper getInstance() {
        if (sInstance == null) {
            sInstance = new RapidCaptureHelper();
        }
        return sInstance;
    }

    public void init(Application application, Callback cb) {
        if (mInitialized) {
            return;
        }
        mInitialized = true;
        mApplication = application;
        mAppContext = mApplication.getBaseContext();
        mSettingsManager = getServices().getSettingsManager();

        mLocationManager = new LocationManager(mAppContext);

        mOrientationManager = new OrientationManagerImpl(mAppContext);
        mOrientationManager.addOnOrientationChangeListener(mMainHandler, this);

        mNamedImages = new PhotoModule.NamedImages();

        mCameraController = new CameraController(mAppContext, this, mMainHandler,
                CameraAgentFactory.getAndroidCameraAgent(mAppContext,
                        CameraAgentFactory.CameraApi.API_1),
                CameraAgentFactory.getAndroidCameraAgent(mAppContext,
                        GservicesHelper.useCamera2ApiThroughPortabilityLayer(mAppContext)?
                                CameraAgentFactory.CameraApi.AUTO:
                                CameraAgentFactory.CameraApi.API_1));
        mCameraExceptionHandler = new CameraExceptionHandler(mCameraExceptionCallback, mMainHandler);
        mCameraId = mCameraController.getFirstBackCameraId();
        mSurface=new SurfaceTexture(0);
        mSurface.detachFromGLContext();
        mServiceCallback = cb;
    }

    public void resume(int type) {
        Log.i(TAG, "resume");
        if (type == TYPE_ONESHOT) {
            mMainHandler.sendEmptyMessageDelayed(MSG_ONESHOT_TIMEOUT, ONESHOT_TIMEOUT);
        } else if (type == TYPE_BURSTSHOT) {
            mMainHandler.sendEmptyMessageDelayed(MSG_BURSTSHOT_TIMEOUT, BURSTSHOT_TIMEOUT);
        }
        releaseScreenWakeLock();
        mOneshotStartTime = System.currentTimeMillis();
        acquireCpuLock(mAppContext);
        mCameraController.setCameraExceptionHandler(mCameraExceptionHandler);
        syncLocationManagerSetting();
        mOrientationManager.resume();
        mType = type;
        requestBackCamera();
    }

    public void pause() {
        if (mType == TYPE_ONESHOT) {
            mMainHandler.removeMessages(MSG_ONESHOT_TIMEOUT);
        } else if (mType == TYPE_BURSTSHOT) {
            mMainHandler.removeMessages(MSG_BURSTSHOT_TIMEOUT);
        }
        mOrientationManager.pause();
        mLocationManager.disconnect();
        pauseLocationManager();
        mCameraController.releaseCamera(mCameraId);
        if (mCameraDevice != null) {
            mCameraDevice.stopPreview();
            mCameraController.closeCamera(true);
        }
        mCameraController.setCameraExceptionHandler(null);
        mCameraDevice = null;
        mType = TYPE_INVALID;
        mCaptureStarted = false;
        releaseCpuLock();
        mOneshotendTime = System.currentTimeMillis();
        Log.i(TAG, "pause end");
        if (mCameraFatalError) {
            mCameraFatalError = false;
            destroy();
        }
    }

    public void destroy() {
        Log.i(TAG, "destroy");
        releaseScreenWakeLock();
        mNamedImages = null;
        mServiceCallback = null;
        mCameraController.removeCallbackReceiver();
        mCameraController = null;
        CameraAgentFactory.recycle(CameraAgentFactory.CameraApi.API_1);
        CameraAgentFactory.recycle(GservicesHelper.useCamera2ApiThroughPortabilityLayer(mAppContext) ?
                CameraAgentFactory.CameraApi.AUTO :
                CameraAgentFactory.CameraApi.API_1);
        mSurface.release();
        sInstance = null;
    }

    private CameraServices getServices() {
        return (CameraServices) mApplication;
    }

    private void requestBackCamera() {
        mopenStartTime = System.currentTimeMillis();
        Log.v(TAG, "requestBackCamera" + mCameraId + ", " + mType);
        if (mCameraId != -1) {
            mCameraController.requestCamera(mCameraId,
                    GservicesHelper.useCamera2ApiThroughPortabilityLayer(mAppContext));
        }
    }

    @Override
    public void onCameraOpened(CameraAgent.CameraProxy camera) {
        Log.v(TAG, "onCameraOpened");
        mopenEndTime = System.currentTimeMillis();
        mCameraDevice = camera;
        mCameraSettings = mCameraDevice.getSettings();
        initializeCapabilities();

        setCameraParameters();
        mCameraPreviewParamsReady = true;
        startPreview();
    }

    @Override
    public void onCameraOpenedBoost(CameraAgent.CameraProxy camera) {
        //dummy
    }

    @Override
    public boolean isBoostPreview() {
        return false;
    }

    @Override
    public Context getCallbackContext() {
        return null;
    }

    @Override
    public CameraSettings.BoostParameters getBoostParam() {
        return null;//dummpy , interface required;
    }

    public void startPreview() {
        Log.v(TAG, "startPreview "+mType);
        if (mCameraDevice == null || !mCameraPreviewParamsReady) {
            Log.v(TAG, "startPreview mCameraDevice null,start later");
            return;
        }
        mpreviewStartTime = System.currentTimeMillis();
        setupPreviewListener();
        mCameraDevice.setPreviewTexture(mSurface);
        CameraAgent.CameraStartPreviewCallback startPreviewCallback =
                new CameraAgent.CameraStartPreviewCallback() {
                    @Override
                    public void onPreviewStarted() {
                        RapidCaptureHelper.this.onPreviewStarted();
                    }
                };
        if (GservicesHelper.useCamera2ApiThroughPortabilityLayer(mAppContext)) {
            mCameraDevice.fakeStartPreview();
            startPreviewCallback.onPreviewStarted();
        } else {
            mCameraDevice.startPreviewWithCallback(new Handler(Looper.getMainLooper()),
                    startPreviewCallback);
        }
    }

    public void setupPreviewListener() {
        if (mCameraDevice == null) {
            return;
        }
        mFirstFrameReceived = false;
        mFrameCount = 0;
        mCameraDevice.setPreviewDataCallback(mMainHandler,
                new CameraAgent.CameraPreviewDataCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, CameraAgent.CameraProxy camera) {
                        if (!mFirstFrameReceived) {
                            mFirstFrameReceived = true;
                            mFirtstFrameTime = System.currentTimeMillis();
                        }
                        mFrameCount++;
                        mCameraDevice.refreshSettings();
                        mCameraSettings = mCameraDevice.getSettings();
                        int aec = mCameraSettings.getAec();
                        Log.d(TAG, "CameraPreviewDataCallback aec:" + aec);
                        if (mCameraSettings.getAec() == 1) {
                            mAecsettledTime = System.currentTimeMillis();
                            mCameraDevice.setPreviewDataCallback(mMainHandler, null);
                            mMainHandler.removeMessages(MSG_AEC_SETTLE_TIMEOUT);
                            capture();
                        }
                    }
                }
        );
    }

    private LongshotPictureCallback mLongshotPictureTakenCallback;
    private void capture() {
        if (mCaptureStarted) {
            return;
        }
        mCaptureStarted = true;
        mCaptureStartTime = System.currentTimeMillis();
        Log.v(TAG, "capture");
        CameraDeviceInfo.Characteristics info = mCameraController.getCharacteristics(mCameraId);
        mJpegRotation = info.getJpegOrientation(mOrientation);
        Log.v(TAG, "capture orientation  " + mOrientation + ",  " + mJpegRotation);
        mCameraDevice.setJpegOrientation(mJpegRotation);
        mCameraDevice.enableShutterSound(Keys.isShutterSoundOn(mSettingsManager));
        Location loc = mLocationManager.getCurrentLocation();
        if (mType == TYPE_ONESHOT) {
            mCameraDevice.takePicture(mMainHandler,
                    mShutterCallback,
                    null, null,
                    new JpegPictureCallback(loc));
        } else {
            mSnapshotBurstNum++;
            mLongshotPictureTakenCallback=new LongshotPictureCallback(loc);
            mCameraDevice.burstShot(null, null, null, null, null);
            mCameraDevice.takePicture(mMainHandler,
                    mLongshotShutterCallback,
                    null, null,
                    mLongshotPictureTakenCallback);
        }
        mNamedImages.nameNewImage(mCaptureStartTime);

    }

    private void onPreviewStarted() {
        mpreviewEndTime = System.currentTimeMillis();
        Log.v(TAG, "onPreviewStarted"+mCaptureStarted);
        if (!mCaptureStarted) {
            mMainHandler.sendEmptyMessageDelayed(MSG_AEC_SETTLE_TIMEOUT, AEC_SETTLE_TIMEOUT);
        }
//        capture();
    }

    public void stopBurst(){
        Log.d(TAG, "stop burst shot "+mType);
        if (mType == TYPE_BURSTSHOT) {
            mBurstshotBreak = true;
        }
    }

    private ShutterCallback mShutterCallback = new ShutterCallback(false);
    private ShutterCallback mLongshotShutterCallback = new ShutterCallback(true);
    private boolean mBurstshotBreak=false;
    private int mSnapshotBurstNum=0;
    private ArrayList<Uri> mBurstShotUris;

    private final class ShutterCallback
            implements CameraAgent.CameraShutterCallback {
        private boolean isFromLongshot = false;

        public ShutterCallback(boolean fromLongshot) {
            isFromLongshot = fromLongshot;
        }

        @Override
        public void onShutter(CameraAgent.CameraProxy camera) {
            mShutterCallbackTime = System.currentTimeMillis();
            Log.i(TAG, "onShutter " + isFromLongshot + ", " + mBurstshotBreak);
            if (isFromLongshot) {
                mNamedImages.nameNewImage(mShutterCallbackTime);
                mMainHandler.postDelayed(burstshot, BURST_DELAY);
            }
        }
    }
    private Runnable burstshot = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "onShutter " +mBurstshotBreak);
            if (mBurstshotBreak || mSnapshotBurstNum >= BURST_MAX) {
                mBurstshotBreak = true;
                if (mBurstShotUris != null && mBurstShotUris.size() == mSnapshotBurstNum) {
                    onCaptureDone(mBurstShotUris.get(mBurstShotUris.size() - 1));
                }
                return;
            }
            mSnapshotBurstNum++;

            mCameraDevice.takePicture(mMainHandler,
                    mLongshotShutterCallback,
                    null, null,
                    mLongshotPictureTakenCallback);
        }
    };

    private final class LongshotPictureCallback implements CameraAgent.CameraPictureCallback {

        Location mLocation;
        private short mLongshotCount = 0;

        public LongshotPictureCallback(Location loc) {
            mLocation = loc;
        }

        @Override
        public void onPictureTaken(byte[] originalJpegData, final CameraAgent.CameraProxy camera) {
            Log.d(TAG, "onPictureTaken"+mBurstshotBreak);

            final ExifInterface exif = Exif.getExif(originalJpegData);
            final PhotoModule.NamedImages.NamedEntity name = mNamedImages.getNextNameEntity();

            final Map<String,Object> externalBundle=new HashMap<>();
            externalBundle.put(ExternalExifInterface.BURST_SHOT_ID, LongshotPictureCallback.this.hashCode());
            externalBundle.put(ExternalExifInterface.BURST_SHOT_INDEX, mLongshotCount++);
            saveFinalPhoto(originalJpegData, name, exif, camera, mLocation, externalBundle);
        }
    }

    private final class JpegPictureCallback
            implements CameraAgent.CameraPictureCallback {
        Location mLocation;

        public JpegPictureCallback(Location loc) {
            mLocation = loc;
        }

        @Override
        public void onPictureTaken(final byte[] originalJpegData, final CameraAgent.CameraProxy camera) {
            Log.i(TAG, "jpegCallback onPictureTaken");
            mJpegPictureCallbackTime = System.currentTimeMillis();
            final ExifInterface exif = Exif.getExif(originalJpegData);
            final PhotoModule.NamedImages.NamedEntity name = mNamedImages.getNextNameEntity();

            saveFinalPhoto(originalJpegData, name, exif, camera, mLocation, null);
        }
    }

    void saveFinalPhoto(final byte[] jpegData, PhotoModule.NamedImages.NamedEntity name,
                        final ExifInterface exif,
                        CameraAgent.CameraProxy camera, Location loc, Map<String,Object> externalInfos) {
        int orientation = Exif.getOrientation(exif);
        String title = (name == null) ? null : name.title;
        long date = (name == null) ? -1 : name.date;
        Size s = mCameraSettings.getCurrentPhotoSize();
        int width, height;
        if ((mJpegRotation + orientation) % 180 == 0) {
            width = s.width();
            height = s.height();
        } else {
            width = s.height();
            height = s.width();
        }
        if(externalInfos!=null){
            String externalJson=CameraUtil.serializeToJson(externalInfos);
            ExifTag externalTag=exif.buildTag(ExifInterface.TAG_USER_COMMENT, externalJson);
            exif.setTag(externalTag);
        }
        getServices().getMediaSaver().addImage(
                jpegData, title, date, loc, width, height,
                orientation, exif, mOnMediaSavedListener, mAppContext.getContentResolver());
    }

    private final MediaSaver.OnMediaSavedListener mOnMediaSavedListener =
            new MediaSaver.OnMediaSavedListener() {
                @Override
                public void onMediaSaved(Uri uri) {
                    Log.i(TAG, "onMediaSaved  " +uri+", "+mCaptureStarted);
                    if (!mCaptureStarted) {
                        return;
                    }
                    if (uri != null) {
                        CameraUtil.broadcastNewPicture(mAppContext, uri);
                    }
                    if (mType == TYPE_ONESHOT) {
                        onCaptureDone(uri);
                    } else if (mType == TYPE_BURSTSHOT){
                        if (mBurstShotUris == null) {
                            mBurstShotUris = new ArrayList<>();
                        }
                        mBurstShotUris.add(uri);
                        Log.i(TAG, "onMediaSaved  " + mBurstshotBreak +","+mBurstShotUris.size()+",  "+mSnapshotBurstNum);
                        if (mBurstshotBreak && mBurstShotUris.size() == mSnapshotBurstNum) {
                            mCameraDevice.abortBurstShot();
                            onCaptureDone(uri);
                        }
                    }
                }
            };

    private void onCaptureDone(Uri uri) {
        Log.i(TAG, "onCaptureDone  "+uri+", "+mType);
        mOneshotendTime = System.currentTimeMillis();
        if (uri != null) {
            if (mType == TYPE_ONESHOT) {
                startViewImageActivity(uri);
            } else if (mType == TYPE_BURSTSHOT){
                startGallery(uri);
            }
//            acquireScreenWakeLock(mAppContext);
        }
        mMainHandler.removeCallbacks(burstshot);
        if (mBurstShotUris != null) {
            mBurstShotUris.clear();
            mBurstShotUris = null;
        }
        mSnapshotBurstNum = 0;
        mBurstshotBreak = false;
        if (mServiceCallback != null) {
            mServiceCallback.onCaptureDone(mCameraFatalError);
        }
    }

    private void startViewImageActivity(Uri uri) {
        Log.i(TAG, "startViewImageActivity");
        Intent intent = new Intent();
        intent.setClass(mAppContext, RapidViewImageActivity.class);
        intent.setDataAndType(uri, "image/jpeg");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Log.d(TAG, "startViewImageActivity" + "oneshotduration:" + (mOneshotendTime - mOneshotStartTime) +
                ",cameraopentime:" + (mopenEndTime - mopenStartTime) + ",previewtime:" + (mpreviewEndTime - mpreviewStartTime) +
                ",firstframe:" + (mFirtstFrameTime - mpreviewEndTime) + ",aecsettled:" + (mAecsettledTime - mFirtstFrameTime) +
                ",framecount:" + mFrameCount + ",capture_shutter:" + (mShutterCallbackTime - mCaptureStartTime) + ",shutter_jpegcallback:" +
                (mJpegPictureCallbackTime - mShutterCallbackTime));
        intent.putExtra("oneshotduration", (mOneshotendTime - mOneshotStartTime));
        intent.putExtra("cameraopentime", (mopenEndTime-mopenStartTime));
        intent.putExtra("previewtime", (mpreviewEndTime-mpreviewStartTime));
        intent.putExtra("firstframe", (mFirtstFrameTime - mpreviewEndTime));
        intent.putExtra("aecsettled", (mAecsettledTime - mFirtstFrameTime));
        intent.putExtra("framecount", mFrameCount);
        intent.putExtra("capture_shutter", (mShutterCallbackTime - mCaptureStartTime));
        intent.putExtra("shutter_jpegcallback", (mJpegPictureCallbackTime - mShutterCallbackTime));
        RapidViewImageActivity.mIsRunning = true;
        mAppContext.startActivity(intent);
    }

    private void startGallery(Uri uri) {
        Log.i(TAG, "startGallery");
        final String GALLERY_PACKAGE_NAME = "com.tct.gallery3d";
        final String GALLERY_ACTIVITY_CLASS = "com.tct.gallery3d.app.PermissionActivity";
        final String REVIEW_ACTION = "com.android.camera.action.REVIEW";

        Intent intent = new Intent(REVIEW_ACTION);
        intent.setClassName(
                GALLERY_PACKAGE_NAME, GALLERY_ACTIVITY_CLASS);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("*/*");
        intent.setData(uri);
        intent.putParcelableArrayListExtra("uriarray", mBurstShotUris);
        try {
            mAppContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG,"not find the activity");
        }
    }

    private void initializeCapabilities() {
        mCameraCapabilities = mCameraDevice.getCapabilities();
    }

    private void setCameraParameters() {
        updateCameraParametersInitialize();

        updateCameraParametersPreference();

        updateParametersPictureSize();
        final Location loc = mLocationManager.getCurrentLocation();
        CameraUtil.setGpsParameters(mCameraSettings, loc);
        if (mCameraDevice != null) {
            mCameraDevice.applySettings(mCameraSettings);
        }
    }

    private void updateCameraParametersInitialize() {
        int[] fpsRange = CameraUtil.getPhotoPreviewFpsRange(mCameraCapabilities);
        if (fpsRange != null && fpsRange.length > 0) {
            mCameraSettings.setPreviewFpsRange(fpsRange[0], fpsRange[1]);
        }

        mCameraSettings.setRecordingHintEnabled(false);

        if (mCameraCapabilities.supports(CameraCapabilities.Feature.VIDEO_STABILIZATION)) {
            mCameraSettings.setVideoStabilization(false);
        }
    }

    private void updateCameraParametersPreference() {
        // some monkey tests can get here when shutting the app down
        // make sure mCameraDevice is still valid, b/17580046
        if (mCameraDevice == null) {
            return;
        }
        mCameraSettings.isZslOn=true;
        // Set JPEG quality.
        updateParametersPictureQuality();
    }

    private void updateParametersPictureQuality() {
        int jpegQuality = CameraProfile.getJpegEncodingQualityParameter(mCameraId,
                CameraProfile.QUALITY_HIGH);
        mCameraSettings.setPhotoJpegCompressionQuality(jpegQuality);
    }

    private void updateParametersPictureSize() {
        if (mCameraDevice == null) {
            Log.w(TAG, "attempting to set picture size without caemra device");
            return;
        }

        SettingsManager settingsManager = getServices().getSettingsManager();
        String pictureSizeKey = Keys.KEY_PICTURE_SIZE_BACK;
        String defaultPicSize = SettingsUtil.getDefaultPictureSize(false);
        String pictureSize = settingsManager.getString(SettingsManager.SCOPE_GLOBAL,
                pictureSizeKey, defaultPicSize);

        List<Size> supported = mCameraCapabilities.getSupportedPhotoSizes();
        CameraPictureSizesCacher.updateSizesForCamera(mAppContext,
                mCameraDevice.getCameraId(), supported);
        SettingsUtil.setCameraPictureSize(pictureSize, supported, mCameraSettings,
                mCameraDevice.getCameraId());

        Size size = SettingsUtil.getPhotoSize(pictureSize, supported,
                mCameraDevice.getCameraId());

        List<Size> sizes = mCameraCapabilities.getSupportedPreviewSizes();
        Size optimalSize = CameraUtil.getOptimalPreviewSize(mAppContext, sizes,
                (double) size.width() / size.height());
        Size original = mCameraSettings.getCurrentPreviewSize();
        if (!optimalSize.equals(original)) {
            Log.v(TAG, "setting preview size. optimal: " + optimalSize + "original: " + original);
            mCameraSettings.setPreviewSize(optimalSize);
        }

        Log.d(TAG, "Preview size is " + optimalSize);
    }


    @Override
    public void onCameraDisabled(int cameraId) {
        Log.w(TAG, "Camera disabled: " + cameraId);
        mCameraFatalError = true;
        onCaptureDone(null);
    }

    @Override
    public void onDeviceOpenFailure(int cameraId, String info) {
        Log.w(TAG, "Camera open failure: " + info);
        mCameraFatalError = true;
        onCaptureDone(null);
    }

    @Override
    public void onDeviceOpenedAlready(int cameraId, String info) {
        Log.w(TAG, "Camera open already: " + cameraId + "," + info);
        mCameraFatalError = true;
        onCaptureDone(null);
    }

    @Override
    public void onReconnectionFailure(CameraAgent mgr, String info) {
        Log.w(TAG, "Camera reconnection failure:" + info);
        onCaptureDone(null);
    }

    @Override
    public void onCameraRequested() {}

    @Override
    public void onCameraClosed() {}

    @Override
    public boolean isReleased() {
        return false;
    }

    public void syncLocationManagerSetting() {
        Keys.syncLocationManager(mSettingsManager, mLocationManager);
    }
    public void pauseLocationManager() {
        if (mLocationManager != null) {
            Keys.pauseLocationManager(mLocationManager);
        }
    }
    private boolean mCameraFatalError = false;
    private final CameraExceptionHandler.CameraExceptionCallback mCameraExceptionCallback = new CameraExceptionHandler.CameraExceptionCallback() {
        @Override
        public void onCameraError(int errorCode) {
            // Not a fatal error. only do Log.e().
            Log.e(TAG, "Camera error callback. error=" + errorCode);
            mCameraFatalError = true;
            onCaptureDone(null);
        }

        @Override
        public void onCameraException(
                RuntimeException ex, String commandHistory, int action, int state) {
            Log.e(TAG, "Camera Exception", ex);
            mCameraFatalError = true;
            onCaptureDone(null);
        }

        @Override
        public void onDispatchThreadException(RuntimeException ex) {
            Log.e(TAG, "DispatchThread Exception", ex);
            mCameraFatalError = true;
            onCaptureDone(null);
        }
    };

    @Override
    public void onOrientationChanged(int orientation) {
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return;
        }

        mOrientation = (360 - orientation) % 360;
    }

    public interface Callback{
        public void onCaptureDone(boolean reset);
    }

    private PowerManager.WakeLock mScreenWakeLock;
    private PowerManager.WakeLock mCPUWakeLock;

    public void acquireScreenWakeLock(Context context) {
        if (mScreenWakeLock == null) {
            mScreenWakeLock = ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.FULL_WAKE_LOCK, "screenwakelock");
            mScreenWakeLock.acquire();
            mScreenWakeLock.setReferenceCounted(false);
        }
        mMainHandler.removeMessages(MSG_REMOVE_SCREEN_WAKELOCK);
        mMainHandler.sendEmptyMessageDelayed(MSG_REMOVE_SCREEN_WAKELOCK, SCREEN_WAKELOCK_TIMEOUT);
    }
    private void releaseScreenWakeLock() {
        mMainHandler.removeMessages(MSG_REMOVE_SCREEN_WAKELOCK);
        if (mScreenWakeLock != null) {
            mScreenWakeLock.release();
            mScreenWakeLock = null;
        }
    }
    public void acquireCpuLock(Context context) {
        if (mCPUWakeLock == null) {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (pm.isScreenOn()) {
                return;
            }
            mCPUWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "cpuwakelock");
            mCPUWakeLock.acquire();
            mCPUWakeLock.setReferenceCounted(false);
        }
    }

    public void releaseCpuLock() {
        if (mCPUWakeLock != null) {
            mCPUWakeLock.release();
            mCPUWakeLock = null;
        }
    }
}
