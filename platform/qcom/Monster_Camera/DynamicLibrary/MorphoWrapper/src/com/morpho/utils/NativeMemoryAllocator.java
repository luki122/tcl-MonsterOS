package com.morpho.utils;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import android.util.Log;

import com.android.classloader.CommonInstruction;

public class NativeMemoryAllocator implements CommonInstruction{

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
    
    @Override
    public Callable<Object> getFunctionPointer(final String msg, final Object... parameters) {
        Callable<Object> callable=new Callable<Object>(){

            @Override
            public Object call() throws Exception {
                switch(msg){
                case allocateBuffer:
                    int size=(int)parameters[0];
                    return allocateBuffer(size);
                case freeBuffer:
                    ByteBuffer byteBuffer=(ByteBuffer)parameters[0];
                    return freeBuffer(byteBuffer);
                default:
                    return null;
                }
            }
            
        };
        return callable;
    }
    public static final native ByteBuffer allocateBuffer(int size);
    public static final native int freeBuffer(ByteBuffer byteBuffer);
}
