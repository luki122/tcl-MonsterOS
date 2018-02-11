/*
 * Copyright (C) 2008 The Android Open Source Project
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

import com.android.calendar.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.text.format.Time;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class BirthdayInfoAdapter extends ResourceCursorAdapter {

    private Context mContext;
    private long selectMillis;

    public BirthdayInfoAdapter(Context context, int resource, long millis) {
        super(context, resource, null, true);
        mContext = context;
        selectMillis = millis;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String eventName = cursor.getString(BirthdayInfoActivity.PROJECTION_TITLE_INDEX);
        long startMillis = cursor.getLong(BirthdayInfoActivity.PROJECTION_DTSTART_INDEX);
        updateView(context, view, eventName, startMillis);
    }

    private void updateView(Context context, View view, String eventName, long startMillis) {
        Resources res = context.getResources();

        TextView titleView = (TextView) view.findViewById(R.id.birthday_info_title);
        TextView contentView = (TextView) view.findViewById(R.id.birthday_info_content);
        ImageView wishView = (ImageView) view.findViewById(R.id.birthday_info_wish);

        // What
        if (eventName == null || eventName.length() == 0) {
            eventName = res.getString(R.string.no_title_label);
        }
        titleView.setText(eventName);

        Time time = new Time();
        time.set(selectMillis);
        time.normalize(true);
        int thisYear = time.year;

        time.set(startMillis);
        time.normalize(true);
        int birthYear = time.year;

        contentView.setText(res.getString(R.string.birthday_content, thisYear - birthYear));

        wishView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getString(R.string.happy_birthday));
                intent.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.happy_birthday));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.wish_birthday)));
            }
        });
    }
}
