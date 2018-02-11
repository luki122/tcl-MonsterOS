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
 * limitations under the License
 */

package com.android.phone.settings;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import mst.preference.CheckBoxPreference;
import mst.preference.Preference;
import mst.preference.PreferenceFragment;
import mst.preference.PreferenceScreen;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import com.android.ims.ImsManager;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.phone.PhoneGlobals;
import com.android.phone.R;
import com.android.phone.settings.TtyModeListPreference;

import mst.preference.SwitchPreference;
import mst.app.dialog.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.WindowManager;
import java.util.List;

public class AccessibilitySettingsFragment extends PreferenceFragment {
    private static final String LOG_TAG = AccessibilitySettingsFragment.class.getSimpleName();
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);

    private static final String BUTTON_TTY_KEY = "button_tty_mode_key";
    private static final String BUTTON_HAC_KEY = "button_hac_key";
    //[BUGFIX]-Add-BEGIN by TCTNB.(chuanjun chen), 12/21/2015, Defect-1193771.
    private static Dialog mDialog;
    //[BUGFIX]-Add-BEGIN by TCTNB.(chuanjun chen).
    //dialog identifiers for HAC
    private static final int HEARING_AIDS_DIALOG=605;//[SOLUTION]-Add-BEGIN by TCTNB.(Yubin.Ying), 08/10/2016, SOLUTION-2504527

    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        /**
         * Disable the TTY setting when in/out of a call (and if carrier doesn't
         * support VoLTE with TTY).
         * @see android.telephony.PhoneStateListener#onCallStateChanged(int,
         * java.lang.String)
         */
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (DBG) Log.d(LOG_TAG, "PhoneStateListener.onCallStateChanged: state=" + state);
            Preference pref = getPreferenceScreen().findPreference(BUTTON_TTY_KEY);
            if (pref != null) {
                final boolean isVolteTtySupported = ImsManager.isVolteEnabledByPlatform(mContext)
                        && getVolteTtySupported();
                pref.setEnabled((isVolteTtySupported && !isVideoCallInProgress()) ||
                        (state == TelephonyManager.CALL_STATE_IDLE));
            }
        }
    };

    //[BUGFIX]-Add-BEGIN by TCTNB.(chuanjun chen), 12/21/2015, Defect-1193771.
    private Dialog.OnKeyListener  backListerer = new Dialog.OnKeyListener() {
        /**
         * called when KEYCODE_BACK is pressed.
         */
        @Override
        public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && mDialog.isShowing()) {
                mButtonHac.setChecked(false);
                updateHearingAidsSetting();
                arg0.dismiss();
            }
            return true;
        }
    };
    //[BUGFIX]-Add-BEGIN by TCTNB.(chuanjun chen),

    private Context mContext;
    private AudioManager mAudioManager;

    private TtyModeListPreference mButtonTty;
    private SwitchPreference mButtonHac;//[SOLUTION]-Add-BEGIN by TCTNB.(Yubin.Ying), 08/10/2016, SOLUTION-2504527

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity().getApplicationContext();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        addPreferencesFromResource(R.xml.accessibility_settings);

        mButtonTty = (TtyModeListPreference) findPreference(
                getResources().getString(R.string.tty_mode_key));
        mButtonHac = (SwitchPreference) findPreference(BUTTON_HAC_KEY);//[SOLUTION]-Add-BEGIN by TCTNB.(Yubin.Ying), 08/10/2016, SOLUTION-2504527

        if (PhoneGlobals.getInstance().phoneMgr.isTtyModeSupported()) {
            mButtonTty.init();
        } else {
            getPreferenceScreen().removePreference(mButtonTty);
            mButtonTty = null;
        }

        if (PhoneGlobals.getInstance().phoneMgr.isHearingAidCompatibilitySupported()) {
            int hac = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.HEARING_AID, SettingsConstants.HAC_DISABLED);
            mButtonHac.setChecked(hac == SettingsConstants.HAC_ENABLED);
        } else {
            getPreferenceScreen().removePreference(mButtonHac);
            mButtonHac = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        TelephonyManager tm =
                (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onPause() {
        super.onPause();
        //[BUGFIX]-Add-BEGIN by TCTNB.(chuanjun chen), 12/21/2015, Defect-1193771.
        if (mDialog != null && mDialog.isShowing()) {
            mButtonHac.setChecked(false);
            updateHearingAidsSetting();
            mDialog.dismiss();
        }
        //[BUGFIX]-Add-BEGIN by TCTNB.(chuanjun chen).
        TelephonyManager tm =
                (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mButtonTty) {
            return true;
        } else if (preference == mButtonHac) {
            //[SOLUTION]-Add-BEGIN by TCTNB.(Yubin.Ying), 08/10/2016, SOLUTION-2504527
            if(getResources().getBoolean(R.bool.feature_hac_dialog_enabled) && mButtonHac.isChecked()){
                Dialog dialog = showDialogIfForeground(HEARING_AIDS_DIALOG);
                if (dialog != null) {
                    //[BUGFIX]-Add-BEGIN by TCTNB.(chuanjun chen), 12/21/2015, Defect-1193771.
                    mDialog = dialog;
                    dialog.show();
                }
            }else {
                updateHearingAidsSetting();
                //[BUGFIX]-Add-END by TCTNB.(chuanjun chen).
            }
            //[SOLUTION]-Add-END by TCTNB.(Yubin.Ying)
            return true;
        }
        return false;
    }

    //[SOLUTION]-Add-BEGIN by TCTNB.(Yubin.Ying), 08/10/2016, SOLUTION-2504527
    private Dialog showDialogIfForeground(int id) {
        if (id == HEARING_AIDS_DIALOG) {
            AlertDialog.Builder b = new AlertDialog.Builder(mContext,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);

            int msgId;
            int titleId = R.string.error_updating_title;
            switch (id) {
                case HEARING_AIDS_DIALOG:
                    titleId=R.string.hearing_aids_dialog_title;
                    msgId=R.string.hearing_aids_dialog_content;
                    b.setPositiveButton(R.string.alert_dialog_turn_on,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // TODO Auto-generated method stub
                                mButtonHac.setChecked(true);
                                //[BUGFIX]-Add-BEGIN by TCTNB.(chuanjun chen), 12/21/2015, Defect-1193771.
                                updateHearingAidsSetting();
                                //[BUGFIX]-Add-BEGIN by TCTNB.(chuanjun chen).
                            }
                        });
                        b.setNegativeButton(R.string.alert_dialog_cancel,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                // TODO Auto-generated method stub
                                mButtonHac.setChecked(false);
                                //[BUGFIX]-Add-BEGIN by TCTNB.(chuanjun chen), 12/21/2015, Defect-1193771.
                                updateHearingAidsSetting();
                                //[BUGFIX]-Add-END by TCTNB.(chuanjun chen).
                            }
                        });
                        break;
                    default:
                        msgId = R.string.exception_error;
                        // Set Button 3, tells the activity that the error is
                        // not recoverable on dialog exit.
                        b.setNeutralButton(R.string.close_dialog, null);
                        break;
                }
                b.setTitle(getText(titleId));
                String message = getText(msgId).toString();
                b.setMessage(message);
                //[BUGFIX]-Add-BEGIN by TCTNB.(chuanjun chen), 12/21/2015, Defect-1193771.
                b.setCancelable(true);
                AlertDialog dialog = b.create();
                dialog.setOnKeyListener(backListerer);
                dialog.setCanceledOnTouchOutside(false);
                //[BUGFIX]-Add-END by TCTNB.(chuanjun chen).
                // make the dialog more obvious by bluring the background.
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                return dialog;
            }
            return null;
    }
    //[SOLUTION]-Add-END by TCTNB.(Yubin.Ying)

    private boolean getVolteTtySupported() {
        CarrierConfigManager configManager =
                (CarrierConfigManager) mContext.getSystemService(Context.CARRIER_CONFIG_SERVICE);
        return configManager.getConfig().getBoolean(
                CarrierConfigManager.KEY_CARRIER_VOLTE_TTY_SUPPORTED_BOOL);
    }

    private boolean isVideoCallInProgress() {
        final Phone[] phones = PhoneFactory.getPhones();
        if (phones == null) {
            if (DBG) Log.d(LOG_TAG, "isVideoCallInProgress: No phones found. Return false");
            return false;
        }

        for (Phone phone : phones) {
            if (phone.isVideoCallPresent()) {
                return true;
            }
        }
        return false;
    }

    //[BUGFIX]-Add-BEGIN by TCTNB.(chuanjun chen), 12/21/2015, Defect-1193771.
    private void updateHearingAidsSetting() {
        int hac = mButtonHac.isChecked()
                ? SettingsConstants.HAC_ENABLED : SettingsConstants.HAC_DISABLED;
        // Update HAC value in Settings database.
        Settings.System.putInt(mContext.getContentResolver(), Settings.System.HEARING_AID, hac);
        // Update HAC Value in AudioManager.
        mAudioManager.setParameter(SettingsConstants.HAC_KEY,
                hac == SettingsConstants.HAC_ENABLED
                        ? SettingsConstants.HAC_VAL_ON : SettingsConstants.HAC_VAL_OFF);
    }
    //[BUGFIX]-Add-BEGIN by TCTNB.(chuanjun chen).
}
