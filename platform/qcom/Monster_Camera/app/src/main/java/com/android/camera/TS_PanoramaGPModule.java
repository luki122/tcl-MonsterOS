package com.android.camera;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.android.camera.app.AppController;
import com.android.camera.debug.Log;
import com.android.camera.exif.ExifInterface;
import com.android.camera.exif.ExifTag;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.util.NV21Convertor;
import com.android.camera.util.SnackbarToast; // MODIFIED by bin-liu3, 2016-11-08,BUG-3253898
import com.android.camera.util.ToastUtil;
import com.android.ex.camera2.portability.CameraAgent;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.ex.camera2.portability.Size;
import com.android.external.plantform.ExtBuild;
import com.morpho.core.Error;
import com.morpho.core.InitParam;
import com.morpho.core.MorphoPanoramaSelfie;
import com.morpho.core.wrapper.MorphoPanoramaGPWrapper;
import com.morpho.utils.NativeMemoryAllocatorWrapper;
import com.morpho.utils.io.FileOperator;
import com.morpho.utils.multimedia.JpegHandler;
import com.morpho.utils.multimedia.MediaProviderUtils;
import com.tct.camera.R;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TS_PanoramaGPModule extends PhotoModule
        implements TS_PanoramaController,
        CameraAgent.CameraPreviewDataCallback {

    public static final String TSPANORAMAGP_MODULE_STRING_ID = "TS_PanoramaGPModule";
    private static final Log.Tag TAG = new Log.Tag(TSPANORAMAGP_MODULE_STRING_ID);

    protected TS_PanoramaGPUI mUI;

    private final boolean CAF_PREVIEW;

    // I want to set the picture size 2592x1944 when aspect ratio is 4/3.
    // private static final int PIXEL_UPPER_BOUND = 5000000;
    private static final int PIXEL_UPPER_BOUND = 6000000;

    private static final int FASE_THRESHOLD = 2300;
    // If platform is MTK, the orientation of still and preview is not the same, so follow Flash3,
    // use preview image.
    // private boolean mUsePreviewImage = false;
    private boolean mUsePreviewImage = ExtBuild.isPlatformMTK(); // MODIFIED by jianying.zhang, 2016-11-10,BUG-3398235
    //    private byte[] mCameraPreviewBuff;
    private int mCameraPreviewW, mCameraPreviewH;
    private int mCameraPictureW, mCameraPictureH;
    private InitParam mInitParam;
    private MorphoPanoramaGPWrapper mMorphoPanoramaGP;
    private int[] mImageID = new int[1];
    private byte[] mMotionData = new byte[256];
    private int[] mStatus = new int[1];
    private int[] mDirection = new int[1];
    private int mPrevDirection;
    private int mUseImage = MorphoPanoramaGPWrapper.USE_IMAGE_NORMAL;
    private int mUseThres = 10;
    private int mSelfieUseThres = 6;
    private int mMotionlessThres = 32768;
    private int mUseSensorThres = 0;
    private int mSaveOutputType = SAVE_OUTPUT_CLIPPING;//SAVE_OUTPUT_BOTH;
    int[] progress = new int[1];
    private Bitmap mPreviewImage;
    private Bitmap mDispPreviewImage;
//    private Bitmap mGuideImage_Left;
//    private Bitmap mGuideImage_Right;
//    private Bitmap mGuideImage_Up;
//    private Bitmap mGuideImage_Down;
    private Bitmap mMoveRight;
    private Bitmap mMoveLeft;
    private String mSaveBaseDirPath;
    private String mSaveInputDirPath;
    private String mShootingDate;
    //private boolean mIsShooting = false;
    private boolean mRequestTakePicture = false;
    private Object mSyncObj = new Object();
    private SaveOutputImageTask mSaveOutputImageTask;
    private ShowSmallPreviewTask mShowSmallPreviewTask = null;
    private boolean mIsShowSmallPreviewTaskFinish = true;

    private Integer mModeSelectionLockToken = null;
    private int mPreviewCount;
    private Uri mLastPhotoUri = null;
    private int mCntReqShoot = 0;   // Still撮影要求(=takePicture発行)回数
    private int mCntProcessd = 0;   // attachStillImageExt処理済み枚数
    ArrayList<StillImageData> mStillProcList;
    StillProcTask mStillProcTask = null;
    private boolean hasShownNoticeOfSlowly = false;
    private ArrayList<captureInfo> mCaptureInfoList = new ArrayList<captureInfo>();

    private final int APP_DIRECTION_HORIZONTAL = 0;
    private final int APP_DIRECTION_VERTICAL = 1;
    private final int APP_DIRECTION_AUTO = 2;
    private final int APP_DIRECTION_PORTRAIT = 0;
    private final int APP_DIRECTION_LANDSCAPE = 4;

    private final int MAX_WAIT_TIME = 2500;

    private final int APP_DIRECTION_PORTRAIT_HORIZONTAL = 0;        // 端末縦向きで横長撮影（ENGINEは縦撮り指定で出力90度回転)
    private final int APP_DIRECTION_PORTRAIT_VERTICAL = 1;            // 端末縦向きで縦長撮影(ENGINEは横撮り指定で出力90度回転)
    private final int APP_DIRECTION_PORTRAIT_AUTO = 2;            // 端末縦向きで撮影方向未定
    private final int APP_DIRECTION_LANDSCAPE_HORIZONTAL = 4;        // 端末横向きで横長撮影(ENGINEは横撮り指定)
    private final int APP_DIRECTION_LANDSCAPE_VERTICAL = 5;            // 端末横向きで縦長撮影(ENGINEは縦撮り指定)
    private final int APP_DIRECTION_LANDSCAPE_AUTO = 6;                // 端末横向きで撮影方向未定

    private int mAppDeviceRotation = APP_DIRECTION_PORTRAIT;
    private int mAppPanoramaDirection = APP_DIRECTION_HORIZONTAL;
    private boolean mForce_PanoramaDirection_HORIZONTAL_RIGHT = true; //TCL CUSTOM , panorama direction: DIRECTION_HORIZONTAL_LEFT,DIRECTION_HORIZONTAL_RIGHT

    public static final int SAVE_OUTPUT_CLIPPING = 1;
    public static final int SAVE_OUTPUT_BOUNDING = (1 << 1);
    public static final int SAVE_OUTPUT_BOTH = SAVE_OUTPUT_CLIPPING | SAVE_OUTPUT_BOUNDING;

    private final boolean USE_MULTI_THREAD = true;
    private int MAX_DST_IMG_WIDTH = 30000;
    private final int MIN_SHOOT_WIDTH = 320;
    private final int MIN_SHOOT_HEIGHT = 240;
    private final String FORMAT = "YVU420_SEMIPLANAR";

    private final int mPreviewCroppingRatio = 0;    /* ex) 0=No cropping,  10=crop 10% each side (i.e. 80% remain),  over 50=Illegal */
    private int mPreviewCroppingAdjustByAuto = 0;    /* Autoモードからの方向決定で生じる余白量 (=CropさせるPixel数) */
    private int mGuidePosY = 0;
    private static final int PREVIEW_SKIP_COUNT = 1;
    private int mPreviewSkipCount = PREVIEW_SKIP_COUNT;
    private final boolean COPY_EXIF_FROM_1ST_SHOOT = false;
    private int picRotation;
    private boolean mSaveInputImages = false;
    //private boolean isInCapture = false;
    private final static int CONTROL_RANGE_OF_GUIDE = 5;
    private SoundClips.Player mSoundPlayer;
    private long mEndTimeForSlowDownTips;
    private static final long TIME_TIP_SLOWDOWN_SHOW = 200;

    boolean mbIsFirstUsePanorama = false;
    private int mGuideViewWidth = 0;
    private int mGuideViewHeight = 0;
    private int mGuideViewMarginPreviewRight;
    private static final double MAX_ANGLE = 180.0;
    private static final int SCALE_PREVIEW_HEIGHT = 7;

    // should at least take one picture before capture stopped
    private boolean mEnableStopCapture = false;

    // capture another picture when capture stopped to make sure
    // FOV of panorama is the same with preview
    private boolean mCaptureWhenFinish;

    /**
     * mCaptureWhenFinish ぁEtrue の場合�Eみ、撮影終亁E�E�Eタン押下時に true となる（撮影開始時にfalseで初期化！E
     * それ以外�E場合�E、常に false
     */
    private boolean mIsFinishShooting = false;
    private boolean mFinishShootingDone = true;

    private boolean mAeLockSupported;
    private boolean mAwbLockSupported;

    private boolean mThumbUpdating = false; //MODIFIED by xuan.zhou, 2016-04-07,BUG-1920473

    // For Panorama Selfie.
    private int mCenterX0 = 0;
    private int mCenterX1 = 0;

    private int mGuideLeft = 0;
    private int mGuideRight = 0;

    private boolean isCenterFrame = true;
    private MorphoPanoramaSelfie mMorphoPanoramaSelfie;
    private int mAttachedLeft;
    private int mAttachedRight;
    private int mAttachedMiddle;
    private int mSelfieDirection;

    private static final int LEFT_SHOT_NUM = 1;
    private static final int RIGHT_SHOT_NUM = 1;
    private static final int SELFIE_DIRECTION_CENTER = 0;
    private static final int SELFIE_DIRECTION_RIGHT = 1;
    private static final int SELFIE_DIRECTION_LEFT = 2;


    private double mAngleOfViewDegree;
    private static final double ANGLE_OF_VIEW_DEGREE_NORMAL = 79.0d;
    private static final double ANGLE_OF_VIEW_DEGREE_SELFIE = 60.0d;
    private static final double DEFAULT_VALUE_ANGLE_OF_VIEW_DEGREE = 65.0d;
    private static final double MIN_ANGLE_OF_VIEW_DEGREE = 20.0d;
    private static final double MAX_ANGLE_OF_VIEW_DEGREE = 120.0d;
    private float mViewAngleH;
    private float mViewAngleV;
    private static final double MAX_ANGLE_SELFIE = 120.0;
    private int mMotionlessThres_Selfie = 500;

    // The frame size for individual panorama preview.
    private int mFrameWidth;
    private int mFrameHeight;
    private int mGuideBorder;
    private static final int BORDER_MARGIN = 2;

    private static final boolean READ_ASPECT_RATIO = true;
    private static final int RATIO_1_TO_1 = 1;
    private static final int RATIO_4_TO_3 = 2;
    private static final int RATIO_16_TO_9 = 3;

    private static final boolean ROUND_CORNER_PREVIEW = true;
    private float mPreviewRadius;

    @Override
    public boolean isRoundCornerPreview() {
        return ROUND_CORNER_PREVIEW;
    }


    private int mPanoState = PANO_STATE_PREVIEW_STOPPED;
    private int mDegrees = 0;
    private int mSavePicOrientation = 0;

    private void setPanoramaState(int state) {
        mPanoState = state;
        switch (state) {
            case PANO_STATE_IDLE:
            case PANO_STATE_RECORDING:
                mAppController.getLockEventListener().onIdle();
                break;
            case PANO_STATE_SAVING:
                mAppController.getLockEventListener().onShutter();
                break;
        }
    }

    // Reset the value when open camera.
    private boolean mIsSelfie = false;

    @Override
    public boolean isSelfie() {
        return mIsSelfie;
    }

    @Override
    public boolean is360Photo() {
        return false;
    }

    @Override
    public boolean isDirectionLTR() {
        return mForce_PanoramaDirection_HORIZONTAL_RIGHT;
    }

    @Override
    public void onDirectionInverted(boolean isLTR) {
        if (isSelfie() || mPanoState != PANO_STATE_IDLE) {
            return;
        }
        mUI.cleanPreviewFrame(false, isDirectionLTR());
        if (isLTR) {
            mUI.transitionToMode(PANO_MODE_PREVIEW_LTR);
        } else {
            mUI.transitionToMode(PANO_MODE_PREVIEW_RTL);
        }
        mForce_PanoramaDirection_HORIZONTAL_RIGHT = isLTR;
    }

    //    private OrientationEventListener mOrientationListener;
    class captureInfo {
        int mId;
        int mStatus;

        public captureInfo(int id, int status) {
            mId = id;
            mStatus = status;
        }
    }

    public TS_PanoramaGPModule(AppController app) {
        super(app);
        Log.i(TAG, "TS_PanoramaGPModule");
        Resources res = app.getAndroidContext().getResources();
//        mGuideImage_Left = BitmapFactory.decodeResource(
//                res, R.drawable.ic_pano_move_left_normal);
//        mGuideImage_Right = BitmapFactory.decodeResource(
//                res, R.drawable.ic_pano_move_right_normal);
//        mGuideImage_Up = BitmapFactory.decodeResource(
//                res, R.drawable.ic_up);
//        mGuideImage_Down = BitmapFactory.decodeResource(
//                res, R.drawable.ic_down);
        mMoveRight = BitmapFactory.decodeResource(res, R.drawable.ic_move_right);
        mMoveLeft = BitmapFactory.decodeResource(res, R.drawable.ic_move_left);
        mAppPanoramaDirection = APP_DIRECTION_HORIZONTAL;
        mCaptureWhenFinish = PreferenceManager.getDefaultSharedPreferences(app.getAndroidContext())
                .getBoolean(res.getString(R.string.pref_key_capture_when_finish), true);
        CAF_PREVIEW = CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_CONTINUOUS_PICTURE_PANO, false);
    }

    protected void initWidgetSize() {
        Resources res = mActivity.getResources();
        mGuideViewWidth = res.getDimensionPixelOffset(R.dimen.panorama_preview_guide_width);
        mGuideViewHeight = res.getDimensionPixelOffset(R.dimen.panorama_preview_guide_height);
        mGuideViewMarginPreviewRight = res.getDimensionPixelOffset(R.dimen.panorama_guide_margin_preview_right);
        mGuideBorder = res.getDimensionPixelOffset(R.dimen.panorama_guide_rect_border);
        mPreviewRadius = res.getDimensionPixelOffset(R.dimen.panorama_selfie_preview_radius);
    }

    @Override
    protected PhotoUI getPhotoUI() {
        if (mUI == null) {
            mUI = new TS_PanoramaGPUI(mActivity, this, mActivity.getModuleLayoutRoot());
            mUI.initSaveProgress(SAVE_MAX_PROGRESS, SAVE_MAX_PROGRESS / STEP_OF_PROGRESS);
            // Set the direction listener in TS_PanoramaGPUI.
//            mUI.mPanoramaPreview_Frame.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (mPanoState != PANO_STATE_IDLE)
//                        return;
//                    mForce_PanoramaDirection_HORIZONTAL_RIGHT = !mForce_PanoramaDirection_HORIZONTAL_RIGHT;
//                    mUI.showSelectDirectionUI(mForce_PanoramaDirection_HORIZONTAL_RIGHT);
//                    if (mbIsFirstUsePanorama && mHelpTipsManager != null) {
//                        mHelpTipsManager.notifyEventFinshed();
//                        mActivity.getSettingsManager().set(SettingsManager.SCOPE_GLOBAL,
//                                Keys.KEY_HELP_TIP_PANO_FINISHED, true);
//                        mbIsFirstUsePanorama = false;
//                    }
//                }
//            });
            initWidgetSize();
        }
        return mUI;
    }

    @Override
    public void hardResetSettings(SettingsManager settingsManager) {
        super.hardResetSettings(settingsManager);
        if (!isSelfieSupported()) {
            settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL, Keys.KEY_CAMERA_ID);
        }
    }

    @Override
    public String getModuleStringIdentifier() {
        return TSPANORAMAGP_MODULE_STRING_ID;
    }

    @Override
    public int getModuleId() {
        return mAppController.getAndroidContext().getResources()
                .getInteger(R.integer.camera_mode_pano);
    }

    @Override
    protected void switchCamera() {
        mUI.cleanPreviewFrame(isSelfie(), isDirectionLTR());
        mUI.cleanLivePanorama();
        mUI.transitionToMode(PANO_MODE_CLEAR);
        setPanoramaState(PANO_STATE_PREVIEW_STOPPED);
        super.switchCamera();
    }

    @Override
    public void onCameraAvailable(CameraAgent.CameraProxy cameraProxy) {
        if (mPaused) {
            return;
        }
        // reset the value before the judgement for startFaceDetection().
        mIsSelfie = isCameraFrontFacing();

        super.onCameraAvailable(cameraProxy);

        getParametersPictureSize();
        mStillProcList = new ArrayList<StillImageData>();
//        mCameraDevice.setJpegOrientation(mDisplayRotation);
//        mCameraDevice.setDisplayOrientation(mDisplayRotation, false);

        CameraCapabilities mCameraCapabilities = mCameraDevice.getCapabilities();

        mAeLockSupported = mCameraCapabilities.supports(CameraCapabilities.Feature.AUTO_EXPOSURE_LOCK);
        mAwbLockSupported = mCameraCapabilities.supports(CameraCapabilities.Feature.AUTO_WHITE_BALANCE_LOCK);
        setAeAwbLock(false);
        // setAutoExposureLock(false);
        // setAutoWhiteBalanceLock(false);

        mViewAngleH = mCameraCapabilities.getHorizontalViewAngle();
        mViewAngleV = mCameraCapabilities.getVerticalViewAngle();
        mAngleOfViewDegree = getAngleOfViewDegree(mViewAngleH, mViewAngleV);
    }

    @Override
    protected boolean isCaptureOrientationFollowPreview() {
        return false;
    }

    protected double getAngleOfViewDegree(float h_fov, float v_fov) {
        if (!isSelfie()) {
            return ANGLE_OF_VIEW_DEGREE_NORMAL;
        }
//        double angle = Math.hypot(h_fov, v_fov);
//        if (angle < MIN_ANGLE_OF_VIEW_DEGREE || angle > MAX_ANGLE_OF_VIEW_DEGREE) {
//            angle = DEFAULT_VALUE_ANGLE_OF_VIEW_DEGREE;
//        }
        return ANGLE_OF_VIEW_DEGREE_SELFIE;
    }

    private void getParametersPictureSize() {
        Size previewSZ = mCameraSettings.getCurrentPreviewSize();
        Size pictureSZ = mCameraSettings.getCurrentPhotoSize();
        mCameraPreviewW = previewSZ.width();
        mCameraPreviewH = previewSZ.height();
        mCameraPictureW = pictureSZ.width();
        mCameraPictureH = pictureSZ.height();
    }

    @Override
    public Size getStillImageSize() {
        return mCameraSettings.getCurrentPhotoSize();
    }

    // Audio record, dummy here and should be overridden in CylindricalPanoramaModule.
    @Override
    public void onAudioRecordOnOffSwitched() {

    }

    public void startAudioRecording() {

    }

    public void stopAudioRecording() {

    }
    // End.

    @Override
    public void onSingleTapUp(View view, int x, int y) {
        //dummy here , manual focus not supported in panorama.
    }

    private final Comparator<Size> mSizeComparator = new Comparator<Size>() {
        @Override
        public int compare(Size size, Size t1) {
            int sizePixel = size.width() * size.height();
            int t1Pixel = t1.width() * t1.height();

            return t1Pixel - sizePixel;
        }
    };

    protected void updateParametersPictureSize() {
        Size pictureSize;
        if (READ_ASPECT_RATIO) {
            pictureSize = getPictureSizeAccordingToRatio(RATIO_4_TO_3);
        } else {
            pictureSize = getPictureSizeAccordingToLimit();
        }

        if (pictureSize.width() == 0 || pictureSize.height() == 0) {
            return;
        }

        mCameraSettings.setPhotoSize(pictureSize);

        // Set a preview size that is closest to the viewfinder height and has
        // the right aspect ratio.
        List<Size> sizes = mCameraCapabilities.getSupportedPreviewSizes();
        Size optimalSize = CameraUtil.getOptimalPreviewSize(mActivity, sizes,
                (double) pictureSize.width() / pictureSize.height());
        Size original = mCameraSettings.getCurrentPreviewSize();
        if (!optimalSize.equals(original)) {
            Log.v(TAG, "setting preview size E . optimal: " + optimalSize + "original: " + original);
            mCameraSettings.setPreviewSize(optimalSize);
            mCameraDevice.applySettings(mCameraSettings);
            Log.v(TAG, "setting preview X");
        }

        if (optimalSize.width() != 0 && optimalSize.height() != 0) {
            Log.v(TAG, "updating aspect ratio");
            getPhotoUI().updatePreviewAspectRatio((float) optimalSize.width()
                    / (float) optimalSize.height());
        }
        Log.d(TAG, "Preview size is " + optimalSize);
    }

    private Size getPictureSizeAccordingToRatio(int type) {
        if (mCameraDevice == null) {
            return new Size(0, 0);
        }

        final float ASPECT_TOLERANCE = 0.1f;
        final float EXPECT_ASPECT_RATIO;
        if (type == RATIO_1_TO_1) {
            EXPECT_ASPECT_RATIO = 1.0f;
        } else if (type == RATIO_4_TO_3) {
            EXPECT_ASPECT_RATIO = 4f / 3f;
        } else {
            EXPECT_ASPECT_RATIO = 16f / 9f;
        }

        Size largestSize = new Size(0, 0);
        List<Size> photoSizes = mCameraCapabilities.getSupportedPhotoSizes();
        for (Size size : photoSizes) {
            float currentRatio = (float) size.width() / (float) size.height();
            if (currentRatio < 1) {
                currentRatio = 1 / currentRatio;
            }
            if (Math.abs(currentRatio - EXPECT_ASPECT_RATIO) < ASPECT_TOLERANCE) {
                int resolution = size.width() * size.height();
                // If supported, the judgement about PIXEL_UPPER_BOUND can be removed.
                if (resolution < PIXEL_UPPER_BOUND &&
                        resolution > (largestSize.width() * largestSize.height())) {
                    largestSize = size;
                }
            }
        }

        return largestSize;
    }

    private Size getPictureSizeAccordingToLimit() {
        if (mCameraDevice == null) {
            Log.e(TAG, "attempting to set picture size without camera device");
            return new Size(0, 0);
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
        if (size.width() * size.height() >= PIXEL_UPPER_BOUND) {
            List<Size> photoSizes = mCameraCapabilities.getSupportedPhotoSizes();
            Collections.sort(photoSizes, mSizeComparator);
            for (Size candidatePhotoSize : photoSizes) {
                if (candidatePhotoSize.width() * candidatePhotoSize.height() < PIXEL_UPPER_BOUND) {
                    size = candidatePhotoSize;
                    break;
                }
            }
        }
        Log.d(TAG, "Take picture with size :" + size);

        return size;
    }


    @Override
    public void onPreviewStarted() {
        super.onPreviewStarted();
        if (mPaused) {
            return;
        }
//        if (!hasShownNoticeOfSlowly) {
//            ToastUtil.makeText(mActivity, mActivity.getString(R.string.MSG_MOVE_CAMERA_SLOWLY), Toast.LENGTH_LONG);
//            ToastUtil.setGravity(Gravity.CENTER, 0, 0);
//            ToastUtil.show();
//            hasShownNoticeOfSlowly = true;
//        }

        setPanoramaState(PANO_STATE_IDLE);
        if (mCameraDevice != null) {
//            mCameraPreviewBuff = new byte[mCameraPreviewW * mCameraPreviewH * 3 / 2];
//            mCameraDevice.addCallbackBuffer(mCameraPreviewBuff);
//            mCameraDevice.setPreviewDataCallbackWithBuffer(mHandler, this);
            mCameraDevice.setPreviewDataCallback(mHandler, this);
        }

        // mUI.showSelectDirectionUI(mForce_PanoramaDirection_HORIZONTAL_RIGHT);
        mUI.cleanPreviewFrame(isSelfie(), isDirectionLTR());
        if (isSelfie()) {
            mUI.transitionToMode(PANO_MODE_SELFIE_PREVIEW);
        } else if (isDirectionLTR()) {
            mUI.transitionToMode(PANO_MODE_PREVIEW_LTR);
        } else {
            mUI.transitionToMode(PANO_MODE_PREVIEW_RTL);
        }

        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(getCameraId(), info);
        mDegrees = CameraUtil.getDisplayRotation(mActivity);
        picRotation = isSelfie() ?
                (info.orientation + mDegrees + 360) % 360 :
                (info.orientation - mDegrees + 360) % 360;
//        if (CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_HELP_TIP_TUTORIAL, false)) {
//            if (mbIsFirstUsePanorama && mHelpTipsManager != null) {
//                mHelpTipsManager.createAndShowHelpTip(HelpTipsManager.PANORAMA_GROUP, true);
//            }
//        }
        if (mAppController.isReversibleWorking()) {
            switch (mDegrees) {
                case 0:
                    mSavePicOrientation = 180;
                    break;
                case 90:
                    mSavePicOrientation = 270;
                    break;
                case 180:
                    mSavePicOrientation = 0;
                    break;
                case 270:
                    mSavePicOrientation = 90;
                    break;
            }
        } else {
            mSavePicOrientation = isSelfie() ? (360 - mDegrees) % 360 : mDegrees;
        }
    }

    @Override
    public void onOrientationChanged(int orientation) {
        super.onOrientationChanged(orientation);
        if (mPanoState != PANO_STATE_IDLE) {
            return;
        }
        if (mAppController.isReversibleWorking()) {
            switch (orientation) {
                case 0:
                    mSavePicOrientation = 180;
                    break;
                case 90:
                    mSavePicOrientation = 270;
                    break;
                case 180:
                    mSavePicOrientation = 0;
                    break;
                case 270:
                    mSavePicOrientation = 90;
                    break;
            }
        } else {
            mSavePicOrientation = isSelfie() ? (360 - orientation) % 360 : orientation;
        }
    }

    @Override
    public boolean onBackPressed() {
        synchronized (mSyncObj) {
            if (mPanoState == PANO_STATE_RECORDING ||
                    mPanoState == PANO_STATE_RECORDING_PENDING_STOP) {
                finishPanoramaShooting(false);
                return true;
            }
            if (mPanoState == PANO_STATE_SAVING) {
                return false;
            }
        }
        return super.onBackPressed();
    }

    @Override
    public void pause() {
        mPaused = true;
        if (mPanoState == PANO_STATE_RECORDING ||
                mPanoState == PANO_STATE_RECORDING_PENDING_STOP) {
            synchronized (mSyncObj) {
                finishPanoramaShooting(false);
            }
        } else {
            setPanoramaState(PANO_STATE_PREVIEW_STOPPED);
        }
        /*MODIFIED-BEGIN by xuan.zhou, 2016-04-07,BUG-1920473*/

        // In normal cases these buttons should be shown with animation and unlocked in the post
        // execute of SaveOutputImageTask, but they may set visible just before the task done in
        // super.pause(), thus they are still locked after resume again, so always reset the
        // visibility of peek and switch buttons here.
        mAppController.getCameraAppUI().showPeek(false);
        mAppController.getCameraAppUI().showSwitchButton(false);

        /*MODIFIED-END by xuan.zhou,BUG-1920473*/
        //mCameraView.setPreviewCallbackWithBuffer(null);
        if (mRequestTakePicture) {
            // suspended while waiting onPictureTaken
            mCntReqShoot--;
        }
//            if (mPreviewImage != null) {
//                mPreviewImage.recycle();
//                mPreviewImage = null;
//            }
        // mUI.mPanoramaPreview_Frame.setVisibility(View.GONE);
        mUI.cleanPreviewFrame(isSelfie(), isDirectionLTR());
        mUI.cleanLivePanorama();
        mUI.transitionToMode(PANO_MODE_CLEAR);
//            if (mDispPreviewImage != null && (mPanoCameraState == PANO_IDLE || mPanoCameraState == PANO_PREVIEW_STOP)) {
//                mDispPreviewImage.recycle();
//                mDispPreviewImage = null;
//            }
//        synchronized (mNV21ConvertorLock) {
//            if (mConvertor != null) {
//                mConvertor.release();
//                mConvertor = null;
//            }
//        }
//        if( mOrientationListener != null)
//            mOrientationListener.disable();
        super.pause();// put super pause here to make sure cameraDevice released after all Pano related resource released, or finishPanoramaShooting would naive skip1
        mActivity.unlockOrientation();
    }

    @Override
    public void onShutterButtonClick() {
        synchronized (mSyncObj) {
            if (mPanoState == PANO_STATE_SAVING || mPanoState == PANO_STATE_PREVIEW_STOPPED) {
                return;
            }

            if (mPanoState == PANO_STATE_RECORDING) {
                if (mEnableStopCapture) {
                    mEnableStopCapture = false;

                    setPanoramaState(PANO_STATE_RECORDING_PENDING_STOP);

                    if (mCaptureWhenFinish && !isSelfie()) {
                        mIsFinishShooting = true;
                    } else {
                        finishPanoramaShooting(true);
                    }
                }
            } else if (mPanoState == PANO_STATE_IDLE) {
                mIsFinishShooting = false;
                mFinishShootingDone = false;
                mThumbUpdating = false; //MODIFIED by xuan.zhou, 2016-04-07,BUG-1920473
                startPanoramaShooting();
            }
        }
    }

    private void finishPanoramaShootingLater(final boolean save_image) {
        // ダイアログ表示を行うため、UIスレチE�E��E�にイベント�EスチE
        mHandler.post(new Runnable() {
            public void run() {
                finishPanoramaShooting(save_image);
            }
        });
    }

    private void playRecordSound() {
        if (Keys.isShutterSoundOn(mAppController.getSettingsManager())) {
            mSoundPlayer.play(SoundClips.START_VIDEO_RECORDING);
        }
    }

    @Override
    public void onPreviewFrame(final byte[] data, CameraAgent.CameraProxy camera) {
        if (mPaused) {
            return;
        }
        synchronized (mSyncObj) {
            if (mPreviewSkipCount > 0) {
                mPreviewSkipCount--;
//                mCameraDevice.addCallbackBuffer(mCameraPreviewBuff);
                return;
            }

            if (isSelfie()) {
                handleFrontCameraPreviewCb(data);
            } else {
                handleRearCameraPreviewCb(data);
            }
        }
    }

    private void handleFrontCameraPreviewCb(final byte[] data) {
        if (mPanoState == PANO_STATE_IDLE) {
            if (mIsShowSmallPreviewTaskFinish) {
                showSmallPreview(data);
            }
            return;
        }

        if (mCameraDevice == null || mMorphoPanoramaSelfie == null ||
                isProcessingFinishTask() || !mIsShowSmallPreviewTaskFinish) {
            return;
        }

        if (mSaveInputImages) {
            String path = String.format("%s/%s/%s/p_%05d.jpg",
                    mSaveInputDirPath, mShootingDate, "preview", mPreviewCount);
            mMorphoPanoramaSelfie.saveJpeg(
                    path, data, FORMAT, mCameraPreviewW, mCameraPreviewH, 0);
        }

        int ret;
        mPreviewCount++;

        // Capture at middle first, then left/right capture finished.
        if (mAttachedLeft >= LEFT_SHOT_NUM && mAttachedRight >= RIGHT_SHOT_NUM) {
            finishPanoramaShooting(true);
            mCameraDevice.startPreview();
        }

        ret = mMorphoPanoramaSelfie.attachPreview(data, mUseImage, mImageID, mMotionData, mStatus, mPreviewImage);
        if (ret != Error.MORPHO_OK) {
            Log.e(TAG, String.format("attachPreview() -> 0x%x", ret));
        }

        boolean showNeedToStop = false;
        boolean showTooFar = false;
        // It's abnormal that the mStatus[0] is always STATUS_STITCHING.
        if (mStatus[0] != MorphoPanoramaSelfie.STATUS_STITCHING) {
            if (mStatus[0] == MorphoPanoramaSelfie.STATUS_WARNING_TOO_FAR_1
                    || mStatus[0] == MorphoPanoramaSelfie.STATUS_WARNING_TOO_FAR_2) {
                /* ズレ過ぎ！ */
/* MODIFIED-BEGIN by feifei.xu, 2016-11-01,BUG-3278858*/
//                Toast toast = Toast.makeText(mActivity, R.string.MSG_MISALIGNMENT, Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
                /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
                SnackbarToast.getSnackbarToast().showToast(mActivity,mActivity.getString(R.string.MSG_MISALIGNMENT)
                        ,SnackbarToast.LENGTH_LONG,SnackbarToast.DEFAULT_Y_OFFSET);
                        /* MODIFIED-END by bin-liu3,BUG-3253898*/
                /* MODIFIED-END by feifei.xu,BUG-3278858*/
                finishPanoramaShooting(false);
                mCameraDevice.startPreview();
                return;
            }
            else if (mStatus[0] == MorphoPanoramaSelfie.STATUS_WHOLE_AREA_COMPLETE) {
                finishPanoramaShooting(true);
                mCameraDevice.startPreview();
            } else {
                switch(mStatus[0]) {
                    case MorphoPanoramaSelfie.STATUS_WARNING_NEED_TO_STOP:
                        showNeedToStop = true;
                        break;
                    case MorphoPanoramaSelfie.STATUS_WARNING_TOO_FAR:
                        showTooFar = true;
                        break;
                }
            }
        }

//            FrameLayout fl_needToStop = mUI.getNeedToStop();
//            if (showNeedToStop) {
//                fl_needToStop.setBackgroundResource(R.drawable.ic_warning);
//            }
//            else if (showTooFar) {
//                switch(mAppDeviceRotation) {
//                    case APP_DIRECTION_LANDSCAPE:
//                        if (!mReverseDirGuide) {
//                            fl_needToStop.setBackgroundResource(mDirection[0] == MorphoPanoramaSelfie.DIRECTION_VERTICAL_DOWN ? R.drawable.ic_too_far_up :
//                                    mDirection[0] == MorphoPanoramaSelfie.DIRECTION_VERTICAL_UP   ? R.drawable.ic_too_far_down :
//                                            mDirection[0] == MorphoPanoramaSelfie.DIRECTION_HORIZONTAL_LEFT ? R.drawable.ic_too_far_right :
//                                                    R.drawable.ic_too_far_left);
//                        }
//                        else {
//                            fl_needToStop.setBackgroundResource(mDirection[0] == MorphoPanoramaSelfie.DIRECTION_VERTICAL_DOWN ? R.drawable.ic_too_far_down :
//                                    mDirection[0] == MorphoPanoramaSelfie.DIRECTION_VERTICAL_UP   ? R.drawable.ic_too_far_up :
//                                            mDirection[0] == MorphoPanoramaSelfie.DIRECTION_HORIZONTAL_LEFT ? R.drawable.ic_too_far_left :
//                                                    R.drawable.ic_too_far_right);
//                        }
//                        break;
//                    case APP_DIRECTION_PORTRAIT:
//                        if (!mReverseDirGuide) {
//                            fl_needToStop.setBackgroundResource(mDirection[0] == MorphoPanoramaSelfie.DIRECTION_VERTICAL_DOWN ? R.drawable.ic_too_far_right :
//                                    mDirection[0] == MorphoPanoramaSelfie.DIRECTION_VERTICAL_UP   ? R.drawable.ic_too_far_left :
//                                            mDirection[0] == MorphoPanoramaSelfie.DIRECTION_HORIZONTAL_LEFT ? R.drawable.ic_too_far_down :
//                                                    R.drawable.ic_too_far_up);
//                        }
//                        else {
//                            fl_needToStop.setBackgroundResource(mDirection[0] == MorphoPanoramaSelfie.DIRECTION_VERTICAL_DOWN ? R.drawable.ic_too_far_left :
//                                    mDirection[0] == MorphoPanoramaSelfie.DIRECTION_VERTICAL_UP   ? R.drawable.ic_too_far_right :
//                                            mDirection[0] == MorphoPanoramaSelfie.DIRECTION_HORIZONTAL_LEFT ? R.drawable.ic_too_far_up :
//                                                    R.drawable.ic_too_far_down);
//                        }
//                        break;
//                }
//            }
//            fl_needToStop.setVisibility((showNeedToStop || showTooFar) ? View.VISIBLE : View.INVISIBLE);

        ret = mMorphoPanoramaSelfie.getCurrentDirection(mDirection);
        if (ret != Error.MORPHO_OK) {
            Log.e(TAG, String.format("getCurrentDirection() -> 0x%x", ret));
        }

        Point attachedPos = new Point();

        int sw = mPreviewImage.getWidth();
        int sh = mPreviewImage.getHeight();
        int dw = mDispPreviewImage.getWidth();
        int dh = mDispPreviewImage.getHeight();

        int offsetX, offsetY;
        Canvas canvas = new Canvas(mDispPreviewImage);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
        Rect dst = new Rect(0, 0, dw, dh);
        Rect src;
        switch(mAppPanoramaDirection) {
            case APP_DIRECTION_HORIZONTAL:
            default:
                sh -= mPreviewCroppingAdjustByAuto*2;
                offsetX = 0;
                offsetY = -sh*mPreviewCroppingRatio/100;
                //src = new Rect(0, mPreviewCroppingAdjustByAuto + sh*mPreviewCroppingRatio/100, sw, mPreviewCroppingAdjustByAuto + sh*(100-mPreviewCroppingRatio*2)/100);
                src = new Rect(0, mPreviewCroppingAdjustByAuto + sh*mPreviewCroppingRatio/100, sw, mPreviewCroppingAdjustByAuto + sh*(100-mPreviewCroppingRatio)/100);
                break;
            case APP_DIRECTION_VERTICAL:
                sw -= mPreviewCroppingAdjustByAuto*2;
                offsetX = -sw*mPreviewCroppingRatio/100;
                offsetY = 0;
                //src = new Rect(mPreviewCroppingAdjustByAuto + sw*mPreviewCroppingRatio/100, 0, mPreviewCroppingAdjustByAuto + sw*(100-mPreviewCroppingRatio*2)/100, sh);
                src = new Rect(mPreviewCroppingAdjustByAuto + sw*mPreviewCroppingRatio/100, 0, mPreviewCroppingAdjustByAuto + sw*(100-mPreviewCroppingRatio)/100, sh);
                break;
        }
        fitUI(src, dst, dw, dh);

        if (isRoundCornerPreview()) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            RectF rect = new RectF(0, 0, dw, dh);
            canvas.drawRoundRect(rect, mPreviewRadius, mPreviewRadius, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(mPreviewImage, src, dst, paint);
        } else {
            canvas.drawBitmap(mPreviewImage, src, dst, null);
        }

        mUI.updateLivePanorama(mDispPreviewImage);

            /* ガイドの表示 */
        MorphoPanoramaSelfie.GuidePositions guides = new MorphoPanoramaSelfie.GuidePositions();
        mMorphoPanoramaSelfie.getGuidancePos(attachedPos, guides);

        switch(mDirection[0]) {
            case MorphoPanoramaSelfie.DIRECTION_HORIZONTAL_LEFT:
            case MorphoPanoramaSelfie.DIRECTION_HORIZONTAL_RIGHT:
            case MorphoPanoramaSelfie.DIRECTION_HORIZONTAL_CENTER:
                if (mInitParam.output_rotation == 0 || mInitParam.output_rotation == 180) {
                    attachedPos.y -= mPreviewCroppingAdjustByAuto;
                    guides.p[0].y -= mPreviewCroppingAdjustByAuto;
                    guides.p[1].y -= mPreviewCroppingAdjustByAuto;
                }
                else {
                    attachedPos.x -= mPreviewCroppingAdjustByAuto;
                    guides.p[0].x -= mPreviewCroppingAdjustByAuto;
                    guides.p[1].x -= mPreviewCroppingAdjustByAuto;
                }
                break;
        }
        int sizeDisplay = (dw<dh ? dw : dh);
        int sizeImage = (int)(sizeDisplay * 0.5);
        int hsz = sizeImage/2;
        float ratio = (dw<dh ? (float)((float)dh/sh) : (float)((float)dw/sw));
        offsetX *= ratio;
        offsetY *= ratio;
        attachedPos.x *= ratio;
        attachedPos.y *= ratio;
        guides.p[0].x *= ratio;
        guides.p[0].y *= ratio;
        guides.p[1].x *= ratio;
        guides.p[1].y *= ratio;

        // Actually guides.p[1] is the left one and guides.p[0] is right.
        if (mAttachedMiddle == 0 && (mGuideLeft == 0 || mGuideRight == 0)) {
            mGuideLeft = guides.p[1].x;
            mGuideRight = guides.p[0].x;
        }

        boolean crossTheBorder = false;
        if (mAttachedLeft >= LEFT_SHOT_NUM &&
                ((attachedPos.x + mFrameWidth/2 < mGuideLeft))) {
            crossTheBorder = true;
        } else if (mAttachedLeft <= RIGHT_SHOT_NUM &&
                ((attachedPos.x -mFrameWidth/2) > mGuideRight)) {
            crossTheBorder = true;
        }
        if (crossTheBorder) {
            finishPanoramaShooting(true);
            mCameraDevice.startPreview();
            return;
        }

        /* draw current frame */
        drawGuideRect(attachedPos.x, attachedPos.y, Color.WHITE, offsetX, offsetY, ratio);

        int tip = R.string.tips_for_panorama_preview;
        int drawable = 0;

        /* draw guide */
        if (mSelfieDirection == SELFIE_DIRECTION_CENTER)
        {
            /* show the nearer one */
            float dist0 = Math.abs(guides.p[0].x - attachedPos.x);
            float dist1 = Math.abs(guides.p[1].x - attachedPos.x);
            if (dist0+20 < dist1){
                drawGuideRect(guides.p[0].x, guides.p[0].y, Color.GREEN, offsetX, offsetY, ratio);
                // drawGuideRect(mDispPreviewImage, getSelfieGuideRect(SELFIE_DIRECTION_RIGHT), Color.GREEN);
                if (mGuideRight >= attachedPos.x) {
                    tip = R.string.tip_for_move_right;
                } else {
                    tip = R.string.tip_for_move_left;
                }
            }
            else if (dist0 > dist1+20){
                drawGuideRect(guides.p[1].x, guides.p[1].y, Color.GREEN, offsetX, offsetY, ratio);
                // drawGuideRect(mDispPreviewImage, getSelfieGuideRect(SELFIE_DIRECTION_LEFT), Color.GREEN);
                if (mGuideLeft <= attachedPos.x) {
                    tip = R.string.tip_for_move_left;
                } else {
                    tip = R.string.tip_for_move_right;
                }
            }
            else
            {
                drawGuideRect(guides.p[0].x, guides.p[0].y, Color.GREEN, offsetX, offsetY, ratio);
                drawGuideRect(guides.p[1].x, guides.p[1].y, Color.GREEN, offsetX, offsetY, ratio);
                // drawGuideRect(mDispPreviewImage, getSelfieGuideRect(SELFIE_DIRECTION_LEFT), Color.GREEN);
                // drawGuideRect(mDispPreviewImage, getSelfieGuideRect(SELFIE_DIRECTION_RIGHT), Color.GREEN);
            }
        }
        else if (mSelfieDirection == SELFIE_DIRECTION_LEFT)
        {
            if (mAttachedLeft < LEFT_SHOT_NUM-1 || (mAttachedLeft == LEFT_SHOT_NUM-1 && mImageID[0] < 0)){
                drawGuideRect(guides.p[1].x, guides.p[1].y, Color.GREEN, offsetX, offsetY, ratio);
                // drawGuideRect(mDispPreviewImage, getSelfieGuideRect(SELFIE_DIRECTION_LEFT), Color.GREEN);
                if (mGuideRight >= attachedPos.x) {
                    tip = R.string.tip_for_move_right;
                } else {
                    tip = R.string.tip_for_move_left;
                }
            }
        }
        else if (mSelfieDirection == SELFIE_DIRECTION_RIGHT)
        {
            if (mAttachedRight < RIGHT_SHOT_NUM-1 || (mAttachedRight == RIGHT_SHOT_NUM-1 && mImageID[0] < 0)){
                drawGuideRect(guides.p[0].x, guides.p[0].y, Color.GREEN, offsetX, offsetY, ratio);
                // drawGuideRect(mDispPreviewImage, getSelfieGuideRect(SELFIE_DIRECTION_RIGHT), Color.GREEN);
                if (mGuideRight >= attachedPos.x) {
                    tip = R.string.tip_for_move_right;
                } else {
                    tip = R.string.tip_for_move_left;
                }
            }
        }

        if (tip == R.string.tip_for_move_left) {
            drawable = R.drawable.ic_move_left;
        } else if (tip == R.string.tip_for_move_right) {
            drawable = R.drawable.ic_move_right;
        }

        if (mAttachedMiddle == 0 ||
                mAttachedLeft < LEFT_SHOT_NUM || mAttachedRight < RIGHT_SHOT_NUM) {
            mUI.showInfoText(tip, drawable, drawable);
        } else {
            mUI.hideInfoText();
        }

        if (mImageID[0] >= 0) {
            final byte[] data2 = data;

            if (mAttachedMiddle == 1) {
                if (attachedPos.x < mDispPreviewImage.getWidth()/2) {
                    mSelfieDirection = SELFIE_DIRECTION_LEFT;
                } else {
                    mSelfieDirection = SELFIE_DIRECTION_RIGHT;
                }
            }

            if (mSelfieDirection == SELFIE_DIRECTION_LEFT) {
                mAttachedLeft ++;
                if (mAttachedLeft >= LEFT_SHOT_NUM) {
                    mSelfieDirection = SELFIE_DIRECTION_RIGHT;
                }
            } else if (mSelfieDirection == SELFIE_DIRECTION_RIGHT) {
                mAttachedRight ++;
                if (mAttachedRight >= RIGHT_SHOT_NUM) {
                    mSelfieDirection = SELFIE_DIRECTION_LEFT;
                }
            } else {
                mAttachedMiddle = 1;
            }

            if (!mPaused) {
                mCaptureInfoList.add(new captureInfo(mImageID[0], mStatus[0]));
                if (!mUsePreviewImage) {
                    mRequestTakePicture = true;
                    mCameraDevice.takePicture(mHandler, null, null, null, getJpegPictureCallback());
                } else {
                    onPictureTakenPreview(data2);
                }
                mEnableStopCapture = true;
                playRecordSound();
//                fl_needToStop.setBackgroundResource(R.drawable.ic_warning);
//                fl_needToStop.setVisibility(View.VISIBLE);
                mCntReqShoot++;
            }
        } else {
//                mCameraView.addCallbackBuffer(mCameraPreviewBuff);
        }

        mPrevDirection = mDirection[0];
    }

    private void fitUI(Rect src, Rect drc, int dw, int dh) {
        double Clipingw = 0;
        if (1.0 * (src.right - src.left) / (src.bottom - src.top) > 1.0 * dw / dh) {
            Clipingw = (src.right - src.left) - (src.bottom - src.top) * 1.0 * dw / dh;
            src.right = src.right - (int) (Clipingw / 2);
            src.left = src.left + (int) (Clipingw / 2);
        } else {
            Clipingw = dw - 1.0 * (src.right - src.left) / (src.bottom - src.top) * dh;
            drc.right = dw - (int) (Clipingw / 2);
            drc.left = (int) (Clipingw / 2);
        }
    }

    private void drawGuideRect(int x, int y, int c, int offsetX, int offsetY,
                               float ratio) {
        int sw = mPreviewImage.getWidth();
        int sh = mPreviewImage.getHeight();
        int dw = mDispPreviewImage.getWidth();
        int dh = mDispPreviewImage.getHeight();
        Canvas canvas = new Canvas(mDispPreviewImage);
        int fw;
        int fh;
        boolean ADJUST_CURRENT_FRAME_ASPECT_RATIO = false;
        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        if (true) {
            fw = mInitParam.preview_height;
            fh = mInitParam.preview_width;
            if (ADJUST_CURRENT_FRAME_ASPECT_RATIO
                    && mAppPanoramaDirection == APP_DIRECTION_VERTICAL) {
                fh = fw * (100 - mPreviewCroppingRatio * 2) / 100;
            }
        } else {
            fw = mInitParam.preview_width;
            fh = mInitParam.preview_height;
            if (ADJUST_CURRENT_FRAME_ASPECT_RATIO
                    && mAppPanoramaDirection == APP_DIRECTION_HORIZONTAL) {
                fh = fw * (100 - mPreviewCroppingRatio * 2) / 100;
            }
        }

        // Scale
        fw /= mInitParam.preview_shrink_ratio;
        fh /= mInitParam.preview_shrink_ratio;
        fw *= ratio;
        fh *= ratio;
        fw /= 2;
        fh /= 2;
        int x0 = offsetX + x - fw;
        int x1 = offsetX + x + fw;
        int y0 = offsetY + y - fh;
        int y1 = offsetY + y + fh;
        if (mAppPanoramaDirection == APP_DIRECTION_HORIZONTAL) {
            // check Projection
            y0 = Math.max(y0, 2);
            y1 = Math.min(y1, dh - 2);
        } else {
            x0 = Math.max(x0, 2);
            x1 = Math.min(x1, dw - 2);
        }
        p.setColor(c);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(mGuideBorder);

        // 缩放指示框，使其适应UI
        double offsetw = 0;
        // Log.d(TAG,"drawRect   x0 = "+x0+"x1 = "+x1+"y0 = "+y0+"y1 = "+y1);
        int RectW = (int) (1.0 * fw * (dh / (fh * (1.0 - mPreviewCroppingRatio * 2.0 / 100))));
        offsetw = (RectW - (x1 - x0)) / 2.0;
        if (!isCenterFrame) {
            x0 = mCenterX0 + (x0 - mCenterX0) * dh / (y1 - y0);
            x1 = mCenterX1 + (x1 - mCenterX1) * dh / (y1 - y0);
        } else {
            mCenterX0 = x0;
            mCenterX1 = x1;
            isCenterFrame = false;
        }
        x0 = (int) (x0 - offsetw);
        x1 = (int) (x1 + offsetw);
        y1 = y0 + dh - 2;
        // Log.d(TAG,"drawRect x0 = "+x0+"x1 = "+x1+"y0 = "+y0+"y1 = "+y1+", h = "+h);
        if (isRoundCornerPreview()) {
            canvas.drawRoundRect(x0, y0, x1, y1, mPreviewRadius, mPreviewRadius, p);
        } else {
            canvas.drawRect(x0, y0, x1, y1, p);
        }
        p = null;
    }

    private void handleRearCameraPreviewCb(final byte[] data){
        if (mIsFinishShooting && mFinishShootingDone) {
            // Capture one picture is enough.
            return;
        }
        //custom for tcl
        if (mPanoState == PANO_STATE_IDLE) {
            if (mIsShowSmallPreviewTaskFinish) {
                showSmallPreview(data);
            }
//                mCameraDevice.addCallbackBuffer(mCameraPreviewBuff);

            return;
        }
        if (mCameraDevice == null || mMorphoPanoramaGP == null || isProcessingFinishTask() || !mIsShowSmallPreviewTaskFinish) {
//                mCameraDevice.addCallbackBuffer(mCameraPreviewBuff);
            return;
        }

        if (mSaveInputImages) {
            String path = String.format("%s/%s/%s/p_%05d.jpg", mSaveInputDirPath, mShootingDate, "preview", mPreviewCount);
            mMorphoPanoramaGP.saveJpeg(path, data, FORMAT, mCameraPreviewW, mCameraPreviewH, 0);
        }
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, mCameraPreviewW, mCameraPreviewH, null);
//            yuvImage.compressToJpeg(new Rect(0, 0, mCameraPreviewW, mCameraPreviewH), 50, out);
//            byte[] imageBytes = out.toByteArray();
//            String path = String.format("%s/4.jpg", mSaveInputDirPath);
//            File file = new File(path);
//            try {
//                FileOutputStream o_stream = new FileOutputStream(file);
//                o_stream.write(imageBytes);
//                o_stream.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        int use_image = mUseImage;
        if (mIsFinishShooting) {
            // 撮影終亁E�E�Eタンが押された場合、PreviewCallbackを解除
//                mCameraView.setPreviewCallbackWithBuffer(null);
            use_image = mMorphoPanoramaGP.USE_IMAGE_FORCE;
        }
        int ret;
        mPreviewCount++;
//            ret = mMorphoPanoramaGP.attachPreview(data, mUseImage, mImageID, mMotionData, mStatus, mPreviewImage);
        ret = mMorphoPanoramaGP.attachPreview(data, use_image, mImageID, mMotionData, mStatus, mPreviewImage);
        if (mStatus[0] != MorphoPanoramaGPWrapper.STATUS_STITCHING) {
            if (mStatus[0] == MorphoPanoramaGPWrapper.STATUS_WARNING_TOO_FAR_1 ||
                    mStatus[0] == MorphoPanoramaGPWrapper.STATUS_WARNING_TOO_FAR_2 ||
                    mStatus[0] == MorphoPanoramaGPWrapper.STATUS_WARNING_NEED_TO_STOP ||
                    mStatus[0] == MorphoPanoramaGPWrapper.STATUS_WARNING_TOO_FAR
                    ) {
                /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
                SnackbarToast.getSnackbarToast().showToast(mActivity, mActivity.getString(R.string.MSG_MISALIGNMENT)
                        , SnackbarToast.LENGTH_LONG,SnackbarToast.DEFAULT_Y_OFFSET);
                        /* MODIFIED-END by bin-liu3,BUG-3253898*/
                finishPanoramaShooting(false);
                mCameraDevice.startPreview();
                return;
            } else if (mStatus[0] == MorphoPanoramaGPWrapper.STATUS_WARNING_REVERSE) {
                finishPanoramaShooting(mEnableStopCapture); //MODIFIED by xuan.zhou, 2016-04-13,BUG-1943018
                mCameraDevice.startPreview();
                return;
            } else if (mStatus[0] == MorphoPanoramaGPWrapper.STATUS_WARNING_TOO_FAST) {
                mUI.showInfoText(R.string.tip_for_move_slow);
                mEndTimeForSlowDownTips = System.currentTimeMillis() + TIME_TIP_SLOWDOWN_SHOW;
            }
        }
        ret = mMorphoPanoramaGP.getCurrentDirection(mDirection);
        if (mPrevDirection != MorphoPanoramaGPWrapper.DIRECTION_HORIZONTAL &&
                mPrevDirection != MorphoPanoramaGPWrapper.DIRECTION_VERTICAL &&
                mPrevDirection != MorphoPanoramaGPWrapper.DIRECTION_AUTO) {

            int sw = mPreviewImage.getWidth();
            int sh = mPreviewImage.getHeight();
            int dw = mDispPreviewImage.getWidth();
            int dh = mDispPreviewImage.getHeight();
            int offsetX, offsetY;
            Canvas canvas = new Canvas(mDispPreviewImage);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);
            Rect dst = new Rect(0, 0, dw, dh);
            Rect src;
            switch (mAppPanoramaDirection) {
                case APP_DIRECTION_HORIZONTAL:
                default:
                    sh -= mPreviewCroppingAdjustByAuto * 2;
                    offsetX = 0;
                    offsetY = -sh * mPreviewCroppingRatio / 100;
                    // src = new Rect(0, mPreviewCroppingAdjustByAuto + sh * mPreviewCroppingRatio / 100, sw, mPreviewCroppingAdjustByAuto + sh * (100 - mPreviewCroppingRatio * 2) / 100);
                    int left = isDirectionLTR() ? BORDER_MARGIN : 0;
                    src = new Rect(left,
                            mPreviewCroppingAdjustByAuto + sh * mPreviewCroppingRatio / 100,
                            sw,
                            mPreviewCroppingAdjustByAuto + sh * (100 - mPreviewCroppingRatio * 2) / 100);
                    break;
                case APP_DIRECTION_VERTICAL:
                    sw -= mPreviewCroppingAdjustByAuto * 2;
                    offsetX = -sw * mPreviewCroppingRatio / 100;
                    offsetY = 0;
                    src = new Rect(mPreviewCroppingAdjustByAuto + sw * mPreviewCroppingRatio / 100, 0, mPreviewCroppingAdjustByAuto + sw * (100 - mPreviewCroppingRatio * 2) / 100, sh);
                    break;
            }

            if (isRoundCornerPreview()) {
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                RectF rect = new RectF(0, 0, dw, dh);
                canvas.drawRoundRect(rect, mPreviewRadius, mPreviewRadius, paint);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
                canvas.drawBitmap(mPreviewImage, src, dst, paint);
            } else {
                canvas.drawBitmap(mPreviewImage, src, dst, null);
            }

            Point attachedPos = new Point();
            Point guidePos = new Point();
            mMorphoPanoramaGP.getGuidancePos(attachedPos, guidePos);
            switch (mDirection[0]) {
                case MorphoPanoramaGPWrapper.DIRECTION_HORIZONTAL_LEFT:
                case MorphoPanoramaGPWrapper.DIRECTION_HORIZONTAL_RIGHT:
                    if (mInitParam.output_rotation == 0 || mInitParam.output_rotation == 180) {
                        attachedPos.y -= mPreviewCroppingAdjustByAuto;
                        guidePos.y -= mPreviewCroppingAdjustByAuto;
                    } else {
                        attachedPos.x -= mPreviewCroppingAdjustByAuto;
                        guidePos.x -= mPreviewCroppingAdjustByAuto;
                    }
                    break;
            }
            int sizeDisplay = (dw < dh ? dw : dh);
            int sizeImage = (int) (sizeDisplay * 0.5);
            int hsz = sizeImage / 2;
            float ratio = (dw < dh ? (float) ((float) dh / sh) : (float) ((float) dw / sw));
            offsetX *= ratio;
            offsetY *= ratio;
            attachedPos.x *= ratio;
            attachedPos.y *= ratio;
            guidePos.x *= ratio;
            guidePos.y *= ratio;

            Rect mGuideRect = getPanoramaGuideRect(attachedPos.x, attachedPos.y, offsetX, offsetY);
            // Use Color.WHITE temporarily.
            drawGuideRect(mDispPreviewImage, mGuideRect, Color.WHITE);

            if (mGuidePosY == 0) {
                mGuidePosY = guidePos.y;
            }
            //add for arrow
            {
//                Bitmap arrowBitmap = mGuideImage_Left;
//                int offset_x = offsetX, offset_y = offsetY;
//                if (mAppDeviceRotation == APP_DIRECTION_PORTRAIT) {
//                    arrowBitmap = mDirection[0] == MorphoPanoramaGPWrapper.DIRECTION_HORIZONTAL_LEFT ? mGuideImage_Up : (
//                            mDirection[0] == MorphoPanoramaGPWrapper.DIRECTION_HORIZONTAL_RIGHT ? mGuideImage_Down : (
//                                    mDirection[0] == MorphoPanoramaGPWrapper.DIRECTION_VERTICAL_UP ? getRightArrowBitmap() : (
//                                            mDirection[0] == MorphoPanoramaGPWrapper.DIRECTION_VERTICAL_DOWN ? getLeftArrowBitmap() : mGuideImage_Up
//                                    )
//                            ));
//
//                } else {
//                    arrowBitmap = mDirection[0] == MorphoPanoramaGPWrapper.DIRECTION_HORIZONTAL_LEFT ? mGuideImage_Left : (
//                            mDirection[0] == MorphoPanoramaGPWrapper.DIRECTION_HORIZONTAL_RIGHT ? mGuideImage_Right : (
//                                    mDirection[0] == MorphoPanoramaGPWrapper.DIRECTION_VERTICAL_UP ? mGuideImage_Up : (
//                                            mDirection[0] == MorphoPanoramaGPWrapper.DIRECTION_VERTICAL_DOWN ? mGuideImage_Down : mGuideImage_Right
//                                    )
//                            ));
//                }

//                if (arrowBitmap == mGuideImage_Left || arrowBitmap == mGuideImage_Right) {
//                    offset_x += arrowBitmap == mGuideImage_Left ? -mGuideViewMarginPreviewRight : mGuideViewMarginPreviewRight;
//                } else if (arrowBitmap == mGuideImage_Up || arrowBitmap == mGuideImage_Down) {
//                    offset_y += arrowBitmap == mGuideImage_Up ? -mGuideViewMarginPreviewRight : mGuideViewMarginPreviewRight;
//                }

//                if (arrowBitmap == mGuideImage_Right) {
//                    dst.set(attachedPos.x + offset_x + mFrameWidth / 2,
//                            attachedPos.y + offset_y - mGuideViewHeight / 2,
//                            attachedPos.x + offset_x + mFrameWidth / 2 + mGuideViewWidth,
//                            attachedPos.y + offset_y + mGuideViewHeight / 2);
//                } else if (arrowBitmap == mGuideImage_Left) {
//                    dst.set(attachedPos.x + offset_x - mFrameWidth / 2 - mGuideViewWidth,
//                            attachedPos.y + offset_y - mGuideViewHeight / 2,
//                            attachedPos.x + offset_x - mFrameWidth / 2,
//                            attachedPos.y + offset_y + mGuideViewHeight / 2);
//                }

                // Ignore the too fast state now.
//                if (mStatus[0] == MorphoPanoramaGPWrapper.STATUS_WARNING_TOO_FAST) {
//                    if (arrowBitmap == mGuideImage_Left) {
//                        arrowBitmap = BitmapFactory.decodeResource(mAppController.getAndroidContext().getResources(), R.drawable.ic_pano_move_left_fast);
//                    } else {
//                        arrowBitmap = BitmapFactory.decodeResource(mAppController.getAndroidContext().getResources(), R.drawable.ic_pano_move_right_fast);
//                    }
//                }

                Bitmap arrowBitmap = isDirectionLTR() ? mMoveRight : mMoveLeft;
                int offset_x = offsetX, offset_y = offsetY;
                offset_x += isDirectionLTR() ?
                        mGuideViewMarginPreviewRight : -mGuideViewMarginPreviewRight;
                if (isDirectionLTR()) {
                    dst.set(attachedPos.x + offset_x + mFrameWidth / 2,
                            attachedPos.y + offset_y - mGuideViewHeight / 2,
                            attachedPos.x + offset_x + mFrameWidth / 2 + mGuideViewWidth,
                            attachedPos.y + offset_y + mGuideViewHeight / 2);
                } else {
                    dst.set(attachedPos.x + offset_x - mFrameWidth / 2 - mGuideViewWidth,
                            attachedPos.y + offset_y - mGuideViewHeight / 2,
                            attachedPos.x + offset_x - mFrameWidth / 2,
                            attachedPos.y + offset_y + mGuideViewHeight / 2);
                }

                src.set(0, 0, arrowBitmap.getWidth(), arrowBitmap.getHeight());
                canvas.drawBitmap(arrowBitmap, src, dst, null);
            }

            int range = attachedPos.y - mGuidePosY;
            int tip = R.string.tip_for_move_center;
            if (range > CONTROL_RANGE_OF_GUIDE) {
                switch (mOrientation) {
                    case 0:
                        tip = R.string.tip_for_move_up;
                        break;
                    case 180:
                        tip = R.string.tip_for_move_up;
                        break;
                    case 270:
                        if (picRotation == 90) {
                            tip = R.string.tip_for_move_right;
                        } else {
                            tip = R.string.tip_for_move_left;
                        }
                        break;
                    case 90:
                        if (picRotation == 90) {
                            tip = R.string.tip_for_move_left;
                        } else {
                            tip = R.string.tip_for_move_right;
                        }
                        break;
                }
            } else if (range < -CONTROL_RANGE_OF_GUIDE) {
                switch (mOrientation) {
                    case 0:
                        tip = R.string.tip_for_move_down;
                        break;
                    case 180:
                        tip = R.string.tip_for_move_down;
                        break;
                    case 270:
                        if (picRotation == 90) {
                            tip = R.string.tip_for_move_left;
                        } else {
                            tip = R.string.tip_for_move_right;
                        }
                        break;
                    case 90:
                        if (picRotation == 90) {
                            tip = R.string.tip_for_move_right;
                        } else {
                            tip = R.string.tip_for_move_left;
                        }
                }
            } else {
                tip = R.string.tip_for_move_center;
            }

            if (System.currentTimeMillis() >= mEndTimeForSlowDownTips) {
                int drawable = isDirectionLTR() ?
                        R.drawable.ic_move_right : R.drawable.ic_move_left;
                mUI.showInfoText(tip, drawable, drawable);
            }

            if (mImageID[0] < 0) {
                dst.set(offsetX + guidePos.x - hsz, offsetY + guidePos.y - hsz, offsetX + guidePos.x + hsz, offsetY + guidePos.y + hsz);
            }
        }

        // mUI.mPanoramaPreview_ImageView.setImageBitmap(mDispPreviewImage);
        mUI.updateLivePanorama(mDispPreviewImage);
        if (mImageID[0] >= 0) {
            final byte[] data2 = data;
/* MODIFIED-BEGIN by xuan.zhou, 2016-04-21,BUG-1964909*/
//                // Async!
//                mCaptureInfoList.add(new captureInfo(mImageID[0], mStatus[0]));
//
//                //final Handler mHandler = new Handler();
//                mHandler.post(new Runnable() {
//                    public void run() {
//                        if(mPaused){
//                            return;
//                        }
//                        if(mUsePreviewImage == false) {
//                            if (!isZslAvailable()) {
//                                mCameraDevice.setPreviewDataCallback(mHandler,null);
//                            }
//                            else {	// if zsl is available, continue to receive the preview image even while taking picture
////                                mCameraDevice.addCallbackBuffer(mCameraPreviewBuff);
//                            }
//                            mCameraDevice.takePicture(mHandler, null, null,null,getJpegPictureCallback());
//                        } else {
//                            onPictureTakenPreview(data2);
//                        }
//                        if (mIsFinishShooting && !mFinishShootingDone) {
//                            mFinishShootingDone = true;
//                        }
//                    }
//                });
            if (!mPaused) {
                // Only add the info when it's pending to capture
                mCaptureInfoList.add(new captureInfo(mImageID[0], mStatus[0]));

                if (mUsePreviewImage == false) {
                    if (!isZslAvailable()) {
                        mCameraDevice.setPreviewDataCallback(mHandler, null);
                    } else {    // if zsl is available, continue to receive the preview image even while taking picture
//                            mCameraDevice.addCallbackBuffer(mCameraPreviewBuff);
                    }
                    mCameraDevice.takePicture(mHandler, null, null, null, getJpegPictureCallback());
                } else {
                    onPictureTakenPreview(data2);
                }
                if (mIsFinishShooting && !mFinishShootingDone) {
                    mFinishShootingDone = true;
                }

                mRequestTakePicture = true;
                mCntReqShoot++;
                    /*MODIFIED-BEGIN by xuan.zhou, 2016-04-13,BUG-1943018*/
                // Set mEnableStopCapture after mCntReqShoot changed.
                mEnableStopCapture = true;
                    /*MODIFIED-END by xuan.zhou,BUG-1943018*/
            }
                /* MODIFIED-END by xuan.zhou,BUG-1964909*/
        } else {
//                mCameraDevice.addCallbackBuffer(mCameraPreviewBuff);
        }
        mPrevDirection = mDirection[0];
    }


    private Rect getPanoramaGuideRect(int posX, int posY, int offsetX, int offsetY) {
        if (mFrameWidth == 0 || mFrameHeight == 0) {
            return new Rect();
        }
        int dw = mDispPreviewImage.getWidth();
        int dh = mDispPreviewImage.getHeight();
        int left, top, right, bottom;
        left = posX + offsetX - mFrameWidth/2;
        right = posX + offsetX + mFrameWidth/2;
        if (isDirectionLTR()) {
            if (right > dw) {
                right = dw;
                left = right - mFrameWidth;
            }
        } else {
            if (left < 0) {
                left = 0;
                right = left + mFrameWidth;
            }
        }
        // Don't calculate the top/bottom now, because the rect can't be drawn out of the holder.
        // top = posY + offsetY - mFrameHeight/2;
        // bottom = posY + offsetY + mFrameHeight/2;
        top = BORDER_MARGIN;
        bottom = dh - BORDER_MARGIN;
        return new Rect(left, top, right, bottom);
    }

    private Rect getSelfieGuideRect(int direction) {
        if (direction != SELFIE_DIRECTION_LEFT &&
                direction != SELFIE_DIRECTION_RIGHT &&
                direction != SELFIE_DIRECTION_CENTER) {
            return new Rect();
        }
        if (mFrameWidth == 0 || mFrameHeight == 0) {
            return new Rect();
        }
        int dw = mDispPreviewImage.getWidth();
        int dh = mDispPreviewImage.getHeight();
        int left, top, right, bottom;
        if (direction == SELFIE_DIRECTION_LEFT) {
            left = BORDER_MARGIN;
            right = dw/2 - mFrameWidth/2;
        } else if (direction == SELFIE_DIRECTION_RIGHT) {
            left = dw/2 + mFrameWidth/2;
            right = dw - BORDER_MARGIN;
        } else {
            left = dw/2 - mFrameWidth/2;
            right = dw/2 + mFrameWidth/2;
        }
        top = BORDER_MARGIN;
        bottom = dh - BORDER_MARGIN;
        return new Rect(left, top, right, bottom);
    }

    private void drawGuideRect(Bitmap bitmap, Rect rect, int color) {
        if (bitmap == null) {
            return;
        }

        if (rect == null || rect.isEmpty()) {
            return;
        }

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(mGuideBorder);
        if (isRoundCornerPreview()) {
            canvas.drawRoundRect(new RectF(rect), mPreviewRadius, mPreviewRadius, paint);
        } else {
            canvas.drawRect(new RectF(rect), paint);
        }
    }


    private Bitmap getLeftArrowBitmap() {
        Bitmap arrowBitmap;
        if (mDegrees == 180 && mAppController.isReversibleWorking()) {
            // arrowBitmap = mGuideImage_Right;
            arrowBitmap = mMoveRight;
        } else {
            // arrowBitmap = mGuideImage_Left;
            arrowBitmap = mMoveLeft;
        }
        return arrowBitmap;
    }

    private Bitmap getRightArrowBitmap() {
        Bitmap arrowBitmap;
        if (mDegrees == 180 && mAppController.isReversibleWorking()) {
            // arrowBitmap = mGuideImage_Left;
            arrowBitmap = mMoveLeft;
        } else {
            // arrowBitmap = mGuideImage_Right;
            arrowBitmap = mMoveRight;
        }
        return arrowBitmap;
    }

    public class StillProcTask extends Thread {
        private int shootCount = 0;
        final Context context = mActivity.getApplicationContext();

        @Override
        public void run() {
            boolean isExit = false;
            while (mPanoState != PANO_STATE_IDLE) {
                if (mStillProcList.size() > 0) {
                    StillImageData dat = mStillProcList.remove(0);
                    attachImage(dat);
                    shootCount++;
                    NativeMemoryAllocatorWrapper.getInstance().freeBuffer(context,dat.mImage);
                    NativeMemoryAllocatorWrapper.getInstance().freeBuffer(context,dat.mMotionData);
                    mCntProcessd++;
                    /* MODIFIED-BEGIN by xuan.zhou, 2016-04-21,BUG-1964909*/
                    // Sometimes StillProcTask run just after onShutterButtonClick, mIsFinishShooting is true
                    // and the finish shooting will take after StillProcTask finish.
                    if (mIsFinishShooting && mFinishShootingDone) {
                    /* MODIFIED-END by xuan.zhou,BUG-1964909*/
                        // 撮影終亁E�E�Eタンが押された場合、スレチE�E��E�処琁E�E��E�抜けて終亁E�E�E琁E�E��E�呼び出ぁE
                        isExit = true;
                    }
                }
                if (isExit) {
                    finishPanoramaShootingLater(true);
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (mCntReqShoot > mCntProcessd) {
                if (mStillProcList.size() > 0) {
                    StillImageData dat = mStillProcList.remove(0);
                    if (dat == null) {
                        continue;
                    }
                    NativeMemoryAllocatorWrapper.getInstance().freeBuffer(context,dat.mImage);
                    NativeMemoryAllocatorWrapper.getInstance().freeBuffer(context,dat.mMotionData);
                    mCntProcessd++;
                }
            }
        }

        private void attachImage(StillImageData data) {
            if (isSelfie() && mMorphoPanoramaSelfie != null) {
                if (!mUsePreviewImage) {
                    Log.d(TAG, "run attachStillImageExt() start :" + data.mId);
                    int ret;
                    // For panorama selfie, the face info is needed to get better performance.
                    int FaceNum = mUI.getFaceNum();
                    if (FaceNum>0){
                        Rect[] StillRects = mUI.getStillRects();
                        ret = mMorphoPanoramaSelfie.setFaceRect(FaceNum, StillRects);
                        if (ret != Error.MORPHO_OK) {
                            Log.e(TAG, String.format("setFaceRect() -> 0x%x", ret));
                        }
                    }
                    ret = mMorphoPanoramaSelfie.attachStillImageExt(data.mImage, data.mId, data.mMotionData);
                    if (ret != Error.MORPHO_OK) {
                        Log.e(TAG, String.format("attachStillImageExt() -> 0x%x", ret));
                    }
                    if (COPY_EXIF_FROM_1ST_SHOOT && (shootCount == 0)) {
                        mMorphoPanoramaSelfie.attachSetJpegForCopyingExif(data.mImage);
                    }
                } else {
                    Log.d(TAG, "run attachStillImageRaw() start :" + data.mId);
                    int ret = mMorphoPanoramaSelfie.attachStillImageRaw(data.mImage, data.mId, data.mMotionData);
                    if (ret != Error.MORPHO_OK) {
                        Log.e(TAG, String.format("attachStillImageExt() -> 0x%x", ret));
                    }
                }
            } else if (!isSelfie() && mMorphoPanoramaGP != null) {
                if (mUsePreviewImage == false) {
                    int ret = mMorphoPanoramaGP.attachStillImageExt(data.mImage, data.mId, data.mMotionData);
                    if (ret != Error.MORPHO_OK) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                finishPanoramaShooting(false);
                            }
                        });
                    }
                    if (COPY_EXIF_FROM_1ST_SHOOT && (shootCount == 0)) {
                        mMorphoPanoramaGP.attachSetJpegForCopyingExif(data.mImage);
                    }
                } else {
                    int ret = mMorphoPanoramaGP.attachStillImageRaw(data.mImage, data.mId, data.mMotionData);
                }
            }
        }
    }

    private void addStillImage(StillImageData dat) {
        mStillProcList.add(dat);
        if (mStillProcTask == null) {
            mStillProcTask = new StillProcTask();
            mStillProcTask.start();
        }
    }

    @Override
    public CameraAgent.CameraPictureCallback getJpegPictureCallback() {
        Location loc = mActivity.getLocationManager().getCurrentLocation();
        return new JpegPictureCallback(loc);
    }

    private final class JpegPictureCallback implements CameraAgent.CameraPictureCallback {
        Location mLocation;

        public JpegPictureCallback(Location loc) {
            mLocation = loc;
        }

//        public void outputData(byte[] data, String path) {
//            File file = new File(path);
//            try {
//                FileOutputStream o_stream = new FileOutputStream(file);
//                o_stream.write(data);
//                o_stream.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        @Override
        public void onPictureTaken(final byte[] data, final CameraAgent.CameraProxy camera) {
            mRequestTakePicture = false;
            if (mSaveInputImages) {
                String path = String.format("%s/%s/%s/s_%05d.jpg", mSaveInputDirPath, mShootingDate, "still", mPreviewCount);
                FileOperator.outputData(data, path);
            }
            synchronized (mSyncObj) {
                if ((isSelfie() && mMorphoPanoramaSelfie == null) ||
                        (!isSelfie() && mMorphoPanoramaGP == null)) {
                    return;
                }

                /* MODIFIED-BEGIN by xuan.zhou, 2016-04-21,BUG-1964909*/
                if (mCaptureInfoList == null || mCaptureInfoList.size() == 0) {
                /* MODIFIED-END by xuan.zhou,BUG-1964909*/
                    return;
                }

                /* MODIFIED-BEGIN by xuan.zhou, 2016-05-16,BUG-2154135*/
                if (mCntReqShoot == 0) {
                    // Ignore the data captured in the last shot.
                    return;
                }
                /* MODIFIED-END by xuan.zhou,BUG-2154135*/

                captureInfo capInfo = mCaptureInfoList.remove(0);
                if (USE_MULTI_THREAD) {
                    StillImageData still_image_data = new StillImageData(capInfo.mId, mPreviewCount, data, mMotionData);
                    addStillImage(still_image_data);
                } else {
                    // USE_MULTI_THREAD is fixed at true.
//                    int ret = mMorphoPanoramaGP.attachStillImage(data, capInfo.mId, mMotionData);
//                    if (ret != Error.MORPHO_OK) {
//                        //DebugLog.e( LOG_TAG, CLASS + String.format("attachStillImage() -> 0x%x", ret));
//                        finishPanoramaShooting(false);
//                    }
//                    if (mIsFinishShooting) {
//                        // 撮影終亁E�E�Eタンが押された場合、終亁E�E�E琁E�E��E�呼び出ぁE
//                        finishPanoramaShootingLater(true);
//                        return;
//                    }
                }

                switch (capInfo.mStatus) {
                    case MorphoPanoramaGPWrapper.STATUS_OUT_OF_MEMORY:
                    case MorphoPanoramaGPWrapper.STATUS_STOPPED_BY_ERROR:
                    case MorphoPanoramaGPWrapper.STATUS_WHOLE_AREA_COMPLETE:
                        finishPanoramaShooting(true);
                        mCameraDevice.startPreview();
                        break;
                    default:
//                        if (!isZslAvailable()) {
//                            try {
//                                Thread.sleep(50);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
                        /* MODIFIED-BEGIN by xuan.zhou, 2016-04-21,BUG-1964909*/
                        // Sometimes onPictureTaken arrived just after onShutterButtonClick, and the
                        // mPanoCameraState is PANO_PENDING_STOP, I'd like to set another preview data
                        // callback to capture the finish shooting.
                        if (mPanoState == PANO_STATE_RECORDING ||
                                mPanoState == PANO_STATE_RECORDING_PENDING_STOP) {
                                /* MODIFIED-END by xuan.zhou,BUG-1964909*/
                            mPreviewSkipCount = PREVIEW_SKIP_COUNT;
                            mCameraDevice.startPreview();
                            mCameraDevice.setPreviewDataCallback(mHandler, TS_PanoramaGPModule.this);
//                                mCameraDevice.addCallbackBuffer(mCameraPreviewBuff);
//                                mCameraDevice.setPreviewDataCallbackWithBuffer(mHandler,TS_PanoramaGPModule.this);
//                                mCameraDevice.setPreviewDataCallback(mHandler,TS_PanoramaGPModule.this);
                        }
//                        }
                        //mCameraDevice.setPreviewDataCallback(mHandler, TS_PanoramaGPModule.this);
                        break;
                }
            }
        }
    }

    public void onPictureTakenPreview(byte[] data) {
        synchronized (mSyncObj) {
            if (mCameraDevice == null) {
                return;
            }
            if ((isSelfie() && mMorphoPanoramaSelfie == null) ||
                    (!isSelfie() && mMorphoPanoramaGP == null)) {
                return;
            }
            if (mSaveInputImages) {
                String path = String.format("%s/%s/%s/s_%05d.jpg", mSaveInputDirPath, mShootingDate, "still", mPreviewCount);
                FileOperator.outputData(data, path);
            }
            captureInfo capInfo = mCaptureInfoList.remove(0);
            if (USE_MULTI_THREAD) {
                StillImageData still_image_data = new StillImageData(capInfo.mId, mPreviewCount, data, mMotionData);
                addStillImage(still_image_data);
            } else {
//                int ret = mMorphoPanoramaGP.attachStillImage(data, capInfo.mId, mMotionData);
//                if (mIsFinishShooting) {
//                    // 撮影終亁E�E�Eタンが押された場合、終亁E�E�E琁E�E��E�呼び出ぁE
//                    finishPanoramaShootingLater(true);
//                    return;
//                }
            }

            switch (capInfo.mStatus) {
                case MorphoPanoramaGPWrapper.STATUS_OUT_OF_MEMORY:
                case MorphoPanoramaGPWrapper.STATUS_STOPPED_BY_ERROR:
                case MorphoPanoramaGPWrapper.STATUS_WHOLE_AREA_COMPLETE:
                    finishPanoramaShooting(true);
                    mCameraDevice.startPreview();
                    break;
                default:
                    mCameraDevice.startPreview();
                    if (mPanoState != PANO_STATE_IDLE && !mIsFinishShooting) {
//                        mCameraDevice.addCallbackBuffer(mCameraPreviewBuff);
                        mPreviewSkipCount = PREVIEW_SKIP_COUNT;
//                        mCameraDevice.setPreviewDataCallbackWithBuffer(mHandler, this);
                        mCameraDevice.setPreviewDataCallback(mHandler, this);
                    }
                    break;
            }
        }
    }

    private class StillImageData {
        public int mId;
        public int mPreviewCnt;
        public ByteBuffer mImage;
        public ByteBuffer mMotionData;

        StillImageData(int image_id, int preview_cnt, byte[] still_image, byte[] motion_data) {
            mId = image_id;
            mPreviewCnt = preview_cnt;
            mImage = createByteBuffer(still_image);
            mMotionData = createByteBuffer(motion_data);
        }
    }

    private ByteBuffer createByteBuffer(byte[] src) {
        Context context = mActivity.getApplicationContext();
        ByteBuffer bb = NativeMemoryAllocatorWrapper.getInstance().allocateBuffer(context, src.length);
        bb.order(ByteOrder.nativeOrder());
        bb.position(0);
        bb.put(src);
        bb.position(0);
        return bb;
    }

    private void allocateDisplayBuffers() {
        if (mPreviewImage != null) {
            mPreviewImage.recycle();
            mPreviewImage = null;
            mDispPreviewImage.recycle();
            mDispPreviewImage = null;
        }

        if (mPreviewImage == null) {
            mPreviewImage = Bitmap.createBitmap(mInitParam.preview_img_width,
                    mInitParam.preview_img_height, Bitmap.Config.ARGB_8888);
            int livePanoramaWidth = mUI.getLivePanoramaWidth();
            int livePanoramaHeight = mUI.getLivePanoramaHeight();
            livePanoramaHeight *= (100 - mPreviewCroppingRatio * 2) / 100;
            mDispPreviewImage = Bitmap.createBitmap(livePanoramaWidth,
                    livePanoramaHeight, Bitmap.Config.ARGB_8888);
        }
    }


    private void getFrameSize() {
        if (mPreviewImage == null || mDispPreviewImage == null) {
            return;
        }
        int sw = mPreviewImage.getWidth();
        int sh = mPreviewImage.getHeight();
        int dw = mDispPreviewImage.getWidth();
        int dh = mDispPreviewImage.getHeight();
        int fw = mInitParam.preview_height;
        int fh = mInitParam.preview_width;

        float ratio = dw < dh ?
                ((float)dh / sh) : ((float)dw / sw);
        mFrameWidth = (int) (fw * ratio / mInitParam.preview_shrink_ratio);
        mFrameHeight = (int) (fh  * ratio / mInitParam.preview_shrink_ratio);
    }

    private void startPanoramaShooting() {
        if (mCameraDevice == null || isProcessingFinishTask()) {
            return;
        }
        getParametersPictureSize();
        if (mModeSelectionLockToken == null) {
            mModeSelectionLockToken = mAppController.lockModuleSelection();
        }
        mAppController.getLockEventListener().onShutter();
        mAppController.getCameraAppUI().changeBottomBarInCapturePanorama();
        //isInCapture = true;
//        mTS_PanoramaGPModuleUI.setTipTextViewVisiblity(false);
        //mCameraDevice.setPreviewDataCallbackWithBuffer(mHandler, null);
        if (isSelfie()) {
            initPanoramaSelfieParameter();
        } else {
            initPanoramaParameter();
        }
        //
        setFocusAndLockAeAwb();
        // setAutoExposureLock(true);
        // setAutoWhiteBalanceLock(true);

       if (isSelfie()) {
           startMorphoPanoramaSelfie();
       } else {
           startMorphoPanorama();
       }

//        mCameraPreviewBuff = new byte[mCameraPreviewW * mCameraPreviewH * 3 / 2];
        mDirection[0] = mInitParam.direction;
        mPrevDirection = mInitParam.direction;
        mPreviewCount = -1;
        mCntReqShoot = 0;
        mCntProcessd = 0;
        mCaptureInfoList.clear();
        mStillProcList.clear(); // MODIFIED by xuan.zhou, 2016-05-16,BUG-2154135
        //mIsShooting = true;
        getFrameSize();

        setPanoramaState(PANO_STATE_RECORDING);
        //mUI.showProcessingUI();
        mUI.cleanLivePanorama();
        if (isSelfie()) {
            mUI.transitionToMode(PANO_MODE_SELFIE_CAPTURE);
        } else {
            mUI.transitionToMode(PANO_MODE_CAPTURE);
        }

        startAudioRecording();

//        mCameraDevice.addCallbackBuffer(mCameraPreviewBuff);
        //mCameraDevice.setPreviewDataCallbackWithBuffer(mHandler, this);

        if (mSaveInputImages) {
            String pr_save_path = String.format("%s/%s/%s", mSaveInputDirPath, mShootingDate, "preview");
            File pr_file = new File(pr_save_path);
            if (!pr_file.exists()) {
                pr_file.mkdirs();
            }
            String st_save_path = String.format("%s/%s/%s", mSaveInputDirPath, mShootingDate, "still");
            File st_file = new File(st_save_path);
            if (!st_file.exists()) {
                st_file.mkdirs();
            }
        }
    }

    protected double getMaxAngle() {
        if (isSelfie()) {
            return MAX_ANGLE_SELFIE;
        }
        return MAX_ANGLE;
    }

    protected Size getDstImageSize() {
        if (mInitParam == null) {
            return null;
        }
        return new Size(mInitParam.dst_img_width, mInitParam.dst_img_height);
    }

    private void initPanoramaParameter() {
        if (mMorphoPanoramaGP != null) {
            return;
        }
        int disp_w = mActivity.getWindowManager().getDefaultDisplay().getWidth();
        int disp_h = mActivity.getWindowManager().getDefaultDisplay().getHeight();
        int shrink_ratio;
        int[] buff_size = new int[1];
        mMorphoPanoramaGP = new MorphoPanoramaGPWrapper(mActivity.getApplicationContext());
        mInitParam = new InitParam();
        mInitParam.format = FORMAT;
        //mInitParam.mode           = MorphoPanoramaGPWrapper.GUIDE_TYPE_HORIZONTAL;
        mInitParam.use_threshold = mUseThres;

        mInitParam.preview_width = mCameraPreviewW;
        mInitParam.preview_height = mCameraPreviewH;
        if (mUsePreviewImage == false) {
            mInitParam.still_width = mCameraPictureW;
            mInitParam.still_height = mCameraPictureH;
        } else {
            mInitParam.still_width = mCameraPreviewW;
            mInitParam.still_height = mCameraPreviewH;
        }
        mInitParam.angle_of_view_degree = mAngleOfViewDegree;
        //mInitParam.draw_cur_image = 1;
        mInitParam.draw_cur_image = 0; //TODO for nostopUI by y-li

        mPreviewCroppingAdjustByAuto = 0;
        mGuidePosY = 0;
        switch (mAppDeviceRotation + mAppPanoramaDirection) {
            case APP_DIRECTION_PORTRAIT_VERTICAL:
                mInitParam.direction = MorphoPanoramaGPWrapper.DIRECTION_HORIZONTAL;
                mInitParam.dst_img_width = mInitParam.still_height;
                mInitParam.dst_img_height = mInitParam.still_width * 10;
                mInitParam.preview_img_width = mInitParam.preview_height;
                mInitParam.preview_img_height = mInitParam.preview_width * 10;
                switch (picRotation) {
                    case 270:
                        mInitParam.output_rotation = 270;
                        break;
                    default:
                        mInitParam.output_rotation = 90;
                        break;
                }
                shrink_ratio = Math.max(mInitParam.preview_img_height / disp_h - 1, 1);
                break;
            case APP_DIRECTION_LANDSCAPE_HORIZONTAL:
                mInitParam.direction = MorphoPanoramaGPWrapper.DIRECTION_HORIZONTAL;
                mInitParam.dst_img_width = mInitParam.still_width * 10;
                mInitParam.dst_img_height = mInitParam.still_height;
                mInitParam.preview_img_width = mInitParam.preview_width * 10;
                mInitParam.preview_img_height = mInitParam.preview_height;
                switch (picRotation) {
                    case 180:
                        mInitParam.output_rotation = 180;
                        break;
                    default:
                        mInitParam.output_rotation = 0;
                        break;
                }
                shrink_ratio = Math.max(mInitParam.preview_img_width / disp_w - 1, 1);
                break;
            case APP_DIRECTION_PORTRAIT_HORIZONTAL:
                if (mDegrees == 180 && mAppController.isReversibleWorking()) {
                    mInitParam.direction = !mForce_PanoramaDirection_HORIZONTAL_RIGHT ? MorphoPanoramaGPWrapper.DIRECTION_VERTICAL_UP : MorphoPanoramaGPWrapper.DIRECTION_VERTICAL_DOWN;//MorphoPanoramaGPWrapper.DIRECTION_VERTICAL;
                } else {
                    mInitParam.direction = mForce_PanoramaDirection_HORIZONTAL_RIGHT ? MorphoPanoramaGPWrapper.DIRECTION_VERTICAL_UP : MorphoPanoramaGPWrapper.DIRECTION_VERTICAL_DOWN;//MorphoPanoramaGPWrapper.DIRECTION_VERTICAL;
                }
                mInitParam.dst_img_width = mInitParam.still_height * 10;
                mInitParam.dst_img_height = mInitParam.still_width;
                mInitParam.preview_img_width = mInitParam.preview_height * 10;
                mInitParam.preview_img_height = mInitParam.preview_width;
                switch (picRotation) {
                    case 270:
                        mInitParam.output_rotation = 270;
                        break;
                    default:
                        mInitParam.output_rotation = 90;
                        break;
                }
                shrink_ratio = SCALE_PREVIEW_HEIGHT;
                break;
            case APP_DIRECTION_LANDSCAPE_VERTICAL:
            default:
                mInitParam.direction = MorphoPanoramaGPWrapper.DIRECTION_VERTICAL;
                mInitParam.dst_img_width = mInitParam.still_width;
                mInitParam.dst_img_height = mInitParam.still_height * 10;
                mInitParam.preview_img_width = mInitParam.preview_width;
                mInitParam.preview_img_height = mInitParam.preview_height * 10;
                switch (picRotation) {
                    case 180:
                        mInitParam.output_rotation = 180;
                        break;
                    default:
                        mInitParam.output_rotation = 0;
                        break;
                }
                shrink_ratio = Math.max(mInitParam.preview_img_height / disp_h - 1, 1);
                break;
            case APP_DIRECTION_PORTRAIT_AUTO:
                mInitParam.direction = MorphoPanoramaGPWrapper.DIRECTION_AUTO;
                mInitParam.preview_width = mCameraPreviewW;
                mInitParam.preview_height = mCameraPreviewH;
                if (mUsePreviewImage == false) {
                    mInitParam.still_width = mCameraPictureW;
                    mInitParam.still_height = mCameraPictureH;
                } else {
                    mInitParam.still_width = mCameraPreviewW;
                    mInitParam.still_height = mCameraPreviewH;
                }
                mInitParam.dst_img_width = mInitParam.still_height;
                mInitParam.dst_img_height = mInitParam.still_width * 10;
                mInitParam.preview_img_width = mInitParam.preview_height;
                mInitParam.preview_img_height = mInitParam.preview_width * 10;
                switch (picRotation) {
                    case 270:
                        mInitParam.output_rotation = 270;
                        break;
                    default:
                        mInitParam.output_rotation = 90;
                        break;
                }
                shrink_ratio = Math.max(mInitParam.preview_img_height / disp_h - 1, 1);
                break;
            case APP_DIRECTION_LANDSCAPE_AUTO:
                mInitParam.direction = MorphoPanoramaGPWrapper.DIRECTION_AUTO;
                mInitParam.dst_img_width = mInitParam.still_width * 10;
                mInitParam.dst_img_height = mInitParam.still_height;
                mInitParam.preview_img_width = mInitParam.preview_width * 10;
                mInitParam.preview_img_height = mInitParam.preview_height;
                switch (picRotation) {
                    case 180:
                        mInitParam.output_rotation = 180;
                        break;
                    default:
                        mInitParam.output_rotation = 0;
                        break;
                }
                shrink_ratio = Math.max(mInitParam.preview_img_width / disp_w - 1, 1);
                break;
        }

        mInitParam.preview_shrink_ratio = shrink_ratio;
        mMorphoPanoramaGP.calcImageSize(mInitParam, getMaxAngle());
        if (MAX_DST_IMG_WIDTH < mInitParam.dst_img_width) {
            float scale = (float) MAX_DST_IMG_WIDTH / mInitParam.dst_img_width;
            mInitParam.dst_img_width = MAX_DST_IMG_WIDTH;
            mInitParam.preview_img_width *= scale;
        }
        if (MAX_DST_IMG_WIDTH < mInitParam.dst_img_height) {
            float scale = (float) MAX_DST_IMG_WIDTH / mInitParam.dst_img_height;
            mInitParam.dst_img_height = MAX_DST_IMG_WIDTH;
            mInitParam.preview_img_height *= scale;
        }
        mInitParam.preview_img_width &= ~1;
        mInitParam.preview_img_height &= ~1;
        mMorphoPanoramaGP.initialize(mInitParam, buff_size);
    }

    private void initPanoramaSelfieParameter() {
        // May use preview when selfie.
        // mUsePreviewImage = true;
        int ret;
        int disp_w = mActivity.getWindowManager().getDefaultDisplay().getWidth();
        int disp_h = mActivity.getWindowManager().getDefaultDisplay().getHeight();
        int shrink_ratio;
        isCenterFrame = true;
        mAttachedLeft = 0;
        mAttachedRight = 0;
        mAttachedMiddle = 0;
        mGuideLeft = 0;
        mGuideRight = 0;
        mSelfieDirection = SELFIE_DIRECTION_CENTER;
        boolean mReverseDirGuide;
        if (mMorphoPanoramaSelfie == null) {
            int[] buff_size = new int[1];
            mMorphoPanoramaSelfie = new MorphoPanoramaSelfie();
            mInitParam = new InitParam();
            mInitParam.format         = FORMAT;
            mInitParam.use_threshold = mSelfieUseThres;

            mInitParam.preview_width  = mCameraPreviewW;
            mInitParam.preview_height = mCameraPreviewH;
            if(!mUsePreviewImage) {
                mInitParam.still_width    = mCameraPictureW;
                mInitParam.still_height   = mCameraPictureH;
            } else {
                mInitParam.still_width    = mCameraPreviewW;
                mInitParam.still_height   = mCameraPreviewH;
            }
            mInitParam.angle_of_view_degree = mAngleOfViewDegree;
            //mInitParam.draw_cur_image = 1;
            mInitParam.draw_cur_image = 0; //TODO for nostopUI by y-li

            Camera.CameraInfo info =  new Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(getCameraId(), info);

            int degrees = 0;
            switch (mActivity.getWindowManager().getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_0: degrees = 0; break;
                case Surface.ROTATION_90: degrees = 90; break;
                case Surface.ROTATION_180: degrees = 180; break;
                case Surface.ROTATION_270: degrees = 270; break;
            }
            int tmpDegrees = (info.orientation - degrees + 360) % 360;

            mPreviewCroppingAdjustByAuto = 0;
            // mAppPanoramaDirection = mAppPanoramaDirectionSettings;
//            mGuidePosY = 0;

            switch(mAppDeviceRotation+mAppPanoramaDirection) {
                case APP_DIRECTION_PORTRAIT_VERTICAL:
                    mInitParam.direction = MorphoPanoramaSelfie.DIRECTION_HORIZONTAL_CENTER;
                /* カメラの向きを0°と見た場合、横長の画像になるが、90度回転指定する場合は回転した後のサイズ（つまり縦長）を渡すみたい */
                    mInitParam.dst_img_width  = mInitParam.still_height;
                    mInitParam.dst_img_height = mInitParam.still_width*10;
                    mInitParam.preview_img_width  = mInitParam.preview_height;
                    mInitParam.preview_img_height = mInitParam.preview_width*10;
                    switch (tmpDegrees) {
                        case 270:     mInitParam.output_rotation = 270; mReverseDirGuide = true;  break;
                        default:    mInitParam.output_rotation =  90; mReverseDirGuide = false; break;
                    }
                    shrink_ratio = Math.max( mInitParam.preview_img_width / disp_h - 1, 1);
                    break;
                case APP_DIRECTION_LANDSCAPE_HORIZONTAL:
                    mInitParam.direction = MorphoPanoramaSelfie.DIRECTION_HORIZONTAL_CENTER;
                    mInitParam.dst_img_width  = mInitParam.still_width*10;
                    mInitParam.dst_img_height = mInitParam.still_height;
                    mInitParam.preview_img_width  = mInitParam.preview_width*10;
                    mInitParam.preview_img_height = mInitParam.preview_height;
                    switch (tmpDegrees) {
                        case 180:     mInitParam.output_rotation = 180; mReverseDirGuide = true;  break;
                        default:    mInitParam.output_rotation =   0; mReverseDirGuide = false; break;
                    }
                    shrink_ratio = Math.max( mInitParam.preview_img_width / disp_w - 1, 1);
                    break;
                case APP_DIRECTION_PORTRAIT_HORIZONTAL:
                    mInitParam.direction = MorphoPanoramaSelfie.DIRECTION_VERTICAL_CENTER;
                /* カメラの向きを0°と見た場合、縦長の画像になるが、90度回転指定する場合は回転した後のサイズ（つまり横長）を渡すみたい */
                    mInitParam.dst_img_width  = mInitParam.still_height*10;
                    mInitParam.dst_img_height = mInitParam.still_width;
                    mInitParam.preview_img_width  = mInitParam.preview_height*10;
                    mInitParam.preview_img_height = mInitParam.preview_width;
                    switch (tmpDegrees) {
                        case 270:     mInitParam.output_rotation = 90; mReverseDirGuide = true;  break;
                        default:    mInitParam.output_rotation =  90; mReverseDirGuide = false; break;
                    }
                    shrink_ratio = Math.max( mInitParam.preview_img_width / disp_w - 1, 1);
                    break;
                case APP_DIRECTION_LANDSCAPE_VERTICAL:
                default:
                    mInitParam.direction = MorphoPanoramaSelfie.DIRECTION_VERTICAL_CENTER;
                    mInitParam.dst_img_width  = mInitParam.still_width;
                    mInitParam.dst_img_height = mInitParam.still_height*10;
                    mInitParam.preview_img_width  = mInitParam.preview_width;
                    mInitParam.preview_img_height = mInitParam.preview_height*10;
                    switch (tmpDegrees) {
                        case 180: mInitParam.output_rotation = 180; mReverseDirGuide = true;  break;
                        default:  mInitParam.output_rotation =   0; mReverseDirGuide = false; break;
                    }
                    shrink_ratio = Math.max( mInitParam.preview_img_height / disp_h - 1, 1);
                    break;
                case APP_DIRECTION_PORTRAIT_AUTO:
                    mInitParam.direction = MorphoPanoramaSelfie.DIRECTION_AUTO;
                /* カメラの向きを0°と見た場合、縦長の画像になるが、90度回転指定する場合は回転した後のサイズ（つまり横長）を渡すみたい */
                    mInitParam.preview_width  = mCameraPreviewW;
                    mInitParam.preview_height = mCameraPreviewH;
                    if(mUsePreviewImage == false) {
                        mInitParam.still_width    = mCameraPictureW;
                        mInitParam.still_height   = mCameraPictureH;
                    } else {
                        mInitParam.still_width    = mCameraPreviewW;
                        mInitParam.still_height   = mCameraPreviewH;
                    }
                    mInitParam.dst_img_width  = mInitParam.still_height;
                    mInitParam.dst_img_height = mInitParam.still_width*10;
                    mInitParam.preview_img_width  = mInitParam.preview_height;
                    mInitParam.preview_img_height = mInitParam.preview_width*10;
                    switch (tmpDegrees) {
                        case 270:     mInitParam.output_rotation = 270; mReverseDirGuide = true;  break;
                        default:    mInitParam.output_rotation =  90; mReverseDirGuide = false; break;
                    }
                    shrink_ratio = Math.max( mInitParam.preview_img_height / disp_h - 1, 1);
                    break;
                case APP_DIRECTION_LANDSCAPE_AUTO:
                    mInitParam.direction = MorphoPanoramaSelfie.DIRECTION_AUTO;
                    mInitParam.dst_img_width  = mInitParam.still_width*10;
                    mInitParam.dst_img_height = mInitParam.still_height;
                    mInitParam.preview_img_width  = mInitParam.preview_width*10;
                    mInitParam.preview_img_height = mInitParam.preview_height;
                    switch (tmpDegrees) {
                        case 180:     mInitParam.output_rotation = 180; mReverseDirGuide = true;  break;
                        default:    mInitParam.output_rotation =   0; mReverseDirGuide = false; break;
                    }
                    shrink_ratio = Math.max( mInitParam.preview_img_width / disp_w - 1, 1);
                    break;
            }

            if (tmpDegrees == 0 || tmpDegrees == 180) {
                mInitParam.output_rotation = (mInitParam.output_rotation + 180) % 360;
            }

            mInitParam.preview_shrink_ratio = shrink_ratio;
            MorphoPanoramaSelfie.calcImageSize(mInitParam, getMaxAngle());

            if (MAX_DST_IMG_WIDTH < mInitParam.dst_img_width) {
                float scale = (float)MAX_DST_IMG_WIDTH / mInitParam.dst_img_width;
                mInitParam.dst_img_width = MAX_DST_IMG_WIDTH;
                mInitParam.preview_img_width *= scale;
            }
            if (MAX_DST_IMG_WIDTH < mInitParam.dst_img_height) {
                float scale = (float)MAX_DST_IMG_WIDTH / mInitParam.dst_img_height;
                mInitParam.dst_img_height = MAX_DST_IMG_WIDTH;
                mInitParam.preview_img_height *= scale;
            }
            mInitParam.preview_img_width  &= ~1;
            mInitParam.preview_img_height &= ~1;

            ret = mMorphoPanoramaSelfie.initialize(mInitParam, buff_size);
            if (ret != Error.MORPHO_OK) {
                Log.e(TAG, String.format("initialize() -> 0x%x", ret));
            }
        }
    }

    private void startMorphoPanorama() {
        if (isSelfie() || mMorphoPanoramaGP == null) {
            return;
        }
        mMorphoPanoramaGP.setMotionlessThreshold(mMotionlessThres);
        mMorphoPanoramaGP.setUseSensorThreshold(mUseSensorThres);
        mMorphoPanoramaGP.setTooFastThreshold(FASE_THRESHOLD);
        allocateDisplayBuffers();
        mShootingDate = getDateString(System.currentTimeMillis());
        playRecordSound();
        mMorphoPanoramaGP.setUseSensorAssist(MorphoPanoramaGPWrapper.USE_SENSOR_FOR_ALIGNMENT_WHEN_FAILED, 0);
        mMorphoPanoramaGP.start();
    }

    private void startMorphoPanoramaSelfie() {
        if (!isSelfie() || mMorphoPanoramaSelfie == null) {
            return;
        }
        mMorphoPanoramaSelfie.setMotionlessThreshold(mMotionlessThres_Selfie);
        mMorphoPanoramaSelfie.setUseSensorThreshold(mUseSensorThres);
        // No this interface.
        // mMorphoPanoramaGP.setTooFastThreshold(FASE_THRESHOLD);
        allocateDisplayBuffers();
        mShootingDate = getDateString(System.currentTimeMillis());
        // Selfie play sound when picture taken.
        // playRecordSound();
        mMorphoPanoramaSelfie.setUseSensorAssist(MorphoPanoramaGPWrapper.USE_SENSOR_FOR_ALIGNMENT_WHEN_FAILED, 0);
        mMorphoPanoramaSelfie.start();
    }

    //private boolean mIsInSaveProgress = false;
    private final static int SAVE_MAX_PROGRESS = 120;
    private final static int GP_MAX_PROGRESS_VALUE = 84;
    private final static int STEP_OF_PROGRESS = 12;
    private final static int TIME_DELAY = 10;
    private final static int FINISH_TIME_DELAY = 80;

    public static final String PANORAMA_PREFIX = "PANO_";
    public static final String AUDIO_PREFIX = "AUDIO_";
    public static final String JPEG_EXTENSION = ".jpg";
    public static final String MP4_EXTENSION = ".mp4";

    public String getBasePath() {
        return mSaveBaseDirPath;
    }

    public String getPanoramaJpgFileName() {
        return PANORAMA_PREFIX + mShootingDate + JPEG_EXTENSION;
    }

    public String getAudioMp4FileName() {
        return AUDIO_PREFIX + mShootingDate + MP4_EXTENSION;
    }

    private class SaveOutputImageTask extends AsyncTask<Void, Integer, Integer> {

        private Context mContext;
        boolean mSaveImage;
        long start_time;
        int oldValue;
        int oldProgress;
        private Runnable mRunnable;

        SaveOutputImageTask(Context context, boolean SaveImage) {
            mContext = context;
            mSaveImage = SaveImage;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            int ret;
            if (USE_MULTI_THREAD) {
                finishAttachStillImageTask();
            }
            if (isSelfie()) {
                ret = mMorphoPanoramaSelfie.end();
            } else {
                ret = mMorphoPanoramaGP.end();
            }
            if (mSaveImage) {
                if ((mSaveOutputType & SAVE_OUTPUT_BOUNDING) > 0) {
                    Rect b_rect = new Rect();
                    ret = isSelfie() ?
                            mMorphoPanoramaSelfie.getBoundingRect(b_rect) :
                            mMorphoPanoramaGP.getBoundingRect(b_rect);
                    String b_file_path = mSaveBaseDirPath + "/" + mShootingDate + "_bounding.jpg";
                    saveOutputJpeg(b_file_path, b_rect, progress);
                }

                if ((mSaveOutputType & SAVE_OUTPUT_CLIPPING) > 0) {
                    Rect c_rect = new Rect();
                    ret = isSelfie() ?
                            mMorphoPanoramaSelfie.getClippingRect(c_rect) :
                            mMorphoPanoramaGP.getClippingRect(c_rect);
                    /*MODIFIED-BEGIN by xuan.zhou, 2016-04-13,BUG-1943018*/
                    if (c_rect.isEmpty() || c_rect.bottom < 0) {
                        return null;
                    }
                    /*MODIFIED-END by xuan.zhou,BUG-1943018*/
                    String c_file_path = getBasePath() + "/" + getPanoramaJpgFileName();
                    saveOutputJpeg(c_file_path, c_rect, progress);
                }
            }
            return null;
        }

        protected void onPreExecute() {
            //mIsInSaveProgress = true;
            mUI.hideInfoText();
            setPanoramaState(PANO_STATE_SAVING);
            if (mSaveImage) {
                mUI.startSaveProgress();
                start_time = System.currentTimeMillis();
                oldValue = 0;
                oldProgress = 0;
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        /*if (progress[0] != GP_MAX_PROGRESS_VALUE) {
                            if (oldValue != progress[0]) {
                                oldValue = progress[0];
                                oldProgress += SAVE_MAX_PROGRESS / STEP_OF_PROGRESS;
                                mTS_PanoramaGPModuleUI.setSaveProgress(oldProgress);
                            }
                            mHandler.postDelayed(this, TIME_DELAY);
                        } else {
                            mTS_PanoramaGPModuleUI.setSaveProgress(SAVE_MAX_PROGRESS);
                        }*/
                        if (oldProgress < GP_MAX_PROGRESS_VALUE) {
                            oldProgress++;
                            mUI.setSaveProgress(oldProgress);
                            mHandler.postDelayed(this, TIME_DELAY);
                        } else {
                            mUI.setSaveProgress(SAVE_MAX_PROGRESS);
                        }
                    }
                };
                mHandler.post(mRunnable);
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (!mPaused) {
                setPanoramaState(PANO_STATE_IDLE);
            } else {
                setPanoramaState(PANO_STATE_PREVIEW_STOPPED);
            }
            mIsFinishShooting = false;
            if (mSaveImage && mLastPhotoUri != null) {
                mThumbUpdating = !mPaused; //MODIFIED by xuan.zhou, 2016-04-07,BUG-1920473
                //mOnMediaSavedListener.onMediaSaved(mLastPhotoUri);
                // int notifyAction=AppController.NOTIFY_NEW_MEDIA_ACTION_ANIMATION|AppController.NOTIFY_NEW_MEDIA_ACTION_UPDATETHUMB;
                mActivity.notifyNewMedia(mLastPhotoUri, AppController.NOTIFY_NEW_MEDIA_ACTION_NONE);
                new AsyncTask<Void, Void, Bitmap>() {

                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        /*MODIFIED-BEGIN by xuan.zhou, 2016-04-07,BUG-1920473*/
                        return Thumbnail.getBitmapFromUri(mActivity.getContentResolver(), mLastPhotoUri, mSavePicOrientation);
                    }

                    @Override
                    protected void onPostExecute(Bitmap bitmap) {
                        if (mPaused) return;
                        mActivity.getCameraAppUI().showPeek(true);
                        mActivity.getCameraAppUI().showSwitchButton(true);
                        if (bitmap != null) {
                            mActivity.getCameraAppUI().updatePeekThumbBitmapWithAnimation(bitmap);
                            mActivity.getCameraAppUI().updatePeekThumbUri(mLastPhotoUri);
                        }
                        mThumbUpdating = false;
                    }

                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }

            mAppController.getCameraAppUI().restoreBottomBarFinishPanorama(mThumbUpdating);
            /*MODIFIED-END by xuan.zhou,BUG-1920473*/
            //isInCapture = false;
            progress[0] = 0;
            //mIsInSaveProgress = false;
            //mTS_PanoramaGPModuleUI.stopSaveProgress();
            mHandler.removeCallbacks(mRunnable);
            mUI.setSaveProgress(SAVE_MAX_PROGRESS);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mUI.stopSaveProgress();
                }
            }, FINISH_TIME_DELAY);
            if (isSelfie()) {
                mMorphoPanoramaSelfie.finish();
                mMorphoPanoramaSelfie = null;
            } else {
                mMorphoPanoramaGP.finish();
                mMorphoPanoramaGP = null;
            }
            mContext = null;
            //mIsShooting = false;
            // mUI.mPanoramaPreview_ImageView.setImageBitmap(null);
            mUI.cleanLivePanorama();
            if (mModeSelectionLockToken != null) {
                mAppController.unlockModuleSelection(mModeSelectionLockToken);
                mModeSelectionLockToken = null;
            }
            if (mPaused) {
                return;
            }

            if (mCameraDevice != null) {
                restoreFocusAndUnlockAeAwb();
                // setAutoExposureLock(false);
                // setAutoWhiteBalanceLock(false);
                mCameraDevice.startPreview();
            }

            //mCameraPreviewBuff = null;

            // mUI.showSelectDirectionUI(mForce_PanoramaDirection_HORIZONTAL_RIGHT);
            mUI.cleanPreviewFrame(isSelfie(), isDirectionLTR());
            if (isSelfie()) {
                mUI.transitionToMode(PANO_MODE_SELFIE_PREVIEW);
            } else if (isDirectionLTR()) {
                mUI.transitionToMode(PANO_MODE_PREVIEW_LTR);
            } else {
                mUI.transitionToMode(PANO_MODE_PREVIEW_RTL);
            }
            mAppController.getLockEventListener().onIdle();
        }
    }

    private void finishPanoramaShooting(boolean save_image) {
        if (mCameraDevice == null) {
            return;
        }
        if ((isSelfie() && mMorphoPanoramaSelfie == null) ||
                (!isSelfie() && mMorphoPanoramaGP == null)) {
            return;
        }
        if (isProcessingFinishTask()) {
            return;
        }

        stopAudioRecording();

        /* MODIFIED-BEGIN by xuan.zhou, 2016-05-25,BUG-2167363*/
        boolean muteEnd = CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_PANORAMA_MUTE_END, false);
        if ((!muteEnd || save_image) && !isSelfie()) {
            playRecordSound();
        }
        /* MODIFIED-END by xuan.zhou,BUG-2167363*/
        //mCameraDevice.setPreviewDataCallbackWithBuffer(mHandler, null);
