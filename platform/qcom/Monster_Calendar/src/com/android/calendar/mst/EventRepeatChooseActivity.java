package com.android.calendar.mst;

import java.util.ArrayList;

import com.android.calendar.R;
import com.android.calendar.Utils;
import com.android.calendar.event.EditEventHelper;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.View;
import android.widget.ArrayAdapter;

import mst.app.MstActivity;
import mst.widget.MstListView;

import com.android.calendarcommon2.EventRecurrence;

public class EventRepeatChooseActivity extends MstActivity {

	public static final int EVENT_REPEAT_CHOOSE_CODE = 1 << 15;

	public static final String EVENT_START_MILLIS = "event_start_millis";
	public static final String EVENT_TIME_ZONE = "event_time_zone";
	public static final String EVENT_RRULE = "event_rrule";

	private long mStartMillis;
	private String mTimeZone;
	private String mRrule;

	private ArrayList<Integer> mRepeatIndexs = new ArrayList<Integer>();;
	private ArrayList<String> mRepeatLabels = new ArrayList<String>();

	private int originalPosition;

	private MstListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		mStartMillis = intent.getLongExtra(EVENT_START_MILLIS,
				System.currentTimeMillis());
		mTimeZone = intent.getStringExtra(EVENT_TIME_ZONE);
		mRrule = intent.getStringExtra(EVENT_RRULE);

		setMstContentView(R.layout.mst_event_repeat_layout);

		getToolbar().setTitle(R.string.event_repeat_label);

