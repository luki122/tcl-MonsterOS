package com.gapp.common.animation.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import com.gapp.common.scroller.SingelScroller;
import com.leon.tools.view.AndroidUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-12.
 * $desc
 */
public class ViewHaloSprite extends ViewSprite {

    private final static float RADIUS_SIZE = 330f;
    private final static int TIME_MILLS = 3000;
    private final static int HOLO_SIZE = 7;
    private final static float MIN_SIZE = 0f;

    private int mMaxRadius;
    private int mMinRadius;

    private float[] mRadius = new float[HOLO_SIZE];

    private Paint mPaint = new Paint();

    private SingelScroller mScroller = new SingelScroller();

    private float mAnimateSkip;

    public ViewHaloSprite(Context context) {
        super(context);
    }

    public ViewHaloSprite(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewHaloSprite(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ViewHaloSprite(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMaxRadius = (int) AndroidUtils.dip2px(getContext(), RADIUS_SIZE);
        setMeasuredDimension(mMaxRadius * 2, mMaxRadius * 2);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            mMinRadius = (int) AndroidUtils.dip2px(getContext(), MIN_SIZE);
            final int minRadius = mMinRadius;
            float step = (mMaxRadius - minRadius) / HOLO_SIZE;
            mAnimateSkip = step;
            for (int i = 0; i < HOLO_SIZE; i++) {
                mRadius[i] = minRadius + step * i;
            }

            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(0xffffffff);

            mScroller.setInterpolator(new LinearInterpolator());

            setRotation(-60);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (float r : mRadius) {
            drawCicle(canvas, r);
        }
    }

    private void drawCicle(Canvas canvas, float radius) {
        float progress = (radius + mScroller.getCurrent()) / mMaxRadius;
        if (progress > 1.0f)
            progress = 1.0f;
        float skip = mMaxRadius - radius;
        mPaint.setShader(new LinearGradient(skip, skip, skip, getHeight() - skip, new int[]{0x0fffffff, 0x5fb3dbfd, 0x66b6daf8}, null, Shader.TileMode.MIRROR));
        mPaint.setAlpha((int) (255 * (1.0f - progress)));
        canvas.drawCircle(mMaxRadius, mMaxRadius, radius + mScroller.getCurrent(), mPaint);
    }

    @Override
    public void running() {
        if (mScroller.computeScrollOffset()) {
            invalidate();
        } else {
            mScroller.start(0, mAnimateSkip, TIME_MILLS);
        }
    }

    @Override
    public void pause() {
        mScroller.pause();
    }

    @Override
    public void resume() {
        mScroller.resume();
    }

    @Override
    public int getZOrder() {
        return -1;
    }
}
