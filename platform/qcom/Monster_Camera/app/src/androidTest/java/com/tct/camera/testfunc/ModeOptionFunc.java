/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.testfunc;

import android.graphics.drawable.StateListDrawable;
import android.util.Log;

import com.android.camera.CameraModule;
import com.android.camera.MultiToggleImageButton;
import com.android.camera.PhotoModule;
import com.android.camera.settings.CameraSettingsActivity;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.tct.camera.R;
import com.tct.camera.utils.AssertUtil;
import com.tct.camera.utils.CommonUtil;
import com.tct.camera.utils.IdUtils;
import com.tct.camera.utils.MainTestUI;

public class ModeOptionFunc extends Func {
	private static final String TAG = "ModeOptionFunc";
	public static final int SETTING_MODE = 1;
	public static final int FLASH_MODE = 2;
	public static final int HDR_MODE = 3;
	public static final int TIME_MODE = 4;
	public static final int LIGHT_MODE = 5;
	public static final int CAMERA_MODE = 6;

	public static void onModeOptionClick(int mode) {
		Log.w(TAG, "test: onModeOptionClick, mode = " + mode);
		switch (mode) {
			case SETTING_MODE:
				CommonUtil.changeToSettings();
				mSolo.goBack();
				break;

			case FLASH_MODE:
				if (mContext.getCurrentModuleController().getBottomBarSpec().enableTorchFlash) {
					clickModeOption(MainTestUI.CommonUIView.getFlashButton(), IdUtils.FLASH_BUTTON_ID, Keys.KEY_VIDEOCAMERA_FLASH_MODE, mContext.getCameraScope());
				} else {
					clickModeOption(MainTestUI.CommonUIView.getFlashButton(), IdUtils.FLASH_BUTTON_ID, Keys.KEY_FLASH_MODE, mContext.getCameraScope());
				}
				break;

			case HDR_MODE:
				clickModeOption(MainTestUI.CommonUIView.getHdrButton(), IdUtils.HDR_BUTTON_ID, Keys.KEY_CAMERA_HDR, SettingsManager.SCOPE_GLOBAL);
				break;

			case TIME_MODE:
				clickModeOption(MainTestUI.CommonUIView.getTimeButton(), IdUtils.TIME_BUTTON_ID, Keys.KEY_COUNTDOWN_DURATION, mContext.getCameraScope());
				break;

			case LIGHT_MODE:
				clickModeOption(MainTestUI.CommonUIView.getLightButton(), IdUtils.LIGHT_BUTTON_ID, Keys.KEY_CAMERA_LOWLIGHT, mContext.getCameraScope());
				break;

			case CAMERA_MODE:
				clickModeOption(MainTestUI.CommonUIView.getCameraButton(), IdUtils.CAMERA_SWITCH_BUTTON_ID, Keys.KEY_CAMERA_ID, SettingsManager.SCOPE_GLOBAL);
				break;
		}
	}

