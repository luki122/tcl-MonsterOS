package com.mst.thememanager.database;

import java.util.List;

import com.mst.thememanager.entities.Theme;

public interface ThemeDatabaseController<T extends Theme> {

	public T getThemeById(int themeId);
	
	public List<T> getThemesByType(int themeType);
	
	public boolean isLoaded(int themeId);
	
	public boolean updateTheme(T t);
	
	public boolean deleteTheme(T t);
	
	public boolean deleteTheme(int themeId);
	
	public void insertTheme(T theme);
	
	public void close();
	
	public void open();
	
	public int getCount();
	
	public int getCountByType(int type);
}
