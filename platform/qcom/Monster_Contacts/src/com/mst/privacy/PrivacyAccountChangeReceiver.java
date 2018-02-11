package com.mst.privacy;

import java.util.ArrayList;
import java.util.List;

import  com.monster.privacymanage.entity.AidlAccountData;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PrivacyAccountChangeReceiver extends BroadcastReceiver{
	
	private static final String TAG = "PrivacyAccountChangeReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (intent.getExtras() != null) {
			String action = intent.getAction();
			AidlAccountData account = intent.getParcelableExtra("account");
        	Log.i(TAG, "PrivacyAccountChangeReceiver  "
        			+ "onReceive action: " + action 
        			+ "  account id: " + account.getAccountId() 
        			+ "  path: " + account.getHomePath());
        	
			if (action != null && action.equals("com.monster.privacymanage.SWITCH_ACCOUNT")) {
	        	PrivacyUtils.setCurrentAccountId(account.getAccountId());
	        	PrivacyUtils.mCurrentAccountHomePath = account.getHomePath();
	        	
	        	if (PrivacyUtils.getCurrentAccountId() > 0) {
	        		PrivacyUtils.mIsPrivacyMode = true;
	        	} else {
	        		PrivacyUtils.mIsPrivacyMode = false;
	        	}
	        	
			} else if (action != null && action.equals("com.monster.privacymanage.DELETE_ACCOUNT")) {
				boolean delete = intent.getBooleanExtra("delete", false);
				PrivacyUtils.mIsPrivacyMode = false;
				PrivacyUtils.setCurrentAccountId(0);
				PrivacyUtils.mCurrentAccountHomePath = null;
			}
			
			PrivacyUtils.killPrivacyActivity();
		}
	}
	
//	private void killTask(Context context, ActivityManager am) {
//        List<RecentTaskInfo> tasks = am.getRecentTasks(TASK_MAX, 0);
//        List<RunningTaskInfo> runTasks = am.getRunningTasks(TASK_MAX);
//        RunningTaskInfo topTask = null;
//        RunningTaskInfo secTask = null;
//        
//        if (runTasks != null && runTasks.size() > 0) {
//            topTask = runTasks.get(0);
//            if (runTasks.size() > 1) {
//                secTask = runTasks.get(1);
//            }
//        }
//        
//        if (tasks == null) {
//            tasks = new ArrayList<ActivityManager.RecentTaskInfo>();
//        }
//        
//        for (int i = 0; i < tasks.size(); i++) {
//            RecentTaskInfo taskInfo = tasks.get(i);
//            
//            if ((topTask != null && topTask.id == taskInfo.persistentId) || 
//                    ((secTask != null && secTask.id == taskInfo.persistentId))) {
//            } else {
//            	try {
//            		String packageName = taskInfo.origActivity.getPackageName();
//                	Log.e("wangth", "packageName = " + packageName);
//                    if (packageName.equals("com.android.contacts")) {
//                    	am.removeTask(taskInfo.persistentId, 0);
//                    }
//            	} catch (Exception e) {
//            		e.printStackTrace();
//            	}
//            }
//            
//        }
//    }

}
