package com.monster.launcher.effect.interpolators;

import android.animation.TimeInterpolator;

/**
 * Created by antino on 16-7-12.
 */
public class ZInterpolator implements TimeInterpolator {
    private float focalLength;
    public ZInterpolator(float foc) {
        focalLength = foc;
    }
    public float getInterpolation(float input) {
        return (1.0f - focalLength / (focalLength + input)) /
                (1.0f - focalLength / (focalLength + 1.0f));
    }
}