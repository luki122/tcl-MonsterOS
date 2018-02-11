package cn.tcl.music.view.xlistview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import cn.tcl.music.R;
import cn.download.mie.base.util.ContextUtils;


/**
 * @author markmjw
 * @Description:
 * @date 2013-10-08 11:28
 * @copyright TCL-MIE
 */
public class XListView extends ListView implements AbsListView.OnScrollListener {
    // private static final String TAG = "XListView";

    private final static int SCROLL_BACK_HEADER = 0;
    private final static int SCROLL_BACK_FOOTER = 1;

    private final static int SCROLL_DURATION = 400;
    private int mLoadMoreHeight = 50;
    private final static float OFFSET_RADIO = 1.8f;

    private float mLastY = -1;
    private Scroller mHeadScroller;
    private Scroller mMarginScroller;
    private OnScrollListener mScrollListener;
    private int mScrollBack;

    private XHeaderView mHeader;
    private RelativeLayout mHeaderContent;
    private int mHeaderHeight;

    private LinearLayout mFooterLayout;
    private XFooterView mFooterView;
    private boolean mIsFooterReady = false;

    private boolean mEnablePullRefresh = true;
    private boolean mPullRefreshing = false;

    private boolean mEnablePullLoad = true;
    private boolean mEnableAutoLoad = false;
    private boolean mPullLoading = false;

    private OnRefreshListener mRefreshListener;
    private OnLoadMoreListener mLoadMoreListener;

    /**
     * for top animation
     */
    private OnHeadTopMarginListener mOnHeadTopMarginListener;
    private int mMaxHeight = 0;
    private int mMinHeight = 0;
    private int mAutoScrollHeight = 0;

    /**
     * 下拉刷新监听接口
     *
     */
    public interface OnRefreshListener {
        void onRefresh();
    }

    /**
     * 加载更多监听接口
     *
     */
    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    public interface OnHeadTopMarginListener {
        void OnHeadTopMargin(int topMargin);
    }

    public boolean canRefresh() {
        return mEnablePullRefresh;
    }

    private int mTotalItemCount;

    public XListView(Context context) {
        this(context, null);
    }

