package com.gapp.common.interpolator;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-17.
 * $desc
 */
public class LinearInterpolatorXy implements InterpolatorXY {

    private float[] mPointXy = new float[2];

    @Override
    public float[] getInterpolation(float input) {
        mPointXy[0] = mPointXy[1] = input;
        return mPointXy;
    }
}
