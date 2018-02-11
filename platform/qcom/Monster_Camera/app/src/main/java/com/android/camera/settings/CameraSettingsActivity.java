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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo; // MODIFIED by jianying.zhang, 2016-05-05,BUG-1997179
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toolbar;

import com.android.camera.ManualModule;
import com.android.camera.NormalPhotoModule;
import com.android.camera.Storage;
import com.android.camera.app.CameraApp;
import com.android.camera.debug.Log;
import com.android.camera.permission.PermissionUtil;
import com.android.camera.settings.SettingsUtil.SelectedPictureSizes;
import com.android.camera.settings.SettingsUtil.SelectedVideoQualities;
import com.android.camera.util.ApiHelper;
import com.android.camera.util.CameraSettingsActivityHelper;
import com.android.camera.util.CameraUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.ex.camera2.portability.CameraAgentFactory;
import com.android.ex.camera2.portability.CameraDeviceInfo;
import com.android.ex.camera2.portability.Size;
import com.android.external.plantform.ExtBuild;
import com.tct.camera.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides the settings UI for the Camera app.
 */
public class CameraSettingsActivity extends Activity {
	/**
        * Used to denote a subsection of the preference tree to display in the
        * Fragment. For instance, if 'Advanced' key is provided, the advanced
        * preference section will be treated as the root for display. This is used
        * to enable activity transitions between preference sections, and allows
        * back/up stack to operate correctly.
        */
    public static final String PREF_SCREEN_EXTRA = "pref_screen_extra";
    private static final Log.Tag TAG = new Log.Tag("CameraSettingsActivity");
    private CameraSettingsFragment dialog;
    private static boolean mSecureCamera = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ApiHelper.isLOrHigher()) {
            setContentView(R.layout.camera_settings_layout);
            Toolbar mBar = (Toolbar) findViewById(R.id.toolbar);
            mBar.setBackgroundColor(this.getResources().getColor(R.color.camera_settings_toolbar_color));
            setActionBar(mBar);
        }

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.mode_settings);

        if (getIntent().getBooleanExtra(Keys.KEY_SECURE_CAMERA, false)) {
            mSecureCamera = true;
        }
        if (mSecureCamera) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
             /*MODIFIED-BEGIN by bin.zhang2-nb, 2016-03-30,BUG-1872318*/
            if (mShutdownReceiverIsRegistered == false) {
                mShutdownReceiverIsRegistered = true;
                // Copy from CameraActivity onCreateTasks
                IntentFilter filter_screen_off = new IntentFilter(Intent.ACTION_SCREEN_OFF);
                registerReceiver(mShutdownReceiver, filter_screen_off);

                IntentFilter filter_user_unlock = new IntentFilter(Intent.ACTION_USER_PRESENT);
                registerReceiver(mShutdownReceiver, filter_user_unlock);
            }
             /*MODIFIED-END by bin.zhang2-nb,BUG-1872318*/
        }

        String moduleScope = getIntent().getStringExtra(Keys.SOURCE_MODULE_SCOPE);
        int cameraId = getIntent().getIntExtra(Keys.SOURCE_CAMERA_ID, 0);
        if (cameraId == -1) {
            CameraApp cameraApp = (CameraApp) getApplication();
            SettingsManager settingsManager = cameraApp.getSettingsManager();
            cameraId = settingsManager.getInteger(SettingsManager.SCOPE_GLOBAL, Keys.KEY_CAMERA_ID);
        }
        // CameraSettingsFragment dialog = new CameraSettingsFragment();
        dialog = new CameraSettingsFragment();
        Bundle bundle = new Bundle(2);
        bundle.putString(Keys.SOURCE_MODULE_SCOPE, moduleScope);
        bundle.putInt(Keys.SOURCE_CAMERA_ID, cameraId);
        dialog.setArguments(bundle);
        if (ApiHelper.isLOrHigher()) {
            getFragmentManager().beginTransaction().replace(R.id.camera_settings, dialog).commit();
        } else {
            getFragmentManager().beginTransaction().replace(android.R.id.content, dialog).commit();
        }
        IntentFilter filter_media_action = new IntentFilter(Intent.ACTION_MEDIA_EJECT);
        filter_media_action.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter_media_action.addDataScheme("file");
        registerReceiver(mMediaActionReceiver, filter_media_action);

        /* MODIFIED-BEGIN by nie.lei, 2016-05-05,BUG-2073514*/
        IntentFilter filter_shutdonw_action = new IntentFilter(Intent.ACTION_SHUTDOWN);
        registerReceiver(mRestartActionReceiver, filter_shutdonw_action);
        /* MODIFIED-END by nie.lei,BUG-2073514*/
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-05-05,BUG-1997179*/
    @Override
    protected void onStart() {
        super.onStart();
        if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
            int appOrientaion = getRequestedOrientation();
            if (appOrientaion != ActivityInfo.SCREEN_ORIENTATION_USER) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            }
            Log.i(TAG, "onStart appOrientaion = " + getRequestedOrientation());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
            int appOrientaion = getRequestedOrientation();
            if (appOrientaion != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
            Log.i(TAG, "onPause appOrientaion = " + getRequestedOrientation());
        }
    }
    /* MODIFIED-END by jianying.zhang,BUG-1997179*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSecureCamera) {
             /*MODIFIED-BEGIN by bin.zhang2-nb, 2016-03-30,BUG-1872318*/
            if (mShutdownReceiverIsRegistered) {
                mShutdownReceiverIsRegistered = false;
                try {
                    // catch IllegalArgumentException.
                    unregisterReceiver(mShutdownReceiver);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Unregister mShutdownReceiver exception");
                }
            }
        }
        unregisterReceiver(mMediaActionReceiver);
        unregisterReceiver(mRestartActionReceiver); // MODIFIED by nie.lei, 2016-05-05,BUG-2073514
    }

    private boolean mShutdownReceiverIsRegistered = false;
     /*MODIFIED-END by bin.zhang2-nb,BUG-1872318*/
    private final BroadcastReceiver mShutdownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    /* MODIFIED-BEGIN by nie.lei, 2016-05-05,BUG-2073514*/
    private boolean mShutDown = false;
    private final BroadcastReceiver mRestartActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e(TAG, "intent Action, " + action);
            if (action.equals(Intent.ACTION_SHUTDOWN)){
                mShutDown = true;
            }
        }
    };

    private final BroadcastReceiver mMediaActionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mShutDown){
                Log.e(TAG, "shutdown and stop to receive broadcast");
                return;
            }
            /* MODIFIED-END by nie.lei,BUG-2073514*/
            String action = intent.getAction();
            if (dialog != null && action.equals(Intent.ACTION_MEDIA_EJECT)) {
                dialog.refreshStorageSettings(false);
            } else if (dialog != null && action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                dialog.refreshStorageSettings(true);
            }
        }
    };
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        dialog.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        dialog.onActivityResult(requestCode, resultCode, data);
    }

    public static class CameraSettingsFragment extends PreferenceFragment implements
            OnSharedPreferenceChangeListener, DialogInterface.OnClickListener{

        public static final String PREF_CATEGORY_RESOLUTION = "pref_category_resolution";
        public static final String PREF_CATEGORY_ADVANCED = "pref_category_advanced";
        public static final String PREF_LAUNCH_HELP = "pref_launch_help";
        public static final String PREF_CATEGORY_PHOTO = "pref_group_photo_key";
        public static final String PREF_CATEGORY_VIDEO = "pref_group_video_key";
        public static final String PREF_CATEGORY_GENERAL = "pref_group_general";
        private static final Log.Tag TAG = new Log.Tag("SettingsFragment");
        private static DecimalFormat sMegaPixelFormat = new DecimalFormat("##0.0");
        private String[] mCamcorderProfileNames;
        private CameraDeviceInfo mInfos;
        private String mPrefKey;
        private String moduleScope;
        private int cameraId;
        private boolean mGetSubPrefAsRoot = true;

        // Selected resolutions for the different cameras and sizes.
        private SelectedPictureSizes mOldPictureSizesBack;
        private SelectedPictureSizes mOldPictureSizesFront;
        private List<Size> mPictureSizesBack;
        private List<Size> mPictureSizesFront;
//        private SelectedVideoQualities mVideoQualitiesBack;
//        private SelectedVideoQualities mVideoQualitiesFront;
        private String[] mVideoQualitiesBack;
        private String[] mVideoQualitiesFront;
        private String[] mVideoQualityTitlesBack;
        private String[] mVideoQualityTitlesFront;
        private boolean mShowCameraPicturesizeFront = true;
        private boolean mShowCameraPicturesizeBack = true;
        private boolean mShowCameraGridLines = true;
        private boolean mShowVideoQualityBack = true;
        private boolean mShowVideoQualityFront = true;
        private boolean mShowVideoEis = true;
        private boolean mShowCameraSavepath = true;
        private boolean mShowFacebeauty = true;
        private boolean mShowAttentionseeker = true;
        private boolean mShowGestureShot = true;
        private boolean mShowAis = false; // MODIFIED by yuanxing.tan, 2016-05-11,BUG-2127821
        private boolean mShowMirrorSelfie = true;

        /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-12,BUG-1892480*/
        private boolean mShowPose = true;
        private boolean mShowCompose = true;
        /*MODIFIED-END by shunyin.zhang,BUG-1892480*/

        private boolean mShowAntiBand = true;
//        private boolean mShowCameraRecordLocation = true;
//        private boolean mShowCameraCaptureSound = true;
        private boolean isFacingBack;
        private ArrayList<String> mVisidonEntries;
        private ArrayList<String> mVisidonEntriesValue;
        private DialogInterface mWarnResetToFactory;
        /* MODIFIED-BEGIN by shunyin.zhang, 2016-05-06,BUG-1892480*/
        private ManagedSwitchPreference mPosePreference;
        private ManagedSwitchPreference mComposePreference;
        private ManagedSwitchPreference mAttentionseekerPreference;

        private Preference.OnPreferenceClickListener mClickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                switch (preference.getKey()){
                    case Keys.KEY_CAMERA_ATTENTIONSEEKER:
                        if (mAttentionseekerPreference.isChecked()){
                            if (mPosePreference.isChecked()){
                                mPosePreference.setChecked(false);
                            }
                            if(mComposePreference.isChecked()){
                                mComposePreference.setChecked(false);
                            }
                        }
                        break;
                    case Keys.KEY_CAMERA_POSE:
                        if (mPosePreference.isChecked()){
                            if (mAttentionseekerPreference.isChecked()){
                                mAttentionseekerPreference.setChecked(false);
                            }
                        }
                        break;
                    case Keys.KEY_CAMERA_COMPOSE:
                        if (mComposePreference.isChecked()){
                            if (mAttentionseekerPreference.isChecked()){
                                mAttentionseekerPreference.setChecked(false);
                            }
                        }
                        break;
                    default:
                        break;


                }
                return false;
            }
        };
        /* MODIFIED-END by shunyin.zhang,BUG-1892480*/
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle arguments = getArguments();
            if (arguments != null) {
                moduleScope = arguments.getString(Keys.SOURCE_MODULE_SCOPE);
                cameraId = arguments.getInt(Keys.SOURCE_CAMERA_ID);
            }
            Context context = this.getActivity().getApplicationContext();
            addPreferencesFromResource(R.xml.camera_preferences);

            // Allow the Helper to edit the full preference hierarchy, not the sub
            // tree we may show as root. See {@link #getPreferenceScreen()}.
            mGetSubPrefAsRoot = false;
            CameraSettingsActivityHelper.addAdditionalPreferences(this, context);
            mGetSubPrefAsRoot = true;

            mCamcorderProfileNames = getResources().getStringArray(R.array.camcorder_profile_names);
            mInfos = CameraAgentFactory
                    .getAndroidCameraAgent(context, CameraAgentFactory.CameraApi.API_1)
                    .getCameraDeviceInfo();

            /* MODIFIED-BEGIN by shunyin.zhang, 2016-05-06,BUG-1892480*/
            mPosePreference = (ManagedSwitchPreference)getPreferenceScreen().findPreference(Keys.KEY_CAMERA_POSE);
            mComposePreference = (ManagedSwitchPreference)getPreferenceScreen().findPreference(Keys.KEY_CAMERA_COMPOSE);
            mAttentionseekerPreference = (ManagedSwitchPreference)getPreferenceScreen().findPreference(Keys.KEY_CAMERA_ATTENTIONSEEKER);
            mPosePreference.setOnPreferenceClickListener(mClickListener);
            mComposePreference.setOnPreferenceClickListener(mClickListener);
            mAttentionseekerPreference.setOnPreferenceClickListener(mClickListener);
            /* MODIFIED-END by shunyin.zhang,BUG-1892480*/
        }



        @Override
        public void onResume() {
            super.onResume();
            final Activity activity = this.getActivity();

            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
            setCameraPreference();

            if (!CameraUtil.checkGpsEnable(activity)) {
                setRememberLocation(false);
            }
        }

        private void setCameraPreference() {
            // Load the camera sizes.
            loadSizes();

            // Make sure to hide settings for cameras that don't exist on this
            // device.
            setVisibilities();

            fillEntriesAndSummaries();
            // If sdcard is not available, disable it in settings.
            PreferenceScreen root = getPreferenceScreen();
            ListPreference savePath = (ListPreference) root.findPreference(Keys.KEY_CAMERA_SAVEPATH);
            if (Storage.isSDCardAvailable()) {
                savePath.setEnabled(true);
            } else {
                savePath.setEnabled(false);
            }
            if (savePath.getValue().equals(Storage.SDCARD_STORAGE)) {
                if (Storage.isSDCardAvailable()) {
                    Storage.setSavePath(Storage.SDCARD_STORAGE);
                } else {
                    savePath.setValue(Storage.PHONE_STORAGE);
                    savePath.setSummary(savePath.getEntry());
                    Storage.setSavePath(Storage.PHONE_STORAGE);
                }
            }
            if (!PermissionUtil.isNoncriticalPermissionGranted(getActivity())) {
                setRememberLocation(false);
            }
        }

        /**
         * Configure home-as-up for sub-screens.
         */
        private void setPreferenceScreenIntent(final PreferenceScreen preferenceScreen) {
            Intent intent = new Intent(getActivity(), CameraSettingsActivity.class);
            intent.putExtra(PREF_SCREEN_EXTRA, preferenceScreen.getKey());
            preferenceScreen.setIntent(intent);
        }

        /**
         * This override allows the CameraSettingsFragment to be reused for
         * different nested PreferenceScreens within the single camera
         * preferences XML resource. If the fragment is constructed with a
         * desired preference key (delivered via an extra in the creation
         * intent), it is used to look up the nested PreferenceScreen and
         * returned here.
         */
        @Override
        public PreferenceScreen getPreferenceScreen() {
            PreferenceScreen root = super.getPreferenceScreen();
            if (!mGetSubPrefAsRoot || mPrefKey == null || root == null) {
                return root;
            } else {
                PreferenceScreen match = findByKey(root, mPrefKey);
                if (match != null) {
                    return match;
                } else {
                    throw new RuntimeException("key " + mPrefKey + " not found");
                }
            }
        }

        private PreferenceScreen findByKey(PreferenceScreen parent, String key) {
            if (key.equals(parent.getKey())) {
                return parent;
            } else {
                for (int i = 0; i < parent.getPreferenceCount(); i++) {
                    Preference child = parent.getPreference(i);
                    if (child instanceof PreferenceScreen) {
                        PreferenceScreen match = findByKey((PreferenceScreen) child, key);
                        if (match != null) {
                            return match;
                        }
                    }
                }
                return null;
            }
        }

        /**
         * Depending on camera availability on the device, this removes settings
         * for cameras the device doesn't have.
         */
        private void setVisibilities() {
            PreferenceGroup photoGroup =
                    (PreferenceGroup) findPreference(PREF_CATEGORY_PHOTO);
            PreferenceGroup videoGroup =
                    (PreferenceGroup) findPreference(PREF_CATEGORY_VIDEO);
            PreferenceGroup generalGroup =
                    (PreferenceGroup) findPreference(PREF_CATEGORY_GENERAL);
            PreferenceScreen root = getPreferenceScreen();
            if (mPictureSizesBack == null) {
                mShowCameraPicturesizeBack = false;
                mShowVideoQualityBack = false;
            }
            if (mPictureSizesFront == null) {
                mShowCameraPicturesizeFront = false;
                mShowVideoQualityFront = false;
            }
            if (!(moduleScope.endsWith(NormalPhotoModule.AUTO_MODULE_STRING_ID) || moduleScope.endsWith(ManualModule.MANUAL_MODULE_STRING_ID))) {
                mShowCameraPicturesizeBack = false;
                mShowCameraPicturesizeFront = false;
            }
            if (!(moduleScope.endsWith(ManualModule.MANUAL_MODULE_STRING_ID) || moduleScope.endsWith(NormalPhotoModule.AUTO_MODULE_STRING_ID))) {
                mShowCameraGridLines = false;
            }
            if (!(moduleScope.endsWith(NormalPhotoModule.AUTO_MODULE_STRING_ID))) {
                mShowVideoQualityBack = false;
                mShowVideoQualityFront = false;
                mShowVideoEis = false;
            } else if (!CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_TCT_EIS, false)) {
                mShowVideoEis = false;
            }
            if (moduleScope.endsWith(NormalPhotoModule.AUTO_MODULE_STRING_ID)) {
                if (!CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_PHOTO_FACEBEAUTY_SUPPORT, true) || isFacingBack) {// MODIFIED by yuanxing.tan, 2016-03-22, BUG-1849045
                    mShowFacebeauty = false;
                }
            } else {
                mShowFacebeauty = false;
            }
            if (moduleScope.endsWith(NormalPhotoModule.AUTO_MODULE_STRING_ID) && CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_ATTENTION_SEEKER, false)) {
                if (!isFacingBack) {
                    mShowAttentionseeker = false;
                }
            } else {
                mShowAttentionseeker = false;
            }
            mShowGestureShot = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_GESTURE_SHOT, false);
            if (mShowGestureShot) {
                if (moduleScope.endsWith(NormalPhotoModule.AUTO_MODULE_STRING_ID)) {
                    if (isFacingBack) {
                        mShowGestureShot = false;
                    }
                } else {
                    mShowGestureShot = false;
                }
            }

            mShowAntiBand = CustomUtil.getInstance().getBoolean(CustomFields.DEF_ANTIBAND_MENU_VISIBLE, false);
            if(ExtBuild.device() != ExtBuild.MTK_MT6755) {
                mShowAis = false;
            }
            if (!mShowCameraPicturesizeBack) {
                recursiveDelete(photoGroup, findPreference(Keys.KEY_PICTURE_SIZE_BACK));
            }
            if (!mShowCameraPicturesizeFront) {
                recursiveDelete(photoGroup, findPreference(Keys.KEY_PICTURE_SIZE_FRONT));
            }
            if (!mShowVideoQualityBack) {
                recursiveDelete(videoGroup, findPreference(Keys.KEY_VIDEO_QUALITY_BACK));
            }
            if (!mShowVideoQualityFront) {
                recursiveDelete(videoGroup, findPreference(Keys.KEY_VIDEO_QUALITY_FRONT));
            }
            /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-19,BUG-1892480*/
            if (ExtBuild.device() == ExtBuild.MTK_MT6755) {
                recursiveDelete(photoGroup, findPreference(Keys.KEY_CAMERA_GRID_LINES));
            } else if (!mShowCameraGridLines) {
            /*MODIFIED-END by shunyin.zhang,BUG-1892480*/
                recursiveDelete(photoGroup, findPreference(Keys.KEY_CAMERA_GRID_LINES));
            }
            if (!mShowAis) {
                recursiveDelete(photoGroup, findPreference(Keys.KEY_CAMERA_AIS));
            }
            if (!mShowVideoEis) {
                recursiveDelete(videoGroup, findPreference(Keys.KEY_VIDEO_EIS));
            }
            if (!mShowCameraSavepath) {
                recursiveDelete(generalGroup, findPreference(Keys.KEY_CAMERA_SAVEPATH));
            }
            if (!mShowFacebeauty) {
                recursiveDelete(photoGroup, findPreference(Keys.KEY_CAMERA_FACEBEAUTY));
            }
            if (!mShowAttentionseeker) {
                recursiveDelete(photoGroup, findPreference(Keys.KEY_CAMERA_ATTENTIONSEEKER));
            }
            if (!mShowGestureShot) {
                recursiveDelete(photoGroup, findPreference(Keys.KEY_CAMERA_GESTURE_DETECTION));
            }
            mShowMirrorSelfie = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_MIRROR_SELFIE, false);
            if (moduleScope.endsWith(NormalPhotoModule.AUTO_MODULE_STRING_ID)) {
                if (isFacingBack) {
                    mShowMirrorSelfie = false;
                }
            } else {
                mShowMirrorSelfie = false;
            }

            if(!mShowMirrorSelfie){
                recursiveDelete(photoGroup, findPreference(Keys.KEY_CAMERA_MIRROR_SELFIE));
            }

            /*MODIFIED-BEGIN by shunyin.zhang, 2016-04-12,BUG-1892480*/
            mShowPose = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_POSE, false);
            if (moduleScope.endsWith(NormalPhotoModule.AUTO_MODULE_STRING_ID)) {
                if (!isFacingBack) {
                    mShowPose = false;
                }
            } else {
                mShowPose = false;
            }

            if(!mShowPose){
                recursiveDelete(photoGroup, findPreference(Keys.KEY_CAMERA_POSE));
            }

            mShowCompose = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_COMPOSE, false);
            if (moduleScope.endsWith(NormalPhotoModule.AUTO_MODULE_STRING_ID)) {
                if (!isFacingBack) {
                    mShowCompose = false;
                }
            } else {
                mShowCompose = false;
            }

            if(!mShowCompose){
                recursiveDelete(photoGroup, findPreference(Keys.KEY_CAMERA_COMPOSE));
            }
            /*MODIFIED-END by shunyin.zhang,BUG-1892480*/

            if (!mShowAntiBand) {
                recursiveDelete(generalGroup, findPreference(Keys.KEY_ANTIBANDING));
            }
            if (photoGroup != null && photoGroup.getPreferenceCount() < 1) {
                recursiveDelete(root, photoGroup);
            }
            if (videoGroup != null && videoGroup.getPreferenceCount() < 1) {
                recursiveDelete(root, videoGroup);
            }
            if (generalGroup != null && generalGroup.getPreferenceCount() < 1) {
                recursiveDelete(root, generalGroup);
            }
        }

        /**
         * Recursively go through settings and fill entries and summaries of our
         * preferences.
         */
        private void fillEntriesAndSummaries(PreferenceGroup group) {
            for (int i = 0; i < group.getPreferenceCount(); ++i) {
                Preference pref = group.getPreference(i);
                if (pref instanceof PreferenceGroup) {
                    fillEntriesAndSummaries((PreferenceGroup) pref);
                }
                setSummary(pref);
                setEntries(pref);
            }
        }

        private void fillEntriesAndSummaries() {
            Preference pref = getPreferenceScreen().findPreference(Keys.KEY_PICTURE_SIZE_FRONT);
            if (pref != null) {
                setEntries(pref);
                setSummary(pref);
                if (CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_VDF_PICTURE_SIZE_TITLE, false)){
                    pref.setTitle(getResources().getString(R.string.pref_front_camera_size_title_vdf));
                }
            }
            pref = getPreferenceScreen().findPreference(Keys.KEY_PICTURE_SIZE_BACK);
            if (pref != null) {
                setEntries(pref);
                setSummary(pref);
                if (CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_VDF_PICTURE_SIZE_TITLE, false)){
                    pref.setTitle(getResources().getString(R.string.pref_rear_camera_size_title_vdf));
                }
            }
            pref = getPreferenceScreen().findPreference(Keys.KEY_VIDEO_QUALITY_FRONT);
            if (pref != null) {
                setEntries(pref);
                setSummary(pref);
            }
            pref = getPreferenceScreen().findPreference(Keys.KEY_VIDEO_QUALITY_BACK);
            if (pref != null) {
                setEntries(pref);
                setSummary(pref);
            }
            pref = getPreferenceScreen().findPreference(Keys.KEY_CAMERA_SAVEPATH);
            if (pref != null) {
                setSummary(pref);
            }
            pref = getPreferenceScreen().findPreference(Keys.KEY_ANTIBANDING);
            if (pref != null) {
                setSummary(pref);
            }
        }

        /**
         * Recursively traverses the tree from the given group as the route and
         * tries to delete the preference. Traversal stops once the preference
         * was found and removed.
         */
        private boolean recursiveDelete(PreferenceGroup group, Preference preference) {
            if (group == null) {
                Log.d(TAG, "attempting to delete from null preference group");
                return false;
            }
            if (preference == null) {
                Log.d(TAG, "attempting to delete null preference");
                return false;
            }
            if (group.removePreference(preference)) {
                // Removal was successful.
                return true;
            }

            for (int i = 0; i < group.getPreferenceCount(); ++i) {
                Preference pref = group.getPreference(i);
                if (pref instanceof PreferenceGroup) {
                    if (recursiveDelete((PreferenceGroup) pref, preference)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
            // Don't finish here, resume may be called later when it's landscape.
            // if (mSecureCamera) {
            //     getActivity().finish();
            // }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            setSummary(findPreference(key));
        }
        public void refreshStorageSettings(boolean mounted) {
            PreferenceScreen root = getPreferenceScreen();
            ListPreference preferenceSavePath = (ListPreference) root.findPreference(Keys.KEY_CAMERA_SAVEPATH);
            if (preferenceSavePath != null) {
                if (preferenceSavePath.getDialog()!=null && preferenceSavePath.getDialog().isShowing()) {
                    preferenceSavePath.getDialog().dismiss();
                }
                if (mounted) {
                    preferenceSavePath.setEnabled(true);
                } else {
                    preferenceSavePath.setEnabled(false);
                }
                if (preferenceSavePath.getValue().equals(Storage.SDCARD_STORAGE)) {
                    if (Storage.isSDCardAvailable()) {
                        Storage.setSavePath(Storage.SDCARD_STORAGE);
                    } else {
                        preferenceSavePath.setValue(Storage.PHONE_STORAGE);
                        preferenceSavePath.setSummary(preferenceSavePath.getEntry());
                        Storage.setSavePath(Storage.PHONE_STORAGE);
                    }
                }
            }
        }
        /**
         * Set the entries for the given preference. The given preference needs
         * to be a {@link ListPreference}
         */
        private void setEntries(Preference preference) {
            if (!(preference instanceof ListPreference)) {
                return;
            }

            ListPreference listPreference = (ListPreference) preference;
            if (listPreference.getKey().equals(Keys.KEY_PICTURE_SIZE_BACK)) {
                setEntriesForSelection(mPictureSizesBack, listPreference);
            } else if (listPreference.getKey().equals(Keys.KEY_PICTURE_SIZE_FRONT)) {
                setEntriesForSelection(mPictureSizesFront, listPreference);
            } else if (listPreference.getKey().equals(Keys.KEY_VIDEO_QUALITY_BACK)) {
//                setEntriesForSelection(mVideoQualitiesBack, listPreference);
                setEntriesForSelection(mVideoQualitiesBack, mVideoQualityTitlesBack, listPreference);
            } else if (listPreference.getKey().equals(Keys.KEY_VIDEO_QUALITY_FRONT)) {
//                setEntriesForSelection(mVideoQualitiesFront, listPreference);
                setEntriesForSelection(mVideoQualitiesFront, mVideoQualityTitlesFront, listPreference);
            }
        }

        /**
         * Set the summary for the given preference. The given preference needs
         * to be a {@link ListPreference}.
         */
        private void setSummary(Preference preference) {
            if (!(preference instanceof ListPreference)) {
                return;
            }

            ListPreference listPreference = (ListPreference) preference;
            if (listPreference.getKey().equals(Keys.KEY_PICTURE_SIZE_BACK)) {
                setSummaryForSelection(mOldPictureSizesBack, mPictureSizesBack, listPreference);
            } else if (listPreference.getKey().equals(Keys.KEY_PICTURE_SIZE_FRONT)) {
                setSummaryForSelection(mOldPictureSizesFront, mPictureSizesFront, listPreference);
            } else if (listPreference.getKey().equals(Keys.KEY_VIDEO_QUALITY_BACK)) {
                setSummaryForSelection(mVideoQualityTitlesBack, listPreference);
            } else if (listPreference.getKey().equals(Keys.KEY_VIDEO_QUALITY_FRONT)) {
                setSummaryForSelection(mVideoQualityTitlesFront, listPreference);
            } else {
                listPreference.setSummary(listPreference.getEntry());
            }
        }

        /**
         * Sets the entries for the given list preference.
         *
         * @param selectedSizes The possible S,M,L entries the user can
         *            choose from.
         * @param preference The preference to set the entries for.
         */
        private void setEntriesForSelection(List<Size> selectedSizes,
                ListPreference preference) {
            if (selectedSizes == null) {
                return;
            }

            String[] entries = new String[selectedSizes.size()];
            String[] entryValues = new String[selectedSizes.size()];
            for (int i = 0; i < selectedSizes.size(); i++) {
                Size size = selectedSizes.get(i);
                entries[i] = getSizeSummaryString(size);
                entryValues[i] = SettingsUtil.sizeToSetting(size);
            }

            preference.setEntries(entries);
            preference.setEntryValues(entryValues);
        }
        private void setEntriesForSelection(String[] qualities,
                                            String[] titles, ListPreference preference) {
            preference.setEntries(titles);
            preference.setEntryValues(qualities);
        }

        /**
         * Sets the entries for the given list preference.
         *
         * @param selectedQualities The possible S,M,L entries the user can
         *            choose from.
         * @param preference The preference to set the entries for.
         */
        private void setEntriesForSelection(SelectedVideoQualities selectedQualities,
                ListPreference preference) {
            if (selectedQualities == null) {
                return;
            }

            // Avoid adding double entries at the bottom of the list which
            // indicates that not at least 3 qualities are supported.
            ArrayList<String> entries = new ArrayList<String>();
            entries.add(mCamcorderProfileNames[selectedQualities.large]);
            if (selectedQualities.medium != selectedQualities.large) {
                entries.add(mCamcorderProfileNames[selectedQualities.medium]);
            }
            if (selectedQualities.small != selectedQualities.medium) {
                entries.add(mCamcorderProfileNames[selectedQualities.small]);
            }
            preference.setEntries(entries.toArray(new String[0]));
        }

        /**
         * Sets the summary for the given list preference.
         *
         * @param oldPictureSizes The old selected picture sizes for small medium and large
         * @param displayableSizes The human readable preferred sizes
         * @param preference The preference for which to set the summary.
         */
        private void setSummaryForSelection(SelectedPictureSizes oldPictureSizes,
                List<Size> displayableSizes, ListPreference preference) {
            if (oldPictureSizes == null) {
                return;
            }

            String setting = preference.getValue();
            Size selectedSize = oldPictureSizes.getFromSetting(setting, displayableSizes);

            preference.setSummary(getSizeSummaryString(selectedSize));
        }

        private void setSummaryForSelection(String[] titles, List<Size> displayableSizes, ListPreference preference) {
            String setting = preference.getValue();
            preference.setSummary(titles[displayableSizes.indexOf(SettingsUtil.sizeFromString(setting))]);
        }

        /**
         * Sets the summary for the given list preference.
         *
         * @param selectedQualities The selected video qualities.
         * @param preference The preference for which to set the summary.
         */
        private void setSummaryForSelection(SelectedVideoQualities selectedQualities,
                ListPreference preference) {
            if (selectedQualities == null) {
                return;
            }

            int selectedQuality = selectedQualities.getFromSetting(preference.getValue());
            preference.setSummary(mCamcorderProfileNames[selectedQuality]);
        }

        private void setSummaryForSelection(String[] titles,
                                            ListPreference preference) {
            if (titles == null) {
                return;
            }
            preference.setSummary(titles[preference.findIndexOfValue(preference.getValue())]);
        }
        /**
         * This method gets the selected picture sizes for S,M,L and populates
         * {@link #mPictureSizesBack}, {@link #mPictureSizesFront},
         * {@link #mVideoQualitiesBack} and {@link #mVideoQualitiesFront}
         * accordingly.
         */
        private void loadSizes() {
            if (mInfos == null) {
                Log.w(TAG, "null deviceInfo, cannot display resolution sizes");
                return;
            }
            isFacingBack = mInfos.getCharacteristics(cameraId).isFacingBack();
            if (isFacingBack) {
                List<Size> sizes = CameraPictureSizesCacher.getSizesForCamera(cameraId,
                        this.getActivity().getApplicationContext());
                if (sizes != null) {
                    mOldPictureSizesBack = SettingsUtil.getSelectedCameraPictureSizes(sizes,
                            cameraId);
//                    mPictureSizesBack = ResolutionUtil
//                            .getDisplayableSizesFromSupported(sizes, true);
                    mPictureSizesBack = sizes;
                }
//                mVideoQualitiesBack = SettingsUtil.getSelectedVideoQualities(cameraId);
                mVideoQualitiesBack = CameraPictureSizesCacher.getQualitiesForCamera(cameraId, this.getActivity().getApplicationContext());
                mVideoQualityTitlesBack = CameraPictureSizesCacher.getQualityTitlesForCamera(cameraId, this.getActivity().getApplicationContext());

                mPictureSizesFront = null;
                mVideoQualitiesFront = null;

            } else {
                mPictureSizesBack = null;
                mVideoQualitiesBack = null;

                List<Size> sizes = CameraPictureSizesCacher.getSizesForCamera(cameraId,
                        this.getActivity().getApplicationContext());
                if (sizes != null) {
                    mOldPictureSizesFront = SettingsUtil.getSelectedCameraPictureSizes(sizes,
                            cameraId);
//                    mPictureSizesFront =
//                            ResolutionUtil.getDisplayableSizesFromSupported(sizes, false);
                    mPictureSizesFront = sizes;
                }
//                mVideoQualitiesFront = SettingsUtil.getSelectedVideoQualities(cameraId);
                mVideoQualitiesFront = CameraPictureSizesCacher.getQualitiesForCamera(cameraId, this.getActivity().getApplicationContext());
                mVideoQualityTitlesFront = CameraPictureSizesCacher.getQualityTitlesForCamera(cameraId, this.getActivity().getApplicationContext());
            }
        }

        /**
         * @param size The photo resolution.
         * @return A human readable and translated string for labeling the
         *         picture size in megapixels.
         */
        private String getSizeSummaryString(Size size) {
            Size approximateSize = ResolutionUtil.getApproximateSize(size);
            long megaPixels = Math.round((size.width() * size.height()) / 1e6);
            int numerator = ResolutionUtil.aspectRatioNumerator(approximateSize);
            int denominator = ResolutionUtil.aspectRatioDenominator(approximateSize);
            String result = getResources().getString(
                    R.string.aspect_ratio_and_megapixels_default, megaPixels, numerator, denominator);
            return result;
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference) {
            if (Keys.KEY_RESTORE_SETTING.equals(preference.getKey())) {
                warnResetToFactory();
                return true;
            }

            if (Keys.KEY_RECORD_LOCATION.equals(preference.getKey())) {
                boolean grant = PermissionUtil.isNoncriticalPermissionGranted(this.getActivity());
                if (!grant) {
                    PermissionUtil.showSnackBar(this.getActivity(), getView(),
                            R.string.location_snack_setting,
                            R.string.grant_access_settings);
                    setRememberLocation(false);
                } else {
                    onGpsSaving();
                }
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        private void setRememberLocation(boolean remember) {
            ManagedSwitchPreference mp = (ManagedSwitchPreference)
                    getPreferenceScreen().findPreference(Keys.KEY_RECORD_LOCATION);
            mp.setChecked(remember);
            CameraApp cameraApp = (CameraApp) (getActivity().getApplication());
            SettingsManager settingsManager = cameraApp.getSettingsManager();
            if (settingsManager.getBoolean(SettingsManager.SCOPE_GLOBAL, Keys.KEY_RECORD_LOCATION) != remember) {
                settingsManager.set(SettingsManager.SCOPE_GLOBAL, Keys.KEY_RECORD_LOCATION, remember);
            }
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (dialog == mWarnResetToFactory) {
                boolean restore = which == DialogInterface.BUTTON_POSITIVE;
                if (restore) {
                    restoreDefaultPreferences();
                }
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            if (mWarnResetToFactory != null) {
                mWarnResetToFactory.dismiss();
            }
            CameraAgentFactory.recycle(CameraAgentFactory.CameraApi.API_1);
        }

        private void warnResetToFactory() {
            // TODO: DialogFragment?
            mWarnResetToFactory = new AlertDialog.Builder(getActivity())
                    .setMessage(getResources().getString(R.string.restore_settings_dialog_msg))
                    .setPositiveButton(R.string.restore_settings_dialog_yes, this)
                    .setNegativeButton(R.string.restore_settings_dialog_no, this)
                    .show();
        }

        private void restoreDefaultPreferences() {
            CameraUtil.cleanSharedPreference(getActivity());
            getActivity().finish();
        }


        private void onGpsSaving() {
            CameraApp cameraApp = (CameraApp) (getActivity().getApplication());
            SettingsManager settingsManager = cameraApp.getSettingsManager();
            boolean isGpsTaggingOn = CameraUtil.gotoGpsSetting(getActivity(), settingsManager, R.drawable.gps_white);
            if (isGpsTaggingOn) {
                setRememberLocation(false);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case CameraUtil.SETGPS: {
                    CameraApp cameraApp = (CameraApp) (getActivity().getApplication());
                    SettingsManager settingsManager = cameraApp.getSettingsManager();
                    boolean isGpsOn = CameraUtil.backFromGpsSetting(getActivity(), settingsManager);
                    if (isGpsOn) {
                        setRememberLocation(true);
                    }
                }
            }
        }
    }
}
