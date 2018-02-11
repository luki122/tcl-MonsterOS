package com.monster.launcher.effect;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.monster.launcher.PagedView;

/**
 * Created by antino on 16-7-12.
 */
public class Overview extends TransitionEffect {
    private AccelerateDecelerateInterpolator mScaleInterpolator = new AccelerateDecelerateInterpolator();

    public Overview(PagedView pagedView) {
        super(pagedView, TRANSITION_EFFECT_OVERVIEW);
    }

    @Override
    public void screenScrolled(View v, int i, float scrollProgress) {
        float scale = 1.0f - 0.1f *
                mScaleInterpolator.getInterpolation(Math.min(0.3f, Math.abs(scrollProgress)) / 0.3f);

        v.setPivotX(scrollProgress < 0 ? 0 : v.getMeasuredWidth());
        v.setPivotY(v.getMeasuredHeight() * 0.5f);
        v.setScaleX(scale);
        v.setScaleY(scale);
        float alpha = scale;
        v.setAlpha(alpha);
    }
}
