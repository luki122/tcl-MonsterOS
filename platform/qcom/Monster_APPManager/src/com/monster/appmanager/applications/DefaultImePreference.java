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

import java.util.List;
import java.util.Objects;

import com.android.internal.telephony.SmsApplication;
import com.monster.appmanager.R;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

public class DefaultImePreference extends DefaultAppPreferenceBase {

    public DefaultImePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        //[BUGFIX]-Add-BEGIN by TCTNB.caixia.chen,05/06/2016,Defect 1864320
        setWidgetLayoutResource(R.layout.pref_widget);
        //[BUGFIX]-Add-END by TCTNB.caixia.chen

        setShowItemNone(false);
        loadImeApps();
    }

    private void loadImeApps() {
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> infos=imm.getEnabledInputMethodList();
        String[] packageNames = new String[infos.size()];
        int i = 0;
        for (InputMethodInfo inputMethodInfo : infos) {
        	packageNames[i++] = inputMethodInfo.getPackageName();
        }
        setPackageNames(packageNames,  getDefaultPackage());
    }

    private String getDefaultPackage() {
        String defaultImeId=Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        if(!TextUtils.isEmpty(defaultImeId)) {
        	return getPackageNameFromId(defaultImeId);        
        }
        return null;
    }
    
    private String getPackageNameFromId(String id) {
    	String packageName = null;
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> infos=imm.getInputMethodList();
        for (InputMethodInfo inputMethodInfo : infos) {
            if(inputMethodInfo.getId().equals(id)){
            	packageName = inputMethodInfo.getPackageName();
                break;
            }
        }
    	return packageName;
    }
    
    private String getIdFromPackageName(String packageName) {
    	String id = null;
        InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> infos=imm.getInputMethodList();
        for (InputMethodInfo inputMethodInfo : infos) {
            if(inputMethodInfo.getPackageName().equals(packageName)){
            	id = inputMethodInfo.getId();
                break;
            }
        }
    	return id;
    }

    private void setInputMethod(String id) {
    	if(!TextUtils.isEmpty(id)) {
    		InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    		imm.setInputMethod(null, id);
    	}
    }
    
    @Override
    protected boolean persistString(String value) {
        if ( !Objects.equals(value, getDefaultPackage())) {
        	if(!TextUtils.isEmpty(value)){
        		String id = getIdFromPackageName(value);
        		setInputMethod(id);
        	}
        }
        return true;
    }

    //[BUGFIX]-Add-BEGIN by TCTNB.caixia.chen,05/06/2016,Defect 1864320
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        View widgetFrame = view.findViewById(com.android.internal.R.id.widget_frame);

        if (widgetFrame != null) {
            TextView v = (TextView) widgetFrame.findViewById(R.id.pref_tv_detail);
            v.setText(R.string.system_default);
        }
    }
    //[BUGFIX]-Add-END by TCTNB.caixia.chen
}
