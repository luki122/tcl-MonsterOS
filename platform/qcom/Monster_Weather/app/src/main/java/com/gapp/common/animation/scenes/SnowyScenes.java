package com.gapp.common.animation.scenes;

import android.content.Context;
import android.os.Handler;
import android.view.View;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.view.SnowSprite;
import com.gapp.common.utils.BitmapManager;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-31.
 * $desc
 */
public class SnowyScenes extends AbsScenes {

    private int size = 30;


    private Handler mHandler = new Handler();

    public SnowyScenes(IPainterView painterView, BitmapManager manager) {
        super(painterView, manager);
    }

    @Override
    public void init() {
        super.init();
        Context context = ((View) mPainterView).getContext();
        View parent = (View) ((View) mPainterView).getParent();
        if (null != parent) {
            ScenesBgDrawable drawable = new ScenesBgDrawable();
            drawable.setLinearColors(0, 0.7f, 0xffd5eff9, 0x00d5eff9);
            ((View) mPainterView).setBackground(drawable);
        }


        postAddItems(context, 0.8f, 0.9f, 30, 16000, -1);

//        for (int i = 0; i < size; i++) {
//            mPainterView.addSprite(new SnowSprite(context));
//        }
//        SnowConnector connector = new SnowConnector(context);
//        connector.setTargetView(parent.findViewById(R.id.city_weather_ll_cardlayout));
//        mPainterView.addServantConnnecter(connector);
    }


    private void postAddItems(final Context context, final float min, final float max, int size, int timeMills, final int zOrder) {
        final int time = timeMills / size;

        for (int i = 0; i < size; i++) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SnowSprite sprite = new SnowSprite(context);
//                    sprite.setScaleRange(min, max);
//                    sprite.setZOrder(zOrder);
                    mPainterView.addSprite(sprite);
                }
            }, i * time);
        }
    }
}
