/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.utils;

import java.util.Random;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.camera.CameraActivity;
import com.android.camera.settings.CameraSettingsActivity;
import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.util.CameraUtil;
import com.tct.camera.R;
import com.tct.camera.robotium.Solo;
import com.tct.camera.testcases.TestCase;
import com.tct.camera.testfunc.ModeOptionFunc;

public class CommonUtil {

	private static final String TAG = "CommonUtil";
	private static Solo mSolo;
	private static CameraActivity mContext;
	private static CommonUtil mCommonUtilInstances;
	private static Instrumentation mInstrumentation;
	public static final String FILE_NAME = "test-all.xml";

	public static final int TEST_CASE = 1;
	public static final int CAMERA_TEST = 2;
	public static final int SETTINGS_TEST = 3;
	public static final int PHOTO_TEST = 4;
	public static final int VIDEO_TEST = 5;
	public static final int AUTO_TEST = 6;
	public static final int AUTO_BACK_TEST = 7;
	public static final int AUTO_FRONT_TEST = 8;
	public static final int PANORAMA_TEST = 9;
	public static final int MANUAL_TEST = 10;
	public static final int SLOW_MOTION_TEST = 11;
	public static final int MICRO_TEST = 12;
	public static final int MICRO_BACK_TEST = 13;
	public static final int MICRO_FRONT_TEST = 14;
	public static final int FYUSE_TEST = 15;
	public static final int LAST_TEST = 16;

	public static CommonUtil getInstances(Solo solo, CameraActivity activity, Instrumentation inst) {
		Log.w(TAG, "test: getInstances");
		if (mCommonUtilInstances == null) {
			mSolo = solo;
			mContext = activity;
			mCommonUtilInstances = new CommonUtil();
			mInstrumentation = inst;
		}
		return mCommonUtilInstances;
	}

	public static View getViewById(String id) {
		if (mSolo == null) {
			return null;
		}
		try {
			return mSolo.getView(id, true);
		} catch (Exception e) {
			Log.w(TAG, "View with id: '" + id + "' is not found!");
		}

		return null;
	}

	public static View getViewById(String id, int index) {
		if (mSolo == null) {
			return null;
		}
		try {
			return mSolo.getView(id, index, true);
		} catch (Exception e) {
			Log.w(TAG, "View with id: '" + id + "' is not found!");
		}

		return null;
	}

	public static boolean clickById(String id) {
		View view = getViewById(id);
		if (view != null && view.getVisibility() == View.VISIBLE) {
			mSolo.clickOnView(view);
			mSolo.sleep(1000);
			return true;
		}
		AssertUtil.assertTrue("view is invisible", false);
		return false;
	}

	public static boolean clickView(View view) {
		if (view != null && view.getVisibility() == View.VISIBLE) {
			mSolo.clickOnView(view);
			mSolo.sleep(1000);
			return true;
		}
		AssertUtil.assertTrue("view is invisible", false);
		return false;
	}

	public static boolean clickLongById(String id, int time) {
		View view = getViewById(id);
		if (view != null && view.getVisibility() == View.VISIBLE) {
			mSolo.clickLongOnView(view, time);
			mSolo.sleep(1000);
			return true;
		}
		AssertUtil.assertTrue("view is invisible", false);
		return false;
	}

	public static boolean clickLongView(View view, int time) {
		if (view != null && view.getVisibility() == View.VISIBLE) {
			mSolo.clickLongOnView(view, time);
			return true;
		}
		AssertUtil.assertTrue("view is invisible", false);
		return false;
	}

	public static boolean isViewVisible(String id) {
		View view = getViewById(id);
		if (view != null && view.getVisibility() == View.VISIBLE) {
			return true;
		}
		return false;
	}

	public static boolean isViewVisible(View view) {
		if (view != null && view.getVisibility() == View.VISIBLE) {
			return true;
		}
		return false;
	}

