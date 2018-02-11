/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import cn.tcl.meetingassistant.bean.MeetingVoice;

/**
 * Created  on 16-9-2.
 */
public class MeetingVoiceDao {

    private static MeetingVoiceDao mMeetingVoiceDao;

    private Context mContext;

    private MeetingVoiceDao(Context context){
        mContext = context;
    }

    public static synchronized MeetingVoiceDao getInstance(Context context){
        if(null == mMeetingVoiceDao){
            mMeetingVoiceDao = new MeetingVoiceDao(context);
        }
        return mMeetingVoiceDao;
    }

    public synchronized long insert(MeetingVoice value){
        DBHelper dbHelper = DBHelper.getInstance(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues contentValues = beanToContentValues(value);

        long result = db.insert(VoiceTable.TABLE_NAME,null,contentValues);
        //db.close();
        return result;
    }


    public synchronized long delete(long id){
        DBHelper dbHelper = DBHelper.getInstance(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result =  db.delete(VoiceTable.TABLE_NAME, VoiceTable.ID+"=?",
                new String[]{String.valueOf(id)});
       // db.close();
        return result;
    }

    public synchronized long update(MeetingVoice voice,long id){
        DBHelper dbHelper = DBHelper.getInstance(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = beanToContentValues(voice);
        long result = db.update(VoiceTable.TABLE_NAME,values, VoiceTable.ID+"=?",
                new String[]{String.valueOf(id)});
        //db.close();
        return result;
    }

    /**
     *
     * @param meetingId if you want to query all,set meetingId to -1
     * @return
     */
    public synchronized Cursor query(long meetingId){
        DBHelper dbHelper = DBHelper.getInstance(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        cursor = db.rawQuery("select * from " + VoiceTable.TABLE_NAME + " where "
                        + VoiceTable.MEETING_ID + "= ? ",
                new String[]{String.valueOf(meetingId)});
        return cursor;
    }

    /**
     * query all voice which contain content and all images
     * @param meetingId if you want to query all,set meetingId to -1;
     * @return
     */
    public synchronized Cursor queryContentAndAllImage(long meetingId){
        DBHelper dbHelper = DBHelper.getInstance(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        if(meetingId != -1){
            cursor = db.rawQuery("select * from " + VoiceTable.TABLE_NAME + " where "
                            + VoiceTable.MEETING_ID + "= ? ",
                    new String[]{String.valueOf(meetingId)});
        }else {
            cursor = db.rawQuery("select * from " + VoiceTable.TABLE_NAME,null);
        }
        return cursor;
    }



    private ContentValues beanToContentValues(MeetingVoice voice){
        ContentValues contentValues = new ContentValues();
        contentValues.put(VoiceTable.RECORDING_PATH, voice.getVoicePath());
        contentValues.put(VoiceTable.MEETING_ID, voice.getMeetingId());
        contentValues.put(VoiceTable.BOOKMARK_DURATION,voice.getDurationMarks());
        contentValues.put(VoiceTable.CREATE_TIME, voice.getCreateTime());
        contentValues.put(VoiceTable.VOICE_TO_TEXT,voice.getVoiceText());
        return contentValues;
    }

}
