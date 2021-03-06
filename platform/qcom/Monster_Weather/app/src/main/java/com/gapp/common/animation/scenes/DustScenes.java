package com.gapp.common.animation.scenes;

import android.content.Context;
import android.view.View;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.utils.BitmapManager;


/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-9-7.
 * $desc
 */
public class DustScenes extends AbsDustScenes {
    private final static int BIG_SIZE = 8;
    private final static int NORMAL_SIZE = 11;
    private final static int SMALL_SIZE = 11;

    private final static int BIG_TIME = 3000;

    private final static int NORMAL_TIME = BIG_TIME * 10 / 7;

    private final static int SAMLL_TIME = BIG_TIME * 2;

    public DustScenes(IPainterView painterView, BitmapManager manager) {
        super(painterView, manager);
        mSpriteAddingManager.setAddSkipTimeMills(SAMLL_TIME / (BIG_SIZE + NORMAL_SIZE + SMALL_SIZE));
    }

    @Override
    public void init() {
        super.init();
        Context context = ((View) mPainterView).getContext();
        postAddItems(context, 0.9f, 1.0f, BIG_SIZE, BIG_TIME, 1);
        postAddItems(context, 0.6f, 0.7f, NORMAL_SIZE, NORMAL_TIME, -1);
        postAddItems(context, 0.4f, 0.5f, SMALL_SIZE, SAMLL_TIME, -1);
    }
}
