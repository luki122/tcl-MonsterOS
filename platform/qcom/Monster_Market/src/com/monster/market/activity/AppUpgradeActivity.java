package com.monster.market.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.monster.market.MarketApplication;
import com.monster.market.R;
import com.monster.market.adapter.UpgradeAppAdapter;
import com.monster.market.bean.AppUpgradeInfo;
import com.monster.market.bean.InstalledAppInfo;
import com.monster.market.constants.Constant;
import com.monster.market.constants.WandoujiaDownloadConstant;
import com.monster.market.db.IgnoreAppDao;
import com.monster.market.download.AppDownloadData;
import com.monster.market.download.AppDownloadService;
import com.monster.market.download.AppDownloader;
import com.monster.market.download.DownloadInitListener;
import com.monster.market.download.DownloadUpdateListener;
import com.monster.market.http.DataResponse;
import com.monster.market.http.RequestError;
import com.monster.market.http.RequestHelper;
import com.monster.market.http.data.AppUpgradeInfoRequestData;
import com.monster.market.http.data.AppUpgradeListResultData;
import com.monster.market.install.InstallAppManager;
import com.monster.market.install.InstallNotification;
import com.monster.market.utils.ApkUtil;
import com.monster.market.utils.LoadingPageUtil;
import com.monster.market.utils.LogUtil;
import com.monster.market.utils.SettingUtil;
import com.monster.market.utils.SystemUtil;

import java.util.ArrayList;
import java.util.List;

import mst.app.dialog.AlertDialog;
import mst.widget.MstListView;
import mst.widget.SliderLayout;
import mst.widget.toolbar.Toolbar;

/**
 * Created by xiaobin on 16-8-23.
 */
public class AppUpgradeActivity extends BaseActivity implements View.OnClickListener {

    private RelativeLayout rl_header;
    private TextView tv_count;
    private TextView tv_update_setting;
    private RelativeLayout rl_list;
    private MstListView listView;
    private TextView btn_update_all;
    private TextView tv_no_app;

    private List<AppUpgradeInfo> appList;
    private List<AppDownloadData> downloadDataList;
    private UpgradeAppAdapter adapter;

    private LoadingPageUtil loadingPageUtil;

