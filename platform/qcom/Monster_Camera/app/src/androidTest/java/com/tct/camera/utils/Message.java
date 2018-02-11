/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.utils;

import java.util.ArrayList;
import java.util.List;

import com.android.camera.test.TestUtils.MESSAGE;

public class Message {
	private List<MESSAGE> mMessageList = new ArrayList<MESSAGE>();

	public void resetMessage() {
		mMessageList.clear();
	}

	public void addMessage(MESSAGE message) {
		AssertUtil.assertTrue(message.toString() + " message has been sent more than one time", !mMessageList.contains(message));
		mMessageList.add(message);
	}

	public int getMessageNum() {
		return mMessageList.size();
	}

	public List<MESSAGE> getAllMessages() {
		return mMessageList;
	}
}
