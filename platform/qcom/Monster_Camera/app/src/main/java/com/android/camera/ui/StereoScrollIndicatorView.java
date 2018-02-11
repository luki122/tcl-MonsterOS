package com.android.camera.ui;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.android.camera.util.CameraUtil;
import com.android.camera.util.CustomFields;
import com.android.camera.util.CustomUtil;
import com.tct.camera.R;

/**
 * Created by sichao.hu on 11/23/15.
 */
public class StereoScrollIndicatorView extends View implements ScrollIndicator {


//    private ColorDrawable mIndicatorColorDrawable;
    private ValueAnimator mWidthAnimator;
    private ValueAnimator mTransAnimator;
    private AnimatorSet mAnimatorSet;
    private int mIndicatorWidth;
    private int mTransX=0;

    public StereoScrollIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
//        mIndicatorColorDrawable=new ColorDrawable(context.getResources().getColor(R.color.mode_scroll_bar_color_select));
        this.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void animateWidth(int from, int to, int duration) {
        if(mWidthAnimator!=null&&mWidthAnimator.isRunning()){
            mWidthAnimator.cancel();
            mIndicatorWidth=to;
            invalidate();
        }
        mWidthAnimator = ValueAnimator.ofInt(from, to);
        mWidthAnimator.setDuration(duration);
        mWidthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mIndicatorWidth=(int)valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        mWidthAnimator.start();

    }


    @Override
    public void animateTrans(int fromWidth,int toWidth,int fromTrans , int toTrans ,int duration){
        if(duration<0){
            mTransX=toTrans;
            mIndicatorWidth=toWidth;
            invalidate();
            return;
        }

        if(mAnimatorSet!=null&&mAnimatorSet.isRunning()){
            mAnimatorSet.cancel();
            fromTrans=mTransX;
            fromWidth=mIndicatorWidth;
        }
        mTransAnimator=ValueAnimator.ofInt(fromTrans,toTrans);
        mTransAnimator.setDuration(duration);
        mTransAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mTransX = (int) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });

        mWidthAnimator = ValueAnimator.ofInt(fromWidth, toWidth);
        mWidthAnimator.setDuration(duration);
        mWidthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mIndicatorWidth = (int) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });

        mAnimatorSet=new AnimatorSet();
        mAnimatorSet.playTogether(mWidthAnimator,mTransAnimator);
        mAnimatorSet.start();
    }

    public int getIndicatorTransX(){
        return mTransX;
    }

    @Override
    public void initializeWidth(int width) {
        mIndicatorWidth=width;
        invalidate();
        mAnimatorSet=null;
        mWidthAnimator=null;
        mTransAnimator=null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (CustomUtil.getInstance().getBoolean(CustomFields.DEF_CAMERA_FIX_MODE_SWITCHING, true)){
          return;
        }

//        int canvasCenter=canvas.getWidth()/2;
//        mIndicatorColorDrawable.setBounds(canvasCenter+mTransX-mIndicatorWidth/2,0,canvasCenter+mTransX+mIndicatorWidth/2,canvas.getHeight());
//        mIndicatorColorDrawable.draw(canvas);
        super.onDraw(canvas);
    }
}
