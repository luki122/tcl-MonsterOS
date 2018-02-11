/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.gl_component;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.view.Surface;

/**
 * Created by sichao.hu on 9/13/16.
 */
public class PIPGLComposer extends NormalGLComposer {
    public PIPGLComposer(Context context) {
        super(context);
    }

    @Override
    protected GLRenderer buildGLRenderer(SurfaceTexture surfaceTexture, Context context) {
        return new PIPGLRenderer(surfaceTexture);
    }

    @Override
    public void onTextureUpdated(final SurfaceTexture texture, Rect recorderSurfaceArea) {
        if(mGLHandler!=null){
            mGLHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mGLRenderer!=null) {
                        if(!EGL14.eglMakeCurrent(mGLDisplay,mGLSurface,mGLSurface,mGLContext)){
                            CustGLException.buildEGLException("GL make current failed");
                        }
                        mGLRenderer.renderingFrame(texture, mWidth, mHeight);
                        EGL14.eglSwapBuffers(mGLDisplay, mGLSurface);
                    }
                }
            });
        }
    }
}
