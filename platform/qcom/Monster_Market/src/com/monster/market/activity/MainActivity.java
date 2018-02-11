package com.monster.market.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MstSearchView;
import android.widget.TextView;

import com.monster.market.MarketApplication;
import com.monster.market.R;
import com.monster.market.adapter.MainAppListAdapter;
import com.monster.market.bean.AdInfo;
import com.monster.market.bean.AppDetailAnimInfo;
import com.monster.market.bean.AppListInfo;
import com.monster.market.bean.MainAppListInfo;
import com.monster.market.constants.HttpConstant;
import com.monster.market.constants.WandoujiaDownloadConstant;
import com.monster.market.download.AppDownloadData;
import com.monster.market.download.AppDownloadService;
import com.monster.market.download.DownloadInitListener;
import com.monster.market.download.DownloadUpdateListener;
import com.monster.market.http.DataResponse;
import com.monster.market.http.RequestError;
import com.monster.market.http.RequestHelper;
import com.monster.market.http.data.AdListResultData;
import com.monster.market.http.data.AppListResultData;
import com.monster.market.http.data.BannerListResultData;
import com.monster.market.utils.ApkUtil;
import com.monster.market.utils.LoadingPageUtil;
import com.monster.market.utils.ProgressBtnUtil;
import com.monster.market.utils.ScreenUtil;
import com.monster.market.utils.SettingUtil;
import com.monster.market.utils.SystemUtil;
import com.monster.market.views.FrameBannerView;
import com.monster.market.views.ListLoadMoreView;
import com.monster.market.views.MainTabView;

import java.util.ArrayList;
import java.util.List;

import mst.app.dialog.AlertDialog;
import mst.widget.toolbar.Toolbar;

import static com.monster.market.utils.DensityUtil.dip2px;

public class MainActivity extends BaseActivity {

    private MstSearchView search_view;
    private TextView tv_search;
    private MainTabView mainTab;
    private ListView listview;
    private ListLoadMoreView loadMoreView;
    private FrameBannerView bannerView;

    private int showMainTabHeight = -1;
    private boolean startCheck = false;

    private LoadingPageUtil loadingPageUtil;

    // 由于首页需要banner和列表一起显示，所以需要一开始两个数据都加载完
    private boolean loadFirstPage = false;
    private boolean loadBannerView = false;

    private int pageNum = 0;
    private final int pageSize = 10;
    private boolean isLoadDataFinish = false;

    private List<MainAppListInfo> mainAppListInfoList;
    private List<AppListInfo> appList;
    private List<AppDownloadData> downloadDataList;
    private List<AdInfo> adList;
    private MainAppListAdapter adapter;

    // 广告加载的间隔
    private final int adInsertInterval = 5;
    // 当前列表总加载数量(MainAppListInfo List)
    private int addItemCount = 0;
    // 当前广告加载的index
    private int adIndex = 0;

    private boolean stopFlag = false;

    private AlertDialog exitDialog;

    // 上一次点击item项的时间
    private long lastClickItemTime = 0;

    // 是否刷新列表图片(防止回到界面的刷新导致图片闪一下)
    private boolean refreshImage = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainAppListInfoList = new ArrayList<MainAppListInfo>();
        appList = new ArrayList<AppListInfo>();
        adList = new ArrayList<AdInfo>();
        downloadDataList = new ArrayList<AppDownloadData>();
        adapter = new MainAppListAdapter(this, mainAppListInfoList, downloadDataList);

        initViews();
        AppDownloadService.checkInit(this, new DownloadInitListener() {

            @Override
            public void onFinishInit() {
                initData();
            }
        });

		SettingUtil.setWifiBlockAlert(this, true);
		SettingUtil.setWifiBlockAlertOperation(this, false);

