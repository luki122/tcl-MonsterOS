/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.testfunc;

import android.app.Instrumentation;

import com.android.camera.CameraActivity;
import com.tct.camera.robotium.Solo;

public class Func {

	private static Func mFuncInstances;
	protected static Solo mSolo;
	protected static CameraActivity mContext;
	protected static Instrumentation mInstrumentation;

	public static Func getInstances(Solo solo, CameraActivity context, Instrumentation inst) {
		if (mFuncInstances == null) {
			mFuncInstances = new Func();
			mSolo = solo;
			mContext = context;
			mInstrumentation = inst;
		}
		return mFuncInstances;
	}
}
