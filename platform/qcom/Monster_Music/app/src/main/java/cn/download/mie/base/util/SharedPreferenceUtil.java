package cn.download.mie.base.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Util class deal with preference.
 *  
 *
 */
public class SharedPreferenceUtil {

	private static final String LEHOSPFILE = "LEHO";

    private SharedPreferenceUtil(){

    }

	/**
	 * Save string key value to the preference.
	 * @param context from which invoke this method.
	 * @param key preference key 
	 * @param value preference value
	 * @return true if save successfully.
	 */
	public static boolean save(Context context, String key,String value) {
		Editor editor = context.getSharedPreferences(LEHOSPFILE,
				Context.MODE_PRIVATE).edit();
		editor.putString(key, value);
		return editor.commit();
	}
	
	/**
	 * Easy understanding way of save(Context context,String key,String value);
	 * @param context from which invoke this method.
	 * @param key preference key 
	 * @param value preference value
	 * @return true if save successfully.
	 */
	public static boolean saveString(Context context, String key,String value) {
		Editor editor = context.getSharedPreferences(LEHOSPFILE,
				Context.MODE_PRIVATE).edit();
		editor.putString(key, value);
		return editor.commit();
	}
	
	/**
	 * Read string from preference.
	 * @param context from which invoke this method.
	 * @param key preference key 
	 * @return preference value of given key
	 */
	public static String readString(Context context, String key){
		SharedPreferences sp = context.getSharedPreferences(LEHOSPFILE, Context.MODE_PRIVATE);
		return sp.getString(key, "");
	}

    /**
     * Save long key value to the preference.
     * @param context from which invoke this method.
     * @param key preference key
     * @param value preference value
     */
    public  static  void   saveLong(Context context,String  key,long  value)
    {
        Editor editor = context.getSharedPreferences(LEHOSPFILE,
                Context.MODE_PRIVATE).edit();
        editor.putLong(key,value);
        editor.commit();
    }
	
	/**
	 * read long from preference
	 * @param context
	 * @param key
	 * @param defaultvalue
	 */
	public  static  long   readLong(Context  context,String key,long  defaultvalue)
	{
		SharedPreferences sp = context.getSharedPreferences(LEHOSPFILE, Context.MODE_PRIVATE);
		return sp.getLong(key, defaultvalue);
	}
	
	/**
	 * Save int key value to the preference.
	 * @param context from which invoke this method.
	 * @param key preference key 
	 * @param value preference value
	 * @return true if save successfully.
	 */
	public static boolean saveInt(Context context, String key,int value) {
		Editor editor = context.getSharedPreferences(LEHOSPFILE,
				Context.MODE_PRIVATE).edit();
		editor.putInt(key, value);
		return editor.commit();
	}
	
	/**
	 * Save boolean key value to the preference.
	 * @param context from which invoke this method.
	 * @param key preference key 
	 * @param value preference value
	 * @return true if save successfully.
	 */
	public static boolean saveBoolean(Context context, String key,boolean value) {
		Editor editor = context.getSharedPreferences(LEHOSPFILE,
				Context.MODE_PRIVATE).edit();
		editor.putBoolean(key, value);
		return editor.commit();

	}	

	/**
	 * Read boolean key value to the preference.
	 * @param context from which invoke this method.
	 * @param key preference key
	 * @return  preference value of given key
	 */
	public static boolean readBoolean(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(LEHOSPFILE, Context.MODE_PRIVATE);
		return sp.getBoolean(key, false);
	}
	
	/**
	 * Read int key value to the preference.
	 * @param context from which invoke this method.
	 * @param key preference key 
	 * @return  preference value of given key
	 */
	public static int readInt(Context context, String key) {
		SharedPreferences sp = context.getSharedPreferences(LEHOSPFILE, Context.MODE_PRIVATE);
		return sp.getInt(key, 0);

	}
	
	/*
	 * @author wells tang
	 * Read int key value to the preference.
	 * @param context from which invoke this method.
	 * @param key preference key 
	 * @defaultValue 
	 * @return  preference value of given key
	 */
	public static int readInt(Context context,String key,int defaultValue)
	{
		SharedPreferences sp = context.getSharedPreferences(LEHOSPFILE, Context.MODE_PRIVATE);
		return sp.getInt(key, defaultValue);
	}
	
	/**
	 * Clear all the preference.
	 * @param context
	 */
	public static void clear(Context context) {
		Editor editor = context.getSharedPreferences(LEHOSPFILE,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
	}

}
