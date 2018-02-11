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

package com.android.camera.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.CamcorderProfile;
import android.text.TextUtils;
import android.util.SparseArray;

import com.android.camera.app.AppController;
import com.android.camera.debug.Log;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.Callback;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.external.ExtSystemProperties;
import com.tct.camera.R;
import com.android.ex.camera2.portability.CameraDeviceInfo;
import com.android.ex.camera2.portability.CameraSettings;
import com.android.ex.camera2.portability.Size;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Utility functions around camera settings.
 */
public class SettingsUtil {

    public static final String PANO_VENDOR_DEFAULT = "morpho";
    public static final String PANO_VENDOR_MTK = "mtk";
    public static final String PANO_VENDOR_NONE = "none";
    /**
     * Returns the maximum video recording duration (in milliseconds).
     */
    public static int getMaxVideoDuration(Context context) {
        int duration = 0; // in milliseconds, 0 means unlimited.
        try {
            duration = context.getResources().getInteger(R.integer.max_video_recording_length);
        } catch (Resources.NotFoundException ex) {
        }
        return duration;
    }

    /** The selected Camera sizes. */
    public static class SelectedPictureSizes {
        public Size large;
        public Size medium;
        public Size small;

        /**
         * This takes a string preference describing the desired resolution and
         * returns the camera size it represents. <br/>
         * It supports historical values of SIZE_LARGE, SIZE_MEDIUM, and
         * SIZE_SMALL as well as resolutions separated by an x i.e. "1024x576" <br/>
         * If it fails to parse the string, it will return the old SIZE_LARGE
         * value.
         *
         * @param sizeSetting the preference string to convert to a size
         * @param supportedSizes all possible camera sizes that are supported
         * @return the size that this setting represents
         */
        public Size getFromSetting(String sizeSetting, List<Size> supportedSizes) {
            if (SIZE_LARGE.equals(sizeSetting)) {
                return large;
            } else if (SIZE_MEDIUM.equals(sizeSetting)) {
                return medium;
            } else if (SIZE_SMALL.equals(sizeSetting)) {
                return small;
            } else if (sizeSetting != null && sizeSetting.split("x").length == 2) {
                Size desiredSize = sizeFromString(sizeSetting);
                if (supportedSizes.contains(desiredSize)) {
                    return desiredSize;
                }
            }
            return large;
        }

        @Override
        public String toString() {
            return "SelectedPictureSizes: " + large + ", " + medium + ", " + small;
        }
    }

    /** The selected {@link CamcorderProfile} qualities. */
    public static class SelectedVideoQualities {
        public int large = -1;
        public int medium = -1;
        public int small = -1;

        public int getFromSetting(String sizeSetting) {
            // Sanitize the value to be either small, medium or large. Default
            // to the latter.
            if (!SIZE_SMALL.equals(sizeSetting) && !SIZE_MEDIUM.equals(sizeSetting)) {
                sizeSetting = SIZE_LARGE;
            }

            if (SIZE_LARGE.equals(sizeSetting)) {
                return large;
            } else if (SIZE_MEDIUM.equals(sizeSetting)) {
                return medium;
            } else {
                return small;
            }
        }
    }

    private static final Log.Tag TAG = new Log.Tag("SettingsUtil");

    /** Enable debug output. */
    private static final boolean DEBUG = false;

    private static final String SIZE_LARGE = "large";
    private static final String SIZE_MEDIUM = "medium";
    private static final String SIZE_SMALL = "small";

    /** The ideal "medium" picture size is 50% of "large". */
    private static final float MEDIUM_RELATIVE_PICTURE_SIZE = 0.5f;

    /** The ideal "small" picture size is 25% of "large". */
    private static final float SMALL_RELATIVE_PICTURE_SIZE = 0.25f;

    /** Video qualities sorted by size. */
    public static int[] sVideoQualities = new int[] {
            CamcorderProfile.QUALITY_2160P,
            CamcorderProfile.QUALITY_1080P,
            CamcorderProfile.QUALITY_720P,
            CamcorderProfile.QUALITY_480P,
            CamcorderProfile.QUALITY_CIF,
            CamcorderProfile.QUALITY_QVGA,
            CamcorderProfile.QUALITY_QCIF
    };


