package com.android.camera;

import com.android.camera.app.AppController;
import com.android.camera.app.CameraAppUI;
import com.android.camera.debug.Log;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.widget.ButtonGroup;
import com.android.camera.widget.TopMenus;
import com.android.ex.camera2.portability.CameraAgent;
import com.android.ex.camera2.portability.Size;
import com.tct.camera.R;

import java.util.List;

/**
 * Created by JianYing.Zhang on 9/14/16.
 */
public class ContactsIntentModule extends PhotoModule{
    private static final String PHOTO_MODULE_STRING_ID = "ContactsIntentModule";

    private static final Log.Tag TAG = new Log.Tag(PHOTO_MODULE_STRING_ID);
    /**
     * Constructs a new photo module.
     *
     * @param app
     */
    public ContactsIntentModule(AppController app) {
        super(app);
    }

    @Override
    public int getModuleId() {
        return mAppController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_contacts_intent);
    }

    @Override
    protected boolean needShowBottomLine() {
        return true;
    }

    @Override
    public void onShutterButtonLongClick() {

    }

    @Override
    protected boolean isContactsShow() {
        return true;
    }

    @Override
    public void onCameraAvailable(CameraAgent.CameraProxy cameraProxy) {
        super.onCameraAvailable(cameraProxy);
        getPhotoUI().showContactsGridLines();
    }

    @Override
    public void resume() {
        super.resume();
        getPhotoUI().hideContactsGridLines();
    }

    @Override
    protected boolean hideSetting() {
        return true;
    }

    @Override
    protected boolean hideCamera() {
        return true;
    }

    @Override
    protected boolean hideCameraForced() {
        return true;
    }

    @Override
    public boolean isExposureSidebarEnabled() {
        return CustomUtil.getInstance().getBoolean(
                CustomFields.DEF_CAMERA_ENABLE_EXPOSURE_SIDEBAR, true);
    }
    @Override
    protected String getPictureSize() {
        if (mCameraDevice == null) {
            return SettingsUtil.getSizeEntryString(new Size(0, 0));
        }
        Size size = SettingsUtil.getPictureSizeAccordingToRatio(mCameraCapabilities);
        return SettingsUtil.getSizeEntryString(size);
    }


    @Override
    protected boolean dismissButtonGroupBar(boolean needAnimation) {
        if (mAppController !=null
                && mAppController.getCameraAppUI()!= null
                && mAppController.getCameraAppUI().getTopMenus().buttonGroupBarVisible()) {
            mAppController.getCameraAppUI().dismissButtonGroupBar(needAnimation, ButtonGroup.OUT_RIGHT);
            return true;
        }
        return false;
    }

    @Override
    protected boolean isCountDownShow() {
        return false;
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
    public void onFlashClicked() {
        mAppController.getCameraAppUI().getTopMenus().initializeButtonGroupWithAnimationDirection(TopMenus.BUTTON_CONTACTS_FLASH, false);
    }
}
