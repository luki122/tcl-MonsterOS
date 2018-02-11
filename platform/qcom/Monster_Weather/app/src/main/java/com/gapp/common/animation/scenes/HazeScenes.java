package com.gapp.common.animation.scenes;

import android.content.Context;
import android.view.View;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.view.ViewLRSVGSprite;
import com.gapp.common.utils.BitmapManager;
import com.leon.tools.view.AndroidUtils;

import cn.tcl.weather.R;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-7.
 * $desc
 */
public class HazeScenes extends AbsScenes {

    private int size = 100;

    public HazeScenes(IPainterView painterView, BitmapManager manager) {
        super(painterView, manager);
    }


    @Override
    public void init() {
        super.init();
        Context context = ((View) mPainterView).getContext();
        View parent = (View) ((View) mPainterView).getParent();
        if (null != parent) {
            ScenesBgDrawable drawable = new ScenesBgDrawable();
            drawable.setLinearColors(0, 0.7f, 0xFFA9B0BA, 0x00A9B0BA);
            ((View) mPainterView).setBackground(drawable);
        }

        ViewLRSVGSprite foggy1 = new ViewLRSVGSprite(context, R.drawable.svg_sand2_drawable, false);
        foggy1.setSize(478.3f, 256f);
        foggy1.setTranslationX(AndroidUtils.dip2px(context, -150f));
        foggy1.setTranslationY(AndroidUtils.dip2px(context, 0f));
        mPainterView.addSprite(foggy1);

        ViewLRSVGSprite foggy2 = new ViewLRSVGSprite(context, R.drawable.svg_sand1_drawable);
        foggy2.setSize(383.3f, 181.7f);
        foggy2.setTranslationX(AndroidUtils.dip2px(context, 193f));
        foggy2.setTranslationY(AndroidUtils.dip2px(context, 20f));
        mPainterView.addSprite(foggy2);

    }
}
