/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.timesave;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;

import cn.tcl.note.activity.NoteEditorActivity;
import cn.tcl.note.data.CommonData;
import cn.tcl.note.db.DBData;
import cn.tcl.note.util.NoteLog;
import cn.tcl.note.util.XmlHash;
import cn.tcl.note.util.XmlPrase;

/**
 * 30s save
 */
public class TimeSaveThread extends Thread {
    private final String TAG = TimeSaveThread.class.getSimpleName();
    private Context mContext;
    private ContentResolver mContentResolver;
    private long mId;
    private long mSleepTime = 30 * 1000;//ms
    private int mStatus;
    private final int STATUS_RUN = 1;
    private final int STATUS_PAUSE = 2;
    private final int STATUS_STOP = 3;
    private LinkedList<CommonData> mData;

    public TimeSaveThread(Context context, LinkedList data, long id) {
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        mData = data;
        mId = id;
    }

    @Override
    public void run() {
        try {
            sleep(mSleepTime);
        } catch (InterruptedException e) {
            NoteLog.e(TAG, "sleep error", e);
        }
        while (mStatus == STATUS_RUN || STATUS_PAUSE == mStatus) {
            //not pause state,then save
            NoteLog.d(TAG, "start run time save");
            long startTime = System.currentTimeMillis();
            synchronized (this) {
                if (mStatus == STATUS_RUN) {
                    saveData();
                }
            }
            NoteLog.d(TAG, "end time save,time is " + (System.currentTimeMillis() - startTime));
            try {
                sleep(mSleepTime);
            } catch (InterruptedException e) {
                NoteLog.e(TAG, "sleep error", e);
            }
        }
        NoteLog.d(TAG, "time save thread finish");
    }

    private void saveData() {
        try {
            ((NoteEditorActivity) mContext).saveEditText();
            ContentValues contentValues = XmlPrase.toContentValues(mData);
            if (!XmlHash.iSSameWithBeforeSave(contentValues.getAsString(DBData.COLUMN_XML))) {
                NoteLog.d(TAG, "time save one data");
                mContentResolver.update(DBData.TABLE_URI, contentValues, DBData.COLUMN_ID + "=?", new String[]{"" + mId});
            }
        } catch (ConcurrentModificationException e) {
            NoteLog.e(TAG, "save data fail", e);
        }

    }

    public synchronized void stopSave() {
        mStatus = STATUS_STOP;
        mData = null;
        mContext = null;
        mContentResolver = null;
        NoteLog.d(TAG, "stop time save");
    }

    public void pauseSave() {
        mStatus = STATUS_PAUSE;
        saveData();
        NoteLog.d(TAG, "pause time save");
    }

    public void startSave() {
        mStatus = STATUS_RUN;
        NoteLog.d(TAG, "start time save");
    }
}
