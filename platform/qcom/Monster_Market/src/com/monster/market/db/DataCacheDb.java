package com.monster.market.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by xiaobin on 16-11-22.
 */
public class DataCacheDb extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "data_cache_db";
    public static final int DATABASE_VERSION = 1;

    public static final String CACHE_TABLE = "cache_table";

    public static final String TYPE = "type";
    public static final String SUB_ID = "sub_id";
    public static final String PAGE_INDEX = "page_index";
    public static final String CACHE_TIME = "cache_time";
    public static final String CONTENT = "content";

    public DataCacheDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS " + CACHE_TABLE + " (");
        sb.append(TYPE + " TEXT ,");
        sb.append(SUB_ID + " TEXT ,");
        sb.append(PAGE_INDEX + " INTEGER,");
        sb.append(CACHE_TIME + " TEXT,");
        sb.append(CONTENT + " TEXT)");
        sqLiteDatabase.execSQL(sb.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CACHE_TABLE);
        onCreate(db);
    }

}
