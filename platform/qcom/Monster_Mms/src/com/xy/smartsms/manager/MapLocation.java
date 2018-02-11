package com.xy.smartsms.manager;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import cn.com.xy.sms.sdk.action.AbsSdkDoAction;

//import com.amap.api.location.AMapLocation;
//import com.amap.api.location.AMapLocationListener;
//import com.amap.api.location.LocationManagerProxy;
//import com.amap.api.location.LocationProviderProxy;




public class MapLocation {
	
	  /*
	 
	    private static LocationManagerProxy mAMapLocationManager;
	    private static AMap aMap;
	    private static Handler mHandler;
	    
	    
	    public static void  getLocation(Context context, Handler handler) {	    
	        mHandler = handler;
	        aMap = new AMap();
	        mAMapLocationManager = LocationManagerProxy.getInstance(context);
	        mAMapLocationManager.requestLocationData(LocationProviderProxy.AMapNetwork, 60 * 1000, 15, aMap); //onlt locate one time

	    }


	    private static class AMap implements AMapLocationListener {

	        @Override
	        public void onLocationChanged(Location location) {

	        }

	        @Override
	        public void onStatusChanged(String provider, int status, Bundle extras) {

	        }

	        @Override
	        public void onProviderEnabled(String provider) {

	        }

	        @Override
	        public void onProviderDisabled(String provider) {

	        }

	        //获取经纬度
	        @Override
	        public void onLocationChanged(AMapLocation location) {
	            Log.d("onLocationChanged", "onLocationChanged()~~~~");

	            if (location == null)
	                return;

	            if (location.getAMapException().getErrorCode() != 0)
	                Log.d("onLocationChanged", "errCode = " + location.getAMapException().getErrorCode() + ", errMsg = " + location.getAMapException().getErrorMessage());

	            Message msg = mHandler.obtainMessage(AbsSdkDoAction.DO_SEND_MAP_QUERY_URL);
	            Bundle bundle = new Bundle();
	            bundle.putDouble("latitude", location.getLatitude());
	            bundle.putDouble("longitude", location.getLongitude());
	            msg.setData(bundle);
	            msg.sendToTarget();
	            
	            destroyAMapLocationListener();
	        }
	        
	        public void destroyAMapLocationListener() { //取消经纬度监听
	            mAMapLocationManager.removeUpdates(aMap);
	            mAMapLocationManager.destory();
	            mAMapLocationManager = null;	  
	            mHandler = null;
	        }

	    }

	    */
}
