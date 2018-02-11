package com.monster.launcher.effect;

import android.view.View;

import com.monster.launcher.PagedView;

/**
 * Created by antino on 16-7-12.
 */
public class Flip extends TransitionEffect {
    protected static float CAMERA_DISTANCE = 6500;

    public Flip(PagedView pagedView) {
        super(pagedView, TRANSITION_EFFECT_FLIP);
    }

    @Override
    public void screenScrolled(View v, int i, float scrollProgress) {
        float rotation = -180.0f * Math.max(-1f, Math.min(1f, scrollProgress));

        v.setCameraDistance(mPagedView.getDensity() * CAMERA_DISTANCE);
        v.setPivotX(v.getMeasuredWidth() * 0.5f);
        v.setPivotY(v.getMeasuredHeight() * 0.5f);
        v.setRotationY(rotation);

        if (scrollProgress >= -0.5f && scrollProgress <= 0.5f) {
            v.setTranslationX(v.getMeasuredWidth() * scrollProgress);
            if (v.getVisibility() != View.VISIBLE) {
                v.setVisibility(View.VISIBLE);
            }
        } else {
            v.setTranslationX(v.getMeasuredWidth() * -10f);
        }
    }
}
