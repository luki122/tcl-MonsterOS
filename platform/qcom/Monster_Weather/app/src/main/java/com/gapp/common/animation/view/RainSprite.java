package com.gapp.common.animation.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

import com.gapp.common.interpolator.LinearInterpolatorXy;
import com.gapp.common.scroller.XyScroller;
import com.gapp.common.utils.RandomUtils;
import com.leon.tools.view.AndroidUtils;

/**
 * Created by on 16-9-9.
 */
public class RainSprite extends ViewSprite {


    private final static float SCALE_TIMES = 1000f;

    private final static int TIME_MILLS = 1 * 1000;

    private XyScroller mXyScroller = new XyScroller();

    private float[] mScaleSpace = new float[]{0.4f, 1.0f};

    private boolean isInfinite = true;

    private RandomUtils mRandom;

    public RainSprite(Context context) {
        super(context);
    }

    public RainSprite(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RainSprite(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public RainSprite(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void setRandomUtils(RandomUtils utils) {
        mRandom = utils;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) AndroidUtils.dip2px(getContext(), 1.6f),
                (int) AndroidUtils.dip2px(getContext(), 36f));
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed)
            reset();
    }

    public void setInfinite(boolean isInfinite) {
        this.isInfinite = isInfinite;
    }


    public void setScaleRange(float min, float max) {
        mScaleSpace[0] = min * SCALE_TIMES;
        mScaleSpace[1] = max * SCALE_TIMES;
    }


    public void reset() {
        final float scale = RandomUtils.getRandomFloat(mScaleSpace[0], mScaleSpace[1]) / SCALE_TIMES;

        float k = RandomUtils.getRandomFloat(1.7f, 1.8f);

        final int width = mParent.getWidth();

        setTranslationX(mRandom.getRandomIntOffset(0, (int) (width * k)));

        setTranslationY(-getHeight() - RandomUtils.getRandomInt((int) AndroidUtils.dip2px(getContext(), 36f)));

        //The rain in the distance fell more slowly.
        int timeMills = (int) (TIME_MILLS / scale);

        float deltaY = mParent.getHeight() + getHeight() - getTranslationY();
        float deltaX = -deltaY / k;
        mXyScroller.start(getTranslationX(), getTranslationY(), deltaX, deltaY, timeMills);

        setScaleX(scale);
        setScaleY(scale);
        setAlpha(scale);
        setRotation(30);
    }

    @Override
    protected void onSetUp() {
        mXyScroller.setInterpolatorXy(new LinearInterpolatorXy());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
    }


    @Override
    public void running() {
        if (mXyScroller.computeScrollOffset()) {
            setTranslationX(mXyScroller.getCurrentX());
            setTranslationY(mXyScroller.getCurrentY());
        } else if (isInfinite) {
            reset();
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

}
