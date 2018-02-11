/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.camera;

import android.content.Context;
/* MODIFIED-BEGIN by yuanxing.tan, 2016-05-09,BUG-2120960*/
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.hardware.Camera;
/* MODIFIED-END by yuanxing.tan,BUG-2120960*/
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.android.camera.app.AppController;
import com.android.camera.app.CameraAppUI;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.ui.RadioOptions;
import com.android.camera.ui.Rotatable;
import com.android.camera.ui.RotatableButton;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.util.LockUtils;
import com.android.camera.util.PhotoSphereHelper;
import com.android.camera.widget.ModeOptions;
import com.tct.camera.R;

import java.util.HashMap;
import java.util.Map;

/**
 * A  class for generating pre-initialized
 * {@link #android.widget.ImageButton}s.
 */
public class ButtonManager implements SettingsManager.OnSettingChangedListener {

//    public static final int BUTTON_FLASH = 0;
//    public static final int BUTTON_TORCH = 1;
//    public static final int BUTTON_HDR_PLUS_FLASH = 2;
    public static final int BUTTON_CAMERA = 3;
    public static final int BUTTON_HDR_PLUS = 4;
    public static final int BUTTON_HDR = 5;
    public static final int BUTTON_CANCEL = 6;
    public static final int BUTTON_DONE = 7;
    public static final int BUTTON_RETAKE = 8;
    public static final int BUTTON_REVIEW = 9;
    public static final int BUTTON_GRID_LINES = 10;
    public static final int BUTTON_EXPOSURE_COMPENSATION = 11;
    public static final int BUTTON_COUNTDOWN = 12;
    public static final int BUTTON_LOWLIGHT = 13;
    public static final int BUTTON_WRAPPER = 14;
    public static final int BUTTON_SETTING=15;
    public static final int BUTTON_SWITCH = 16;

    /** For two state MultiToggleImageButtons, the off index. */
    public static final int OFF = 0;
    /** For two state MultiToggleImageButtons, the on index. */
    public static final int ON = 1;

    /** A reference to the application's settings manager. */
    private final SettingsManager mSettingsManager;


    /** Bottom bar options toggle buttons. */
    private RotatableButton mButtonSetting;
    private MultiToggleImageButton mButtonCamera;
//    private MultiToggleImageButton mButtonFlash;
    private MultiToggleImageButton mButtonHdr;
    private MultiToggleImageButton mButtonGridlines;
    private MultiToggleImageButton mButtonCountdown;
    private MultiToggleImageButton mButtonLowlight;

    /** Intent UI buttons. */
    private ImageButton mButtonCancel;
    private ImageButton mButtonDone;
    private ImageButton mButtonRetake; // same as review.

    private ImageButton mButtonExposureCompensation;
    private ImageButton mExposureN2;
    private ImageButton mExposureN1;
    private ImageButton mExposure0;
    private ImageButton mExposureP1;
    private ImageButton mExposureP2;
    private RadioOptions mModeOptionsExposure;
    private RadioOptions mModeOptionsPano;
    private View mModeOptionsButtons;
    private ModeOptions mModeOptions;

    private ImageButton mWrapperButton;

    private int mMinExposureCompensation;
    private int mMaxExposureCompensation;
    private float mExposureCompensationStep;

    /** A listener for button enabled and visibility
        state changes. */
    private ButtonStatusListener mListener;

    /** An reference to the gcam mode index. */
    private static int sGcamIndex;

    private final AppController mAppController;

    private HelpTipsManager mHelpTipsManager;

    /**
     * Get a new global ButtonManager.
     */
    public ButtonManager(AppController app) {
        mAppController = app;

        Context context = app.getAndroidContext();
        sGcamIndex = context.getResources().getInteger(R.integer.camera_mode_gcam);

        mSettingsManager = app.getSettingsManager();
        mSettingsManager.addListener(this);
    }
    public void registerOnSharedPreferenceChangeListener() {
        if (mSettingsManager != null) {
            mSettingsManager.addListener(this);
        }
    }
    /**
     * Load references to buttons under a root View.
     * Call this after the root clears/reloads all of its children
     * to prevent stale references button views.
     */
    public void load(View root) {
        getButtonsReferences(root);
    }

    /**
     * ButtonStatusListener provides callbacks for when button's
     * visibility changes and enabled status changes.
     */
    public interface ButtonStatusListener {
        /**
         * A button's visibility has changed.
         */
        public void onButtonVisibilityChanged(ButtonManager buttonManager, int buttonId);

        /**
         * A button's enabled state has changed.
         */
        public void onButtonEnabledChanged(ButtonManager buttonManager, int buttonId);
    }

    /**
     * Sets the ButtonStatusListener.
     */
    public void setListener(ButtonStatusListener listener) {
        mListener = listener;
    }

