package com.monster.launcher.effect;

import android.view.View;

import com.monster.launcher.PagedView;

/**
 * Created by antino on 16-7-12.
 */
public class Cube extends TransitionEffect{
    private boolean mIn;
    protected static float CAMERA_DISTANCE = 6500;
    public Cube(PagedView pagedView, boolean in) {
        super(pagedView, in ? TRANSITION_EFFECT_CUBE_IN : TRANSITION_EFFECT_CUBE_OUT);
        mIn = in;
    }

    @Override
    public void screenScrolled(View v, int i, float scrollProgress) {
        float rotation = (mIn ? 90.0f : -90.0f) * scrollProgress;
        if (mIn) {
            v.setCameraDistance(mPagedView.getDensity() * CAMERA_DISTANCE);
        }

        v.setPivotX(scrollProgress < 0 ? 0 : v.getMeasuredWidth());
        v.setPivotY(v.getMeasuredHeight() * 0.5f);
        v.setRotationY(rotation);
    }
}
