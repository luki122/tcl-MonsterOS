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

import android.annotation.TargetApi;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Range;
import android.util.Rational;

import com.android.ex.camera2.portability.debug.Log;

import java.util.Arrays;

import static android.hardware.camera2.CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AF_MODE_AUTO;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_VIDEO;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AF_MODE_EDOF;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AF_MODE_MACRO;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AF_MODE_OFF;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AWB_MODE_AUTO;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AWB_MODE_DAYLIGHT;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AWB_MODE_FLUORESCENT;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AWB_MODE_INCANDESCENT;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AWB_MODE_SHADE;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AWB_MODE_TWILIGHT;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AWB_MODE_WARM_FLUORESCENT;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_MAX_REGIONS_AE;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_MAX_REGIONS_AF;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_SCENE_MODE_ACTION;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_SCENE_MODE_BARCODE;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_SCENE_MODE_BEACH;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_SCENE_MODE_CANDLELIGHT;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_SCENE_MODE_DISABLED;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_SCENE_MODE_FIREWORKS;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_SCENE_MODE_LANDSCAPE;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_SCENE_MODE_NIGHT;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_SCENE_MODE_PARTY;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_SCENE_MODE_PORTRAIT;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_SCENE_MODE_SNOW;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_SCENE_MODE_SPORTS;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_SCENE_MODE_STEADYPHOTO;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_SCENE_MODE_SUNSET;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_SCENE_MODE_THEATRE;
import static android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE;
import static android.hardware.camera2.CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE;
import static android.hardware.camera2.CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES;
import static android.hardware.camera2.CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR;
import static android.hardware.camera2.CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING;
import static android.hardware.camera2.CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING;
import static android.hardware.camera2.CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM;
import static android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP;
import static android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE;
import static android.hardware.camera2.CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE;
import static android.hardware.camera2.CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT;

/**
 * The subclass of {@link CameraCapabilities} for Android Camera 2 API.
 */
