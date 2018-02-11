/*
 * Copyright (C) 2008 The Android Open Source Project
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
/******************************************************************************/
/* ========================================================================== */
/*     Modifications on Features list / Changes Request / Problems Report     */
/* -------------------------------------------------------------------------- */
/*    date   |        author        |         Key          |     comment      */
/* ----------|----------------------|----------------------|----------------- */
/* 11/01/2016|     Dandan.Fang      |    DEFECT3278027     |[Call settings]   */
/*           |                      |                      |"IMS settings" s- */
/*           |                      |                      |hould be hidden   */
/*           |                      |                      |when operator not */
/*           |                      |                      | support volte.   */
/* ----------|----------------------|----------------------|----------------- */
/******************************************************************************/

package com.android.phone;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityOptions;
import mst.app.dialog.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.os.UserManager;
import mst.preference.CheckBoxPreference;
import mst.preference.ListPreference;
import mst.preference.Preference;
import mst.preference.PreferenceActivity;
import mst.preference.PreferenceManager;
import mst.preference.PreferenceScreen;
import android.provider.Settings;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import mst.preference.SwitchPreference;

import com.android.ims.ImsConfig;
import com.android.ims.ImsManager;
import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.phone.common.util.SettingsUtil;
import com.android.phone.settings.AccountSelectionPreference;
import com.android.phone.settings.PhoneAccountSettingsFragment;
import com.android.phone.settings.VoicemailSettingsActivity;
import com.android.phone.settings.fdn.FdnSetting;
import com.android.services.telephony.sip.SipUtil;
//[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/26/2016, SOLUTION- 2500182 And TASk-2781355
import com.android.internal.telephony.TelephonyProperties;
import android.os.SystemProperties;
//[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen).

import java.lang.String;
import java.util.ArrayList;
import java.util.List;

/**
 * Top level "Call settings" UI; see res/xml/call_feature_setting.xml
 *
 * This preference screen is the root of the "Call settings" hierarchy available from the Phone
 * app; the settings here let you control various features related to phone calls (including
 * voicemail settings, the "Respond via SMS" feature, and others.)  It's used only on
 * voice-capable phone devices.
 *
 * Note that this activity is part of the package com.android.phone, even
 * though you reach it from the "Phone" app (i.e. DialtactsActivity) which
 * is from the package com.android.contacts.
 *
 * For the "Mobile network settings" screen under the main Settings app,
 * See {@link MobileNetworkSettings}.
 *
 * @see com.android.phone.MobileNetworkSettings
 */
public class CallFeaturesSetting extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {
    private static final String LOG_TAG = "CallFeaturesSetting";
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);

    // String keys for preference lookup
    // TODO: Naming these "BUTTON_*" is confusing since they're not actually buttons(!)
    // TODO: Consider moving these strings to strings.xml, so that they are not duplicated here and
    // in the layout files. These strings need to be treated carefully; if the setting is
    // persistent, they are used as the key to store shared preferences and the name should not be
    // changed unless the settings are also migrated.
    //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/23/2016, SOLUTION- 2504602 And TASk-2781337
    private static final String CALL_FORWARDING_KEY = "call_forwarding_key";
    private static final String ADDITIONAL_GSM_SETTINGS_KEY = "additional_gsm_call_settings_key";
    private static final String BUTTON_CB_EXPAND_KEY = "button_callbarring_expand_key";
    //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
    private static final String VOICEMAIL_SETTING_SCREEN_PREF_KEY = "button_voicemail_category_key";
    private static final String BUTTON_FDN_KEY   = "button_fdn_key";
    private static final String BUTTON_RETRY_KEY       = "button_auto_retry_key";
    private static final String BUTTON_GSM_UMTS_OPTIONS = "button_gsm_more_expand_key";
    private static final String BUTTON_CDMA_OPTIONS = "button_cdma_more_expand_key";

    private static final String PHONE_ACCOUNT_SETTINGS_KEY =
            "phone_account_settings_preference_screen";

    private static final String ENABLE_VIDEO_CALLING_KEY = "button_enable_video_calling";

  //[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,11/01/2016,DEFECT3278027,
  //[Call settings] "IMS settings" should be hidden when operator not support volte.
    private static final String BUTTON_IMS_SETTINGS_KEY =  "ims_settings_key";
    //[BUGFIX]-Add-END by TCTNB.Dandan.Fang

    private static String mSimConfig; //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/26/2016, SOLUTION- 2500182 And TASk-2781355
    private Phone mPhone;
    private SubscriptionInfoHelper mSubscriptionInfoHelper;
    private TelecomManager mTelecomManager;

    private CheckBoxPreference mButtonAutoRetry;
    private PreferenceScreen mVoicemailSettingsScreen;
    private CheckBoxPreference mEnableVideoCalling;

