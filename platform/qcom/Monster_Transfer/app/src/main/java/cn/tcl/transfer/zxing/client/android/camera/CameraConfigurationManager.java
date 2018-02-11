/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.transfer.zxing.client.android.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import cn.tcl.transfer.util.LogUtils;
import cn.tcl.transfer.zxing.client.android.PreferencesActivity;

import cn.tcl.transfer.camera.CameraConfigurationUtils;

/**
 * A class which deals with reading, parsing, and setting the camera parameters which are used to
 * configure the camera hardware.
 */
final class CameraConfigurationManager {

    private static final String TAG = "CameraConfiguration";
    private static final int ROTATION_ANGLE = 90;
    private static final int OTHER_HEIGHT = 180;

    private final Context context;
    private Point screenResolution;
    private Point cameraResolution;

    CameraConfigurationManager(Context context) {
        this.context = context;
    }

    /**
     * Reads, one time, values from the camera that are needed by the app.
     */
    void initFromCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point theScreenResolution = new Point();
        display.getSize(theScreenResolution);
        theScreenResolution.y = theScreenResolution.y - OTHER_HEIGHT;
        screenResolution = theScreenResolution;
        LogUtils.i(TAG, "Screen resolution: " + screenResolution);
        Point p = new Point();
        p.x = this.screenResolution.x;
        p.y = this.screenResolution.y;
        if (this.screenResolution.x < this.screenResolution.y) {
            p.x = this.screenResolution.y;
            p.y = this.screenResolution.x;
        }
        cameraResolution = CameraConfigurationUtils.findBestPreviewSizeValue(parameters, p);
        LogUtils.i(TAG, "Camera resolution: " + cameraResolution);
    }

    void setDesiredCameraParameters(Camera camera, boolean safeMode) {
        camera.setDisplayOrientation(ROTATION_ANGLE);
        Camera.Parameters parameters = camera.getParameters();

        if (parameters == null) {
            Log.e(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
            return;
        }

        LogUtils.i(TAG, "Initial camera parameters: " + parameters.flatten());

        if (safeMode) {
            LogUtils.w(TAG, "In camera config safe mode -- most settings will not be honored");
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        initializeTorch(parameters, prefs, safeMode);

        CameraConfigurationUtils.setFocus(
                parameters,
                prefs.getBoolean(PreferencesActivity.KEY_AUTO_FOCUS, true),
                prefs.getBoolean(PreferencesActivity.KEY_DISABLE_CONTINUOUS_FOCUS, true),
                safeMode);

        if (!safeMode) {
            if (prefs.getBoolean(PreferencesActivity.KEY_INVERT_SCAN, false)) {
                CameraConfigurationUtils.setInvertColor(parameters);
            }

            if (!prefs.getBoolean(PreferencesActivity.KEY_DISABLE_BARCODE_SCENE_MODE, true)) {
                CameraConfigurationUtils.setBarcodeSceneMode(parameters);
            }

            if (!prefs.getBoolean(PreferencesActivity.KEY_DISABLE_METERING, true)) {
                CameraConfigurationUtils.setVideoStabilization(parameters);
                CameraConfigurationUtils.setFocusArea(parameters);
                CameraConfigurationUtils.setMetering(parameters);
            }

        }

        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);

        LogUtils.i(TAG, "Final camera parameters: " + parameters.flatten());

        camera.setParameters(parameters);

        Camera.Parameters afterParameters = camera.getParameters();
        Camera.Size afterSize = afterParameters.getPreviewSize();
        if (afterSize!= null && (cameraResolution.x != afterSize.width || cameraResolution.y != afterSize.height)) {
            LogUtils.w(TAG, "Camera said it supported preview size " + cameraResolution.x + 'x' + cameraResolution.y +
                    ", but after setting it, preview size is " + afterSize.width + 'x' + afterSize.height);
            cameraResolution.x = afterSize.width;
            cameraResolution.y = afterSize.height;
        }
    }

    Point getCameraResolution() {
        return cameraResolution;
    }

    Point getScreenResolution() {
        return screenResolution;
    }

    boolean getTorchState(Camera camera) {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters != null) {
                String flashMode = parameters.getFlashMode();
                return flashMode != null &&
                        (Camera.Parameters.FLASH_MODE_ON.equals(flashMode) ||
                                Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode));
            }
        }
        return false;
    }

    void setTorch(Camera camera, boolean newSetting) {
        Camera.Parameters parameters = camera.getParameters();
        doSetTorch(parameters, newSetting, false);
        camera.setParameters(parameters);
    }

    private void initializeTorch(Camera.Parameters parameters, SharedPreferences prefs, boolean safeMode) {
        boolean currentSetting = FrontLightMode.readPref(prefs) == FrontLightMode.ON;
        doSetTorch(parameters, currentSetting, safeMode);
    }

    private void doSetTorch(Camera.Parameters parameters, boolean newSetting, boolean safeMode) {
        CameraConfigurationUtils.setTorch(parameters, newSetting);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!safeMode && !prefs.getBoolean(PreferencesActivity.KEY_DISABLE_EXPOSURE, true)) {
            CameraConfigurationUtils.setBestExposure(parameters, newSetting);
        }
    }

}
