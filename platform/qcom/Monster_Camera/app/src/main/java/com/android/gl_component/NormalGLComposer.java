/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.gl_component;

import android.content.Context;
/* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;

import com.android.camera.debug.Log;


/**
 * Created by sichao.hu on 4/11/16.
 */
/* MODIFIED-BEGIN by jianying.zhang, 2016-11-15,BUG-3467717*/
public class NormalGLComposer extends GLProxy implements GLAnimationProxy {
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    protected GLRenderer mGLRenderer;
    private Log.Tag TAG = new Log.Tag("NormalGLComposer");

    protected boolean mIsRendering;
    protected Handler mGLHandler;

    protected EGLSurface mGLSurface = EGL14.EGL_NO_SURFACE;
    protected EGLSurface mRecorderSurface = EGL14.EGL_NO_SURFACE;
    protected EGLDisplay mGLDisplay = EGL14.EGL_NO_DISPLAY;
    protected EGLContext mGLContext = EGL14.EGL_NO_CONTEXT;
    /* MODIFIED-END by jianying.zhang,BUG-3467717*/
    /* MODIFIED-END by sichao.hu,BUG-2821981*/

    private GLRenderThread mGLRenderThread;

    private Context mContext;
    protected int mWidth;
    protected int mHeight;
    protected Long mStartTime = null; // MODIFIED by jianying.zhang, 2016-11-15,BUG-3467717
    private GLRenderer.FirstFrameListener mFirstFrameListener;
    private EGLConfig[] mChosenConfig; // MODIFIED by sichao.hu, 2016-08-30,BUG-2821981

    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-28,BUG-3137073*/
    private boolean mIsRecordingPaused = false;
    private long mPausedDuration = 0;
    private long mLastTimestamp = 0;
    /* MODIFIED-END by jianying.zhang,BUG-3137073*/

    public NormalGLComposer(Context context) {
        mContext = context; // MODIFIED by jianying.zhang, 2016-11-15,BUG-3467717
    }

