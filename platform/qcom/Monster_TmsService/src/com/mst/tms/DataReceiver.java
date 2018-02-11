package com.mst.tms;    

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class DataReceiver extends BroadcastReceiver {  
	public static final String TAG = "TmsDataReceiver";
	
        @Override  
        public void onReceive(Context context, Intent intent) {  
            // TODO Auto-generated method stub  
    		Log.d(TAG, "TmsDataReceiver onReceive");
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
//            NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);  
//            NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
            NetworkInfo activeInfo = manager.getActiveNetworkInfo();  
            if(activeInfo != null && activeInfo.isConnected()) {
        		Log.d(TAG, "wifi connected");
                SharedPreferences prefs = context.getSharedPreferences("updatedatabase", Context.MODE_PRIVATE);
                long lastUpdateTime =  prefs.getLong("lastUpdateTime", 0);
                boolean lastResult = prefs.getBoolean("update_result", false);
                 
                /* Should Activity Check for Updates Now? */
         		Log.d(TAG, "lastUpdateTime =" + lastUpdateTime);
                if ((lastUpdateTime + (24 * 7 * 60 * 60 * 1000)) < System.currentTimeMillis() || !lastResult) {
         
                    /* Save current timestamp for next Check*/
                	if(lastResult) {
	                    lastUpdateTime = System.currentTimeMillis();           
	                    SharedPreferences.Editor editor = prefs.edit();
	                    editor.putLong("lastUpdateTime", lastUpdateTime);
	                    editor.commit();
                	}
         
                    /* Start Update */           
        	    	UpdataManagerMst.updateDatabaseIfNeed();
                }    
            }
        } 
      
    }  