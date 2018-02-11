/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.meetingassistant.bean.MeetingInfo;
import cn.tcl.meetingassistant.bean.MeetingStaticInfo;
import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * The tool of to handle the sql table of a meeting's main information
 */
public class MeetingInfoDBUtil {

    protected static final String TABLE_NAME = "meeting";

    protected static final String _ID = "_id";

    private static final String TITLE = "title";

    private static final String TOPICS = "topics";

    private static final String PERSON = "person";

    private static final String START_TIME = "start_time";

    private static final String END_TIME = "end_time";

    private static final String ADDRESS = "address";

    private static final String UPDATE_TIME = "update_time";

    private static final String TAG = MeetingInfoDBUtil.class.getSimpleName();

    protected static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            TITLE + " TEXT, " +
            TOPICS + " TEXT, " +
            PERSON + " TEXT, " +
            ADDRESS + " TEXT, " +
            START_TIME + " DATE, " +
            UPDATE_TIME + " LONG, " +
            END_TIME + " DATE );";


    /**
     * insert a MeetingInfo data
     */
    public static void insert(MeetingInfo meeting, Context context, OnDoneInsertAndUpdateListener listener) {
        DBHelper dbHelper = DBHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        AsynInsertOrUpdateTask asynDataBaseTask = new AsynInsertOrUpdateTask(db);
        asynDataBaseTask.setOnDoneInsertListener(listener);
        asynDataBaseTask.execute(meeting);
    }

    public void updateTime(MeetingInfo meeting){
        meeting.setUpdateTime(System.currentTimeMillis());
    }

    /**
     * delete a MeetingInfo data
     */
    public static void delete(long id, Context context,OnDoneDeletedListener listener) {
        DBHelper dbHelper = DBHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        AsyncDeleteTask asyncDataBaseTask = new AsyncDeleteTask(db,listener);
        asyncDataBaseTask.execute(id);
    }

