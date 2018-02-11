/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.testcases;

import android.widget.ImageView;
import android.widget.SeekBar;

import com.android.camera.ui.CustomSeekBar;
import com.android.camera.ui.FaceBeautyOption;
import com.tct.camera.R;
import com.tct.camera.robotium.Solo;
import com.tct.camera.testfunc.PreviewScreenFunc;
import com.tct.camera.utils.AssertUtil;
import com.tct.camera.utils.CommonUtil;
import com.tct.camera.utils.IdUtils;
import com.tct.camera.utils.MainTestUI;

public class AutoFrontTest extends AutoTest {

//	private static final String TAG = "AutoFrontTest";

	public AutoFrontTest() {
		mCurrentTestCaseIndex = CommonUtil.AUTO_FRONT_TEST;
	}

	@Override
	public void checkViewVisibility() {
		MainTestUI.CommonUISpec spec = new MainTestUI.CommonUISpec();
		spec.needSetting = true;
		spec.needFlash = true;
		spec.needHdr = false;
		spec.needTime = true;
		spec.needLight = false;
		spec.needCamera = true;

		spec.needPeekThumb = true;
		spec.needShutterButton = true;
		spec.needVideoShutterButton = true;
		spec.needVideoSnapButton = false;
		spec.needSegmentRemoveButton = false;
		spec.needSegmentRemixButton = false;

		AssertUtil.assertViewVisibility(spec);
	}

	public void testFaceBeauty() {
		if (!isFunctionEnabled(FACE_BEAUTY)) {
			return;
		}

		FaceBeautyOption faceBeautyOption = (FaceBeautyOption) CommonUtil.getViewById(IdUtils.FACE_BEAUTY_OPTION_ID);
		ImageView faceBeautyIcon = (ImageView) CommonUtil.getViewById(IdUtils.FACE_BEAUTY_ICON_ID);
		AssertUtil.assertTrue("Face beauty should be visible", CommonUtil.isViewVisible(faceBeautyOption) &&
				CommonUtil.isViewVisible(faceBeautyIcon));

		CustomSeekBar faceBeautyCustomSeekBar = (CustomSeekBar) CommonUtil.getViewById(IdUtils.FACE_BEAUTY_SEEKBAR_ID);
		for (int i = 0; i < 4; i++) {
			CommonUtil.clickView(faceBeautyIcon);
			if (CommonUtil.isViewVisible(faceBeautyCustomSeekBar)) {
				CommonUtil.clickView(faceBeautyIcon);
				AssertUtil.assertTrue("face beauty seekbar should be invisible after click face beauty icon", !CommonUtil.isViewVisible(faceBeautyCustomSeekBar));
			}
			CommonUtil.clickView(faceBeautyIcon);
			AssertUtil.assertTrue("face beauty seekbar should be visible after click face beauty icon", CommonUtil.isViewVisible(faceBeautyCustomSeekBar));
			CommonUtil.DragSeekBar((SeekBar) CommonUtil.getViewById(IdUtils.COMMON_SEEKBAR_ID));
		}
	}
}