    public static final HashMap<String, Integer> VIDEO_QUALITY_TABLE = new HashMap<String, Integer>();
    static {

        //video qualities
        VIDEO_QUALITY_TABLE.put("3840x2160", CamcorderProfile.QUALITY_2160P);
        VIDEO_QUALITY_TABLE.put("1920x1080", CamcorderProfile.QUALITY_1080P);
        VIDEO_QUALITY_TABLE.put("1280x720",  CamcorderProfile.QUALITY_720P);
        VIDEO_QUALITY_TABLE.put("720x480",   CamcorderProfile.QUALITY_480P);
        VIDEO_QUALITY_TABLE.put("352x288",   CamcorderProfile.QUALITY_CIF);
        VIDEO_QUALITY_TABLE.put("320x240",   CamcorderProfile.QUALITY_QVGA);
        VIDEO_QUALITY_TABLE.put("176x144",   CamcorderProfile.QUALITY_QCIF);
    }

    public static final HashMap<String, Integer> VIDEO_QUALITY_VALUE_TABLE = new HashMap<String, Integer>();
    public static final String QUALITY_1080P_60FPS_NAME = "QUALITY_1080P_60FPS";
    public static final String QUALITY_1080P_60FPS = "-6";
    static {
        VIDEO_QUALITY_VALUE_TABLE.put(QUALITY_1080P_60FPS, CamcorderProfile.QUALITY_1080P);
    }

    public static SparseArray<SelectedPictureSizes> sCachedSelectedPictureSizes =
            new SparseArray<SelectedPictureSizes>(2);
    public static SparseArray<SelectedVideoQualities> sCachedSelectedVideoQualities =
            new SparseArray<SelectedVideoQualities>(2);

    /**
     * Based on the selected size, this method selects the matching concrete
     * resolution and sets it as the picture size.
     *
     * @param sizeSetting The setting selected by the user. One of "large",
     *            "medium, "small" or two integers separated by "x".
     * @param supported The list of supported resolutions.
     * @param settings The Camera settings to set the selected picture
     *            resolution on.
     * @param cameraId This is used for caching the results for finding the
     *            different sizes.
     */
    public static void setCameraPictureSize(String sizeSetting, List<Size> supported,
            CameraSettings settings, int cameraId) {
        Size selectedSize = getCameraPictureSize(sizeSetting, supported, cameraId);
        Log.d(TAG, "Selected " + sizeSetting + " resolution: " + selectedSize.width() + "x" +
                selectedSize.height());
        settings.setPhotoSize(selectedSize);
    }

    /**
     * Based on the selected size, this method returns the matching concrete
     * resolution.
     *
     * @param sizeSetting The setting selected by the user. One of "large",
     *            "medium, "small".
     * @param supported The list of supported resolutions.
     * @param cameraId This is used for caching the results for finding the
     *            different sizes.
     */
    public static Size getPhotoSize(String sizeSetting, List<Size> supported, int cameraId) {
        if (ResolutionUtil.NEXUS_5_LARGE_16_BY_9.equals(sizeSetting)) {
            return ResolutionUtil.NEXUS_5_LARGE_16_BY_9_SIZE;
        }
        Size selectedSize = getCameraPictureSize(sizeSetting, supported, cameraId);
        return selectedSize;
    }

    /**
     * Based on the selected size (large, medium or small), and the list of
     * supported resolutions, this method selects and returns the best matching
     * picture size.
     *
     * @param sizeSetting The setting selected by the user. One of "large",
     *            "medium, "small".
     * @param supported The list of supported resolutions.
     * @param cameraId This is used for caching the results for finding the
     *            different sizes.
     * @return The selected size.
     */
    private static Size getCameraPictureSize(String sizeSetting, List<Size> supported,
            int cameraId) {
        return getSelectedCameraPictureSizes(supported, cameraId).getFromSetting(sizeSetting,
                supported);
    }

