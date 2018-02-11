package com.android.camera.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.android.camera.debug.Log;
import com.android.ex.camera2.portability.Size;
import com.tct.camera.R;

/**
 * Created by sichao.hu on 1/8/16.
 */
public class GestureView extends View implements PreviewStatusListener.PreviewAreaChangedListener{

    private Log.Tag TAG=new Log.Tag("GestureView");
    private Rect mGestureBound;
    private int mColor;
    private Paint mPaint;
    private RectF mPreviewArea;
    private Size mPreviewSize;
    private int mPostGestureRotation;
    private int mDisplayOrientation;
    private int mSensorOrientation;
    private boolean mPreviewMirrored;

    public GestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = getResources();
//        mColor = res.getColor(R.color.face_detect_start);
        mColor= Color.GREEN;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mColor);
        mPaint.setStrokeWidth(res.getDimension(R.dimen.face_circle_stroke));
        this.setVisibility(View.GONE);
    }


    public void showGesture(Rect bound,Size previewSize){
        this.setVisibility(View.VISIBLE);
        mGestureBound=bound;
        mPreviewSize=previewSize;
        invalidate();
    }

    public void hideGesture(){
        mGestureBound=null;
        this.setVisibility(View.GONE);
        invalidate();
    }

    public void setPostGestureRotation(int orientation){
        mPostGestureRotation =orientation;
    }

    public void setPreviewMirrored(boolean isPreviewMirrored){
        mPreviewMirrored=isPreviewMirrored;
    }

    public void setDisplayOrientation(int orientation){
        mDisplayOrientation =orientation;
    }

    public void setSensorOrientation(int orientation){
        mSensorOrientation=orientation;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mGestureBound!=null){
            Rect gestureBound=new Rect(mGestureBound);
            Log.v(TAG, "input bound is " + gestureBound);
            canvas.save();
            rotateRect(gestureBound, mPreviewArea, mPreviewMirrored);
            Log.v(TAG, "mPreviewArea is " + mPreviewArea + " canvas is " + canvas.getWidth() + "x" + canvas.getHeight());
            gestureBound.offset((int)mPreviewArea.left,(int)mPreviewArea.top);
            Log.v(TAG,"gesture bound offset is "+gestureBound);
            canvas.drawRect(gestureBound, mPaint);
            canvas.restore();
        }
        super.onDraw(canvas);
    }

    private void rotateRect(Rect gestureBound,RectF previewArea,boolean isFlipped){
        int rw, rh,sw,sh;
        rw = (int) previewArea.width();
        rh = (int) previewArea.height();
        sw=mPreviewSize.height();
        sh=mPreviewSize.width();
        // Prepare the matrix.
        if (((rh > rw) && ((mSensorOrientation == 0) || (mSensorOrientation == 180)))
                || ((rw > rh) && ((mSensorOrientation == 90) || (mSensorOrientation == 270)))) {
            int temp = rw;
            rw = rh;
            rh = temp;
            temp=sw;
            sw=sh;
            sh=temp;
        }

        Point lt=new Point(gestureBound.left,gestureBound.top);
        Point rb=new Point(gestureBound.right,gestureBound.bottom);
        Rect detectBound=new Rect(lt.x,lt.y,rb.x,rb.y);
        Log.w(TAG,String.format("post gesture rotation is %d , display rotation is %d",mPostGestureRotation,mDisplayOrientation));

        if(isFlipped){

            if(mDisplayOrientation==0){//portrait
                detectBound=new Rect(sw-rb.x,lt.y,sw-lt.x,rb.y);
            }else if(mDisplayOrientation==90) {//landscape
                detectBound = new Rect(sw - rb.y, sh - rb.x, sw - lt.y, sh - lt.x);
            }else if(mDisplayOrientation==270){
                detectBound = new Rect(lt.y,lt.x,rb.y,rb.x);
            }else{//90
                detectBound = new Rect(lt.x,sh-rb.y,rb.x,sh-lt.y);
            }
        }else{
            if(mDisplayOrientation==0){
                detectBound = new Rect(lt.x,lt.y,rb.x,rb.y);
            }else if(mDisplayOrientation ==90){
                detectBound = new Rect(sw-rb.y,lt.x,sw-lt.y,rb.x);
            }else if(mDisplayOrientation ==180){//portrait
                detectBound=new Rect(sw-rb.x,sh-rb.y,sw-lt.x,sh-lt.y);
            }else if(mDisplayOrientation ==270) {//landscape
                detectBound = new Rect(lt.y, sh - rb.x, rb.y, sh - lt.x);
            }
        }

        Log.v(TAG,String.format("rw is %d rh is %d sw is %d sh is %d",rw,rh,sw,sh));
        float scaleX=rw/(float)sw;
        float scaleY=rh/(float)sh;
        gestureBound.set((int)(detectBound.left*scaleX),(int)(detectBound.top*scaleY),(int)(detectBound.right*scaleX),(int)(detectBound.bottom*scaleY));
    }

    @Override
    public void onPreviewAreaChanged(RectF previewArea) {
        mPreviewArea=previewArea;
    }
}
