package com.gapp.common.animation.scenes;

import android.content.Context;
import android.os.Handler;
import android.view.View;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.view.RainSprite;
import com.gapp.common.animation.view.ViewCloudSprite;
import com.gapp.common.utils.BitmapManager;
import com.gapp.common.utils.RandomUtils;
import com.leon.tools.view.AndroidUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-10-18.
 * $desc
 */
public abstract class AbsRainScenes extends AbsScenes {

    protected Handler mHandler = new Handler();
    protected RandomUtils mUtils = new RandomUtils();

    public AbsRainScenes(IPainterView painterView, BitmapManager manager) {
        super(painterView, manager);
    }

    protected void postAddItems(final Context context, final float min, final float max, int size, int timeMills, final int zOrder) {
        final int time = timeMills / size;

        for (int i = 0; i < size; i++) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    RainSprite sprite = new RainSprite(context);
                    sprite.setRandomUtils(mUtils);
                    sprite.setScaleRange(min, max);
                    sprite.setZOrder(zOrder);
                    mSpriteAddingManager.addSprite(sprite);
                }
            }, i * time);
        }
    }

    protected abstract void onInit(Context context);

    @Override
    public void init() {
        super.init();
        final Context context = ((View) mPainterView).getContext();
        View parent = (View) ((View) mPainterView).getParent();
        if (null != parent) {
            ScenesBgDrawable drawable = new ScenesBgDrawable();
            drawable.setLinearColors(0, 0.7f, 0xffa4b8c9, 0x00a4b8c9);
            ((View) mPainterView).setBackground(drawable);
        }

        onInit(context);


        float raidus1 = AndroidUtils.dip2px(context, 180);
        float raidus2 = AndroidUtils.dip2px(context, 260);
        float raidus3 = AndroidUtils.dip2px(context, 230);
        float raidus4 = AndroidUtils.dip2px(context, 180);

        int[] color1 = {0x84B8CAD8, 0x195981AB};
        int[] color2 = {0x33B8CAD8, 0x0F5981AB};
        int[] color3 = {0x33B8CAD8, 0x385981AB};
        int[] color4 = {0x33B8CAD8, 0x235981AB};

        float px35 = AndroidUtils.dip2px(context, 35f);
        float px81_3 = AndroidUtils.dip2px(context, 81.3f);
        float px80 = AndroidUtils.dip2px(context, 80f);
        float px98_7 = AndroidUtils.dip2px(context, 98.7f);
        float px145 = AndroidUtils.dip2px(context, 145f);
        float px162_3 = AndroidUtils.dip2px(context, 162.3f);
        float px357_7 = AndroidUtils.dip2px(context, 357.7f);
        float px106_3 = AndroidUtils.dip2px(context, 106.3f);

        mPainterView.addSprite(new ViewCloudSprite(context).setColor(color1).setRadius(raidus1).setStartXY((int) (-raidus1 + px35), (int) (-raidus1 - px81_3)).setStartOffset(0.7f));
        mPainterView.addSprite(new ViewCloudSprite(context).setColor(color2).setRadius(raidus2).setStartXY((int) (-raidus2 + px80), (int) (-raidus2 - px98_7)));
        mPainterView.addSprite(new ViewCloudSprite(context).setColor(color3).setRadius(raidus3).setStartXY((int) (-raidus4 + px145), (int) (-raidus3 - px162_3)).setStartOffset(0.9f));
        mPainterView.addSprite(new ViewCloudSprite(context).setColor(color4).setRadius(raidus4).setStartXY((int) (-raidus4 + px357_7), (int) (-raidus4 - px106_3)).setStartOffset(0.3f));
    }
}
