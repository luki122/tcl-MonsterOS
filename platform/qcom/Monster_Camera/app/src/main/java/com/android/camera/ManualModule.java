package com.android.camera;

import android.view.View;

import com.android.camera.app.AppController;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.widget.TopMenus;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.tct.camera.R;

public class ManualModule extends PhotoModule {
    public static final String MANUAL_MODULE_STRING_ID = "ManualModule";
    private static final String TAG = "ManualModule";

    public ManualModule(AppController app) {
        super(app);
    }
    private ManualUI mManualModuleUI;
    @Override
    protected PhotoUI getPhotoUI() {
        if (mManualModuleUI == null) {
            mManualModuleUI = new ManualUI(mActivity, this, mActivity.getModuleLayoutRoot(),
                    new CameraManualModeCallBackListener());
        }
        return mManualModuleUI;
    }

    @Override
    public String getModuleStringIdentifier() {
        return MANUAL_MODULE_STRING_ID;
    }

    @Override
    public int getModuleId() {
        return mAppController.getAndroidContext().getResources()
                .getInteger(R.integer.camera_mode_manual);
    }

    @Override
    public void resume() {
        super.resume();
        if(CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_HELP_TIP_TUTORIAL,false)){
            if(mManualModuleUI != null){
                mManualModuleUI.initManualUIForTutorial();
            }
        }
    }

    @Override
    public void pause() {
        resetManualModeParamters();
        super.pause();
    }

    /* MODIFIED-BEGIN by xuan.zhou, 2016-05-27,BUG-2200127*/
    // When shutter button is clicked, ManualGroup will get the callback from OnShutterButtonListener
    // and invoke collapse. But if user captures through the volume key or the boom key, no event
    // will be sent by OnShutterButtonListener, so I'd like to override onShutterButtonClick and
    // onShutterButtonFocus here to make sure the manual menu is collapsed after capture.
    @Override
    public void onShutterButtonClick() {
        super.onShutterButtonClick();
        ((ManualUI)getPhotoUI()).collapseManualMenu();
    }

    @Override
    public void onShutterButtonFocus(boolean pressed) {
        super.onShutterButtonFocus(pressed);
        ((ManualUI)getPhotoUI()).collapseManualMenu();
    }
    /* MODIFIED-END by xuan.zhou,BUG-2200127*/

    @Override
    public void onShutterButtonLongClick() {
    }

    public void resetManualModeParamters() {
        if (mCameraSettings == null) {
            return;
        }
        mCameraSettings.setISOValue(CameraCapabilities.KEY_AUTO_ISO);
        mCameraSettings.setFocusMode(CameraCapabilities.FocusMode.CONTINUOUS_PICTURE);
        mCameraSettings.setWhiteBalance(CameraCapabilities.WhiteBalance.AUTO);
        mCameraSettings.setExposureTime("0");
        if (mCameraDevice != null) {
            mCameraDevice.applySettings(mCameraSettings);
        }
    }
    @Override
    public boolean isZslOn() {
        return false;
    }
    @Override
    public void hardResetSettings(SettingsManager settingsManager) {
        super.hardResetSettings(settingsManager);
        settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL, Keys.KEY_CAMERA_ID);
    }
    @Override
    public void onLongPress(int x, int y) {
        return;
    }

    private class CameraManualModeCallBackListener implements
            ManualUI.ManualModeCallBackListener {

        @Override
        public void updateISOValue(boolean auto, int isoValue) {
            if (mPaused || mCameraSettings == null || mCameraDevice == null) {
                return;
            }
            if (auto) {
                mCameraSettings.setISOValue(CameraCapabilities.KEY_AUTO_ISO);
            } else {
                clearFocusWithoutChangingState();
                setCameraState(IDLE);
                mCameraSettings.setContinuousIso(isoValue);
                mCameraSettings.setISOValue(CameraCapabilities.KEY_MANUAL_ISO);

            }
            if (mCameraDevice != null) {
                mCameraDevice.applySettings(mCameraSettings);
            }
        }

        @Override
        public void updateManualFocusValue(boolean auto, int focusPos) {
            if (mPaused || mCameraSettings == null || mCameraDevice == null) {
                return;
            }
            if (auto) {
                mCameraSettings.setFocusMode(CameraCapabilities.FocusMode.CONTINUOUS_PICTURE);
            } else {
                clearFocusWithoutChangingState();
                setCameraState(IDLE);
                mCameraSettings.setFocusMode(CameraCapabilities.FocusMode.MANUAL);
                mCameraSettings.setManualFocusPosition(focusPos);
            }
            if (mCameraDevice != null) {
                mCameraDevice.applySettings(mCameraSettings);
            }
        }

        @Override
        public void updateWBValue(boolean auto, String wbValue) {
            if (mPaused || mCameraSettings == null || mCameraDevice == null) {
                return;
            }
            CameraCapabilities.Stringifier stringifier = mCameraCapabilities.getStringifier();
            if (auto) {
                mCameraSettings.setWhiteBalance(CameraCapabilities.WhiteBalance.AUTO);
            } else {
                mCameraSettings.setWhiteBalance(stringifier.whiteBalanceFromString(wbValue));
            }
            if (mCameraDevice != null) {
                mCameraDevice.applySettings(mCameraSettings);
            }
        }
        @Override
        public void updateExposureTime(boolean auto, String ecValue) {
            if (mPaused || mCameraSettings == null || mCameraDevice == null) {
                return;
            }
            if (auto) {
                mCameraSettings.setExposureTime("0");//"o" is "auto"
            } else {
                clearFocusWithoutChangingState();
                setCameraState(IDLE);
                mCameraSettings.setExposureTime(ecValue);
            }
            if (mCameraDevice != null) {
                mCameraDevice.applySettings(mCameraSettings);
            }
        }
    }

    @Override
    protected boolean hideCamera() {
        return true;
    }

    @Override
    protected boolean isHdrShow() {
        return false;
    }
    @Override
    protected boolean isWrapperButtonShow() {
        return false;
    }
    @Override
    protected boolean needEnableExposureAdjustment(){
        if(CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_ENABLE_COMPENSATION_OTHER_THAN_AUTO,true)){
            return true;
        }
        return !mManualModuleUI.isManualMode(Keys.KEY_MANUAL_ISO_STATE) && !mManualModuleUI.isManualMode(Keys.KEY_CUR_EXPOSURE_TIME_STATE);
    }

    @Override
    public void onSingleTapUp(View view, int x, int y) {
        ((ManualUI)getPhotoUI()).collapseManualMenu();
        if (mManualModuleUI.isManualMode(Keys.KEY_CUR_FOCUS_STATE)) {
            return;
        }
        super.onSingleTapUp(view, x, y);
    }
    @Override
    protected void updateParametersFocusMode() {
        if (!mManualModuleUI.isManualMode(Keys.KEY_CUR_FOCUS_STATE)) {
            super.updateParametersFocusMode();
        }
    }

    /* MODIFIED-BEGIN by peixin, 2016-05-09,BUG-2011866*/
    @Override
    protected void onPreviewStarted() {
        super.onPreviewStarted();
        // mAppController.getButtonManager().enableButton(ButtonManager.BUTTON_FLASH);
        mAppController.getCameraAppUI().getTopMenus().enableButton(TopMenus.BUTTON_FLASH);
        /* MODIFIED-BEGIN by peixin, 2016-05-13,BUG-2146006*/
        if (mActivity.BATTERY_STATUS_WARNING == mActivity.getCurrentBatteryStatus()) {
            // mAppController.getButtonManager().disableButton(ButtonManager.BUTTON_FLASH);
            mAppController.getCameraAppUI().getTopMenus().disableButton(TopMenus.BUTTON_FLASH);
        }
        /* MODIFIED-END by peixin,BUG-2146006*/
    }
    /* MODIFIED-END by peixin,BUG-2011866*/
}
