package com.gapp.common.animation.scenes;

import android.content.Context;
import android.os.Handler;
import android.view.View;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.view.DustSprite;
import com.gapp.common.utils.BitmapManager;
import com.gapp.common.utils.RandomUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-10-18.
 * $desc
 */
public class AbsDustScenes extends AbsScenes {

    protected Handler mHandler = new Handler();
    protected RandomUtils mUtils = new RandomUtils(30);


    public AbsDustScenes(IPainterView painterView, BitmapManager manager) {
        super(painterView, manager);
    }


    @Override
    public void init() {
        super.init();
        View parent = (View) ((View) mPainterView).getParent();
        if (null != parent) {
            ScenesBgDrawable drawable = new ScenesBgDrawable();
            drawable.setLinearColors(0, 0.7f, 0xffd0ba95, 0x00d0ba95);
            ((View) mPainterView).setBackground(drawable);
        }
    }


    protected void postAddItems(final Context context, final float min, final float max, int size, int timeMills, final int zOrder) {
        final int time = timeMills / size;

        for (int i = 0; i < size; i++) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    DustSprite sprite = new DustSprite(context);
                    sprite.setRandomUtils(mUtils);
                    sprite.setScaleRange(min, max);
                    sprite.setZOrder(zOrder);
                    mSpriteAddingManager.addSprite(sprite);
                }
            }, i * time);
        }
    }


    protected void postAddItems(final Context context, final float min, final float max, int size, int timeMills, final int zOrder, final boolean isStorm) {
        final int time = timeMills / size;

        for (int i = 0; i < size; i++) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    DustSprite sprite = new DustSprite(context);
                    sprite.setTimeMills(13*1000);
                    sprite.setIsStorm(isStorm);
                    sprite.setRandomUtils(mUtils);
                    sprite.setScaleRange(min, max);
                    sprite.setZOrder(zOrder);
                    mSpriteAddingManager.addSprite(sprite);
                }
            }, i * time);
        }
    }

}