    /**
     * Based on the list of supported resolutions, this method selects the ones
     * that shall be selected for being 'large', 'medium' and 'small'.
     *
     * @return It's guaranteed that all three sizes are filled. If less than
     *         three sizes are supported, the selected sizes might contain
     *         duplicates.
     */
    static SelectedPictureSizes getSelectedCameraPictureSizes(List<Size> supported, int cameraId) {
        List<Size> supportedCopy = new LinkedList<Size>(supported);
        if (sCachedSelectedPictureSizes.get(cameraId) != null) {
            return sCachedSelectedPictureSizes.get(cameraId);
        }
        if (supportedCopy == null) {
            return null;
        }

        SelectedPictureSizes selectedSizes = new SelectedPictureSizes();

        // Sort supported sizes by total pixel count, descending.
        Collections.sort(supportedCopy, new Comparator<Size>() {
            @Override
            public int compare(Size lhs, Size rhs) {
                int leftArea = lhs.width() * lhs.height();
                int rightArea = rhs.width() * rhs.height();
                return rightArea - leftArea;
            }
        });
        if (DEBUG) {
            Log.d(TAG, "Supported Sizes:");
            for (Size size : supportedCopy) {
                Log.d(TAG, " --> " + size.width() + "x" + size.height() + "  "
                        + ((size.width() * size.height()) / 1000000f) + " - "
                        + (size.width() / (float) size.height()));
            }
        }

        // Large size is always the size with the most pixels reported.
        selectedSizes.large = supportedCopy.remove(0);

        // If possible we want to find medium and small sizes with the same
        // aspect ratio as 'large'.
        final float targetAspectRatio = selectedSizes.large.width()
                / (float) selectedSizes.large.height();

        // Create a list of sizes with the same aspect ratio as "large" which we
        // will search in primarily.
        ArrayList<Size> aspectRatioMatches = new ArrayList<Size>();
        for (Size size : supportedCopy) {
            float aspectRatio = size.width() / (float) size.height();
            // Allow for small rounding errors in aspect ratio.
            if (Math.abs(aspectRatio - targetAspectRatio) < 0.01) {
                aspectRatioMatches.add(size);
            }
        }

        // If we have at least two more resolutions that match the 'large'
        // aspect ratio, use that list to find small and medium sizes. If not,
        // use the full list with any aspect ratio.
        final List<Size> searchList = (aspectRatioMatches.size() >= 2) ? aspectRatioMatches
                : supportedCopy;

        // Edge cases: If there are no further supported resolutions, use the
        // only one we have.
        // If there is only one remaining, use it for small and medium. If there
        // are two, use the two for small and medium.
        // These edge cases should never happen on a real device, but might
        // happen on test devices and emulators.
        if (searchList.isEmpty()) {
            Log.w(TAG, "Only one supported resolution.");
            selectedSizes.medium = selectedSizes.large;
            selectedSizes.small = selectedSizes.large;
        } else if (searchList.size() == 1) {
            Log.w(TAG, "Only two supported resolutions.");
            selectedSizes.medium = searchList.get(0);
            selectedSizes.small = searchList.get(0);
        } else if (searchList.size() == 2) {
            Log.w(TAG, "Exactly three supported resolutions.");
            selectedSizes.medium = searchList.get(0);
            selectedSizes.small = searchList.get(1);
        } else {

            // Based on the large pixel count, determine the target pixel count
            // for medium and small.
            final int largePixelCount = selectedSizes.large.width() * selectedSizes.large.height();
            final int mediumTargetPixelCount = (int) (largePixelCount * MEDIUM_RELATIVE_PICTURE_SIZE);
            final int smallTargetPixelCount = (int) (largePixelCount * SMALL_RELATIVE_PICTURE_SIZE);

            int mediumSizeIndex = findClosestSize(searchList, mediumTargetPixelCount);
            int smallSizeIndex = findClosestSize(searchList, smallTargetPixelCount);

            // If the selected sizes are the same, move the small size one down
            // or
            // the medium size one up.
            if (searchList.get(mediumSizeIndex).equals(searchList.get(smallSizeIndex))) {
                if (smallSizeIndex < (searchList.size() - 1)) {
                    smallSizeIndex += 1;
                } else {
                    mediumSizeIndex -= 1;
                }
            }
            selectedSizes.medium = searchList.get(mediumSizeIndex);
            selectedSizes.small = searchList.get(smallSizeIndex);
        }
        sCachedSelectedPictureSizes.put(cameraId, selectedSizes);
        return selectedSizes;
    }

