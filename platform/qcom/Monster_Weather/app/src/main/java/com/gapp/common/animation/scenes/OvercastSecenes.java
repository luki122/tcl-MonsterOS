package com.gapp.common.animation.scenes;

import android.content.Context;
import android.view.View;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.view.ViewCloudSVGSprite;
import com.gapp.common.utils.BitmapManager;
import com.leon.tools.view.AndroidUtils;

import cn.tcl.weather.R;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-9-12.
 * $desc
 */
public class OvercastSecenes extends AbsScenes {
    private final static float OVERCAST_1_W = 398.7f;
    private final static float OVERCAST_1_H = 204.3f;

    private final static float OVERCAST_2_W = 447.3f;
    private final static float OVERCAST_2_H = 224f;

    private final static float OVERCAST_3_W = 585.7f;
    private final static float OVERCAST_3_H = 320f;

    private final static float OVERCAST_4_W = 616f;
    private final static float OVERCAST_4_H = 401f;


    private final static float OVERCAST_OFFSET_1_W = -316f / 3.0f;
    private final static float OVERCAST_OFFSET_1_H = -274f / 3.0f;

    private final static float OVERCAST_OFFSET_2_W = -406f / 3.0f;
    private final static float OVERCAST_OFFSET_2_H = -182f / 3.0f;

    private final static float OVERCAST_OFFSET_3_W = -406f / 3.0f;
    private final static float OVERCAST_OFFSET_3_H = -258f / 3.0f;

    private final static float OVERCAST_OFFSET_4_W = -394f / 3.0f;
    private final static float OVERCAST_OFFSET_4_H = -274f / 3.0f;


    private final static float SCROLL_HEIGHT = 25f / 3.0f;

    private final static float SCROLL_DX_1 = (352f - 281.5f) / 3.0f;
    private final static float SCROLL_DY_1 = (-4.5f - 31.5f) / 3.0f;

    private final static float SCROLL_DX_2 = (382.5f - 265f) / 3.0f;
    private final static float SCROLL_DY_2 = (99.5f - 153.5f) / 3.0f;

    private final static float SCROLL_DX_3 = (581f - 473f) / 3.0f;
    private final static float SCROLL_DY_3 = (173f - 224f) / 3.0f;

    private final static float SCROLL_DX_4 = (638 - 530f) / 3.0f;
    private final static float SCROLL_DY_4 = (264f - 322f) / 3.0f;

    public OvercastSecenes(IPainterView painterView, BitmapManager manager) {
        super(painterView, manager);
    }

    @Override
    public void init() {
        super.init();
        Context context = ((View) mPainterView).getContext();
        View parent = (View) ((View) mPainterView).getParent();
        if (null != parent) {
            ScenesBgDrawable drawable = new ScenesBgDrawable();
            drawable.setLinearColors(0, 0.7f, 0xffb3bcca, 0x00b3bcca);
            ((View) mPainterView).setBackground(drawable);
        }


        final float height = AndroidUtils.dip2px(context, SCROLL_HEIGHT);
        ViewCloudSVGSprite sprite = new ViewCloudSVGSprite(context, R.drawable.svg_overcast_4);
        sprite.setTranslationX(AndroidUtils.dip2px(context, OVERCAST_OFFSET_4_W));
        sprite.setTranslationY(AndroidUtils.dip2px(context, OVERCAST_OFFSET_4_H));
        sprite.setMovePoints(0, 0, AndroidUtils.dip2px(context, SCROLL_DX_4), AndroidUtils.dip2px(context, SCROLL_DY_4), height);
        sprite.setSize(OVERCAST_4_W, OVERCAST_4_H);
        mPainterView.addSprite(sprite);

        sprite = new ViewCloudSVGSprite(context, R.drawable.svg_overcast_3);
        sprite.setOffsetTime(1600);
        sprite.setTranslationX(AndroidUtils.dip2px(context, OVERCAST_OFFSET_3_W));
        sprite.setTranslationY(AndroidUtils.dip2px(context, OVERCAST_OFFSET_3_H));
        sprite.setMovePoints(0, 0, AndroidUtils.dip2px(context, SCROLL_DX_3), AndroidUtils.dip2px(context, SCROLL_DY_3), height);
        sprite.setSize(OVERCAST_3_W, OVERCAST_3_H);
        mPainterView.addSprite(sprite);

        sprite = new ViewCloudSVGSprite(context, R.drawable.svg_overcast_2);
        sprite.setOffsetTime(1600*2);
        sprite.setTranslationX(AndroidUtils.dip2px(context, OVERCAST_OFFSET_2_W));
        sprite.setTranslationY(AndroidUtils.dip2px(context, OVERCAST_OFFSET_2_H));
        sprite.setMovePoints(0, 0, AndroidUtils.dip2px(context, SCROLL_DX_2), AndroidUtils.dip2px(context, SCROLL_DY_2), height);
        sprite.setSize(OVERCAST_2_W, OVERCAST_2_H);
        mPainterView.addSprite(sprite);

        sprite = new ViewCloudSVGSprite(context, R.drawable.svg_overcast_1);
        sprite.setTranslationX(AndroidUtils.dip2px(context, OVERCAST_OFFSET_1_W));
        sprite.setTranslationY(AndroidUtils.dip2px(context, OVERCAST_OFFSET_1_H));
        sprite.setMovePoints(0, 0, AndroidUtils.dip2px(context, SCROLL_DX_1), AndroidUtils.dip2px(context, SCROLL_DY_1), height);
        sprite.setOffsetTime(1600*3);
        sprite.setSize(OVERCAST_1_W, OVERCAST_1_H);
        mPainterView.addSprite(sprite);

    }


}
