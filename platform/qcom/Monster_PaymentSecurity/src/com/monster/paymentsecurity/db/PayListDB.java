package com.monster.paymentsecurity.db;

import android.content.Context;

/**
 * Created by sandysheny on 16-11-22.
 */

public class PayListDB extends DataBaseHelper {

    public static final String DATABASE_NAME = "paymentsecurity.db"; // 数据库名
    public static final int DATABASE_VERSION = 1; // 数据库版本

    public static final String PAYLIST_TABLE = "table_paylist";

    public static final String NAME = "name"; // 应用名
    public static final String PACKAGENAME = "packageName"; // 包名
    public static final String NEED_DETECT = "need_detect"; // 是否开启检测

    PayListDB(Context context) {
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
        String sb = ("CREATE TABLE IF NOT EXISTS " + PAYLIST_TABLE + " (") +
                NAME + " VARCHAR(100)," +
                PACKAGENAME + " VARCHAR(100) UNIQUE ON CONFLICT REPLACE," +
                NEED_DETECT + " INTEGER DEFAULT 1)";
        a[0] = sb;
        return a;
    }

    @Override
    protected String[] getDbUpdateSql(Context context) {
        String[] a = new String[1];
        a[0] = ("DROP TABLE IF EXISTS " + PAYLIST_TABLE);
        return a;
    }
}
