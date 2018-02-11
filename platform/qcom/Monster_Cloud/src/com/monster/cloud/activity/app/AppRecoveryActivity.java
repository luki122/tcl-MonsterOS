package com.monster.cloud.activity.app;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.monster.cloud.R;
import com.monster.cloud.activity.BaseActivity;
import com.monster.cloud.adpater.AppRecoveryAdapter;
import com.monster.cloud.bean.RecoveryAppInfo;
import com.monster.cloud.bean.RecoveryAppItem;
import com.monster.cloud.http.DataResponse;
import com.monster.cloud.http.RequestError;
import com.monster.cloud.http.RequestHelper;
import com.monster.cloud.http.data.CloudAppRecoveryAppInfoRequestData;
import com.monster.cloud.http.data.CloudAppRecoveryResultData;
import com.monster.cloud.utils.LoadingPageUtil;
import com.monster.cloud.utils.LogUtil;
import com.monster.market.download.AppDownloadData;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.tencent.qqpim.softbox.SoftBoxProtocolModel;
import com.tencent.software.AppInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mst.widget.recycleview.GridLayoutManager;
import mst.widget.recycleview.RecyclerView;

/**
 * Created by xiaobin on 16-10-24.
 */
public class AppRecoveryActivity extends BaseActivity {

    public static final String START_TYPE = "start_type";
    public static final String RESULT_RECOVERY_APP_LIST_KEY = "recovery_app_list";
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_CHOOSE_APP = 1;

    private int openType = TYPE_NORMAL;

    private RecyclerView recyclerView;
    private Button btn_recovery;

    private List<AppInfo> list = new ArrayList<AppInfo>();
    private List<RecoveryAppItem> itemList;
    private AppRecoveryAdapter adapter;
    private int recoveryAppCount = 0;
    private int recommendAppCount = 0;

