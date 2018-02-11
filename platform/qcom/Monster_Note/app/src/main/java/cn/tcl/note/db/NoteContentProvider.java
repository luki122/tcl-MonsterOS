/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import cn.tcl.note.util.NoteLog;
import cn.tcl.note.util.TimeUtils;

public class NoteContentProvider extends ContentProvider {
    private final String TAG = NoteContentProvider.class.getSimpleName();
    private NoteDBHelper mNoteDBHelper;

    public NoteContentProvider() {
    }

    @Override
    public boolean onCreate() {
        mNoteDBHelper = new NoteDBHelper(getContext(), DBData.DB_NAME, 1);
        return true;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        NoteLog.d(TAG, "delete the id:" + selectionArgs[0]);
        SQLiteDatabase db = mNoteDBHelper.getWritableDatabase();
        return db.delete(DBData.TABLE_NAME, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        return "";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        NoteLog.d(TAG, "insert a data");
        SQLiteDatabase db = mNoteDBHelper.getWritableDatabase();
        values.put(DBData.COLUMN_TIME, TimeUtils.formatCurrentTime());
        long id = db.insert(DBData.TABLE_NAME, null, values);
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        NoteLog.d(TAG, "query a data");
        SQLiteDatabase db = mNoteDBHelper.getReadableDatabase();
        Cursor cursor = db.query(DBData.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        NoteLog.d(TAG, "update the id " + selectionArgs[0]);
        SQLiteDatabase db = mNoteDBHelper.getWritableDatabase();
        if (!values.containsKey(DBData.COLUMN_TIME)) {
            values.put(DBData.COLUMN_TIME, TimeUtils.formatCurrentTime());
        }
        return db.update(DBData.TABLE_NAME, values, selection, selectionArgs);
    }
}
