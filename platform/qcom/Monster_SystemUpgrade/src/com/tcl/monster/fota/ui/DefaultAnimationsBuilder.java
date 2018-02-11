
package com.tcl.monster.fota.ui;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

/** Builds the default animations for showing and hiding a {@link Crouton}. */
final class DefaultAnimationsBuilder {
    private static final long DURATION = 400;
    private static Animation slideInDownAnimation, slideOutUpAnimation;
    private static int lastInAnimationHeight, lastOutAnimationHeight;

    private DefaultAnimationsBuilder() {
        /* no-op */
    }

    /**
     * @param croutonView The croutonView which gets animated.
     * @return The default Animation for a showing {@link Crouton}.
     */
    static Animation buildDefaultSlideInDownAnimation(View croutonView) {
        if (!areLastMeasuredInAnimationHeightAndCurrentEqual(croutonView)
                || (null == slideInDownAnimation)) {
            slideInDownAnimation = new TranslateAnimation(
                    0, 0, // X: from, to
                    -croutonView.getMeasuredHeight(), 0 // Y: from, to
            );
            slideInDownAnimation.setDuration(DURATION);
            setLastInAnimationHeight(croutonView.getMeasuredHeight());
        }
        return slideInDownAnimation;
    }

    /**
     * @param croutonView The croutonView which gets animated.
     * @return The default Animation for a hiding {@link Crouton}.
     */
    static Animation buildDefaultSlideOutUpAnimation(View croutonView) {
        if (!areLastMeasuredOutAnimationHeightAndCurrentEqual(croutonView)
                || (null == slideOutUpAnimation)) {
            slideOutUpAnimation = new TranslateAnimation(
                    0, 0, // X: from, to
                    0, -croutonView.getMeasuredHeight() // Y: from, to
            );
            slideOutUpAnimation.setDuration(DURATION);
            setLastOutAnimationHeight(croutonView.getMeasuredHeight());
        }
        return slideOutUpAnimation;
    }

    private static boolean areLastMeasuredInAnimationHeightAndCurrentEqual(View croutonView) {
        return areLastMeasuredAnimationHeightAndCurrentEqual(lastInAnimationHeight, croutonView);
    }

    private static boolean areLastMeasuredOutAnimationHeightAndCurrentEqual(View croutonView) {
        return areLastMeasuredAnimationHeightAndCurrentEqual(lastOutAnimationHeight, croutonView);
    }

    private static boolean areLastMeasuredAnimationHeightAndCurrentEqual(int lastHeight,
            View croutonView) {
        return lastHeight == croutonView.getMeasuredHeight();
    }

    private static void setLastInAnimationHeight(int lastInAnimationHeight) {
        DefaultAnimationsBuilder.lastInAnimationHeight = lastInAnimationHeight;
    }

    private static void setLastOutAnimationHeight(int lastOutAnimationHeight) {
        DefaultAnimationsBuilder.lastOutAnimationHeight = lastOutAnimationHeight;
    }
}
