package com.gapp.common.animation.scenes;

import android.os.Handler;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.ISprite;
import com.gapp.common.obj.IManager;

import java.util.LinkedList;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-10-20.
 * $desc
 */
public class SpriteAddingManager implements IManager, Runnable {

    private IPainterView mPainterView;

    private boolean isAdding;

    private LinkedList<ISprite> mAddingSprite = new LinkedList<>();
    private int mStepTimeMills;
    private Handler mHandler = new Handler();

    private boolean isPaused = true;


    public SpriteAddingManager(IPainterView painterView) {
        this(painterView, 60);
    }

    public SpriteAddingManager(IPainterView painterView, int addSkipTimeMills) {
        mPainterView = painterView;
        mStepTimeMills = addSkipTimeMills;
    }

//    public void setData(int timeMills, int maxSize) {
//        mStepTimeMills = timeMills / maxSize;
//    }

    public void setAddSkipTimeMills(int timeMills) {
        mStepTimeMills = timeMills;
    }


    public void addSprite(ISprite sprite) {
        mAddingSprite.add(sprite);
        startAdding();
    }

    public void pause() {
        stopAdding();
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
        startAdding();
    }

    private void startAdding() {
        if (!isPaused && !isAdding && !mAddingSprite.isEmpty()) {
            isAdding = true;
            mHandler.removeCallbacks(this);
            mHandler.post(this);
        }
    }

    private void stopAdding() {
        if (isAdding) {
            isAdding = false;
            mHandler.removeCallbacks(this);
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void recycle() {
        stopAdding();
        mAddingSprite.clear();
    }

    @Override
    public void onTrimMemory(int level) {

    }

    @Override
    public void run() {
        ISprite sprite = mAddingSprite.poll();
        if (null != sprite) {
            mPainterView.addSprite(sprite);
            mHandler.postDelayed(this, mStepTimeMills);
        }
    }
}
