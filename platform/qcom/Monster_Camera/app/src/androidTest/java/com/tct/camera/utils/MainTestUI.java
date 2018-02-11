/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.utils;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.android.camera.MultiToggleImageButton;
import com.android.camera.ShutterButton;
import com.android.camera.ui.EvoSlider;
import com.android.camera.ui.FocusOverlay;
import com.android.camera.ui.PeekImageView;
import com.android.camera.ui.RotatableButton;
import com.android.camera.ui.StereoModeStripView;
import com.android.camera.ui.StereoScrollIndicatorView;
import com.android.camera.ui.ZoomBar;
import com.android.camera.widget.ModeOptionsOverlay;
import com.tct.camera.utils.CommonUtil;
import com.tct.camera.utils.IdUtils;

public class MainTestUI {

	public static class CommonUISpec {
		public boolean needSetting;

		public boolean needFlash;

		public boolean needHdr;

		public boolean needTime;

		public boolean needLight;

		public boolean needCamera;

		public boolean needPeekThumb;

		public boolean needShutterButton;

		public boolean needVideoShutterButton;

		public boolean needVideoSnapButton;

		public boolean needSegmentRemoveButton;

		public boolean needSegmentRemixButton;

	}

	public static class CommonUIView {
		public static void getCommonUIView() {
			getModeOptionOvelay();
			getSettingButton();
			getFlashButton();
			getHdrButton();
			getTimeButton();
			getLightButton();
			getCameraButton();
			getShutterButton();
			getShutterCancelButton();
			getPeekImageView();
			getZoomBar();
			getZoomIn();
			getZoomOut();
			getZoomSeekBar();
			getFocusOverlay();
			getEvoSlider();
			getModScrollIndicator();
			getModeStripView();
			getStereoGroup();
		}

		private static ModeOptionsOverlay mModeOptionsOverlay;
		public static ModeOptionsOverlay getModeOptionOvelay() {
			if (mModeOptionsOverlay == null) {
				mModeOptionsOverlay = (ModeOptionsOverlay) CommonUtil.getViewById(IdUtils.MODE_OPTION_OVERLAY_ID);
			}
			return mModeOptionsOverlay;
		}

		private static RotatableButton mSettingButton;
		public static RotatableButton getSettingButton() {
			if (mSettingButton == null) {
				mSettingButton = (RotatableButton) CommonUtil.getViewById(IdUtils.SETTING_BUTTON_ID);
			}
			return mSettingButton;
		}

		private static MultiToggleImageButton mFlashButton;
		public static MultiToggleImageButton getFlashButton() {
			if (mFlashButton == null) {
				mFlashButton = (MultiToggleImageButton) CommonUtil.getViewById(IdUtils.FLASH_BUTTON_ID);
			}
			return mFlashButton;
		}

		private static MultiToggleImageButton mHdrButton;
		public static MultiToggleImageButton getHdrButton() {
			if (mHdrButton == null) {
				mHdrButton = (MultiToggleImageButton) CommonUtil.getViewById(IdUtils.HDR_BUTTON_ID);
			}
			return mHdrButton;
		}

		private static MultiToggleImageButton mTimeButton;
		public static MultiToggleImageButton getTimeButton() {
			if (mTimeButton == null) {
				mTimeButton = (MultiToggleImageButton) CommonUtil.getViewById(IdUtils.TIME_BUTTON_ID);
			}
			return mTimeButton;
		}

		private static MultiToggleImageButton mLightButton;
		public static MultiToggleImageButton getLightButton() {
			if (mLightButton == null) {
				mLightButton = (MultiToggleImageButton) CommonUtil.getViewById(IdUtils.LIGHT_BUTTON_ID);
			}
			return mLightButton;
		}

		private static MultiToggleImageButton mCameraButton;
		public static MultiToggleImageButton getCameraButton() {
			if (mCameraButton == null) {
				mCameraButton = (MultiToggleImageButton) CommonUtil.getViewById(IdUtils.CAMERA_SWITCH_BUTTON_ID);
			}
			return mCameraButton;
		}