    /**
     * Gets references to all known buttons.
     */
    private void getButtonsReferences(View root) {
        mButtonSetting
            =(RotatableButton)root.findViewById(R.id.menu_setting_button);
        mButtonCamera
            = (MultiToggleImageButton) root.findViewById(R.id.camera_toggle_button);
//        mButtonFlash
//            = (MultiToggleImageButton) root.findViewById(R.id.flash_toggle_button);
        mButtonHdr
            = (MultiToggleImageButton) root.findViewById(R.id.hdr_plus_toggle_button);
        mButtonGridlines
            = (MultiToggleImageButton) root.findViewById(R.id.grid_lines_toggle_button);

        mButtonCancel
            = (ImageButton) root.findViewById(R.id.cancel_button);
        mButtonDone
            = (ImageButton) root.findViewById(R.id.done_button);
        mButtonRetake
            = (ImageButton) root.findViewById(R.id.retake_button);
        mButtonLowlight
                = (MultiToggleImageButton) root.findViewById(R.id.lowlight_toggle_button);

        mWrapperButton = (ImageButton) root.findViewById(R.id.wrapper_button);

        mButtonExposureCompensation =
            (ImageButton) root.findViewById(R.id.exposure_button);
        mExposureN2 = (ImageButton) root.findViewById(R.id.exposure_n2);
        mExposureN1 = (ImageButton) root.findViewById(R.id.exposure_n1);
        mExposure0 = (ImageButton) root.findViewById(R.id.exposure_0);
        mExposureP1 = (ImageButton) root.findViewById(R.id.exposure_p1);
        mExposureP2 = (ImageButton) root.findViewById(R.id.exposure_p2);
        mModeOptionsExposure = (RadioOptions) root.findViewById(R.id.mode_options_exposure);
        mModeOptionsPano = (RadioOptions) root.findViewById(R.id.mode_options_pano);
        mModeOptionsButtons = root.findViewById(R.id.mode_options_buttons);
        mModeOptions = (ModeOptions) root.findViewById(R.id.mode_options);

        mButtonCountdown = (MultiToggleImageButton) root.findViewById(R.id.countdown_toggle_button);

        mAppController.addRotatableToListenerPool(new Rotatable.RotateEntity(mButtonCountdown,true));
        mAppController.addRotatableToListenerPool(new Rotatable.RotateEntity(mButtonSetting,true));
        mAppController.addRotatableToListenerPool(new Rotatable.RotateEntity(mButtonCamera,true));
//        mAppController.addRotatableToListenerPool(new Rotatable.RotateEntity(mButtonFlash, true));
        mAppController.addRotatableToListenerPool(new Rotatable.RotateEntity(mButtonHdr, true));
        mAppController.addRotatableToListenerPool(new Rotatable.RotateEntity(mButtonGridlines, true));
        mAppController.addRotatableToListenerPool(new Rotatable.RotateEntity(mButtonLowlight, true));

        mAppController.addLockableToListenerPool(mButtonSetting);
        mAppController.addLockableToListenerPool(mButtonCamera);
//        mAppController.addLockableToListenerPool(mButtonFlash);
        mAppController.addLockableToListenerPool(mButtonHdr);
        mAppController.addLockableToListenerPool(mButtonGridlines);
        mAppController.addLockableToListenerPool(mButtonLowlight);

    }

    @Override
    public void onSettingChanged(SettingsManager settingsManager, String key) {
        MultiToggleImageButton button = null;
        int index = 0;

        if (key.equals(Keys.KEY_FLASH_MODE)) {
//            index = mSettingsManager.getIndexOfCurrentValue(mAppController.getCameraScope(),
//                                                            Keys.KEY_FLASH_MODE);
//            button = getButtonOrError(BUTTON_FLASH);
        } else if (key.equals(Keys.KEY_VIDEOCAMERA_FLASH_MODE)) {
//            index = mSettingsManager.getIndexOfCurrentValue(mAppController.getCameraScope(),
//                                                            Keys.KEY_VIDEOCAMERA_FLASH_MODE);
//            button = getButtonOrError(BUTTON_TORCH);
        } else if (key.equals(Keys.KEY_HDR_PLUS_FLASH_MODE)) {
//            index = mSettingsManager.getIndexOfCurrentValue(mAppController.getModuleScope(),
//                                                            Keys.KEY_HDR_PLUS_FLASH_MODE);
//            button = getButtonOrError(BUTTON_HDR_PLUS_FLASH);
        } else if (key.equals(Keys.KEY_CAMERA_ID)) {
            index = mSettingsManager.getIndexOfCurrentValue(SettingsManager.SCOPE_GLOBAL,
                                                            Keys.KEY_CAMERA_ID);
            button = getButtonOrError(BUTTON_CAMERA);
        } else if (key.equals(Keys.KEY_CAMERA_HDR_PLUS)) {
            index = mSettingsManager.getIndexOfCurrentValue(SettingsManager.SCOPE_GLOBAL,
                                                            Keys.KEY_CAMERA_HDR_PLUS);
            button = getButtonOrError(BUTTON_HDR_PLUS);
        } else if (key.equals(Keys.KEY_CAMERA_HDR)) {
            index = mSettingsManager.getIndexOfCurrentValue(SettingsManager.SCOPE_GLOBAL,
                                                            Keys.KEY_CAMERA_HDR);
            button = getButtonOrError(BUTTON_HDR);
        } else if (key.equals(Keys.KEY_CAMERA_GRID_LINES)) {
            index = mSettingsManager.getIndexOfCurrentValue(SettingsManager.SCOPE_GLOBAL,
                                                            Keys.KEY_CAMERA_GRID_LINES);
            button = getButtonOrError(BUTTON_GRID_LINES);
        } else if (key.equals(Keys.KEY_CAMERA_PANO_ORIENTATION)) {
            updatePanoButtons();
        } else if (key.equals(Keys.KEY_EXPOSURE)) {
            updateExposureButtons();
        } else if (key.equals(Keys.KEY_COUNTDOWN_DURATION)) {
            index = mSettingsManager.getIndexOfCurrentValue(mAppController.getCameraScope(),
                                                            Keys.KEY_COUNTDOWN_DURATION);
            button = getButtonOrError(BUTTON_COUNTDOWN);
        } else if (key.equals(Keys.KEY_CAMERA_LOWLIGHT)) {
            index = mSettingsManager.getIndexOfCurrentValue(mAppController.getCameraScope(),
                    Keys.KEY_CAMERA_LOWLIGHT);
            button = getButtonOrError(BUTTON_LOWLIGHT);
        }

        if (button != null && button.getState() != index) {
            button.setState(Math.max(index, 0), false);
        }
    }

