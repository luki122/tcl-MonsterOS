/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.db;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.meetingassistant.bean.ImportPoint;
import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * ImportPointDBUtil
 */
public class ImportPointDBUtil {

    public static String TAG = ImportPointDBUtil.class.getSimpleName();

    public static void insert(ImportPoint importPoint,Context context,OnDoneInsertAndUpdateListener listener){
        AsyncInsertOrUpdateTask asyncDataBaseTask = new AsyncInsertOrUpdateTask(context);
        asyncDataBaseTask.setOnDoneInsertAndUpdateListener(listener);
        asyncDataBaseTask.execute(importPoint);
    }

    public static void query(Context context, Long id, OnDoneQueryListener onDoneQueryListener) {
        AsyncQueryTask asyncDataBaseTask = new AsyncQueryTask(context);
        asyncDataBaseTask.setOnDoneQueryListener(onDoneQueryListener);
        asyncDataBaseTask.execute(id);
    }

    public static void delete(long id, Context context,OnDoneDeletedListener listener) {
        AsyncDeleteTask asyncDataBaseTask = new AsyncDeleteTask(context,listener);
        asyncDataBaseTask.execute(id);
    }

    public static void update(ImportPoint importPoint,Context context,OnDoneInsertAndUpdateListener listener){
        AsyncInsertOrUpdateTask asyncDataBaseTask = new AsyncInsertOrUpdateTask(context);
        asyncDataBaseTask.setOnDoneInsertAndUpdateListener(listener);
        asyncDataBaseTask.execute(importPoint);
    }


    private static class AsyncQueryTask extends AsyncTask<Long, Integer, List<ImportPoint>> {
        private Context mContext;
        private OnDoneQueryListener onDoneQueryListener;

        public AsyncQueryTask(Context context) {
            this.mContext = context;
        }

        @Override
        protected List<ImportPoint> doInBackground(Long... longs) {

            ImportPointDao importPointDao = ImportPointDao.getInstance(mContext);
            Cursor cursor = importPointDao.query(longs[0]);


            List<ImportPoint> pointList = createImportPointFromCursor(cursor);

            //close the query cursor
            cursor.close();
            //close the database connection
            return pointList;
        }

        @Override
        protected void onPostExecute(List<ImportPoint> queryResult) throws RuntimeException {
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

    public static interface OnDoneQueryListener {
        void onDoneQuery(List<ImportPoint> queryResult);
    }


    public static class AsyncInsertOrUpdateTask extends AsyncTask<ImportPoint,Integer,Long>{

        private long mResultCode = -1;
        private Context mContext;
        private OnDoneInsertAndUpdateListener mOnDoneInsertListener;

        public AsyncInsertOrUpdateTask(Context context) {
            this.mContext = context;
        }

        public void setOnDoneInsertAndUpdateListener(OnDoneInsertAndUpdateListener onDoneInsertListener) {
            this.mOnDoneInsertListener = onDoneInsertListener;
        }


        @Override
        protected Long doInBackground(ImportPoint... importPoints) {
            long resultCode = -1;
            ImportPoint point =  importPoints[0];
            ImportPointDao importPointDao = ImportPointDao.getInstance(mContext);
            if(point.getId() == -1){
                resultCode = importPointDao.insert(point);
            }else if((point.getId() != -1)){
                resultCode = importPointDao.update(point,point.getId());
            }
            return resultCode;
        }


        @Override
        protected void onPostExecute(Long aLong) {
            if(null != mOnDoneInsertListener){
                mOnDoneInsertListener.onDone(aLong);
            }

        }
    }

    private static class AsyncDeleteTask extends AsyncTask<Long, Integer, Boolean> {
        private Context mContext;
        private OnDoneDeletedListener mOnDoneDeletedListener;

        AsyncDeleteTask(Context context,OnDoneDeletedListener listener) {
            this.mContext = context;
            mOnDoneDeletedListener = listener;
        }

        @Override
        protected Boolean doInBackground(Long... longs) {
            ImportPointDao importPointDao = ImportPointDao.getInstance(mContext);
            long length = importPointDao.delete(longs[0]);
            return length > 0;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mOnDoneDeletedListener.postDeleted(aBoolean);
        }
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
        public void postDeleted(boolean isSuccess);

    }



    public static List<ImportPoint> createImportPointFromCursor(Cursor cursor) {
        ArrayList<ImportPoint> points = new ArrayList<>(cursor.getCount());
        if (cursor.getCount() >= 1) {
            while (cursor.moveToNext()) {
                ImportPoint point = new ImportPoint();
                point.setId(cursor.getLong(cursor.getColumnIndex(ImportPointTable._ID)));
                point.setInfoContent(cursor.getString(cursor.getColumnIndex(ImportPointTable.CONTENT)));
                point.setMeetingId(cursor.getLong(cursor.getColumnIndex(ImportPointTable.MEETING_ID)));
                point.setCreatTime(cursor.getLong(cursor.getColumnIndex(ImportPointTable.CREATE_TIME)));
                points.add(point);
            }
        }
        return points;
    }
}
