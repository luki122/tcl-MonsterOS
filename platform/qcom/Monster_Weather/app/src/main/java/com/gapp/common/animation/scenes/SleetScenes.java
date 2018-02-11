package com.gapp.common.animation.scenes;

import android.content.Context;
import android.os.Handler;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.view.SnowSprite;
import com.gapp.common.utils.BitmapManager;
import com.gapp.common.utils.RandomUtils;
import com.leon.tools.view.AndroidUtils;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-10-19.
 * $desc
 */
public class SleetScenes extends AbsRainScenes{
    private final static int RAIN_LIGHT_COUNT = 60;
    private final static int RAIN_MODERATE_COUNT = 40;
    private final static int RAIN_BIG_COUNT = 10;

    private final static int SNOW_LIGHT_COUNT = 18;
    private final static int SNOW_MODERATE_COUNT = 12;
    private final static int SNOW_BIG_COUNT = 5;

    private final float RADIUS = 8.3f;

    private Handler mHandler = new Handler();
    private RandomUtils mRandomUtils = new RandomUtils(SNOW_LIGHT_COUNT + SNOW_MODERATE_COUNT + SNOW_BIG_COUNT);

    public SleetScenes(IPainterView painterView, BitmapManager manager) {
        super(painterView, manager);
        mSpriteAddingManager.setAddSkipTimeMills(5500 / (SNOW_LIGHT_COUNT + SNOW_MODERATE_COUNT + SNOW_BIG_COUNT));
    }

    @Override
    protected void onInit(Context context) {
        // Snow sprite
        addSnowSprite(context, 0.4f, 0.5f, SNOW_LIGHT_COUNT, 5000, -1, 10);     // Light snow
        addSnowSprite(context, 0.6f, 0.7f, SNOW_MODERATE_COUNT, 10000, -1, 10); // Moderate snow
        addSnowSprite(context, 0.8f, 0.9f, SNOW_BIG_COUNT, 15000, 1, 10);       // Big snow

        // Rain sprite
        postAddItems(context, 0.8f, 0.9f, RAIN_BIG_COUNT, 2000, 1);         // Big rain
        postAddItems(context, 0.6f, 0.7f, RAIN_MODERATE_COUNT, 3500, -1);   // Moderate rain
        postAddItems(context, 0.4f, 0.5f, RAIN_LIGHT_COUNT, 5500, -1);      // Light rain
    }

    /**
     * Add snow to sleet scenes
     * @param context   this scenes' context
     * @param min       min diameter percent
     * @param max       max diameter percent
     * @param count     snow's count
     * @param timeMills Diffrent sprite have diffrent adding time, so their order can be disrupted.
     * @param zOrder    z order
     */
    private void addSnowSprite(final Context context, final float min, final float max, final int count, final int timeMills, final int zOrder, final int fallSecond){
        int delayTime = timeMills / count;
        final int radius = (int) AndroidUtils.dip2px(context, RADIUS);

        for(int i = 0; i<count; i++){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SnowSprite snowSprite = new SnowSprite(context);
                    snowSprite.setScaleRange(min, max);
                    snowSprite.setZOrder(zOrder);
                    snowSprite.setRandomUtils(mRandomUtils);
                    snowSprite.setRadius(radius);
                    snowSprite.setFallTime(fallSecond);
                    mSpriteAddingManager.addSprite(snowSprite);
                }
            }, delayTime * i);
        }
    }
}
