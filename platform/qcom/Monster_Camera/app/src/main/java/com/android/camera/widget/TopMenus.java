/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera.widget;

import android.content.Context;
import android.content.res.Resources;
import android.hardware.Camera;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.android.camera.ButtonManager;
import com.android.camera.CameraModule;
import com.android.camera.MultiToggleImageButton;
import com.android.camera.PhotoModule;
import com.android.camera.app.AppController;
import com.android.camera.debug.Log;
import com.android.camera.module.ModuleController;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.ui.LockRotatableButton;
import com.android.camera.ui.Rotatable;
import com.android.camera.ui.RotatableButton;
/* MODIFIED-BEGIN by fei.hui, 2016-10-27,BUG-3201458*/
import com.android.camera.ui.RotateImageView;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.android.camera.util.LockUtils;
import com.android.camera.util.SnackbarToast;
/* MODIFIED-END by fei.hui,BUG-3201458*/
import com.tct.camera.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by JianYing.Zhang on 8/9/16.
 */
public class TopMenus extends RelativeLayout implements SettingsManager.OnSettingChangedListener {
    private final static Log.Tag TAG = new Log.Tag("TopMenus");

    public static final int BUTTON_FLASH = 1;
    public static final int BUTTON_TORCH = 2;
    public static final int BUTTON_HDR_PLUS_FLASH = 3;
    public static final int BUTTON_POSE = 4;
    public static final int BUTTON_FILTER = 5;
    public static final int BUTTON_SETTING = 6;
    public static final int BUTTON_ASPECT_RATIO = 7;

    public static final int TIME_INDICATOR = 8;
    public static final int BUTTON_CONTACTS_FLASH = 9;
    public static final int BUTTON_CONTACTS_BACK = 10;

    public static final int ACTIVE_FILTER = 0x01;
    public static final int INACTIVE_FILTER = 0x02;
    private AppController mAppController;
    /**
     * A reference to the application's settings manager.
     */
    private SettingsManager mSettingsManager;
    private boolean isImageCaptureIntent = false;

    private LockRotatableButton mFlashButton;
    private LockRotatableButton mContactsFlashButton;
    private LockRotatableButton mContactsBackButton;
    private LockRotatableButton mPoseButton;
    private LockRotatableButton mSettingButton;
    private LockRotatableButton mFiltersButton;
    private RotatableButton mTimeIndicator;

    private FrameLayout mButtonGroupLayout;
    private ButtonGroup mButtonGroup;
    /**
     * A listener for button enabled and visibility
     * state changes.
     */
    private TopMenusButtonStatusListener mListener;

    public TopMenus(AppController app) {
        super(app.getAndroidContext(), null);
    }

