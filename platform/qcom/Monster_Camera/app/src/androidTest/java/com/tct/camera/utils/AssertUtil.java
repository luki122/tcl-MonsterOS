/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.android.camera.CameraActivity;
import com.android.camera.settings.Keys;
import com.android.camera.test.TestUtils;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.tct.camera.R;
import com.tct.camera.robotium.Solo;
import com.tct.camera.testfunc.BottomBarFunc;
import com.tct.camera.testfunc.ModeOptionFunc;

import junit.framework.Assert;

import java.util.Arrays;
import java.util.List;

public class AssertUtil {

	private static final String TAG = "AssertUtil";
	private static AssertUtil mAssertInstances;
	protected static Solo mSolo;
	protected static CameraActivity mContext;

	public static AssertUtil getInstances(Solo solo, CameraActivity context) {
		if (mAssertInstances == null) {
			mAssertInstances = new AssertUtil();
			mSolo = solo;
			mContext = context;
		}
		return mAssertInstances;
	}

	public static void assertCondition(boolean needOperation, String msg1, boolean flag1, String msg2, boolean flag2) {
		if (needOperation) {
			Assert.assertTrue(msg1, flag1);
		} else {
			Assert.assertTrue(msg2, flag2);
		}
	}

	public static void assertTrue(String msg, boolean flag) {
		Assert.assertTrue(msg, flag);
	}

	public static void assertViewVisibility(MainTestUI.CommonUISpec spec) {

		boolean isSettingBtnShow = ModeOptionFunc.isModeOptionVisible(ModeOptionFunc.SETTING_MODE);
		assertTrue("wrong settings button visibility", isSettingBtnShow == spec.needSetting);

		boolean isFlashBtnShow = ModeOptionFunc.isModeOptionVisible(ModeOptionFunc.FLASH_MODE);
		assertTrue("wrong flash button visibility", isFlashBtnShow == spec.needFlash);

		boolean isHdrBtnShow = ModeOptionFunc.isModeOptionVisible(ModeOptionFunc.HDR_MODE);
		assertTrue("wrong hdr button visibility", isHdrBtnShow == spec.needHdr);

		boolean isTimeBtnShow = ModeOptionFunc.isModeOptionVisible(ModeOptionFunc.TIME_MODE);
		assertTrue("wrong time button visibility", isTimeBtnShow == spec.needTime);

		boolean isLightBtnShow = ModeOptionFunc.isModeOptionVisible(ModeOptionFunc.LIGHT_MODE);
		assertTrue("wrong light button visibility", isLightBtnShow == spec.needLight);

		boolean isCameraBtnShow = ModeOptionFunc.isModeOptionVisible(ModeOptionFunc.CAMERA_MODE);
		assertTrue("wrong camera button visibility", isCameraBtnShow == spec.needCamera);

		boolean isPeekThumbBtnShow = BottomBarFunc.isBottomButtonVisible(BottomBarFunc.PEEK_THUMB);
		assertTrue("wrong camera button visibility", isPeekThumbBtnShow == spec.needPeekThumb);

		boolean isShutterBtnShow = BottomBarFunc.isBottomButtonVisible(BottomBarFunc.SHUTTER_BUTTON);
		assertTrue("wrong shutter button visibility", isShutterBtnShow == spec.needShutterButton);

		boolean isVideoShutterBtnShow = BottomBarFunc.isBottomButtonVisible(BottomBarFunc.VIDEO_SHUTTER);
		assertTrue("wrong video shutter button visibility", isVideoShutterBtnShow == spec.needVideoShutterButton);

		boolean isVideoSnapBtnShow = BottomBarFunc.isBottomButtonVisible(BottomBarFunc.VIDEO_SNAP);
		assertTrue("wrong video snap button visibility", isVideoSnapBtnShow == spec.needVideoSnapButton);

		boolean isSegmentRemoveBtnShow = BottomBarFunc.isBottomButtonVisible(BottomBarFunc.SEGMENT_REMOVE);
		assertTrue("wrong segment remove button visibility", isSegmentRemoveBtnShow == spec.needSegmentRemoveButton);

		boolean isSegmentRemixBtnShow = BottomBarFunc.isBottomButtonVisible(BottomBarFunc.SEGMENT_REMIX);
		assertTrue("wrong segment remix button visibility", isSegmentRemixBtnShow == spec.needSegmentRemixButton);
	}

