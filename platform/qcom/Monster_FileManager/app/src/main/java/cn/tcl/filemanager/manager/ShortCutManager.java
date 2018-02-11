/* Copyright (C) 2016 Tcl Corporation Limited */

package cn.tcl.filemanager.manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import cn.tcl.filemanager.utils.FileManagerHelper;

public class ShortCutManager {

    private FileManagerHelper mFileManagerHelper;
    private Context mContext;

    private SQLiteDatabase db;

    public ShortCutManager(Context context) {
/* MODIFIED-BEGIN by haifeng.tang, 2016-04-23,BUG-1956936*/
//        this.mContext = context;
//        mFileManagerHelper = new FileManagerHelper(context);
    }

//    public void insertShortcut(String path) {
//        db = mFileManagerHelper.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(FileManagerHelper.FILE_PATH, path);
//        String sqls = " insert into " + FileManagerHelper.TABLE_NAME + "("
//                + FileManagerHelper.FILE_PATH + ")" + " values( '" + path + "');";
//        try {
//            db.execSQL(sqls);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (db != null) {
//                db.close();
//            }
//        }
//    }
//
//    public List<FileInfo> queryShortCut() {
//        ArrayList<FileInfo> shortcutList = new ArrayList<FileInfo>();
//        shortcutList.clear();
//        db = mFileManagerHelper.getWritableDatabase();
//        Cursor cursor=null;
//        try {
//            String sqls = "select " + FileManagerHelper.FILE_PATH + " from "
//                    + FileManagerHelper.TABLE_NAME + ";";
//             cursor = db.rawQuery(sqls, null);
//            if (cursor != null) {
//                while (cursor.moveToNext()) {
//                    shortcutList.add(new FileInfo(mContext,new File(cursor.getString(cursor
//                            .getColumnIndex(FileManagerHelper.FILE_PATH)))));
//                }
//               Log.d("SHO","this is shortcut "+shortcutList.size());
//            } else {
//                return new ArrayList<FileInfo>();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (db != null) {
//                db.close();
//            }
//            if (cursor!=null){
//                cursor.close();
//            }
//        }
//        if(shortcutList == null){
//            return new ArrayList<>();
//        }
//        return shortcutList;
//    }
//
//    public int getShortcutCount(){
//        db = mFileManagerHelper.getWritableDatabase();
//        Cursor cursor=null;
//        try {
//            String sqls = "select " + FileManagerHelper.FILE_PATH + " from "
//                    + FileManagerHelper.TABLE_NAME + ";";
//            cursor = db.rawQuery(sqls, null);
//            if (cursor != null) {
//                return cursor.getCount();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (db != null) {
//                db.close();
//            }
//            if(cursor!=null){
//                cursor.close();
//            }
//        }
//        return 0;
//    }
//
//    public boolean isShortcutExist(String path){
//        db = mFileManagerHelper.getWritableDatabase();
//        Cursor cursor=null;
//        try {
//            String sqls = "select * from "
//                    + FileManagerHelper.TABLE_NAME + " where "+FileManagerHelper.FILE_PATH +" = ?;";
//            Log.d("SQL","this is sqlite"+sqls);
//             cursor = db.rawQuery(sqls, new String[]{path});
//            if (cursor != null && cursor.getCount()>0) {
//                return false;
//            } else {
//                return true;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (db != null) {
//                db.close();
//            }
//            if(cursor!=null){
//                cursor.close();
//            }
//        }
//        return true;
//    }
//
//    public void deleteShortcut(String path){
//        db = mFileManagerHelper.getWritableDatabase();
//        try {
//            db.delete(FileManagerHelper.TABLE_NAME, FileManagerHelper.FILE_PATH +" = ?;", new String[]{path});
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (db != null) {
//                db.close();
//            }
//        }
//    }
/* MODIFIED-END by haifeng.tang,BUG-1956936*/

}
