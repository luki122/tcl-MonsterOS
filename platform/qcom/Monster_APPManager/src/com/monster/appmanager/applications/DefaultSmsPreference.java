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

import java.util.Collection;
import java.util.Objects;

import com.android.internal.telephony.SmsApplication;
import com.android.internal.telephony.SmsApplication.SmsApplicationData;
import com.monster.appmanager.R;
//[BUGFIX]-Add-END by TCTNB.caixia.chen

import android.content.ComponentName;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
//[BUGFIX]-Add-BEGIN by TCTNB.caixia.chen,05/06/2016,Defect 1864320
import android.view.View;
import android.widget.TextView;

public class DefaultSmsPreference extends DefaultAppPreferenceBase {

    public DefaultSmsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        //[BUGFIX]-Add-BEGIN by TCTNB.caixia.chen,05/06/2016,Defect 1864320
        setWidgetLayoutResource(R.layout.pref_widget);
        //[BUGFIX]-Add-END by TCTNB.caixia.chen

        setShowItemNone(false);
        loadSmsApps();
    }

    private void loadSmsApps() {
        Collection<SmsApplicationData> smsApplications =
                SmsApplication.getApplicationCollection(getContext());

        int count = smsApplications.size();
        String[] packageNames = new String[count];
        int i = 0;
        for (SmsApplicationData smsApplicationData : smsApplications) {
            packageNames[i++] = smsApplicationData.mPackageName;
        }
        setPackageNames(packageNames,  getDefaultPackage());
    }

    private String getDefaultPackage() {
        ComponentName appName = SmsApplication.getDefaultSmsApplication(getContext(), true);
        if (appName != null) {
            return appName.getPackageName();
        }
        return null;
    }

    @Override
    protected boolean persistString(String value) {
        if ( !Objects.equals(value, getDefaultPackage())) {
        	if(TextUtils.isEmpty(value)){
        		// have no effect
        		String defaultPackage = getDefaultPackage();
        		mPm.clearPackagePreferredActivities(defaultPackage);
        	} else {
        		SmsApplication.setDefaultApplication(value, getContext());
        	}
        }
        //[BUGFIX]-Del-BEGIN by TCTNB.caixia.chen,05/06/2016,Defect 1864320
        //setSummary(getEntry());
        //[BUGFIX]-Del-END by TCTNB.caixia.chen
        return true;
    }

    public static boolean isAvailable(Context context) {
        TelephonyManager tm =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.isSmsCapable();
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
