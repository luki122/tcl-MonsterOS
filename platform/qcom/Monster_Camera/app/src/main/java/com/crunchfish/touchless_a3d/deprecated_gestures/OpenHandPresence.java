/* Copyright (C) 2016 Tcl Corporation Limited */

package com.crunchfish.touchless_a3d.deprecated_gestures;

/**
 * Copyright (c) 2013 Crunchfish AB. All rights reserved. All information herein
 * is or may be trade secrets of Crunchfish AB.
 * <p/>
 * The Open Hand Presence gesture starts whenever an open hand is seen in the
 * input images to
 * {@link com.crunchfish.touchless_a3d.TouchlessA3D#handleImage(long, byte[])}.
 * A OpenHandPresence object with the {@link ObjectPresence.Action} START is
 * then sent to any {@link Gesture.Listener} object registered on the
 * {@link com.crunchfish.touchless_a3d.TouchlessA3D} object with
 * OPEN_HAND_PRESENCE as argument to
 * {@link com.crunchfish.touchless_a3d.TouchlessA3D#registerGestureListener(int, Gesture.Listener)}.
 * <p/>
 * A OpenHandPresence object with the Action MOVEMENT is then sent for every
 * input image in which the open hand is still seen. When the open hand is no
 * longer found in the input images, a OpenHandPresence object with the Action
 * END is sent. After that, no more OpenHandPresence objects are sent for that
 * object id, but objects with a new id will be sent if an open hand is again
 * seen in the input images.
 *
 * @deprecated Use {@link com.crunchfish.touchless_a3d.gesture.Pose} instead.
 */
public class OpenHandPresence extends ObjectPresence {

    /*
     * Unique identifier for this type of gesture.
     */
    public final static int TYPE = 0;

    /* package */OpenHandPresence() {
    }

    /*
     * @see com.crunchfish.touchless_a3d.TA3DGesture#getType()
     */
    @Override
    public int getType() {
        return TYPE;
    }
}
