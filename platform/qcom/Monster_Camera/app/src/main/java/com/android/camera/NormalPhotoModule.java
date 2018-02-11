package com.android.camera;

import com.android.camera.app.AppController;
import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.util.ToastUtil;
import com.android.camera.widget.ButtonGroup;
import com.android.camera.widget.TopMenus;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.tct.camera.R;

/**
 * Created by sichao.hu on 12/23/15.
 */
public class NormalPhotoModule extends PhotoModule {

    public static final String AUTO_MODULE_STRING_ID = "AutoModule";

    private static final Log.Tag TAG=new Log.Tag(AUTO_MODULE_STRING_ID);


    private static final int CLEAR_ASPECT_RATIO_VIEW_DELAY = 4000;
    /**
     * Constructs a new photo module.
     *
     * @param app
     */
    public NormalPhotoModule(AppController app) {
        super(app);
    }

    @Override
    public String getModuleStringIdentifier() {
        return AUTO_MODULE_STRING_ID;
    }

    @Override
    public int getModuleId() {
        return mAppController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_photo);
    }

    @Override
    protected boolean isLowLightShow() {
        return !isCameraFrontFacing();
    }


    @Override
    protected boolean isEnableGestureRecognization() {
        SettingsManager settingsManager = mActivity.getSettingsManager();
        boolean isGestureDetectionOn = Keys.isGestureDetectionOn(settingsManager);
        Log.d(TAG, "isEnableGestureRecognization" + isGestureDetectionOn); // MODIFIED by jianying.zhang, 2016-05-25,BUG-2202266
        return isGestureDetectionOn && isCameraFrontFacing();
    }

    @Override
    protected boolean isSuperResolutionEnabled() {
        return true;
    }

    @Override
    protected boolean isVisidonModeEnabled() {
        return true;
    }

    @Override
    public boolean isFacebeautyEnabled() {
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-03-22, BUG-1849045 */
        boolean bFacebeautyFeatureOn =  CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_PHOTO_FACEBEAUTY_SUPPORT, true) &&
                Keys.isFacebeautyOn(mActivity.getSettingsManager()) && isCameraFrontFacing();
                /* MODIFIED-END by yuanxing.tan,BUG-1849045 */
        return bFacebeautyFeatureOn;
    }

    @Override
    public boolean isAttentionSeekerShow() {
        return Keys.isAttentionseekerOn(mActivity.getSettingsManager()) && !isCameraFrontFacing();
    }

    @Override
    public boolean isGesturePalmShow() {
        /*MODIFIED-BEGIN by wenhua.tu, 2016-04-07,BUG-1915739*/
        boolean isMTKGesture = CustomUtil.getInstance().getBoolean(CustomFields.DEF_MTK_GESTURE_ICON, false);
        return isEnableGestureRecognization() && !isMTKGesture; //MODIFIED by jianying.zhang, 2016-04-08,BUG-1878128
        /*MODIFIED-END by wenhua.tu,BUG-1915739*/
    }

    public boolean isShowPose() {
        /* MODIFIED-BEGIN by fei.hui, 2016-09-29,BUG-2994050*/
        String moduleScope = mAppController.getModuleScope();
        if (Keys.isPoseOn(mAppController.getSettingsManager()) &&
                /* MODIFIED-BEGIN by feifei.xu, 2016-10-31,BUG-3150038*/
                (moduleScope.endsWith(AUTO_MODULE_STRING_ID)) && !isCameraFrontFacing() &&
                !isImageCaptureIntent()) {
                /* MODIFIED-END by feifei.xu,BUG-3150038*/
            return true;
        }else{
            return false;
        }
        /* MODIFIED-END by fei.hui,BUG-2994050*/

    }

    public boolean isShowCompose() {
        String moduleScope = mAppController.getModuleScope();
        if (Keys.isComposeOn(mAppController.getSettingsManager()) &&
                (moduleScope.endsWith(NormalPhotoModule.AUTO_MODULE_STRING_ID)) && !isCameraFrontFacing()) {
            return true;
        } else {
            return false;
        }
    }

    protected boolean showFilter() {
        return !isImageCaptureIntent(); // MODIFIED by jianying.zhang, 2016-10-26,BUG-3229970
    }

    protected boolean needCountDownIndicatorShow() {
        if (SettingsUtil.getCountDownDuration(mAppController,
                mActivity.getSettingsManager()) > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void resume() {
        super.resume();
        Log.w(TAG, "on Resume");
    }

    @Override
    public void pause() {
        super.pause();
        Log.w(TAG, "on Pause"); // MODIFIED by jianying.zhang, 2016-05-30,BUG-2202266
    }

    @Override
    protected boolean isNeedMirrorSelfie() {
         return Keys.isMirrorSelfieOn(mAppController.getSettingsManager());
     }

    @Override
    protected void updateFrontPhotoFlipMode(){
        if(isCameraFrontFacing())
            mCameraSettings.setMirrorSelfieOn(Keys.isMirrorSelfieOn(mAppController.getSettingsManager()));
    }

    @Override
    protected boolean isVolumeKeySystemBehaving() {
        if (getPhotoUI().isSoundGroupPlaying()) {
            Log.w(TAG, "process volume key as system service");
            return true;
        }
        return false;
    }

    @Override
    protected void setCameraState(int state) {
        super.setCameraState(state);
        if(state==IDLE){
            getPhotoUI().enableZoom();
            //notify video tip when camera state is idle
            HelpTipsManager helpTipsManager = mAppController.getHelpTipsManager();
            if (helpTipsManager != null) { //MODIFIED by nie.lei, 2016-04-01,BUG-1875810
                helpTipsManager.setVideoReadlyFlags();
            }
        }
    }

    @Override
    protected void transitionToTimer(boolean isShow) {
        if (isShow) {
            getPhotoUI().showFacebeauty();
            getPhotoUI().showGesturePalm();
            getPhotoUI().showSoundGroup();
        } else {
            getPhotoUI().hideFacebeauty();
            getPhotoUI().hideGesturePalm();
            getPhotoUI().hideSoundGroup();
        }
    }

    @Override
    protected void initializeFocusModeSettings() {
        CameraCapabilities.Stringifier stringifier = mCameraCapabilities.getStringifier();
        SettingsManager settingsManager = mAppController.getSettingsManager();
        if(mCameraCapabilities.supports(CameraCapabilities.FocusMode.CONTINUOUS_PICTURE)) {
            settingsManager.set(mAppController.getCameraScope(), Keys.KEY_FOCUS_MODE,
                    stringifier.stringify(CameraCapabilities.FocusMode.CONTINUOUS_PICTURE));
        }else{
            /* MODIFIED-BEGIN by jianying.zhang, 2016-05-25,BUG-2202266*/
            Log.e(TAG, "Continous Picture Focus not supported");
        }
    }

    @Override
    protected boolean aspectRatioVisible() {
        return !isImageCaptureIntent();
    }

    @Override
    public boolean isExposureSidebarEnabled() {
        return CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_ENABLE_EXPOSURE_SIDEBAR, true);
    }

    @Override
    public boolean isMeteringEnabled() {
        return CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_ENABLE_METERING, true);
    }

    @Override
    protected boolean clearAspectRatioViewer(boolean needAnimation) {
        if (getPhotoUI().isAspectRatioBarVisible()) {
            getPhotoUI().clearAspectRatioViewer(needAnimation);
            mHandler.removeMessages(MSG_CLEAR_ASPECT_RATIO_VIEW);
            return true;
        }
        return false;
    }

    @Override
    public void onAspectRatioClicked() {
        dismissButtonGroupBar(true);
        if (getPhotoUI().isAspectRatioBarVisible()) {
            clearAspectRatioViewer(true);
            return;
        }
        getPhotoUI().setupAspectRatioViewer(TopMenus.BUTTON_ASPECT_RATIO, new ButtonGroup.ButtonCallback() {
            @Override
            public void onStateChanged() {
                clearAspectRatioViewer(true);
            }
        }, mCameraCapabilities.getSupportedPhotoSizes());
        ToastUtil.cancelToast();
        mHandler.sendEmptyMessageDelayed(MSG_CLEAR_ASPECT_RATIO_VIEW, CLEAR_ASPECT_RATIO_VIEW_DELAY);
    }

    /**
     * Determines whether to continue onLongPress . onSingleTapUp and onBackPressed method
     * if ButtonGroupBar is showing , dismiss ButtonGroupBar, return true;
     * @param needAnimation
     * @return
     * return
     */
    @Override
    protected boolean dismissButtonGroupBar(boolean needAnimation) {
        if (mAppController !=null
                && mAppController.getCameraAppUI()!= null
                && mAppController.getCameraAppUI().getTopMenus().buttonGroupBarVisible()) {
            mAppController.getCameraAppUI().dismissButtonGroupBar(needAnimation, ButtonGroup.OUT_LEFT);
            return true;
        }
        return false;
    }
}