		if (MarketApplication.appUpgradeNeedCheck) {
			ApkUtil.checkUpdateApp(this);
		}
	}

    @Override
    protected void onStart() {
        super.onStart();

        if (stopFlag) {
            updateListener.downloadProgressUpdate();
            stopFlag = false;
            listview.postInvalidate();
        }
        AppDownloadService.registerUpdateListener(updateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        stopFlag = true;
        AppDownloadService.unRegisterUpdateListener(updateListener);
    }

    @Override
    protected void onPause() {
        if (bannerView != null) {
            bannerView.stop();
        }

        if (exitDialog != null && exitDialog.isShowing()) {
            exitDialog.dismiss();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        if (bannerView != null && bannerView.getPic_dotype() != 2) {
            bannerView.start();
        }
        super.onResume();

        if (MarketApplication.screenWidth == 0) {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width = (int) (dm.widthPixels * dm.density);
            int height = (int) (dm.heightPixels * dm.density);
            MarketApplication.screenWidth = width;
            MarketApplication.screenHeight = height;
        }

        ProgressBtnUtil.clearProgressBtnTag(listview, R.id.progressBtn);
        refreshImage = false;
        updateListener.downloadProgressUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bannerView != null) {
            if (bannerView.isRunning()) {
                bannerView.stop();
            }
            bannerView.exit();
        }
        if (loadingPageUtil != null) {
            loadingPageUtil.exit();
        }
    }

    @Override
    public void initViews() {

        mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        mainTab = (MainTabView) findViewById(R.id.mainTab);
        listview = (ListView) findViewById(R.id.swipe_target);
        tv_search = (TextView) findViewById(R.id.tv_search);
        View.OnClickListener searchClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(searchIntent);
            }
        };
        tv_search.setOnClickListener(searchClick);
        mToolbar.setNavigationIcon(R.drawable.toolbar_search_normal);
        mToolbar.inflateMenu(R.menu.toolbar_action_button);
        mToolbar.setNavigationOnClickListener(searchClick);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getTitle().equals("setting")) {
                    Intent i = new Intent(MainActivity.this, ManagerPreferenceActivity.class);
                    startActivity(i);
                }
                return false;
            }
        });

        bannerView = new FrameBannerView(this);
        listview.addHeaderView(bannerView);

        loadMoreView = new ListLoadMoreView(this);
        loadMoreView.showNormalProgress();
        listview.addFooterView(loadMoreView);

        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (System.currentTimeMillis() - lastClickItemTime > 1000) {
                    int index = i - listview.getHeaderViewsCount();

                    if (index >= 0 && index < mainAppListInfoList.size()) {
                        MainAppListInfo info = mainAppListInfoList.get(index);
                        if (info.getType() == MainAppListInfo.TYPE_LIST) {
                            Intent intent = new Intent(MainActivity.this, AppDetailActivity.class);
                            intent.putExtra(AppDetailActivity.PACKAGE_NAME, mainAppListInfoList.get(index)
                                    .getAppListInfo().getPackageName());
                            intent.putExtra(AppDetailActivity.REPORT_MODULID, downloadDataList.get(index)
                                    .getReportModulId());
                            intent.putExtra(AppDetailActivity.ANIM_PARAMS, getAppDetailAnimInfo(view));
                            intent.putExtra(AppDetailActivity.ICON_URL, mainAppListInfoList.get(index).getAppListInfo().getBigAppIcon());
                            startActivity(intent);
                            overridePendingTransition(0, 0);

                        } else if (info.getType() == MainAppListInfo.TYPE_AD) {

                            AdInfo adInfo = mainAppListInfoList.get(index).getAdInfo();
                            if (adInfo.getAdType().equals(HttpConstant.AD_TYPE_APP)) {
                                Intent intent = new Intent(MainActivity.this, AppDetailActivity.class);
                                intent.putExtra(AppDetailActivity.PACKAGE_NAME, adInfo.getAdUrl());
                                intent.putExtra(AppDetailActivity.ANIM_PARAMS, getBannerAnimInfo(view));
                                startActivity(intent);
                                overridePendingTransition(0,0);
                            }

                        }

                    }

                    lastClickItemTime = System.currentTimeMillis();
                }
            }
        });

        listview.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    if (listview != null && listview.getChildCount() > 0) {
                        startCheck = true;
                    } else {
                        startCheck = false;
                    }

                    adapter.setLoadImage(false);
                } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    adapter.setLoadImage(true);
                    adapter.notifyDataSetChanged();

                } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    adapter.setLoadImage(false);
                }

                if (isLoadDataFinish)
                    return;

                boolean scrollEnd = false;
                try {
                    if (view.getPositionForView(loadMoreView) == view
                            .getLastVisiblePosition()) {
                        scrollEnd = true;
                    }
                } catch (Exception e) {
                    scrollEnd = false;
                }

                if (scrollEnd && !loadMoreView.isLoading()) {
                    loadMoreView.showProgress();
                    getIndexData(pageNum + 1, pageSize);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                checkTabView(firstVisibleItem);
            }
        });

        loadMoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loadMoreView.getStatus() == ListLoadMoreView.STATUS_ERROR) {
                    loadMoreView.showProgress();
                    getIndexData(pageNum + 1, pageSize);
                }
            }
        });

        initLoadingPage();
    }

    private AppDetailAnimInfo getAppDetailAnimInfo(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        Point point = new Point(location[0], location[1]);

        AppDetailAnimInfo animInfo = new AppDetailAnimInfo();
        animInfo.setLayoutInitHeight(dip2px(this, 80))
                .setLayoutMarginTop(dip2px(this, 156))
                .setIconMarginLeft(dip2px(this, 16))
                .setIconMarginTop(dip2px(this, 14))
                .setInitIconSize(dip2px(this, 52))
                .setFinalIconSize(dip2px(this, 63))
                .setCoordinate(point);
        return animInfo;
    }

    private AppDetailAnimInfo getBannerAnimInfo(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        Point point = new Point(0, ScreenUtil.getScreenHeight(this) - dip2px(this, 151));

        AppDetailAnimInfo animInfo = new AppDetailAnimInfo();
        animInfo.setLayoutInitHeight(dip2px(this, 151))
                .setLayoutMarginTop(dip2px(this, 156))
                .setIconMarginLeft(dip2px(this, 0))
                .setIconMarginTop(dip2px(this, 0))
                .setInitIconSize(dip2px(this, 0))
                .setFinalIconSize(dip2px(this, 0))
                .setCoordinate(point)
                .setType(AppDetailAnimInfo.TYPE_BOTTOM_SLIDE_IN);
        return animInfo;
    }

    @Override
    public void initData() {

        // 先加载广告, 成功后加载列表数据
        RequestHelper.getAdList(this, 0, 10, new DataResponse<AdListResultData>() {

            @Override
            public void onResponse(AdListResultData value) {

                adList.clear();
                adList.addAll(value.getAdList());


                // 开始加载列表数据和banner数据
                loadFirstPage = false;
                loadBannerView = false;
                pageNum = 0;

                getIndexData(pageNum, pageSize);

                // 获取banner
                RequestHelper.getBanner(MainActivity.this, new DataResponse<BannerListResultData>() {
                    @Override
                    public void onResponse(BannerListResultData value) {
                        loadBannerView = true;
                        bannerView.setImages(value.getBannerList());

                        checkAndShowFirstPage();
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

            @Override
            public void onErrorResponse(RequestError error) {
                if (error.getErrorType() == RequestError.ERROR_NO_NETWORK) {
                    loadingPageUtil.showNoNetWork();
                } else {
                    loadingPageUtil.showNetworkError();
                }
            }
        });

        // 更新右上角显示
        if (AppDownloadService.getDownloadingCountMore() > 0 ||
                SettingUtil.getLastUpdateAppCount(MainActivity.this) > 0) {
            mToolbar.getMenu().findItem(R.id.menu_setting)
                    .setIcon(R.drawable.toolbar_setting_message_normal);
        } else {
            mToolbar.getMenu().findItem(R.id.menu_setting)
                    .setIcon(R.drawable.toolbar_setting_normal);
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
                listview.setVisibility(View.GONE);
            }
        });
        loadingPageUtil.setOnHideListener(new LoadingPageUtil.OnHideListener() {
            @Override
            public void onHide() {
                listview.setVisibility(View.VISIBLE);
            }
        });
        loadingPageUtil.showLoadPage();
        loadingPageUtil.showLoading();
    }

    private void getIndexData(final int pNum, final int pSize) {
        RequestHelper.getIndexInfo(this, pNum, pSize, new DataResponse<AppListResultData>() {

            @Override
            public void onResponse(AppListResultData value) {
                loadFirstPage = true;
                pageNum = pNum;

                int size = value.getAppList().size();
                if (size < pageSize)
                    isLoadDataFinish = true;

                appList.addAll(value.getAppList());

                // 加载列表及广告
                MainAppListInfo info;
                for (int i = 0; i < value.getAppList().size(); i++) {
                    info = new MainAppListInfo();
                    info.setType(MainAppListInfo.TYPE_LIST);
                    info.setAppListInfo(value.getAppList().get(i));
                    mainAppListInfoList.add(info);

                    AppDownloadData data = SystemUtil.buildAppDownloadData(value.getAppList().get(i));
                    data.setPos(WandoujiaDownloadConstant.POS_HOMEPAGE);
                    data.setReportModulId(HttpConstant.REPORT_MODULID_HOMEPAGE);
                    downloadDataList.add(data);

                    addItemCount++;
                    if (adIndex < adList.size() && addItemCount % adInsertInterval == 0) {    // 到达间隔值
                        MainAppListInfo adInfo = new MainAppListInfo();
                        adInfo.setType(MainAppListInfo.TYPE_AD);
                        adInfo.setAdInfo(adList.get(adIndex));
                        mainAppListInfoList.add(adInfo);

                        AppDownloadData adData = new AppDownloadData();
                        adData.setTaskId("ad");
                        downloadDataList.add(adData);

                        adIndex++;
                    }
                }

                disView();
            }

            @Override
            public void onErrorResponse(RequestError error) {
                if (pageNum == 0) {
                    loadingPageUtil.showNetworkError();
                    loadMoreView.showError();
                } else {
                    loadMoreView.showError();
                }
            }
        });
    }

    private void checkAndShowFirstPage() {
        if (loadBannerView && loadFirstPage) {
            loadingPageUtil.hideLoadPage();
        }
        showMainTabHeight = bannerView.getLoopBannerHeight();
    }

    private void disView() {
        if (pageNum == 0) {
            adapter.notifyDataSetChanged();
            checkAndShowFirstPage();
        } else {
            adapter.notifyDataSetChanged();
        }

        if (loadMoreView.isLoading()) {
            loadMoreView.showNormalProgress();
        }

        if (isLoadDataFinish) {
            loadMoreView.showFinish();
        }

    }

    private void checkTabView(int firstVisibleItem) {
        if (!startCheck) {
            return;
        }

        if (firstVisibleItem == 0) {
            if (showMainTabHeight == 0) {
                showMainTabHeight = bannerView.getLoopBannerHeight();
            }
            if (-listview.getChildAt(firstVisibleItem).getTop() < (showMainTabHeight)) {
                mainTab.setVisibility(View.GONE);
            } else {
                mainTab.setVisibility(View.VISIBLE);
            }
        } else {
            mainTab.setVisibility(View.VISIBLE);
        }
    }

    private DownloadUpdateListener updateListener = new DownloadUpdateListener() {

        @Override
        public void downloadProgressUpdate() {
            if (adapter != null) {
                adapter.updateView(listview, refreshImage);
                refreshImage = true;
            }

            // 更新右上角显示
            if (AppDownloadService.getDownloadingCountMore() > 0 ||
                    SettingUtil.getLastUpdateAppCount(MainActivity.this) > 0) {
                mToolbar.getMenu().findItem(R.id.menu_setting)
                        .setIcon(R.drawable.toolbar_setting_message_normal);
            } else {
                mToolbar.getMenu().findItem(R.id.menu_setting)
                        .setIcon(R.drawable.toolbar_setting_normal);
            }
        }
    };

    @Override
    public void onBackPressed() {
        int downloadCount = AppDownloadService.getDownloadingCount();
        if (downloadCount > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.exit_comfirm);
            String msg = getString(R.string.exit_tip_message, downloadCount);
            builder.setMessage(msg);
            builder.setNegativeButton(R.string.dialog_cancel, null);
            builder.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    AppDownloadService.pauseAllDownloads();
                    finish();
                }
            });
            exitDialog = builder.create();
            exitDialog.setCanceledOnTouchOutside(false);
            exitDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    exitDialog = null;
                }
            });
            exitDialog.show();
            return;
        }

        super.onBackPressed();
    }
}