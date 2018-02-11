/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.testcases;


import android.util.Log;
import android.view.View;

import com.android.camera.NormalPhotoModule;
import com.android.camera.settings.Keys;
import com.android.camera.test.TestUtils;
import com.tct.camera.R;
import com.tct.camera.testfunc.BottomBarFunc;
import com.tct.camera.testfunc.ModeOptionFunc;
import com.tct.camera.utils.AssertUtil;
import com.tct.camera.utils.CommonUtil;
import com.tct.camera.utils.IdUtils;
import com.tct.camera.utils.MainTestUI;


/**
 * @author wenhua.tu
 *
 */
public class AutoTest extends PhotoTest {

	private static final String TAG = "AutoTest";
	protected static final int ATTENTION_SEEKER = 0;
	protected static final int FACE_BEAUTY = 1;
	protected static final int GESTURE_PALM = 2;

	public AutoTest() {
		mCurrentTestCaseIndex = CommonUtil.AUTO_TEST;
	}

	@Override
	protected void takePhotoOrVideo(int time) {
		int countDownDuration = mContext.getSettingsManager().getInteger(mContext.getCameraScope(), Keys.KEY_COUNTDOWN_DURATION);
		if (ModeOptionFunc.isModeOptionVisible(ModeOptionFunc.TIME_MODE) && countDownDuration > 0) {
			View modeScrollIndicator = MainTestUI.CommonUIView.getModScrollIndicator();
			View modeStripView = MainTestUI.CommonUIView.getModeStripView();
			View modeOptionOverlay = MainTestUI.CommonUIView.getModeOptionOvelay();
			View facebeautyOption = CommonUtil.getViewById(IdUtils.FACE_BEAUTY_OPTION_ID);
			View gesturePalmOption = CommonUtil.getViewById(IdUtils.GESTURE_PALM_OPTION);
			View soundGroupOption = CommonUtil.getViewById(IdUtils.ATTENTION_SOUND_GROUP_ID);
			View countDownView = CommonUtil.getViewById(IdUtils.COUNT_DOWN_VIEW_ID);

			AssertUtil.assertTrue("before click, attention seeker visibility is wrong", (isFunctionEnabled(ATTENTION_SEEKER) && CommonUtil.isViewVisible(soundGroupOption)) || (!isFunctionEnabled(ATTENTION_SEEKER) && !CommonUtil.isViewVisible(soundGroupOption)));
			AssertUtil.assertTrue("before click, face beauty visibility is wrong", (isFunctionEnabled(FACE_BEAUTY) && CommonUtil.isViewVisible(facebeautyOption)) || (!isFunctionEnabled(FACE_BEAUTY) && !CommonUtil.isViewVisible(facebeautyOption)));
			AssertUtil.assertTrue("before click, gesture palm visibility is wrong", (isFunctionEnabled(GESTURE_PALM) && CommonUtil.isViewVisible(gesturePalmOption)) || (!isFunctionEnabled(GESTURE_PALM) && !CommonUtil.isViewVisible(gesturePalmOption)));
			AssertUtil.assertTrue("before click, timer ui is wrong", CommonUtil.isViewVisible(modeScrollIndicator) && CommonUtil.isViewVisible(modeStripView) && CommonUtil.isViewVisible(modeOptionOverlay) && !CommonUtil.isViewVisible(countDownView));

			BottomBarFunc.onBottomButtonClick(BottomBarFunc.SHUTTER_BUTTON, time, TestUtils.AUTO_COUNTDOWN_SHUTTER_CLICK);
			AssertUtil.assertTrue("on click, attention seeker visibility is wrong", !CommonUtil.isViewVisible(soundGroupOption));
			AssertUtil.assertTrue("on click, face beauty visibility is wrong", !CommonUtil.isViewVisible(facebeautyOption));
			AssertUtil.assertTrue("on click, gesture palm visibility is wrong", !CommonUtil.isViewVisible(gesturePalmOption));
			AssertUtil.assertTrue("on click, timer ui is wrong", !CommonUtil.isViewVisible(modeScrollIndicator) && !CommonUtil.isViewVisible(modeStripView) && !CommonUtil.isViewVisible(modeOptionOverlay) && CommonUtil.isViewVisible(countDownView));

			BottomBarFunc.onBottomButtonClick(BottomBarFunc.SHUTTER_CANCEL_BUTTON, time, TestUtils.AUTO_COUNTDOWN_CANCEL_SHUTTER_CLICK);
			AssertUtil.assertTrue("cancel click, attention seeker visibility is wrong", (isFunctionEnabled(ATTENTION_SEEKER) && CommonUtil.isViewVisible(soundGroupOption)) || (!isFunctionEnabled(ATTENTION_SEEKER) && !CommonUtil.isViewVisible(soundGroupOption)));
			AssertUtil.assertTrue("cancel click, face beauty visibility is wrong", (isFunctionEnabled(FACE_BEAUTY) && CommonUtil.isViewVisible(facebeautyOption)) || (!isFunctionEnabled(FACE_BEAUTY) && !CommonUtil.isViewVisible(facebeautyOption)));
			AssertUtil.assertTrue("cancel click, gesture palm visibility is wrong", (isFunctionEnabled(GESTURE_PALM) && CommonUtil.isViewVisible(gesturePalmOption)) || (!isFunctionEnabled(GESTURE_PALM) && !CommonUtil.isViewVisible(gesturePalmOption)));
			AssertUtil.assertTrue("cancel click, timer ui is wrong", CommonUtil.isViewVisible(modeScrollIndicator) && CommonUtil.isViewVisible(modeStripView) && CommonUtil.isViewVisible(modeOptionOverlay) && !CommonUtil.isViewVisible(countDownView));

			BottomBarFunc.onBottomButtonClick(BottomBarFunc.SHUTTER_BUTTON, time, TestUtils.AUTO_COUNTDOWN_SHUTTER_CLICK);
			AssertUtil.assertTrue("click again, attention seeker visibility is wrong", !CommonUtil.isViewVisible(soundGroupOption));
			AssertUtil.assertTrue("click again, face beauty visibility is wrong", !CommonUtil.isViewVisible(facebeautyOption));
			AssertUtil.assertTrue("click again, gesture palm visibility is wrong", !CommonUtil.isViewVisible(gesturePalmOption));
			AssertUtil.assertTrue("click again, timer ui is wrong", !CommonUtil.isViewVisible(modeScrollIndicator) && !CommonUtil.isViewVisible(modeStripView) && !CommonUtil.isViewVisible(modeOptionOverlay) && CommonUtil.isViewVisible(countDownView));

			mSolo.sleep(countDownDuration * 1000 + 5000);
			AssertUtil.assertTrue("after click, attention seeker visibility is wrong", (isFunctionEnabled(ATTENTION_SEEKER) && CommonUtil.isViewVisible(soundGroupOption)) || (!isFunctionEnabled(ATTENTION_SEEKER) && !CommonUtil.isViewVisible(soundGroupOption)));
			AssertUtil.assertTrue("after click, face beauty visibility is wrong", (isFunctionEnabled(FACE_BEAUTY) && CommonUtil.isViewVisible(facebeautyOption)) || (!isFunctionEnabled(FACE_BEAUTY) && !CommonUtil.isViewVisible(facebeautyOption)));
			AssertUtil.assertTrue("after click, gesture palm visibility is wrong", (isFunctionEnabled(GESTURE_PALM) && CommonUtil.isViewVisible(gesturePalmOption)) || (!isFunctionEnabled(GESTURE_PALM) && !CommonUtil.isViewVisible(gesturePalmOption)));
			AssertUtil.assertTrue("after click, timer ui is wrong", CommonUtil.isViewVisible(modeScrollIndicator) && CommonUtil.isViewVisible(modeStripView) && CommonUtil.isViewVisible(modeOptionOverlay) && !CommonUtil.isViewVisible(countDownView));

			return;
		}

		BottomBarFunc.onBottomButtonClick(BottomBarFunc.SHUTTER_BUTTON, time, TestUtils.AUTO_SHUTTER_CLICK);
	}

