package com.android.camera.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * Created by Sean Scott on 9/2/16.
 */
public class FractionFrameLayout extends FrameLayout {

    private float xFraction = 0;

    public FractionFrameLayout(Context context) {
        super(context);
    }

    public FractionFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FractionFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private ViewTreeObserver.OnPreDrawListener preDrawListener = null;

    public void setXFraction(float fraction) {

        this.xFraction = fraction;

        if (getWidth() == 0) {
            if (preDrawListener == null) {
                preDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
                        setXFraction(xFraction);
                        return true;
                    }
                };
                getViewTreeObserver().addOnPreDrawListener(preDrawListener);
            }
            return;
        }

        float translationX = getWidth() * fraction;
        setTranslationX(translationX);
    }

    public float getXFraction() {
        return this.xFraction;
    }
}