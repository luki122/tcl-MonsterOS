/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.camera;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;

import com.android.camera.app.AppController;
import com.android.camera.debug.Log;
import com.android.camera.util.GservicesHelper;
import com.android.ex.camera2.portability.CameraAgent;
import com.android.ex.camera2.portability.Size;
import com.android.gl_component.BaseGLRenderer;
import com.android.gl_component.GLProxy;
import com.android.gl_component.GLRenderer;
import com.android.gl_component.NormalGLComposer;

/**
 * Created by sichao.hu on 9/13/16.
 */
public abstract class BaseGLModule extends OptimizeBurstPhotoModule {
    private Log.Tag TAG=new Log.Tag("BaseGLModule");
    protected GLProxy mGLComponent;
    private CameraAgent.CameraProxy.WaitDoneCallback mWaitDoneCallback;
    protected Point mSurfaceSpec;
    private SurfaceTexture mCameraInputTexture;


    public BaseGLModule(AppController app) {
        super(app);
    }


    protected GLProxy.OnTextureGeneratedListener mTextureAvailableListener=new GLProxy.OnTextureGeneratedListener() {
        @Override
        public void onTextureGenerated(final int[] ids) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    BaseGLModule.this.onTextureGenerated(ids);
                }
            });
        }
    };

    protected void onTextureGenerated(int[] ids){
        mCameraInputTexture = new SurfaceTexture(ids[0]);
        Log.w(TAG, "texture available, texture Id: "+ids[0]);
        mCameraInputTexture.setOnFrameAvailableListener(mUpdateListener);
        startPreview();
    }

    private final SurfaceTexture.OnFrameAvailableListener mUpdateListener=new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            if(mPaused || mCameraState == SWITCHING_CAMERA || mGLComponent == null) {
                return;
            }
            /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/

            Log.w(TAG,"frame available");
            mGLComponent.onTextureUpdated(surfaceTexture, getRecorderSurfaceArea());
        }
    };

    protected Rect getRecorderSurfaceArea(){
        return null;
    }

    @Override
    protected SurfaceTexture getTexture() {
        return mCameraInputTexture;
    }
    @Override
    public void onSurfaceAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        super.onSurfaceAvailable(surfaceTexture, width, height);
        mSurfaceSpec=new Point(width,height);
        if(mGLComponent==null) {
            initializeGLComponent(surfaceTexture);
        }
    }

    @Override
    public void onSurfaceTextureChanged(SurfaceTexture surfaceTexture, int w, int h) {
        mSurfaceSpec=new Point(w,h);
        if(mGLComponent!=null){
            mGLComponent.updateSurfaceSize(w, h);
        }
    }

    private void checkTextureOrientation(){
        if(mGLComponent!=null){
            mGLComponent.rotateTexture(isCameraFrontFacing());
        }
    }

    @Override
    public void onCameraAvailable(CameraAgent.CameraProxy cameraProxy) {
        super.onCameraAvailable(cameraProxy);
        checkTextureOrientation();
        SurfaceTexture texture=mAppController.getCameraAppUI().getSurfaceTexture();
        if(texture!=null&&mGLComponent==null&&!mPaused) {
            mSurfaceSpec=mAppController.getCameraAppUI().getSurfaceTextureSize();
            initializeGLComponent(texture);
        }
    }

    @Override
    protected void requestCameraOpen() {
        Log.w(TAG, "requestCameraOpen " + mCameraId);
        mActivity.getCameraProvider().forceRequestCamera(mCameraId,
                GservicesHelper.useCamera2ApiThroughPortabilityLayer(mActivity.getAndroidContext()));
    }

    protected final void initializeGLComponent(SurfaceTexture displayTexture){
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-15,BUG-3467717*/
        if (mPaused) {
            return;
        }
        /* MODIFIED-END by jianying.zhang,BUG-3467717*/
        mGLComponent = buildGLProxy(); // MODIFIED by sichao.hu, 2016-09-12,BUG-2895116
        Log.w(TAG, "initialize gl component");

        Point size=mAppController.getCameraAppUI().getSurfaceTextureSize();
        if(size!=null){
            Log.w(TAG,String.format("size is %dx%d",size.x,size.y));
            mGLComponent.updateSurfaceSize(size.x, size.y);
        }
        checkTextureOrientation();
        mGLComponent.setOnFirstFrameListener(new GLRenderer.FirstFrameListener() {
            @Override
            public void onFirstFrameArrive() {
                Log.w(TAG,"onFirstFrameArrive");
                BaseGLModule.this.onFirstFrameArrive();
            }
        });
        mGLComponent.createWindow(displayTexture, mTextureAvailableListener);
        mGLComponent.startRendering();
    }


    protected void onFirstFrameArrive(){
        Log.w(TAG,"baseGLModule onFirstFrame");
    }


    protected abstract GLProxy buildGLProxy();

    @Override
    public void pause() {
        if(mGLComponent!=null){
            mGLComponent.stopRendering();
            mGLComponent.destroyWindow(); // MODIFIED by sichao.hu, 2016-09-12,BUG-2895116
            mGLComponent=null;
        }
        if (mCameraInputTexture != null) {
            mCameraInputTexture.setOnFrameAvailableListener(null);
            mCameraInputTexture = null;
        }
//        mEncoder.release(); // MODIFIED by sichao.hu, 2016-08-30,BUG-2821981
        super.pause();
    }

    @Override
    protected boolean isOptimizeCapture() {
        return false;
    }
}