    public XListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public XListView(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public XListView(Context context, AttributeSet attrs, int defStyle, int styleRes) {
        super(context, attrs, defStyle);

        mHeadScroller = new Scroller(context, new DecelerateInterpolator());
        mMarginScroller = new Scroller(context, new DecelerateInterpolator());
        super.setOnScrollListener(this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XListView);
        mMaxHeight = a.getDimensionPixelSize(R.styleable.XListView_maxTopHeight, 0);
        mMinHeight = a.getDimensionPixelSize(R.styleable.XListView_minTopHeight, 0);
        a.recycle();

        mAutoScrollHeight = (mMaxHeight - mMinHeight) / 2 + mMinHeight;

        // init header view
        mHeader = new XHeaderView(context);
        mHeaderContent = (RelativeLayout) mHeader.findViewById(R.id.header_content);
        mLoadMoreHeight = ContextUtils.dip2px(context, 50);

        addHeaderView(mHeader);

        // init footer view
        mFooterView = new XFooterView(context);
        mFooterLayout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        mFooterLayout.addView(mFooterView, params);
        mHeader.setMarginTop(mMaxHeight);
        ViewTreeObserver observer = mHeader.getViewTreeObserver();
        if (null != observer) {
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressWarnings("deprecation")
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void onGlobalLayout() {
                    mHeaderHeight = mHeaderContent.getHeight();
                    ViewTreeObserver observer = getViewTreeObserver();

                    if (null != observer) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            observer.removeGlobalOnLayoutListener(this);
                        } else {
                            observer.removeOnGlobalLayoutListener(this);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (!mIsFooterReady) {
            mIsFooterReady = true;
            addFooterView(mFooterLayout);
        }

        super.setAdapter(adapter);
    }

    /**
     * Enable or disable pull down refresh feature.
     *
     * @param enable
     */
    public void setCanRefresh(boolean enable) {
        mEnablePullRefresh = enable;

        // disable, hide the content
        mHeaderContent.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Enable or disable pull up load more feature.
     *
     * @param enable
     */
    public void setCanLoadMore(boolean enable) {
        mEnablePullLoad = enable;

        if (!mEnablePullLoad) {
            mFooterView.setBottomMargin(0);
            mFooterView.hide();
            mFooterView.setPadding(0, 0, 0, mFooterView.getHeight() * (-1));
            mFooterView.setOnClickListener(null);

        } else {
            mPullLoading = false;
            mFooterView.setPadding(0, 0, 0, 0);
            mFooterView.show();
            mFooterView.setState(XFooterView.STATE_NORMAL);
            mFooterView.setOnClickListener(loadMoreOnClickListener);
        }
    }

    /**
     * 没有更多数据加载
     */
    public void setNoMoreData(){
        mEnablePullLoad = false;
        mPullLoading = false;
        mFooterView.setPadding(0, 0, 0, 0);
        mFooterView.show();
        mFooterView.setState(XFooterView.STATE_NOMOREDATA);
        mFooterView.setOnClickListener(null);
    }

    OnClickListener loadMoreOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startLoadMore();
        }
    };

    /**
     * Enable or disable auto load more feature when scroll to bottom.
     *
     * @param enable
     */
    public void setAutoLoadMore(boolean enable) {
        mEnableAutoLoad = enable;
    }

    /**
     * Stop refresh, reset header view.
     */
    public void stopRefresh() {
        if (mPullRefreshing) {
            mPullRefreshing = false;
            resetHeaderHeight();
        }
    }

    /**
     * Stop load more, reset footer view.
     */
    public void stopLoadMore() {
        if (mPullLoading) {
            mPullLoading = false;
            mFooterView.setState(XFooterView.STATE_NORMAL);
        }
    }

    /**
     * Set listener.
     *
     * @param refreshListener
     */
    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        this.mRefreshListener = refreshListener;
    }

    /**
     * Set listener.
     *
     * @param loadMoreListener
     */
    public void setOnLoadListener(OnLoadMoreListener loadMoreListener) {
        this.mLoadMoreListener = loadMoreListener;
    }

    public void setOnHeadTopMarginListener(OnHeadTopMarginListener mOnHeadTopMarginListener) {
        this.mOnHeadTopMarginListener = mOnHeadTopMarginListener;
    }

    /**
     * Auto call back refresh.
     */
    public void autoRefresh() {
        mHeader.setVisibleHeight(mHeaderHeight);

        if (mEnablePullRefresh && !mPullRefreshing) {
            // update the arrow image not refreshing
            if (mHeader.getVisibleHeight() > mHeaderHeight) {
                mHeader.setState(XHeaderView.STATE_READY);
            } else {
                mHeader.setState(XHeaderView.STATE_NORMAL);
            }
        }

        if (mHeader.getState() != XHeaderView.STATE_REFRESHING) {
            mPullRefreshing = true;
            mHeader.setState(XHeaderView.STATE_REFRESHING);
            refresh();
        }
    }

    private void updateHeaderHeight(float delta) {
        setSelection(0);
        if ((int) delta + mHeader.getVisibleHeight() == mHeader.getVisibleHeight()) {
            return;
        }

        mHeader.setVisibleHeight((int) delta + mHeader.getVisibleHeight());
        if (mEnablePullRefresh && !mPullRefreshing) {
            if (mHeader.getVisibleHeight() > mHeaderHeight) {
                mHeader.setState(XHeaderView.STATE_READY);
            } else {
                mHeader.setState(XHeaderView.STATE_NORMAL);
            }
        }
    }

    private void resetHeaderHeight() {
        int height = mHeader.getVisibleHeight();
        if (height == 0) {
            return;
        }
        if (mPullRefreshing && height <= mHeaderHeight) {
            return;
        }
        int finalHeight = 0;
        if (mPullRefreshing && height > mHeaderHeight) {
            finalHeight = mHeaderHeight;
        }

        mScrollBack = SCROLL_BACK_HEADER;
        mHeadScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);

        // trigger computeScroll
        invalidate();
    }

    private void updateFooterHeight(float delta) {
        int height = mFooterView.getBottomMargin() + (int) delta;

        if (mEnablePullLoad && !mPullLoading) {
            if (height > mLoadMoreHeight) {
                // height enough to invoke load more.
                mFooterView.setState(XFooterView.STATE_READY);
            } else {
                mFooterView.setState(XFooterView.STATE_NORMAL);
            }
        }

        mFooterView.setBottomMargin(height);
    }

    private void resetFooterHeight() {
        int bottomMargin = mFooterView.getBottomMargin();

        if (bottomMargin > 0) {
            mScrollBack = SCROLL_BACK_FOOTER;
            mHeadScroller.startScroll(0, bottomMargin, 0, -bottomMargin, SCROLL_DURATION);
            invalidate();
        }
    }

    private void startLoadMore() {
        mPullLoading = true;
        if (mFooterView.getState() != XFooterView.STATE_LOADING) {
            mFooterView.setState(XFooterView.STATE_LOADING);
            loadMore();
        }
    }

    float y1 = 0, y2 = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            mLastY = ev.getRawY();
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();

                if (mHeader.getTop() >= 0 && getFirstVisiblePosition() == 0 && (mHeader.getVisibleHeight() > 0 || deltaY > 0)) {
                    // the first item is showing, header has shown or pull down.
                    if (mEnablePullRefresh) {
                        if (mMaxHeight != 0 && mHeader.getMarginTop() != mMaxHeight) {

                        } else {
                            updateHeaderHeight(deltaY / OFFSET_RADIO);
                        }
                    }

                } else if (getLastVisiblePosition() == mTotalItemCount - 1 && (mFooterView.getBottomMargin() > 0 || deltaY < 0)) {
                    if (mEnablePullLoad) {
                        updateFooterHeight(-deltaY / OFFSET_RADIO);
                    }
                }

