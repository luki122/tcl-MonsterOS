package com.mst.wallpaper.db;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class SharePreference {

	private static final String NAME = "Monster_wallpaper";
	public static final String KEY_SELECT_DESKTOP_POSITION = "select_position";
	public static final String KEY_SELECT_DESKTOP_PATH = "select_path";
	public static final String KEY_SELECT_KEYGUARD_ID = "select_keyguard_position";
	public static final String KEY_SELECT_KEYGUARD_NAME = "select_keyguard_name";
	
	
	public static void setBooleanPreference(Context context, String key, boolean value) {
			SharedPreferences preferences = context.getSharedPreferences(NAME, 
	        		Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
	        SharedPreferences.Editor editor = preferences.edit();
	        editor.putBoolean(key, value);
	        editor.commit();
	    }
	    
	    public static boolean getBooleanPreference(Context context, String key, boolean defaultValue) {
	        SharedPreferences preferences = context.getSharedPreferences(NAME, 
	        		Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
	        boolean result = preferences.getBoolean(key, defaultValue);
	        return result;
	    }
	    
	    public static void setIntPreference(Context context, String key, int value) {
	        SharedPreferences preferences = context.getSharedPreferences(NAME, 
	        		Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
	        SharedPreferences.Editor editor = preferences.edit();
	        editor.putInt(key, value);
	        editor.commit();
	    }
	    
	    public static int getIntPreference(Context context, String key, int defaultValue) {
	        SharedPreferences preferences = context.getSharedPreferences(NAME, 
	        		Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
	        int result = preferences.getInt(key, defaultValue);
	        return result;
	    }
	    
	    public static void setStringPreference(Context context, String key, String value) {
	        SharedPreferences preferences = context.getSharedPreferences(NAME, 
	        		Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
	        SharedPreferences.Editor editor = preferences.edit();
	        editor.putString(key, value);
	        editor.commit();
	    }
	    
	    public static String getStringPreference(Context context, String key, String defaultValue) {
	        SharedPreferences preferences = context.getSharedPreferences(NAME, 
	        		Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
	        String result = preferences.getString(key, defaultValue);
	        return result;
	    }
	    
	    public static void saveDesktopWallpaper(Context context,int selectPosition,String wallpaperPath){
	    	SharedPreferences preferences = context.getSharedPreferences(NAME, 
	        		Context.MODE_MULTI_PROCESS | Context.MODE_WORLD_READABLE);
	        SharedPreferences.Editor editor = preferences.edit();
	        editor.putInt(KEY_SELECT_DESKTOP_POSITION, selectPosition);
	        editor.putString(KEY_SELECT_DESKTOP_PATH, wallpaperPath);
	        editor.commit();
	    }
	    
}
