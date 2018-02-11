/* Copyright (C) 2016 Tcl Corporation Limited */

package com.crunchfish.touchless_a3d;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.util.SparseArray;

import com.crunchfish.touchless_a3d.active_area.ActiveArea;
import com.crunchfish.touchless_a3d.exception.DeadInstanceException;
import com.crunchfish.touchless_a3d.exception.LicenseNotValidException;
import com.crunchfish.touchless_a3d.exception.LicenseServerUnavailableException;
import com.crunchfish.touchless_a3d.gesture.Event;

/**
 * Copyright (c) 2013 Crunchfish AB. All rights reserved. All information herein
 * is or may be trade secrets of Crunchfish AB.
 * <p>
 * TouchlessA3D is the main engine. Create an instance, register gesture
 * listeners, and then feed the engine with images by calling
 * {@link #handleImage(long, byte[])}.
 */
public class TouchlessA3D implements Closeable {


    /**
     * Can be used as argument when calling the function
     * {@link TouchlessA3D#handleImage(long, byte[])}.
     */
    public enum Rotate {
        /** No rotation. */
        DO_NOT_ROTATE,
        /**
         * Rotate the input image 90 degrees clockwise before detecting objects
         * in it.
         */
        ROTATE_90,
        /** Rotate the input image 180 degrees before detecting objects in it. */
        ROTATE_180,
        /**
         * Rotate the input image 270 degrees clockwise before detecting objects
         * in it.
         */
        ROTATE_270
    }

    /**
     * Parameters used for tuning the touchless experience.
     *
     * @see TouchlessA3D#setParameter(int, int)
     */
    public class Parameters {
        /**
         * Parameter for switching extended range of touchless interaction on
         * and off.
         */
        public static final int EXTENDED_RANGE = 1002;
    }

    private long mNativeObj = 0;
    // You need to synchronize on this lock when using the value of the
    // native object pointer.
    private final ReentrantReadWriteLock mNativeLock = new ReentrantReadWriteLock();
    @SuppressWarnings("deprecation")
    private SparseArray<List<com.crunchfish.touchless_a3d.deprecated_gestures.Gesture.Listener>> mListeners =
            new SparseArray<List<com.crunchfish.touchless_a3d.deprecated_gestures.Gesture.Listener>>();
    private Set<ActiveArea.Listener> mActiveAreaListeners = new HashSet<ActiveArea.Listener>();

    private static native long cAlloc(TouchlessA3D thiz, int imageWidth, int imageHeight,
            int imageStep, int offset);

    private static native void cFree(long ptr);

    private static native void cSetParameter(long ptr, int key, int value);

    private static native int cGetParameter(long ptr, int key);

    /**
     * @deprecated
     */
    private static native void cRegisterListener(long ptr, int type);

    /**
     * @deprecated
     */
    private static native void cUnregisterListener(long ptr, int type);

    private static native void cRegisterGesture(long ptr,
            com.crunchfish.touchless_a3d.gesture.Gesture gesture);

    private static native void cUnregisterGesture(long ptr,
            com.crunchfish.touchless_a3d.gesture.Gesture gesture);

    private static native void cRegisterActiveAreaListener(long ptr);

    private static native void cUnregisterActiveAreaListener(long ptr);

    private static native void cHandleImage(
            long ptr,
            long timestamp,
            byte[] imageData,
            int rotate,
            @SuppressWarnings("deprecation") ArrayList<com.crunchfish.touchless_a3d.deprecated_gestures.Gesture> outGestures,
            ArrayList<Event> outGestureEvents,
            ActiveArea activeArea);

    /**
     * Constructs a TouchlessA3D object.
     *
     * @param imageWidth Width of images sent as input to
     *            {@link #handleImage(long, byte[])}.
     * @param imageHeight Height of images sent as input to
     *            {@link #handleImage(long, byte[])}.
     */
    public TouchlessA3D(int imageWidth, int imageHeight) throws LicenseNotValidException,
            LicenseServerUnavailableException {
        this(imageWidth, imageHeight, imageWidth, 0);
    }

    /**
     * Constructs a TouchlessA3D object.
     *
     * @param imageWidth Width of images sent as input to
     *            {@link #handleImage(long, byte[])}.
     * @param imageHeight Height of images sent as input to
     *            {@link #handleImage(long, byte[])}.
     * @param imageStep Distance in bytes between the start of lines in the
     *            input data sent to {@link #handleImage(long, byte[])}. Must be
     *            a positive integer.
     */
    public TouchlessA3D(int imageWidth, int imageHeight, int imageStep)
            throws LicenseNotValidException, LicenseServerUnavailableException {
        this(imageWidth, imageHeight, imageStep, 0);
    }

