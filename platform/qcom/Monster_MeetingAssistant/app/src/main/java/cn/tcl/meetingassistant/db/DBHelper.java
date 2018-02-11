/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.tcl.meetingassistant.bean.Meeting;
import cn.tcl.meetingassistant.log.MeetingLog;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * App's DBHelper.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MeetingRecorder.db";
    private static final int DATABASE_VERSION = 15;
    private static DBHelper mInstance = null;

    private static final String TAG = DBHelper.class.getSimpleName();

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public synchronized static DBHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DBHelper(context);

        }
        return mInstance;
    }

    public synchronized static DBHelper getInstance() {
        if (mInstance == null) {
            return null;
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createMeetingRecorderTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            MeetingLog.i(TAG, "onUpgrade begin to drop tables");
            db.beginTransaction();
            db.execSQL("DROP TABLE IF EXISTS " + MeetingInfoDBUtil.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + MeetingDecisionDBUtil.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + ImportPointTable.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + VoiceTable.TABLE_NAME);
            db.setTransactionSuccessful();
            db.endTransaction();
            MeetingLog.i(TAG, "onUpgrade end drop tables");
            onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MeetingLog.i(TAG, "onDowngrade begin to drop tables");
        db.beginTransaction();
        db.execSQL("DROP TABLE IF EXISTS " + MeetingInfoDBUtil.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MeetingDecisionDBUtil.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ImportPointTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + VoiceTable.TABLE_NAME);
        db.setTransactionSuccessful();
        db.endTransaction();
        MeetingLog.i(TAG, "onDowngrade end drop tables");
        onCreate(db);
    }

    private void createMeetingRecorderTable(SQLiteDatabase db) {
        //create sqlite tables
        db.beginTransaction();
        db.execSQL(MeetingInfoDBUtil.CREATE_TABLE);
        MeetingLog.i(TAG, "meeting table sql :" + MeetingInfoDBUtil.CREATE_TABLE);
        db.execSQL(MeetingDecisionDBUtil.CREATE_TABLES);
        MeetingLog.i(TAG, "meeting decision table sql :" + MeetingDecisionDBUtil.CREATE_TABLES);
        db.execSQL(ImportPointTable.CREATE_TABLE);
        MeetingLog.i(TAG, "meeting import table table sql :" + ImportPointTable.CREATE_TABLE);
        db.execSQL(VoiceTable.CREATE_TABLE);
        MeetingLog.i(TAG, "record voice table table sql :" + VoiceTable.CREATE_TABLE);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

}
