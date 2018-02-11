/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.gl_component;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.support.annotation.NonNull;

import com.android.camera.debug.Log;

import java.nio.FloatBuffer;

/**
 * Created by sichao.hu on 9/12/16.
 */
public class PIPGLRenderer extends GLRenderer  implements  IDragable{

    private static final Log.Tag TAG=new Log.Tag("PIPRenderer");

    private static final int TEXTURE_FULL=0;
    private static final int TEXTURE_CIRCULAR=TEXTURE_FULL+1;//1
    private static final int TEXTURE_SUM=TEXTURE_CIRCULAR+1;//2
    private float[] mMVPMatrix=new float[16];
    protected FloatBuffer mVertexCoordBuffer;
    protected FloatBuffer mTextureCoordBuffer;
    protected int mTextures[];

    public PIPGLRenderer(SurfaceTexture surfaceTexture) {
        super(surfaceTexture);
        Matrix.setIdentityM(mMVPMatrix, 0);
    }

    @Override
    public int[] prepareTextures() {
        mTextures=new int[TEXTURE_SUM];
        GLES20.glGenTextures(TEXTURE_SUM, mTextures, 0);
        return mTextures;
    }

    @Override
    public int loadShader() {
        prepareBuffer(false);
        int vShader=Utils.compileShader(GLES20.GL_VERTEX_SHADER, PIPShader.PREVIEW_VERTEX_SHADER);
        int fShader=Utils.compileShader(GLES20.GL_FRAGMENT_SHADER, PIPShader.PREVIEW_FRAGMENT_SHADER);
        GLES20.glAttachShader(mProgram, vShader);
        GLES20.glAttachShader(mProgram, fShader);
        GLES20.glBindAttribLocation(mProgram, 0, PIPShader.VERTEX_COORD_HANDLER);
        GLES20.glBindAttribLocation(mProgram, 1, PIPShader.TEX_COORD_HANDLER);
        GLES20.glLinkProgram(mProgram);
        return mProgram;
    }

    @Override
    public void prepareBuffer(boolean isReversed) {
        float[] textureCoords=Utils.calculateTextureCoordinates(PIPShader.TEX_COORD, 2);//to render 2 preview window
        float[] reverseTex_coord=Utils.calculateTextureCoordinates(PIPShader.REVERSE_TEX_COORD, 2);
        mTextureCoordBuffer=Utils.convertArrayToBuffer(isReversed?reverseTex_coord:textureCoords);
    }

    @Override
    protected void releaseBuffer() {
        //Nothing to release override super
    }

    @Override
    protected int[] enableAttributes() {
        GLES20.glUseProgram(mProgram);
        int vCoordAttribute=GLES20.glGetAttribLocation(mProgram, PlainShader.VERTEX_COORD_HANDLER);
        int texCoordAttribute = GLES20.glGetAttribLocation(mProgram, PlainShader.TEX_COORD_HANDLER);

        float[] vertexCoord=PIPShader.VERTEX_COORD;

        mVertexCoordBuffer= Utils.convertArrayToBuffer(vertexCoord);
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
    }

    //public static final String MVP_MATRIX_HANDLER="mvpMatrix";
//    public static final String CIRCULAR_HANDLER="mIsCircular";
//    public static final String WIDTH_HANDLER="width";
//    public static final String HEIGHT_HANDLER="height";
//    public static final String PREVIEW_SAMPLER_HANDLER="previewTexture";
    @Override
    protected void bindUniforms(int program, int w, int h) {
        int pWidth=GLES20.glGetUniformLocation(mProgram,PIPShader.WIDTH_HANDLER);
        int pHeight=GLES20.glGetUniformLocation(mProgram,PIPShader.HEIGHT_HANDLER);
        int pMVPMatrix=GLES20.glGetUniformLocation(mProgram,PIPShader.MVP_MATRIX_HANDLER);

        GLES20.glUniform1f(pWidth, (float) w);
        GLES20.glUniform1f(pHeight, (float) h);
        GLES20.glUniformMatrix4fv(pMVPMatrix, 1, false, mMVPMatrix, 0);

    }

    protected void bindPreviewTexture(int index,int pTexture){
        int glTextureIndex=GLES20.GL_TEXTURE0+index;
        GLES20.glActiveTexture(glTextureIndex);//Sample SurfaceTexture populated image into uniform
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextures[index]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glUniform1i(pTexture, index);
    }

    @Override
    protected void drawFrame(int program, int width, int height) {
        super.drawFrame(program, width, height);
        GLES20.glUseProgram(mProgram);
        GLES20.glViewport(0, 0, width, height);
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        if(mInputTexture!=null) {
            try {
                mInputTexture.updateTexImage();//flush input
            } catch (Exception e) {
                Log.e(TAG, "failed to update texture", e);
                return;

            }
        }

        int pPreviewTex=GLES20.glGetUniformLocation(mProgram,PIPShader.PREVIEW_SAMPLER_HANDLER);
        int pCircular=GLES20.glGetUniformLocation(mProgram,PIPShader.CIRCULAR_HANDLER);

        bindPreviewTexture(mTextures[TEXTURE_FULL], pPreviewTex);
        GLES20.glUniform1i(pCircular,PIPShader.FALSE);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        bindPreviewTexture(mTextures[TEXTURE_CIRCULAR], pPreviewTex);
        GLES20.glUniform1i(pCircular, PIPShader.TRUE);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 4, 4);//

    }

    @Override
    public void Drag(@NonNull Rect dragArea,@NonNull Rect fullArea) {
        int transX=dragArea.left;
        int transY=dragArea.top;
        int dragLong=Math.max(dragArea.width(),dragArea.height());
        int fullLong=Math.max(fullArea.width(),fullArea.height());
        float scale=dragLong/(float)fullLong;
        Matrix.setIdentityM(mMVPMatrix,0);
        Matrix.scaleM(mMVPMatrix,0,scale,scale,1.0f);
        Matrix.translateM(mMVPMatrix,0,transX,transY,0);
    }
}
