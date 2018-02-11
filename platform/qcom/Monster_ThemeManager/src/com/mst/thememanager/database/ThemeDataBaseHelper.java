package com.mst.thememanager.database;


import com.mst.thememanager.utils.Config;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ThemeDataBaseHelper extends SQLiteOpenHelper {
    /** Database filename */
    private static final String DB_NAME = "themes.db";
    /** Current database version */
    private static final int DB_VERSION = 2;
    /** Name of table in the database */
    public static final String DB_TABLE = "themes";
	private static final String TAG = null;
    
    
    public ThemeDataBaseHelper(final Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Creates database the first time we try to open it.
     */
    @Override
    public void onCreate(final SQLiteDatabase db) {
    	createThemesTable(db);
    }

   
    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldV, final int newV) {
            upgradeTo(db);
    }

    private void upgradeTo(SQLiteDatabase db) {
    	db.execSQL("DROP TABLE IF EXISTS " + DB_TABLE);
    	createThemesTable(db);
    }


    /**
     * Add a column to a table using ALTER TABLE.
     * @param dbTable name of the table
     * @param columnName name of the column to add
     * @param columnDefinition SQL for the column definition
     */
    public void addColumn(SQLiteDatabase db, String dbTable, String columnName,
                           String columnDefinition) {
        db.execSQL("ALTER TABLE " + dbTable + " ADD COLUMN " + columnName + " "
                   + columnDefinition);
    }

    public long insert(SQLiteDatabase db,ContentValues values){
    	return db.insert(DB_TABLE, null, values);
    }
    
    
    /**
     * Creates the table that'll hold the theme information.
     */
    private void createThemesTable(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE " + DB_TABLE + "(" +
                    Config.DatabaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Config.DatabaseColumns.URI + " TEXT, " +
                    Config.DatabaseColumns.FILE_PATH + " TEXT, " +
                    Config.DatabaseColumns.LOADED_PATH + " TEXT, " +
                    Config.DatabaseColumns.NAME + " TEXT, " +
                    Config.DatabaseColumns.TYPE + " INTEGER, " +
                    Config.DatabaseColumns.APPLY_STATUS + " INTEGER, " +
                    Config.DatabaseColumns.LOADED + " INTEGER, " +
                    Config.DatabaseColumns.DOWNLOAD_STATUS + " INTEGER, " +
                    Config.DatabaseColumns.LAST_MODIFIED_TIME + " BIGINT, " +
                    Config.DatabaseColumns.DESGINER + " TEXT, " +
                    Config.DatabaseColumns.TOTAL_BYTES + " INTEGER, " +
                    Config.DatabaseColumns.CURRENT_BYTES + " INTEGER, " +
                    Config.DatabaseColumns.VERSION + " TEXT, " +
                    Config.DatabaseColumns.DESCRIPTION + " TEXT, "+
                    Config.DatabaseColumns.SYSTEM_WALLPAPER_NUMBER+" INTEGER DEFAULT -1, "+
                    Config.DatabaseColumns.SYSTEM_KEYGUARD_WALLPAPER_NAME+" TEXT);");
        } catch (SQLException ex) {
            Log.e(TAG, "couldn't create table in themes database");
            throw ex;
        }
    }

}
