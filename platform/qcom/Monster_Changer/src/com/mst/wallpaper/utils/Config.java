package com.mst.wallpaper.utils;

import java.io.File;

import android.net.Uri;
import android.os.Environment;

public final class Config {

	private Config() {
	}

	public static final boolean DEBUG = false;

	public static int isChangedByLocal = 0;
	public static boolean isWallPaperChanged = false;

	public static final String IS_FIRST_TIME_START = "isFirstTimeStart";

	public static final String WALLPAPER_STOTED_PATH = Environment
			.getExternalStorageDirectory() + "/Wallpaper";

	public static final String SYSTEM_WALL_PAPER_PATH = "system/monster/wallpaper/";

	public static final String SYSTEM_DESKTOP_WALLPAPER = SYSTEM_WALL_PAPER_PATH
			+ "desktop/";

	public static final String SYSTEM_KEYGUARD_WALLPAPER = SYSTEM_WALL_PAPER_PATH
			+ "keyguard/";

	public static final String WALLPAPER_THUMB_CACHE = "wallpaper_cache";

	public static final String WALLPAPER_PREVIEW_IMAGE_CACHE_DIR = "preview_thumbs";

	public static final String SYSTEM_DESKTOP_WALLPAPER_PATH = SYSTEM_WALL_PAPER_PATH
			+ "desktop/";

	public static String SYSTEM_DESKTOP_DEFAULT_WALLPAPER ;
	
	public static final String KEYGUARD_WALLPAPER_CACHE_DIR = "thumbs";
	
	
	public static final String PATH_DATA_MONSTER = "/sdcard/monster/";
    public static final String PATH_DATA_MONSTER_WALLPAPER = PATH_DATA_MONSTER+"wallpaper/";
    public static final String PATH_DATA_MONSTER_WALLPAPER_KEYGUARD = PATH_DATA_MONSTER_WALLPAPER
    		+"keyguard/";
	
	/**
	 * Default system desktop wallpaper names
	 */
	public static String[] LOCAL_DESKTOP_WALLPAPERS ;/* = {
			"wallpaper_01.jpg", "wallpaper_02.jpg", "wallpaper_03.jpg",
			"wallpaper_04.jpg", "wallpaper_05.jpg", "wallpaper_06.jpg",
			"wallpaper_07.jpg", "wallpaper_08.jpg", "wallpaper_09.jpg" 
			};*/
	
	static{
		File systemDesktopWallpaperDir = new File("system/monster/wallpaper/desktop/");
		if(systemDesktopWallpaperDir.exists()){
			LOCAL_DESKTOP_WALLPAPERS = systemDesktopWallpaperDir.list();
			SYSTEM_DESKTOP_DEFAULT_WALLPAPER = SYSTEM_DESKTOP_WALLPAPER+LOCAL_DESKTOP_WALLPAPERS[0];
		}
	}
	/**
	 * 壁纸实时预览需要显示的图标(底部)
	 */
	public static final String[] WALLPAPER_PREVIEW_BOTTOM_ICONS={
		"com.android.dialer",//电话
		"com.android.contacts",//短信
		"com.android.chrome"//浏览器
	};
	
	
	/**
	 * 壁纸实时预览需要显示的图标(顶部)
	 */
	public static final String[] WALLPAPER_PREVIEW_TOP_ICONS={
		"com.tct.camera",//相机
		"com.android.gallery3d",//相册
		"cn.tcl.music",//音乐
		"cn.tcl.note",//备忘录
		"com.android.calendar",//日历
		"com.android.deskclock"//闹钟
	};
	
	
	
	public static final class HandlerIntMessage {

		public static final int MSG_IMAGE_LOAD_DONE = 0x01;

		public static final int MSG_IMAGE_LOAD_ERROR = 0x02;

	}

