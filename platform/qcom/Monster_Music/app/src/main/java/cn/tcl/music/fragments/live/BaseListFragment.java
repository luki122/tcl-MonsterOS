package cn.tcl.music.fragments.live;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.adapter.ListBaseAdapter;
import cn.tcl.music.network.LiveMusicSingerTask;
import cn.tcl.music.util.LogUtil;
import cn.tcl.music.view.EmptyLayout;
import cn.tcl.music.view.EmptyLayoutV2;

@SuppressLint("NewApi")
public abstract class BaseListFragment<T extends Serializable> extends MyABaseFragment
        implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener,
        AbsListView.OnScrollListener {
    public static final String TAG = BaseListFragment.class.getSimpleName();
    protected View rootView;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected ListView mListView;
    protected ListBaseAdapter<T> mAdapter;
    protected EmptyLayoutV2 mErrorLayout;
    protected int mStoreEmptyState = -1;


    protected int mCurrentPage = 0;
    protected static int DEFAULT_START_PAGE = 1;


    @Override
    protected int inflateContentView() {
        return R.layout.fragment_pull_refresh_listview;
    }

    @Override
    protected void layoutInit(LayoutInflater inflater, Bundle savedInstanceSate) {
        super.layoutInit(inflater, savedInstanceSate);
        rootView = getRootView();
        initView();
    }

    public void initView() {
        rootView = getRootView();
        initSwipeRefreshLayout();
        mListView = (ListView) rootView.findViewById(R.id.listview);
        mErrorLayout = (EmptyLayoutV2) rootView.findViewById(R.id.error_layout);
        mErrorLayout.setOnLayoutClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentPage = DEFAULT_START_PAGE;
                mState = STATE_REFRESH;
                mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
                requestData(true);
            }
        });
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);
        if (mAdapter != null) {
            mListView.setAdapter(mAdapter);
            mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
        } else {
            mAdapter = getListAdapter();
            mListView.setAdapter(mAdapter);
            if (requestDataIfViewCreated()) {
                mErrorLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
                mState = STATE_NONE;
                requestData(false);
            } else {
                mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
            }
        }
        if (mStoreEmptyState != -1) {
            mErrorLayout.setErrorType(mStoreEmptyState);
        }
    }

    private void initSwipeRefreshLayout() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefreshlayout);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefreshlayout);
            mSwipeRefreshLayout.setOnRefreshListener(this);
            mSwipeRefreshLayout.setColorSchemeResources(
                    R.color.swiperefresh_scheme);
            mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.swiperefresh_progress_background);
        }

    }

    @Override
    public void onDestroyView() {
        mStoreEmptyState = mErrorLayout.getErrorState();
        super.onDestroyView();
    }

    protected abstract ListBaseAdapter<T> getListAdapter();

    // 下拉刷新数据
    @Override
    public void onRefresh() {
        if (mState == STATE_REFRESH) {
            return;
        }
        // 设置顶部正在刷新
        mListView.setSelection(0);
        setSwipeRefreshLoadingState();
        mCurrentPage = DEFAULT_START_PAGE;
        mState = STATE_REFRESH;
        requestData(true);
    }

    protected boolean requestDataIfViewCreated() {
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
    }

    /***
     * 获取列表数据
     *
     * @param refresh
     * @return void
     */
    protected void requestData(boolean refresh) {
        // 取新的数据
        sendRequestData();
    }

    // 是否到时间去刷新数据了
    private boolean onTimeRefresh() {
        return false;
    }

    /***
     * 自动刷新的时间
     * <p>
     * 默认：自动刷新的时间为半天时间
     *
     * @return
     */
    protected long getAutoRefreshTime() {
        return 12 * 60 * 60;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (onTimeRefresh()) {
            onRefresh();
        }
    }

    protected void sendRequestData() {
    }

    public static final int DEFAULT_SHOW_NO_MORE_TEXT_DATA_SIZE = 10;

    protected void executeOnLoadDataSuccess(List<T> data) {
        executeOnLoadFinish();
        if (data == null) {
            data = new ArrayList<>();
        }
        mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
        if (mCurrentPage == DEFAULT_START_PAGE) {
            mAdapter.clear();
        }

        for (int i = 0; i < data.size(); i++) {
            if (filterSameData(mAdapter.getData(), data.get(i))) {
                data.remove(i);
                i--;
            }
        }

        //默认为暂无数据
        int adapterState = ListBaseAdapter.STATE_EMPTY_ITEM;
        if ((mAdapter.getCount() + data.size()) == 0) {
            adapterState = ListBaseAdapter.STATE_EMPTY_ITEM;
        } else if (data.size() == 0 || (haveNoMoreData(data) && mCurrentPage == DEFAULT_START_PAGE)) {
            LogUtil.d(TAG, "executeOnLoadDataSuccess STATE_NO_MORE");
            if (mAdapter.getData().size() < DEFAULT_SHOW_NO_MORE_TEXT_DATA_SIZE) {
                LogUtil.d(TAG, "executeOnLoadDataSuccess STATE_NO_MORE data size < 10, not show foot view");
                mAdapter.setShowFootView(false);
            } else {
                LogUtil.d(TAG, "executeOnLoadDataSuccess STATE_NO_MORE data size > 10,  show foot view");
                mAdapter.setShowFootView(true);
            }
            adapterState = ListBaseAdapter.STATE_NO_MORE;
            mAdapter.notifyDataSetChanged();
        } else {

            if (isScrollEnd(mListView)) {
                LogUtil.d(TAG, "executeOnLoadDataSuccess scroll to end, need load more");
                //由于虾米服务器数据问题 请求10条 但是服务器只返回2条 但是实际上服务器上是还有更多数据的
                //解决此时显示"加载中..."的bug
                mAdapter.setShowFootView(true);
            } else {
                mAdapter.setShowFootView(false);
                LogUtil.d(TAG, "executeOnLoadDataSuccess not scroll to end, ");
            }
            adapterState = ListBaseAdapter.STATE_LOAD_MORE;
        }
        mAdapter.setState(adapterState);
        mAdapter.setShowFootView(false);
        mAdapter.addData(data);
        //对数据做处理 例如排序
        dealResult(mAdapter.getData());
        mAdapter.notifyDataSetChanged();

        // 判断等于是因为最后有一项是listview的状态
        if (mAdapter.getCount() == 0) {
            if (needShowEmptyNoData()) {
                mErrorLayout.setErrorType(EmptyLayout.NODATA);
            } else {
                mAdapter.setState(ListBaseAdapter.STATE_EMPTY_ITEM);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * 一般情况下 只需要判断服务器本次返回的数据 小于请求的数据条数 则认为没有更多数据了
     * 但是虾米sdk不一样  虾米在返回数据中提供了一个叫more的字段来标志是否有跟多数据
     *
     * @return
     */
    protected boolean haveNoMoreData(List<T> data) {
        return getDataSize(data) < getPageSize();
    }


    /**
     * 解决当用listview来做gridview显示时  数据还没拉完就提示已经玩全部了
     *
     * @param data
     * @return
     */
    protected int getDataSize(List<T> data) {
        return data.size();
    }

    protected void dealResult(ArrayList<T> datas) {

    }

    protected boolean filterSameData(List<? extends Serializable> datas, T data) {

        return false;
    }

    /**
     * 是否需要隐藏listview，显示无数据状态
     */
    protected boolean needShowEmptyNoData() {
        return true;
    }

    protected int getPageSize() {
        return LiveMusicSingerTask.DEFAULT_PAGE_LIMIT;
    }

    protected void onRefreshNetworkSuccess() {
    }

    protected void executeOnLoadDataError(String error) {
        LogUtil.d(TAG, "BaseListFragment executeOnLoadDataError");
        if (mCurrentPage == 1
                /*&& !CacheManager.isExistDataCache(getActivity(), getCacheKey())*/) {
            mErrorLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
        } else {
            mErrorLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);


            mAdapter.setState(ListBaseAdapter.STATE_NETWORK_ERROR);
            mAdapter.notifyDataSetChanged();
        }
        executeOnLoadFinish();
    }

    // 完成刷新
    protected void executeOnLoadFinish() {
        setSwipeRefreshLoadedState();
        mState = STATE_NONE;
    }

    /**
     * 设置顶部正在加载的状态
     */
    protected void setSwipeRefreshLoadingState() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(true);
            // 防止多次重复刷新
            mSwipeRefreshLayout.setEnabled(false);
        }
    }

    /**
     * 设置顶部加载完毕的状态
     */
    protected void setSwipeRefreshLoadedState() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
            mSwipeRefreshLayout.setEnabled(true);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mAdapter == null || mAdapter.getCount() == 0) {
            return;
        }
        // 数据已经全部加载，或数据为空时，或正在加载，不处理滚动事件
        if (mState == STATE_LOADMORE || mState == STATE_REFRESH) {
            return;
        }
        // 判断是否滚动到底部
        boolean scrollEnd = false;
        scrollEnd = isScrollEnd(view);

        if (mState == STATE_NONE && scrollEnd) {
            if (mAdapter.getState() == ListBaseAdapter.STATE_LOAD_MORE
                    || mAdapter.getState() == ListBaseAdapter.STATE_NETWORK_ERROR) {
                LogUtil.d(TAG, "scroll to end, load next page data");
                mCurrentPage++;
                mState = STATE_LOADMORE;
                requestData(false);
                mAdapter.setFooterViewLoading();
            }
        }
    }

    private boolean isScrollEnd(AbsListView view) {
        // 判断是否滚动到底部
        boolean scrollEnd = false;
        try {
            if (view.getPositionForView(mAdapter.getFooterView()) == view
                    .getLastVisiblePosition())
                scrollEnd = true;
        } catch (Exception e) {
            scrollEnd = false;
        }
        return scrollEnd;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
    }

}