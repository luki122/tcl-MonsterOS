package com.monster.market.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.monster.market.R;
import com.monster.market.activity.AppDetailActivity;
import com.monster.market.adapter.AppListAdapter;
import com.monster.market.bean.AppDetailAnimInfo;
import com.monster.market.bean.AppListInfo;
import com.monster.market.bean.TopicInfo;
import com.monster.market.constants.HttpConstant;
import com.monster.market.constants.WandoujiaDownloadConstant;
import com.monster.market.download.AppDownloadData;
import com.monster.market.download.AppDownloadService;
import com.monster.market.download.DownloadUpdateListener;
import com.monster.market.http.DataResponse;
import com.monster.market.http.RequestError;
import com.monster.market.http.RequestHelper;
import com.monster.market.http.data.AppListResultData;
import com.monster.market.http.data.SearchAppListResultData;
import com.monster.market.http.data.TopicDetailResultData;
import com.monster.market.utils.LoadingPageUtil;
import com.monster.market.utils.LogUtil;
import com.monster.market.utils.SettingUtil;
import com.monster.market.utils.SystemUtil;
import com.monster.market.views.ListLoadMoreView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.monster.market.utils.DensityUtil.dip2px;

/**
 * Created by xiaobin on 16-8-5.
 */
public class AppListFragment extends BaseFragment {

    public static final int TYPE_MAIN = 0;
    public static final int TYPE_NEW = 1;
    public static final int TYPE_RANK = 2;
    public static final int TYPE_CATEGORY = 3;
    public static final int TYPE_TOPIC = 4;
    public static final int TYPE_SEARCH = 5;
    public static final int TYPE_AWARD = 6;
    public static final int TYPE_ESSENTIAL = 7;
    public static final int RANK_TYPE_APP = 2;
    public static final int RANK_TYPE_GAME = 1;
    public static final int ESSENTIAL_TYPE_APP = 2;
    public static final int ESSENTIAL_TYPE_GAME = 1;

    private Context mContext;

    // 0主界面 1新品 2排行 3分类 4专题 5搜索 6设计奖 7必备
    private int type = TYPE_NEW;
    private int subId = -1;
    private String key = "";

    private ListView listview;
    private ListLoadMoreView loadMoreView;

    private LoadingPageUtil loadingPageUtil;

    private int pageNum = 0;
    private final int pageSize = 10;
    private boolean isLoadDataFinish = false;

    private List<AppListInfo> appList;
    private List<AppDownloadData> downloadDataList;
    private AppListAdapter adapter;

    private boolean stopFlag = false;

    private View topicTopView;
    private TopicInfo topicInfo;

    private String categoryName;    // 分类名称

    // 上一次点击item项的时间
    private long lastClickItemTime = 0;

    public static AppListFragment newInstance(int type, int id) {
        AppListFragment f = new AppListFragment();
        Bundle b = new Bundle();
        b.putInt("type", type);
        b.putInt("id", id);
        f.setArguments(b);
        return f;
    }

