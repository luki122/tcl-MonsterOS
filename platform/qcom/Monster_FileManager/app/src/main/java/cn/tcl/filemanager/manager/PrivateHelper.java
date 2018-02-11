/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PrivateHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "msb.db";
    public static final String USER_TABLE_NAME = "mf";
    public static final String FILE_TABLE_NAME = "ff";


    public static final String USER_FIELD_WT = "wt";
    public static final String USER_FIELD_WT1 = "wf1";
    public static final String USER_FIELD_WT2 = "wf2";
    public static final String USER_FIELD_WT3 = "wf3";
    public static final String USER_FIELD_QD = "qd";
    public static final String USER_FIELD_ST = "st";
    public static final String USER_FIELD_CT = "ct";
    public static final String USER_FIELD_CP = "cp";
    public static final String USER_FIELD_CD = "cd";
    public static final String USER_FIELD_OP = "op";
    public static final String USER_FIELD_AL = "al";
    public static final String USER_FIELD_UT = "ut";

    public static final String FILE_FIELD_FT = "ft";
    public static final String FILE_FIELD_WT = "wt";
    public static final String FILE_FIELD_SP = "sp";
    public static final String FILE_FIELD_DP = "dp";
    public static final String FILE_FIELD_DN = "dn";
    public static final String FILE_FIELD_SS = "ss";
    public static final String FILE_FIELD_DS = "ds";
    public static final String FILE_FIELD_TP = "tp";
    public static final String FILE_FIELD_PT = "pt";
    public static final String FILE_FIELD_FS = "fs";
    public static final String FILE_FIELD_CT = "ct";
    public static final String FILE_FIELD_LM = "lm";
    public static final String FILE_FIELD_UT = "ut";
    public static final String FILE_FIELD_CU = "cu";
    public static final String FILE_FIELD_CD = "cd";


    private static int version = 1;

    public PrivateHelper(Context context,String name) {
        super(context, name+DATABASE_NAME, null, version);
        Log.d("DATA", "this is enter " + name + DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + USER_TABLE_NAME
                + " (_id integer primary key autoincrement, " +
                USER_FIELD_WT+" integer not null, "+
                USER_FIELD_WT1 +" text, " +
                USER_FIELD_WT2 +" text, " +
                USER_FIELD_WT3 +" text, " +
                USER_FIELD_QD +" integer not null, " +
                USER_FIELD_ST +" text, " +
                USER_FIELD_CT +" integer not null, " +
                USER_FIELD_CP +" text, " +
                USER_FIELD_CD +" text, " +
                USER_FIELD_OP +" integer not null, " +
                USER_FIELD_AL +" text, " +
                USER_FIELD_UT +" text)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + FILE_TABLE_NAME
                + " (_id integer primary key autoincrement, " +
                FILE_FIELD_FT+" integer not null, "+
                FILE_FIELD_WT+" integer not null, "+
                FILE_FIELD_SP +" text, " +
                FILE_FIELD_DP +" text, " +
                FILE_FIELD_DN +" text, " +
                FILE_FIELD_SS +" integer not null, " +
                FILE_FIELD_DS +" integer not null, " +
                FILE_FIELD_TP +" text, " +
                FILE_FIELD_PT +" integer not null, " +
                FILE_FIELD_FS +" integer not null, " +
                FILE_FIELD_CT +" integer not null, " +
                FILE_FIELD_LM +" integer not null, " +
                FILE_FIELD_UT +" text, " +
                FILE_FIELD_CU +" text, " +
                FILE_FIELD_CD +" text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }

}