	@Override
	public void onShutterButtonClick() {
		takePhotoOrVideo(0);
	}

	@Override
	public void onShutterButtonLongClick() {
		// low light,face beauty, gesture, time
		boolean isLowlightOn = Keys.isLowlightOn(mContext.getSettingsManager(), mContext.getCameraScope());
		boolean isCountDownOn = mContext.getSettingsManager().getInteger(mContext.getCameraScope(), Keys.KEY_COUNTDOWN_DURATION) > 0;
		boolean isFaceBeautyOn = isFunctionEnabled(FACE_BEAUTY);
		boolean isGestureOn = isFunctionEnabled(GESTURE_PALM);
		boolean needLongClick = !(isLowlightOn || isCountDownOn || isFaceBeautyOn || isGestureOn);
		if (needLongClick) {
			BottomBarFunc.onBottomButtonClick(BottomBarFunc.SHUTTER_BUTTON_LONG, 2000, TestUtils.AUTO_SHUTTER_LONG_CLICK);
		} else {
			BottomBarFunc.onBottomButtonClick(BottomBarFunc.SHUTTER_BUTTON_LONG, 2000, null);
		}
	}

	// first run, before than testMainFunctions()
	public void testFirstVideoRecording() {
		Log.w(TAG, "test: testVideoRecording()");
		BottomBarFunc.onBottomButtonClick(BottomBarFunc.VIDEO_SHUTTER, 0, TestUtils.NORMAL_VIDEO_SHUTTER_CLICK_START);
		for (int i = 0; i < 2; i++) {
			BottomBarFunc.onBottomButtonClick(BottomBarFunc.VIDEO_SNAP, 0, TestUtils.VIDEO_SNAP);
		}
		BottomBarFunc.onBottomButtonClick(BottomBarFunc.SHUTTER_BUTTON, 0, TestUtils.NORMAL_VIDEO_SHUTTER_CLICK_STOP);
		mSolo.sleep(1000);
		AssertUtil.assertTrue("not change back to auto mode, getCurrentModuleIndex = " + mContext.getCurrentModuleIndex(),
				mContext.getCurrentModuleIndex()== mContext.getResources().getInteger(R.integer.camera_mode_photo));
	}

