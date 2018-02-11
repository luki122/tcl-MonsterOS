/* Copyright (C) 2016 Tcl Corporation Limited */
package com.android.gl_component;

/**
 * Created by sichao.hu on 4/12/16.
 */
public class PlainShader {

        public static int VERTEX_COORD_LENGTH=3;
        public static final float[] VERTEX_COORD=new float[]{

                -1.0f,1.0f,0,//0
                -1.0f,0.4f,0,//1
                -0.4f,1.0f,0,//2
                -0.4f,0.4f,0,//3

                -0.3f,1.0f,0,//4
                -0.3f,0.4f,0,//5
                0.3f,1.0f,0,//6
                0.3f,0.4f,0,//7

                0.4f,1.0f,0,//8
                0.4f,0.4f,0,//9
                1.0f,1.0f,0,//10
                1.0f,0.4f,0,//11

                -1.0f,0.3f,0,//12
                -1.0f,-0.3f,0,//13,
                -0.4f,0.3f,0,//14
                -0.4f,-0.3f,0,//15

                -0.3f,0.3f,0,//16
                -0.3f,-0.3f,0,//17
                0.3f, 0.3f,0,//18
                0.3f,-0.3f,0,//19

                0.4f,0.3f,0,//20
                0.4f,-0.3f,0,//21
                1.0f,0.3f,0,//22
                1.0f,-0.3f,0,//23

                -1.0f,-0.4f,0,//24
                -1.0f,-1.0f,0,//25
                -0.4f,-0.4f,0,//26
                -0.4f,-1.0f,0,//27

                -0.3f,-0.4f,0,//28
                -0.3f,-1.0f,0,//29
                0.3f,-0.4f,0,//30
                0.3f,-1.0f,0,//31

                0.4f,-0.4f,0,//32
                0.4f,-1.0f,0,//33
                1.0f,-0.4f,0,//34
                1.0f,-1.0f,0,//35


        };

        /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
        public static final float[] RECORDER_VERTEX_COORD=new float[]{
                -1.0f, 1.0f,0,
                -1.0f,-1.0f,0,
                1.0f, 1.0f,0,
                1.0f,-1.0f,0,
        };
    /* MODIFIED-END by sichao.hu,BUG-2821981*/


