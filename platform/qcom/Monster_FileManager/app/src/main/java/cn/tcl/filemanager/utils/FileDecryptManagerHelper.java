/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;

public class FileDecryptManagerHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "decrypt.db";
    public static final String SHORT_TABLE_NAME = "shortcut_file";
    public static final String FILE_ENCRYPTE_PATH = "encrypte_data";
    public static final String FILE_DECRYPTE_PATH = "decrypte_data";
    public static final String FILE_DECRYPTE_CRAETE_TIME = "decrypte_create_time";
    public static final String FILE_DECRYPTE_MODIFY_TIME = "decrypte_modify_time";

    private static int version = 1;

    public FileDecryptManagerHelper(Context context) {
        super(context, DATABASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SHORT_TABLE_NAME
                + "(_id integer primary key autoincrement, " + FILE_ENCRYPTE_PATH + " text not null, "
                + FILE_DECRYPTE_PATH + " text not null, "
                + FILE_DECRYPTE_CRAETE_TIME + " long not null, "
                + FILE_DECRYPTE_MODIFY_TIME + " long not null"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertDecryptValue(String encryptPath, String decryptPath) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FILE_ENCRYPTE_PATH, encryptPath);
        values.put(FILE_DECRYPTE_PATH, decryptPath);
        File file = new File(decryptPath);
        values.put(FILE_DECRYPTE_CRAETE_TIME, file.lastModified());
        values.put(FILE_DECRYPTE_MODIFY_TIME, file.lastModified());
        sqLiteDatabase.insert(SHORT_TABLE_NAME, null, values);
        sqLiteDatabase.close();
    }

    public void deleValueByDecryptPath(String decryptPath) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(SHORT_TABLE_NAME, FILE_DECRYPTE_PATH + "= ? ", new String[]{decryptPath});
        sqLiteDatabase.close();
    }

    public void updateByDecryptPath(String decryptPath, long modifyTime) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FILE_DECRYPTE_MODIFY_TIME, modifyTime);
        sqLiteDatabase.update(SHORT_TABLE_NAME,values, FILE_DECRYPTE_PATH + " = ? ", new String[]{decryptPath});
        sqLiteDatabase.close();
    }

    public long queryModifyTimeByDecryptPath(String decryptPath) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.query(SHORT_TABLE_NAME, new String[]{FILE_DECRYPTE_MODIFY_TIME}, FILE_DECRYPTE_PATH + " = ?", new String[]{decryptPath}, null, null, null);
        long modifyTime = -1;
        if (cursor != null && cursor.moveToFirst()) {
            modifyTime = cursor.getLong(cursor.getColumnIndex(FILE_DECRYPTE_MODIFY_TIME));
        }
        cursor.close();
        sqLiteDatabase.close();
        return modifyTime;
    }
}
