package com.android.calculator2.exchange.view;

import android.support.v4.widget.ScrollerCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;

public class SwipeItemLayout extends FrameLayout {
    private View contentView = null;
    private View menuView = null;
    private Interpolator closeInterpolator = null;
    private Interpolator openInterpolator = null;

    private ScrollerCompat mOpenScroller;
    private ScrollerCompat mCloseScroller;

    // private FrameLayout.LayoutParams mContentparams;
    // private FrameLayout.LayoutParams mMenuparams;

    private int mBaseX;
    private int mDownX;
    private int mDis = -1;
    private int state = STATE_CLOSE;

    private static final int STATE_CLOSE = 0;
    private static final int STATE_OPEN = 1;

    public SwipeItemLayout(View contentView, View menuView, Interpolator closeInterpolator,
            Interpolator openInterpolator) {
        super(contentView.getContext());
        this.contentView = contentView;
        this.menuView = menuView;
        this.closeInterpolator = closeInterpolator;
        this.openInterpolator = openInterpolator;
        init();
        // mContentparams = new FrameLayout.LayoutParams(contentView.getWidth(),
        // contentView.getHeight());
        // mMenuparams = new FrameLayout.LayoutParams(menuView.getWidth(),
        // menuView.getHeight());
    }

    private void init() {
        setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        if (closeInterpolator != null) {
            mCloseScroller = ScrollerCompat.create(getContext(), closeInterpolator);
        } else {
            mCloseScroller = ScrollerCompat.create(getContext());
        }
        if (openInterpolator != null) {
            mOpenScroller = ScrollerCompat.create(getContext(), openInterpolator);
        } else {
            mOpenScroller = ScrollerCompat.create(getContext());
        }

        LayoutParams contentParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        contentView.setLayoutParams(contentParams);

        menuView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        addView(contentView);
        addView(menuView);
    }

    public boolean onSwipe(MotionEvent event) {
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mDownX = (int) event.getX();
            break;
        case MotionEvent.ACTION_MOVE:
            mDis = (int) (mDownX - event.getX());
            if (state == STATE_OPEN) {
                mDis += menuView.getWidth();
            }
            swipe();
            break;
        case MotionEvent.ACTION_UP:
            if ((mDownX - event.getX()) > (menuView.getWidth() / 4)) {
                // open
                smoothOpenMenu();
            } else {
                // close
                smoothCloseMenu();
                return false;
            }
            break;
        }
        return true;
    }

    public boolean isOpen() {
        return state == STATE_OPEN;
    }

    private void swipe() {
        if (mDis > menuView.getWidth()) {
            mDis = menuView.getWidth();
        }
        if (mDis < 0) {
            mDis = 0;
        }

        // mContentparams.setMargins(-dis, contentView.getTop(),
        // contentView.getWidth()
        // - dis, getMeasuredHeight());
        // contentView.setLayoutParams(mContentparams);

        // mMenuparams.setMargins(contentView.getWidth() - dis,
        // menuView.getTop(),
        // contentView.getWidth() + menuView.getWidth() - dis,
        // menuView.getBottom());
        // menuView.setLayoutParams(mMenuparams);

        contentView.layout(-mDis, contentView.getTop(), contentView.getWidth() - mDis, getMeasuredHeight());

        menuView.layout(contentView.getWidth() - mDis, menuView.getTop(), contentView.getWidth() + menuView.getWidth()
                - mDis, menuView.getBottom());
    }

    @Override
    public void computeScroll() {
        if (state == STATE_OPEN) {
            if (mOpenScroller.computeScrollOffset()) {
                mDis = mOpenScroller.getCurrX();
                swipe();
                postInvalidate();
            }
        } else {
            if (mCloseScroller.computeScrollOffset()) {
                mDis = mBaseX - mCloseScroller.getCurrX();
                swipe();
                postInvalidate();
            }
        }
    }

    public void smoothCloseMenu() {
        state = STATE_CLOSE;
        mBaseX = -contentView.getLeft();
        System.out.println(mBaseX);
        mCloseScroller.startScroll(0, 0, mBaseX, 0, 350);
        contentView.requestLayout();
        menuView.requestLayout();
        postInvalidate();
    }

    public void smoothOpenMenu() {
        state = STATE_OPEN;
        mOpenScroller.startScroll(-contentView.getLeft(), 0, menuView.getWidth(), 0, 350);
        contentView.requestLayout();
        menuView.requestLayout();
        postInvalidate();
    }

    public View getContentView() {
        return contentView;
    }

    public View getMenuView() {
        return menuView;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        menuView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mDis < 0) {
            contentView.layout(0, 0, getMeasuredWidth(), contentView.getMeasuredHeight());
            menuView.layout(getMeasuredWidth(), 0, getMeasuredWidth() + menuView.getMeasuredWidth(),
                    contentView.getMeasuredHeight());
        } else {
            contentView.layout(-mDis, contentView.getTop(), contentView.getWidth() - mDis, getMeasuredHeight());
            menuView.layout(contentView.getWidth() - mDis, menuView.getTop(),
                    contentView.getWidth() + menuView.getWidth() - mDis, menuView.getBottom());
        }
    }
}
