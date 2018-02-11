package com.morpho.utils;

import android.content.Context;

import com.android.camera.debug.Log;

import java.nio.ByteBuffer;

public class NativeMemoryAllocatorWrapper {

    public final static Log.Tag TAG = new Log.Tag("MemoryAllocator");

    private NativeMemoryAllocatorWrapper(){

    }

    private static class NativeMemoryAllocatorHolder{
        private static final NativeMemoryAllocatorWrapper mInstance=new NativeMemoryAllocatorWrapper();
    }

    /* MODIFIED-BEGIN by yuanxing.tan, 2016-09-12,BUG-2861353*/
    public static NativeMemoryAllocatorWrapper getInstance(){
        return NativeMemoryAllocatorHolder.mInstance;
    }

    public static final String allocateBuffer="allocateBuffer";
    public static final String freeBuffer="freeBuffer";

    public ByteBuffer allocateBuffer(Context context,int size){
        return com.morpho.utils.NativeMemoryAllocator.allocateBuffer(size);
    }
    public int freeBuffer(Context context,ByteBuffer byteBuffer){
        return com.morpho.utils.NativeMemoryAllocator.freeBuffer(byteBuffer);
        /* MODIFIED-END by yuanxing.tan,BUG-2861353*/
    }

}
