package com.gapp.common.animation.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;

import com.gapp.common.scroller.SingelScroller;
import com.leon.tools.view.AndroidUtils;

import cn.tcl.weather.R;
import cn.tcl.weather.utils.LogUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-10-17.
 * $desc
 */
public class ViewSunLightSprite extends ViewSprite {
    private final static float SIZE = 17f / 3.0f;
    private final static float[] SIZES = new float[]{SIZE, SIZE * 1.8f, SIZE * 2.5f, SIZE * 2.8f, SIZE * 5.4f};
    private final static float[] POISITIONS = new float[]{67f / 3.0f, 135f / 3.0f, 289f / 3.0f, 350f / 3.0f, 603f / 3.0f};

    private final static float[][] ALPHA = new float[][]{{0f, 0f}, {2.3f, 0.5f}, {3.0f, 1.0f}, {3.7f, 0.5f}, {4.7f, 0.8f}, {6.0f, 0f}};

    private final static int[] COLORS = new int[]{0x7Fffffff, 0x7Fffffff, 0x66ffffff, 0x4cffffff, 0x66ffffff};

    private final static int ANIMATE_TIMEMILLS = 6 * 1000;

    private final static int STATE_NULL = 0;
    private final static int STATE_RUNNING = 1;

    private Hexagon[] mHexagons;

    private IState[] mStates = new IState[]{new NullState(), new RunningState()};
    private IState mCurrentState;

    public ViewSunLightSprite(Context context) {
        super(context);
    }