        public static int TEX_COORD_LENGTH=2;
        public static final float[] TEX_COORD=new float[]{
//            0.0f, 0.0f,
//            0.0f, 1.0f,
//            1.0f, 0.0f,
//            1.0f, 1.0f,

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


        public static final String PREVIEW_SAMPLER_HANDLER="previewTexture";
        public static final String MVP_MATRIX_HANDLER="mvpMatrix";

        public static final String VERTEX_COORD_HANDLER="aPosition";
        public static final String TEX_COORD_HANDLER="aTextureCoord";
        public static final String SEPARATOR_WIDTH_HANDLER="separatorWidth";
        public static final String ANIMATION_PROGRESS_HANDLER="animationProgress";
        public static final String CHOSEN_INDEX_PROGRESS="chosenIndex";

        /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
        public static String PREVIEW_VERTEX_SHADER =
                "precision mediump float;\n"+
            /* MODIFIED-END by sichao.hu,BUG-2821981*/
                        "attribute vec4 aPosition;\n"+
                        "attribute vec2 aTextureCoord;\n"+
                        "varying vec2 vTextureCoord;\n" +
                        "uniform mat4 mvpMatrix;\n" +
                        "uniform float separatorWidth;\n" +
                        "uniform float animationProgress;\n" +//ranges from 0 to 1
                        "uniform int chosenIndex;\n" +
                        "void main() {\n" +
                        "gl_Position = mvpMatrix*aPosition;\n" +
                        "float maxRatio=2.0/((2.0-2.0*separatorWidth)/3.0);\n" +
                        "" +
                        "float finalTrans=2.0+maxRatio*separatorWidth;\n" +// the x coordinate ranges from -1.0 to 1.0 , the separator is also enlarged in the final state, so does the y coordinate
                        "float xFactor=1.0-floor(mod(float(chosenIndex),3.0));\n" +// column 0 means need transform towards (-1,0) direction ,column 1 means doesn't need transformation on x direction while column2 means need transform towards (1,0)
                        "float yFactor=floor(float(chosenIndex)/3.0)-1.0;\n" +//row 0 means need transform towards (0,1) row 3 mea
                        "float xTrans=xFactor*finalTrans*animationProgress;\n" +
                        "float yTrans=yFactor*finalTrans*animationProgress;\n" +
                        "gl_Position=vec4(gl_Position.x+xTrans,gl_Position.y+yTrans,gl_Position.z,1.0);\n" +
                        "vTextureCoord = aTextureCoord;\n" +
                        "}\n";



        public static String FBO_HANDLER="frameBuffer"; // MODIFIED by sichao.hu, 2016-08-30,BUG-2821981



        /**
         * This fragment shader is rendered for each filter windows using lookup table
         */
        public static String LUT_NONE_HANDLER ="LUT_NONE";
        public static String LUT_DIM_HANDLER="LUT_dimension";
        public static String NEED_VIGNET_HANDLER="needVignetting";
        public static int TRUE=1;
        public static int FALSE=0;
        /* MODIFIED-BEGIN by sichao.hu, 2016-08-30,BUG-2821981*/
        public static String PREVIEW_FRAGMENT_SHADER =
                "#extension GL_OES_EGL_image_external : require\n" +
                        "#define TRUE 1\n" +
                        "#define FALSE 0\n"+
                        "precision mediump float;\n"+
                        "varying vec2 vTextureCoord;\n" +
                        "uniform float LUT_dimension;\n" +
                        "uniform sampler2D frameBuffer;\n" +//Used to cache frameBuffer
            /* MODIFIED-END by sichao.hu,BUG-2821981*/
                        "uniform int needVignetting;\n" +//0 =false , 1 =true
                        "uniform sampler2D LUT_NONE;\n" +
                        "uniform samplerExternalOES previewTexture;\n" +
                        "void main() {\n" +
                        "vec4 originColor=texture2D(previewTexture,vTextureCoord);\n" +
                        "originColor=clamp(originColor, 0.0, 1.0);\n"+
                        "if(needVignetting==TRUE){\n" +
                        "float x=vTextureCoord.x;\n" +
                        "float y=vTextureCoord.y;\n" +
                        "float radiusSquare=abs((x-0.5)*(x-0.5)+(y-0.5)*(y-0.5));\n" +//The coordinate of center
                        "float maxDistanceSquare=0.5;\n" +//x=1,y=1
                        "float alpha=1.0-0.9*(radiusSquare/maxDistanceSquare);\n" +
                        "originColor=vec4(originColor.r*alpha,originColor.g*alpha,originColor.b*alpha,1.0);\n" +
                        "}\n" +
                        "float r=originColor.r;\n" +//coordinate for red in LUT,already normalized to 0-1
                        "float g=originColor.g;\n"+//coordinate for green in LUT
                        "float texelWidth=1.0/LUT_dimension;\n" +
                        "float texelHeight=1.0/(LUT_dimension*LUT_dimension);\n" +
                        "float rCoord=r*(LUT_dimension-1.0)/LUT_dimension+0.5*texelWidth;\n" + // (r * (LUT_dimension-1) + 0.5) * texelWidth
                        "float channelBlueSteps=originColor.b*(LUT_dimension-1.0);\n" +//there are 32 gaps among 33 blocks,this arithmetic is used to calculate the level normalized based on 33
                        "float bLow=floor(channelBlueSteps);\n" +//get the real Level , ranges from 0-32
                        "float bUp=bLow+1.0;\n" +//to interpolate the real pixel , sample the lower-bound and upper-bound
                        "float gCoord=(g*(LUT_dimension-1.0) + 0.5) * texelHeight;\n"+//f(g)=g*33texelHeight, G(f(g))=32*f(g)/33+0.5texelHeight=32*g*texelHeight+0.5*texelHeight
                        "vec2 lut_coord_low=vec2(rCoord,gCoord+(1.0/LUT_dimension)*bLow);\n" +
                        "vec2 lut_coord_up=vec2(rCoord,gCoord+(1.0/LUT_dimension)*bUp);\n" +
                        "vec4 colorLow=texture2D(LUT_NONE,lut_coord_low);\n" +
                        "vec4 colorUp=texture2D(LUT_NONE,lut_coord_up);\n" +
                        "float frac=fract(channelBlueSteps); //-bLow;\n" +
                        "gl_FragColor=mix(colorLow,colorUp,frac);\n" +
                        "}\n" ;





}
