/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.db;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.meetingassistant.bean.MeetingDecisionData;
import cn.tcl.meetingassistant.bean.MeetingVoice;
import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * Created by user on 16-9-2.
 */
public class MeetingVoiceDBUtil {

    private static final String TAG = MeetingVoiceDBUtil.class.getSimpleName();


    public static void insert(MeetingVoice meetingVoice,Context context,
                              OnDoneInsertAndUpdateListener onDoneInsertAndUpdateListener){
        AsyncInsertOrUpdateTask asyncDataBaseTask = new AsyncInsertOrUpdateTask(context);
        asyncDataBaseTask.setOnDoneInsertAndUpdateListener(onDoneInsertAndUpdateListener);
        asyncDataBaseTask.execute(meetingVoice);
    }


    public static void delete(long id,Context context,
                              OnDoneDeletedListener onDoneDeletedListener){
        AsyncDeleteTask asyncDataBaseTask = new AsyncDeleteTask(context,onDoneDeletedListener);
        asyncDataBaseTask.execute(id);
    }

    public static void update(MeetingVoice meetingVoice,Context context,OnDoneInsertAndUpdateListener listener){
        AsyncInsertOrUpdateTask asyncDataBaseTask = new AsyncInsertOrUpdateTask(context);
        asyncDataBaseTask.setOnDoneInsertAndUpdateListener(listener);
        asyncDataBaseTask.execute(meetingVoice);
    }




    public static class AsyncInsertOrUpdateTask extends AsyncTask<MeetingVoice,Integer,Long> {

        private Context mContext;
        private OnDoneInsertAndUpdateListener mOnDoneInsertListener;

        public AsyncInsertOrUpdateTask(Context context) {
            this.mContext = context;
        }

        public void setOnDoneInsertAndUpdateListener(OnDoneInsertAndUpdateListener onDoneInsertListener) {
            this.mOnDoneInsertListener = onDoneInsertListener;
        }


        @Override
        protected Long doInBackground(MeetingVoice... voices) {
            long resultCode = -1;
            MeetingVoice point =  voices[0];
            MeetingVoiceDao meetingVoiceDao = MeetingVoiceDao.getInstance(mContext);
            if(point.getId() == -1){
                resultCode = meetingVoiceDao.insert(point);
            }else if((point.getId() != -1)){
                resultCode = meetingVoiceDao.update(point,point.getId());
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
            MeetingVoiceDao meetingVoiceDao = MeetingVoiceDao.getInstance(mContext);
            long count = meetingVoiceDao.delete(longs[0]);
            return count > 0;
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


    /**
     * create a decision object list with a cursor
     *
     * @param cursor cursor
     * @return list of result
     */
    public static List<MeetingVoice> createDecisionFromCursor(Cursor cursor) {
        ArrayList<MeetingVoice> voiceArrayList = new ArrayList<>();
        if (cursor.getCount() >= 1) {
            while (cursor.moveToNext()) {
                MeetingVoice voice = new MeetingVoice();
                voice.setId(cursor.getLong(cursor.getColumnIndex(VoiceTable.ID)));
                voice.setMeetingId(cursor.getLong(cursor.getColumnIndex(VoiceTable.MEETING_ID)));
                voice.setCreateTime(cursor.getLong(cursor.getColumnIndex(VoiceTable.CREATE_TIME)));
                voice.setVoicePath(cursor.getString(cursor.getColumnIndex(VoiceTable.RECORDING_PATH)));
                voice.setDurationMarks(cursor.getString(cursor.getColumnIndex(VoiceTable.BOOKMARK_DURATION)));
                voice.setVoiceText(cursor.getString(cursor.getColumnIndex(VoiceTable.VOICE_TO_TEXT)));
                voiceArrayList.add(voice);
            }
        }
        MeetingLog.d(TAG, "query all voice info,size is " + voiceArrayList.size());
        return voiceArrayList;
    }

}
