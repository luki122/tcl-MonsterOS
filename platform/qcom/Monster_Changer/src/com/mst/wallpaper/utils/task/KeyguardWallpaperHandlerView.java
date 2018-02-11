package com.mst.wallpaper.utils.task;

import java.util.List;

import android.view.View;

public interface KeyguardWallpaperHandlerView {

	public void onWallpaperIntentHandled(List<String> images,String name);
	
	public View getItemView(int position);
	
	public void refreshStatus(boolean finish);
	
}
