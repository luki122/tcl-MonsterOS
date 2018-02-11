/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.camera.app.AppController;
import com.android.camera.debug.Log;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.settings.SettingsUtil;
import com.android.camera.util.CameraUtil;
import com.android.ex.camera2.portability.Size;
import com.tct.camera.R;

import java.util.List;

/**
 * Created by JianYing.Zhang on 8/10/16.
 * <p>
 * Use to set the specific content,eg Flash (on/off/auto) and preview size (1:1/4:3/16:9)
 */
public class ButtonGroup extends LinearLayout {
    private static final String BUTTON_GROUP_STRING_ID = "ButtonGroup";
    private static final Log.Tag TAG = new Log.Tag(BUTTON_GROUP_STRING_ID);

    public static final int IN_RIGHT = 1;
    public static final int OUT_LEFT = 2;
    public static final int IN_LEFT = 3;
    public static final int OUT_RIGHT = 4;
    public static final int BOTTOM_TO_TOP = 5;
    public static final int TOP_TO_BOTTOM = 6;

    private static final float BUTTON_GROUP_TEXT_SIZE = 12; // MODIFIED by jianying.zhang, 2016-10-26,BUG-3229970
    private AppController mAppController;
    private SettingsManager mSettingsManager;

    public ButtonGroup(Context context) {
        super(context, null);
    }

    public ButtonGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void load(AppController appController) {
        mAppController = appController;
        mSettingsManager = mAppController.getSettingsManager();
    }

