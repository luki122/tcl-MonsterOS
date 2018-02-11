package com.monster.market.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.monster.market.MarketApplication;
import com.monster.market.R;
import com.monster.market.download.AppDownloadService;
import com.monster.market.download.DownloadUpdateListener;
import com.monster.market.utils.ApkUtil;
import com.monster.market.utils.SettingUtil;
import com.monster.market.views.NumPreference;

import mst.preference.Preference;
import mst.preference.PreferenceActivity;
import mst.preference.PreferenceScreen;
import mst.widget.toolbar.Toolbar;

/**
 * Created by xiaobin on 16-8-17.
 */
public class ManagerPreferenceActivity extends PreferenceActivity {

    private final static String APP_UPDATE_KEY = "app_update_check_key";
    private final static String DOWNLOAD_MANAGER_KEY = "download_manager_key";
    private final static String APP_MANAGER_KEY = "app_manager_key";
    private final static String SETTINGS_KEY = "settings_key";

    private NumPreference mAppUpdateCheckPref;
    private NumPreference mDownloadManagerPref;
    private PreferenceScreen mAppManagerPref;
    private PreferenceScreen mSettingsPref;
    private NumPreference testPref;

    private int mUpdateCount;
    private int downloadCount;

    private boolean stopFlag = false;
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        addPreferencesFromResource(R.xml.market_manager_prefs);

        initViews();
        initData();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (stopFlag) {
            updateListener.downloadProgressUpdate();
            stopFlag = false;

            mUpdateCount = SettingUtil.getLastUpdateAppCount(this);
            mAppUpdateCheckPref.setDisUpSum(mUpdateCount);
        }
        AppDownloadService.registerUpdateListener(updateListener);

        if (MarketApplication.appUpgradeNeedCheck) {
            ApkUtil.checkUpdateApp(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopFlag = true;
        AppDownloadService.unRegisterUpdateListener(updateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateListener.downloadProgressUpdate();
                    isFirst = false;
                }
            }, 50);
        }
    }

    private void initViews() {

        Toolbar mToolbar = getToolbar();
        mToolbar.setTitle(getString(R.string.market_manager_page));

        mAppUpdateCheckPref = (NumPreference) findPreference(APP_UPDATE_KEY);
        mDownloadManagerPref = (NumPreference) findPreference(DOWNLOAD_MANAGER_KEY);
        mAppManagerPref = (PreferenceScreen) findPreference(APP_MANAGER_KEY);
        mSettingsPref = (PreferenceScreen) findPreference(SETTINGS_KEY);

        mUpdateCount = SettingUtil.getLastUpdateAppCount(this);
        downloadCount = AppDownloadService.getDownloadingCountMore();

        mAppUpdateCheckPref.setSum(mUpdateCount);
        mDownloadManagerPref.setSum(downloadCount);
    }

    private void initData() {

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (APP_UPDATE_KEY.equals(preference.getKey())) {
            Intent upgrade = new Intent(this, AppUpgradeActivity.class);
            startActivity(upgrade);

        } else if (APP_UPDATE_KEY.equals(preference.getKey())) {

        } else if (DOWNLOAD_MANAGER_KEY.equals(preference.getKey())) {

            Intent dInt = new Intent(this, DownloadManagerActivity.class);
            startActivity(dInt);

        } else if (APP_MANAGER_KEY.equals(preference.getKey())) {

            startActivity(new Intent(android.app.monster.MulwareProviderHelp.ACTION_APP_LIST));

        } else if (SETTINGS_KEY.equals(preference.getKey())) {
            Intent setting = new Intent(ManagerPreferenceActivity.this, SettingPreferenceActivity.class);
            startActivity(setting);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub

        }

    };

    private DownloadUpdateListener updateListener = new DownloadUpdateListener() {
        @Override
        public void downloadProgressUpdate() {
            int count = AppDownloadService.getDownloadingCountMore();
            if (downloadCount != count) {
                downloadCount = count;
                mDownloadManagerPref.setDisUpSum(downloadCount);
            }

            int updateCount = SettingUtil.getLastUpdateAppCount(ManagerPreferenceActivity.this);
            if (updateCount != mUpdateCount) {
                mUpdateCount = updateCount;
                mAppUpdateCheckPref.setDisUpSum(mUpdateCount);
            }

        }
    };

}
