package com.android.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.tct.camera.R;

/**
 * Created by fanghua.gu on 12/29/15.
 */

public class ShutterSaveProgressbar extends View {

    private Paint mArcPaint = new Paint();
    private Paint mCirclePaint = new Paint();
    private RectF mCircleBounds = new RectF();
    private float mArcProgress = 0;
    private int mBarWidth;
    private boolean mIsNeedLoopValidate = false;
    private int mTimeDelay = 30;
    private final static int TIME_DELAY_AVERAGE = 10;
    private final static int TIME_DELAY_MAX = 20;
    private final static int TIME_DELAY_MIN = 5;

    private float mProgress_max = 120;
    private int mProgress_last_second = 110;
    private int mProgressStep = 10;
    private int mCurrentProgress;

    public ShutterSaveProgressbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBarWidth = (int) context.getResources().getDimension(R.dimen.panorama_circle_bar_width);
        intPaints();
    }
    
    private void intPaints() {
        mArcPaint.setColor(getResources().getColor(R.color.panorama_save_arc));
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Style.STROKE);
        mArcPaint.setStrokeWidth(mBarWidth);
        mCirclePaint.setColor(getResources().getColor(R.color.panorama_save_circle));
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStyle(Style.STROKE);
        mCirclePaint.setStrokeWidth(mBarWidth);
    }
    
    @Override
    protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
        super.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
        mCircleBounds = new RectF(mBarWidth /2, mBarWidth /2,getWidth()- mBarWidth /2,getHeight()- mBarWidth /2);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawArc(mCircleBounds, -90, 360, false, mCirclePaint);
        canvas.drawArc(mCircleBounds, -90, mArcProgress, false, mArcPaint);
        if(mIsNeedLoopValidate)
            postValidateDelay();
    }

    public void init(int max,int step){
        mProgress_max = max;
        mProgress_last_second = max - step;
        mProgressStep = step;
    }

    public void setProgress(int progress){
        if(progress - mCurrentProgress > mProgressStep /2){
            mCurrentProgress = progress;
            mTimeDelay = TIME_DELAY_MIN;
        }else if(progress - mCurrentProgress < 0){
            mTimeDelay = TIME_DELAY_MAX;
        }else{
            mTimeDelay = TIME_DELAY_AVERAGE;
            mCurrentProgress = progress;
        }
        if(progress== mProgress_last_second){
            mTimeDelay = TIME_DELAY_MIN;
        }
    }

    private void postValidateDelay(){
        if(mCurrentProgress<0.97*mProgress_max){
            mCurrentProgress++;
        }
        mArcProgress = (float) (360.0 * ( mCurrentProgress / mProgress_max));
        postInvalidateDelayed(mTimeDelay);
    }
    
    public void startPlay(){
        mIsNeedLoopValidate = true;
        mCurrentProgress=0;
        postInvalidate();
    }
    
    public void stopPlay(){
        mIsNeedLoopValidate = false;
        mCurrentProgress = 0;
        mArcProgress = 0;
    }

}