	private static void clickModeOption(MultiToggleImageButton toggleImageButton, String id, String keyName, String scope) {
		if (CommonUtil.isViewVisible(toggleImageButton)) {
			int stateBeforeClick = toggleImageButton.getState();
			CommonUtil.clickView(toggleImageButton);
			int settingValueIndex = mContext.getSettingsManager().getIndexOfCurrentValue(scope, keyName);
			assertModeOptionClick(toggleImageButton, settingValueIndex, stateBeforeClick);

			boolean isFlashOff = mContext.getSettingsManager().getString(mContext.getCameraScope(), Keys.KEY_FLASH_MODE).
									equals(mSolo.getString(R.string.pref_camera_flashmode_off));
			boolean isHDROff = Keys.isHdrOff(mContext.getSettingsManager(), mContext); // MODIFIED by xuyang.liu, 2016-10-13,BUG-3110198
			boolean isLightOff = !SettingsManager.convertToBoolean(mContext.getSettingsManager().getString(mContext.getCameraScope(), Keys.KEY_CAMERA_LOWLIGHT));

			CameraModule cameraModule = mContext.getCurrentModule();
			if (CommonUtil.isStringEquals(id, IdUtils.FLASH_BUTTON_ID)) {
				if (!isFlashOff) {
					// also need to compare ui icon
					AssertUtil.assertTrue("Once Flash on, HDR and Low Light should be off", isHDROff && isLightOff);
				}
			} else if (CommonUtil.isStringEquals(id, IdUtils.HDR_BUTTON_ID)) {
				if (!isHDROff) {
					// also need to compare ui icon
					AssertUtil.assertTrue("Once HDR on, Flash and Low Light should be off", isFlashOff && isLightOff);
					boolean isHdrToastEnable = ((PhotoModule) cameraModule).hdrNightToastEnable(true);
					boolean isHdrToastShow = mSolo.searchText(mSolo.getString(R.string.hdr_on_toast), true);
					Log.w(TAG, "isHdrToastEnable: " + isHdrToastEnable + "  isHdrToastShow " + isHdrToastShow);
					AssertUtil.assertTrue("HDR on toast show wrong", (isHdrToastEnable && isHdrToastShow) || (!isHdrToastEnable && !isHdrToastShow));

				}
			} else if (CommonUtil.isStringEquals(id, IdUtils.LIGHT_BUTTON_ID)) {
				if (!isLightOff) {
					// also need to compare ui icon
					AssertUtil.assertTrue("Once Low Light on, Flash and HDR should be off", isFlashOff && isHDROff);
					boolean isNightToastEnable = ((PhotoModule) cameraModule).hdrNightToastEnable(false);
					boolean isNightToastShow = mSolo.searchText(mSolo.getString(R.string.night_mode_on_toast), true);
					Log.w(TAG, "isNightToastEnable: " + isNightToastEnable + "  isNightToastShow " + isNightToastShow);
					AssertUtil.assertTrue("Night on toast show wrong", (isNightToastEnable && isNightToastShow) || (!isNightToastEnable && !isNightToastShow));
				}
			}
		}
	}

	private static void assertModeOptionClick(MultiToggleImageButton toggleImageButton, int currentsettingValue, int stateBeforeClick) {
		if (toggleImageButton == null) {
			return;
		}
		int currentState = toggleImageButton.getState();
		int[] imageIds = toggleImageButton.getImageIds();

		// imageIds is null
		AssertUtil.assertTrue("cannot access button imageIds", imageIds != null);
		// db value vs button value
		AssertUtil.assertTrue("preference value is not same as button", currentsettingValue == currentState);
		// button value change rightly or not
		AssertUtil.assertTrue("After click mode option,its state is wrong", stateBeforeClick == currentState - 1 || ((stateBeforeClick + 1) == imageIds.length && currentState == 0));
		// also need to compare ui icon with res icon
		AssertUtil.assertUIComparedWithResource((StateListDrawable) toggleImageButton.getDrawable(), (int) mContext.getResources().getDimension(R.dimen.mode_option_width),
				(int) mContext.getResources().getDimension(R.dimen.mode_option_height), imageIds[currentState]);
	}

	public static boolean isModeOptionVisible(int mode) {
		Log.w(TAG, "test: isModeOptionVisible, mode = " + mode);
		switch (mode) {
			case SETTING_MODE:
				return CommonUtil.isViewVisible(MainTestUI.CommonUIView.getSettingButton());

			case FLASH_MODE:
				return CommonUtil.isViewVisible(MainTestUI.CommonUIView.getFlashButton());

			case HDR_MODE:
				return CommonUtil.isViewVisible(MainTestUI.CommonUIView.getHdrButton());

			case TIME_MODE:
				return CommonUtil.isViewVisible(MainTestUI.CommonUIView.getTimeButton());

			case LIGHT_MODE:
				return CommonUtil.isViewVisible(MainTestUI.CommonUIView.getLightButton());

			case CAMERA_MODE:
				return CommonUtil.isViewVisible(MainTestUI.CommonUIView.getCameraButton());
		}
		return false;
	}

	public static boolean isAllModeOptionsHide() {
		boolean hasOneVisible = isModeOptionVisible(SETTING_MODE) || isModeOptionVisible(FLASH_MODE) ||
				isModeOptionVisible(HDR_MODE) || isModeOptionVisible(TIME_MODE) || isModeOptionVisible(LIGHT_MODE) ||
				isModeOptionVisible(CAMERA_MODE);

		return !hasOneVisible;
	}
}
