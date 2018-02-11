/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.phone.settings;

import mst.app.dialog.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.UserHandle;
import mst.preference.ListPreference;
import mst.preference.Preference;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.util.AttributeSet;
import android.util.Log;

import com.android.internal.telephony.Phone;
import com.android.phone.PhoneGlobals;
import com.android.phone.R;

public class TtyModeListPreference extends ListPreference
        implements Preference.OnPreferenceChangeListener {
    private static final String LOG_TAG = TtyModeListPreference.class.getSimpleName();
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);
    //[SOLUTION]-Add-BEGIN by TCTNB.(Yubin.Ying), 08/12/2016, SOLUTION-2472507
    Context mContext;
    AudioManager mAudioManager;
    //[SOLUTION]-Add-END by TCTNB.(Yubin.Ying)

    public TtyModeListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        //[SOLUTION]-Add-BEGIN by TCTNB.(Yubin.Ying), 08/12/2016, SOLUTION-2472507
        mContext = context;
        //[SOLUTION]-Add-END by TCTNB.(Yubin.Ying)
    }

    public void init() {
        setOnPreferenceChangeListener(this);

        int settingsTtyMode = Settings.Secure.getInt(getContext().getContentResolver(),
                Settings.Secure.PREFERRED_TTY_MODE,
                TelecomManager.TTY_MODE_OFF);
        setValue(Integer.toString(settingsTtyMode));
        updatePreferredTtyModeSummary(settingsTtyMode);
        //[SOLUTION]-Add-BEGIN by TCTNB.(Yubin.Ying), 08/12/2016, SOLUTION-2472507
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        //[SOLUTION]-Add-END by TCTNB.(Yubin.Ying)
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == this) {
            int buttonTtyMode = Integer.parseInt((String) objValue);
            int settingsTtyMode = android.provider.Settings.Secure.getInt(
                    getContext().getContentResolver(),
                    Settings.Secure.PREFERRED_TTY_MODE,
                    TelecomManager.TTY_MODE_OFF);
            if (DBG) log("handleTTYChange: requesting set TTY mode enable (TTY) to" +
                    Integer.toString(buttonTtyMode));

            if (buttonTtyMode != settingsTtyMode) {
                switch(buttonTtyMode) {
                    case TelecomManager.TTY_MODE_OFF:
                    case TelecomManager.TTY_MODE_FULL:
                    case TelecomManager.TTY_MODE_HCO:
                    case TelecomManager.TTY_MODE_VCO:
                        Settings.Secure.putInt(
                                getContext().getContentResolver(),
                                Settings.Secure.PREFERRED_TTY_MODE,
                                buttonTtyMode);
                        break;
                    default:
                        buttonTtyMode = TelecomManager.TTY_MODE_OFF;
                }

                setValue(Integer.toString(buttonTtyMode));
                updatePreferredTtyModeSummary(buttonTtyMode);
                Intent ttyModeChanged =
                        new Intent(TelecomManager.ACTION_TTY_PREFERRED_MODE_CHANGED);
                ttyModeChanged.putExtra(TelecomManager.EXTRA_TTY_PREFERRED_MODE, buttonTtyMode);
                getContext().sendBroadcastAsUser(ttyModeChanged, UserHandle.ALL);
                //[SOLUTION]-Add-BEGIN by TCTNB.(Yubin.Ying), 08/12/2016, SOLUTION-2472507
                if (mContext.getResources().getBoolean(R.bool.feature_phone_activateTTYalsoActivateVibrateMode_on)
                        && buttonTtyMode != Phone.TTY_MODE_OFF) {
                    new AlertDialog.Builder(this.mContext).setTitle(R.string.tty_vibrate_dialog_title)
                            .setMessage(R.string.tty_vibrate_dialog_message)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, AudioManager.VIBRATE_SETTING_ON);
                                    Settings.System.putInt(mContext.getContentResolver(), Settings.System.VIBRATE_IN_SILENT, 1);
                                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            }).show();
                }
                //[SOLUTION]-Add-END by TCTNB.(Yubin.Ying)
            }
        }
        return true;
    }

    private void updatePreferredTtyModeSummary(int TtyMode) {
        String [] txts = getContext().getResources().getStringArray(R.array.tty_mode_entries);
        switch(TtyMode) {
            case TelecomManager.TTY_MODE_OFF:
            case TelecomManager.TTY_MODE_HCO:
            case TelecomManager.TTY_MODE_VCO:
            case TelecomManager.TTY_MODE_FULL:
                setSummary(txts[TtyMode]);
                break;
            default:
                setEnabled(false);
                setSummary(txts[TelecomManager.TTY_MODE_OFF]);
                break;
        }
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
}