    public ViewSunLightSprite(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewSunLightSprite(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ViewSunLightSprite(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        final int width = (int) (AndroidUtils.dip2px(getContext(), SIZES[SIZES.length - 1]) * 2);
        setMeasuredDimension(width, height);
    }


    private static float getAlpha(float offset) {
        int index = ALPHA.length - 1;
        while (ALPHA[index][0] > offset) {
            index--;
            if (index < 0)
                break;
        }
        if (index < ALPHA.length - 1 && index > -1) {
            float bottom = ALPHA[index][0];
            float top = ALPHA[index + 1][0];

            float progress = (offset - bottom) / (top - bottom);

            float bAlpha = ALPHA[index][1];
            float lAlpha = ALPHA[index + 1][1];

            float alpha = (lAlpha - bAlpha) * progress + bAlpha;
            return alpha;

        }
        return 0f;
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            if (null == mHexagons) {
                mHexagons = new Hexagon[SIZES.length];
                for (int i = SIZES.length - 1; i >= 0; i--)
                    mHexagons[i] = new Hexagon();
            }

            mPaint.setAntiAlias(true);
            mPaint.setColor(0xffffffff);
            mPaint.setStyle(Paint.Style.FILL);

            for (int i = SIZES.length - 1; i >= 0; i--) {
                mHexagons[i].setLength(AndroidUtils.dip2px(getContext(), SIZES[i]));
                mHexagons[i].moveTo(0, AndroidUtils.dip2px(getContext(), POISITIONS[i]));
            }
            setPivotX(getWidth() / 2.0f);
            setPivotY(AndroidUtils.dip2px(getContext(), POISITIONS[0]));
            setCurrentState(STATE_NULL);
            setAlpha(0);
        }
    }


    private void setCurrentState(int state) {
        if (mCurrentState != mStates[state]) {
            if (null != mCurrentState)
                mCurrentState.stop();
            mCurrentState = mStates[state];
            mCurrentState.reset();
            postInvalidate();
        }
    }

    @Override
    public void running() {
        if (null != mCurrentState)
            mCurrentState.running();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (null != mCurrentState)
            mCurrentState.onDraw(canvas);
    }

    @Override
    public void pause() {
        if (null != mCurrentState)
            mCurrentState.pause();
    }

    @Override
    public void resume() {
        if (null != mCurrentState)
            mCurrentState.resume();
    }

    final static float W = (float) Math.sqrt(3f) / 2.0f;
    final static float[][] HexagonPoints = new float[][]{{0f, 1f}, {W, 0.5f}, {W, -0.5f},
            {0f, -1f}, {-W, -0.5f}, {-W, 0.5f}};

    class Hexagon {

        private Path path = new Path();

        void moveTo(float x, float y) {
            path.offset(x, y);
        }

        void setLength(float length) {
            path.reset();
            path.moveTo(HexagonPoints[0][0] * length, HexagonPoints[0][1] * length);
            for (int i = 1; i < 6; i++) {
                path.lineTo(HexagonPoints[i][0] * length, HexagonPoints[i][1] * length);
            }
            path.close();
        }
    }

    @Override
    public int getZOrder() {
        return 1;
    }


    private interface IState {
        void running();

        void onDraw(Canvas canvas);

        void reset();

        void pause();

        void resume();

        void stop();
    }

    private class RunningState implements IState {
        private SingelScroller mRotateScroller = new SingelScroller();
        private SingelScroller mAlphaScroller = new SingelScroller();

        private Paint mLastPaint = new Paint();
        private Paint mForthPaint = new Paint();

        @Override
        public void running() {
            if (mAlphaScroller.computeScrollOffset()) {
                float alpha = getAlpha(mAlphaScroller.getCurrent());
                setAlpha(alpha);
            }

            if (mRotateScroller.computeScrollOffset()) {
                setRotation(mRotateScroller.getCurrent());
            } else {
                setRotation(10 - 90);
                setCurrentState(STATE_NULL);
            }
        }

        public void reset() {
            mRotateScroller.start(10 - 90, 75, ANIMATE_TIMEMILLS);
            mAlphaScroller.start(0, 6, ANIMATE_TIMEMILLS);
            mLastPaint.setAntiAlias(true);
            mLastPaint.setStyle(Paint.Style.FILL);
            mForthPaint.setAntiAlias(true);
            mForthPaint.setStyle(Paint.Style.FILL);

            // The last hexagon
            final float fifthHexagonY =  AndroidUtils.dip2px(getContext(), POISITIONS[POISITIONS.length - 1]);
            final float fifthHexagonR =  AndroidUtils.dip2px(getContext(), 38.5f);

            // The forth hexagon
            final float forthHexagonY = AndroidUtils.dip2px(getContext(), POISITIONS[POISITIONS.length - 2]);
            final float forthHexagonR = AndroidUtils.dip2px(getContext(), 21.5f);

            mLastPaint.setShader(new RadialGradient(0, fifthHexagonY, fifthHexagonR, 0x33FFFFFF, 0x33FFF9B8, Shader.TileMode.CLAMP));
            mForthPaint.setShader(new RadialGradient(0, forthHexagonY, forthHexagonR, 0x4CFFFFFF, 0x4CFFF9B8, Shader.TileMode.CLAMP));
        }

        @Override
        public void pause() {
            mRotateScroller.pause();
            mAlphaScroller.pause();
        }

        @Override
        public void resume() {
            mAlphaScroller.resume();
            mRotateScroller.resume();
        }

        @Override
        public void stop() {
            mAlphaScroller.stop();
            mRotateScroller.stop();
        }

        @Override
        public void onDraw(Canvas canvas) {
            if (null != mHexagons) {
                canvas.save();
                canvas.translate(getWidth() / 2.0f, 0f);
                final int size = mHexagons.length - 2;
                for (int i = 0; i < size; i++) {
                    mPaint.setColor(COLORS[i]);
                    canvas.drawPath(mHexagons[i].path, mPaint);
                }

                // The forth and fifth hexagon need radialGradient
                canvas.drawPath(mHexagons[size].path, mForthPaint);
                canvas.drawPath(mHexagons[size + 1].path, mLastPaint);
                canvas.restore();
            }
        }
    }

    private class NullState implements IState {
        private SingelScroller mRotateScroller = new SingelScroller();
        private boolean isFirst = true;

        @Override
        public void running() {
            if (!mRotateScroller.computeScrollOffset()) {
                setCurrentState(STATE_RUNNING);
            }
        }

        @Override
        public void onDraw(Canvas canvas) {
        }

        @Override
        public void reset() {
            if(isFirst){
                mRotateScroller.start(0, 1f, 1 * 1000);
                isFirst = false;
            }else{
                mRotateScroller.start(0, 1f, 3 * 1000);
            }
        }

        @Override
        public void pause() {
            mRotateScroller.pause();
        }

        @Override
        public void resume() {
            mRotateScroller.resume();
        }

        @Override
        public void stop() {
            mRotateScroller.stop();
        }
    }
}
