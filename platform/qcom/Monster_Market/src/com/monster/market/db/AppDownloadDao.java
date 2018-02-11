package com.monster.market.db;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.monster.market.download.AppDownloadData;
import com.monster.market.download.AppDownloader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AppDownloadDao {

	private AppDownloadDb appDownload;
	private SQLiteDatabase db;
	protected String TABLE_NAME = AppDownloadDb.DOWNLOAD_TABLE;

	public AppDownloadDao(Context context) {
		appDownload = new AppDownloadDb(context);
	}

	public AppDownloadDao(Context context, String tableName) {
		TABLE_NAME = tableName;
		appDownload = new AppDownloadDb(context);
	}

	/**
	 * 打开数据库
	 */
	public void openDatabase() {
		db = appDownload.getWritableDatabase();
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
	 * 判断是否已存在数据
	 * @param taskId
	 * @return
	 */
	public boolean isExist(String taskId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME, new String[] { AppDownloadDb.TASK_ID }, AppDownloadDb.TASK_ID + "=?",
					new String[] { taskId + "" }, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.close();
				return true;
			}
			cursor.close();
		}
		return false;
	}

	/**
	 * 删除数据库相应记录
	 * 
	 * @param taskId
	 */
	public void delete(String taskId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			db.delete(TABLE_NAME, AppDownloadDb.TASK_ID + "=?", new String[] { taskId + "" });
		}
	}

	/**
	 * 插入一条数据库记录
	 * 
	 * @param data
	 * @param status
	 * @param fileSize
	 */
	public void insert(AppDownloadData data, long createTime, long status, long fileSize) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(AppDownloadDb.TASK_ID, data.getTaskId());
			values.put(AppDownloadDb.APK_ID, data.getApkId());
			values.put(AppDownloadDb.APPNAME, data.getApkName());
			values.put(AppDownloadDb.DOWNLOAD_PATH, data.getApkDownloadPath());
			values.put(AppDownloadDb.VERSION, data.getVersionName());
			values.put(AppDownloadDb.VCODE, data.getVersionCode());
			values.put(AppDownloadDb.PACKAGENAME, data.getPackageName());
			values.put(AppDownloadDb.CREATE_TIME, createTime);
			values.put(AppDownloadDb.STATUS, status);
			values.put(AppDownloadDb.FILE_SIZE, fileSize);
			values.put(AppDownloadDb.ICON_PATH, data.getApkLogoPath());
			values.put(AppDownloadDb.FINISH_TIME, 0);
			values.put(AppDownloadDb.POS, data.getPos());
			values.put(AppDownloadDb.DOWNLOAD_TYPE, data.getDownload_type());
			db.insert(TABLE_NAME, null, values);
		}
	}

	/**
	 * 获取创建时间
	 *
	 * @param taskId
	 * @return
	 */
	public long getCreateTime(String taskId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME, new String[] { AppDownloadDb.CREATE_TIME },
					AppDownloadDb.TASK_ID + "=?", new String[] { taskId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				long createTime = Long.parseLong(cursor.getString(0));
				cursor.close();
				return createTime;
			}
			cursor.close();
		}
		return 0;
	}

	/**
	 * 更新状态
	 *
	 * @param taskId
	 * @param status
	 */
	public void updateStatus(String taskId, int status) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(AppDownloadDb.STATUS, status);
			db.update(TABLE_NAME, values, AppDownloadDb.TASK_ID + "=?", new String[] { taskId + "" });
		}
	}

	/**
	 * 获取状态信息
	 *
	 * @param taskId
	 * @return
	 */
	public int getStatus(String taskId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME, new String[] { AppDownloadDb.STATUS }, AppDownloadDb.TASK_ID + "=?",
					new String[] { taskId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int status = cursor.getInt(0);
				cursor.close();
				return status;
			}
			cursor.close();
		}
		return AppDownloader.STATUS_DEFAULT;
	}

	/**
	 * 更新文件大小
	 * 
	 * @param taskId
	 * @param fileSize
	 */
	public void updateFileSize(String taskId, long fileSize) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(AppDownloadDb.FILE_SIZE, fileSize);
			db.update(TABLE_NAME, values, AppDownloadDb.TASK_ID + "=?", new String[] { taskId + "" });
		}
	}

	/**
	 * 获取文件大小
	 * 
	 * @param taskId
	 * @return
	 */
	public long getFileSize(String taskId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME, new String[] { AppDownloadDb.FILE_SIZE }, AppDownloadDb.TASK_ID + "=?",
					new String[] { taskId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				long size = cursor.getLong(0);
				cursor.close();
				return size;
			}
			cursor.close();
		}
		return 0;
	}

	/**
	 * 获取已文件下载大小
	 * 
	 * @param taskId
	 * @return
	 */
	public long getDownloadSize(String taskId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME, new String[] { AppDownloadDb.DOWN_LENGTH },
					AppDownloadDb.TASK_ID + "=?", new String[] { taskId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				long size = cursor.getLong(0);
				cursor.close();
				return size;
			}
			cursor.close();
		}
		return 0;
	}

	/**
	 * 获取已文件下载大小
	 * 
	 */
	public void updateDownloadSize(String taskId, long downloadSize) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(AppDownloadDb.DOWN_LENGTH, downloadSize);
			db.update(TABLE_NAME, values, AppDownloadDb.TASK_ID + "=?", new String[] { taskId + "" });
		}
	}

	/**
	 * 更新文件存放名称
	 * 
	 * @param taskId
	 * @param fileName
	 */
	public void updateFileName(String taskId, String fileName) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(AppDownloadDb.FILE_NAME, fileName);
			db.update(TABLE_NAME, values, AppDownloadDb.TASK_ID + "=?", new String[] { taskId + "" });
		}
	}

	/**
	 * 更新文件存放目录及名称
	 * 
	 * @param taskId
	 * @param dir
	 * @param fileName
	 */
	public void updateFileDirAndName(String taskId, String dir, String fileName) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(AppDownloadDb.FILE_DIR, dir);
			values.put(AppDownloadDb.FILE_NAME, fileName);
			db.update(TABLE_NAME, values, AppDownloadDb.TASK_ID + "=?", new String[] { taskId + "" });
		}
	}

	/**
	 * 获取文件存放名称
	 * 
	 * @param taskId
	 * @return
	 */
	public String getFileName(String taskId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME, new String[] { AppDownloadDb.FILE_NAME }, AppDownloadDb.TASK_ID + "=?",
					new String[] { taskId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String fileName = cursor.getString(0);
				cursor.close();
				return fileName;
			}
			cursor.close();
		}
		return "";
	}

	/**
	 * 获取文件存放目录
	 * 
	 * @param taskId
	 * @return
	 */
	public String getFileDirWithId(String taskId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME, new String[] { AppDownloadDb.FILE_DIR }, AppDownloadDb.TASK_ID + "=?",
					new String[] { taskId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String dir = cursor.getString(0);
				cursor.close();
				return dir;
			}
			cursor.close();
		}
		return "";
	}

	/**
	 * 获取文件存放目录
	 *
	 * @return
	 */
	public String getFileDirWithPackageName(String pkgName) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME, new String[] { AppDownloadDb.FILE_DIR, AppDownloadDb.FILE_NAME },
					AppDownloadDb.PACKAGENAME + "=?", new String[] { pkgName + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String dir = cursor.getString(0);
				String name = cursor.getString(1);
				cursor.close();
				return dir + name;
			}
			cursor.close();
		}
		return "";
	}

	/**
	 * 更新任务完成时间
	 * 
	 * @param taskId
	 */
	public void updateFileFinishTime(String taskId, long finishTime) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(AppDownloadDb.FINISH_TIME, finishTime);
			db.update(TABLE_NAME, values, AppDownloadDb.TASK_ID + "=?", new String[] { taskId + "" });
		}
	}

	/**
	 * 获取数据库中所有taskId
	 * 
	 * @return
	 */
	public List<String> getAllTaskId() {
		List<String> list = new ArrayList<String>();
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME, new String[] { AppDownloadDb.TASK_ID }, null, null, null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					list.add(cursor.getString(0));
				}
			}
			cursor.close();
		}

		// 删除重复元素
		HashSet<String> h = new HashSet<String>(list);
		list.clear();
		list.addAll(h);

		return list;
	}

	/**
	 * 返回一个AppDownloadData对象, 如果找不到则返回null
	 * 
	 * @param taskId
	 * @return
	 */
	public AppDownloadData getAppDownloadData(String taskId) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadDb.TASK_ID, AppDownloadDb.APK_ID, AppDownloadDb.APPNAME,
							AppDownloadDb.DOWNLOAD_PATH, AppDownloadDb.VERSION, AppDownloadDb.VCODE,
							AppDownloadDb.PACKAGENAME, AppDownloadDb.ICON_PATH, AppDownloadDb.STATUS,
							AppDownloadDb.FILE_DIR, AppDownloadDb.FILE_NAME, AppDownloadDb.FINISH_TIME,
							AppDownloadDb.POS, AppDownloadDb.DOWNLOAD_TYPE},
					AppDownloadDb.TASK_ID + "=?", new String[] { taskId + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				AppDownloadData AppDownloadData = new AppDownloadData();
				int index = 0;
				AppDownloadData.setTaskId(cursor.getString(index));
				AppDownloadData.setApkId(cursor.getInt(++index));
				AppDownloadData.setApkName(cursor.getString(++index));
				AppDownloadData.setApkDownloadPath(cursor.getString(++index));
				AppDownloadData.setVersionName(cursor.getString(++index));
				AppDownloadData.setVersionCode(cursor.getInt(++index));
				AppDownloadData.setPackageName(cursor.getString(++index));
				AppDownloadData.setApkLogoPath(cursor.getString(++index));
				AppDownloadData.setStatus(cursor.getInt(++index));
				AppDownloadData.setFileDir(cursor.getString(++index));
				AppDownloadData.setFileName(cursor.getString(++index));
				AppDownloadData.setFinishTime(cursor.getLong(++index));
				AppDownloadData.setPos(cursor.getString(++index));
				AppDownloadData.setDownload_type(cursor.getString(++index));
				cursor.close();
				cursor = null;
				return AppDownloadData;
			}
			cursor.close();
		}
		return null;
	}

	/**
	 * 得到是否下载完成
	 * 
	 * @param taskId
	 * @return
	 */
	public boolean getIsDownloaded(String taskId) {
		boolean downloaded = false;
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			if (isExist(taskId)) {
				Cursor cursor = db.query(TABLE_NAME, new String[] { AppDownloadDb.STATUS }, AppDownloadDb.TASK_ID + "=?",
						new String[] { taskId + "" }, null, null, null);
				while (cursor.moveToNext()) {
					if (cursor.getInt(0) >= AppDownloader.STATUS_INSTALL_WAIT) {
						File file = new File(getFileDirWithId(taskId), getFileName(taskId));
						if (file.exists()) {
							downloaded = true;
						}
					}
				}
				cursor.close();
			}
		}
		return downloaded;
	}

	/**
	 * 删除所有数据库记录
	 */
	public void deleteAll() {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			db.delete(TABLE_NAME, null, null);
		}
	}

	/**
	 * 根据包名获取fileId
	 * 
	 * @param pkg
	 * @return
	 */
	public int getFileIdByPkg(String pkg) {
		int fileId = -1;
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME, new String[] { AppDownloadDb.APK_ID },
					AppDownloadDb.PACKAGENAME + "=?", new String[] { pkg + "" }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				fileId = cursor.getInt(0);
			}
			cursor.close();
		}
		return fileId;
	}

	/**
	 * 获取已完成但未安装列表
	 * 
	 * @return
	 */
	public List<AppDownloadData> getUninstallApp() {
		List<AppDownloadData> data = new ArrayList<AppDownloadData>();
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadDb.TASK_ID, AppDownloadDb.APK_ID, AppDownloadDb.APPNAME,
							AppDownloadDb.DOWNLOAD_PATH, AppDownloadDb.VERSION, AppDownloadDb.VCODE,
							AppDownloadDb.PACKAGENAME, AppDownloadDb.ICON_PATH, AppDownloadDb.STATUS,
							AppDownloadDb.FILE_DIR, AppDownloadDb.FILE_NAME, AppDownloadDb.FINISH_TIME,
							AppDownloadDb.POS, AppDownloadDb.DOWNLOAD_TYPE},
					AppDownloadDb.INSTALLED + "=? and " + AppDownloadDb.STATUS + "=? or " + AppDownloadDb.STATUS
							+ "=? ",
					new String[] { "0", AppDownloader.STATUS_INSTALL_WAIT + "", AppDownloader.STATUS_INSTALLING + "" },
					null, null, null);
			while (cursor.moveToNext()) {
				// 由于多线程下载会出现多条下载记录，因此把重复ApkId项过滤
				boolean add = true;
				for (AppDownloadData d : data) {
					if (d.getTaskId().equals(cursor.getString(0))) {
						add = false;
						break;
					}
				}
				if (add) {
					AppDownloadData AppDownloadData = new AppDownloadData();
					int index = 0;
					AppDownloadData.setTaskId(cursor.getString(index));
					AppDownloadData.setApkId(cursor.getInt(++index));
					AppDownloadData.setApkName(cursor.getString(++index));
					AppDownloadData.setApkDownloadPath(cursor.getString(++index));
					AppDownloadData.setVersionName(cursor.getString(++index));
					AppDownloadData.setVersionCode(cursor.getInt(++index));
					AppDownloadData.setPackageName(cursor.getString(++index));
					AppDownloadData.setApkLogoPath(cursor.getString(++index));
					AppDownloadData.setStatus(cursor.getInt(++index));
					AppDownloadData.setFileDir(cursor.getString(++index));
					AppDownloadData.setFileName(cursor.getString(++index));
					AppDownloadData.setFinishTime(cursor.getLong(++index));
					AppDownloadData.setPos(cursor.getString(++index));
					AppDownloadData.setDownload_type(cursor.getString(++index));
					data.add(AppDownloadData);
				}
			}
			cursor.close();
		}
		return data;
	}

	/**
	 * 获取已完成列表
	 * 
	 * @return
	 */
	public List<AppDownloadData> getDownloadedApp() {
		List<AppDownloadData> data = new ArrayList<AppDownloadData>();
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME,
					new String[] { AppDownloadDb.TASK_ID, AppDownloadDb.APK_ID, AppDownloadDb.APPNAME,
							AppDownloadDb.DOWNLOAD_PATH, AppDownloadDb.VERSION, AppDownloadDb.VCODE,
							AppDownloadDb.PACKAGENAME, AppDownloadDb.ICON_PATH, AppDownloadDb.STATUS,
							AppDownloadDb.FILE_DIR, AppDownloadDb.FILE_NAME, AppDownloadDb.FINISH_TIME,
							AppDownloadDb.POS, AppDownloadDb.DOWNLOAD_TYPE},
					AppDownloadDb.STATUS + ">=?", new String[] { AppDownloader.STATUS_INSTALL_WAIT + "" }, null, null,
					null);
			while (cursor.moveToNext()) {
				// 由于多线程下载会出现多条下载记录，因此把重复ApkId项过滤
				boolean add = true;
				for (AppDownloadData d : data) {
					if (d.getTaskId().equals(cursor.getString(0))) {
						add = false;
						break;
					}
				}
				if (add) {
					AppDownloadData AppDownloadData = new AppDownloadData();
					int index = 0;
					AppDownloadData.setTaskId(cursor.getString(index));
					AppDownloadData.setApkId(cursor.getInt(++index));
					AppDownloadData.setApkName(cursor.getString(++index));
					AppDownloadData.setApkDownloadPath(cursor.getString(++index));
					AppDownloadData.setVersionName(cursor.getString(++index));
					AppDownloadData.setVersionCode(cursor.getInt(++index));
					AppDownloadData.setPackageName(cursor.getString(++index));
					AppDownloadData.setApkLogoPath(cursor.getString(++index));
					AppDownloadData.setStatus(cursor.getInt(++index));
					AppDownloadData.setFileDir(cursor.getString(++index));
					AppDownloadData.setFileName(cursor.getString(++index));
					AppDownloadData.setFinishTime(cursor.getLong(++index));
					AppDownloadData.setPos(cursor.getString(++index));
					AppDownloadData.setDownload_type(cursor.getString(++index));
					data.add(AppDownloadData);
				}
			}
			cursor.close();
		}
		return data;
	}

	public void setAppInstall(String packageName) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			ContentValues values = new ContentValues();
			values.put(AppDownloadDb.INSTALLED, 1);
			db.update(TABLE_NAME, values, AppDownloadDb.PACKAGENAME + "=?", new String[] { packageName });
		}
	}

	public int getApkIdByPkgName(String packageName) {
		if (db == null) {
			openDatabase();
		}
		if (db != null) {
			Cursor cursor = db.query(TABLE_NAME, new String[] { AppDownloadDb.APK_ID },
					AppDownloadDb.PACKAGENAME + "=?", new String[] { packageName }, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int apkId = cursor.getInt(0);
				cursor.close();
				return apkId;
			}
			cursor.close();
		}
		return 0;
	}

}