    /**
     * Determines the video quality for large/medium/small for the given camera.
     * Returns the one matching the given setting. Defaults to 'large' of the
     * qualitySetting does not match either large. medium or small.
     *
     * @param qualitySetting One of 'large', 'medium', 'small'.
     * @param cameraId The ID of the camera for which to get the quality
     *            setting.
     * @return The CamcorderProfile quality setting.
     */
    public static int getVideoQuality(String qualitySetting, int cameraId) {
        return getSelectedVideoQualities(cameraId).getFromSetting(qualitySetting);
    }

    static SelectedVideoQualities getSelectedVideoQualities(int cameraId) {
        if (sCachedSelectedVideoQualities.get(cameraId) != null) {
            return sCachedSelectedVideoQualities.get(cameraId);
        }

        // Go through the sizes in descending order, see if they are supported,
        // and set large/medium/small accordingly.
        // If no quality is supported at all, the first call to
        // getNextSupportedQuality will throw an exception.
        // If only one quality is supported, then all three selected qualities
        // will be the same.
        int largeIndex = getNextSupportedVideoQualityIndex(cameraId, -1);
        int mediumIndex = getNextSupportedVideoQualityIndex(cameraId, largeIndex);
        int smallIndex = getNextSupportedVideoQualityIndex(cameraId, mediumIndex);

        SelectedVideoQualities selectedQualities = new SelectedVideoQualities();
        selectedQualities.large = sVideoQualities[largeIndex];
        selectedQualities.medium = sVideoQualities[mediumIndex];
        selectedQualities.small = sVideoQualities[smallIndex];
        sCachedSelectedVideoQualities.put(cameraId, selectedQualities);
        return selectedQualities;
    }

    /**
     * Starting from 'start' this method returns the next supported video
     * quality.
     */
    private static int getNextSupportedVideoQualityIndex(int cameraId, int start) {
        for (int i = start + 1; i < sVideoQualities.length; ++i) {
            if (isVideoQualitySupported(sVideoQualities[i])
                    && CamcorderProfile.hasProfile(cameraId, sVideoQualities[i])) {
                // We found a new supported quality.
                return i;
            }
        }

        // Failed to find another supported quality.
        if (start < 0 || start >= sVideoQualities.length) {
            // This means we couldn't find any supported quality.
            throw new IllegalArgumentException("Could not find supported video qualities.");
        }

        // We previously found a larger supported size. In this edge case, just
        // return the same index as the previous size.
        return start;
    }

    /**
     * @return Whether the given {@link CamcorderProfile} is supported on the
     *         current device/OS version.
     */
    public static boolean isVideoQualitySupported(int videoQuality) {
        // 4k is only supported on L or higher but some devices falsely report
        // to have support for it on K, see b/18172081.
        if (!ApiHelper.isLOrHigher() && videoQuality == CamcorderProfile.QUALITY_2160P) {
            return false;
        }
        return true;
    }

    /**
     * Returns the index of the size within the given list that is closest to
     * the given target pixel count.
     */
    private static int findClosestSize(List<Size> sortedSizes, int targetPixelCount) {
        int closestMatchIndex = 0;
        int closestMatchPixelCountDiff = Integer.MAX_VALUE;

        for (int i = 0; i < sortedSizes.size(); ++i) {
            Size size = sortedSizes.get(i);
            int pixelCountDiff = Math.abs((size.width() * size.height()) - targetPixelCount);
            if (pixelCountDiff < closestMatchPixelCountDiff) {
                closestMatchIndex = i;
                closestMatchPixelCountDiff = pixelCountDiff;
            }
        }
        return closestMatchIndex;
    }

    /**
     * This is used to serialize a size to a string for storage in settings
     *
     * @param size The size to serialize.
     * @return the string to be saved in preferences
     */
    public static String sizeToSetting(Size size) {
        return ((Integer) size.width()).toString() + "x" + ((Integer) size.height()).toString();
    }

