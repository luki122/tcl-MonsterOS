package com.gapp.common.animation.scenes;

import android.content.Context;
import android.view.View;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.utils.BitmapManager;
import com.leon.tools.view.AndroidUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-31.
 * $desc
 */
public class HeavySnowScenes extends AbsSnowScenes {

    private final static float RADIUS = 8.3f;

    private final static int BIG_SIZE = 16;
    private final static int NORMAL_SIZE = 32;
    private final static int SMALL_SIZE = 42;


    public HeavySnowScenes(IPainterView painterView, BitmapManager manager) {
        super(painterView, manager);
        mSpriteAddingManager.setAddSkipTimeMills(20000 / (BIG_SIZE + NORMAL_SIZE + SMALL_SIZE));
    }

    @Override
    public void init() {
        super.init();
        Context context = ((View) mPainterView).getContext();
        final int radius = (int) AndroidUtils.dip2px(context, RADIUS);
        postAddItems(context, radius, 0.9f, 1.0f, BIG_SIZE, 10000, 1);
        postAddItems(context, radius, 0.6f, 0.7f, NORMAL_SIZE, 17000, -1);
        postAddItems(context, radius, 0.4f, 0.5f, SMALL_SIZE, 20000, -1);
    }
}
