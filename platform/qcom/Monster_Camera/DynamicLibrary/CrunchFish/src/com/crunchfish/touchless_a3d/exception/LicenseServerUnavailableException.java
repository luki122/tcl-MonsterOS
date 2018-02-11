package com.crunchfish.touchless_a3d.exception;

/**
 * Copyright (c) 2013 Crunchfish AB. All rights reserved. All information herein
 * is or may be trade secrets of Crunchfish AB.
 *<p>
 * Exception thrown when the license could not be verified by the license-server.
 * This could be due to bad or no network connection.
 */
public class LicenseServerUnavailableException extends Exception {

    /**
     * Default serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    public LicenseServerUnavailableException()
    {
        super();
    }

    public LicenseServerUnavailableException(String message)
    {
        super(message);
    }

    public LicenseServerUnavailableException(Throwable cause)
    {
        super(cause);
    }

    public LicenseServerUnavailableException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
