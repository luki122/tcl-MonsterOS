/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.gl_component;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.android.camera.debug.Log;
import com.android.camera.util.Gusterpolator;
import com.android.ex.camera2.portability.CameraAgent;
import com.android.renderscript_post_process.LUTConfiguration;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * Created by sichao.hu on 7/19/16.
 */
public abstract class BaseGLRenderer extends GLRenderer{


    private static final int STATE_IDLE_SHRINKED =0;
    private static final int STATE_IDLE_ENLARGED =1;
    private static final int STATE_ANIMATING=2;
    private static final int STATE_UNITIALIZED=3;



    /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
    private static final int PREVIEW_TEX_INDEX=0;
    private static final int LUT_TEX_INDEX=PREVIEW_TEX_INDEX+1;//1
    private static final int FBO_TEX_INDEX=LUT_TEX_INDEX+1;//2
    private static final int TEX_INDEX_SUM=FBO_TEX_INDEX+1;


    protected static final int INDEX_NONE_FILTER = CameraAgent.INDEX_NONE_FILTER;

    private static final int PREVIEW_FBO_INDEX=0;
    private static final int RECORD_FBO_INDEX=PREVIEW_FBO_INDEX+1;
    private static final int FBO_INDEX_SUM=RECORD_FBO_INDEX+1;
    /* MODIFIED-END by sichao.hu,BUG-2821981*/

    private int mState=STATE_UNITIALIZED;
    private final Log.Tag TAG=new Log.Tag("BaseRender");
    private static final int LUT_WIDTH=33;
    private static final int LUT_HEIGHT=33*33;
    //private static final int LUT_WIDTH=4;
//    private static final int LUT_HEIGHT=4*4;
    public static final float SEPARATOR_WIDTH=0.05f; // MODIFIED by jianying.zhang, 2016-11-08,BUG-3255060

    private static final int ANIMATION_DURATION=300;

    protected int mPreviewTexture;
    /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
    protected int mLUTTexture;
    protected int mFBOTexture;
    protected int mTextures[];
    protected int mFBO[];
    protected Point[] mFBOSpecs;
    /* MODIFIED-END by sichao.hu,BUG-2821981*/

    protected FloatBuffer mVertexCoordBuffer;

    protected FloatBuffer mTextureCoordBuffer;
    private Context mContext;

    private float mAnimateZoomRatio =1.0f;
    private ValueAnimator mEnlargeAnimator;
    private ValueAnimator mShrinkAnimator;
    private float[] mVerticesCoords;

    private int mChosenIndex=INDEX_NONE_FILTER;



    private Bitmap[] mLUTs=new Bitmap[9];

    public BaseGLRenderer(SurfaceTexture surfaceTexture,Context context) {
        super(surfaceTexture);
        mContext=context;
        resetMatrix(mMVPMatrix);
    }


    private void loadLUT(){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inScaled=false;
        for(int i=0;i<mLUTs.length;i++){
            mLUTs[i]=BitmapFactory.decodeResource(mContext.getResources(), LUTConfiguration.LUT_INDICES[i].getId(),options);
        }
    }
    private void checkGLErrorState(){ // MODIFIED by sichao.hu, 2016-08-30,BUG-2821981
//            int error;
//            while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR){
//                Log.e(TAG, "_.checkGlError " + error);
//                throw new RuntimeException("glError " + error);
//            }
    }




    public int[] prepareTextures(){
        /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
        checkGLErrorState();
        loadLUT();
        mTextures=new int[TEX_INDEX_SUM];
        GLES20.glGenTextures(TEX_INDEX_SUM, mTextures, 0);
        checkGLErrorState();
        mPreviewTexture=mTextures[PREVIEW_TEX_INDEX];
        mLUTTexture =mTextures[LUT_TEX_INDEX];
        mFBOTexture=mTextures[FBO_TEX_INDEX];

        mFBO=new int[FBO_INDEX_SUM];
        mFBOSpecs=new Point[FBO_INDEX_SUM];
        GLES20.glGenFramebuffers(FBO_INDEX_SUM, mFBO, 0);
        return mTextures;
    }

