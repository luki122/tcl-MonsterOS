package com.gapp.common.interpolator;

import android.graphics.Path;
import android.graphics.PathMeasure;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-17.
 * $desc
 */
public class CubicXyInterpolator implements InterpolatorXY {

    private Path mPath = new Path();
    private PathMeasure mPathMeasure = new PathMeasure();
    private float[] mPosition = new float[2];

    public void addCubicPoints(float sx, float sy, float cx, float cy, float ex, float ey) {
        mPath.cubicTo(sx, sy, cx, cy, ex, ey);
    }

    @Override
    public float[] getInterpolation(float input) {
        mPathMeasure.setPath(mPath, false);
        mPathMeasure.getPosTan(input * mPathMeasure.getLength(), mPosition, null);
        return mPosition;
    }
}
