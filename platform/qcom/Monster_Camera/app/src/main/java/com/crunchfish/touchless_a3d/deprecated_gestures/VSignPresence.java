/* Copyright (C) 2016 Tcl Corporation Limited */

package com.crunchfish.touchless_a3d.deprecated_gestures;

/**
 * Copyright (c) 2014 Crunchfish AB. All rights reserved. All information herein
 * is or may be trade secrets of Crunchfish AB.
 * <p/>
 * The V-Sign Presence gesture starts whenever a V-Sign gesture is seen in the
 * input images to
 * {@link com.crunchfish.touchless_a3d.TouchlessA3D#handleImage(long, byte[])}.
 * A VSignPresence object with the {@link ObjectPresence.Action} START is then
 * sent to any {@link Gesture.Listener} object registered on the
 * {@link com.crunchfish.touchless_a3d.TouchlessA3D} object with
 * {@link VSignPresence.TYPE} as argument to
 * {@link com.crunchfish.touchless_a3d.TouchlessA3D#registerGestureListener(int, Gesture.Listener)}.
 * <p/>
 * A VSignPresence object with the Action MOVEMENT is then sent for every input
 * image in which the V-Sign gesture is still seen. When the V-Sign gesture is
 * no longer found in the input images, a VSignPresence object with the Action
 * END is sent. After that, no more VSignPresence objects are sent for that
 * object id, but objects with a new id will be sent if a V-Sign gesture is
 * again seen in the input images.
 *
 * @deprecated Use {@link com.crunchfish.touchless_a3d.gesture.Pose} instead.
 */
public class VSignPresence extends ObjectPresence {

    /*
     * Unique identifier for this type of gesture.
     */
    public final static int TYPE = 4;

    /* package */VSignPresence() {
    }

    /*
     * @see com.crunchfish.touchless_a3d.TA3DGesture#getType()
     */
    @Override
    public int getType() {
        return TYPE;
    }
}