    protected void bindPreviewTexture(int index,int pTexture){
        int glTextureIndex=GLES20.GL_TEXTURE0+index;
        checkGLErrorState();
        GLES20.glActiveTexture(glTextureIndex);//Sample SurfaceTexture populated image into uniform
        checkGLErrorState();
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextures[index]);
        checkGLErrorState();
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        checkGLErrorState();
        GLES20.glUniform1i(pTexture, index);
        checkGLErrorState();
    }

    protected void bindFBOTexture(int index,int pTexture){
        int glTextureIndex=GLES20.GL_TEXTURE0+index;

        GLES20.glActiveTexture(glTextureIndex);//Sample SurfaceTexture populated image into uniform
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[index]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glUniform1i(pTexture, index);
        /* MODIFIED-END by sichao.hu,BUG-2821981*/
    }

    protected void bindLUT(int index,int pTexture,Bitmap LUT){
        int glTextureIndex=GLES20.GL_TEXTURE0+index;

        ByteBuffer lutBuffer=ByteBuffer.allocate(LUT.getAllocationByteCount());
        LUT.copyPixelsToBuffer(lutBuffer);
        lutBuffer.position(0);
        GLES20.glActiveTexture(glTextureIndex);//Sample SurfaceTexture populated image into uniform
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[index]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, LUT_WIDTH, LUT_HEIGHT, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, lutBuffer);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glUniform1i(pTexture, index);
    }

    public int loadShader(){
        /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
        checkGLErrorState();
        prepareBuffer(false);
        int vShader=Utils.compileShader(GLES20.GL_VERTEX_SHADER, PlainShader.PREVIEW_VERTEX_SHADER);
        int fShader=Utils.compileShader(GLES20.GL_FRAGMENT_SHADER, PlainShader.PREVIEW_FRAGMENT_SHADER);
        GLES20.glAttachShader(mProgram, vShader);
        GLES20.glAttachShader(mProgram, fShader);
        GLES20.glBindAttribLocation(mProgram, 0, PlainShader.VERTEX_COORD_HANDLER);
        GLES20.glBindAttribLocation(mProgram, 1, PlainShader.TEX_COORD_HANDLER);
        GLES20.glLinkProgram(mProgram);
        checkGLErrorState();

        return mProgram;
    }

    @Override
    public void prepareBuffer(boolean isReversed){

        float[] textureCoords=Utils.calculateTextureCoordinates(PlainShader.TEX_COORD, 9);//to render 9 filters
        float[] reverseTex_coord=Utils.calculateTextureCoordinates(PlainShader.REVERSE_TEX_COORD, 9);
        mTextureCoordBuffer=Utils.convertArrayToBuffer(isReversed?reverseTex_coord:textureCoords);

    }



    protected void releaseBuffer(){
        GLES20.glDeleteFramebuffers(FBO_INDEX_SUM, mFBO, 0);
        GLES20.glDeleteTextures(TEX_INDEX_SUM, mTextures, 0);
    }



    protected int[] enableAttributes(){
        GLES20.glUseProgram(mProgram);
        int vCoordAttribute=GLES20.glGetAttribLocation(mProgram, PlainShader.VERTEX_COORD_HANDLER);
        int texCoordAttribute = GLES20.glGetAttribLocation(mProgram, PlainShader.TEX_COORD_HANDLER);

        if(mVerticesCoords==null){
            mVerticesCoords=Utils.calculateVerticesCoordinates(SEPARATOR_WIDTH);
            /* MODIFIED-END by sichao.hu,BUG-2821981*/
        }

//        mVertexCoordBuffer= Utils.convertArrayToBuffer(PlainShader.VERTEX_COORD);
        mVertexCoordBuffer= Utils.convertArrayToBuffer(mVerticesCoords);
        GLES20.glEnableVertexAttribArray(vCoordAttribute);
        GLES20.glVertexAttribPointer(vCoordAttribute, PlainShader.VERTEX_COORD_LENGTH,
                GLES20.GL_FLOAT, false,
                PlainShader.VERTEX_COORD_LENGTH * 4, mVertexCoordBuffer);
        GLES20.glEnableVertexAttribArray(texCoordAttribute);
        GLES20.glVertexAttribPointer(texCoordAttribute, PlainShader.TEX_COORD_LENGTH,
                GLES20.GL_FLOAT, false,
                PlainShader.TEX_COORD_LENGTH * 4, mTextureCoordBuffer);


        int[] attributes=new int[]{vCoordAttribute,texCoordAttribute};
        return attributes;
    }//step 2 for rendering


    /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
    protected void bindUniforms(int program,int w,int h){

        int pPreviewTextureSampler= GLES20.glGetUniformLocation(program, PlainShader.PREVIEW_SAMPLER_HANDLER);
        bindPreviewTexture(PREVIEW_TEX_INDEX, pPreviewTextureSampler);
        int pSPWidth=GLES20.glGetUniformLocation(program,PlainShader.SEPARATOR_WIDTH_HANDLER);
        GLES20.glUniform1f(pSPWidth, SEPARATOR_WIDTH);

        int pMVPMatrix=GLES20.glGetUniformLocation(program, PlainShader.MVP_MATRIX_HANDLER);
        /* MODIFIED-END by sichao.hu,BUG-2821981*/
        Log.w(TAG, "mvpMatrix index is " + pMVPMatrix);
        resetMatrix(mMVPMatrix);
        float maxRatio=2.0f/((2.0f-2*SEPARATOR_WIDTH)/3);
        Matrix.scaleM(mMVPMatrix, 0, getCurrentZoomRatio(maxRatio), getCurrentZoomRatio(maxRatio), 1.0f);
        GLES20.glUniformMatrix4fv(pMVPMatrix, 1, false, mMVPMatrix, 0);

        float animationProgress=(getCurrentZoomRatio(maxRatio)-1.0f)/(maxRatio-1.0f);

        /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
        int pAnimationProgress=GLES20.glGetUniformLocation(program,PlainShader.ANIMATION_PROGRESS_HANDLER);
        int pChosenIndex=GLES20.glGetUniformLocation(program,PlainShader.CHOSEN_INDEX_PROGRESS);
        /* MODIFIED-END by sichao.hu,BUG-2821981*/

        GLES20.glUniform1f(pAnimationProgress, animationProgress);
        GLES20.glUniform1i(pChosenIndex, mChosenIndex);


        /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
        int pLUTDimHandler=GLES20.glGetUniformLocation(program,PlainShader.LUT_DIM_HANDLER);
        float lutDim=(float)LUT_WIDTH;
        GLES20.glUniform1f(pLUTDimHandler, lutDim);

        checkGLErrorState();
        /* MODIFIED-END by sichao.hu,BUG-2821981*/

    }

    protected float getCurrentZoomRatio(float maxRatio){
        return mAnimateZoomRatio;
    }


    private float[] mMVPMatrix =new float[16];
    private void resetMatrix(float[] matrix){
        //it MUST be a 4x4 matrix
        if(matrix==null||matrix.length!=16){
            return;
        }
        Matrix.setIdentityM(matrix, 0);

    }

    private ValueAnimator.AnimatorUpdateListener mMatrixAnimatorListener =new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            mAnimateZoomRatio = (float) valueAnimator.getAnimatedValue();
        }
    };

    @Override
    /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
    public void startEnlargeAnimation(int chosenFilterIndex, final GLAnimationProxy.AnimationProgressListener listener) {
        Log.w(TAG,String.format("mState is %d , chosenFilterIndex = %d",mState,chosenFilterIndex));
        if (mState == STATE_IDLE_SHRINKED ) {
            if(chosenFilterIndex != -1){
                mChosenIndex = chosenFilterIndex;
            }
            /* MODIFIED-END by Sichao Hu,BUG-2989818*/
            /* MODIFIED-END by jianying.zhang,BUG-3255060*/
        }
        Log.w(TAG,"start enlargeAnimation");
        resetMatrix(mMVPMatrix);
        float target=2.0f/((2.0f-2*SEPARATOR_WIDTH)/3);
        if(mShrinkAnimator!=null){
            mShrinkAnimator.cancel();
        }
        if (mEnlargeAnimator != null) {
            mEnlargeAnimator.end();
        } else{
            mEnlargeAnimator = ValueAnimator.ofFloat(1.0f,target);
        }

        mEnlargeAnimator.setInterpolator(Gusterpolator.INSTANCE);
        mEnlargeAnimator.addUpdateListener(mMatrixAnimatorListener);
        mEnlargeAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                setState(STATE_ANIMATING);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(mState==STATE_ANIMATING){
                    setState(STATE_IDLE_ENLARGED);
                }
                mEnlargeAnimator.removeUpdateListener(mMatrixAnimatorListener);
                mEnlargeAnimator.removeListener(this);
                if(listener!=null) {
                    listener.onAnimationDone(mChosenIndex);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                mEnlargeAnimator.removeUpdateListener(mMatrixAnimatorListener);
                mEnlargeAnimator.removeListener(this);
                if(listener!=null) {
                    listener.onAnimationDone(mChosenIndex);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        mEnlargeAnimator.setDuration(ANIMATION_DURATION);
        mEnlargeAnimator.start();
    }
    @Override

    /* MODIFIED-BEGIN by Sichao Hu, 2016-09-23,BUG-2989818*/
    public void switchToSingleWindowImmediately(int chosenFilterIndex, GLAnimationProxy.AnimationProgressListener listener) { // MODIFIED by jianying.zhang, 2016-11-08,BUG-3255060
        Log.w(TAG, "start enlargeAnimation");
        resetMatrix(mMVPMatrix);
        float target=2.0f/((2.0f-2*SEPARATOR_WIDTH)/3);
        if(mShrinkAnimator!=null){
            mShrinkAnimator.cancel();
        }
        if (mEnlargeAnimator != null) {
            mEnlargeAnimator.end();
        }

        setState(STATE_IDLE_ENLARGED);

        /* MODIFIED-BEGIN by jianying.zhang, 2016-11-08,BUG-3255060*/
        mAnimateZoomRatio = target;
        mChosenIndex = chosenFilterIndex;
        /* MODIFIED-END by jianying.zhang,BUG-3255060*/
        if(listener!=null) {
            listener.onAnimationDone(mChosenIndex);
        }
    }

    @Override
    public void startShrinkAnimation(final GLAnimationProxy.AnimationProgressListener listener){
        Log.w(TAG, "start shrink animation"); // MODIFIED by sichao.hu, 2016-08-30,BUG-2821981
        resetMatrix(mMVPMatrix);
        float start=2.0f/((2.0f-2*SEPARATOR_WIDTH)/3);
        if(mEnlargeAnimator!=null){
            mEnlargeAnimator.cancel();
        }
        if(mShrinkAnimator!=null){
            mShrinkAnimator.end();
        }else{
            mShrinkAnimator=ValueAnimator.ofFloat(start,1.0f);
        }
        mShrinkAnimator.setInterpolator(Gusterpolator.INSTANCE);
        mShrinkAnimator.addUpdateListener(mMatrixAnimatorListener);
        mShrinkAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                setState(STATE_ANIMATING);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(mState==STATE_ANIMATING){
                    setState(STATE_IDLE_SHRINKED);
                }
                mShrinkAnimator.removeListener(this);
                mShrinkAnimator.removeUpdateListener(mMatrixAnimatorListener);

                if(listener!=null) {
                    listener.onAnimationDone(mChosenIndex);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                mShrinkAnimator.removeListener(this);
                mShrinkAnimator.removeUpdateListener(mMatrixAnimatorListener);
                if(listener!=null) {
                    listener.onAnimationDone(mChosenIndex);
                }
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        mShrinkAnimator.setDuration(ANIMATION_DURATION);
        mShrinkAnimator.start();
    }


    /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
    protected void drawFrame(int program,int width,int height){
        super.drawFrame(program, width, height);
        Log.w(TAG, "draw frame");
        int status=GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if(status!=GLES20.GL_FRAMEBUFFER_COMPLETE){
            return;//FBO not ready
        }


        GLES20.glUseProgram(program);

        if(width!=0&&height!=0) {
            mSurfaceTexture.setDefaultBufferSize(width, height);
            GLES20.glViewport(0, 0, width, height);//not necessary, it's default set
            com.android.camera.debug.Log.w(TAG, String.format("view port is %dx%d", width, height));
            if(mInputTexture!=null) {
                try {
                    mInputTexture.updateTexImage();//flush input
                } catch (Exception e) {
                    Log.e(TAG, "failed to update texture", e);
                    return;

                }
            }
        }
        checkGLErrorState();

        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);


        int pLUT_none=GLES20.glGetUniformLocation(program,PlainShader.LUT_NONE_HANDLER);
        int pNeedVignetting=GLES20.glGetUniformLocation(program,PlainShader.NEED_VIGNET_HANDLER);
        if(mState==STATE_UNITIALIZED){//don't show any texture if animation not ready
            return;
        }

        for(int i=0;i<9;i++){
            bindLUT(LUT_TEX_INDEX,pLUT_none,mLUTs[i]);
            GLES20.glUniform1i(pNeedVignetting,LUTConfiguration.LUT_INDICES[i].isNeedVignetting());
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, getFirstCoordAccordToIndex(i), 4);
        }



        checkGLErrorState();
    }

    @Override
    protected void preparePreviewFBO(int width, int height) {
        GLES20.glUseProgram(mProgram);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFBO[PREVIEW_FBO_INDEX]);
        if(mFBOSpecs[PREVIEW_FBO_INDEX]==null||mFBOSpecs[PREVIEW_FBO_INDEX].x!=width||mFBOSpecs[PREVIEW_FBO_INDEX].y!=height){

            mFBOSpecs[PREVIEW_FBO_INDEX]=new Point(width,height);
            int pFBO=GLES20.glGetUniformLocation(mProgram,PlainShader.FBO_HANDLER);
            bindFBOTexture(FBO_TEX_INDEX,pFBO);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);//set the target render target texture as an empty texture
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTextures[FBO_TEX_INDEX], 0);//bind texture as color attachment of frameBuffer
        }
    }


    @Override
    protected void renderFromFBOToWindow(int width, int height) {

        GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, 0);
        GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, mFBO[PREVIEW_FBO_INDEX]);


        GLES30.glReadBuffer(GLES30.GL_COLOR_ATTACHMENT0);
        GLES30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GLES30.GL_COLOR_BUFFER_BIT, GLES30.GL_LINEAR);

    }

    protected void copyFBOToRecorderSurface(int width, int height,int previewWidth,int mPreviewHeight){
        GLES20.glViewport(0, 0, width, height);//not necessary, it's default set
        GLES20.glClearColor(0, 0, 0, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES30.glBindFramebuffer(GLES30.GL_DRAW_FRAMEBUFFER, 0);
        GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, mFBO[PREVIEW_FBO_INDEX]);


        GLES30.glReadBuffer(GLES30.GL_COLOR_ATTACHMENT0);
        GLES30.glBlitFramebuffer(0,0,previewWidth,mPreviewHeight,0,0,width,height,GLES30.GL_COLOR_BUFFER_BIT,GLES30.GL_LINEAR);

        checkGLErrorState();
        /* MODIFIED-END by sichao.hu,BUG-2821981*/
    }


    private int getFirstCoordAccordToIndex(int index){
        return index*4;//4 vertex for each rectangle
    }

    // step 6 not supposed implemented here

    private void setState(int state){
        mState=state;
    }

}
