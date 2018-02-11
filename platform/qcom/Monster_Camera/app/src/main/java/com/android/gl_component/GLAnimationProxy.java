/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.gl_component;

import android.graphics.SurfaceTexture;

/**
 * Created by sichao.hu on 16-9-22.
 */
public interface GLAnimationProxy {

    public interface AnimationProgressListener{
        public void onAnimationDone(int chosenIndex);
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
    public void startEnlargeAnimation(int chosenFilterIndex,AnimationProgressListener listener);
    public void startShrinkAnimation(AnimationProgressListener listener);
    public void switchToSingleWindowImmediately(int chosenFilterIndex,AnimationProgressListener listener);
    /* MODIFIED-END by jianying.zhang,BUG-3255060*/
}
