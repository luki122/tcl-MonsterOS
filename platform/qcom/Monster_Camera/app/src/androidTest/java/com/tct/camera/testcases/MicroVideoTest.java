/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.testcases;

import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.android.camera.MicroVideoModule;
import com.android.camera.settings.Keys;
import com.android.camera.test.TestUtils;
import com.tct.camera.R;
import com.tct.camera.testfunc.BottomBarFunc;
import com.tct.camera.testfunc.ModeOptionFunc;
import com.tct.camera.utils.AssertUtil;
import com.tct.camera.utils.CommonUtil;
import com.tct.camera.utils.IdUtils;
import com.tct.camera.utils.MainTestUI;

public class MicroVideoTest extends VideoTest {

	private static final String TAG = "AutoTest";

	public MicroVideoTest() {
		mCurrentTestCaseIndex = CommonUtil.MICRO_TEST;
	}

	// first run, before than testMainFunctions()
	public void testFirstMicroGuide() {
		Log.w(TAG, "test: testFirstMicroGuide()");
		View microGuideLayout = CommonUtil.getViewById(IdUtils.MICRO_GUIDE_LAYOUT);
		if (Keys.isShowMicroGuide(mContext.getSettingsManager()) &&
				Keys.isNewLaunchingForMicroguide(mContext.getSettingsManager())) {
			AssertUtil.assertTrue("micro guide layout should be visible", CommonUtil.isViewVisible(microGuideLayout));

			CheckBox microGuideCheckBox = (CheckBox) CommonUtil.getViewById(IdUtils.MICRO_GUIDE_CHECK_BOX);
			AssertUtil.assertTrue("micro guide check box should be visible", CommonUtil.isViewVisible(microGuideCheckBox));
			AssertUtil.assertTrue("micro guide button should be visible", mSolo.searchText(mSolo.getString(R.string.micro_video_guide_button), true));

			CommonUtil.clickView(microGuideCheckBox);
			CommonUtil.clickById(IdUtils.MICRO_GUIDE_BUTTON_ID);
			AssertUtil.assertTrue("micro guide check box should be checked", microGuideCheckBox.isChecked());
			AssertUtil.assertTrue("after click button, guide layout should not be visible", !CommonUtil.isViewVisible(microGuideLayout) &&
					!Keys.isShowMicroGuide(mContext.getSettingsManager()));
		} else {
			AssertUtil.assertTrue("micro guide layout should not be visible", !CommonUtil.isViewVisible(microGuideLayout));
		}
	}

	@Override
	protected void takePhotoOrVideo(int time) {
		BottomBarFunc.onBottomButtonClick(BottomBarFunc.SHUTTER_BUTTON_LONG, time, TestUtils.MICRO_SHUTTER_LONG_CLICK);
		AssertUtil.assertTrue("segment remix button should be visible", CommonUtil.isViewVisible(IdUtils.SEGMENT_REMIX_BUTTON_ID));
		AssertUtil.assertTrue("segment remove button should be visible", CommonUtil.isViewVisible(IdUtils.SEGMENT_REMOVE_BUTTON_ID));
		AssertUtil.assertTrue("settings button should be invisible", !ModeOptionFunc.isModeOptionVisible(ModeOptionFunc.SETTING_MODE));
		AssertUtil.assertTrue("camera switch button should be invisible", !ModeOptionFunc.isModeOptionVisible(ModeOptionFunc.CAMERA_MODE));
		AssertUtil.assertTrue("flash button should be invisible", ModeOptionFunc.isModeOptionVisible(ModeOptionFunc.FLASH_MODE));
		AssertUtil.assertTrue("mode scroll indicator should be hidden", !CommonUtil.isViewVisible(MainTestUI.CommonUIView.getModScrollIndicator()));
		AssertUtil.assertTrue("mode strip view should be hidden", !CommonUtil.isViewVisible(MainTestUI.CommonUIView.getModeStripView()));
	}

	@Override
	public void onShutterButtonClick() {
		BottomBarFunc.onBottomButtonClick(BottomBarFunc.SHUTTER_BUTTON, 0, null);
		AssertUtil.assertTrue("click micro shutter just show tip", mSolo.searchText(mSolo.getString(R.string.micro_shutter_help_tip), true));
	}

	@Override
	public void onShutterButtonLongClick() {
		takePhotoOrVideo(5000);
		backToNormalState();
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
		spec.needSegmentRemoveButton = false;// need modify
		spec.needSegmentRemixButton = false;// need modify

		AssertUtil.assertViewVisibility(spec);
	}

	public void testSegmentButton() {
		if (mContext.getCurrentModuleIndex() != mContext.getResources().getInteger(R.integer.camera_mode_micro_video)) {
			return;
		}
		MicroVideoModule microVideoModule = (MicroVideoModule) mContext.getCurrentModule();
		ImageView segmentRemixBtn = (ImageView) CommonUtil.getViewById(IdUtils.SEGMENT_REMIX_BUTTON_ID);

		takePhotoOrVideo(2000);// less than 3000;
		AssertUtil.assertTrue("total video numbers is wrong", microVideoModule.getRemixVideoPathNum() == 1);

		mSolo.clickOnView(segmentRemixBtn);
		AssertUtil.assertTrue("video clips should be same", microVideoModule.getRemixVideoPathNum() == 1);
		AssertUtil.assertTrue("remix video minitime tip should be displayed", CommonUtil.isViewVisible(IdUtils.MICRO_MINITIME_TIP));
		AssertUtil.assertTrue("remix button state is wrong", !segmentRemixBtn.isEnabled());
		AssertUtil.assertUIComparedWithResource((StateListDrawable) segmentRemixBtn.getDrawable(),
				(int) mContext.getResources().getDimension(R.dimen.bottom_bar_thumb_size),
				(int) mContext.getResources().getDimension(R.dimen.bottom_bar_thumb_size), R.drawable.ic_microvideo_ok_pressed);

		int remixVideoPathNum = microVideoModule.getRemixVideoPathNum();
		int recordingTimes = 2;
		for (int i = 0; i < recordingTimes; i++) {
			takePhotoOrVideo(4000);
		}
		AssertUtil.assertTrue("total video numbers should equals " + recordingTimes,
				microVideoModule.getRemixVideoPathNum() == (recordingTimes + remixVideoPathNum));

		backToNormalState();
	}

	private void backToNormalState() {
		MicroVideoModule microVideoModule = (MicroVideoModule) mContext.getCurrentModule();
		int remixVideoPathNum = microVideoModule.getRemixVideoPathNum();
		while (remixVideoPathNum > 0) {
			ImageView segmentRemoveBtn = (ImageView) CommonUtil.getViewById(IdUtils.SEGMENT_REMOVE_BUTTON_ID);
			mSolo.clickOnView(segmentRemoveBtn);
			AssertUtil.assertTrue("video clip need not be removed actually", microVideoModule.getRemixVideoPathNum() == remixVideoPathNum);

			mSolo.clickOnView(segmentRemoveBtn);
			mSolo.sleep(1000);
			AssertUtil.assertTrue("video clip should be removed", microVideoModule.getRemixVideoPathNum() == --remixVideoPathNum);;
		}

		AssertUtil.assertTrue("there is no video clips last acutually", remixVideoPathNum == 0);
		checkViewVisibility();
	}
}
