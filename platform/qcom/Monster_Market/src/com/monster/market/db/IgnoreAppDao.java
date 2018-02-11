package com.monster.market.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.monster.market.bean.AppUpgradeInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 对忽略应用更新的数据库操作
 */
public class IgnoreAppDao extends IgnoreAppDb {

    public static final String TAG = "IgnoreAdapter";
    public static final String TABLE_NAME = "tbl_ignore";// 数据库表名
    public static final String ID = "_id"; // 表属性ID
    public static final String APP_ID = "appId";
    public static final String APP_ICON = "icon";
    public static final String APP_NAME = "title";
    public static final String APP_SIZE = "appSize";
    public static final String APP_SIZE_NEW = "appSizeNew";
    public static final String APP_VERSION_CODE = "versionCode";
    public static final String APP_VERSION_CODE_NEW = "versionCodeNew";
    public static final String APP_VERSION_NAME = "appVersionName";
    public static final String APP_VERSION_NAME_NEW = "appVersionNameNew";
    public static final String APP_DOWNLOAD_URL_DIF = "downloadUrlDif";
    public static final String APP_DOWNLOAD_URL = "downloadUrl";
    public static final String APP_PACKAGE_NAME = "packageName";

    private DBOpenHelper mDBOpenHelper;
    private SQLiteDatabase mDb;
    private Context mContext;

    public IgnoreAppDao(Context context) {
        this.mContext = context;
    }

    /**
     * 空间不够存储的时候设为只读
     * @throws SQLiteException
     */

    public void open() throws SQLiteException {
        mDBOpenHelper = new DBOpenHelper(mContext);
        try {
            mDb = mDBOpenHelper.getWritableDatabase();
        } catch (SQLiteException e) {
            mDb = mDBOpenHelper.getReadableDatabase();
        }
    }

    /**
     * 调用SQLiteDatabase对象的close()方法关闭数据库
     */
    public void close() {
        if (mDb != null) {
            mDb.close();
            mDb = null;
        }
    }

    /**
     * 保存备忘录信息(多条)
     */
    public void insert(List<AppUpgradeInfo> infoList) {
        mDb.beginTransaction();

        try {
            for (AppUpgradeInfo info : infoList) {
                insert(info);
            }
            mDb.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDb.endTransaction();
        }
    }

    /**
     * 保存忽略应用(单条)
     */
    public void insert(AppUpgradeInfo info) {
        ContentValues value = new ContentValues();
        value.put(APP_ID, info.getAppId());
        value.put(APP_ICON, info.getAppIcon());
        value.put(APP_NAME, info.getAppName());
        value.put(APP_SIZE, info.getAppSize());
        value.put(APP_SIZE_NEW, info.getAppSizeNew());
        value.put(APP_VERSION_CODE, info.getVersionCode());
        value.put(APP_VERSION_CODE_NEW, info.getVersionCodeNew());
        value.put(APP_VERSION_NAME, info.getVersionName());
        value.put(APP_VERSION_NAME_NEW, info.getVersionNameNew());
        value.put(APP_DOWNLOAD_URL_DIF, info.getDownloadUrlDif());
        value.put(APP_DOWNLOAD_URL, info.getDownloadUrl());
        value.put(APP_PACKAGE_NAME, info.getPackageName());
        mDb.insert(TABLE_NAME, null, value);
    }

    /**
     * 查询全部数据
     * @return
     */

    public ArrayList<AppUpgradeInfo> queryAllData() {
        String selection = null;

        Cursor result = mDb.query(TABLE_NAME, null, selection, null, null, null, ID + " desc");
        ArrayList<AppUpgradeInfo> data = ConvertToNote(result);
        if (result != null && !result.isClosed()) {
            result.close();
            result = null;
        }
        return data;
    }

    /**
     * 获取count
     * @return
     */
    public int getCount() {
        int count = 0;
        Cursor result = mDb.query(TABLE_NAME, null, null, null, null, null, null);
        if (result != null) {
            count = result.getCount();
        }
        if (result != null && !result.isClosed()) {
            result.close();
            result = null;
        }
        return count;
    }

    public ArrayList<String> queryAllPackageData() {
        String selection = null;

        Cursor result = mDb.query(TABLE_NAME, new String[]{ID, APP_NAME,
                        APP_PACKAGE_NAME}, selection, null, null, null, ID + " desc");

        ArrayList<String> upPac = ConvertToPac(result);
        if (result != null && !result.isClosed()) {
            result.close();
            result = null;
        }
        return upPac;
    }

    private ArrayList<String> ConvertToPac(Cursor cursor) {
        int resultCounts = cursor.getCount();
        if (resultCounts == 0 || !cursor.moveToFirst()) {
            return null;
        }
        ArrayList<String> rentalCar = new ArrayList<String>();
        for (int i = 0; i < resultCounts; i++) {
            rentalCar.add(cursor.getString(cursor
                    .getColumnIndex(APP_PACKAGE_NAME)));

            cursor.moveToNext();
        }
        return rentalCar;
    }

    private ArrayList<AppUpgradeInfo> ConvertToNote(Cursor cursor) {
        int resultCounts = cursor.getCount();
        if (resultCounts == 0 || !cursor.moveToFirst()) {
            return null;
        }
        ArrayList<AppUpgradeInfo> rentalCar = new ArrayList<AppUpgradeInfo>();
        for (int i = 0; i < resultCounts; i++) {
            AppUpgradeInfo m_result = new AppUpgradeInfo();

            m_result.setAppId(cursor.getInt(cursor.getColumnIndex(APP_ID)));
            m_result.setAppIcon(cursor.getString(cursor.getColumnIndex(APP_ICON)));
            m_result.setAppName(cursor.getString(cursor.getColumnIndex(APP_NAME)));
            m_result.setAppSize(cursor.getLong(cursor.getColumnIndex(APP_SIZE)));
            m_result.setAppSizeNew(cursor.getLong(cursor.getColumnIndex(APP_SIZE_NEW)));
            m_result.setVersionCode(cursor.getInt(cursor.getColumnIndex(APP_VERSION_CODE)));
            m_result.setVersionCodeNew(cursor.getInt(cursor.getColumnIndex(APP_VERSION_CODE_NEW)));
            m_result.setVersionName(cursor.getString(cursor.getColumnIndex(APP_VERSION_NAME)));
            m_result.setVersionNameNew(cursor.getString(cursor.getColumnIndex(APP_VERSION_NAME_NEW)));
            m_result.setDownloadUrlDif(cursor.getString(cursor.getColumnIndex(APP_DOWNLOAD_URL_DIF)));
            m_result.setDownloadUrl(cursor.getString(cursor.getColumnIndex(APP_DOWNLOAD_URL)));
            m_result.setPackageName(cursor.getString(cursor.getColumnIndex(APP_PACKAGE_NAME)));

            rentalCar.add(m_result);
            cursor.moveToNext();
        }
        return rentalCar;
    }

    public int deleteDataById(String packagename) {
        return mDb.delete(TABLE_NAME, APP_PACKAGE_NAME + "=?",
                new String[]{packagename});
    }

    public int deleteAll() {
        return mDb.delete(TABLE_NAME, null, null);
    }

}
