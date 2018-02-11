package com.monster.paymentsecurity.db;

import android.content.Context;


/**
 * Created by sandysheny on 16-11-22.
 */

public class WhiteListDB extends DataBaseHelper {

    public static final String DATABASE_NAME = "whitelist.db"; // 数据库名
    public static final int DATABASE_VERSION = 1; // 数据库版本

    public static final String WHITELIST_TABLE = "table_white";

    public static final String NAME = "name"; // 应用名
    public static final String PACKAGENAME = "packageName"; // 包名
    public static final String APKPATH = "apkpath"; // 包名
    public static final String APPSTATE = "appState";
    public static final String APKTYPE = "apktype";

    WhiteListDB(Context context) {
        super(context);
    }


    @Override
    protected int getMDbVersion(Context context) {
        return DATABASE_VERSION;
    }

    @Override
    protected String getDbName(Context context) {
        return DATABASE_NAME;
    }

    @Override
    protected String[] getDbCreateSql(Context context) {
        String[] a = new String[1];
        String sb = ("CREATE TABLE IF NOT EXISTS " + WHITELIST_TABLE + " (") +
                NAME + " VARCHAR(100)," +
                PACKAGENAME + " VARCHAR(100)," +
                APKPATH + " VARCHAR(100)," +
                APPSTATE + " INTEGER DEFAULT 1," +
                APKTYPE + " INTEGER DEFAULT 0)";
        a[0] = sb;
        return a;
    }

    @Override
    protected String[] getDbUpdateSql(Context context) {
        String[] a = new String[1];
        a[0] = ("DROP TABLE IF EXISTS " + WHITELIST_TABLE);
        return a;
    }
}
