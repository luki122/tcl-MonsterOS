package com.mst.tms;
import android.util.Log;

public class AreaManager {
	
	public static final String TAG = "AreaManager";
	
	public static String getArea(String number) {
		// get location of the input phone number 
		// 获取用户输入的号码的归属地
		String location = TmsServiceManager.getInstance().getArea(number);
		Log.d(TAG, " number = " + number + ", area = " + location);
		return location;
	}
}
