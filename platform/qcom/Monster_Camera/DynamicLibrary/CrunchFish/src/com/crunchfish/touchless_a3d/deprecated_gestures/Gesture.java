
package com.crunchfish.touchless_a3d.deprecated_gestures;

/**
 * Copyright (c) 2013 Crunchfish AB. All rights reserved. All information herein
 * is or may be trade secrets of Crunchfish AB.
 *
 * @deprecated Use {@link com.crunchfish.touchless_a3d.gesture.Gesture} instead.
 */
public interface Gesture {
    /**
     * Interface for receiving TouchlessA3D gestures.
     *
     * @deprecated Use
     *             {@link com.crunchfish.touchless_a3d.gesture.Gesture.Listener}
     *             instead.
     */
    public interface Listener {
        /**
         * Called from a TouchlessA3D instance whenever it has a new gesture of
         * the type for which this listener instance has been registered.
         *
         * @see TouchlessA3D#registerGestureListener(int, Listener)
         * @param gesture The new gesture emitted by the TouchlessA3d instance
         *            on which this listener instance has been registered.
         */
        void onGesture(Gesture gesture);
    }

    /**
     * @return The type of gesture that this object is an instance of.
     */
    int getType();

    /**
     * @return The timestamp in milliseconds for this gesture instance.
     */
    long getTimestamp();
}
