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

package com.monster.interception.activity;

import android.content.Context;
import mst.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.monster.interception.R;

/**
 * A {@link Preference} that provides a two-state toggleable option.
 * <p>
 * This preference will store a boolean into the SharedPreferences.
 *
 * @attr ref android.R.styleable#SwitchPreference_summaryOff
 * @attr ref android.R.styleable#SwitchPreference_summaryOn
 * @attr ref android.R.styleable#SwitchPreference_switchTextOff
 * @attr ref android.R.styleable#SwitchPreference_switchTextOn
 * @attr ref android.R.styleable#SwitchPreference_disableDependentsState
 */
public class CountAndNarrowPreference extends Preference {

    private TextView mCount;

    public CountAndNarrowPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // TODO Auto-generated constructor stub
    }

    public CountAndNarrowPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // TODO Auto-generated constructor stub
    }

    public CountAndNarrowPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public CountAndNarrowPreference(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mCount = (TextView)view.findViewById(R.id.count_tv);
    }

    public void setCount(String count) {
        if(mCount != null) {
            mCount.setText(count);
        }
    }
}
