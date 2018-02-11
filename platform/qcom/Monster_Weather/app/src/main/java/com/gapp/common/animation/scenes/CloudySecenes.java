package com.gapp.common.animation.scenes;

import android.content.Context;
import android.view.View;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.view.ViewCloudSprite;
import com.gapp.common.utils.BitmapManager;
import com.leon.tools.view.AndroidUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-9-12.
 * $desc
 */
public class CloudySecenes extends AbsScenes {

    public CloudySecenes(IPainterView painterView, BitmapManager manager) {
        super(painterView, manager);
    }

    @Override
    public void init() {
        super.init();
        Context context = ((View) mPainterView).getContext();
        View parent = (View) ((View) mPainterView).getParent();
        if (null != parent) {
            ScenesBgDrawable drawable = new ScenesBgDrawable();
            drawable.setLinearColors(0, 0.7f, 0xffa1c8e3, 0x00a1c8e3);
            ((View) mPainterView).setBackground(drawable);
        }
        float raidus1 = AndroidUtils.dip2px(context, 183);
        float raidus2 = AndroidUtils.dip2px(context, 235);
        float raidus3 = AndroidUtils.dip2px(context, 230);
        float raidus4 = AndroidUtils.dip2px(context, 236);

        int[] color1 = {0x0F9BC4E1, 0x0F4892CA};
        int[] color2 = {0x199BC4E1, 0x164892CA};
        int[] color3 = {0x199BC4E1, 0x0F4892CA};
        int[] color4 = {0x199BC4E1, 0x0F4892CA};

        float px53 = AndroidUtils.dip2px(context, 53.7f);
        float px70_3 = AndroidUtils.dip2px(context, 70.3f);
        float px105_3 = AndroidUtils.dip2px(context, 105.3f);
        float px164 = AndroidUtils.dip2px(context, 164);
        float px248_3 = AndroidUtils.dip2px(context, 248.3f);
        float px155_7 = AndroidUtils.dip2px(context, 155.7f);
        float px251_3 = AndroidUtils.dip2px(context, 251.3f);
        float px111 = AndroidUtils.dip2px(context, 111);

        mPainterView.addSprite(new ViewCloudSprite(context).setColor(color1).setRadius(raidus1).setStartXY((int) (-raidus1 - px53), (int) (-raidus1 - px70_3)).setStartOffset(0.7f));
        mPainterView.addSprite(new ViewCloudSprite(context).setColor(color2).setRadius(raidus2).setStartXY((int) (-raidus2 + px105_3), (int) (-raidus2 - px164)));
        mPainterView.addSprite(new ViewCloudSprite(context).setColor(color3).setRadius(raidus3).setStartXY((int) (-raidus4 + px248_3), (int) (-raidus3 - px155_7)).setStartOffset(0.9f));
        mPainterView.addSprite(new ViewCloudSprite(context).setColor(color4).setRadius(raidus4).setStartXY((int) (-raidus4 + px251_3), (int) (-raidus4 - px111)).setStartOffset(0.3f));
    }


}