    /**
     * Constructs a TouchlessA3D object.
     *
     * @param imageWidth Width of images sent as input to
     *            {@link #handleImage(long, byte[])}.
     * @param imageHeight Height of images sent as input to
     *            {@link #handleImage(long, byte[])}.
     * @param imageStep Distance in bytes between the start of lines in the
     *            input data sent to {@link #handleImage(long, byte[])}. Must be
     *            a positive integer.
     * @param offset Specifies from which index in the byte array sent as input
     *            to {@link #handleImage(long, byte[])} we should start reading
     *            the image from.
     */
    public TouchlessA3D(int imageWidth, int imageHeight, int imageStep, int offset)
            throws LicenseNotValidException, LicenseServerUnavailableException {
        mNativeObj = cAlloc(this, imageWidth, imageHeight, imageStep, offset);
    }

    /**
     * Frees the native object used and marks the pointer to the native object
     * as invalid. After this method has been called, this instance is basically
     * dead and should not be used anymore. <br/>
     * This method must not be called from any listener registered with this
     * instance.
     */
    private void freeNativeObject() {
        mNativeLock.writeLock().lock();
        try {
            if (0 != mNativeObj) {
                cFree(mNativeObj);
                mNativeObj = 0;
            }
        } finally {
            mNativeLock.writeLock().unlock();
        }
    }

    /**
     * Closes this instance. <br/>
     * This method should be called when you are done using the library, e.g.
     * from {@link android.app.Activity#onStop() Activity.onStop()} or
     * {@link android.app.Service#onDestroy() Service.onDestroy()} method in
     * Android. The method also enables you to use the TouchlessA3D instance in
     * a Java7 try-with block. <br/>
     * After this method is called, this instance is dead and should not be used
     * anymore. <br/>
     * This method must not be called from any listener registered with this
     * instance.
     */
    @Override
    public void close() {
        freeNativeObject();
    }

    @Override
    public void finalize() {
        freeNativeObject();
    }

    /**
     * Sets a global parameter for this TouchlessA3D instance.
     *
     * @see #getParameter(int)
     * @param key Identifies which parameter to set.
     * @param value Value to set for the specified parameter.
     * @throws DeadInstanceException If {@link #close()} has been called.
     */
    public void setParameter(int key, int value) {
        mNativeLock.readLock().lock();
        try {
            if (0 == mNativeObj) {
                throw new DeadInstanceException();
            }
            cSetParameter(mNativeObj, key, value);
        } finally {
            mNativeLock.readLock().unlock();
        }
    }

    /**
     * Gets the value for a global parameter for this TouchlessA3D instance.
     *
     * @see #setParameter(int, int)
     * @param key Identifies which parameter to get the value for.
     * @return The value for the specified parameter.
     * @throws DeadInstanceException If {@link #close()} has been called.
     */
    public int getParameter(int key) {
        mNativeLock.readLock().lock();
        try {
            if (0 == mNativeObj) {
                throw new DeadInstanceException();
            }
            return cGetParameter(mNativeObj, key);
        } finally {
            mNativeLock.readLock().unlock();
        }
    }

    /**
     * Registers a listener for a specific type of gesture.
     *
     * @deprecated Use
     *             {@link #registerGesture(com.crunchfish.touchless_a3d.gesture.Gesture)}
     *             instead.
     * @see #unregisterGestureListener(int,
     *      com.crunchfish.touchless_a3d.deprecated_gestures.Gesture.Listener)
     * @param type The type of gesture to listen for.
     * @param listener Is called whenever there is a new gesture of the
     *            specified type.
     * @throws DeadInstanceException If {@link #close()} has been called.
     */
    public void registerGestureListener(int type,
            com.crunchfish.touchless_a3d.deprecated_gestures.Gesture.Listener listener) {

        List<com.crunchfish.touchless_a3d.deprecated_gestures.Gesture.Listener> listeners = mListeners
                .get(type);
        if (listeners == null) {
            listeners = new ArrayList<com.crunchfish.touchless_a3d.deprecated_gestures.Gesture.Listener>();
            mListeners.put(type, listeners);
            mNativeLock.readLock().lock();
            try {
                if (0 == mNativeObj) {
                    throw new DeadInstanceException();
                }
                cRegisterListener(mNativeObj, type);
            } finally {
                mNativeLock.readLock().unlock();
            }
        }
        listeners.add(listener);
    }

