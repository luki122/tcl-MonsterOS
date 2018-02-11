package com.monster.interception;

import java.util.List;

import com.monster.interception.notification.ManageReject;
import com.mst.tms.TmsServiceManager;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;

public class InterceptionApplication extends Application {
	private static InterceptionApplication application;
	private List<String> blackList;
	private int countCall = 0;
	private int countSms = 0;

	public int getCountCall() {
		return countCall;
	}

	public void setCountCall(int countCall) {
		this.countCall = countCall;
	}

	public int getCountSms() {
		return countSms;
	}

	public void setCountSms(int countSms) {
		this.countSms = countSms;
	}

	public List<String> getBlackList() {
		return blackList;
	}

	public void setBlackList(List<String> blackList) {
		this.blackList = blackList;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		application = this;
    	TmsServiceManager.getInstance(this).bindService();
    	mManageReject = new ManageReject(this);
	}

	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		super.onTerminate();
	}

	public static InterceptionApplication getInstance() {
		return application;
	}
	
	public ManageReject mManageReject;

}
