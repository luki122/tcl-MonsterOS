/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.tcl.meetingassistant.bean.ImportPoint;
import cn.tcl.meetingassistant.bean.Meeting;
import cn.tcl.meetingassistant.bean.MeetingDecisionData;
import cn.tcl.meetingassistant.bean.MeetingInfo;
import cn.tcl.meetingassistant.bean.MeetingVoice;
import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * The asyncTask for query all information
 */
public class AsyncQueryAll extends AsyncTask<Void,Void,List<Meeting>> {
    private final String TAG = AsyncQueryAll.class.getSimpleName();
    private Context mContext;

    private CallBack mCallBack;
    public AsyncQueryAll(Context context,CallBack callBack){
        mContext = context;
        mCallBack = callBack;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(null != mCallBack){
            mCallBack.onPreExecute();

        }
    }

    @Override
    protected List<Meeting> doInBackground(Void... voids) {
        // get db
        DBHelper dbHelper = DBHelper.getInstance(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.beginTransaction();

        // query meeting info
        Cursor cursor = db.rawQuery("select * from " + MeetingInfoDBUtil.TABLE_NAME, null);
        List<MeetingInfo> meetingInfos =  MeetingInfoDBUtil.createMeetingFromCursor(cursor);
        List<Meeting> meetings = new ArrayList<>(meetingInfos.size());
        for(MeetingInfo meetingInfo : meetingInfos){
            Meeting meeting = new Meeting();
            meeting.setMeetingInfo(meetingInfo);
            meetings.add(meeting);
        }
        cursor.close();

        // query meeting import point
        cursor = db.rawQuery("select * from " + ImportPointTable.TABLE_NAME, null);
        List<ImportPoint> importPoints = ImportPointDBUtil.createImportPointFromCursor(cursor);
        for(ImportPoint importPoint : importPoints){
            for(Meeting meeting : meetings){
                if(meeting.getId() == importPoint.getMeetingId()){
                    meeting.addImportPoint(importPoint);
                    break;
                }
            }
        }
        cursor.close();

        //query meeting decisions
        cursor = db.rawQuery("select * from " + MeetingDecisionDBUtil.TABLE_NAME, null);
        List<MeetingDecisionData> decisionDatas = MeetingDecisionDBUtil.createDecisionFromCursor(cursor);
        for(MeetingDecisionData decisionData : decisionDatas){
            for(Meeting meeting : meetings){
                if(meeting.getId() == decisionData.getMeetingId()){
                    meeting.addDecision(decisionData);
                    break;
                }
            }
        }
        cursor.close();

        //query meeting voices
        cursor = db.rawQuery("select * from " + VoiceTable.TABLE_NAME, null);
        List<MeetingVoice> meetingVoices = MeetingVoiceDBUtil.createDecisionFromCursor(cursor);
        for(MeetingVoice voice : meetingVoices){
            for(Meeting meeting : meetings){
                if(meeting.getId() == voice.getMeetingId()){
                    meeting.addMeetingVoice(voice);
                    break;
                }
            }
        }
        cursor.close();

        Collections.sort(meetings, new Comparator<Meeting>() {
            @Override
            public int compare(Meeting meeting, Meeting meeting2) {
                int result = 0;
                if(meeting2.getMeetingInfo().getUpdateTime() > meeting.getMeetingInfo().getUpdateTime()){
                    result = 1;
                }else {
                    result = -1;
                }
                MeetingLog.i(TAG,"meeting  " + meeting.getMeetingInfo().getUpdateTime());
                MeetingLog.i(TAG,"meeting2 " + meeting2.getMeetingInfo().getUpdateTime());
                return result;
            }
        });

        db.setTransactionSuccessful();
        db.endTransaction();
        return meetings;
    }

    @Override
    protected void onPostExecute(List<Meeting> meetings) {
        super.onPostExecute(meetings);
        if(null != mCallBack){
            mCallBack.onPostExecute(meetings);
        }
    }

    public interface CallBack{
        void onPreExecute();
        void onPostExecute(List<Meeting> meetings);
    }
}