	/**
	 * scroll on the screen
	 * @param direction left or right
	 * @param stepCount scroll times
	 */
	public static void onScroll(int direction, int stepCount) {
		mSolo.sleep(1000);
		for (int i = 0; i < stepCount; i++) {
			mSolo.sleep(1000);
			mSolo.scrollToSide(direction, 0.75F, stepCount);
		}
		mSolo.sleep(1000);
	}

	public static boolean isSDCardAvaliable() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	public static String getTestResultDir(Context context) {
		String packageName = "/robotium";
		String filePath = context.getFilesDir() + packageName;
		if (isSDCardAvaliable()) {
			filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + packageName;
		}
		return filePath;
	}

	public static boolean isStringEmpty(String text) {
		if (text == null || text.equals("")) {
			return true;
		}
		return false;
	}

	public static boolean isStringEquals(String src, String des) {
		if (src != null && des != null && src.equals(des)) {
			return true;
		}
		return false;
	}


	public static float generateRandomNumber(float min, float max) {
		Random random = new Random();
		return random.nextInt((int) max) % ((int) max - (int) min + 1) + (int) min;
	}

	public static int generateRandomNumber(int min, int max) {
		Random random = new Random();
		return random.nextInt(max) % (max - min + 1) + min;
	}

	public static int generateRandomNumber(int max) {
		if (max <= 0) {
			return 0;
		}

		Random random = new Random();
		return random.nextInt(max);
	}

	public static boolean isBitmapEqual(Bitmap actualBitmap, Bitmap expectedBitmap) {
		int nonMatchingPixels = 0;
		int allowedMaxNonMatchPixels = 10;

		if (expectedBitmap == null || actualBitmap == null) {
			return false;
		}

		int actualWidth = actualBitmap.getWidth();
		int actualHeight = actualBitmap.getHeight();
		int expectedWidth = expectedBitmap.getWidth();
		int expectedHeight = expectedBitmap.getHeight();
		if (actualWidth == 0 || actualHeight == 0 || expectedWidth == 0 || expectedHeight == 0) {
			return false;
		}

		if (actualWidth != expectedWidth || actualHeight != expectedHeight) {
			return false;
		}

		int[] actualBmpPixels = new int[actualWidth * actualHeight];
		actualBitmap.getPixels(actualBmpPixels, 0, actualWidth, 0, 0, actualWidth, actualHeight);

		int[] expectedBmpPixels = new int[expectedWidth * expectedHeight];
		expectedBitmap.getPixels(expectedBmpPixels, 0, expectedWidth, 0, 0, expectedWidth, expectedHeight);

		if (actualBmpPixels.length != expectedBmpPixels.length) {
			return false;
		}

		for (int i = 0; i < actualBmpPixels.length; i++) {
			if (actualBmpPixels[i] != expectedBmpPixels[i]) {nonMatchingPixels++;}
		}

		if (nonMatchingPixels > allowedMaxNonMatchPixels) {
			return false;
		}

		return true;
	}

	// change mode
	public static void changeToSpecialMode(final int modeIndex, boolean isBackCamera) {
		int backCameraId = Integer.parseInt(mContext.getString(R.string.pref_camera_id_entry_back_value));
		int frontCameraId = Integer.parseInt(mContext.getString(R.string.pref_camera_id_entry_front_value));
		int currentCameraId = mContext.getSettingsManager().getInteger(SettingsManager.SCOPE_GLOBAL, Keys.KEY_CAMERA_ID);

		if (mContext.getCurrentModuleIndex() == modeIndex && (isBackCamera ? (currentCameraId == backCameraId) : (currentCameraId == frontCameraId))) {
			return;
		}
		if (mContext.getCurrentModuleIndex() == modeIndex) {
			if (ModeOptionFunc.isModeOptionVisible(ModeOptionFunc.CAMERA_MODE)) {
				ModeOptionFunc.onModeOptionClick(ModeOptionFunc.CAMERA_MODE);
				return;
			}
		}

		final String modeText = CameraUtil.getCameraModeText(modeIndex, mContext);
		if (CommonUtil.isStringEmpty(modeText)) {
			return;
		}
		final TextView modeTextView = mSolo.getText(modeText, true);
		AssertUtil.assertTrue("cannot find " + modeText + "text in the preview screen", modeTextView != null);

		if (isBackCamera) {
			mContext.getSettingsManager().set(SettingsManager.SCOPE_GLOBAL, Keys.KEY_CAMERA_ID, backCameraId);
		} else {
			mContext.getSettingsManager().set(SettingsManager.SCOPE_GLOBAL, Keys.KEY_CAMERA_ID, frontCameraId);
		}

		mInstrumentation.runOnMainSync(new Runnable() {
			@Override
			public void run() {
				modeTextView.performClick();
			}
		});
		mSolo.sleep(1000);
		currentCameraId = mContext.getSettingsManager().getInteger(SettingsManager.SCOPE_GLOBAL, Keys.KEY_CAMERA_ID);
		AssertUtil.assertTrue("change to mode " + modeText + "fail", mContext.getCurrentModuleIndex() == modeIndex &&
				(isBackCamera ? (currentCameraId == backCameraId) : (currentCameraId == frontCameraId)));
	}

