package com.mst.thememanager.utils;

import android.os.Environment;

public class Config {

	public static final boolean DEBUG = true;
	
	public static final String LOCAL_THEME_PATH = "sdcard/monster/theme/";
	
	public static final String LOCAL_THEME_PACKAGE_PATH = LOCAL_THEME_PATH+"pkg/";
	
	public static final String LOCAL_THEME_FONTS_PATH = LOCAL_THEME_PATH+"fonts/";
	
	public static final String LOCAL_THEME_RINGTONG_PATH = LOCAL_THEME_PATH+"ringtong/";
	
	public static final String LOCAL_THEME_WALLPAPER_PATH = LOCAL_THEME_PATH+"wallpaper/";
	
	public static final String LOCAL_THEME_DESCRIPTION_FILE_NAME = "description.xml";
	
	public static final String LOCAL_THEME_PREVIEW_DIR_NAME = "previews/";
	public static final String THEME_EXTRACT_TMP_DIR = "sdcard/monster/theme/tmp/";
	public static final String THEME_APPLY_DIR = "/data/monster/theme/current/";
	public static final String THEME_APPLY_ICON_DIR = "/data/monster/theme/current/icons/";
	public static final String THEME_APPLY_ICON_TEMP_DIR = THEME_EXTRACT_TMP_DIR+"icons/";
	public static final String THEME_DEFAULT_PATH = "/system/monster/theme/system_default/default/";
	public static final String THEME_APPLY_ICON_BACKUP_DIR = THEME_DEFAULT_PATH+"icons/";
	/**
	 * Path for saved loaded theme's informations .
	 */
	public static final String LOCAL_THEME_INFO_DIR = "sdcard/monster/theme/.data/";
	
	public static final String LOCAL_THEME_PKG_INFO_DIR =  LOCAL_THEME_INFO_DIR+"pkg/";
	
	public static final String SYSTEM_THEME_LOADED_DIR = "system/monster/theme/system_load/";
	
	public static final String SYSTEM_THEME_DIR = "system/monster/theme/system_default/";
	
	
	public static final String LAUNCHER_THEME_PKG_NAME = "com.monster.launcher.theme";
	
	public static final String LAUNCHER_THEME_APK_NAME = THEME_EXTRACT_TMP_DIR+"monster_launcher_theme.apk";
	
	public static final String LAUNCHER_PKG_NAME="com.monster.launcher";
	
	public static final String LAUNCHER_COMPONENT_NAME = "com.monster.launcher.Launcher";

	public static final int DEFAULT_THEME_ID =  -1 ;
	
	public static final String DEFAULT_THEME_COVER = "default_theme_previews/1.png";
	
	public static final String DEFAUTL_THEME_KEYGUARD_WALLPAPER = "Elegant";
	
	public static final int DEFAULT_THEHE_DESKTOP_WALLPAPER = 0;
	
	public static final String[] DEFAUTL_THEME_PREVIEWS = {
		"default_theme_previews/1.png",
		"default_theme_previews/2.png",
		"default_theme_previews/3.png"
	};
	
	/**
	 *Information for parse Theme from sdcard.
	 *These tags are declared in description.xml 
	 */
	public static final class ThemeDescription{
		/**
		 * Theme Designer
		 */
		public static final String TAG_DESIGNER = "Designer";
		
		/**
		 * Theme Name
		 */
		public static final String TAG_NAME = "Name";
		
		/**
		 * Theme Description
		 */
		public static final String TAG_DESCRIPTION = "Description";
		
		/**
		 * Theme Package Size
		 */
		public static final String TAG_SIZE = "Size";
		
		/**
		 * Theme Version
		 */
		public static final String TAG_VERSION = "Version";
		
		public static final String TAG_WALLPAPER = "System_Wallpaper";
		
		public static final String TAG_KEYGUARD_WALLPAPER = "System_LockScreen_Wallpaper";
	}
	
	public static final class BUNDLE_KEY{ 
		public static final String KEY_THEME_PKG_DETAIL = "theme:theme_detail";
	}
	
	public static  class DatabaseColumns{
		public static final String _ID = "_id";
		
		public static final String NAME = "name";
		
		public static final String DESGINER = "desginer";
		
		public static final String DESCRIPTION = "description";
		
		/**
		 * Real Theme file path
		 */
		public static final String FILE_PATH = "file_path";
		
		/**
		 * Theme information's saved path
		 */
		public static final String LOADED_PATH = "loaded_path";
		
		public static final String LOADED = "loaded";

		/**
		 * Theme type, one of{@link com.mst.thememanager.entities.Theme#THEME_PKG},{@link com.mst.thememanager.entities.Theme#RINGTONG},
		 * {@link com.mst.thememanager.entities.Theme#WALLPAPER},or {@link com.mst.thememanager.entities.Theme#FONTS}
		 */
		public static final String TYPE = "type";
		
		/**
		 * Version Code for current theme.
		 */
		public static final String VERSION = "version";
		
		public static final String LAST_MODIFIED_TIME = "last_modified_time";
		
		public static final String URI = "url";
		
		public static final String APPLY_STATUS = "apply_status";
		
		public static final String DOWNLOAD_STATUS = "download_status";
		
		public static final String TOTAL_BYTES = "total_bytes";
		
		public static final String CURRENT_BYTES = "current_bytes";
		
		public static final String SYSTEM_WALLPAPER_NUMBER = "system_wallpaper";
		
		public static final String SYSTEM_KEYGUARD_WALLPAPER_NAME = "system_keyguard_wallpaper";
		
	}
	
	public static class ThemeApplyStatus{
		public static final int STATUS_CURRENT_APPLIED = 0;
		public static final int STATUS_FAILED = 1;
		public static final int STATUS_APPLING = 2;
		public static final int STATUS_SUCCESS = 3;
		public static final int STATUS_THEME_FILE_NOT_EXITS = 4;
	}
	
}