		initData();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.mst_repeat_label_item, mRepeatLabels);

		mListView = (MstListView) findViewById(R.id.repeat_labels_list);
		mListView.setAdapter(adapter);
		mListView.setChoiceMode(MstListView.CHOICE_MODE_SINGLE);
		mListView.setItemChecked(originalPosition, true);
	}

	@Override
	public void onNavigationClicked(View view) {
		onBackPressed();
	}

	@Override
	public void finish() {
		int position = mListView.getCheckedItemPosition();
		if (position != -1 && position != originalPosition) {
			int repeatIndex = mRepeatIndexs.get(position);
			mRrule = getRecurrenceRule(repeatIndex, mStartMillis, mTimeZone,
					Utils.getFirstDayOfWeekAsCalendar(this));

			Intent intent = new Intent();
			intent.putExtra(EVENT_RRULE, mRrule);

			setResult(RESULT_OK, intent);
		} else {
			setResult(RESULT_CANCELED);
		}

		super.finish();
	}

	private void initData() {
		Time time = new Time(mTimeZone);
		time.set(mStartMillis);

		Resources res = getResources();

		mRepeatIndexs.add(EditEventHelper.DOES_NOT_REPEAT);
		mRepeatLabels.add(res.getString(R.string.event_no_repeat));

		mRepeatIndexs.add(EditEventHelper.REPEATS_DAILY);
		mRepeatLabels.add(res.getString(R.string.event_daily));

		if (time.weekDay != Time.SUNDAY && time.weekDay != Time.SATURDAY) {
			mRepeatIndexs.add(EditEventHelper.REPEATS_EVERY_WEEKDAY);
			mRepeatLabels.add(res.getString(R.string.event_every_weekday));
		}

		mRepeatIndexs.add(EditEventHelper.REPEATS_WEEKLY_ON_DAY);
		mRepeatLabels.add(res.getString(R.string.event_weekly, time.format("%A")));

		String[] ordinalLabels = res.getStringArray(R.array.ordinal_labels);
		int weekOrdinal = (time.monthDay - 1) / 7;

		mRepeatIndexs.add(EditEventHelper.REPEATS_MONTHLY_ON_DAY_COUNT);
		mRepeatLabels.add(res.getString(R.string.event_monthly_on_day_count,
				ordinalLabels[weekOrdinal], time.format("%A")));

		mRepeatIndexs.add(EditEventHelper.REPEATS_MONTHLY_ON_DAY);
		mRepeatLabels.add(res.getString(R.string.monthly_on_day, time.monthDay));

		mRepeatIndexs.add(EditEventHelper.REPEATS_YEARLY);
		mRepeatLabels.add(res.getString(R.string.yearly_plain));

		int repeatIndex = EditEventHelper.DOES_NOT_REPEAT;
		if (!TextUtils.isEmpty(mRrule)) {
			EventRecurrence eventRecurrence = new EventRecurrence();
			eventRecurrence.parse(mRrule);

			repeatIndex = getRepeatIndex(eventRecurrence);
		}

		if (repeatIndex == EditEventHelper.REPEATS_CUSTOM) {
			mRepeatIndexs.add(EditEventHelper.REPEATS_CUSTOM);
			mRepeatLabels.add(res.getString(R.string.custom));
		}

		originalPosition = mRepeatIndexs.indexOf(repeatIndex);
	}

	private int getRepeatIndex(EventRecurrence recurrence) {
		switch (recurrence.freq) {
		case EventRecurrence.DAILY:
			return EditEventHelper.REPEATS_DAILY;
		case EventRecurrence.WEEKLY: {
			if (recurrence.repeatsOnEveryWeekDay()) {
				return EditEventHelper.REPEATS_EVERY_WEEKDAY;
			}
			return EditEventHelper.REPEATS_WEEKLY_ON_DAY;
		}
		case EventRecurrence.MONTHLY: {
			if (recurrence.repeatsMonthlyOnDayCount()) {
				return EditEventHelper.REPEATS_MONTHLY_ON_DAY_COUNT;
			}
			return EditEventHelper.REPEATS_MONTHLY_ON_DAY;
		}
		case EventRecurrence.YEARLY:
			return EditEventHelper.REPEATS_YEARLY;
		}

		return EditEventHelper.REPEATS_CUSTOM;
	}

	private static String getRecurrenceRule(int selection, long startMillis,
			String timezone, int weekStart) {
		EventRecurrence eventRecurrence = new EventRecurrence();

		if (selection == EditEventHelper.DOES_NOT_REPEAT) {
			return null;
		} else if (selection == EditEventHelper.REPEATS_CUSTOM) {
			return null;
		} else if (selection == EditEventHelper.REPEATS_DAILY) {
			eventRecurrence.freq = EventRecurrence.DAILY;
		} else if (selection == EditEventHelper.REPEATS_EVERY_WEEKDAY) {
			eventRecurrence.freq = EventRecurrence.WEEKLY;
			int dayCount = 5;
			int[] byday = new int[dayCount];
			int[] bydayNum = new int[dayCount];

			byday[0] = EventRecurrence.MO;
			byday[1] = EventRecurrence.TU;
			byday[2] = EventRecurrence.WE;
			byday[3] = EventRecurrence.TH;
			byday[4] = EventRecurrence.FR;
			for (int day = 0; day < dayCount; day++) {
				bydayNum[day] = 0;
			}

			eventRecurrence.byday = byday;
			eventRecurrence.bydayNum = bydayNum;
			eventRecurrence.bydayCount = dayCount;
		} else if (selection == EditEventHelper.REPEATS_WEEKLY_ON_DAY) {
			eventRecurrence.freq = EventRecurrence.WEEKLY;
			int[] days = new int[1];
			int dayCount = 1;
			int[] dayNum = new int[dayCount];
			Time startTime = new Time(timezone);
			startTime.set(startMillis);

			days[0] = EventRecurrence.timeDay2Day(startTime.weekDay);
			// not sure why this needs to be zero, but set it for now.
			dayNum[0] = 0;

			eventRecurrence.byday = days;
			eventRecurrence.bydayNum = dayNum;
			eventRecurrence.bydayCount = dayCount;
		} else if (selection == EditEventHelper.REPEATS_MONTHLY_ON_DAY) {
			eventRecurrence.freq = EventRecurrence.MONTHLY;
			eventRecurrence.bydayCount = 0;
			/*eventRecurrence.bymonthdayCount = 1;
			int[] bymonthday = new int[1];
			Time startTime = new Time(timezone);
			startTime.set(startMillis);
			bymonthday[0] = startTime.monthDay;
			eventRecurrence.bymonthday = bymonthday;*/
		} else if (selection == EditEventHelper.REPEATS_MONTHLY_ON_DAY_COUNT) {
			eventRecurrence.freq = EventRecurrence.MONTHLY;
			eventRecurrence.bydayCount = 1;
			eventRecurrence.bymonthdayCount = 0;

			int[] byday = new int[1];
			int[] bydayNum = new int[1];
			Time startTime = new Time(timezone);
			startTime.set(startMillis);
			// Compute the week number (for example, the "2nd" Monday)
			int dayCount = 1 + ((startTime.monthDay - 1) / 7);
			if (dayCount == 5) {
				dayCount = -1;
			}
			bydayNum[0] = dayCount;
			byday[0] = EventRecurrence.timeDay2Day(startTime.weekDay);
			eventRecurrence.byday = byday;
			eventRecurrence.bydayNum = bydayNum;
		} else if (selection == EditEventHelper.REPEATS_YEARLY) {
			eventRecurrence.freq = EventRecurrence.YEARLY;
		}

		// Set the week start day.
		eventRecurrence.wkst = EventRecurrence.calendarDay2Day(weekStart);
		return eventRecurrence.toString();
	}
}