	public static void changeToSpecialMode(int modeIndex) {
		if (mContext.getCurrentModuleIndex() == modeIndex) {
			return;
		}

		String modeText = CameraUtil.getCameraModeText(modeIndex, mContext);
		if (CommonUtil.isStringEmpty(modeText)) {
			return;
		}
		final TextView modeTextView = mSolo.getText(modeText, true);
		AssertUtil.assertTrue("cannot find " + modeText + "text in the preview screen", modeTextView != null);

		mInstrumentation.runOnMainSync(new Runnable() {
			@Override
			public void run() {
				modeTextView.performClick();
			}
		});
		mSolo.sleep(1000);
		AssertUtil.assertTrue("change to mode " + modeText + "fail", mContext.getCurrentModuleIndex() == modeIndex);
	}

	public static void changeToSettings() {
		CommonUtil.clickView(MainTestUI.CommonUIView.getSettingButton());
		if (mSolo.waitForActivity(CameraSettingsActivity.class)) {
			AssertUtil.assertTrue("change to settings unsuccessfully", mSolo.searchText(mContext.getString(R.string.mode_settings), true));
		}
	}
	public static void updateCameraMode(int currentModeIndex) {
		Log.w(TAG, "update camera mode");
		switch (currentModeIndex) {
			case SETTINGS_TEST:
				CommonUtil.changeToSpecialMode(mContext.getResources().getInteger(R.integer.camera_mode_photo));
				changeToSettings();
				break;
			case AUTO_TEST:
				CommonUtil.changeToSpecialMode(mContext.getResources().getInteger(R.integer.camera_mode_photo));
				break;
			case AUTO_BACK_TEST:
				CommonUtil.changeToSpecialMode(mContext.getResources().getInteger(R.integer.camera_mode_photo), true);
				break;
			case AUTO_FRONT_TEST:
				CommonUtil.changeToSpecialMode(mContext.getResources().getInteger(R.integer.camera_mode_photo), false);
				break;
			case PANORAMA_TEST:
				CommonUtil.changeToSpecialMode(mContext.getResources().getInteger(R.integer.camera_mode_pano));
				break;
			case MANUAL_TEST:
				CommonUtil.changeToSpecialMode(mContext.getResources().getInteger(R.integer.camera_mode_manual));
				break;
			case SLOW_MOTION_TEST:
				CommonUtil.changeToSpecialMode(mContext.getResources().getInteger(R.integer.camera_mode_slowmotion));
				break;
			case MICRO_TEST:
				CommonUtil.changeToSpecialMode(mContext.getResources().getInteger(R.integer.camera_mode_micro_video));
				break;
			case MICRO_BACK_TEST:
				CommonUtil.changeToSpecialMode(mContext.getResources().getInteger(R.integer.camera_mode_micro_video), true);
				break;
			case MICRO_FRONT_TEST:
				CommonUtil.changeToSpecialMode(mContext.getResources().getInteger(R.integer.camera_mode_micro_video), false);
				break;
			case FYUSE_TEST:
				CommonUtil.changeToSpecialMode(mContext.getResources().getInteger(R.integer.camera_mode_parallax));
				break;
		}
	}