    /**
     * Unregisters a listener for a specific type of gesture. If the listener
     * has been registered more than once for the specified gesture type, only
     * one of the registrations is removed.
     *
     * @deprecated Use
     *             {@link #unregisterGesture(com.crunchfish.touchless_a3d.gesture.Gesture)}
     *             instead.
     * @see #registerGestureListener(int,
     *      com.crunchfish.touchless_a3d.deprecated_gestures.Gesture.Listener)
     * @param type The type of gesture to stop listening for.
     * @param listener Will no longer be called when there is a new gesture of
     *            the specified type.
     * @throws DeadInstanceException If {@link #close()} has been called.
     */
    public void unregisterGestureListener(int type,
            com.crunchfish.touchless_a3d.deprecated_gestures.Gesture.Listener listener) {

        List<com.crunchfish.touchless_a3d.deprecated_gestures.Gesture.Listener> listeners = mListeners
                .get(type);
        if (listeners != null) {
            listeners.remove(listener);

            if (listeners.isEmpty()) {
                mListeners.remove(type);
                mNativeLock.readLock().lock();
                try {
                    if (0 == mNativeObj) {
                        throw new DeadInstanceException();
                    }
                    cUnregisterListener(mNativeObj, type);
                } finally {
                    mNativeLock.readLock().unlock();
                }
            }
        }
    }

    /**
     * Registers a {@link com.crunchfish.touchless_a3d.gesture.Gesture gesture}
     * with the TouchlessA3D engine. To get events from the gesture listeners
     * should be added to the
     * {@link com.crunchfish.touchless_a3d.gesture.Gesture gesture}.
     *
     * @see #unregisterGesture(com.crunchfish.touchless_a3d.gesture.Gesture)
     * @param gesture gesture to register
     */
    public void registerGesture(final com.crunchfish.touchless_a3d.gesture.Gesture gesture) {
        mNativeLock.readLock().lock();
        try {
            if (0 == mNativeObj) {
                throw new DeadInstanceException();
            }
            cRegisterGesture(mNativeObj, gesture);
        } finally {
            mNativeLock.readLock().unlock();
        }
    }

    /**
     * Unregisters a {@link com.crunchfish.touchless_a3d.gesture.Gesture
     * gesture} from the touchlessA3D engine.
     *
     * @see #registerGesture(com.crunchfish.touchless_a3d.gesture.Gesture)
     * @param gesture gesture to register
     */
    public void unregisterGesture(final com.crunchfish.touchless_a3d.gesture.Gesture gesture) {
        mNativeLock.readLock().lock();
        try {
            if (0 == mNativeObj) {
                throw new DeadInstanceException();
            }
            cUnregisterGesture(mNativeObj, gesture);
        } finally {
            mNativeLock.readLock().unlock();
        }
    }

    /**
     * Registers an {@link ActiveArea active area} {@link ActiveArea.Listener
     * listener}.
     *
     * @param listener Will be called whenever a new active area is detected.
     * @throws DeadInstanceException If {@link #close()} has been called.
     */
    public void registerActiveAreaListener(
            final ActiveArea.Listener listener) {
        synchronized (mActiveAreaListeners) {
            final boolean wasEmpty = mActiveAreaListeners.isEmpty();
            mActiveAreaListeners.add(listener);
            if (wasEmpty) {
                mNativeLock.readLock().lock();
                try {
                    if (0 == mNativeObj) {
                        throw new DeadInstanceException();
                    }
                    cRegisterActiveAreaListener(mNativeObj);
                } finally {
                    mNativeLock.readLock().unlock();
                }
            }
        }
    }

    /**
     * Unregisters an {@link ActiveArea active area} {@link ActiveArea.Listener
     * listener}.
     *
     * @param listener The listener to remove a registration for.
     * @throws DeadInstanceException If {@link #close()} has been called.
     */
    public void unregisterActiveAreaListener(
            final ActiveArea.Listener listener) {
        synchronized (mActiveAreaListeners) {
            mActiveAreaListeners.remove(listener);
            if (mActiveAreaListeners.isEmpty()) {
                mNativeLock.readLock().lock();
                try {
                    if (0 == mNativeObj) {
                        throw new DeadInstanceException();
                    }
                    cUnregisterActiveAreaListener(mNativeObj);
                } finally {
                    mNativeLock.readLock().unlock();
                }
            }
        }
    }

