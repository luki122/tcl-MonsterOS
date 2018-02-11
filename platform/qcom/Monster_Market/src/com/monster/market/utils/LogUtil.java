package com.monster.market.utils;

import android.util.Log;

public class LogUtil {
	
	public static final String TAG = "LogUtil";
	
	private static boolean logSwitch = true;
	
	public static void i(String msg) {
		i(TAG, msg);
	}
	
	public static void i(String tag, String msg) {
		if (logSwitch) {
			Log.i(tag, msg);
		}
	}
	
	public static void w(String msg) {
		w(TAG, msg);
	}
	
	public static void w(String tag, String msg) {
		if (logSwitch) {
			Log.w(tag, msg);
		}
	}
	
	public static void d(String msg) {
		d(TAG, msg);
	}
	
	public static void d(String tag, String msg) {
		if (logSwitch) {
			Log.d(tag, msg);
		}
	}
	
	public static void e(String msg) {
		e(TAG, msg);
	}
	
	public static void e(String tag, String msg) {
		if (logSwitch) {
			Log.e(tag, msg);
		}
	}
	
	public static void print(String msg) {
		System.out.println(msg);
	}
	
	public static void printLog(String msg) {
		Log.i(TAG, msg);
	}
	
}
