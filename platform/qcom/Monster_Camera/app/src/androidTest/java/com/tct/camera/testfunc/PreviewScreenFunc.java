/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.testfunc;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.android.camera.CameraModule;
import com.android.camera.PhotoModule;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.ui.EvoSlider;
import com.android.camera.ui.FocusOverlay;
import com.android.camera.ui.ZoomBar;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.tct.camera.R;
import com.tct.camera.utils.AssertUtil;
import com.tct.camera.utils.CommonUtil;
import com.tct.camera.utils.MainTestUI;


public class PreviewScreenFunc extends Func {
	private static final String TAG = "PreviewScreenFunc";
	public static final int ZOON_BAR = 1;
	public static final int ZOOM_OUT = 2;
	public static final int ZOOM_IN = 3;
	public static final int ZOOM_SEEKER = 4;

	/**
	 * scroll on the screen
	 * @param direction left or right
	 * @param stepCount scroll times
	 */
	public static void changMode(int direction, int stepCount) {
		CommonUtil.onScroll(direction, stepCount);
	}

	public static boolean isPreviewScreenViewVisible(int viewNameIndex) {
		Log.w(TAG, "test: isPreviewScreenViewVisible, viewNameIndex = " + viewNameIndex);
		switch (viewNameIndex) {
			case ZOON_BAR:
				return CommonUtil.isViewVisible(MainTestUI.CommonUIView.getZoomBar());

			case ZOOM_OUT:
				return CommonUtil.isViewVisible(MainTestUI.CommonUIView.getZoomOut());

			case ZOOM_IN:
				return CommonUtil.isViewVisible(MainTestUI.CommonUIView.getZoomIn());

			case ZOOM_SEEKER:
				return CommonUtil.isViewVisible(MainTestUI.CommonUIView.getZoomSeekBar());
		}
		return false;
	}

