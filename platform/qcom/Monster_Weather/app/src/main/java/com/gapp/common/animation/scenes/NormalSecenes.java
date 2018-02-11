package com.gapp.common.animation.scenes;

import android.content.Context;
import android.view.View;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.utils.BitmapManager;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-12.
 * $desc
 */
public class NormalSecenes extends AbsScenes {

    public NormalSecenes(IPainterView painterView, BitmapManager manager) {
        super(painterView, manager);
    }

    @Override
    public void init() {
        super.init();
        Context context = ((View) mPainterView).getContext();
        View parent = (View) ((View) mPainterView).getParent();
        if (null != parent) {
            ScenesBgDrawable drawable = new ScenesBgDrawable();
            drawable.setLinearColors(0, 1.0f, 0xffd5eff9, 0x00d5eff9);
            ((View) mPainterView).setBackground(drawable);
        }
    }


}
