/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.gl_component;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by sichao.hu on 4/12/16.
 */
public class Utils {

    public static FloatBuffer convertArrayToBuffer(float[] array){
        ByteBuffer bb=ByteBuffer.allocateDirect(array.length*4);
        bb.order(ByteOrder.nativeOrder());

        FloatBuffer fb=bb.asFloatBuffer();
        fb.put(array);
        fb.position(0);
        return fb;
    }

    public static ShortBuffer convertArrayToBuffer(short[] array){
        ByteBuffer bb=ByteBuffer.allocateDirect(array.length*2);
        bb.order(ByteOrder.nativeOrder());

        ShortBuffer sb=bb.asShortBuffer();
        sb.put(array);
        sb.position(0);
        return sb;
    }

    public static int compileShader(int type , String source){
        int shader=GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        int[] compileResults=new int[1];
        GLES20.glCompileShader(shader);
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileResults, 0);
        if(compileResults[0]==0){
            String err = GLES20.glGetShaderInfoLog(shader);
            Log.e("ShaderError","Shader is :"+source);
            Log.e("ShaderError","error is "+err);

            throw new RuntimeException(String.format("shader compile error for %s",err));
        }
        return shader;
    }

    public static short[] calculateVertexIndices(int rectCount){
        int verticesCount=rectCount*4;//4 vertices for each rectangle
        short[] vertexIndices=new short[verticesCount];
        for(short i=0;i<verticesCount;i++){
            vertexIndices[i]=i;//because it's drawn in mode strip
        }
        return vertexIndices;
    }

    public static float[] calculateTextureCoordinates(float[] singleTextureCoords,int count){
        float[] textureCoords=new float[singleTextureCoords.length*count];
        for(int i=0;i<textureCoords.length;i+=singleTextureCoords.length){
            for(int j=0;j<singleTextureCoords.length;j++){
                textureCoords[i+j]=singleTextureCoords[j];
            }
        }
        return textureCoords;
    }

    public static float[] calculateVerticesCoordinates(float spWidth){ // MODIFIED by sichao.hu, 2016-08-30,BUG-2821981
        float[] vertexCoordinates=new float[PlainShader.VERTEX_COORD_LENGTH*36];// 9 windows with 4 vertices on each one
        for(int i=0;i<vertexCoordinates.length;){
            int coordCountInSingleWindow=3*4;//4 vertex with stride of 3
            int baseCoord=i;
            checkVertexXCoord(vertexCoordinates,baseCoord,i,spWidth);
            checkVertexYCoord(vertexCoordinates, baseCoord, i,spWidth);
            checkVertexZCoord(vertexCoordinates, baseCoord, i,spWidth);
            i+=coordCountInSingleWindow;
        }
        return vertexCoordinates;
    }

    private static void checkVertexXCoord(float[] vertexCoords, int baseCoord, int i,float spWidth){
        float windowSpec=(2.0f-spWidth*2)/3;//total width/height is 1.0-(-1.0)=2.0
        int stride=3;
        int vertexCountPerWindow=4;
        int coordCountInSingleWindow=stride*vertexCountPerWindow;//4 vertex with stride of 3
        int index=i/coordCountInSingleWindow;
        switch (index%3){//#column
            case 0://left
                vertexCoords[baseCoord]=-1.0f;//top left
                vertexCoords[baseCoord+stride]=-1.0f;//top right
                vertexCoords[baseCoord+2*stride]=-1.0f+windowSpec;//bottom left
                vertexCoords[baseCoord+3*stride]=-1.0f+windowSpec;//bottom right
                break;
            case 1://mid
                vertexCoords[baseCoord]=-1.0f+spWidth+windowSpec;
                vertexCoords[baseCoord+stride]=-1.0f+spWidth+windowSpec;
                vertexCoords[baseCoord+2*stride]=-1.0f+spWidth+2*windowSpec;
                vertexCoords[baseCoord+3*stride]=-1.0f+spWidth+2*windowSpec;
                break;
            case 2://right
                vertexCoords[baseCoord]=-1.0f+2*spWidth+2*windowSpec;
                vertexCoords[baseCoord+stride]=-1.0f+2*spWidth+2*windowSpec;
                vertexCoords[baseCoord+2*stride]=1.0f;
                vertexCoords[baseCoord+3*stride]=1.0f;
                break;
        }
    }

    private static void checkVertexYCoord(float[] vertexCoords, int baseCoord, int i,float spWidth){
        float windowSpec=(2.0f-spWidth*2)/3;//total width/height is 1.0-(-1.0)=2.0
        int stride=3;
        int vertexCountPerWindow=4;
        int coordCountInSingleWindow=stride*vertexCountPerWindow;//4 vertex with stride of 3
        int index=i/coordCountInSingleWindow;
        switch (index/3){//#row
            case 0://top
                vertexCoords[baseCoord+1]=1.0f;//top left
                vertexCoords[baseCoord+1+2*stride]=1.0f;//top right
                vertexCoords[baseCoord+1+stride]=1.0f-windowSpec;//bottom left
                vertexCoords[baseCoord+1+3*stride]=1.0f-windowSpec;//bottom right
                break;
            case 1://mid
                vertexCoords[baseCoord+1]=1.0f-spWidth-windowSpec;
                vertexCoords[baseCoord+1+2*stride]=1.0f-spWidth-windowSpec;
                vertexCoords[baseCoord+1+stride]=1.0f-spWidth-2*windowSpec;
                vertexCoords[baseCoord+1+3*stride]=1.0f-spWidth-2*windowSpec;
                break;
            case 2://bottom
                vertexCoords[baseCoord+1]=1.0f-2*spWidth-2*windowSpec;
                vertexCoords[baseCoord+1+2*stride]=1.0f-2*spWidth-2*windowSpec;
                vertexCoords[baseCoord+1+stride]=-1.0f;
                vertexCoords[baseCoord+1+3*stride]=-1.0f;
                break;
        }
    }

    private static void checkVertexZCoord(float[] vertexCoords,int baseCoord,int i,float spWidth){
        int stride=3;
        int vertexCountPerWindow=4;
        for(int j=0;j<vertexCountPerWindow;j++){
//            vertexCoords[baseCoord+2+j*stride]=0;
        }
    }
}
