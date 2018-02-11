package com.crunchfish.core;

import java.util.concurrent.Callable;

import android.content.Context;

import com.android.classloader.CommonInstruction;
import com.crunchfish.touchless_a3d.TouchlessA3D.Rotate;

public class GestureInstructionImpl implements CommonInstruction{
    private static final String contructGesture="constructGesture";
    private static final String initializeGestureCore="initializeGestureCore";
    private static final String setGestureListener="setGestureListener";
    private static final String handleImage="handleImage";
    private static final String releaseEngine="releaseEngine";
    private GestureCore mGestureCore;
    
    private static final int ROTATE_NONE=0;
    private static final int ROTATE_90=ROTATE_NONE+1;
    private static final int ROTATE_180=ROTATE_90+1;
    private static final int ROTATE_270=ROTATE_180+1;
    
    @Override
    public Callable<Object> getFunctionPointer(final String msg, final Object... parameters) {
        Callable<Object> callable=new Callable<Object>(){

            @Override
            public Object call() throws Exception {
                switch(msg){
                case contructGesture:{
                    Context context=(Context)parameters[0];
                    mGestureCore=new GestureCore(context);
                }
                    break;
                case initializeGestureCore:{
                    int width=(int)parameters[0];
                    int height=(int)parameters[1];
                    mGestureCore.initializeGestureCore(width, height);
                }
                    break;
                case setGestureListener:{
                    GestureDetectionCallback callback=(GestureDetectionCallback)parameters[0];
                    mGestureCore.setGestureListener(callback);
                }
                    break;
                case handleImage:{
                    long time=(long)parameters[0];
                    byte[] data=(byte[])parameters[1];
                    Rotate rotate=Rotate.DO_NOT_ROTATE;
                    int rotation=(int)parameters[2];
                    switch(rotation){
                    case ROTATE_NONE:
                        rotate=Rotate.DO_NOT_ROTATE;
                        break;
                    case ROTATE_90:
                        rotate=Rotate.ROTATE_90;
                        break;
                    case ROTATE_180:
                        rotate=Rotate.ROTATE_180;
                        break;
                    case ROTATE_270:
                        rotate=Rotate.ROTATE_270;
                        break;
                    }
                    mGestureCore.handleImage(time, data, rotate);
                }
                    break;
                case releaseEngine:{
                    mGestureCore.releaseEngine();
                    break;
                }
                }
                return null;
            }
        };
        return callable;
    }
    
    
}
