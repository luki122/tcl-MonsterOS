package com.mst.thememanager.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.mst.thememanager.entities.Theme;
import com.mst.thememanager.utils.Config;
import com.mst.thememanager.utils.Config.DatabaseColumns;

public class ThemePkgDbController extends DatabaseColumns implements ThemeDatabaseController<Theme> {
	
	private ThemeDataBaseHelper mHelper;
	
	private SQLiteDatabase mDb;
	
	private String TABLE = ThemeDataBaseHelper.DB_TABLE;
	
	public  ThemePkgDbController(Context context) {
		// TODO Auto-generated constructor stub
		mHelper = new ThemeDataBaseHelper(context);
		
	}
	
	
	@Override
	public Theme getThemeById(int themeId) {
		// TODO Auto-generated method stub
		open();
		Theme theme = null;
		if (mDb != null) {
			Cursor cursor = mDb.query(ThemeDataBaseHelper.DB_TABLE,
					null, Config.DatabaseColumns._ID + "=?", new String[]{String.valueOf(themeId)}, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				
				if (cursor.moveToNext()) {
					theme = new Theme();
					theme.name = cursor.getString(cursor.getColumnIndex(NAME));
					theme.description = cursor.getString(cursor.getColumnIndex(DESCRIPTION));
					theme.designer = cursor.getString(cursor.getColumnIndex(DESGINER));
					theme.totalBytes = cursor.getLong(cursor.getColumnIndex(TOTAL_BYTES));
					theme.downloadStatus = cursor.getInt(cursor.getColumnIndex(DOWNLOAD_STATUS));
					theme.downloadUrl = cursor.getString(cursor.getColumnIndex(URI));
					theme.id = cursor.getInt(cursor.getColumnIndex(_ID));
					theme.lastModifiedTime = cursor.getLong(cursor.getColumnIndex(LAST_MODIFIED_TIME));
					theme.loaded = cursor.getInt(cursor.getColumnIndex(LOADED)) == Theme.APPLIED;
					theme.loadedPath = cursor.getString(cursor.getColumnIndex(LOADED_PATH));
					theme.version = cursor.getString(cursor.getColumnIndex(VERSION));
					theme.systemWallpaperNumber = cursor.getInt(cursor.getColumnIndex(SYSTEM_WALLPAPER_NUMBER));
					theme.systemKeyguardWallpaperName = cursor.getString(cursor.getColumnIndex(SYSTEM_KEYGUARD_WALLPAPER_NAME));
				}
			}
			cursor.close();
		}
		close();
		return theme;
	}