    /**
     * Lets this TouchlessA3D instance do calculations on a new image. Might
     * cause gestures to be emitted.
     *
     * @see #handleImage(long, byte[])
     * @param timestamp Timestamp for the input image, in milliseconds. Must be
     *            a higher value than in any previous call to this method.
     * @param imageData Pixel data for the input image. The array must have a
     *            size of at least imageWidth * imageHeight elements, where
     *            imageWidth and imageHeight are the parameters given to the
     *            TouchlessA3D constructor. The data must be given as row-by-row
     *            grayscale values. That is, the first imageWidth number of
     *            elements must contain grayscale values for the first row in
     *            the image. The second imageWidth number of elements must
     *            contain grayscale values for the second row in the image, etc.
     *            The byte array given by the Android device as parameter to the
     *            {@link android.hardware.Camera.PreviewCallback} interface
     *            method
     *            {@link android.hardware.Camera.PreviewCallback#onPreviewFrame(byte[], android.hardware.Camera)}
     *            fullfills this requirement.
     * @param rotate Specifies how this TouchlessA3D instance should rotate the
     *            input image internally before performing calculations on it.
     * @throws DeadInstanceException If {@link #close()} has been called.
     */
    @SuppressWarnings("deprecation")
    public void handleImage(long timestamp, byte[] imageData, Rotate rotate) {
        final ActiveArea activeArea = new ActiveArea();
        final ArrayList<com.crunchfish.touchless_a3d.deprecated_gestures.Gesture> outGestures =
                new ArrayList<com.crunchfish.touchless_a3d.deprecated_gestures.Gesture>();
        final ArrayList<Event> outGestureEvents = new ArrayList<Event>();

        mNativeLock.readLock().lock();
        try {
            if (0 == mNativeObj) {
                throw new DeadInstanceException();
            }
            cHandleImage(mNativeObj, timestamp, imageData, rotate.ordinal(), outGestures,
                    outGestureEvents, activeArea);
        } finally {
            mNativeLock.readLock().unlock();
        }

        List<ActiveArea.Listener> activeAreaListeners;
        synchronized (mActiveAreaListeners) {
            activeAreaListeners = new ArrayList<ActiveArea.Listener>(mActiveAreaListeners);
        }
        for (final ActiveArea.Listener listener : activeAreaListeners) {
            listener.onActiveArea(activeArea);
        }

        for (final com.crunchfish.touchless_a3d.deprecated_gestures.Gesture gesture : outGestures) {
            ArrayList<com.crunchfish.touchless_a3d.deprecated_gestures.Gesture.Listener> listenersCopy =
                    new ArrayList<com.crunchfish.touchless_a3d.deprecated_gestures.Gesture.Listener>(
                            mListeners.get(gesture.getType()));

            for (com.crunchfish.touchless_a3d.deprecated_gestures.Gesture.Listener listener : listenersCopy) {
                listener.onGesture(gesture);
            }
        }

        for (final Event event : outGestureEvents) {
            event.dispatch();
        }
    }

    /**
     * Lets this TouchlessA3D instance do calculations on a new image. Might
     * cause gestures to be emitted.
     *
     * @see #handleImage(long, byte[])
     * @param timestamp Timestamp for the input image, in milliseconds. Must be
     *            a higher value than in any previous call to this method.
     * @param imageData Pixel data for the input image. The array must have a
     *            size of at least imageWidth * imageHeight elements, where
     *            imageWidth and imageHeight are the parameters given to the
     *            TouchlessA3D constructor. The data must be given as row-by-row
     *            grayscale values. That is, the first imageWidth number of
     *            elements must contain grayscale values for the first row in
     *            the image. The second imageWidth number of elements must
     *            contain grayscale values for the second row in the image, etc.
     *            The byte array given by the Android device as parameter to the
     *            {@link android.hardware.Camera.PreviewCallback} interface
     *            method
     *            {@link android.hardware.Camera.PreviewCallback#onPreviewFrame(byte[], android.hardware.Camera)}
     *            fullfills this requirement.
     * @throws DeadInstanceException If {@link #close()} has been called.
     */
    public void handleImage(long timestamp, byte[] imageData) {
        handleImage(timestamp, imageData, Rotate.DO_NOT_ROTATE);
    }
}
