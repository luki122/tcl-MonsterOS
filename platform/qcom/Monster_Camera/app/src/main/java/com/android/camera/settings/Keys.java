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

import android.content.Context;

/* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
import com.android.camera.CameraActivity;
import com.android.camera.app.LocationManager;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.ex.camera2.portability.CameraAgent;
/* MODIFIED-END by jianying.zhang,BUG-3255060*/
import com.android.external.plantform.ExtBuild;
import com.tct.camera.R;

/**
 * Keys is a class for storing SharedPreferences keys and configuring
 * their defaults.
 *
 * For each key that has a default value and set of possible values, it
 * stores those defaults so they can be used by the SettingsManager
 * on lookup.  This step is optional, and it can be done anytime before
 * a setting is accessed by the SettingsManager API.
 */
public class Keys {

    public static final String KEY_RECORD_LOCATION = "pref_camera_recordlocation_key";
    public static final String KEY_VIDEO_QUALITY_BACK = "pref_video_quality_back_key";
    public static final String KEY_VIDEO_QUALITY_FRONT = "pref_video_quality_front_key";
    public static final String KEY_PICTURE_SIZE_BACK = "pref_camera_picturesize_back_key";
    public static final String KEY_PICTURE_SIZE_FRONT = "pref_camera_picturesize_front_key";
    public static final String KEY_JPEG_QUALITY = "pref_camera_jpegquality_key";
    public static final String KEY_FOCUS_MODE = "pref_camera_focusmode_key";
    public static final String KEY_FLASH_MODE = "pref_camera_flashmode_key";
    public static final String KEY_VIDEOCAMERA_FLASH_MODE = "pref_camera_video_flashmode_key";
    public static final String KEY_SCENE_MODE = "pref_camera_scenemode_key";
    public static final String KEY_EXPOSURE = "pref_camera_exposure_key";
    public static final String KEY_VIDEO_EFFECT = "pref_video_effect_key";
    public static final String KEY_CAMERA_ID = "pref_camera_id_key";

    public static final String KEY_CAMERA_HDR_AUTO = "pref_camera_hdr_auto_key";
    public static final String KEY_CAMERA_HDR = "pref_camera_hdr_key";
    public static final String KEY_CAMERA_HDR_PLUS = "pref_camera_hdr_plus_key";
    public static final String KEY_CAMERA_FIRST_USE_HINT_SHOWN =
            "pref_camera_first_use_hint_shown_key";
    public static final String KEY_VIDEO_FIRST_USE_HINT_SHOWN =
            "pref_video_first_use_hint_shown_key";
    public static final String KEY_STARTUP_MODULE_INDEX = "camera.startup_module";
    public static final String KEY_CAMERA_MODULE_LAST_USED =
            "pref_camera_module_last_used_index";
    public static final String KEY_SECURE_MODULE_INDEX = "pref_camera_secure_module_index";
    public static final String KEY_CAMERA_PANO_ORIENTATION = "pref_camera_pano_orientation";
    public static final String KEY_CAMERA_GRID_LINES = "pref_camera_grid_lines";

    /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-12,BUG-1892480*/
    public static final String KEY_CAMERA_POSE = "pref_camera_pose";
    public static final String KEY_CAMERA_COMPOSE = "pref_camera_compose";
    /*MODIFIED-END by shunyin.zhang,BUG-1892480*/

    public static final String KEY_RELEASE_DIALOG_LAST_SHOWN_VERSION =
            "pref_release_dialog_last_shown_version";
    public static final String KEY_FLASH_SUPPORTED_BACK_CAMERA =
            "pref_flash_supported_back_camera";
    public static final String KEY_UPGRADE_VERSION = "pref_upgrade_version";
    public static final String KEY_REQUEST_RETURN_HDR_PLUS = "pref_request_return_hdr_plus";
    public static final String KEY_SHOULD_SHOW_REFOCUS_VIEWER_CLING =
            "pref_should_show_refocus_viewer_cling";
    public static final String KEY_EXPOSURE_COMPENSATION_ENABLED =
            "pref_camera_exposure_compensation_key";
    public static final String KEY_USER_SELECTED_ASPECT_RATIO = "pref_user_selected_aspect_ratio";
    public static final String KEY_COUNTDOWN_DURATION = "pref_camera_countdown_duration_key";
    public static final String KEY_HDR_PLUS_FLASH_MODE = "pref_hdr_plus_flash_mode";
    public static final String KEY_SHOULD_SHOW_SETTINGS_BUTTON_CLING =
            "pref_should_show_settings_button_cling";

