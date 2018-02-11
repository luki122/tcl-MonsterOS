package com.monster.interception.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.monster.interception.util.BlackUtils;
import com.monster.interception.util.SimUtils;

import android.content.Context;
import android.database.Cursor;

public class CallLogDatabaseHelper {

	public static List<CallLogEntity> queryCallLogs(Cursor cursor,
			Context context) {
		Map<String, CallLogEntity> temMap = new HashMap<String, CallLogEntity>();
		String number;
		long id;
		int type;
		long date;
		long duration;
		String area;
		int reject;
		String name;
		String lable;
		int simId;
		CallLogEntity callLogEntity = null;
		CallLogItem callLogItem = null;
		do {

			id = cursor.getLong(cursor.getColumnIndex("_id"));
			number = cursor.getString(cursor.getColumnIndex("number"))
					.replace("-", "").replace(" ", "");
			if (number.startsWith("+86")) {
				number = number.substring(3);
			}
			area = cursor.getString(cursor.getColumnIndex("geocoded_location"));
			date = cursor.getLong(cursor.getColumnIndex("date"));
			type = cursor.getInt(cursor.getColumnIndex("type"));
			duration = cursor.getLong(cursor.getColumnIndex("duration"));
			reject = cursor.getInt(cursor.getColumnIndex("reject"));
			simId = Integer.valueOf(cursor.getString(cursor.getColumnIndex("subscription_id")));

			simId = SimUtils.getSlotbyId(context, simId);
			callLogEntity = temMap.get(number);
			if (callLogEntity == null) {
				callLogEntity = new CallLogEntity();
				name = BlackUtils.getBlackNameByPhoneNumber(context, number);
				if (name == null) {
					name = BlackUtils.getBlackNameByCalllog(context, number);
				}
				if (name == null) {
					callLogEntity.setName("");
				} else {
					callLogEntity.setName(name);
				}
				lable = BlackUtils.getLableByPhoneNumber(context, number);
				if (lable == null) {
					lable = BlackUtils.getLableByCalllog(context, number);
				}
				if (lable == null) {
					callLogEntity.setLabel("");
				} else {
					callLogEntity.setLabel(lable);
				}
				callLogEntity.setSimId(simId);
				callLogEntity.setLastCallDate(date);
				callLogEntity.setDBPhomeNumber(number);
				callLogEntity.setArea(area);
				callLogEntity.setReject(reject);
				temMap.put(number, callLogEntity);
			}
			callLogItem = new CallLogItem();
			callLogItem.setId(id);
			callLogItem.setmType(type);
			callLogItem.setCallTime(date);
			callLogItem.setDuratation(duration);
			callLogEntity.addCallLogItem(callLogItem);

		} while (cursor.moveToNext());
		cursor.close();
		List<CallLogEntity> callLogEntities = new ArrayList<CallLogEntity>(
				temMap.values());
		Collections.sort(callLogEntities);
		return callLogEntities;
	}

}
