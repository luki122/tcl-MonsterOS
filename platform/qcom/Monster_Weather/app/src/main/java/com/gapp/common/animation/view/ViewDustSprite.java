package com.gapp.common.animation.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.gapp.common.interpolator.RandomBezillXyInterpolator;
import com.gapp.common.scroller.XyScroller;
import com.gapp.common.utils.RandomUtils;
import com.leon.tools.view.AndroidUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-29.
 * $desc
 */
public class ViewDustSprite extends ViewSprite {

    private final static float SIZE = 1.0f;
    private final static int TIME_MILLS = 8 * 1000;

    private float mRadius;
    private Paint mPaint = new Paint();

    private int mSize;

    private XyScroller mXyScroller = new XyScroller();

    public ViewDustSprite(Context context) {
        super(context);
    }

    public ViewDustSprite(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewDustSprite(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ViewDustSprite(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            mRadius = AndroidUtils.dip2px(getContext(), SIZE);
            mSize = (int) (mRadius * 2 + 0.5f);
            layout(0, 0, mSize, mSize);
        }
        reset();
    }


    private void reset() {
        setTranslationX(RandomUtils.getRandomInt(mParent.getWidth()));
        setTranslationY(-RandomUtils.getRandomInt(mParent.getHeight()));
        int timeMills = RandomUtils.getRandomInt(TIME_MILLS) + TIME_MILLS;
        mXyScroller.start(getTranslationY(), getTranslationX(), mParent.getHeight() - getTranslationY(), mParent.getWidth() >> 2, timeMills);
    }

    @Override
    protected void onSetUp() {
        mPaint.setColor(Color.LTGRAY);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        RandomBezillXyInterpolator interpolator = new RandomBezillXyInterpolator();
        interpolator.setBezillCurveRandom();
        mXyScroller.setInterpolatorXy(interpolator);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float center = getWidth() / 2.0f;
        canvas.drawCircle(center, center, mRadius, mPaint);
    }

    @Override
    public void running() {
        if (mXyScroller.computeScrollOffset()) {
            setTranslationX(mXyScroller.getCurrentY());
            setTranslationY(mXyScroller.getCurrentX());
        } else {
            reset();
        }
    }

    @Override
    public int getZOrder() {
        return 1;
    }

    @Override
    public void pause() {
        mXyScroller.pause();
    }

    @Override
    public void resume() {
        mXyScroller.resume();
    }
}