    public static final String KEY_VIDEO_EIS = "pref_video_eis_key";
    public static final String KEY_CAMERA_SAVEPATH = "pref_camera_savepath_key";
    public static final String KEY_SOUND = "pref_camera_capture_sound";
    public static final String KEY_RESTORE_SETTING = "pref_camera_restore_setting";

    public static final String KEY_MANUAL_ISO_STATE = "curIsoState";
    public static final String KEY_CUR_FOCUS_STATE = "curFocusState";
    public static final String KEY_CUR_EXPOSURE_TIME_STATE = "curExposureTimeState";
    public static final String KEY_CUR_WHITE_BALANCE_STATE = "curWhiteBalanceState";
    public static final String KEY_CAMERA_LOWLIGHT = "pref_camera_lowlight_key";
    public static final String KEY_CAMERA_FACEBEAUTY = "pref_camera_facebeauty_key";

    public static final String KEY_FACEBEAUTY_SKIN_SMOOTHING="pref_facebeauty_skinsmoothing_key";
    public static final String KEY_FACEBEAUTY_SKIN_WHITE="pref_facebeauty_skinwhitening_key"; // MODIFIED by bin.zhang2-nb, 2016-04-26,BUG-1996450

    public static final String SOURCE_MODULE_SCOPE = "source_module_scope";
    public static final String SOURCE_CAMERA_ID = "source_camera_id";
    public static final String KEY_TIZR_PROMPT = "pref_tizr_prompt_key";

    public static final String KEY_VIDEO_BOOM_KEY_TIP = "pref_video_boom_key_tip";

    public static final String KEY_CAMERA_ATTENTIONSEEKER = "pref_camera_attentionseeker_key";
    public static final String KEY_MICROVIDEO_GUIDE = "pref_microvideo_guide_key";
    public static final String KEY_NEW_LAUNCHING_TIMES_FOR_MICROTIP = "pref_new_launching_times_for_microtip_key";
    public static final String KEY_NEW_LAUNCHING_FOR_MICROTIP = "pref_new_launching_for_microtip_key";
    public static final String KEY_NEW_LAUNCHING_FOR_MICROGUIDE = "pref_new_launching_for_microguide_key";
    public static final String KEY_NEW_LAUNCHING_TIMES_FOR_HDRTOAST = "pref_new_launching_times_for_hdrtoast_key";
    public static final String KEY_NEW_LAUNCHING_FOR_HDRTOAST = "pref_new_launching_for_hdrtoast_key";
    public static final String KEY_NEW_LAUNCHING_TIMES_FOR_NIGHTTOAST = "pref_new_launching_times_for_nighttoast_key";
    public static final String KEY_NEW_LAUNCHING_FOR_NIGHTTOAST = "pref_new_launching_for_nighttoast_key";
    public static final String KEY_CAMERA_AIS = "pref_camera_ais_key";
    public static final String KEY_CAMERA_GESTURE_DETECTION = "pref_camera_gesture_detection_key";
    public static final String KEY_CAMERA_MIRROR_SELFIE = "pref_camera_mirror_key";

    public static final String KEY_HELP_TIP_WELCOME_FINISHED = "pref_help_tip_welcome_finished_key";
    public static final String KEY_HELP_TIP_WELCOME_STEP = "pref_help_tip_welcome_step_key";
    public static final String KEY_HELP_TIP_PANO_FINISHED = "pref_help_tip_pano_finished_key";
    public static final String KEY_HELP_TIP_MANUAL_FINISHED = "pref_help_tip_manaul_finished_key";
    public static final String KEY_HELP_TIP_PINCH_ZOOM_FINISHED = "pref_help_tip_pinch_zoom_finished_key";

