package com.android.camera.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import com.android.camera.debug.Log;
import com.android.camera.util.Gusterpolator;
import com.tct.camera.R;

import java.util.List;

/**
 * Created by sdduser on 10/22/15.
 */
public class ModeScrollBar extends View {

    private final Log.Tag TAG = new Log.Tag("ModeScrollBar");

    private int mScreenWidth;
    private int barCenter;
    private int barWidth;
    private int barLeft;
    private int barRight;

    ColorDrawable selectBar;
    ColorDrawable leftBar;
    ColorDrawable rightBar;

    private List<Integer> mItemWidthList;
    private final int ITEM_LIST_NOT_INIT = -1;
    private final int INDEX_OUT_OF_BOUNDS = -2;

    //PHASE_ONE refers to the animation of sliding to the target position while the PHASE_TWO referes to the sliding back;
    private final static int ANIM_PHASE_ONE_DURATION=300;
    private final static int ANIM_PHASE_TWO_DURATION=250;

    public ModeScrollBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        selectBar = new ColorDrawable(
                getResources().getColor(R.color.mode_scroll_bar_color_select));
        leftBar = new ColorDrawable(
                getResources().getColor(R.color.mode_scroll_bar_color_background));
        rightBar = new ColorDrawable(
                getResources().getColor(R.color.mode_scroll_bar_color_background));
    }

    public interface onBarStatueChangedListener {
        void onScrollStarted();
        void onItemReached();
        void onEndArrived();
        void onScrollFinished();
    }

    public void setItemWidth(List list) {
        mItemWidthList = list;
    }

    private int getItemWidth(int index) {
        if (mItemWidthList == null) {
            return ITEM_LIST_NOT_INIT;
        }
        if (index >=0 && index < mItemWidthList.size()) {
            return mItemWidthList.get(index);
        } else {
            return INDEX_OUT_OF_BOUNDS;
        }
    }

    private boolean checkValue(int sp, int ep, int sw, int ew) {
        return ((sp > 0) && (ep > 0) && (sw > 0) && (ew > 0));
    }

    public void setOriIndex(int index) {
        if (mScreenWidth == 0) {
            mScreenWidth = getMeasuredWidth();
        }
        updateWidth(index);
    }

    public void updateWidth(int index) {
        int width = getItemWidth(index);
        if (width > 0) {
            barLeft = mScreenWidth / 2 - width / 2;
            barRight = mScreenWidth / 2 + width / 2;
            invalidate();
        }
    }

    public void scrollToLeft(int index, onBarStatueChangedListener listener) {
        if (index < 1) {
            return;
        }
        int startPos = mScreenWidth / 2;
        int startWidth = getItemWidth(index);
        int endWidth = getItemWidth(index - 1);
        int endPos = startPos - (startWidth + endWidth) / 2;
        if (checkValue(startPos, endPos, startWidth, endWidth)) {
            startScrollAnimation(startPos, endPos, startWidth, endWidth, listener);
        }
    }

    public void scrollToRight(int index, onBarStatueChangedListener listener) {
        if (mItemWidthList == null || index > (mItemWidthList.size() - 2)) {
            return;
        }
        int startPos = mScreenWidth / 2;
        int startWidth = getItemWidth(index);
        int endWidth = getItemWidth(index + 1);
        int endPos = startPos + (startWidth + endWidth) / 2;
        if (checkValue(startPos, endPos, startWidth, endWidth)) {
            startScrollAnimation(startPos, endPos, startWidth, endWidth, listener);
        }
    }

    public void scrollToItem(int current, int target, int pos, onBarStatueChangedListener listener) {
        int startPos = mScreenWidth / 2;
        int startWidth = getItemWidth(current);
        int endWidth = getItemWidth(target);
        int endPos = pos;
        if (checkValue(startPos, endPos, startWidth, endWidth)) {
            startScrollAnimation(startPos, endPos, startWidth, endWidth, listener);
        }
    }

    AnimatorSet mAnimator;
    private void startScrollAnimation(final float startPosition, final float targetPosition, float startWidth, final float targetWidth, final onBarStatueChangedListener listener){
        if(mAnimator!=null&&mAnimator.isRunning()){
            return;
        }
        mAnimator=new AnimatorSet();
        ValueAnimator widthAnimator=ValueAnimator.ofFloat(startWidth,targetWidth);
        widthAnimator.setDuration(ANIM_PHASE_ONE_DURATION);
        ValueAnimator posStartAnimator=ValueAnimator.ofFloat(startPosition, targetPosition);
        posStartAnimator.setDuration(ANIM_PHASE_ONE_DURATION);
        ValueAnimator posEndAnimator=ValueAnimator.ofFloat(targetPosition,startPosition);
        posEndAnimator.setDuration(ANIM_PHASE_TWO_DURATION);
        widthAnimator.setInterpolator(Gusterpolator.INSTANCE);
        posStartAnimator.setInterpolator(Gusterpolator.INSTANCE);
        posEndAnimator.setInterpolator(Gusterpolator.INSTANCE);

        final boolean headingLeft=(targetPosition-startPosition<0);

        widthAnimator.addUpdateListener(mWidthAnimatorListener);

        ValueAnimator.AnimatorUpdateListener positionAnimatorListener=new ValueAnimator.AnimatorUpdateListener() {
            private boolean isItemReachCallbacked=false;

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                barCenter=(int)((float)valueAnimator.getAnimatedValue());
                barLeft = barCenter - (barWidth / 2);
                barRight = barCenter + (barWidth / 2);
                if(!isItemReachCallbacked) {
                    if (headingLeft && barLeft < targetPosition + targetWidth / 2) {
                        listener.onItemReached();
                        isItemReachCallbacked = true;
                    } else if (!headingLeft && barRight > targetPosition - targetWidth / 2) {
                        listener.onItemReached();
                        isItemReachCallbacked = true;
                    }
                }
                invalidate();
            }
        };

        posStartAnimator.addUpdateListener(positionAnimatorListener);
        posEndAnimator.addUpdateListener(positionAnimatorListener);

        posEndAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(listener!=null){
                    listener.onScrollFinished();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        AnimatorSet phaseStartAnimation=new AnimatorSet();
        phaseStartAnimation.playTogether(widthAnimator, posStartAnimator);

        phaseStartAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                if(listener!=null) {
                    listener.onScrollStarted();
                }
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(listener!=null){
                    listener.onEndArrived();
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        mAnimator.playSequentially(phaseStartAnimation, posEndAnimator);

        mAnimator.start();
    }

    private ValueAnimator.AnimatorUpdateListener mWidthAnimatorListener=new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            barWidth=(int)((float)valueAnimator.getAnimatedValue());
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        int height=canvas.getHeight();
        selectBar.setBounds(barLeft, 0, barRight, height);
        selectBar.draw(canvas);
        leftBar.setBounds(0, 0, barLeft, height);
        leftBar.draw(canvas);
        rightBar.setBounds(barRight, 0, mScreenWidth, height);
        rightBar.draw(canvas);
        canvas.restore();
        super.onDraw(canvas);
    }

    public void hide() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ModeScrollBar.this.setVisibility(View.INVISIBLE);
        } else {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    ModeScrollBar.this.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    public void show() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ModeScrollBar.this.setVisibility(View.VISIBLE);
        } else {
            getHandler().post(new Runnable() {
                @Override
                public void run() {
                    ModeScrollBar.this.setVisibility(View.VISIBLE);
                }
            });
        }
    }
}