	public static void assertUIComparedWithResource(StateListDrawable drawable, int width, int height, int resId) {
		Bitmap uiBitmap = ((BitmapDrawable) drawable.getCurrent()).getBitmap();
		ImageView imageView = new ImageView(mContext);
		imageView.setLayoutParams(new LayoutParams(width, height));
		imageView.setImageResource(resId);
		Bitmap resBitmap = ((BitmapDrawable) imageView.getDrawable().getCurrent()).getBitmap();
		assertTrue("ui icon may be wrong", CommonUtil.isBitmapEqual(uiBitmap, resBitmap));
	}

	public static void assertSuperZoomOn(boolean currentSuperResolutionOn, boolean lastSuperResolutionOn) {
		if (currentSuperResolutionOn) {
			// flash off
			AssertUtil.assertTrue("super zoom on, set flash off", CommonUtil.isStringEquals(mContext.getSettingsManager().getString(mContext.getCameraScope(), Keys.KEY_FLASH_MODE),
					mContext.getCameraCapabilities().getStringifier().stringify(CameraCapabilities.FlashMode.OFF)));
			// flash disable
			AssertUtil.assertUIComparedWithResource((StateListDrawable) MainTestUI.CommonUIView.getFlashButton().getDrawable(),
					(int) mContext.getResources().getDimension(R.dimen.mode_option_width),
					(int) mContext.getResources().getDimension(R.dimen.mode_option_height),
					R.drawable.ic_flash_off_disabled);
		} else {
			if (lastSuperResolutionOn) {
				// flash off
				AssertUtil.assertTrue("super zoom off, enable flash", CommonUtil.isStringEquals(mContext.getSettingsManager().getString(mContext.getCameraScope(), Keys.KEY_FLASH_MODE),
						mContext.getCameraCapabilities().getStringifier().stringify(CameraCapabilities.FlashMode.OFF)));
				// flash enable
				AssertUtil.assertUIComparedWithResource((StateListDrawable) MainTestUI.CommonUIView.getFlashButton().getDrawable(),
						(int) mContext.getResources().getDimension(R.dimen.mode_option_width),
						(int) mContext.getResources().getDimension(R.dimen.mode_option_height),
						R.drawable.ic_flash_off_normal);
			}
		}
	}

	public static void assertCallbackMessage(View view, List<TestUtils.MESSAGE> receivedMessages, TestUtils.MESSAGE[] expectedMessages) {
		if (expectedMessages == null) {
			AssertUtil.assertTrue("Since message sent is null, it can not receive message", receivedMessages.size() == 0);
		} else {
			Log.w(TAG, "receive num: " + receivedMessages.size() + ", already sent num: " + expectedMessages.length);
			boolean result = compareMessages(receivedMessages, Arrays.asList(expectedMessages));
			AssertUtil.assertTrue("button operation is unsuccessfully ", result);
		}
	}

	private static boolean compareMessages(List<TestUtils.MESSAGE> receivedMessages, List<TestUtils.MESSAGE> expectedMessages) {
		if (receivedMessages == null || expectedMessages == null) {
			return false;
		}

		// both list equals
		if (receivedMessages.containsAll(expectedMessages) && receivedMessages.size() == expectedMessages.size()) {
			return true;
		}

		if (receivedMessages.containsAll(expectedMessages)) {
			for (int i = 0; i < receivedMessages.size(); i++) {
				// if fail, there are some message not be sent in code
				AssertUtil.assertTrue(receivedMessages.get(i).toString() + "is not received",
						expectedMessages.contains(receivedMessages.get(i)));
			}

		} else if (expectedMessages.containsAll(receivedMessages)) {
			for (int i = 0; i < expectedMessages.size(); i++) {
				// if fail, please add the constant message
				AssertUtil.assertTrue("please add " + receivedMessages.get(i).toString() + "to expected messages",
						receivedMessages.contains(expectedMessages.get(i)));
			}

		} else {
			AssertUtil.assertTrue("the code send message is not consistent", false);
		}

		return true;
	}
}