                if (getFirstVisiblePosition() > 0) {
                    mHeader.setVisibleHeight(0);
                }

                if (mHeader.getVisibleHeight() == 0) {

                    int topMargin = (int) (mHeader.getMarginTop() + deltaY / OFFSET_RADIO);
                    if (topMargin < mMinHeight) {
                        topMargin = mMinHeight;
                    }

                    if (topMargin > mMaxHeight) {
                        topMargin = mMaxHeight;
                    }

                    if (topMargin != mHeader.getMarginTop()) {
                        mHeader.setMarginTop(topMargin);

                        if (mOnHeadTopMarginListener != null) {
                            mOnHeadTopMarginListener.OnHeadTopMargin(mHeader.getMarginTop());
                        }

                    }
                }
                break;

            default:
                mLastY = -1;
                if (getFirstVisiblePosition() == 0) {
                    if (mEnablePullRefresh && mHeader.getVisibleHeight() > mHeaderHeight && mHeader.getState() != XHeaderView.STATE_REFRESHING) {
                        mPullRefreshing = true;
                        mHeader.setState(XHeaderView.STATE_REFRESHING);
                        refresh();
                    }

                    if (mEnablePullRefresh && mHeader.getVisibleHeight() > 0) {
                        resetHeaderHeight();
                    }
                } else if (getLastVisiblePosition() == mTotalItemCount - 1) {
                    if (mEnablePullLoad && mFooterView.getBottomMargin() > mLoadMoreHeight && mFooterView.getState() != XFooterView.STATE_LOADING) {
                        startLoadMore();
                    }

                    if (mEnablePullLoad) {
                        resetFooterHeight();
                    }
                }

                if (mMaxHeight != 0) {
                    resetHeadMargin();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    private void resetHeadMargin() {
        if (mHeader.getMarginTop() > mAutoScrollHeight) {
            mMarginScroller.startScroll(0, mHeader.getMarginTop(), 0, mMaxHeight - mHeader.getMarginTop(), SCROLL_DURATION);
            invalidate();
        } else if (mHeader.getMarginTop() != mMinHeight) {
            mMarginScroller.startScroll(0, mHeader.getMarginTop(), 0, mMinHeight - mHeader.getMarginTop(), SCROLL_DURATION);
            invalidate();
        }
    }

    @Override
    public void computeScroll() {
        if (mHeadScroller.computeScrollOffset()) {
            if (mScrollBack == SCROLL_BACK_HEADER) {
                mHeader.setVisibleHeight(mHeadScroller.getCurrY());
            } else if (mScrollBack == SCROLL_BACK_FOOTER) {
                mFooterView.setBottomMargin(mHeadScroller.getCurrY());
            }

            postInvalidate();
        }

        if (mMarginScroller.computeScrollOffset()) {
            mHeader.setMarginTop(mMarginScroller.getCurrY());
            if (mOnHeadTopMarginListener != null) {
                mOnHeadTopMarginListener.OnHeadTopMargin(mHeader.getMarginTop());
            }

            postInvalidate();
        }

        super.computeScroll();
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mScrollListener = l;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mScrollListener != null) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }

        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            if (mEnablePullLoad && mEnableAutoLoad && getLastVisiblePosition() == getCount() - 1 && getCount() > 3) {
                startLoadMore();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mTotalItemCount = totalItemCount;
        if (mScrollListener != null) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    private void refresh() {
        if (mEnablePullRefresh && null != mRefreshListener) {
            mRefreshListener.onRefresh();
        }
    }

    private void loadMore() {
        if (mEnablePullLoad && null != mLoadMoreListener) {
            mLoadMoreListener.onLoadMore();
        }
    }

    /**
     * 加载更多完成
     *
     */
    public void onLoadMoreComplete(boolean succ) {
        mPullLoading = false;
        if (succ) {
            mFooterView.setState(XFooterView.STATE_NORMAL);
        } else {
            mFooterView.setState(XFooterView.STATE_FAILED);
        }
    }

    /**
     * 下拉刷新完成
     *
     */
    public void onRefreshComplete() {
        if (mPullRefreshing) {
            mPullRefreshing = false;
            mHeader.setState(XHeaderView.STATE_DONE);
            resetHeaderHeight();
        }
    }

    public void setMarginTop(int topMargin) {
        mHeader.setMarginTop(topMargin);
    }

    public int getMarginTop() {
        return mHeader.getMarginTop();
    }

    public void onDestory() {
        setOnLoadListener(null);
        setOnRefreshListener(null);
        removeFooterView(mFooterLayout);
        removeHeaderView(mHeader);
        if (mFooterView != null) {
            mFooterView.setOnClickListener(null);
        }
    }
}

