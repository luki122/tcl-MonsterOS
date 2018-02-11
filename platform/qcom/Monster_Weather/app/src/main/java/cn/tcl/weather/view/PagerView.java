package cn.tcl.weather.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.EdgeEffect;
import android.widget.Scroller;

import cn.tcl.weather.utils.LogUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-9-2.
 * $desc
 */
public class PagerView extends ViewGroup implements GestureDetector.OnGestureListener {
    private final static String TAG = "PagerVeiw";
    private static final int INVALID_POINTER = -1;
    private int mActivePointerId = INVALID_POINTER;

    private final static int ANIMATION_TIME = 500;
    private GestureDetector mDetector = new GestureDetector(this);

    private Scroller mScroller;

    private int mCurrentIndex;

    private int mFlingOffset;

    private MotionEvent mDownEvent;

    private float mLastMotionX, mLastMotionY;

    private PagerViewAdapter mCurrentPagerAdapter;

    private int mDownScrollX;

    private Indictor mIndictor;


    public PagerView(Context context) {
        super(context);
        init();
    }

    public PagerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PagerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public PagerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());
        mEdgeEffectTouchEvent = new EdgeEffectTouchEvent(getContext());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = getWidth();
        final int height = getHeight();
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(i * width, 0, width * (i + 1), height);
        }

        setIndictor(mIndictor);
        if (null != mCurrentPagerAdapter) {
            mCurrentPagerAdapter.onPageChanged(0, mCurrentIndex);
        }
        mEdgeEffectTouchEvent.onLayout(changed, l, t, r, b);
    }


    @Override
    public boolean canScrollVertically(int direction) {
        View target = getChildAt(mCurrentIndex);
        if (super.canScrollVertically(direction)) {
            return true;
        } else {
            float scrollY = target.getScrollY();
            if (direction < 0) {
                if (scrollY > 0) {
                    return true;
                }
            } else if (direction > 0) {
                if (scrollY < 0) {
                    return true;
                }
            }
            return ViewCompat.canScrollVertically(target, -direction);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isIntercept = false;
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            resetMotionEvent();
            mDownEvent = MotionEvent.obtain(ev);
            mLastMotionX = ev.getX();
            mLastMotionY = ev.getY();
            mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
        } else if (action == MotionEvent.ACTION_MOVE) {
            final int activePointerId = mActivePointerId;
            if (activePointerId != INVALID_POINTER) {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                final float dx = x - mLastMotionX;
                final float xDiff = Math.abs(dx);
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float yDiff = Math.abs(y - mLastMotionY);
                if (xDiff > yDiff) {
                    isIntercept = true;
                }
                PagerViewAdapter adapter = mCurrentPagerAdapter;
                if (isIntercept && null != adapter) {
                    isIntercept = !adapter.canScrollHorizontally((int) (MotionEventCompat.getX(mDownEvent, pointerIndex) - x), getCurrentItem(), mDownEvent);
                    if (isIntercept) {
                        mLastMotionX = x;
                        mLastMotionY = y;
                        onTouchEvent(mDownEvent);
                    }
                }
            }
        } else {
            resetMotionEvent();
        }

        return isIntercept;
    }

    public int getCurrentItem() {
        return mCurrentIndex;
    }


    private void resetMotionEvent() {
        mLastMotionX = 0;
        mLastMotionY = 0;
        mActivePointerId = INVALID_POINTER;
        if (null != mDownEvent) {
            mDownEvent.recycle();
            mDownEvent = null;
        }
    }

    @Override
    protected int computeHorizontalScrollRange() {
        if (null != mCurrentPagerAdapter) {
            return getWidth() * mCurrentPagerAdapter.getCount();
        }
        return getWidth();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollToX(mScroller.getCurrX());
            postInvalidate();
        }
    }

    private void scrollToX(int x) {
        if (x < 0) {
            x = 0;
        } else if (x > getContentWidth()) {
            x = getContentWidth();
        }
        if (getScrollX() != x) {
            scrollTo(x, getScrollY());
        }
    }


    private int getContentWidth() {
        return (getChildCount() - 1) * (getWidth() - getPaddingLeft() - getPaddingRight());
    }

    private void scrollByX(int offsetX) {
        scrollToX(getScrollX() + offsetX);
    }

    public void scrollToPage(int index) {
        boolean changed = false;
        if (index < 0) {
            index = 0;
        } else if (index >= getChildCount()) {
            index = getChildCount() - 1;
        }
        if (getChildCount() == 0) {
            index = 0;
        }

        if (mCurrentIndex != index) {
            changed = true;
            onPageChanged(mCurrentIndex, index);
            mCurrentIndex = index;
        }

        final int offset = mCurrentIndex * getWidth() - getScrollX();
        if (offset != 0 || changed) {
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            mScroller.startScroll(getScrollX(), 0, offset, 0, ANIMATION_TIME);
            postInvalidate();
        }
    }

    protected void onPageChanged(int oldIndex, int currentIndex) {
        if (null != mCurrentPagerAdapter) {
            mCurrentPagerAdapter.onPageChanged(oldIndex, currentIndex);
        }

        if (null != mIndictor) {
            mIndictor.onScrolled(currentIndex);
        }

    }

    private void caculateCurrentScrollPage() {
        final int pageIndex = (getScrollX() + getWidth() / 2) / getWidth();
        scrollToPage(pageIndex);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean isActed = mDetector.onTouchEvent(event);
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (MotionEvent.ACTION_UP == action || MotionEvent.ACTION_CANCEL == action) {
            if (mFlingOffset == 0) {
                caculateCurrentScrollPage();
            } else {
                scrollToPage(mCurrentIndex + mFlingOffset);
            }
            resetMotionEvent();
        }
        if (isActed)
            mEdgeEffectTouchEvent.onTouchEvent(event);
        return isActed;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
        mDownScrollX = getScrollX();
        mFlingOffset = 0;
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (null != mIndictor) {
            mIndictor.draw(canvas);
        }
        mEdgeEffectTouchEvent.onDraw(canvas);
    }


    public void setIndictor(Indictor indictor) {
        if (mIndictor != indictor) {
            mIndictor = indictor;
            if (null != mIndictor) {
                mIndictor.setParent(this);
            }
        }
        if (null != mIndictor) {
            if (getWidth() != 0 && getHeight() != 0) {
                mIndictor.layout(getWidth(), getHeight(), getChildCount());
                mIndictor.onScrolled(mCurrentIndex);
            }
        }
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mFlingOffset = 0;
        float distance = mDownScrollX + e1.getRawX() - e2.getRawX();
        scrollToX((int) (distance));
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (velocityX < 0) {
            mFlingOffset = 1;
        } else if (velocityX > 0) {
            mFlingOffset = -1;
        }
        return true;
    }


    public void setPagerAdapter(PagerViewAdapter adapter) {
        if (mCurrentPagerAdapter != adapter) {
            if (null != mCurrentPagerAdapter) {
                mCurrentPagerAdapter.setPagerView(null);
            }
            mCurrentPagerAdapter = adapter;
            if (null != mCurrentPagerAdapter) {
                mCurrentPagerAdapter.setPagerView(this);
                mCurrentPagerAdapter.notifyDataSetChanged();
                mCurrentPagerAdapter.onPageChanged(0, mCurrentIndex);
            }
        }
    }


    private void notifyDataSetChanged(PagerViewAdapter adapter) {
        if (mCurrentPagerAdapter == adapter) {
            final int size = mCurrentPagerAdapter.getCount();
            if (size < getChildCount()) {
                for (int i = getChildCount() - 1; i >= size; i--) {
                    removeView(getChildAt(i));
                }
            }
            for (int i = 0; i < size; i++) {
                View oldView = getChildView(i);
                View view = mCurrentPagerAdapter.getView(oldView, i);
                if (null == oldView) {
                    addView(view);
                }
            }
            scrollToPage(mCurrentIndex);
        }
    }

    public View getChildView(int index) {
        if (index < getChildCount()) {
            return getChildAt(index);
        } else {
            return null;
        }
    }


    public static abstract class PagerViewAdapter {
        protected PagerView mPagerView;


        protected abstract boolean canScrollHorizontally(int direction, int currentPosition, MotionEvent downEvent);

        void setPagerView(PagerView pv) {
            mPagerView = pv;
        }

        public abstract int getCount();

        public abstract View getView(View contentView, int index);

        public void notifyDataSetChanged() {
            if (null != mPagerView) {
                mPagerView.notifyDataSetChanged(this);
            }
        }

        protected void onPageChanged(int oldIndex, int currentIndex) {

        }
    }


    public static abstract class Indictor {

        protected PagerView mParent;

        protected float mLeft, mTop;
        protected Rect mDrawingRect = new Rect();

        void setParent(PagerView view) {
            mParent = view;
        }

        protected abstract void layout(int parentW, int parentH, int childCounts);


        protected abstract void onScrolled(int position);


        void draw(Canvas canvas) {
            canvas.save();
            canvas.translate(mLeft + mParent.getScrollX(), mTop + mParent.getScrollY());
            onDraw(canvas);
            canvas.restore();
        }

        protected abstract void onDraw(Canvas canvas);

        public void translateTo(float px, float py) {
            if (px != mLeft || py != mTop) {
                invalidate();
                mLeft = px;
                mTop = py;
            }
        }

        public Rect getDrawingRect() {
            return mDrawingRect;
        }


        public void invalidate() {
            if (null != mParent && !mDrawingRect.isEmpty()) {
                Rect rect = new Rect(mDrawingRect.left, mDrawingRect.top, mDrawingRect.right + 1, mDrawingRect.bottom + 1);
                rect.offset((int) mLeft, (int) mTop);
                mParent.invalidate(mDrawingRect.left, mDrawingRect.top, mDrawingRect.right, mDrawingRect.bottom);
            }
        }


        public void postInvalidate() {
            if (null != mParent && !mDrawingRect.isEmpty()) {
                Rect rect = new Rect(mDrawingRect.left, mDrawingRect.top, mDrawingRect.right + 1, mDrawingRect.bottom + 1);
                rect.offset((int) mLeft, (int) mTop);
                mParent.postInvalidate(mDrawingRect.left, mDrawingRect.top, mDrawingRect.right, mDrawingRect.bottom);
            }
        }
    }

    private EdgeEffectTouchEvent mEdgeEffectTouchEvent;


    private class EdgeEffectTouchEvent {
        // for edgeEffect
        private float mDownScrollX;
        private float mDownEventX;
        private int mActivePointerId;
        private float mTouchSlop;

        private boolean isEdgeScrolling;

        private EdgeEffect mLeftEdge, mRightEdge;
        private float mFirstOffset = -Float.MAX_VALUE;
        private float mLastOffset = Float.MAX_VALUE;

        public EdgeEffectTouchEvent(Context context) {
            final ViewConfiguration configuration = ViewConfiguration.get(context);
            mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
            mLeftEdge = new EdgeEffect(getContext());
            mRightEdge = new EdgeEffect(getContext());
        }


        void onLayout(boolean changed, int l, int t, int r, int b) {
            if (changed) {
                mLeftEdge.setSize(getHeight() - getPaddingTop() - getPaddingBottom(), getWidth());
                mLeftEdge.setColor(0xDBDBDB);
                mRightEdge.setSize(getHeight() - getPaddingTop() - getPaddingBottom(), getWidth());
                mRightEdge.setColor(0xDBDBDB);
            }
        }


        void onDraw(Canvas canvas) {
            boolean needsInvalidate = false;
            if (!mLeftEdge.isFinished()) {
                final int restoreCount = canvas.save();
                final int height = getHeight() - getPaddingTop() - getPaddingBottom();

                canvas.rotate(270);
                canvas.translate(-height + getPaddingTop(), 0);
                needsInvalidate |= mLeftEdge.draw(canvas);
                canvas.restoreToCount(restoreCount);
            }
            if (!mRightEdge.isFinished()) {
                final int restoreCount = canvas.save();
                canvas.rotate(90);
                canvas.translate(getPaddingTop(), -getContentWidth() - getWidth());
                needsInvalidate |= mRightEdge.draw(canvas);
                canvas.restoreToCount(restoreCount);
            }
            if (needsInvalidate) {
                // Keep animating
                ViewCompat.postInvalidateOnAnimation(PagerView.this);
            }
        }

        void onTouchEvent(MotionEvent ev) {
            final int action = ev.getAction() & MotionEvent.ACTION_MASK;
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    isEdgeScrolling = false;
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    mDownEventX = ev.getX();
                    mDownScrollX = getScrollX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    float dx = MotionEventCompat.getX(ev, pointerIndex) - mDownEventX;
                    if (isEdgeScrolling || dx < -mTouchSlop || dx > mTouchSlop) {
                        float x = mDownScrollX - dx;
                        if (x < 0) {
                            mFirstOffset = x / getWidth();
                            mLeftEdge.onPull(mFirstOffset);
                            isEdgeScrolling = true;
                        } else if (x > getContentWidth()) {
                            x = x - getContentWidth();
                            mLastOffset = x / getWidth();
                            mRightEdge.onPull(mLastOffset);
                            isEdgeScrolling = true;
                        }
                        if (isEdgeScrolling)
                            postInvalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (!mLeftEdge.isFinished())
                        mLeftEdge.onRelease();
                    if (!mRightEdge.isFinished())
                        mRightEdge.onRelease();
                    break;
            }
        }

    }
}
