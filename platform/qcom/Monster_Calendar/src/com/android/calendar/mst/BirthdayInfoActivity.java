/*
 * Copyright (C) 2010 The Android Open Source Project
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
 * limitations under the License.
 */
package com.android.calendar.mst;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import com.android.calendar.AsyncQueryService;
import com.android.calendar.Event;
import com.android.calendar.R;
import com.android.calendar.Utils;

public class BirthdayInfoActivity extends Activity {

    public static final String BIRTHDAY_START_MILLIS = "birthday_start_millis";

    private static final String WHERE = Calendars.OWNER_ACCOUNT + "=?";
    private static final String[] WHERE_ARGS = { Utils.BIRTHDAY_REMINDER_ACCOUNT_NAME };
    private static final String SORT_EVENTS_BY = "begin ASC, end DESC, title ASC";

    private static final String DISPLAY_AS_ALLDAY = "dispAllday";
    // The projection to use when querying instances to build a list of events
    public static final String[] EVENT_PROJECTION = new String[] {
            Instances.TITLE,                 // 0
            Instances.EVENT_LOCATION,        // 1
            Instances.ALL_DAY,               // 2
            Instances.DISPLAY_COLOR,         // 3 If SDK < 16, set to Instances.CALENDAR_COLOR.
            Instances.EVENT_TIMEZONE,        // 4
            Instances.EVENT_ID,              // 5
            Instances.BEGIN,                 // 6
            Instances.END,                   // 7
            Instances._ID,                   // 8
            Instances.START_DAY,             // 9
            Instances.END_DAY,               // 10
            Instances.START_MINUTE,          // 11
            Instances.END_MINUTE,            // 12
            Instances.HAS_ALARM,             // 13
            Instances.RRULE,                 // 14
            Instances.RDATE,                 // 15
            Instances.SELF_ATTENDEE_STATUS,  // 16
            Events.ORGANIZER,                // 17
            Events.GUESTS_CAN_MODIFY,        // 18
            Instances.ALL_DAY + "=1 OR (" + Instances.END + "-" + Instances.BEGIN + ")>="
            + DateUtils.DAY_IN_MILLIS + " AS " + DISPLAY_AS_ALLDAY, // 19
            Instances.OWNER_ACCOUNT,         // 20
            Instances.DTSTART,               // 21
    };

    // The indices for the projection array above.
    public static final int PROJECTION_TITLE_INDEX = 0;
    public static final int PROJECTION_LOCATION_INDEX = 1;
    public static final int PROJECTION_ALL_DAY_INDEX = 2;
    public static final int PROJECTION_COLOR_INDEX = 3;
    public static final int PROJECTION_TIMEZONE_INDEX = 4;
    public static final int PROJECTION_EVENT_ID_INDEX = 5;
    public static final int PROJECTION_BEGIN_INDEX = 6;
    public static final int PROJECTION_END_INDEX = 7;
    public static final int PROJECTION_START_DAY_INDEX = 9;
    public static final int PROJECTION_END_DAY_INDEX = 10;
    public static final int PROJECTION_START_MINUTE_INDEX = 11;
    public static final int PROJECTION_END_MINUTE_INDEX = 12;
    public static final int PROJECTION_HAS_ALARM_INDEX = 13;
    public static final int PROJECTION_RRULE_INDEX = 14;
    public static final int PROJECTION_RDATE_INDEX = 15;
    public static final int PROJECTION_SELF_ATTENDEE_STATUS_INDEX = 16;
    public static final int PROJECTION_ORGANIZER_INDEX = 17;
    public static final int PROJECTION_GUESTS_CAN_INVITE_OTHERS_INDEX = 18;
    public static final int PROJECTION_DISPLAY_AS_ALLDAY = 19;
    public static final int PROJECTION_OWNER_ACCOUNT_INDEX = 20;
    public static final int PROJECTION_DTSTART_INDEX = 21;

    static {
        if (!Utils.isJellybeanOrLater()) {
            EVENT_PROJECTION[PROJECTION_COLOR_INDEX] = Instances.CALENDAR_COLOR;
        }
    }

    private long mStartMillis;
    private int mStartDay;

    private Context mContext;
    private BirthdayInfoAdapter mAdapter;
    private QueryHandler mQueryHandler;
    private Cursor mCursor;
    private ListView mListView;

    private ArrayList<Event> mEvents = new ArrayList<Event>();
    private String[] mConstellationArray;

    private TextView mBirthdayNotice;
    private TextView mBirthdayDate;
    private TextView mBirthdayConstellation;

    private class QueryHandler extends AsyncQueryService {
        public QueryHandler(Context context) {
            super(context);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            // Only set mCursor if the AuroraActivity is not finishing. Otherwise close the cursor.
            if (!isFinishing()) {
                mCursor = cursor;
                mAdapter.changeCursor(cursor);

                mBirthdayNotice.setText(
                        mContext.getResources().getString(R.string.birthday_notice, cursor.getCount()));

                mEvents.clear();
                Event.buildEventsFromCursor(mEvents, mCursor, mContext, mStartDay, mStartDay);
            } else {
                cursor.close();
            }
        }

