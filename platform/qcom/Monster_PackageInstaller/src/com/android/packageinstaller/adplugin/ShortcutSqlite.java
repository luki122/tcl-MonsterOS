package com.android.packageinstaller.adplugin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ShortcutSqlite  extends SQLiteOpenHelper{
	 private static final String DATABASE_NAME = "shortcut_data.db";
     private static final int DATABASE_VERSION = 1;
     
     public static final String TABLE_NAME = "shortcut";
     public static final String AD_PACKAGENAME = "ad_packagename";
     public static final String AD_NAME = "ad_name";
     
     public static final String TABLE_SQL = "CREATE TABLE " + TABLE_NAME + " ("
     		+ AD_PACKAGENAME + " TEXT PRIMARY KEY,"
             + AD_NAME + " TEXT"+");";
	
     public ShortcutSqlite(Context context) {

         // calls the super constructor, requesting the default cursor factory.
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }

	@Override
	public void onCreate(SQLiteDatabase db) {
		//创建数据库
 	   db.execSQL(TABLE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	public void save(String pkgName,String name){
		SQLiteDatabase db = getReadableDatabase() ;
		ContentValues cv=new ContentValues();  
        cv.put(AD_PACKAGENAME, pkgName);  
        cv.put(AD_NAME, name); 
        Cursor cursor = db.rawQuery("select  *  from " + TABLE_NAME + "  where  "  + AD_PACKAGENAME + " =   ? " , new String[]{pkgName}) ;
		if(cursor == null || cursor.getCount() <= 0 ){
			getWritableDatabase().insert(TABLE_NAME, null, cv) ;
		}
		cursor.close() ;
		db.close() ;
	}
	
	public boolean isProhibit(String pkgName,String name){
		SQLiteDatabase db = getReadableDatabase() ;
		try{
				Cursor cursor = db.rawQuery("select  *  from " + TABLE_NAME + "  where  "  + AD_PACKAGENAME + " =   ? " , new String[]{pkgName}) ;
				if(cursor != null && cursor.getCount() > 0 ){
					return true ;
				}
				cursor.close() ;
		}catch(Exception e){
			e.printStackTrace() ;
		}finally{
			db.close() ;
		}
		return false ;
	}
	
	
	public void delete(String pkgName,String name){
		SQLiteDatabase db = getWritableDatabase() ;
	    String whereClause = AD_PACKAGENAME + " = ?";//删除的条件  
	    String[] whereArgs = {pkgName};//删除的条件参数  
	    db.delete(TABLE_NAME,whereClause,whereArgs);//执行删除  
	    db.close() ;
	}
	

}
