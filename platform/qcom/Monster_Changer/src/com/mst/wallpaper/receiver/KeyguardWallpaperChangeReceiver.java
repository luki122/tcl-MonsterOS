package com.mst.wallpaper.receiver;


import com.mst.wallpaper.db.SharePreference;
import com.mst.wallpaper.utils.CommonUtil;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.FileUtils;
import com.mst.wallpaper.utils.WallpaperConfigUtil;
import com.mst.wallpaper.utils.WallpaperTimeUtils;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;

public class KeyguardWallpaperChangeReceiver extends BroadcastReceiver {
	
	public static final String LOCK_TAG = "KeyguardWallpaperChangeReceiver";
	private static final String TAG = "KeyguardWallpaperChangeReceiver";
	

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if(Config.DEBUG)
        Log.d(LOCK_TAG, TAG + " action=" + action);
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            setNextAlarmManager(context);
            CommonUtil.chmodFile(Config.PATH_DATA_MONSTER);
			CommonUtil.chmodFile(Config.PATH_DATA_MONSTER_WALLPAPER);
			CommonUtil.chmodFile(Config.PATH_DATA_MONSTER_WALLPAPER_KEYGUARD);
            refreshCurrentData(context);
        } else if (action.equals(Config.Action.ACTION_COPY_FILE)) {
            new MyAsyncTask().execute(context);
        } else if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
        	new MyAsyncTask().execute(context);
            setNextAlarmManager(context);
        } else if (action.equals(Config.Action.ACTION_RESET_ALARM)) {
        	setNextAlarmManager(context);
        	//request next day keyguard wallpaper from internet
		} else if (action.equals(Config.Action.ACTION_CHMOD_FILE)) {
			CommonUtil.chmodFile(Config.WallpaperStored.KEYGUARD_WALLPAPER_PATH);
		} else if (action.equals(Intent.ACTION_WALLPAPER_CHANGED)) {
			Config.isWallPaperChanged = true;
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            if (Config.isChangedByLocal == 0 && (!sp.getString(SharePreference.KEY_SELECT_DESKTOP_PATH, "-1")
            		.equals("-1") || sp.getInt(SharePreference.KEY_SELECT_DESKTOP_POSITION, -1) != -1)) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(SharePreference.KEY_SELECT_DESKTOP_PATH, "-1");
                editor.putInt(SharePreference.KEY_SELECT_DESKTOP_POSITION, -1);
                editor.commit();
                Config.isChangedByLocal = 1;
            }
        } else if (action.equals(Config.Action.ACTION_WALLPAPER_SET)) {
        	Config.isChangedByLocal = 2;
        	
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
        if(Config.DEBUG)
        Log.d(LOCK_TAG, TAG + " refreshData = "+path);
        if (path == null) {
            return bool;
        }
        
        bool = FileUtils.copyFile(path, Config.WallpaperStored.KEYGUARD_WALLPAPER_PATH, context);
        
        return bool;
    }

    private class MyAsyncTask extends AsyncTask<Context, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
        	 if(Config.DEBUG)
        	Log.d(LOCK_TAG, TAG + "-->MyAsyncTask ：doInBackground");
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
            alarmManager.cancel(refreshIntent);
            long nextTime = WallpaperTimeUtils.getNextTime();
            long firstTime = System.currentTimeMillis();
            if(Config.DEBUG)
            Log.d(LOCK_TAG, TAG+" firstTime=" + firstTime + ",nextTime=" + nextTime);
            if (nextTime <= firstTime) {
            	alarmManager.set(AlarmManager.RTC_WAKEUP, firstTime + 5 * 1000, refreshIntent);
            } else {
            	alarmManager.set(AlarmManager.RTC_WAKEUP, nextTime, refreshIntent);
            }
        } else {
            Log.d(LOCK_TAG, TAG+" pending intent is null");
        }
    }
}
