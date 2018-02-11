package com.monster.market.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by xiaobin on 16-11-22.
 */
public class DataCacheDao {

    private DataCacheDb dataCache;
    private SQLiteDatabase db;

    public DataCacheDao(Context context) {
        dataCache = new DataCacheDb(context);
    }

    /**
     * 打开数据库
     */
    public void openDatabase() {
        db = dataCache.getWritableDatabase();
    }

    /**
     * 关闭数据库
     */
    public void closeDatabase() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    public long getCacheTime(int type, int subId, int pageIndex) {
        long cacheTime = 0;
        if (db == null) {
            openDatabase();
        }
        if (db != null) {
            String selection = DataCacheDb.TYPE + "=? and " + DataCacheDb.SUB_ID + "=? and "
                    + DataCacheDb.PAGE_INDEX + "=?";
            String [] selectionArgs = new String[]
                    { String .valueOf(type), String.valueOf(subId), String.valueOf(pageIndex) };
            Cursor cursor = db.query(DataCacheDb.CACHE_TABLE, new String[] { DataCacheDb.CACHE_TIME }, selection,
                    selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                cacheTime = cursor.getLong(cursor.getColumnIndex(DataCacheDb.CACHE_TIME));
            }
            cursor.close();
        }
        return cacheTime;
    }


    public String getCacheContent(int type, int subId, int pageIndex) {
        String cacheContent = "";
        if (db == null) {
            openDatabase();
        }
        if (db != null) {
            String selection = DataCacheDb.TYPE + "=? and " + DataCacheDb.SUB_ID + "=? and "
                    + DataCacheDb.PAGE_INDEX + "=?";
            String [] selectionArgs = new String[]
                    { String .valueOf(type), String.valueOf(subId), String.valueOf(pageIndex) };
            Cursor cursor = db.query(DataCacheDb.CACHE_TABLE, new String[] { DataCacheDb.CONTENT }, selection,
                    selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                cacheContent = cursor.getString(cursor.getColumnIndex(DataCacheDb.CONTENT));
            }
            cursor.close();
        }
        return cacheContent;
    }

    public void saveCache(int type, int subId, int pageIndex, long cacheTime, String cacheContent) {
        if (db == null) {
            openDatabase();
        }
        if (db != null) {
            ContentValues values = new ContentValues();
            values.put(DataCacheDb.TYPE, type);
            values.put(DataCacheDb.SUB_ID, subId);
            values.put(DataCacheDb.PAGE_INDEX, pageIndex);
            values.put(DataCacheDb.CACHE_TIME, cacheTime);
            values.put(DataCacheDb.CONTENT, cacheContent);
            db.insert(DataCacheDb.CACHE_TABLE, null, values);
        }
    }

    public boolean deleteCache(int type, int subId, int pageIndex) {
        if (db == null) {
            openDatabase();
        }
        if (db != null) {
            String where = DataCacheDb.TYPE + "=? and " + DataCacheDb.SUB_ID + "=? and "
                    + DataCacheDb.PAGE_INDEX + "=?";
            String[] whereArgs = new String[]
                    { String .valueOf(type), String.valueOf(subId), String.valueOf(pageIndex) };
            int count = db.delete(DataCacheDb.CACHE_TABLE, where, whereArgs);
            if (count > 0) {
                return true;
            }
        }
        return false;
    }


}
