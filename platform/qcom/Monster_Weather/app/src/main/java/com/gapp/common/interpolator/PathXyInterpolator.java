package com.gapp.common.interpolator;

import android.graphics.Path;
import android.graphics.PathMeasure;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-17.
 * $desc
 */
public class PathXyInterpolator implements InterpolatorXY {
    private Path mPath;
    private PathMeasure mPathMeasure = new PathMeasure();
    private float[] mPosition = new float[2];


    public void setPath(Path path) {
        mPath = path;
    }

    @Override
    public float[] getInterpolation(float input) {
        mPathMeasure.setPath(mPath, false);
        mPathMeasure.getPosTan(input * mPathMeasure.getLength(), mPosition, null);
        return mPosition;
    }
}