        @Override
        protected void onUpdateComplete(int token, Object cookie, int result) {
            // Ignore
        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        int flag = getWindow().getDecorView().getSystemUiVisibility();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | flag);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        super.onCreate(icicle);

        mContext = this;

        mStartMillis = getIntent().getLongExtra(BIRTHDAY_START_MILLIS, System.currentTimeMillis());

        Time time = new Time();
        time.set(mStartMillis);
        mStartDay = Time.getJulianDay(mStartMillis, time.gmtoff);

        setContentView(R.layout.mst_birthday_info_layout);      
        initResources();
        initViews();
        updateViews();

        mQueryHandler = new QueryHandler(this);
        mAdapter = new BirthdayInfoAdapter(this, R.layout.mst_birthday_info_item, mStartMillis);

        mListView = (ListView) findViewById(R.id.birthday_info_list);
        mListView.setAdapter(mAdapter);

        View listHeader = LayoutInflater.from(this).inflate(R.layout.mst_birthday_info_header, null);
        mListView.addHeaderView(listHeader);
        mListView.setSelector(R.drawable.mst_birthday_info_selector);
        mListView.setDivider(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // From the Android Dev Guide: "It's important to note that when
        // onNewIntent(Intent) is called, the Activity has not been restarted,
        // so the getIntent() method will still return the Intent that was first
        // received with onCreate(). This is why setIntent(Intent) is called
        // inside onNewIntent(Intent) (just in case you call getIntent() at a
        // later time)."
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If the cursor is null, start the async handler. If it is not null just requery.
        if (mCursor == null) {
            Uri.Builder builder = Instances.CONTENT_BY_DAY_URI.buildUpon();
            ContentUris.appendId(builder, mStartDay);
            ContentUris.appendId(builder, mStartDay);
            mQueryHandler.startQuery(0, null, builder.build(), Event.EVENT_PROJECTION, WHERE, WHERE_ARGS, SORT_EVENTS_BY);
        } else {
            if (!mCursor.requery()) {
                mCursor.close();
                mCursor = null;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mCursor != null) {
            mCursor.deactivate();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCursor != null) {
            mCursor.close();
        }
    }

    private void initResources() {
        mConstellationArray = getResources().getStringArray(R.array.constellation_labels);
    }

    private void initViews() {
        int i = (int) (Math.random() * 2);
        if (i >= 1) {
        	findViewById(R.id.birthday_info_banner).setBackgroundResource(R.drawable.mst_event_banner_2);
        }

        findViewById(R.id.back_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mBirthdayNotice = (TextView) findViewById(R.id.birthday_notice);
        mBirthdayDate = (TextView) findViewById(R.id.birthday_date);
        mBirthdayConstellation = (TextView) findViewById(R.id.birthday_constellation);
    }

    private void updateViews() {
        Time time = new Time();
        time.setJulianDay(mStartDay);
        time.normalize(true);

        mBirthdayDate.setText("" + (time.month + 1) + "." + time.monthDay);
        mBirthdayConstellation.setText(caculateConstellation(time.month + 1, time.monthDay));
    }

    private String caculateConstellation(int month, int monthDay) {
        if (month == 1) {
            if (monthDay <= 20) {
                return mConstellationArray[0];
            } else {
                return mConstellationArray[1];
            }
        }

        if (month == 2) {
            if (monthDay <= 19) {
                return mConstellationArray[1];
            } else {
                return mConstellationArray[2];
            }
        }

        if (month == 3) {
            if (monthDay <= 20) {
                return mConstellationArray[2];
            } else {
                return mConstellationArray[3];
            }
        }

        if (month == 4) {
            if (monthDay <= 20) {
                return mConstellationArray[3];
            } else {
                return mConstellationArray[4];
            }
        }

        if (month == 5) {
            if (monthDay <= 21) {
                return mConstellationArray[4];
            } else {
                return mConstellationArray[5];
            }
        }

        if (month == 6) {
            if (monthDay <= 21) {
                return mConstellationArray[5];
            } else {
                return mConstellationArray[6];
            }
        }

        if (month == 7) {
            if (monthDay <= 22) {
                return mConstellationArray[6];
            } else {
                return mConstellationArray[7];
            }
        }

        if (month == 8) {
            if (monthDay <= 23) {
                return mConstellationArray[7];
            } else {
                return mConstellationArray[8];
            }
        }

        if (month == 9) {
            if (monthDay <= 23) {
                return mConstellationArray[8];
            } else {
                return mConstellationArray[9];
            }
        }

        if (month == 10) {
            if (monthDay <= 23) {
                return mConstellationArray[9];
            } else {
                return mConstellationArray[10];
            }
        }

        if (month == 11) {
            if (monthDay <= 22) {
                return mConstellationArray[10];
            } else {
                return mConstellationArray[11];
            }
        }

        if (month == 12) {
            if (monthDay <= 21) {
                return mConstellationArray[11];
            } else {
                return mConstellationArray[0];
            }
        }

        return mConstellationArray[9];
    }

}
