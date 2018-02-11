package com.crunchfish.core;

public enum RotateWrapper {
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
