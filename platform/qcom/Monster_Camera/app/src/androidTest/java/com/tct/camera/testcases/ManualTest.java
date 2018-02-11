/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.testcases;

import org.json.JSONException;
import org.json.JSONObject;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.camera.ManualUI;
import com.android.camera.settings.Keys;
import com.android.camera.test.TestUtils;
import com.android.camera.ui.ManualItem;
import com.android.camera.widget.FloatingActionsMenu;
import com.android.ex.camera2.portability.CameraCapabilities;
import com.android.ex.camera2.portability.CameraSettings;
import com.tct.camera.R;
import com.tct.camera.robotium.Solo;
import com.tct.camera.testfunc.BottomBarFunc;
import com.tct.camera.testfunc.PreviewScreenFunc;
import com.tct.camera.utils.AssertUtil;
import com.tct.camera.utils.CommonUtil;
import com.tct.camera.utils.IdUtils;
import com.tct.camera.utils.MainTestUI;

public class ManualTest extends PhotoTest {

	public ManualTest() {
		mCurrentTestCaseIndex = CommonUtil.MANUAL_TEST;
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

	@Override
	protected void takePhotoOrVideo(int time) {
		BottomBarFunc.onBottomButtonClick(BottomBarFunc.SHUTTER_BUTTON, time, TestUtils.MANUAL_SHUTTER_CLICK);
	}

	@Override
	public void onShutterButtonClick() {
		takePhotoOrVideo(0);
	}

	@Override
	public void onShutterButtonLongClick() {
		BottomBarFunc.onBottomButtonClick(BottomBarFunc.SHUTTER_BUTTON_LONG, 2000, null);
	}

	public void testManualItem() throws JSONException {
		FloatingActionsMenu floatingActionMenu = (FloatingActionsMenu) CommonUtil.getViewById(IdUtils.FLOATING_ACTION_MENU);
		AssertUtil.assertTrue("manual item menu should be visible", CommonUtil.isViewVisible(floatingActionMenu));
		AssertUtil.assertTrue("manual item menu children count is not 5", floatingActionMenu.getChildCount() == 5);
		View addButton = ((LinearLayout) floatingActionMenu.getChildAt(floatingActionMenu.getChildCount() - 1)).getChildAt(0);
		AssertUtil.assertTrue("manual item add button type is wrong", !(addButton instanceof ManualItem));

		// test add button
		boolean previousExpanded = floatingActionMenu.isExpanded();
		mSolo.clickOnView(addButton);
		mSolo.sleep(1000);
		AssertUtil.assertTrue("maual item menu expanded wrong on fisrt click", previousExpanded != floatingActionMenu.isExpanded());

		if (!floatingActionMenu.isExpanded()) {
			mSolo.clickOnView(addButton);
			mSolo.sleep(1000);
			AssertUtil.assertTrue("maual item menu expanded wrong on second click", previousExpanded == floatingActionMenu.isExpanded());
		}

		int manualItemCount = floatingActionMenu.getChildCount() - 1;
		for (int i = 0; i < manualItemCount; i++) {
			ManualItem manualItem = (ManualItem) floatingActionMenu.getChildAt(manualItemCount - i - 1);
			View itemRoot = manualItem.findViewById(R.id.item_root);
			AssertUtil.assertTrue("child " + i + " should be visible", CommonUtil.isViewVisible(itemRoot));

			View itemProgressView = manualItem.findViewById(R.id.manual_progress_view);
			boolean previousItemProgressViewVisible = CommonUtil.isViewVisible(itemProgressView);
			CommonUtil.clickView(itemRoot);
			AssertUtil.assertTrue("manual item progress view's visibility is wrong", previousItemProgressViewVisible != CommonUtil.isViewVisible(itemProgressView));

			if (!CommonUtil.isViewVisible(itemProgressView)) {
				CommonUtil.clickView(itemRoot);
				AssertUtil.assertTrue("manual item progess view should be visible", CommonUtil.isViewVisible(itemProgressView));
			}

			View itemAutoLayout = manualItem.findViewById(R.id.auto_layout);
			TextView itemDescription = (TextView) manualItem.findViewById(R.id.description_item);
			String previousItemDes = (String) itemDescription.getText();
			mSolo.clickOnView(itemAutoLayout);
			mSolo.sleep(1000);
			String currentItemDes = (String) itemDescription.getText();
			AssertUtil.assertTrue("manual item auto setting should change", !CommonUtil.isStringEquals(previousItemDes, currentItemDes));

			CameraSettings cameraSettings = mContext.getCameraSettings();
			SeekBar seekBar = (SeekBar) manualItem.findViewById(R.id.customseekbar);
			int index;
			switch(i) {
			case ManualItem.MANUAL_SETTING_ISO:
				if (CommonUtil.isStringEquals(cameraSettings.getISOValue(), CameraCapabilities.KEY_AUTO_ISO)) {
					AssertUtil.assertTrue("iso description is wrong", CommonUtil.isStringEquals(currentItemDes, mContext.getString(R.string.pref_camera_iso_s_f_entry_auto)));
				} else {
					String curIsoState = mContext.getSettingsManager().getString(mContext.getModuleScope(), Keys.KEY_MANUAL_ISO_STATE);
					JSONObject job=new JSONObject(curIsoState);
					index = (int) job.get(ManualUI.SETTING_INDEX);
					AssertUtil.assertTrue("iso description and value is wrong", CommonUtil.isStringEquals(currentItemDes, manualItem.getTitles().get(index) + "") &&
																	  CommonUtil.isStringEquals(cameraSettings.getISOValue(), CameraCapabilities.KEY_MANUAL_ISO) &&
																	  cameraSettings.getContinuousIso() == (int) manualItem.getValues().get(index));
				}

				CommonUtil.DragSeekBar(seekBar);

				index = seekBar.getProgress() / manualItem.getScaleFactor();
				AssertUtil.assertTrue("iso description and value is wrong", CommonUtil.isStringEquals((String) itemDescription.getText(), manualItem.getTitles().get(index) + "") &&
						  CommonUtil.isStringEquals(cameraSettings.getISOValue(), CameraCapabilities.KEY_MANUAL_ISO) &&
						  cameraSettings.getContinuousIso() == (int) manualItem.getValues().get(index));
				break;
			case ManualItem.MANUAL_SETTING_EXPOSURE:
				if (CommonUtil.isStringEquals(cameraSettings.getExposureTime(), "0")) {
					AssertUtil.assertTrue("exposure description is wrong", CommonUtil.isStringEquals(currentItemDes, mContext.getString(R.string.pref_camera_iso_s_f_entry_auto)));
				} else {
					String curExposureState = mContext.getSettingsManager().getString(mContext.getModuleScope(), Keys.KEY_CUR_EXPOSURE_TIME_STATE);
					JSONObject job=new JSONObject(curExposureState);
					index = (int) job.get(ManualUI.SETTING_INDEX);
					AssertUtil.assertTrue("exposure description and value is wrong", CommonUtil.isStringEquals(currentItemDes, manualItem.getTitles().get(index) + "") &&
																				CommonUtil.isStringEquals(cameraSettings.getExposureTime(),(String) manualItem.getValues().get(index)));
				}

				CommonUtil.DragSeekBar(seekBar);

				index = seekBar.getProgress() / manualItem.getScaleFactor();
				AssertUtil.assertTrue("exposure description and value is wrong", CommonUtil.isStringEquals((String) itemDescription.getText(), manualItem.getTitles().get(index) + "") &&
						CommonUtil.isStringEquals(cameraSettings.getExposureTime(),(String) manualItem.getValues().get(index)));
				break;
			case ManualItem.MANUAL_SETTING_WHITE_BALANCE:
				if (cameraSettings.getWhiteBalance() == CameraCapabilities.WhiteBalance.AUTO) {
					AssertUtil.assertTrue("white balance description is wrong", CommonUtil.isStringEquals(currentItemDes, mContext.getString(R.string.pref_camera_iso_s_f_entry_auto)));
				} else {
					String curWBState = mContext.getSettingsManager().getString(mContext.getModuleScope(), Keys.KEY_CUR_WHITE_BALANCE_STATE);
					JSONObject job=new JSONObject(curWBState);
					index = (int) job.get(ManualUI.SETTING_INDEX);
					AssertUtil.assertTrue("white balance description and value is wrong", CommonUtil.isStringEquals(currentItemDes, manualItem.getTitles().get(index) + "") &&
						cameraSettings.getWhiteBalance() == mContext.getCameraCapabilities().getStringifier().whiteBalanceFromString((String) manualItem.getValues().get(index)));
				}

				CommonUtil.DragSeekBar(seekBar);

				index = seekBar.getProgress() / manualItem.getScaleFactor();
				AssertUtil.assertTrue("white balance description and value is wrong", CommonUtil.isStringEquals((String) itemDescription.getText(), manualItem.getTitles().get(index) + "") &&
						cameraSettings.getWhiteBalance() == mContext.getCameraCapabilities().getStringifier().whiteBalanceFromString((String) manualItem.getValues().get(index)));
				break;
			case ManualItem.MANUAL_SETTING_FOCUS_POS:
				if (cameraSettings.getCurrentFocusMode() == CameraCapabilities.FocusMode.CONTINUOUS_PICTURE) {
					AssertUtil.assertTrue("focus description is wrong", CommonUtil.isStringEquals(currentItemDes, mContext.getString(R.string.pref_camera_iso_s_f_entry_auto)));
				} else {
					String curFocusState = mContext.getSettingsManager().getString(mContext.getModuleScope(), Keys.KEY_CUR_FOCUS_STATE);
					JSONObject job=new JSONObject(curFocusState);
					index = (int) job.get(ManualUI.SETTING_INDEX);
					AssertUtil.assertTrue("focus description and value is wrong",
						((seekBar.getProgress() == ((int) manualItem.getValues().get(1) - (int) manualItem.getValues().get(0))) &&
									CommonUtil.isStringEquals(currentItemDes, (String) manualItem.getValues().get(2)) ||
						     (seekBar.getProgress() != ((int) manualItem.getValues().get(1) - (int) manualItem.getValues().get(0)) &&
					                CommonUtil.isStringEquals(currentItemDes, index + ""))) &&
					    cameraSettings.getCurrentFocusMode() == CameraCapabilities.FocusMode.MANUAL &&
					    cameraSettings.getManualFocusPosition() == index);
				}

				CommonUtil.DragSeekBar(seekBar);

				index = (int) manualItem.getValues().get(0) + seekBar.getProgress();
				AssertUtil.assertTrue("focus description and value is wrong",
						((seekBar.getProgress() == ((int) manualItem.getValues().get(1) - (int) manualItem.getValues().get(0))) &&
									CommonUtil.isStringEquals((String) itemDescription.getText(), (String) manualItem.getValues().get(2)) ||
						     (seekBar.getProgress() != ((int) manualItem.getValues().get(1) - (int) manualItem.getValues().get(0)) &&
					                CommonUtil.isStringEquals((String) itemDescription.getText(), index + ""))) &&
					    cameraSettings.getCurrentFocusMode() == CameraCapabilities.FocusMode.MANUAL &&
					    cameraSettings.getManualFocusPosition() == index);
			}
		}
	}
}
