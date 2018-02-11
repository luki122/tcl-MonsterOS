package com.gapp.common.animation.scenes;

import android.content.Context;
import android.view.View;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.view.ViewHaloSprite;
import com.gapp.common.animation.view.ViewSunLightSprite;
import com.gapp.common.utils.BitmapManager;
import com.leon.tools.view.AndroidUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-9-12.
 * $desc
 */
public class SunnyScenes extends AbsScenes {
    private final static float RADIUS_SIZE = 330f;
    private final static float X = 132f / 3.0f;
    private final static float Y = -100f / 3.0f;

    private final static float OFFSET_X = 15f;
    private final static float OFFSET_Y = 5f;


    public SunnyScenes(IPainterView painterView, BitmapManager manager) {
        super(painterView, manager);
    }

    @Override
    public void init() {
        super.init();
        Context context = ((View) mPainterView).getContext();
        View parent = (View) ((View) mPainterView).getParent();
        if (null != parent) {
            ScenesBgDrawable drawable = new ScenesBgDrawable();
            drawable.setLinearColors(0, 0.7f, 0xffbcddf8, 0x00bcddf8);
            ((View) mPainterView).setBackground(drawable);
        }

        int raidus = (int) AndroidUtils.dip2px(context, RADIUS_SIZE);
        int x = (int) AndroidUtils.dip2px(context, X);
        int y = (int) AndroidUtils.dip2px(context, Y);
        ViewHaloSprite sprite = new ViewHaloSprite(context);
        sprite.setTranslationX(-raidus + x);
        sprite.setTranslationY(-raidus + y);
        mPainterView.addSprite(sprite);

        ViewSunLightSprite vsSprite = new ViewSunLightSprite(context);
        vsSprite.setTranslationX(AndroidUtils.dip2px(context, OFFSET_X));
        vsSprite.setTranslationY(AndroidUtils.dip2px(context, OFFSET_Y));
        mPainterView.addSprite(vsSprite);
    }
}
