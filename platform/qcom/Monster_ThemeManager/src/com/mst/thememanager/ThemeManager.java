package com.mst.thememanager;

import java.util.List;

import android.content.Context;

import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.listener.OnThemeApplyListener;
import com.mst.thememanager.listener.OnThemeLoadedListener;

public interface ThemeManager {
	
	/**
	 * Apply theme here.
	 * @param theme  The theme need to apply.
	 * @param context
	 * @return true if current theme is apply success.
	 */
	public boolean applyTheme(Theme theme,Context context,OnThemeApplyListener listener);
	
	/**
	 * Get apply status of target theme
	 * @param theme
	 * @return true if target theme applied.
	 */
	public boolean themeApplied(Theme theme);
	
	/**
	 * Get current applied theme's id,just work for
	 * the theme that type is {@link com.mst.thememanager.entities.Theme#THEME_PKG}
	 * @param context
	 * @return id of the theme applied
	 */
	public int getAppliedThemeId(Context context);
	
	/**
	 * Get current wallpaper's id
	 * @param context
	 * @return id of current wallpaper
	 */
	public int getAppliedWallpaperId(Context context);

	/**
	 * Get current fonts id
	 * @param context
	 * @return
	 */
	public int getAppliedFontsId(Context context);
	
	
	public int getAppliedRingTongId(Context context);
	
	/**
	 * If target theme has new version in server,update it.
	 * @param theme
	 * @return true if update success.
	 */
	public boolean updateThemeFromInternet(Theme theme);
	
	/**
	 * Update target theme status in database.
	 * @param theme
	 * @return true if update success.
	 */
	public boolean updateThemeinDatabase(Theme theme);
	
	/**
	 * Delete target theme.only for the theme not applied.
	 * @param theme
	 */
	public void deleteTheme(Theme theme);
	
	/**
	 * Delete target themes,only for the theme not applied.
	 * @param themes
	 */
	public void deleteTheme(List<Theme> themes);
	
	/**
	 * Load themes by theme type,load  from database.
	 * @param themeType see{@link com.mst.thememanager.entities.Theme#type},
	 *                  must be one of{@link com.mst.thememanager.entities.Theme#THEME_PKG},
	 *                  {@link com.mst.thememanager.entities.Theme#RINGTONG},
	 *                  {@link com.mst.thememanager.entities.Theme#WALLPAPER},or
	 *                  {@link com.mst.thememanager.entities.Theme#FONTS}
	 * @return Themes was loaded.
	 */
	public void loadThemes(int themeType);

	/**
	 * Load theme from theme file and save information into database
	 * @param themeType see{@link com.mst.thememanager.entities.Theme#type},
	 *                  must be one of{@link com.mst.thememanager.entities.Theme#THEME_PKG},
	 *                  {@link com.mst.thememanager.entities.Theme#RINGTONG},
	 *                  {@link com.mst.thememanager.entities.Theme#WALLPAPER},or
	 *                  {@link com.mst.thememanager.entities.Theme#FONTS}
	 * @return Theme was loaded.
	 */
	public void loadTheme(String themePath,int themeType);
	
	
	public void setThemeLoadListener(OnThemeLoadedListener listener);
	
	
	public void loadSystemTheme(int themeType);
	
}
