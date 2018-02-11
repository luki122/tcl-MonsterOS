package com.android.camera.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by wenhua.tu on 9/1/15.
 */
public class InfoProvider extends ContentProvider {

    public static final int VIDETAIL_DIR = 0;

    public static final int VIDETAIL_ITEM = 1;

    private static UriMatcher mUriMacher;

    private InfoDatabaseHelper mDbHelper;

    static{
        mUriMacher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMacher.addURI(ProviderUtil.AUTHORITY, InfoDatabaseHelper.TABLE_NAME, VIDETAIL_DIR);
        mUriMacher.addURI(ProviderUtil.AUTHORITY, InfoDatabaseHelper.TABLE_NAME + "/#", VIDETAIL_ITEM);
    }

    @Override
    public boolean onCreate() {
        // create database
        mDbHelper = new InfoDatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (mUriMacher.match(uri)){
            case VIDETAIL_DIR:
                cursor = db.query(InfoDatabaseHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case VIDETAIL_ITEM:
                String videtailId = uri.getPathSegments().get(1);
                cursor = db.query(InfoDatabaseHelper.TABLE_NAME, projection, "id = ?", new String[]{videtailId}, null, null, sortOrder);
                break;
            default:
                break;
        }
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Uri uriReturn = null;
        switch (mUriMacher.match(uri)){
            case VIDETAIL_DIR:
            case VIDETAIL_ITEM:
                long newVidetailId = db.insert(InfoDatabaseHelper.TABLE_NAME, null, values);
                uriReturn = Uri.parse(ProviderUtil.VIDETAIL_URI + newVidetailId);
                break;
            default:
                break;
        }
        return uriReturn;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        int updateRows = 0;
        switch (mUriMacher.match(uri)){
            case VIDETAIL_DIR:
                updateRows = db.update(InfoDatabaseHelper.TABLE_NAME, values, selection, selectionArgs);
                break;
            case VIDETAIL_ITEM:
                String videtailId = uri.getPathSegments().get(1);
                updateRows = db.update(InfoDatabaseHelper.TABLE_NAME, values, "id = ?", new String[]{videtailId});
                break;
        }
        return updateRows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int deleteRows = 0;
        switch (mUriMacher.match(uri)){
            case VIDETAIL_DIR:
                deleteRows = db.delete(InfoDatabaseHelper.TABLE_NAME, selection, selectionArgs);
                break;
            case VIDETAIL_ITEM:
                String videtailId = uri.getPathSegments().get(1);
                deleteRows = db.delete(InfoDatabaseHelper.TABLE_NAME, "id = ?", new String[]{videtailId});
                break;
            default:
                break;
        }
        return deleteRows;
    }

    @Override
    public String getType(Uri uri) {
        return "video/mp4";
    }
}
