package com.monster.market.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.monster.market.R;

import mst.app.dialog.AlertDialog;
import mst.preference.Preference;
import mst.preference.PreferenceActivity;
import mst.preference.PreferenceManager;
import mst.preference.PreferenceScreen;
import mst.preference.SwitchPreference;
import mst.widget.toolbar.Toolbar;

/**
 * Created by xiaobin on 16-8-17.
 */
public class UpdateSettingsPreferenceActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    public final static String WIFI_AUTO_UPGRADE_KEY = "wifi_auto_upgrade_key";
    public final static String SOFTWARE_AUTO_UPDATE_TIP_KEY = "software_auto_update_tip_key";
    public final static String APPS_UPDATE_IGNORED_KEY = "apps_update_ignored_key";

    private SwitchPreference mWifiAutoUpgradePref;
    private SwitchPreference mSoftwareAutoUpdateTipPref;
    private PreferenceScreen mAppsUpdateIgnoredPref;

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        addPreferencesFromResource(R.xml.update_settings_prefs);

        initViews();
    }

    private void initViews() {

        Toolbar mToolbar = getToolbar();
        mToolbar.setTitle(getString(R.string.update_settings_pref));

        mWifiAutoUpgradePref = (SwitchPreference) findPreference(WIFI_AUTO_UPGRADE_KEY);
        mSoftwareAutoUpdateTipPref = (SwitchPreference) findPreference(SOFTWARE_AUTO_UPDATE_TIP_KEY);
        mAppsUpdateIgnoredPref = (PreferenceScreen) findPreference(APPS_UPDATE_IGNORED_KEY);

        mWifiAutoUpgradePref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (APPS_UPDATE_IGNORED_KEY.equals(preference.getKey())) {
            Intent ignore = new Intent(this, AppIgnoreActivity.class);
            startActivity(ignore);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        boolean changed = (Boolean) value;
        if (preference.getKey().equals(WIFI_AUTO_UPGRADE_KEY)) {
            if (!mWifiAutoUpgradePref.isChecked() && changed) {
                mWifiAutoUpgradePref.setChecked(true);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.wifi_auto_upgrade_open_tip));
                builder.setPositiveButton(getString(R.string.dialog_confirm),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                mWifiAutoUpgradePref.setChecked(true);
                            }
                        });
                builder.setNegativeButton(getString(R.string.dialog_cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                mWifiAutoUpgradePref.setChecked(false);
                            }
                        });

                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface arg0) {
                        mWifiAutoUpgradePref.setChecked(false);
                    }
                });
                builder.show();
            } else {
                mWifiAutoUpgradePref.setChecked(false);
            }
        }
        return true;
    }

    public static boolean getPreferenceValue(final Context pContext,
                                             String pPrefKey) {
        SharedPreferences mSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(pContext);
        return mSharedPrefs.getBoolean(pPrefKey, false);
    }

    public static boolean getPreferenceValue(final Context pContext,
                                             String pPrefKey, boolean defaultValue) {
        SharedPreferences mSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(pContext);
        return mSharedPrefs.getBoolean(pPrefKey, defaultValue);
    }

}
