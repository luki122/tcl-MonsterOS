package com.tcl.monster.fota.provider;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.tcl.monster.fota.downloadengine.DownloadTask;
import com.tcl.monster.fota.utils.FotaLog;

public final class Fota {
    private static final String TAG = "Fota";

    private Fota() {
    }

    public static final class Firmware {
        public static final String ID = "id";
        public static final String PAUSED_REASON = "paused_reason";
        public static final String CURRENTBYTES = "current_bytes";
        public static final String TOTALBYTES = "total_bytes";
        public static final String STATE = "state";
        public static final String URL_BEST = "url_best";
        public static final String UPDATE_INFO_JSON = "updateinfo";
        public static final String DOWNLOAD_INFO_JSON = "downloadinfo";
        public static final String DOWNLOAD_TASKS_JSON = "smalldownloadtasksinfo";

        public static final Uri CONTENT_URI = Uri.parse("content://com.tcl.monster.fota/fota");

        public static final int PAUSED_REASON_NOT_PAUSED = 0;
        public static final int PAUSED_REASON_NETWORK = 1;
        public static final int PAUSED_REASON_SERVER_ERROR = 2;
        public static final int PAUSED_REASON_USER = 3;
        public static final int PAUSED_REASON_ERROR = 4;
        public static final int PAUSE_REASON_STORAGE_NOT_ENOUGH = 5;

        public static void saveDownloadTask(ContentResolver cr, DownloadTask task) {
            FotaLog.d(TAG, "saveDownloadTask -> task = " + task);
            ContentValues values = createDownloadTaskValues(task);
            cr.insert(CONTENT_URI, values);
        }

        public static void updateDownloadTask(ContentResolver cr, DownloadTask task) {
            ContentValues values = createDownloadTaskValues(task);
            String where = "id=? ";
            String[] whereArgs = new String[] {
                task.getId()
            };

            cr.update(CONTENT_URI, values, where, whereArgs);
        }

        public static DownloadTask findDownloadTaskById(ContentResolver cr, String id) {
            if (TextUtils.isEmpty(id)) {
                return null;
            }
            DownloadTask task = null;
            Cursor cursor = cr.query(CONTENT_URI, null, ID + "=?", new String[] {
                id
            }, null);
            if (cursor.moveToNext()) {
                task = restoreDownloadTaskFromCursor(cursor);
            }
            cursor.close();

            return task;
        }

        public static void deleteDownloadTask(ContentResolver cr, DownloadTask task) {
            cr.delete(CONTENT_URI, ID + "=?", new String[] {
                task.getId()
            });
        }

        private static ContentValues createDownloadTaskValues(DownloadTask task) {
            ContentValues values = new ContentValues();
            values.put(ID, task.getId());
            values.put(PAUSED_REASON, task.getPausedReason());
            values.put(CURRENTBYTES, task.getCurrentBytes());
            values.put(TOTALBYTES, task.getTotalBytes());
            values.put(STATE, task.getState());
            values.put(URL_BEST, task.getBestUrl());
            values.put(UPDATE_INFO_JSON, task.getUpdateInfoJson());
            values.put(DOWNLOAD_INFO_JSON, task.getDownloadInfoJson());
            values.put(DOWNLOAD_TASKS_JSON, task.getSmallTasksJson());
            return values;
        }

        public static DownloadTask restoreDownloadTaskFromCursor(Cursor cursor) {
            DownloadTask task = new DownloadTask();
            task.setId(cursor.getString(cursor.getColumnIndex(ID)));
            task.setPausedReason(cursor.getInt(cursor.getColumnIndex(PAUSED_REASON)));
            task.setCurrentBytes(cursor.getLong(cursor.getColumnIndex(CURRENTBYTES)));
            task.setTotalBytes(cursor.getLong(cursor.getColumnIndex(TOTALBYTES)));
            task.setState(cursor.getString(cursor.getColumnIndex(STATE)));
            task.setBestUrl(cursor.getString(cursor.getColumnIndex(URL_BEST)));
            task.setUpdateInfoJson(cursor.getString(cursor.getColumnIndex(UPDATE_INFO_JSON)));
            task.setDownlaodInfoJson(cursor.getString(cursor.getColumnIndex(DOWNLOAD_INFO_JSON)));
            task.setSmallTasksJson(cursor.getString(cursor.getColumnIndex(DOWNLOAD_TASKS_JSON)));
            FotaLog.v(TAG, "restoreDownloadTaskFromCursor -> task = " + task);
            return task;
        }
    }

    public static final class Report {
        public static final String _ID = "_id";
        public static final String PARAM = "param";
        public static final String ORIGIN = "origin";

        public static final int FIRMWARE_REPORT = 0;
        public static final int APP_REPORT = 1;

        public static final Uri CONTENT_URI = Uri.parse("content://com.tcl.monster.fota/report");
    }
}