    private boolean stopFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_app_upgrade);

        MarketApplication.appUpgradeNeedFresh = false;

        appList = new ArrayList<AppUpgradeInfo>();
        downloadDataList = new ArrayList<AppDownloadData>();
        adapter = new UpgradeAppAdapter(this, appList, downloadDataList);

        initViews();
        AppDownloadService.checkInit(this, new DownloadInitListener() {

            @Override
            public void onFinishInit() {
                initData();
            }
        });

        InstallNotification.cancelUpdateNotify();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (stopFlag) {
            updateListener.downloadProgressUpdate();
            stopFlag = false;
            listView.postInvalidate();
        }
        AppDownloadService.registerUpdateListener(updateListener);

        if (MarketApplication.appUpgradeNeedFresh) {
            appList.clear();
            downloadDataList.clear();
            adapter.notifyDataSetChanged();

            loadingPageUtil.showLoadPage();
            loadingPageUtil.showLoading();

            initData();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        MarketApplication.appUpgradeNeedFresh = false;
        stopFlag = true;
        AppDownloadService.unRegisterUpdateListener(updateListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (loadingPageUtil != null) {
            loadingPageUtil.exit();
        }
    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }

    @Override
    public void initViews() {
        mToolbar = getToolbar();
        mToolbar.setTitle(getString(R.string.app_update_check_pref));
        mToolbar.inflateMenu(R.menu.toolbar_action_update_button);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getTitle().equals("download")) {
                    Intent i = new Intent(AppUpgradeActivity.this, DownloadManagerActivity.class);
                    startActivity(i);
                }
                return false;
            }
        });

        rl_header = (RelativeLayout) findViewById(R.id.rl_header);
        tv_count = (TextView) findViewById(R.id.tv_count);
        tv_update_setting = (TextView) findViewById(R.id.tv_update_setting);
        rl_list = (RelativeLayout) findViewById(R.id.rl_list);
        listView = (MstListView) findViewById(R.id.listView);
        btn_update_all = (TextView) findViewById(R.id.btn_update_all);
        tv_no_app = (TextView) findViewById(R.id.tv_no_app);

        listView.setAdapter(adapter);

        initLoadingPage();

        tv_update_setting.setOnClickListener(this);
        btn_update_all.setOnClickListener(this);

        tv_count.setText(getString(R.string.can_update_count, appList.size()));
    }

    @Override
    public void initData() {
        new Thread() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                final List<AppUpgradeInfoRequestData> infoList = ApkUtil.getUpgradeList(AppUpgradeActivity.this);
                long end = System.currentTimeMillis();
                LogUtil.i(TAG, "getUpgradeList time: " + (end - start));
                LogUtil.i(TAG, infoList.toString());
                runOnUiThread(new Thread() {
                    @Override
                    public void run() {
                        RequestHelper.getAppUpdateList(AppUpgradeActivity.this, infoList,
                                new DataResponse<AppUpgradeListResultData>() {
                                    @Override
                                    public void onResponse(AppUpgradeListResultData value) {

                                        if (value.getAppList() != null) {
                                            SettingUtil.setLastUpdateAppCount(AppUpgradeActivity.this,
                                                    value.getAppList().size());
                                        }

                                        if (value.getAppList().size() > 0) {
                                            appList.addAll(value.getAppList());
                                            List<AppDownloadData> tempList = new ArrayList<AppDownloadData>();
                                            for (AppUpgradeInfo info : value.getAppList()) {
                                                AppDownloadData data = SystemUtil.buildAppDownloadData(info);
                                                data.setPos(WandoujiaDownloadConstant.POS_UPDATE);
                                                data.setDownload_type(WandoujiaDownloadConstant.TYPE_UPDATE);
                                                tempList.add(data);
                                            }
                                            downloadDataList.addAll(tempList);

                                            rl_header.setVisibility(View.VISIBLE);
                                            loadingPageUtil.hideLoadPage();
                                            adapter.notifyDataSetChanged();

                                            rl_list.setVisibility(View.VISIBLE);
                                            tv_no_app.setVisibility(View.GONE);

                                        } else {
                                            // no data
                                            rl_header.setVisibility(View.GONE);
                                            loadingPageUtil.hideLoadPage();
                                            rl_list.setVisibility(View.GONE);
                                            tv_no_app.setVisibility(View.VISIBLE);
                                        }

                                        tv_count.setText(getString(R.string.can_update_count, appList.size()));

                                        checkUpdateAllButton();
                                    }

                                    @Override
                                    public void onErrorResponse(RequestError error) {
                                        if (error.getErrorType() == RequestError.ERROR_NO_NETWORK) {
                                            loadingPageUtil.showNoNetWork();
                                        } else {
                                            loadingPageUtil.showNetworkError();
                                        }
                                    }
                                });
                    }
                });
            }
        }.start();

        // 更新右上角显示
        if (AppDownloadService.getDownloadingCountMore() > 0) {
            mToolbar.getMenu().findItem(R.id.menu_download)
                    .setIcon(R.drawable.toolbar_download_message_normal);
        } else {
            mToolbar.getMenu().findItem(R.id.menu_download)
                    .setIcon(R.drawable.toolbar_download_normal);
        }
    }

    private void initLoadingPage() {
        loadingPageUtil = new LoadingPageUtil();
        loadingPageUtil.init(this, findViewById(R.id.frameLayout));
        loadingPageUtil.setOnRetryListener(new LoadingPageUtil.OnRetryListener() {
            @Override
            public void retry() {
                initData();
            }
        });
        loadingPageUtil.setOnShowListener(new LoadingPageUtil.OnShowListener() {
            @Override
            public void onShow() {
//				mListView.setVisibility(View.GONE);
            }
        });
        loadingPageUtil.setOnHideListener(new LoadingPageUtil.OnHideListener() {
            @Override
            public void onHide() {
//				mListView.setVisibility(View.VISIBLE);
            }
        });
        loadingPageUtil.showLoadPage();
        loadingPageUtil.showLoading();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_update_setting:
                Intent updateSetting = new Intent(AppUpgradeActivity.this,
                        UpdateSettingsPreferenceActivity.class);
                startActivity(updateSetting);
                break;
            case R.id.btn_update_all:

                if (!SettingUtil.canDownload(AppUpgradeActivity.this)) {
                    AlertDialog mWifiConDialog = new AlertDialog.Builder(AppUpgradeActivity.this)
                            .setTitle(AppUpgradeActivity.this.getResources().getString(
                                            R.string.dialog_prompt))
                            .setMessage(AppUpgradeActivity.this.getResources().getString(
                                            R.string.no_wifi_download_message))
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {

                                            SharedPreferences sp = PreferenceManager
                                                    .getDefaultSharedPreferences(AppUpgradeActivity.this);
                                            SharedPreferences.Editor ed = sp.edit();
                                            ed.putBoolean(Constant.SP_WIFI_DOWNLOAD_KEY, false);
                                            ed.commit();

                                            // 开始全部更新
                                            if (downloadDataList != null && downloadDataList.size() > 0) {
                                                for (AppDownloadData data : downloadDataList) {
                                                    InstalledAppInfo appInfo = InstallAppManager.
                                                            getInstalledAppInfo(AppUpgradeActivity.this, data.getPackageName());
                                                    if (appInfo != null && appInfo.getVersionCode() >= data.getVersionCode()) {
                                                        continue;
                                                    }
                                                    AppDownloadService.startDownload(AppUpgradeActivity.this, data);
                                                }
                                            }

                                            btn_update_all.setBackgroundResource(R.drawable.button_weak_normal);
                                            btn_update_all.setTextColor(getResources().getColor(android.R.color.darker_gray));
                                            btn_update_all.setEnabled(false);
                                        }

                                    }).create();
                    mWifiConDialog.show();

                } else if (!SystemUtil.hasNetwork()) {
                    Toast.makeText(AppUpgradeActivity.this, AppUpgradeActivity.this
                            .getString(R.string.no_network_download_toast), Toast.LENGTH_SHORT).show();
                } else {

                    // 开始全部更新
                    if (downloadDataList != null && downloadDataList.size() > 0) {
                        for (AppDownloadData data : downloadDataList) {
                            InstalledAppInfo appInfo = InstallAppManager.
                                    getInstalledAppInfo(AppUpgradeActivity.this, data.getPackageName());
                            if (appInfo != null && appInfo.getVersionCode() >= data.getVersionCode()) {
                                continue;
                            }
                            AppDownloadService.startDownload(AppUpgradeActivity.this, data);
                        }
                    }

                    btn_update_all.setBackgroundResource(R.drawable.button_weak_normal);
                    btn_update_all.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    btn_update_all.setEnabled(false);
                }

                break;
        }
    }

    private DownloadUpdateListener updateListener = new DownloadUpdateListener() {
        @Override
        public void downloadProgressUpdate() {
            if (adapter != null) {
                adapter.updateView(listView);
            }

            // 更新右上角显示
            if (AppDownloadService.getDownloadingCountMore() > 0) {
                mToolbar.getMenu().findItem(R.id.menu_download)
                        .setIcon(R.drawable.toolbar_download_message_normal);
            } else {
                mToolbar.getMenu().findItem(R.id.menu_download)
                        .setIcon(R.drawable.toolbar_download_normal);
            }
        }
    };

    public void ignoreApp(AppUpgradeInfo info, AppDownloadData downloadData) {
        IgnoreAppDao ignoreAppDao = new IgnoreAppDao(this);
        ignoreAppDao.open();
        ignoreAppDao.insert(info);
        ignoreAppDao.close();

        appList.remove(info);
        downloadDataList.remove(downloadData);
        adapter.notifyDataSetChanged();

        closeSliderView(listView);

        if (appList.size() > 0) {
            tv_no_app.setVisibility(View.GONE);
        } else {
            rl_list.setVisibility(View.GONE);
            tv_no_app.setVisibility(View.VISIBLE);
        }

        int updateCount = SettingUtil.getLastUpdateAppCount(this);
        SettingUtil.setLastUpdateAppCount(this, (updateCount - 1));

        tv_count.setText(getString(R.string.can_update_count, appList.size()));

        if (appList.size() == 0) {
            rl_header.setVisibility(View.GONE);
        }
    }

    private void closeSliderView(MstListView listView) {
        if (listView == null) {
            return;
        }

        int count = listView.getChildCount();

        for (int i = 0; i < count; i++) {
            View view = listView.getChildAt(i);
            SliderLayout sliderLayout = (SliderLayout) view.findViewById(com.mst.R.id.slider_view);
            if (sliderLayout != null && sliderLayout.isOpened()) {
                sliderLayout.close(false);
            }
        }
    }

    private void checkUpdateAllButton() {
        // 检查一键更新按钮的状态
        boolean enable = false;
        for (AppDownloadData data : downloadDataList) {
            if (!AppDownloadService.getDownloaders().containsKey(data.getTaskId())) {
                enable = true;
                break;
            } else {
                AppDownloader downloader = AppDownloadService.getDownloaders().get(data.getTaskId());
                if (downloader != null) {
                    int status = downloader.getStatus();
                    if (!(status == AppDownloader.STATUS_WAIT
                            || status == AppDownloader.STATUS_CONNECTING
                            || status == AppDownloader.STATUS_DOWNLOADING
                            || status == AppDownloader.STATUS_CONNECT_RETRY
                            || status == AppDownloader.STATUS_INSTALL_WAIT
                            || status == AppDownloader.STATUS_INSTALLING)) {
                        enable = true;
                        break;
                    }
                }
            }
        }
        if (enable) {
            btn_update_all.setBackgroundResource(R.drawable.button_default_selector);
            btn_update_all.setEnabled(true);
        } else {
            btn_update_all.setBackgroundResource(R.drawable.button_weak_normal);
            btn_update_all.setTextColor(getResources().getColor(android.R.color.darker_gray));
            btn_update_all.setEnabled(false);
        }
    }

}
