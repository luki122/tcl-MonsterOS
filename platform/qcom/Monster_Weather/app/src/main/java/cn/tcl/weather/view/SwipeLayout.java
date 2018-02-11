package cn.tcl.weather.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.leon.tools.view.AndroidUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-9-5.
 * $desc
 */
public class SwipeLayout extends FrameLayout {
    private final static int SCROLLING_TIME = 300;

    private final static float MAX_SCROLL_X = 62f;

    private final static int STATE_NULL = 0;

    private final static int STATE_SCROLLING = 1;

    private final static int STATE_SCROLLED = 3;

    private final static int STATE_FLING_LEFT = 5;

    private final static int STATE_FLING_RIGHT = 7;

    public int isScroll = 0;

    private Scroller mScroller;

    private float mMaxScrollX;

    private float mFirstChildTx;

    private GestureDetector mGestureDetector;

    private OnClickListener mClickListener;

    private int mStataus = STATE_NULL;


    private IInterceptyTouchEvent.HorizontalInterceptTouchEvent mHorizontalTouchEvent;


    public SwipeLayout(Context context) {
        super(context);
        init(context);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(21)
    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mMaxScrollX = AndroidUtils.dip2px(getContext(), MAX_SCROLL_X);
    }

    private void init(Context context) {
        mHorizontalTouchEvent = new IInterceptyTouchEvent.HorizontalInterceptTouchEvent(this);
        mScroller = new Scroller(getContext());
        mGestureDetector = new GestureDetector(mListener);
        mContext = context;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mClickListener = l;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mHorizontalTouchEvent.onInterceptTouchEvent(ev)) {
            mHorizontalTouchEvent.touchDownEvent();
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getChildCount() > 0) {
            mGestureDetector.onTouchEvent(event);
            final int action = event.getAction() & MotionEvent.ACTION_MASK;
            if (action != MotionEvent.ACTION_DOWN && action != MotionEvent.ACTION_MOVE) {
                switch (mStataus) {
                    case STATE_SCROLLED:
                        autoScrolling();
                        break;
                    case STATE_FLING_LEFT:
                        show();
                        break;
                    case STATE_FLING_RIGHT:
                        hide();
                        break;
                    default:
                        break;
                }
                requestDisallowInterceptTouchEvent(false);
            }
            return true;
        }
        return false;
    }

    private void autoScrolling() {
        if (getChildCount() > 0) {
            View view = getChildAt(getChildCount() - 1);
            int tx = (int) view.getTranslationX();
            int left = (int) (-mMaxScrollX - tx);
            if (left > tx) {
                show();
            } else {
                hide();
            }
        }
    }

    public void show() {
        if (isEnabled()) {
            if (getChildCount() > 0) {
                View view = getChildAt(getChildCount() - 1);
                int tx = (int) view.getTranslationX();
                int left = (int) (-mMaxScrollX - tx);
                mScroller.startScroll(tx, 0, left, 0, SCROLLING_TIME);
                post(mRunningRunnable);
                isScroll = 1;
            }
        }
    }

    public void hide() {
        if (isEnabled()) {
            if (getChildCount() > 0) {
                View view = getChildAt(getChildCount() - 1);
                int tx = (int) view.getTranslationX();
                if (tx < 0) {
                    mScroller.startScroll(tx, 0, -tx, 0, SCROLLING_TIME);
                    post(mRunningRunnable);
                    isScroll = 0;
                }
            }
        }
    }


    private void scrollChild(float tx) {
        if (tx > 0) {
            tx = 0;
        } else if (tx < -mMaxScrollX) {
            tx = -mMaxScrollX;
        }
        if (getChildCount() > 0) {
            View child = getChildAt(getChildCount() - 1);
            child.setTranslationX(tx);
        }
    }


    private GestureDetector.OnGestureListener mListener = new GestureDetector.OnGestureListener() {


        @Override
        public boolean onDown(MotionEvent e) {
            mStataus = STATE_NULL;
            mFirstChildTx = getChildAt(getChildCount() - 1).getTranslationX();
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
                removeCallbacks(mRunningRunnable);
            }
            return true;
        }

        boolean isScrolling() {
            return (mStataus & STATE_SCROLLING) == STATE_SCROLLING;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            hide();
            if (null != mClickListener && !isShown()) {
                mClickListener.onClick(SwipeLayout.this);
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (isEnabled()) {
                if (isScrolling() || Math.abs(distanceX) > Math.abs(distanceY)) {
                    mStataus = STATE_SCROLLED;
                    scrollChild(mFirstChildTx - e1.getRawX() + e2.getRawX());
                    requestDisallowInterceptTouchEvent(true);
                }
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (isEnabled()) {
                if (isScrolling() || Math.abs(velocityX) > Math.abs(velocityY)) {
                    mStataus = velocityX > 0 ? STATE_FLING_RIGHT : STATE_FLING_LEFT;
                    requestDisallowInterceptTouchEvent(true);
                }
            }
            return true;
        }
    };

    public boolean isShown() {
        if (getChildCount() > 0) {
            return getChildAt(getChildCount() - 1).getTranslationX() < 0f;
        }
        return false;
    }

    private Runnable mRunningRunnable = new Runnable() {
        @Override
        public void run() {
            if (getChildCount() > 0) {
                if (mScroller.computeScrollOffset()) {
                    View view = getChildAt(getChildCount() - 1);
                    view.setTranslationX(mScroller.getCurrX());
                    post(this);
                } else if (null != mOnSwipeLayoutListener) {
                    if (isShown()) {
                        mOnSwipeLayoutListener.onShow(SwipeLayout.this);
                    } else {
                        mOnSwipeLayoutListener.onHide(SwipeLayout.this);
                    }
                }
            }
        }
    };

    public int getScrollState() {
        return mStataus;
    }


    private OnSwipeLayoutListener mOnSwipeLayoutListener;

    public void setOnSwipeLayoutListener(OnSwipeLayoutListener l) {
        mOnSwipeLayoutListener = l;
        if (null != mOnSwipeLayoutListener) {
            if (isShown()) {
                mOnSwipeLayoutListener.onShow(this);
            } else {
                mOnSwipeLayoutListener.onHide(this);
            }
        }
    }


    public static interface OnSwipeLayoutListener {
        /**
         * enable delete city
         */
        void onShow(SwipeLayout layout);

        /**
         * disable delete city
         */
        void onHide(SwipeLayout layout);
    }
}
