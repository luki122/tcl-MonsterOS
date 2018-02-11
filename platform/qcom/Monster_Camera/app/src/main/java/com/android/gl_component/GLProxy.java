/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.gl_component;

/* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.view.Surface;
/* MODIFIED-END by sichao.hu,BUG-2821981*/

/**
 * Created by sichao.hu on 4/12/16.
 */

/* MODIFIED-BEGIN by Sichao Hu, 2016-09-23,BUG-2989818*/
public abstract class GLProxy {
    public interface OnTextureGeneratedListener{
        public void onTextureGenerated(int[] ids);
    }
    //    public interface AnimationProgressListener{
//        public void onAnimationDone(int chosenIndex);
//    }
//
    public abstract void createWindow(SurfaceTexture texture,OnTextureGeneratedListener listener);
    public abstract void startRendering();
    public abstract void stopRendering();
//
//    public void startEnlargeAnimation(AnimationProgressListener listener){
//
//    }
//    public void startEnlargeAnimation(float x,float y,AnimationProgressListener listener){
//
//    }
//    public void startShrinkAnimation(AnimationProgressListener listener){
//
//    }
//
//    public void switchToSingleWindowImmediately(int chosenFilterIndex,AnimationProgressListener listener){ // MODIFIED by jianying.zhang, 2016-11-08,BUG-3255060
//
//    }

    public abstract void rotateTexture(boolean isReversed);
    /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
    public abstract void onTextureUpdated(SurfaceTexture texture,Rect recorderSrufaceArea);
    public abstract void updateSurfaceSize(int w, int h);
    public abstract boolean isRendering();
    public abstract void destroyWindow(); // MODIFIED by sichao.hu, 2016-09-12,BUG-2895116
    public abstract void attachRecordSurface(Surface surface);
    /* MODIFIED-END by sichao.hu,BUG-2821981*/
    public abstract void setOnFirstFrameListener(GLRenderer.FirstFrameListener listener);
    /* MODIFIED-END by Sichao Hu,BUG-2989818*/
    /* MODIFIED-END by Sichao Hu,BUG-2989818*/
}
