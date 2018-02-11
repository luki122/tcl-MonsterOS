package com.mst.wallpaper;

import java.io.File;

import com.mst.wallpaper.db.WallpaperDbController;
import com.mst.wallpaper.utils.Config;
import com.mst.wallpaper.utils.FileUtils;

import android.app.Application;
import android.util.Log;

public class WallpaperApplication extends Application {
	
	private MainWorker mMainWorker;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mMainWorker = MainWorker.getMainWorker();
		mMainWorker.setContext(getApplicationContext());
		WallpaperDbController db = new WallpaperDbController(this);
		db.openDb(this);
		File f = new File(Config.WALLPAPER_STOTED_PATH);
		f.mkdir();
		if(!FileUtils.fileExists(Config.WALLPAPER_STOTED_PATH)){
			FileUtils.createDirectory(Config.WALLPAPER_STOTED_PATH);
		}
		
		if(!FileUtils.fileExists(Config.WallpaperStored.DEFAULT_SDCARD_KEYGUARD_WALLPAPER_PATH)){
			FileUtils.createDirectory(Config.WallpaperStored.DEFAULT_SDCARD_KEYGUARD_WALLPAPER_PATH);
		}
	}
	

}
