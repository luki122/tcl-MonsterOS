/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.gl_component;

import android.opengl.EGL14; // MODIFIED by sichao.hu, 2016-08-30,BUG-2821981
import android.opengl.GLUtils;

import javax.microedition.khronos.egl.EGL10;

/**
 * Created by sichao.hu on 4/11/16.
 */
public class CustGLException extends RuntimeException{
    public CustGLException(String errorMessge, EGL10 egl10){
        super(errorMessge+" :"+ GLUtils.getEGLErrorString(egl10.eglGetError()));
    }

    /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
    public CustGLException(String errorMessage){
        super(errorMessage+" :"+ GLUtils.getEGLErrorString(EGL14.eglGetError()));
    }
    public static CustGLException buildEGLException(String erroMessage,EGL10 egl10){
        throw new CustGLException(erroMessage,egl10);
    }

    public static CustGLException buildEGLException(String erroMessage){
        throw new CustGLException(erroMessage);
    }
    /* MODIFIED-END by sichao.hu,BUG-2821981*/
}
