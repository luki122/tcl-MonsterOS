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
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class CommonSwitcherPreference extends SwitchPreference {

	private View mRootView;
	
    public CommonSwitcherPreference(Context context) {
        super(context);
    }

    public CommonSwitcherPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public CommonSwitcherPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	
    public CommonSwitcherPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}


    @Override
    protected View onCreateView(ViewGroup parent) {
    	mRootView = super.onCreateView(parent);
//    	mRootView.setPadding(mRootView.getLeft() + 40, mRootView.getTop(), mRootView.getRight(), mRootView.getBottom());
    	return mRootView;
    }

    @Override
    protected void onBindView(View view) {
    	super.onBindView(view);
    }

    public View findViewById(int id) {
        return mRootView.findViewById(id);
    }

}
