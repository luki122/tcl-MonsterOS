/*Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.transfer.fasttransfer;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import cn.tcl.transfer.R;

public class SendBackground extends RelativeLayout {
    private Context mContext;
    private ImageView mArrow;
    private ImageView mBubble1;
    private ImageView mBubble2;
    private ArrayList<Animator> arrowAnimList = new ArrayList<Animator>();
    private AnimatorSet animatorSet;
    private Animator.AnimatorListener lastAnimatorListener;
    private ValueAnimator mBubbleAnim1;
    private ValueAnimator mBubbleAnim2;
    private LayoutParams bubbleParams;
    private LayoutParams arrowParams;
    private boolean animationRunning=false;

    public SendBackground(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public SendBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public SendBackground(Context context) {
        super(context);
        mContext = context;
    }
    public void init(final Context context) {
        bubbleParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        arrowParams = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        mArrow = new ImageView(context);
        mArrow.setImageResource(R.drawable.arrow_up);
        mBubble1 = new ImageView(context);
        mBubble1.setImageResource(R.drawable.bubble);
        mBubble2 = new ImageView(context);
        mBubble2.setImageResource(R.drawable.bubble);
        addView(mArrow,arrowParams);
        addView(mBubble1,bubbleParams);
        addView(mBubble2,bubbleParams);
        mBubbleAnim1 = ObjectAnimator.ofFloat(mBubble1, "translationY", getHeight(), -2*getHeight());
        mBubbleAnim1.setRepeatCount(ObjectAnimator.INFINITE);
        mBubbleAnim1.setRepeatMode(ObjectAnimator.RESTART);
        mBubbleAnim1.setDuration(3000);
        mBubbleAnim2 = ObjectAnimator.ofFloat(mBubble2, "translationY", 2*getHeight(), -getHeight());
        mBubbleAnim2.setRepeatCount(ObjectAnimator.INFINITE);
        mBubbleAnim2.setRepeatMode(ObjectAnimator.RESTART);
        mBubbleAnim2.setDuration(3000);
        ValueAnimator arrowAnim1 = ObjectAnimator.ofFloat(mArrow, "translationY", getHeight(), 0);
        arrowAnim1.setDuration(750);
        ValueAnimator arrowAnim2 = ObjectAnimator.ofFloat(mArrow, "translationY", 0, getHeight()/20);
        arrowAnim2.setDuration(250);
        ValueAnimator arrowAnim3 = ObjectAnimator.ofFloat(mArrow, "translationY", getHeight()/20, 0);
        arrowAnim3.setDuration(250);
        ValueAnimator arrowAnim4 = ObjectAnimator.ofFloat(mArrow, "translationY", 0, getHeight()/20);
        arrowAnim4.setDuration(250);
        ValueAnimator arrowAnim5 = ObjectAnimator.ofFloat(mArrow, "translationY", getHeight()/20, 0);
        arrowAnim5.setDuration(250);
        ValueAnimator arrowAnim6 = ObjectAnimator.ofFloat(mArrow, "translationY", 0, getHeight()/20);
        arrowAnim6.setDuration(250);
        ValueAnimator arrowAnim7 = ObjectAnimator.ofFloat(mArrow, "translationY", getHeight()/20, -getHeight());
        arrowAnim7.setDuration(1000);
        arrowAnimList.clear();
        arrowAnimList.add(arrowAnim1);
        arrowAnimList.add(arrowAnim2);
        arrowAnimList.add(arrowAnim3);
        arrowAnimList.add(arrowAnim4);
        arrowAnimList.add(arrowAnim5);
        arrowAnimList.add(arrowAnim6);
        arrowAnimList.add(arrowAnim7);
        lastAnimatorListener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animatorSet.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        };
        animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.playSequentially(arrowAnimList);
        animatorSet.addListener(lastAnimatorListener);
    }
    public void startRippleAnimation(){
        if(!isRippleAnimationRunning()){
            animatorSet.start();
            mBubbleAnim1.start();
            mBubbleAnim2.start();
            animationRunning=true;
        }
    }

    public void stopRippleAnimation(){
        if(isRippleAnimationRunning()){
            animationRunning=false;
            animatorSet.end();
            mBubbleAnim1.end();
            mBubbleAnim2.end();
        }
    }

    public boolean isRippleAnimationRunning(){
        return animationRunning;
    }
}
