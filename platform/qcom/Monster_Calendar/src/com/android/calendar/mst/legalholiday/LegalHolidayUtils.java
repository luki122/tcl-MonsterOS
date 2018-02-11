package com.android.calendar.mst.legalholiday;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.android.calendar.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.text.format.Time;

public class LegalHolidayUtils implements LegalHoliday {

	public static final String TAG = "LegalHoliday";

	private static final String INIT_NETWORK_DATA_JULIANDAY = "init_network_data_julianday";
	private static final String KEY_LEGAL_HOLIDAY = "preference_legal_holiday";
	private static final String KEY_LEGAL_WORKDAY = "preference_legal_workday";

	private static final String SEPARATOR = ",";

	private static LegalHolidayUtils sInstance = null;
	private Map<Integer, Integer> mLegalHolidayMap = null;

	private LegalHolidayCallback callback;

	public interface LegalHolidayCallback {
		void legalHolidayUpdated();
	}

	public void setLegalHolidayCallback(LegalHolidayCallback callback) {
		this.callback = callback;
	}

	private LegalHolidayUtils() {
		mLegalHolidayMap = new LinkedHashMap<Integer, Integer>();
	}

	public static LegalHolidayUtils getInstance() {
		if (sInstance == null) {
			sInstance = new LegalHolidayUtils();
		}

		return sInstance;
	}

