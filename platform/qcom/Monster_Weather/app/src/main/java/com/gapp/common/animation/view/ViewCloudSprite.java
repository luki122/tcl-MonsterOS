package com.gapp.common.animation.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.gapp.common.interpolator.LinearInterpolatorXy;
import com.gapp.common.scroller.XyScroller;
import com.gapp.common.utils.RandomUtils;
import com.leon.tools.view.AndroidUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-12.
 * $desc
 */
public class ViewCloudSprite extends ViewSprite {

    private float RADIUS_SIZE = 300f;
    private final static int TIME_MILLS = 8000;

    private int mRadius;
    private int mScrollingSkip;

    private int mStartTx;
    private int mStartTy;

    private int mColor[];

    private XyScroller mXyScroller = new XyScroller();

    private boolean isBack;

    public ViewCloudSprite(Context context) {
        super(context);
    }

    public ViewCloudSprite(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewCloudSprite(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ViewCloudSprite(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (0 == mRadius)
            mRadius = (int) AndroidUtils.dip2px(getContext(), RADIUS_SIZE);
        setMeasuredDimension(mRadius * 2, mRadius * 2);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            mScrollingSkip = ((int) AndroidUtils.dip2px(getContext(), RADIUS_SIZE) * 10) / 50;

            setShader();

            LinearInterpolatorXy interpolatorXy = new LinearInterpolatorXy();
            mXyScroller.setInterpolatorXy(interpolatorXy, new AccelerateDecelerateInterpolator());
            mXyScroller.start(mStartTx, 0, mScrollingSkip, 0, TIME_MILLS + RandomUtils.getRandomInt(TIME_MILLS));
            if (getTranslationY() == 0) {
                setTranslationX(mStartTx);
                setTranslationY(-getHeight());
            }
        }
    }

    public ViewCloudSprite setStartOffset(float progress) {
        mXyScroller.setOffsetPercent(progress);
        return this;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.drawCircle(mRadius, mRadius, mRadius, mPaint);
        canvas.restore();
    }


    public ViewCloudSprite setStartXY(int startX, int startY) {
        mStartTx = startX;
        mStartTy = startY;
        return this;
    }

    @Override
    public void running() {
        if (mXyScroller.computeScrollOffset()) {
            setTranslationX(mXyScroller.getCurrentX());
            setTranslationY(mStartTy);
        } else {
            mXyScroller.setOffsetPercent(0);
            isBack = !isBack;
            if (isBack) {
                float startX = getTranslationX();
                float scrollSkip = mStartTx - startX;
                mXyScroller.start(startX, 0, scrollSkip, 0, TIME_MILLS + RandomUtils.getRandomInt(TIME_MILLS));
            } else {
                mXyScroller.start(mStartTx, 0, mScrollingSkip, 0, TIME_MILLS + RandomUtils.getRandomInt(TIME_MILLS));
            }
        }
    }

    @Override
    public void pause() {
        mXyScroller.pause();
    }

    @Override
    public void resume() {
        mXyScroller.resume();
    }

    public ViewCloudSprite setRadius(float radius) {
        mRadius = (int) radius;
        return this;
    }

    @Override
    public int getZOrder() {
        return -2;
    }

    public ViewCloudSprite setColor(int[] color) {
        mColor = color;
        return this;
    }

    public ViewCloudSprite setShader() {
        mPaint.setShader(new LinearGradient(0, 0, 0, getHeight(), mColor[0], mColor[1], Shader.TileMode.REPEAT));
        setRotation(-30);
        postInvalidate();
        return this;
    }
}
