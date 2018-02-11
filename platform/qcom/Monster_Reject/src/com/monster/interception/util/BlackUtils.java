package com.monster.interception.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog.Calls;

public class BlackUtils {
	
	public static Uri BLACK_URI = Uri.parse("content://com.android.contacts/black");
	public static Uri MARK_URI = Uri.parse("content://com.android.contacts/mark");
	public static final String BLACK_AUTHORITY = "com.android.contacts";
	
	public static String getBlackNameByCalllog(Context context,
			String address) {
		Cursor cursor = null;
		if (address.length() > 10) {
			cursor = context.getContentResolver().query(Calls.CONTENT_URI,
					null, "number LIKE '%" + address + "'" + " and reject=1",
					null, null);
		} else {
			cursor = context.getContentResolver().query(Calls.CONTENT_URI,
					null, "number='" + address + "'" + " and reject=1", null,
					null);
		}
		if (cursor != null) {
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				String name = cursor.getString(cursor
						.getColumnIndex("black_name"));
				cursor.close();
				return name;
			}
			cursor.close();
		}
		return null;
	}
	
	private static Uri uri = BlackUtils.BLACK_URI;
	
	public static String getBlackNameByPhoneNumber(Context context,String address){
		Cursor cursor=null;
		if(address.length()>10){
			cursor= context.getContentResolver().query(uri, null, "number LIKE '%"+address+"'", null, null);
		}else{
			cursor = context.getContentResolver().query(uri, null, "number='"+address+"'", null, null);
		}
		
		if(cursor!=null){
			for(int i = 0; i < cursor.getCount(); i++){
				cursor.moveToPosition(i);
				String name = cursor.getString(cursor.getColumnIndex("black_name"));
				cursor.close();
				return name;
			}
			cursor.close();
		}
		
		return null;
	}
	
	public static String getLableByPhoneNumber(Context context, String address) {
		Cursor cursor = context.getContentResolver().query(uri, null,
				"number='" + address + "'", null, null);
		if (cursor != null) {
			for (int i = 0; i < cursor.getCount(); i++) {
				cursor.moveToPosition(i);
				String lable = cursor.getString(cursor.getColumnIndex("lable"));
				cursor.close();
				return lable;
			}
			cursor.close();
		}

		return null;
	}
	
	public static String getLableByCalllog(Context context,String address){
		Cursor cursor = context.getContentResolver().query(Calls.CONTENT_URI, null, "number='"+address+"'"+" and reject=1", null, null);
		if(cursor!=null){
			for(int i = 0; i < cursor.getCount(); i++){
				cursor.moveToPosition(i);
				String lable = cursor.getString(cursor.getColumnIndex("mark"));
				cursor.close();
				return lable;
			}
			cursor.close();
		}
		
		return null;
	}

}