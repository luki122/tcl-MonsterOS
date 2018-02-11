/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.testcases;

import com.android.camera.test.TestUtils;
import com.tct.camera.testfunc.BottomBarFunc;
import com.tct.camera.testfunc.ModeOptionFunc;
import com.tct.camera.testfunc.PreviewScreenFunc;
import com.tct.camera.utils.CommonUtil;
import com.tct.camera.utils.ModuleInterface;

public class CameraTest extends TestCase implements ModuleInterface.CommonModule, ModuleInterface.BottomBarModule,
		ModuleInterface.ModeOptionModule, ModuleInterface.PreviewScreenModule {

	public CameraTest() {
		mCurrentTestCaseIndex = CommonUtil.CAMERA_TEST;
	}

	public void testMainFunctions() {
		// assert each mode UI is right
		checkViewVisibility();
		onModeOptionsClick();
		onBottomButtonsClick();
		onZoomTouch();
		onAutoFocusTouch();
	}

	@Override
	public void onModeOptionsClick() {
		ModeOptionFunc.onModeOptionClick(ModeOptionFunc.SETTING_MODE);
		ModeOptionFunc.onModeOptionClick(ModeOptionFunc.FLASH_MODE);
		ModeOptionFunc.onModeOptionClick(ModeOptionFunc.HDR_MODE);
		ModeOptionFunc.onModeOptionClick(ModeOptionFunc.TIME_MODE);
		ModeOptionFunc.onModeOptionClick(ModeOptionFunc.LIGHT_MODE);
	}

	@Override
	public void onBottomButtonsClick() {
		BottomBarFunc.onBottomButtonClick(BottomBarFunc.PEEK_THUMB, 0, TestUtils.PEEK_THUMB);
		onShutterButtonClick();
		onShutterButtonLongClick();
	}

	@Override
	public void onShutterButtonClick() {}

	@Override
	public void onShutterButtonLongClick() {}

	@Override
	public void checkViewVisibility() {}

	protected void takePhotoOrVideo(int time) {}

	@Override
	public void onZoomTouch() {
		PreviewScreenFunc.onZoomPinch();
	}

	@Override
	public void onAutoFocusTouch() {
		PreviewScreenFunc.onAutoFocusTouch();
	}
}
