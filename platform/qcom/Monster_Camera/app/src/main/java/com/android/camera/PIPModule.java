/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera;

import android.graphics.SurfaceTexture;

import com.android.camera.app.AppController;
import com.android.gl_component.GLProxy;
import com.android.gl_component.PIPComposer;

/**
 * Created by sichao.hu on 9/13/16.
 */
public class PIPModule extends BaseGLModule {
    public PIPModule(AppController app) {
        super(app);
    }



    //TODO : Open 2 Camera device together at once , retrive 2 single texture ids to generate surfaceTexture , and parcel it to 2 camera devices

    @Override
    protected GLProxy buildGLProxy() {
        return new PIPComposer(mAppController.getAndroidContext());
    }

    private SurfaceTexture mPIPScreenTexture;
    @Override
    protected void onTextureGenerated(int[] ids) {
        mPIPScreenTexture=new SurfaceTexture(ids[1]);

    }

    @Override
    protected void startPreview() {
        super.startPreview();
        if(mPIPScreenTexture!=null){
            mCameraDevice.setPreviewTexture(mPIPScreenTexture);
        }
    }

    @Override
    protected SurfaceTexture getTexture() {
        return super.getTexture();
    }

    @Override
    public void pause() {
        super.pause();
        mPIPScreenTexture=null;
    }
}
