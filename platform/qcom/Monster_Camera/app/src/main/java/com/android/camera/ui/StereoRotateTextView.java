package com.android.camera.ui;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class StereoRotateTextView extends TextView {

    private static final int DEFAULT_GRADIENT_LEVEL=7;//This number stands for affected area
    private static final int MAX_LEVEL=10;//This number stands for the total canvas area

    private Camera mCamera;
    public StereoRotateTextView(Context context) {
        super(context);
        mCamera=new Camera();
    }
    
    public StereoRotateTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mCamera=new Camera();
    }
    
    public StereoRotateTextView(Context context, AttributeSet attrs,int defStyleAttr) {
        super(context, attrs,defStyleAttr);
        mCamera=new Camera();
    }
    
    public StereoRotateTextView(Context context, AttributeSet attrs,int defStyleAttr,int defStyleRes) {
        super(context, attrs,defStyleAttr,defStyleRes);
        mCamera=new Camera();
    }

    private int mDegreeY;
    private boolean mAlignLeft=true;
    private Shader mGradientShader;
    private int mCorrespondingLevel;
    public void rotateY(int degree,boolean alignLeft) {
        mDegreeY=degree;
        mAlignLeft=alignLeft;
    }

    private int mMaxRotation=0;
    private int TOLERANCE=10;
    public void setMaxRotation(int rotation){
        mMaxRotation=rotation;
    }

    private float getGradientRadius(int degree){
        int remainSlotDegree=Math.abs(Math.abs(degree)-mMaxRotation);
        float radius=remainSlotDegree/(float)mMaxRotation;
        if(Math.abs(degree)-mMaxRotation>0){
            radius=1.0f;
        }
        float gradientRadius=DEFAULT_GRADIENT_LEVEL*radius/(float)MAX_LEVEL;
        return gradientRadius;
    }

    private boolean isCloseToMaxRotation(int degree){
        return (Math.abs(Math.abs(degree)-mMaxRotation)<TOLERANCE||Math.abs(degree)>mMaxRotation);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        if (isCloseToMaxRotation(mDegreeY)) {
//            int color=this.getTextColors().getColorForState(View.EMPTY_STATE_SET,Color.WHITE);
//            int targetColor=color-0x00444444;//Make the color blacker
//            if(targetColor<0){
//                targetColor=0;
//            }
//            if(mAlignLeft) {
//                mGradientShader = new LinearGradient(0, canvas.getHeight() / 2, canvas.getWidth()*getGradientRadius(mDegreeY), canvas.getHeight() / 2, targetColor, color, Shader.TileMode.CLAMP);
//            }else{
//                mGradientShader = new LinearGradient(canvas.getWidth()*(1-getGradientRadius(mDegreeY)), canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight() / 2, color, targetColor, Shader.TileMode.CLAMP);
//            }
//            this.getPaint().setShader(mGradientShader);
//        }else{
//            this.getPaint().setShader(null);
//        }
//        Matrix matrix = new Matrix();
//        ColorDrawable colorDrawable = new ColorDrawable();
//        mCamera.save();
//        mCamera.rotateY(mDegreeY);
//        mCamera.setLocation(0, 0, canvas.getWidth());
//        mCamera.getMatrix(matrix);
//        mCamera.restore();
//        if (mAlignLeft) {
//            double radiansAngle = Math.toRadians(mDegreeY);
//            float dx = (float) canvas.getWidth() - (float) Math.cos(radiansAngle) * (float) canvas.getWidth();
//            canvas.translate(dx, 0);
//        }
//        canvas.concat(matrix);
        super.onDraw(canvas);

    }
    
    
    
    
    
}
