package com.monster.paymentsecurity.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sandysheny on 16-11-22.
 */

public abstract class DataBaseHelper {
    private static final String TAG = "db";

    /**
     * 用来创建和获取数据库的SQLiteOpenHelper
     */
    protected DBHelper mDbHelper;
    /**
     * 数据库对象
     */
    protected SQLiteDatabase mDb;

    /**
     * 数据库信息
     */
    private int mDbVersion;//版本
    private String mDbName;//数据库名

    protected abstract int getMDbVersion(Context context);

    protected abstract String getDbName(Context context);

    /**
     * 创建表语句
     */
    protected abstract String[] getDbCreateSql(Context context);

    /**
     * 更新表语句
     */
    protected abstract String[] getDbUpdateSql(Context context);

    public DataBaseHelper(Context context) {
        this.mDbVersion = this.getMDbVersion(context);
        this.mDbName = this.getDbName(context);
        this.mDbHelper = new DBHelper(context, this.mDbName, this.mDbVersion, getDbCreateSql(context), getDbUpdateSql(context));
    }


    protected void open() {
        mDb = mDbHelper.getWritableDatabase();
    }

    public void close() {
        this.mDbHelper.close();
        mDb = null;
        mDbHelper = null;
    }

    private static class DBHelper extends SQLiteOpenHelper {
        private final String[] mDbUpdateSql;
        private final String[] mDbCreateSql;

        public DBHelper(Context context, String name, int version, String[] dbCreateSql, String[] dbUpdateSql) {
            super(context, name, null, version);
            this.mDbCreateSql = dbCreateSql;
            this.mDbUpdateSql = dbUpdateSql;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String[] arr = mDbCreateSql;
            //执行创建表语句
            for (String sql : arr) {
                db.execSQL(sql);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String[] arr = mDbUpdateSql;
            //执行更新语句
            for (String sql : arr) {
                db.execSQL(sql);
            }
        }
    }


    /**
     * 统一对ContentValues处理
     *
     * @param contentValues
     * @param key
     * @param value
     */
    private void ContentValuesPut(ContentValues contentValues, String key, Object value) {
        if (value == null) {
            contentValues.put(key, "");
        } else {
            String className = value.getClass().getName();
            switch (className) {
                case "java.lang.String":
                    contentValues.put(key, value.toString());
                    break;
                case "java.lang.Integer":
                    contentValues.put(key, Integer.valueOf(value.toString()));
                    break;
                case "java.lang.Float":
                    contentValues.put(key, Float.valueOf(value.toString()));
                    break;
                case "java.lang.Double":
                    contentValues.put(key, Double.valueOf(value.toString()));
                    break;
                case "java.lang.Boolean":
                    contentValues.put(key, Boolean.valueOf(value.toString()));
                    break;
                case "java.lang.Long":
                    contentValues.put(key, Long.valueOf(value.toString()));
                    break;
                case "java.lang.Short":
                    contentValues.put(key, Short.valueOf(value.toString()));
                    break;
            }
        }
    }

    /**
     * 根据数组的列和值进行insert
     *
     * @param tableName
     * @param columns
     * @param values
     * @return
     */
    public boolean insert(String tableName, String[] columns, Object[] values) {
        ContentValues contentValues = new ContentValues();
        for (int rows = 0; rows < columns.length; ++rows) {
            ContentValuesPut(contentValues, columns[rows], values[rows]);
        }
        long rowId = this.mDb.insert(tableName, null, contentValues);
        return rowId != -1;
    }

    /**
     * 根据map来进行insert
     *
     * @param tableName
     * @param columnValues
     * @return
     */
    public boolean insert(String tableName, Map<String, Object> columnValues) {
        ContentValues contentValues = new ContentValues();
        Iterator iterator = columnValues.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            this.ContentValuesPut(contentValues, key, columnValues.get(key));
        }

        long rowId = this.mDb.insert(tableName, null, contentValues);
        return rowId != -1;
    }


    /**
     * 统一对数组where条件进行拼接
     *
     * @param whereColumns
     * @return
     */
    private String initWhereSqlFromArray(String[] whereColumns) {
        StringBuilder whereStr = new StringBuilder();
        for (int i = 0; i < whereColumns.length; ++i) {
            whereStr.append(whereColumns[i]).append(" = ? ");
            if (i < whereColumns.length - 1) {
                whereStr.append(" and ");
            }
        }
        return whereStr.toString();
    }

