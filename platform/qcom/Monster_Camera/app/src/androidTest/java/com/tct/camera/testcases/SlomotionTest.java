/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.testcases;

import com.android.camera.test.TestUtils;
import com.tct.camera.R;
import com.tct.camera.robotium.Solo;
import com.tct.camera.testfunc.BottomBarFunc;
import com.tct.camera.testfunc.PreviewScreenFunc;
import com.tct.camera.utils.AssertUtil;
import com.tct.camera.utils.CommonUtil;
import com.tct.camera.utils.MainTestUI;

public class SlomotionTest extends VideoTest {

	public SlomotionTest() {
		mCurrentTestCaseIndex = CommonUtil.SLOW_MOTION_TEST;
	}

	@Override
	protected void takePhotoOrVideo(int time) {
		BottomBarFunc.onBottomButtonClick(BottomBarFunc.SHUTTER_BUTTON, time, TestUtils.SLOMOTION_SHUTTER_CLICK_START);
		mSolo.sleep(5000);
		BottomBarFunc.onBottomButtonClick(BottomBarFunc.SHUTTER_BUTTON, time, TestUtils.SLOMOTION_SHUTTER_CLICK_STOP);
	}

	@Override
	public void onShutterButtonClick() {
		takePhotoOrVideo(0);
	}

	@Override
	public void onShutterButtonLongClick() {
		BottomBarFunc.onBottomButtonClick(BottomBarFunc.SHUTTER_BUTTON_LONG, 0, null);
	}

	@Override
	public void checkViewVisibility() {
		MainTestUI.CommonUISpec spec = new MainTestUI.CommonUISpec();
		spec.needSetting = true;
		spec.needFlash = true;
		spec.needHdr = false;
		spec.needTime = false;
		spec.needLight = false;
		spec.needCamera = true;

		spec.needPeekThumb = true;
		spec.needShutterButton = true;
		spec.needVideoShutterButton = false;
		spec.needVideoSnapButton = false;
		spec.needSegmentRemoveButton = false;
		spec.needSegmentRemixButton = false;

		AssertUtil.assertViewVisibility(spec);
	}
}
