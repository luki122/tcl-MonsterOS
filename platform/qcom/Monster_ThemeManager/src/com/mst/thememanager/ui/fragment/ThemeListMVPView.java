package com.mst.thememanager.ui.fragment;

import java.util.List;

import com.mst.thememanager.MvpView;
import com.mst.thememanager.entities.Theme;

public interface ThemeListMVPView extends MvpView {
	
	/**
	 * Method for test mvp view,delete later
	 */
	void updateThemeList(Theme theme);

}
