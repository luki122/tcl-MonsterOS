package com.android.camera.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wenhua.tu on 9/1/15.
 */
public class InfoDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "VideoInfo.db";

    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME = "videtail";

    public static final String CREATE_TABLE = "create table videtail("
            + "id integer primary key autoincrement, "
            + "title text, "
            + "display_name text, "
            + "mime_type text, "
            + "date_taken integer, "
            + "date_modified integer, "
            + "data text, "
            + "width integer, "
            + "height integer, "
            + "resolution text, "
            + "size integer, "
            + "latitude float, "
            + "longitude float, "
            + "duration integer, "
            + "make text, "
            + "weather text)";

    private Context mContext;

    public InfoDatabaseHelper(Context context){
        // create database
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // create table
        String updateSql = "DROP TABLE IF EXISTS videtail";
        db.execSQL(updateSql);

        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String updateSql = "DROP TABLE IF EXISTS videtail";
        db.execSQL(updateSql);
    }
}
