package com.monster.market.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.monster.market.R;
import com.monster.market.adapter.TopicAdapter;
import com.monster.market.bean.AppListInfo;
import com.monster.market.bean.TopicInfo;
import com.monster.market.download.AppDownloadData;
import com.monster.market.http.DataResponse;
import com.monster.market.http.RequestError;
import com.monster.market.http.RequestHelper;
import com.monster.market.http.data.TopicResultData;
import com.monster.market.utils.LoadingPageUtil;
import com.monster.market.utils.LogUtil;
import com.monster.market.utils.SystemUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiaobin on 16-8-20.
 */
public class TopicActivity extends BaseActivity {

    private SwipeToLoadLayout swipeToLoad;
    private ListView listview;

    private int pageNum = 0;
    private final int pageSize = 10;
    private boolean isLoadDataFinish = false;

    private LoadingPageUtil loadingPageUtil;

    private List<TopicInfo> topicInfoList;
    private TopicAdapter topicAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMstContentView(R.layout.activity_topic);

        topicInfoList = new ArrayList<TopicInfo>();
        topicAdapter = new TopicAdapter(this, topicInfoList);

        initViews();
        initData();
    }

    @Override
    public void initViews() {
        mToolbar = getToolbar();
        mToolbar.setTitle(getString(R.string.tab_topic));

        swipeToLoad = (SwipeToLoadLayout) findViewById(R.id.swipeToLoad);
        listview = (ListView) findViewById(R.id.swipe_target);
        listview.setAdapter(topicAdapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(TopicActivity.this, AppListActivity.class);
                intent.putExtra(AppListActivity.OPEN_TYPE, AppListActivity.TYPE_TOPIC);
                intent.putExtra(AppListActivity.TYPE_SUB_ID, topicInfoList.get(i).getId());
                startActivity(intent);
            }
        });

        swipeToLoad.setRefreshEnabled(false);
        swipeToLoad.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                getNetData(pageNum + 1, pageSize);
            }
        });

        initLoadingPage();
    }

    @Override
    public void initData() {
        pageNum = 0;

        getNetData(pageNum, pageSize);
    }

    @Override
    public void onNavigationClicked(View view) {
        finish();
    }

    private void getNetData(final int pNum, final int pSize) {
        RequestHelper.getTopicList(this, pNum, pSize, new DataResponse<TopicResultData>() {

            @Override
            public void onResponse(TopicResultData value) {
                pageNum = pNum;

                int size = value.getTopicList().size();
                if (size < pageSize)
                    isLoadDataFinish = true;

                topicInfoList.addAll(value.getTopicList());

                if (pageNum == 0) {
                    topicAdapter.notifyDataSetChanged();
                    loadingPageUtil.hideLoadPage();
                } else {
                    topicAdapter.notifyDataSetChanged();
                }

                if (swipeToLoad != null && swipeToLoad.isLoadingMore()) {
                    swipeToLoad.setLoadingMore(false);
                }

                if (isLoadDataFinish) {
                    swipeToLoad.setLoadMoreEnabled(false);
                }
            }

            @Override
            public void onErrorResponse(RequestError error) {
                LogUtil.i(TAG, error.toString());
                loadingPageUtil.showNetworkError();
            }
        });
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

}
