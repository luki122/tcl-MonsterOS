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

import com.android.camera.settings.Keys;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.ex.camera2.portability.debug.Log;
import com.android.external.ExtendParameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The subclass of {@link CameraCapabilities} for Android Camera 1 API.
 */
class AndroidCameraCapabilities extends CameraCapabilities {

    private static Log.Tag TAG = new Log.Tag("AndCamCapabs");

    /** Conversion from ratios to percentages. */
    public static final float ZOOM_MULTIPLIER = 100f;

    private FpsComparator mFpsComparator = new FpsComparator();
    private SizeComparator mSizeComparator = new SizeComparator();

    AndroidCameraCapabilities(Camera.Parameters p) {
        super(new Stringifier());
        mMaxExposureCompensation = p.getMaxExposureCompensation();
        ExtendParameters exP=ExtendParameters.getInstance(p);
        List<String> modes=exP.getSupportedZSLValues();
        mZslSupported=(modes!=null&&modes.size()>0);
        mMinExposureCompensation = p.getMinExposureCompensation();
        mExposureCompensationStep = p.getExposureCompensationStep();
        mMaxNumOfFacesSupported = p.getMaxNumDetectedFaces();
        mMaxNumOfMeteringArea = p.getMaxNumMeteringAreas();
        mPreferredPreviewSizeForVideo = new Size(p.getPreferredPreviewSizeForVideo());
        mSupportedPreviewFormats.addAll(p.getSupportedPreviewFormats());
        mSupportedPhotoFormats.addAll(p.getSupportedPictureFormats());
        mHorizontalViewAngle = p.getHorizontalViewAngle();
        mVerticalViewAngle = p.getVerticalViewAngle();
        buildPreviewFpsRange(p);
        buildPreviewSizes(p);
        buildVideoSizes(p);
        buildPictureSizes(p);
        buildSceneModes(p);
        buildFlashModes(p);
        buildFocusModes(p);
        buildWhiteBalances(p);
        buildVideoHighFrameRates(p);
        buildHsrSizes(p);
        if (p.isZoomSupported()) {
            mMaxZoomRatio = p.getZoomRatios().get(p.getMaxZoom()) / ZOOM_MULTIPLIER;
            mSupportedFeatures.add(Feature.ZOOM);
        }
        if (p.isVideoSnapshotSupported()) {
            mSupportedFeatures.add(Feature.VIDEO_SNAPSHOT);
        }
       if (p.isVideoStabilizationSupported() || CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_TCT_EIS, false)) {
            mSupportedFeatures.add(Feature.VIDEO_STABILIZATION);
        }
        if (p.isAutoExposureLockSupported()) {
            mSupportedFeatures.add(Feature.AUTO_EXPOSURE_LOCK);
        }
        if (p.isAutoWhiteBalanceLockSupported()) {
            mSupportedFeatures.add(Feature.AUTO_WHITE_BALANCE_LOCK);
        }
        if (supports(FocusMode.AUTO)) {
            mMaxNumOfFocusAreas = p.getMaxNumFocusAreas();
            if (mMaxNumOfFocusAreas > 0) {
                mSupportedFeatures.add(Feature.FOCUS_AREA);
            }
        }
        if (mMaxNumOfMeteringArea > 0) {
            mSupportedFeatures.add(Feature.METERING_AREA);
        }
        buildManualModes(p);
        List<String> supportedAntibanding = p.getSupportedAntibanding();
        if (supportedAntibanding != null) {
            mSupportedAntibanding.addAll(supportedAntibanding);
        }
    }

    AndroidCameraCapabilities(AndroidCameraCapabilities src) {
        super(src);
    }

    private void buildPreviewFpsRange(Camera.Parameters p) {
        List<int[]> supportedPreviewFpsRange = p.getSupportedPreviewFpsRange();
        if (supportedPreviewFpsRange != null) {
            mSupportedPreviewFpsRange.addAll(supportedPreviewFpsRange);
        }
        Collections.sort(mSupportedPreviewFpsRange, mFpsComparator);
    }

    private void buildPreviewSizes(Camera.Parameters p) {
        List<Camera.Size> supportedPreviewSizes = p.getSupportedPreviewSizes();
        if (supportedPreviewSizes != null) {
            for (Camera.Size s : supportedPreviewSizes) {
                mSupportedPreviewSizes.add(new Size(s.width, s.height));
            }
        }
        Collections.sort(mSupportedPreviewSizes, mSizeComparator);
    }

    private void buildVideoSizes(Camera.Parameters p) {
        List<Camera.Size> supportedVideoSizes = p.getSupportedVideoSizes();
        if (supportedVideoSizes != null) {
            for (Camera.Size s : supportedVideoSizes) {
                mSupportedVideoSizes.add(new Size(s.width, s.height));
            }
        }
        Collections.sort(mSupportedVideoSizes, mSizeComparator);
    }

    private void buildPictureSizes(Camera.Parameters p) {
        List<Camera.Size> supportedPictureSizes = p.getSupportedPictureSizes();
        if (supportedPictureSizes != null) {
            for (Camera.Size s : supportedPictureSizes) {
                mSupportedPhotoSizes.add(new Size(s.width, s.height));
            }
        }
        Collections.sort(mSupportedPhotoSizes, mSizeComparator);

    }

    private void buildSceneModes(Camera.Parameters p) {
        List<String> supportedSceneModes = p.getSupportedSceneModes();
        if (supportedSceneModes != null) {
            for (String scene : supportedSceneModes) {
                if (Camera.Parameters.SCENE_MODE_AUTO.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.AUTO);
                } else if (Camera.Parameters.SCENE_MODE_ACTION.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.ACTION);
                } else if (Camera.Parameters.SCENE_MODE_BARCODE.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.BARCODE);
                } else if (Camera.Parameters.SCENE_MODE_BEACH.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.BEACH);
                } else if (Camera.Parameters.SCENE_MODE_CANDLELIGHT.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.CANDLELIGHT);
                } else if (Camera.Parameters.SCENE_MODE_FIREWORKS.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.FIREWORKS);
                } else if (Camera.Parameters.SCENE_MODE_HDR.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.HDR);
                } else if (Camera.Parameters.SCENE_MODE_LANDSCAPE.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.LANDSCAPE);
                } else if (Camera.Parameters.SCENE_MODE_NIGHT.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.NIGHT);
                } else if (Camera.Parameters.SCENE_MODE_NIGHT_PORTRAIT.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.NIGHT_PORTRAIT);
                } else if (Camera.Parameters.SCENE_MODE_PARTY.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.PARTY);
                } else if (Camera.Parameters.SCENE_MODE_PORTRAIT.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.PORTRAIT);
                } else if (Camera.Parameters.SCENE_MODE_SNOW.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.SNOW);
                } else if (Camera.Parameters.SCENE_MODE_SPORTS.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.SPORTS);
                } else if (Camera.Parameters.SCENE_MODE_STEADYPHOTO.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.STEADYPHOTO);
                } else if (Camera.Parameters.SCENE_MODE_SUNSET.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.SUNSET);
                } else if (Camera.Parameters.SCENE_MODE_THEATRE.equals(scene)) {
                    mSupportedSceneModes.add(SceneMode.THEATRE);
                }
            }
        }
        /* MODIFIED-BEGIN by xuyang.liu, 2016-10-13,BUG-3110198*/
        if (Keys.isAutoHdrEnable()) {
            mSupportedSceneModes.add(SceneMode.HDR_AUTO);
        }
        /* MODIFIED-END by xuyang.liu,BUG-3110198*/
    }

    private void buildFlashModes(Camera.Parameters p) {
        List<String> supportedFlashModes = p.getSupportedFlashModes();
        if (supportedFlashModes == null) {
            // Camera 1 will return NULL if no flash mode is supported.
            mSupportedFlashModes.add(FlashMode.NO_FLASH);
        } else {
            for (String flash : supportedFlashModes) {
                if (Camera.Parameters.FLASH_MODE_AUTO.equals(flash)) {
                    mSupportedFlashModes.add(FlashMode.AUTO);
                } else if (Camera.Parameters.FLASH_MODE_OFF.equals(flash)) {
                    mSupportedFlashModes.add(FlashMode.OFF);
                } else if (Camera.Parameters.FLASH_MODE_ON.equals(flash)) {
                    mSupportedFlashModes.add(FlashMode.ON);
                } else if (Camera.Parameters.FLASH_MODE_RED_EYE.equals(flash)) {
                    mSupportedFlashModes.add(FlashMode.RED_EYE);
                } else if (Camera.Parameters.FLASH_MODE_TORCH.equals(flash)) {
                    mSupportedFlashModes.add(FlashMode.TORCH);
                }
            }
        }
    }

    private void buildFocusModes(Camera.Parameters p) {
        List<String> supportedFocusModes = p.getSupportedFocusModes();
        if (supportedFocusModes != null) {
            for (String focus : supportedFocusModes) {
                if (Camera.Parameters.FOCUS_MODE_AUTO.equals(focus)) {
                    mSupportedFocusModes.add(FocusMode.AUTO);
                } else if (Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE.equals(focus)) {
                    mSupportedFocusModes.add(FocusMode.CONTINUOUS_PICTURE);
                } else if (Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO.equals(focus)) {
                    mSupportedFocusModes.add(FocusMode.CONTINUOUS_VIDEO);
                } else if (Camera.Parameters.FOCUS_MODE_EDOF.equals(focus)) {
                    mSupportedFocusModes.add(FocusMode.EXTENDED_DOF);
                } else if (Camera.Parameters.FOCUS_MODE_FIXED.equals(focus)) {
                    mSupportedFocusModes.add(FocusMode.FIXED);
                } else if (Camera.Parameters.FOCUS_MODE_INFINITY.equals(focus)) {
                    mSupportedFocusModes.add(FocusMode.INFINITY);
                } else if (Camera.Parameters.FOCUS_MODE_MACRO.equals(focus)) {
                    mSupportedFocusModes.add(FocusMode.MACRO);
                } else if (FOCUS_MODE_MANUAL.equals(focus)) {
                    mSupportedFocusModes.add(FocusMode.MANUAL);
                }
            }
        }
    }

    private void buildWhiteBalances(Camera.Parameters p) {
        List<String> supportedWhiteBalances = p.getSupportedWhiteBalance();
        if (supportedWhiteBalances != null) {
            for (String wb : supportedWhiteBalances) {
                if (Camera.Parameters.WHITE_BALANCE_AUTO.equals(wb)) {
                    mSupportedWhiteBalances.add(WhiteBalance.AUTO);
                } else if (Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT.equals(wb)) {
                    mSupportedWhiteBalances.add(WhiteBalance.CLOUDY_DAYLIGHT);
                } else if (Camera.Parameters.WHITE_BALANCE_DAYLIGHT.equals(wb)) {
                    mSupportedWhiteBalances.add(WhiteBalance.DAYLIGHT);
                } else if (Camera.Parameters.WHITE_BALANCE_FLUORESCENT.equals(wb)) {
                    mSupportedWhiteBalances.add(WhiteBalance.FLUORESCENT);
                } else if (Camera.Parameters.WHITE_BALANCE_INCANDESCENT.equals(wb)) {
                    mSupportedWhiteBalances.add(WhiteBalance.INCANDESCENT);
                } else if (Camera.Parameters.WHITE_BALANCE_SHADE.equals(wb)) {
                    mSupportedWhiteBalances.add(WhiteBalance.SHADE);
                } else if (Camera.Parameters.WHITE_BALANCE_TWILIGHT.equals(wb)) {
                    mSupportedWhiteBalances.add(WhiteBalance.TWILIGHT);
                } else if (Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT.equals(wb)) {
                    mSupportedWhiteBalances.add(WhiteBalance.WARM_FLUORESCENT);
                }
            }
        }
    }

    private void buildManualModes(Camera.Parameters p) {
        ExtendParameters parameters=ExtendParameters.getInstance(p);
        mMinISO=parameters.getMinISO();
        mSupportedIsoValues=parameters.getSupportedISOValues();
        mMaxISO=parameters.getMaxISO();
        mMinExposureTime=parameters.getMinExposureTime();
        mMaxExposureTime=parameters.getMaxExposureTime();
        mMinFocusScale =parameters.getMinFocusScale();
        mMaxFocusScale =parameters.getMaxFocusScale();
    }
    private ArrayList<String> split(String str) {
        if (str == null) return null;

        TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(',');
        splitter.setString(str);
        ArrayList<String> substrings = new ArrayList<String>();
        for (String s : splitter) {
            substrings.add(s);
        }
        return substrings;
    }

    private static class FpsComparator implements Comparator<int[]> {
        @Override
        public int compare(int[] fps1, int[] fps2) {
            return (fps1[0] == fps2[0] ? fps1[1] - fps2[1] : fps1[0] - fps2[0]);
        }
    }

    private void buildVideoHighFrameRates(Camera.Parameters p) {
        String supportedVideoHighFrameRates = p.get(KEY_VIDEO_HIGH_FRAME_RATE_MODES);
        if (supportedVideoHighFrameRates != null){
            TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(',');
            splitter.setString(supportedVideoHighFrameRates);

            for (String s : splitter) {
                mSupportedVideoHighFrameRates.add(s);
            }
        }
    }

    private void buildHsrSizes(Camera.Parameters p) {
        String supportedHsrSizes = p.get(KEY_HSR_SIZES);
        if(supportedHsrSizes != null){
            TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(',');
            splitter.setString(supportedHsrSizes);

            for (String s : splitter) {

                int pos = s.indexOf('x');
                if (pos != -1) {
                    String width = s.substring(0, pos);
                    String height = s.substring(pos + 1);
                    mSupportedHsrSizes.add(new Size(Integer.parseInt(width), Integer.parseInt(height)));
                }
            }
        }
    }

    private static class SizeComparator implements Comparator<Size> {

        @Override
        public int compare(Size size1, Size size2) {
            return (size1.width() == size2.width() ? size1.height() - size2.height() :
                    size1.width() - size2.width());
        }
    }
}
