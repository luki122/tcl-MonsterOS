package com.gapp.common.animation.scenes;

import android.content.Context;

import com.gapp.common.animation.IPainterView;
import com.gapp.common.animation.view.ThunderSprite;
import com.gapp.common.utils.BitmapManager;

/**
 * Created on 16-10-31.
 */
public class ThunderShowerScenes extends AbsRainScenes{

        private final static int LEVE_1_SIZE = 6;
        private final static int LEVE_2_SIZE = 48;
        private final static int LEVE_3_SIZE = 30;

        public ThunderShowerScenes(IPainterView painterView, BitmapManager manager) {
            super(painterView, manager);
            mSpriteAddingManager.setAddSkipTimeMills(5500/(LEVE_1_SIZE + LEVE_2_SIZE + LEVE_3_SIZE));
        }

        @Override
        protected void onInit(Context context) {
            postAddItems(context, 0.8f, 0.9f, LEVE_1_SIZE, 2000, 1);
            postAddItems(context, 0.6f, 0.7f, LEVE_2_SIZE, 3500, -1);
            postAddItems(context, 0.4f, 0.5f, LEVE_3_SIZE, 5500, -1);

            mPainterView.addSprite(new ThunderSprite(context).setZOrder(-3));
        }

}
