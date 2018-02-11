package com.tcl.monster.fota.utils;

import android.os.Build;

import java.lang.reflect.Method;

//Add by xiaolu.li 2015-10-19,1099340 begin
public class MyTelephonyManager {
	private static final String className = "android.telephony.TelephonyManager";
	private static Class<?> relectClassInfo = null;
	private static Object obj = null;

	public MyTelephonyManager(Object this_obj) {
		try {
			relectClassInfo = Class.forName(className);
			obj = this_obj;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public int getPhoneCount() {
		int rtnValue = 1;
		if (Build.VERSION.SDK_INT >= 20) {
			try {
				Method method = relectClassInfo.getDeclaredMethod("getPhoneCount");
				rtnValue = (Integer) method.invoke(obj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return rtnValue;
	}

	public boolean isMultiSimEnabled() {
		boolean rtnValue = false;
		Method method;
		try {
			method = relectClassInfo.getDeclaredMethod("isMultiSimEnabled");
			rtnValue = (Boolean) method.invoke(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rtnValue;
	}

	public String getDeviceId() {
		String rtnStr = null;
		try {
			Method method = relectClassInfo.getDeclaredMethod("getDeviceId");
			rtnStr = (String) method.invoke(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rtnStr;
	}

	public String getDeviceId(int slotId) {
		String rtnStr = null;
		try {
			Method method = relectClassInfo.getDeclaredMethod("getDeviceId", int.class);
			rtnStr = (String) method.invoke(obj, slotId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rtnStr;
	}

	public String getIMEI(int slotId) {
		String rtnStr = null;
		try {
			Method[] a=relectClassInfo.getMethods();
			Method method = relectClassInfo.getDeclaredMethod("getImei", int.class);

			rtnStr = (String) method.invoke(obj, slotId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rtnStr;
	}
}
//Add by xiaolu.li 2015-10-19, PR1099340 end
