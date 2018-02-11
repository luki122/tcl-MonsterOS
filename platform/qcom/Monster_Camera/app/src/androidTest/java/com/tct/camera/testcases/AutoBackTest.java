/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.testcases;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.camera.settings.Keys;
import com.android.camera.settings.SettingsManager;
import com.android.camera.ui.SoundGroup;
import com.android.camera.util.CameraUtil;
import com.android.camera.widget.SoundAction;
import com.tct.camera.R;
import com.tct.camera.testfunc.ModeOptionFunc;
import com.tct.camera.utils.AssertUtil;
import com.tct.camera.utils.CommonUtil;
import com.tct.camera.utils.IdUtils;
import com.tct.camera.utils.MainTestUI;

public class AutoBackTest extends AutoTest {

	public AutoBackTest() {
		mCurrentTestCaseIndex = CommonUtil.AUTO_BACK_TEST;
	}

	@Override
	public void checkViewVisibility() {
		MainTestUI.CommonUISpec spec = new MainTestUI.CommonUISpec();
		spec.needSetting = true;
		spec.needFlash = true;
		spec.needHdr = true;
		spec.needTime = true;
		spec.needLight = true;
		spec.needCamera = true;

		spec.needPeekThumb = true;
		spec.needShutterButton = true;
		spec.needVideoShutterButton = true;
		spec.needVideoSnapButton = false;
		spec.needSegmentRemoveButton = false;
		spec.needSegmentRemixButton = false;

		AssertUtil.assertViewVisibility(spec);
	}

	public void testAttentionSound() {
		if (!isFunctionEnabled(ATTENTION_SEEKER)) {
			return;
		}

		SoundGroup soundGroup = (SoundGroup) CommonUtil.getViewById(IdUtils.ATTENTION_SOUND_GROUP_ID);
		SoundAction soundAction = (SoundAction) soundGroup.getChildAt(0);
		ImageView soundImageView = (ImageView) soundAction.getChildAt(soundAction.getChildCount() - 1);
		AssertUtil.assertTrue("attention sound icon should be visible",
				CommonUtil.isViewVisible(soundGroup) && CommonUtil.isViewVisible(soundAction) &&
				CommonUtil.isViewVisible(soundImageView));

		boolean isAnimalIconExpanded = soundAction.isExpand();
		int childIndex = -1;
		for (int i = 0; i < 4; i++) {
			if (isAnimalIconExpanded) {
				for (int j = 0; j < 4; j++) {
					int tempChildIndex = CommonUtil.generateRandomNumber(soundAction.getChildCount() - 1);
					ImageView animalImageView = (ImageView) soundAction.getChildAt(tempChildIndex);
					CommonUtil.clickView(animalImageView);
					AssertUtil.assertTrue("the animal sound should be playing", (childIndex == tempChildIndex && !soundGroup.isSoundPlaying()) ||
							((childIndex != tempChildIndex || childIndex == -1) && soundGroup.isSoundPlaying()));
					if (childIndex != tempChildIndex) {
						childIndex = tempChildIndex;
					}
				}
			}
			CommonUtil.clickView(soundImageView);
			AssertUtil.assertTrue("click attention sound has no effect", soundAction.isExpand() == !isAnimalIconExpanded);
			isAnimalIconExpanded = !isAnimalIconExpanded;
		}
		if (soundGroup.isSoundPlaying()) {
			if (!isAnimalIconExpanded) {
				CommonUtil.clickView(soundImageView);
				AssertUtil.assertTrue("click attention sound has no effect", soundAction.isExpand() == !isAnimalIconExpanded);
				isAnimalIconExpanded = !isAnimalIconExpanded;
			}
			CommonUtil.clickView(soundAction.getChildAt(childIndex));
			AssertUtil.assertTrue("attention sound should stop", !soundGroup.isSoundPlaying());
		}
	}

	public void testModeNameClick() {
		int currentModeIndex = mContext.getCurrentModuleIndex();
		RelativeLayout stereoGroup = MainTestUI.CommonUIView.getStereoGroup();
		AssertUtil.assertTrue("stereo group should show", CommonUtil.isViewVisible(stereoGroup));
		for (int i = 0; i < stereoGroup.getChildCount(); i++) {
			TextView modeTextView = (TextView) stereoGroup.getChildAt(i);
			CommonUtil.clickView(modeTextView);
			AssertUtil.assertTrue("change to mode " + modeTextView.getText() + "fail",
					CommonUtil.isStringEquals(CameraUtil.getCameraModeText(mContext.getCurrentModuleIndex(), mContext), (String) modeTextView.getText()));
		}
		CommonUtil.changeToSpecialMode(currentModeIndex);
	}
}
