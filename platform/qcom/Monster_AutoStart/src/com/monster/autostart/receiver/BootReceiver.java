package com.monster.autostart.receiver;

import java.util.List;

import com.monster.autostart.bean.AppManagerState;
import com.monster.autostart.interfaces.IBaseSolution;
import com.monster.autostart.service.BootReceiverServices;
import com.monster.autostart.utils.Utilities;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver{

	final String ACTION_SERVICE_START = "android.intent.action.ACTION_SERVICE_START";
	
	final String ACTION_BOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.e(Utilities.TAG, "_CLS_:"+"BootReceiver"+";"+"_FUNCTION_:"+"onReceive" +";"+"act="+intent.getAction());  
		
		switch (intent.getAction()) {
		case ACTION_SERVICE_START:
		case ACTION_BOOT_COMPLETE:
			context.startService(new Intent(context, BootReceiverServices.class));
			break;

		default:
			break;
		}
	
	}	
}
