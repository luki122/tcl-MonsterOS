package com.monster.launcher.effect;

import android.view.View;

import com.monster.launcher.PagedView;

/**
 * Created by antino on 16-7-12.
 */
public class Accordion extends TransitionEffect {
    public Accordion(PagedView pagedView) {
        super(pagedView, TRANSITION_EFFECT_ACCORDION);
    }

    @Override
    public void screenScrolled(View v, int i, float scrollProgress) {
        float scale = 1.0f - Math.abs(scrollProgress);
        v.setScaleX(scale);
        v.setPivotX(scrollProgress < 0 ? 0 : v.getMeasuredWidth());
        v.setPivotY(v.getMeasuredHeight() / 2f);
    }
}