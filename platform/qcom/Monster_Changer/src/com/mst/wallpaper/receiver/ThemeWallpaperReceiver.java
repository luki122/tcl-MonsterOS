package com.mst.wallpaper.receiver;

import com.mst.wallpaper.MainWorker.OnRequestListener;
import com.mst.wallpaper.db.WallpaperDbColumns;
import com.mst.wallpaper.db.WallpaperDbController;
import com.mst.wallpaper.object.Wallpaper;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.WallpaperManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

public class ThemeWallpaperReceiver extends BroadcastReceiver implements OnRequestListener{

	private static final String TAG = "ThemeWallpaperReceiver";
	private static final String ACTION = "monster.intent.action.APPLY_WALLPAPER_FROM_THEME";
	private static final String KEY_WALLPAPER_FROM_THEME = "system_wallpaper_from_theme";
	private static final String KEY_KEYGUARD_WALLPAPER_FROM_THEME = "system_keyguard_wallpaper_from_theme";
	private WallpaperManager mWm;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		mWm = WallpaperManager.getInstance();
		
		if(ACTION.equals(action)){
			int wallpaperNumber = intent.getIntExtra(KEY_WALLPAPER_FROM_THEME, -1);
			String keyguardGroupName = intent.getStringExtra(KEY_KEYGUARD_WALLPAPER_FROM_THEME);
			if(wallpaperNumber != -1){
				if(wallpaperNumber < Config.LOCAL_DESKTOP_WALLPAPERS.length){
					mWm.addWallpaperHandleListener(this.getClass().getName(), this);
					String wallpaperPath = Config.LOCAL_DESKTOP_WALLPAPERS[wallpaperNumber];
					Wallpaper wallpaper = new Wallpaper();
					wallpaper.type = Wallpaper.TYPE_DESKTOP;
					wallpaper.addPaths(Config.SYSTEM_DESKTOP_WALLPAPER+wallpaperPath);
					mWm.applyWallpaper(wallpaper, context, this.getClass().getName(), wallpaperNumber);
				}
		        
			}
			if(!TextUtils.isEmpty(keyguardGroupName)){
				mWm.addWallpaperHandleListener(this.getClass().getName(), this);
				WallpaperDbController wdbc = new WallpaperDbController(context);
				Wallpaper w = wdbc.queryKeyguardWallpaperByName(keyguardGroupName);
				w.type = Wallpaper.TYPE_KEYGUARD;
				mWm.applyWallpaper(w, context, this.getClass().getName(), -1);
			}
		}

	}
	
	@Override
	public void onSuccess(Object data, Object t, int statusCode) {
		// TODO Auto-generated method stub
		mWm.removeWallpaperHandleListener(this.getClass().getName(), this);
	}
	@Override
	public void onStartRequest(Object data, int statusCode) {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	
	
	
	
	

}
