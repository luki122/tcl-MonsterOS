package com.monster.interception.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.monster.interception.util.BlackUtils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog.Calls;


public class SmsDatabaseHelper {

	public static List<SmsEntity> querySms(Cursor cursor,List<MmsItem> list,Context mContext) {
		Map<String, Long> dates = new HashMap<String, Long>();
		Map<String, SmsEntity> temMap = new HashMap<String, SmsEntity>();
		String number;
		long id;
		int type;
		long date=0;
		String body;
		long thread_id;
		int read;
		String name=null;
		SmsEntity smsEntity = null;
		SmsItem smsItem = null;
		if(cursor.getCount()>0){
			do {
				id = cursor.getLong(cursor.getColumnIndex("_id"));
				thread_id=cursor.getLong(cursor.getColumnIndex("thread_id"));
				number = cursor.getString(cursor.getColumnIndex("address")).replace("-","").replace(" ","");
				if(number.startsWith("+86")){
					number=number.substring(3);
				}
				read = cursor.getInt(cursor.getColumnIndex("read"));
				date = cursor.getLong(cursor.getColumnIndex("date"));
				type = cursor.getInt(cursor.getColumnIndex("type"));
				body=cursor.getString(cursor.getColumnIndex("body"));
				smsEntity = temMap.get(number);
				if (smsEntity == null) {
					smsEntity = new SmsEntity();
					name=BlackUtils.getBlackNameByPhoneNumber(mContext, number);
					if(name==null||"".equals(name)){
		        		name=BlackUtils.getBlackNameByCalllog(mContext, number);
		        	}
					if(name==null){
						smsEntity.setName("");
					}else{
						smsEntity.setName(name);
					}
					smsEntity.setLastDate(date);
					dates.put(number, date);
					smsEntity.setDBPhomeNumber(number);
					smsEntity.setBody(body);
					smsEntity.setThread_id(thread_id);
					smsEntity.setIsMms(0);
					smsEntity.setRead(read);
					temMap.put(number, smsEntity);
				}
				smsItem = new SmsItem();
				smsItem.setId(id);
				smsItem.setType(type);
				smsItem.setDate(date);
				smsEntity.addSmsItem(smsItem);

			} while (cursor.moveToNext());
		}
		if(cursor != null) {
			cursor.close();
		}
	
		
		if(list!=null){
			for(int i=0;i<list.size();i++){
				id = list.get(i).getId();
				thread_id=list.get(i).getThread_id();
				Cursor cursors=mContext.getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), null, "_id in (select recipient_ids from threads where _id ="+ thread_id+")", null, null);
				if(!cursors.moveToFirst()){
					if(cursors!=null){
						cursors.close();
					}
					continue;
				}
				number=cursors.getString(cursors.getColumnIndex("address")).replace("-","").replace(" ","");
				if(cursors!=null){
					cursors.close();
				}
				if(number.startsWith("+86")){
					number=number.substring(3);
				}
				date = list.get(i).getDate()*1000;
				type = list.get(i).getType();
				body=list.get(i).getBody();
				read=list.get(i).getRead();
				smsEntity = temMap.get(number);
				if (smsEntity == null) {
					smsEntity = new SmsEntity();
					name=BlackUtils.getBlackNameByPhoneNumber(mContext, number);
					if(name==null||"".equals(name)){
		        		name=BlackUtils.getBlackNameByCalllog(mContext, number);
		        	}
					if(name==null){
						smsEntity.setName("");
					}else{
						smsEntity.setName(name);
					}
					smsEntity.setLastDate(date);
					smsEntity.setDBPhomeNumber(number);
					smsEntity.setBody(body);
					smsEntity.setThread_id(thread_id);
					smsEntity.setIsMms(1);
					smsEntity.setRead(read);
					temMap.put(number, smsEntity);
				}else{
					if(dates.get(number)!=null){
						if(date>dates.get(number)){
							SmsEntity smsEntitys=  temMap.get(number);
							smsEntitys.setLastDate(date);
							smsEntitys.setDBPhomeNumber(number);
							smsEntitys.setBody(body);
							smsEntitys.setThread_id(thread_id);
							smsEntitys.setIsMms(1);
							smsEntity.setRead(read);
							temMap.put(number, smsEntitys);
						}
					}
					
				}
				smsItem = new SmsItem();
				smsItem.setId(id);
				smsItem.setType(type);
				smsItem.setDate(date);
				smsEntity.addSmsItem(smsItem);
			}
		}
		List<SmsEntity> smsEntitys = new ArrayList<SmsEntity>(
				temMap.values());
		Collections.sort(smsEntitys);
		return smsEntitys;
	}


}
