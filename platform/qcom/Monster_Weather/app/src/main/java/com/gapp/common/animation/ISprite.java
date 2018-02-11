package com.gapp.common.animation;

import android.graphics.Canvas;
import android.graphics.RectF;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-15.
 * the sprite for {@link IPainterView}
 */
public interface ISprite {

    void setUp(IPainterView painterView);

    void onLayout(boolean changed, int left, int top, int right, int bottom);

    void draw(Canvas canvas);

    void running();

    void pause();

    void resume();

    void getLocation(RectF rectf);

    void tearDown();

    int getZOrder();

}
