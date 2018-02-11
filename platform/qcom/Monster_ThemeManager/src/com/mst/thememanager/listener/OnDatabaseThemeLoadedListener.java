package com.mst.thememanager.listener;

import java.util.List;

import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.listener.OnThemeLoadedListener;

public interface OnDatabaseThemeLoadedListener extends OnThemeLoadedListener {

	public void onThemeLoaded(boolean loaded,List<Theme> themes);
}
