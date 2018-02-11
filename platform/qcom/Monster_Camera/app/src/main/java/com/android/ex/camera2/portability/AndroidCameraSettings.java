/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.ex.camera2.portability;

import android.hardware.Camera;
import android.text.TextUtils;

import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.ex.camera2.portability.debug.Log;
import com.android.external.ExtendKey;
import com.android.external.ExtendParameters;
import com.android.external.plantform.ExtBuild; // MODIFIED by bin.zhang2-nb, 2016-04-26,BUG-1996450

/**
 * The subclass of {@link CameraSettings} for Android Camera 1 API.
 */
public class AndroidCameraSettings extends CameraSettings {
    private static final Log.Tag TAG = new Log.Tag("AndCamSet");

    private static final String TRUE = "true";
    private static final String RECORDING_HINT = "recording-hint";

    public AndroidCameraSettings(CameraCapabilities capabilities, Camera.Parameters params) {
        if (params == null) {
            Log.w(TAG, "Settings ctor requires a non-null Camera.Parameters.");
            return;
        }

        CameraCapabilities.Stringifier stringifier = capabilities.getStringifier();

        setSizesLocked(false);

        // Preview
        Camera.Size paramPreviewSize = params.getPreviewSize();
        setPreviewSize(new Size(paramPreviewSize.width, paramPreviewSize.height));
        setPreviewFrameRate(params.getPreviewFrameRate());
        int[] previewFpsRange = new int[2];
        params.getPreviewFpsRange(previewFpsRange);
        setPreviewFpsRange(previewFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                previewFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
        setPreviewFormat(params.getPreviewFormat());

        // Capture: Focus, flash, zoom, exposure, scene mode.
        if (capabilities.supports(CameraCapabilities.Feature.ZOOM)) {
            setZoomRatio(params.getZoomRatios().get(params.getZoom()) / 100f);
        } else {
            setZoomRatio(CameraCapabilities.ZOOM_RATIO_UNZOOMED);
        }
        setExposureCompensationIndex(params.getExposureCompensation());
        setFlashMode(stringifier.flashModeFromString(params.getFlashMode()));
        setFocusMode(stringifier.focusModeFromString(params.getFocusMode()));
        setSceneMode(stringifier.sceneModeFromString(params.getSceneMode()));
        String tsMode = params.get(ExtendKey.TS_MODE);
        Log.i(TAG, "getsettings tsMode:" + tsMode);
        /* MODIFIED-BEGIN by xuyang.liu, 2016-10-13,BUG-3110198*/
        if (TextUtils.equals(tsMode, ExtendKey.TS_HDR_ON) ) {
            setSceneMode(CameraCapabilities.SceneMode.HDR);
        } else if (TextUtils.equals(tsMode, ExtendKey.TS_HDR_AUTO)) {
            setSceneMode(CameraCapabilities.SceneMode.HDR_AUTO);
        } else if (TextUtils.equals(tsMode, ExtendKey.TS_HDR_OFF)) {
            setSceneMode(CameraCapabilities.SceneMode.AUTO);
            /* MODIFIED-END by xuyang.liu,BUG-3110198*/
        }
        String visidonMode = params.get(ExtendKey.VISIDON_MODE);
        Log.i(TAG, "getsettings visidonMode:" + visidonMode);
        if (visidonMode != null) {
            if (visidonMode.equals(ExtendKey.VISIDON_LOW_LIGHT)) {
                setLowLight(true);
            } else if (visidonMode.equals(ExtendKey.VISIDON_FACE_BEAUTY)) {

                /* MODIFIED-BEGIN by bin.zhang2-nb, 2016-04-26,BUG-1996450*/
                if (ExtBuild.device() != ExtBuild.MTK_MT6755) {
                    String smooth = params.get(ExtendKey.VISIDON_SKIN_SMOOTHING);
                    int skinSmooth = ExtendKey.SKIN_SMOOTHING_DEFAULT;
                    if (smooth != null) {
                        skinSmooth = Integer.parseInt(smooth);
                    }
                    setFaceBeauty(true, skinSmooth);
                } else {
                    String smooth = params.get(ExtendKey.VISIDON_SKIN_SMOOTHING);
                    int skinSmooth = ExtendKey.SKIN_SMOOTHING_DEFAULT;
                    if (smooth != null) {
                        skinSmooth = Integer.parseInt(smooth);
                    }
                    setFaceBeautySmoothing(true, skinSmooth);

                    String white = params.get(ExtendKey.VISIDON_SKIN_WHITENING);
                    int skinWhite = ExtendKey.SKIN_WHITENING_DEFAULT;
                    if (white != null) {
                        skinWhite = Integer.parseInt(white);
                    }
                    setFaceBeautyWhitening(true, skinWhite);
                    /* MODIFIED-END by bin.zhang2-nb,BUG-1996450*/
                }
            } else if (visidonMode.equals(ExtendKey.VISIDON_SUPER_RESOLUTION)) {
                setSuperResolutionOn(true);
            }
        }
        if (CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_TCT_EIS, false)) {
            String eisOn = params.get(ExtendKey.TCT_EIS_ENABLE);
            if (TextUtils.equals(eisOn, "on")) {
                setVideoStabilization(true);
            } else {
                setVideoStabilization(false);
            }
        } else {
            // Video capture.
            if (capabilities.supports(CameraCapabilities.Feature.VIDEO_STABILIZATION)) {
                setVideoStabilization(isVideoStabilizationEnabled());
            }
        }
        setRecordingHintEnabled(TRUE.equals(params.get(RECORDING_HINT)));

        // Output: Photo size, compression quality
        setPhotoJpegCompressionQuality(params.getJpegQuality());
        Camera.Size paramPictureSize = params.getPictureSize();
        setPhotoSize(new Size(paramPictureSize.width, paramPictureSize.height));
        setPhotoFormat(params.getPictureFormat());

        String paramVideoSize = params.get(CameraCapabilities.KEY_VIDEO_SIZE);
        if(paramVideoSize != null){
            int pos = paramVideoSize.indexOf('x');
            if (pos != -1) {
                int width = Integer.parseInt(paramVideoSize.substring(0, pos));
                int height = Integer.parseInt(paramVideoSize.substring(pos + 1));
                if (width > 0 && height > 0) {
                    setVideoSize(new Size(width, height));
                }
            }
        }

        // video-hsr
        setHsr(params.get(CameraCapabilities.KEY_VIDEO_HSR));
        setAec(params.get(CameraCapabilities.KEY_AEC_SETTLED));
        ExtendParameters extParams=ExtendParameters.getInstance(params);
        isZslOn = extParams.getZSLMode();
        isInstantAEC=false;
    }

    public AndroidCameraSettings(AndroidCameraSettings other) {
        super(other);
    }

    @Override
    public CameraSettings copy() {
        return new AndroidCameraSettings(this);
    }
}
