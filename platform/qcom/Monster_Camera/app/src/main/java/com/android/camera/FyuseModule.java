package com.android.camera;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.view.KeyEvent;
import android.view.View;

import com.android.camera.app.AppController;
import com.android.camera.app.CameraAppUI;
import com.android.camera.debug.Log;

import com.android.camera.hardware.HardwareSpec;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.ui.TouchCoordinate;
import com.android.ex.camera2.portability.CameraAgent;
import com.tct.camera.R;

import java.io.ByteArrayOutputStream;

public class FyuseModule extends CameraModule {

    public static final String FYUSE_MODULE_STRING_ID = "FyuseModule";
    private final Log.Tag TAG = new Log.Tag("FyuseModule");

    private CameraActivity mActivity;
    private boolean mStartFyuse = true;
    private Integer mModeSelectionLockToken = null;

    static class FyuseRequest {
        public static final int VIEWER = 0;
        public static final int FILE_LISTING = 1;
        public static final int START_CAMERA_MODE = 2;
    }

    static class CameraMode {
        public static final String MANUAL = "Manual";
        public static final String PANO = "Pano";
        public static final String PHOTO = "Camera";
        public static final String SLO_MO = "SlowMo";
        public static final String MICRO_VIDEO = "MicroVideo";
    }


    public FyuseModule(AppController app) {
        super(app);
    }

    @Override
    public String getModuleStringIdentifier() {
        return FYUSE_MODULE_STRING_ID;
    }

    @Override
    public int getModuleId() {
        return mActivity.getResources()
                .getInteger(R.integer.camera_mode_parallax);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onSingleTapUp(View view, int x, int y) {

    }

    @Override
    public String getPeekAccessibilityString() {
        return null;
    }

    @Override
    public void init(CameraActivity activity, boolean isSecureCamera, boolean isCaptureIntent) {
        mActivity = activity;
    }

    @Override
    public void resume() {
        Log.i(TAG, "Resume " + mStartFyuse);
        /* MODIFIED-BEGIN by xuan.zhou, 2016-04-28,BUG-2005112*/
        if (mStartFyuse && mActivity.isFyuseEnabled()) {
            // Don't call super.resume() here because there is no need to request camera.
            startFyuse(mActivity);
            /* MODIFIED-END by xuan.zhou,BUG-2005112*/
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public boolean isPaused() {
        return false;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void onLayoutOrientationChanged(boolean isLandscape) {

    }

    @Override
    public void onOrientationChanged(int orientation) {

    }

    @Override
    public void onCameraAvailable(CameraAgent.CameraProxy cameraProxy) {

    }

    @Override
    public void hardResetSettings(SettingsManager settingsManager) {

    }

    @Override
    public HardwareSpec getHardwareSpec() {
        return null;
    }

    @Override
    public CameraAppUI.BottomBarUISpec getBottomBarSpec() {
        return null;
    }

    @Override
    public boolean isUsingBottomBar() {
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != FyuseRequest.START_CAMERA_MODE) {
            return;
        }
        if (mActivity == null) return;

        if (mModeSelectionLockToken != null) {
            mActivity.unlockModuleSelection(mModeSelectionLockToken);
        }
        mActivity.setSecureFyuseModule(false);

        // If operation fail, return after unlockModuleSelection.
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        mStartFyuse = false;

        if (data != null) {
            String mode = data.getStringExtra("selectedMode");
            if (mode != null && !mode.equals("")) {
                int index = getIndexByName(mode);
                // update mode directly.
                /*
                if (mActivity.isSecureCamera()) {
                    // If secure camera, it will not back here and updateModuleForFyuse,
                    // so set KEY_SECURE_MODULE_INDEX and do the mode switch when activity onStart.
                    mActivity.getSettingsManager().set(SettingsManager.SCOPE_GLOBAL,
                            Keys.KEY_SECURE_MODULE_INDEX, index);
                } else {
                    // Set KEY_STARTUP_MODULE_INDEX here and do the mode switch when activity onStart.
                    mActivity.getSettingsManager().set(SettingsManager.SCOPE_GLOBAL,
                            Keys.KEY_STARTUP_MODULE_INDEX, index);
                }
                */
                mActivity.updateModeForFyusion(index);
            } else {
                // Back pressed in Fyuse
                mActivity.onBackPressed();
            }
        }
    }

    private int getIndexByName(String mode) {
        int index = mActivity.getResources().getInteger(R.integer.camera_mode_photo);
        if (mode != null) {
            if (mode.equals(CameraMode.MANUAL)) {
                index = mActivity.getResources().getInteger(R.integer.camera_mode_manual);
            } else if (mode.equals(CameraMode.PANO)) {
                index = mActivity.getResources().getInteger(R.integer.camera_mode_pano);
            } else if (mode.equals(CameraMode.SLO_MO)) {
                index = mActivity.getResources().getInteger(R.integer.camera_mode_slowmotion);
            } else if (mode.equals(CameraMode.MICRO_VIDEO)) {
                index = mActivity.getResources().getInteger(R.integer.camera_mode_micro_video);
            } else {
                index = mActivity.getResources().getInteger(R.integer.camera_mode_photo);
            }
        }
        return index;
    }

    public boolean startFyuse(Context context) {
        try {
            Log.e(TAG, "KPI startFyuse e");

            if(mModeSelectionLockToken==null) {
                mModeSelectionLockToken = mActivity.lockModuleSelection();
            }

            boolean isSecureCamera = mActivity.isSecureCamera();
            if (isSecureCamera) {
                mActivity.setSecureFyuseModule(true);
            }

            String packageName = FyuseAPI.FYUSE_PACKAGE_NAME;
            String className = FyuseAPI.FYUSE_SDK_APPNAME + FyuseAPI.Action.RESUME_CAMERA;
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(packageName, className));
            intent.putExtra(Keys.KEY_SECURE_CAMERA, isSecureCamera);
            intent.putExtra(Keys.KEY_CAMERA_ID, mActivity.getCurrentCameraId());
            intent.putExtra(Keys.KEY_THUMB_URI, mActivity.getPeekThumbUri());
            Bitmap coveredBitmap = mActivity.getCameraAppUI().getCoveredBitmap();
            if (coveredBitmap != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                coveredBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                byte[] bitmapByte = baos.toByteArray();
                intent.putExtra(Keys.KEY_BLURRED_BITMAP_BYTE, bitmapByte);

                RectF previewArea = mActivity.getCameraAppUI().getCoveredArea();
                if (previewArea.top > 0 || previewArea.bottom > 0) {
                    intent.putExtra(Keys.KEY_PREVIEW_AREA,
                            new float[] {previewArea.top, previewArea.bottom});
                }
            }
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            mActivity.startActivityForResult(intent, FyuseRequest.START_CAMERA_MODE);
            // Use 0 for no animation
            mActivity.overridePendingTransition(0, 0);
            Log.e(TAG, "KPI startFyuse x");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "No Fyuse installed");

            if (mModeSelectionLockToken != null) {
                mActivity.unlockModuleSelection(mModeSelectionLockToken);
            }

            mStartFyuse = false;
            mActivity.setSecureFyuseModule(false);
            mActivity.switchToMode(mActivity.getResources().getInteger(R.integer.camera_mode_photo));
            return false;
        }
    }

    @Override
    public void onShutterButtonFocus(boolean pressed) {

    }

    @Override
    public void onShutterCoordinate(TouchCoordinate coord) {

    }

    @Override
    public void onShutterButtonClick() {

    }

    @Override
    public void onShutterButtonLongClick() {
    }

}
