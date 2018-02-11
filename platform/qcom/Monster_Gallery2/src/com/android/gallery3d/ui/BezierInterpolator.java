package com.android.gallery3d.ui;

import android.view.animation.Interpolator;

public class BezierInterpolator implements Interpolator {
    
    private float mX1, mY1;
    private float mX2, mY2;
    
    public static BezierInterpolator mDefaultBezierInterpolator = new BezierInterpolator(0.33f, 0, 0.33f, 1);
    
    public BezierInterpolator(float x1, float y1, float x2, float y2) {
        mX1 = x1;
        mY1 = y1;
        mX2 = x2;
        mY2 = y2;
    }
    
    @Override
    public float getInterpolation(float input) {
        float timeProgress = calculateByBezierEquation(input, mX1, mX2);
        float ret = calculateByBezierEquation(timeProgress, mY1, mY2);
        return ret;
    }
    
    public float calculateByBezierEquation(float t, float value1, float value2) {
        float u = 1 - t;
        float sqrtU = u * u;
        float sqrtT = t * t;
        float cubicT = sqrtT * t;
        return 3 * sqrtU * t * value1 + 3 * u * sqrtT * value2 + cubicT;
    }
}
