/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.weather.viewhelper;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.leon.tools.view.AndroidUtils;
import com.leon.tools.view.UiController;

import cn.tcl.weather.R;
import cn.tcl.weather.WeatherCNApplication;
import cn.tcl.weather.utils.IBoardcaster;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-12.
 * $desc
 */
public class VerticalScrollBehavior extends CoordinatorLayout.Behavior<View> implements GestureDetector.OnGestureListener {

    public final static String ACTION = "scroll_up_down_action";
    public final static String ACTION_MOVING = "scroll_moving";
    public final static String PROGRESS_KEY = "progress";

    public final static int MESSAGE_WHAT_SHOW = 0;
    public final static int MESSAGE_WHAT_HIDE = 1;

    private static final int INVALID_POINTER = -1;
    private int mActivePointerId = INVALID_POINTER;
    private final static int ANIMATION_TIME = 300;
    private final float SCALE_PX = 1.5f;
    private final static float TEMP_SCOLLED_SCALE = 0.5f;

    private static int mCurrentState = MESSAGE_WHAT_SHOW;

    private float mLastMotionX;
    private float mLastMotionY;

    private boolean isDraging;
    private CoordinatorLayout mParent;
    private View mChild;
//    private float mMaxHeightScale = 0.6f;

    private GestureDetector mDetctor;

    private MotionEvent mDownEvent;

    private int mMaxScrollingHeight;

    private float mTempratureScrolledY;

    private UiController mUiController;

    private ObjectAnimator mObjectAnimator;

    private final static int STATE_NULL = 0;
    private final static int STATE_SCROLL = 1;
    private final static int STATE_FLING = 2;


    private int mScrollState = STATE_NULL;


    public VerticalScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mDetctor = new GestureDetector(this);
        WeatherCNApplication.getWeatherCnApplication().regiestOnReceiver(ACTION, mStatausReceiver);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        if (null == mUiController) {
            mUiController = new UiController(parent);
        }
        mParent = parent;
        mChild = child;
        return dependency.getId() == R.id.tcl_weather_sv;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {

        View animTemp = mUiController.findViewById(R.id.city_weather_anim_temp);
//        final float animTempScrolledH = animTemp.getHeight() * TEMP_SCOLLED_SCALE;
        View cityName = mUiController.findViewById(R.id.city_weather_city_name);
        mMaxScrollingHeight = (int) (animTemp.getTop() - AndroidUtils.dip2px(parent.getContext(), 35f));// cacaulte the scrollY of parent
//        mTempratureScrolledY = mMaxScrollingHeight - animTemp.getTop() - 30;//caculate the translationY of temp

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        if (null == params)
            params = new CoordinatorLayout.LayoutParams(parent.getWidth(), ViewGroup.LayoutParams.MATCH_PARENT);
        else
            params.width = parent.getWidth();
        child.setLayoutParams(params);
        params = (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();
        if (null == params) {
            params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        params.height = parent.getHeight() + mMaxScrollingHeight - child.getHeight();
        dependency.setLayoutParams(params);
        dependency.setTranslationY(child.getHeight());

        setShowState(mCurrentState, false);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {

        if (!isDraging) {
            final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
            // Always take care of the touch gesture being complete.
            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                resetMotionEvent();
                return false;
            }

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    resetMotionEvent();
                    mLastMotionX = ev.getX();
                    mLastMotionY = ev.getY();
                    mDownEvent = MotionEvent.obtain(ev);
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    break;
                case MotionEvent.ACTION_MOVE:
                    final int activePointerId = mActivePointerId;
                    if (activePointerId == INVALID_POINTER) {
                        // If we don't have a valid id, the touch down wasn't on content.
                        break;
                    }

                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float dx = x - mLastMotionX;
                    final float xDiff = Math.abs(dx);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);
                    final float yDiff = Math.abs(y - mLastMotionY);
                    if (yDiff > xDiff * 2) {
                        isDraging = isInterceptTouchEvent(parent, child, y - mLastMotionY);
                        if (isDraging) {
                            mLastMotionX = x;
                            mLastMotionY = y;
                        }
                    }
                    break;
            }

        }

        if (ev.getPointerCount() > 1) {
            isDraging = true;
        }
        return isDraging;
    }


    private boolean canScrollVeiwScrolling(int direction) {
        ScrollView sc = mUiController.findViewById(R.id.tcl_weather_sv);
        return sc.canScrollVertically(direction);
    }

