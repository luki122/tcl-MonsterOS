package com.monster.autostart.service;

import java.util.List;

import com.monster.autostart.AppManagerApplication;
import com.monster.autostart.bean.AppManagerState;
import com.monster.autostart.bean.ProcessObserverController;
import com.monster.autostart.interfaces.IBaseSolution;
import com.monster.autostart.loader.AutoStartLoader;
import com.monster.autostart.utils.Utilities;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class BootReceiverServices extends Service{

	List<IBaseSolution> mSolution;
	
	ProcessObserverController sProcessController;
	
	protected AppManagerState mState;

	protected AutoStartLoader sLoader;
	
	@Override 
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.e(Utilities.TAG, "_CLS_:"+"BootReceiverServices"+";"+"_FUNCTION_:"+"onCreate"); 

	}


	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.e(Utilities.TAG, "_CLS_:"+"BootReceiverServices"+";"+"_FUNCTION_:"+"onStartCommand"); 

		return super.onStartCommand(intent, flags, startId);
	}
	
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		mState = AppManagerState.getInstance();
		
		Log.e(Utilities.TAG, "_CLS_:"+"BootReceiverServices"+";"+"_FUNCTION_:"+"onStart"); 
		sProcessController = new ProcessObserverController();
		sProcessController.registerProcessObserver();

		/**M: fix for android N when clear App's data.and reboot will not reload data again so we just load it again begin*/
		if(Utilities.ATLEAST_ANDROID_N){
			sLoader = mState.getLoader();
			sLoader.startLoader();
		}
		/**M: fix for android N when clear App's data.and reboot will not reload data again so we just load it again end*/
		super.onStart(intent, startId);

	}


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.e(Utilities.TAG, "_CLS_:"+"BootReceiverServices"+";"+"_FUNCTION_:"+"onDestroy"); 
		sProcessController.onDestroy();
		super.onDestroy();
	}

}