    /**
     * 统一对map的where条件和值进行处理
     *
     * @param whereParams
     * @return
     */
    private Map<String, Object> initWhereSqlFromMap(Map<String, String> whereParams) {
        Set set = whereParams.keySet();
        String[] temp = new String[whereParams.size()];
        int i = 0;
        Iterator iterator = set.iterator();
        StringBuffer whereStr = new StringBuffer();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            whereStr.append(key).append(" = ? ");
            temp[i] = whereParams.get(key);
            if (i < set.size() - 1) {
                whereStr.append(" and ");
            }
            i++;
        }
        HashMap result = new HashMap();
        result.put("whereSql", whereStr);
        result.put("whereSqlParam", temp);
        return result;
    }


    /**
     * 根据数组条件来update
     *
     * @param tableName
     * @param columns
     * @param values
     * @param whereColumns
     * @param whereArgs
     * @return
     */
    public boolean update(String tableName, String[] columns, Object[] values, String[] whereColumns, String[] whereArgs) {
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < columns.length; ++i) {
            this.ContentValuesPut(contentValues, columns[i], values[i]);
        }
        String whereClause = this.initWhereSqlFromArray(whereColumns);
        int rowNumber = this.mDb.update(tableName, contentValues, whereClause, whereArgs);
        return rowNumber > 0;
    }

    /**
     * 根据map值来进行update
     *
     * @param tableName
     * @param columnValues
     * @param whereParam
     * @return
     */
    public boolean update(String tableName, Map<String, Object> columnValues, Map<String, String> whereParam) {
        ContentValues contentValues = new ContentValues();
        Iterator iterator = columnValues.keySet().iterator();

        String columns;
        while (iterator.hasNext()) {
            columns = (String) iterator.next();
            ContentValuesPut(contentValues, columns, columnValues.get(columns));
        }

        Map map = this.initWhereSqlFromMap(whereParam);
        int rowNumber = this.mDb.update(tableName, contentValues, (String) map.get("whereSql"), (String[]) map.get("whereSqlParam"));
        return rowNumber > 0;
    }

    /**
     * 根据数组条件进行delete
     *
     * @param tableName
     * @param whereColumns
     * @param whereParam
     * @return
     */
    public boolean delete(String tableName, String[] whereColumns, String[] whereParam) {
        String whereStr = this.initWhereSqlFromArray(whereColumns);
        int rowNumber = this.mDb.delete(tableName, whereStr, whereParam);
        return rowNumber > 0;
    }


    /**
     * 根据map来进行delete
     *
     * @param tableName
     * @param whereParams
     * @return
     */
    public boolean delete(String tableName, Map<String, String> whereParams) {
        Map map = this.initWhereSqlFromMap(whereParams);
        int rowNumber = this.mDb.delete(tableName, map.get("whereSql").toString(), (String[]) map.get("whereSqlParam"));
        return rowNumber > 0;
    }


    /**
     * 查询返回List
     *
     * @param sql
     * @param params
     * @return
     */
    public List<Map> queryListMap(String sql, String[] params) {
        ArrayList list = new ArrayList();
        Cursor cursor = this.mDb.rawQuery(sql, params);
        int columnCount = cursor.getColumnCount();
        while (cursor.moveToNext()) {
            HashMap item = new HashMap();
            for (int i = 0; i < columnCount; ++i) {
                int type = cursor.getType(i);
                switch (type) {
                    case 0:
                        item.put(cursor.getColumnName(i), null);
                        break;
                    case 1:
                        item.put(cursor.getColumnName(i), cursor.getInt(i));
                        break;
                    case 2:
                        item.put(cursor.getColumnName(i), cursor.getFloat(i));
                        break;
                    case 3:
                        item.put(cursor.getColumnName(i), cursor.getString(i));
                        break;
                }
            }
            list.add(item);
        }
        cursor.close();
        return list;
    }

    /**
     * 查询单条数据返回map
     *
     * @param sql
     * @param params
     * @return
     */
    public Map queryItemMap(String sql, String[] params) {
        Cursor cursor = this.mDb.rawQuery(sql, params);
        HashMap map = new HashMap();
        if (cursor.moveToNext()) {
            for (int i = 0; i < cursor.getColumnCount(); ++i) {
                int type = cursor.getType(i);
                switch (type) {
                    case 0:
                        map.put(cursor.getColumnName(i), null);
                        break;
                    case 1:
                        map.put(cursor.getColumnName(i), cursor.getInt(i));
                        break;
                    case 2:
                        map.put(cursor.getColumnName(i), cursor.getFloat(i));
                        break;
                    case 3:
                        map.put(cursor.getColumnName(i), cursor.getString(i));
                        break;
                }
            }
        }
        cursor.close();
        return map;
    }

    public void execSQL(String sql) {
        this.mDb.execSQL(sql);
    }

    public void execSQL(String sql, Object[] params) {
        this.mDb.execSQL(sql, params);
    }
}
