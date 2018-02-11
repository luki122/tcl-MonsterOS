package com.tcl.monster.fota.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.tcl.monster.fota.utils.FotaLog;

public class FotaProvider extends ContentProvider {
    private static final String TAG = "FotaProvider";

    private SQLiteOpenHelper mOpenHelper;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int FOTA = 0;
    private static final int FOTA_ID = 1;
    private static final int REPORT = 2;
    private static final int REPORT_ID = 3;

    static {
        URI_MATCHER.addURI("com.tcl.monster.fota", "fota", FOTA);
        URI_MATCHER.addURI("com.tcl.monster.fota", "fota/#", FOTA_ID);
        URI_MATCHER.addURI("com.tcl.monster.fota", "report", REPORT);
        URI_MATCHER.addURI("com.tcl.monster.fota", "report/#", REPORT_ID);
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = DbHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder) {
        FotaLog.d(TAG, "query -> uri = " + uri);
        if (getContext().checkCallingOrSelfPermission("android.permission.ACCESS_OTA_DATA")
                != PackageManager.PERMISSION_GRANTED) {
            FotaLog.d(TAG, "query " + uri + ", permission denied");
            return null;
        }
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        int match = URI_MATCHER.match(uri);
        FotaLog.d(TAG, "query -> match = " + match + ", uri = " + uri);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (match) {
            case FOTA:
                qb.setTables(DbHelper.TASK_TABLE);
                break;
            case FOTA_ID:
                qb.setTables(DbHelper.TASK_TABLE);
                qb.appendWhere("id = " + uri.getPathSegments().get(1));
                break;
            case REPORT:
                qb.setTables(DbHelper.REPORT_TABLE);
                break;
            case REPORT_ID:
                qb.setTables(DbHelper.REPORT_TABLE);
                qb.appendWhere("_id = " + uri.getPathSegments().get(1));
                break;

        }
        Cursor cursor = qb.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        if (cursor != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    private String[] insertSelectionArg(String[] selectionArgs, String arg) {
        if (selectionArgs == null) {
            return new String[] {
                arg
            };
        } else {
            int newLength = selectionArgs.length + 1;
            String[] newSelectionArgs = new String[newLength];
            newSelectionArgs[0] = arg;
            System.arraycopy(selectionArgs, 0, newSelectionArgs, 1, selectionArgs.length);
            return newSelectionArgs;
        }
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        FotaLog.d(TAG, "delete -> uri = " + uri);
        if (getContext().checkCallingOrSelfPermission("android.permission.ACCESS_OTA_DATA")
                != PackageManager.PERMISSION_GRANTED) {
            FotaLog.d(TAG, "delete " + uri + ", permission denied");
            return 0;
        }
        String table = null;
        int match = URI_MATCHER.match(uri);
        FotaLog.d(TAG, "delete -> match = " + match + ", uri = " + uri);
        switch (match) {
            case FOTA:
                table = DbHelper.TASK_TABLE;
                break;
            case FOTA_ID:
                table = DbHelper.TASK_TABLE;
                where = DatabaseUtils.concatenateWhere("id = " + uri.getPathSegments().get(1),
                        where);
                break;
            case REPORT:
                table = DbHelper.REPORT_TABLE;
                break;
            case REPORT_ID:
                table = DbHelper.REPORT_TABLE;
                where = DatabaseUtils.concatenateWhere("_id = " + uri.getPathSegments().get(1),
                        where);
                break;
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(table, where, whereArgs);
        if (count > 0) {
            notifyChange(uri);
        }
        return count;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        FotaLog.d(TAG, "insert -> uri = " + uri);
        if (getContext().checkCallingOrSelfPermission("android.permission.ACCESS_OTA_DATA")
                != PackageManager.PERMISSION_GRANTED) {
            FotaLog.d(TAG, "insert " + uri + ", permission denied");
            return null;
        }
        int match = URI_MATCHER.match(uri);
        String table = DbHelper.TASK_TABLE;
        FotaLog.d(TAG, "insert -> match = " + match + ", uri = " + uri);
        switch (match) {
            case FOTA:
                table = DbHelper.TASK_TABLE;
                break;
            case REPORT:
                table = DbHelper.REPORT_TABLE;
                break;
        }

        boolean notify = true;

        if (values.containsKey("need_notify")) {
            notify = values.getAsBoolean("need_notify");
            values.remove("need_notify");
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(table, null, values);

        if (rowId < 0) {
            return null;
        }

        if (notify) {
            notifyChange(uri);
        }

        return ContentUris.withAppendedId(uri, rowId);
    }

    @Override
    public int update(Uri url, ContentValues values, String where, String[] whereArgs) {
        FotaLog.d(TAG, "update -> url = " + url);
        if (getContext().checkCallingOrSelfPermission("android.permission.ACCESS_OTA_DATA")
                != PackageManager.PERMISSION_GRANTED) {
            FotaLog.d(TAG, "update " + url + ", permission denied");
            return 0;
        }
        int count = 0;
        String table = null;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int match = URI_MATCHER.match(url);
        FotaLog.d(TAG, "update -> match = " + match + ", url = " + url);
        switch (match) {
            case FOTA:
                table = DbHelper.TASK_TABLE;
                break;
            case FOTA_ID:
                table = DbHelper.TASK_TABLE;
                where = DatabaseUtils.concatenateWhere("id = " + url.getPathSegments().get(1),
                        where);
                break;
            case REPORT:
                table = DbHelper.REPORT_TABLE;
                break;
            case REPORT_ID:
                table = DbHelper.REPORT_TABLE;
                where = DatabaseUtils.concatenateWhere("_id = " + url.getPathSegments().get(1),
                        where);
                break;
        }
        boolean notify = true;
        if (values.containsKey("need_notify")) {
            notify = values.getAsBoolean("need_notify");
            values.remove("need_notify");
        }

        count = db.update(table, values, where, whereArgs);
        if (count > 0 && notify) {
            notifyChange(url);
        }
        return count;
    }

    private void notifyChange(Uri uri) {
        ContentResolver cr = getContext().getContentResolver();
        cr.notifyChange(uri, null);
    }
}