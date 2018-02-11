package com.mst.tms;
import android.util.Log;
import android.text.TextUtils;

public class AreaManager {
	
	public static final String TAG = "AreaManager";
	
	public static String getArea(String number) {
		// get location of the input phone number 
		// 获取用户输入的号码的归属地
		if(!TextUtils.isEmpty(number)) {
			number = number.replaceAll(" ", "");
		}
		String location = TmsServiceManager.getInstance().getArea(number);
		if(!TextUtils.isEmpty(location)) {
			location = location.replaceAll(" ", "");
		}
		Log.d(TAG, " number = " + number + ", area = " + location);
		return location;
	}
}
