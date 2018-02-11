package com.monster.launcher.effect;

import android.view.View;

import com.monster.launcher.PagedView;

/**
 * Created by antino on 16-7-12.
 */
public class Cylinder extends TransitionEffect {
    protected static final float TRANSITION_SCREEN_ROTATION = 12.5f;
    private boolean mIn;

    public Cylinder(PagedView pagedView, boolean in) {
        super(pagedView, in ? TRANSITION_EFFECT_CYLINDER_IN : TRANSITION_EFFECT_CYLINDER_OUT);
        mIn = in;
    }

    @Override
    public void screenScrolled(View v, int i, float scrollProgress) {
        float rotation = (mIn ? TRANSITION_SCREEN_ROTATION : -TRANSITION_SCREEN_ROTATION) * scrollProgress;
        v.setPivotX((scrollProgress + 1) * v.getMeasuredWidth() * 0.5f);
        v.setPivotY(v.getMeasuredHeight() * 0.5f);
        v.setRotationY(rotation);
    }
}