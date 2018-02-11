/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.calendar.mst;

import java.util.TimeZone;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.Data;
import android.text.format.Time;

import com.android.calendar.Utils;
import com.android.calendarcommon2.EventRecurrence;

public class BirthdayService extends IntentService {

    public BirthdayService() {
        super("BirthdayService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onHandleIntent(Intent intent) {
        ContentResolver cr = getContentResolver();

        Cursor cursor = cr.query(
                Calendars.CONTENT_URI,
                new String[] { Calendars._ID },
                Calendars.ACCOUNT_NAME + "='" + Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME + "'",
                null,
                null);
        if (cursor == null) return;

        long calendarId = 0;
        if (cursor.moveToFirst()) {
            calendarId = cursor.getLong(0);
        } else {
            ContentValues values = new ContentValues();
            values.put(Calendars.ACCOUNT_NAME, Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME);
            values.put(Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
            values.put(Calendars.NAME, Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME);
            values.put(Calendars.CALENDAR_DISPLAY_NAME, Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME);
            values.put(Calendars.CALENDAR_COLOR, -9215145);
            values.put(Calendars.CALENDAR_ACCESS_LEVEL, 700);
            values.put(Calendars.SYNC_EVENTS, 1);
            values.put(Calendars.OWNER_ACCOUNT, Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME);
            Uri uri = cr.insert(Calendars.CONTENT_URI, values);

            calendarId = ContentUris.parseId(uri);
        }
        cursor.close();

        cr.delete(Events.CONTENT_URI, Events.CALENDAR_ID + "=" + calendarId, null);

        Cursor birthdayCursor = cr.query(
                Data.CONTENT_URI,
                new String[] {Data.DISPLAY_NAME, Data.DATA1, Data._ID},
                Data.MIMETYPE + "='" + CommonDataKinds.Event.CONTENT_ITEM_TYPE + "' AND "
                        + Data.DATA2 + "=" + CommonDataKinds.Event.TYPE_BIRTHDAY,
                null,
                null);
        if (birthdayCursor != null) {
            ContentValues values = new ContentValues();

            String eventTimezone = TimeZone.getDefault().getID();

            EventRecurrence eventRecurrence = new EventRecurrence();
            eventRecurrence.freq = EventRecurrence.YEARLY;
            eventRecurrence.wkst = EventRecurrence.calendarDay2Day(Utils.getFirstDayOfWeek(this) + 1);
            String rrule = eventRecurrence.toString();

            birthdayCursor.moveToPosition(-1);
            while (birthdayCursor.moveToNext()) {
                String title = birthdayCursor.getString(0);
                String birthdayDate = birthdayCursor.getString(1);

                if (title == null || birthdayDate == null) continue;

                if (birthdayDate.indexOf("T") != -1) {
                    birthdayDate = birthdayDate.substring(0, birthdayDate.indexOf("T"));
                }
                birthdayDate = birthdayDate.replace("-", "");
                birthdayDate = birthdayDate.trim();

                if (birthdayDate.length() != 8 || !(birthdayDate.startsWith("20") || birthdayDate.startsWith("19"))) continue;

                Time time = new Time();
                time.parse(birthdayDate);
                long startMillis = time.toMillis(true);

                values.clear();
                values.put(Events.CALENDAR_ID, calendarId);
                values.put(Events.EVENT_TIMEZONE, eventTimezone);
                values.put(Events.TITLE, title);
                values.put(Events.ALL_DAY, 0);
                values.put(Events.DTSTART, startMillis + 1000 * 3600 * 9);
                values.put(Events.RRULE, rrule);
                values.put(Events.DURATION, "P43200S");
                values.put(Events.DTEND, (Long) null);
                values.put(Events.HAS_ATTENDEE_DATA, 0);
                values.put(Events.STATUS, Events.STATUS_CONFIRMED);

                Uri uri = cr.insert(Events.CONTENT_URI, values);

                values.clear();
                values.put(Reminders.MINUTES, 0);
                values.put(Reminders.METHOD, Reminders.METHOD_ALERT);
                values.put(Reminders.EVENT_ID, ContentUris.parseId(uri));
                cr.insert(Reminders.CONTENT_URI, values);
            }
            birthdayCursor.close();
        }
    }
}
