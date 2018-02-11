package com.gapp.common.animation.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.IServantConnecter;
import com.gapp.common.animation.ISprite;
import com.gapp.common.utils.BitmapManager;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-22.
 * $desc
 */
public class PainterFrameView extends FrameLayout implements IPainterView {

    private ViewGroupPainterHelper mVgPainterHelper;
    private Rect mRect = new Rect();


    public PainterFrameView(Context context) {
        super(context);
    }

    public PainterFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PainterFrameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public PainterFrameView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (null != mVgPainterHelper) {
            mVgPainterHelper.addState(ViewGroupPainterHelper.STATE_READY);
        }
        mRect.set(0, 0, getWidth(), getHeight());
    }

    @Deprecated
    @Override
    public void setZOrderOnTop(boolean isOnTop) {

    }

    @Override
    public BitmapManager getBitmapManager() {
        return mVgPainterHelper.getBitmapManager();
    }

    @Override
    public void addSprite(ISprite sprite) {
        if (sprite instanceof ViewSprite) {
            mVgPainterHelper.addSprite(sprite);
        }
    }

    @Override
    public void removeSprite(ISprite sprite) {
        if (sprite instanceof ViewSprite) {
            mVgPainterHelper.removeSprite(sprite);
        }
    }

    @Override
    public void clearSprites() {
        mVgPainterHelper.clearSprites();
    }

    @Override
    public void start() {
        mVgPainterHelper.start();
    }

    @Override
    public void stop() {
        mVgPainterHelper.stop();
    }

    @Override
    public void addServantConnnecter(IServantConnecter connecter) {
        mVgPainterHelper.addServantConnnecter(connecter);
    }

    @Override
    public void removeServantConnecter(IServantConnecter connecter) {
        mVgPainterHelper.removeServantConnecter(connecter);
    }

    @Override
    public void clearServantConnecters() {
        mVgPainterHelper.clearServantConnecters();
    }

    @Override
    public void setOnRunningListener(OnRunningListener l) {
        mVgPainterHelper.setOnRunningListener(l);
    }


    @Override
    public void init() {
        mVgPainterHelper = new ViewGroupPainterHelper(this);
        mVgPainterHelper.init();
        if (getWidth() > 0 || getHeight() > 0) {
            mVgPainterHelper.addState(ViewGroupPainterHelper.STATE_READY);
        }
    }

    @Override
    public void recycle() {
        mVgPainterHelper.recycle();
    }

    @Override
    public void onTrimMemory(int level) {
        mVgPainterHelper.onTrimMemory(level);
    }

    @Override
    public void runOnThread(Runnable runnable) {
        mVgPainterHelper.runOnThread(runnable);
    }


}
