package com.mst.thememanager;

import java.io.File;
import java.util.List;

import android.content.Context;

import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.job.Task;
import com.mst.thememanager.job.ThemeManagerTask;
import com.mst.thememanager.listener.OnThemeApplyListener;
import com.mst.thememanager.listener.OnThemeLoadedListener;
import com.mst.thememanager.utils.Config;
import com.mst.thememanager.utils.FileUtils;

public class ThemeManagerImpl implements ThemeManager {
	
	private static final Object mLock = new Object();
	
	private static  ThemeManager mInstance;
	
	private Task mThemeTask;
	
	private ThemeManagerImpl(Context context){
		
		mThemeTask = new ThemeManagerTask(context);
	}
	
	public static ThemeManager getInstance(Context context){
		synchronized (mLock) {
			if(mInstance == null){
				mInstance = new ThemeManagerImpl(context);
			}
			return mInstance;
		}
	}

	@Override
	public boolean applyTheme(Theme theme, Context context,OnThemeApplyListener listener) {
		// TODO Auto-generated method stub
		return mThemeTask.applyTheme(theme, context,listener);
	}

	@Override
	public boolean themeApplied(Theme theme) {
		// TODO Auto-generated method stub
		return mThemeTask.themeApplied(theme);
	}

	@Override
	public int getAppliedThemeId(Context context) {
		// TODO Auto-generated method stub
		return mThemeTask.getAppliedThemeId(context);
	}

	@Override
	public int getAppliedWallpaperId(Context context) {
		// TODO Auto-generated method stub
		return mThemeTask.getAppliedWallpaperId(context);
	}

	@Override
	public int getAppliedFontsId(Context context) {
		// TODO Auto-generated method stub
		return mThemeTask.getAppliedFontsId(context);
	}

	@Override
	public int getAppliedRingTongId(Context context) {
		// TODO Auto-generated method stub
		return mThemeTask.getAppliedRingTongId(context);
	}

	@Override
	public boolean updateThemeFromInternet(Theme theme) {
		// TODO Auto-generated method stub
		return mThemeTask.updateThemeFromInternet(theme);
	}

	@Override
	public boolean updateThemeinDatabase(Theme theme) {
		// TODO Auto-generated method stub
		return mThemeTask.updateThemeinDatabase(theme);
	}

	@Override
	public void deleteTheme(Theme theme) {
		// TODO Auto-generated method stub
		mThemeTask.deleteTheme(theme);
	}

	@Override
	public void deleteTheme(List<Theme> themes) {
		// TODO Auto-generated method stub
		mThemeTask.deleteTheme(themes);
	}

	@Override
	public void loadThemes(int themeType) {
		// TODO Auto-generated method stub
		 mThemeTask.loadThemes(themeType);
	}

	@Override
	public void loadTheme(String themePath,int themeType) {
		// TODO Auto-generated method stub
		 mThemeTask.loadTheme(themePath, themeType);
	}

	@Override
	public void setThemeLoadListener(OnThemeLoadedListener listener) {
		// TODO Auto-generated method stub
		mThemeTask.setThemeLoadListener(listener);
	}

	@Override
	public void loadSystemTheme(int themeType) {
		// TODO Auto-generated method stub
		mThemeTask.loadSystemTheme(themeType);
	}

}
