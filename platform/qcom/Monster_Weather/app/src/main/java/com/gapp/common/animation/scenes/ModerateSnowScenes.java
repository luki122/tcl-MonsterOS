package com.gapp.common.animation.scenes;

import android.content.Context;
import android.view.View;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.utils.BitmapManager;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * Created on 16-8-31.
 * $desc
 */
public class ModerateSnowScenes extends AbsSnowScenes {

    private final static int BIG_SIZE = 10;
    private final static int NORMAL_SIZE = 20;
    private final static int SMALL_SIZE = 30;

    public ModerateSnowScenes(IPainterView painterView, BitmapManager manager) {
        super(painterView, manager);
        mSpriteAddingManager.setAddSkipTimeMills(20000 / (BIG_SIZE + NORMAL_SIZE + SMALL_SIZE));
    }

    @Override
    public void init() {
        super.init();
        Context context = ((View) mPainterView).getContext();
        postAddItems(context, 0.9f, 1.0f, BIG_SIZE, 10000, 1);
        postAddItems(context, 0.6f, 0.7f, NORMAL_SIZE, 17000, -1);
        postAddItems(context, 0.4f, 0.5f, SMALL_SIZE, 20000, -1);
    }
}