//[SOLUTION]-Add-BEGIN by TCTNB.(JiangLong Pan), 08/12/2016, SOLUTION-2480868
//[SIM]SDN not found in SIM Tool or other menu.
    private PreferenceScreen mButtonSDNSettings;
    private static final String BUTTON_SDN_LIST_KEY = "button_sdn_list_key";
//[SOLUTION]-Add-END by TCTNB.(JiangLong Pan)
    //ADD-BEGIN by Dingyi  2016/08/12 SOLUTION 2473833
    private SwitchPreference mVibrateReminder;
    private static final String BUTTON_REMINDER_KEY = "button_reminder_key";
    private SharedPreferences sharedPreferences;
    //ADD-END by Dingyi  2016/08/12 SOLUTION 2473833
    /*
     * Click Listeners, handle click based on objects attached to UI.
     */

    // Click listener for all toggle events
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mButtonAutoRetry) {
            android.provider.Settings.Global.putInt(mPhone.getContext().getContentResolver(),
                    android.provider.Settings.Global.CALL_AUTO_RETRY,
                    mButtonAutoRetry.isChecked() ? 1 : 0);
            return true;
        }
        return false;
    }

    /**
     * Implemented to support onPreferenceChangeListener to look for preference
     * changes.
     *
     * @param preference is the preference to be changed
     * @param objValue should be the value of the selection, NOT its localized
     * display value.
     */
    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (DBG) log("onPreferenceChange: \"" + preference + "\" changed to \"" + objValue + "\"");

        if (preference == mEnableVideoCalling) {
            if (ImsManager.isEnhanced4gLteModeSettingEnabledByUser(mPhone.getContext())) {
                PhoneGlobals.getInstance().phoneMgr.enableVideoCalling((boolean) objValue);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                DialogInterface.OnClickListener networkSettingsClickListener =
                        new Dialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(mPhone.getContext(),
                                        com.android.phone.MobileNetworkSettings.class));
                            }
                        };
                builder.setMessage(getResources().getString(
                                R.string.enable_video_calling_dialog_msg))
                        .setNeutralButton(getResources().getString(
                                R.string.enable_video_calling_dialog_settings),
                                networkSettingsClickListener)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                return false;
            }
        //ADD-BEGIN by Dingyi  2016/08/12 SOLUTION 2473833
        }else if (preference == mVibrateReminder) {
            boolean vibrateReminder = (Boolean) objValue;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(BUTTON_REMINDER_KEY + mPhone.getPhoneId(), vibrateReminder).commit();
        //ADD-END by Dingyi  2016/08/12 SOLUTION 2473833
        }

        // Always let the preference setting proceed.
        return true;
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (DBG) log("onCreate: Intent is " + getIntent());

        // Make sure we are running as an admin user.
        if (!UserManager.get(this).isAdminUser()) {
            Toast.makeText(this, R.string.call_settings_admin_user_only,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //ADD-BEGIN by Dingyi  2016/08/12 SOLUTION 2473833
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
        //ADD-END by Dingyi  2016/08/12 SOLUTION 2473833
        mSubscriptionInfoHelper = new SubscriptionInfoHelper(this, getIntent());
        mSubscriptionInfoHelper.setActionBarTitle(
                getActionBar(), getResources(), R.string.call_settings_with_label);
        mPhone = mSubscriptionInfoHelper.getPhone();
        mTelecomManager = TelecomManager.from(this);
        ActivityCollector.addActivity(this); //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/31/2016, SOLUTION- 2499549 And TASk-2781362
        mSimConfig = SystemProperties.get(TelephonyProperties.PROPERTY_MULTI_SIM_CONFIG); //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/26/2016, SOLUTION- 2500182 And TASk-2781355
    }

    @Override
    protected void onResume() {
        super.onResume();

        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            preferenceScreen.removeAll();
        }
        boolean isMoveMenuforalgb = getResources().getBoolean(R.bool.def_remove_additional_menu_for_algb_on); //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/23/2016, SOLUTION- 2504602 And TASk-2781337

        addPreferencesFromResource(R.xml.call_feature_setting);

        TelephonyManager telephonyManager =
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        Preference phoneAccountSettingsPreference = findPreference(PHONE_ACCOUNT_SETTINGS_KEY);
        //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/26/2016, SOLUTION- 2500182 And TASk-2781355.
        boolean isRemoveCallAccount = getResources().getBoolean(R.bool.feature_phone_remove_call_account);
        if (telephonyManager.isMultiSimEnabled() || !SipUtil.isVoipSupported(mPhone.getContext())
            || "ssss".equals(mSimConfig) || isRemoveCallAccount) {
            getPreferenceScreen().removePreference(phoneAccountSettingsPreference);
        }
        //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen).
        PreferenceScreen prefSet = getPreferenceScreen();

        //ADD-BEGIN by Dingyi  2016/08/12 SOLUTION 2473833
        mVibrateReminder = (SwitchPreference) findPreference(BUTTON_REMINDER_KEY);
        mVibrateReminder.setOnPreferenceChangeListener(this);
        boolean checkVibrate = sharedPreferences.getBoolean(BUTTON_REMINDER_KEY + mPhone.getPhoneId(),
                               getResources().getBoolean(R.bool.def_phone_vibrateReminder_on));
        log("onCreate: def_phone_vibrateReminder_on = " + getResources().getBoolean(R.bool.def_phone_vibrateReminder_on));
        log("onCreate: checkVibrate = " + checkVibrate);
        mVibrateReminder.setChecked(checkVibrate);
        //ADD-END by Dingyi  2016/08/12 SOLUTION 2473833

        mVoicemailSettingsScreen =
                (PreferenceScreen) findPreference(VOICEMAIL_SETTING_SCREEN_PREF_KEY);
        mVoicemailSettingsScreen.setIntent(mSubscriptionInfoHelper.getIntent(
                VoicemailSettingsActivity.class));

        mButtonAutoRetry = (CheckBoxPreference) findPreference(BUTTON_RETRY_KEY);

        mEnableVideoCalling = (CheckBoxPreference) findPreference(ENABLE_VIDEO_CALLING_KEY);

//[SOLUTION]-Add-BEGIN by TCTNB.(JiangLong Pan), 08/12/2016, SOLUTION-2480868
//[SIM]SDN not found in SIM Tool or other menu.
        mButtonSDNSettings = (PreferenceScreen)findPreference(BUTTON_SDN_LIST_KEY);
        if (!getResources().getBoolean(R.bool.feature_phone_sdnmenu_on)) {
            prefSet.removePreference(mButtonSDNSettings);
        }
//[SOLUTION]-Add-END by TCTNB.(JiangLong Pan)

        PersistableBundle carrierConfig =
                PhoneGlobals.getInstance().getCarrierConfigForSubId(mPhone.getSubId());

        if (carrierConfig.getBoolean(CarrierConfigManager.KEY_AUTO_RETRY_ENABLED_BOOL)) {
            mButtonAutoRetry.setOnPreferenceChangeListener(this);
            int autoretry = Settings.Global.getInt(
                    getContentResolver(), Settings.Global.CALL_AUTO_RETRY, 0);
            mButtonAutoRetry.setChecked(autoretry != 0);
        } else {
            prefSet.removePreference(mButtonAutoRetry);
            mButtonAutoRetry = null;
        }

        Preference cdmaOptions = prefSet.findPreference(BUTTON_CDMA_OPTIONS);
        Preference gsmOptions = prefSet.findPreference(BUTTON_GSM_UMTS_OPTIONS);
        //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/23/2016, SOLUTION- 2504602 And TASk-2781337
        Preference additionalGsmSettingsPref = prefSet.findPreference(ADDITIONAL_GSM_SETTINGS_KEY);
        Preference callBarringPref = prefSet.findPreference(BUTTON_CB_EXPAND_KEY);
        Preference callForwardingPref = prefSet.findPreference(CALL_FORWARDING_KEY);
        boolean needRemoveVoice = getResources().getBoolean(R.bool.def_remove_voice_from_call_forwarding);
        if (needRemoveVoice){
                final String SUB_ID_EXTRA = "com.android.phone.settings.SubscriptionInfoHelper.SubscriptionId";
                Intent myIntent = mSubscriptionInfoHelper.getIntent(GsmUmtsCallForwardOptions.class);
                myIntent.putExtra(PhoneUtils.SERVICE_CLASS, PhoneUtils.SERVICE_CLASS_VOICE);
                myIntent.putExtra(SUB_ID_EXTRA, mSubscriptionInfoHelper.getPhone().getSubId());
                callForwardingPref.setIntent(myIntent);
            }else{
                callForwardingPref.setIntent(mSubscriptionInfoHelper.getIntent(CallForwardType.class));
            }
        if (isMoveMenuforalgb) {
            prefSet.removePreference(gsmOptions);
        } else {
            prefSet.removePreference(additionalGsmSettingsPref);
            prefSet.removePreference(callForwardingPref);
            prefSet.removePreference(callBarringPref);
        }
        //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
        Preference fdnButton = prefSet.findPreference(BUTTON_FDN_KEY);
        //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 09/01/2016, SOLUTION- 2499549 And TASk-2781362
        Log.i(LOG_TAG, "mPhone.getSIMFDNServiceStatus()="+mPhone.getSIMFDNServiceStatus()+"fdnButton != null?"+(fdnButton != null));
        boolean isParticularSuscriptionOn = getResources().getBoolean(R.bool.feature_fdn_particular_suscription_on);
        if (isParticularSuscriptionOn && !mPhone.getSIMFDNServiceStatus() && fdnButton != null) {
            prefSet.removePreference(fdnButton);
        }
        //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
        fdnButton.setIntent(mSubscriptionInfoHelper.getIntent(FdnSetting.class));
        if (carrierConfig.getBoolean(CarrierConfigManager.KEY_WORLD_PHONE_BOOL)) {
            cdmaOptions.setIntent(mSubscriptionInfoHelper.getIntent(CdmaCallOptions.class));
            getPreferenceScreen().removePreference(cdmaOptions);
            gsmOptions.setIntent(mSubscriptionInfoHelper.getIntent(GsmUmtsCallOptions.class));
            //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/23/2016, SOLUTION- 2504602 And TASk-2781337
            additionalGsmSettingsPref.setIntent(
                    mSubscriptionInfoHelper.getIntent(GsmUmtsAdditionalCallOptions.class));
            //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
        } else {
            prefSet.removePreference(cdmaOptions);
            prefSet.removePreference(gsmOptions);

            int phoneType = mPhone.getPhoneType();
            if (carrierConfig.getBoolean(CarrierConfigManager.KEY_HIDE_CARRIER_NETWORK_SETTINGS_BOOL)) {
                prefSet.removePreference(fdnButton);
            } else {
                if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
                    prefSet.removePreference(fdnButton);
                    addPreferencesFromResource(R.xml.cdma_call_privacy);

                    if (carrierConfig.getBoolean(
                            CarrierConfigManager.KEY_VOICE_PRIVACY_DISABLE_UI_BOOL)) {
                        CdmaVoicePrivacyCheckBoxPreference prefPri = (CdmaVoicePrivacyCheckBoxPreference)
                                prefSet.findPreference("button_voice_privacy_key");
                        if (prefPri != null) {
                             prefSet.removePreference(prefPri);
                        }
                    }

                    if (carrierConfig.getBoolean(
                                CarrierConfigManager.KEY_CDMA_CW_CF_ENABLED_BOOL)
                                && CdmaCallOptions.isCdmaCallWaitingActivityPresent(mPhone.getContext())) {
                        Log.d(LOG_TAG, "Enabled CW CF");
                        PreferenceScreen prefCW = (PreferenceScreen)
                                prefSet.findPreference("button_cw_key");
                        if (prefCW != null) {
                            prefCW.setOnPreferenceClickListener(
                                    new Preference.OnPreferenceClickListener() {
                                        @Override
                                        public boolean onPreferenceClick(Preference preference) {
                                            Intent intent = new Intent(CdmaCallOptions.CALL_WAITING_INTENT);
                                            intent.putExtra(PhoneConstants.SUBSCRIPTION_KEY, mPhone.getSubId());
                                            startActivity(intent);
                                            return true;
                                        }
                                    });
                        }
                        PreferenceScreen prefCF = (PreferenceScreen)
                                prefSet.findPreference("button_cf_expand_key");
                        if (prefCF != null) {
                            prefCF.setOnPreferenceClickListener(
                                    new Preference.OnPreferenceClickListener() {
                                        @Override
                                        public boolean onPreferenceClick(Preference preference) {
                                            Intent intent = new Intent(CdmaCallOptions.CALL_FORWARD_INTENT);
                                            intent.putExtra(PhoneConstants.SUBSCRIPTION_KEY, mPhone.getSubId());
                                            startActivity(intent);
                                            return true;
                                        }
                                    });
                        }
                    } else {
                        Log.d(LOG_TAG, "Disabled CW CF");
                        PreferenceScreen prefCW = (PreferenceScreen)
                                prefSet.findPreference("button_cw_key");
                        if (prefCW != null) {
                            prefSet.removePreference(prefCW);
                        }
                        PreferenceScreen prefCF = (PreferenceScreen)
                                prefSet.findPreference("button_cf_expand_key");
                        if (prefCF != null) {
                            prefSet.removePreference(prefCF);
                        }
                    }
                } else if (phoneType == PhoneConstants.PHONE_TYPE_GSM) {

                    if (carrierConfig.getBoolean(
                            CarrierConfigManager.KEY_ADDITIONAL_CALL_SETTING_BOOL)) {
                        addPreferencesFromResource(R.xml.gsm_umts_call_options);
                        GsmUmtsCallOptions.init(prefSet, mSubscriptionInfoHelper);
                    }
                } else {
                    throw new IllegalStateException("Unexpected phone type: " + phoneType);
                }
            }
        }

        //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/31/2016, SOLUTION- 2500194 And TASk-2781349
        //[Dialer][call][Dual]Set up SIM card 1 Turn on video calling options, SIM card 2 settings will change
        if ("ssss".equals(mSimConfig) && ImsManager.isVtEnabledByPlatform(mPhone.getContext())) {
        //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
            boolean currentValue =
                    ImsManager.isEnhanced4gLteModeSettingEnabledByUser(mPhone.getContext())
                    ? PhoneGlobals.getInstance().phoneMgr.isVideoCallingEnabled(
                            getOpPackageName()) : false;
            mEnableVideoCalling.setChecked(currentValue);
            mEnableVideoCalling.setOnPreferenceChangeListener(this);
        } else {
            prefSet.removePreference(mEnableVideoCalling);
        }

        if (ImsManager.isVolteEnabledByPlatform(this) &&
                !carrierConfig.getBoolean(
                        CarrierConfigManager.KEY_CARRIER_VOLTE_TTY_SUPPORTED_BOOL)) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            /* tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE); */
        }

        Preference wifiCallingSettings = findPreference(
                getResources().getString(R.string.wifi_calling_settings_key));

        final PhoneAccountHandle simCallManager = mTelecomManager.getSimCallManager();
        if (simCallManager != null) {
            Intent intent = PhoneAccountSettingsFragment.buildPhoneAccountConfigureIntent(
                    this, simCallManager);
            if (intent != null) {
                PackageManager pm = mPhone.getContext().getPackageManager();
                List<ResolveInfo> resolutions = pm.queryIntentActivities(intent, 0);
                if (!resolutions.isEmpty()) {
                    wifiCallingSettings.setTitle(resolutions.get(0).loadLabel(pm));
                    wifiCallingSettings.setSummary(null);
                    wifiCallingSettings.setIntent(intent);
                } else {
                    prefSet.removePreference(wifiCallingSettings);
                }
            } else {
                prefSet.removePreference(wifiCallingSettings);
            }
        } else if (!ImsManager.isWfcEnabledByPlatform(mPhone.getContext())) {
            prefSet.removePreference(wifiCallingSettings);
        } else {
            int resId = com.android.internal.R.string.wifi_calling_off_summary;
            if (ImsManager.isWfcEnabledByUser(mPhone.getContext())) {
                int wfcMode = ImsManager.getWfcMode(mPhone.getContext());
                switch (wfcMode) {
                    case ImsConfig.WfcModeFeatureValueConstants.WIFI_ONLY:
                        resId = com.android.internal.R.string.wfc_mode_wifi_only_summary;
                        break;
                    case ImsConfig.WfcModeFeatureValueConstants.CELLULAR_PREFERRED:
                        resId = com.android.internal.R.string.wfc_mode_cellular_preferred_summary;
                        break;
                    case ImsConfig.WfcModeFeatureValueConstants.WIFI_PREFERRED:
                        resId = com.android.internal.R.string.wfc_mode_wifi_preferred_summary;
                        break;
                    default:
                        if (DBG) log("Unexpected WFC mode value: " + wfcMode);
                }
            }
            wifiCallingSettings.setSummary(resId);
        }

      //[BUGFIX]-Add-BEGIN by TCTNB.Dandan.Fang,11/01/2016,DEFECT3278027,
      //[Call settings] "IMS settings" should be hidden when operator not support volte.
        Preference imsSettinsButton = prefSet.findPreference(BUTTON_IMS_SETTINGS_KEY);
        if(imsSettinsButton != null){
            /*if (!(ImsManager.isVolteEnabledByPlatform(this)&& ImsManager.isVolteProvisionedOnDevice(this))
                    || (mPhone.getImsPhone() == null)) {*/
                 prefSet.removePreference(imsSettinsButton);
            /* }*/
        }
        //[BUGFIX]-Add-END by TCTNB.Dandan.Fang
    }

    @Override
    protected void onNewIntent(Intent newIntent) {
        setIntent(newIntent);

        mSubscriptionInfoHelper = new SubscriptionInfoHelper(this, getIntent());
        mSubscriptionInfoHelper.setActionBarTitle(
                getActionBar(), getResources(), R.string.call_settings_with_label);
        mPhone = mSubscriptionInfoHelper.getPhone();
    }

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {  // See ActionBar#setDisplayHomeAsUpEnabled()
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Finish current Activity and go up to the top level Settings ({@link CallFeaturesSetting}).
     * This is useful for implementing "HomeAsUp" capability for second-level Settings.
     */
    public static void goUpToTopLevelSetting(
            Activity activity, SubscriptionInfoHelper subscriptionInfoHelper) {
 	   TelephonyManager telephonyManager =
               (TelephonyManager) PhoneGlobals.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
	   Intent intent;
	   if(telephonyManager.isMultiSimEnabled()) {
	        intent = subscriptionInfoHelper.getIntent(MSimCallFeaturesSetting.class);    
	   } else {
	        intent = subscriptionInfoHelper.getIntent(CallFeaturesSetting.class);    		   
	   }
        intent.setAction(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.finish();
    }

    //[SOLUTION]-Add-BEGIN by TCTNB.(Chuanjun Chen), 08/31/2016, SOLUTION- 2499549 And TASk-2781362
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
    //[SOLUTION]-Add-END by TCTNB.(Chuanjun Chen)
}
