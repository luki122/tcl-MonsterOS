package com.monster.market.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

	private static final String TAG = "TimeUtil";
	
	/**
	* @Title: getTimeString
	* @Description: 获取时间戳，格式为20140702102040
	* @param @param time
	* @param @return
	* @return String
	* @throws
	 */
	public static String getTimeString(long time) {
		Date date = new Date(time);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
		return dateFormat.format(date);
	}

	/**
	 * 获取现在时间
	 * @return 返回短时间格式 yyyy-MM-dd
	 */
	public static String getStringDateShort() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 获取时间 yyyy-MM-dd
	 * @param time
	 * @return
     */
	public static String getStringDateShort(long time) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(time);
		return dateString;
	}

	/**
	 * 判断是否缓存有效时间(凌晨3点)
	 * @param cacheTime
	 * @return
     */
	public static boolean isCacheEffectiveTime(long cacheTime) {
		if (cacheTime != 0) {
			int effectiveHour = 3;
			Calendar cacheCalender = Calendar.getInstance();
			cacheCalender.setTimeInMillis(cacheTime);

			Calendar effectiveCalendar = Calendar.getInstance();
			effectiveCalendar.set(cacheCalender.get(Calendar.YEAR), cacheCalender.get(Calendar.MONTH),
					cacheCalender.get(Calendar.DATE), effectiveHour, 0, 0);

			int cacheHour = cacheCalender.get(Calendar.HOUR_OF_DAY);
			long effectiveTime = 0;
			if (cacheHour < effectiveHour) {
				effectiveTime = effectiveCalendar.getTimeInMillis();
			} else {
				effectiveCalendar.add(Calendar.HOUR_OF_DAY, 24);
				effectiveTime = effectiveCalendar.getTimeInMillis();
			}

			if (System.currentTimeMillis() < effectiveTime) {
				return true;
			}

		}

		return false;
	}

}
