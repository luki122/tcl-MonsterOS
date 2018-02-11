/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.meetingassistant.bean.MeetingDecisionData;
import cn.tcl.meetingassistant.log.MeetingLog;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * The tool of to handle the sql table of a meeting decision information
 */
public class MeetingDecisionDBUtil {

    protected static final String TABLE_NAME = "meeting_decision";
    private static final String ID = "id";
    private static final String MEETING_ID = "meetingID";
    private static final String DECISION_INFO = "decisionInfo";
    private static final String PERSONS = "persons";
    private static final String DEADLINE = "deadline";

    private static final String TAG = MeetingDecisionDBUtil.class.getSimpleName();

    protected static String CREATE_TABLES = "CREATE TABLE " + TABLE_NAME + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            MEETING_ID + " INTEGER," +
            DECISION_INFO + " TEXT," +
            PERSONS + " TEXT," +
            DEADLINE + " LONG," +
            "FOREIGN KEY (" + MEETING_ID + ") REFERENCES " +
            MeetingInfoDBUtil.TABLE_NAME + "(" + MeetingInfoDBUtil._ID + "))";

    /**
     * insert a decision of a meeting to database,set a listener to recall when
     * finish inserting as the same time
     *
     * @param context         context
     * @param meetingDecision the object to insert to database
     * @param doneListener    listener
     */
    public static void insert(Context context, MeetingDecisionData meetingDecision,
                              OnDoneInsertAndUpdateListener doneListener) {
        DBHelper dbHelper = DBHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        AsyncInsertOrUpdateTask asyncInsertOrUpdateTask = new AsyncInsertOrUpdateTask(db);
        asyncInsertOrUpdateTask.setOnDoneInsertListener(doneListener);
        asyncInsertOrUpdateTask.execute(meetingDecision);
    }

    /**
     * delete a decision of a meeting to database,set a listener to recall when
     * finish inserting as the same time
     *
     * @param id                    the primary key id of a decision what you want to delete
     * @param context               context
     * @param onDoneDeletedListener the listener support call back method you want
     */
    public static void delete(long id, Context context, OnDoneDeletedListener onDoneDeletedListener) {
        DBHelper dbHelper = DBHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        AsyncDeleteTask asyncDataBaseTask = new AsyncDeleteTask(db, onDoneDeletedListener);
        asyncDataBaseTask.execute(id);
    }

    /**
     * query data,if you want to query all ,set id to null.
     *
     * @param context             context
     * @param id                  the id you want to query
     *                            set to null if you want to query all date
     * @param meeting_id          the meeting id you want to query
     * @param onDoneQueryListener callback listener
     */
    public static void query(Context context, Long id, Long meeting_id, OnDoneQueryListener onDoneQueryListener) {
        DBHelper dbHelper = DBHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        AsyncQueryTask asynDataBaseTask = new AsyncQueryTask(db);
        asynDataBaseTask.setOnDoneQueryListener(onDoneQueryListener);
        asynDataBaseTask.execute(id, meeting_id);
    }

    /**
     * update a decision of a meeting to database,set a listener to recall when
     * finish update as the same time
     *
     * @param context            context
     * @param meetingDecision    the object to update to database
     * @param doneInsertListener listener callback listener,set null if you don't need it
     */
    public static void update(Context context, MeetingDecisionData meetingDecision,
                              OnDoneInsertAndUpdateListener doneInsertListener) {
        DBHelper dbHelper = DBHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        AsyncInsertOrUpdateTask asyncInsertOrUpdateTask = new AsyncInsertOrUpdateTask(db);
        asyncInsertOrUpdateTask.setOnDoneInsertListener(doneInsertListener);
        asyncInsertOrUpdateTask.execute(meetingDecision);
    }

    /**
     * Copyright (C) 2016 Tcl Corporation Limited
     * <p>
     * Created on 16-8-3.
     * AsyncTask for Insert and Update meeting decision.If you want to do something after inserting or updating,
     * set a OnDoneInsertListener to this object.
     */
    public static class AsyncInsertOrUpdateTask extends AsyncTask<MeetingDecisionData, Long, Long> {

        private SQLiteDatabase db;

        private OnDoneInsertAndUpdateListener onDoneInsertListener;

        private long id = -1;

        AsyncInsertOrUpdateTask(SQLiteDatabase sqLiteDatabase) {
            db = sqLiteDatabase;
        }

        public void setOnDoneInsertListener(OnDoneInsertAndUpdateListener onDoneInsertListener) {
            this.onDoneInsertListener = onDoneInsertListener;
        }

        @Override
        protected Long doInBackground(MeetingDecisionData... meetingDecisions) {
            {
                db.beginTransaction();
                try {
                    for (MeetingDecisionData decision : meetingDecisions) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(DECISION_INFO, decision.getDecisionInfo());
                        contentValues.put(PERSONS, decision.getPersons());
                        contentValues.put(DEADLINE, decision.getDeadline());
                        contentValues.put(MEETING_ID, decision.getMeetingId());
                        id = decision.getId();
                        if (decision.getId() == -1) {
                            id = db.insert(TABLE_NAME, null, contentValues);
                        } else {
                            id = db.update(TABLE_NAME, contentValues, ID + "=?",
                                    new String[]{String.valueOf(decision.getId())});
                        }
                    }
                    db.setTransactionSuccessful();
                } catch (Exception e) {
                    MeetingLog.e(TAG, "Fail to insert or update this meeting decision", e);
                    return id;
                } finally {
                    db.endTransaction();
                    //close the database connection
                }
                return id;
            }
        }

