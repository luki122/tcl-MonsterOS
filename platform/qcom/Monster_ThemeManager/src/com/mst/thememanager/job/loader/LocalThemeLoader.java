package com.mst.thememanager.job.loader;

import android.content.Context;

import com.mst.thememanager.ThemeManager;
import com.mst.thememanager.ThemeManagerApplication;
import com.mst.thememanager.ThemeManagerImpl;
import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.listener.OnThemeLoadedListener;

public class LocalThemeLoader implements ThemeLoader {

	
	private ThemeManager mManager;
	
	private Context mContext;
	
	public  LocalThemeLoader(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
		mManager = ((ThemeManagerApplication)context.getApplicationContext()).getThemeManager();
		mManager.loadSystemTheme(Theme.THEME_PKG);
	}
	
	@Override
	public void loadTheme(String themeUrl) {
		// TODO Auto-generated method stub
		 mManager.loadTheme(themeUrl,Theme.THEME_PKG);
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setThemeLoadListener(OnThemeLoadedListener listener) {
		// TODO Auto-generated method stub
		mManager.setThemeLoadListener(listener);
	}
	
	

}
