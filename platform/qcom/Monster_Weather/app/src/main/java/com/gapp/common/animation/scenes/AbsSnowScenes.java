package com.gapp.common.animation.scenes;

import android.content.Context;
import android.os.Handler;
import android.view.View;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.view.SnowSprite;
import com.gapp.common.utils.BitmapManager;
import com.gapp.common.utils.RandomUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-10-18.
 * $desc
 */
public class AbsSnowScenes extends AbsScenes {

    protected Handler mHandler = new Handler();
    protected RandomUtils mUtils = new RandomUtils(30);


    public AbsSnowScenes(IPainterView painterView, BitmapManager manager) {
        super(painterView, manager);
    }

    @Override
    public void init() {
        super.init();
        View parent = (View) ((View) mPainterView).getParent();
        if (null != parent) {
            ScenesBgDrawable drawable = new ScenesBgDrawable();
            drawable.setLinearColors(0, 0.7f, 0xff8EB5CB, 0x008EB5CB);
            ((View) mPainterView).setBackground(drawable);
        }
    }


    protected void postAddItems(final Context context, final float min, final float max, int size, int timeMills, final int zOrder) {
        final int time = timeMills / size;

        for (int i = 0; i < size; i++) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SnowSprite sprite = new SnowSprite(context);
                    sprite.setRandomUtils(mUtils);
                    sprite.setScaleRange(min, max);
                    sprite.setZOrder(zOrder);
                    mSpriteAddingManager.addSprite(sprite);
                }
            }, i * time);
        }
    }

    protected void postAddItems(final Context context, final int radius, final float min, final float max, int size, int timeMills, final int zOrder) {
        final int time = timeMills / size;

        for (int i = 0; i < size; i++) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SnowSprite sprite = new SnowSprite(context);
                    sprite.setRadius(radius);
                    sprite.setRandomUtils(mUtils);
                    sprite.setScaleRange(min, max);
                    sprite.setZOrder(zOrder);
                    mSpriteAddingManager.addSprite(sprite);
                }
            }, i * time);
        }
    }
}
