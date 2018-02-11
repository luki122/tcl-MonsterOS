/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.db;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * Info of highlight table
 */
public class ImportPointTable {

    public static String TABLE_NAME = "highlight";

    public static String _ID = "_id";

    public static String CONTENT = "content";

    public static String MEETING_ID = "meeting_id";

    public static String CREATE_TIME = "create_time";

    public static String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
            _ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            MEETING_ID + " INTEGER," +
            CONTENT + " TEXT," +
            CREATE_TIME + " INTEGER,"+
            "FOREIGN KEY (" + MEETING_ID + ") REFERENCES " +
            MeetingInfoDBUtil.TABLE_NAME + "(" + MeetingInfoDBUtil._ID + "))";
}
