/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FileManagerHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "favorite_db.db";
    public static final String TABLE_NAME = "favorite_file";
    public static final String SHORT_TABLE_NAME = "shortcut_file";
    public static final String FILE_PATH = "_data";

    private static int version = 1;
    private boolean mHasCalledOnOpen;//add for PR969817 by yane.wang@jrdcom.com 20150408

    public FileManagerHelper(Context context) {
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SHORT_TABLE_NAME
                + "(_id integer primary key autoincrement, " + FILE_PATH + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    //add for PR969817 by yane.wang@jrdcom.com 20150408 begin
    @Override
    public void onOpen(SQLiteDatabase db) {
        mHasCalledOnOpen = true;
    }

    public boolean hasCalledOnOpen() {
        return mHasCalledOnOpen;
    }

    public void resetStatus() {
        mHasCalledOnOpen = false;
    }

}
