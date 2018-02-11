/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.data.calendar;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.TimeZone;

import cn.tcl.transfer.systemApp.CalendarSysApp;

/**
 * Created by user on 16-9-13.
 */
public class CalendarEventHelper {

    private static final String TAG = "CalendarEventHelper";

    // event
    public static final String[] EVENT_PROJECTION = new String[] {
            Events._ID, // 0
            Events.TITLE, // 1
            Events.DESCRIPTION, // 2
            Events.EVENT_LOCATION, // 3
            Events.ALL_DAY, // 4
            Events.HAS_ALARM, // 5
            Events.CALENDAR_ID, // 6
            Events.DTSTART, // 7
            Events.DTEND, // 8
            Events.DURATION, // 9
            Events.EVENT_TIMEZONE, // 10
            Events.RRULE, // 11
            Events._SYNC_ID, // 12
            Events.AVAILABILITY, // 13
            Events.ACCESS_LEVEL, // 14
            Events.OWNER_ACCOUNT, // 15
            Events.HAS_ATTENDEE_DATA, // 16
            Events.ORIGINAL_SYNC_ID, // 17
            Events.ORGANIZER, // 18
            Events.GUESTS_CAN_MODIFY, // 19
            Events.ORIGINAL_ID, // 20
            Events.STATUS, // 21
    };
    protected static final int EVENT_INDEX_ID = 0;
    protected static final int EVENT_INDEX_TITLE = 1;
    protected static final int EVENT_INDEX_DESCRIPTION = 2;
    protected static final int EVENT_INDEX_EVENT_LOCATION = 3;
    protected static final int EVENT_INDEX_ALL_DAY = 4;
    protected static final int EVENT_INDEX_HAS_ALARM = 5;
    protected static final int EVENT_INDEX_CALENDAR_ID = 6;
    protected static final int EVENT_INDEX_DTSTART = 7;
    protected static final int EVENT_INDEX_DTEND = 8;
    protected static final int EVENT_INDEX_DURATION = 9;
    protected static final int EVENT_INDEX_TIMEZONE = 10;
    protected static final int EVENT_INDEX_RRULE = 11;
    protected static final int EVENT_INDEX_SYNC_ID = 12;
    protected static final int EVENT_INDEX_AVAILABILITY = 13;
    protected static final int EVENT_INDEX_ACCESS_LEVEL = 14;
    protected static final int EVENT_INDEX_OWNER_ACCOUNT = 15;
    protected static final int EVENT_INDEX_HAS_ATTENDEE_DATA = 16;
    protected static final int EVENT_INDEX_ORIGINAL_SYNC_ID = 17;
    protected static final int EVENT_INDEX_ORGANIZER = 18;
    protected static final int EVENT_INDEX_GUESTS_CAN_MODIFY = 19;
    protected static final int EVENT_INDEX_ORIGINAL_ID = 20;
    protected static final int EVENT_INDEX_EVENT_STATUS = 21;
    protected static final String EVENTS_WHERE = Events.CALENDAR_ID + "=?";

    // reminder
    public static final String[] REMINDERS_PROJECTION = new String[] {
            Reminders._ID, // 0
            Reminders.MINUTES, // 1
            Reminders.METHOD, // 2
    };
    public static final int REMINDERS_INDEX_MINUTES = 1;
    public static final int REMINDERS_INDEX_METHOD = 2;
    public static final String REMINDERS_WHERE = Reminders.EVENT_ID + "=?";

    // attendee
    static final String[] ATTENDEES_PROJECTION = new String[] {
            Attendees._ID, // 0
            Attendees.ATTENDEE_NAME, // 1
            Attendees.ATTENDEE_EMAIL, // 2
            Attendees.ATTENDEE_STATUS, // 3
            Attendees.ATTENDEE_RELATIONSHIP, // 4
            Attendees.ATTENDEE_TYPE, // 5
    };
    static final int ATTENDEES_INDEX_ID = 0;
    static final int ATTENDEES_INDEX_NAME = 1;
    static final int ATTENDEES_INDEX_EMAIL = 2;
    static final int ATTENDEES_INDEX_STATUS = 3;
    static final int ATTENDEES_INDEX_RELATIONSHIP = 4;
    static final int ATTENDEES_INDEX_TYPE = 5;
    static final String ATTENDEES_WHERE = Attendees.EVENT_ID + "=?";

