package com.monster.interception.util;

import com.mst.tms.AreaManager;
import com.mst.tms.MarkManager;
import com.mst.tms.MarkResult;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;


public class YuloreUtil {

	public static boolean insertUserMark(Context context, String number,
			String lable) {
		if (number == null) {
			return false;
		}

		ContentValues cv = new ContentValues();
		cv.put("lable", lable);
		cv.put("number", number);

		try {
			Cursor cursor = context.getContentResolver().query(BlackUtils.MARK_URI, null,
					"PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null,
					null);
			if (cursor != null) {
				try {
					if (cursor.moveToFirst()) {
						if (lable == null) {
							int count = context.getContentResolver().delete(
									BlackUtils.MARK_URI,
									"PHONE_NUMBERS_EQUAL(number, " + number
											+ ", 0)", null);
							if (count > 0) {
								return true;
							}
						} else {
							int count = context.getContentResolver().update(
									BlackUtils.MARK_URI,
									cv,
									"PHONE_NUMBERS_EQUAL(number, " + number
											+ ", 0)", null);
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

		Uri uri = context.getContentResolver().insert(BlackUtils.MARK_URI, cv);
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
			int count = context.getContentResolver().delete(BlackUtils.MARK_URI,
					"PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null);
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
			Cursor cursor = context.getContentResolver().query(BlackUtils.MARK_URI, null,
					"PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null,
					null);
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

	public static String getMarkContent(String number, Context context) {
		MarkResult mark =  MarkManager.getMark(16, number);
		if(mark != null) {
		    return mark.getName();
		} else {
		    return "";
		}
	}

	public static int getMarkNumber(Context context, String number) {
		MarkResult mark =  MarkManager.getMark(16, number);
        if(mark != null) {
            return mark.getTagCount();
        } else {
            return 0;
        }

	}

	public static String getArea(String num) {
		return AreaManager.getArea(num);
	}

}
