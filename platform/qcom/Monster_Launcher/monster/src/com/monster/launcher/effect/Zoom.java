package com.monster.launcher.effect;

import android.view.View;

import com.monster.launcher.PagedView;

/**
 * Created by antino on 16-7-12.
 */
public class Zoom extends  TransitionEffect {
    private boolean mIn;
    public Zoom(PagedView pagedView, boolean in) {
        super(pagedView, in ? TRANSITION_EFFECT_ZOOM_IN : TRANSITION_EFFECT_ZOOM_OUT);
        mIn = in;
    }

    @Override
    public void screenScrolled(View v, int i, float scrollProgress) {
        float scale = 1.0f + (mIn ? -0.2f : 0.1f) * Math.abs(scrollProgress);
        if (!mIn) {
            float translationX = v.getMeasuredWidth() * 0.1f * -scrollProgress;
            v.setTranslationX(translationX);
        }
        v.setScaleX(scale);
        v.setScaleY(scale);
    }
}
