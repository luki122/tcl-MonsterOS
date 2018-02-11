/* Copyright (C) 2016 Tcl Corporation Limited */

package com.crunchfish.touchless_a3d.gesture;

import java.util.Collection;
import java.util.HashMap;

/**
 * Copyright (c) 2014 Crunchfish AB. All rights reserved. All information herein
 * is or may be trade secrets of Crunchfish AB.
 * <p/>
 * Instances of this class are sent from a {@link Gesture} to its
 * {@link Gesture.Listener}s. It contains a user defined text string, which is
 * set from the JSON description of the {@link Gesture}, and a collection of
 * {@link Identifiable}s. The collection of {@link Identifiable}s is populated
 * with all objects ({@link Pose} and {@link Swipe}) that had an id set when the
 * notification was sent. An Event is coupled to a notify-block in the JSON
 * description.
 */
public final class Event {
    private final Gesture mGesture;
    private final String mText;
    private HashMap<String, Identifiable> mIdentifiableMap;

    /**
     * Get the text as defined in the notify-block of the {@link Gesture
     * gesture}.
     *
     * @return The text defined for the event.
     */
    public String getText() {
        return mText;
    }

    /**
     * Get an @{link Identifiable} with a specific id.
     *
     * @param id The identifier as described in the JSON description of the
     *            {@link Gesture}.
     * @return A matching {@link Identifiable} or null if the id was not found.
     */
    public Identifiable getIdentifiable(String id) {
        return mIdentifiableMap.get(id);
    }

    /**
     * Get all the {@link Identifiable}s from the event.
     *
     * @return A collection of identified objects.
     */
    public Collection<Identifiable> getIdentifiables() {
        return mIdentifiableMap.values();
    }

    /**
     * Dispatches this event from the {@link Gesture} from which it emanates.
     */
    public void dispatch() {
        mGesture.dispatchEvent(this);
    }

    private Event(final Gesture gesture, final String text) {
        mGesture = gesture;
        mText = text;
        mIdentifiableMap = new HashMap<String, Identifiable>();
    }

    @SuppressWarnings("unused")
    private void addPose(String id, int objectType, int x, int y, int width, int height) {
        mIdentifiableMap.put(id, new Pose(id, objectType, x, y, width, height));
    }

    @SuppressWarnings("unused")
    private void addSwipe(String id, int direction) {
        if (direction == Swipe.Direction.SWIPE_LEFT.ordinal()) {
            mIdentifiableMap.put(id, new Swipe(id, Swipe.Direction.SWIPE_LEFT));
        } else {
            mIdentifiableMap.put(id, new Swipe(id, Swipe.Direction.SWIPE_RIGHT));
        }
    }
}