    public static final String KEY_HELP_TIP_QUICK_SETTINGS_FINISHED = "pref_help_tip_quick_settings_finished_key";
    public static final String KEY_HELP_TIP_SETTINGS_FINISHED = "pref_help_tip_settings_finished_key";
    public static final String KEY_HELP_TIP_FRONT_CAMERA_FINISHED = "pref_help_tip_front_camera_finished_key";
    public static final String KEY_HELP_TIP_GESTURE_FINISHED = "pref_help_tip_gesture_finished_key";
    public static final String KEY_HELP_TIP_MODE_FINISHED = "pref_help_tip_mode_finished_key";
    public static final String KEY_HELP_TIP_STOP_VIDEO_FINISHED = "pref_help_tip_stop_video_finished_key";
    public static final String KEY_HELP_TIP_VIDEO_SNAP_FINISHED = "pref_help_tip_video_snap_finished_key";
    public static final String KEY_HELP_TIP_RECENT_FINISHED = "pref_help_tip_recent_finished_key";

    public static final String KEY_HELP_TIP_USER_APP_TIMES = "pref_help_tip_user_app_times_key";
    public static final String KEY_HELP_TIP_VIDEO_TIMES = "pref_help_tip_video_times_key";
    public static final String KEY_HELP_TIP_FRONT_CAMERA_OPENED_TIMES = "pref_help_tip_front_camera_opened_times_key";
    public static final String KEY_HELP_TIP_SYSTEM_TIME = "pref_help_tip_system_time_key";
    public static final String KEY_SECURE_CAMERA = "secure_camera"; // Key for CameraSettingsActivity and Fyuse
    public static final String KEY_ANTIBANDING = "pref_camera_antibanding_key";

    public static final String KEY_THUMB_URI = "thumb_uri"; // Key for Fyuse
    public static final String KEY_BLURRED_BITMAP_BYTE = "blurred_bitmap";
    public static final String KEY_PREVIEW_AREA = "preview_area";

    public static final String KEY_CAMERA_PHOTO_AUDIO_RECORD = "pref_camera_photo_audio_record";


    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
    public static final String KEY_FILTER_MODULE_SELECTED = "pref_filter_module_selected";
    public static final String KEY_VIDEO_FILTER_MODULE_SELECTED = "pref_video_filter_module_selected";
    /* MODIFIED-END by jianying.zhang,BUG-3255060*/

    /**
     * Set some number of defaults for the defined keys.
     * It's not necessary to set all defaults.
     */
    public static void setDefaults(SettingsManager settingsManager, Context context) {
        if(CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_VDF_COUNT_TIMER, false)){
            settingsManager.setDefaults(KEY_COUNTDOWN_DURATION, 0,
                    context.getResources().getIntArray(R.array.vdf_pref_countdown_duration));
        }else{
            settingsManager.setDefaults(KEY_COUNTDOWN_DURATION, 0,
                    context.getResources().getIntArray(R.array.pref_countdown_duration));
        }


        settingsManager.setDefaults(KEY_CAMERA_ID,
            context.getString(R.string.pref_camera_id_default),
            context.getResources().getStringArray(R.array.camera_id_entryvalues));

        settingsManager.setDefaults(KEY_SCENE_MODE,
            context.getString(R.string.pref_camera_scenemode_default),
            context.getResources().getStringArray(R.array.pref_camera_scenemode_entryvalues));

        settingsManager.setDefaults(KEY_FLASH_MODE,
            context.getString(R.string.pref_camera_flashmode_default),
            context.getResources().getStringArray(R.array.pref_camera_flashmode_entryvalues));

        /* MODIFIED-BEGIN by xuyang.liu, 2016-10-13,BUG-3110198*/
        if (isAutoHdrEnable()) {
            settingsManager.setDefaults(KEY_CAMERA_HDR,
                    context.getString(R.string.pref_camera_hdr_off),
                    context.getResources().getStringArray(R.array.pref_camera_hdr_entryvalues_with_auto));
        } else {
            settingsManager.setDefaults(KEY_CAMERA_HDR,
                    context.getString(R.string.setting_off_value),
                    context.getResources().getStringArray(R.array.pref_camera_hdr_entryvalues));
        }
        /* MODIFIED-END by xuyang.liu,BUG-3110198*/
        settingsManager.setDefaults(KEY_CAMERA_HDR_PLUS, false);

        settingsManager.setDefaults(KEY_CAMERA_FIRST_USE_HINT_SHOWN, true);

        settingsManager.setDefaults(KEY_FOCUS_MODE,
            context.getString(R.string.pref_camera_focusmode_default),
            context.getResources().getStringArray(R.array.pref_camera_focusmode_entryvalues));