		private static ShutterButton mShutterButton;
		public static ShutterButton getShutterButton() {
			if (mShutterButton == null) {
				mShutterButton = (ShutterButton) CommonUtil.getViewById(IdUtils.SHUTTER_BUTTON_ID);
			}
			return mShutterButton;
		}

		private static RotatableButton mShutterCancelButton;
		public static RotatableButton getShutterCancelButton() {
			if (mShutterCancelButton == null) {
				mShutterCancelButton = (RotatableButton) CommonUtil.getViewById(IdUtils.SHUTTER_CANCEL_BUTTON_ID);
			}
			return mShutterCancelButton;
		}

		private static PeekImageView mPeekImageView;
		public static PeekImageView getPeekImageView() {
			if (mPeekImageView == null) {
				mPeekImageView = (PeekImageView) CommonUtil.getViewById(IdUtils.PEEK_THUMB_BUTTON_ID);
			}
			return mPeekImageView;
		}

		private static ZoomBar mZoomBar;
		public static ZoomBar getZoomBar() {
			if (mZoomBar == null) {
				mZoomBar = (ZoomBar) CommonUtil.getViewById(IdUtils.ZOOM_BAR);
			}
			return mZoomBar;
		}

		private static ImageView mZoomIn;
		public static ImageView getZoomIn() {
			if (mZoomIn == null) {
				mZoomIn = (ImageView) CommonUtil.getViewById(IdUtils.ZOOM_IN_ID);
			}
			return mZoomIn;
		}

		private static ImageView mZoomOut;
		public static ImageView getZoomOut() {
			if (mZoomOut == null) {
				mZoomOut = (ImageView) CommonUtil.getViewById(IdUtils.ZOOM_OUT_ID);
			}
			return mZoomOut;
		}

		private static SeekBar mZoomSeekBar;
		public static SeekBar getZoomSeekBar() {
			if (mZoomSeekBar == null) {
				mZoomSeekBar = (SeekBar) CommonUtil.getViewById(IdUtils.ZOOM_SEEK_ID);
			}
			return mZoomSeekBar;
		}

		private static FocusOverlay mFocusOverlay;
		public static FocusOverlay getFocusOverlay() {
			if (mFocusOverlay == null) {
				mFocusOverlay = (FocusOverlay) CommonUtil.getViewById(IdUtils.FOCUS_OVERLAY_ID);
			}
			return mFocusOverlay;
		}

		private static EvoSlider mEvoSlider;
		public static EvoSlider getEvoSlider() {
			if (mEvoSlider == null) {
				mEvoSlider = (EvoSlider) CommonUtil.getViewById(IdUtils.EXPOSURE_SLIDER_ID);
			}
			return mEvoSlider;
		}

		private static StereoScrollIndicatorView mModeScrollIndicator;
		public static StereoScrollIndicatorView getModScrollIndicator() {
			if (mModeScrollIndicator == null) {
				mModeScrollIndicator = (StereoScrollIndicatorView) CommonUtil.getViewById(IdUtils.MODE_SCROLL_INDICATOR_ID);
			}
			return mModeScrollIndicator;
		}

		private static StereoModeStripView mModeStripView;
		public static StereoModeStripView getModeStripView() {
			if (mModeStripView == null) {
				mModeStripView = (StereoModeStripView) CommonUtil.getViewById(IdUtils.MODE_STRIP_VIEW_ID);
			}
			return mModeStripView;
		}

		private static RelativeLayout mStereoGroup;
		public static RelativeLayout getStereoGroup() {
			if (mStereoGroup == null) {
				mStereoGroup = (RelativeLayout) CommonUtil.getViewById(IdUtils.STEREO_GROUP_ID);
			}
			return mStereoGroup;
		}
	}
}
