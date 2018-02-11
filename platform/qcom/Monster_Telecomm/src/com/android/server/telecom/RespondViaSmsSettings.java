/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.server.telecom;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import mst.preference.*;

// TODO: This class is newly copied into Telecom (com.android.server.telecom) from it previous
// location in Telephony (com.android.phone). User's preferences stored in the old location
// will be lost. We need code here to migrate KLP -> LMP settings values.

/**
 * Settings activity to manage the responses available for the "Respond via SMS Message" feature to
 * respond to incoming calls.
 */
public class RespondViaSmsSettings extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.d(this, "Settings: onCreate()...");

        // This function guarantees that QuickResponses will be in our
        // SharedPreferences with the proper values considering there may be
        // old QuickResponses in Telephony pre L.
        QuickResponseUtils.maybeMigrateLegacyQuickResponses(this);

        getPreferenceManager().setSharedPreferencesName(QuickResponseUtils.SHARED_PREFERENCES_NAME);
        mPrefs = getPreferenceManager().getSharedPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();

        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            preferenceScreen.removeAll();
        }

        // This preference screen is ultra-simple; it's just 4 plain
        // <EditTextPreference>s, one for each of the 4 "canned responses".
        //
        // The only nontrivial thing we do here is copy the text value of
        // each of those EditTextPreferences and use it as the preference's
        // "title" as well, so that the user will immediately see all 4
        // strings when they arrive here.
        //
        // Also, listen for change events (since we'll need to update the
        // title any time the user edits one of the strings.)

        addPreferencesFromResource(R.xml.respond_via_sms_settings);
        //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/11/2016, SOLUTION-2455894
        SharedPreferences sharedpref = getSharedPreferences(QuickResponseUtils.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        initSharedPreferences(sharedpref);
        if (getResources().getBoolean(R.bool.def_Quick_Response_Via_SMS_For_FRANCE_Orange)) {
            PreferenceScreen prefSet = getPreferenceScreen();
            EditTextPreference pref = (EditTextPreference) findPreference(
                    QuickResponseUtils.KEY_CANNED_RESPONSE_PREF_1);
            prefSet.removePreference(pref);
        } else {
            initPref(findPreference(QuickResponseUtils.KEY_CANNED_RESPONSE_PREF_1));
        }
        //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
        initPref(findPreference(QuickResponseUtils.KEY_CANNED_RESPONSE_PREF_2));
        initPref(findPreference(QuickResponseUtils.KEY_CANNED_RESPONSE_PREF_3));
//        initPref(findPreference(QuickResponseUtils.KEY_CANNED_RESPONSE_PREF_4));

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/11/2016, SOLUTION-2455894
    public void initSharedPreferences(SharedPreferences share) {
        SharedPreferences.Editor editor = share.edit();
        String flag = share.getString("flag", "flag");
        if (!flag.equals(getResources().getString(R.string.respond_via_sms_canned_response_4))){
            editor.putString("flag",
                    getResources().getString(R.string.respond_via_sms_canned_response_4));
            editor.putString(QuickResponseUtils.KEY_CANNED_RESPONSE_PREF_1,
                    getResources().getString(R.string.respond_via_sms_canned_response_1));
            editor.putString(QuickResponseUtils.KEY_CANNED_RESPONSE_PREF_2,
                    getResources().getString(R.string.respond_via_sms_canned_response_2));
            editor.putString(QuickResponseUtils.KEY_CANNED_RESPONSE_PREF_3,
                    getResources().getString(R.string.respond_via_sms_canned_response_3));
//            editor.putString(QuickResponseUtils.KEY_CANNED_RESPONSE_PREF_4,
//                    getResources().getString(R.string.respond_via_sms_canned_response_4));
            editor.commit();
        }
    }
    //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)

    // Preference.OnPreferenceChangeListener implementation
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Log.d(this, "onPreferenceChange: key = %s", preference.getKey());
        Log.d(this, "  preference = '%s'", preference);
        Log.d(this, "  newValue = '%s'", newValue);
        
        //add by lgy
        String value = (String) newValue;
        if(TextUtils.isEmpty(value) || TextUtils.isEmpty(value.trim())  ) {
            Toast.makeText(
                    this,
                    this.getResources().getString(R.string.not_empty_value),
                    Toast.LENGTH_LONG).show();
            return false;
        }

        EditTextPreference pref = (EditTextPreference) preference;

        // Copy the new text over to the title, just like in onCreate().
        // (Watch out: onPreferenceChange() is called *before* the
        // Preference itself gets updated, so we need to use newValue here
        // rather than pref.getText().)
        String quickResponse = ((String) newValue).trim();
        if (TextUtils.isEmpty(quickResponse)) {
            return false;
        }

        //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/11/2016, SOLUTION-2455894
        if(!TextUtils.isEmpty(((String)newValue))){
            pref.setTitle((String) newValue);
            // Save the new preference value.
            SharedPreferences.Editor editor = mPrefs.edit();
            editor.putString(pref.getKey(), (String) newValue).commit();
            return true;  // means it's OK to update the state of the Preference with the new value
        }else{
            Toast.makeText(RespondViaSmsSettings.this,
                     R.string.respond_via_sms_cannot_be_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                goUpToTopLevelSetting(this);
                return true;
            //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/11/2016, SOLUTION-2455894
            case R.id.respond_via_message_reset:
                // Reset the preferences settings
                SharedPreferences prefs = getSharedPreferences(
                        QuickResponseUtils.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                EditTextPreference pref;

                if (!getResources().getBoolean(R.bool.def_Quick_Response_Via_SMS_For_FRANCE_Orange)) {
                    pref = (EditTextPreference) findPreference(QuickResponseUtils.KEY_CANNED_RESPONSE_PREF_1);
                    pref.setTitle(getResources().getString(R.string.respond_via_sms_canned_response_1));
                    pref.setText(getResources().getString(R.string.respond_via_sms_canned_response_1));
                }
                pref = (EditTextPreference) findPreference(QuickResponseUtils.KEY_CANNED_RESPONSE_PREF_2);
                pref.setTitle(getResources().getString(R.string.respond_via_sms_canned_response_2));
                pref.setText(getResources().getString(R.string.respond_via_sms_canned_response_2));
                pref = (EditTextPreference) findPreference(QuickResponseUtils.KEY_CANNED_RESPONSE_PREF_3);
                pref.setTitle(getResources().getString(R.string.respond_via_sms_canned_response_3));
                pref.setText(getResources().getString(R.string.respond_via_sms_canned_response_3));
//                pref = (EditTextPreference) findPreference(QuickResponseUtils.KEY_CANNED_RESPONSE_PREF_4);
//                pref.setTitle(getResources().getString(R.string.respond_via_sms_canned_response_4));
//                pref.setText(getResources().getString(R.string.respond_via_sms_canned_response_4));
                editor.clear();
                editor.apply();
                initSharedPreferences(prefs);
                return true;
                //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/11/2016, SOLUTION-2455894
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.respond_via_message_settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)

    /**
     * Finish current Activity and go up to the top level Settings.
     */
    public static void goUpToTopLevelSetting(Activity activity) {
        activity.finish();
    }

    /**
     * Initialize the preference to the persisted preference value or default text.
     */
    private void initPref(Preference preference) {
        EditTextPreference pref = (EditTextPreference) preference;
        pref.setText(mPrefs.getString(pref.getKey(), pref.getText()));
        pref.setTitle(mPrefs.getString(pref.getKey(), pref.getText()));//[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/11/2016, SOLUTION-2455894
        pref.setOnPreferenceChangeListener(this);
    }
    
    //add by lgy for 3326121
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,  
            Preference preference) {  
           // TODO Auto-generated method stub  
            super.onPreferenceTreeClick(preferenceScreen, preference);  
            // 判断是否是EditTextPreference
            if (preference instanceof EditTextPreference) {
                EditText ed = ((EditTextPreference) preference).getEditText();
                Editable etable = ed.getText();
                ed.setSelection(etable.length());// 光标置位
    
            }
            return true;  
     } 
}
