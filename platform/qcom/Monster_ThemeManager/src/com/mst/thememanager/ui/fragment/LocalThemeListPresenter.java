package com.mst.thememanager.ui.fragment;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.mst.thememanager.BasePresenter;
import com.mst.thememanager.ThemeManager;
import com.mst.thememanager.ThemeManagerApplication;
import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.job.loader.LocalThemeLoader;
import com.mst.thememanager.job.loader.ThemeLoader;
import com.mst.thememanager.listener.OnThemeLoadedListener;
import com.mst.thememanager.utils.Config;
import com.mst.thememanager.utils.TLog;

public class LocalThemeListPresenter extends BasePresenter<ThemeListMVPView> implements OnThemeLoadedListener{
	private static final String TAG = "ThemeListPresenter";
	private ThemeManager mThemeManager;
	
	public LocalThemeListPresenter(Context context){
		mThemeManager =  ((ThemeManagerApplication)context.getApplicationContext()).getThemeManager();
		mThemeManager.setThemeLoadListener(this);
	}
	
	/**
	 * Load theme package from sdcard.
	 */
	public void loadTheme(){
		mThemeManager.loadThemes(Theme.THEME_PKG);
	}

	@Override
	public void onDestory() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onThemeLoaded(boolean loaded, Theme theme) {
		// TODO Auto-generated method stub
		if(theme != null){
			getMvpView().updateThemeList(theme);
		}
	}

	
	
}