	public static Rect getViewBounds(View view, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
	    int xBegin = location[0] + paddingLeft;
	    int yBegin = location[1] + paddingTop;
	    int xEnd = location[0] + view.getWidth() - paddingRight;
	    int yEnd = location[1] + view.getHeight() - paddingBottom;

	    return new Rect(xBegin, yBegin, xEnd, yEnd);
	}

	public static void DragSeekBar(SeekBar seekBar) {
		AssertUtil.assertTrue("can not find seek bar " + seekBar, CommonUtil.isViewVisible(seekBar));

		Rect seekBarBoundsRect = CommonUtil.getViewBounds(seekBar, seekBar.getPaddingLeft(), seekBar.getPaddingTop(),
				seekBar.getPaddingRight(), seekBar.getPaddingBottom());
		int fromPosX = CommonUtil.generateRandomNumber(seekBarBoundsRect.left, seekBarBoundsRect.right);
		int ToPosX = CommonUtil.generateRandomNumber(seekBarBoundsRect.left, seekBarBoundsRect.right);
		int centerY = (seekBarBoundsRect.top + seekBarBoundsRect.bottom) / 2;

		int perviousProgress = seekBar.getProgress();
		Log.w(TAG, "before drag: progress is " + perviousProgress);
		mSolo.drag(fromPosX, ToPosX, centerY, centerY, 20);
		mSolo.sleep(1000);
		int currentProgress = seekBar.getProgress();
		int currentPosProgress = (int) (((float) (ToPosX - seekBarBoundsRect.left)/(seekBarBoundsRect.right - seekBarBoundsRect.left))*seekBar.getMax());
		Log.w(TAG, "after drag: progress is " + currentProgress + " ,position progress is " + currentPosProgress);
		boolean flag = (currentProgress >= (currentPosProgress -2) && currentProgress <= (currentPosProgress + 2));
		AssertUtil.assertTrue("the seekbar progress is wrong, perviousProgress = " + perviousProgress + ", " +
						"currentProgress = " + currentProgress + ", currentPosProgress = " + currentPosProgress +
						", fromPosX = " + fromPosX + ", ToPosX = " + ToPosX, flag);
	}

	public static double distanceBetweenFingers(PointF point1, PointF point2) {
        float disX = Math.abs(point1.x - point2.x);
        float disY = Math.abs(point1.y - point2.y);
        return Math.sqrt(disX * disX + disY * disY);
    }

	public static RectF getValidPreviewScreen() {
		RectF previewOverlayBounds = mContext.getCameraAppUI().getPreviewArea();
		if (previewOverlayBounds.top == 0) {
			previewOverlayBounds.top = previewOverlayBounds.top + mContext.getResources().getDimension(R.dimen.mode_option_height);
		}
		RectF bottomBarRect = mContext.getCaptureLayoutHelper().getBottomBarRect();
		if (previewOverlayBounds.bottom > bottomBarRect.top) {
			previewOverlayBounds.bottom = bottomBarRect.top;
		}

		return previewOverlayBounds;
	}

	public static boolean isBackCameraFacing() {
		return mContext.getCameraProvider().getCharacteristics(mContext.getCurrentCameraId()).isFacingBack();
	}

	public static void lanuchCamera() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
	    intent.setComponent(new ComponentName(TestCase.PACKAGE_NAME,TestCase.CLASS_NAME));
	    mSolo.getCurrentActivity().startActivity(intent);
	    mSolo.sleep(2000);
	    String modeText = CameraUtil.getCameraModeText(mContext.getCurrentModuleIndex(), mContext);
	    AssertUtil.assertTrue("back to camera successfully", !CommonUtil.isStringEmpty(modeText) && mSolo.searchText(modeText, true));
	}
}
