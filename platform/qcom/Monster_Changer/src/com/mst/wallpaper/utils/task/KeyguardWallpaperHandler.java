package com.mst.wallpaper.utils.task;

import java.util.List;

import android.content.Intent;

public interface KeyguardWallpaperHandler {

	
	
	
	public void handleIntent(Intent intent);
	
	public void cropWallpaper(String wallpaperName,int currentItem,List<String> images);

}
