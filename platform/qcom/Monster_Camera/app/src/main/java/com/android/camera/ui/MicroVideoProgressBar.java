package com.android.camera.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.tct.camera.R;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sichao.hu on 10/13/15.
 */
public class MicroVideoProgressBar extends View {

    private static final int DEFAULT_PROGRESS_COLOR=Color.argb(0xff, 0x1e, 0x88, 0xe5);
    private static final int DEFAULT_BACKGROUND_COLOR=Color.argb(0x33, 0xff, 0xff, 0xff);
    private static final int DEFAULT_UPDATING_COLOR=Color.argb(0xff,0x1e,0x88,0xe5);
    private static final int DEFAULT_MINIMUM_PROGRESS_COLOR=Color.argb(0x8a,0xff,0xff,0xff);
    private static final int DEFAULT_SEGMENT_SELECTED_COLOR=Color.argb(0xff,0xff,0x52,0x52);
    private int mProgressColor= DEFAULT_PROGRESS_COLOR;
    private int mBackgroundColor=DEFAULT_BACKGROUND_COLOR;
    private int mUpdatingColor =DEFAULT_UPDATING_COLOR;
    private int mSegmentSelectedColor=DEFAULT_SEGMENT_SELECTED_COLOR;
    private float mProgress=0;
    private int mSegmentWidth;
    private boolean mLastSegmentSelected;

    public MicroVideoProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        parseAttributeSet(attrs);
        mSegmentWidth = (int) getResources().getDimension(R.dimen.micro_segment_width);
    }

    private void parseAttributeSet(AttributeSet attrs){
        TypedArray a=this.getContext().obtainStyledAttributes(attrs, R.styleable.MicroVideoProgressBar);
        mProgressColor=a.getColor(R.styleable.MicroVideoProgressBar_progressColor,DEFAULT_PROGRESS_COLOR);
        mBackgroundColor=a.getColor(R.styleable.MicroVideoProgressBar_backgroundColor,DEFAULT_BACKGROUND_COLOR);
        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        ColorDrawable progressColor=new ColorDrawable(mProgressColor);
        ColorDrawable backgroundColor=new ColorDrawable(mBackgroundColor);
        ColorDrawable segmentColor=new ColorDrawable(mBackgroundColor);
        int width=canvas.getWidth();
        int height=canvas.getHeight();
        if(mProgress==0){
            backgroundColor.setBounds(0,0,width,height);
            backgroundColor.draw(canvas);
        }else{

            int firstSegment=0-mSegmentWidth;
            float lastSegment=0;
            if(mSegmentStarts.size()>0){
                lastSegment=mSegmentStarts.get(mSegmentStarts.size()-1);
            }
            int progressBound=(int)(mProgress*width/mProgressUpperBound);
            int lastSegmentBound=(int)(lastSegment*width/mProgressUpperBound);
            backgroundColor.setBounds(progressBound, 0, width, height);
            backgroundColor.draw(canvas);

            for(float segmentStart : mSegmentStarts){
                if (mLastSegmentSelected && segmentStart == lastSegment) {
                    progressColor=new ColorDrawable(mSegmentSelectedColor);
                }
                int start=(int)(segmentStart*width/mProgressUpperBound);
                progressColor.setBounds(firstSegment+mSegmentWidth, 0, start, height);
                progressColor.draw(canvas);
                if (start != lastSegmentBound) {
                    segmentColor.setBounds(start,0,start+mSegmentWidth,height);
                    segmentColor.draw(canvas);
                }
                firstSegment = start;
            }

            ColorDrawable updatingColor = new ColorDrawable(mUpdatingColor);
            int updatingStart = (lastSegmentBound == 0 ? 0 : lastSegmentBound+mSegmentWidth);
            if (updatingStart < progressBound) {
                updatingColor.setBounds(updatingStart, 0, progressBound, height);
            }
            updatingColor.draw(canvas);
        }
        if (mProgress > 0 && mProgress < mProgressLowerBound) {
            ColorDrawable lowerBoundColor=new ColorDrawable(DEFAULT_MINIMUM_PROGRESS_COLOR);
            int start=(int)(mProgressLowerBound*width/mProgressUpperBound);
            lowerBoundColor.setBounds(start,0,start+mSegmentWidth,height);
            lowerBoundColor.draw(canvas);
        }
        canvas.restore();
        super.onDraw(canvas);
        mLastSegmentSelected = false;
    }

    /**
     * Update progress from 0 to progressUpperBound which is prior set in {@link #setProgressUpperBound(float)},default for 100
     * @param progress
     */
    public void updateProgress(float progress){
        if(progress>mProgressUpperBound){
            progress=mProgressUpperBound;
        }
        if(progress<0){
            progress=0;
        }
        mProgress=progress;
        invalidate();
    }

    List<Float> mSegmentStarts =new LinkedList<>();
    public void markSegmentStart(float duration){
        if(duration<0){
            duration=0;
        }
        if(duration>=mProgressUpperBound){
            duration=mProgressUpperBound;
        }
        float lastSegmentStart=0;
        if(mSegmentStarts.size()>0){
            lastSegmentStart=mSegmentStarts.get(mSegmentStarts.size()-1);
        }

        float currentStart=lastSegmentStart+duration;
        if(lastSegmentStart+duration>=mProgressUpperBound){
            currentStart=mProgressUpperBound;
        }
        mSegmentStarts.add(currentStart);
        mProgress=currentStart;
        invalidate();
    }

    public float getSumDuration(){
        if(mSegmentStarts.size()>0){
            return mSegmentStarts.get(mSegmentStarts.size()-1);
        }
        return 0;
    }

    public float segmentRemove(){
        float removedStart=0;
        if(mSegmentStarts.size()>0){
            removedStart=mSegmentStarts.remove(mSegmentStarts.size()-1);
            if(mSegmentStarts.size()>0){
                mProgress= mSegmentStarts.get(mSegmentStarts.size()-1);
            }else{
                mProgress=0;
            }
        }else{
            mProgress=0;
        }
        invalidate();
        return removedStart-mProgress;
    }

    public void clearProgress(){
        mProgress=0;
        mSegmentStarts.clear();
        invalidate();
    }

    private float mProgressUpperBound=100;
    public void setProgressUpperBound(float upperBound){
        if(upperBound<=0){
            throw new RuntimeException("Invalid upper bound for ProgressBar");
        }
        mProgressUpperBound=upperBound;
    }

    private float mProgressLowerBound=0;
    public void setProgressLowerBound(float lowerBound){
        if(lowerBound<=0){
            throw new RuntimeException("Invalid lower bound for ProgressBar");
        }
        mProgressLowerBound=lowerBound;
    }

    public void clearPendingProgress(){
        if(mSegmentStarts.size()==0){
            mProgress=0;
        }else {
            mProgress = mSegmentStarts.get(mSegmentStarts.size() - 1);
        }
        invalidate();
    }

    public void changeLastSegmentColor() {
        mLastSegmentSelected = true;
        invalidate();
    }

}
