package com.mst.thememanager.database;

import com.mst.thememanager.entities.Theme;

import android.content.Context;

public class DatabaseFactory {
	
	/**
	 * Create Database controller.
	 * @param type
	 * @return
	 */
	public static ThemeDatabaseController<Theme> createDatabaseController(int type,Context context){
		if(type == Theme.THEME_PKG){
			return new ThemePkgDbController(context);
		}
		return null;
	}

	
	
}
