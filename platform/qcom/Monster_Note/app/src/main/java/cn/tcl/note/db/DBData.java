/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.note.db;

import android.net.Uri;

public class DBData {
    public final static String URI_AUTHORITY = "cn.tcl.note";
    public final static String DB_NAME = "NoteCN.db";
    public final static Uri TABLE_URI = Uri.parse("content://" + URI_AUTHORITY + "/" + DB_NAME);
    public final static String TABLE_NAME = "notes";
    public final static String COLUMN_ID = "_id";
    public final static String COLUMN_XML = "xml";
    public final static String COLUMN_FIRSTLINE = "first";
    public final static String COLUMN_SECOND_LINE = "second";
    public final static String COLUMN_WILL = "will";
    public final static String COLUMN_IMG = "img";
    public final static String COLUMN_AUDIO = "audio";
    public final static String COLUMN_TIME = "time";
    public final static String COLUMN_THEME = "theme";
}