    public TopMenus(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public TopMenus(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
    }

    public TopMenus(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * ButtonStatusListener provides callbacks for when button's
     * visibility changes and enabled status changes.
     */
    public interface TopMenusButtonStatusListener {
        /**
         * A button's visibility has changed.
         */
        public void onMenusVisibilityChanged(TopMenus topMenus, int buttonId);

        /**
         * A button's enabled state has changed.
         */
        public void onMenusEnabledChanged(TopMenus topMenus, int buttonId);
    }

    /**
     * Sets the ButtonStatusListener.
     */
    public void setListener(TopMenusButtonStatusListener listener) {
        mListener = listener;
    }

    /**
     * Load references to buttons under a root View.
     * Call this after the root clears/reloads all of its children
     * to prevent stale references button views.
     */
    public void load(View root, AppController app, boolean isCaptureIntent) {
        mAppController = app;
        mSettingsManager = app.getSettingsManager();
        mSettingsManager.addListener(this);
        getTopModeOptionReferences(root);
        isImageCaptureIntent = isCaptureIntent;
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-18,BUG-2715761*/
    public void registerOnSharedPreferenceChangeListener() {
        if (mSettingsManager != null) {
            mSettingsManager.addListener(this);
        }
    }

    public void unregisterOnSharedPreferenceChangeListener() {
        if (mSettingsManager != null) {
            mSettingsManager.removeListener(this);
        }
    }
    /* MODIFIED-END by jianying.zhang,BUG-2715761*/

    private void getTopModeOptionReferences(View root) {
        mFlashButton = (LockRotatableButton) root.findViewById(R.id.camera_flash_button);
        mContactsFlashButton = (LockRotatableButton) root.findViewById(R.id.camera_contacts_flash_button);
        mContactsBackButton = (LockRotatableButton) root.findViewById(R.id.camera_contacts_back_button);
        mPoseButton = (LockRotatableButton) root.findViewById(R.id.camera_pose_button);
        mSettingButton = (LockRotatableButton) root.findViewById(R.id.menu_setting_button_new);
        mFiltersButton = (LockRotatableButton) root.findViewById(R.id.camera_filters_button);
        mTimeIndicator = (LockRotatableButton) root.findViewById(R.id.countdown_timers_indicator);

        mButtonGroupLayout = (FrameLayout) root.findViewById(R.id.settings_framelayout);

        mAppController.addRotatableToListenerPool(new Rotatable.RotateEntity(mFlashButton, true));
        mAppController.addRotatableToListenerPool(new Rotatable.RotateEntity(mContactsFlashButton, true));
        mAppController.addRotatableToListenerPool(new Rotatable.RotateEntity(mContactsBackButton, true));
        mAppController.addRotatableToListenerPool(new Rotatable.RotateEntity(mPoseButton, true));
        mAppController.addRotatableToListenerPool(new Rotatable.RotateEntity(mSettingButton, true));
        mAppController.addRotatableToListenerPool(new Rotatable.RotateEntity(mFiltersButton, true));
        mAppController.addRotatableToListenerPool(new Rotatable.RotateEntity(mTimeIndicator, true));
        mAppController.addLockableToListenerPool(mFlashButton);
        mAppController.addLockableToListenerPool(mContactsFlashButton);
        mAppController.addLockableToListenerPool(mContactsBackButton);
        mAppController.addLockableToListenerPool(mPoseButton);
        mAppController.addLockableToListenerPool(mSettingButton);
        mAppController.addLockableToListenerPool(mFiltersButton);
        mAppController.addLockableToListenerPool(mTimeIndicator);
    }

    public void initializeButton(final int buttonType, final Runnable runnable) {
        final RotatableButton mRotatableButton = getButtonOrError(buttonType);
        int index = 0;
        boolean buttonTypeRelateFlash = false;
        if (buttonType == BUTTON_CONTACTS_FLASH || buttonType == BUTTON_FLASH ||
                buttonType == BUTTON_TORCH || buttonType == BUTTON_HDR_PLUS_FLASH) {
            buttonTypeRelateFlash = true;
        }
        switch (buttonType) {
            case BUTTON_CONTACTS_FLASH:
            case BUTTON_FLASH:
                Resources res = mAppController.getAndroidContext().getResources();
                if (mAppController.getCurrentCameraId() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mSettingsManager.setDefaults(
                            Keys.KEY_FLASH_MODE,
                            CustomUtil.getInstance().getString(
                                    CustomFields.DEF_CAMERA_FLASHMODE_FRONT, res.getString(R.string.pref_camera_flashmode_default)),
                            res.getStringArray(R.array.pref_camera_flashmode_entryvalues));
                } else {
                    mSettingsManager.setDefaults(Keys.KEY_FLASH_MODE,
                            res.getString(R.string.pref_camera_flashmode_default),
                            res.getStringArray(R.array.pref_camera_flashmode_entryvalues));
                }
                index = mSettingsManager.getIndexOfCurrentValue(mAppController.getCameraScope(),
                        Keys.KEY_FLASH_MODE);
                break;
            case BUTTON_TORCH:
                index = mSettingsManager.getIndexOfCurrentValue(mAppController.getCameraScope(),
                        Keys.KEY_VIDEOCAMERA_FLASH_MODE);
                break;
            case BUTTON_HDR_PLUS_FLASH:
                index = mSettingsManager.getIndexOfCurrentValue(mAppController.getModuleScope(),
                        Keys.KEY_HDR_PLUS_FLASH_MODE);
                break;
            /* MODIFIED-BEGIN by jianying.zhang, 2016-10-25,BUG-3205846*/
            case BUTTON_FILTER:
                if (mAppController.isFilterModule()) {
                    index = ACTIVE_FILTER;
                } else {
                    index = INACTIVE_FILTER;
                }
                break;
                /* MODIFIED-END by jianying.zhang,BUG-3205846*/
        }

        setButtonImageResource(buttonType, index);
        if (mRotatableButton.getVisibility() == View.GONE) {
            mRotatableButton.setVisibility(View.VISIBLE);
        }
        mRotatableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
        enableButton(buttonType);
        mRotatableButton.setOnTouchListener(new RotatableButton.OnTouchListener() {
            @Override
            public void onTouchDown() {
                mAppController.getLockEventListener().onMenuClicked(mRotatableButton.hashCode());
                Log.d(TAG, "OnTouchListener buttonType : " + buttonType + " onTouchDown");
            }

            @Override
            public void onTouchUp() {
                mAppController.getLockEventListener().onIdle();
                Log.d(TAG, "OnTouchListener buttonType : " + buttonType + " onTouchUp");
            }
        });

        /* MODIFIED-BEGIN by fei.hui, 2016-10-27,BUG-3201458*/
        if (buttonTypeRelateFlash) {
            mRotatableButton.setOnUnhandledClickListener(new RotateImageView.OnUnhandledClickListener() {
                @Override
                public void unhandledClick() {
                    if (mAppController.isBatteryWarningOrLow()) {
                        /* MODIFIED-BEGIN by bin-liu3, 2016-11-09,BUG-3253898*/
                        SnackbarToast.getSnackbarToast().showToast(getContext(),
                                getContext().getString(R.string.battery_info_low_toast_message)
                                ,SnackbarToast.LENGTH_LONG,SnackbarToast.DEFAULT_Y_OFFSET);
                                /* MODIFIED-END by bin-liu3,BUG-3253898*/
                    }
                }
            });
        }
        /* MODIFIED-END by fei.hui,BUG-3201458*/
    }

    public void setButtonImageResource(int typeView, int index) {
        switch (typeView) {
            case BUTTON_CONTACTS_FLASH:
                mContactsFlashButton.setImageResource(
                        mAppController.getAndroidContext().getResources().obtainTypedArray(
                                R.array.camera_flashmode_icons).getResourceId(index, 0));
                mFlashButton.setImageBitmap(null);
                mFlashButton.setVisibility(GONE);
                break;
            case BUTTON_FLASH:
            case BUTTON_HDR_PLUS_FLASH:
                mFlashButton.setImageResource(
                        mAppController.getAndroidContext().getResources().obtainTypedArray(
                                R.array.camera_flashmode_icons).getResourceId(index, 0));
                mContactsFlashButton.setImageBitmap(null);
                mContactsFlashButton.setVisibility(GONE);
                break;
            case BUTTON_TORCH:
                mFlashButton.setImageResource(
                        mAppController.getAndroidContext().getResources().obtainTypedArray(
                                R.array.video_flashmode_icons).getResourceId(index, 0));
                mContactsFlashButton.setImageBitmap(null);
                mContactsFlashButton.setVisibility(GONE);
                break;
            case BUTTON_POSE:

                break;
            case BUTTON_FILTER:
                if (ACTIVE_FILTER == index) {
                    mFiltersButton.setActivated(true);
                } else {
                    mFiltersButton.setActivated(false);
                }
                break;
            case TIME_INDICATOR:
                if (CustomUtil.getInstance().getBoolean(
                        CustomFields.DEF_CAMERA_VDF_COUNT_TIMER, false)) {
                    mTimeIndicator.setImageResource(
                            mAppController.getAndroidContext().getResources().obtainTypedArray(
                                    R.array.vdf_countdown_duration_icons).getResourceId(index, 0));
                } else {
                    mTimeIndicator.setImageResource(
                            mAppController.getAndroidContext().getResources().obtainTypedArray(
                                    R.array.countdown_duration_icons).getResourceId(index, 0));
                }
                break;
        }
    }

    /**
     * Returns the appropriate {@link com.android.camera.MultiToggleImageButton}
     * based on button id.  An IllegalStateException will be throw if the
     * button could not be found in the view hierarchy.
     */
    private RotatableButton getButtonOrError(int buttonId) {
        switch (buttonId) {
            case BUTTON_FLASH:
                if (mFlashButton == null) {
                    throw new IllegalStateException("flash button could not be found.");
                }
                return mFlashButton;
            case BUTTON_TORCH:
                if (mFlashButton == null) {
                    throw new IllegalStateException("flash button could not be found.");
                }
                return mFlashButton;
            case BUTTON_HDR_PLUS_FLASH:
                if (mFlashButton == null) {
                    throw new IllegalStateException("flash button could not be found.");
                }
                return mFlashButton;
            case BUTTON_CONTACTS_FLASH:
                if (mContactsFlashButton == null) {
                    throw new IllegalStateException("contacts flash button could not be found.");
                }
                return mContactsFlashButton;
            case BUTTON_CONTACTS_BACK:
                if (mContactsBackButton == null) {
                    throw new IllegalStateException("contacts back button could not be found.");
                }
                return mContactsBackButton;
            case BUTTON_POSE:
                if (mPoseButton == null) {
                    throw new IllegalStateException("pose button could not be found.");
                }
                return mPoseButton;
            case BUTTON_FILTER:
                if (mFiltersButton == null) {
                    throw new IllegalStateException("filter button could not be found.");
                }
                return mFiltersButton;
            case BUTTON_SETTING:
                if (mSettingButton == null) {
                    throw new IllegalStateException("setting button could not be found.");
                }
                return mSettingButton;
            case TIME_INDICATOR:
                if (mTimeIndicator == null) {
                    throw new IllegalStateException("countdown_timer_indicator imageView could not be found.");
                }
                return mTimeIndicator;
            default:
                throw new IllegalArgumentException("button not known by id=" + buttonId);
        }
    }

    /**
     * Sets a button in its disabled (greyed out) state.
     */
    public void disableButton(int buttonId) {
        RotatableButton button = getButtonOrError(buttonId);

        if (button.isEnabled()) {
            button.setEnabled(false);
            if (mListener != null) {
                mListener.onMenusEnabledChanged(this, buttonId);
            }
        }
        button.setTag(R.string.tag_enabled_id, null);

        if (button.getVisibility() != View.VISIBLE) {
            button.setVisibility(View.VISIBLE);
            if (mListener != null) {
                mListener.onMenusVisibilityChanged(this, buttonId);
            }
        }
    }

    private Map<Integer, LockUtils.Lock> mLockMap = new HashMap<>();

    public int disableButtonWithLock(int buttonId) {
        LockUtils.Lock lock = null;
        if (!mLockMap.containsKey(buttonId)) {
            lock = LockUtils.getInstance().generateMultiLock(LockUtils.LockType.MULTILOCK);
            mLockMap.put(buttonId, lock);
        } else {
            lock = mLockMap.get(buttonId);
        }
        disableButton(buttonId);
        return lock.aquireLock();
    }

    public void enableButtonWithToken(int buttonId, int token) {
        if (!mLockMap.containsKey(buttonId)) {
            enableButton(buttonId);
            return;
        }
        LockUtils.Lock lock = mLockMap.get(buttonId);
        lock.unlockWithToken(token);

        if (!lock.isLocked()) {
            mLockMap.remove(buttonId); // MODIFIED by yuanxing.tan, 2016-06-18,BUG-2202739
            enableButton(buttonId);
        }
    }

    /**
     * Enables a button that has already been initialized.
     */
    public void enableButton(int buttonId) {
        RotatableButton button = getButtonOrError(buttonId);
        if (!button.isEnabled() && !mLockMap.containsKey(buttonId)) { // MODIFIED by yuanxing.tan, 2016-06-18,BUG-2202739
            button.setEnabled(true);
            if (mListener != null) {
                mListener.onMenusEnabledChanged(this, buttonId);
            }
        }
        button.setTag(R.string.tag_enabled_id, buttonId);

        if (button.getVisibility() != View.VISIBLE) {
            button.setVisibility(View.VISIBLE);
            if (mListener != null) {
                mListener.onMenusVisibilityChanged(this, buttonId);
            }
        }
    }

    /**
     * Hide a button by id.
     */
    public void hideButton(int buttonId) {
        final View button = getButtonOrError(buttonId);
        if (button != null) {
            if (button.getVisibility() != View.GONE) {
                if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                    button.setVisibility(View.GONE);
                } else {
                    button.post(new Runnable() {
                        @Override
                        public void run() {
                            button.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }
    }

    /**
     * show a button by id.
     */
    public void showButton(int buttonId) {
        final View button = getButtonOrError(buttonId);
        if (button != null) {
            if (button.getVisibility() != View.VISIBLE) {
                if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                    button.setVisibility(View.VISIBLE);
                } else {
                    button.post(new Runnable() {
                        @Override
                        public void run() {
                            button.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        }
    }

    public void showIndicator(int buttonId) {
        int index = SettingsUtil.getCountDownDuration(mAppController,
                mSettingsManager);
        int photoModeId = mAppController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_photo);
        int filterModeId = mAppController.getAndroidContext().getResources().getInteger(R.integer.camera_mode_filter);
        int currentModule = mAppController.getCurrentModuleIndex();
        Log.d(TAG, "setTopModeOptionVisibility index : " + index + " currentModule : " + currentModule);
        if ((currentModule == photoModeId || currentModule == filterModeId) && index > 0 && !isImageCaptureIntent
                && getVisibility() == VISIBLE) {
            RotatableButton mRotatableButton = getButtonOrError(buttonId);
            mRotatableButton.setVisibility(View.VISIBLE);
            initializeCountdownButton(buttonId);
        } else {
            dismissIndicator();
        }
    }

    /**
     * Initialize a countdown timer button.
     */
    private void initializeCountdownButton(int buttonId) {
        setButtonImageResource(buttonId, SettingsUtil.getCountDownDuration(mAppController,
                mSettingsManager));
    }

    public void dismissIndicator() {
        mTimeIndicator.setImageBitmap(null);
        mTimeIndicator.setVisibility(View.GONE);
    }

    public void setTopModeOptionVisibility(boolean visibility) {
        if (visibility) {
            setVisibility(VISIBLE);
            showIndicator(TIME_INDICATOR);
        } else {
            setVisibility(GONE);
            dismissIndicator();
        }
    }

    public void initializeButtonGroupWithAnimationDirection(final int viewerType, final boolean leftToRight) {
        if (buttonGroupBarVisible()) {
            if (leftToRight) {
                dismissButtonGroupBar(true, ButtonGroup.OUT_LEFT);
            } else {
                dismissButtonGroupBar(true, ButtonGroup.OUT_RIGHT);
            }
            return;
        }
        if (mButtonGroupLayout != null) {
            mButtonGroupLayout.setVisibility(VISIBLE);
//             Creates refocus SettingFrameLayout.
            LayoutInflater inflater = (LayoutInflater) mAppController.getAndroidContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mButtonGroup = (ButtonGroup) inflater
                    .inflate(R.layout.button_group, null, false);
            mButtonGroup.load(mAppController);
            mButtonGroup.initializeLayout(viewerType, null, new ButtonItemCallback() {
                @Override
                public void onItemClicked() {
                    if (leftToRight) {
                        dismissButtonGroupBar(true, ButtonGroup.OUT_LEFT);
                    } else {
                        dismissButtonGroupBar(true, ButtonGroup.OUT_RIGHT);
                    }
                }
            });
            if (leftToRight) {
                mButtonGroup.startAnimation(ButtonGroup.IN_LEFT);
            } else {
                mButtonGroup.startAnimation(ButtonGroup.IN_RIGHT);
            }
//             Adds SettingFrameLayout into view hierarchy.
            mButtonGroupLayout.addView(mButtonGroup, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            setTopModeOptionVisibility(false);
        }
    }

    @Override
    public void onSettingChanged(SettingsManager settingsManager, String key) {
        int index = 0;
        int viewType = -1;
        if (key.equals(Keys.KEY_FLASH_MODE)) {
            index = mSettingsManager.getIndexOfCurrentValue(mAppController.getCameraScope(),
                    Keys.KEY_FLASH_MODE);
            if (mContactsFlashButton != null && mContactsFlashButton.getVisibility() == VISIBLE) {
                viewType = BUTTON_CONTACTS_FLASH;
            } else {
                viewType = BUTTON_FLASH;
            }
        } else if (key.equals(Keys.KEY_VIDEOCAMERA_FLASH_MODE)) {
            index = mSettingsManager.getIndexOfCurrentValue(mAppController.getCameraScope(),
                    Keys.KEY_VIDEOCAMERA_FLASH_MODE);
            viewType = BUTTON_TORCH;
        } else if (key.equals(Keys.KEY_HDR_PLUS_FLASH_MODE)) {
            index = mSettingsManager.getIndexOfCurrentValue(mAppController.getCameraScope(),
                    Keys.KEY_HDR_PLUS_FLASH_MODE);
            viewType = BUTTON_HDR_PLUS_FLASH;
        } else if (key.equals(Keys.KEY_COUNTDOWN_DURATION)) {
            viewType = TIME_INDICATOR;
            index = SettingsUtil.getCountDownDuration(mAppController,
                    mSettingsManager);
            if (index > 0) {
                showIndicator(viewType);
            } else {
                dismissIndicator();
            }
        }
        Log.d(TAG, "onSettingChanged key : " + key);
        setButtonImageResource(viewType, index);
    }

    public interface ButtonItemCallback {
        void onItemClicked();
    }

    public void dismissButtonGroupBar(boolean needAnimation, int animationType) {
        if (mButtonGroup == null) {
            // No mButtomGroup is created for the specific viewer type.
            return;
        }
        if (needAnimation) {
            mButtonGroup.startAnimation(animationType, new ButtonGroup.AnimationEndCallBack() {
                @Override
                public void onAnimationEnd() {
                    mButtonGroup.setVisibility(View.GONE);
                    mButtonGroup = null;
                    mButtonGroupLayout.setVisibility(GONE);
                    setTopModeOptionVisibility(true);
                }
            });
        } else {
            mButtonGroup.setVisibility(View.GONE);
            mButtonGroup = null;
            mButtonGroupLayout.setVisibility(GONE);
            setTopModeOptionVisibility(true);
        }
    }

    public boolean buttonGroupBarVisible() {
        if (mButtonGroup != null
                && mButtonGroup.getVisibility() == View.VISIBLE) {
            return true;
        }
        return false;
    }

    /**
     * Check if a button is enabled with the given button id..
     */
    public boolean isButtonEnabled(int buttonId) {
        View button;
        try {
            button = getButtonOrError(buttonId);
        } catch (IllegalArgumentException e) {
            return false;
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
    public boolean isButtonVisible(int buttonId) {
        View button;
        try {
            button = getButtonOrError(buttonId);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return (button.getVisibility() == View.VISIBLE);
    }

    @Override
    public void removeAllViews() {
        super.removeAllViews();
        mAppController.removeRotatableFromListenerPool(mFlashButton.hashCode());
        mAppController.removeRotatableFromListenerPool(mContactsFlashButton.hashCode());
        mAppController.removeRotatableFromListenerPool(mContactsBackButton.hashCode());
        mAppController.removeRotatableFromListenerPool(mPoseButton.hashCode());
        mAppController.removeRotatableFromListenerPool(mSettingButton.hashCode());
        mAppController.removeRotatableFromListenerPool(mFiltersButton.hashCode());
        mAppController.removeRotatableFromListenerPool(mTimeIndicator.hashCode());
    }


    /**
     * Disable click reactions for a button without affecting visual state.
     * For most cases you'll want to use {@link #disableButton(int)}.
     *
     * @param buttonId The id of the button.
     */
    public void disableButtonClick(int buttonId) {
        RotatableButton button = getButtonOrError(buttonId);
        if (button instanceof RotatableButton) {
            ((RotatableButton) button).setEnabled(false);
        }
    }

    /**
     * Enable click reactions for a button without affecting visual state.
     * For most cases you'll want to use {@link #enableButton(int)}.
     *
     * @param buttonId The id of the button.
     */
    public void enableButtonClick(int buttonId) {
        RotatableButton button = getButtonOrError(buttonId);
        if (button instanceof RotatableButton) {
            ((RotatableButton) button).setEnabled(true);
        }
    }
}
