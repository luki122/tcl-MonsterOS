/* Copyright (C) 2016 Tcl Corporation Limited */
package com.morpho.utils;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import android.util.Log;

public class NativeMemoryAllocator {

    private static final String TAG="MorphoNativeMemoryAllocator";
    static {
        try {
            System.loadLibrary("morpho_memory_allocator");
            Log.e(TAG, "load morpho lib success");

        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "load morpho pano lib failed :"+e.getMessage());
        }
    }

    public static final String allocateBuffer="allocateBuffer";
    public static final String freeBuffer="freeBuffer";

    public static final native ByteBuffer allocateBuffer(int size);
    public static final native int freeBuffer(ByteBuffer byteBuffer);
}