	public static void onZoomPinch() {
		if (!CommonUtil.isBackCameraFacing() || mContext.getCurrentModuleIndex() == mContext.getResources().getInteger(R.integer.camera_mode_pano)) {
			return;
		}
		CameraModule cameraModule = mContext.getCurrentModule();
		boolean lastSuperResolutionOn = cameraModule instanceof PhotoModule && ((PhotoModule) cameraModule).getCameraSettings().isSuperResolutionOn();
		boolean currentSuperResolutionOn = lastSuperResolutionOn;
		AssertUtil.assertSuperZoomOn(lastSuperResolutionOn, lastSuperResolutionOn);

		RectF previewOverlayBounds = CommonUtil.getValidPreviewScreen();

		for (int i = 0; i < 2; i++) {
			PointF startPoint1 = new PointF(CommonUtil.generateRandomNumber(previewOverlayBounds.left, previewOverlayBounds.right),
		    		CommonUtil.generateRandomNumber(previewOverlayBounds.top, previewOverlayBounds.bottom));
		    PointF startPoint2 = new PointF(CommonUtil.generateRandomNumber(previewOverlayBounds.left, previewOverlayBounds.right),
		    		CommonUtil.generateRandomNumber(previewOverlayBounds.top, previewOverlayBounds.bottom));
		    PointF endPoint1 = new PointF(CommonUtil.generateRandomNumber(previewOverlayBounds.left, previewOverlayBounds.right),
		    		CommonUtil.generateRandomNumber(previewOverlayBounds.top, previewOverlayBounds.bottom));
		    PointF endPoint2 = new PointF(CommonUtil.generateRandomNumber(previewOverlayBounds.left, previewOverlayBounds.right),
		    		CommonUtil.generateRandomNumber(previewOverlayBounds.top, previewOverlayBounds.bottom));

		    int previousDistance = (int) CommonUtil.distanceBetweenFingers(startPoint1, startPoint2);
		    int laterDistance = (int) CommonUtil.distanceBetweenFingers(endPoint1, endPoint2);
		    if (previousDistance == laterDistance) {
				return;
			}

		    mSolo.pinchToZoom(startPoint1, startPoint2, endPoint1, endPoint2);

		    final ZoomBar zoomBar = MainTestUI.CommonUIView.getZoomBar();
		    SeekBar zoomSeekBar = MainTestUI.CommonUIView.getZoomSeekBar();
			boolean isZoomVisible = CommonUtil.isViewVisible(zoomBar) && CommonUtil.isViewVisible(zoomSeekBar);

		    boolean bSupportFrontPinchZoom = CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_SUPPORT_FRONTFACING_PINCH_ZOOM,false);
	        boolean bFrontFacing = !Keys.isCameraBackFacing(mContext.getSettingsManager(), SettingsManager.SCOPE_GLOBAL);
	        boolean isZoomNotSupported = bFrontFacing && !bSupportFrontPinchZoom;

	        AssertUtil.assertTrue("zoom bar visibility is wrong, isZoomNotSupported = " + isZoomNotSupported + ", isZoomVisible = " + isZoomVisible,
					(!isZoomNotSupported && isZoomVisible) || (isZoomNotSupported && !isZoomVisible));

	        if(isZoomVisible) {
	        	currentSuperResolutionOn = cameraModule instanceof PhotoModule && ((PhotoModule) cameraModule).getCameraSettings().isSuperResolutionOn();
	        	AssertUtil.assertSuperZoomOn(currentSuperResolutionOn, lastSuperResolutionOn);
	        	lastSuperResolutionOn = currentSuperResolutionOn;

	        	int previousProgress = zoomSeekBar.getProgress();
	        	CommonUtil.clickLongView(MainTestUI.CommonUIView.getZoomIn(), 1000);
	        	Log.w(TAG, "zoom in, progress is:" + zoomSeekBar.getProgress() + ", previous progress is: " + previousProgress);
	        	AssertUtil.assertTrue("zoom progress should be increasing", zoomSeekBar.getProgress() > previousProgress ||
						(previousProgress == zoomSeekBar.getMax() && zoomSeekBar.getProgress() == previousProgress));
	        	currentSuperResolutionOn = cameraModule instanceof PhotoModule && ((PhotoModule) cameraModule).getCameraSettings().isSuperResolutionOn();
	        	AssertUtil.assertSuperZoomOn(currentSuperResolutionOn, lastSuperResolutionOn);
	        	lastSuperResolutionOn = currentSuperResolutionOn;

				CommonUtil.DragSeekBar(zoomSeekBar);
				currentSuperResolutionOn = cameraModule instanceof PhotoModule && ((PhotoModule) cameraModule).getCameraSettings().isSuperResolutionOn();
				AssertUtil.assertSuperZoomOn(currentSuperResolutionOn, lastSuperResolutionOn);
				lastSuperResolutionOn = currentSuperResolutionOn;

	        	previousProgress = zoomSeekBar.getProgress();
	        	boolean result = CommonUtil.clickLongView(MainTestUI.CommonUIView.getZoomOut(), 1000);
	        	Log.w(TAG, "zoom out, progress is:" + zoomSeekBar.getProgress() + ", previous progress is: " + previousProgress + ", result:" + result);
	        	AssertUtil.assertTrue("zoom progress should be decreasing", zoomSeekBar.getProgress() < previousProgress ||
						(previousProgress == 0 && zoomSeekBar.getProgress() == previousProgress));
	        	currentSuperResolutionOn = cameraModule instanceof PhotoModule && ((PhotoModule) cameraModule).getCameraSettings().isSuperResolutionOn();
	        	AssertUtil.assertSuperZoomOn(currentSuperResolutionOn, lastSuperResolutionOn);
	        	lastSuperResolutionOn = currentSuperResolutionOn;

	        	mInstrumentation.runOnMainSync(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						zoomBar.setVisibility(View.GONE);
					}
				});
				AssertUtil.assertTrue("zoom bar should be invisible", !CommonUtil.isViewVisible(zoomBar));
	        	mSolo.sleep(1000);
	        }
		}
	}

	public static void onAutoFocusTouch() {
		if (!CommonUtil.isBackCameraFacing() ||
				mContext.getCurrentModuleIndex() == mContext.getResources().getInteger(R.integer.camera_mode_pano) ||
				mContext.getCurrentModuleIndex() == mContext.getResources().getInteger(R.integer.camera_mode_manual)) {
			return;
		}
		RectF previewOverlayBounds = CommonUtil.getValidPreviewScreen();

		for (int i = 0; i < 2; i++) {
			Point point = new Point(CommonUtil.generateRandomNumber((int) previewOverlayBounds.left, (int) previewOverlayBounds.right),
		    		CommonUtil.generateRandomNumber((int) previewOverlayBounds.top, (int) previewOverlayBounds.bottom));
			mSolo.clickOnScreen(point.x, point.y);
			mSolo.sleep(1000);
			FocusOverlay focusOverlay = MainTestUI.CommonUIView.getFocusOverlay();
			AssertUtil.assertTrue("focus ui visibility is  wrong, focusOverlayVisible = " + CommonUtil.isViewVisible(focusOverlay) + ", isBackFacing = " + CommonUtil.isBackCameraFacing(),
					CommonUtil.isViewVisible(focusOverlay) && CommonUtil.isBackCameraFacing());

//			EvoSlider evoSlider = MainTestUI.CommonUIView.getEvoSlider();
//			CameraModule cameraModule = mContext.getCurrentModule();
//			boolean exposureSliderNeed = (cameraModule instanceof PhotoModule) && CommonUtil.isBackCameraFacing();
//			AssertUtil.assertTrue("exposure slider visiblity is wrong", (exposureSliderNeed && CommonUtil.isViewVisible(evoSlider)) ||
//																		(!exposureSliderNeed && !CommonUtil.isViewVisible(evoSlider)));
//			if (exposureSliderNeed) {
//
//			    View flashBtn = MainTestUI.CommonUIView.getFlashButton();
//			    AssertUtil.assertTrue("flash btn should be visible", CommonUtil.isViewVisible(flashBtn));
//			    boolean isPreviousFlashEnable = flashBtn.isEnabled();
//			    int previousExposure = mContext.getSettingsManager().getInteger(mContext.getCameraScope(), Keys.KEY_EXPOSURE);
//
//			    PointF startPoint = new PointF(CommonUtil.generateRandomNumber(previewOverlayBounds.left, previewOverlayBounds.right),
//			    		CommonUtil.generateRandomNumber(previewOverlayBounds.top, previewOverlayBounds.bottom));
//			    PointF endPoint = new PointF(CommonUtil.generateRandomNumber(previewOverlayBounds.left, previewOverlayBounds.right),
//			    		CommonUtil.generateRandomNumber(previewOverlayBounds.top, previewOverlayBounds.bottom));
//			    mSolo.drag(startPoint.x, startPoint.x, startPoint.y, endPoint.y, 20);
//			    mSolo.sleep(1000);
//
//			    int currentExposure = mContext.getSettingsManager().getInteger(mContext.getCameraScope(), Keys.KEY_EXPOSURE);
//
//			    AssertUtil.assertTrue("after change exposure, flash button should be disable", !flashBtn.isEnabled());
//			    AssertUtil.assertTrue("exposure value changed unsuccessfully", previousExposure !=currentExposure && CommonUtil.isViewVisible(evoSlider));
//			}
		}
	}
}