        String videoQualityBackDefaultValue = context.getString(R.string.pref_video_quality_large);
        // TODO: We tweaked the default setting based on model string which is not ideal. Detecting
        // CamcorderProfile capability is a better way to get this job done. However,
        // |CamcorderProfile.hasProfile| needs camera id info. We need a way to provide camera id to
        // this method. b/17445274
        // Don't set the default resolution to be large if the device supports 4k video.
        if (ApiHelper.IS_NEXUS_6) {
            videoQualityBackDefaultValue = context.getString(R.string.pref_video_quality_medium);
        }
//        settingsManager.setDefaults(
//            KEY_VIDEO_QUALITY_BACK,
//            videoQualityBackDefaultValue,
//            context.getResources().getStringArray(R.array.pref_video_quality_entryvalues));
//        if (!settingsManager.isSet(SettingsManager.SCOPE_GLOBAL, Keys.KEY_VIDEO_QUALITY_BACK)) {
//            settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL,
//                                         Keys.KEY_VIDEO_QUALITY_BACK);
//        }

//        settingsManager.setDefaults(KEY_VIDEO_QUALITY_FRONT,
//            context.getString(R.string.pref_video_quality_large),
//            context.getResources().getStringArray(R.array.pref_video_quality_entryvalues));
//        if (!settingsManager.isSet(SettingsManager.SCOPE_GLOBAL, Keys.KEY_VIDEO_QUALITY_FRONT)) {
//            settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL,
//                                         Keys.KEY_VIDEO_QUALITY_FRONT);
//        }

        settingsManager.setDefaults(KEY_JPEG_QUALITY,
            context.getString(R.string.pref_camera_jpeg_quality_normal),
            context.getResources().getStringArray(
                R.array.pref_camera_jpeg_quality_entryvalues));

        settingsManager.setDefaults(KEY_VIDEOCAMERA_FLASH_MODE,
            context.getString(R.string.pref_camera_video_flashmode_default),
            context.getResources().getStringArray(
                R.array.pref_camera_video_flashmode_entryvalues));

        settingsManager.setDefaults(KEY_VIDEO_EFFECT,
            context.getString(R.string.pref_video_effect_default),
            context.getResources().getStringArray(R.array.pref_video_effect_entryvalues));

        settingsManager.setDefaults(KEY_VIDEO_FIRST_USE_HINT_SHOWN, true);

        settingsManager.setDefaults(KEY_STARTUP_MODULE_INDEX,
            context.getResources().getInteger(R.integer.camera_mode_photo),
            context.getResources().getIntArray(R.array.camera_modes));

        settingsManager.setDefaults(KEY_CAMERA_MODULE_LAST_USED,
            context.getResources().getInteger(R.integer.camera_mode_photo),
            context.getResources().getIntArray(R.array.camera_modes));

        settingsManager.setDefaults(KEY_CAMERA_PANO_ORIENTATION,
            context.getString(R.string.pano_orientation_horizontal),
            context.getResources().getStringArray(
                    R.array.pref_camera_pano_orientation_entryvalues));

        settingsManager.setDefaults(KEY_CAMERA_GRID_LINES, false);

        settingsManager.setDefaults(KEY_SHOULD_SHOW_REFOCUS_VIEWER_CLING, true);

        settingsManager.setDefaults(KEY_HDR_PLUS_FLASH_MODE,
            context.getString(R.string.pref_camera_hdr_plus_flashmode_default),
            context.getResources().getStringArray(
                R.array.pref_camera_hdr_plus_flashmode_entryvalues));

        settingsManager.setDefaults(KEY_SHOULD_SHOW_SETTINGS_BUTTON_CLING, true);

        settingsManager.setDefaults(KEY_CAMERA_LOWLIGHT, false);
        settingsManager.setDefaults(KEY_CAMERA_FACEBEAUTY, true);
        settingsManager.setDefaults(KEY_RECORD_LOCATION, false);

