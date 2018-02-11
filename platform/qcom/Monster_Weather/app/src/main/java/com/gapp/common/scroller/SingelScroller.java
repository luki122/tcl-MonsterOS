package com.gapp.common.scroller;

import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-15.
 * $desc
 */
public class SingelScroller {

    private Interpolator mInterpolator = new LinearInterpolator();

    private float mDurationReciprocal;

    private boolean isFinised = true;
    private long mStartTime;
    private float mStart, mCurrent, mDistance;
    private int mDuration;
    private boolean isPaused = false;

    private int mPausePassedTime;

    private boolean isInvalited;

    private int mDelayTime;

    public void setInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }


    public SingelScroller setDelayTime(int timeMills) {
        mDelayTime = timeMills;
        return this;
    }


    public void start(float start, float distance, int duration) {
        isFinised = false;
        mStart = start;
        mDistance = distance;
        mDuration = duration;
        mDurationReciprocal = 1.0f / (float) duration;
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
    }

    public void start(float start, float distance, int duration, boolean isInvalited) {
        this.isInvalited = isInvalited;
        isFinised = false;
        mStart = start;
        mDistance = distance;
        mDuration = duration;
        mDurationReciprocal = 1.0f / (float) duration;
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
    }

    public boolean computeScrollOffset() {
        if (isPaused) {
            return true;
        }
        if (isFinised) {
            if (isInvalited) {
                start(mStart, mDistance, mDuration, isInvalited);
            }
            return false;
        }
        int timePassed = (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime) - mDelayTime;

        if (timePassed < 0)
            timePassed = 0;

        if (timePassed < mDuration) {
            float detal = mInterpolator.getInterpolation(mDurationReciprocal * timePassed) * mDistance;
            mCurrent = mStart + detal;
        } else {
            isFinised = true;
            mCurrent = mStart + mDistance;
        }
        return true;
    }

    public void pause() {
        isPaused = true;
        mPausePassedTime = (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);
    }

    public void resume() {
        mStartTime = AnimationUtils.currentAnimationTimeMillis() - mPausePassedTime;
        isPaused = false;
    }

    public void stop() {
        isFinised = true;
        isInvalited = false;
    }


    public boolean isFinised() {
        return isFinised;
    }

    public float getCurrent() {
        return mCurrent;
    }

}
