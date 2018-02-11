/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Created by thundersoft on 16-7-28.
 */
class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static class HelperHolder {
        private static DatabaseHelper dbHelper;

        private static DatabaseHelper getDbHelperInstance(Context context) {
            if (null == dbHelper) {
                dbHelper = new DatabaseHelper(context);
            }
            return dbHelper;
        }
    }

    static DatabaseHelper getDatabaseHelperInstance(Context context) {
        return HelperHolder.getDbHelperInstance(context);
    }


    private final static int VERSION = 1;
    private final static String DB_NAME = "tcl_weather.db";

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
//            TableUtils.createTable(connectionSource, City.class);
//            TableUtils.createTable(connectionSource, CityInfo.class);
//            TableUtils.createTable(connectionSource, CurrentConditions.class);
            TableUtils.createTable(connectionSource, DbTableCityData.class);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i1) {

    }
}
