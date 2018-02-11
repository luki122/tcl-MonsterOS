package com.crunchfish.core;

import android.content.Context;

import com.android.camera.debug.Log;
import com.android.classloader.ExternalLoader;
import com.crunchfish.touchless_a3d.TouchlessA3D.Rotate; // MODIFIED by yuanxing.tan, 2016-09-12,BUG-2861353


/**
 * Created by sichao.hu on 12/1/15.
 */
public class GestureInstructionWrapper {
    private ExternalLoader mLibloader;
    private static final String contructGesture="constructGesture";
    private static final String initializeGestureCore="initializeGestureCore";
    private static final String setGestureListener="setGestureListener";
    private static final String handleImage="handleImage";
    private static final String releaseEngine="releaseEngine";
    private Context mContext;
    private static final Log.Tag TAG=new Log.Tag("GestureInstructionWrapper");

    public static final int ROTATE_NONE=0;
    public static final int ROTATE_90=ROTATE_NONE+1;
    public static final int ROTATE_180=ROTATE_90+1;
    public static final int ROTATE_270=ROTATE_180+1;

    /* MODIFIED-BEGIN by yuanxing.tan, 2016-09-12,BUG-2861353*/
    private GestureCore mGestureCore;
    public GestureInstructionWrapper(Context context){
        mContext=context;
    }

    public void initGestureWrapper(){
        mGestureCore = new GestureCore(mContext);
    }

    public void updateParameters(int width,int height){
        mGestureCore.initializeGestureCore(width, height);
    }

    public void setGestureDetectionCallback(GestureDetectionCallback callback){
        mGestureCore.setGestureListener(callback);
        /* MODIFIED-END by yuanxing.tan,BUG-2861353*/
    }

    /**
     *
     * @param timestamp
     * @param data
     * @param rotate : 0 stands for rotate_0, 1 for rotate_90, 2 for rotate_180, 3 for rotate_270
     */
    /* MODIFIED-BEGIN by yuanxing.tan, 2016-09-12,BUG-2861353*/
    public void handleImage(long timestamp,byte[] data,int rotation){
        Rotate rotate = Rotate.DO_NOT_ROTATE;
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
        mGestureCore.handleImage(timestamp, data, rotate);
    }

    public void releaseEngine(){
        mGestureCore.releaseEngine();
        /* MODIFIED-END by yuanxing.tan,BUG-2861353*/
    }

}
