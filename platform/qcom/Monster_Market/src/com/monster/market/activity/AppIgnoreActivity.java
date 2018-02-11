package com.monster.market.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.monster.market.MarketApplication;
import com.monster.market.R;
import com.monster.market.adapter.IgnoreAppAdapter;
import com.monster.market.bean.AppUpgradeInfo;
import com.monster.market.db.IgnoreAppDao;
import com.monster.market.download.AppDownloadData;
import com.monster.market.utils.LoadingPageUtil;
import com.monster.market.utils.SettingUtil;
import com.monster.market.utils.SystemUtil;

import java.util.ArrayList;
import java.util.List;

import mst.app.dialog.AlertDialog;
import mst.widget.MstListView;

/**
 * Created by xiaobin on 16-8-24.
 */
public class AppIgnoreActivity extends BaseActivity {

    private RelativeLayout rl_header;
    private TextView tv_count;
    private TextView tv_recovery_all;
    private MstListView listView;
    private LinearLayout ll_no_app;

    private List<AppUpgradeInfo> appList;
    private IgnoreAppAdapter adapter;

    private LoadingPageUtil loadingPageUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_app_ignore);

        appList = new ArrayList<AppUpgradeInfo>();
        adapter = new IgnoreAppAdapter(this, appList);

        initViews();
        initData();
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
        mToolbar.setTitle(getString(R.string.apps_update_ignored));

        rl_header = (RelativeLayout) findViewById(R.id.rl_header);
        tv_count = (TextView) findViewById(R.id.tv_count);
        tv_recovery_all = (TextView) findViewById(R.id.tv_recovery_all);
        listView = (MstListView) findViewById(R.id.listView);
        ll_no_app = (LinearLayout) findViewById(R.id.ll_no_app);

        listView.setAdapter(adapter);

        tv_recovery_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AppIgnoreActivity.this);
                builder.setTitle(R.string.dialog_prompt);
                builder.setMessage(R.string.recovery_all_tip);
                builder.setNegativeButton(R.string.dialog_cancel, null);
                builder.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        recoveryAll();
                    }
                });
                builder.create().show();
            }
        });

        initLoadingPage();
    }

    @Override
    public void initData() {
        new Thread() {
            @Override
            public void run() {
                IgnoreAppDao ignoreAppDao = new IgnoreAppDao(AppIgnoreActivity.this);
                ignoreAppDao.open();
                final List<AppUpgradeInfo> data = ignoreAppDao.queryAllData();
                ignoreAppDao.close();

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (data != null && data.size() > 0) {
                            appList.addAll(data);

                            tv_count.setText(String.format(getString(R.string.ignore_count), data.size()));

                            loadingPageUtil.hideLoadPage();
                            rl_header.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        } else {
                            // no data
                            loadingPageUtil.hideLoadPage();
                            rl_header.setVisibility(View.GONE);
                            listView.setVisibility(View.GONE);
                            ll_no_app.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }.start();
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

    public void recoveryUpdate(AppUpgradeInfo info) {
        IgnoreAppDao ignoreAppDao = new IgnoreAppDao(this);
        ignoreAppDao.open();
        ignoreAppDao.deleteDataById(info.getPackageName());
        ignoreAppDao.close();

        appList.remove(info);
        adapter.notifyDataSetChanged();

        if (appList.size() == 0) {
            // no data
            loadingPageUtil.hideLoadPage();
            listView.setVisibility(View.GONE);
            ll_no_app.setVisibility(View.VISIBLE);
            rl_header.setVisibility(View.GONE);
        } else {
            rl_header.setVisibility(View.VISIBLE);
            tv_count.setText(String.format(getString(R.string.ignore_count), appList.size()));
        }

        int updateCount = SettingUtil.getLastUpdateAppCount(this);
        SettingUtil.setLastUpdateAppCount(this, (updateCount + 1));

        // 需要刷新APP更新页
        MarketApplication.appUpgradeNeedFresh = true;
    }

    public void recoveryAll() {
        IgnoreAppDao ignoreAppDao = new IgnoreAppDao(this);
        ignoreAppDao.open();
        int count = ignoreAppDao.getCount();
        ignoreAppDao.deleteAll();
        ignoreAppDao.close();

        appList.clear();
        adapter.notifyDataSetChanged();

        // no data
        rl_header.setVisibility(View.GONE);
        loadingPageUtil.hideLoadPage();
        listView.setVisibility(View.GONE);
        ll_no_app.setVisibility(View.VISIBLE);

        int updateCount = SettingUtil.getLastUpdateAppCount(this);
        SettingUtil.setLastUpdateAppCount(this, (updateCount + count));

        // 需要刷新APP更新页
        MarketApplication.appUpgradeNeedFresh = true;
    }

}
