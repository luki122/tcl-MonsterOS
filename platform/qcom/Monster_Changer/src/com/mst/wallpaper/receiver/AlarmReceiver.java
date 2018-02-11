package com.mst.wallpaper.receiver;


import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.FileUtils;
import com.mst.wallpaper.utils.WallpaperManager;
import com.mst.wallpaper.utils.WallpaperTimeUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver{
	
	private static final String TAG = "AlarmReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (Config.Action.ACTION_INTENT_ALARM.equals(intent.getAction())) {
			 if(Config.DEBUG)
			Log.d(KeyguardWallpaperChangeReceiver.LOCK_TAG, TAG + " -->receiver = ACTION_INTENT_ALARM");
			new MyAsyncTask().execute(context);
		}
	}
	
	private boolean refreshCurrentData(Context context) {
        boolean bool = false;
        String path = "";
        try {
            path = WallpaperTimeUtils.getCurrentKeyguardWallPaperPath(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(KeyguardWallpaperChangeReceiver.LOCK_TAG, TAG + " refreshData = "+path);
        if (path == null) {
            return bool;
        }
        
        bool = FileUtils.copyFile(path, Config.WallpaperStored.KEYGUARD_WALLPAPER_PATH, context);
      
        setNextAlarmManager(context);
        return bool;
    }

    private class MyAsyncTask extends AsyncTask<Context, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            return refreshCurrentData(params[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }
    
    private void setNextAlarmManager(Context context) {
        Intent intent = new Intent(Config.Action.ACTION_INTENT_ALARM);
        PendingIntent refreshIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        if (refreshIntent != null) {
            AlarmManager alarmManager = ( AlarmManager ) context.getSystemService(Context.ALARM_SERVICE);
            long nextTime = WallpaperTimeUtils.getNextTime();
            long firstTime = System.currentTimeMillis();
            if(Config.DEBUG)
            Log.d(KeyguardWallpaperChangeReceiver.LOCK_TAG, TAG+" firstTime=" + firstTime + ",nextTime=" + nextTime);
            alarmManager.cancel(refreshIntent);
            if (nextTime <= firstTime) {
                //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, firstTime + 5 * 1000, 1000 * 60 * 60 * 2, refreshIntent);
            	alarmManager.set(AlarmManager.RTC_WAKEUP, firstTime + 5 * 1000, refreshIntent);
            } else {
                //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, nextTime, 1000 * 60 * 60 * 2, refreshIntent);
            	alarmManager.set(AlarmManager.RTC_WAKEUP, nextTime, refreshIntent);
            }
        } else {
            /*RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();*/
            Log.d(KeyguardWallpaperChangeReceiver.LOCK_TAG, TAG+" pending intent is null");
        }
    }
}
