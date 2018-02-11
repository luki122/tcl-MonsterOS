/* Copyright (C) 2016 Tcl Corporation Limited */
package com.crunchfish.touchless_a3d.exception;

/**
 * Copyright (c) 2013 Crunchfish AB. All rights reserved. All information herein
 * is or may be trade secrets of Crunchfish AB.
 *<p>
 * Exception thrown when the license for TouchlessA3D is not valid.
 */
public class LicenseNotValidException extends Exception {

    /**
     * Default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public LicenseNotValidException ()
    {
        super();
    }

    public LicenseNotValidException(String message)
    {
        super(message);
    }

    public LicenseNotValidException(Throwable cause)
    {
        super(cause);
    }

    public LicenseNotValidException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
