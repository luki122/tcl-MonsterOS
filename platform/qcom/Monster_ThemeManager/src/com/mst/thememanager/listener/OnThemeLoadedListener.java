package com.mst.thememanager.listener;

import com.mst.thememanager.entities.Theme;

public interface OnThemeLoadedListener {
	
	public void onThemeLoaded(boolean loaded,Theme theme);

}