    private LoadingPageUtil loadingPageUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_app_recovery);

        itemList = new ArrayList<RecoveryAppItem>();
        adapter = new AppRecoveryAdapter(this, itemList);

        getIntentData();
        initViews();
        initData();
    }

    private void getIntentData(){
        Intent i = getIntent();
        if (i != null) {
            openType = i.getIntExtra(START_TYPE, TYPE_NORMAL);
        }
    }

    @Override
    public void initViews() {

        mToolbar = getToolbar();
        mToolbar.setTitle(R.string.app_recovery_title);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        btn_recovery = (Button) findViewById(R.id.btn_recovery);

        int spacing = getResources()
                .getDimensionPixelOffset(R.dimen.app_recovery_item_left_right);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacing));
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.isHeader(position) ? gridLayoutManager.getSpanCount() : 1;
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new AppRecoveryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (!adapter.isHeader(position)) {
                    if (!adapter.getSelectSet().contains(position)) {
                        adapter.getSelectSet().add(position);
                    } else {
                        adapter.getSelectSet().remove(position);
                    }
                    adapter.notifyItemChanged(position);
                    updateSelectCountText();
                }
            }
        });

        btn_recovery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter.getSelectSet().size() > 0) {

                    if (openType == TYPE_NORMAL) {
                        Intent sIntent = new Intent();
                        sIntent.setComponent(new ComponentName("com.monster.market",
                                "com.monster.market.download.AppDownloadService"));
                        Bundle startDownloadBundle = new Bundle();
                        startDownloadBundle.putInt("download_operation", 109);

                        ArrayList<AppDownloadData> listData = buildAppDownloadDataList();
                        startDownloadBundle.putParcelableArrayList("download_data_list", listData);

                        sIntent.putExtras(startDownloadBundle);
                        startService(sIntent);

                        Intent managerIntent = new Intent("com.monster.market.downloadmanager");
                        startActivity(managerIntent);

                        finish();
                    } else if (openType == TYPE_CHOOSE_APP) {
                        Intent resultIntent = new Intent();
                        Bundle resultBundle = new Bundle();
                        ArrayList<AppDownloadData> listData = buildAppDownloadDataList();
                        resultBundle.putParcelableArrayList(RESULT_RECOVERY_APP_LIST_KEY, listData);
                        resultIntent.putExtras(resultBundle);

                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }


                } else {
                    Toast.makeText(AppRecoveryActivity.this, "请勾选需要恢复的应用", Toast.LENGTH_SHORT).show();
                }
            }
        });

        initLoadingPage();
        updateSelectCountText();
    }

    @Override
    public void initData() {

        new AppRecoveryTask().execute();

    }

    @Override
    public void onNavigationClicked(View view) {
        super.onNavigationClicked(view);
        finish();
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
                recyclerView.setVisibility(View.GONE);
            }
        });
        loadingPageUtil.setOnHideListener(new LoadingPageUtil.OnHideListener() {
            @Override
            public void onHide() {
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        loadingPageUtil.showLoadPage();
        loadingPageUtil.showLoading();
    }

    public void selectAllRecoveryApp() {
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getType() == RecoveryAppItem.ITEM_VIEW_TYPE_ITEM
                    && itemList.get(i).getAppType() == RecoveryAppItem.ITEM_VIEW_APP_TYPE_RECOVERY) {
                if (!adapter.getSelectSet().contains(i)) {
                    adapter.getSelectSet().add(i);
                    adapter.notifyItemChanged(i);
                }
            }
        }
        updateSelectCountText();
    }

    public void selectAllRecommendApp() {
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getType() == RecoveryAppItem.ITEM_VIEW_TYPE_ITEM
                    && itemList.get(i).getAppType() == RecoveryAppItem.ITEM_VIEW_APP_TYPE_RECOMMEND) {
                if (!adapter.getSelectSet().contains(i)) {
                    adapter.getSelectSet().add(i);
                    adapter.notifyItemChanged(i);
                }
            }
        }
        updateSelectCountText();
    }

    public void updateSelectCountText() {
        int count = adapter.getSelectSet().size();
        if (openType == TYPE_NORMAL) {
            String text = getString(R.string.app_recovery_immediately, count);
            btn_recovery.setText(text);
        } else if (openType == TYPE_CHOOSE_APP) {
            String text = getString(R.string.app_recovery_ok_with_num, count);
            btn_recovery.setText(text);
        }
    }

    private ArrayList<AppDownloadData> buildAppDownloadDataList() {
        ArrayList<AppDownloadData> result = new ArrayList<AppDownloadData>();
        AppDownloadData downloadData = null;
        Set<Integer> selectSet = adapter.getSelectSet();
        for (int e : selectSet) {
            RecoveryAppItem item = itemList.get(e);
            if (item.getAppInfo() != null) {
                downloadData = buildAppDownloadData(item.getAppInfo());
                result.add(downloadData);
            }
        }

        return result;
    }

    private String buildDownloadTaskId(String packageName, int versionCode) {
        return packageName + "_" + versionCode;
    }

    private AppDownloadData buildAppDownloadData(RecoveryAppInfo info) {
        if (info != null) {
            AppDownloadData tmp_data = new AppDownloadData();
            tmp_data.setTaskId(buildDownloadTaskId(info.getPackageName(), info.getVersionCode()));
            tmp_data.setApkId(info.getAppId());
            tmp_data.setApkDownloadPath(info.getDownloadUrl());
            tmp_data.setApkLogoPath(info.getBigAppIcon());
            tmp_data.setApkName(info.getAppName());
            tmp_data.setPackageName(info.getPackageName());
            tmp_data.setVersionCode(info.getVersionCode());
            tmp_data.setVersionName(info.getVersionName());
            tmp_data.setShowAppSize(info.getAppSize());
            return tmp_data;
        }
        return null;
    }

    public int getRecoveryAppCount() {
        return recoveryAppCount;
    }

    public int getRecommendAppCount() {
        return recommendAppCount;
    }

    class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;

        public GridSpacingItemDecoration(int spanCount, int spacing) {
            this.spanCount = spanCount;
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position

            if (adapter.isHeader(position)) {
                outRect.left = 0;
                outRect.right = 0;
                outRect.top = 0;
                outRect.bottom = 0;
            } else {
                outRect.left = 0;
                outRect.right = 0;
                outRect.top = 0;
                outRect.bottom = 0;
            }

//            int column = position % spanCount; // item column
//
//            outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
//            outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)
//
//            if (position < spanCount) { // top edge
//                outRect.top = spacing;
//            }
//            outRect.bottom = spacing; // item bottom
        }
    }

    private class AppRecoveryTask extends AsyncTask<Void, Integer, Integer> {

        List<PackageInfo> installPkgList;

        @Override
        protected void onPreExecute() {
            installPkgList = getPackageManager().getInstalledPackages(0);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheInMemory(true).cacheOnDisk(true).build();
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                    AppRecoveryActivity.this).defaultDisplayImageOptions(defaultOptions)
                    .threadPriority(Thread.NORM_PRIORITY - 2)
                    .denyCacheImageMultipleSizesInMemory()
                    .tasksProcessingOrder(QueueProcessingType.LIFO)
                    .diskCacheSize(100 * 1024 * 1024)
                    .memoryCache(new LruMemoryCache(20 * 1024 * 1024))
                    .memoryCacheSize(20 * 1024 * 1024)
                    .build();
            // Initialize ImageLoader with configuration.
            ImageLoader.getInstance().init(config);


            list.clear();
            int result = SoftBoxProtocolModel.getSoftBackupRecord(list);
            return result;
        }

        @Override
        protected void onPostExecute(Integer o) {
            int result = o;
            if (result == SoftBoxProtocolModel.RESULT_SUCCESS) {
                List<CloudAppRecoveryAppInfoRequestData> requestInfoList =
                        new ArrayList<CloudAppRecoveryAppInfoRequestData>();
                CloudAppRecoveryAppInfoRequestData requestData = null;
                for (AppInfo info : list) {

                    //已安装的情况，本地版本低于恢复版本，可以恢复；本地版本高于恢复版本，不可恢复
                    boolean add = true;
                    for (PackageInfo packageInfo : installPkgList) {
                        if (packageInfo.packageName.equals(info.getPkgName())) {
                            if (packageInfo.versionCode >= info.getVersionCode()) {
                                add = false;
                                break;
                            }
                        }
                    }

                    if (add) {
                        requestData = new CloudAppRecoveryAppInfoRequestData();
                        requestData.setPackageName(info.getPkgName());
                        requestInfoList.add(requestData);
                    }

                }

                RequestHelper.getCloudAppRecoveryApp(AppRecoveryActivity.this, requestInfoList,
                        new DataResponse<CloudAppRecoveryResultData>() {
                    @Override
                    public void onResponse(CloudAppRecoveryResultData value) {

                        List<RecoveryAppInfo> appList = value.getAppList();
                        List<RecoveryAppInfo> recList = value.getRecList();

                        if (appList != null && appList.size() > 0) {
                            recoveryAppCount = appList.size();
                            RecoveryAppItem item;
                            item = new RecoveryAppItem();
                            item.setType(RecoveryAppItem.ITEM_VIEW_TYPE_HEADER);
                            item.setHeaderType(RecoveryAppItem.ITEM_VIEW_HEADER_TYPE_RECOVERY);
                            itemList.add(item);

                            for (int i = 0; i < appList.size(); i++) {
                                item = new RecoveryAppItem();
                                item.setType(RecoveryAppItem.ITEM_VIEW_TYPE_ITEM);
                                item.setAppType(RecoveryAppItem.ITEM_VIEW_APP_TYPE_RECOVERY);
                                item.setAppInfo(appList.get(i));
                                itemList.add(item);
                            }
                        }

                        // 过滤已安装的应用
                        List<RecoveryAppInfo> newRecList = new ArrayList<RecoveryAppInfo>();
                        if (recList != null && recList.size() > 0) {
                            for (int i = 0; i < recList.size(); i++) {
                                boolean add = true;
                                for (PackageInfo packageInfo : installPkgList) {
                                    if (packageInfo.packageName.equals(recList.get(i).getPackageName())) {
                                        add = false;
                                        break;
                                    }
                                }

                                if (add) {
                                    newRecList.add(recList.get(i));
                                }
                            }
                        }

                        if (newRecList != null && newRecList.size() > 0) {
                            recommendAppCount = newRecList.size();
                            RecoveryAppItem item;
                            item = new RecoveryAppItem();
                            item.setType(RecoveryAppItem.ITEM_VIEW_TYPE_HEADER);
                            item.setHeaderType(RecoveryAppItem.ITEM_VIEW_HEADER_TYPE_RECOMMEND);
                            itemList.add(item);

                            for (int i = 0; i < newRecList.size(); i++) {
                                item = new RecoveryAppItem();
                                item.setType(RecoveryAppItem.ITEM_VIEW_TYPE_ITEM);
                                item.setAppType(RecoveryAppItem.ITEM_VIEW_HEADER_TYPE_RECOMMEND);
                                item.setAppInfo(newRecList.get(i));
                                itemList.add(item);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        loadingPageUtil.hideLoadPage();
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
            } else if (result == SoftBoxProtocolModel.RESULT_FAIL) {
                Toast.makeText(getApplicationContext(), "查看备份软件测试接口调用失败", Toast.LENGTH_SHORT).show();
            } else if (result == SoftBoxProtocolModel.RESULT_LOGINKEY_EXPIRE) {
                Toast.makeText(getApplicationContext(), "查看备份软件测试接口调用登录态过期", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
