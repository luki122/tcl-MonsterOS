/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.gl_component;

/**
 * Created by sichao.hu on 9/13/16.
 */
public class PIPShader {

    public static final String MVP_MATRIX_HANDLER="mvpMatrix";
    public static final String VERTEX_COORD_HANDLER="aPosition";
    public static final String TEX_COORD_HANDLER="aTextureCoord";
    public static String PREVIEW_VERTEX_SHADER =
            /* MODIFIED-END by sichao.hu,BUG-2821981*/
            "attribute vec4 aPosition;\n"+
                    "attribute vec2 aTextureCoord;\n"+
                    "varying vec2 vTextureCoord;\n" +
                    "uniform mat4 mvpMatrix;\n"+
                    "void main() {\n" +
                    "gl_Position=mvpMatrix*aPosition;\n" +
                    "vTextureCoord = aTextureCoord;\n" +
                    "}\n";



    public static final float[] VERTEX_COORD=new float[]{
            -1.0f, 1.0f,0,//0
            -1.0f,-1.0f,0,//1
            1.0f, 1.0f,0,//2
            1.0f,-0.1f,0,//3

            -1.0f, 1.0f,0,//0
            -1.0f,-1.0f,0,//1
            1.0f, 1.0f,0,//2
            1.0f,-0.1f,0,//3
    };

    public static final float[] TEX_COORD=new float[]{
            0.0f, 1.0f,//1
            1.0f, 1.0f,//2
            0.0f, 0.0f,//3
            1.0f, 0.0f,//4

    };

    public static final float[] REVERSE_TEX_COORD=new float[]{
            1.0f, 1.0f,//2
            0.0f, 1.0f,//1
            1.0f, 0.0f,//4
            0.0f, 0.0f,//3

    };

    public static final String CIRCULAR_HANDLER="mIsCircular";
    public static final String WIDTH_HANDLER="width";
    public static final String HEIGHT_HANDLER="height";
    public static final String PREVIEW_SAMPLER_HANDLER="previewTexture";
    public static final int TRUE=1;
    public static final int FALSE=0;
    public static String PREVIEW_FRAGMENT_SHADER=
            "#extension GL_OES_EGL_image_external : require\n" +
                    "#define TRUE 1\n" +
                    "#define FALSE 0\n"+
                    "precision mediump float;\n"+
                    "varying vec2 vTextureCoord;\n" +
                    "uniform int mIsCircular;\n" +
                    "uniform samplerExternalOES previewTexture;\n" +
                    "uniform float width;\n" +
                    "uniform float height;\n" +
                    "void main(){\n" +
                    "if(mIsCircular!=TRUE){\n" +
                    "gl_FragColor=texture2D(previewTexture,vTextureCoord);\n" +
                    "return;" +
                    "}\n" +
                    "float centerX=0.5;float centerY=0.5;\n" +
                    "float coordX=abs(vTextureCoord.x-centerX)*width;\n" +
                    "float coordY=abs(vTextureCoord.y-centerY)*height;\n" +
                    "float radius=min(width,height)/2.0;" +
                    "float distance=sqrt(coordX*coordX+coordY*coordY);\n" +
                    "if(distance>radius){\n" +
                    "gl_FragColor=vec4(0,0,0,0);\n" +
                    "}else{\n" +
                    "gl_FragColor=texture2D(previewTexture,vTextureCoord);\n" +
                    "};\n";

}
