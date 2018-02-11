
package com.crunchfish.touchless_a3d.deprecated_gestures;

import com.crunchfish.touchless_a3d.TouchlessA3D;

import android.util.Log;

/**
 * Copyright (c) 2013 Crunchfish AB. All rights reserved. All information herein
 * is or may be trade secrets of Crunchfish AB.
 * <p/>
 * Base class for Presence gestures of different types.
 *
 * @deprecated Use {@link com.crunchfish.touchless_a3d.gesture.Pose} instead.
 */
public abstract class ObjectPresence implements Gesture {
    /* package */static final String LOG_TAG = "crunchfish";

    /**
     * Every ObjectPresence life cycle starts with a single event of the Action
     * type START. Then follows an arbitrary number of MOVEMENT gestures, and
     * finally a single END gesture.
     *
     * @deprecated
     */
    public enum Action {
        START,
        MOVEMENT,
        END
    }

    private long mTimestamp;
    private int mObjectId;
    private Action mAction;
    private int mX, mY, mZ, mPrevX, mPrevY, mPrevZ, mWidth, mHeight;

    protected ObjectPresence() {
    }

    /**
     * Factory method used for creating instances of object presences.
     * Internally used.
     */
    public static <T extends ObjectPresence> T create(final Class<T> clazz, long timestamp,
            int objectId,
            int action, int x, int y, int z,
            int prevX, int prevY, int prevZ, int width, int height) {
        try {
            final T t = clazz.newInstance();
            final ObjectPresence presence = t;
            presence.mTimestamp = timestamp;
            presence.mObjectId = objectId;
            presence.mAction = Action.values()[action];
            presence.mX = x;
            presence.mY = y;
            presence.mZ = z;
            presence.mPrevX = prevX;
            presence.mPrevY = prevY;
            presence.mPrevZ = prevZ;
            presence.mWidth = width;
            presence.mHeight = height;
            return t;
        } catch (final InstantiationException e) {
            Log.e(LOG_TAG,
                    String.format("Failed creating instance of class %s", clazz.getName()), e);
        } catch (final IllegalAccessException e) {
            Log.e(LOG_TAG,
                    String.format("Failed accessing constructor of class %s", clazz.getName()), e);
        }
        return null;
    }

    /*
     * @see Gesture#getTimestamp()
     */
    @Override
    public long getTimestamp() {
        return mTimestamp;
    }

    /**
     * @return An identifier for a specific object in the input images. For
     *         example, if there are two hands currently visible in the input
     *         images, they will each have a unique ObjectId value that stays
     *         the same for all gestures concerning that specific object.
     */
    public int getObjectId() {
        return mObjectId;
    }

    /**
     * @return Specifies which part of the gesture life cycle this gesture
     *         instance represents.
     */
    public Action getAction() {
        return mAction;
    }

    /**
     * @return The x coordinate of the center of the object specified by
     *         {@link #getObjectId()}. The position is given as a coordinate in
     *         the input image. The position can in some cases exceed the image
     *         bounds.
     */
    public int getCenterX() {
        return mX;
    }

    /**
     * @return The y coordinate of the center of the object specified by
     *         {@link #getObjectId()}. The position is given as a coordinate in
     *         the input image. The position can in some cases exceed the image
     *         bounds.
     */
    public int getCenterY() {
        return mY;
    }

    /**
     * @return The relative z coordinate of the object specified by
     *         {@link #getObjectId()}. When {@link #getAction()} returns START,
     *         this method returns zero. All z values later returned by this
     *         method for the object specified by {@link #getObjectId()} are
     *         relative to that initial position.
     */
    public int getZ() {
        return mZ;
    }

    /**
     * @return The center x coordinate in the previous gesture of this type for
     *         the object specified by {@link #getObjectId()}.
     */
    public int getPrevCenterX() {
        return mPrevX;
    }

    /**
     * @return The center y coordinate in the previous gesture of this type for
     *         the object specified by {@link #getObjectId()}.
     */
    public int getPrevCenterY() {
        return mPrevY;
    }

    /**
     * @return The relative z coordinate in the previous gesture of this type
     *         for the object specified by {@link #getObjectId()}.
     */
    public int getPrevZ() {
        return mPrevZ;
    }

    /**
     * @return The width of the object specified by {@link #getObjectId()}, at
     *         the time specified by {@link #getTimestamp()}. The range of the
     *         returned value is from one to the width of the input image after
     *         any rotation specified in the call to
     *         {@link TouchlessA3D#handleImage(long, byte[])}.
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * @return The height of the object specified by {@link #getObjectId()}, at
     *         the time specified by {@link #getTimestamp()}. The range of the
     *         returned value is from one to the height of the input image after
     *         any rotation specified in the call to
     *         {@link TouchlessA3D#handleImage(long, byte[])}.
     */
    public int getHeight() {
        return mHeight;
    }
}
