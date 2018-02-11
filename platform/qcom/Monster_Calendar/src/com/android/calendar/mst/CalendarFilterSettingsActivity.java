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

import android.app.ActionBar;
import android.os.Bundle;

import mst.preference.PreferenceActivity;

import com.android.calendar.R;

public class CalendarFilterSettingsActivity extends PreferenceActivity {

	public static final String KEY_FESTIVAL_HOLIDAY = "festival_holiday";
	public static final String KEY_HOLIDAY_WORKDAY = "holiday_workday";
	public static final String KEY_LUNAR_DATE = "lunar_date";
	public static final String KEY_EVENT_TAG = "event_tag";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.mst_calendar_view_filter);
    }

}