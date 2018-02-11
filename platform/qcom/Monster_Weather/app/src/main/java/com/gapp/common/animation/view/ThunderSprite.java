package com.gapp.common.animation.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.animation.Interpolator;

import com.gapp.common.scroller.SingelScroller;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-9-12.
 * $desc
 */
public class ThunderSprite extends ViewSprite {


    private SingelScroller mAlphaScroller = new SingelScroller();

    public ThunderSprite(Context context) {
        super(context);
    }

    public ThunderSprite(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThunderSprite(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ThunderSprite(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mParent.getWidth(), mParent.getHeight() - 100);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            setAlpha(0);
            mAlphaScroller.setInterpolator(mInterpolator);
            mAlphaScroller.start(0, 1.0f, 3897, true);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
    }


    @Override
    public void running() {
        if (mAlphaScroller.computeScrollOffset()) {
            setAlpha(mAlphaScroller.getCurrent());
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    /**
     * the alpha of the view will change with the time pass
     */
    private final static float MAX_INPUT = 3897f;
    private Interpolator mInterpolator = new Interpolator() {

        private PieceInterpolator[] mPieceInterpolators = new PieceInterpolator[]{
                new PieceInterpolator(0, 0, 3500f / MAX_INPUT, 0),
                new PieceInterpolator(3500f / MAX_INPUT, 0, 3665f / MAX_INPUT, 0.75f),
                new PieceInterpolator(3665f / MAX_INPUT, 0.75f, 3731f / MAX_INPUT, 0.2f),
                new PieceInterpolator(3731f / MAX_INPUT, 0.2f, 3831f / MAX_INPUT, 0.65f),
                new PieceInterpolator(3831f / MAX_INPUT, 0.65f, 3897f / MAX_INPUT, 0)
        };

        @Override
        public float getInterpolation(float input) {
            for (PieceInterpolator interpolator : mPieceInterpolators) {
                if (interpolator.isInPiece(input)) {
                    return interpolator.getInterpolation(input);
                }
            }
            return 0;
        }
    };

    private class PieceInterpolator implements Interpolator {
        private float mSTime, mETime;
        private float mK, mB;

        private PieceInterpolator(float st, float sValue, float et, float eValue) {
            mSTime = st;
            mETime = et;
            mK = (sValue - eValue) / (st - et);
            mB = eValue - mK * et;
        }

        /**
         * Whether it belongs to this subsection function
         * @param input
         * @return
         */
        private boolean isInPiece(float input) {
            return mSTime <= input && mETime > input;
        }

        /**
         * y = kx + b
         */
        @Override
        public float getInterpolation(float input) {
            return input * mK + mB;
        }
    }
}
