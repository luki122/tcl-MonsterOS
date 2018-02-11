package com.mst;    

import com.android.incallui.InCallApp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class SpeedReceiver extends BroadcastReceiver {  
	public static final String TAG = "SpeedReceiver";
	
        @Override  
        public void onReceive(Context context, Intent intent) { 
            // TODO Auto-generated method stub  
    		Log.d(TAG, "SpeedReceiver onReceive");
    		InCallApp.getInstance().displayCallScreenforSpeed();                             
        } 
      
    }  