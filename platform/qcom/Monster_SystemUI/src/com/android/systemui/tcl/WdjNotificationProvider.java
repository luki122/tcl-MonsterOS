package com.android.systemui.tcl;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by liuzhicang on 16-10-14.
 */

public class WdjNotificationProvider extends ContentProvider {
    public static final String AUTHORITY = "com.android.systemui.tcl.WdjNotificationProvider";
    private static final String DATABASE_NAME = "wdj_notify.db";
    private static final int DATABASE_VERSION = 1;
    private SQLiteHeler mSQLiteHeler;
    private ContentResolver mContentResolver;
    private final static int MATCH_CODE_NOTIFY_COUNT = 1;

    //豌豆荚通知数量
    public static final String TABLE_NOTIFY_COUNT = "count";
    public static final String ITEM_PACKAGENAME = "package";
    public static final String ITEM_TOTAL_COUNT = "total_count";
    public static final String ITEM_CLEAR_COUNT = "clear_count";


    private static final UriMatcher sMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sMatcher.addURI(AUTHORITY, TABLE_NOTIFY_COUNT, MATCH_CODE_NOTIFY_COUNT);
    }

    @Override
    public boolean onCreate() {
        mSQLiteHeler = new SQLiteHeler(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mSQLiteHeler.getReadableDatabase();
        Cursor cursor;
        String tab = getContentTab(uri);
        cursor = db.query(tab, projection, selection, selectionArgs, null,
                null, sortOrder);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mSQLiteHeler.getWritableDatabase();
        mContentResolver = getContext().getContentResolver();
        String tab = getContentTab(uri);
        db.insert(tab, null, values);
        mContentResolver.notifyChange(uri, null, false);
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mSQLiteHeler.getWritableDatabase();
        mContentResolver = getContext().getContentResolver();
        int count;
        String tab = getContentTab(uri);
        count = db.delete(tab, selection, selectionArgs);
        mContentResolver.notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mSQLiteHeler.getWritableDatabase();
        mContentResolver = getContext().getContentResolver();
        int count;
        String tab = getContentTab(uri);
        count = db.update(tab, values, selection, selectionArgs);
        mContentResolver.notifyChange(uri, null);
        return count;
    }


    private String getContentTab(Uri uri) {
        int code = sMatcher.match(uri);
        switch (code) {
            case MATCH_CODE_NOTIFY_COUNT:
                return TABLE_NOTIFY_COUNT;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }

    private class SQLiteHeler extends SQLiteOpenHelper {

        public SQLiteHeler(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // TODO Auto-generated method stub
            db.execSQL("CREATE TABLE " + TABLE_NOTIFY_COUNT + " (" + ITEM_PACKAGENAME + "," + ITEM_TOTAL_COUNT + "," + ITEM_CLEAR_COUNT + ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFY_COUNT);
            onCreate(db);
        }

    }
}
