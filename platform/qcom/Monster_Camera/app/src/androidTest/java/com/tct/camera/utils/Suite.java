/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.utils;

import java.util.ArrayList;
import java.util.UUID;

/**
 * @author wenhua.tu
 *
 */
public class Suite extends ArrayList<Case> {

	private static final long serialVersionUID = 1L;
	private String mId;

	public Suite() {
		setmId(UUID.randomUUID().toString());
	}

	public String getmId() {
		return mId;
	}

	public void setmId(String mId) {
		this.mId = mId;
	}

//	public void addCase(Case testCase) {
//		add(testCase);
//	}
//
//	public void removeCase(Case testCase) {
//		remove(testCase);
//	}
//
//	public void clearCases() {
//		clear();
//	}

}
