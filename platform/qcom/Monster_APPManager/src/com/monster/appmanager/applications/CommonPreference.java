/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.monster.appmanager.applications;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.monster.appmanager.R;
import com.monster.appmanager.Utils;

public class CommonPreference extends Preference {

    private View mRootView;

    public CommonPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
//        setSelectable(false);
        final TypedArray a = context.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.Preference, 0, 0);
        int layoutResource = a.getResourceId(com.android.internal.R.styleable.Preference_layout,
                0);
        if (layoutResource == 0) {
            throw new IllegalArgumentException("LayoutPreference requires a layout to be defined");
        }
        View container = LayoutInflater.from(getContext())
                .inflate(R.layout.preference_container, null, false);
        // Need to create view now so that findViewById can be called immediately.
        final View view = LayoutInflater.from(getContext())
                .inflate(layoutResource, (ViewGroup)container, false);

        if (view!= null) {
            Utils.forceCustomPadding(view, true /* additive padding */);
        }
        mRootView = view;
        setShouldDisableView(false);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        return mRootView;
    }

    @Override
    protected void onBindView(View view) {
        // Do nothing.
    }

    public View findViewById(int id) {
        return mRootView.findViewById(id);
    }

}
