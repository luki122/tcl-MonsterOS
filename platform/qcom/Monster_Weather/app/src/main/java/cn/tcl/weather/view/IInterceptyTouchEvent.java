package cn.tcl.weather.view;

import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-9-9.
 * $desc
 */
public interface IInterceptyTouchEvent {

    boolean onInterceptTouchEvent(MotionEvent ev);


    class VerticalInterceptTouchEvent implements IInterceptyTouchEvent {
        private static final int INVALID_POINTER = -1;

        private View mView;
        private MotionEvent mDownEvent;
        private float mTouchSlop;
        private int mActivePointerId = INVALID_POINTER;
        private View mIgnoreView;
        private boolean isInIgnoreView;


        public VerticalInterceptTouchEvent(View view) {
            mView = view;
            mTouchSlop = ViewConfiguration.get(mView.getContext()).getScaledTouchSlop();
        }


        public void resetMotionEvent() {
            if (null != mDownEvent) {
                mDownEvent.recycle();
                mActivePointerId = INVALID_POINTER;
                mDownEvent = null;
                isInIgnoreView = false;
            }
        }

        public void setIgnoreView(View view) {
            mIgnoreView = view;
        }


        private void checkInIgnoreView(MotionEvent ev) {
            if (null != mIgnoreView && !isInIgnoreView) {
                int[] ps = new int[2];
                float x = ev.getRawX();
                float y = ev.getRawY();

                mIgnoreView.getLocationOnScreen(ps);
                RectF rect = new RectF(ps[0], ps[1], ps[0] + mIgnoreView.getWidth(), ps[1] + mIgnoreView.getHeight());
                isInIgnoreView = rect.contains(x, y);
            }
        }

        public boolean onInterceptTouchEvent(MotionEvent ev) {
            boolean isIntercept = false;
            switch (ev.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    resetMotionEvent();
                    mDownEvent = MotionEvent.obtain(ev);
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    checkInIgnoreView(mDownEvent);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mActivePointerId == INVALID_POINTER)
                        break;

                    float dx = MotionEventCompat.getX(mDownEvent, mActivePointerId) - MotionEventCompat.getX(ev, mActivePointerId);
                    float dy = MotionEventCompat.getY(mDownEvent, mActivePointerId) - MotionEventCompat.getY(ev, mActivePointerId);
                    if (Math.abs(dy) > mTouchSlop && Math.abs(dy) > Math.abs(dx)) {
                        if (isInIgnoreView) {
                            int direct = dy > 0 ? 1 : -1;// up or down
                            isIntercept = !mIgnoreView.canScrollVertically(direct);
                        } else {
                            isIntercept = true;
                        }
                    }
                    break;
                default:
                    resetMotionEvent();
                    break;
            }
            return isIntercept;
        }

        public void touchDownEvent() {
            mView.onTouchEvent(mDownEvent);
            resetMotionEvent();
        }
    }


    class HorizontalInterceptTouchEvent implements IInterceptyTouchEvent {
        private static final int INVALID_POINTER = -1;

        private View mView;
        private MotionEvent mDownEvent;
        private float mTouchSlop;
        private int mActivePointerId = INVALID_POINTER;


        public HorizontalInterceptTouchEvent(View view) {
            mView = view;
            mTouchSlop = ViewConfiguration.get(mView.getContext()).getScaledTouchSlop();
        }


        public void resetMotionEvent() {
            if (null != mDownEvent) {
                mDownEvent.recycle();
                mActivePointerId = INVALID_POINTER;
                mDownEvent = null;
            }
        }

        public boolean onInterceptTouchEvent(MotionEvent ev) {
            boolean isIntercept = false;
            switch (ev.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    resetMotionEvent();
                    mDownEvent = MotionEvent.obtain(ev);
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mActivePointerId == INVALID_POINTER)
                        break;

                    float dx = MotionEventCompat.getX(mDownEvent, mActivePointerId) - MotionEventCompat.getX(ev, mActivePointerId);
                    float dy = MotionEventCompat.getY(mDownEvent, mActivePointerId) - MotionEventCompat.getY(ev, mActivePointerId);
                    if (Math.abs(dx) > mTouchSlop && Math.abs(dx) > Math.abs(dy)) {
                        isIntercept = true;
                    }
                    break;
                default:
                    resetMotionEvent();
                    break;
            }
            return isIntercept;
        }

        public void touchDownEvent() {
            mView.onTouchEvent(mDownEvent);
            resetMotionEvent();
        }
    }
}
