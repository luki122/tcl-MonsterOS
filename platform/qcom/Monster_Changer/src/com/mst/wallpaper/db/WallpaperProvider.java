package com.mst.wallpaper.db;

import com.mst.wallpaper.utils.Config;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;


public class WallpaperProvider extends ContentProvider{
	
	private WallpaperDbHelper Db = null;
	
	private static final String[] LOCAL_WALLPAPER_COLUMNS = {
		Config.WallpaperStored.WALLPAPER_ID,
		Config.WallpaperStored.WALLPAPER_MODIFIED,
		Config.WallpaperStored.WALLPAPER_OLDPATH,
		Config.WallpaperStored.WALLPAPER_FILENAME
	};
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int res = Db.mSqlDb.delete(WallpaperDbColumns.DESKTOP_WALLPAPER_TABLE_NAME, where, whereArgs);
		if (res > 0) {
			notifyChange(uri);
		}
		return res;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return Config.WallpaperStored.WALLPAPER_URI_TYPE;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowId = Db.mSqlDb.insert(WallpaperDbColumns.DESKTOP_WALLPAPER_TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri resUri = ContentUris.withAppendedId(uri, rowId);
			notifyChange(resUri);
			return resUri;
		}
		return null;
	}

	@Override
	public boolean onCreate() {
		Db = new WallpaperDbHelper(getContext());
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor cursor = Db.mSqlDb.query(WallpaperDbColumns.DESKTOP_WALLPAPER_TABLE_NAME, 
				projection, selection, selectionArgs, null, null, sortOrder);
		if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int res = Db.mSqlDb.update(WallpaperDbColumns.DESKTOP_WALLPAPER_TABLE_NAME,
				values, selection, selectionArgs);
		if (res > 0) {
			notifyChange(uri);
		}
		return res;
	}
	
	private void notifyChange(Uri uri) {
		getContext().getContentResolver().notifyChange(uri, null);
	}
}
