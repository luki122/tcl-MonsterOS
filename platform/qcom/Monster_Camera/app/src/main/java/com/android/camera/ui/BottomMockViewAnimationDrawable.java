package com.android.camera.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Point;

import com.android.camera.debug.Log;

/**
 * Created by sichao.hu on 12/15/15.
 */
public class BottomMockViewAnimationDrawable extends CustomizeDrawable{

    private static int DRAWABLE_MAX_LEVEL = 10000;
    private static final int CIRCLE_ANIM_DURATION_MS = 300;
    private Point mCenterPoint;
    private float mRadius;
    private int mCanvasWidth;
    private int mCanvasHeight;
    private int mSmallRadius;
    private int mBackgroundColor;
    private ValueAnimator mScaleValueAnimator=new ValueAnimator();
    public BottomMockViewAnimationDrawable(Point center,int color,int smallRadius){
        super();
        mCenterPoint =center;
        setARGB(color);
        mBackgroundColor =color;
        initAinimator();
        mSmallRadius=smallRadius/2;
    }

    private void initAinimator(){
        mScaleValueAnimator.setDuration(CIRCLE_ANIM_DURATION_MS);
        mScaleValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int level=(int)valueAnimator.getAnimatedValue();
                BottomMockViewAnimationDrawable.this.setLevel(level);
            }
        });

    }

    @Override
    protected boolean onLevelChange(int level) {
        invalidateSelf();
        return true;
    }

    public void animateToCircle(final int targetColor,int width,int height){
        if(mScaleValueAnimator.isRunning()){
            mScaleValueAnimator.cancel();
        }
        BottomMockViewAnimationDrawable.this.setARGB(mBackgroundColor);
        int smallLevel=map(mSmallRadius,0,diagonalLength(width,height),0,DRAWABLE_MAX_LEVEL);
        mScaleValueAnimator.setIntValues(getLevel(),smallLevel);
        mScaleValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                BottomMockViewAnimationDrawable.this.setARGB(targetColor);
                Log.v(TAG, "level is " + getLevel());
                animator.removeListener(this);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                BottomMockViewAnimationDrawable.this.setARGB(targetColor);
                animator.removeListener(this);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        mScaleValueAnimator.start();
    }

    public static interface OnAnimationAsyncListener{
        public void onAnimationFinish();
    }
    public void animateToFullSize(final OnAnimationAsyncListener listener){
        if(mScaleValueAnimator.isRunning()){
            mScaleValueAnimator.cancel();
        }
        BottomMockViewAnimationDrawable.this.setARGB(mBackgroundColor);
        Log.w(TAG,"level is "+getLevel());
        mScaleValueAnimator.setIntValues(getLevel(),DRAWABLE_MAX_LEVEL);
        mScaleValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (listener != null)
                    listener.onAnimationFinish();
                animator.removeListener(this);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                if (listener != null)
                    listener.onAnimationFinish();
                animator.removeListener(this);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        mScaleValueAnimator.start();
    }



    private Log.Tag TAG=new Log.Tag("BottomMockAnimation");
    @Override
    public void draw(Canvas canvas) {
        mCanvasHeight=canvas.getHeight();
        mCanvasWidth=canvas.getWidth();
        mRadius=map(getLevel(),0,DRAWABLE_MAX_LEVEL,0,diagonalLength(mCanvasWidth,mCanvasHeight));
        Log.w(TAG,"canvas width is "+mCanvasWidth+" canvas height is "+mCanvasHeight+" radius is "+mRadius);
        canvas.drawCircle(mCenterPoint.x,mCenterPoint.y,mRadius,getPaint());
    }

    /**
     * Maps a given value x from one input range [in_min, in_max] to
     * another output range [out_min, out-max].
     * @param x Value to be mapped.
     * @param in_min Input range minimum.
     * @param in_max Input range maximum.
     * @param out_min Output range minimum.
     * @param out_max Output range maximum.
     * @return The mapped value.
     */
    private static int map(int x, int in_min, int in_max, int out_min, int out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    private static int diagonalLength(int w, int h) {
        return (int) Math.sqrt((w*w) + (h*h));
    }
}
