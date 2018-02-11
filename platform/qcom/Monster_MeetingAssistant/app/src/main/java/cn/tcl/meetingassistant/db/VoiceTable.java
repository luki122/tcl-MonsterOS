/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.db;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * The database info of voice
 */
public class VoiceTable {

    private final String TAG = DBHelper.class.getSimpleName();
    public static final String TABLE_NAME = "record_voice";
    public static final String ID = "id";
    public static final String MEETING_ID = "meeting_id";
    public static final String RECORDING_PATH = "recording_path";
    public static final String BOOKMARK_DURATION = "bookmark_duration";
    public static final String CREATE_TIME = "create_time";
    public static final String VOICE_TO_TEXT = "voice_to_text";

    public static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            MEETING_ID + " INTEGER," +
            RECORDING_PATH + " TEXT," +
            CREATE_TIME + " LONG," +
            BOOKMARK_DURATION + " TEXT," +
            VOICE_TO_TEXT + " TEXT," +
            "FOREIGN KEY (" + MEETING_ID + ") REFERENCES " +
            MeetingInfoDBUtil.TABLE_NAME + "(" + MeetingInfoDBUtil._ID + "))";

}


