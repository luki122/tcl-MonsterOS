/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.gl_component;

/* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.view.Surface;
/* MODIFIED-END by sichao.hu,BUG-2821981*/

import com.android.camera.debug.Log;

/**
 * Created by sichao.hu on 4/12/16.
 *
 * * The rough pipeline can be described as :
 * First for initialization:
 * 1. Before drawing , you have to create program instance to make all the following step possible .
 * 2. Generate an texture by glGenTextures , bind the texture ,and specify ID of texture which is intended to be an uniform used in fragment shader
 * 3. Compile your shader source on runtime into GL pipeline , attach the compiled shader onto program instance
 * 4. Declare your attributes name and map the location in shader by glBindAttribLocation
 *
 * Then we go to the rendering stage:
 * 1.Allocate buffers to the attributes , because the shader is created on runtime , there's no memory allocation for all the stuff working in shader
 * 2.Query the exact location of attributes you've declared in vertex shader , and bind the supposed value onto it , remember to enable it before drawing
 * 3.Query the exact location of uniform and parse your value into it.
 * 4.Specify the texture to be processed in shader , in Android , it's convenient to use GLUtils.texImage2D to parse a bitmap into the shader ,
 * 5.Call DrawElement to draw the primitives expected , plz refer to the OpenGLES2.0 programming guide of the indices order of each draw type enum.
 * 6.After draw finish , make sure to swap buffer , thus all your stuff rendered would come to the front , this should be done outside the renderer , GLContext need to implement it on every frame finished
 * 7.Disable your attributes which you've enabled in a single draw
 */
public abstract class GLRenderer {
    protected int mProgram;
    protected SurfaceTexture mSurfaceTexture;
    protected int mWidth;
    protected int mHeight;
    protected SurfaceTexture mInputTexture;
    private boolean mIsFrameArrived=false;
    private FirstFrameListener mFirstFrameListener;

    private Log.Tag TAG=new Log.Tag("GLRender");

    public interface FirstFrameListener{
        public void onFirstFrameArrive();
    }

    public GLRenderer(SurfaceTexture surfaceTexture){
        mSurfaceTexture=surfaceTexture;
        mProgram= GLES20.glCreateProgram();//step 1 for initialization
    }


    public abstract int[] prepareTextures();//step 2 for initialization // MODIFIED by Sichao Hu, 2016-09-23,BUG-2989818

    public abstract int loadShader();//step 3,4 for initialization

    public abstract void prepareBuffer(boolean isReversed);//step 1 for rendering , it can be done once or do reset if necessary , reset is integrated in renderingFrame

    protected abstract void releaseBuffer();

    public final void renderingFrame(SurfaceTexture inputTexture,int width,int height){
        mInputTexture=inputTexture;
        resetBuffer();
        /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
        preparePreviewFBO(width, height);
        int[] attrs=enableAttributes();
        bindUniforms(mProgram,width,height);
        drawFrame(mProgram, width, height);
        renderFromFBOToWindow(width, height);
        disableAttributes(attrs);
    }

    public final void renderingFrame(SurfaceTexture inputTexture,int width,int height,Rect recordArea){
        mInputTexture=inputTexture;
        resetBuffer();
        int largerWidth,largerHeight;
        if(width>=recordArea.width()){
            largerWidth=width;
            largerHeight=height;
        }else{
            largerWidth=recordArea.width();
            largerHeight=recordArea.height();
        }
        preparePreviewFBO(largerWidth, largerHeight);
        int[] attrs=enableAttributes();
        bindUniforms(mProgram,largerWidth,largerHeight);
        drawFrame(mProgram, largerWidth, largerHeight);
        renderFromFBOToWindow(width, height);
        disableAttributes(attrs);
    }

    public final void renderingFrameToRecorder(int width, int height,int previewWidth,int previewHeight){
//        bindUniforms(mRecordingProgram,width,height);
        copyFBOToRecorderSurface(width, height,previewWidth,previewHeight);
    }

    protected void preparePreviewFBO(int width, int height){

    }


    protected void renderFromFBOToWindow(int width, int height){

    }

    protected void resetBuffer(){
        //step 1 for rendering on each frame , it's optional , override it if necessary
    }

    protected abstract int[] enableAttributes();//step 2 for rendering


    protected void bindUniforms(int program,int w,int h){
    /* MODIFIED-END by sichao.hu,BUG-2821981*/
        //step 3 for rendering , it's optional
    }

    public final void setOnFirstFrameListener(FirstFrameListener listener){
        mFirstFrameListener=listener;
    }


    protected void drawFrame(int program,int width,int height){ // MODIFIED by sichao.hu, 2016-08-30,BUG-2821981
        if(!mIsFrameArrived){
            mIsFrameArrived=true;
            if(mFirstFrameListener!=null){
                mFirstFrameListener.onFirstFrameArrive();
                Log.w(TAG,"onFirstFrameArrive");
            }
        }
    }//step 4 ,5

    /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
    protected void copyFBOToRecorderSurface(int width, int height,int previewWidth,int previewHeight){

    }
    /* MODIFIED-END by sichao.hu,BUG-2821981*/

    // step 6 not supposed implemented here


    /* MODIFIED-BEGIN by Sichao Hu, 2016-09-23,BUG-2989818*/
    protected void disableAttributes(int[] attrs){
        for(int attr:attrs){
            GLES20.glDisableVertexAttribArray(attr);
        }
    }


    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
    public void startEnlargeAnimation(int chosenFilterIndex,GLAnimationProxy.AnimationProgressListener listener){

    }

    public void switchToSingleWindowImmediately(int chosenFilterIndex,GLAnimationProxy.AnimationProgressListener listener){
    /* MODIFIED-END by jianying.zhang,BUG-3255060*/

    }

    public void startShrinkAnimation(GLAnimationProxy.AnimationProgressListener listener){
    /* MODIFIED-END by Sichao Hu,BUG-2989818*/

    }

}
