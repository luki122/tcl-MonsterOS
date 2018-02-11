package com.android.calendar.mst;

import com.android.calendar.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.text.format.Time;

public class BirthdayReceicer extends BroadcastReceiver {

    private static final String ACTION_UPDATE_BIRTHDAY_REMINDER = "com.mst.calendar.UPDATE_BIRTHDAY_REMINDER";	

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            /*ContentResolver cr = context.getContentResolver();
            checkLocalAccount(cr);
            checkBirthdayReminderAccount(cr);*/
            scheduleAlarms(context);
        }

        checkBirthdayReminder(context);
    }

    public static void checkLocalAccount(ContentResolver resolver) {
        Cursor cursor = resolver.query(
                Calendars.CONTENT_URI,
                new String[] { Calendars._ID },
                Calendars.ACCOUNT_NAME + "='" + Utils.LOCAL_ACCOUNT_NAME + "'",
                null,
                null);
        if (cursor != null && cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(Calendars.ACCOUNT_NAME, Utils.LOCAL_ACCOUNT_NAME);
            values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
            values.put(Calendars.NAME, Utils.LOCAL_ACCOUNT_NAME);
            values.put(Calendars.CALENDAR_DISPLAY_NAME, Utils.LOCAL_ACCOUNT_NAME);
            values.put(Calendars.CALENDAR_COLOR, -9215145);
            values.put(Calendars.CALENDAR_ACCESS_LEVEL, 700);
            values.put(Calendars.SYNC_EVENTS, 1);
            values.put(Calendars.OWNER_ACCOUNT, Utils.LOCAL_ACCOUNT_NAME);
            resolver.insert(Calendars.CONTENT_URI, values);
        }
        if (cursor != null) cursor.close();
    }

    public static void checkBirthdayReminderAccount(ContentResolver resolver) {
        Cursor cursor = resolver.query(
                Calendars.CONTENT_URI,
                new String[] { Calendars._ID },
                Calendars.ACCOUNT_NAME + "='" + Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME + "'",
                null,
                null);
        if (cursor != null && cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(Calendars.ACCOUNT_NAME, Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME);
            values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
            values.put(Calendars.NAME, Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME);
            values.put(Calendars.CALENDAR_DISPLAY_NAME, Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME);
            values.put(Calendars.CALENDAR_COLOR, -9215145);
            values.put(Calendars.CALENDAR_ACCESS_LEVEL, 700);
            values.put(Calendars.SYNC_EVENTS, 1);
            values.put(Calendars.OWNER_ACCOUNT, Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME);
            resolver.insert(Calendars.CONTENT_URI, values);
        }
        if (cursor != null) cursor.close();
    }

    public static void checkBirthdayReminder(Context context) {
        boolean doReminderBirthday = Utils.getSharedPreference(context, CalendarSettingsActivity.KEY_BIRTHDAY_REMINDER, true);
        if (!doReminderBirthday) return;

        context.startService(new Intent(context, BirthdayService.class));
    }

    public static void checkBirthdayReminder(Context context, ContentResolver resolver) {
        Cursor cursor = resolver.query(
                Events.CONTENT_URI,
                new String[] { Events._ID },
                Events.OWNER_ACCOUNT + "='" + Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME + "'",
                null,
                null);
        if (cursor != null && cursor.getCount() == 0) {
            scheduleAlarms(context);
            checkBirthdayReminder(context);
        }
        if (cursor != null) cursor.close();
    }

    public static void scheduleAlarms(Context context) {
       Time now = new Time();
       now.setToNow();
       if (now.hour >= 8) {
           now.monthDay += 1;
       }
       now.hour = 8;
       now.minute = 0;
       now.second = 0;
       long timeMillis = now.normalize(true);

       Intent intent = new Intent(ACTION_UPDATE_BIRTHDAY_REMINDER);
       PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, timeMillis, AlarmManager.INTERVAL_DAY, pi);
    }
}
