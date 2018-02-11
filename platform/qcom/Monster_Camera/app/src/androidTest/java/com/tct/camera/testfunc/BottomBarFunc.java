/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.testfunc;

import android.util.Log;

import com.android.camera.test.TestUtils.MESSAGE;
import com.android.camera.test.TestUtils.TestCallBack;
import com.android.camera.ui.RotateImageView;
import com.tct.camera.R;
import com.tct.camera.utils.AssertUtil;
import com.tct.camera.utils.CommonUtil;
import com.tct.camera.utils.IdUtils;
import com.tct.camera.utils.MainTestUI;
import com.tct.camera.utils.Message;

public class BottomBarFunc extends Func {

	private static final String TAG = "BottomBarFunc";
	public static final int SHUTTER_BUTTON = 1;
	public static final int SHUTTER_BUTTON_LONG = 2;
	public static final int SHUTTER_CANCEL_BUTTON = 3;
	public static final int PEEK_THUMB = 4;
	public static final int VIDEO_SHUTTER = 5;
	public static final int VIDEO_SNAP = 6;
	public static final int SEGMENT_REMOVE = 7;
	public static final int SEGMENT_REMIX = 8;

	public static final int BOTTOM_BAR_TOTAL = SEGMENT_REMIX;

	static Message[] mBottomBarMessage = new Message[100];
	static {
		for (int i = 0; i < BOTTOM_BAR_TOTAL; i++) {
			mBottomBarMessage[i] = new Message();
		}
	}

	public static void onBottomButtonClick(int mode, int time, MESSAGE[] messages) {
		Log.w(TAG, "test: onBottomButtonClick, mode = " + mode);
		switch (mode) {
			case SHUTTER_BUTTON:
				AssertUtil.assertTrue("shutter button not found", CommonUtil.clickView(MainTestUI.CommonUIView.getShutterButton()));
				break;

			case SHUTTER_BUTTON_LONG:
				clickBottomBar(MainTestUI.CommonUIView.getShutterButton(), SHUTTER_BUTTON_LONG, true, time, 0, messages);
				break;

			case SHUTTER_CANCEL_BUTTON:
				clickBottomBar(MainTestUI.CommonUIView.getShutterCancelButton(), SHUTTER_CANCEL_BUTTON, true, time, 0, messages);
				break;

			case PEEK_THUMB:
				clickBottomBar(MainTestUI.CommonUIView.getPeekImageView(), PEEK_THUMB, false, 0, 0, messages);
				CommonUtil.lanuchCamera();
				break;

			case VIDEO_SHUTTER:
				int orginalModuleIndex = mContext.getCurrentModuleIndex();
				clickBottomBar((RotateImageView) CommonUtil.getViewById(IdUtils.VIDEO_SHUTTER_BUTTON_ID), VIDEO_SHUTTER, false, 0, 0, messages);
				int newModuleIndex = mContext.getCurrentModuleIndex();
				Log.w(TAG, "test: orginalModuleIndex: " + orginalModuleIndex + ",newModuleIndex:" + newModuleIndex);
				AssertUtil.assertTrue("change to normal video module unsuccessfully",
						orginalModuleIndex == mContext.getResources().getInteger(R.integer.camera_mode_photo) &&
						newModuleIndex == mContext.getResources().getInteger(R.integer.camera_mode_video));
				break;

			case VIDEO_SNAP:
				mSolo.sleep(1000);
				clickBottomBar((RotateImageView) CommonUtil.getViewById(IdUtils.VIDEO_SNAP_BUTTON_ID), VIDEO_SNAP, false, 0, 3000, messages);
				break;
		}
	}

	private static void clickBottomBar(RotateImageView view, final int buttonIndex, boolean isLongClick, int longClickTime, int maxRuntime, MESSAGE[] messages) {
		AssertUtil.assertTrue("button is not found", view != null);
		mBottomBarMessage[buttonIndex].resetMessage();
		view.setTestCallBack(new TestCallBack() {
			@Override
			public void sendMessage(MESSAGE message) {
				mBottomBarMessage[buttonIndex].addMessage(message);
			}
		});

		AssertUtil.assertTrue("button is invisible", isLongClick ? CommonUtil.clickLongView(view, longClickTime) : CommonUtil.clickView(view));

		if (maxRuntime > 0) {
			mSolo.sleep(maxRuntime);
		}
		AssertUtil.assertCallbackMessage(view, mBottomBarMessage[buttonIndex].getAllMessages(), messages);
	}

	public static boolean isBottomButtonVisible(int mode) {
		Log.w(TAG, "test: isBottomButtonVisible, mode = " + mode);
		switch (mode) {
			case SHUTTER_BUTTON:
				return CommonUtil.isViewVisible(MainTestUI.CommonUIView.getShutterButton());

			case PEEK_THUMB:
				return CommonUtil.isViewVisible(MainTestUI.CommonUIView.getPeekImageView());

			case VIDEO_SHUTTER:
				return CommonUtil.isViewVisible(IdUtils.VIDEO_SHUTTER_BUTTON_ID);

			case VIDEO_SNAP:
				return CommonUtil.isViewVisible(IdUtils.VIDEO_SNAP_BUTTON_ID);

			case SEGMENT_REMOVE:
				return CommonUtil.isViewVisible(IdUtils.SEGMENT_REMOVE_BUTTON_ID);

			case SEGMENT_REMIX:
				return CommonUtil.isViewVisible(IdUtils.SEGMENT_REMIX_BUTTON_ID);
 		}
		return false;
	}
}
