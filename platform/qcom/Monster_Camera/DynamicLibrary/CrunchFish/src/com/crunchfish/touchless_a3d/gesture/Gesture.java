
package com.crunchfish.touchless_a3d.gesture;

import java.util.ArrayList;

/**
 * Copyright (c) 2014 Crunchfish AB. All rights reserved. All information herein
 * is or may be trade secrets of Crunchfish AB.
 * <p/>
 * A class that describes a user defined gesture. Each instance is created from
 * a JSON description of a gesture. The JSON description defines when
 * {@link Event event}s should be reported and these {@link Event event}s can be
 * monitored via registering {@link Listener listener}s to this class.
 */
public class Gesture {
    /**
     * An interface for receiving events from a {@link Gesture gesture}.
     */
    public interface Listener {

        /**
         * Called from the {@link Gesture gesture} when an event related to it
         * occurs.
         *
         * @see Gesture#registerListener(Listener)
         * @param event The {@link Event event} described by the notify-block in
         *            the JSON description.
         */
        void onEvent(Event event);
    }

    private long mNativeObj = 0;
    private ArrayList<Listener> mListeners;

    /**
     * Constructs a gesture instance from a JSON description.
     *
     * @throws IllegalArgumentException Thrown when JSON description is not
     *             valid.
     */
    public Gesture(String json) {
        mListeners = new ArrayList<Listener>();
        mNativeObj = cCreateGesture(json);
        if (mNativeObj == 0) {
            throw new IllegalArgumentException("Could not parse json argument");
        }
    }

    /**
     * Registers a {@link Listener listener} with the gesture.
     *
     * @see #unregisterListener(Listener)
     * @param listener The listener to be called when notification events occur
     *            in the gesture.
     */
    public void registerListener(Listener listener) {
        mListeners.add(listener);
    }

    /**
     * Unregisters a {@link Listener listener} from this gesture. If the
     * listener has been registered more than once, only one of the
     * registrations is removed.
     *
     * @see #registerListener(Listener)
     * @param listener The listener to unregister. It will no longer be called.
     */
    public void unregisterListener(Listener listener) {
        mListeners.remove(listener);
    }

    void dispatchEvent(final Event event) {
        for (Listener listener : mListeners) {
            listener.onEvent(event);
        }
    }

    private long getNativeObject() {
        return mNativeObj;
    }

    private native long cCreateGesture(String json);
}