    /**
     * update a MeetingInfo data
     */
    public static void update(MeetingInfo meeting, Context context, OnDoneInsertAndUpdateListener listener) {
        if (meeting.getId() == -1) {
            throw new RuntimeException("update database: meetingInfo id is must required");
        } else {
            DBHelper dbHelper = DBHelper.getInstance(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            AsynInsertOrUpdateTask asynDataBaseTask = new AsynInsertOrUpdateTask(db);
            asynDataBaseTask.setOnDoneInsertListener(listener);
            asynDataBaseTask.execute(meeting);
        }
    }

    /**
     * MeetingInfo data query,if you want to query all,set id to null;
     *
     * @param context             context
     * @param id                  the id you want to query
     *                            set to null if you want to query all date
     * @param onDoneQueryListener callback listener
     */
    public static void query(Context context, Long id, OnDoneQueryListener onDoneQueryListener) {
        DBHelper dbHelper = DBHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        AsyncQueryTask asynDataBaseTask = new AsyncQueryTask(db);
        asynDataBaseTask.setOnDoneQueryListener(onDoneQueryListener);
        asynDataBaseTask.execute(id);
    }


    /**
     * Copyright (C) 2016 Tcl Corporation Limited
     * <p>
     * Created on 16-8-3.
     * AsyncTask for Insert and Update meetingInfo.IF you want to do something after inserting or updating,
     * set a OnDoneInsertListener to this object.
     */
    private static class AsynInsertOrUpdateTask extends AsyncTask<MeetingInfo, Integer, Long> {

        private long resultCode = -1;
        private SQLiteDatabase db;
        private OnDoneInsertAndUpdateListener onDoneInsertListener;

        public AsynInsertOrUpdateTask(SQLiteDatabase db) {
            this.db = db;
        }

        /**
         * set the callback when insert
         *
         * @param onDoneInsertListener
         */
        public void setOnDoneInsertListener(OnDoneInsertAndUpdateListener onDoneInsertListener) {
            this.onDoneInsertListener = onDoneInsertListener;
        }

        @Override
        protected Long doInBackground(MeetingInfo... meetings) {
            try {
                db.beginTransaction();
                for (MeetingInfo meeting : meetings) {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(TITLE, meeting.getTitle());
                    contentValues.put(TOPICS, meeting.getTopics());
                    contentValues.put(PERSON, meeting.getPersons());
                    contentValues.put(START_TIME, meeting.getStartTime());
                    contentValues.put(END_TIME, meeting.getEndTime());
                    contentValues.put(ADDRESS, meeting.getAddress());
                    contentValues.put(UPDATE_TIME,meeting.getUpdateTime());

                    if (meeting.getId() == -1) {
                        resultCode = db.insert(TABLE_NAME, null, contentValues);
                    } else {
                        resultCode = db.update(TABLE_NAME, contentValues, _ID + "=?",
                                new String[]{String.valueOf(meeting.getId())});
                    }
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                MeetingLog.e(TAG, "fail to insert a meeting info", e);
            } finally {
                db.endTransaction();
                //close the database connection
                return resultCode;
            }
        }

        @Override
        protected void onPostExecute(Long id) {
            super.onPostExecute(id);
            if (onDoneInsertListener != null) {
                onDoneInsertListener.onDone(id);
            }
        }
    }

    /**
     * Copyright (C) 2016 Tcl Corporation Limited
     * <p>
     * Created on 16-8-3.
     * AsyncTask for query meetingInfo.
     */

    private static class AsyncQueryTask extends AsyncTask<Long, Integer, List<MeetingInfo>> {
        private SQLiteDatabase db;
        private OnDoneQueryListener onDoneQueryListener;

        public AsyncQueryTask(SQLiteDatabase db) {
            this.db = db;
        }

        @Override
        protected List<MeetingInfo> doInBackground(Long... longs) {
            Cursor cursor;
            if (longs == null || longs[0] == null) {
                cursor = db.rawQuery("select * from " + TABLE_NAME, null);
            } else {
                cursor = db.rawQuery("select * from " + TABLE_NAME + " where " + _ID + "= ?",
                        new String[]{longs[0].toString()});
            }
            List<MeetingInfo> meetingList = createMeetingFromCursor(cursor);

            //close the query cursor
            cursor.close();
            //close the database connection
            return meetingList;
        }

        @Override
        protected void onPostExecute(List<MeetingInfo> queryResult) throws RuntimeException {
            if (onDoneQueryListener != null) {
                onDoneQueryListener.onDoneQuery(queryResult);
            } else {
                MeetingLog.w(TAG, "This query has no call back");
            }

        }

        public void setOnDoneQueryListener(OnDoneQueryListener onDoneQueryListener) {
            this.onDoneQueryListener = onDoneQueryListener;
        }
    }

    /**
     * Copyright (C) 2016 Tcl Corporation Limited
     * <p>
     * Created on 16-8-3.
     * AsyncTask for delete meetingInfo.
     */
    private static class AsyncDeleteTask extends AsyncTask<Long, Integer, Boolean> {
        private SQLiteDatabase db;
        private OnDoneDeletedListener mListener;

        AsyncDeleteTask(SQLiteDatabase db,OnDoneDeletedListener listener) {
            this.db = db;
            this.mListener = listener;
        }

        @Override
        protected Boolean doInBackground(Long... longs) {
            int num = 0;
            try {
                MeetingLog.d(TAG,"AsyncDeleteTask dobackgrounnd db is open " + db.isOpen());
                db.beginTransaction();
                for (Long id : longs) {
                    db.execSQL("PRAGMA foreign_keys=ON;");
                    num = db.delete(TABLE_NAME, _ID + " =?", new String[]{id.toString()});
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                MeetingLog.e(TAG, "Fail to Delete a meeting", e);
            } finally {
                db.endTransaction();
            }
            return num > 0;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(mListener != null){
                mListener.onDeleted(aBoolean);
            }
            super.onPostExecute(aBoolean);
        }
    }


    /**
     * create a meeting object list with a cursor
     *
     * @param cursor cursor
     * @return list of result
     */
    public static List<MeetingInfo> createMeetingFromCursor(Cursor cursor) {
        ArrayList<MeetingInfo> meetingArrayList = new ArrayList<>();
        if (cursor.getCount() >= 1) {
            while (cursor.moveToNext()) {
                MeetingInfo meeting = new MeetingInfo();
                meeting.setId(cursor.getLong(cursor.getColumnIndex(_ID)));
                meeting.setTitle(cursor.getString(cursor.getColumnIndex(TITLE)));
                meeting.setTopics(cursor.getString(cursor.getColumnIndex(TOPICS)));
                meeting.setPersons(cursor.getString(cursor.getColumnIndex(PERSON)));
                meeting.setAddress(cursor.getString(cursor.getColumnIndex(ADDRESS)));
                meeting.setStartTime(cursor.getLong(cursor.getColumnIndex(START_TIME)));
                meeting.setEndTime(cursor.getLong(cursor.getColumnIndex(END_TIME)));
                meeting.setUpdateTime(cursor.getLong(cursor.getColumnIndex(UPDATE_TIME)));
                meetingArrayList.add(meeting);
            }
        }
        return meetingArrayList;
    }


    /**
     * Copyright (C) 2016 Tcl Corporation Limited
     * <p>
     * Created on 16-8-3.
     * Listener of a finished event of sql query.Must implement this interface
     * when you want to do something after a query you called.
     */
    public interface OnDoneQueryListener {
        void onDoneQuery(List<MeetingInfo> queryResult);
    }

    public interface OnDoneDeletedListener{
        void onDeleted(boolean isSuccess);
    }

    public static ContentValues meetingToContentValues(MeetingInfo meeting) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TITLE, meeting.getTitle());
        contentValues.put(TOPICS, meeting.getTopics());
        contentValues.put(PERSON, meeting.getPersons());
        contentValues.put(START_TIME, meeting.getStartTime());
        contentValues.put(END_TIME, meeting.getEndTime());
        contentValues.put(ADDRESS, meeting.getAddress());
        contentValues.put(UPDATE_TIME,meeting.getUpdateTime());
        return contentValues;
    }
}


