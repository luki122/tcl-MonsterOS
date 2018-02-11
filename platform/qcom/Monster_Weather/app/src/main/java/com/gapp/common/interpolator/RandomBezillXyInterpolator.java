package com.gapp.common.interpolator;

import java.util.Random;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-16.
 * $desc
 */
public class RandomBezillXyInterpolator extends BezillCurveXyInterpolator {

    private final static int TS_RANDOM = 50;
    private final static int VS_RANDOM = 30;
    private final static float MIN_STEP = 5f;
    private final static int OFFSET_STEP = 10;

    private Random mRandom;

    public RandomBezillXyInterpolator() {
        this(new Random(System.currentTimeMillis()));
    }

    public RandomBezillXyInterpolator(Random random) {
        mRandom = random;
    }


    private static float caculateValue(Random random, float s, int count) {
        return s * count - ((float) random.nextInt(OFFSET_STEP) - (OFFSET_STEP >> 1)) / 100f;
    }


    public void setBezillCurveRandom() {
        Random random = mRandom;
        float ts = ((float) random.nextInt(TS_RANDOM) + MIN_STEP) / 100f;
        float vs = ((float) random.nextInt(VS_RANDOM) + MIN_STEP) / 100f;
        int i = 1;
        float t1, v1, t2, v2;
        t2 = 0f;
        boolean isPositive = random.nextBoolean();
        for (int k = 0; t2 < 1.0f; ) {
            t1 = caculateValue(random, ts, i++);
            v1 = caculateValue(random, vs, 1);
            t2 = caculateValue(random, ts, i++);
            v2 = caculateValue(random, vs, 0);
            if (!isPositive) {
                v1 = -v1;
            }
            addCurvePoints(t1, v1, t2, v2);

            isPositive = !isPositive;
        }
    }
}
