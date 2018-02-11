/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.gl_component;

import android.opengl.EGL14;

import javax.microedition.khronos.egl.EGL10;

/**
 * Created by sichao.hu on 4/11/16.
 */
public class GLConfigs {
    public final static int[] EGL_CONFIG= new int[]{
            EGL10.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_ALPHA_SIZE, 8,
            EGL10.EGL_DEPTH_SIZE, 0,
            EGL10.EGL_STENCIL_SIZE, 0,
            EGL10.EGL_NONE
    };


    public final static int[] EGL_RENDER_ATTRIBUTES={
            EGL14.EGL_RENDER_BUFFER,EGL14.EGL_BACK_BUFFER,
            EGL10.EGL_NONE
    };

    public final static int[] EGL_CONTEXT_CONFIG= {
            EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
            EGL10.EGL_NONE
    };
}
