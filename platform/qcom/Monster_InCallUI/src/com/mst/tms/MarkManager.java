package com.mst.tms;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class MarkManager {
    
    public static final String TAG = "MarkManager";
    
    private static final Uri mMarkUri = Uri.parse("content://com.android.contacts/mark");
    
    public static MarkResult getMark(int type, String number) {
        MarkResult mark = TmsServiceManager.getInstance().getMark(type, number);
        Log.d(TAG, "mark = " + mark);
        return mark;
    }
    
       public static MarkResult getMark(String number) {
           return getMark(16, number);
        }
    
    public static boolean insertUserMark(Context context, String number,String lable) {
        if (number == null) {
            return false;
        }
        
        ContentValues cv = new ContentValues();
        cv.put("lable", lable);
        cv.put("number", number);
        
        try {
        Cursor cursor = context.getContentResolver().query(mMarkUri, null, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    if (lable == null) {
                        int count = context.getContentResolver().delete(mMarkUri, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null);
                        if (count > 0) {
                            return true;
                        }
                    } else {
                        int count = context.getContentResolver().update(mMarkUri, cv, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null);
                        if (count > 0) {
                            return true;
                        }
                    }
                }
            } finally {
                cursor.close();
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Uri uri = context.getContentResolver().insert(mMarkUri, cv);
        if (uri != null) {
            return true;
        }
        
        return false;
    }
    
    public static boolean deleteUserMark(Context context, String number) {
        if (number == null) {
            return false;
        }
        try {
        int count = context.getContentResolver().delete(mMarkUri, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null);
        if (count > 0) {
            return true;
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public static String getUserMark(Context context, String number) {
        if (number == null) {
            return null;
        }
        String result = null;
        try {
        Cursor cursor = context.getContentResolver().query(mMarkUri, null, "PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    result = cursor.getString(1);
                }
            } finally {
                cursor.close();
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
}

