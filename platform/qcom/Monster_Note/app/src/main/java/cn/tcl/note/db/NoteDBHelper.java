/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.tcl.note.util.XmlPrase;

public class NoteDBHelper extends SQLiteOpenHelper {
    private Context mContext;

    public NoteDBHelper(Context context, String name, int version) {
        super(context, name, null, version);
        this.mContext = context;
    }

    public NoteDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DBData.TABLE_NAME + "("
                + DBData.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DBData.COLUMN_XML + " TEXT,"
                + DBData.COLUMN_FIRSTLINE + "  TEXT,"
                + DBData.COLUMN_SECOND_LINE + " TEXT,"
                + DBData.COLUMN_WILL + " INTEGER DEFAULT 0,"
                + DBData.COLUMN_IMG + " INTEGER DEFAULT 0,"
                + DBData.COLUMN_AUDIO + " INTEGER DEFAULT 0,"
                + DBData.COLUMN_TIME + " INTEGER,"
                + DBData.COLUMN_THEME + " INTEGER)");

        XmlPrase.presetNote(mContext, db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
