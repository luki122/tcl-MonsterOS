package com.monster.launcher.effect;

import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.monster.launcher.CellLayout;
import com.monster.launcher.PagedView;
import com.monster.launcher.effect.interpolators.ZInterpolator;

/**
 * Created by antino on 16-7-12.
 */
public class Stack extends TransitionEffect {
    private ZInterpolator mZInterpolator = new ZInterpolator(0.5f);
    private DecelerateInterpolator mLeftScreenAlphaInterpolator = new DecelerateInterpolator(4);
    protected AccelerateInterpolator mAlphaInterpolator = new AccelerateInterpolator(0.9f);
    private float TRANSITION_SCALE_FACTOR = 0.74f;

    public Stack(PagedView pagedView) {
        super(pagedView, TRANSITION_EFFECT_STACK);
    }

    @Override
    public void screenScrolled(View v, int i, float scrollProgress) {
        final boolean isRtl = mPagedView.isLayoutRtl();
        float interpolatedProgress;
        float translationX;
        float maxScrollProgress = Math.max(0, scrollProgress);
        float minScrollProgress = Math.min(0, scrollProgress);

        if (mPagedView.isLayoutRtl()) {
            translationX = maxScrollProgress * v.getMeasuredWidth();
            interpolatedProgress = mZInterpolator.getInterpolation(Math.abs(maxScrollProgress));
        } else {
            translationX = minScrollProgress * v.getMeasuredWidth();
            interpolatedProgress = mZInterpolator.getInterpolation(Math.abs(minScrollProgress));
        }
        float scale = (1 - interpolatedProgress) +
                interpolatedProgress * TRANSITION_SCALE_FACTOR;

        float alpha;
        if (isRtl && (scrollProgress > 0)) {
            alpha = mAlphaInterpolator.getInterpolation(1 - Math.abs(maxScrollProgress));
        } else if (!isRtl && (scrollProgress < 0)) {
            alpha = mAlphaInterpolator.getInterpolation(1 - Math.abs(scrollProgress));
        } else {
            //  On large screens we need to fade the page as it nears its leftmost position
            alpha = mLeftScreenAlphaInterpolator.getInterpolation(1 - scrollProgress);
        }

        v.setTranslationX(translationX);
        v.setScaleX(scale);
        v.setScaleY(scale);
        if (v instanceof CellLayout) {
            ((CellLayout) v).getShortcutsAndWidgets().setAlpha(alpha);
        } else {
            v.setAlpha(alpha);
        }

        // If the view has 0 alpha, we move it off screen so as to prevent
        // it from accepting touches
        if (alpha == 0) {
            v.setTranslationX(v.getMeasuredWidth() * -10f);
        } else if (v.getVisibility() != View.VISIBLE) {
            v.setVisibility(View.VISIBLE);
        }
    }
}
