/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.Encoder;

/**
 * Created by sichao.hu on 10/27/16.
 */
public class EncoderUtils {

    public static long systemTimeInMicroSecond() {
        return System.nanoTime() / 1000;
    }
}
