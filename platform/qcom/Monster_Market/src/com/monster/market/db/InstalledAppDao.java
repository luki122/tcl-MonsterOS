package com.monster.market.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.monster.market.bean.InstalledAppInfo;

import java.util.ArrayList;
import java.util.List;



public class InstalledAppDao {

	private InstalledAppDB dbHelper; // 数据库帮助类
	private SQLiteDatabase db; // 数据库对象

	public InstalledAppDao(Context context) {
		dbHelper = new InstalledAppDB(context);
	}

	/**
	 * 打开数据库
	 */
	public void openDatabase() {
		db = dbHelper.getWritableDatabase();
	}

	/**
	 * 关闭数据库
	 */
	public void closeDatabase() {
		if (db != null && db.isOpen()) {
			db.close();
		}
	}

	/**
	 * 插入一条已安装APP信息
	 * 
	 * @param appInfo
	 */
	public void insert(InstalledAppInfo appInfo) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(InstalledAppDB.APPNAME, appInfo.getName());
			values.put(InstalledAppDB.ICONID, appInfo.getIconId());
			values.put(InstalledAppDB.VERSIONCODE,
					appInfo.getVersionCode());
			values.put(InstalledAppDB.VERSION, appInfo.getVersion());
			values.put(InstalledAppDB.PACKAGENAME,
					appInfo.getPackageName());
			values.put(InstalledAppDB.APKPATH, appInfo.getApkPath());
			values.put(InstalledAppDB.FLAG, appInfo.getAppFlag());
			values.put(InstalledAppDB.APK_MD5, appInfo.getMd5());
			values.put(InstalledAppDB.CER_STR_MD5, appInfo.getCerStrMd5());
			db.insert(InstalledAppDB.INSTALLED_TABLE, null, values);
		}
	}

	public int getInstalledCount() {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(InstalledAppDB.INSTALLED_TABLE,
					null, null, null, null, null, null);
			if (cursor != null) {
				int count = cursor.getCount();
				cursor.close();
				return count;
			}
		}
		return 0;
	}

	/**
	 * 获取已安装APP列表
	 * 
	 * @return
	 */
	public List<InstalledAppInfo> getInstalledAppList() {
		List<InstalledAppInfo> infos = new ArrayList<InstalledAppInfo>();
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(InstalledAppDB.INSTALLED_TABLE,
					null, null, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					InstalledAppInfo appInfo = new InstalledAppInfo();
					appInfo.setName(cursor.getString(0));
					appInfo.setIconId(cursor.getInt(1));
					appInfo.setVersionCode(cursor.getInt(2));
					appInfo.setVersion(cursor.getString(3));
					appInfo.setPackageName(cursor.getString(4));
					appInfo.setApkPath(cursor.getString(5));
					appInfo.setAppFlag(cursor.getInt(6));
					appInfo.setMd5(cursor.getString(7));
					appInfo.setCerStrMd5(cursor.getString(8));
					infos.add(appInfo);
				}
			}
			cursor.close();
		}
		return infos;
	}

	public InstalledAppInfo getInstalledAppInfo(String packageName) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(InstalledAppDB.INSTALLED_TABLE,
					null, InstalledAppDB.PACKAGENAME + "=?",
					new String[] { packageName }, null, null, null);
			InstalledAppInfo appInfo = null;
			if (cursor.moveToFirst()) {
				appInfo = new InstalledAppInfo();
				appInfo.setName(cursor.getString(0));
				appInfo.setIconId(cursor.getInt(1));
				appInfo.setVersionCode(cursor.getInt(2));
				appInfo.setVersion(cursor.getString(3));
				appInfo.setPackageName(cursor.getString(4));
				appInfo.setApkPath(cursor.getString(5));
				appInfo.setAppFlag(cursor.getInt(6));
				appInfo.setMd5(cursor.getString(7));
				appInfo.setCerStrMd5(cursor.getString(8));
			}
			cursor.close();
			return appInfo;
		}
		return null;
	}

	public void deleteInstalledApp(String packageName) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			db.delete(InstalledAppDB.INSTALLED_TABLE,
					InstalledAppDB.PACKAGENAME + "=?",
					new String[] { packageName });
		}
	}

	public void updateInstalledApp(InstalledAppInfo appInfo) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(InstalledAppDB.APPNAME, appInfo.getName());
			values.put(InstalledAppDB.ICONID, appInfo.getIconId());
			values.put(InstalledAppDB.VERSIONCODE,
					appInfo.getVersionCode());
			values.put(InstalledAppDB.VERSION, appInfo.getVersion());
			values.put(InstalledAppDB.PACKAGENAME,
					appInfo.getPackageName());
			values.put(InstalledAppDB.APKPATH, appInfo.getApkPath());
			values.put(InstalledAppDB.APK_MD5, appInfo.getMd5());
			values.put(InstalledAppDB.CER_STR_MD5, appInfo.getCerStrMd5());
			db.update(InstalledAppDB.INSTALLED_TABLE, values,
					InstalledAppDB.PACKAGENAME + "=?",
					new String[] { appInfo.getPackageName() });
		}
	}

	public void updateInstalledAppMd5(String packageName, String md5) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(InstalledAppDB.APK_MD5, md5);
			db.update(InstalledAppDB.INSTALLED_TABLE, values,
					InstalledAppDB.PACKAGENAME + "=?",
					new String[] { packageName });
		}
	}

	public void updateInstalledAppCerStrMd5(String packageName, String cerStrMd5) {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(InstalledAppDB.CER_STR_MD5, cerStrMd5);
			db.update(InstalledAppDB.INSTALLED_TABLE, values,
					InstalledAppDB.PACKAGENAME + "=?",
					new String[] { packageName });
		}
	}

	/**
	 * 删除所有数据
	 */
	public void deleteAll() {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			db.delete(InstalledAppDB.INSTALLED_TABLE, null, null);
		}
	}

	/**
	 * 获取已安装APP列表
	 */
	public void printInstalledAppList() {
		if (db == null || !db.isOpen()) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(InstalledAppDB.INSTALLED_TABLE,
					null, null, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					InstalledAppInfo appInfo = new InstalledAppInfo();
					appInfo.setName(cursor.getString(0));
					appInfo.setIconId(cursor.getInt(1));
					appInfo.setVersionCode(cursor.getInt(2));
					appInfo.setVersion(cursor.getString(3));
					appInfo.setPackageName(cursor.getString(4));
					appInfo.setApkPath(cursor.getString(5));
					appInfo.setAppFlag(cursor.getInt(6));
					appInfo.setMd5(cursor.getString(7));
					appInfo.setCerStrMd5(cursor.getString(8));
					System.out.println(appInfo);
				}
			}
			cursor.close();
		}
	}

}
