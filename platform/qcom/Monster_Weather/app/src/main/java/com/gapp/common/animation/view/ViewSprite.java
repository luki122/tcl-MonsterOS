package com.gapp.common.animation.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.ISprite;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created on 16-8-22.
 * $desc
 */
public abstract class ViewSprite extends View implements ISprite {
    protected Paint mPaint = new Paint();

    protected IPainterView mParent;

    private int mZorder = -1;

    public ViewSprite(Context context) {
        super(context);
    }

    public ViewSprite(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewSprite(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ViewSprite(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public final void setUp(IPainterView painterView) {
        mParent = painterView;
        onSetUp();
    }

    protected void onSetUp() {

    }

    @Override
    public final void tearDown() {
        onTearDown();
    }

    protected void onTearDown() {
    }


    @Override
    public final void getLocation(RectF rectf) {
        rectf.set(getTranslationX() + getLeft(), getTranslationY() + getTop(), getTranslationX() + getRight(), getTranslationY() + getBottom());
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    public ViewSprite setZOrder(int order) {
        mZorder = order;
        return this;
    }

    @Override
    public int getZOrder() {
        return mZorder;
    }
}