//        if (!save_image) {
//            mIsShooting = false;
//        }
        if (mPrevDirection == MorphoPanoramaGPWrapper.DIRECTION_HORIZONTAL ||
                mPrevDirection == MorphoPanoramaGPWrapper.DIRECTION_VERTICAL) {
            save_image = false;
        }
        mSaveOutputImageTask = new SaveOutputImageTask(mActivity, save_image);
        mSaveOutputImageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private boolean isProcessingFinishTask() {
        if (mSaveOutputImageTask != null && mSaveOutputImageTask.getStatus() != AsyncTask.Status.FINISHED) {
            return true;
        }
        return false;
    }

    private void finishAttachStillImageTask() {
        int waitTime = 0;
        while (mCntReqShoot > mCntProcessd) {
            try {
                Thread.sleep(100);
                waitTime += 100;
                if (waitTime >= MAX_WAIT_TIME) {
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        mStillProcTask = null;
    }


    private void saveOutputJpeg(String path, Rect rect, int[] progress) {
        int orientation = mSavePicOrientation;
        int ret;
        if (isSelfie()) {
            boolean isMirror = Keys.isMirrorSelfieOn(mAppController.getSettingsManager());
            ret = mMorphoPanoramaSelfie.saveOutputJpeg(path, rect, orientation, isMirror);
        } else {
            ret = mMorphoPanoramaGP.saveOutputJpeg(path, rect, orientation, progress);
        }
        addImageAsApplication(path, orientation, rect);
    }

    private void addImageAsApplication(String file_path, int orientation, Rect rect) {
        writeMetadata(file_path, rect);
        Location location = null;
        if (mActivity != null && mActivity.getLocationManager() != null) {
            location = mActivity.getLocationManager().getCurrentLocation();
        }
        if (!COPY_EXIF_FROM_1ST_SHOOT) {
            Map<String, Object> externalInfo = buildExternalBundle();
            String externalJson = CameraUtil.serializeToJson(externalInfo);
            // externalJson added by JpegHandler doesn't work, rewrite it by exif/ExifInterface.
            // JpegHandler.setExifData(file_path, location, 0, externalJson);
            JpegHandler.setExifData(file_path, location, 0, null);
            if (externalJson != null) {
                rewriteExternalJson(file_path, externalJson);
            }
        }
        mLastPhotoUri = MediaProviderUtils.addImageExternal(mActivity.getContentResolver(),
                file_path, "image/jpeg", orientation, location, rect);
    }

    protected void writeMetadata(String file_path, Rect rect) {
        // No metadata need to be written in normal or selfie panorama.
    }

    private void rewriteExternalJson(String file_path, String externalJson) {
        ExifInterface exifInterface = new ExifInterface();
        try {
            exifInterface.readExif(file_path);
            ExifTag userTag = exifInterface.buildTag(ExifInterface.TAG_USER_COMMENT, externalJson);
            exifInterface.setTag(userTag);
            exifInterface.forceRewriteExif(file_path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getDateString(long dateTaken) {
        return DateFormat.format("yyyy-MM-dd_kk-mm-ss", dateTaken).toString();
    }

    private boolean isZslAvailable() {
//        if( mCameraDevice == null)
//            return false;
//        Camera.Parameters parameters = mCameraDevice.getCamera().getParameters();
//        if( parameters != null){
//            String s = parameters.get("zsl");
//            if (s != null && s.contains("on"))
//                return true;
//            else{
//                parameters.set("zsl", "on");
//                parameters.set("zsd-mode", "on");
//                mCameraDevice.getCamera().setParameters(parameters);
//            }
//            parameters = mCameraDevice.getCamera().getParameters();
//            s = parameters.get("zsl");
//            if (s != null && s.contains("on"))
//                return true;
//        }
        return mCameraSettings.isZslOn;
//        return false;
    }

    private void setAeAwbLock(boolean lock) {
        if (mCameraDevice == null) return;
        if (mCameraSettings == null) {
            mCameraSettings = mCameraDevice.getSettings();
        }

        if (mAeLockSupported) {
            mCameraSettings.setAutoExposureLock(lock);
        }
        if (mAwbLockSupported) {
            mCameraSettings.setAutoWhiteBalanceLock(lock);
        }

        if (mAeLockSupported || mAwbLockSupported) {
            mCameraDevice.applySettings(mCameraSettings);
        }
    }

    private boolean isAutoExposureLockSupported() {
        if (mPaused) {
            return false;
        }
        Camera.Parameters parameters = null;
        Method methodIsAutoExposureLockSupported;
        boolean isSupported = false;
        try {
            parameters = mCameraDevice.getCamera().getParameters();
            methodIsAutoExposureLockSupported = parameters.getClass().getMethod("isAutoExposureLockSupported", new Class[]{});
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
        } else {
        }
        return isSupported;
    }

    private boolean isAutoWhiteBalanceLockSupported() {
        if (mPaused) {
            return false;
        }
        Camera.Parameters parameters = null;
        Method methodIsAutoWhiteBalanceLockSupported;
        boolean isSupported = false;
        try {
            parameters = mCameraDevice.getCamera().getParameters();
            methodIsAutoWhiteBalanceLockSupported = parameters.getClass().getMethod("isAutoWhiteBalanceLockSupported", new Class[]{});
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
        } else {
        }
        return isSupported;
    }

    public void setAutoExposureLock(boolean lock) {
        if (mCameraDevice.getCamera() == null) return;
        if (!isAutoExposureLockSupported()) {
            return;
        }
        Camera.Parameters parameters = null;
        Method methodSetAutoExposureLock;
        try {
            parameters = mCameraDevice.getCamera().getParameters();
            methodSetAutoExposureLock = parameters.getClass().getMethod("setAutoExposureLock", new Class[]{boolean.class});
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
        } else {
        }
    }

    public void setAutoWhiteBalanceLock(boolean lock) {
        if (mCameraDevice.getCamera() == null) return;
        if (!isAutoWhiteBalanceLockSupported()) {
            return;
        }
        Camera.Parameters parameters = null;
        Method methodSetAutoWhiteBalanceLock;
        try {
            parameters = mCameraDevice.getCamera().getParameters();
            methodSetAutoWhiteBalanceLock = parameters.getClass().getMethod("setAutoWhiteBalanceLock", new Class[]{boolean.class});
        } catch (Exception e) {
            methodSetAutoWhiteBalanceLock = null;
        }

        if (methodSetAutoWhiteBalanceLock != null) {
            try {
                methodSetAutoWhiteBalanceLock.invoke(parameters, lock);
                mCameraDevice.getCamera().setParameters(parameters);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
        }
    }

    private NV21Convertor mConvertor;
    private Object mNV21ConvertorLock = new Object();

    @Override
    public void resume() {
        mActivity.lockOrientation();
        mSoundPlayer = mActivity.getSoundClipPlayer();
        synchronized (mNV21ConvertorLock) {
            mConvertor = new NV21Convertor(this.mActivity.getApplicationContext());
        }
        mbIsFirstUsePanorama = !mActivity.getSettingsManager().getBoolean(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_HELP_TIP_PANO_FINISHED, false);
        super.resume();
        setSavePath();
    }

    private void setSavePath() {
        if (mSaveBaseDirPath == null || !mSaveBaseDirPath.equals(Storage.DIRECTORY)) {
            mSaveBaseDirPath = Storage.DIRECTORY;
            mSaveInputDirPath = mSaveBaseDirPath + "/input";
            File save_dir = new File(mSaveInputDirPath);
            if (!save_dir.exists()) {
                save_dir.mkdirs();
            }
        }
    }

    @Override
    protected void onMediaPathChanged(Context context, Intent intent) {
        super.onMediaAction(context, intent);
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
                || action.equals(Intent.ACTION_MEDIA_EJECT)) {
            setSavePath();
        }
    }

    @Override
    /* MODIFIED-BEGIN by nie.lei, 2016-05-26,BUG-2208223*/
    protected void onStoragePathChanged() {
        super.onStoragePathChanged();
        setSavePath();
    }

    @Override
    /* MODIFIED-END by nie.lei,BUG-2208223*/
    public void preparePause() {
        if (mPanoState != PANO_STATE_IDLE) {
            return;
        }
        stopPreview();
        setPanoramaState(PANO_STATE_PREVIEW_STOPPED);
    }

    private void showSmallPreview(byte[] data) {
        mShowSmallPreviewTask = new ShowSmallPreviewTask(data);
        mShowSmallPreviewTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class ShowSmallPreviewTask extends AsyncTask<Void, Void, Void> {
        byte[] previewData = null;
        Bitmap mSmallDispPreviewImage = null;

        ShowSmallPreviewTask(byte[] data) {
            previewData = data;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mIsShowSmallPreviewTaskFinish = false;
        }

        @Override
        protected Void doInBackground(Void... params) {
            synchronized (mNV21ConvertorLock) {
                if (mConvertor != null) {
                    /* MODIFIED-BEGIN by xuan.zhou, 2016-05-28,BUG-2220324*/
                    // The preview data format is YUV420sp, so the length should be width * height * 3 / 2.
                    if (previewData != null &&
                            previewData.length == mCameraPreviewW * mCameraPreviewH * 3 / 2) {
                        Bitmap tempBitmap = mConvertor.convertNV21ToBitmap(previewData, mCameraPreviewW, mCameraPreviewH);
                        float scale;
                        int mCameraPreviewHeight = mUI.getCameraPreviewHeight();
                        scale = mCameraPreviewHeight / tempBitmap.getHeight();
                        if (picRotation == 90 || picRotation == 270) {
                            scale = (float) mCameraPreviewHeight / tempBitmap.getWidth();
                        }
                        Matrix matrix = new Matrix();
                        matrix.postScale(scale, isSelfie() ? -scale : scale);
                        matrix.postRotate(picRotation);
                        mSmallDispPreviewImage = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), matrix, true);
                        tempBitmap.recycle();
                    } else if (previewData != null) {
                        // Invalid size for this data.
                        getParametersPictureSize();
                        /* MODIFIED-END by xuan.zhou,BUG-2220324*/
                    }
                }
            }
            return null;
        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
            mIsShowSmallPreviewTaskFinish = true;
        }


        @Override
        protected void onPostExecute(Void arg) {
            super.onPostExecute(arg);
            mIsShowSmallPreviewTaskFinish = true;
            if (mPaused) {
                return;
            }
            mShowSmallPreviewTask = null;
            // mUI.mPanoramaPreview__selectdirection_ui_preview_left.setImageBitmap(mSmallDispPreviewImage);
            // mUI.mPanoramaPreview__selectdirection_ui_preview_right.setImageBitmap(mSmallDispPreviewImage);
            mUI.updatePreviewFrame(mSmallDispPreviewImage, isSelfie(), isDirectionLTR());
        }
    }

    @Override
    protected CameraCapabilities.FocusMode getOverrideFocusMode() {
        if (CAF_PREVIEW) {
            return null;
        }
        return CameraCapabilities.FocusMode.INFINITY;
    }

    private void setFocusAndLockAeAwb() {
        if (mCameraDevice == null) {
            return;
        }

        if (CAF_PREVIEW) {
            // Set focus mode auto to set fixed focus
            mCameraSettings.setFocusMode(CameraCapabilities.FocusMode.AUTO);
        }
        setAeAwbLock(true);
    }

    private void restoreFocusAndUnlockAeAwb() {
        if (mCameraDevice == null) {
            return;
        }

        if (CAF_PREVIEW && mFocusManager != null) {
            mFocusManager.overrideFocusMode(getOverrideFocusMode());
            mCameraSettings.setFocusMode(
                    mFocusManager.getFocusMode(mCameraSettings.getCurrentFocusMode()));
        }
        setAeAwbLock(false);
    }

    @Override
    protected boolean needFaceDetection() {
        return false;
    }

    @Override
    protected void updateAutoFocusMoveCallback() {
        return;
    }

    @Override
    protected boolean hideCamera() {
        // return true;
        return !isSelfieSupported();
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
    protected boolean isGridLinesEnabled() {
        return false;
    }

    @Override
    public void onShutterButtonLongClick() {
    }

    @Override
    protected boolean isCountDownShow() {
        return false;
    }

    @Override
    protected int getRemodeShutterIcon() {
        return -1;
    }

    protected boolean isSelfieSupported() {
        return CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_SUPPORT_PANO_SELFIE, false);
    }
}