        settingsManager.setDefaults(KEY_MICROVIDEO_GUIDE, true);
        settingsManager.setDefaults(KEY_NEW_LAUNCHING_TIMES_FOR_MICROTIP, 0,
                context.getResources().getIntArray(R.array.camera_launching_times_for_micro_tip));
        settingsManager.setDefaults(KEY_NEW_LAUNCHING_TIMES_FOR_HDRTOAST, 0,
                context.getResources().getIntArray(R.array.camera_launching_times_for_hdr_toast));
        settingsManager.setDefaults(KEY_NEW_LAUNCHING_TIMES_FOR_NIGHTTOAST, 0,
                context.getResources().getIntArray(R.array.camera_launching_times_for_night_toast));
        settingsManager.setDefaults(KEY_NEW_LAUNCHING_FOR_MICROTIP, false);
        settingsManager.setDefaults(KEY_NEW_LAUNCHING_FOR_MICROGUIDE, false);
        settingsManager.setDefaults(KEY_NEW_LAUNCHING_FOR_HDRTOAST, false);
        settingsManager.setDefaults(KEY_NEW_LAUNCHING_FOR_NIGHTTOAST, false);

        CustomUtil util = CustomUtil.getInstance();
        /*MODIFIED-BEGIN by yuanxing.tan, 2016-04-09,BUG-1928402*/
        boolean defShutterSoundOn = util.getBoolean(CustomFields.DEF_CAMERA_SHUTTER_SOUND_ON, true);
        settingsManager.setDefaults(KEY_SOUND, defShutterSoundOn);
        /*MODIFIED-END by yuanxing.tan,BUG-1928402*/
        boolean defGestureDetectionEnabled = util.getBoolean(CustomFields.DEF_CAMERA_SUPPORT_GESTURE_SHOT, false)
                && util.getBoolean(CustomFields.DEF_CAMERA_GESTURE_SHOT_ON, false);
        settingsManager.setDefaults(KEY_CAMERA_GESTURE_DETECTION, defGestureDetectionEnabled);

        boolean defAis = ExtBuild.device() == ExtBuild.MTK_MT6755 && util.getBoolean(CustomFields.DEF_CAMERA_AIS_ON, false);
        settingsManager.setDefaults(KEY_CAMERA_AIS, defAis);

        boolean defAttentionSeekerOn = util.getBoolean(CustomFields.DEF_CAMERA_SUPPORT_ATTENTION_SEEKER, false)
                && util.getBoolean(CustomFields.DEF_CAMERA_ATTENTION_SEEKER_ON, false);
        settingsManager.setDefaults(KEY_CAMERA_ATTENTIONSEEKER, defAttentionSeekerOn);

        boolean defEisOn = util.getBoolean(CustomFields.DEF_CAMERA_SUPPORT_TCT_EIS, false)
                && util.getBoolean(CustomFields.DEF_CAMERA_TCT_EIS_ON, false);
        settingsManager.setDefaults(KEY_VIDEO_EIS, defEisOn);

        boolean defMirrorSelfieOn = util.getBoolean(CustomFields.DEF_CAMERA_SUPPORT_MIRROR_SELFIE, false)
                && util.getBoolean(CustomFields.DEF_CAMERA_MIRROR_SELFIE_ON, false);
        settingsManager.setDefaults(KEY_CAMERA_MIRROR_SELFIE, defMirrorSelfieOn);

        /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-12,BUG-1892480*/
        boolean defPoseOn = util.getBoolean(CustomFields.DEF_CAMERA_SUPPORT_POSE, false)
                && util.getBoolean(CustomFields.DEF_CAMERA_POSE_ON, false);
        settingsManager.setDefaults(KEY_CAMERA_POSE, defPoseOn);
        boolean defComposeOn = util.getBoolean(CustomFields.DEF_CAMERA_SUPPORT_COMPOSE, false)
                && util.getBoolean(CustomFields.DEF_CAMERA_COMPOSE_ON, false);
        settingsManager.setDefaults(KEY_CAMERA_COMPOSE, defComposeOn);
        /*MODIFIED-END by shunyin.zhang,BUG-1892480*/

        String defAntiBand = util.getString(CustomFields.DEF_ANTIBAND_DEFAULT, context.getResources().getString(R.string.pref_camera_antibanding_default));
        settingsManager.setDefaults(KEY_ANTIBANDING, defAntiBand, context.getResources().getStringArray(R.array.pref_camera_antibanding_entryvalues));

