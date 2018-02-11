package com.gapp.common.animation.scenes;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.obj.IManager;
import com.gapp.common.utils.BitmapManager;

import cn.tcl.weather.utils.CommonUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-31.
 * $desc
 */
public abstract class AbsScenes implements IManager {

    protected IPainterView mPainterView;
    protected BitmapManager mBitmapManager;
    private boolean isStart;
    protected SpriteAddingManager mSpriteAddingManager;

    public AbsScenes(IPainterView painterView, BitmapManager manager) {
        mPainterView = painterView;
        mBitmapManager = manager;
        mSpriteAddingManager = new SpriteAddingManager(mPainterView);
    }

    @Override
    public void init() {
        mBitmapManager.init();
        mPainterView.init();
        mSpriteAddingManager.init();
        isStart = !CommonUtils.IS_DEBUG;
    }

    public void pause() {
        if (isStart) {
            mSpriteAddingManager.pause();
            mPainterView.stop();
        }
    }

    public void toggleAnmate() {
        if (CommonUtils.IS_DEBUG) {
            if (isStart) {
                pause();
                isStart = false;
            } else {
                isStart = true;
                resume();
            }
        }
    }


    public void resume() {
        if (isStart) {
            mPainterView.start();
            mSpriteAddingManager.resume();
        }
    }


    @Override
    public void recycle() {
        mSpriteAddingManager.recycle();
        mPainterView.clearServantConnecters();
        mPainterView.clearSprites();
        mPainterView.recycle();
        mBitmapManager.recycle();
    }

    @Override
    public void onTrimMemory(int level) {
        mPainterView.onTrimMemory(level);
        mBitmapManager.onTrimMemory(level);
    }
}
