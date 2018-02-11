package com.gapp.common.animation.scenes;

import android.content.Context;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.utils.BitmapManager;

/**
 * Created on 16-9-9.
 */
public class RainScenes extends AbsRainScenes {
    private int size = 30;
    private final static float RADIUS_SIZE = 300f;

    public RainScenes(IPainterView painterView, BitmapManager manager) {
        super(painterView, manager);
        mSpriteAddingManager.setAddSkipTimeMills(2000 / size);
    }

    @Override
    protected void onInit(Context context) {
        postAddItems(context, 0.4f, 1.0f, size, 2000, -1);
    }
}
