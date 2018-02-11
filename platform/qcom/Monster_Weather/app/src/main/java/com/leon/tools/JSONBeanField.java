/* Copyright (C) 2016 Tcl Corporation Limited */
package com.leon.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONBeanField {
    /**
     */
    String name();
}
