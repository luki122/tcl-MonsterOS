package com.android.calendar.mst.utils;

import java.util.Locale;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.Utils;

public class CalendarUtils {
	private static final String LOG_TAG = "GNCalendarUtils";

	public static final String GN_PREF_NAME = "com.gionee.calendar.pref";

	public static final int MIN_YEAR_NUM = 1970;
	public static final int MAX_YEAR_NUM = 2036;

	public static boolean isYearInRange(Time t) {
		if(t == null) return false;
		
		return (t.year >= MIN_YEAR_NUM && t.year <= MAX_YEAR_NUM);
	}

	public static void correctInvalidTime(Time t) {
		if(t == null) return;
		
		if(t.year >= CalendarUtils.MAX_YEAR_NUM) {
			t.year = CalendarUtils.MAX_YEAR_NUM;
			t.month = 11;
			t.monthDay = 31;
		} else {
			t.year = CalendarUtils.MIN_YEAR_NUM;
			t.month = 0;
			t.monthDay = 1;
		}
		t.normalize(true);
	}

	public static final class MonthName {
		private static final int MONTH_BASE = 0;
		
		public static final int JANURAY = MONTH_BASE;
		public static final int FEBRURAY = MONTH_BASE + 1;
	}

	// compare two Time object, if these two object indicate
	// the same date, then return true
	public static boolean isIdenticalDate(Time date1, Time date2) {
		if(date1 == null || date2 == null) {
			return false;
		}
		
		if(date1.year == date2.year && date1.yearDay == date2.yearDay) {
			return true;
		}
		
		return false;
	}

	public static int compareDate(Time date1, Time date2) {
		if(date1 == null || date2 == null) {
			throw new RuntimeException("invalid null params");
		}
		
		int diffYear = date1.year - date2.year;
		if(diffYear != 0) return diffYear;
		
		int diffMonth = date1.month - date2.month;
		if(diffMonth != 0) return diffMonth;
		
		int diffMonthDay = date1.monthDay - date2.monthDay;
		return diffMonthDay;
	}

	public static boolean isIdenticalMonth(Time date1, Time date2) {
		if(date1 == null || date2 == null) {
			return false;
		}
		
		if(date1.year == date2.year && date1.month == date2.month) {
			return true;
		}
		
		return false;
	}

	public static int compareMonth(Time date1, Time date2) {
		if(date1 == null || date2 == null) {
			throw new RuntimeException("invalid null params");
		}
		
		int diffYear = date1.year - date2.year;
		if(diffYear != 0) return diffYear;
		
		int diffMonth = date1.month - date2.month;
		return diffMonth;
	}

	// compute week num of first day of specific month, 20
	public static int getWeekNumOfFirstMonthDay(Time time, int firstDayOfWeek) {
		Time temp = new Time(time);
		temp.monthDay = 1;
		long millis = temp.normalize(true);
		
		int julianDay = Time.getJulianDay(millis, temp.gmtoff);
		int weekNum = Utils.getWeeksSinceEpochFromJulianDay(julianDay, firstDayOfWeek);
		
		return weekNum;
	}

	public static Time getJulianMondayTimeFromWeekNum(int weekNum) {
		int julianDay = Utils.getJulianMondayFromWeeksSinceEpoch(weekNum);
		Time time = new Time();
		// Utils.setJulianDayInGeneral(time, julianDay);
		time.setJulianDay(julianDay);
		time.normalize(true);
		
		return time;
	}

	public static String printDate(Time time) {
		StringBuilder builder = new StringBuilder(100);
		builder.append(time.year).append("-").
				append(time.month + 1).append("-").
				append(time.monthDay);
		
		return builder.toString();
	}

	public static String printTime(Time time) {
		StringBuilder builder = new StringBuilder(100);
		builder.append(time.year).append("-").
				append(time.month + 1).append("-").
				append(time.monthDay);
		builder.append(" ").append(time.hour).append(":").
				append(time.minute).append(":").
				append(time.second);
		
		return builder.toString();
	}

	public static String printDate(int julianDay) {
		Time time = new Time();
		time.setJulianDay(julianDay);
		time.normalize(true);
		
		return printDate(time);
	}

	public static int getLastMonthDayJulianDay(Time currTime) {
		Log.d(LOG_TAG, "invoke getLastMonthDayJulianDay() begin");
		Log.d(LOG_TAG, "current date is: " + printDate(currTime));
		Time temp = new Time(currTime);
		temp.month += 1;
		temp.monthDay = 1;
		temp.monthDay -= 1;
		temp.normalize(true);
		Log.d(LOG_TAG, "the last month day is: " + printDate(temp));
		Log.d(LOG_TAG, "invoke getLastMonthDayJulianDay() end");
		
		return Time.getJulianDay(temp.toMillis(true), temp.gmtoff);
	}

	public static boolean isChineseEnv() {
    	String lang = Locale.getDefault().getLanguage();
    	if(lang.equals(Locale.CHINA.getLanguage()) 
    			|| lang.equals(Locale.CHINESE.getLanguage()) 
    			|| lang.equals(Locale.TAIWAN.getLanguage())
    			|| lang.equals(Locale.SIMPLIFIED_CHINESE.getLanguage()) 
    			|| lang.equals(Locale.TRADITIONAL_CHINESE.getLanguage())) {
    		// Chinese env
    		return true;
    	}
    	
    	return false;
    }

	public static long checkTimeRange(long millis) {
		Time t = new Time();
		t.set(millis);
		
		if(t.year < CalendarUtils.MIN_YEAR_NUM) {
			t.year = CalendarUtils.MIN_YEAR_NUM;
			t.month = 0;
			t.monthDay = 1;
			t.normalize(true);
			
			return t.toMillis(true);
		} else if(t.year > CalendarUtils.MAX_YEAR_NUM) {
			t.year = CalendarUtils.MAX_YEAR_NUM;
			t.month = 11;
			t.monthDay = 31;
			t.normalize(true);
			
			return t.toMillis(true);
		}
		
		return millis;
	}

	public static void updateActionBarTime(Time time, Context context) {
		if(time == null || time.year < 1970 || time.year > 2036) {
			return;
		}
		
		Time now = new Time(time.timezone);
		now.setToNow();
		now.normalize(true);
		time.hour = now.hour;
		time.minute = now.minute;
		time.second = now.second;
		time.normalize(true);
	
	    CalendarController controller = CalendarController.getInstance(context);
		controller.sendEvent(context, EventType.UPDATE_TITLE, null, null, time, 
				-1, ViewType.CURRENT, 0, null, null);
	}

	private static Toast sToast = null;
	
	public static void showToast(Context context, String text, int duration) {
		if(sToast == null) {
			sToast = Toast.makeText(context, text, duration);
		}
		
		sToast.setText(text);
		sToast.setDuration(duration);
		sToast.show();
	}
}
