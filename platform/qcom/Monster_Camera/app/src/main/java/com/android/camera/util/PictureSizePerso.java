package com.android.camera.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.android.camera.settings.CameraPictureSizesCacher;
import com.android.camera.settings.CameraSettingsActivity;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.settings.SettingsUtil;
import com.android.ex.camera2.portability.CameraSettings;
import com.android.ex.camera2.portability.Size;
import com.android.ex.camera2.portability.debug.Log;
import com.android.external.CamcorderProfileEx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PictureSizePerso {
    private static final Log.Tag TAG = new Log.Tag("SizePerso");
    private static PictureSizePerso mPerso;
    private CustomUtil mCuzUtil;
    private SizeComparator mSizeComparator = new SizeComparator();

    private static class SizeComparator implements Comparator<Size> {

        @Override
        public int compare(Size size1, Size size2) {
            return (size1.width() == size2.width() ? size1.height() - size2.height() :
                    size1.width() - size2.width());
        }
    }

    private PictureSizePerso() {
        mCuzUtil = CustomUtil.getInstance();
    }

    public static PictureSizePerso getInstance(){
        if(mPerso==null) {
            mPerso = new PictureSizePerso();
        }
        return mPerso;
    }

    public void init(Context context, List<Size> supportedSizes,int cameraId,CameraSettings.BoostParameters parameters){
        if (supportedSizes == null || (cameraId != 0 && cameraId != 1)) {
            return;
        }
        String key_build = CameraPictureSizesCacher.PICTURE_SIZES_BUILD_KEY + cameraId;
        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String thisCameraCachedBuild = defaultPrefs.getString(key_build, null);
        if(parameters==null) {
            if (thisCameraCachedBuild != null && thisCameraCachedBuild.equals(Build.DISPLAY)) {
                return;
            }
        }else{
            if (thisCameraCachedBuild != null && thisCameraCachedBuild.equals(Build.DISPLAY)) {
                Size defaultSize=getCachedPictureSize(parameters.settingsManager,cameraId== Camera.CameraInfo.CAMERA_FACING_FRONT);
                if(supportedSizes.contains(defaultSize)){
                    Log.i(TAG,"preference hit support list");
                    return;
                }else{
                    Log.i(TAG,"unsupported sized detected in current preference , reset preference");
                    CameraUtil.cleanSharedPreference(context);
                }
            }
        }
        filterPersoUnSupportedPhotoSizes(context, supportedSizes, cameraId);
        filterPersoUnSupportedVideoSizes(context, cameraId);
    }

    private Size getCachedPictureSize(SettingsManager settingsManager,boolean isCameraFacingFront){
        String pictureSizeKey = isCameraFacingFront ? Keys.KEY_PICTURE_SIZE_FRONT
                : Keys.KEY_PICTURE_SIZE_BACK;
        // If the size from pictureSizeKey is null, use the default size from plf instead.
        String defaultPicSize = SettingsUtil.getDefaultPictureSize(isCameraFacingFront);
        String pictureSize = settingsManager.getString(SettingsManager.SCOPE_GLOBAL,
                pictureSizeKey, defaultPicSize);
        Size size = SettingsUtil.sizeFromString(pictureSize);
        return size;
    }

    public void init(Context context, List<Size> supportedSizes,int cameraId){
        init(context,supportedSizes,cameraId,null);
    }

    public void filterPersoUnSupportedVideoSizes(Context context, int cameraId){
        String keyQuality;
        String keyQualityTitle;
        String defaultFlag=null;
        if (cameraId == 0) {
            keyQualityTitle = CustomFields.DEF_VIDEO_QUALITIES_TITLE_REAR;
            keyQuality = CustomFields.DEF_VIDEO_QUALITIES_REAR;
            defaultFlag=mCuzUtil.getString(CustomFields.DEF_VIDEO_QUALITIES_FLAG_DEFAULT_REAR, "0");
        } else {
            keyQualityTitle = CustomFields.DEF_VIDEO_QUALITIES_TITLE_FRONT;
            keyQuality = CustomFields.DEF_VIDEO_QUALITIES_FRONT;
            defaultFlag=mCuzUtil.getString(CustomFields.DEF_VIDEO_QUALITIES_FLAG_DEFAULT_FRONT, "0");
        }
        String qualityTitles = mCuzUtil.getString(keyQualityTitle, "");
        String qualities =  mCuzUtil.getString(keyQuality, "");
        Log.d(TAG, "filterPersoUnSupportedVideoSizes " + qualityTitles + ",  " + qualities);
        String[] flatQualitiesTitles = TextUtils.split(qualityTitles, Size.DELIMITER);
        String[] flatQualities = TextUtils.split(qualities, Size.DELIMITER);
        List<String> filteredQualities = new ArrayList<String>();
        List<String> filteredQualitiesTitles = new ArrayList<String>();
        for (int i = 0; i < flatQualities.length; i++) {
            String qualityStr = flatQualities[i].trim();
            if (qualityStr != null && qualityStr.equals(SettingsUtil.QUALITY_1080P_60FPS_NAME)) {
                filteredQualities.add(SettingsUtil.QUALITY_1080P_60FPS);
                filteredQualitiesTitles.add(flatQualitiesTitles[i]);
                continue;
            }

            int quality = CamcorderProfileEx.getQualityNum(flatQualities[i].trim());
            if (SettingsUtil.isVideoQualitySupported(quality) && CamcorderProfile.hasProfile(cameraId, quality)) {
                filteredQualities.add(String.valueOf(quality));
                filteredQualitiesTitles.add(flatQualitiesTitles[i]);
            }
        }
        if (filteredQualities.size() == 0) {
            if(cameraId == 0) {
                filteredQualities.add("5");//QUALITY_720P
                filteredQualitiesTitles.add("HD 720p");
            } else {
                filteredQualities.add("10");//QUALITY_VGA
                filteredQualitiesTitles.add("VGA");
            }
        }
        int videoQualityFlagNum = Integer.valueOf(defaultFlag);
        if (videoQualityFlagNum >= filteredQualities.size()) {
            videoQualityFlagNum = 0;
        }
        String defVideoQuality = filteredQualities.get(videoQualityFlagNum);
        String videoQualityKey = cameraId == 0 ? Keys.KEY_VIDEO_QUALITY_BACK
                : Keys.KEY_VIDEO_QUALITY_FRONT;
        String key_quality = CameraPictureSizesCacher.VIDEO_QUALITIES_KEY + cameraId;
        String key_quality_titles = CameraPictureSizesCacher.VIDEO_QUALITIES_TITLES_KEY + cameraId;
        Log.d(TAG, "filterPersoUnSupportedVideoSizes filtered:" + TextUtils.join(Size.DELIMITER, filteredQualities) + ",  " + TextUtils.join(Size.DELIMITER, filteredQualitiesTitles));
        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        String oldQualities = defaultPrefs.getString(key_quality, null);
        String newQualities = TextUtils.join(Size.DELIMITER, filteredQualities);
        String oldQualityTitles = defaultPrefs.getString(key_quality_titles, null);
        String newQualityTitles = TextUtils.join(Size.DELIMITER, filteredQualitiesTitles);
        if (!TextUtils.equals(oldQualities, newQualities) || !TextUtils.equals(oldQualityTitles, newQualityTitles)) {
            SharedPreferences.Editor editor = defaultPrefs.edit();
            editor.putString(key_quality, newQualities);
            editor.putString(key_quality_titles, newQualityTitles);
            editor.putString(videoQualityKey, defVideoQuality);
            editor.commit();
        }
    }

    private void filterPersoUnSupportedPhotoSizes(Context context, List<Size> supportedSizes,int cameraId){
        String keySize;
        String defaultFlag=null;
        if (cameraId == 0) {
            keySize = CustomFields.DEF_PICTURE_SIZE_REAR;
            defaultFlag=mCuzUtil.getString(CustomFields.PREF_PICTURE_SIZE_DEF_REAR, "0");
        } else {
            keySize = CustomFields.DEF_PICTURE_SIZE_FRONT;
            defaultFlag=mCuzUtil.getString(CustomFields.PREF_PICTURE_SIZE_DEF_FRONT, "0");
        }
        Collections.sort(supportedSizes, mSizeComparator);
        String sizes =  mCuzUtil.getString(keySize, "");
        Log.i(TAG, "filterPersoUnSupportedPhotoSizes "  + ",  " + sizes + ", " + Size.listToString(supportedSizes));
        String[] flatSizes = TextUtils.split(sizes, Size.DELIMITER);
        List<Size> filteredSizes = new ArrayList<Size>();
        for (int i = 0; i < flatSizes.length; i++) {
            String size = flatSizes[i].trim();
            if (size.split("x").length == 2) {
                Size desiredSize = SettingsUtil.sizeFromString(size);
                if (supportedSizes.contains(desiredSize)) {
                    filteredSizes.add(desiredSize);
                }
            }
        }
        if (filteredSizes.size() == 0) {
            filteredSizes.add(supportedSizes.get(supportedSizes.size() - 1));
            if (supportedSizes.size()>1){
                filteredSizes.add(supportedSizes.get(supportedSizes.size() - 2));
            }
        }

        int pictureSizeFlagNum = Integer.valueOf(defaultFlag);
        if (pictureSizeFlagNum >= filteredSizes.size()) {
            pictureSizeFlagNum = 0;
        }
        Size s = filteredSizes.get(pictureSizeFlagNum);
        String pictureSize = SettingsUtil.sizeToSetting(s);
        String pictureSizeKey = cameraId == 0 ? Keys.KEY_PICTURE_SIZE_BACK
                : Keys.KEY_PICTURE_SIZE_FRONT;

        String key_build = CameraPictureSizesCacher.PICTURE_SIZES_BUILD_KEY + cameraId;
        String key_sizes = CameraPictureSizesCacher.PICTURE_SIZES_SIZES_KEY + cameraId;
        SharedPreferences defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Log.d(TAG, "filterPersoUnSupportedPhotoSizes filteredsize:" + Size.listToString(filteredSizes));
        SharedPreferences.Editor editor = defaultPrefs.edit();
        editor.putString(key_build, Build.DISPLAY);
        String oldSizes = defaultPrefs.getString(key_sizes, null);
        String newSizes = Size.listToString(filteredSizes);
        if (!TextUtils.equals(oldSizes, newSizes)) {
            editor.putString(key_sizes, newSizes);
            editor.putString(pictureSizeKey, pictureSize);
        }
        editor.commit();
    }
}