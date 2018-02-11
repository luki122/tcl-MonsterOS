package com.android.calendar.mst;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

import com.android.calendar.CalendarController;
import com.android.calendar.Event;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.month.MonthByWeekFragment;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Reminders;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import mst.widget.SliderLayout;

public class MonthAgendaAdapter extends BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private MonthByWeekFragment mParentFragment;
	private ArrayList<Event> mBirthdayEvents;
	private ArrayList<Event> mEvents;

	private final StringBuilder mStringBuilder;
	private final Formatter mFormatter;

	public MonthAgendaAdapter(Context context, MonthByWeekFragment fragment,
			ArrayList<Event> birthdayEvents, ArrayList<Event> events) {
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mParentFragment = fragment;
		mBirthdayEvents = birthdayEvents;
		mEvents = events;

		mStringBuilder = new StringBuilder(50);
		mFormatter = new Formatter(mStringBuilder, Locale.getDefault());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.mst_month_agenda_adapter, null);
		}

		TextView titleView = (TextView) convertView.findViewById(R.id.month_agenda_title);
		TextView summaryView = (TextView) convertView.findViewById(R.id.month_agenda_time);
		ImageView reminderView = (ImageView) convertView.findViewById(R.id.month_agenda_reminder);
		TextView deleteView = (TextView) convertView.findViewById(R.id.action_delete);
		SliderLayout slidaerLayout = (SliderLayout) convertView;
		slidaerLayout.setLockDrag(false);

		if (mBirthdayEvents.isEmpty()) {
			Event event = mEvents.get(position);
			updateView(event, titleView, summaryView, reminderView, deleteView, slidaerLayout);
		} else if (position == 0) {
			slidaerLayout.setLockDrag(true);

			titleView.setText(R.string.birthday_reminder);

			String name = mBirthdayEvents.get(0).title.toString();
			if (mBirthdayEvents.size() == 1) {
				summaryView.setText(mContext.getString(R.string.birthday_only_one, name));
			} else {
				summaryView.setText(mContext.getString(R.string.birthday_have_other, name,
						mBirthdayEvents.size()));
			}

			reminderView.setImageResource(R.drawable.mst_birthday_icon);
			reminderView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					sendBirthdayWish();
				}
			});
		} else {
			Event event = mEvents.get(position - 1);
			updateView(event, titleView, summaryView, reminderView, deleteView, slidaerLayout);
		}

		return convertView;
	}

	@Override
	public int getCount() {
		return (mBirthdayEvents.isEmpty() ? 0 : 1) + mEvents.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	private void updateView(final Event event, TextView titleView, TextView summaryView,
			ImageView reminderView, TextView deleteView, final SliderLayout slidaerLayout) {

		titleView.setText(event.title);
		summaryView.setText(getWhenString(event));

        if (event.hasAlarm) {
            reminderView.setImageResource(R.drawable.mst_agenda_reminder_open);
        } else {
            reminderView.setImageResource(R.drawable.mst_agenda_reminder_close);
        }

        final long eventId = event.id;
        final boolean hasAlarm = event.hasAlarm;

        reminderView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentResolver cr = mContext.getContentResolver();
                if (hasAlarm) {
                    cr.delete(Reminders.CONTENT_URI, "event_Id=" + eventId, null);
                    Toast.makeText(mContext, R.string.event_reminder_close, Toast.LENGTH_SHORT).show();
                } else {
                    ContentValues values = new ContentValues();
                    values.put(Reminders.EVENT_ID, eventId);
                    values.put(Reminders.MINUTES, Utils.getDefaultReminderMinutes(mContext));
                    values.put(Reminders.METHOD, Reminders.METHOD_ALERT);
                    cr.insert(Reminders.CONTENT_URI, values);
                    Toast.makeText(mContext, R.string.event_reminder_open, Toast.LENGTH_SHORT).show();
                }
            }
        });

        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slidaerLayout.close(true);
                CalendarController controller = CalendarController.getInstance(mContext);
                controller.sendEventRelatedEventWithExtra(
                    mContext,
                    EventType.DELETE_EVENT,
                    eventId,
                    event.startMillis,
                    event.endMillis,
                    0, 0,
                    CalendarController.EventInfo.buildViewExtraLong(Attendees.ATTENDEE_STATUS_NONE, event.allDay),
                    controller.getTime());
            }
        });
	}

	private String getWhenString(Event event) {
		long begin = event.startMillis;
        long end = event.endMillis;
        boolean allDay = event.allDay;
        String eventTz = event.timeZone;
        int flags = 0;
        String whenString;
        String tzString = Utils.getTimeZone(mContext, null);
        if (allDay) {
            tzString = Time.TIMEZONE_UTC;
        } else if (end - begin < DateUtils.DAY_IN_MILLIS) {
            flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_ABBREV_MONTH;
        } else {
            flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_MONTH;
        }
        if (DateFormat.is24HourFormat(mContext)) {
            flags |= DateUtils.FORMAT_24HOUR;
        }
        mStringBuilder.setLength(0);
        whenString = DateUtils.formatDateRange(mContext, mFormatter, begin, end, flags, tzString).toString();
        if (!allDay && !TextUtils.equals(tzString, eventTz)) {
            String displayName;
            // Figure out if this is in DST
            Time date = new Time(tzString);
            date.set(begin);

            TimeZone tz = TimeZone.getTimeZone(tzString);
            if (tz == null || tz.getID().equals("GMT")) {
                displayName = tzString;
            } else {
                displayName = tz.getDisplayName(date.isDst != 0, TimeZone.SHORT);
            }
            whenString += " (" + displayName + ")";
        }

        return whenString;
	}

	private void sendBirthdayWish() {
		Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.happy_birthday));
        intent.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.happy_birthday));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.wish_birthday)));
	}

}
