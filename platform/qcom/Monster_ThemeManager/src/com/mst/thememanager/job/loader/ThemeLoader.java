package com.mst.thememanager.job.loader;

import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.listener.OnThemeLoadedListener;

public interface ThemeLoader {
	
	public void loadTheme(String themeUrl);
	
	public void setThemeLoadListener(OnThemeLoadedListener listener);
	
	public void stop();

}