    /**
     * The very first intention for the initialization is to create EGLContext ,
     * and bind the context to EGL instance to make as current context , which requires glDisplay and glSurface (You may also add a PBuffer for the off-screen rendering)
     * intialize steps:
     * 1. create EGL instance , it's the where the GL pipeline layout on
     * 2. create display instance
     * 3. initialize display and choose version
     * 4. choose configuration (several enums...) for GL which is required for the eglSurface creation
     * 5. create window surface and specify the render attributes
     * 6. initialize context
     * 7. specific current context .
     // MODIFIED-BEGIN by jianying.zhang, 2016-11-15, BUG-3467717
     * <p/>
     * After all , context is initialized and binded as current within the GL instance
     */
    private void initGLWindow() {
        //step1
        //step2
        /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
        mGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (mGLDisplay == EGL14.EGL_NO_DISPLAY) {
            CustGLException.buildEGLException("EGL no display");
        }
        //step3
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mGLDisplay, version, 0, version, 1)) {
            CustGLException.buildEGLException("Failed to initialize");
        }

        //step4
        int[] numConfigs = new int[1];
        mChosenConfig = new EGLConfig[1];
        if (!EGL14.eglChooseConfig(mGLDisplay, GLConfigs.EGL_CONFIG, 0, mChosenConfig, 0, 1, numConfigs, 0)) {
            CustGLException.buildEGLException("Configuration error");
        }

        //step5
        mGLSurface = EGL14.eglCreateWindowSurface(mGLDisplay, mChosenConfig[0], mSurfaceTexture, GLConfigs.EGL_RENDER_ATTRIBUTES, 0);

        if (null == mGLSurface || EGL14.EGL_NO_SURFACE == mGLSurface) {
            CustGLException.buildEGLException("GL create surface failed");
        }
        //step6 , first time use eglCreateContext to  specify configuration and initialize glContext instance
        mGLContext = EGL14.eglCreateContext(mGLDisplay, mChosenConfig[0], EGL14.EGL_NO_CONTEXT, GLConfigs.EGL_CONTEXT_CONFIG, 0);

        if (EGL14.EGL_NO_CONTEXT == mGLContext) {
            CustGLException.buildEGLException("Context create failed");
        }


        //step7
        if (!EGL14.eglMakeCurrent(mGLDisplay, mGLSurface, mGLSurface, mGLContext)) {
            CustGLException.buildEGLException("GL make current failed");
            /* MODIFIED-END by sichao.hu,BUG-2821981*/
        }

    }

    private class GLRenderThread extends HandlerThread {
    /* MODIFIED-END by jianying.zhang,BUG-3467717*/
        public GLRenderThread(String name) {
            super(name);
        }
    }


    /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
    @Override
    public void attachRecordSurface(final Surface surface) {
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-15,BUG-3467717*/
        if (mGLHandler != null) {
            mGLHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRecorderSurface != EGL14.EGL_NO_SURFACE) {
                        EGL14.eglDestroySurface(mGLDisplay, mRecorderSurface);
                    }
                    mSurface = surface;
                    mRecorderSurface = EGL14.EGL_NO_SURFACE;
                    initializeRecorderContext(surface);
                }
            });
        }

    }

    private void initializeRecorderContext(Surface surface) {
        if (surface == null || mChosenConfig == null) {
            return;
        }
        if (mRecorderSurface != EGL14.EGL_NO_SURFACE) {
            return;
        }

        //step5
        mRecorderSurface = EGL14.eglCreateWindowSurface(mGLDisplay, mChosenConfig[0], surface, GLConfigs.EGL_RENDER_ATTRIBUTES, 0);

        if (null == mGLSurface || EGL14.EGL_NO_SURFACE == mGLSurface) {
        /* MODIFIED-END by jianying.zhang,BUG-3467717*/
            CustGLException.buildEGLException("GL create surface failed");
        }

    }
    /* MODIFIED-END by sichao.hu,BUG-2821981*/

    /**
     * Generate an texture name which is supposed bound with surfaceTexture
     // MODIFIED-BEGIN by jianying.zhang, 2016-11-15, BUG-3467717
     *
     * @return
     */
    private int[] initializeWindow() {
        initGLWindow();
        //TODO: choose GLRenderer implement and initialize it
        mGLRenderer = buildGLRenderer(mSurfaceTexture, mContext);
        mGLRenderer.setOnFirstFrameListener(mFirstFrameListener);
        if (mGLRenderer != null) {
            return mGLRenderer.prepareTextures();
        }
        return null;
    }

    protected GLRenderer buildGLRenderer(SurfaceTexture surfaceTexture, Context context) {
        return new BaseGLRendererImpl(surfaceTexture, context);
    }

    @Override
    public void updateSurfaceSize(int w, int h) {
        Log.d(TAG, "width : " + w + " height : " + h);
        mWidth = w;
        mHeight = h;
    }


    private boolean mIsReversed = false;

    @Override
    public void rotateTexture(final boolean isReverse) {
        mIsReversed = isReverse;
        if (mIsRendering && mGLHandler != null) {
            mGLHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mGLRenderer != null) {
                        mGLRenderer.prepareBuffer(isReverse);
                    }
                    /* MODIFIED-END by jianying.zhang,BUG-3467717*/
                }
            });

        }
    }

    /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
    @Override
    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-15,BUG-3467717*/
    public void onTextureUpdated(final SurfaceTexture texture, final Rect recorderSurfaceArea) {
        if (mGLHandler != null) {
            mGLHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mGLRenderer != null) {
                        //step7
                        if (mRecorderSurface != EGL14.EGL_NO_SURFACE) {
                            //To ensure the final data is not interpolated , we use the higher resolution surface to generate FBO
                            if (!EGL14.eglMakeCurrent(mGLDisplay, mGLSurface, mGLSurface, mGLContext)) {
                                CustGLException.buildEGLException("GL make current failed");
                            }
                            mGLRenderer.renderingFrame(texture, mWidth, mHeight, recorderSurfaceArea);
                            /* MODIFIED-END by jianying.zhang,BUG-3467717*/
                            Log.d(TAG, "onTextureUpdated mRecorderSurface");

                            EGL14.eglSwapBuffers(mGLDisplay, mGLSurface);

                            /* MODIFIED-BEGIN by jianying.zhang, 2016-10-28,BUG-3137073*/
                            if (mIsRecordingPaused) {
                                mPausedDuration += texture.getTimestamp() - mLastTimestamp;
                                mLastTimestamp = texture.getTimestamp();
                                return;
                            }
                            mLastTimestamp = texture.getTimestamp();
                            /* MODIFIED-END by jianying.zhang,BUG-3137073*/

                            /* MODIFIED-BEGIN by jianying.zhang, 2016-11-15,BUG-3467717*/
                            if (!EGL14.eglMakeCurrent(mGLDisplay, mRecorderSurface, mRecorderSurface, mGLContext)) {
                                CustGLException.buildEGLException("GL make current failed");
                            }
                            if (recorderSurfaceArea != null) {
                                mGLRenderer.renderingFrameToRecorder(recorderSurfaceArea.width(), recorderSurfaceArea.height(), mWidth, mHeight);
                            } else {
                                mGLRenderer.renderingFrameToRecorder(mWidth, mHeight, mWidth, mHeight);
                            }
                            if (mStartTime == null) {
                                mStartTime = texture.getTimestamp();
                            }
                            /* MODIFIED-BEGIN by jianying.zhang, 2016-10-28,BUG-3137073*/
                            EGLExt.eglPresentationTimeANDROID(mGLDisplay, mRecorderSurface,
                                    mLastTimestamp - mStartTime - mPausedDuration);
                                    /* MODIFIED-END by jianying.zhang,BUG-3137073*/
                            EGL14.eglSwapBuffers(mGLDisplay, mRecorderSurface);
                        } else {
                            if (!EGL14.eglMakeCurrent(mGLDisplay, mGLSurface, mGLSurface, mGLContext)) {
                                CustGLException.buildEGLException("GL make current failed");
                            }
                            mGLRenderer.renderingFrame(texture, mWidth, mHeight);
                            Log.d(TAG, "onTextureUpdated");
                            EGL14.eglSwapBuffers(mGLDisplay, mGLSurface);
                            mStartTime = null;
                            /* MODIFIED-END by jianying.zhang,BUG-3467717*/

                            /* MODIFIED-BEGIN by jianying.zhang, 2016-10-28,BUG-3137073*/
                            mIsRecordingPaused = false;
                            mPausedDuration = 0;
                            /* MODIFIED-END by jianying.zhang,BUG-3137073*/
                        }
                        /* MODIFIED-END by sichao.hu,BUG-2821981*/
                    }
                }
            });
        }
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-10-28,BUG-3137073*/
    public void pauseRecording(final boolean isPaused) {
        if (mGLHandler != null) {
            mGLHandler.post(new Runnable() {
                @Override
                public void run() {
                    mIsRecordingPaused = isPaused;
                }
            });
        }
    }
    /* MODIFIED-END by jianying.zhang,BUG-3137073*/

    @Override
    public void startRendering() {
        Log.w(TAG, String.format("render start %sx%s", mWidth, mHeight));
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-15,BUG-3467717*/
        if (mGLHandler != null) {
            mGLHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mGLRenderer != null) {
                        mGLRenderer.loadShader();
                        if (mIsReversed) {
                            mGLRenderer.prepareBuffer(mIsReversed);
                        }
                    }
                }
            });
            mIsRendering = true;
        }
    }

    @Override
    public void createWindow(SurfaceTexture texture, final OnTextureGeneratedListener listener) {
        mSurfaceTexture = texture;
        mGLRenderThread = new GLRenderThread(TAG.toString());
        mGLRenderThread.start();
        mGLHandler = new Handler(mGLRenderThread.getLooper());
        mGLHandler.post(new Runnable() {
            @Override
            public void run() {
                int[] texturesId = initializeWindow();
                if (texturesId != null) {
                    listener.onTextureGenerated(texturesId);
                }
            }
        });
    }

    @Override
    public void stopRendering() {
        mIsRendering = false;
        Log.d(TAG, "stopRendering");

    }

    @Override
    public void setOnFirstFrameListener(final GLRenderer.FirstFrameListener listener) {
        mFirstFrameListener = listener;
        if (mGLRenderer != null) {
        /* MODIFIED-END by jianying.zhang,BUG-3467717*/
            mGLRenderer.setOnFirstFrameListener(listener);
        }
    }

    @Override
    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
    public void startEnlargeAnimation(final int chosenFilterIndex, final AnimationProgressListener listener) {
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-15,BUG-3467717*/
        if (mGLHandler != null) {
            mGLHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mGLRenderer != null) {
                        mGLRenderer.startEnlargeAnimation(chosenFilterIndex, listener);
                        /* MODIFIED-END by jianying.zhang,BUG-3255060*/
                    }
                    /* MODIFIED-END by jianying.zhang,BUG-3467717*/
                }
            });
        }
    }

    @Override
    public void startShrinkAnimation(final AnimationProgressListener listener) {
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-15,BUG-3467717*/
        if (mGLHandler != null) {
            mGLHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mGLRenderer != null) {
                        mGLRenderer.startShrinkAnimation(listener);
                    }
                    /* MODIFIED-END by jianying.zhang,BUG-3467717*/
                }
            });
        }
    }

    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
    @Override
    public void switchToSingleWindowImmediately(final int chosenFilterIndex, final AnimationProgressListener listener) {
        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-15,BUG-3467717*/
        if (mGLHandler != null) {
            mGLHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mGLRenderer != null) {
                        mGLRenderer.switchToSingleWindowImmediately(chosenFilterIndex, listener);
                        /* MODIFIED-END by jianying.zhang,BUG-3255060*/
                    }
                    /* MODIFIED-END by jianying.zhang,BUG-3467717*/
                }
            });
        }
    }

    @Override
    public boolean isRendering() {
        return mIsRendering;
    }

    /**
     * MUST be called after stop rendering! Or could cause unexpected exception
     */
    @Override
    public void destroyWindow() {
        /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
        final Object waitLock = new Object(); // MODIFIED by jianying.zhang, 2016-11-15,BUG-3467717
        Runnable syncEndRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (waitLock) {
                    mGLRenderer = null;
                    if (mGLRenderThread != null) {
                        mGLHandler.removeCallbacks(null);
                        mGLRenderThread.quitSafely();
                        mGLRenderThread = null;
                    }
                    mGLHandler = null;
                    waitLock.notifyAll();
                }
            }
        };
        synchronized (waitLock) {
            /* MODIFIED-BEGIN by jianying.zhang, 2016-11-15,BUG-3467717*/
            if (mGLHandler != null) {
                mGLHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mGLRenderer != null) {
                            mGLRenderer.releaseBuffer();
                            boolean result = EGL14.eglDestroySurface(mGLDisplay, mGLSurface);
                            Log.w(TAG, "destroy surface result is " + result);
                            if (mRecorderSurface != EGL14.EGL_NO_SURFACE) {
                                EGL14.eglDestroySurface(mGLDisplay, mRecorderSurface);
                            }
                            result = EGL14.eglDestroyContext(mGLDisplay, mGLContext);
                            Log.w(TAG, "destroy context result is " + result);
                            EGL14.eglTerminate(mGLDisplay);
                            EGL14.eglReleaseThread();
                            /* MODIFIED-END by sichao.hu,BUG-2821981*/
                        }
                    }
                });
                mGLHandler.post(syncEndRunnable);
                try {
                    waitLock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
/* MODIFIED-END by jianying.zhang,BUG-3467717*/