	public static void initHolidayData(Context context) {
		int lastJulianDay = Utils.getSharedPreference(context, INIT_NETWORK_DATA_JULIANDAY, 0);

		Time now = new Time();
		now.setToNow();
		int todayJulianDay = Time.getJulianDay(now.toMillis(true), now.gmtoff);

		if (lastJulianDay == todayJulianDay) {
			initLocalData(context);
			return;
		}

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isAvailable()) {
			LegalHolidayTask task = new LegalHolidayTask(context);
			task.execute((String) null);
		} else {
			initLocalData(context);
		}
	}

	public static void initNetworkData(Context context, LegalHolidayResponseBody legalHoliday) {
		LegalHolidayUtils legalHolidayUtils = LegalHolidayUtils.getInstance();
		legalHolidayUtils.setLagelHoliday(context, legalHoliday);

		Time now = new Time();
		now.setToNow();
		int todayJulianDay = Time.getJulianDay(now.toMillis(true), now.gmtoff);
		Utils.setSharedPreference(context, INIT_NETWORK_DATA_JULIANDAY, todayJulianDay);
	} 

	public static void initLocalData(Context context) {
		LegalHolidayUtils legalHolidayUtils = LegalHolidayUtils.getInstance();
		LegalHolidayResponseBody legalHoliday = legalHolidayUtils.getLegalHoliday(context);

		List<Integer> holidayList = legalHoliday.getHolidayList();
		List<Integer> workdayList = legalHoliday.getWorkdayList();
		if (!holidayList.isEmpty() || !workdayList.isEmpty()) {
			legalHolidayUtils.setLagelHoliday(holidayList, workdayList);
		} else {
			legalHolidayUtils.initLocalData();
		}
	}

	private void setLagelHoliday(List<Integer> holidayList, List<Integer> workdayList) {
		for (int day : holidayList) {
			mLegalHolidayMap.put(day, DAY_TYPE_HOLIDAY);
		}
		for (int day : workdayList) {
			mLegalHolidayMap.put(day, DAY_TYPE_WORKDAY);
		}
	}

	private void setLagelHoliday(Context context, LegalHolidayResponseBody legalHoliday) {
		mLegalHolidayMap.clear();

		List<Integer> holidayList = legalHoliday.getHolidayList();
		List<Integer> workdayList = legalHoliday.getWorkdayList();

		if (holidayList != null && !holidayList.isEmpty()) {
			String holidayStr = "";
			for (int day : holidayList) {
				holidayStr += String.valueOf(day);
				holidayStr += SEPARATOR;

				mLegalHolidayMap.put(day, DAY_TYPE_HOLIDAY);
			}
			if (!TextUtils.isEmpty(holidayStr)) {
				Utils.setSharedPreference(context, KEY_LEGAL_HOLIDAY, holidayStr);
			}
		}

		if (workdayList != null && !workdayList.isEmpty()) {
			String workdayStr = "";
			for (int day : workdayList) {
				workdayStr += String.valueOf(day);
				workdayStr += SEPARATOR;

				mLegalHolidayMap.put(day, DAY_TYPE_WORKDAY);
			}
			if (!TextUtils.isEmpty(workdayStr)) {
				Utils.setSharedPreference(context, KEY_LEGAL_WORKDAY, workdayStr);
			}
		}
	}

	private LegalHolidayResponseBody getLegalHoliday(Context context) {
		mLegalHolidayMap.clear();

		LegalHolidayResponseBody legalHoliday = new LegalHolidayResponseBody();

		String holidayStr = Utils.getSharedPreference(context, KEY_LEGAL_HOLIDAY, "");
		String workdayStr = Utils.getSharedPreference(context, KEY_LEGAL_WORKDAY, "");

		if (!TextUtils.isEmpty(holidayStr)) {
			ArrayList<Integer> holidayList = new ArrayList<Integer>();
			String[] holidays = holidayStr.split(SEPARATOR);
			for (int i = 0; i < holidays.length; i++) {
				holidayList.add(Integer.parseInt(holidays[i]));
			}
			legalHoliday.setHolidayList(holidayList);
		}

		if (!TextUtils.isEmpty(workdayStr)) {
			ArrayList<Integer> workdayList = new ArrayList<Integer>();
			String[] workdays = workdayStr.split(SEPARATOR);
			for (int i = 0; i < workdays.length; i++) {
				workdayList.add(Integer.parseInt(workdays[i]));
			}
			legalHoliday.setWorkdayList(workdayList);
		}

		return legalHoliday;
	}

	private void initLocalData() {
		Time t = new Time();
		t.year = 2015;
		t.month = 0;
		t.monthDay = 1;
		t.normalize(true);
		int firstJulianDay2015 = Time.getJulianDay(t.toMillis(true), t.gmtoff);

		int yearDays2015[] = {0, 1, 2, 3, 45, 48, 49, 50, 51, 52, 53, 54, 58, 93, 94, 95, 120, 121, 122, 170, 171, 172,
				245, 246, 247, 248, 268, 269, 273, 274, 275, 276, 277, 278, 279, 282};
		int dayType2015[] = {1, 1, 1, 2, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1,
				1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2};

		for (int i = 0; i < yearDays2015.length; i++) {
			if (!mLegalHolidayMap.containsKey(firstJulianDay2015 + yearDays2015[i])) {
				mLegalHolidayMap.put(firstJulianDay2015 + yearDays2015[i], dayType2015[i]);
			}
		}

		t.year = 2016;
		t.month = 0;
		t.monthDay = 1;
		t.normalize(true);
		int firstJulianDay2016 = Time.getJulianDay(t.toMillis(true), t.gmtoff);

		int yearDays2016[] = {0, 1, 2, 36, 37, 38, 39, 40, 41, 42, 43, 44, 92, 93, 94, 120, 121, 122, 160, 161, 162, 163,
				258, 259, 260, 261, 274, 275, 276, 277, 278, 279, 280, 281, 282};
		int dayType2016[] = {1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2,
				1, 1, 1, 2, 1, 1, 1, 1, 1, 1, 1, 2, 2};

		for (int i = 0; i < yearDays2016.length; i++) {
			if (!mLegalHolidayMap.containsKey(firstJulianDay2016 + yearDays2016[i])) {
				mLegalHolidayMap.put(firstJulianDay2016 + yearDays2016[i], dayType2016[i]);
			}
		}
	}

	@Override
	public int getDayType(int julianDay) {
		if (mLegalHolidayMap.containsKey(julianDay)) {
			return mLegalHolidayMap.get(julianDay);
		}

		return DAY_TYPE_NORMAL;
	}
}
