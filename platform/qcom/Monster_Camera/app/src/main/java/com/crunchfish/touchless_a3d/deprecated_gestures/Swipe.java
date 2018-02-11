/* Copyright (C) 2016 Tcl Corporation Limited */

package com.crunchfish.touchless_a3d.deprecated_gestures;

import android.annotation.SuppressLint;

/**
 * Copyright (c) 2013 Crunchfish AB. All rights reserved. All information herein
 * is or may be trade secrets of Crunchfish AB.
 * <p/>
 * The Swipe gesture is emitted whenever the user swipes quickly across the
 * camera, either to the left or to the right.
 *
 * @deprecated Use {@link com.crunchfish.touchless_a3d.gesture.Swipe} instead.
 */
public class Swipe implements Gesture {

    /*
     * Unique identifier for this type of gesture.
     */
    public final static int TYPE = 6;

    /**
     * @deprecated
     */
    @SuppressLint("RtlHardcoded")
    public enum Direction {
        LEFT,
        RIGHT
    }

    private long mTimestamp;
    private Direction mDirection;

    public Swipe(long timestamp, int direction) {
        mTimestamp = timestamp;
        mDirection = Direction.values()[direction];
    }

    @Override
    public int getType() {
        return TYPE;
    }

    @Override
    public long getTimestamp() {
        return mTimestamp;
    }

    public Direction getDirection() {
        return mDirection;
    }
}
