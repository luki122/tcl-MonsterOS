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
import android.os.UserManager;
import android.telecom.DefaultDialerManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.monster.appmanager.AppListPreference;

import java.util.List;
import java.util.Objects;

//[BUGFIX]-Add-BEGIN by TCTNB.caixia.chen,05/06/2016,Defect 1864320
import android.view.View;
import android.widget.TextView;
import com.monster.appmanager.R;
//[BUGFIX]-Add-END by TCTNB.caixia.chen

public class DefaultPhonePreference extends DefaultAppPreferenceBase {
    private final Context mContext;

    public DefaultPhonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        //[BUGFIX]-Add-BEGIN by TCTNB.caixia.chen,05/06/2016,Defect 1864320
        setWidgetLayoutResource(R.layout.pref_widget);
        //[BUGFIX]-Add-END by TCTNB.caixia.chen

        setShowItemNone(false);
        mContext = context.getApplicationContext();
        if (isAvailable(context)) {
            loadDialerApps();
        }
    }

    @Override
    protected boolean persistString(String value) {
        if (!TextUtils.isEmpty(value) && !Objects.equals(value, getDefaultPackage())) {
            TelecomManager.from(mContext).setDefaultDialer(value);
        }
        //[BUGFIX]-Del-BEGIN by TCTNB.caixia.chen,05/06/2016,Defect 1864320
        //setSummary(getEntry());
        //[BUGFIX]-Del-END by TCTNB.caixia.chen
        return true;
    }

    private void loadDialerApps() {
        List<String> dialerPackages =
                DefaultDialerManager.getInstalledDialerApplications(getContext());

        final String[] dialers = new String[dialerPackages.size()];
        for (int i = 0; i < dialerPackages.size(); i++) {
            dialers[i] = dialerPackages.get(i);
        }
        setPackageNames(dialers, getDefaultPackage());
    }

    private String getDefaultPackage() {
        return DefaultDialerManager.getDefaultDialerApplication(getContext());
    }

    public static boolean isAvailable(Context context) {
        final TelephonyManager tm =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (!tm.isVoiceCapable()) {
            return false;
        }

        final UserManager um =
                (UserManager) context.getSystemService(Context.USER_SERVICE);
        return !um.hasUserRestriction(UserManager.DISALLOW_OUTGOING_CALLS);
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
