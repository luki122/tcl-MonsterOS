/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.meetingassistant.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import cn.tcl.meetingassistant.log.MeetingLog;

/**
 * Created on 16/9/22.
 */

public class DecisionPageScrollLayout extends LinearLayout {

    private final String TAG = DecisionPageScrollLayout.class.getSimpleName();

    private final int MAX_ANIMATION_DURATION = 200;
    private boolean mListScrollTop = true;
    private Activity mActivity;
    private int mHalfHeight;
    private int mFullHeight;
    private int SCROLL_GATE = 200;
    private float TOUCH_INTERCEPT_GATE = 20;
    private float mTouchDistance;
    float mStartY;
    float mLastY;
    float mStartX;
    long mLastTime;
    boolean isAnimating = false;
    private RecyclerView mRecyclerView;
    private OnScrollProgressListener mOnScrollProgressListener;
    private OnScrolledTopListener mOnScrolledTopListener;
    private OnScrolledHalfListener mOnScrolledHalfListener;
    private OnScrolledOffFromTop mOnScrolledOffFromTop;
    private float mSpeed = 0;

    private enum Status {
        Top, Half
    }
    private Status mCurrentStatus;

    public DecisionPageScrollLayout(Context context) {
        this(context, null);
    }

    public DecisionPageScrollLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DecisionPageScrollLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCurrentStatus = Status.Half;
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        MeetingLog.i(TAG,"onInterceptTouchEvent start");
        MeetingLog.i(TAG,"onInterceptTouchEvent action is" + ev.getAction());
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mStartY = ev.getRawY();
            mStartX = ev.getRawX();
            mLastY = ev.getRawY();
            mLastTime = System.currentTimeMillis();
            mTouchDistance = 0;
        }
        boolean isMoreThanGate = false;
        if(ev.getAction() == MotionEvent.ACTION_MOVE){
            double TouchDistancePow = Math.pow(ev.getRawX() - mStartX,2) + Math.pow(ev.getRawY() - mStartY,2);
            if(TouchDistancePow >  Math.pow(TOUCH_INTERCEPT_GATE,2)){
                isMoreThanGate = true;
            }
        }
        boolean intercept;
        if(!isMoreThanGate){
            intercept = super.onInterceptTouchEvent(ev);
        }
        else if(ev.getAction() == MotionEvent.ACTION_MOVE && mCurrentStatus != Status.Top){
            intercept = true;
        }
        else if(isAnimating){
            intercept = true;
        }

        else if (mCurrentStatus == Status.Top && ev.getAction() == MotionEvent.ACTION_MOVE && ev.getRawY() - mStartY > 0 &&
                mListScrollTop) {
            intercept = true;
        } else {
            intercept = super.onInterceptTouchEvent(ev);
        }
        MeetingLog.i(TAG,"onInterceptTouchEvent " + intercept);
        MeetingLog.i(TAG,"onInterceptTouchEvent end");
        return intercept;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartY = event.getRawY();
                mLastY = event.getRawY();
                mLastTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                float cY = event.getRawY();
                float dy = cY - mLastY;
                long now = System.currentTimeMillis();
                mSpeed = dy / (now - mLastTime);
                mLastY = event.getRawY();
                mLastTime = System.currentTimeMillis();
                if (getY() <= 0 && dy < 0) {
                    scrollToY(0);
                    return false;
                }

                else if (dy < 0 && getY() > 0 && getY() + dy <= 0) {
                    scrollToY(0);
                    return false;
                }

                else if (dy < 0 && getY() > 0 && getY() + dy > 0) {
                    scrollToY(getY() + dy);
                }

                else if (dy > 0) {
                   scrollToY(getY() + dy);

                }

                break;
            case MotionEvent.ACTION_UP:

            case MotionEvent.ACTION_CANCEL:

                if (mCurrentStatus == Status.Half) {
                    if (mLastY - mStartY < -SCROLL_GATE) {
                        scrollToTopFromHalf();
                    } else if (mLastY - mStartY < 0 && mLastY - mStartY > -SCROLL_GATE) {
                        scrollToHalfFromTop();
                    } else if (mLastY - mStartY > SCROLL_GATE) {
                        dismiss();
                    } else {
                        dismiss();
                    }
                } else if (mCurrentStatus == Status.Top) {
                    if (mLastY - mStartY < 0) {
                        scrollToTopFromHalf();
                    } else if (mLastY - mStartY > SCROLL_GATE) {
                      //  scrollToHalfFromTop();
                        dismiss();
                    } else {
                        scrollToTopFromHalf();
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    int mItemHeight = 0;
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    private void scrollToTopFromHalf() {
        if (isAnimating) {
            return;
        }
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1f, 0f);
        int duration = (int) (getY() / Math.abs(mSpeed));
        if(mSpeed < 1){
            duration = MAX_ANIMATION_DURATION;
        }
        final float startY = getY();
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float f = (float) valueAnimator.getAnimatedValue();
                scrollToY(startY * f);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimating = false;
                scrollToY(0);
                mCurrentStatus = Status.Top;
                if(mOnScrolledTopListener != null){
                    mOnScrolledTopListener.onScrolledTop();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        valueAnimator.start();
    }

    public void changeHeightHalf1ToHalf2(final int newHalfHeight) {
        if (isAnimating) {
            return;
        }
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        int duration = MAX_ANIMATION_DURATION;
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float f = (float) valueAnimator.getAnimatedValue();
                scrollToY(mFullHeight - mHalfHeight + (mHalfHeight - newHalfHeight) * f);
                MeetingLog.d(TAG,"scroll to " + (mFullHeight - mHalfHeight - mItemHeight * f) );
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimating = false;
                mHalfHeight = newHalfHeight;
                scrollToY(mFullHeight - newHalfHeight);
                mCurrentStatus = Status.Half;
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        valueAnimator.start();
    }

    private void scrollToHalfFromTop() {
        if (isAnimating) {
            return;
        }
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        int duration = Math.abs((int) ((mFullHeight - mHalfHeight - getY()) / mSpeed));
        if(mSpeed < 1){
            duration = MAX_ANIMATION_DURATION;
        }
        final float startY = getY();
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float f = (float) valueAnimator.getAnimatedValue();
                scrollToY(startY - (startY - mFullHeight + mHalfHeight) * f);
            }
        });
        valueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimating = false;
                scrollToY(mFullHeight - mHalfHeight);
                mCurrentStatus = Status.Half;
                if(mOnScrolledHalfListener != null){
                    mOnScrolledHalfListener.onScrolledHalf();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                isAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        valueAnimator.start();
    }

    private void scrollToY(float y) {
        setY(y);
        if(mOnScrollProgressListener != null){
            float progress = (mFullHeight - mHalfHeight - y)  /(mFullHeight - mHalfHeight);
            mOnScrollProgressListener.progress(progress);
        }
        if(y > 0 && mOnScrolledOffFromTop != null){
            mOnScrolledOffFromTop.onScrolledOff();
        }
    }


    public void setHalfHeight(int halfHeight) {
        this.mHalfHeight = halfHeight;
        if(mCurrentStatus == Status.Half){
            changeHeightHalf1ToHalf2(mHalfHeight);
        }
    }

    public void setFullHeight(int fullHeight) {
        this.mFullHeight = fullHeight;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mRecyclerView.canScrollVertically(-1))
                    mListScrollTop = false;
                else mListScrollTop = true;
            }
        });
    }

    public void dismiss() {
        if (mActivity != null) {
            mActivity.onBackPressed();
        }
    }

    public interface OnScrollProgressListener{
        /**
         *
         * @param progress 1->top 0->bottom
         */
        void progress(float progress);
    }

    public void setOnScrollProgressListener(OnScrollProgressListener onScrollProgressListener) {
        this.mOnScrollProgressListener = onScrollProgressListener;
    }

    public interface OnScrolledTopListener{
        void onScrolledTop();
    }

    public interface OnScrolledOffFromTop{
        void onScrolledOff();
    }

    public interface OnScrolledHalfListener{
        void onScrolledHalf();
    }

    public void setOnScrolledOffFromTop(OnScrolledOffFromTop onScrolledOffFromTop) {
        this.mOnScrolledOffFromTop = onScrolledOffFromTop;
    }

    public void setOnScrolledTopListener(OnScrolledTopListener onScrolledTopListener) {
        this.mOnScrolledTopListener = onScrolledTopListener;
    }

    public void setOnScrolledHalfListener(OnScrolledHalfListener onScrolledHalfListener) {
        this.mOnScrolledHalfListener = onScrolledHalfListener;
    }
}