    public static AppListFragment newInstance(int type, String key) {
        AppListFragment f = new AppListFragment();
        Bundle b = new Bundle();
        b.putInt("type", type);
        b.putString("key", key);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mContext = null;

        if (loadingPageUtil != null) {
            loadingPageUtil.exit();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            type = args.getInt("type", TYPE_NEW);
            subId = args.getInt("id", -1);
            key = args.getString("key", "");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_applist, container,
                false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        appList = new ArrayList<AppListInfo>();
        downloadDataList = new ArrayList<AppDownloadData>();
        adapter = new AppListAdapter(mContext, appList, downloadDataList);

        initViews();
        initData();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (stopFlag) {
            updateListener.downloadProgressUpdate();
            stopFlag = false;
            listview.postInvalidate();
        }
        AppDownloadService.registerUpdateListener(updateListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        stopFlag = true;
        AppDownloadService.unRegisterUpdateListener(updateListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void initViews() {
        View view = getView();
        listview = (ListView) view.findViewById(R.id.swipe_target);

        if (type == TYPE_TOPIC) {
            topicTopView = LayoutInflater.from(getActivity()).inflate(R.layout.item_topic, null);
            listview.addHeaderView(topicTopView);
        }

        loadMoreView = new ListLoadMoreView(getActivity());
        loadMoreView.showNormalProgress();
        listview.addFooterView(loadMoreView);

        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (System.currentTimeMillis() - lastClickItemTime > 1000) {
                    if (getActivity() != null) {
                        int index = i - listview.getHeaderViewsCount();
                        if (index >= 0 && index < appList.size()) {
                            Intent intent = new Intent(getActivity(), AppDetailActivity.class);
                            intent.putExtra(AppDetailActivity.PACKAGE_NAME, appList.get(index).getPackageName());
                            intent.putExtra(AppDetailActivity.REPORT_MODULID, downloadDataList.get(index).getReportModulId());
                            intent.putExtra(AppDetailActivity.ANIM_PARAMS, getAppDetailAnimInfo(view));
                            intent.putExtra(AppDetailActivity.ICON_URL, appList.get(index).getBigAppIcon());
                            startActivity(intent);
                            getActivity().overridePendingTransition(0,0);
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
                    adapter.setLoadImage(scrollState);
                } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    adapter.setLoadImage(scrollState);
                } else if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    adapter.setLoadImage(scrollState);
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
                    getAppData(pageNum + 1, pageSize);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

            }
        });

        loadMoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loadMoreView.getStatus() == ListLoadMoreView.STATUS_ERROR) {
                    loadMoreView.showProgress();;
                    getAppData(pageNum + 1, pageSize);
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
        animInfo.setLayoutInitHeight(dip2px(getContext(), 80))
                .setLayoutMarginTop(dip2px(getContext(), 156))
                .setIconMarginLeft(dip2px(getContext(), 16))
                .setIconMarginTop(dip2px(getContext(), 14))
                .setInitIconSize(dip2px(getContext(), 52))
                .setFinalIconSize(dip2px(getContext(), 63))
                .setCoordinate(point);
        return animInfo;
    }

    @Override
    public void initData() {

        pageNum = 0;

        getAppData(pageNum, pageSize);
    }

    private void initLoadingPage() {
        View view = getView();
        loadingPageUtil = new LoadingPageUtil();
        loadingPageUtil.init(mContext, view.findViewById(R.id.frameLayout));
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

        if (type == TYPE_SEARCH) {
            loadingPageUtil.hideLoadPage();
        }
    }

    private void getAppData(final int pNum, final int pSize) {
        if (mContext == null) {
            return;
        }

        DataResponse<AppListResultData> response = new DataResponse<AppListResultData>() {

            @Override
            public void onResponse(AppListResultData value) {
                appListOnResponse(value.getAppList(), pNum);
            }

            @Override
            public void onErrorResponse(RequestError error) {
                dealOnErrorResponse(error);
            }
        };

        switch (type) {
            case TYPE_NEW:

                RequestHelper.getNewAppList(mContext, pNum, pSize, "1", response);
                break;
            case TYPE_RANK:
                RequestHelper.getRankingAppList(mContext, pNum, pSize, subId, response);
                break;
            case TYPE_CATEGORY:
                RequestHelper.getAppTypeInfoList(mContext, pNum, pSize, subId, response);
                break;
            case TYPE_TOPIC:
                RequestHelper.getTopicDetail(mContext, pNum, pSize, subId, new DataResponse<TopicDetailResultData>() {
                    @Override
                    public void onResponse(TopicDetailResultData value) {
                        dealTopicInfoResponse(value.getSpecialTopic());

                        appListOnResponse(value.getAppList(), pNum);
                    }

                    @Override
                    public void onErrorResponse(RequestError error) {
                        dealOnErrorResponse(error);
                    }
                });
                break;
            case TYPE_SEARCH:
                if (!TextUtils.isEmpty(key)) {
                    RequestHelper.searchAppList(mContext, pNum, pSize, key, new DataResponse<SearchAppListResultData>() {
                        @Override
                        public void onResponse(SearchAppListResultData value) {
                            appListOnResponse(value.getAppList(), pNum);
                        }

                        @Override
                        public void onErrorResponse(RequestError error) {
                            dealOnErrorResponse(error);
                        }
                    });
                }
                break;
            case TYPE_AWARD:
                RequestHelper.getAward(mContext, pNum, pSize, response);
                break;
            case TYPE_ESSENTIAL:
                RequestHelper.getEssentialList(mContext, pNum, pSize, subId, response);
                break;
        }
    }

    private void appListOnResponse(List<AppListInfo> data, int pNum) {
        // 豌豆荚需要的pos字段
        String pos = WandoujiaDownloadConstant.POS_HOMEPAGE;
        switch (type) {
            case TYPE_NEW:
                pos = WandoujiaDownloadConstant.POS_APP_NEW;
                break;
            case TYPE_RANK:
                pos = WandoujiaDownloadConstant.POS_TOP;
                break;
            case TYPE_CATEGORY:
                pos = WandoujiaDownloadConstant.POS_CATEGORY;
                pos = pos.replace("categoryName", categoryName);
                break;
            case TYPE_TOPIC:
                break;
            case TYPE_SEARCH:
                pos = WandoujiaDownloadConstant.POS_SEARCH;
                pos = pos.replace("keyword", key);
                break;
        }

        // 后台需要的下载上报字段
        int reportModulId = HttpConstant.REPORT_MODULID_HOMEPAGE;
        switch (type) {
            case TYPE_NEW:
                reportModulId = HttpConstant.REPORT_MODULID_NEW;
                break;
            case TYPE_RANK:
                if (subId == RANK_TYPE_APP) {
                    reportModulId = HttpConstant.REPORT_MODULID_APP_RANKING;
                } else if (subId == RANK_TYPE_GAME) {
                    reportModulId = HttpConstant.REPORT_MODULID_GAME_RANKING;
                }
                break;
            case TYPE_CATEGORY:
                reportModulId = HttpConstant.REPORT_MODULID_CATEGORY;
                break;
            case TYPE_TOPIC:
                break;
            case TYPE_SEARCH:
                reportModulId = HttpConstant.REPORT_MODULID_SEARCH;
                break;
            case TYPE_AWARD:
                reportModulId = HttpConstant.REPORT_MODULID_AWARD;
                break;
            case TYPE_ESSENTIAL:
                if (subId == ESSENTIAL_TYPE_APP) {
                    reportModulId = HttpConstant.REPORT_MODULID_APP_ESSENTIAL;
                } else if (subId == ESSENTIAL_TYPE_GAME) {
                    reportModulId = HttpConstant.REPORT_MODULID_GAME_ESSENTIAL;
                }
                break;
        }

        pageNum = pNum;

        int size = data.size();
        if (size < pageSize)
            isLoadDataFinish = true;

        appList.addAll(data);
        List<AppDownloadData> tempList = new ArrayList<AppDownloadData>();
        for (AppListInfo info : data) {
            AppDownloadData downloadData = SystemUtil.buildAppDownloadData(info);
            downloadData.setPos(pos);
            downloadData.setReportModulId(reportModulId);
            tempList.add(downloadData);
        }
        downloadDataList.addAll(tempList);

        disView();
    }

    private void dealOnErrorResponse(RequestError error) {
        LogUtil.i(TAG, error.toString());
        if (pageNum == 0) {
            if (error.getErrorType() == RequestError.ERROR_NO_NETWORK) {
                loadingPageUtil.showNoNetWork();
            } else {
                loadingPageUtil.showNetworkError();
            }
            loadMoreView.showError();
        } else {
            loadMoreView.showError();
        }
    }

    private void dealTopicInfoResponse(TopicInfo info) {
        topicInfo = info;
        if (topicTopView != null) {
            topicTopView.setClickable(false);

            RelativeLayout rl_bottom = (RelativeLayout) topicTopView.findViewById(R.id.bottom);
            TextView tv_name_detail = (TextView) topicTopView.findViewById(R.id.tv_name_detail);
            TextView tv_count_detail = (TextView) topicTopView.findViewById(R.id.tv_count_detail);
            ImageView iv_img = (ImageView) topicTopView.findViewById(R.id.iv_img);

            rl_bottom.setVisibility(View.GONE);

            ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
            // 图片加载工具
            ImageLoader imageLoader = ImageLoader.getInstance();
            DisplayImageOptions optionsImage = SystemUtil.buildTopicDisplayImageOptions(mContext);

            if (SettingUtil.isLoadingImage(mContext)) {
                imageLoader.displayImage(info.getIcon(),
                        iv_img, optionsImage, animateFirstListener);
            } else {
                iv_img.setImageResource(R.drawable.ic_launcher);
            }

            tv_name_detail.setText(info.getName());
            tv_count_detail.setText(String.format(getString(R.string.topic_app_count), info.getAppNum()));
        }
    }

    private void disView() {
        if (pageNum == 0) {
            adapter.notifyDataSetChanged();
            loadingPageUtil.hideLoadPage();
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

    public void search(String key) {
        this.key = key;

        isLoadDataFinish = false;
        pageNum = 0;
        appList.clear();
        downloadDataList.clear();
        listview.setAdapter(adapter);

        loadingPageUtil.showLoadPage();
        loadingPageUtil.showLoading();
        getAppData(pageNum, pageSize);
    }

    private static class AnimateFirstDisplayListener extends
            SimpleImageLoadingListener {

        static final List<String> displayedImages = Collections
                .synchronizedList(new LinkedList<String>());

        @Override
        public void onLoadingComplete(String imageUri, View view,
                                      Bitmap loadedImage) {
            if (loadedImage != null) {
                ImageView imageView = (ImageView) view;
                boolean firstDisplay = !displayedImages.contains(imageUri);
                if (firstDisplay) {
                    FadeInBitmapDisplayer.animate(imageView, 500);
                    displayedImages.add(imageUri);
                }
            }
        }
    }

    private DownloadUpdateListener updateListener = new DownloadUpdateListener() {
        @Override
        public void downloadProgressUpdate() {
            if (adapter != null) {
                adapter.updateView(listview);
            }
        }
    };


    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

}
