package com.mst.tms;
import android.util.Log;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.location.LocationManager;

public class AreaManager {
	
	public static final String TAG = "AreaManager";
	
	private static LocationManager mLocationManager;
	
	public static synchronized  LocationManager  getInstance() {
		if(mLocationManager == null) {
			mLocationManager = ManagerCreatorC.getManager(LocationManager.class);
		}
		return mLocationManager;
	}
	
	public static String getArea(String number) {
		// get location of the input phone number 
		// 获取用户输入的号码的归属地
		String location = getInstance().getLocation(number);
		Log.d(TAG, " number = " + number + ", area = " + location);
		return location;
	}
}