    /**
     * A callback executed in the state listener of a button.
     *
     * Used by a module to set specific behavior when a button's
     * state changes.
     */
    public interface ButtonCallback {
        public void onStateChanged(int state);
    }
    public interface ExtendButtonCallback extends ButtonCallback{
        public void onUnhandledClick();
    }
    /**
     * Returns the appropriate {@link com.android.camera.MultiToggleImageButton}
     * based on button id.  An IllegalStateException will be throw if the
     * button could not be found in the view hierarchy.
     */
    private MultiToggleImageButton getButtonOrError(int buttonId) {
        switch (buttonId) {
//            case BUTTON_FLASH:
//                if (mButtonFlash == null) {
//                    throw new IllegalStateException("Flash button could not be found.");
//                }
//                return mButtonFlash;
//            case BUTTON_TORCH:
//                if (mButtonFlash == null) {
//                    throw new IllegalStateException("Torch button could not be found.");
//                }
//                return mButtonFlash;
//            case BUTTON_HDR_PLUS_FLASH:
//                if (mButtonFlash == null) {
//                    throw new IllegalStateException("Hdr plus torch button could not be found.");
//                }
//                return mButtonFlash;
            case BUTTON_CAMERA:
                if (mButtonCamera == null) {
                    throw new IllegalStateException("Camera button could not be found.");
                }
                return mButtonCamera;
            case BUTTON_SWITCH:
                if (mButtonCamera == null) {
                    throw new IllegalStateException("Camera button could not be found.");
                }
                return mButtonCamera;
            case BUTTON_HDR_PLUS:
                if (mButtonHdr == null) {
                    throw new IllegalStateException("Hdr plus button could not be found.");
                }
                return mButtonHdr;
            case BUTTON_HDR:
                if (mButtonHdr == null) {
                    throw new IllegalStateException("Hdr button could not be found.");
                }
                return mButtonHdr;
            case BUTTON_LOWLIGHT:
                if (mButtonLowlight == null) {
                    throw new IllegalStateException("Hdr button could not be found.");
                }
                return mButtonLowlight;
            case BUTTON_GRID_LINES:
                if (mButtonGridlines == null) {
                    throw new IllegalStateException("Grid lines button could not be found.");
                }
                return mButtonGridlines;

            case BUTTON_COUNTDOWN:
                if (mButtonCountdown == null) {
                    throw new IllegalStateException("Countdown button could not be found.");
                }
                return mButtonCountdown;
            default:
                throw new IllegalArgumentException("button not known by id=" + buttonId);
        }
    }

    /**
     * Returns the appropriate {@link android.widget.ImageButton}
     * based on button id.  An IllegalStateException will be throw if the
     * button could not be found in the view hierarchy.
     */
    private ImageButton getImageButtonOrError(int buttonId) {
        switch (buttonId) {
            case BUTTON_CANCEL:
                if (mButtonCancel == null) {
                    throw new IllegalStateException("Cancel button could not be found.");
                }
                return mButtonCancel;
            case BUTTON_DONE:
                if (mButtonDone == null) {
                    throw new IllegalStateException("Done button could not be found.");
                }
                return mButtonDone;
            case BUTTON_RETAKE:
                if (mButtonRetake == null) {
                    throw new IllegalStateException("Retake button could not be found.");
                }
                return mButtonRetake;
            case BUTTON_REVIEW:
                if (mButtonRetake == null) {
                    throw new IllegalStateException("Review button could not be found.");
                }
                return mButtonRetake;
            case BUTTON_EXPOSURE_COMPENSATION:
                if (mButtonExposureCompensation == null) {
                    throw new IllegalStateException("Exposure Compensation button could not be found.");
                }
                return mButtonExposureCompensation;
            case BUTTON_WRAPPER:
                if (mWrapperButton == null) {
                    throw new IllegalStateException("Wrapper button could not be found.");
                }
                return mWrapperButton;
            default:
                throw new IllegalArgumentException("button not known by id=" + buttonId);
        }
    }


    /**
     * Initialize a known button by id, with a state change callback and
     * a resource id that points to an array of drawables, and then enable
     * the button.
     */
    public void initializeButton(int buttonId, ButtonCallback cb) {
        MultiToggleImageButton button = getButtonOrError(buttonId);
        button.setVisibility(View.VISIBLE);
        switch (buttonId) {
//            case BUTTON_FLASH:
//                initializeFlashButton(button, cb, R.array.camera_flashmode_icons);
//                break;
//            case BUTTON_TORCH:
//                initializeTorchButton(button, cb, R.array.video_flashmode_icons);
//                break;
//            case BUTTON_HDR_PLUS_FLASH:
//                initializeHdrPlusFlashButton(button, cb, R.array.camera_flashmode_icons);
//                break;
            case BUTTON_CAMERA:
                if(CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_VDF_SWITCH_CAMERA_ICONS_CUSTOMIZE, false)){
                    initializeCameraButton(button, cb, R.array.camera_id_icons_vdf);
                }else {
                    initializeCameraButton(button, cb, R.array.camera_id_icons);
                }

                break;
            case BUTTON_SWITCH:
                if(CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_VDF_SWITCH_CAMERA_ICONS_CUSTOMIZE, false)){
                    initializeSwitchButton(button, cb, R.array.camera_id_icons_vdf);
                }else {
                    initializeSwitchButton(button, cb, R.array.camera_id_icons);
                }
                break;
            case BUTTON_HDR_PLUS:
                initializeHdrPlusButton(button, cb, R.array.pref_camera_hdr_plus_icons);
                break;
            case BUTTON_HDR:
                initializeHdrButton(button, cb, R.array.pref_camera_hdr_icons);
                break;
            case BUTTON_GRID_LINES:
                initializeGridLinesButton(button, cb, R.array.grid_lines_icons);
                break;