    private void resetMotionEvent() {
        isDraging = false;
        if (null != mDownEvent) {
            mDownEvent.recycle();
            mDownEvent = null;
        }
    }


    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {
        if (null == mUiController) {
            mUiController = new UiController(parent);
        }

        if (isDraging) {
            mDetctor.onTouchEvent(mDownEvent);
            resetMotionEvent();
        }

        final boolean isTouched = mDetctor.onTouchEvent(ev);
        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        if (MotionEvent.ACTION_UP == action || MotionEvent.ACTION_CANCEL == action) {
            resetMotionEvent();
            if (mScrollState == STATE_SCROLL) {
                final float sy = mParent.getScrollY();
                if (sy > 0 && sy < mMaxScrollingHeight) {
                    final float ey;
                    if (sy < mMaxScrollingHeight / 2.0f) {// if less than a half, back to 0
                        ey = 0;
                    } else {
                        ey = mMaxScrollingHeight;
                    }
                    startObjectAnimator(sy, ey);
                }
            }
        }
        return isTouched;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        mScrollState = STATE_NULL;
        onDown(mParent, mChild);
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mScrollState != STATE_FLING) {
            mScrollState = STATE_SCROLL;
        }
        onScrollVertical(mParent, distanceY);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        mScrollState = STATE_FLING;
        onFlingVertical(mParent, mChild, e1, e2, velocityY);
        return true;
    }

    private float mScrolly;

    protected void onDown(CoordinatorLayout parent, View child) {
    }

    protected boolean isInterceptTouchEvent(CoordinatorLayout parent, View child, float distanceY) {
        final int sy = parent.getScrollY();
        if (distanceY > 0) {
            if (canScrollVeiwScrolling(-1))
                return false;
            return sy > 0;
        } else {
            return sy < mMaxScrollingHeight;
        }
    }

    /**
     * scroll parent Y
     *
     * @param parent
     * @param distanceY
     * @return
     */
    protected final boolean onScrollVertical(CoordinatorLayout parent, float distanceY) {
        mScrolly = parent.getScrollY();

        float scy = mScrolly + distanceY / SCALE_PX;
        if (scy < 0)
            scy = 0;
        else if (scy > mMaxScrollingHeight) {
            scy = mMaxScrollingHeight;
        }
        if (parent.getScrollY() != scy) {
            setScrollY(scy);
            return true;
        }
        return false;
    }

    private void setScrollY(float sy) {
        setScrollY(sy, true);
    }


    private void setScrollY(float sy, boolean sendMsg) {
        if (null == mUiController) {
            return;
        }
        mUiController.getView().scrollTo(0, (int) sy);
        final float progress = sy / mMaxScrollingHeight;
        float alpha = 1f - progress;
        mUiController.findViewById(R.id.city_weather_ll_cardlayout).setAlpha(alpha);
        mUiController.findViewById(R.id.city_weather_city_name).setTranslationY(-progress * AndroidUtils.dip2px(mParent.getContext(), 100f / 3));

        mUiController.findViewById(R.id.city_weather_anim_temp).setAlpha(1 - alpha);
        mUiController.findViewById(R.id.city_weather_anim_temp).setTranslationY(progress * AndroidUtils.dip2px(mParent.getContext(), 30f / 3));

        mUiController.findViewById(R.id.city_weather_pfv).setTranslationY((int) sy);
        mUiController.findViewById(R.id.weather_head_view).setTranslationY(-(int) sy + progress * AndroidUtils.dip2px(mParent.getContext(), 30f / 3));

        if (progress < 0.002f) {
            if (progress == 0f && sendMsg) {
                Message msg = Message.obtain();
                msg.what = MESSAGE_WHAT_SHOW;
                WeatherCNApplication.getWeatherCnApplication().sendMessage(ACTION, msg);
            }
        } else {
            if (progress == 1.0f && sendMsg) {
                Message msg = Message.obtain();
                msg.what = MESSAGE_WHAT_HIDE;
                WeatherCNApplication.getWeatherCnApplication().sendMessage(ACTION, msg);
            }
        }

        sendActionMovingMessage(sy, progress);
    }


    private void sendActionMovingMessage(float sy, float progress) {
        Message msg = Message.obtain();
        msg.obj = sy;
        Bundle bundle = new Bundle();
        bundle.putFloat(PROGRESS_KEY, progress);
        msg.setData(bundle);
        WeatherCNApplication.getWeatherCnApplication().sendMessage(ACTION_MOVING, msg);
    }


    protected void onFlingVertical(CoordinatorLayout parent, View child, MotionEvent e1, MotionEvent e2, float distanceY) {
        final float ey = distanceY < 0 ? mMaxScrollingHeight : 0;
        startObjectAnimator(parent.getScrollY(), ey);
    }


    private void startObjectAnimator(float start, float end) {
        if (null != mObjectAnimator) {
            mObjectAnimator.cancel();
        }
        mObjectAnimator = ObjectAnimator.ofFloat(this, "scrollY", start, end);
        mObjectAnimator.setDuration(ANIMATION_TIME);
        mObjectAnimator.start();
    }

    private void setShowState(int state, boolean sendMsg) {
        if (state == MESSAGE_WHAT_SHOW) {
            setScrollY(0, sendMsg);
        } else {
            setScrollY(mMaxScrollingHeight, sendMsg);
        }
    }


    private IBoardcaster.Receiver mStatausReceiver = new IBoardcaster.Receiver() {
        @Override
        public void onReceived(String action, Message msg) {
            setShowState(msg.what, false);
            mCurrentState = msg.what;
        }
    };


    private boolean isScrollup;

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child, View directTargetChild, View target, int nestedScrollAxes) {
        isScrollup = false;
        return target.getId() == R.id.tcl_weather_sv;
    }


    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (!isScrollup) {
            isScrollup = dyUnconsumed < 0;
        }
        if (isScrollup) {
            if (dyUnconsumed < 0) {//when child can not scroll up
                onScrollVertical(coordinatorLayout, dyUnconsumed);
                target.setScrollY(0);
            } else if (dyConsumed > 0) {
                if (onScrollVertical(coordinatorLayout, dyConsumed)) {// if parent is scrolling, child will not  scroll
                    target.setScrollY(0);
                }
            }
        }
    }


    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, View child, View target) {
        if (isScrollup) {
            final float sy = mParent.getScrollY();
            if (sy > 0 && sy < mMaxScrollingHeight) {
                final float ey;
                if (sy < mMaxScrollingHeight / 2.0f) {// if less than a half, back to 0
                    ey = 0;
                } else {
                    ey = mMaxScrollingHeight;
                }
                startObjectAnimator(sy, ey);
            }
        }
    }
}
