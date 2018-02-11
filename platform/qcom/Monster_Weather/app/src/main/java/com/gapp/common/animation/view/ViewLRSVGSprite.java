package com.gapp.common.animation.view;

import android.content.Context;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.gapp.common.scroller.SingelScroller;
import com.gapp.common.utils.RandomUtils;
import com.leon.tools.view.AndroidUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-13.
 * $desc
 */
public class ViewLRSVGSprite extends ViewSprite {

    private final static int SCROLLING_TIME = 6000;

    private final static int MIN_SCROLL_PX = 150;


    private SingelScroller mSingelScroller = new SingelScroller();

    private boolean isReset;
    private float mLastScrollDx;
    private float mWidth, mHeight;
    private boolean isPositive = true;


    public ViewLRSVGSprite(Context context, int svgResId) {
        super(context);
        setBackgroundResource(svgResId);
        mSingelScroller.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    public ViewLRSVGSprite(Context context, int svgResId, boolean isPositive) {
        super(context);
        setBackgroundResource(svgResId);
        mSingelScroller.setInterpolator(new LinearInterpolator());
        this.isPositive = isPositive;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) AndroidUtils.dip2px(getContext(), mWidth),(int) AndroidUtils.dip2px(getContext(), mHeight));
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            mLastScrollDx = MIN_SCROLL_PX + RandomUtils.getRandomInt(MIN_SCROLL_PX);
            if (!isPositive)
                mLastScrollDx = -mLastScrollDx;
        }
    }

    public void setSize(float wdp, float hdp) {
        mWidth = wdp;
        mHeight = hdp;
    }


    private void resetViews() {
        if (!isReset) {
            isReset = true;
            mSingelScroller.start(getTranslationX(), mLastScrollDx, SCROLLING_TIME + RandomUtils.getRandomInt(SCROLLING_TIME));
        } else {
            mSingelScroller.start(getTranslationX(), -mLastScrollDx, SCROLLING_TIME + RandomUtils.getRandomInt(SCROLLING_TIME));
            isReset = false;
        }
    }

    @Override
    public void running() {
        if (mSingelScroller.computeScrollOffset()) {
            setTranslationX(mSingelScroller.getCurrent());
        } else {
            resetViews();
        }
    }

    @Override
    public void pause() {
        mSingelScroller.pause();
    }

    @Override
    public void resume() {
        mSingelScroller.resume();
    }

    @Override
    public int getZOrder() {
        return -2;
    }
}