            case BUTTON_COUNTDOWN:
                if(CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_VDF_COUNT_TIMER, false)){
                    initializeCountdownButton(button, cb, R.array.vdf_countdown_duration_icons, R.array.vdf_countdown_duration_descriptions);
//                    if(button != null && button instanceof MultiToggleImageButton){
//                        ((MultiToggleImageButton)button).overrideContentDescriptions(R.array.vdf_countdown_duration_descriptions);
//                    }
                }else {
                    initializeCountdownButton(button, cb, R.array.countdown_duration_icons);
                }

                break;
            case BUTTON_LOWLIGHT:
                initializeLowlightButton(button, cb, R.array.lowlight_icons);
                break;
            default:
                throw new IllegalArgumentException("button not known by id=" + buttonId);
        }

        enableButton(buttonId);
    }

    /**
     * Initialize a known button with a click listener and a resource id.
     * Sets the button visible.
     */
    public void initializePushButton(int buttonId, View.OnClickListener cb,
            int imageId) {
        ImageButton button = getImageButtonOrError(buttonId);
        button.setOnClickListener(cb);
        button.setImageResource(imageId);

        if (!button.isEnabled()) {
            button.setEnabled(true);
            if (mListener != null) {
                mListener.onButtonEnabledChanged(this, buttonId);
            }
        }
        button.setTag(R.string.tag_enabled_id, buttonId);

        if (button.getVisibility() != View.VISIBLE) {
            button.setVisibility(View.VISIBLE);
            if (mListener != null) {
                mListener.onButtonVisibilityChanged(this, buttonId);
            }
        }
    }

    /**
     * Initialize a known button with a click listener. Sets the button visible.
     */
    public void initializePushButton(int buttonId, View.OnClickListener cb) {
        ImageButton button = getImageButtonOrError(buttonId);
        if (cb != null) {
            button.setOnClickListener(cb);
        }

        if (!button.isEnabled()) {
            button.setEnabled(true);
            if (mListener != null) {
                mListener.onButtonEnabledChanged(this, buttonId);
            }
        }
        button.setTag(R.string.tag_enabled_id, buttonId);

        if (button.getVisibility() != View.VISIBLE) {
            button.setVisibility(View.VISIBLE);
            if (mListener != null) {
                mListener.onButtonVisibilityChanged(this, buttonId);
            }
        }
    }

    /**
     * Sets a button in its disabled (greyed out) state.
     */
    public void disableButton(int buttonId) {
        MultiToggleImageButton button = getButtonOrError(buttonId);

        // HDR and HDR+ buttons share the same button object,
        // but change actual image icons at runtime.
        // This extra check is to ensure the correct icons are used
        // in the case of the HDR[+] button being disabled at startup,
        // e.g. app startup with front-facing camera.
        // b/18104680
        if (buttonId == BUTTON_HDR_PLUS) {
            initializeHdrPlusButtonIcons(button, R.array.pref_camera_hdr_plus_icons);
        } else if (buttonId == BUTTON_HDR) {
            initializeHdrButtonIcons(button, R.array.pref_camera_hdr_icons);
        }

        if (button.isEnabled()) {
            button.setEnabled(false);
            if (mListener != null) {
                mListener.onButtonEnabledChanged(this, buttonId);
            }
        }
        button.setTag(R.string.tag_enabled_id, null);

        if (button.getVisibility() != View.VISIBLE) {
            button.setVisibility(View.VISIBLE);
            if (mListener != null) {
                mListener.onButtonVisibilityChanged(this, buttonId);
            }
        }
    }

    private Map<Integer,LockUtils.Lock> mLockMap=new HashMap<>();
    public int disableButtonWithLock(int buttonId){
        LockUtils.Lock lock=null;
        if(!mLockMap.containsKey(buttonId)){
            lock=LockUtils.getInstance().generateMultiLock(LockUtils.LockType.MULTILOCK);
            mLockMap.put(buttonId,lock);
        }else{
            lock=mLockMap.get(buttonId);
        }
        disableButton(buttonId);
        return lock.aquireLock();
    }

    public void enableButtonWithToken(int buttonId,int token){
        if(!mLockMap.containsKey(buttonId)){
            enableButton(buttonId);
            return;
        }
        LockUtils.Lock lock=mLockMap.get(buttonId);
        lock.unlockWithToken(token);

        if(!lock.isLocked()){
            mLockMap.remove(buttonId); // MODIFIED by yuanxing.tan, 2016-06-18,BUG-2202739
            enableButton(buttonId);
        }
    }

    /**
     * Enables a button that has already been initialized.
     */
    public void enableButton(int buttonId) {
        MultiToggleImageButton button = getButtonOrError(buttonId);
        if (!button.isEnabled() && !mLockMap.containsKey(buttonId)) { // MODIFIED by yuanxing.tan, 2016-06-18,BUG-2202739
            button.setEnabled(true);
            if (mListener != null) {
                mListener.onButtonEnabledChanged(this, buttonId);
            }
        }
        button.setTag(R.string.tag_enabled_id, buttonId);

        if (button.getVisibility() != View.VISIBLE) {
            button.setVisibility(View.VISIBLE);
            if (mListener != null) {
                mListener.onButtonVisibilityChanged(this, buttonId);
            }
        }
    }


    /**
     * Disable click reactions for a button without affecting visual state.
     * For most cases you'll want to use {@link #disableButton(int)}.
     * @param buttonId The id of the button.
     */
    public void disableButtonClick(int buttonId) {
        MultiToggleImageButton button = getButtonOrError(buttonId);
        if (button instanceof MultiToggleImageButton) {
            ((MultiToggleImageButton) button).setClickEnabled(false);
        }
    }

    /**
     * Enable click reactions for a button without affecting visual state.
     * For most cases you'll want to use {@link #enableButton(int)}.
     * @param buttonId The id of the button.
     */
    public void enableButtonClick(int buttonId) {
        MultiToggleImageButton button = getButtonOrError(buttonId);
        if (button instanceof MultiToggleImageButton) {
            ((MultiToggleImageButton) button).setClickEnabled(true);
        }
    }

    /**
     * Hide a button by id.
     */
    public void hideButton(int buttonId) {
        View button;
        try {
            button = getButtonOrError(buttonId);
        } catch (IllegalArgumentException e) {
            button = getImageButtonOrError(buttonId);
        }
        if (button.getVisibility() == View.VISIBLE) {
            button.setVisibility(View.GONE);
            if (mListener != null) {
                mListener.onButtonVisibilityChanged(this, buttonId);
            }
        }
    }

    public void setButtonInvisible(int buttonId){
        View button;
        try {
            button = getButtonOrError(buttonId);
        } catch (IllegalArgumentException e) {
            button = getImageButtonOrError(buttonId);
        }
        if (button.getVisibility() == View.VISIBLE) {
            button.setVisibility(View.INVISIBLE);
            if (mListener != null) {
                mListener.onButtonVisibilityChanged(this, buttonId);
            }
        }
    }

    /**
     * Since setting button is not a MultiToggleImageButton , perform the action separately
     */

    public void hideSettings(){
        if(mButtonSetting!=null){
            if(mButtonSetting.getVisibility()!=View.INVISIBLE) {
                if(Thread.currentThread()== Looper.getMainLooper().getThread()) {
                    mButtonSetting.setVisibility(View.INVISIBLE);
                }else{
                    mButtonSetting.post(new Runnable() {
                        @Override
                        public void run() {
                            mButtonSetting.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }
        }
    }

    public void showSettings(){
        if(mButtonSetting!=null){
            if(mButtonSetting.getVisibility()!=View.VISIBLE) {
                if(Thread.currentThread()== Looper.getMainLooper().getThread()) {
                    mButtonSetting.setVisibility(View.VISIBLE);
                }else{
                    mButtonSetting.post(new Runnable() {
                        @Override
                        public void run() {
                            mButtonSetting.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }
    }

    public void setToInitialState() {
        mModeOptions.setMainBar(ModeOptions.BAR_STANDARD);
    }

    public void setExposureCompensationCallback(final CameraAppUI.BottomBarUISpec
                                        .ExposureCompensationSetCallback cb) {
        if (cb == null) {
            mModeOptionsExposure.setOnOptionClickListener(null);
        } else {
            mModeOptionsExposure
                .setOnOptionClickListener(new RadioOptions.OnOptionClickListener() {
                    @Override
                    public void onOptionClicked(View v) {
                        int comp = Integer.parseInt((String) (v.getTag()));

                        if (mExposureCompensationStep != 0.0f) {
                            int compValue =
                                    Math.round(comp / mExposureCompensationStep);
                            cb.setExposure(compValue);
                        }
                    }
                });
        }
    }

    /**
     * Set the exposure compensation parameters supported by the current camera mode.
     * @param min Minimum exposure compensation value.
     * @param max Maximum exposure compensation value.
     * @param step Expsoure compensation step value.
     */
    public void setExposureCompensationParameters(int min, int max, float step) {
        mMaxExposureCompensation = max;
        mMinExposureCompensation = min;
        mExposureCompensationStep = step;


        setVisible(mExposureN2, (Math.round(min * step) <= -2));
        setVisible(mExposureN1, (Math.round(min * step) <= -1));
        setVisible(mExposureP1, (Math.round(max * step) >= 1));
        setVisible(mExposureP2, (Math.round(max * step) >= 2));

        updateExposureButtons();
    }

    private static void setVisible(View v, boolean visible) {
        if (visible) {
            v.setVisibility(View.VISIBLE);
        } else {
            v.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * @return The exposure compensation step value.
     **/
    public float getExposureCompensationStep() {
        return mExposureCompensationStep;
    }

    /**
     * Check if a button is enabled with the given button id..
     */
    public boolean isEnabled(int buttonId) {
        View button;
        try {
            button = getButtonOrError(buttonId);
        } catch (IllegalArgumentException e) {
            button = getImageButtonOrError(buttonId);
        }

        Integer enabledId = (Integer) button.getTag(R.string.tag_enabled_id);
        if (enabledId != null) {
            return (enabledId.intValue() == buttonId) && button.isEnabled();
        } else {
            return false;
        }
    }

    /**
     * Check if a button is visible.
     */
    public boolean isVisible(int buttonId) {
        View button;
        try {
            button = getButtonOrError(buttonId);
        } catch (IllegalArgumentException e) {
            button = getImageButtonOrError(buttonId);
        }
        return (button.getVisibility() == View.VISIBLE);
    }

    public void initializeSettingButton(final Runnable runnable) {
        if(mButtonSetting.getVisibility()==View.GONE) {
            mButtonSetting.setVisibility(View.VISIBLE);
        }
        mButtonSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHelpTipsManager != null) {
                    mHelpTipsManager.clickSettingResponse();
                }
                runnable.run();
            }
        });
    }


    /**
     * Initialize a flash button.
     */
    private void initializeFlashButton(final MultiToggleImageButton button,
            final ButtonCallback cb, int resIdImages) {

        if (resIdImages > 0) {
            button.overrideImageIds(resIdImages);
        }
        button.overrideContentDescriptions(R.array.camera_flash_descriptions);
        /* MODIFIED-BEGIN by yuanxing.tan, 2016-05-09,BUG-2120960*/
        Resources res = mAppController.getAndroidContext().getResources();
        if (mAppController.getCurrentCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mSettingsManager.setDefaults(Keys.KEY_FLASH_MODE, CustomUtil.getInstance().getString(CustomFields.DEF_CAMERA_FLASHMODE_FRONT, res.getString(R.string.pref_camera_flashmode_default)), res.getStringArray(R.array.pref_camera_flashmode_entryvalues));
        } else {
            mSettingsManager.setDefaults(Keys.KEY_FLASH_MODE, res.getString(R.string.pref_camera_flashmode_default), res.getStringArray(R.array.pref_camera_flashmode_entryvalues));
        }
        /* MODIFIED-END by yuanxing.tan,BUG-2120960*/
        int index = mSettingsManager.getIndexOfCurrentValue(mAppController.getCameraScope(),
                                                            Keys.KEY_FLASH_MODE);
        button.setState(index >= 0 ? index : 0, false, false);

        button.setOnStateChangeListener(new MultiToggleImageButton.OnStateChangeListener() {
            @Override
            public void stateChanged(View view, int state) {
                mSettingsManager.setValueByIndex(mAppController.getCameraScope(),
                        Keys.KEY_FLASH_MODE, state);
                String value = mSettingsManager.getValueByIndex(mAppController.getCameraScope(), Keys.KEY_FLASH_MODE, state);
                if (Keys.isCameraBackFacing(mSettingsManager, SettingsManager.SCOPE_GLOBAL) && !value.equals(mAppController.getAndroidContext().getString(R.string.pref_camera_flashmode_off))) {
                    mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                            Keys.KEY_CAMERA_HDR, false);
                    mSettingsManager.set(mAppController.getCameraScope(),
                            Keys.KEY_CAMERA_LOWLIGHT, false);
                }
                if (cb != null) {
                    cb.onStateChanged(state);
                }
                if (mHelpTipsManager != null) {
                    mHelpTipsManager.notifyEventFinshed();
                }
            }
        });

        button.setOnTouchListener(new MultiToggleImageButton.OnTouchListener() {
            @Override
            public void onTouchDown() {
                mAppController.getLockEventListener().onMenuClicked(button.hashCode());
            }

            @Override
            public void onTouchUp() {
                mAppController.getLockEventListener().onIdle();
            }
        });
        button.setOnUnhandledClickListener(new MultiToggleImageButton.OnUnhandledClickListener() {
            @Override
            public void unhandledClick() {
                if (cb != null && cb instanceof ExtendButtonCallback) {
                    ((ExtendButtonCallback) cb).onUnhandledClick();
                }
            }
        });
    }

    /**
     * Initialize video torch button
     */
    private void initializeTorchButton(final MultiToggleImageButton button,
            final ButtonCallback cb, int resIdImages) {

        if (resIdImages > 0) {
            button.overrideImageIds(resIdImages);
        }
        button.overrideContentDescriptions(R.array.video_flash_descriptions);

        int index = mSettingsManager.getIndexOfCurrentValue(mAppController.getCameraScope(),
                                                            Keys.KEY_VIDEOCAMERA_FLASH_MODE);
        button.setState(index >= 0 ? index : 0, false, false);

        button.setOnStateChangeListener(new MultiToggleImageButton.OnStateChangeListener() {
            @Override
            public void stateChanged(View view, int state) {
                mAppController.getLockEventListener().onIdle();
                mSettingsManager.setValueByIndex(mAppController.getCameraScope(),
                        Keys.KEY_VIDEOCAMERA_FLASH_MODE, state);
                if (cb != null) {
                    cb.onStateChanged(state);
                }
            }
        });

        button.setOnTouchListener(new MultiToggleImageButton.OnTouchListener() {
            @Override
            public void onTouchDown() {
                mAppController.getLockEventListener().onMenuClicked(button.hashCode());
            }

            @Override
            public void onTouchUp() {
                mAppController.getLockEventListener().onIdle();
            }
        });
    }

    /**
     * Initialize hdr plus flash button
     */
    private void initializeHdrPlusFlashButton(final MultiToggleImageButton button,
            final ButtonCallback cb, int resIdImages) {

        if (resIdImages > 0) {
            button.overrideImageIds(resIdImages);
        }
        button.overrideContentDescriptions(R.array.hdr_plus_flash_descriptions);

        int index = mSettingsManager.getIndexOfCurrentValue(mAppController.getModuleScope(),
                                                            Keys.KEY_HDR_PLUS_FLASH_MODE);
        button.setState(index >= 0 ? index : 0, false);

        button.setOnStateChangeListener(new MultiToggleImageButton.OnStateChangeListener() {
            @Override
            public void stateChanged(View view, int state) {

                mAppController.getLockEventListener().onIdle();
                mSettingsManager.setValueByIndex(mAppController.getModuleScope(),
                        Keys.KEY_HDR_PLUS_FLASH_MODE, state);
                if (cb != null) {
                    cb.onStateChanged(state);
                }
            }
        });

        button.setOnTouchListener(new MultiToggleImageButton.OnTouchListener() {
            @Override
            public void onTouchDown() {
                mAppController.getLockEventListener().onMenuClicked(button.hashCode());
            }

            @Override
            public void onTouchUp() {
                mAppController.getLockEventListener().onIdle();
            }
        });
    }

    /**
     * Initialize a camera button.
     */
    private void initializeCameraButton(final MultiToggleImageButton button,
            final ButtonCallback cb, int resIdImages) {

        if (resIdImages > 0) {
            button.overrideImageIds(resIdImages);
        }

        int index = mSettingsManager.getIndexOfCurrentValue(SettingsManager.SCOPE_GLOBAL,
                                                            Keys.KEY_CAMERA_ID);
        button.setState(index >= 0 ? index : 0, false, false);

        button.setOnStateChangeListener(new MultiToggleImageButton.OnStateChangeListener() {
            @Override
            public void stateChanged(View view, int state) {
                mSettingsManager.setValueByIndex(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_CAMERA_ID, state);
                int cameraId = mSettingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_CAMERA_ID);
                // This is a quick fix for ISE in Gcam module which can be
                // found by rapid pressing camera switch button. The assumption
                // here is that each time this button is clicked, the listener
                // will do something and then enable this button again.
                button.setEnabled(false);
                if (cb != null) {
                    cb.onStateChanged(cameraId);
                }

                HelpTipsManager helpTipsManager = mAppController.getHelpTipsManager();
                if(helpTipsManager != null){
                    helpTipsManager.openGestureHelpTip(cameraId);
                }
//                mAppController.getCameraAppUI().onChangeCamera();
            }
        });


        button.setOnTouchListener(new MultiToggleImageButton.OnTouchListener() {
            @Override
            public void onTouchDown() {
                mAppController.getLockEventListener().onMenuClicked(button.hashCode());
            }

            @Override
            public void onTouchUp() {
                mAppController.getLockEventListener().onIdle();
            }
        });
    }

    /**
     * Initialize a switch button.
     */
    private void initializeSwitchButton(final MultiToggleImageButton button,
                                        final ButtonCallback cb, int resIdImages) {

        if (resIdImages > 0) {
            button.overrideImageIds(resIdImages);
        }

        int index = mSettingsManager.getIndexOfCurrentValue(SettingsManager.SCOPE_GLOBAL,
                Keys.KEY_CAMERA_ID);
        button.setState(index >= 0 ? index : 0, false, false);

        button.setOnStateChangeListener(new MultiToggleImageButton.OnStateChangeListener() {
            @Override
            public void stateChanged(View view, int state) {
                mSettingsManager.setValueByIndex(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_CAMERA_ID, state);
                int cameraId = mSettingsManager.getInteger(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_CAMERA_ID);

                mAppController.getLockEventListener().onSwitching();
                mAppController.switchToMode(mAppController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_photo), false);
                // This is a quick fix for ISE in Gcam module which can be
                // found by rapid pressing camera switch button. The assumption
                // here is that each time this button is clicked, the listener
                // will do something and then enable this button again.
                button.setEnabled(false);

                HelpTipsManager helpTipsManager = mAppController.getHelpTipsManager();
                if (helpTipsManager != null) {
                    helpTipsManager.openGestureHelpTip(cameraId);
                }

//                mAppController.getCameraAppUI().onChangeCamera();
            }
        });


        button.setOnTouchListener(new MultiToggleImageButton.OnTouchListener() {
            @Override
            public void onTouchDown() {
                mAppController.getLockEventListener().onMenuClicked(button.hashCode());
            }

            @Override
            public void onTouchUp() {
                mAppController.getLockEventListener().onIdle();
            }
        });
    }

    /**
     * Initialize an hdr plus button.
     */
    private void initializeHdrPlusButton(final MultiToggleImageButton button,
            final ButtonCallback cb, int resIdImages) {
        initializeHdrPlusButtonIcons(button, resIdImages);

        int index = mSettingsManager.getIndexOfCurrentValue(SettingsManager.SCOPE_GLOBAL,
                                                            Keys.KEY_CAMERA_HDR_PLUS);
        button.setState(index >= 0 ? index : 0, false);

        button.setOnStateChangeListener(new MultiToggleImageButton.OnStateChangeListener() {
            @Override
            public void stateChanged(View view, int state) {
                mAppController.getLockEventListener().onIdle();
                mSettingsManager.setValueByIndex(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_CAMERA_HDR_PLUS, state);
                if (cb != null) {
                    cb.onStateChanged(state);
                }
            }
        });

        button.setOnTouchListener(new MultiToggleImageButton.OnTouchListener() {
            @Override
            public void onTouchDown() {
                mAppController.getLockEventListener().onMenuClicked(button.hashCode());
            }

            @Override
            public void onTouchUp() {
                mAppController.getLockEventListener().onIdle();
            }
        });
    }

    private void initializeHdrPlusButtonIcons(MultiToggleImageButton button, int resIdImages) {
        if (resIdImages > 0) {
            button.overrideImageIds(resIdImages);
        }
        button.overrideContentDescriptions(R.array.hdr_plus_descriptions);
    }

    /**
     * Initialize an hdr button.
     */
    private void initializeHdrButton(final MultiToggleImageButton button,
            final ButtonCallback cb, int resIdImages) {
        initializeHdrButtonIcons(button, resIdImages);

        int index = mSettingsManager.getIndexOfCurrentValue(SettingsManager.SCOPE_GLOBAL,
                                                            Keys.KEY_CAMERA_HDR);
        button.setState(index >= 0 ? index : 0, false);

        button.setOnStateChangeListener(new MultiToggleImageButton.OnStateChangeListener() {
            @Override
            public void stateChanged(View view, int state) {
                mAppController.getLockEventListener().onIdle();
                mSettingsManager.setValueByIndex(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_CAMERA_HDR, state);
                String value = mSettingsManager.getValueByIndex(SettingsManager.SCOPE_GLOBAL,
                        Keys.KEY_CAMERA_HDR, state);
                if (SettingsManager.convertToBoolean(value)) {
                    mSettingsManager.set(mAppController.getCameraScope(),
                            Keys.KEY_CAMERA_LOWLIGHT, false);
                    mSettingsManager.set(mAppController.getCameraScope(),
                            Keys.KEY_FLASH_MODE, mAppController.getAndroidContext().getString(R.string.pref_camera_flashmode_off));
                }
                if (cb != null) {
                    cb.onStateChanged(state);
                }
                if (mHelpTipsManager != null) {
                    mHelpTipsManager.notifyEventFinshed();
                }
            }
        });

        button.setOnTouchListener(new MultiToggleImageButton.OnTouchListener() {
            @Override
            public void onTouchDown() {
                mAppController.getLockEventListener().onMenuClicked(button.hashCode());
            }

            @Override
            public void onTouchUp() {
                mAppController.getLockEventListener().onIdle();
            }
        });
    }

    private void initializeHdrButtonIcons(MultiToggleImageButton button, int resIdImages) {
        if (resIdImages > 0) {
            button.overrideImageIds(resIdImages);
        }
        button.overrideContentDescriptions(R.array.hdr_descriptions);
    }

    private void initializeCountdownButton(final MultiToggleImageButton button,
                                           final ButtonCallback cb, int resIdImages, int resIdContentDescriptions) {
        if (resIdContentDescriptions > 0) {
            button.overrideContentDescriptions(resIdContentDescriptions);
        }
        initializeCountdownButton(button, cb, resIdImages);
    }

    /**
     * Initialize a countdown timer button.
     */
    private void initializeCountdownButton(final MultiToggleImageButton button,
            final ButtonCallback cb, int resIdImages) {
        if (resIdImages > 0) {
            button.overrideImageIds(resIdImages);
        }

        if (!CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_VDF_COUNT_TIMER, false)) {
            final int REAR_CAMERA = 0;
            final int[] possibleTimer = mAppController.getAndroidContext().getResources().
                    getIntArray(R.array.pref_countdown_duration);
            // The first possible value of pref_countdown_duration is always 0, so for front camera
            // set the second value as default.
            /* MODIFIED-BEGIN by yuanxing.tan, 2016-04-27,BUG-2001149*/
            int duration = possibleTimer[0];
            if (mAppController.getCurrentCameraId() != REAR_CAMERA) {
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
            mSettingsManager.setDefaults(Keys.KEY_COUNTDOWN_DURATION, duration, possibleTimer);
            /* MODIFIED-END by yuanxing.tan,BUG-2001149*/
        }

        int index = mSettingsManager.getIndexOfCurrentValue(mAppController.getCameraScope(),
                                                            Keys.KEY_COUNTDOWN_DURATION);
        button.setState(index >= 0 ? index : 0, false);

        button.setOnStateChangeListener(new MultiToggleImageButton.OnStateChangeListener() {
            @Override
            public void stateChanged(View view, int state) {
                mAppController.getLockEventListener().onIdle();
                mSettingsManager.setValueByIndex(mAppController.getCameraScope(),
                        Keys.KEY_COUNTDOWN_DURATION, state);
                if (cb != null) {
                    cb.onStateChanged(state);
                }

                if (mHelpTipsManager != null) {
                    mHelpTipsManager.notifyEventFinshed();
                }
            }
        });

        button.setOnTouchListener(new MultiToggleImageButton.OnTouchListener() {
            @Override
            public void onTouchDown() {
                mAppController.getLockEventListener().onMenuClicked(button.hashCode());
            }

            @Override
            public void onTouchUp() {
                mAppController.getLockEventListener().onIdle();
            }
        });
    }
    private void initializeLowlightButton(final MultiToggleImageButton button,
                                           final ButtonCallback cb, int resIdImages) {
        if (resIdImages > 0) {
            button.overrideImageIds(resIdImages);
        }

        int index = mSettingsManager.getIndexOfCurrentValue(mAppController.getCameraScope(),
                Keys.KEY_CAMERA_LOWLIGHT);
        button.setState(index >= 0 ? index : 0, false);

        button.setOnStateChangeListener(new MultiToggleImageButton.OnStateChangeListener() {
            @Override
            public void stateChanged(View view, int state) {
                mAppController.getLockEventListener().onIdle();
                mSettingsManager.setValueByIndex(mAppController.getCameraScope(),
                        Keys.KEY_CAMERA_LOWLIGHT, state);
                String value = mSettingsManager.getValueByIndex(mAppController.getCameraScope(),
                        Keys.KEY_CAMERA_LOWLIGHT, state);
                if (SettingsManager.convertToBoolean(value)) {
                    mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                            Keys.KEY_CAMERA_HDR, mAppController.getAndroidContext().getString(R.string.pref_camera_hdr_off)); // MODIFIED by xuyang.liu, 2016-10-13,BUG-3110198
                    mSettingsManager.set(mAppController.getCameraScope(),
                            Keys.KEY_FLASH_MODE, mAppController.getAndroidContext().getString(R.string.pref_camera_flashmode_off));
                }
                if (cb != null) {
                    cb.onStateChanged(state);
                }

                if (mHelpTipsManager != null) {
                    mHelpTipsManager.notifyEventFinshed();
                }
            }
        });

        button.setOnTouchListener(new MultiToggleImageButton.OnTouchListener() {
            @Override
            public void onTouchDown() {
                mAppController.getLockEventListener().onMenuClicked(button.hashCode());
            }

            @Override
            public void onTouchUp() {
                mAppController.getLockEventListener().onIdle();
            }
        });
    }

    /**
     * Update the visual state of the manual exposure buttons
     */
    public void updateExposureButtons() {
        int compValue = mSettingsManager.getInteger(mAppController.getCameraScope(),
                                                    Keys.KEY_EXPOSURE);
        if (mExposureCompensationStep != 0.0f) {
            int comp = Math.round(compValue * mExposureCompensationStep);
            mModeOptionsExposure.setSelectedOptionByTag(String.valueOf(comp));
        }
    }

    /**
     * Initialize a grid lines button.
     */
    private void initializeGridLinesButton(final MultiToggleImageButton button,
            final ButtonCallback cb, int resIdImages) {

        if (resIdImages > 0) {
            button.overrideImageIds(resIdImages);
        }
        button.overrideContentDescriptions(R.array.grid_lines_descriptions);

        button.setOnStateChangeListener(new MultiToggleImageButton.OnStateChangeListener() {
            @Override
            public void stateChanged(View view, int state) {
                mAppController.getLockEventListener().onIdle();
                mSettingsManager.setValueByIndex(SettingsManager.SCOPE_GLOBAL,
                                                 Keys.KEY_CAMERA_GRID_LINES, state);
                if (cb != null) {
                    cb.onStateChanged(state);
                }
            }
        });

        int index = mSettingsManager.getIndexOfCurrentValue(SettingsManager.SCOPE_GLOBAL,
                                                            Keys.KEY_CAMERA_GRID_LINES);
        button.setState(index >= 0 ? index : 0, true);

        button.setOnTouchListener(new MultiToggleImageButton.OnTouchListener() {
            @Override
            public void onTouchDown() {
                mAppController.getLockEventListener().onMenuClicked(button.hashCode());
            }

            @Override
            public void onTouchUp() {
                mAppController.getLockEventListener().onIdle();
            }
        });
    }

    public boolean isPanoEnabled() {
        return mModeOptions.getMainBar() == ModeOptions.BAR_PANO;
    }

   /**
     * Initialize a panorama orientation buttons.
     */
    public void initializePanoOrientationButtons(final ButtonCallback cb) {
        int resIdImages = PhotoSphereHelper.getPanoramaOrientationOptionArrayId();
        int resIdDescriptions = PhotoSphereHelper.getPanoramaOrientationDescriptions();
        if (resIdImages > 0) {
            TypedArray imageIds = null;
            TypedArray descriptionIds = null;
            try {
                mModeOptions.setMainBar(ModeOptions.BAR_PANO);
                imageIds = mAppController
                    .getAndroidContext().getResources().obtainTypedArray(resIdImages);
                descriptionIds = mAppController
                    .getAndroidContext().getResources().obtainTypedArray(resIdDescriptions);
                mModeOptionsPano.removeAllViews();
                final boolean isHorizontal =
                    (mModeOptionsPano.getOrientation() == LinearLayout.HORIZONTAL);
                final int numImageIds = imageIds.length();
                for (int index = 0; index < numImageIds; index++) {
                    int i;
                    // if in portrait orientation (pano bar horizonal), order buttons normally
                    // if in landscape orientation (pano bar vertical), reverse button order
                    if (isHorizontal) {
                        i = index;
                    } else {
                        i = numImageIds - index - 1;
                    }

                    int imageId = imageIds.getResourceId(i, 0);
                    if (imageId > 0) {
                        ImageButton imageButton = (ImageButton) LayoutInflater
                            .from(mAppController.getAndroidContext())
                            .inflate(R.layout.mode_options_imagebutton_template,
                                     mModeOptionsPano, false);
                        imageButton.setImageResource(imageId);
                        imageButton.setTag(String.valueOf(i));
                        mModeOptionsPano.addView(imageButton);

                        int descriptionId = descriptionIds.getResourceId(i, 0);
                        if (descriptionId > 0) {
                            imageButton.setContentDescription(
                                    mAppController.getAndroidContext().getString(descriptionId));
                        }
                    }
                }
                mModeOptionsPano.updateListeners();
                mModeOptionsPano
                    .setOnOptionClickListener(new RadioOptions.OnOptionClickListener() {
                        @Override
                        public void onOptionClicked(View v) {
                            if (cb != null) {
                                int state = Integer.parseInt((String)v.getTag());
                                mSettingsManager.setValueByIndex(SettingsManager.SCOPE_GLOBAL,
                                                                 Keys.KEY_CAMERA_PANO_ORIENTATION,
                                                                 state);
                                cb.onStateChanged(state);
                            }
                        }
                    });
                updatePanoButtons();
            } finally {
                if (imageIds != null) {
                    imageIds.recycle();
                }
                if (descriptionIds != null) {
                    descriptionIds.recycle();
                }
            }
        }
    }

    private void updatePanoButtons() {
        int modeIndex = mSettingsManager.getIndexOfCurrentValue(SettingsManager.SCOPE_GLOBAL,
                                                                Keys.KEY_CAMERA_PANO_ORIENTATION);
        mModeOptionsPano.setSelectedOptionByTag(String.valueOf(modeIndex));
    }

    public void setHelpTipListener(HelpTipsManager manager) {
        mHelpTipsManager = manager;
    }
}
