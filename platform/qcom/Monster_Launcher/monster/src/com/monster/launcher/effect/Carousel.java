package com.monster.launcher.effect;

import android.view.View;

import com.monster.launcher.PagedView;

/**
 * Created by antino on 16-7-12.
 */
public class Carousel extends TransitionEffect {
    protected static float CAMERA_DISTANCE = 6500;
    public Carousel(PagedView pagedView) {
        super(pagedView, TRANSITION_EFFECT_CAROUSEL);
    }

    @Override
    public void screenScrolled(View v, int i, float scrollProgress) {
        float rotation = 90.0f * scrollProgress;

        v.setCameraDistance(mPagedView.getDensity() * CAMERA_DISTANCE);
        v.setTranslationX(v.getMeasuredWidth() * scrollProgress);
        v.setPivotX(!mPagedView.isLayoutRtl() ? 0f : v.getMeasuredWidth());
        v.setPivotY(v.getMeasuredHeight() / 2);
        v.setRotationY(-rotation);
    }
}
