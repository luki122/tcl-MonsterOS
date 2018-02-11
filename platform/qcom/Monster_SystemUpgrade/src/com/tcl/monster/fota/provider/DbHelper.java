
package com.tcl.monster.fota.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
    private static final String TAG = "DbHelper";

    private final Context mContext;
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "fota.db";

    public static final String TASK_TABLE = "firmware";
    public static final String REPORT_TABLE = "report";

    private static DbHelper sInstance = null;

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    static synchronized DbHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DbHelper(context);
        }
        return sInstance;
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createFirmwareTables(db);
        createReportTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void createFirmwareTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TASK_TABLE + " (" +
                Fota.Firmware.ID + " TEXT PRIMARY KEY," +
                Fota.Firmware.PAUSED_REASON + " INTEGER," +
                Fota.Firmware.CURRENTBYTES + " INTEGER," +
                Fota.Firmware.TOTALBYTES + " INTEGER," +
                Fota.Firmware.STATE + " TEXT," +
                Fota.Firmware.URL_BEST + " TEXT," +
                Fota.Firmware.UPDATE_INFO_JSON + " TEXT," +
                Fota.Firmware.DOWNLOAD_INFO_JSON + " TEXT," +
                Fota.Firmware.DOWNLOAD_TASKS_JSON + " TEXT);");
    }

    private void createReportTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + REPORT_TABLE + " (" +
                Fota.Report._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Fota.Report.PARAM + " TEXT," +
                Fota.Report.ORIGIN + " INTEGER);");
    }

    private boolean checkColumnExists(SQLiteDatabase db, String tableName, String columnName) {
        boolean result = false ;
        Cursor cursor = null ;
        try{
            cursor = db.rawQuery( "select * from sqlite_master where name = ? and sql like ?",
                    new String[]{tableName, "%" + columnName + "%"} );
            result = null != cursor && cursor.moveToFirst() ;
        } catch (Exception e){
            Log.e(TAG,"checkColumnExists ..." + e.getMessage()) ;
        } finally {
            if(null != cursor && !cursor.isClosed()){
                cursor.close() ;
            }
        }
        return result ;
    }
}