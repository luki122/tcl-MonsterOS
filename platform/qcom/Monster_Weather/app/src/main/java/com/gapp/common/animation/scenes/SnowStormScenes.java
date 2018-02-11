package com.gapp.common.animation.scenes;

import android.content.Context;
import android.view.View;
import android.view.ViewTreeObserver;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.connector.SnowConnector;
import com.gapp.common.utils.BitmapManager;
import com.leon.tools.view.AndroidUtils;

import cn.tcl.weather.R;
import cn.tcl.weather.view.BoardLinerLayout;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p>
 * Created on 16-8-31.
 * $desc
 */
public class SnowStormScenes extends AbsSnowScenes {
    private final static float RADIUS = 7.0f;

    private final static int BIG_SIZE = 12;
    private final static int NORMAL_SIZE = 32;
    private final static int SMALL_SIZE = 56;


    public SnowStormScenes(IPainterView painterView, BitmapManager manager) {
        super(painterView, manager);
        mSpriteAddingManager.setAddSkipTimeMills(20000 / (BIG_SIZE + NORMAL_SIZE + SMALL_SIZE));
    }

    @Override
    public void init() {
        super.init();
        Context context = ((View) mPainterView).getContext();
        final int radius = (int) AndroidUtils.dip2px(context, RADIUS);
        postAddItems(context, radius, 0.8f, 1.0f, BIG_SIZE, 10000, 1);
        postAddItems(context, radius, 0.6f, 0.7f, NORMAL_SIZE, 17000, -1);
        postAddItems(context, radius, 0.4f, 0.5f, SMALL_SIZE, 20000, -1);
//        initConnector((View) mPainterView, context, radius);// remove do not add this
    }

    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;

    private void recycleConnector() {
        if (null != mOnScrollChangedListener)
            ((View) mPainterView).getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
        mOnScrollChangedListener = null;
    }

    private void initConnector(View parent, Context context, final int radius) {
        final SnowConnector connector2 = new SnowConnector(context);
        mPainterView.addServantConnnecter(connector2);
        final BoardLinerLayout bll = (BoardLinerLayout) parent.findViewById(R.id.city_weather_ll_cardlayout);
        final View header = parent.findViewById(R.id.weather_head_view);
        mOnScrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                connector2.setTranslationY(header.getTranslationY());
            }
        };
        parent.getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener);
        bll.setOnBoardLinerLayoutListener(new BoardLinerLayout.OnBoardLinerLayoutListener() {

            private boolean isAnimation;

            @Override
            public void onLayout(boolean changed, int l, int t, int r, int b) {
                final int width = bll.getWidth();
                final int height = bll.getTop();
                connector2.setCheckerboard(80, 40);
                connector2.setConnectorSize(width, height);
//                connector2.setBackgroundColor(0x6044ff44);
                connector2.setPosition(l, t - height + 2*radius);
                connector2.setPadding(0, 0, 0, 2*radius);
                connector2.setMaxSize(100);
            }

            @Override
            public void onScrollChanged(float cy) {
                if (isAnimation)
                    return;
                if (cy < 0) {
                    isAnimation = true;
                    connector2.startBallAnimation();
                    connector2.removeCallbacks(mResetRunnable);
                    connector2.postDelayed(mResetRunnable, 800);
                } else {
                    connector2.setTranslationY(cy);// connector follow bll change
                }
            }


            private Runnable mResetRunnable = new Runnable() {
                @Override
                public void run() {
                    connector2.setTranslationY(bll.getTranslationY());
                    isAnimation = false;
                }
            };
        });
    }

    @Override
    public void recycle() {
        recycleConnector();
        super.recycle();
    }
}