        settingsManager.setDefaults(KEY_CAMERA_PHOTO_AUDIO_RECORD,
                util.getBoolean(CustomFields.DEF_CAMERA_SUPPORT_PHOTO_AUDIO_RECORD, true));

        setToDefaults(settingsManager, context);
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
    public static void setFilterValueDefaults(SettingsManager settingsManager,
                                              Context context) {
        String backCameraId = context.getResources().getString(R.string.pref_camera_id_index_back);
        String frontCameraId = context.getResources().getString(R.string.pref_camera_id_index_front);
        String frontScope = CameraActivity.CAMERA_SCOPE_PREFIX + frontCameraId;
        String backScope = CameraActivity.CAMERA_SCOPE_PREFIX + backCameraId;
        settingsManager.setChosenFilterIndex(frontScope, KEY_FILTER_MODULE_SELECTED,
                CameraAgent.INDEX_NONE_FILTER);
        settingsManager.setChosenFilterIndex(backScope, KEY_FILTER_MODULE_SELECTED,
                CameraAgent.INDEX_NONE_FILTER);
        settingsManager.setChosenFilterIndex(frontScope, KEY_VIDEO_FILTER_MODULE_SELECTED,
                CameraAgent.INDEX_NONE_FILTER);
        settingsManager.setChosenFilterIndex(backScope, KEY_VIDEO_FILTER_MODULE_SELECTED,
                CameraAgent.INDEX_NONE_FILTER);
    }
    /* MODIFIED-END by jianying.zhang,BUG-3255060*/