public class AndroidCamera2Capabilities extends CameraCapabilities {
    private static Log.Tag TAG = new Log.Tag("AndCam2Capabs");

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    AndroidCamera2Capabilities(CameraCharacteristics p) {
        super(new Stringifier());

        StreamConfigurationMap s = p.get(SCALER_STREAM_CONFIGURATION_MAP);

        for (Range<Integer> fpsRange : p.get(CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)) {
            mSupportedPreviewFpsRange.add(new int[] { fpsRange.getLower(), fpsRange.getUpper() });
        }

        // TODO: We only support TextureView preview rendering
        mSupportedPreviewSizes.addAll(Size.buildListFromAndroidSizes(Arrays.asList(
                s.getOutputSizes(SurfaceTexture.class))));
        for (int format : s.getOutputFormats()) {
            mSupportedPreviewFormats.add(format);
        }

        // TODO: We only support MediaRecorder video capture
        mSupportedVideoSizes.addAll(Size.buildListFromAndroidSizes(Arrays.asList(
                s.getOutputSizes(MediaRecorder.class))));

        // TODO: We only support JPEG image capture
        mSupportedPhotoSizes.addAll(Size.buildListFromAndroidSizes(Arrays.asList(
                s.getOutputSizes(ImageFormat.JPEG))));
        mSupportedPhotoFormats.addAll(mSupportedPreviewFormats);

        buildSceneModes(p);
        buildFlashModes(p);
        buildFocusModes(p);
        buildWhiteBalances(p);
        // TODO: Populate mSupportedFeatures

        // TODO: Populate mPreferredPreviewSizeForVideo

        Range<Integer> ecRange = p.get(CONTROL_AE_COMPENSATION_RANGE);
        mMinExposureCompensation = ecRange.getLower();
        mMaxExposureCompensation = ecRange.getUpper();

        Rational ecStep = p.get(CONTROL_AE_COMPENSATION_STEP);
        mExposureCompensationStep = (float) ecStep.getNumerator() / ecStep.getDenominator();

        mMaxNumOfFacesSupported = p.get(STATISTICS_INFO_MAX_FACE_COUNT);
        mMaxNumOfMeteringArea = p.get(CONTROL_MAX_REGIONS_AE);

        mMaxZoomRatio = p.get(SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
        // TODO: Populate mHorizontalViewAngle
        // TODO: Populate mVerticalViewAngle
        // TODO: Populate mZoomRatioList
        // TODO: Populate mMaxZoomIndex

        if (supports(FocusMode.AUTO)) {
            mMaxNumOfFocusAreas = p.get(CONTROL_MAX_REGIONS_AF);
            if (mMaxNumOfFocusAreas > 0) {
                mSupportedFeatures.add(Feature.FOCUS_AREA);
            }
        }
        if (mMaxNumOfMeteringArea > 0) {
            mSupportedFeatures.add(Feature.METERING_AREA);
        }

        if (mMaxZoomRatio > CameraCapabilities.ZOOM_RATIO_UNZOOMED) {
            mSupportedFeatures.add(Feature.ZOOM);
        }

         /* MODIFIED-BEGIN by sichao.hu, 2016-06-27,BUG-2418995*/
        int[] capabilities=p.get(REQUEST_AVAILABLE_CAPABILITIES);

        mZslSupported=false;
        for(int cap:capabilities){
            if(cap==REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING ||
                    cap==REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING){
                mZslSupported=true;
            }
            if(cap==REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR){
                mManualSensorSupported=true;
            }

        }

        double oneMillisSecondToNano=1000000;//nano
        Range<Long> exposureRange=p.get(SENSOR_INFO_EXPOSURE_TIME_RANGE);
        /* MODIFIED-BEGIN by sichao.hu, 2016-08-16,BUG-2743263*/
        if(exposureRange!=null) {
            mMinExposureTime = ((double) exposureRange.getLower() / oneMillisSecondToNano) + "";
            mMaxExposureTime = ((double) exposureRange.getUpper() / oneMillisSecondToNano) + "";
        }
        Range<Integer> isoRange=p.get(SENSOR_INFO_SENSITIVITY_RANGE);

        if(isoRange!=null) {
            mMinISO = isoRange.getLower();
            mMaxISO = isoRange.getUpper();
        }

        Log.w(TAG,String.format("exposure time ranges from %s to %s  and iso ranges from %d to %d",mMinExposureTime,mMaxExposureTime,mMinISO,mMaxISO));

        if(p.get(LENS_INFO_MINIMUM_FOCUS_DISTANCE)!=null) {
            float minFocusScale = 0;
            float maxFocusScale = p.get(LENS_INFO_MINIMUM_FOCUS_DISTANCE);  // 0.0 stands for infinity while the focus value will clamped to [0.0 , LENS_INFO_MINIMUM_FOCUS_DISTANCE]
            Log.w(TAG, "maxFocusScale is " + maxFocusScale);
            if (isoRange != null) {
                mMinFocusScale = 0;
                mMaxFocusScale = (int) maxFocusScale * 10;
        /* MODIFIED-END by sichao.hu,BUG-2418995*/
                // TODO: Detect other features
            }
        }
        /* MODIFIED-END by sichao.hu,BUG-2743263*/
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void buildSceneModes(CameraCharacteristics p) {
        int[] scenes = p.get(CONTROL_AVAILABLE_SCENE_MODES);
        if (scenes != null) {
            for (int scene : scenes) {
                SceneMode equiv = sceneModeFromInt(scene);
                if (equiv != null) {
                    mSupportedSceneModes.add(equiv);
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void buildFlashModes(CameraCharacteristics p) {
        mSupportedFlashModes.add(FlashMode.OFF);
        if (p.get(FLASH_INFO_AVAILABLE)) {
            mSupportedFlashModes.add(FlashMode.AUTO);
            mSupportedFlashModes.add(FlashMode.ON);
            mSupportedFlashModes.add(FlashMode.TORCH);
            for (int expose : p.get(CONTROL_AE_AVAILABLE_MODES)) {
                if (expose == CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE) {
                    mSupportedFlashModes.add(FlashMode.RED_EYE);
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void buildFocusModes(CameraCharacteristics p) {
        int[] focuses = p.get(CONTROL_AF_AVAILABLE_MODES);
        if (focuses != null) {
            for (int focus : focuses) {
                FocusMode equiv = focusModeFromInt(focus);
                if (equiv != null) {
                    mSupportedFocusModes.add(equiv);
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void buildWhiteBalances(CameraCharacteristics p) {
        int[] bals = p.get(CONTROL_AWB_AVAILABLE_MODES);
        if (bals != null) {
            for (int bal : bals) {
                WhiteBalance equiv = whiteBalanceFromInt(bal);
                if (equiv != null) {
                    mSupportedWhiteBalances.add(equiv);
                }
            }
        }
    }

    /**
     * Converts the API-related integer representation of the focus mode to the
     * abstract representation.
     *
     * @param fm The integral representation.
     * @return The mode represented by the input integer, or {@code null} if it
     *         cannot be converted.
     */
    public static FocusMode focusModeFromInt(int fm) {
        switch (fm) {
            case CONTROL_AF_MODE_AUTO:
                return FocusMode.AUTO;
            case CONTROL_AF_MODE_CONTINUOUS_PICTURE:
                return FocusMode.CONTINUOUS_PICTURE;
            case CONTROL_AF_MODE_CONTINUOUS_VIDEO:
                return FocusMode.CONTINUOUS_VIDEO;
            case CONTROL_AF_MODE_EDOF:
                return FocusMode.EXTENDED_DOF;
            case CONTROL_AF_MODE_OFF:
                return FocusMode.FIXED;
            // TODO: We cannot support INFINITY
            case CONTROL_AF_MODE_MACRO:
                return FocusMode.MACRO;
        }
        Log.w(TAG, "Unable to convert from API 2 focus mode: " + fm);
        return null;
    }

    /**
     * Converts the API-related integer representation of the scene mode to the
     * abstract representation.
     *
     * @param sm The integral representation.
     * @return The mode represented by the input integer, or {@code null} if it
     *         cannot be converted.
     */
    public static SceneMode sceneModeFromInt(int sm) {
        switch (sm) {
            case CONTROL_SCENE_MODE_DISABLED:
                return SceneMode.AUTO;
            case CONTROL_SCENE_MODE_ACTION:
                return SceneMode.ACTION;
            case CONTROL_SCENE_MODE_BARCODE:
                return SceneMode.BARCODE;
            case CONTROL_SCENE_MODE_BEACH:
                return SceneMode.BEACH;
            case CONTROL_SCENE_MODE_CANDLELIGHT:
                return SceneMode.CANDLELIGHT;
            case CONTROL_SCENE_MODE_FIREWORKS:
                return SceneMode.FIREWORKS;
            case CONTROL_SCENE_MODE_LANDSCAPE:
                return SceneMode.LANDSCAPE;
            case CONTROL_SCENE_MODE_NIGHT:
                return SceneMode.NIGHT;
            // TODO: We cannot support NIGHT_PORTRAIT
            case CONTROL_SCENE_MODE_PARTY:
                return SceneMode.PARTY;
            case CONTROL_SCENE_MODE_PORTRAIT:
                return SceneMode.PORTRAIT;
            case CONTROL_SCENE_MODE_SNOW:
                return SceneMode.SNOW;
            case CONTROL_SCENE_MODE_SPORTS:
                return SceneMode.SPORTS;
            case CONTROL_SCENE_MODE_STEADYPHOTO:
                return SceneMode.STEADYPHOTO;
            case CONTROL_SCENE_MODE_SUNSET:
                return SceneMode.SUNSET;
            case CONTROL_SCENE_MODE_THEATRE:
                return SceneMode.THEATRE;
            // TODO: We cannot expose FACE_PRIORITY, or HIGH_SPEED_VIDEO
        }

        if (sm == LegacyVendorTags.CONTROL_SCENE_MODE_HDR) {
            return SceneMode.HDR;
        }

        Log.w(TAG, "Unable to convert from API 2 scene mode: " + sm);
        return null;
    }

    /**
     * Converts the API-related integer representation of the white balance to
     * the abstract representation.
     *
     * @param wb The integral representation.
     * @return The balance represented by the input integer, or {@code null} if
     *         it cannot be converted.
     */
    public static WhiteBalance whiteBalanceFromInt(int wb) {
        switch (wb) {
            case CONTROL_AWB_MODE_AUTO:
                return WhiteBalance.AUTO;
            case CONTROL_AWB_MODE_CLOUDY_DAYLIGHT:
                return WhiteBalance.CLOUDY_DAYLIGHT;
            case CONTROL_AWB_MODE_DAYLIGHT:
                return WhiteBalance.DAYLIGHT;
            case CONTROL_AWB_MODE_FLUORESCENT:
                return WhiteBalance.FLUORESCENT;
            case CONTROL_AWB_MODE_INCANDESCENT:
                return WhiteBalance.INCANDESCENT;
            case CONTROL_AWB_MODE_SHADE:
                return WhiteBalance.SHADE;
            case CONTROL_AWB_MODE_TWILIGHT:
                return WhiteBalance.TWILIGHT;
            case CONTROL_AWB_MODE_WARM_FLUORESCENT:
                return WhiteBalance.WARM_FLUORESCENT;
        }
        Log.w(TAG, "Unable to convert from API 2 white balance: " + wb);
        return null;
    }
}
