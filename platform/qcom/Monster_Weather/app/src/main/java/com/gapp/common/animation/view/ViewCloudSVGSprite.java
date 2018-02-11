package com.gapp.common.animation.view;

import android.content.Context;
import android.graphics.Path;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.gapp.common.interpolator.InterpolatorXY;
import com.gapp.common.interpolator.PathXyInterpolator;
import com.gapp.common.scroller.XyScroller;
import com.leon.tools.view.AndroidUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-13.
 * $desc
 */
public class ViewCloudSVGSprite extends ViewSprite {

    private final static int ANIMATION_TIMEMILLS = 12 * 1000;

    private float mWidth, mHeight;

    private XyScroller mXyScroller = new XyScroller();

    private float mSx, mSy, mScrolldx, mScrolldy;

    private float mOffsetSetTimes;

    private boolean isBack = false;

    private InterpolatorXY [] mXyInterpolator = new InterpolatorXY[2];
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    public ViewCloudSVGSprite(Context context, int svgResId) {
        super(context);
        setBackgroundResource(svgResId);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) AndroidUtils.dip2px(getContext(), mWidth), (int) AndroidUtils.dip2px(getContext(), mHeight));
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            Path path = new Path();
            path.reset();
            path.quadTo(0.25f, -1.0f, 0.5f, 0f);
            path.quadTo(0.75f, 1.0f, 1.0f, 0f);
            PathXyInterpolator interpolator = new PathXyInterpolator();
            interpolator.setPath(path);
            mXyInterpolator[0] = interpolator;


            path = new Path();
            path.moveTo(1.0f, 0f);
            path.quadTo(0.75f, 1.0f, 0.5f, 0f);
            path.quadTo(0.25f, -1.0f, 0f, 0f);
            interpolator = new PathXyInterpolator();
            interpolator.setPath(path);
            mXyInterpolator[1] = interpolator;

//            mXyScroller.setInterpolatorXy(interpolator);
        }
    }

    public void setSize(float wdp, float hdp) {
        mWidth = wdp;
        mHeight = hdp;
    }

    public void setMovePoints(float sx, float sy, float ex, float ey, float height) {
        float dx = ex - sx;
        float dy = ey - sy;
        float r = (float) Math.sqrt(dx * dx + dy * dy);
        float[] matrix = new float[4];
        matrix[3] = matrix[0] = dx / r;
        matrix[1] = dy / r;
        matrix[2] = -matrix[1];
        mSx = sx + getTranslationX();
        mSy = sy + getTranslationY();
        mScrolldx = r;
        mScrolldy = height;
        mXyScroller.setMatrixData(matrix);
    }

    public void setOffsetTime(int timeMills) {
        mOffsetSetTimes = ((float) timeMills) / (float) ANIMATION_TIMEMILLS;
    }

    private void resetViews() {
        mXyScroller.setOffsetPercent(mOffsetSetTimes);
        if(isBack){
            mXyScroller.setInterpolatorXy(mXyInterpolator[1], mInterpolator);
        }else {
            mXyScroller.setInterpolatorXy(mXyInterpolator[0], mInterpolator);
        }
        mXyScroller.start(mSx, mSy, mScrolldx, mScrolldy, ANIMATION_TIMEMILLS);
        isBack = !isBack;
        mOffsetSetTimes = 0;
    }

    @Override
    public void running() {
        if (null != mXyScroller) {
            if (mXyScroller.computeScrollOffset()) {
                setTranslationX(mXyScroller.getCurrentX());
                setTranslationY(mXyScroller.getCurrentY());
            } else {
                resetViews();
            }
        }
    }

    @Override
    public void pause() {
        if (null != mXyScroller)
            mXyScroller.pause();
    }

    @Override
    public void resume() {
        if (null != mXyScroller)
            mXyScroller.resume();
    }

    @Override
    public int getZOrder() {
        return -2;
    }
}
