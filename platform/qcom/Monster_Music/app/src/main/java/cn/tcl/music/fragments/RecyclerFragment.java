package cn.tcl.music.fragments;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import cn.tcl.music.R;
import cn.tcl.music.activities.SearchActivity;
import cn.tcl.music.activities.SettingsActivity;
import cn.tcl.music.adapter.BaseRecyclerAdapter;
import mst.widget.toolbar.Toolbar;

/**
 * Created by dongdong.huang on 2015/11/10.
 * RecyclerView 滚动底部加载更多
 * vertial layout
 */
public abstract class RecyclerFragment extends NetWorkBaseFragment {
    private RecyclerView mRecyclerView;
    private BaseRecyclerAdapter mRecyclerAdapter;
    private List mRecyclerList;
    private LinearLayoutManager mLayoutManager;
    private OnLoadMoreListener mLoadMoreListener;
    private int mLastVisibleItem;
    private int mDistanceY;
    private boolean mIsLoading = false;
    private int mSpanCount = 2;

    @Override
    protected int getSubContentLayout() {
        return R.layout.fragment_recycler;
    }

    @Override
    protected void findViewByIds(View parent) {
        super.findViewByIds(parent);
        setCustomView(parent);

        mRecyclerView = (RecyclerView) parent.findViewById(R.id.recycle_view);
        if (mLayoutManager == null) {
            mLayoutManager = new LinearLayoutManager(getActivity());
        }
        mLayoutManager.setOrientation(LinearLayout.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mRecyclerView.setVerticalScrollBarEnabled(true);

        RecyclerView.ItemDecoration decoration = getRecyclerViewLineDecoration();
        if (decoration != null) {
            mRecyclerView.addItemDecoration(decoration);
        }

        mRecyclerList = new ArrayList();
        mRecyclerAdapter = new RecyclerAdaper(getActivity(), mRecyclerList);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerAdapter.hideLoading();
        setOnScrollListener();
        mIsLoading = false;
        mDistanceY = 0;
        mLastVisibleItem = 0;
    }

    protected void setGridLayoutManager() {
        mLayoutManager = new GridLayoutManager(getActivity(), mSpanCount);
    }

    protected void setCustomView(View parent) {
        Toolbar toolbar = (Toolbar) parent.findViewById(R.id.online_toolbar);
        toolbar.inflateMenu(R.menu.menu_local_music);
        toolbar.setTitle(getTitle());
        toolbar.setTitleTextAppearance(getActivity(), R.style.ToolbarTitle);
        toolbar.setOnMenuItemClickListener(onMenuItemClick);
        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClick = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_search:
                    Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                    startActivity(searchIntent);
                    break;
                case R.id.action_setting:
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    startActivity(intent);
                    break;
            }
            return true;
        }
    };

    protected String getTitle() {
        return "";
    }

    protected RecyclerView.ItemDecoration getRecyclerViewLineDecoration() {
        return null;
    }

    @Override
    protected void initViews() {
        super.initViews();
    }

    private void setOnScrollListener() {
        if (mRecyclerView != null) {
            mRecyclerView.addOnScrollListener(new OnScrollListener() {
                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && mLastVisibleItem + 1 == mRecyclerAdapter.getItemCount()) {
                        if (mLoadMoreListener != null && !mIsLoading && mDistanceY > 0) {
                            mLoadMoreListener.onLoadMore();
                            mRecyclerAdapter.showLoading();
                            mIsLoading = true;
                        }
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    mLastVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                    mDistanceY = dy;
                }
            });
        }
    }

    protected void removeOnScrollListener() {
        if (mRecyclerView != null) {
            mRecyclerView.removeOnScrollListener(null);
        }
    }

    protected void addLoadMoreListener(OnLoadMoreListener listener) {
        mLoadMoreListener = listener;
    }

    /**
     * add data for RecyclerView
     */
    final protected void addData2RecyclerView(List list) {
        if (list != null && mRecyclerList != null) {
            mRecyclerList.addAll(list);
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    protected void finishBottomLoading() {
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.hideLoading();
            mIsLoading = false;
        }
    }

    class RecyclerAdaper extends BaseRecyclerAdapter {
        private List mList;

        public RecyclerAdaper(Context context, List list) {
            super(context, list);
            mList = list;
        }

        @Override
        public RecyclerView.ViewHolder onItemHolderCreate() {
            return onItemHolderCreated();
        }

        @Override
        public void onItemHolderBind(RecyclerView.ViewHolder holder, int position) {
            onItemHolderBinded(holder, position, mList.get(position));
        }

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            super.onViewRecycled(holder);
            onItemRecycled(holder);
        }
    }

    public abstract RecyclerView.ViewHolder onItemHolderCreated();

    public abstract void onItemHolderBinded(RecyclerView.ViewHolder holder, int position, Object item);

    public void onItemRecycled(RecyclerView.ViewHolder holder) {

    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }
}