	/**
	 * Intent使用到的所有Action和Key
	 * 
	 *
	 */
	public static class Action {
		/**
		 * Action to show desktop wallpaper list
		 */
		public static final String ACTION_DESKTOP_WALLPAPER_LIST = "com.mst.wallpaper.DESKTOP_WALLPAPER_LIST";
		/**
		 * Action to show keyguard wallpaper list
		 */
		public static final String ACTION_KEYGUARD_WALLPAPER_LIST = "com.mst.wallpaper.KEYGUARD_WALLPAPER_LIST";

		public static final String ACTION_WALLPAPER_SET = "com.mst.action.ACTION_WALLPAPER_SET";
		
		public static final String ACTION_COPY_FILE = "com.mst.wallpaper.ACTION_COPY_FILE";
		
		public static final String ACTION_CHMOD_FILE = "com.mst.wallpaper.CHMOD_FILE";
		
		public static final String ACTION_RESET_ALARM = "com.mst.wallpaper.RESET_ALARM";
		
		public static final String ACTION_INTENT_ALARM = "com.mst.wallpaper.alarm.ACTION_INTENT_ALARM";
		
		public static final String ACTION_KEYGUARD_WALLPAPER_CHANGED = "com.mst.wallpaper.alarm.wallpaperupdate";
		

		public static final String KEY_WALLPAPER_PREVIEW_POSITION = "preview_position";
		
		public static final String KEY_WALLPAPER_PREVIEW_WIDGET_INIT_COLOR = "init_widget_color";

		public static final String KEY_WALLPAPER_PREVIEW_TYPE = "wallpaper_type";

		public static final String KEY_WALLPAPER_PREVIEW_DATA_LIST = "wallpaper_list";
		
		public static final String KEY_KEYGUARD_WALLPAPER_PREVIEW_IAMGE_POSITION = "image_position";
		
		public static final String KEY_KEYGUARD_WALLPAPER_PREVIEW_POSITION_IN_LIST = "position_in_list";
		
		

	}

	/**
	 * 加载本地壁纸的所有常量
	 *
	 */
	public static class LocalWallpaperStatus {
		public static final int STATUS_LOAD_SUCCESS = 0;

		public static final int STATUS_LOAD_FAILURE = 1;

	}

	/**
	 *设置壁纸成功或者不成功的状态
	 *
	 */
	public static class SetWallpaperStatus{
		
		public static final int STATUS_SUCCESS = 0;
		
		public static final int STATUS_WALLPAPER_EMPTY = 1;
		
		public static final int STATUS_FAILED = 2;
		
		public static final int STATUS_UNKOWNE_WALLPAPER_TYPE = 4;
		
		public static final int STATUS_WALLPAPER_APPLIED = 5;
		
	}
	
	/**
	 *
	 * 处理的Bitmap的来源
	 */
	public static class BitmapSource {
		public static final int DISK = 0;

		public static final int INTENT = 1;

		public static final int INTENET = 2;
	}

	/**
	 * 壁纸的存储属性
	 *
	 */
	public static class WallpaperStored {
		// wallpaper_set.xml文件名
		public static final String WALLPAPER_SET_FILE = "wallpaper_set.xml";
		// keyguard_set.xml文件名
		public static final String KEYGUARD_SET_FILE = "lockpaper_set.xml";

		public static final String WALLPAPER_VERSION = "wallpaper_version";
		public static final String WALLPAPER_GROUP_POSITION_KEY = "group_position";

		public static final String WALLPAPER_KEYGUARD_TYPE = "keyguard";
		public static final String WALLPAPER_DESKTOP_TYPE = "wallpaper";
		// 锁屏壁纸存放的地址
		public static final String KEYGUARD_WALLPAPER_PATH = "/data/monster/wallpaper/keyguard/wallpaper.png";
		public static final String DEFAULT_SYSTEM_KEYGUARD_WALLPAPER_PATH = "/system/monster/wallpaper/keyguard/";
		public static final String DEFAULT_SYSTEM_DESKTOP_WALLPAPER_PATH = "/monster/wallpaper/desktop/";
		public static final String DEFAULT_SDCARD_KEYGUARD_WALLPAPER_PATH = "/mnt/sdcard/monster/wallpaper/keyguard/";
		public static final String DEFAULT_SYSTEM_KEYGUARD_WALLPAPER_FLAG = "/system/monster/";
		public static final String DEFAULT_KEYGUARD_FILE_NAME = "Custom";

