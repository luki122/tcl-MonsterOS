package com.mst.thememanager.ui.fragment.themedetail;

import java.util.List;

import android.graphics.Bitmap;

import com.mst.thememanager.MvpView;
import com.mst.thememanager.entities.Theme;

public interface ThemePkgDetailMVPView extends MvpView {
	
	/**
	 * Method for test mvp view,delete later
	 */
	void updatePreview(Bitmap previewBitmap);
	
	
	void updateThemeInfo(Theme theme);
	
	
	void updateTheme(Theme theme);

}
