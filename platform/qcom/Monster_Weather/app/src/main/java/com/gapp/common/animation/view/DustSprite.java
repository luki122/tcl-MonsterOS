package com.gapp.common.animation.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.gapp.common.scroller.SingelScroller;
import com.gapp.common.scroller.XyScroller;
import com.gapp.common.utils.RandomUtils;
import com.leon.tools.view.AndroidUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-10-25.
 * $desc
 */
public class DustSprite extends ViewSprite {
    private final static int TIME_MILLS = 3 * 1000;//15 seconds
    private final static float RADIUS = 10f;

    private int mRadius;
    private float[] mScaleSpace = new float[]{0.4f, 1.0f};
    private RandomUtils mRandom;
    private XyScroller mXyScroller = new XyScroller();

    private SingelScroller mAlphaScroller = new SingelScroller();

    private boolean isStorm;

    private int mTimeMills = TIME_MILLS;

    public DustSprite(Context context) {
        super(context);
    }


    public DustSprite(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DustSprite(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DustSprite(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mRadius = (int) AndroidUtils.dip2px(getContext(), RADIUS);
        setMeasuredDimension(mRadius * 2, mRadius * 2);
    }


    public void setRandomUtils(RandomUtils utils) {
        mRandom = utils;
    }

    public void setTimeMills(int timeMills){
        mTimeMills = timeMills;
    }

    public void setScaleRange(float min, float max) {
        mScaleSpace[0] = min;
        mScaleSpace[1] = max;
    }

    public void setIsStorm(boolean isStorm) {
        this.isStorm = isStorm;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            mPaint.setAntiAlias(true);
            mPaint.setColor(0xff000000);
            mPaint.setAlpha(0x0d);
            reset();
        }
    }


    private void reset() {
        final float pWidth = mParent.getWidth();
        if (pWidth > 0f) {
            final float scale = RandomUtils.getRandomFloat(mScaleSpace[0], mScaleSpace[1]);
            setScaleX(scale);
            setScaleY(scale);

            setTranslationY(RandomUtils.getRandomFloat(-getHeight()) - getHeight());
            //The rain in the distance fell more slowly.
            int timeMills = (int) (mTimeMills / scale);
            float deltaY = mParent.getHeight() + getHeight() - getTranslationY();
            float deltaX = 0;
            if (isStorm) {
                float k = RandomUtils.getRandomFloat(1.7f, 1.8f);
                setTranslationX(mRandom.getRandomIntOffset(0, (int) (pWidth * k)));
                deltaX = -deltaY / k;
            } else {
                setTranslationX(mRandom.getRandomIntOffset(0, (int) pWidth));
                timeMills = mTimeMills * 4;
            }
            mXyScroller.start(getTranslationX(), getTranslationY(), deltaX, deltaY, timeMills);
            mAlphaScroller.setDelayTime(timeMills / 2).start(1.0f, -1.0f, timeMills * 3 / 20);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(mRadius, mRadius, mRadius, mPaint);
    }


    @Override
    public void running() {
        if (mXyScroller.computeScrollOffset()) {
            setTranslationX(mXyScroller.getCurrentX());
            setTranslationY(mXyScroller.getCurrentY());
        }

        if (mAlphaScroller.computeScrollOffset())
            setAlpha(mAlphaScroller.getCurrent());
        else
            reset();
    }

    @Override
    public void pause() {
        mXyScroller.pause();
        mAlphaScroller.pause();
    }

    @Override
    public void resume() {
        mXyScroller.resume();
        mAlphaScroller.resume();
    }
}