		public static final String KEYGUARD_WALLPAPER_NAME_KEY = "name";
		public static final String KEYGUARD_WALLPAPER_CROP_TYPE = "crop_type";
		public static final String KEYGUARD_WALLPAPER_CROP_SOURCE = "crop_from_source";

		public static final String CURRENT_KEYGUARD_WALLPAPER = "current_keyguard_group";
		public static final String CURRENT_KEYGUARD_WALLPAPER_ID = "current_keyguard_group_id";

		// time of current group is black
		public static final String CURRENT_KEYGUARD_GROUP_TIME_BLACK = "current_keyguard_group_time_black";
		// time of current group is black
		public static final String CURRENT_KEYGUARD_GROUP_STATUS_BLACK = "current_keyguard_group_status_black";

		public static final String NEXTDAY_WALLPAPER_PATH = "/mnt/sdcard/monster/.keyguard/";

		public static final String WALLPAPER_PATH = Environment
				.getExternalStorageDirectory() + "/monster/wallpaper/desktop/";
		public static final String WALLPAPER_ASSETS_DIR = "wallpaper/";
		public static final String WALLPAPER_ID = "_id";
		public static final String WALLPAPER_BLACK_WIDGET = "black_widget";
		public static final String WALLPAPER_MODIFIED = "modified";
		public static final String WALLPAPER_OLDPATH = "oldpath";
		public static final String WALLPAPER_FILENAME = "filename";
		public static final String WALLPAPER_SELECTED = "seleted";
		public static final String WALLPAPER_URI_TYPE = "vnd.android.cursor.dir/wallpaper";
		public static final Uri LOCAL_WALLPAPER_URI = Uri
				.parse("content://com.mst.wallpaper.provider/wallpaper");

		public static final  String DEFAULT_KEYGUARD_GROUP = "Elegant";

		// the picture date for nextday
		public static final String NEXTDAY_PICTURE_DATE = "picture_date";

		// path for wallpaper saved by nextday
		public static final String NEXTDAY_WALLPAPER_SAVED = "/mnt/sdcard/monster_wallpaper/";
		// nextday wallpaper operation type
		public static final String NEXTDAY_OPERATION_WAKEUP = "wakeup";
		public static final String NEXTDAY_OPERATION_SAVE = "save";
		public static final String NEXTDAY_OPERATION_SET = "set";
		public static final String NEXTDAY_OPERATION_SHARE = "share";

		public static final String NEXTDAY_HTTP_TYPE_INIT = "init";
		public static final String NEXTDAY_HTTP_TYPE_PREVIEW = "preview";
		public static final String NEXTDAY_HTTP_TYPE_SHOW = "show";

		public static final String NEXTDAY_PICTURE_LOADTYPE_NONE = "none";
		public static final String NEXTDAY_PICTURE_LOADTYPE_INFO = "info";
		public static final String NEXTDAY_PICTURE_LOADTYPE_PICTURE = "picture";
		public static final String NEXTDAY_PICTURE_LOADTYPE_DOWNLOAD = "download";
		// string data for app initiate
		public static final String NEXTDAY_INITDATA_KEY = "imei";
		// string data for app initiate
		public static final String NEXTDAY_INITDATA_VALUE = "008600215140400";

		// the wifi setting for nextday
		public static final String NEXTDAY_WIFI_NETWORK_SETTINGS = "only_wifi_enable";
		// the loading tips for nextday
		public static final String NEXTDAY_LOADING_ANYWAY = "loading_anyway";
		// the loading tips for nextday
		public static final String NEXTDAY_SHOW_COMMENTS = "show_comments";

		public static final int KEYGUARD_WALLPAPER_IS_SYSTEM = 1;
	}
	
	/**
	 * Colors for invoke by java
	 *
	 */
	public static class Color{
		public static final int COLOR_WHITE = 0xFFFFFFFF;
	}

}
