package com.gapp.common.animation.scenes;

import android.content.Context;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.utils.BitmapManager;

/**
 * Created on 16-9-9.
 */
public class SevereStormScenes extends AbsRainScenes {
    private final static int LEVE_1_SIZE = 30;
    private final static int LEVE_2_SIZE = 120;
    private final static int LEVE_3_SIZE = 70;

    public SevereStormScenes(IPainterView painterView, BitmapManager manager) {
        super(painterView, manager);
        mSpriteAddingManager.setAddSkipTimeMills(3600/(LEVE_1_SIZE + LEVE_2_SIZE + LEVE_3_SIZE));
    }

    @Override
    protected void onInit(Context context) {
        postAddItems(context, 0.9f, 1.0f, LEVE_1_SIZE, 2000, 1);
        postAddItems(context, 0.7f, 0.8f, LEVE_2_SIZE, 3000, -1);
        postAddItems(context, 0.5f, 0.6f, LEVE_3_SIZE, 3600, -1);
    }
}
