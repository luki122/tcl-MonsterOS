package com.mst.thememanager;

import java.io.File;

import com.mst.thememanager.database.DatabaseFactory;
import com.mst.thememanager.database.ThemeDatabaseController;
import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.utils.Config;

import android.app.Application;

public class ThemeManagerApplication extends Application {
	
	
	private ThemeManager mThemeManager;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		getThemeManager();
		loadInternalTheme();
	}

	public void loadInternalTheme(){
		
		File systemThemeLoadDir = new File(Config.SYSTEM_THEME_LOADED_DIR);
		if(!systemThemeLoadDir.exists()){
			return;
		}
		mThemeManager.loadSystemTheme(Theme.THEME_PKG);
	}
	
	public ThemeManager getThemeManager(){
		if(mThemeManager == null){
			mThemeManager = ThemeManagerImpl.getInstance(this);
		}
		return mThemeManager;
	}
	
}
