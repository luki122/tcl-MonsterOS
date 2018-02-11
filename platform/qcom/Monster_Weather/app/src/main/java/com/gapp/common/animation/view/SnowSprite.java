package com.gapp.common.animation.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import com.gapp.common.animation.IServantConnecter;
import com.gapp.common.animation.connector.SnowConnector;
import com.gapp.common.scroller.SingelScroller;
import com.gapp.common.utils.RandomUtils;
import com.leon.tools.view.AndroidUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-9-1.
 * snow
 */
public class SnowSprite extends ViewSprite implements IServantConnecter.IServant {

    private final static float RADIUS_SIZE = 6.7f;

    public final static int STATE_NORMAL = 0;
    public final static int STATE_CONTROLLING = 1;
    public final static int STATE_CONTROLLED = 2;

    private RunningStatus[] mRunningStatus = new RunningStatus[]{new NormalRunningStatus(), new ControllingStatus(), new ControlledStatus()};

    private RunningStatus mCurrentStatus = mRunningStatus[0];
    private int mCurrentState = STATE_NORMAL;

    private float[] mScaleSpace = new float[]{0.4f, 1.0f};
    private RandomUtils mRandom;
    private float mRadus;

    private int mZorder = -1;

    private final static int TIME_MILLS = 10 * 1000;// default fall time is 10 seconds
    private int mFallTime = TIME_MILLS;

//    private SingelScroller mAlphaScroller = new SingelScroller();

    public SnowSprite(Context context) {
        super(context);
    }

    public SnowSprite(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SnowSprite(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public SnowSprite(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void setRadius(int radius) {
        mRadus = radius;
    }

    /**
     * Set the time of snow falling
     *
     * @param second The snow fall time
     */
    public void setFallTime(int second) {
        mFallTime = second * 1000;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mRadus == 0)
            mRadus = (int) AndroidUtils.dip2px(getContext(), RADIUS_SIZE);
        setMeasuredDimension((int) mRadus * 2, (int) mRadus * 2);
    }

    public void setRandomUtils(RandomUtils utils) {
        mRandom = utils;
    }

    public void setScaleRange(float min, float max) {
        mScaleSpace[0] = min;
        mScaleSpace[1] = max;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed)
            mCurrentStatus.layout(mParent.getWidth(), mParent.getHeight());
    }


    @Override
    public void running() {
        mCurrentStatus.running();
    }

    @Override
    public void pause() {
        mCurrentStatus.pause();
    }

    @Override
    public void resume() {
        mCurrentStatus.resume();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        mCurrentStatus.onDraw(canvas);
    }


    private void setState(int state) {
        RunningStatus status = mRunningStatus[state];
        if (mCurrentStatus != status) {
            mCurrentState = state;

            mCurrentStatus.pause();
            mCurrentStatus.stop();
            mCurrentStatus = status;
            mCurrentStatus.start();
            mCurrentStatus.resume();
        }
    }

    private void reset() {
        mCurrentStatus.reset();
    }

    public int getCurrentState() {
        return mCurrentState;
    }

    @Override
    public void onControlByConnecter(IServantConnecter connecter, int state) {
        switch (state) {
            case SnowConnector.CONTROLL_STATE_CONNECTED:
                mCurrentStatus.setConnector(connecter);
                break;
            case SnowConnector.CONTROLL_STATE_ATTACHED:
                reset();
                break;
        }
    }


    private interface RunningStatus {

        void setConnector(IServantConnecter connector);

        void layout(int pW, int pH);

        void reset();

        void start();

        void stop();

        void running();

        void pause();

        void resume();

        void onDraw(Canvas canvas);
    }


    private class ControlledStatus implements RunningStatus {

        @Override
        public void setConnector(IServantConnecter connector) {

        }

        @Override
        public void layout(int pW, int pH) {

        }

        @Override
        public void reset() {

        }

        @Override
        public void start() {

        }

        @Override
        public void stop() {

        }

        @Override
        public void running() {

        }

        @Override
        public void pause() {

        }

        @Override
        public void resume() {

        }

        @Override
        public void onDraw(Canvas canvas) {

        }
    }


    private class ControllingStatus implements RunningStatus {
        private final static int MAX_STEP = 100;
        int mSteps;
        private float mStartTy;

        @Override
        public void setConnector(IServantConnecter connector) {

        }

        @Override
        public void layout(int pW, int pH) {

        }

        @Override
        public void reset() {

        }

        @Override
        public void start() {
            mSteps = MAX_STEP;
            mStartTy = getTranslationY();
        }

        @Override
        public void stop() {

        }

        @Override
        public void running() {
            mSteps--;
            float progress = ((float) mSteps) / ((float) MAX_STEP);
            setAlpha(progress);
            setTranslationY(mStartTy + getHeight() * progress / 4);
            if (mSteps == 0) {
                setState(STATE_CONTROLLED);
            }
        }

        @Override
        public void pause() {
        }

        @Override
        public void resume() {

        }

        @Override
        public void onDraw(Canvas canvas) {
            canvas.drawCircle(mRadus, mRadus, mRadus, mPaint);
        }
    }


    private class NormalRunningStatus implements RunningStatus {

        private SingelScroller mScrollerY = new SingelScroller();

        private IServantConnecter mConnecter;

        @Override
        public void setConnector(IServantConnecter connector) {
            mConnecter = connector;
            if (!mScrollerY.isFinised()) {
                mConnecter.onServantStateChanged(SnowSprite.this, SnowConnector.SERVANT_STATE_RESET);
            }
        }

        @Override
        public void layout(int pW, int pH) {
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Paint.Style.FILL);
            reset();
        }

        @Override
        public void reset() {
            final float pWidth = mParent.getWidth();
            final float scale = RandomUtils.getRandomFloat(mScaleSpace[0], mScaleSpace[1]);
            setTranslationX(mRandom.getRandomIntOffset(0, (int) pWidth));
            setTranslationY(RandomUtils.getRandomFloat(-getHeight()) - getHeight());
            setScaleX(scale);
            setScaleY(scale);
            setAlpha(scale);

            final int timeMills;
            timeMills = (int) (mFallTime / scale);

            mScrollerY.stop();
            mScrollerY.setInterpolator(new LinearInterpolator());
            mScrollerY.start(getTranslationY(), mParent.getHeight() + getHeight() - getTranslationY(), timeMills);

            if (null != mConnecter)
                mConnecter.onServantStateChanged(SnowSprite.this, SnowConnector.SERVANT_STATE_RESET);
        }

        @Override
        public void start() {
            reset();
        }

        @Override
        public void stop() {

        }

        @Override
        public void running() {
            if (mScrollerY.computeScrollOffset()) {
                setTranslationY(mScrollerY.getCurrent());
            } else {
                reset();
            }
        }

        @Override
        public void pause() {
            mScrollerY.pause();
        }

        @Override
        public void resume() {
            mScrollerY.resume();
        }

        @Override
        public void onDraw(Canvas canvas) {
            canvas.drawCircle(mRadus, mRadus, mRadus, mPaint);
        }
    }
}
