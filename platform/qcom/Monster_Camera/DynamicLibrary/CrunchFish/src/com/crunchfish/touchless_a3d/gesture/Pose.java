
package com.crunchfish.touchless_a3d.gesture;

import android.graphics.Rect;

/**
 * Copyright (c) 2014 Crunchfish AB. All rights reserved. All information herein
 * is or may be trade secrets of Crunchfish AB.
 * <p/>
 * This class represents a pose. A pose has an area within the input image and a
 * type: open hand, closed hand etc.
 */
public class Pose extends Identifiable {
    private final int mObjectType;
    private final Rect mRect;

    Pose(String id,
            final int objectType,
            final int topLeftX,
            final int topLeftY,
            final int bottomRightX,
            final int bottomRightY) {
        super(Identifiable.Type.POSE, id);

        mObjectType = objectType;

        mRect = new Rect(topLeftX, topLeftY, bottomRightX, bottomRightY);
    }

    /**
     * Get the type of the pose.
     *
     * @see ObjectPresence for different kind of pose types.
     * @return Type of pose.
     */
    public int getPoseType() {
        return mObjectType;
    }

    /**
     * Get the position and area of the pose.
     *
     * @return The bounding area of the pose.
     */
    public Rect getBoundingArea() {
        return mRect;
    }
}