	@Override
	public List<Theme> getThemesByType(int themeType) {
		// TODO Auto-generated method stub
		open();
		ArrayList<Theme> themes = new ArrayList<Theme>();
		if (mDb != null) {
			Cursor cursor = mDb.query(ThemeDataBaseHelper.DB_TABLE,
					null, Config.DatabaseColumns.TYPE + "=?", new String[]{String.valueOf(themeType)}, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					Theme theme = new Theme();
					theme.name = cursor.getString(cursor.getColumnIndex(NAME));
					theme.themeFilePath = cursor.getString(cursor.getColumnIndex(FILE_PATH));
					theme.description = cursor.getString(cursor.getColumnIndex(DESCRIPTION));
					theme.designer = cursor.getString(cursor.getColumnIndex(DESGINER));
					theme.totalBytes = cursor.getLong(cursor.getColumnIndex(TOTAL_BYTES));
					theme.downloadStatus = cursor.getInt(cursor.getColumnIndex(DOWNLOAD_STATUS));
					theme.downloadUrl = cursor.getString(cursor.getColumnIndex(URI));
					theme.id = cursor.getInt(cursor.getColumnIndex(_ID));
					theme.lastModifiedTime = cursor.getLong(cursor.getColumnIndex(LAST_MODIFIED_TIME));
					theme.loaded = cursor.getInt(cursor.getColumnIndex(LOADED)) == Theme.APPLIED;
					theme.loadedPath = cursor.getString(cursor.getColumnIndex(LOADED_PATH));
					theme.version = cursor.getString(cursor.getColumnIndex(VERSION));
					theme.systemWallpaperNumber = cursor.getInt(cursor.getColumnIndex(SYSTEM_WALLPAPER_NUMBER));
					theme.systemKeyguardWallpaperName = cursor.getString(cursor.getColumnIndex(SYSTEM_KEYGUARD_WALLPAPER_NAME));
					themes.add(theme);
				}
			}
			cursor.close();
		}
		close();
		return themes;
		
	}

	@Override
	public boolean isLoaded(int themeId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean updateTheme(Theme theme) {
		open();
		if (mDb != null) {
			ContentValues values = new ContentValues();
			values.put(Config.DatabaseColumns.URI, theme.downloadUrl);
			values.put(Config.DatabaseColumns.FILE_PATH, theme.themeFilePath);
			
			values.put(Config.DatabaseColumns.LOADED_PATH, theme.loadedPath);
			values.put(Config.DatabaseColumns.NAME, theme.name);
			
			values.put(Config.DatabaseColumns.TYPE, theme.type);
			values.put(Config.DatabaseColumns.APPLY_STATUS, theme.applyStatus?1:0);
			
			values.put(Config.DatabaseColumns.LOADED, theme.loaded?1:0);
			values.put(Config.DatabaseColumns.DOWNLOAD_STATUS, theme.downloadStatus);
			
			values.put(Config.DatabaseColumns.LAST_MODIFIED_TIME, theme.lastModifiedTime);
			values.put(Config.DatabaseColumns.DESGINER, theme.designer);
			
			values.put(Config.DatabaseColumns.VERSION, theme.version);
			values.put(Config.DatabaseColumns.DESCRIPTION, theme.description);
			
			values.put(Config.DatabaseColumns.TOTAL_BYTES, theme.totalBytes);
			
			values.put(Config.DatabaseColumns.SYSTEM_WALLPAPER_NUMBER, theme.systemWallpaperNumber);
			
			values.put(Config.DatabaseColumns.SYSTEM_KEYGUARD_WALLPAPER_NAME, theme.systemKeyguardWallpaperName);
			
			mDb.update(TABLE, values, Config.DatabaseColumns._ID + "=?", new String[]{String.valueOf(theme.id)});
		}
		close();
		return true;
	}

	@Override
	public boolean deleteTheme(Theme theme) {
		// TODO Auto-generated method stub
		return deleteTheme(theme.id);
	}

	@Override
	public boolean deleteTheme(int themeId) {
		open();
		return mDb.delete(TABLE, Config.DatabaseColumns._ID + "=?"
				, new String[]{String.valueOf(themeId)}) != 0;
	}


	@Override
	public void insertTheme(Theme theme) {
		// TODO Auto-generated method stub

		open();
		if (mDb != null) {
			ContentValues values = new ContentValues();
			values.put(Config.DatabaseColumns.URI, theme.downloadUrl);
			values.put(Config.DatabaseColumns.FILE_PATH, theme.themeFilePath);
			
			values.put(Config.DatabaseColumns.LOADED_PATH, theme.loadedPath);
			values.put(Config.DatabaseColumns.NAME, theme.name);
			
			values.put(Config.DatabaseColumns.TYPE, theme.type);
			values.put(Config.DatabaseColumns.APPLY_STATUS, theme.applyStatus?1:0);
			
			values.put(Config.DatabaseColumns.LOADED, theme.loaded?1:0);
			values.put(Config.DatabaseColumns.DOWNLOAD_STATUS, theme.downloadStatus);
			
			values.put(Config.DatabaseColumns.LAST_MODIFIED_TIME, theme.lastModifiedTime);
			values.put(Config.DatabaseColumns.DESGINER, theme.designer);
			
			values.put(Config.DatabaseColumns.VERSION, theme.version);
			values.put(Config.DatabaseColumns.DESCRIPTION, theme.description);
			
			values.put(Config.DatabaseColumns.TOTAL_BYTES, theme.totalBytes);
			
			values.put(Config.DatabaseColumns.SYSTEM_WALLPAPER_NUMBER, theme.systemWallpaperNumber);
			
			values.put(Config.DatabaseColumns.SYSTEM_KEYGUARD_WALLPAPER_NAME, theme.systemKeyguardWallpaperName);
			
			long ok = mHelper.insert(mDb, values);
			Log.d("insert", "ok-->"+ok);
		}
		close();
	}


	@Override
	public void close() {
		// TODO Auto-generated method stub
		if (mDb != null && mDb.isOpen()) {
			mDb.close();
		}
	}


	@Override
	public void open() {
		// TODO Auto-generated method stub
		if(mDb == null || !mDb.isOpen()){
			mDb = mHelper.getWritableDatabase();
		}
	}


	@Override
	public int getCount() {
		open();
		if (mDb != null) {
			Cursor cursor = mDb.query(TABLE,
					null, null, null, null, null, null);
			if (cursor != null) {
				int count = cursor.getCount();
				cursor.close();
				close();
				return count;
			}
		}
		close();
		return 0;
	}


	@Override
	public int getCountByType(int type) {
		open();
		if (mDb != null) {
			Cursor cursor = mDb.query(TABLE,
					null, Config.DatabaseColumns.TYPE + "=?", new String[]{String.valueOf(type)}, null, null, null);
			if (cursor != null) {
				int count = cursor.getCount();
				cursor.close();
				close();
				return count;
			}
		}
		close();
		return 0;
	}
	
	
	

}
