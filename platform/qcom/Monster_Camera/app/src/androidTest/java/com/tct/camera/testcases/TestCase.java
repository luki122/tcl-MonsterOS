/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.testcases;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.android.camera.CameraActivity;
import com.tct.camera.R;
import com.tct.camera.robotium.Solo;
import com.tct.camera.testfunc.Func;
import com.tct.camera.utils.AssertUtil;
import com.tct.camera.utils.CommonUtil;
import com.tct.camera.utils.MainTestUI;

public class TestCase extends ActivityInstrumentationTestCase2<CameraActivity>{

	private static final String TAG = "AutoTest";
	private static boolean mTestFinished = false;
	protected static Solo mSolo;
	protected static CameraActivity mContext;
	protected static Instrumentation mInstrumentation;
	protected int mCurrentTestCaseIndex;
	private int mOldTestCaseIndex;
	public static final String PACKAGE_NAME = "com.tct.camera";
	public static final String CLASS_NAME = "com.android.camera.CameraActivity";

	public TestCase() {
		super(CameraActivity.class);
		mCurrentTestCaseIndex = CommonUtil.TEST_CASE;
		mOldTestCaseIndex = mCurrentTestCaseIndex;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (mContext == null) {
			mTestFinished = false;
			mContext = getActivity();
		}
		if (mSolo == null) {
			mInstrumentation = getInstrumentation();
			mSolo = new Solo(mInstrumentation, getActivity());
			CommonUtil.getInstances(mSolo, mContext, mInstrumentation);
			Func.getInstances(mSolo, mContext, mInstrumentation);
			AssertUtil.getInstances(mSolo, mContext);
			MainTestUI.CommonUIView.getCommonUIView();
		}
		// before start a new Test Case, change camera mode
		if (mCurrentTestCaseIndex != mOldTestCaseIndex) {
			CommonUtil.updateCameraMode(mCurrentTestCaseIndex);
			mOldTestCaseIndex = mCurrentTestCaseIndex;
			mSolo.sleep(1000);
		}
		Log.w(TAG, "test: " + mSolo.toString() + " setUp()");
	}

	@Override
	protected void tearDown() throws Exception {
		// if want to run tearDown(), please set mTestFinished true
 		if (mTestFinished) {
			Log.w(TAG, "test: " + mSolo.toString() + " tearDown()");
			mSolo.finishOpenedActivities();
			super.tearDown();
		}
	}

	public void setTestFinished(boolean isFinished) {
		mTestFinished = isFinished;
	}
}
