/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import cn.tcl.meetingassistant.bean.ImportPoint;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-3.
 * Import Point Dao
 */
public class ImportPointDao {

    private static ImportPointDao mHighLightDao;

    private Context mContext;

    private ImportPointDao(Context context){
        mContext = context;
    }

    public static synchronized ImportPointDao getInstance(Context context){
        if(null == mHighLightDao){
            mHighLightDao = new ImportPointDao(context);
        }
        return mHighLightDao;
    }

    public synchronized long insert(ImportPoint value){
        DBHelper dbHelper = DBHelper.getInstance(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues contentValues = beanToContentValues(value);

        long result = db.insert(ImportPointTable.TABLE_NAME,null,contentValues);
        //db.close();
        return result;
    }


    public synchronized long delete(long id){
        DBHelper dbHelper = DBHelper.getInstance(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result =  db.delete(ImportPointTable.TABLE_NAME, ImportPointTable._ID+"=?",
                new String[]{String.valueOf(id)});
        //db.close();
        return result;
    }

    public synchronized long update(ImportPoint point,long id){
        DBHelper dbHelper = DBHelper.getInstance(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = beanToContentValues(point);
        long result = db.update(ImportPointTable.TABLE_NAME,values, ImportPointTable._ID+"=?",
                new String[]{String.valueOf(id)});
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
            cursor = db.rawQuery("select * from " + ImportPointTable.TABLE_NAME + " where "
                            + ImportPointTable.MEETING_ID + "= ? ",
                    new String[]{String.valueOf(meetingId)});
        return cursor;
    }

    /**
     * query all import point which contain content and all images
     * @param meetingId if you want to query all,set meetingId to -1;
     * @return
     */
    public synchronized Cursor queryContentAndAllImage(long meetingId){
        DBHelper dbHelper = DBHelper.getInstance(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor;
        if(meetingId != -1){
            cursor = db.rawQuery("select * from " + ImportPointTable.TABLE_NAME + " where "
                            + ImportPointTable.MEETING_ID + "= ? ",
                    new String[]{String.valueOf(meetingId)});
        }else {
            cursor = db.rawQuery("select * from " + ImportPointTable.TABLE_NAME,null);
        }
        return cursor;
    }


    private ContentValues beanToContentValues(ImportPoint point){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ImportPointTable.CONTENT, point.getInfoContent());
        contentValues.put(ImportPointTable.MEETING_ID, point.getMeetingId());
        contentValues.put(ImportPointTable.CREATE_TIME,point.getCreatTime());
        return contentValues;
    }
}
