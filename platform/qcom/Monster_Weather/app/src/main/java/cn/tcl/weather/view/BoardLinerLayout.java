/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.view;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-11-8.
 * $desc
 */
public class BoardLinerLayout extends LinearLayout implements GestureDetector.OnGestureListener {

    private final static float MAX_PY = 200f;

    private final static float K = 0.3f;

    private OnBoardLinerLayoutListener mOnBoardLinerLayoutListener;

    private IInterceptyTouchEvent.VerticalInterceptTouchEvent mIterceptyTouchEvent;

    private GestureDetector mGestureDetector = new GestureDetector(this);

    private ObjectAnimator mAnimation;


    public BoardLinerLayout(Context context) {
        super(context);
        init();
    }


    public BoardLinerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoardLinerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public BoardLinerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {
        mIterceptyTouchEvent = new IInterceptyTouchEvent.VerticalInterceptTouchEvent(this);
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        if (mIterceptyTouchEvent.onInterceptTouchEvent(ev)) {
//            mIterceptyTouchEvent.touchDownEvent();
//            return true;
//        }
//        return super.onInterceptTouchEvent(ev);
//    }

    private void startAnimation() {
        if (getTranslationY() > 0) {
            if (null == mAnimation || !mAnimation.isRunning()) {
                mAnimation = ObjectAnimator.ofFloat(this, "scrollToY", getTranslationY(), 0);
                mAnimation.setDuration(500);
                mAnimation.setInterpolator(new SpringInterpolator());
                mAnimation.start();
            }
        }
    }


    private void setScrollToY(float py) {
        if (py > MAX_PY)
            py = MAX_PY;
        setTranslationY(py);
        if (null != mOnBoardLinerLayoutListener)
            mOnBoardLinerLayoutListener.onScrollChanged(py);
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (null == mAnimation || !mAnimation.isRunning()) {
//            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
//                startAnimation();
//            }
//            return mGestureDetector.onTouchEvent(event);
//        }
//        return false;
//    }


//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (null == mAnimation || !mAnimation.isRunning()) {
//            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
//                startAnimation();
//            }
//            return mGestureDetector.onTouchEvent(event);
//        }
//        return false;
//    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (null != mOnBoardLinerLayoutListener)
            mOnBoardLinerLayoutListener.onLayout(changed, l, t, r, b);
    }

    public void setOnBoardLinerLayoutListener(OnBoardLinerLayoutListener l) {
        mOnBoardLinerLayoutListener = l;
    }

    @Override
    public boolean canScrollVertically(int direction) {
//        return true;
        return false;//no need to scroll, del snow animation
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        setScrollToY((motionEvent1.getRawY() - motionEvent.getRawY()) * K);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return true;
    }


    public class SpringInterpolator implements Interpolator {

        private float mTension = 2.0f;

        @Override
        public float getInterpolation(float input) {
            input -= 1.0f;
            return input * input * ((mTension + 1) * input + mTension) + 1.0f;
        }
    }

    public static interface OnBoardLinerLayoutListener {
        void onLayout(boolean changed, int l, int t, int r, int b);
        void onScrollChanged(float cy);
    }
}
