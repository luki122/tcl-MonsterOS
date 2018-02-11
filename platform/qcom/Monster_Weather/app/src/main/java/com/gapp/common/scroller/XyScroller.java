package com.gapp.common.scroller;

import android.renderscript.Matrix2f;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.gapp.common.interpolator.InterpolatorXY;
import com.gapp.common.interpolator.LinearInterpolatorXy;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-17.
 * $desc
 */
public class XyScroller {

    private InterpolatorXY mInterpolatorXY = new LinearInterpolatorXy();

    private float mDurationReciprocal;

    private boolean isFinised = true;
    private long mStartTime;
    private float mStartX, mDeltaX, mStartY, mDeltaY, mCurrentX, mCurrentY;
    private int mDuration;
    private boolean isPaused = false;

    private int mPausePassedTime;

    private boolean isInvalited;

    private float mOffsetPercent;

    private Interpolator mInterpolator = new LinearInterpolator();

    private float[] mMatrixData;

    private int mDelayTime;

    public XyScroller setDelayTime(int timeMills) {
        mDelayTime = timeMills;
        return this;
    }

    public void setInterpolatorXy(InterpolatorXY interpolatorXy) {
        setInterpolatorXy(interpolatorXy, new LinearInterpolator());
    }

    public void setInterpolatorXy(InterpolatorXY interpolatorXY, Interpolator interpolator) {
        if (null != interpolatorXY)
            mInterpolatorXY = interpolatorXY;
        if (null != interpolator)
            mInterpolator = interpolator;
    }

    public void start(float startX, float startY, float deltaX, float deltaY, int duration) {
        start(startX, startY, deltaX, deltaY, duration, false);
    }

    public void start(float startX, float startY, float deltaX, float deltaY, int duration, boolean isInvalited) {
        this.isInvalited = isInvalited;
        isFinised = false;
        mStartX = startX;
        mDeltaX = deltaX;
        mStartY = startY;
        mDeltaY = deltaY;
        mDuration = duration;
        mDurationReciprocal = 1.0f / (float) duration;
        mStartTime = android.view.animation.AnimationUtils.currentAnimationTimeMillis();
    }

    public boolean computeScrollOffset() {
        if (isPaused) {
            return true;
        }
        if (isFinised) {
            if (isInvalited) {
                start(mStartX, mStartY, mDeltaX, mDeltaY, mDuration, isInvalited);
            }
            return false;
        }
        int timePassed = (int) (android.view.animation.AnimationUtils.currentAnimationTimeMillis() - mStartTime) - mDelayTime;

        if (timePassed < 0)
            timePassed = 0;

        float input;
        if (timePassed < mDuration) {
            input = mDurationReciprocal * timePassed;
        } else {
            isFinised = true;
            input = 1.0f;
        }

        input += mOffsetPercent;
        if (input > 1.0f) {
            input = 1.0f;
            isFinised = true;
        }

        input = mInterpolator.getInterpolation(input);
        float[] values = mInterpolatorXY.getInterpolation(input);
        float detalX = Math.round(values[0] * mDeltaX);
        float detalY = Math.round(values[1] * mDeltaY);
        if (null != mMatrixData) {
            float x = detalX * mMatrixData[0] + detalY * mMatrixData[2];
            float y = detalX * mMatrixData[1] + detalY * mMatrixData[3];
            detalX = x;
            detalY = y;
        }
        mCurrentX = mStartX + detalX;
        mCurrentY = mStartY + detalY;
        return true;
    }


    public void setOffsetPercent(float percent) {
        mOffsetPercent = percent;
    }


    public void pause() {
        isPaused = true;
        mPausePassedTime = (int) (android.view.animation.AnimationUtils.currentAnimationTimeMillis() - mStartTime);
    }

    public void resume() {
        mStartTime = android.view.animation.AnimationUtils.currentAnimationTimeMillis() - mPausePassedTime;
        isPaused = false;
    }

    public void stop() {
        isFinised = true;
        isInvalited = false;
    }


    public boolean isFinised() {
        return isFinised;
    }

    public float getCurrentX() {
        return mCurrentX;
    }

    public float getCurrentY() {
        return mCurrentY;
    }


    public void setMatrix2f(Matrix2f matrix2f) {
        mMatrixData = matrix2f.getArray();
    }

    public void setMatrixData(float[] values) {
        if (values.length < 4)
            return;
        mMatrixData = values;
    }
}
