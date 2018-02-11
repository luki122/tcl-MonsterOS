package com.monster.paymentsecurity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.UserHandle;

import com.monster.paymentsecurity.constant.Constant;
import com.monster.paymentsecurity.detection.AccessibilityUtils;
import com.monster.paymentsecurity.tmsdk.TMSDKUpdateService;
import com.monster.paymentsecurity.util.SettingUtil;
import com.monster.paymentsecurity.views.PayListPreference;

import java.util.Set;

import mst.preference.Preference;
import mst.preference.PreferenceActivity;
import mst.preference.PreferenceCategory;
import mst.preference.PreferenceScreen;
import mst.preference.SwitchPreference;
import mst.widget.toolbar.Toolbar;

/**
 * Created by xiaobin on 16-8-17.
 */
public class SettingPreferenceActivity extends PreferenceActivity {

    public static final String TAG = "SettingPreferenceActivity";

    public final static String INSTALL_DETECTION = "install_detection";
    public final static String WHITE_LIST = "white_list";
    public final static String PAY_LIST_CATEGORY = "pay_list_category";
    public final static String PAY_APP_MONITOR = "pay_app_monitor";
    public final static String PAY_LIST = "pay_list";


    private PackageChangeReceiver mPackageChangeReceiver;
    private VirusLibUpdateReceiver mVirusLibUpdateReceiver;

    private PreferenceCategory mPayListCategory;
    private SwitchPreference mPayAppPref;
    private PayListPreference mPayListPref;
    private SwitchPreference virusPref;


    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        addPreferencesFromResource(R.xml.settings_prefs);
        initViews();
        registerPackageChangeReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterPackageChangeReceiver();
    }

    @SuppressWarnings("deprecation")
    private void initViews() {
        Toolbar mToolbar = getToolbar();
        mToolbar.setTitle(getString(R.string.settings_pref));

        PreferenceScreen mWhiteListPref = (PreferenceScreen) findPreference(WHITE_LIST);
        mPayListCategory = (PreferenceCategory) findPreference(PAY_LIST_CATEGORY);
        mPayAppPref = (SwitchPreference) findPreference(PAY_APP_MONITOR);
        mPayListPref = (PayListPreference) findPreference(PAY_LIST);
//        SwitchPreference mInstallDetectionPref = (SwitchPreference) findPreference(INSTALL_DETECTION);
        virusPref = (SwitchPreference) findPreference(Constant.SP_UPDATE_VIRUS_LIB);
        virusPref.setSummary(SettingUtil.getVirusLibUpdateTime(this));

        Intent whiteListIntent = new Intent(this, WhiteListActivity.class);
        mWhiteListPref.setIntent(whiteListIntent);

    }

    @SuppressWarnings("deprecation")
    private void initData() {
//        mPayAppPref.setChecked(isPayAppDetectionEnable());

        if (!mPayAppPref.isChecked()) {
            getPreferenceScreen().removePreference(mPayListCategory);
        }
    }

    private boolean isPayAppDetectionEnable() {
        Set<ComponentName> enabledServices = AccessibilityUtils.getEnabledServicesFromSettings(this);
        String str;
        for (ComponentName componentName : enabledServices) {
            str = componentName.flattenToShortString();
            if (str.equals(Constant.ACCESSIBILITY_NAME)) {
                return true;
            }
        }
        return false;
    }


    @SuppressWarnings("deprecation")
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (Constant.SP_PAY_APP_MONITOR.equals(preference.getKey())) {
            SwitchPreference switchPreference = (SwitchPreference) preference;
            SettingUtil.setPayAppDetectionEnable(this, switchPreference.isChecked());
            if (!switchPreference.isChecked()) {
                getPreferenceScreen().removePreference(mPayListCategory);
            } else {
                getPreferenceScreen().addPreference(mPayListCategory);
            }
            sendBroadcastAsUser(new Intent(Constant.ACTION_PAYENV_CHANGE), UserHandle.CURRENT);
        } else if (Constant.SP_INSTALL_DETECTION.equals(preference.getKey())) {
            SwitchPreference switchPreference = (SwitchPreference) preference;
            SettingUtil.setInstallDetectionEnable(this, switchPreference.isChecked());
        } else if (Constant.SP_UPDATE_VIRUS_LIB.equals(preference.getKey())){
            SwitchPreference switchPreference = (SwitchPreference) preference;
            boolean isChecked = switchPreference.isChecked();
            if (isChecked) {
                Intent serviceIntent = new Intent(this, TMSDKUpdateService.class);
                startServiceAsUser(serviceIntent, UserHandle.CURRENT);
            }
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }


    private void registerPackageChangeReceiver() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Constant.ACTION_APP_CHANGE);
        mPackageChangeReceiver = new PackageChangeReceiver();
        registerReceiver(mPackageChangeReceiver, mIntentFilter);

        //病毒库更新
        IntentFilter filter = new IntentFilter(Constant.ACTION_VIRUS_LIB_CHANGE);
        mVirusLibUpdateReceiver = new VirusLibUpdateReceiver();
        registerReceiver(mVirusLibUpdateReceiver, filter);
    }


    private void unregisterPackageChangeReceiver() {
        if (mPackageChangeReceiver != null) {
            unregisterReceiver(mPackageChangeReceiver);
            mPackageChangeReceiver = null;
        }
        if (null != mVirusLibUpdateReceiver){
            unregisterReceiver(mVirusLibUpdateReceiver);
            mVirusLibUpdateReceiver = null;
        }
    }

    private class PackageChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Constant.ACTION_APP_CHANGE.equals(action)) {
                mPayListPref.refresh();
            }
        }
    }

    private class VirusLibUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constant.ACTION_VIRUS_LIB_CHANGE.equals(intent.getAction())){
                virusPref.setSummary(intent.getStringExtra(TMSDKUpdateService.VIRUS_LIB_UPDATE_TIME));
            }
        }
    }

}
