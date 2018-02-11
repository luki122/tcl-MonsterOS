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

package com.monster.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.TextUtils;

import com.monster.launcher.R;
import com.monster.launcher.unread.NotificationMonitorListener;

/**
 * Settings activity for Launcher. Currently implements the following setting: Allow rotation
 */
public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new LauncherSettingsFragment())
                .commit();
    }

    /**
     * This fragment shows the launcher preferences.
     */
    public static class LauncherSettingsFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {
        SwitchPreference prefShowUnread;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.launcher_preferences);

            SwitchPreference pref = (SwitchPreference) findPreference(
                    Utilities.ALLOW_ROTATION_PREFERENCE_KEY);
            pref.setPersistent(false);

            Bundle extras = new Bundle();
            extras.putBoolean(LauncherSettings.Settings.EXTRA_DEFAULT_VALUE, false);
            Bundle value = getActivity().getContentResolver().call(
                    LauncherSettings.Settings.CONTENT_URI,
                    LauncherSettings.Settings.METHOD_GET_BOOLEAN,
                    Utilities.ALLOW_ROTATION_PREFERENCE_KEY, extras);
            pref.setChecked(value.getBoolean(LauncherSettings.Settings.EXTRA_VALUE));

            pref.setOnPreferenceChangeListener(this);


            prefShowUnread = (SwitchPreference) findPreference(
                    Utilities.UNREAD_PREFERENCE_KEY);
            if(prefShowUnread!=null && !Utilities.isUnreadSupportedForDevice(getActivity().getApplicationContext())){
                getPreferenceScreen().removePreference(prefShowUnread);
            }else {
                prefShowUnread.setChecked(Utilities.isUnreadSupportedPrefEnabled(getActivity().getApplicationContext(),false)
                        && Utilities.isUnreadNotificationAccessed(getActivity().getApplicationContext()));
                prefShowUnread.setPersistent(true);
                prefShowUnread.setOnPreferenceChangeListener(this);
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            final String key = preference.getKey();
            if (Utilities.UNREAD_PREFERENCE_KEY.equals(key)) {
                boolean check = ((SwitchPreference) preference).isChecked();
                if(!check && !Utilities.isUnreadNotificationAccessed(getActivity())){
                    Context context = getActivity();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(context.getString(R.string.unread_notification_access_setting_content));
                    builder.setTitle(R.string.unread_notification_access_setting_title);
                    builder.setPositiveButton(R.string.unread_notification_access_setting_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            startActivity(intent);
                        }
                    });
                    builder.setNegativeButton(R.string.unread_notification_access_setting_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            prefShowUnread.setChecked(false);
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
                Bundle extras = new Bundle();
                extras.putBoolean(LauncherSettings.Settings.EXTRA_VALUE, (Boolean) newValue);
                getActivity().getContentResolver().call(
                        LauncherSettings.Settings.CONTENT_URI,
                        LauncherSettings.Settings.METHOD_SET_BOOLEAN,
                        preference.getKey(), extras);

                getActivity().sendBroadcast(new Intent("unread_support_pref_change"));
            }else if(Utilities.ALLOW_ROTATION_PREFERENCE_KEY.equals(key)){
                Bundle extras = new Bundle();
                extras.putBoolean(LauncherSettings.Settings.EXTRA_VALUE, (Boolean) newValue);
                getActivity().getContentResolver().call(
                        LauncherSettings.Settings.CONTENT_URI,
                        LauncherSettings.Settings.METHOD_SET_BOOLEAN,
                        preference.getKey(), extras);
            }
            return true;
        }

        @Override
        public void onResume() {
            super.onResume();
            if (prefShowUnread.isChecked() && !Utilities.isUnreadNotificationAccessed(getActivity())) {
                prefShowUnread.setChecked(false);
                Bundle extras = new Bundle();
                extras.putBoolean(LauncherSettings.Settings.EXTRA_VALUE, false);
                getActivity().getContentResolver().call(
                        LauncherSettings.Settings.CONTENT_URI,
                        LauncherSettings.Settings.METHOD_SET_BOOLEAN,
                        prefShowUnread.getKey(), extras);
                getActivity().sendBroadcast(new Intent("unread_support_pref_change"));
            }
        }
    }
}
