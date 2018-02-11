package com.monster.market.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.monster.market.R;
import com.monster.market.download.AppDownloadService;
import com.monster.market.utils.LogUtil;
import com.monster.market.utils.SettingUtil;
import com.monster.market.utils.SystemUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import mst.preference.Preference;
import mst.preference.PreferenceActivity;
import mst.preference.PreferenceManager;
import mst.preference.PreferenceScreen;
import mst.preference.SwitchPreference;
import mst.widget.toolbar.Toolbar;

/**
 * Created by xiaobin on 16-8-17.
 */
public class SettingPreferenceActivity extends PreferenceActivity {

    public static final String TAG = "SettingPreferenceActivity";

    public final static String UPDATE_SETTINGS_KEY = "update_settings_key";
    public final static String WIFI_DOWNLOAD_KEY = "wifi_download_key";
    public final static String NONE_DOWNLOAD_PIC_KEY = "none_download_pic_key";
    public final static String HOLD_APP_KEY = "hold_app_key";

    private PreferenceScreen mUpdateSettingsPref;
    private SwitchPreference mWifiDownloadPref;
    private SwitchPreference mNoneDownloadPicKey;
    private SwitchPreference mHoldAppKey;

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        addPreferencesFromResource(R.xml.settings_prefs);

        initViews();
    }

    private void initViews() {

        Toolbar mToolbar = getToolbar();
        mToolbar.setTitle(getString(R.string.settings_pref));

        mUpdateSettingsPref = (PreferenceScreen) findPreference(UPDATE_SETTINGS_KEY);
        mWifiDownloadPref = (SwitchPreference) findPreference(WIFI_DOWNLOAD_KEY);
        mNoneDownloadPicKey = (SwitchPreference) findPreference(NONE_DOWNLOAD_PIC_KEY);
        mHoldAppKey = (SwitchPreference) findPreference(HOLD_APP_KEY);

        mWifiDownloadPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                LogUtil.i(TAG, "mWifiDownloadPref onPreferenceClick");

                if (getPreferenceValue(SettingPreferenceActivity.this, WIFI_DOWNLOAD_KEY)) {
                    int status = SystemUtil.getNetStatus(SettingPreferenceActivity.this);
                    if (status == 2) {
                        AppDownloadService.pauseAllDownloads();
                    }
                }

                return false;
            }
        });

        mNoneDownloadPicKey.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                LogUtil.i(TAG, "mNoneDownloadPicKey onPreferenceClick");

                // 更新网络加载图片状态
                if (SettingUtil.isLoadingImage(SettingPreferenceActivity.this)) {
                    ImageLoader.getInstance().denyNetworkDownloads(false);
                } else {
                    ImageLoader.getInstance().denyNetworkDownloads(true);
                }
                return false;
            }
        });
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (UPDATE_SETTINGS_KEY.equals(preference.getKey())) {
            Intent update = new Intent(SettingPreferenceActivity.this, UpdateSettingsPreferenceActivity.class);
            startActivity(update);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public static boolean getPreferenceValue(final Context pContext,
                                             String pPrefKey) {
        SharedPreferences mSharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(pContext);
        return mSharedPrefs.getBoolean(pPrefKey, false);
    }

}
