package com.monster.launcher.effect;

import android.view.View;

import com.monster.launcher.PagedView;

/**
 * Created by antino on 16-7-12.
 */
public class Rotate extends TransitionEffect {
    protected static final float TRANSITION_SCREEN_ROTATION = 12.5f;
    private boolean mUp;

    public Rotate(PagedView pagedView, boolean up) {
        super(pagedView, up ? TRANSITION_EFFECT_ROTATE_UP : TRANSITION_EFFECT_ROTATE_DOWN);
        mUp = up;
    }

    @Override
    public void screenScrolled(View v, int i, float scrollProgress) {
        float rotation =
                (mUp ? TRANSITION_SCREEN_ROTATION : -TRANSITION_SCREEN_ROTATION) * scrollProgress;
        float translationX = v.getMeasuredWidth() * scrollProgress;
        float rotatePoint =
                (v.getMeasuredWidth() * 0.5f) /
                        (float) Math.tan(Math.toRadians((double) (TRANSITION_SCREEN_ROTATION * 0.5f)));
        v.setPivotX(v.getMeasuredWidth() * 0.5f);
        if (mUp) {
            v.setPivotY(-rotatePoint);
        } else {
            v.setPivotY(v.getMeasuredHeight() + rotatePoint);
        }
        v.setRotation(rotation);
        v.setTranslationX(translationX);
    }
}

