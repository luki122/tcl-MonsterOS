/* Copyright (C) 2016 Tcl Corporation Limited */
package com.crunchfish.touchless_a3d.exception;

/**
 * Copyright (c) 2014 Crunchfish AB. All rights reserved. All information herein
 * is or may be trade secrets of Crunchfish AB.
 * <p>
 * Exception thrown when a call is made to a dead TouchlessA3D instance.
 *
 * @see com.crunchfish.touchless_a3d.TouchlessA3D#close()
 */
public class DeadInstanceException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DeadInstanceException() {
    }

    public DeadInstanceException(final String detailMessage) {
        super(detailMessage);
    }

    public DeadInstanceException(final Throwable throwable) {
        super(throwable);
    }

    public DeadInstanceException(final String detailMessage, final Throwable throwable) {
        super(detailMessage, throwable);
    }
}
