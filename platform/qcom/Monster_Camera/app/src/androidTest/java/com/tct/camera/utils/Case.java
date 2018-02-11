/* Copyright (C) 2016 Tcl Corporation Limited */
package com.tct.camera.utils;

import java.util.UUID;

/**
 * @author wenhua.tu
 *
 */
public class Case {

	private String mId;
	private String mSuiteName;
	private String mClassName;
	private String mMethodeName;
	private String mIsRight;
	private String mTime;
	private String mMessage;
	private String mType;
	private String mStack;

	public Case() {
		setmId(UUID.randomUUID().toString());
	}

	public String getmId() {
		return mId;
	}
	public void setmId(String mId) {
		this.mId = mId;
	}
	public String getmSuiteName() {
		if (CommonUtil.isStringEmpty(mSuiteName)) {
			return "-";
		}
		return mSuiteName;
	}
	public void setmSuiteName(String mSuiteName) {
		this.mSuiteName = mSuiteName;
	}
	public String getmClassName() {
		if (CommonUtil.isStringEmpty(mClassName)) {
			return "-";
		}
		return mClassName;
	}
	public void setmClassName(String mClassName) {
		this.mClassName = mClassName;
	}
	public String getmMethodeName() {
		if (CommonUtil.isStringEmpty(mMethodeName)) {
			return "-";
		}
		return mMethodeName;
	}
	public void setmMethodeName(String mMethodeName) {
		this.mMethodeName = mMethodeName;
	}
	public String getmIsRight() {
		if (CommonUtil.isStringEmpty(mIsRight)) {
			return "-";
		}
		return mIsRight;
	}
	public void setmIsRight(String mIsRight) {
		this.mIsRight = mIsRight;
	}
	public String getmTime() {
		if (CommonUtil.isStringEmpty(mTime)) {
			return "-";
		}
		return mTime;
	}
	public void setmTime(String mTime) {
		this.mTime = mTime;
	}
	public String getmMessage() {
		if (CommonUtil.isStringEmpty(mMessage)) {
			return "-";
		}
		return mMessage;
	}
	public void setmMessage(String mMessage) {
		this.mMessage = mMessage;
	}
	public String getmType() {
		if (CommonUtil.isStringEmpty(mType)) {
			return "-";
		}
		return mType;
	}
	public void setmType(String mType) {
		this.mType = mType;
	}
	public String getmStack() {
		if (CommonUtil.isStringEmpty(mStack)) {
			return "-";
		}
		return mStack;
	}
	public void setmStack(String mStack) {
		this.mStack = mStack;
	}
}