        @Override
        protected void onPostExecute(Long aLong) {
            if (onDoneInsertListener != null)
                onDoneInsertListener.onDone(aLong);
            super.onPostExecute(aLong);
        }
    }

    /**
     * Copyright (C) 2016 Tcl Corporation Limited
     * <p>
     * Created on 16-8-3.
     * AsyncTask for query meeting decision.If you want to do something after querying,
     * set a onDoneQueryListener to this object.
     */
    private static class AsyncQueryTask extends AsyncTask<Long, Integer, List<MeetingDecisionData>> {
        private SQLiteDatabase db;
        private OnDoneQueryListener onDoneQueryListener;

        public AsyncQueryTask(SQLiteDatabase db) {
            this.db = db;
        }

        public void setOnDoneQueryListener(OnDoneQueryListener onDoneQueryListener) {
            this.onDoneQueryListener = onDoneQueryListener;
        }

        @Override
        protected List<MeetingDecisionData> doInBackground(Long... longs) {
            db.beginTransaction();
            Cursor cursor;
            if (longs == null || (longs[0] == null && longs[1] == null)) {
                cursor = db.rawQuery("select * from " + TABLE_NAME, null);
            } else {
                //if meeting id is not null
                if (longs[0] != null) {
                    cursor = db.rawQuery("select * from " + TABLE_NAME + " where " + MEETING_ID + "= ? ",
                            new String[]{longs[0].toString()});
                } else {
                    cursor = db.rawQuery("select * from " + TABLE_NAME + " where " + ID + "= ?",
                            new String[]{longs[1].toString()});
                }
            }

            List<MeetingDecisionData> meetingList = createDecisionFromCursor(cursor);
            //close the query cursor
            cursor.close();
            db.setTransactionSuccessful();
            db.endTransaction();
            //close the database connection
            return meetingList;
        }

        @Override
        protected void onPostExecute(List<MeetingDecisionData> meetingDecisions) {
            super.onPostExecute(meetingDecisions);
            onDoneQueryListener.onDone(meetingDecisions);
        }
    }

    /**
     * Copyright (C) 2016 Tcl Corporation Limited
     * <p>
     * Created on 16-8-3.
     * AsyncTask for delete meeting decision.If you want to do something after deleting,
     * set a onDoneDeletedListener to this object.
     */
    private static class AsyncDeleteTask extends AsyncTask<Long, Integer, Boolean> {
        private SQLiteDatabase db;
        private OnDoneDeletedListener onDoneDeletedListener;

        AsyncDeleteTask(SQLiteDatabase db, OnDoneDeletedListener onDoneDeletedListener) {
            this.db = db;
            this.onDoneDeletedListener = onDoneDeletedListener;
        }

        @Override
        protected Boolean doInBackground(Long... longs) {
            db.beginTransaction();
            try {
                for (Long id : longs) {
                    db.delete(TABLE_NAME, ID + " =?", new String[]{id.toString()});
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                MeetingLog.e(TAG, "Fail to delete this meeting decision", e);
                return false;
            } finally {
                db.endTransaction();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (null != onDoneDeletedListener && aBoolean) {
                onDoneDeletedListener.postDeleted();
            } else if (null != onDoneDeletedListener && !aBoolean) {
                onDoneDeletedListener.failDeleted();
            }
        }
    }

    /**
     * create a decision object list with a cursor
     *
     * @param cursor cursor
     * @return list of result
     */
    public static List<MeetingDecisionData> createDecisionFromCursor(Cursor cursor) {
        ArrayList<MeetingDecisionData> decisionsArrayList = new ArrayList<>();
        if (cursor.getCount() >= 1) {
            while (cursor.moveToNext()) {
                MeetingDecisionData decision = new MeetingDecisionData();
                decision.setId(cursor.getLong(cursor.getColumnIndex(ID)));
                decision.setMeetingId(cursor.getLong(cursor.getColumnIndex(MEETING_ID)));
                decision.setDecisionInfo(cursor.getString(cursor.getColumnIndex(DECISION_INFO)));
                decision.setPersons(cursor.getString(cursor.getColumnIndex(PERSONS)));
                decision.setDeadline(cursor.getLong(cursor.getColumnIndex(DEADLINE)));
                decisionsArrayList.add(decision);
            }
        }
        MeetingLog.d(TAG, "query all Meeting decision,size is " + decisionsArrayList.size());
        return decisionsArrayList;
    }

    public interface OnDoneQueryListener {
        void onDone(List<MeetingDecisionData> decisionsArrayList);
    }

    /**
     * Copyright (C) 2016 Tcl Corporation Limited
     * <p>
     * Created on 16-8-3.
     * Listener of a finished event of sql delete.Must implement this interface
     * when you want to do something after a delete you call.
     */
    public interface OnDoneDeletedListener {
        /**
         * If deleting is successful,this method will be called.
         */
        public void postDeleted();

        /**
         * If deleting is unsuccessful,this method will be called.
         */
        public void failDeleted();
    }

}