    private Context mContext;

    public CalendarEventHelper(Context context) {
        mContext = context;
    }

    private void setModelFromCursor(CalendarEventModel model, Cursor cursor) {
        if (model == null || cursor == null /*|| cursor.getCount() != 1*/) {
            Log.wtf(TAG, "Attempted to build non-existent model or from an incorrect query.");
            return;
        }

        model.clear();
        //cursor.moveToFirst();

        model.mId = cursor.getInt(EVENT_INDEX_ID);
        model.mTitle = cursor.getString(EVENT_INDEX_TITLE);
        model.mDescription = cursor.getString(EVENT_INDEX_DESCRIPTION);
        model.mLocation = cursor.getString(EVENT_INDEX_EVENT_LOCATION);
        model.mAllDay = cursor.getInt(EVENT_INDEX_ALL_DAY) != 0;
        model.mHasAlarm = cursor.getInt(EVENT_INDEX_HAS_ALARM) != 0;
        model.mCalendarId = cursor.getInt(EVENT_INDEX_CALENDAR_ID);
        model.mStart = cursor.getLong(EVENT_INDEX_DTSTART);
        String tz = cursor.getString(EVENT_INDEX_TIMEZONE);
        if (!TextUtils.isEmpty(tz)) {
            model.mTimezone = tz;
        }
        String rRule = cursor.getString(EVENT_INDEX_RRULE);
        model.mRrule = rRule;
        model.mSyncId = cursor.getString(EVENT_INDEX_SYNC_ID);
        model.mAvailability = cursor.getInt(EVENT_INDEX_AVAILABILITY);
        int accessLevel = cursor.getInt(EVENT_INDEX_ACCESS_LEVEL);
        //model.mOwnerAccount = cursor.getString(EVENT_INDEX_OWNER_ACCOUNT);
        model.mHasAttendeeData = cursor.getInt(EVENT_INDEX_HAS_ATTENDEE_DATA) != 0;
        model.mOriginalSyncId = cursor.getString(EVENT_INDEX_ORIGINAL_SYNC_ID);
        model.mOriginalId = cursor.getString(EVENT_INDEX_ORIGINAL_ID);
        model.mOrganizer = cursor.getString(EVENT_INDEX_ORGANIZER);
        model.mGuestsCanModify = cursor.getInt(EVENT_INDEX_GUESTS_CAN_MODIFY) != 0;

        if (accessLevel > 0) {
            // For now the array contains the values 0, 2, and 3. We subtract
            // one to make it easier to handle in code as 0,1,2.
            // Default (0), Private (1), Public (2)
            accessLevel--;
        }
        model.mAccessLevel = accessLevel;
        model.mEventStatus = cursor.getInt(EVENT_INDEX_EVENT_STATUS);

        boolean hasRRule = !TextUtils.isEmpty(rRule);

        // We expect only one of these, so ignore the other
        if (hasRRule) {
            model.mDuration = cursor.getString(EVENT_INDEX_DURATION);
        } else {
            model.mEnd = cursor.getLong(EVENT_INDEX_DTEND);
        }

        Cursor reminderCursor = null;
        int minutes, method;
        try {
            reminderCursor = mContext.getContentResolver().query(Reminders.CONTENT_URI, REMINDERS_PROJECTION,
                    REMINDERS_WHERE, new String[]{String.valueOf(model.mId)}, null, null);
            if (reminderCursor != null && reminderCursor.getCount() > 0) {
                Log.i(TAG, "setModelFromCursor: reminderCursor.localCount = " + reminderCursor.getCount());
                if (reminderCursor.moveToFirst()) {
                    do {
                        minutes = reminderCursor.getInt(REMINDERS_INDEX_MINUTES);
                        method = reminderCursor.getInt(REMINDERS_INDEX_METHOD);
                        CalendarEventModel.Reminder reminder = new CalendarEventModel.Reminder(minutes, method);
                        model.mRemindersList.add(reminder);
                    } while (reminderCursor.moveToNext());
                }
            } else {
                Log.e(TAG, "setModelFromCursor: reminderCursor is null or localCount < 1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reminderCursor != null) {
                reminderCursor.close();
            }
        }

    }

    private boolean saveEvent(CalendarEventModel model, long calendarId) {

        if (model == null) {
            Log.e(TAG, "Attempted to save null model.");
            return false;
        }

        if (mContext.checkSelfPermission(android.Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "has no READ_CALENDAR permission");
            return false;
        }
        ContentValues values = getContentValuesFromModel(model, calendarId);
        Uri newUri = mContext.getContentResolver().insert(Events.CONTENT_URI, values);

        if (newUri != null) {
            Log.i(TAG, "saveEvent: newUri = " + newUri.getPathSegments().get(1));
            String eventId = newUri.getPathSegments().get(1);
            for (CalendarEventModel.Reminder reminder : model.mRemindersList) {
                ContentValues reminderValues = new ContentValues();

                reminderValues.put(Reminders.EVENT_ID, eventId);
                reminderValues.put(Reminders.MINUTES, reminder.mMinutes);
                reminderValues.put(Reminders.METHOD, reminder.mMethod);

                mContext.getContentResolver().insert(Reminders.CONTENT_URI, reminderValues);
            }
        }

        return true;
    }

    private ContentValues getContentValuesFromModel(CalendarEventModel model, long calendarId) {

        ContentValues values = new ContentValues();

        values.put(Events.TITLE, model.mTitle);
        if (model.mDescription != null) {
            values.put(Events.DESCRIPTION, model.mDescription.trim());
        } else {
            values.put(Events.DESCRIPTION, (String) null);
        }
        if (model.mLocation != null) {
            values.put(Events.EVENT_LOCATION, model.mLocation.trim());
        } else {
            values.put(Events.EVENT_LOCATION, (String) null);
        }
        values.put(Events.ALL_DAY, model.mAllDay ? 1 : 0);
        values.put(Events.HAS_ALARM, model.mHasAlarm ? 1 : 0);
        values.put(Events.CALENDAR_ID, calendarId);

        values.put(Events.DTSTART, model.mStart);
        if (!TextUtils.isEmpty(model.mRrule)) {
            values.put(Events.DTEND, (Long) null);
            values.put(Events.DURATION, model.mDuration);
        } else {
            values.put(Events.DTEND, model.mEnd);
            values.put(Events.DURATION, (String) null);
        }
        values.put(Events.EVENT_TIMEZONE, model.mTimezone);
        values.put(Events.RRULE, model.mRrule);

        values.put(Events.AVAILABILITY, model.mAvailability);
        values.put(Events.ACCESS_LEVEL, model.mAccessLevel);
        //values.put(Events.OWNER_ACCOUNT, model.mOwnerAccount);
        values.put(Events.HAS_ATTENDEE_DATA, model.mHasAttendeeData ? 1 : 0);
        if (model.mOriginalSyncId != null) {
            values.put(Events.ORIGINAL_SYNC_ID, model.mOriginalSyncId);
        }
        if (model.mOriginalSyncId != null) {
            values.put(Events.ORGANIZER, model.mOrganizer);
        }
        values.put(Events.GUESTS_CAN_MODIFY, model.mGuestsCanModify ? 1 : 0);
        if (model.mOriginalId != null) {
            values.put(Events.ORIGINAL_ID, model.mOriginalId);
        }
        values.put(Events.STATUS, model.mEventStatus);

        return values;
    }

    public boolean backupCalendarEvents(String dest) {

        if (TextUtils.isEmpty(dest)) {
            return false;
        }
        if (mContext.checkSelfPermission(android.Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "backupCalendarEvents: has no READ_CALENDAR permission");
            return false;
        }

        long calendarId = getLocalCalendarId();
        if (calendarId < 1) {
            Log.i(TAG, "backupCalendarEvents: calendarId < 1");
            return false;
        }

        Cursor cursor = null;
        String[] selectionArg = new String[]{String.valueOf(calendarId)};
        ArrayList<CalendarEventModel> eventList = new ArrayList<>();
        CalendarSysApp.localCount = 0;
        try {
            cursor = mContext.getContentResolver().query(Events.CONTENT_URI, EVENT_PROJECTION,
                    EVENTS_WHERE, selectionArg, null);
            if (cursor != null && cursor.getCount() > 0) {
                Log.i(TAG, "localCount = " + cursor.getCount());
                if (cursor.moveToFirst()) {
                    do {
                        Log.i(TAG, "event_tile: " + cursor.getString(EVENT_INDEX_TITLE));
                        CalendarEventModel model = new CalendarEventModel();
                        setModelFromCursor(model, cursor);
                        eventList.add(model);
                    } while (cursor.moveToNext());

                    ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(dest));
                    os.writeObject(eventList);
                    os.close();
                    CalendarSysApp.localCount = eventList.size();
                }
            } else {
                Log.e(TAG, "backupCalendarEvents: cursor is null or localCount < 1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return true;
    }

    public int getCalendarEventCount(String source) {
        if (TextUtils.isEmpty(source)) {
            return 0;
        }
        File file = new File(source);
        if (!file.exists() || file.length() == 0) {
            return 0;
        }

        long calendarId = getLocalCalendarId();
        if (calendarId < 1) {
            Log.i(TAG, "backupCalendarEvents: calendarId < 1");
            return 0;
        }
        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(source));
            ArrayList<CalendarEventModel> eventList = (ArrayList<CalendarEventModel>) is.readObject();
            return eventList.size();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean restoreCalendarEvents(String source) {
        CalendarSysApp.inertCount = 0;
        if (TextUtils.isEmpty(source)) {
            return false;
        }
        File file = new File(source);
        if (!file.exists() || file.length() == 0) {
            return false;
        }

        long calendarId = getLocalCalendarId();
        if (calendarId < 1) {
            Log.i(TAG, "backupCalendarEvents: calendarId < 1");
            return false;
        }

        try {
            ObjectInputStream is = new ObjectInputStream(new FileInputStream(source));
            ArrayList<CalendarEventModel> eventList = (ArrayList<CalendarEventModel>) is.readObject();
            Log.i(TAG, "restoreCalendarEvents: eventList.localCount = " + eventList.size());
            for(CalendarEventModel model: eventList) {
                saveEvent(model, calendarId);
                CalendarSysApp.inertCount++;
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return true;
    }
    private long getLocalCalendarId() {

        String selection = Calendars.ACCOUNT_TYPE + "=?";
        String[] selectionArgs = new String[]{CalendarContract.ACCOUNT_TYPE_LOCAL};
        Cursor cursor = null;

        if (mContext.checkSelfPermission(android.Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "has no READ_CALENDAR permission");
            return -1;
        }
        try {
            cursor = mContext.getContentResolver().query(Calendars.CONTENT_URI, null, selection,
                    selectionArgs, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                Log.i(TAG, "getLocalCalendarId: local calendar localCount = " + cursor.getCount());
                if (cursor.moveToFirst()) {
                    return cursor.getLong(cursor.getColumnIndex(Calendars._ID));
                } else {
                    return -1;
                }
            } else {
                return insertLocalCalendar();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return -1;
    }

    private long insertLocalCalendar() {
        ContentValues values = new ContentValues();
        values.put(Calendars.NAME, "Local");
        values.put(Calendars.ACCOUNT_NAME, "Local");
        values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        values.put(Calendars.OWNER_ACCOUNT, "Local");
        values.put(Calendars.CALENDAR_DISPLAY_NAME, "Local");
        values.put(Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().getDisplayName());
        values.put(Calendars.VISIBLE, 1);
        values.put(Calendars.CALENDAR_ACCESS_LEVEL, Calendars.CAL_ACCESS_OWNER);
        values.put(Calendars.CALENDAR_COLOR, Color.parseColor("#33b679"));
        values.put(Calendars.SYNC_EVENTS, 1);

        Uri newUri = mContext.getContentResolver().insert(
                addSyncQueryParams(Calendars.CONTENT_URI, "Local",
                        CalendarContract.ACCOUNT_TYPE_LOCAL), values);
        if (newUri != null) {
            return Long.parseLong(newUri.getPathSegments().get(1));
        } else {
            return -1;
        }
    }

    private Uri addSyncQueryParams(Uri uri, String account, String accountType) {
        return uri.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(Calendars.ACCOUNT_NAME, account)
                .appendQueryParameter(Calendars.ACCOUNT_TYPE, accountType).build();
    }
}