    /**
     * This parses a setting string and returns the representative size.
     *
     * @param sizeSetting The string to parse.
     * @return the represented Size.
     */
    static public Size sizeFromString(String sizeSetting) {
        String[] parts = sizeSetting.split("x");
        if (parts.length == 2) {
            return new Size(Integer.valueOf(parts[0]), Integer.valueOf(parts[1]));
        } else {
            return null;
        }
    }

    /**
     * Updates an AlertDialog.Builder to explain what it means to enable
     * location on captures.
     */
    public static AlertDialog.Builder getFirstTimeLocationAlertBuilder(
            AlertDialog.Builder builder, Callback<Boolean> callback) {
        if (callback == null) {
            return null;
        }

        getLocationAlertBuilder(builder, callback)
                .setMessage(R.string.remember_location_prompt);

        return builder;
    }

    /**
     * Updates an AlertDialog.Builder for choosing whether to include location
     * on captures.
     */
    public static AlertDialog.Builder getLocationAlertBuilder(AlertDialog.Builder builder,
            final Callback<Boolean> callback) {
        if (callback == null) {
            return null;
        }

        builder.setTitle(R.string.remember_location_title)
                .setPositiveButton(R.string.remember_location_yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                callback.onCallback(true);
                            }
                        })
                .setNegativeButton(R.string.remember_location_no,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                callback.onCallback(false);
                            }
                        });

        return builder;
    }

    /**
     * Gets the first (lowest-indexed) camera matching the given criterion.
     *
     * @param facing Either {@link CAMERA_FACING_BACK}, {@link CAMERA_FACING_FRONT}, or some other
     *               implementation of {@link CameraDeviceSelector}.
     * @return The ID of the first camera matching the supplied criterion, or
     *         -1, if no camera meeting the specification was found.
     */
    public static int getCameraId(CameraDeviceInfo info, CameraDeviceSelector chooser) {
        int numCameras = info.getNumberOfCameras();
        for (int i = 0; i < numCameras; ++i) {
            CameraDeviceInfo.Characteristics props = info.getCharacteristics(i);
            if (props == null) {
                // Skip this device entry
                continue;
            }
            if (chooser.useCamera(props)) {
                return i;
            }
        }
        return -1;
    }

    public static interface CameraDeviceSelector {
        /**
         * Given the static characteristics of a specific camera device, decide whether it is the
         * one we will use.
         *
         * @param info The static characteristics of a device.
         * @return Whether we're electing to use this particular device.
         */
        public boolean useCamera(CameraDeviceInfo.Characteristics info);
    }

    public static final CameraDeviceSelector CAMERA_FACING_BACK = new CameraDeviceSelector() {
        @Override
        public boolean useCamera(CameraDeviceInfo.Characteristics info) {
            return info.isFacingBack();
        }};

    public static final CameraDeviceSelector CAMERA_FACING_FRONT = new CameraDeviceSelector() {
        @Override
        public boolean useCamera(CameraDeviceInfo.Characteristics info) {
            return info.isFacingFront();
        }};

    public static String getDefaultPictureSize(boolean isFrontCamera) {
        String key = isFrontCamera ?
                CustomFields.DEF_PICTURE_SIZE_FRONT : CustomFields.DEF_PICTURE_SIZE_REAR;
        String flag = isFrontCamera ?
                CustomUtil.getInstance().getString(CustomFields.PREF_PICTURE_SIZE_DEF_FRONT, "0") :
                CustomUtil.getInstance().getString(CustomFields.PREF_PICTURE_SIZE_DEF_REAR, "0");
        int value = Integer.valueOf(flag);

        String sizeQueue = CustomUtil.getInstance().getString(key, null);

        if (sizeQueue == null) {
            Log.i(TAG, "get " + key + " is null");
            return null;
        }

        String[] sizes = TextUtils.split(sizeQueue, Size.DELIMITER);
        if (sizes != null && sizes.length > value) {
            return sizes[value];
        }
        Log.i(TAG, "Cannot get default size for flag " + value  + "in queue " + sizeQueue);
        return null;
    }

    private static final float RATIO_1_TO_1 = 1.0f;
    private static final float RATIO_4_TO_3 = 4f / 3f;
    private static final float RATIO_16_TO_9 = 16f / 9f;
    private static float[] mRatios = {RATIO_1_TO_1,RATIO_4_TO_3,RATIO_16_TO_9};
    /**
     * Get the picture size according to different proportions
     * @param previewSizes
     * @return List<Size>
     */
    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-18,BUG-3140939*/
    public static List<Size> getPictureSizeAccordingToRatio(List<Size> previewSizes) {
        final float ASPECT_TOLERANCE = 0.1f;
        float EXPECT_ASPECT_RATIO;
        //Temporarily stop using perso mechanism
        //perso mechanism's picture size , camera devices unsupport
//        String key = isFrontCamera ?
//                CustomFields.DEF_PICTURE_SIZE_FRONT : CustomFields.DEF_PICTURE_SIZE_REAR;
//        String sizeQueue = CustomUtil.getInstance().getString(key, null);
//        if (sizeQueue != null) {
//            Log.i(TAG, "get " + key + " is null");
//        }
//        String[] sizes = TextUtils.split(sizeQueue, Size.DELIMITER);
/* MODIFIED-END by jianying.zhang,BUG-3140939*/
        List<Size> mSizes = new ArrayList<>();
        for (int i = 0; i< mRatios.length; i++) {
            Size largestSize = new Size(0,0);
            EXPECT_ASPECT_RATIO = mRatios[i];
            for (Size size : previewSizes) {
                float currentRatio = (float) size.width() / (float) size.height();
                if (currentRatio < 1) {
                    currentRatio = 1 / currentRatio;
                }
                if (Math.abs(currentRatio - EXPECT_ASPECT_RATIO) < ASPECT_TOLERANCE) {
                    int resolution = size.width() * size.height();
                    // If supported, the judgement about PIXEL_UPPER_BOUND can be removed.
                    if (resolution > (largestSize.width() * largestSize.height())) {
                        largestSize = size;
                    }
                }
            }
/* MODIFIED-BEGIN by jianying.zhang, 2016-10-18,BUG-3140939*/
//            if (sizes != null && sizes.length == 2) {
//                for (String sizeStr : sizes){
//                    String[] sizeStrs = sizeStr.split("x");
//                    Size size = new Size(Integer.parseInt(sizeStrs[0]),Integer.parseInt(sizeStrs[1]));
//                    float currentRatio = (float) size.width() / (float) size.height();
//                    if (currentRatio < 1) {
//                        currentRatio = 1 / currentRatio;
//                    }
//                    if (Math.abs(currentRatio - EXPECT_ASPECT_RATIO) < ASPECT_TOLERANCE) {
//                        largestSize = size;
//                    }
//                }
//            }
/* MODIFIED-END by jianying.zhang,BUG-3140939*/
            if (largestSize.width() != 0 && largestSize.height() != 0) {
                mSizes.add(largestSize);
                Log.d(TAG,"largestSize : " + largestSize.width() + " x " + largestSize.height());
            }
        }
        return mSizes;
    }

    public static double getCachedPictureSize(SettingsManager settingsManager,
                                              boolean isCameraFacingFront) {
        double targetRatio;
        String pictureSizeKey = isCameraFacingFront ? Keys.KEY_PICTURE_SIZE_FRONT
                : Keys.KEY_PICTURE_SIZE_BACK;
        // If the size from pictureSizeKey is null, use the default size from plf instead.
        String defaultPicSize = getDefaultPictureSize(isCameraFacingFront);
        String pictureSize = settingsManager.getString(SettingsManager.SCOPE_GLOBAL,
                pictureSizeKey, defaultPicSize);
        Size size = sizeFromString(pictureSize);
        targetRatio = (double) size.width() / size.height();
        return targetRatio;
    }


    /**
     * @param size The photo resolution.
     * @return A human readable and translated string for labeling the
     *         picture size in megapixels.
     */
    public static String getSizeSummaryString(Context context, Size size) {
        Size approximateSize = ResolutionUtil.getApproximateSize(size);
        long megaPixels = Math.round((size.width() * size.height()) / 1e6);
        int numerator = ResolutionUtil.aspectRatioNumerator(approximateSize);
        int denominator = ResolutionUtil.aspectRatioDenominator(approximateSize);
        String result = context.getResources().getString(
                R.string.aspect_ratio_default, numerator, denominator);
        return result;
    }

    public static String getSizeEntryString(Size size) {
        return size.width()+ "x" + size.height();
    }

    public static int getCountDownDuration(AppController appController,
                                           SettingsManager settingsManager) {
        if (!CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_VDF_COUNT_TIMER, false)) {
            final int REAR_CAMERA = 0;
            final int[] possibleTimer = appController.getAndroidContext().getResources().
                    getIntArray(R.array.pref_countdown_duration);
            // The first possible value of pref_countdown_duration is always 0, so for front camera
            // set the second value as default.
            /* MODIFIED-BEGIN by yuanxing.tan, 2016-04-27,BUG-2001149*/
            int duration = possibleTimer[0];
            if (appController.getCurrentCameraId() != REAR_CAMERA) {
                int defPt = possibleTimer[0];
                if (possibleTimer.length > 2) {
                    defPt = possibleTimer[1];
                }
                duration = CustomUtil.getInstance().getInt(CustomFields.DEF_CAMERA_COUNTDOWN_DURATION_FRONT, defPt);
            } else {
                duration = CustomUtil.getInstance().getInt(CustomFields.DEF_CAMERA_COUNTDOWN_DURATION_REAR, possibleTimer[0]);
            }
            boolean durationValid = false;
            for (int pt:possibleTimer) {
                if (pt == duration) {
                    durationValid = true;
                    break;
                }
            }
            if (!durationValid) {
                duration = possibleTimer[0];
            }
            settingsManager.setDefaults(Keys.KEY_COUNTDOWN_DURATION, duration, possibleTimer);
            /* MODIFIED-END by yuanxing.tan,BUG-2001149*/
        }
        return settingsManager.getIndexOfCurrentValue(appController.getCameraScope(),
                Keys.KEY_COUNTDOWN_DURATION);
    }

    public static boolean isCameraSoundForced() {
        // Read ro.tct.camera.sound.forced first. If it's not set,
        // getprop from value set in def_camera_sound_forced_config.
        final String DEFAULT_CONFIG = "ro.tct.camera.sound.forced";
        String value = ExtSystemProperties.get(DEFAULT_CONFIG);
        // If the key isn't found, the value will be null or an empty string otherwise.
        if (value != null && !value.equalsIgnoreCase("")) {
            return (value.equalsIgnoreCase("true") || value.equals("1"));
        } else {
            final String CONFIG_CAMERA_SOUND_FORCED = CustomUtil.getInstance().getString(
                    CustomFields.DEF_CAMERA_SOUND_FORCED_CONFIG, null);
            Log.i(TAG, "Sound forced config is " + CONFIG_CAMERA_SOUND_FORCED);
            if (CONFIG_CAMERA_SOUND_FORCED == null ||
                    CONFIG_CAMERA_SOUND_FORCED.equalsIgnoreCase("")) {
                return false;
            }
            return ExtSystemProperties.getBoolean(CONFIG_CAMERA_SOUND_FORCED);
        }
    }

    public static Size getPictureSizeAccordingToRatio(CameraCapabilities cameraCapabilities) {
        if (cameraCapabilities == null) {
            return new Size(0, 0);
        }
        final float ASPECT_TOLERANCE = 0.1f;
        final float EXPECT_ASPECT_RATIO = 4f / 3f;
        Size largestSize = new Size(0, 0);
        List<Size> photoSizes = cameraCapabilities.getSupportedPhotoSizes();
        for (Size size : photoSizes) {
            float currentRatio = (float) size.width() / (float) size.height();
            if (currentRatio < 1) {
                currentRatio = 1 / currentRatio;
            }
            if (Math.abs(currentRatio - EXPECT_ASPECT_RATIO) < ASPECT_TOLERANCE) {
                int resolution = size.width() * size.height();
                // If supported, the judgement about PIXEL_UPPER_BOUND can be removed.
                if (resolution > (largestSize.width() * largestSize.height())) {
                    largestSize = size;
                }
            }
        }
        return largestSize;
    }
}
