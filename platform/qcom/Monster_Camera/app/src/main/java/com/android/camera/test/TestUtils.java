/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera.test;

import java.util.List;

import android.R.menu;
import android.view.View;

import com.android.camera.CameraActivity;
import com.android.camera.ui.RotateImageView;




/**
 * @author wenhua.tu
 *
 */
public class TestUtils {

	public static final boolean IS_TEST = true;
	private static CameraActivity mActivity;
	public static TestUtils mInstances;

	public static TestUtils getInstances(CameraActivity activity) {
		if (!IS_TEST) {
			return null;
		}
		if (mInstances == null) {
			mInstances = new TestUtils();
			mActivity = activity;
		}

		return mInstances;
	}

	public interface TestCallBack {
		public void sendMessage(MESSAGE message);
	}

	public enum MESSAGE {
		PICTURE_TAKEN,
		MEDIA_SAVED,
		THUMBNAIL_CLICKED,
	}

	public static MESSAGE[] PEEK_THUMB = {
		MESSAGE.THUMBNAIL_CLICKED,
	};

	public static MESSAGE[] VIDEO_SNAP = {
		MESSAGE.PICTURE_TAKEN,
		MESSAGE.MEDIA_SAVED,
	};

	public static MESSAGE[] SEGMENT_REMOVE = {

	};

	public static MESSAGE[] SEGMENT_REMIX = {

	};

	public static MESSAGE[] AUTO_SHUTTER_LONG_CLICK = {

	};

	public static MESSAGE[] AUTO_SHUTTER_CLICK = {

	};

	public static MESSAGE[] AUTO_COUNTDOWN_SHUTTER_CLICK = {

	};

	public static MESSAGE[] AUTO_COUNTDOWN_CANCEL_SHUTTER_CLICK = {

	};

	public static MESSAGE[] MANUAL_SHUTTER_CLICK = {

	};

	public static MESSAGE[] SLOMOTION_SHUTTER_CLICK_START = {

	};

	public static MESSAGE[] SLOMOTION_SHUTTER_CLICK_STOP = {

	};

	public static MESSAGE[] MICRO_SHUTTER_LONG_CLICK = {

	};

	public static MESSAGE[] PANO_SHUTTER_CLICK_START = {

	};

	public static MESSAGE[] PANO_SHUTTER_CLICK_STOP = {

	};

	public static MESSAGE[] NORMAL_VIDEO_SHUTTER_CLICK_START = {

	};

	public static MESSAGE[] NORMAL_VIDEO_SHUTTER_CLICK_STOP = {

	};

	public static void sendMessage(View view, MESSAGE message) {
		if (!IS_TEST) {
			return;
		}
		if (view == null || !(view instanceof RotateImageView)) {
			return;
		}

		TestCallBack callBack = ((RotateImageView) view).getTestCallBack();
		if (callBack != null) {
			callBack.sendMessage(message);
		}
	}

	public static void sendMessage(int resId, MESSAGE message) {
		if (!IS_TEST || mActivity == null) {
			return;
		}
		sendMessage(mActivity.findViewById(resId), message);
	}
}