    /**
     * @param viewType
     * @param cb
     * @param bcb
     */
    public void initializeLayout(final int viewType, final ButtonCallback cb, final TopMenus.ButtonItemCallback bcb) {
        if (getVisibility() == GONE) {
            setVisibility(VISIBLE);
        }
        removeAllViews();
        final SettingLayoutItemViewValues mSettingLayoutItemViewValues =
                getResourceByType(getContext(), viewType);
        String[] ids = mSettingLayoutItemViewValues.getTextValues();
        if (mSettingLayoutItemViewValues == null || ids == null || ids.length == 0) {
            Log.d(TAG, "mSettingLayoutItemViewValues is null");
            return;
        }
        String currentSelected = null;
        setWeightSum(ids.length);
        LinearLayout.LayoutParams ps = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT);
        ps.gravity = Gravity.CENTER_VERTICAL;
        ps.weight = 1;
        if (viewType == TopMenus.BUTTON_FLASH
                || viewType == TopMenus.BUTTON_CONTACTS_FLASH
                || viewType == TopMenus.BUTTON_HDR_PLUS_FLASH) {
            currentSelected = mAppController.getSettingsManager()
                    .getString(mAppController.getCameraScope(), Keys.KEY_FLASH_MODE);
        }
        if (viewType == TopMenus.BUTTON_TORCH) {
            currentSelected = mSettingsManager.getString(mAppController.getCameraScope(),
                    Keys.KEY_VIDEOCAMERA_FLASH_MODE);
        }
        for (int i = 0; i < ids.length; i++) {
            TextView mTextView = new TextView(mAppController.getAndroidContext());
            if (viewType == TopMenus.BUTTON_ASPECT_RATIO) {
                currentSelected = mCurrentPreviewSize;
            }
            setTextViewValues(mTextView, currentSelected, i, mSettingLayoutItemViewValues);
            mTextView.setTextSize(BUTTON_GROUP_TEXT_SIZE); // MODIFIED by jianying.zhang, 2016-10-26,BUG-3229970
            mTextView.setLayoutParams(ps);
            final int selectedId = i;
            mTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Ignore the click during the animation.
                    if (mAnimationRunning) {
                        return;
                    }

                    switch (viewType) {
                        case TopMenus.BUTTON_CONTACTS_FLASH:
                        case TopMenus.BUTTON_FLASH:
                            mSettingsManager.setValueByIndex(mAppController.getCameraScope(),
                                    Keys.KEY_FLASH_MODE, selectedId);
                            String value = mSettingsManager.getValueByIndex(mAppController
                                    .getCameraScope(), Keys.KEY_FLASH_MODE, selectedId);
                            if (Keys.isCameraBackFacing(mSettingsManager,
                                    SettingsManager.SCOPE_GLOBAL)) {
                                if (value.equals(mAppController.getAndroidContext()
                                        .getString(R.string.pref_camera_flashmode_on))) {
                                    //flash on
                                    Keys.switchHdr(mSettingsManager, false);
                                    mSettingsManager.set(mAppController.getCameraScope(),
                                            Keys.KEY_CAMERA_LOWLIGHT, false);
                                } else if (value.equals(mAppController.getAndroidContext()
                                        .getString(R.string.pref_camera_flashmode_auto))) {
                                    //flash auto
                                    mSettingsManager.set(mAppController.getCameraScope(),
                                            Keys.KEY_CAMERA_LOWLIGHT, false);
                                } else {
                                    //flash off
                                }
                            }
                            break;
                        case TopMenus.BUTTON_TORCH:
                            mSettingsManager.setValueByIndex(mAppController.getCameraScope(),
                                    Keys.KEY_VIDEOCAMERA_FLASH_MODE, selectedId);
                            break;
                        case TopMenus.BUTTON_HDR_PLUS_FLASH:
                            mSettingsManager.setValueByIndex(mAppController.getModuleScope(),
                                    Keys.KEY_HDR_PLUS_FLASH_MODE, selectedId);
                            break;
                        case TopMenus.BUTTON_ASPECT_RATIO:
                            if (mCurrentPreviewSize.equals(mSettingLayoutItemViewValues
                                    .getEntryValues(selectedId))) {
                                break; // MODIFIED by xuan.zhou, 2016-11-03,BUG-3311864
                            }
                            String pictureSizeKey = isCameraFrontFacing() ?
                                    Keys.KEY_PICTURE_SIZE_FRONT
                                    : Keys.KEY_PICTURE_SIZE_BACK;
                            mSettingsManager.set(SettingsManager.SCOPE_GLOBAL,
                                    pictureSizeKey, mSettingLayoutItemViewValues
                                            .getEntryValues(selectedId));
                            break;
                    }
                    if (cb != null) {
                        cb.onStateChanged();
                    }
                    if (bcb != null) {
                        bcb.onItemClicked();
                    }
                }
            });
            addView(mTextView, ps);
        }
    }

    private void setTextViewValues(TextView textView, String currentSelected, int index,
                                   SettingLayoutItemViewValues mSettingLayoutItemViewValues) {
        textView.setContentDescription(mSettingLayoutItemViewValues.getDescriptionValues(index));
        textView.setAllCaps(true);
        textView.setText(mSettingLayoutItemViewValues.getTextValues(index));
        textView.setGravity(Gravity.CENTER);
        if (currentSelected != null &&
                mSettingLayoutItemViewValues.getEntryValues(index).equals(currentSelected)) {
            textView.setTextColor(mAppController.getAndroidContext()
                    .getResources().getColor(R.color.setting_layout_selectedcolor));
        } else {
            textView.setTextColor(mAppController.getAndroidContext()
                    .getResources().getColor(R.color.setting_layout_unselectedcolor));
        }
        textView.setTag(mSettingLayoutItemViewValues.getEntryValues(index));
    }

    private SettingLayoutItemViewValues getResourceByType(Context context, int viewType) {
        switch (viewType) {
            case TopMenus.BUTTON_CONTACTS_FLASH:
            case TopMenus.BUTTON_FLASH:
            case TopMenus.BUTTON_HDR_PLUS_FLASH:
                return new SettingLayoutItemViewValues(
                        context.getResources().getStringArray(R.array.camera_flash_descriptions),
                        context.getResources().getStringArray(R.array.pref_camera_flashmode_entryvalues),
                        context.getResources().getStringArray(R.array.pref_camera_flashmode_entries));
            case TopMenus.BUTTON_TORCH:
                return new SettingLayoutItemViewValues(
                        context.getResources().getStringArray(R.array.video_flash_descriptions),
                        context.getResources().getStringArray(R.array.pref_camera_video_flashmode_entryvalues),
                        context.getResources().getStringArray(R.array.pref_camera_video_flashmode_entries));
            case TopMenus.BUTTON_ASPECT_RATIO:
                return new SettingLayoutItemViewValues(mSupportPreviewSize,
                        mSupportPreViewEntrySize, mSupportPreviewSize);

            default:
                Log.d(TAG, "viewType " + viewType + " not found ");
                return null;
        }
    }

    private static class SettingLayoutItemViewValues {
        private String[] mDescriptionValues;
        private String[] mEntryValues;
        private String[] mTextValues;

        public SettingLayoutItemViewValues(String[] mDescriptionValues,
                                           String[] mEntryValues, String[] mTextValues) {
            this.mDescriptionValues = mDescriptionValues;
            this.mEntryValues = mEntryValues;
            this.mTextValues = mTextValues;
        }

        public String[] getTextValues() {
            return mTextValues;
        }

        public String getDescriptionValues(int index) {
            if (index < 0 || index >= mDescriptionValues.length) {
                return null;
            }
            return mDescriptionValues[index];
        }

        public String getEntryValues(int index) {
            if (index < 0 || index >= mEntryValues.length) {
                return null;
            }
            return mEntryValues[index];
        }

        public String getTextValues(int index) {
            if (index < 0 || index >= mTextValues.length) {
                return null;
            }
            return mTextValues[index];
        }
    }

    public interface ButtonCallback {
        void onStateChanged();
    }

    private boolean mAnimationRunning;

    public void startAnimation(int viewType) {
        startAnimation(viewType, null);
    }

    public void startAnimation(int viewType, final AnimationEndCallBack cb) {
        Animation animation = null;
        int animId = -1;
        switch (viewType) {
            case IN_RIGHT: {
                animId = R.anim.setting_dialog_in_right;
                break;
            }
            case OUT_LEFT: {
                animId = R.anim.setting_dialog_out_left;
                break;
            }
            case IN_LEFT: {
                animId = R.anim.setting_dialog_in_left;
                break;
            }
            case OUT_RIGHT: {
                animId = R.anim.setting_dialog_out_right;
                break;
            }
            case BOTTOM_TO_TOP: {
                animId = R.anim.setting_dialog_enter_bottom_to_top;
                break;
            }
            case TOP_TO_BOTTOM: {
                animId = R.anim.setting_dialog_exit_top_to_bottom;
                break;
            }
        }

        animation = AnimationUtils.loadAnimation(mAppController.getAndroidContext(), animId);
        if (animation == null) {
            return;
        }
        startAnimation(animation);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mAnimationRunning = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mAnimationRunning = false;
                if (cb != null) {
                    cb.onAnimationEnd();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public interface AnimationEndCallBack {
        void onAnimationEnd();
    }

    private int mCameraId;
    private String mCurrentPreviewSize;
    private String[] mSupportPreviewSize;
    private String[] mSupportPreViewEntrySize;

    public void initCameraPreference(List<Size> sizes) {
        mCameraId = mSettingsManager.getInteger(SettingsManager.SCOPE_GLOBAL, Keys.KEY_CAMERA_ID);
        List<Size> previewSizes = SettingsUtil.getPictureSizeAccordingToRatio(sizes); // MODIFIED by jianying.zhang, 2016-10-18,BUG-3140939
        // Current preview size.
        Size optimalSize = CameraUtil.getOptimalPreviewSize(mAppController.getAndroidContext(),
                previewSizes, SettingsUtil
                        .getCachedPictureSize(mSettingsManager, isCameraFrontFacing()));
        mCurrentPreviewSize = SettingsUtil.getSizeEntryString(optimalSize);
        mSupportPreviewSize = new String[previewSizes.size()];
        mSupportPreViewEntrySize = new String[previewSizes.size()];
        for (int i = 0; i < previewSizes.size(); i++) {
            mSupportPreviewSize[i] = SettingsUtil.getSizeSummaryString(mAppController
                    .getAndroidContext(), previewSizes.get(i));
            mSupportPreViewEntrySize[i] = SettingsUtil.getSizeEntryString(previewSizes.get(i));
        }
    }

    /**
     * @return Whether the currently active camera is front-facing.
     */
    protected boolean isCameraFrontFacing() {
        return mAppController.getCameraProvider().getCharacteristics(mCameraId)
                .isFacingFront();
    }

}
