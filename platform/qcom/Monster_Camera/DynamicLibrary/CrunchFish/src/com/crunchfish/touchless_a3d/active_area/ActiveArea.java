
package com.crunchfish.touchless_a3d.active_area;

import android.graphics.Rect;
import android.graphics.Region;

/**
 * Copyright (c) 2014 Crunchfish AB. All rights reserved. All information herein
 * is or may be trade secrets of Crunchfish AB.
 * <p/>
 * Represents an active area (area where things seem to be happening) within an
 * image.
 */
public final class ActiveArea {
    /**
     * Interface for receiving TouchlessA3D {@link ActiveArea active area}s.
     */
    public interface Listener {
        /**
         * Called from a TouchlessA3D instance when activity detection have been
         * carried out.
         *
         * @see TouchlessA3D#registerActiveAreaListener(Listener)
         * @param activeArea The new active area emitted by the TouchlessA3D
         *            instance on which this listener instance has been
         *            registered.
         */
        void onActiveArea(final ActiveArea activeArea);
    }

    private static final int COORDINATES_PER_CORNER = 2;
    private static final int CORNERS_PER_RECTANGLE = 2;
    private static final int COORDINATES_PER_RECTANGLE =
            COORDINATES_PER_CORNER * CORNERS_PER_RECTANGLE;

    private final Region mRegion = new Region();

    /**
     * Creates an empty active area.
     */
    public ActiveArea() {
    }

    /**
     * Adds areas with activity to the internal region.
     *
     * @param areaCorners A flat array of corner coordinates for rectangles
     *            within which activity has been detected. The coordinates are
     *            ordered as left, top, right, bottom for each rectangle.
     * @throws IllegalArgumentException if the parameter does not contain
     *             coordinates for an integral number of rectangles.
     */
    @SuppressWarnings("unused")
    private void addSubAreas(final int[] areaCorners)
            throws IllegalArgumentException {
        if (0 != areaCorners.length % COORDINATES_PER_RECTANGLE) {
            throw new IllegalArgumentException("Missing coordinates from array");
        }

        // Add each found rectangle where activity has been detected to the
        // region.
        for (int i = 0; i < areaCorners.length; i += COORDINATES_PER_RECTANGLE) {
            addSubArea(
                    areaCorners[i],
                    areaCorners[i + 1],
                    areaCorners[i + 2],
                    areaCorners[i + 3]);
        }
    }

    /**
     * Adds an area with activity to the internal region.
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    private void addSubArea(final int left, final int top, final int right,
            final int bottom) {
        /*
         * There is a difference between the rectangle in the TouchlessA3D
         * native library and the one in Android: while the TouchlessA3D
         * rectangle is a closed two-dimensional interval, the Android rectangle
         * is a right-open two-dimensional interval, i.e. the TouchlessA3D
         * rectangle includes the bottom right corner while the Android
         * rectangle does not.
         */
        final Rect rectangle = new Rect(left, top, right + 1, bottom + 1);
        mRegion.union(rectangle);
    }

    /**
     * Returns the region in the analyzed image where activity was detected.
     * <p/>
     * These statements will always hold:
     * <ul>
     * <li>No activity has been detected outside of the region.</li>
     * <li>Unless the region is empty, activity has been detected inside the
     * region.</li>
     * </ul>
     *
     * @return The region where activity was detected.
     */
    public Region getRegion() {
        return mRegion;
    }
}