	// pr1871725
	public void testShutterButtonLongClickCondition() {
		Log.w(TAG, "test: testShutterButtonLongClickCondition");
		onShutterButtonLongClick();
	}

	protected boolean isFunctionEnabled(int functionMode) {
		if (mContext.getCurrentModuleIndex() != mContext.getResources().getInteger(R.integer.camera_mode_photo)) {
			return false;
		}
		NormalPhotoModule normalPhotoModule = (NormalPhotoModule) mContext.getCurrentModule();

		switch (functionMode) {
		case ATTENTION_SEEKER:
			if (!CommonUtil.isBackCameraFacing()) {
				return false;
			}
			if (!normalPhotoModule.isAttentionSeekerShow() || normalPhotoModule.isImageCaptureIntent()) {
				return false;
			}
			break;

		case FACE_BEAUTY:
			if (CommonUtil.isBackCameraFacing()) {
				return false;
			}
			if (!normalPhotoModule.isFacebeautyEnabled() || mContext.getCameraAppUI().isInIntentReview()) {
				return false;
			}
			break;

		case GESTURE_PALM:
			if (CommonUtil.isBackCameraFacing()) {
				return false;
			}
			if (!normalPhotoModule.isGesturePalmShow() || mContext.getCameraAppUI().isInIntentReview()) {
				return false;
			}
			break;
		}

		return true;
	}
}