    /**
     * Set initial values for the defined keys according to customize.
     * It's not necessary to set all defaults.
     */
    public static void setToDefaults(SettingsManager settingsManager, Context context) {
        if (!settingsManager.isSet(SettingsManager.SCOPE_GLOBAL, KEY_CAMERA_GESTURE_DETECTION)) {
            settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL,
                    KEY_CAMERA_GESTURE_DETECTION);
        }
        if (!settingsManager.isSet(SettingsManager.SCOPE_GLOBAL, KEY_CAMERA_AIS)) {
            settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL,
                    KEY_CAMERA_AIS);
        }
        if (!settingsManager.isSet(SettingsManager.SCOPE_GLOBAL, KEY_CAMERA_ATTENTIONSEEKER)) {
            settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL,
                    KEY_CAMERA_ATTENTIONSEEKER);
        }
        if (!settingsManager.isSet(SettingsManager.SCOPE_GLOBAL, KEY_VIDEO_EIS)) {
            settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL,
                    KEY_VIDEO_EIS);
        }
        if (!settingsManager.isSet(SettingsManager.SCOPE_GLOBAL, KEY_CAMERA_MIRROR_SELFIE)) {
            settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL,
                    KEY_CAMERA_MIRROR_SELFIE);
        }

        /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-12,BUG-1892480*/
        if (!settingsManager.isSet(SettingsManager.SCOPE_GLOBAL, KEY_CAMERA_POSE)) {
            settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL,
                    KEY_CAMERA_POSE);
        }
        if (!settingsManager.isSet(SettingsManager.SCOPE_GLOBAL, KEY_CAMERA_COMPOSE)) {
            settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL,
                    KEY_CAMERA_COMPOSE);
        }
        /*MODIFIED-END by shunyin.zhang,BUG-1892480*/

        if (!settingsManager.isSet(SettingsManager.SCOPE_GLOBAL, KEY_ANTIBANDING)) {
            settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL,
                    KEY_ANTIBANDING);
        }
        /*MODIFIED-BEGIN by yuanxing.tan, 2016-04-09,BUG-1928402*/
        if (!settingsManager.isSet(SettingsManager.SCOPE_GLOBAL, KEY_SOUND)) {
            settingsManager.setToDefault(SettingsManager.SCOPE_GLOBAL,
                    KEY_SOUND);
        }
        /*MODIFIED-END by yuanxing.tan,BUG-1928402*/
    }
    /** Helper functions for some defined keys. */

    /**
     * Returns whether the camera has been set to back facing in settings.
     */
    public static boolean isCameraBackFacing(SettingsManager settingsManager,
                                             String moduleScope) {
        return settingsManager.isDefault(moduleScope, KEY_CAMERA_ID);
    }

    /**
     * Returns whether hdr plus mode is set on.
     */
    public static boolean isHdrPlusOn(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                KEY_CAMERA_HDR_PLUS);
    }

    /**
     * Returns whether hdr mode is set on.
     */
    public static boolean isAutoHdrEnable(){
        return CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_ENABLE_TS_HDR_AUTO, false);
    }

    public static boolean isHdrOn(SettingsManager settingsManager, Context context) {
        return !isAutoHdrEnable()
                && settingsManager.getString(SettingsManager.SCOPE_GLOBAL, KEY_CAMERA_HDR)
                .equals(context.getString(R.string.setting_on_value));
    }

    public static boolean isHdrOff(SettingsManager settingsManager, Context context) {
        if(isAutoHdrEnable()) {
            return settingsManager.getString(SettingsManager.SCOPE_GLOBAL, KEY_CAMERA_HDR)
                    .equals(context.getString(R.string.pref_camera_hdr_off));
        } else {
            return settingsManager.getString(SettingsManager.SCOPE_GLOBAL, KEY_CAMERA_HDR)
                    .equals(context.getString(R.string.setting_off_value));
        }
    }

    public static boolean isHdrAuto(SettingsManager settingsManager, Context context) {
        if(isAutoHdrEnable()) {
            return settingsManager.getString(SettingsManager.SCOPE_GLOBAL, KEY_CAMERA_HDR)
                    .equals(context.getString(R.string.pref_camera_hdr_auto));
        } else {
            return false;
        }
    }

    public static void setHdrState(SettingsManager settingsManager, Context context,boolean state){
        if(isAutoHdrEnable()) {
            settingsManager.set(SettingsManager.SCOPE_GLOBAL, KEY_CAMERA_HDR,
                    state? context.getString(R.string.pref_camera_hdr_auto)
                    :context.getString(R.string.pref_camera_hdr_off));
        } else {
            settingsManager.set(SettingsManager.SCOPE_GLOBAL, KEY_CAMERA_HDR, state?
                    context.getString(R.string.setting_on_value)
                    :context.getString(R.string.setting_off_value));
        }

    }

    public static void switchHdr(SettingsManager settingsManager, boolean on) {
        settingsManager.set(SettingsManager.SCOPE_GLOBAL, KEY_CAMERA_HDR_AUTO,on);
    }

    /**
     * Returns whether the app should return to hdr plus mode if possible.
     */
    public static boolean requestsReturnToHdrPlus(SettingsManager settingsManager,
                                                  String moduleScope) {
        return settingsManager.getBoolean(moduleScope, KEY_REQUEST_RETURN_HDR_PLUS);
    }

    /**
     * Returns whether grid lines are set on.
     */
    public static boolean areGridLinesOn(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                KEY_CAMERA_GRID_LINES);
    }

    /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-12,BUG-1892480*/
    public static boolean isPoseOn(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                KEY_CAMERA_POSE);
    }

    public static boolean isComposeOn(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                KEY_CAMERA_COMPOSE);
    }
    /*MODIFIED-END by shunyin.zhang,BUG-1892480*/


    /**
     * Returns whether pano orientation is horizontal.
     */
    public static boolean isPanoOrientationHorizontal(SettingsManager settingsManager) {
        return settingsManager.isDefault(SettingsManager.SCOPE_GLOBAL,
                KEY_CAMERA_PANO_ORIENTATION);
    }

    /**
     * Sets the settings for whether location recording should be enabled or
     * not. Also makes sure to pass on the change to the location manager.
     */
    public static void setLocation(SettingsManager settingsManager, boolean on,
                                   LocationManager locationManager) {
        settingsManager.set(SettingsManager.SCOPE_GLOBAL, KEY_RECORD_LOCATION, on);
        locationManager.recordLocation(on);
    }

    /**
     * Sets the user selected aspect ratio setting to selected.
     */
    public static void setAspectRatioSelected(SettingsManager settingsManager) {
        settingsManager.set(SettingsManager.SCOPE_GLOBAL,
                KEY_USER_SELECTED_ASPECT_RATIO, true);
    }

    /**
     * Sets the manual exposure compensation enabled setting
     * to on/off based on the given argument.
     */
    public static void setManualExposureCompensation(SettingsManager settingsManager,
                                                     boolean on) {
        settingsManager.set(SettingsManager.SCOPE_GLOBAL,
                KEY_EXPOSURE_COMPENSATION_ENABLED, on);
    }

    /**
     * Reads the current location recording settings and passes it on to the
     * given location manager.
     */
    public static void syncLocationManager(SettingsManager settingsManager,
                                    LocationManager locationManager) {
        boolean value = settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                KEY_RECORD_LOCATION);
        locationManager.recordLocation(value);
    }

    // Stop location updates when pause.
    public static void pauseLocationManager(LocationManager locationManager) {
        locationManager.recordLocation(false);
    }

    public static boolean isShutterSoundOn(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                KEY_SOUND);
    }

    public static boolean isVideoStabilizationEnabled(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                KEY_VIDEO_EIS);
    }

    public static boolean isLowlightOn(SettingsManager settingsManager, String moduleScope) {
        return settingsManager.getBoolean(moduleScope, KEY_CAMERA_LOWLIGHT);
    }

    public static boolean isGestureDetectionOn(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                KEY_CAMERA_GESTURE_DETECTION);
    }

    public static boolean isFacebeautyOn(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                KEY_CAMERA_FACEBEAUTY);
    }

    public static boolean isAttentionseekerOn(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                KEY_CAMERA_ATTENTIONSEEKER);
    }

    public static boolean isCameraAisOn(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                KEY_CAMERA_AIS);
    }
    /**
     * Sets the settings for whether microvideo guide show or not
     */
    public static void setMicroGuide(SettingsManager settingsManager, boolean show) {
        settingsManager.set(SettingsManager.SCOPE_GLOBAL, KEY_MICROVIDEO_GUIDE, show);
    }

    public static boolean isShowMicroGuide(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL, KEY_MICROVIDEO_GUIDE);
    }

    public static void setNewLaunchingForMicrotip(SettingsManager settingsManager, boolean isNew) {
        if (isNewLaunchingForMicrotip(settingsManager) != isNew) {
            settingsManager.set(SettingsManager.SCOPE_GLOBAL, KEY_NEW_LAUNCHING_FOR_MICROTIP, isNew);
        }
    }

    public static boolean isNewLaunchingForMicrotip(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL, KEY_NEW_LAUNCHING_FOR_MICROTIP);
    }

    public static void setNewLaunchingForMicroguide(SettingsManager settingsManager, boolean isNew) {
        if (isNewLaunchingForMicroguide(settingsManager) != isNew) {
            settingsManager.set(SettingsManager.SCOPE_GLOBAL, KEY_NEW_LAUNCHING_FOR_MICROGUIDE, isNew);
        }
    }

    public static boolean isNewLaunchingForMicroguide(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL, KEY_NEW_LAUNCHING_FOR_MICROGUIDE);
    }

    public static void setNewLaunchingForHdrtoast(SettingsManager settingsManager, boolean isNew) {
        if (isNewLaunchingForHdrtoast(settingsManager) != isNew) {
            settingsManager.set(SettingsManager.SCOPE_GLOBAL, KEY_NEW_LAUNCHING_FOR_HDRTOAST, isNew);
        }
    }

    public static boolean isNewLaunchingForHdrtoast(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL, KEY_NEW_LAUNCHING_FOR_HDRTOAST);
    }

    public static void setNewLaunchingForNighttoast(SettingsManager settingsManager, boolean isNew) {
        if (isNewLaunchingForNighttoast(settingsManager) != isNew) {
            settingsManager.set(SettingsManager.SCOPE_GLOBAL, KEY_NEW_LAUNCHING_FOR_NIGHTTOAST, isNew);
        }
    }

    public static boolean isNewLaunchingForNighttoast(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL, KEY_NEW_LAUNCHING_FOR_NIGHTTOAST);
    }

    public static boolean isMirrorSelfieOn(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                KEY_CAMERA_MIRROR_SELFIE);
    }

    public static boolean isPhotoAudioRecordOn(SettingsManager settingsManager) {
        return settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL,
                KEY_CAMERA_PHOTO_AUDIO_RECORD);
    }

    public static void resetSecureModuleIndex(SettingsManager settingsManager, int modeIndex) {
        settingsManager.set(SettingsManager.SCOPE_GLOBAL, KEY_SECURE_MODULE_INDEX, modeIndex);
    }
}

